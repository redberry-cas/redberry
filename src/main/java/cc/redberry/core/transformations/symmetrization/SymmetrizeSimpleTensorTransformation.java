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
package cc.redberry.core.transformations.symmetrization;

import cc.redberry.core.combinatorics.Permutation;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.math.MathUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;

import java.util.Arrays;

import static cc.redberry.core.tensor.ApplyIndexMapping.applyIndexMapping;
import static cc.redberry.core.tensor.Tensors.*;
/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SymmetrizeSimpleTensorTransformation implements Transformation {
    private final SimpleTensor tensor;
    private final int[] freeIndices;
    private final Symmetries symmetries;

    public SymmetrizeSimpleTensorTransformation(SimpleTensor tensor, int[] freeIndices, Symmetries symmetries) {
        this.tensor = tensor;
        this.freeIndices = freeIndices;
        this.symmetries = symmetries;
    }

    @Override
    public Tensor transform(Tensor t) {
        return null;
    }

    public static Tensor symmetrize(SimpleTensor tensor, int[] freeIndices, Symmetries symmetries) {
        if (symmetries.isEmpty())
            return tensor;
        int[] tempI = freeIndices.clone();
        int[] allFreeIndices = tensor.getIndices().getFree().getAllIndices().copy();
        Arrays.sort(tempI);
        Arrays.sort(allFreeIndices);
        int[] diff = MathUtils.intSetDifference(tempI, allFreeIndices);
        System.arraycopy(freeIndices, 0, allFreeIndices, 0, freeIndices.length);
        System.arraycopy(diff, 0, allFreeIndices, freeIndices.length, diff.length);

        SumBuilder builder = new SumBuilder();
        Tensor temp;
        for (Symmetry symmetry : symmetries) {
            temp = applyIndexMapping(tensor, allFreeIndices, permute(allFreeIndices, symmetry), new int[0]);
            if (symmetry.isAntiSymmetry())
                temp = negate(temp);
            builder.put(temp);
        }
        temp = builder.build();
        if (temp instanceof Sum) {
            //retrieving factor
            Complex factor = null, tempF;
            for (int i = temp.size() - 1; i >= 0; --i) {
                if (temp.get(i) instanceof Product) {
                    tempF = ((Product) temp.get(i)).getFactor();
                    assert tempF.isInteger();
                    tempF = tempF.abs();
                } else
                    tempF = Complex.ONE;
                if (factor == null)
                    factor = tempF;
                assert factor.equals(tempF);
            }

            if (!factor.isOne())
                temp = FastTensors.multiplySumElementsOnFactor((Sum) temp, factor.reciprocal());
            return multiply(new Complex(new Rational(1, temp.size())), temp);
        }
        return temp;
    }

    private static final int[] permute(int[] array, Permutation permutation) {
        int[] copy = new int[array.length];
        int i, length;
        for (i = 0, length = permutation.dimension(); i < length; ++i)
            copy[permutation.newIndexOf(i)] = array[i];
        for (length = array.length; i < length; ++i)
            copy[i] = array[i];
        return copy;
    }
}

