package cc.redberry.core.transformations;

import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.transformations.substitutions.Substitution;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ApplyIndexMapping.applyIndexMapping;
import static cc.redberry.core.transformations.ContractIndices.contract;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Differentiate implements Transformation {
    private final SimpleTensor[] vars;

    public Differentiate(SimpleTensor... vars) {
        this.vars = vars;
    }

    @Override
    public Tensor transform(Tensor t) {
        return differentiate(t, vars);
    }

    public static Tensor differentiate(Tensor tensor, SimpleTensor var, int order) {
        if (var.getIndices().size() != 0 && order > 1)
            throw new IllegalArgumentException();
        for (; order > 0; --order)
            tensor = differentiate(tensor, var);
        return tensor;
    }

    public static Tensor differentiate(Tensor tensor, SimpleTensor... vars) {
        TIntHashSet forbidden = TensorUtils.getAllIndicesNamesT(tensor);
        for (SimpleTensor var : vars)
            forbidden.addAll(getIndicesNames(var.getIndices().getFree()));

        SimpleTensor[] vars1 = vars.clone();
        for (int i = 0; i < vars.length; ++i) {
            if (!forbidden.isEmpty() && vars1[i].getIndices().size() != 0) {
                if (vars1[i].getIndices().size() != vars1[i].getIndices().getFree().size())
                    vars1[i] = (SimpleTensor) ApplyIndexMapping.renameDummy(vars1[i], forbidden.toArray());
                forbidden.addAll(getIndicesNames(vars1[i].getIndices()));
            }
        }
        tensor = ApplyIndexMapping.renameDummy(tensor, TensorUtils.getAllIndicesNamesT(vars1).toArray());
        for (SimpleTensor var : vars1)
            tensor = differentiate1(tensor, createRule(var));
        return tensor;
    }

    public static Tensor differentiate(Tensor tensor, SimpleTensor var) {
        if (var.getIndices().size() != 0) {
            if (var.getIndices().size() != var.getIndices().getFree().size())
                var = (SimpleTensor) ApplyIndexMapping.renameDummy(var, TensorUtils.getAllIndicesNamesT(tensor).toArray());
            tensor = ApplyIndexMapping.renameDummy(tensor, TensorUtils.getAllIndicesNamesT(var).toArray());
        }
        return differentiate1(tensor, createRule(var));
    }

    private static Tensor differentiate1(Tensor tensor, SimpleTensorDifferentiationRule rule) {
        if (tensor.getClass() == SimpleTensor.class)
            return rule.differentiateSimpleTensor((SimpleTensor) tensor);
        if (tensor.getClass() == TensorField.class) {
            TensorLastIterator iterator = new TensorLastIterator(tensor);
            Tensor c;
            while ((c = iterator.next()) != null) {
                if (c.getClass() == SimpleTensor.class) {
                    if (((SimpleTensor) c).getName() == rule.var.getName())
                        throw new UnsupportedOperationException();
                }
            }
            return Complex.ZERO;
        }
        if (tensor instanceof Sum) {
            SumBuilder builder = new SumBuilder();
            for (Tensor t : tensor)
                builder.put(differentiate1(t, rule));
            return builder.build();
        }
        if (tensor instanceof ScalarFunction) {
            return multiply(((ScalarFunction) tensor).derivative(), differentiate1(tensor.get(0), rule));
        }
        if (tensor instanceof Power) {
            //e^f*ln(g) -> g^f*(f'*ln(g)+f/g*g') ->f*g^(f-1)*g' + g^f*ln(g)*f'
            return sum(
                    multiply(tensor.get(1),
                            pow(tensor.get(0), sum(tensor.get(1), Complex.MINUSE_ONE)),
                            differentiate1(tensor.get(0), rule)),
                    multiply(tensor,
                            log(tensor.get(0)),
                            differentiate1(tensor.get(1), rule)));
        }
        if (tensor instanceof Product) {
            SumBuilder result = new SumBuilder();
            for (int i = tensor.size() - 1; i >= 0; --i) {
                if (!(tensor.get(i) instanceof Complex))
                    if (rule.var.getIndices().size() == 0)
                        result.put(tensor.set(i, differentiate1(tensor.get(i), rule)));
                    else
                        result.put(contract(tensor.set(i, differentiate1(tensor.get(i), rule))));
            }
            return result.build();
        }
        if (tensor instanceof Complex)
            return Complex.ZERO;
        throw new UnsupportedOperationException();
    }

    //    private static Tensor symmetricDifferentiate(SimpleTensor tensor, SimpleTensor var) {
//        if (tensor.getIndices().getSymmetries().isEmpty()) {
//            Indices tensorIndices = tensor.getIndices();
//            ProductBuilder pb = new ProductBuilder(0, tensorIndices.size());
//            for (int i = 0, size = tensorIndices.size(); i < size; ++i) {
//                pb.put(createMetricOrKronecker(
//                        tensorIndices.get(i),
//                        inverseIndexState(var.getIndices().get(i))));
//            }
//            return pb.build();
//        }
//        List<Symmetry> symmetries = tensor.getIndices().getSymmetries().getInnerSymmetries().getBasisSymmetries();
//        int[] indices = tensor.getIndices().getAllIndices().copy();
//        int[] freeIndices = new int[indices.length];
//        IndexGenerator generator = new IndexGenerator(var.getIndices());
//        int i;
//        for (i = 0; i < indices.length; ++i)
//            freeIndices[i] = getRawStateInt(indices[i]) | generator.generate(getType(indices[i]));
//
//
//        ProductBuilder pb = new ProductBuilder(0, freeIndices.length);
//        for (i = 0; i < indices.length; ++i)
//            pb.put(createMetricOrKronecker(freeIndices[i], inverseIndexState(var.getIndices().get(i))));
//        Tensor temp = pb.build();
//
//        int[] varFreeIndices = var.getIndices().getFree().getInverse().getAllIndices().copy();
//        SumBuilder builder = new SumBuilder();
//        builder.put(temp);
//        Symmetry s;
//        for (i = symmetries.size() - 1; i >= 1; --i) {
//            if (temp == null)//preventing build on first iteration
//                temp = builder.build();
//            s = symmetries.get(i);
//            temp = applyIndexMapping(temp,
//                    ArraysUtils.addAll(getFree(freeIndices), varFreeIndices),
//                    ArraysUtils.addAll(getFree(s.permute(freeIndices)), varFreeIndices),
//                    new int[0]);
//            builder.put(s.isAntiSymmetry() ? negate(temp) : temp);
//            temp = null;
//        }
//        temp = builder.build();
//        if (temp instanceof Sum)
//            temp = multiply(new Complex(new Rational(1, temp.size())), temp);
//
//        return applyIndexMapping(temp,
//                ArraysUtils.addAll(freeIndices, varFreeIndices),
//                ArraysUtils.addAll(indices, varFreeIndices),
//                new int[0]);
//    }
    private static SimpleTensorDifferentiationRule createRule(SimpleTensor var) {
        if (var.getIndices().size() == 0)
            return new SymbolicDifferentiationRule(var);
        return new SymmetricDifferentiationRule(var);
    }

    private static abstract class SimpleTensorDifferentiationRule {
        private final SimpleTensor var;

        protected SimpleTensorDifferentiationRule(SimpleTensor var) {
            this.var = var;
        }


        Tensor differentiateSimpleTensor(SimpleTensor simpleTensor) {
            if (simpleTensor.getName() != var.getName())
                return Complex.ZERO;
            return differentiateSimpleTensorWithoutCheck(simpleTensor);
        }

        abstract Tensor differentiateSimpleTensorWithoutCheck(SimpleTensor simpleTensor);
    }

    private static final class SymbolicDifferentiationRule extends SimpleTensorDifferentiationRule {
        private SymbolicDifferentiationRule(SimpleTensor var) {
            super(var);
        }

        @Override
        Tensor differentiateSimpleTensorWithoutCheck(SimpleTensor simpleTensor) {
            return Complex.ONE;
        }
    }

    private static final class SymmetricDifferentiationRule extends SimpleTensorDifferentiationRule {
        private final Tensor derivative;
        private final int[] allFreeFrom, freeVarIndices;

        SymmetricDifferentiationRule(SimpleTensor var) {
            super(var);
            SimpleIndices varIndices = var.getIndices();
            int[] allFreeVarIndices = new int[varIndices.size()];
            int[] allFreeArgIndices = new int[varIndices.size()];
            byte type;
            int state, i = 0, length = allFreeArgIndices.length;
            IndexGenerator indexGenerator = new IndexGenerator(varIndices);
            for (; i < length; ++i) {
                type = getType(varIndices.get(i));
                state = getRawStateInt(varIndices.get(i));
                allFreeVarIndices[i] = setRawState(indexGenerator.generate(type), inverseIndexState(state));
                allFreeArgIndices[i] = setRawState(indexGenerator.generate(type), state);
            }
            int[] allIndices = ArraysUtils.addAll(allFreeVarIndices, allFreeArgIndices);
            SimpleIndices dIndices = IndicesFactory.createSimple(null, allIndices);
            SimpleTensor symmetric = simpleTensor("@!@#@##_AS@23@@#", dIndices);
            Tensor derivative = SymmetrizeSimpleTensor.symmetrize(
                    symmetric,
                    allFreeVarIndices,
                    varIndices.getSymmetries().getInnerSymmetries());
            derivative = applyIndexMapping(
                    derivative,
                    allIndices,
                    ArraysUtils.addAll(varIndices.getInverse().getAllIndices().copy(), allFreeArgIndices),
                    new int[0]);
            ProductBuilder builder = new ProductBuilder(0, length);
            for (i = 0; i < length; ++i)
                builder.put(createMetricOrKronecker(allFreeArgIndices[i], allFreeVarIndices[i]));
            derivative = new Substitution(symmetric, builder.build()).transform(derivative);
            this.derivative = derivative;
            this.freeVarIndices = var.getIndices().getFree().getInverse().getAllIndices().copy();
            this.allFreeFrom = ArraysUtils.addAll(allFreeArgIndices, freeVarIndices);
        }

        Tensor differentiateSimpleTensorWithoutCheck(SimpleTensor simpleTensor) {
            int[] to = simpleTensor.getIndices().getAllIndices().copy();
            to = ArraysUtils.addAll(to, freeVarIndices);
            return ApplyIndexMapping.applyIndexMapping(derivative, allFreeFrom.clone(), to, new int[0]);
        }
    }
}
