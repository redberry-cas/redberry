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
package cc.redberry.core.tensorgenerator;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.context.CC;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.math.frobenius.FrobeniusSolver;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.symmetrization.SymmetrizeSimpleTensorTransformation;
import cc.redberry.core.transformations.symmetrization.SymmetrizeUpperLowerIndicesTransformation;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generates tensor of the most general form with specified free indices from specified tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class TensorGenerator {

    private final Tensor[] samples;
    private final int[] lowerArray, upperArray;
    private final List<SimpleTensor> coefficients = new ArrayList<>();
    private final boolean symmetricForm;
    private final Symmetries symmetries;
    private final SimpleIndices indices;
    private Tensor result;
    private final boolean withCoefficients;

    private TensorGenerator(boolean withCoefficients, SimpleIndices indices, boolean symmetricForm, Symmetries symmetries, Tensor... samples) {
        this.samples = samples;
        this.symmetries = symmetries;
        this.indices = indices;
        this.symmetricForm = symmetricForm;
        this.lowerArray = indices.getLower().copy();
        this.upperArray = indices.getUpper().copy();
        this.withCoefficients = withCoefficients;
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
        SumBuilder result = new SumBuilder();
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
                    temp = ApplyIndexMapping.applyIndexMapping(temp, new Mapping(oldIndices, newIndices), new int[0]);
                    tCombination.add(temp);
                }

            //creating term & processing combinatorics            
            Tensor term = SymmetrizeUpperLowerIndicesTransformation.symmetrizeUpperLowerIndices(Tensors.multiplyAndRenameConflictingDummies(tCombination.toArray(new Tensor[tCombination.size()])));
            if (symmetricForm || !(term instanceof Sum)) {
                Tensor coefficient;
                if (withCoefficients) {
                    coefficient = CC.generateNewSymbol();
                    coefficients.add((SimpleTensor) coefficient);
                } else coefficient = Complex.ONE;

                term = Tensors.multiply(coefficient, term, term instanceof Sum ? new Complex(new Rational(1, term.size())) : Complex.ONE);
            } else if (withCoefficients)
                term = FastTensors.multiplySumElementsOnFactors((Sum) term);
            result.put(term);
        }
        this.result = (symmetries == null || symmetries.isEmpty()) ? result.build() : symmetrize(result.build(), symmetries);
    }

    private Tensor symmetrize(Tensor result, Symmetries symmetries) {
        //todo rewrite this slag
        SimpleTensor st = Tensors.parseSimple("#$#$#A$#ASD" + indices);
        Expression expr = Tensors.expression(st, result);
        Tensor symmetrized = SymmetrizeSimpleTensorTransformation.symmetrize(
                st, st.getIndices().getAllIndices().copy(), symmetries);
        if (symmetrized instanceof Product)
            symmetrized = Tensors.multiply(symmetrized, ((Product) symmetrized).getFactor().reciprocal());

        assert symmetrized instanceof Sum;

        result = ExpandTransformation.expand(expr.transform(symmetrized));


        if (!(result instanceof Sum))
            return result;

        TIntObjectHashMap<List<Tensor[]>> coefficients = new TIntObjectHashMap<>();
        Tensor oldCoefficient, newCoefficient = null;
        TensorBuilder rebuild = result.getBuilder();
        List<Tensor[]> list;
        for (Tensor t : result) {
            assert t instanceof Product;
            if (t instanceof Product) {
                Tensor[] sc = ((Product) t).getAllScalarsWithoutFactor();
                if (sc.length == 0)
                    continue;

                assert sc.length == 1;
                oldCoefficient = sc[0];

                list = coefficients.get(oldCoefficient.hashCode());
                if (list == null) {
                    list = new ArrayList<>();
                    coefficients.put(oldCoefficient.hashCode(), list);
                }

                Mapping match = null;
                for (Tensor[] transformed : list) {
                    match = IndexMappings.getFirst(transformed[0], oldCoefficient);
                    if (match != null) {
                        newCoefficient = match.getSign() ? Tensors.negate(transformed[1]) : transformed[1];
                        break;
                    }
                }
                if (match == null) {
                    if (oldCoefficient instanceof SimpleTensor) {
                        newCoefficient = oldCoefficient;
                    } else {
                        if (withCoefficients) {
                            newCoefficient = CC.generateNewSymbol();
                            this.coefficients.add((SimpleTensor) newCoefficient);
                            this.coefficients.removeAll(TensorUtils.getAllSymbols(oldCoefficient));
                        }
                    }
                    list.add(new Tensor[]{oldCoefficient, newCoefficient});
                }

                rebuild.put(Tensors.multiply(((Product) t).getFactor(), newCoefficient, ((Product) t).getDataSubProduct()));
            }
        }
        return rebuild.build();
    }

    private Tensor result() {
        return result;
    }

    /**
     * Generates tensor of the most general form with specified free indices from specified tensors.
     *
     * @param indices       free indices of the resulting tensor
     * @param symmetricForm specifies whether the resulting tensor should be symmetric
     * @param samples       samples which used to  generate tensor of the general form
     * @return tensor of the most general form with specified free indices from specified tensors
     */
    public static Tensor generate(SimpleIndices indices, boolean symmetricForm, Tensor... samples) {
        return new TensorGenerator(true, indices, symmetricForm, null, samples).result();
    }

    /**
     * Generates tensor of the most general form with specified free indices from specified tensors.
     *
     * @param withCoefficients specifies whether each term in the result should be multiplied on arbitrary coefficient
     * @param indices          free indices of the resulting tensor
     * @param symmetricForm    specifies whether the resulting tensor should be symmetric
     * @param samples          samples which used to  generate tensor of the general form
     * @return tensor of the most general form with specified free indices from specified tensors
     */
    public static Tensor generate(boolean withCoefficients, SimpleIndices indices, boolean symmetricForm, Tensor... samples) {
        return new TensorGenerator(withCoefficients, indices, symmetricForm, null, samples).result();
    }


    public static GeneratedTensor generateStructure(SimpleIndices indices, boolean symmetricForm, Symmetries symmetries, Tensor... samples) {
        return generateStructure(true, indices, symmetricForm, symmetries, samples);
    }

    //todo comments
    public static GeneratedTensor generateStructure(boolean withCoefficients, SimpleIndices indices, boolean symmetricForm, Symmetries symmetries, Tensor... samples) {
        TensorGenerator generator = new TensorGenerator(withCoefficients, indices, symmetricForm, symmetries, samples);
        SimpleTensor[] generatedCoefficients = TensorUtils.getAllSymbols(generator.result()).toArray(new SimpleTensor[0]);
        return new GeneratedTensor(generatedCoefficients,
                generator.result());
    }

    //todo comment
    public static Tensor generate(boolean withCoefficients, SimpleIndices indices, boolean symmetricForm, Symmetries symmetries, Tensor... samples) {
        return new TensorGenerator(withCoefficients, indices, symmetricForm, symmetries, samples).result();
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
