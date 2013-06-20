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
package cc.redberry.core.transformations.symmetrization;

import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class SymmetrizeUpperLowerIndicesTransformation implements Transformation {

    public final static SymmetrizeUpperLowerIndicesTransformation SYMMETRIZE_UPPER_LOWER_INDICES
            = new SymmetrizeUpperLowerIndicesTransformation();

    private SymmetrizeUpperLowerIndicesTransformation() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return symmetrizeUpperLowerIndices(t);
    }

    public static Tensor symmetrizeUpperLowerIndices(Tensor tensor) {
        return symmetrizeUpperLowerIndices(tensor, false);
    }

    public static Tensor symmetrizeUpperLowerIndices(Tensor tensor, boolean multiplyOnSymmetryFactor) {
        Indices indices = IndicesFactory.create(tensor.getIndices().getFree());
        int[] indicesArray = indices.getAllIndices().copy();
        Symmetries symmetries = TensorUtils.getIndicesSymmetriesForIndicesWithSameStates(indicesArray, tensor);
        int lowerCount = indices.getLower().length(), upperCount = indices.getUpper().length();

        IntPermutationsGenerator lowIndicesPermutationsGenerator,
                upperIndicesPermutationGenerator;
        SumBuilder sumBuilder = new SumBuilder();
        Tensor summand;
        List<int[]> generatedPermutations = new ArrayList<>();
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
                    summand = permute(tensor, indicesArray, upperPermutation, lowerPermutation, generatedPermutations, symmetries);
                    if (summand != null)
                        sumBuilder.put(summand);
                }
            }
        } else if (upperCount == 0) {
            lowIndicesPermutationsGenerator = new IntPermutationsGenerator(lowerCount);
            while (lowIndicesPermutationsGenerator.hasNext()) {
                int[] lowerPermutation = lowIndicesPermutationsGenerator.next();
                summand = permute(tensor, indicesArray, new int[0], lowerPermutation, generatedPermutations, symmetries);
                if (summand != null)
                    sumBuilder.put(summand);
            }
        } else if (lowerCount == 0) {
            upperIndicesPermutationGenerator = new IntPermutationsGenerator(upperCount);
            while (upperIndicesPermutationGenerator.hasNext()) {
                int[] upperPermutation = upperIndicesPermutationGenerator.next();
                summand = permute(tensor, indicesArray, upperPermutation, new int[0], generatedPermutations, symmetries);
                if (summand != null)
                    sumBuilder.put(summand);
            }
        }
        if (multiplyOnSymmetryFactor)
            return Tensors.multiply(new Complex(new Rational(1, generatedPermutations.size())), sumBuilder.build());
        else
            return sumBuilder.build();
    }

    private static Tensor permute(Tensor tensor,
                                  int[] indicesArray,
                                  int[] upperPermutation,
                                  int[] lowerPermutation,
                                  List<int[]> generatedPermutations,
                                  Symmetries symmetries) {
        //creating resulting permutation upper indices are first,
        //because initial indices are sorted
        int[] permutation = new int[upperPermutation.length + lowerPermutation.length];
        System.arraycopy(upperPermutation, 0, permutation, 0, upperPermutation.length);
        System.arraycopy(lowerPermutation, 0, permutation, upperPermutation.length, lowerPermutation.length);

        //TODO discover better algorithm (possible using stretches of symmetries)
        //checking wheather the way beetween current permutation and already
        //generated combinatorics exists throw any possible combination of symmetries

        for (int[] p : generatedPermutations)
            for (Symmetry symmetry : symmetries)
                if (Arrays.equals(permutation, symmetry.permute(p)))
                    return null;
        generatedPermutations.add(permutation);

        //processing new indices from permutation
        final int[] newIndices = new int[indicesArray.length];
        for (int i = 0; i < indicesArray.length; ++i)
            newIndices[i] = indicesArray[permutation[i]];

        //processing new tensor
        return ApplyIndexMapping.applyIndexMapping(tensor, indicesArray, newIndices, new int[0]);
    }

    static Tensor[] getAllPermutations(Tensor tensor) {
        return symmetrizeUpperLowerIndices(tensor).toArray();
    }
}
