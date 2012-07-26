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
package cc.redberry.core.transformations;

import cc.redberry.concurrent.OutputPort;
import cc.redberry.core.context.ContextManager;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Expand implements Transformation {

    private final Indicator<Tensor> indicator;
    private final int threads;

    public Expand(Indicator<Tensor> indicator, int threads) {
        this.indicator = indicator;
        this.threads = threads;
    }

    public Expand() {
        this.indicator = Indicator.TRUE_INDICATOR;
        this.threads = 1;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return expand(tensor, indicator, new Transformation[0], threads);
    }

    public static Tensor expand(Tensor tensor) {
        return expand(tensor, Indicator.TRUE_INDICATOR, new Transformation[0], 1);
    }

    public static Tensor expand(Tensor tensor, int threads) {
        return expand(tensor, Indicator.TRUE_INDICATOR, new Transformation[0], threads);
    }

    public static Tensor expand(Tensor tensor, Transformation... transformations) {
        return expand(tensor, Indicator.TRUE_INDICATOR, transformations, 1);
    }

    public static Tensor expand(Tensor tensor, Transformation[] transformations, int threads) {
        return expand(tensor, Indicator.TRUE_INDICATOR, transformations, threads);
    }

    public static Tensor expand(Tensor tensor, Indicator<Tensor> indicator, Transformation[] transformations, int threads) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        TraverseState state;
        Tensor current;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            current = iterator.current();
            if (current instanceof Product && indicator.is(current))
                iterator.set(expandProductOfSums(current, indicator, transformations, threads));
            else if (current instanceof Power && current.get(0) instanceof Sum
                    && TensorUtils.isNatural(current.get(1)) && indicator.is(current))
                iterator.set(expandPower((Sum) current.get(0), ((Complex) current.get(1)).getReal().intValue(), transformations, threads));
        }
        return iterator.result();
    }

    private static Tensor expandProductOfSums(Tensor current, Indicator<Tensor> indicator, Transformation[] transformations, final int threads) {

        // g_mn  {_m->^a, _n->_b}  => g^a_b  <=> d^a_b |           Tensors.isMetric(g_ab )  d_a^b

        // a*b | a_m*b_v | (a+b*f) | (a_i+(c+2)*b_i) 

        //1: a*b | (a+b*f)  => result1 = a**2*b+b**2*a*f    Tensors.multiplyAndExpand(nonSum, Sum)
        //2: a_m*b_v | (a_i+(c+2)*b_i)   => result2 = a_m*b_v*a_i+(c+2)*a_m*b_v*b_i
        //3: result1 * result2                              Tensors.multiplyAndExpand(scalarSum, nonScalarSum)
        // (a_m^m**2*b+b**2*a*a_m^m) * (a_m^m*a_m*b_v*a_i+a_m^m**2*B_avi+(c+2)*a_m*b_v*b_i) => (.....)*a_m*b_v*a_i +(....)*B_avi + ( .....  )*a_m*b_v*b_i 

        ArrayList<Tensor> indexlessNonSums = new ArrayList<>();
        ArrayList<Tensor> nonSums = new ArrayList<>();

        Sum indexlessSum = null;
        Sum sum = null;

        int i;
        Tensor t, temp;
        boolean expand = false;
        for (i = current.size() - 1; i >= 0; --i) {
            t = current.get(i);
            if (t.getIndices().size() == 0)
                if (t instanceof Sum)
                    if (indexlessSum == null)
                        indexlessSum = (Sum) t;
                    else {
                        temp = expandPairOfSumsConcurrent((Sum) t, indexlessSum, transformations, threads);
                        expand = true;
                        if (temp instanceof Sum)
                            indexlessSum = (Sum) temp;
                        else {
                            indexlessNonSums.add(temp);
                            indexlessSum = null;
                        }
                    }
                else
                    indexlessNonSums.add(t);
            else if (t instanceof Sum)
                if (sum == null)
                    sum = (Sum) t;
                else {
                    temp = expandPairOfSumsConcurrent((Sum) t, sum, transformations, threads);
                    temp = expand(temp, indicator, transformations, threads);
                    expand = true;
                    if (temp instanceof Sum)
                        sum = (Sum) temp;
                    else {
                        nonSums.add(temp);
                        sum = null;
                    }
                }
            else
                nonSums.add(t);
        }

        if (!expand && sum == null)
            if (indexlessSum == null || (indexlessNonSums.isEmpty()))
                return current;


        Tensor indexless = multiply(indexlessNonSums.toArray(new Tensor[indexlessNonSums.size()]));
        if (indexlessSum != null)
            indexless = multiplySumElementsOnFactor(indexlessSum, indexless);

        Tensor main = multiply(nonSums.toArray(new Tensor[nonSums.size()]));
        if (sum != null)
            main = multiplySumElementsOnFactor(sum, multiply(nonSums.toArray(new Tensor[nonSums.size()])));

        if (main instanceof Sum)
            main = multiplySumElementsOnFactorAndExpandScalars((Sum) main, indexless);
        else
            main = multiply(indexless, main);

        return main;
    }

    private static Tensor expandPower(Sum argument, int power, Transformation[] transformations, final int threads) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
        Set<Integer> initialDummy = TensorUtils.getAllIndicesNames(argument);
        int[] initialForbidden = new int[initialDummy.size()];
        i = -1;
        for (Integer index : initialDummy)
            initialForbidden[++i] = index;
        IndexMapper mapper = new IndexMapper(initialForbidden);//(a_m^m+b_m^m)^30  
        for (i = power - 1; i >= 1; --i)
            temp = expandPairOfSumsConcurrent((Sum) temp, (Sum) renameDummy(argument, mapper), transformations, threads);
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
                    iterator.set(Tensors.setIndicesToField((TensorField) simpleTensor, newIndices));
                else
                    iterator.set(Tensors.setIndicesToSimpleTensor(simpleTensor, newIndices));
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

    public static final class ExpandPairPort implements OutputPort<Tensor> {

        private final Tensor sum1, sum2;
        private final AtomicLong atomicLong = new AtomicLong();

        public ExpandPairPort(Sum s1, Sum s2) {
            sum1 = s1;
            sum2 = s2;
        }

        @Override
        public Tensor take() {
            long index = atomicLong.getAndIncrement();
            if (index >= sum1.size() * sum2.size())
                return null;
            int i1 = (int) (index / sum2.size());
            int i2 = (int) (index % sum2.size());
            return Tensors.multiply(sum1.get(i1), sum2.get(i2));
        }
    }

    public static Tensor expandPairOfSums(Sum s1, Sum s2) {
        return expandPairOfSums(s1, s2, new Transformation[0]);
    }

    public static Tensor expandPairOfSums(Sum s1, Sum s2, Transformation[] transformations) {
        ExpandPairPort epp = new ExpandPairPort(s1, s2);
        TensorBuilder sum = new SumBuilder();
        Tensor t;
        while ((t = epp.take()) != null) {
            for (Transformation transformation : transformations)
                t = transformation.transform(t);
            sum.put(t);
        }
        return sum.build();
    }

    public static Tensor expandPairOfSumsConcurrent(final Sum s1, final Sum s2, int threads) {
        return expandPairOfSumsConcurrent(s1, s2, new Transformation[0], threads);
    }

    public static Tensor expandPairOfSumsConcurrent(final Sum s1, final Sum s2, Transformation[] transformations, final int threads) {
        if (threads == 1)
            return expandPairOfSums(s1, s2, transformations);
        Future[] futures = new Future[threads];
        ExpandPairPort epp = new ExpandPairPort(s1, s2);
        TensorBuilder sum = new SumBuilder();

        for (int i = 0; i < threads; ++i)
            futures[i] = ContextManager.getExecutorService().submit(new Worker(epp, sum, transformations));

        try {
            for (Future future : futures)
                future.get();
            return sum.build();
        } catch (ExecutionException | InterruptedException ee) {
            throw new RuntimeException(ee);
        }
    }

    private static final class Worker implements Runnable {

        private final ExpandPairPort epp;
        private final TensorBuilder builder;
        private final Transformation[] transformations;

        public Worker(ExpandPairPort epp, TensorBuilder builder, Transformation[] transformations) {
            this.epp = epp;
            this.builder = builder;
            this.transformations = transformations;
        }

        @Override
        public void run() {
            Tensor term;
            while ((term = epp.take()) != null) {
                for (Transformation transformation : transformations)
                    term = transformation.transform(term);
                builder.put(term);
            }
        }
    }
}
