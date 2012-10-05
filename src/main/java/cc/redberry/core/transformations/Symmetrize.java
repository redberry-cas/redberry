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

import cc.redberry.core.combinatorics.Permutation;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//TODO include antisymmetries
public final class Symmetrize implements Transformation {

    private final int[] freeIndices;
    private final Symmetries symmetries;
    private final boolean multiplyFactorial;

    public Symmetrize(int[] freeIndices, Permutation[] symmetries, boolean multiplyFactorial) {
        checkConsistensy(freeIndices, symmetries);
        this.freeIndices = freeIndices;
        if (symmetries.length == 0)
            this.symmetries = SymmetriesFactory.createSymmetries(0);
        else
            this.symmetries = SymmetriesFactory.createSymmetries(symmetries[0].dimension());
        for (Permutation s : symmetries)
            this.symmetries.add(s.asSymmetry());
        this.multiplyFactorial = multiplyFactorial;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return symmetrizeWithoutCheck(tensor, freeIndices, symmetries, multiplyFactorial);
    }

    public static Tensor symmetrize(Tensor tensor, int[] freeIndicesNames, Permutation[] symmetries, boolean multiplyFactorial) {
        checkConsistensy(freeIndicesNames, symmetries);
        Symmetries symmetries1 = SymmetriesFactory.createSymmetries(symmetries[0].dimension());
        for (Permutation s : symmetries)
            symmetries1.add(s.asSymmetry());
        return symmetrizeWithoutCheck(tensor, freeIndicesNames, symmetries1, multiplyFactorial);
    }

    private static Tensor symmetrizeWithoutCheck(Tensor tensor, int[] freeIndicesNames, Symmetries symmetries, boolean multiplyFactorial) {
        if (!IndicesUtils.equalsRegardlessOrder(tensor.getIndices().getFree(), freeIndicesNames))
            throw new IllegalArgumentException("Specified indices are not equal (regardless order) to the free indices of specified tensor .");

        if (symmetries.dimension() == 0)
            return tensor;
        Symmetries tensorSymmetries = TensorUtils.getIndicesSymmetries(freeIndicesNames, tensor);
        List<Tensor> generatedTensors = new ArrayList<>();
        List<Permutation> generatedPermutations = new ArrayList<>();

        OUT:
        for (Permutation permutation : symmetries) {
            for (Permutation generatedPermutation : generatedPermutations)
                for (Permutation tensorSymmetry : tensorSymmetries)
                    if (permutation.equals(generatedPermutation.composition(tensorSymmetry)))
                        continue OUT;
            generatedPermutations.add(permutation);
            int[] newIndicesNames = permutation.permute(freeIndicesNames);
            generatedTensors.add(ApplyIndexMapping.applyIndexMapping(tensor, freeIndicesNames, newIndicesNames, new int[0]));
        }
        Tensor[] summands = generatedTensors.toArray(new Tensor[generatedTensors.size()]);
        if (multiplyFactorial)
            return Tensors.multiply(new Complex(new Rational(1, generatedTensors.size())), Tensors.sum(summands));
        else
            return Tensors.sum(summands);
    }

    private static void checkConsistensy(int[] indices, Permutation[] symmetries) {
        for (Permutation s : symmetries)
            if (!IndicesUtils.isPermutationConsistentWithIndices(indices, s))
                throw new IllegalArgumentException("Inconsistent symmetry.");
    }
}
