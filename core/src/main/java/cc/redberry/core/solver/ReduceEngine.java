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
package cc.redberry.core.solver;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensorgenerator.GeneratedTensor;
import cc.redberry.core.tensorgenerator.TensorGenerator;
import cc.redberry.core.tensorgenerator.TensorGeneratorUtils;
import cc.redberry.core.transformations.CollectNonScalarsTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.*;

import static cc.redberry.core.indices.IndicesUtils.*;

/**
 * This class implements an algorithm for reducing a system of tensorial equations into system of symbolic equations.
 * The underlying algorithm simply generates tensor of the most general form for each unknown tensorial variable and
 * then tries to build a system of equations fot its coefficients.
 * <p/>
 * <b>Note:</b> the implementation does not guaranties that solution (or proof of its absence) will be found.
 * <p/>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.5
 */
public final class ReduceEngine {
    /**
     * Maximum number of tries to reduce the system.
     */
    private static final int ITERATION_LIMIT = 10000;

    private ReduceEngine() {
    }

    /**
     * Tries to reduces a system of tensorial equations into a system of symbolic equations or return {@code null} if
     * it fails to reduce.
     *
     * @param equations equations
     * @param vars      unknown variables
     * @param rules     additional transformations/rules to be applied or taken into account
     * @return reduced system of symbolic equations and solutions general form
     */
    public static ReducedSystem reduceToSymbolicSystem(Expression[] equations, SimpleTensor[] vars,
                                                       Transformation[] rules) {
        return reduceToSymbolicSystem(equations, vars, rules, new boolean[vars.length]);
    }

    /**
     * Tries to reduces a system of tensorial equations into a system of symbolic equations or return {@code null} if
     * it fails to reduce.
     *
     * @param equations     equations
     * @param vars          unknown variables
     * @param rules         additional transformations/rules to be applied or taken into account
     * @param symmetricForm specifies whether solutions should be putted into symmetric form (with respect to upper and
     *                      lower indices separately)
     * @return reduced system of symbolic equations and solutions general form
     */
    public static ReducedSystem reduceToSymbolicSystem(Expression[] equations, SimpleTensor[] vars,
                                                       Transformation[] rules, boolean[] symmetricForm) {

        Tensor[] zeroReduced = new Tensor[equations.length];
        for (int i = equations.length - 1; i >= 0; --i) {
            zeroReduced[i] = Tensors.subtract(equations[i].get(0), equations[i].get(1));
            zeroReduced[i] = ExpandTransformation.expand(zeroReduced[i],
                    EliminateMetricsTransformation.ELIMINATE_METRICS);
            zeroReduced[i] = EliminateMetricsTransformation.eliminate(zeroReduced[i]);
        }

        TIntHashSet varsNames = new TIntHashSet(vars.length);
        for (SimpleTensor var : vars)
            varsNames.add(var.getName());
        Tensor[] samples = getSamples(zeroReduced, varsNames);
        if (samples.length == 0)
            for (int i = 0; i < vars.length; ++i)
                if (vars[i].getIndices().size() != 0)
                    return null;

        final Expression[] generalSolutions = new Expression[vars.length];
        GeneratedTensor generatedTensor;
        ArrayList<SimpleTensor> unknownCoefficients = new ArrayList<>();

        for (int i = 0; i < generalSolutions.length; ++i) {
            if (vars[i].getIndices().size() == 0) {
                SimpleTensor nVar = CC.generateNewSymbol();
                unknownCoefficients.add(nVar);
                generalSolutions[i] = Tensors.expression(vars[i], nVar);
            } else {
                generatedTensor = TensorGenerator.generateStructure(vars[i].getIndices(), samples, symmetricForm[i], true, true);

                unknownCoefficients.ensureCapacity(generatedTensor.coefficients.length);
                for (SimpleTensor st : generatedTensor.coefficients)
                    unknownCoefficients.add(st);
                generalSolutions[i] = Tensors.expression(vars[i], generatedTensor.generatedTensor);
            }
        }


        ArrayList<Transformation> allRules = new ArrayList<>(Arrays.asList(rules));
        allRules.add(0, EliminateMetricsTransformation.ELIMINATE_METRICS);
        Transformation simplification = new TransformationCollection(allRules);

        ArrayList<Expression> reducedSystem = new ArrayList<>();
        for (Tensor equation : zeroReduced) {
            int count = ITERATION_LIMIT;
            do {
                for (Expression solution : generalSolutions)
                    equation = solution.transform(equation);

                equation = ExpandTransformation.expand(equation, simplification);
                equation = simplification.transform(equation);
                equation = CollectNonScalarsTransformation.collectNonScalars(equation);
                if (!TensorUtils.containsSimpleTensors(equation, varsNames))
                    break;

            } while (count-- > 0);
            if (count <= 0)
                throw new RuntimeException("Maximum number of iterations exceeded: the system cannot be reduced after 10 000 iterations.");


            if (equation.getIndices().size() == 0) {
                reducedSystem.add(Tensors.expression(equation, Complex.ZERO));
            } else {
                if (equation instanceof Sum)
                    for (Tensor t : equation)
                        reducedSystem.add(Tensors.expression(Split.splitScalars(t).summand, Complex.ZERO));
                else
                    reducedSystem.add(Tensors.expression(Split.splitScalars(equation).summand, Complex.ZERO));
            }
        }
        return new ReducedSystem(
                reducedSystem.toArray(new Expression[reducedSystem.size()]),
                unknownCoefficients.toArray(new SimpleTensor[unknownCoefficients.size()]),
                generalSolutions);
    }

    private static Tensor[] getSamples(Tensor[] zeroReduced, TIntHashSet vars) {
        Collection<SimpleTensor> content = TensorUtils.getAllDiffSimpleTensors(zeroReduced);
        ArrayList<Tensor> samples = new ArrayList<>(content.size() + 1);
        Set<IndexType> usedTypes = new HashSet<>();

        for (SimpleTensor st : content) {
            if (vars.contains(st.getName()))
                continue;
            if (st.getIndices().size() == 0)
                continue;
            if (Tensors.isKroneckerOrMetric(st)) {
                usedTypes.add(getTypeEnum(st.getIndices().get(0)));
                continue;
            }

            SimpleIndices si = st.getIndices();
            int[] free = new int[si.size()];
            for (int i = si.size() - 1; i >= 0; --i) {
                usedTypes.add(getTypeEnum(si.get(i)));
                free[i] = createIndex(i, getType(si.get(i)), getState(si.get(i)));
            }
            st = Tensors.setIndices(st, IndicesFactory.createSimple(null, si));
            samples.addAll(Arrays.asList(TensorGeneratorUtils.allStatesCombinations(st)));
        }

        for (IndexType type : usedTypes) {
            byte btype = type.getType();
            //adding Kronecker
            samples.add(Tensors.createKronecker(setType(btype, 0), 0x80000000 | setType(btype, 1)));
            if (CC.isMetric(btype)) {
                samples.add(Tensors.createMetric(setType(btype, 0), setType(btype, 1)));
                samples.add(Tensors.createMetric(0x80000000 | setType(btype, 0), 0x80000000 | setType(btype, 1)));
            }
        }
        return samples.toArray(new Tensor[samples.size()]);
    }
}
