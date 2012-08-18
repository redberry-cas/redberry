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
package cc.redberry.tensorgenerator;

import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.*;
import cc.redberry.core.utils.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexMappingPermutationsGenerator {

    private final Tensor tensor;
    private final int[] indicesNames;
    private final int lowerCount, upperCount;
    private final Symmetries symmetries;

    private IndexMappingPermutationsGenerator(Tensor tensor) {
        this.tensor = tensor;
        Indices indices = IndicesFactory.createSorted(tensor.getIndices().getFreeIndices());

        symmetries = TensorUtils.getIndicesSymmetries(indices.getAllIndices().copy(), tensor);
        lowerCount = indices.getLower().length();
        upperCount = indices.getUpper().length();

        this.indicesNames = indices.getAllIndices().copy();
        for (int i = 0; i < this.indicesNames.length; ++i)
            this.indicesNames[i] = IndicesUtils.getNameWithType(this.indicesNames[i]);

    }
    private final List<Tensor> result = new ArrayList<>();

    private List<Tensor> get() {
        IntPermutationsGenerator lowIndicesPermutator, upperIndicesPermutator;

        if (upperCount != 0 && lowerCount != 0) {
            lowIndicesPermutator = new IntPermutationsGenerator(lowerCount);
            while (lowIndicesPermutator.hasNext()) {
                int[] lowerPermutation = lowIndicesPermutator.next().clone();
                for (int i = 0; i < lowerCount; ++i)
                    lowerPermutation[i] = lowerPermutation[i] + upperCount;
                upperIndicesPermutator = new IntPermutationsGenerator(upperCount);
                UPPER:
                while (upperIndicesPermutator.hasNext()) {
                    int[] upperPermutation = upperIndicesPermutator.next();
                    permute(upperPermutation, lowerPermutation);
                }
            }
        } else if (upperCount == 0) {
            lowIndicesPermutator = new IntPermutationsGenerator(lowerCount);
            while (lowIndicesPermutator.hasNext()) {
                int[] lowerPermutation = lowIndicesPermutator.next();
                permute(new int[0], lowerPermutation);
            }
        } else if (lowerCount == 0) {
            upperIndicesPermutator = new IntPermutationsGenerator(upperCount);
            while (upperIndicesPermutator.hasNext()) {
                int[] upperPermutation = upperIndicesPermutator.next();
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
        final int[] newIndicesNames = new int[indicesNames.length];
        for (int i = 0; i < indicesNames.length; ++i)
            newIndicesNames[i] = indicesNames[permutation[i]];

        //processing new tensor
        result.add(ApplyIndexMapping.applyIndexMapping(tensor, indicesNames, newIndicesNames, new int[0]));
    }

    public static List<Tensor> getAllPermutations(Tensor tensor) {
        return new IndexMappingPermutationsGenerator(tensor).get();
    }
}
