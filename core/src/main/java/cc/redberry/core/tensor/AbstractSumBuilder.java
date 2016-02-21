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

import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
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
    int[] sortedNames;
    private int size = 0;

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

    AbstractSumBuilder(TIntObjectHashMap<List<FactorNode>> summands, Complex complex, Indices indices, int[] sortedNames) {
        this.summands = summands;
        this.complex = complex;
        this.indices = indices;
        this.sortedNames = sortedNames;
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
            sortedNames = IndicesUtils.getIndicesNames(indices);
            Arrays.sort(sortedNames);
        } else if (!indices.equalsRegardlessOrder(tensor.getIndices().getFree()))
            throw new TensorException("Inconsistent indices in sum. " +
                    "Expected: " + indices + " Actual: " + tensor.getIndices().getFree(), tensor);//TODO improve message
        if (tensor instanceof Sum) {
            summands.ensureCapacity(tensor.size() - summands.size());
            for (Tensor s : tensor)
                put(s);
            return;
        }
        if (tensor instanceof Complex) {
            complex = complex.add((Complex) tensor);
            return;
        }

        final Split split = split(tensor);

        final int hash = iHash(split.factor, sortedNames);
        final List<FactorNode> factorNodes = summands.get(hash);
        if (factorNodes == null) {
            List<FactorNode> fns = new ArrayList<>(1);
            fns.add(new FactorNode(split.factor, split.getBuilder()));
            summands.put(hash, fns);
            ++size;
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
            if (b == null) {
                factorNodes.add(new FactorNode(split.factor, split.getBuilder()));
                if (DEBUG_PRINT_SAME_FLAG) {
                    System.out.println("\n");
                    for (FactorNode node : factorNodes)
                        System.out.println(node.factor);
                }
                ++size;
            }
        }
    }

    private static int iHash(final Tensor t, final int[] sortedNames) {
        if (t instanceof Product)
            return ((Product) t).iHashCode();
        else if (t instanceof SimpleTensor)
            return HashingStrategy.iGraphHash((SimpleTensor) t, sortedNames);
        else
            return HashingStrategy.iHash(t, sortedNames);
    }

    public static boolean DEBUG_PRINT_SAME_FLAG = false;

    @Override
    public abstract TensorBuilder clone();

    public int size() {
        return size + (complex.isZero() ? 0 : 1);
    }

    public int sizeOfMap() {
        return summands.size();
    }

    static Boolean compareFactors(Tensor u, Tensor v) {
        return IndexMappings.compare1_withoutCheck(u, v);
    }
}
