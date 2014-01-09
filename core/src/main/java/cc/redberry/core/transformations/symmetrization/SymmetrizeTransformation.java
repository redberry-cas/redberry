/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;

import java.util.List;

/**
 * Gives a symmetrization of tensor with respect to specified indices under the specified symmetries.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.6
 */
public final class SymmetrizeTransformation implements Transformation {
    private final int[] indices;
    private final List<Permutation>  symmetries;
    private final boolean multiplyBySymmetryFactor;

    /**
     * @param indices                  indices
     * @param symmetries               symmetries
     * @param multiplyBySymmetryFactor specifies whether the resulting expression should be divided by the
     *                                 number of symmetries (the order of the corresponding symmetric group)
     */
    public SymmetrizeTransformation(int[] indices, List<Permutation> symmetries, boolean multiplyBySymmetryFactor) {
        this.indices = indices;
        this.symmetries = symmetries;
        this.multiplyBySymmetryFactor = multiplyBySymmetryFactor;
    }

    @Override
    public Tensor transform(Tensor t) {
        if (!multiplyBySymmetryFactor) {
            SumBuilder sb = new SumBuilder();
            for (Permutation symmetry : symmetries)
                sb.put(ApplyIndexMapping.applyIndexMappingAutomatically(t,
                        new Mapping(indices, symmetry.permute(indices), symmetry.antisymmetry())));

            return sb.build();
        } else {
            long length = 0;
            SumBuilder sb = new SumBuilder();
            for (Permutation symmetry : symmetries) {
                sb.put(ApplyIndexMapping.applyIndexMappingAutomatically(t,
                        new Mapping(indices, symmetry.permute(indices), symmetry.antisymmetry())));
                ++length;
            }
            t = sb.build();
            if (t instanceof Sum)
                return FastTensors.multiplySumElementsOnFactor((Sum) t, new Complex(new Rational(1L, length)));
            return Tensors.multiply(new Complex(new Rational(1L, length)), t);
        }
    }
}
