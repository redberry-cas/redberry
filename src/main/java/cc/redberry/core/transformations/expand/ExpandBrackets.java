/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.transformations.expand;

import cc.redberry.core.indexgenerator.*;
import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;
import java.util.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandBrackets implements Transformation {

    private final Indicator<Tensor> indicator;
    private final int threads;

    public ExpandBrackets(Indicator<Tensor> indicator, int threads) {
        this.indicator = indicator;
        this.threads = threads;
    }

    public ExpandBrackets() {
        this.indicator = Indicator.TRUE_INDICATOR;
        this.threads = 1;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return expandBrackets(tensor, indicator, threads);
    }

    public static Tensor expandBrackets(Tensor tensor) {
        return expandBrackets(tensor, Indicator.TRUE_INDICATOR, 1);
    }

    public static Tensor expandBrackets(Tensor tensor, int threads) {
        return expandBrackets(tensor, Indicator.TRUE_INDICATOR, threads);
    }

    public static Tensor expandBrackets(Tensor tensor, Indicator<Tensor> indicator, int threads) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        TraverseState state;
        Tensor current;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            current = iterator.current();
            if (current instanceof Product && indicator.is(current))
                iterator.set(expandProductOfSums(current, threads));
            if (current instanceof Power && current.get(0) instanceof Sum
                    && TensorUtils.isInteger(current.get(1)) && indicator.is(current))
                iterator.set(expandPower((Sum) current.get(0), ((Complex) current.get(1)).getReal().intValue(), threads));
        }
        return iterator.result();
    }

    private static Tensor expandProductOfSums(Tensor current, final int threads) {
        ArrayDeque<Sum> indexlessSums = new ArrayDeque<>();
        ArrayDeque<Sum> sums = new ArrayDeque<>();
        ArrayList<Tensor> indexlessNonSums = new ArrayList<>();
        ArrayList<Tensor> nonSums = new ArrayList<>();
        int i;
        Tensor t;
        for (i = current.size() - 1; i >= 0; --i) {
            t = current.get(i);
            if (t instanceof Sum)
                if (t.getIndices().size() == 0)
                    indexlessSums.push((Sum) t);
                else
                    sums.push((Sum) t);
            else if (t.getIndices().size() == 0)
                indexlessNonSums.add(t);
            else
                nonSums.add(t);
        }

        if (sums.isEmpty() && indexlessSums.isEmpty())
            return current;

        Sum s1, s2;
        Tensor temp;
        while (sums.size() > 1) {
            s1 = sums.poll();
            s2 = sums.poll();
            temp = ExpandUtils.expandPairOfSumsConcurrent(s1, s2, threads);
            if (temp instanceof Sum)
                sums.add((Sum) temp);
            else
                nonSums.add(temp);
        }
        while (indexlessSums.size() > 1) {
            s1 = indexlessSums.poll();
            s2 = indexlessSums.poll();
            temp = ExpandUtils.expandPairOfSumsConcurrent(s1, s2, threads);
            if (temp instanceof Sum)
                indexlessSums.add((Sum) temp);
            else
                indexlessNonSums.add(temp);
        }

        //processing indexless
        if (indexlessSums.isEmpty())
            if (indexlessNonSums.isEmpty())
                t = null;
            else
                t = UnsafeTensors.unsafeMultiplyWithoutIndicesRenaming(indexlessNonSums.toArray(new Tensor[indexlessNonSums.size()]));
        else {
            Sum indexlessSum = indexlessSums.peek();
            Tensor[] newSum = new Tensor[indexlessSum.size()];
            for (i = indexlessSum.size() - 1; i >= 0; --i)
                newSum[i] = multiply(indexlessNonSums, indexlessSum.get(i));
            t = UnsafeTensors.unsafeSumWithouBuilder(newSum);
        }

        //processing part with free indices
        if (sums.isEmpty())
            if (nonSums.isEmpty()) {
                assert t != null;
                return t;
            } else
                if (t != null)
                    return multiply(nonSums, t);
                else
                    return UnsafeTensors.unsafeMultiplyWithoutIndicesRenaming(nonSums.toArray(new Tensor[nonSums.size()]));
        else {
            Sum sum = sums.peek();
            Tensor[] newSum = new Tensor[sum.size()];
            for (i = sum.size() - 1; i >= 0; --i) {
                temp = multiply(nonSums, sum.get(i));
                if (t != null)
                    temp = expandProductOfSums(UnsafeTensors.unsafeMultiplyWithoutIndicesRenaming(t, temp), threads);
                newSum[i] = temp;
            }
            return UnsafeTensors.unsafeSumWithouBuilder(newSum);
        }
    }

    private static Tensor multiply(ArrayList<Tensor> list, Tensor tensor) {
        if (list.isEmpty())
            return tensor;
        ProductBuilder builder = new ProductBuilder();
        for (Tensor t : list)
            builder.put(t);
        builder.put(tensor);
        return builder.build();
    }

    private static Tensor expandPower(Sum argument, int power, final int threads) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
        Set<Integer> initialDummy = TensorUtils.getAllIndices(argument);
        int[] initialForbidden = new int[initialDummy.size()];
        i = -1;
        for (Integer index : initialDummy)
            initialForbidden[++i] = index;
        IndexMapper mapper = new IndexMapper(initialForbidden);
        for (i = power - 1; i >= 1; --i)
            temp = ExpandUtils.expandPairOfSumsConcurrent((Sum) temp, (Sum) renameDummy(argument, mapper), threads);
        return temp;

    }

    private static Tensor renameDummy(Tensor tensor, IndexMapper mapper) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        SimpleIndices oldIndices, newIndices;
        SimpleTensor simpleTensor;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;

            if (!(iterator.current() instanceof SimpleTensor))
                continue;
            simpleTensor = (SimpleTensor) iterator.current();
            oldIndices = simpleTensor.getIndices();
            newIndices = oldIndices.applyIndexMapping(mapper);
            if (oldIndices != newIndices)
                if (simpleTensor instanceof TensorField)
                    iterator.set(UnsafeTensors.unsafeSetIndicesToField((TensorField) simpleTensor, newIndices));
                else
                    iterator.set(UnsafeTensors.unsafeSetIndicesToSimpleTensor(simpleTensor, newIndices));
        }
        return iterator.result();
    }

    private static final class IndexMapper implements IndexMapping {

        private final IndexGenerator generator;
        private final Map<Integer, Integer> map;

        public IndexMapper(int[] initialUsed) {
            generator = new IndexGenerator(initialUsed);
            map = new HashMap<>(initialUsed.length);
        }

        @Override
        public int map(int from) {
            Integer to = map.get(IndicesUtils.getNameWithType(from));
            if (to == null)
                map.put(from, to = generator.generate(IndicesUtils.getType(from)));
            return IndicesUtils.getRawStateInt(from) ^ to;
        }
    }
}
