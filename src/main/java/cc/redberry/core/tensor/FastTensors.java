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
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (TensorUtils.haveIndicesIntersections(sum, factor)) {
            SumBuilder sb = new SumBuilder(sum.size());
            for (Tensor t : sum)
                sb.put(multiply(t, factor));
            return sb.build();
        }

        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiply(factor, sum.get(i));
        return new Sum(newSumData, IndicesFactory.create(newSumData[0].getIndices().getFree()));
    }

    /**
     * Multiplies each element in specified sum on some factor and expands indexless parts.
     *
     * @param sum    sum
     * @param factor factor
     * @return resulting sum
     */
    public static Tensor multiplySumElementsOnFactorAndExpand(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (factor instanceof Sum && factor.getIndices().size() != 0)
            throw new IllegalArgumentException();
        if (TensorUtils.haveIndicesIntersections(sum, factor)) {
            SumBuilder sb = new SumBuilder(sum.size());
            for (Tensor t : sum)
                sb.put(ExpandUtils.expandIndexlessSubproduct.transform(multiply(t, factor)));
            return sb.build();
        }

        return multiplySumElementsOnScalarFactorAndExpandScalars1(sum, factor);
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
     * Multiplies each element in specified sum on some scalar factor and expands indexless parts.
     *
     * @param sum    sum
     * @param factor scalar factor
     * @return resulting sum
     * @throws IllegalArgumentException if factor is not scalar
     */
    public static Tensor multiplySumElementsOnScalarFactorAndExpandScalars(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (factor.getIndices().size() != 0)
            throw new IllegalArgumentException();
        return multiplySumElementsOnScalarFactorAndExpandScalars1(sum, factor);
    }

    private static Tensor multiplySumElementsOnScalarFactorAndExpandScalars1(Sum sum, Tensor factor) {
        final ArrayList<Tensor> newSumData = new ArrayList<>(sum.size());
        Tensor temp;
        for (int i = sum.size() - 1; i >= 0; --i) {
            temp = ExpandUtils.expandIndexlessSubproduct.transform(multiply(factor, sum.get(i)));
            if (!TensorUtils.isZero(temp))
                newSumData.add(temp);
        }
        if (newSumData.size() == 0)
            return Complex.ZERO;
        if (newSumData.size() == 1)
            return newSumData.get(0);

        return new Sum(newSumData.toArray(new Tensor[newSumData.size()]),
                IndicesFactory.create(newSumData.get(0).getIndices().getFree()));
    }
}
