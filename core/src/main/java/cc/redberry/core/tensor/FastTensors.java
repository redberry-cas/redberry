/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandUtils;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;

import static cc.redberry.core.tensor.Tensors.multiply;

/**
 * Utility rare, but fast methods with tensor modifications.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class FastTensors {
    private FastTensors() {
    }

    /**
     * Multiplies each element in specified sum on some factor.
     *
     * @param sum    sum
     * @param factor factor
     * @return resulting sum
     */
    public static Tensor multiplySumElementsOnFactor(Sum sum, Tensor factor) {
        return multiplySumElementsOnFactor(sum, factor, new Transformation[0]);
    }

    /**
     * Multiplies each element in specified sum on some factor (not a sum) and expands indexless parts.
     *
     * @param sum    sum
     * @param factor factor
     * @return resulting sum
     * @throws IllegalArgumentException if factor is sum
     */
    public static Tensor multiplySumElementsOnFactorAndExpand(Sum sum, Tensor factor) {
        if (factor instanceof Sum && factor.getIndices().size() != 0)
            throw new IllegalArgumentException();
        return multiplySumElementsOnFactor(sum, factor, new Transformation[]{ExpandUtils.expandIndexlessSubproduct});
    }

    private static Tensor multiplySumElementsOnFactor(Sum sum, Tensor factor, Transformation[] transformations) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (TensorUtils.haveIndicesIntersections(sum, factor))
            return multiplyWithBuilder(sum, factor, transformations);
        else
            return multiplyWithFactory(sum, factor, transformations);
    }

    private static Tensor multiplyWithBuilder(Sum sum, Tensor factor, Transformation... transformations) {
        SumBuilder sb = new SumBuilder(sum.size());
        for (Tensor t : sum)
            sb.put(Transformation.Util.applySequentially(multiply(t, factor), transformations));
        return sb.build();
    }

    private static Tensor multiplyWithFactory(Sum sum, Tensor factor, Transformation... transformations) {
        final ArrayList<Tensor> newSumData = new ArrayList<>(sum.size());
        Tensor temp;
        boolean reduced = false;
        for (int i = sum.size() - 1; i >= 0; --i) {
            temp = Transformation.Util.applySequentially(multiply(factor, sum.get(i)), transformations);
            if (!TensorUtils.isZero(temp)) {
                newSumData.add(temp);
                if (!reduced && isReduced(sum.get(i), factor, temp))
                    reduced = true;
            }
        }
        if (newSumData.size() == 0)
            return Complex.ZERO;
        if (newSumData.size() == 1)
            return newSumData.get(0);

        final Tensor[] data = newSumData.toArray(new Tensor[newSumData.size()]);
        if (reduced)
            return SumFactory.FACTORY.create(data);
        return new Sum(data,
                IndicesFactory.create(newSumData.get(0).getIndices().getFree()));
    }


    /**
     * @deprecated very unsafe method without checks
     */
    @Deprecated
    public static Tensor multiplySumElementsOnFactors(Sum sum, OutputPortUnsafe<Tensor> factorsProvider) {
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiply(factorsProvider.take(), sum.get(i));
        return new Sum(newSumData, IndicesFactory.create(newSumData[0].getIndices().getFree()));
    }

    /**
     * Checks whether the resulting tensor was reduced to simplified form after multiply
     *
     * @param initial initial sum element
     * @param factor  factor
     * @param result  resulting product
     * @return whether the resulting tensor was reduced to simplified form after multiply
     */
    private static boolean isReduced(Tensor initial, Tensor factor, Tensor result) {
        if (initial instanceof Product && !(result instanceof Product))
            return true;
        return false;
    }

}

