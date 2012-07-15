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
package cc.redberry.core.tensor;

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.functions.*;
import cc.redberry.core.transformations.expand.*;
import cc.redberry.core.utils.*;
import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Tensors {

    private Tensors() {
    }

    public static Tensor pow(Tensor argument, int power) {
        return pow(argument, new Complex(power));
    }

    public static Tensor pow(Tensor argument, Tensor power) {
        PowerBuilder pb = new PowerBuilder();
        pb.put(argument);
        pb.put(power);
        return pb.build();
    }

    private static Tensor multiplyComplexAndTensor(Complex complex, Tensor tensor) {
        if (tensor instanceof Complex)
            return ((Complex) tensor).multiply(complex);
        else if (complex.isZero())
            return Complex.ZERO;
        else if (complex.isOne())
            return tensor;
        else if (tensor instanceof Product) {
            Product p = (Product) tensor;
            return new Product(complex.multiply(p.factor), p.indexlessData, p.data, p.contentReference.get(), p.indices);
        } else if (tensor.getIndices().size() == 0)
            return new Product(complex, new Tensor[]{tensor}, new Tensor[0], ProductContent.EMPTY_INSTANCE, IndicesFactory.EMPTY_INDICES);
        else
            return new Product(complex, new Tensor[0], new Tensor[]{tensor}, null, IndicesFactory.createSorted(tensor.getIndices()));
    }

    private static Tensor multiplyIndexlessAndTensor(Tensor indexless, Tensor tensor) {

        assert indexless.getIndices().size() == 0;
        assert !(tensor instanceof Complex);
        assert !(indexless instanceof Complex);

        if (tensor instanceof Product && indexless instanceof Product) {
            Product p = (Product) tensor;
            if (p.indexlessData.length != 0)//TODO change condition if collect power strategy will change, e.g. a*Power[a_m^m,2] & a_m^m*g_ab
                return buildProduct(indexless, tensor);
            Product ip = (Product) indexless;
            return new Product(p.factor.multiply(ip.factor), ip.indexlessData, p.data, p.contentReference.get(), p.indices);
        }
        if (indexless instanceof Product) {
            Product ip = (Product) indexless;
            if (tensor.getIndices().size() == 0)
                return buildProduct(indexless, tensor);
            else
                return new Product(ip.factor, ip.indexlessData, new Tensor[]{tensor}, null, tensor.getIndices());
        }
        if (tensor instanceof Product) {
            Product p = (Product) tensor;
            if (p.indexlessData.length == 0)
                return new Product(p.factor, new Tensor[]{indexless}, p.data, p.contentReference.get(), p.indices);
            else
                return buildProduct(indexless, tensor);
        }
        if (tensor.getIndices().size() == 0)
            return buildProduct(indexless, tensor);
        return new Product(Complex.ONE, new Tensor[]{indexless}, new Tensor[]{tensor}, null, IndicesFactory.createSorted(tensor.getIndices()));
    }

    private static Tensor multiplyPair(Tensor t1, Tensor t2) {
        if (t1 instanceof Complex)
            return multiplyComplexAndTensor((Complex) t1, t2);
        if (t2 instanceof Complex)
            return multiplyComplexAndTensor((Complex) t2, t1);

        if (t1.getIndices().size() == 0)
            return multiplyIndexlessAndTensor(t1, t2);
        if (t2.getIndices().size() == 0)
            return multiplyIndexlessAndTensor(t2, t1);

        return buildProduct(t1, t2);
    }

    private static Tensor buildProduct(Tensor... tensors) {
        //we want initialize product builder in the most efficient way
        Tensor maxProduct = null;
        for (Tensor t : tensors)
            if (t instanceof Product)
                if (maxProduct == null || t.size() > maxProduct.size())
                    maxProduct = t;

        ProductBuilder builder;
        if (maxProduct != null)
            builder = new ProductBuilder((Product) maxProduct);
        else
            builder = new ProductBuilder();

        for (Tensor t : tensors)
            if (t != maxProduct)
                builder.put(t);
        return builder.build();
    }

    private static Tensor buildProductRenamingConflictingIndices(Tensor... tensors) {
        ProductBuilderResolvingConfilcts builder = new ProductBuilderResolvingConfilcts();
        for (Tensor t : tensors)
            builder.put(t);
        return builder.build();
    }

    private static Tensor tryToMultiplyComplexFactors(final Tensor... factors) {
        Complex complex = Complex.ONE;
        Tensor nonComplex = null;
        boolean allComplex = true;
        for (Tensor tensor : factors)
            if (tensor instanceof Complex)
                complex = complex.multiply((Complex) tensor);
            else if (nonComplex != null)
                allComplex = false;
            else
                nonComplex = tensor;
        if (complex.isZero() || complex.isNaN() || complex.isInfinite())
            return complex;
        if (allComplex)
            if (nonComplex == null)
                return complex;
            else
                return multiplyComplexAndTensor(complex, nonComplex);
        return null;
    }

    public static Tensor multiply(final Tensor... factors) {
        if (factors.length == 0)
            return Complex.ONE;
        if (factors.length == 1)
            return factors[0];
        if (factors.length == 2)
            return multiplyPair(factors[0], factors[1]);

        Tensor temp = tryToMultiplyComplexFactors(factors);
        if (temp != null)
            return temp;

        return buildProduct(factors);
    }

    public static Tensor multiplyAndRenameConflictingDummies(final Tensor... factors) {
        Tensor temp = tryToMultiplyComplexFactors(factors);
        if (temp != null)
            return temp;
        return buildProductRenamingConflictingIndices(factors);
    }

    public static Tensor sum(Tensor... tensors) {
        TensorBuilder sb = SumBuilderFactory.defaultSumBuilder(tensors.length);
        for (Tensor t : tensors)
            sb.put(t);
        return sb.build();
    }

    public static SimpleTensor simpleTensor(String name, SimpleIndices indices) {
        NameDescriptor descriptor = CC.getNameManager().mapNameDescriptor(name, indices.getIndicesTypeStructure());
        return new SimpleTensor(descriptor.getId(),
                                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(),
                                                                    indices));
    }

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

    public static TensorField field(String name, SimpleIndices indices, Tensor[] arguments) {
        SimpleIndices[] argIndices = new SimpleIndices[arguments.length];
        for (int i = 0; i < argIndices.length; ++i)
            argIndices[i] = IndicesFactory.createSimple(null, arguments[i].getIndices().getFreeIndices());
        return field(name, indices, argIndices, arguments);
    }

    public static TensorField field(String name, SimpleIndices indices, SimpleIndices[] argIndices, Tensor[] arguments) {
        if (argIndices.length != arguments.length)
            throw new IllegalArgumentException("Argument indices array and arguments array have different length.");
        if (arguments.length == 0)
            throw new IllegalArgumentException("No arguments in field.");
        for (int i = 0; i < argIndices.length; ++i)
            if (!arguments[i].getIndices().getFreeIndices().equalsRegardlessOrder(argIndices[i]))
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
            if (!arguments[i].getIndices().getFreeIndices().equalsRegardlessOrder(argIndices[i]))
                throw new IllegalArgumentException("Arguments indices are inconsistent with arguments.");
        }
        return new TensorField(name,
                               UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                               arguments, argIndices);
    }

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
            argIndices[i] = IndicesFactory.createSimple(null, arguments[i].getIndices().getFreeIndices());
        return new TensorField(name,
                               UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                               arguments, argIndices);
    }

    public static TensorField setIndicesToField(TensorField field, SimpleIndices newIndices) {
        NameDescriptor descriptor = CC.getNameDescriptor(field.name);
        if (!descriptor.getIndicesTypeStructure().isStructureOf(newIndices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        return new TensorField(field.name, newIndices, field.args, field.argIndices);
    }

    public static SimpleTensor setIndicesToSimpleTensor(SimpleTensor simpleTensor, SimpleIndices newIndices) {
        NameDescriptor descriptor = CC.getNameDescriptor(simpleTensor.name);
        if (!descriptor.getIndicesTypeStructure().isStructureOf(newIndices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        return new SimpleTensor(simpleTensor.name, UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), newIndices));
    }

    public static Expression expression(Tensor left, Tensor right) {
        ExpressionBuilder eb = new ExpressionBuilder();
        eb.put(left);
        eb.put(right);
        return eb.build();
    }

    public static Expression expression(String expression) {
        return (Expression) parse(expression);
    }

    public static Tensor sin(Tensor argument) {
        return new Sin.SinBuilder().eval(argument);
    }

    public static Tensor cos(Tensor argument) {
        return new Cos.CosBuilder().eval(argument);
    }

    public static Tensor tan(Tensor argument) {
        return new Tan.TanBuilder().eval(argument);
    }

    public static Tensor cot(Tensor argument) {
        return new Cot.CotBuilder().eval(argument);
    }

    public static Tensor arcsin(Tensor argument) {
        return new ArcSin.ArcSinBuilder().eval(argument);
    }

    public static Tensor arccos(Tensor argument) {
        return new ArcCos.ArcCosBuilder().eval(argument);
    }

    public static Tensor arctan(Tensor argument) {
        return new ArcTan.ArcTanBuilder().eval(argument);
    }

    public static Tensor arccot(Tensor argument) {
        return new ArcCot.ArcCotBuilder().eval(argument);
    }

    public static Tensor log(Tensor argument) {
        return new Log.LogBuilder().eval(argument);
    }

    public static Tensor exp(Tensor argument) {
        return new Exp.ExpBuilder().eval(argument);
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
        return isKronecker(t) || isMetric(t);
    }

    public static Tensor parse(String expression) {
        return CC.current().getParseManager().parse(expression);
    }

    public static SimpleTensor parseSimple(String expression) {
        Tensor t = CC.current().getParseManager().parse(expression);
        if (!(t instanceof SimpleTensor))
            throw new IllegalArgumentException("Input tensor is not SimpleTensor.");
        return (SimpleTensor) t;
    }

    public static void addSymmetry(String tensor, IndexType type, boolean sign, int... symmetry) {
        parseSimple(tensor).getIndices().getSymmetries().add(type.getType(), new Symmetry(symmetry, sign));
    }

    public static void addSymmetry(SimpleTensor tensor, IndexType type, boolean sign, int... symmetry) {
        tensor.getIndices().getSymmetries().add(type.getType(), new Symmetry(symmetry, sign));
    }

    public static Tensor negate(Tensor tensor) {
        if (tensor instanceof Complex)
            return ((Complex) tensor).negate();
        return multiplyComplexAndTensor(Complex.MINUSE_ONE, tensor);
    }

    public static Tensor multiplySumElementsOnFactor(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiplyPair(factor, sum.get(i));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFreeIndices()));
    }

    public static Tensor multiplySumElementsOnFactorAndExpandScalars(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = ExpandBrackets.expandBrackets(multiplyPair(factor, sum.get(i)));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFreeIndices()));
    }
}
