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
 * the Free Software Foundation, either version 2 of the License, or
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

import static cc.redberry.core.tensor.Tensors.multiply;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FastTensors {

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
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }

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

        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = ExpandUtils.expandIndexlessSubproduct.transform(multiply(factor, sum.get(i)));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }

    public static Tensor multiplySumElementsOnFactors(Sum sum, OutputPortUnsafe<Tensor> factorsProvider) {
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiply(factorsProvider.take(), sum.get(i));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }

    public static Tensor multiplySumElementsOnScalarFactorAndExpandScalars(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        if (factor.getIndices().size() != 0)
            throw new IllegalArgumentException();
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = ExpandUtils.expandIndexlessSubproduct.transform(multiply(factor, sum.get(i)));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFree()));
    }
}
