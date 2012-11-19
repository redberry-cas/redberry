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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.tensor;

import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseNodeTransformer;
import cc.redberry.core.tensor.functions.*;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;

/**
 * <p>Factory methods to create any tensor are collected in this class. Using
 * the methods implemented in this class is the preferred way to create a
 * tensor.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see TensorFactory
 * @see TensorBuilder
 */
public final class Tensors {

    private Tensors() {
    }

    // ==========================  Factory Methods ============================

    /**
     * Returns tensor in the integer power. Common result is an object of class
     * Power, but for some input arguments result could have different type.
     *
     * @param argument base
     * @param power    power
     * @return result of argument exponentiation
     */
    public static Tensor pow(Tensor argument, int power) {
        return pow(argument, new Complex(power));
    }

    /**
     * Returns tensor in the scalar power. Common result is an object of class
     * Power, but for some input arguments result could have different type.
     *
     * @param argument base
     * @param power    power
     * @return result of argument exponentiation
     */
    public static Tensor pow(Tensor argument, Tensor power) {
        PowerBuilder pb = new PowerBuilder();
        pb.put(argument);
        pb.put(power);
        return pb.build();
    }

    /**
     * <p>Returns result of multiplication of several tensors. Einstein notation
     * assumed. Common result is an object of class Product, but for some input
     * arguments result could have different type.</p> <p>If there is a chance
     * that some factors have conflicting (same name) dummy indices use {@link #multiplyAndRenameConflictingDummies(Tensor...)}
     * instead.</p>
     *
     * @param factors array of factors to be multiplied
     * @return result of multiplication
     */
    public static Tensor multiply(final Tensor... factors) {
        //TODO add check for indices consistency
        return ProductFactory.FACTORY.create(factors);
    }

    /**
     * <p>Returns result of multiplication of several tensors taking care about
     * all conflicting dummy indices in the factors. Einstein notation assumed.
     * Common result is an object of class Product, but for some input arguments
     * result could have different type.</p>
     *
     * @param factors array of factors to be multiplied
     * @return result of multiplication
     */
    public static Tensor multiplyAndRenameConflictingDummies(Tensor... factors) {
        return ProductFactory.FACTORY.create(resolveDummy(factors));
//        Tensor t = ProductFactory.FACTORY.create(factors);
//        if (!(t instanceof Product))
//            return t;
//
//        //postprocessing product
//        Product p = (Product) t;
//        //all product indices
//        Set<Integer> totalIndices = new HashSet<>();
//        int i, j;
//        Indices indices = p.indices;
//        for (i = indices.size() - 1; i >= 0; --i)
//            totalIndices.add(IndicesUtils.getNameWithType(indices.get(i)));
//
//        int[] forbidden;
//        Tensor current;
//        //processing indexless data
//        for (i = 0; i < p.indexlessData.length; ++i) {
//            current = p.indexlessData[i];
//            if (current instanceof Sum || current instanceof Power) {
//                forbidden = new int[totalIndices.size()];
//                j = -1;
//                for (Integer index : totalIndices)
//                    forbidden[++j] = index;
//                p.indexlessData[i] = ApplyIndexMapping.renameDummyFromClonedSource(current, forbidden);
//                totalIndices.addAll(TensorUtils.getAllIndicesNames(p.indexlessData[i]));
//            }
//        }
//        Set<Integer> free;
//        for (i = 0; i < p.data.length; ++i) {
//            current = p.data[i];
//            if (current instanceof Sum || current instanceof Power) {
//                free = new HashSet<>(current.getIndices().size());
//                for (j = current.getIndices().size() - 1; j >= 0; --j)
//                    free.add(IndicesUtils.getNameWithType(current.getIndices().get(j)));
//                totalIndices.removeAll(free);
//                forbidden = new int[totalIndices.size()];
//                j = -1;
//                for (Integer index : totalIndices)
//                    forbidden[++j] = index;
//                p.data[i] = ApplyIndexMapping.renameDummyFromClonedSource(current, forbidden);
//                totalIndices.addAll(TensorUtils.getAllIndicesNames(p.data[i]));
//            }
//        }
//        return p;
    }

    public static Tensor[] resolveDummy(Tensor[] factors) {
        //TODO preserve ordering
        Tensor[] result = new Tensor[factors.length];
        TIntHashSet forbidden = new TIntHashSet();
        ArrayList<Tensor> toResolve = new ArrayList<>();
        int position = -1;
        for (Tensor f : factors) {
            if (f instanceof Sum || f instanceof Power || f instanceof ScalarFunction) {
                toResolve.add(f);
                forbidden.addAll(f.getIndices().getFree().getAllIndices().copy());
            } else {
                forbidden.addAll(TensorUtils.getAllIndicesNamesT(f));
                result[++position] = f;
            }
        }

        Tensor factor, newFactor;
        for (int i = toResolve.size() - 1; i >= 0; --i) {
            factor = toResolve.get(i);
            newFactor = ApplyIndexMapping.renameDummy(factor, forbidden.toArray());
            forbidden.addAll(TensorUtils.getAllIndicesNamesT(newFactor));
            result[++position] = newFactor;
        }
        return result;
    }

    /**
     * Returns result of summation of several tensors. Common result is an
     * object of class Sum, but for some input arguments result could have
     * different type.
     *
     * @param tensors array of summands
     * @return result of summation
     */
    public static Tensor sum(Tensor... tensors) {
        return SumFactory.FACTORY.create(tensors);
    }

    public static Tensor subtract(Tensor a, Tensor b) {
        return sum(a, negate(b));
    }

    /**
     * Returns new simple tensor with specified string name and indices.
     *
     * @param name    string name of the tensor
     * @param indices indices
     * @return new instance of {@link SimpleTensor} object
     */
    public static SimpleTensor simpleTensor(String name, SimpleIndices indices) {
        NameDescriptor descriptor = CC.getNameManager().mapNameDescriptor(name, indices.getIndicesTypeStructure());
        return new SimpleTensor(descriptor.getId(),
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(),
                        indices));
    }

    /**
     * Returns new simple tensor with specified int name (see {@link cc.redberry.core.context.NameManager}
     * for details) and indices.
     *
     * @param name    int name of the tensor
     * @param indices indices
     * @return new instance of {@link SimpleTensor} object
     */
    public static SimpleTensor simpleTensor(int name, SimpleIndices indices) {
        NameDescriptor descriptor = CC.getNameDescriptor(name);
        if (descriptor == null)
            throw new IllegalArgumentException("This name is not registered in the system.");
        if (!descriptor.getIndicesTypeStructure().isStructureOf(indices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        return new SimpleTensor(name,
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(),
                        indices));
    }

    /**
     * Returns new tensor field with specified string name, indices and
     * arguments list. Free indices of arguments assumed as arguments indices
     * bindings of this field bindings.
     *
     * @param name      int name of the field
     * @param indices   indices
     * @param arguments arguments list
     * @return new instance of {@link TensorField} object
     */
    public static TensorField field(String name, SimpleIndices indices, Tensor[] arguments) {
        SimpleIndices[] argIndices = new SimpleIndices[arguments.length];
        for (int i = 0; i < argIndices.length; ++i)
            argIndices[i] = IndicesFactory.createSimple(null, arguments[i].getIndices().getFree());
        return field(name, indices, argIndices, arguments);
    }

    /**
     * Returns new tensor field with specified string name, indices, arguments
     * list and explicit argument indices bindings.
     *
     * @param name       int name of the field
     * @param indices    indices
     * @param argIndices argument indices bindings
     * @param arguments  arguments list
     * @return new instance of {@link TensorField} object
     */
    public static TensorField field(String name, SimpleIndices indices, SimpleIndices[] argIndices, Tensor[] arguments) {
        if (argIndices.length != arguments.length)
            throw new IllegalArgumentException("Argument indices array and arguments array have different length.");
        if (arguments.length == 0)
            throw new IllegalArgumentException("No arguments in field.");
        for (int i = 0; i < argIndices.length; ++i)
            if (!arguments[i].getIndices().getFree().equalsRegardlessOrder(argIndices[i]))
                throw new IllegalArgumentException("Arguments indices are inconsistent with arguments.");

        IndicesTypeStructure[] structures = new IndicesTypeStructure[argIndices.length + 1];
        structures[0] = indices.getIndicesTypeStructure();
        for (int i = 0; i < argIndices.length; ++i)
            structures[i + 1] = argIndices[i].getIndicesTypeStructure();
        NameDescriptor descriptor = CC.getNameManager().mapNameDescriptor(name, structures);
        return new TensorField(descriptor.getId(),
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                arguments, argIndices);
    }

    /**
     * Returns new tensor field with specified int name (see {@link cc.redberry.core.context.NameManager}
     * for details), indices, arguments list and explicit argument indices
     * bindings.
     *
     * @param name       int name of the field
     * @param indices    indices
     * @param argIndices argument indices bindings
     * @param arguments  arguments list
     * @return new instance of {@link TensorField} object
     */
    public static TensorField field(int name, SimpleIndices indices, SimpleIndices[] argIndices, Tensor[] arguments) {
        if (argIndices.length != arguments.length)
            throw new IllegalArgumentException("Argument indices array and arguments array have different length.");
        if (arguments.length == 0)
            throw new IllegalArgumentException("No arguments in field.");
        NameDescriptor descriptor = CC.getNameDescriptor(name);
        if (descriptor == null)
            throw new IllegalArgumentException("This name is not registered in the system.");
        if (!descriptor.isField())
            throw new IllegalArgumentException("Name correspods to simple tensor (not a field).");
        if (descriptor.getIndicesTypeStructures().length - 1 != argIndices.length)
            throw new IllegalArgumentException("This name corresponds to field with different number of arguments.");
        if (!descriptor.getIndicesTypeStructure().isStructureOf(indices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        for (int i = 0; i < argIndices.length; ++i) {
            if (!descriptor.getIndicesTypeStructures()[i + 1].isStructureOf(argIndices[i]))
                throw new IllegalArgumentException("Arguments indices are inconsistent with field signature.");
            if (!arguments[i].getIndices().getFree().equalsRegardlessOrder(argIndices[i]))
                throw new IllegalArgumentException("Arguments indices are inconsistent with arguments.");
        }
        return new TensorField(name,
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                arguments, argIndices);
    }

    /**
     * Returns new tensor field with specified int name (see {@link cc.redberry.core.context.NameManager}
     * for details), indices and arguments list. Free indices of arguments
     * assumed as arguments indices bindings of this field bindings.
     *
     * @param name      int name of the field
     * @param indices   indices
     * @param arguments arguments list
     * @return new instance of {@link TensorField} object
     */
    public static TensorField field(int name, SimpleIndices indices, Tensor[] arguments) {
        if (arguments.length == 0)
            throw new IllegalArgumentException("No arguments in field.");
        NameDescriptor descriptor = CC.getNameDescriptor(name);
        if (descriptor == null)
            throw new IllegalArgumentException("This name is not registered in the system.");
        if (!descriptor.getIndicesTypeStructure().isStructureOf(indices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        SimpleIndices[] argIndices = new SimpleIndices[arguments.length];
        for (int i = 0; i < arguments.length; ++i)
            argIndices[i] = IndicesFactory.createSimple(null, arguments[i].getIndices().getFree());
        return new TensorField(name,
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                arguments, argIndices);
    }

    /**
     * Creates an expression object from two tensors.
     *
     * @param left  left part of expression
     * @param right right part of expression
     * @return new object of type {@link Expression}
     */
    public static Expression expression(Tensor left, Tensor right) {
        return ExpressionFactory.FACTORY.create(left, right);
    }

    /**
     * Creates a sinus object from scalar argument. Common result is an object
     * of class Sin, but for some input argument result could have different
     * type.
     *
     * @param argument scalar argument of sinus
     * @return sinus of argument
     */
    public static Tensor sin(Tensor argument) {
        return Sin.SinFactory.FACTORY.create(argument);
    }

    /**
     * Creates a cosine object from scalar argument. Common result is an object
     * of class Cos, but for some input argument result could have different
     * type.
     *
     * @param argument scalar argument of cosine
     * @return cosine of argument
     */
    public static Tensor cos(Tensor argument) {
        return Cos.CosFactory.FACTORY.create(argument);
    }

    /**
     * Creates a tangent object from scalar argument. Common result is an object
     * of class Tan, but for some input argument result could have different
     * type.
     *
     * @param argument scalar argument of tangent
     * @return tangent of argument
     */
    public static Tensor tan(Tensor argument) {
        return Tan.TanFactory.FACTORY.create(argument);
    }

    /**
     * Creates a cotangent object from scalar argument. Common result is an
     * object of class Cot, but for some input argument result could have
     * different type.
     *
     * @param argument scalar argument of cotangent
     * @return cotangent of argument
     */
    public static Tensor cot(Tensor argument) {
        return Cot.CotFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arcsinus object from scalar argument. Common result is an
     * object of class ArcSin, but for some input argument result could have
     * different type.
     *
     * @param argument scalar argument of arcsinus
     * @return arcsinus of argument
     */
    public static Tensor arcsin(Tensor argument) {
        return ArcSin.ArcSinFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arccosine object from scalar argument. Common result is an
     * object of class ArcCos, but for some input argument result could have
     * different type.
     *
     * @param argument scalar argument of arccosine
     * @return arccosine of argument
     */
    public static Tensor arccos(Tensor argument) {
        return ArcCos.ArcCosFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arctangent object from scalar argument. Common result is an
     * object of class ArcTan, but for some input argument result could have
     * different type.
     *
     * @param argument scalar argument of arctangent
     * @return arctangent of argument
     */
    public static Tensor arctan(Tensor argument) {
        return ArcTan.ArcTanFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arcotangent object from scalar argument. Common result is an
     * object of class ArcCot, but for some input argument result could have
     * different type.
     *
     * @param argument scalar argument of arccotangent
     * @return arcotangent of argument
     */
    public static Tensor arccot(Tensor argument) {
        return ArcCot.ArcCotFactory.FACTORY.create(argument);
    }

    /**
     * Creates a natural logarithm object from scalar argument. Common result is
     * an object of class Log, but for some input argument result could have
     * different type.
     *
     * @param argument scalar argument of logarithm
     * @return natural logarithm of argument
     */
    public static Tensor log(Tensor argument) {
        return Log.LogFactory.FACTORY.create(argument);
    }

    /**
     * Creates a exponent object from scalar argument. Common result is an
     * object of class Exp, but for some input argument result could have
     * different type. See {@link #pow(Tensor, Tensor)}.
     *
     * @param argument scalar argument of exponent
     * @return exponent of argument
     */
    public static Tensor exp(Tensor argument) {
        return Exp.ExpFactory.FACTORY.create(argument);
    }

    public static SimpleTensor createKronecker(int index1, int index2) {
        return CC.current().createKronecker(index1, index2);
    }

    public static SimpleTensor createMetric(int index1, int index2) {
        return CC.current().createMetric(index1, index2);
    }

    public static SimpleTensor createMetricOrKronecker(int index1, int index2) {
        return CC.current().createMetricOrKronecker(index1, index2);
    }

    public static boolean isKronecker(Tensor t) {
        if (!(t instanceof SimpleTensor))
            return false;
        return CC.current().isKronecker((SimpleTensor) t);
    }

    public static boolean isMetric(Tensor t) {
        if (!(t instanceof SimpleTensor))
            return false;
        return CC.current().isMetric((SimpleTensor) t);
    }

    public static boolean isKroneckerOrMetric(Tensor t) {
        if (!(t instanceof SimpleTensor))
            return false;
        return CC.current().isKroneckerOrMetric((SimpleTensor) t);
    }

    public static boolean isKroneckerOrMetric(SimpleTensor t) {
        return CC.current().isKroneckerOrMetric(t);
    }

    /**
     * Parses a string to tensor.
     *
     * @param expression string to be parsed
     * @return result of parsing
     */
    public static Tensor parse(String expression) {
        return CC.current().getParseManager().parse(expression);
    }

    /**
     * Parses an array of strings and returns array of parsed tensors.
     *
     * @param expressions array of strings to be parsed
     * @return array of parsed tensors
     */
    public static Tensor[] parse(final String... expressions) {
        Tensor[] r = new Tensor[expressions.length];
        for (int i = 0; i < expressions.length; ++i)
            r[i] = parse(expressions[i]);
        return r;
    }

    public static Tensor parse(String expression, ParseNodeTransformer... preprocessors) {
        return CC.current().getParseManager().parse(expression, preprocessors);
    }

    /**
     * Parses a string to tensor and casts it to SimpleTensor.
     *
     * @param expression string to be parsed
     * @return simple tensor
     */
    public static SimpleTensor parseSimple(String expression) {
        Tensor t = parse(expression);
        if (!(t instanceof SimpleTensor))
            throw new IllegalArgumentException("Input tensor is not SimpleTensor.");
        return (SimpleTensor) t;
    }

    /**
     * Parses a string to tensor and casts it to Expression.
     *
     * @param expression expression to be parsed
     * @return expression object
     */
    public static Expression parseExpression(String expression) {
        Tensor t = parse(expression);
        if (!(t instanceof Expression))
            throw new IllegalArgumentException("Input tensor is not Expression.");
        return (Expression) t;
    }

    public static void addSymmetry(String tensor, IndexType type, boolean sign, int... permutation) {
        parseSimple(tensor).getIndices().getSymmetries().add(type.getType(), new Symmetry(permutation, sign));
    }

    public static void addSymmetry(SimpleTensor tensor, IndexType type, boolean sign, int... permutation) {
        tensor.getIndices().getSymmetries().add(type.getType(), new Symmetry(permutation, sign));
    }

    public static void addSymmetry(String tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, false, permutation);
    }

    public static void addSymmetry(SimpleTensor tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, false, permutation);
    }

    public static void addAntiSymmetry(String tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, true, permutation);
    }

    public static void addAntiSymmetry(SimpleTensor tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, true, permutation);
    }

    public static void addSymmetry(String tensor, int... permutation) {
        parseSimple(tensor).getIndices().getSymmetries().addSymmetry(permutation);
    }

    public static void addSymmetry(SimpleTensor tensor, int... permutation) {
        tensor.getIndices().getSymmetries().addSymmetry(permutation);
    }

    public static void addAntiSymmetry(String tensor, int... permutation) {
        parseSimple(tensor).getIndices().getSymmetries().addAntiSymmetry(permutation);
    }

    public static void addAntiSymmetry(SimpleTensor tensor, int... permutation) {
        tensor.getIndices().getSymmetries().addAntiSymmetry(permutation);
    }

    public static void setSymmetric(SimpleTensor tensor, IndexType type) {
        int dimension = tensor.getIndices().size(type);
        addSymmetry(tensor, type, false, Combinatorics.createCycle(dimension));
        addSymmetry(tensor, type, false, Combinatorics.createTransposition(dimension));
    }

    public static void setSymmetric(String tensor) {
        setSymmetric(parseSimple(tensor));
    }

    public static void setSymmetric(SimpleTensor tensor) {
        int dimension = tensor.getIndices().size();
        addSymmetry(tensor, Combinatorics.createCycle(dimension));
        addSymmetry(tensor, Combinatorics.createTransposition(dimension));
    }

    /**
     * Multiplies a tensor by minus one.
     *
     * @param tensor tensor to be negotiated
     * @return tensor of opposite sign
     */
    public static Tensor negate(Tensor tensor) {
        if (tensor instanceof Complex)
            return ((Complex) tensor).negate();
        return multiply(Complex.MINUS_ONE, tensor);
    }

    public static Tensor reciprocal(Tensor tensor) {
        return pow(tensor, Complex.MINUS_ONE);
    }
}
