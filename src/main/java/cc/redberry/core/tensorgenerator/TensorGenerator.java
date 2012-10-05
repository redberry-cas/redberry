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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.math.frobenius.FrobeniusSolver;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.SymmetrizeUpperLowerIndices;
import cc.redberry.core.utils.IntArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorGenerator {

    private final Tensor[] samples;
    private final int[] lowerArray, upperArray;
    private final OutputPortUnsafe<Tensor> coefficientsGenerator;
    private final SumBuilder result = new SumBuilder();
    private final boolean symmetricForm;

    private TensorGenerator(String coefficientName, Indices indices, boolean symmetricForm, Tensor... samples) {
        this.samples = samples;
        if (coefficientName.isEmpty())
            this.coefficientsGenerator = OnePort.INSTANCE;
        else
            this.coefficientsGenerator = new SymbolsGeneratorWithHistory(coefficientName);
        this.symmetricForm = symmetricForm;
        this.lowerArray = indices.getLower().copy();
        this.upperArray = indices.getUpper().copy();
        Arrays.sort(lowerArray);
        Arrays.sort(upperArray);
        generate();
    }

    private void generate() {

        //processing low indices
        int totalLowCount = lowerArray.length, i, k;
        int[] lowCounts = new int[samples.length + 1];
        for (i = 0; i < samples.length; ++i)
            lowCounts[i] = samples[i].getIndices().getFree().getLower().length();
        lowCounts[i] = totalLowCount;

        //processing up indices
        int totalUpCount = upperArray.length;
        int[] upCounts = new int[samples.length + 1];
        for (i = 0; i < samples.length; ++i)
            upCounts[i] = samples[i].getIndices().getFree().getUpper().length();
        upCounts[i] = totalUpCount;

        //solving Frobenius equations
        FrobeniusSolver fbSolver = new FrobeniusSolver(lowCounts, upCounts);

        //processing combinations
        int u, l;
        int[] combination;
        while ((combination = fbSolver.take()) != null) {

            List<Tensor> tCombination = new ArrayList<>();
            u = 0;
            l = 0;
            for (i = 0; i < combination.length; ++i)
                for (int j = 0; j < combination[i]; ++j) {
                    Tensor temp = samples[i];

//                    IndexMappingDirect im = new IndexMappingDirect();
                    IntArray termLow = temp.getIndices().getFree().getLower();
                    IntArray termUp = temp.getIndices().getFree().getUpper();

                    int[] oldIndices = new int[termUp.length() + termLow.length()],
                            newIndices = oldIndices.clone();
                    for (k = 0; k < termUp.length(); ++k) {
                        oldIndices[k] = termUp.get(k);
                        newIndices[k] = upperArray[u++];
                    }
                    for (k = 0; k < termLow.length(); ++k) {
                        oldIndices[k + termUp.length()] = termLow.get(k);
                        newIndices[k + termUp.length()] = lowerArray[l++];
                    }
                    temp = ApplyIndexMapping.applyIndexMapping(temp, oldIndices, newIndices, new int[0]);
                    tCombination.add(temp);
                }

            //creating term & processing combinatorics            
            Tensor term = SymmetrizeUpperLowerIndices.symmetrizeUpperLowerIndices(Tensors.multiplyAndRenameConflictingDummies(tCombination.toArray(new Tensor[tCombination.size()])));
            if (symmetricForm || !(term instanceof Sum))
                term = Tensors.multiply(coefficientsGenerator.take(), term, term instanceof Sum ? new Complex(new Rational(1, term.size())) : Complex.ONE);
            else
                term = Tensors.multiplySumElementsOnFactors((Sum) term, coefficientsGenerator);
            result.put(term);
        }
    }

    private Tensor result() {
        return result.build();
    }

    public static Tensor generate(String coefficientName, Indices indices, boolean symmetricForm, Tensor... samples) {
        return new TensorGenerator(coefficientName, indices, symmetricForm, samples).result();
    }

    public static GeneratedTensor generateStructure(String coefficientName, Indices indices, boolean symmetricForm, Tensor... samples) {
        TensorGenerator generator = new TensorGenerator(coefficientName, indices, symmetricForm, samples);
        SimpleTensor[] generatedCoefficients;
        if (coefficientName.isEmpty())
            generatedCoefficients = new SimpleTensor[0];
        else {
            SymbolsGeneratorWithHistory coefficientsGenerator = (SymbolsGeneratorWithHistory) generator.coefficientsGenerator;
            generatedCoefficients = coefficientsGenerator.generated.toArray(new SimpleTensor[coefficientsGenerator.generated.size()]);
        }
        return new GeneratedTensor(generatedCoefficients,
                                   generator.result());
    }

    private static final class OnePort implements OutputPortUnsafe<Tensor> {

        public static final OnePort INSTANCE = new OnePort();

        private OnePort() {
        }

        @Override
        public Tensor take() {
            return Complex.ONE;
        }
    }
}
