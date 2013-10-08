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
package cc.redberry.core.transformations;

import cc.redberry.core.indexgenerator.IndexGeneratorImpl;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.transformations.symmetrization.SymmetrizeSimpleTensorTransformation;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.ApplyIndexMapping.*;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.utils.ArraysUtils.addAll;

/**
 * Differentiates specified tensor with respect to specified simple tensors.
 * It temporary does not support derivatives of tensor fields.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class DifferentiateTransformation implements Transformation {

    private final SimpleTensor[] vars;
    private final Transformation[] expandAndContract;

    /**
     * Creates transformations which differentiate with respect to specified simple tensors.
     *
     * @param vars
     */
    public DifferentiateTransformation(SimpleTensor... vars) {
        this.vars = vars;
        this.expandAndContract = new Transformation[0];
    }

    public DifferentiateTransformation(SimpleTensor[] vars, Transformation[] expandAndContract) {
        this.vars = vars;
        this.expandAndContract = expandAndContract;
    }

    @Override
    public Tensor transform(Tensor t) {
        return differentiate(t, expandAndContract, vars);
    }

    /**
     * Gives the multiple derivative of specified order of specified tensor with respect to specified simple tensor.
     *
     * @param tensor tensor to be differentiated
     * @param var    simple tensor
     * @param order  order of derivative
     * @return derivative
     * @throws IllegalArgumentException if both order is not one and var is not scalar.
     */
    public static Tensor differentiate(Tensor tensor, SimpleTensor var, int order) {
        if (var.getIndices().size() != 0 && order > 1)
            throw new IllegalArgumentException();
        for (; order > 0; --order)
            tensor = differentiate(tensor, new Transformation[0], var);
        return tensor;
    }

    /**
     * Gives the multiple derivative of specified tensor with respect to specified arguments.
     *
     * @param tensor tensor to be differentiated
     * @param vars   arguments
     * @return derivative
     * @throws IllegalArgumentException if there is clash of indices
     */
    public static Tensor differentiate(Tensor tensor, SimpleTensor... vars) {
        if (vars.length == 0)
            return tensor;
        if (vars.length == 1)
            return differentiate(tensor, new Transformation[0], vars[0]);
        return differentiate(tensor, new Transformation[0], vars);
    }

    /**
     * Gives the multiple derivative of specified tensor with respect to specified arguments.
     *
     * @param tensor            tensor to be differentiated
     * @param vars              arguments
     * @param expandAndContract additional transformations to be applied after each step of differentiation
     * @return derivative
     * @throws IllegalArgumentException if there is clash of indices
     */
    public static Tensor differentiate(Tensor tensor, Transformation[] expandAndContract, SimpleTensor... vars) {
        if (vars.length == 0)
            return tensor;
        if (vars.length == 1)
            return differentiate(tensor, expandAndContract, vars[0]);

        boolean needRename = false;
        for (SimpleTensor var : vars)
            if (var.getIndices().size() != 0) {
                needRename = true;
                break;
            }

        SimpleTensor[] resolvedVars = vars;
        if (needRename) {
            TIntHashSet forbidden = TensorUtils.getAllIndicesNamesT(tensor);
            for (SimpleTensor var : vars)
                forbidden.addAll(getIndicesNames(var.getIndices().getFree()));

            resolvedVars = vars.clone();
            for (int i = 0; i < vars.length; ++i)
                if (!forbidden.isEmpty() && resolvedVars[i].getIndices().size() != 0) {
                    if (resolvedVars[i].getIndices().size() != resolvedVars[i].getIndices().getFree().size())
                        resolvedVars[i] = (SimpleTensor) renameDummy(resolvedVars[i], forbidden.toArray());
                    forbidden.addAll(getIndicesNames(resolvedVars[i].getIndices()));
                }
            tensor = renameDummy(tensor, TensorUtils.getAllIndicesNamesT(resolvedVars).toArray(), forbidden);
            tensor = renameIndicesOfFieldsArguments(tensor, forbidden);
        }

        for (SimpleTensor var : resolvedVars)
            tensor = differentiate1(tensor, createRule(var), expandAndContract);

        return tensor;
    }

    private static Tensor differentiate(Tensor tensor, Transformation[] expandAndContract, SimpleTensor var) {
        if (var.getIndices().size() != 0) {
            TIntHashSet forbidden = TensorUtils.getAllIndicesNamesT(tensor);
            var = (SimpleTensor) renameDummy(var, TensorUtils.getAllIndicesNamesT(tensor).toArray());
            forbidden.addAll(IndicesUtils.getIndicesNames(var.getIndices()));
            tensor = renameDummy(tensor, TensorUtils.getAllIndicesNamesT(var).toArray(), forbidden);
            tensor = renameIndicesOfFieldsArguments(tensor, forbidden);
        }
        return differentiate1(tensor, createRule(var), expandAndContract);
    }

    private static Tensor differentiateWithRenaming(Tensor tensor, SimpleTensorDifferentiationRule rule, Transformation[] expandAndEliminate) {
        SimpleTensorDifferentiationRule newRule = rule.newRuleForTensor(tensor);
        tensor = renameDummy(tensor, newRule.getForbidden());
        return differentiate1(tensor, newRule, expandAndEliminate);
    }

    private static Tensor differentiate1(Tensor tensor, SimpleTensorDifferentiationRule rule, Transformation[] transformations) {
        if (tensor.getClass() == SimpleTensor.class) {
            Tensor temp = rule.differentiateSimpleTensor((SimpleTensor) tensor);
            return applyTransformations(temp, transformations);
        }
        if (tensor.getClass() == TensorField.class) {
            TensorField field = (TensorField) tensor;
            SumBuilder result = new SumBuilder(tensor.size());
            Tensor dArg;

            for (int i = tensor.size() - 1; i >= 0; --i) {
                dArg = differentiate1(field.get(i), rule, transformations);
                if (TensorUtils.isZero(dArg)) continue;

                result.put(
                        multiply(dArg,
                                fieldDerivative(field, field.getArgIndices(i).getInverted(), i))
                );

            }
            return applyTransformations(EliminateMetricsTransformation.eliminate(result.build()), transformations);
        }
        if (tensor instanceof Sum) {
            SumBuilder builder = new SumBuilder();
            Tensor temp;
            for (Tensor t : tensor) {
                temp = differentiate1(t, rule, transformations);
                temp = applyTransformations(temp, transformations);
                builder.put(temp);
            }
            return builder.build();
        }
        if (tensor instanceof ScalarFunction) {
            Tensor temp = multiply(((ScalarFunction) tensor).derivative(),
                    differentiateWithRenaming(tensor.get(0), rule, transformations));
            temp = applyTransformations(temp, transformations);
            return temp;
        }
        if (tensor instanceof Power) {
            //e^f*ln(g) -> g^f*(f'*ln(g)+f/g*g') ->f*g^(f-1)*g' + g^f*ln(g)*f'
            Tensor temp = sum(
                    multiply(tensor.get(1),
                            pow(tensor.get(0), sum(tensor.get(1), Complex.MINUS_ONE)),
                            differentiate1(tensor.get(0), rule, transformations)),
                    multiply(tensor,
                            log(tensor.get(0)),
                            differentiateWithRenaming(tensor.get(1), rule, transformations)));
            temp = applyTransformations(temp, transformations);
            return temp;
        }
        if (tensor instanceof Product) {
            SumBuilder result = new SumBuilder();
            Tensor temp;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                temp = tensor.set(i, differentiate1(tensor.get(i), rule, transformations));
                if (rule.var.getIndices().size() != 0)
                    temp = EliminateMetricsTransformation.eliminate(temp);
                temp = applyTransformations(temp, transformations);
                result.put(temp);
            }
            return result.build();
        }
        if (tensor instanceof Complex)
            return Complex.ZERO;
        throw new UnsupportedOperationException();
    }

    private static Tensor applyTransformations(Tensor tensor, Transformation[] transformations) {
        for (Transformation transformation : transformations)
            tensor = transformation.transform(tensor);
        return tensor;
    }

    private static SimpleTensorDifferentiationRule createRule(SimpleTensor var) {
        if (var.getIndices().size() == 0)
            return new SymbolicDifferentiationRule(var);
        return new SymmetricDifferentiationRule(var);
    }

    private static abstract class SimpleTensorDifferentiationRule {

        protected final SimpleTensor var;

        protected SimpleTensorDifferentiationRule(SimpleTensor var) {
            this.var = var;
        }

        Tensor differentiateSimpleTensor(SimpleTensor simpleTensor) {
            if (simpleTensor.getName() != var.getName())
                return Complex.ZERO;
            return differentiateSimpleTensorWithoutCheck(simpleTensor);
        }

        abstract SimpleTensorDifferentiationRule newRuleForTensor(Tensor tensor);

        abstract Tensor differentiateSimpleTensorWithoutCheck(SimpleTensor simpleTensor);

        abstract int[] getForbidden();
    }

    private static final class SymbolicDifferentiationRule extends SimpleTensorDifferentiationRule {

        private SymbolicDifferentiationRule(SimpleTensor var) {
            super(var);
        }

        @Override
        Tensor differentiateSimpleTensorWithoutCheck(SimpleTensor simpleTensor) {
            return Complex.ONE;
        }

        @Override
        SimpleTensorDifferentiationRule newRuleForTensor(Tensor tensor) {
            return this;
        }

        @Override
        int[] getForbidden() {
            return new int[0];
        }
    }

    private static final class SymmetricDifferentiationRule extends SimpleTensorDifferentiationRule {

        private final Tensor derivative;
        private final int[] allFreeFrom, freeVarIndices;

        private SymmetricDifferentiationRule(SimpleTensor var, Tensor derivative, int[] allFreeFrom, int[] freeVarIndices) {
            super(var);
            this.derivative = derivative;
            this.allFreeFrom = allFreeFrom;
            this.freeVarIndices = freeVarIndices;
        }

        SymmetricDifferentiationRule(SimpleTensor var) {
            super(var);
            SimpleIndices varIndices = var.getIndices();
            int[] allFreeVarIndices = new int[varIndices.size()];
            int[] allFreeArgIndices = new int[varIndices.size()];
            byte type;
            int state, i = 0, length = allFreeArgIndices.length;
            IndexGeneratorImpl indexGenerator = new IndexGeneratorImpl(varIndices);
            for (; i < length; ++i) {
                type = getType(varIndices.get(i));
                state = getRawStateInt(varIndices.get(i));
                allFreeVarIndices[i] = setRawState(indexGenerator.generate(type), inverseIndexState(state));
                allFreeArgIndices[i] = setRawState(indexGenerator.generate(type), state);
            }
            int[] allIndices = addAll(allFreeVarIndices, allFreeArgIndices);
            SimpleIndices dIndices = IndicesFactory.createSimple(null, allIndices);
            SimpleTensor symmetric = simpleTensor("@!@#@##_AS@23@@#", dIndices);
            Tensor derivative = SymmetrizeSimpleTensorTransformation.symmetrize(
                    symmetric,
                    allFreeVarIndices,
                    varIndices.getSymmetries().getInnerSymmetries());
            derivative = applyIndexMapping(
                    derivative,
                    new Mapping(allIndices,
                            addAll(varIndices.getInverted().getAllIndices().copy(), allFreeArgIndices)),
                    new int[0]);
            ProductBuilder builder = new ProductBuilder(0, length);
            for (i = 0; i < length; ++i)
                builder.put(createMetricOrKronecker(allFreeArgIndices[i], allFreeVarIndices[i]));
            derivative = new SubstitutionTransformation(symmetric, builder.build()).transform(derivative);
            this.derivative = derivative;
            this.freeVarIndices = var.getIndices().getFree().getInverted().getAllIndices().copy();
            this.allFreeFrom = addAll(allFreeArgIndices, freeVarIndices);
        }

        @Override
        Tensor differentiateSimpleTensorWithoutCheck(SimpleTensor simpleTensor) {
            int[] to = simpleTensor.getIndices().getAllIndices().copy();
            to = addAll(to, freeVarIndices);
            return applyIndexMapping(derivative, new Mapping(allFreeFrom, to), new int[0]);
        }

        @Override
        SimpleTensorDifferentiationRule newRuleForTensor(Tensor tensor) {
            return new SymmetricDifferentiationRule(this.var,
                    renameDummy(derivative, TensorUtils.getAllIndicesNamesT(tensor).toArray()), allFreeFrom, freeVarIndices);
        }

        @Override
        int[] getForbidden() {
            return TensorUtils.getAllIndicesNamesT(derivative).toArray();
        }
    }
}
