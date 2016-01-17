/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensorgenerator.GeneratedTensor;
import cc.redberry.core.tensorgenerator.TensorGenerator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.collect.CollectTransformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.TensorUtils;

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ExpandAndEliminateTransformation.expandAndEliminate;
import static cc.redberry.core.transformations.Transformation.IDENTITY;

/**
 * @author Stanislav Poslavsky
 */
public final class PassarinoVeltman {
    private PassarinoVeltman() {
    }

    /**
     * Generates a substitution for tensor integral reduction via Passarino-Veltman method. Note: the performance is
     * limited for a large order or large number of external momentums.
     *
     * @param order             power of loop momentum (q_i - 1, q_i*q_j - 2 etc.)
     * @param loopMomentum      loop momentum
     * @param externalMomentums list of external momentums
     * @return substitution in the form like {@code q_i*q_j = p1_i * p2_j * C1 + ... }
     */
    public static Expression generateSubstitution(int order, SimpleTensor loopMomentum,
                                                  SimpleTensor[] externalMomentums) {
        return generateSubstitution(order, loopMomentum, externalMomentums, IDENTITY);
    }

    /**
     * Generates a substitution for tensor integral reduction via Passarino-Veltman method. Note: the performance is
     * limited for a large order or large number of external momentums.
     *
     * @param order             power of loop momentum (q_i - 1, q_i*q_j - 2 etc.)
     * @param loopMomentum      loop momentum
     * @param externalMomentums list of external momentums
     * @param simplifications   additional simplification rules (e.g. Mandelstam substitutions for products of external momentum)
     * @return substitution in the form like {@code q_i*q_j = p1_i * p2_j * C1 + ... }
     */
    public static Expression generateSubstitution(int order, SimpleTensor loopMomentum,
                                                  SimpleTensor[] externalMomentums,
                                                  Transformation simplifications) {
        if (order == 0)
            throw new IllegalArgumentException("Zero order");
        check(loopMomentum, externalMomentums);

        byte indexType = getType(loopMomentum.getIndices().get(0));
        int[] indicesArray = new int[order];
        for (int i = 0; i < order; i++)
            indicesArray[i] = setType(indexType, i);
        SimpleIndices indices = IndicesFactory.createSimple(null, indicesArray);

        Tensor loopProduct = Complex.ONE;
        for (int i = 0; i < indices.size(); ++i)
            loopProduct = multiply(loopProduct, Tensors.setIndices(loopMomentum, new int[]{indicesArray[i]}));


        GeneratedTensor genTensor = TensorGenerator.generateStructure(indices,
                ArraysUtils.addAll(externalMomentums, createMetric(setType(indexType, 0), setType(indexType, 1))),
                true, true, true);
        SimpleTensor[] params = genTensor.coefficients;
        Tensor expression = genTensor.generatedTensor;

        expression = new CollectTransformation(params).transform(expression);
        Tensor[] coeffs = coefficientsList(expression, params);

        Tensor[][] matrix = new Tensor[params.length][params.length];
        Tensor[] rhs = new Tensor[params.length];
        for (int j = 0; j < params.length; ++j) {
            Tensor inverted = ApplyIndexMapping.invertIndices(coeffs[j]);
            for (int i = 0; i < params.length; ++i) {
                matrix[i][j] = expandAndEliminate(multiplyAndRenameConflictingDummies(coeffs[i], inverted), simplifications);
                matrix[i][j] = simplifications.transform(matrix[i][j]);
            }
            rhs[j] = expandAndEliminate(multiplyAndRenameConflictingDummies(loopProduct, inverted));
        }

        Tensor[][] inverse = TensorUtils.inverse(matrix);
        SumBuilder solution = new SumBuilder();
        for (int j = 0; j < params.length; ++j) {
            SumBuilder sol = new SumBuilder();
            for (int i = 0; i < params.length; i++)
                sol.put(multiplyAndRenameConflictingDummies(inverse[i][j], rhs[i]));
            solution.put(multiplyAndRenameConflictingDummies(sol.build(), coeffs[j]));
        }

        return expression(loopProduct, solution.build());
    }

    private static void check(SimpleTensor loopMomentum, SimpleTensor[] externalMomentums) {
        check(null, loopMomentum);
        IndexType type = getTypeEnum(loopMomentum.getIndices().get(0));
        for (SimpleTensor externalMomentum : externalMomentums)
            check(type, externalMomentum);
    }

    private static void check(IndexType type, SimpleTensor momentum) {
        if (momentum.getIndices().size() != 1)
            throw new IllegalArgumentException("Not a momentum: " + momentum);
        if (type != null && type != getTypeEnum(momentum.getIndices().get(0)))
            throw new IllegalArgumentException("Not a momentum: " + momentum + " wrong index type");
    }

    private static Tensor[] coefficientsList(Tensor t, SimpleTensor[] coefficients) {
        boolean hasSymbolic = false;
        for (SimpleTensor coefficient : coefficients)
            if (coefficient.getIndices().size() == 0)
                hasSymbolic = true;

        t = new CollectTransformation(coefficients, hasSymbolic).transform(t);

        Tensor[] result = new Tensor[coefficients.length];
        Arrays.fill(result, Complex.ZERO);

        if (t instanceof Product) {
            Monomial m = getFromProduct(t, coefficients);
            if (m != null)
                result[m.index] = m.coeff;
        } else
            for (Tensor term : t) {
                Monomial m = getFromProduct(term, coefficients);
                if (m != null)
                    result[m.index] = m.coeff;
            }
        return result;
    }

    private static Monomial getFromProduct(Tensor t, SimpleTensor[] coefficients) {
        if (!(t instanceof Product))
            return null;
        for (int j = 0; j < t.size(); j++) {
            if (!(t.get(j) instanceof SimpleTensor))
                continue;
            for (int i = 0; i < coefficients.length; i++)
                if (t.get(j).equals(coefficients[i]))
                    return new Monomial(((Product) t).remove(j), i);
        }
        return null;
    }

    private static final class Monomial {
        final Tensor coeff;
        final int index;

        public Monomial(Tensor coeff, int index) {
            this.coeff = coeff;
            this.index = index;
        }
    }
}
