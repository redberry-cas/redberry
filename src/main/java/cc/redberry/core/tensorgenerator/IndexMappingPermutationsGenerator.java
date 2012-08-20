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
package cc.redberry.core.tensorgenerator;

import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class IndexMappingPermutationsGenerator {

    private final Tensor tensor;
    private final int[] indices;
    private final int lowerCount, upperCount;
    private final Symmetries symmetries;

    private IndexMappingPermutationsGenerator(Tensor tensor) {
        this.tensor = tensor;
        Indices indices = IndicesFactory.createSorted(tensor.getIndices().getFree());
        this.indices = indices.getAllIndices().copy();
        this.symmetries = TensorUtils.getIndicesSymmetriesForIndicesWithSameStates(this.indices, tensor);
        this.lowerCount = indices.getLower().length();
        this.upperCount = indices.getUpper().length();
        
    }
    private final List<Tensor> result = new ArrayList<>();

    private List<Tensor> get() {
        IntPermutationsGenerator lowIndicesPermutationsGenerator,
                upperIndicesPermutationGenerator;

        if (upperCount != 0 && lowerCount != 0) {
            lowIndicesPermutationsGenerator = new IntPermutationsGenerator(lowerCount);
            while (lowIndicesPermutationsGenerator.hasNext()) {
                int[] lowerPermutation = lowIndicesPermutationsGenerator.next().clone();
                for (int i = 0; i < lowerCount; ++i)
                    lowerPermutation[i] = lowerPermutation[i] + upperCount;
                upperIndicesPermutationGenerator = new IntPermutationsGenerator(upperCount);
                UPPER:
                while (upperIndicesPermutationGenerator.hasNext()) {
                    int[] upperPermutation = upperIndicesPermutationGenerator.next();
                    permute(upperPermutation, lowerPermutation);
                }
            }
        } else if (upperCount == 0) {
            lowIndicesPermutationsGenerator = new IntPermutationsGenerator(lowerCount);
            while (lowIndicesPermutationsGenerator.hasNext()) {
                int[] lowerPermutation = lowIndicesPermutationsGenerator.next();
                permute(new int[0], lowerPermutation);
            }
        } else if (lowerCount == 0) {
            upperIndicesPermutationGenerator = new IntPermutationsGenerator(upperCount);
            while (upperIndicesPermutationGenerator.hasNext()) {
                int[] upperPermutation = upperIndicesPermutationGenerator.next();
                permute(upperPermutation, new int[0]);
            }
        }
        return result;
    }
    private final List<int[]> generatedPermutations = new ArrayList<>();

    private void permute(int[] upperPermutation, int[] lowerPermutation) {
        //creating resulting permutation upper indices are first,
        //because initial indices are sorted
        int[] permutation = new int[lowerCount + upperCount];
        System.arraycopy(upperPermutation, 0, permutation, 0, upperCount);
        System.arraycopy(lowerPermutation, 0, permutation, upperCount, lowerCount);

        //TODO discover better algorithm (possible using stretches of symmetries)
        //checking wheather the way beetween current permutation and already
        //generated combinatorics exists throw any possible combination of symmetries

        for (int[] p : generatedPermutations)
            for (Symmetry symmetry : symmetries)
                if (Arrays.equals(permutation, symmetry.permute(p)))
                    return;
        generatedPermutations.add(permutation);

        //processing new indices from permutation
        final int[] newIndices = new int[indices.length];
        for (int i = 0; i < indices.length; ++i)
            newIndices[i] = indices[permutation[i]];

        //processing new tensor
        result.add(ApplyIndexMapping.applyIndexMapping(tensor, indices, newIndices, new int[0]));
    }

    static List<Tensor> getAllPermutations(Tensor tensor) {
        return new IndexMappingPermutationsGenerator(tensor).get();
    }
}
