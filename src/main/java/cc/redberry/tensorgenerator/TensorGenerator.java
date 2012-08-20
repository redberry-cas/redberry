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

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.math.frobenius.FrobeniusSolver;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Symmetrize;
import cc.redberry.core.utils.IntArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorGenerator {

    private final Tensor[] samples;
    private final int[] indices;
    private final int[] lowArray;
    private final int[] upArray;
    private final ScalarTensorGenerator scalarTensorGenerator;
    private final List<Tensor> sumands = new ArrayList<>();
    private final List<Tensor> coefficients;
    private final Symmetries symmetries;

//    private TensorGenerator(String indices, Symmetries symmetries, String... samples) {
//        this("c", ParserIndices.parseSimple(indices), symmetries, Tensors.parse(samples));
//    }
//
//    private TensorGenerator(Indices indices, Symmetries symmetries, Tensor... samples) {
//        this("c", indices, symmetries, samples);
//    }
    private TensorGenerator(String coef, Indices indices, Symmetries symmetries, Tensor... samples) {
        this.samples = samples;
        this.scalarTensorGenerator = new ScalarTensorGenerator(coef);
        this.lowArray = indices.getLower().copy();
        this.upArray = indices.getUpper().copy();
        this.coefficients = new ArrayList<>();
        this.symmetries = symmetries;
        Arrays.sort(lowArray);
        Arrays.sort(upArray);
        this.indices = IndicesFactory.createSorted(indices.getFree()).getAllIndices().copy();
        generate();
    }

    private void generate() {

        //processing low indices
        int totalLowCount = lowArray.length, i, k;
        int[] lowCounts = new int[samples.length + 1];
        for (i = 0; i < samples.length; ++i)
            lowCounts[i] = samples[i].getIndices().getLower().length();
        lowCounts[i] = totalLowCount;

        //processing up indices
        int totalUpCount = upArray.length;
        int[] upCounts = new int[samples.length + 1];
        for (i = 0; i < samples.length; ++i)
            upCounts[i] = samples[i].getIndices().getUpper().length();
        upCounts[i] = totalUpCount;

        //solving Frobenius equations
        FrobeniusSolver fbSolver = new FrobeniusSolver(lowCounts, upCounts);

        //processing combinations
        int u, l;
        int[] combination;
        while ((combination = fbSolver.take()) != null) {

            LinkedList<Tensor> tCombination = new LinkedList<>();
            u = 0;
            l = 0;
            for (i = 0; i < combination.length; ++i)
                for (int j = 0; j < combination[i]; ++j) {
                    Tensor temp = samples[i];

//                    IndexMappingDirect im = new IndexMappingDirect();
                    IntArray termLow = temp.getIndices().getLower();
                    IntArray termUp = temp.getIndices().getUpper();

                    int[] oldIndices = new int[termUp.length() + termLow.length()],
                            newIndices = oldIndices.clone();
                    for (k = 0; k < termUp.length(); ++k) {
                        oldIndices[k] = termUp.get(k);
                        newIndices[k] = upArray[u++];
                    }
                    for (k = 0; k < termLow.length(); ++k) {
                        oldIndices[k + termUp.length()] = termLow.get(k);
                        newIndices[k + termUp.length()] = lowArray[l++];
                    }
//                    
//                    im.add(termLow, Arrays.copyOfRange(lowArray, l, l + termLow.length()));
//                    l += termLow.length();
//
//                    im.add(termUp, Arrays.copyOfRange(upArray, u, u + termUp.length()));
//                    u += termUp.length();
//
//                    temp = ApplyIndexMappingDirectTransformation.INSTANCE.perform(temp, im);
//                    tCombination.add(temp);
                    temp = ApplyIndexMapping.applyIndexMapping(temp, oldIndices, newIndices, new int[0]);
                    tCombination.add(temp);
                }

            //creating term & processing combinatorics
            Tensor coefficient;
            if (symmetries == null) {
                Symmetrize symmetrize = new Symmetrize(
                        indices,
                        SymmetriesFactory.createFullSymmetries(totalUpCount, totalLowCount).getBasisSymmetries().toArray(new Symmetry[0]),
                        false);
                Tensor terms = symmetrize.transform(Tensors.multiplyAndRenameConflictingDummies(tCombination.toArray(new Tensor[tCombination.size()])));
                if (terms instanceof Sum)
                    for (Tensor t : terms) {
                        sumands.add(Tensors.multiply(coefficient = scalarTensorGenerator.next(), t));
                        coefficients.add(coefficient);
                    }
                else {
                    sumands.add(Tensors.multiply(coefficient = scalarTensorGenerator.next(), terms));
                    coefficients.add(coefficient);
                }
            } else {
                Symmetrize symmetrize = new Symmetrize(
                        indices,
                        symmetries.getBasisSymmetries().toArray(new Symmetry[0]),
                        true);
                Tensor terms = symmetrize.transform(Tensors.multiplyAndRenameConflictingDummies(tCombination.toArray(new Tensor[tCombination.size()])));
                sumands.add(Tensors.multiply(coefficient = scalarTensorGenerator.next(), terms));
                coefficients.add(coefficient);
            }
        }
    }

    private Tensor result() {
        return Tensors.sum(sumands.toArray(new Tensor[sumands.size()]));
    }

    public static Tensor generate(String coef, Indices indices, Symmetries symmetries, Tensor... samples) {
        return new TensorGenerator(coef, indices, symmetries, samples).result();
    }

    public static GeneratedTensor generateStructure(String coef, Indices indices, Symmetries symmetries, Tensor... samples) {
        TensorGenerator generator = new TensorGenerator(coef, indices, symmetries, samples);
        return new GeneratedTensor(generator.coefficients.toArray(new Tensor[generator.coefficients.size()]),
                                   generator.result());
    }
    
}
