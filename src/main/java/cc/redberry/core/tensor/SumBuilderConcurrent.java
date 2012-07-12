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
package cc.redberry.core.tensor;

import cc.redberry.concurrent.ConcurrentGrowingList;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumBuilderConcurrent implements TensorBuilder {

    private final ConcurrentHashMap<Integer, ConcurrentGrowingList<FactorNode>> summands;
    private final ThreadLocal<ConcurrentGrowingList<FactorNode>> threadLocalList = new ThreadLocal<ConcurrentGrowingList<FactorNode>>() {

        @Override
        protected ConcurrentGrowingList<FactorNode> initialValue() {
            return new ConcurrentGrowingList<>();
        }
    };
    private final AtomicReference<Indices> atomicIndices = new AtomicReference<>(null);
    private final AtomicReference<Complex> atomicComplex = new AtomicReference<>(Complex.ZERO);

    public SumBuilderConcurrent(int initialCapacity) {
        summands = new ConcurrentHashMap<>(initialCapacity);
    }

    public SumBuilderConcurrent() {
        summands = new ConcurrentHashMap<>(7);
    }

    @Override
    public Tensor build() {
        Complex complex = atomicComplex.get();
        if (complex.isNaN() || complex.isInfinite())
            return complex;

        List<Tensor> sum = new ArrayList<>();
        if (!complex.isZero())
            sum.add(complex);

        FactorNode node;
        for (Map.Entry<Integer, ConcurrentGrowingList<FactorNode>> entry : summands.entrySet()) {
            ConcurrentGrowingList<FactorNode>.GrowingIterator gi = entry.getValue().iterator();
            while ((node = gi.next()) != null) {
                Tensor summand = Tensors.multiply(node.builder.build(), node.factor);//for performance
                if (!TensorUtils.isZero(summand))
                    sum.add(summand);
            }
        }

        if (sum.isEmpty())
            return complex;
        if (sum.size() == 1)
            return sum.get(0);

        return new Sum(sum.toArray(new Tensor[sum.size()]), atomicIndices.get());
    }

    @Override
    public void put(final Tensor tensor) {
        if (TensorUtils.isZero(tensor))
            return;

        Indices currentIndices = tensor.getIndices().getFreeIndices();
        if (atomicIndices.get() == null)
            atomicIndices.compareAndSet(null, IndicesFactory.createSorted(currentIndices));
        if (!atomicIndices.get().equalsRegardlessOrder(currentIndices))
            throw new TensorException("Inconsinstent indices in sum.", tensor);

        if (tensor instanceof Sum) {
            for (Tensor s : tensor)
                put(s);
            return;
        }

        if (tensor instanceof Complex) {
            Complex oldVal, newVal, toAdd = (Complex) tensor;
            do {
                oldVal = atomicComplex.get();
                newVal = oldVal.add(toAdd);
            } while (!atomicComplex.compareAndSet(oldVal, newVal));
            return;
        }

        Split split = Split.split(tensor, true);

        Integer hash = split.factor.hashCode();

        //List with factors with the same hashes
        ConcurrentGrowingList<FactorNode> growingList;

        //Chached instance
        ConcurrentGrowingList<FactorNode> newList = threadLocalList.get();
        growingList = summands.putIfAbsent(hash, newList);

        if (growingList == null) { //If new list was putted successfuly       
            growingList = newList;
            threadLocalList.remove();
        }

        ConcurrentGrowingList<FactorNode>.GrowingIterator iterator = growingList.iterator();

        FactorNode newFactorNode = null;
        FactorNode current;
        while (true) {
            current = iterator.next();
            if (current == null) {
                if (newFactorNode == null)
                    newFactorNode = new FactorNode(split.factor, split.getBuilder());
                if ((current = iterator.set(newFactorNode)) == null)
                    break;
            }

            Boolean b = SumBuilder.compareFactors(split.factor, current.factor);
            if (b == null)
                continue;
            if (b)
                current.builder.put(Tensors.negate(split.summand));
            else
                current.builder.put(split.summand);
            break;
        }
    }
}
