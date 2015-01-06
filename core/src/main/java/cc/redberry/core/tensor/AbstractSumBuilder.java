/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorHashCalculator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.transformations.ToNumericTransformation.toNumeric;

/**
 * Abstract implementation of {@link TensorBuilder} for sums.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public abstract class AbstractSumBuilder implements TensorBuilder {

    final TIntObjectHashMap<List<FactorNode>> summands;
    Complex complex = Complex.ZERO;
    Indices indices = null;
    int[] sortedFreeIndices;

    /**
     * Creates builder with default initial capacity.
     */
    public AbstractSumBuilder() {
        this(7);
    }

    /**
     * Creates builder with specified initial capacity.
     *
     * @param initialCapacity initial capacity
     */
    public AbstractSumBuilder(int initialCapacity) {
        summands = new TIntObjectHashMap<>(initialCapacity);
    }

    AbstractSumBuilder(TIntObjectHashMap<List<FactorNode>> summands, Complex complex, Indices indices, int[] sortedFreeIndices) {
        this.summands = summands;
        this.complex = complex;
        this.indices = indices;
        this.sortedFreeIndices = sortedFreeIndices;
    }

    @Override
    public Tensor build() {
        if (complex.isNaN() || complex.isInfinite())
            return complex;

        List<Tensor> sum = new ArrayList<>();

        final boolean isNumeric = complex.isNumeric();
        for (List<FactorNode> nodes : summands.valueCollection())
            for (FactorNode node : nodes) {
                if (isNumeric) {
                    Tensor summand = Tensors.multiply(toNumeric(node.build()), toNumeric(node.factor));
                    if (summand instanceof Complex)
                        complex = complex.add((Complex) summand);
                    else
                        sum.add(summand);
                } else {
                    Tensor summand = Tensors.multiply(node.build(), node.factor);
                    if (!TensorUtils.isZero(summand))
                        sum.add(summand);
                }
            }

        if (sum.isEmpty())
            return complex;

        if (!complex.isZero())
            sum.add(complex);

        if (sum.size() == 1)
            return sum.get(0);

        return new Sum(sum.toArray(new Tensor[sum.size()]), indices);
    }

    protected abstract Split split(Tensor tensor);

    @Override
    public void put(Tensor tensor) {
        if (complex.isNaN())
            return;
        if (complex.isNumeric())
            tensor = toNumeric(tensor);
        if (TensorUtils.isZero(tensor))
            return;
        if (TensorUtils.isIndeterminate(tensor)) {
            complex = complex.add((Complex) tensor);
            return;
        }
        if (complex.isInfinite()) {
            if (tensor instanceof Complex)
                complex.add((Complex) tensor);
            return;
        }
        if (indices == null) {
            indices = IndicesFactory.create(tensor.getIndices().getFree());
            sortedFreeIndices = indices.getAllIndices().copy();
            Arrays.sort(sortedFreeIndices);
        } else if (!indices.equalsRegardlessOrder(tensor.getIndices().getFree()))
            throw new TensorException("Inconsistent indices in sum. " +
                    "Expected: " + indices + " Actual: " + tensor.getIndices().getFree(), tensor);//TODO improve message
        if (tensor instanceof Sum) {
            for (Tensor s : tensor)
                put(s);
            return;
        }
        if (tensor instanceof Complex) {
            complex = complex.add((Complex) tensor);
            return;
        }

        Split split = split(tensor);

        Integer hash = TensorHashCalculator.hashWithIndices(split.factor, sortedFreeIndices);//=split.factor.hashCode();
        List<FactorNode> factorNodes = summands.get(hash);
        if (factorNodes == null) {
            List<FactorNode> fns = new ArrayList<>();
            fns.add(new FactorNode(split.factor, split.getBuilder()));
            summands.put(hash, fns);
        } else {
            Boolean b = null;
            for (FactorNode node : factorNodes)
                if ((b = compareFactors(split.factor, node.factor)) != null) {
                    if (b)
                        node.put(Tensors.negate(split.summand), split.factor);
                    else
                        node.put(split.summand, split.factor);
                    break;
                }
            if (b == null)
                factorNodes.add(new FactorNode(split.factor, split.getBuilder()));
        }
    }

    @Override
    public abstract TensorBuilder clone();

    static Boolean compareFactors(Tensor u, Tensor v) {
        return TensorUtils.compare1(u, v);
//        IndexMappingBuffer buffer;
//        if (u.getIndices().size() == 0) <- getFree() !!!
//            buffer = IndexMappings.createPort(u, v).take();
//        else {
//            int[] fromIndices = u.getIndices().getFree().getAllIndices().copy();
//            for (int i = 0; i < fromIndices.length; ++i)
//                fromIndices[i] = IndicesUtils.getNameWithType(fromIndices[i]);
//            buffer = IndexMappings.createPort(new IndexMappingBufferTester(fromIndices, false), u, v).take();
//        }
//        if (buffer == null)
//            return null;
//        assert buffer.isEmpty();
//        return buffer.getSignum();
    }

//    @Override
//    public String toString() {
//        return clone().build().toString();
//    }
}
