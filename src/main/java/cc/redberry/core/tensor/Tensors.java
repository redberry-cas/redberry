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
import cc.redberry.core.parser.*;
import cc.redberry.core.tensor.functions.*;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.utils.TensorUtils;
import java.util.HashSet;
import java.util.Set;

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

    public static Tensor multiply(final Tensor... factors) {
        return ProductFactory.FACTORY.create(factors);
    }

    public static Tensor multiplyAndRenameConflictingDummies(Tensor... tensors) {
        Tensor t = multiply(tensors);
        if (!(t instanceof Product))
            return t;

        //postprocessing product
        Product p = (Product) t;
        //all product indices
        Set<Integer> totalIndices = new HashSet<>();
        int i, j;
        Indices indices = p.indices;
        for (i = indices.size() - 1; i >= 0; --i)
            totalIndices.add(IndicesUtils.getNameWithType(indices.get(i)));

        int[] forbidden;
        Tensor current;
        //processing indexless data
        for (i = 0; i < p.indexlessData.length; ++i) {
            current = p.indexlessData[i];
            if (current instanceof Sum || current instanceof Power) {
                forbidden = new int[totalIndices.size()];
                j = -1;
                for (Integer index : totalIndices)
                    forbidden[++j] = index;
                p.indexlessData[i] = ApplyIndexMapping.renameDummyFromClonedSource(current, forbidden);
                totalIndices.addAll(TensorUtils.getAllIndicesNames(p.indexlessData[i]));
            }
        }
        Set<Integer> free;
        for (i = 0; i < p.data.length; ++i) {
            current = p.data[i];
            if (current instanceof Sum || current instanceof Power) {
                free = new HashSet<>(current.getIndices().size());
                for (j = current.getIndices().size() - 1; j >= 0; --j)
                    free.add(IndicesUtils.getNameWithType(current.getIndices().get(j)));
                totalIndices.removeAll(free);
                forbidden = new int[totalIndices.size()];
                j = -1;
                for (Integer index : totalIndices)
                    forbidden[++j] = index;
                p.data[i] = ApplyIndexMapping.renameDummyFromClonedSource(current, forbidden);
                totalIndices.addAll(TensorUtils.getAllIndicesNames(p.data[i]));
            }
        }
        return p;
    }

    public static Tensor sum(Tensor... tensors) {
        return SumFactory.FACTORY.create(tensors);
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
        return ExpressionFactory.FACTORY.create(left, right);
    }

    public static Tensor sin(Tensor argument) {
        return Sin.SinFactory.FACTORY.create(argument);
    }

    public static Tensor cos(Tensor argument) {
        return Cos.CosFactory.FACTORY.create(argument);
    }

    public static Tensor tan(Tensor argument) {
        return Tan.TanFactory.FACTORY.create(argument);
    }

    public static Tensor cot(Tensor argument) {
        return Cot.CotFactory.FACTORY.create(argument);
    }

    public static Tensor arcsin(Tensor argument) {
        return ArcSin.ArcSinFactory.FACTORY.create(argument);
    }

    public static Tensor arccos(Tensor argument) {
        return ArcCos.ArcCosFactory.FACTORY.create(argument);
    }

    public static Tensor arctan(Tensor argument) {
        return ArcTan.ArcTanFactory.FACTORY.create(argument);
    }

    public static Tensor arccot(Tensor argument) {
        return ArcCot.ArcCotFactory.FACTORY.create(argument);
    }

    public static Tensor log(Tensor argument) {
        return Log.LogFactory.FACTORY.create(argument);
    }

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

    public static Tensor parse(String expression) {
        return CC.current().getParseManager().parse(expression);
    }

    //TODO improve API
    public static Tensor parse(String expression, ParseNodeTransformer... preprocessors) {
        return ParseManager.parse(expression, preprocessors);
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
        return multiply(Complex.MINUSE_ONE, tensor);
    }

    public static Tensor multiplySumElementsOnFactor(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = multiply(factor, sum.get(i));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFreeIndices()));
    }

    public static Tensor multiplySumElementsOnFactorAndExpandScalars(Sum sum, Tensor factor) {
        if (TensorUtils.isZero(factor))
            return Complex.ZERO;
        if (TensorUtils.isOne(factor))
            return sum;
        final Tensor[] newSumData = new Tensor[sum.size()];
        for (int i = newSumData.length - 1; i >= 0; --i)
            newSumData[i] = Expand.expand(multiply(factor, sum.get(i)));
        return new Sum(newSumData, IndicesFactory.createSorted(newSumData[0].getIndices().getFreeIndices()));
    }
}
