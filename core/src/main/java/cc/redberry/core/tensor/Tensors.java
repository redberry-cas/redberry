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
package cc.redberry.core.tensor;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.context.NameDescriptorForSimpleTensor;
import cc.redberry.core.context.NameDescriptorForTensorField;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParseTokenTransformer;
import cc.redberry.core.tensor.functions.*;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Factory methods to create tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class Tensors {

    private Tensors() {
    }

    /*********************************************************************************
     ******************************* Math operations *********************************
     *********************************************************************************/

    /**
     * Power function. Returns tensor raised to specified integer power.
     *
     * @param argument base
     * @param power    power
     * @return result of argument exponentiation
     * @throws IllegalArgumentException if argument is not scalar
     */
    public static Tensor pow(Tensor argument, int power) {
        return pow(argument, new Complex(power));
    }

    /**
     * Power function. Returns tensor raised to specified integer power.
     *
     * @param argument base
     * @param power    power
     * @return result of argument exponentiation
     * @throws IllegalArgumentException if argument is not scalar
     */
    public static Tensor pow(Tensor argument, java.math.BigInteger power) {
        return pow(argument, new Complex(power));
    }

    /**
     * Power function. Returns tensor raised to specified power.
     *
     * @param argument base
     * @param power    power
     * @return result of argument exponentiation
     * @throws IllegalArgumentException if argument or power is not scalar
     */
    public static Tensor pow(Tensor argument, Tensor power) {
        PowerBuilder pb = new PowerBuilder();
        pb.put(argument);
        pb.put(power);
        return pb.build();
    }

    /**
     * Returns the result of multiplication of specified tensors. Einstein notation
     * assumed. If there is a chance that some factors have conflicting
     * (same name) dummy indices use {@link #multiplyAndRenameConflictingDummies(Tensor...)}
     * instead.
     *
     * @param factors array of factors to be multiplied
     * @return result of multiplication
     * @throws InconsistentIndicesException if there is indices clash
     */
    public static Tensor multiply(final Tensor... factors) {
        //TODO add check for indices consistency
        return ProductFactory.FACTORY.create(factors);
    }

    /**
     * Returns the result of multiplication of specified tensors. Einstein notation
     * assumed. If there is a chance that some factors have conflicting
     * (same name) dummy indices use {@link #multiplyAndRenameConflictingDummies(Tensor...)}
     * instead.
     *
     * @param factors collection of factors to be multiplied
     * @return result of multiplication
     * @throws InconsistentIndicesException if there is indices clash
     */
    public static Tensor multiply(final Collection<Tensor> factors) {
        //TODO add check for indices consistency
        return multiply(factors.toArray(new Tensor[factors.size()]));
    }

    /**
     * Returns result of multiplication of specified tensors taking care about
     * all conflicting dummy indices in factors. Einstein notation assumed.
     *
     * @param factors array of factors to be multiplied
     * @return result of multiplication
     * @throws InconsistentIndicesException if there is indices clash
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

    /**
     * Returns result of multiplication of specified tensors taking care about
     * all conflicting dummy indices in factors. Einstein notation assumed.
     *
     * @param factors array of factors to be multiplied
     * @return result of multiplication
     * @throws InconsistentIndicesException if there is indices clash
     */
    public static Tensor multiplyAndRenameConflictingDummies(Collection<Tensor> factors) {
        return multiplyAndRenameConflictingDummies(factors.toArray(new Tensor[factors.size()]));
    }

    /**
     * Renames dummies in specified tensors, such that it becomes safe to multiply them.
     *
     * @param factors tensors
     * @return the array of tensors with renamed dummy indices
     */
    public static Tensor[] resolveDummy(Tensor... factors) {
        Tensor[] result = new Tensor[factors.length];
        TIntHashSet forbidden = new TIntHashSet();
        ArrayList<Tensor> toResolve = new ArrayList<>();
        //int position = -1;
        int i;
        Tensor f;
        for (i = factors.length - 1; i >= 0; --i) {
            if ((f = factors[i]) instanceof MultiTensor || f.getIndices().getFree().size() == 0) {
                toResolve.add(f);
                forbidden.addAll(IndicesUtils.getIndicesNames(f.getIndices().getFree()));
            } else {
                forbidden.addAll(TensorUtils.getAllIndicesNamesT(f));
                result[i] = f;
            }
        }

        Tensor factor, newFactor;
        int toResolvePosition = toResolve.size();
        for (i = factors.length - 1; i >= 0; --i)
            if (result[i] == null) {
                factor = toResolve.get(--toResolvePosition);
                newFactor = ApplyIndexMapping.renameDummy(factor, forbidden.toArray());
                forbidden.addAll(TensorUtils.getAllIndicesNamesT(newFactor));
                result[i] = newFactor;
            }
//        for (int i = toResolve.size() - 1; i >= 0; --i) {
//            factor = toResolve.get(i);
//            newFactor = ApplyIndexMapping.renameDummy(factor, forbidden.toArray());
//            forbidden.addAll(TensorUtils.getAllIndicesNamesT(newFactor));
//            result[++position] = newFactor;
//        }
        return result;
    }

    public static void resolveAllDummies(Tensor[] factors) {
        TIntHashSet forbidden = new TIntHashSet();
        int i;
        for (i = factors.length - 1; i >= 0; --i)
            forbidden.addAll(TensorUtils.getAllIndicesNamesT(factors[i]));

        for (i = factors.length - 1; i >= 0; --i) {
            factors[i] = ApplyIndexMapping.renameDummy(factors[i], forbidden.toArray());
            forbidden.addAll(TensorUtils.getAllIndicesNamesT(factors[i]));
        }
    }

    /**
     * Returns {@code a} divided by {@code b}.
     *
     * @param a tensor
     * @param b scalar tensor
     * @return {@code a} divided by {@code b}.
     * @throws IllegalArgumentException if b is not scalar
     */
    public static Tensor divide(Tensor a, Tensor b) {
        return multiply(a, reciprocal(b));
    }

    /**
     * Returns the result of summation of several tensors.
     *
     * @param tensors array of summands
     * @return result of summation
     * @throws TensorException if tensors have different free indices
     */
    public static Tensor sum(Tensor... tensors) {
        return SumFactory.FACTORY.create(tensors);
    }

    /**
     * Returns the result of summation of several tensors.
     *
     * @param tensors collection of summands
     * @return result of summation
     * @throws TensorException if tensors have different free indices
     */
    public static Tensor sum(Collection<Tensor> tensors) {
        return sum(tensors.toArray(new Tensor[tensors.size()]));
    }

    /**
     * Subtracts {@code b} from {@code a}
     *
     * @param a tensor
     * @param b tensor
     * @return {@code a} - {@code b}
     * @throws TensorException if tensors have different free indices
     */
    public static Tensor subtract(Tensor a, Tensor b) {
        return sum(a, negate(b));
    }


    /**
     * Multiplies specified tensor by minus one.
     *
     * @param tensor tensor to be negotiated
     * @return tensor negate to specified one
     */
    public static Tensor negate(Tensor tensor) {
        if (tensor instanceof Complex)
            return ((Complex) tensor).negate();
        return multiply(Complex.MINUS_ONE, tensor);
    }

    /**
     * Returns reciprocal of the specified tensor, i.e. one divided by it.
     *
     * @param tensor tensor
     * @return reciprocal of the specified tensor
     * @throws IllegalArgumentException if specified tensor is not scalar
     */
    public static Tensor reciprocal(Tensor tensor) {
        return pow(tensor, Complex.MINUS_ONE);
    }


    /*********************************************************************************
     ********************************* Simple tensors ********************************
     *********************************************************************************/


    /**
     * Returns new simple tensor with specified string name and indices.
     *
     * @param name    string name of the tensor
     * @param indices indices
     * @return new instance of {@link SimpleTensor} object
     */
    public static SimpleTensor simpleTensor(String name, SimpleIndices indices) {
        NameDescriptor descriptor = CC.getNameManager().mapNameDescriptor(name, indices.getStructureOfIndices());
        if (indices.size() == 0) {
            assert indices == IndicesFactory.EMPTY_SIMPLE_INDICES;

            NameDescriptorForSimpleTensor nst = (NameDescriptorForSimpleTensor) descriptor;
            if (nst.getCachedSymbol() == null) {
                SimpleTensor st;
                nst.setCachedInstance(st = new SimpleTensor(descriptor.getId(), indices));
                return st;
            } else
                return nst.getCachedSymbol();
        }
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
        if (!descriptor.getStructureOfIndices().isStructureOf(indices))
            throw new IllegalArgumentException("Specified indices ( " + indices + " )are not indices of specified tensor ( " + descriptor + " ).");

        if (indices.size() == 0) {
            assert indices == IndicesFactory.EMPTY_SIMPLE_INDICES;

            NameDescriptorForSimpleTensor nst = (NameDescriptorForSimpleTensor) descriptor;
            if (nst.getCachedSymbol() == null) {
                SimpleTensor st;
                nst.setCachedInstance(st = new SimpleTensor(descriptor.getId(), indices));
                return st;
            } else
                return nst.getCachedSymbol();
        }

        return new SimpleTensor(name,
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(),
                        indices));
    }

    /**
     * Creates 1-st derivative with respect to specified argument of specified tensor field with specified derivative
     * indices
     *
     * @param parent            tensor field
     * @param derivativeIndices indices of the var (inverted)
     * @param argPosition       argument
     * @return 1-st derivative
     */
    public static TensorField fieldDerivative(TensorField parent, SimpleIndices derivativeIndices, final int argPosition) {
        return fieldDerivative(parent, derivativeIndices, argPosition, 1);
    }

    /**
     * Creates n-th derivative with respect to specified argument of specified tensor field with specified derivative
     * indices
     *
     * @param parent            tensor field
     * @param derivativeIndices indices of the var (inverted)
     * @param argPosition       argument
     * @param order             order of derivative
     * @return n-th derivative
     */
    public static TensorField fieldDerivative(TensorField parent, SimpleIndices derivativeIndices,
                                              final int argPosition, final int order) {

        if (!derivativeIndices.getStructureOfIndices().equals(parent.argIndices[argPosition].getInverted().getStructureOfIndices().pow(order)))
            throw new IllegalArgumentException("Illegal derivative indices.");

        int[] orders = new int[parent.size()];
        orders[argPosition] = order;
        NameDescriptorForTensorField fieldDescriptor = parent.getNameDescriptor();
        NameDescriptor derivativeDescriptor = fieldDescriptor.getDerivative(orders);

        SimpleIndices totalIndices;

        if (!fieldDescriptor.isDerivative() || derivativeIndices.size() == 0 || parent.indices.size() == 0) {
            totalIndices = new SimpleIndicesBuilder().append(parent.getIndices()).append(derivativeIndices).getIndices();
        } else {
            orders = fieldDescriptor.getDerivativeOrders();

            SimpleIndicesBuilder ib = new SimpleIndicesBuilder();
            StructureOfIndices[] structures = fieldDescriptor.getStructuresOfIndices();
            int i, from;
            SimpleIndices singleType;
            IndexType eType;
            for (byte type = IndexType.TYPES_COUNT - 1; type >= 0; --type) {
                eType = IndexType.values()[type];
                singleType = parent.getIndices().getOfType(eType);
                from = fieldDescriptor.getParent().getStructureOfIndices().getTypeData(type).length;
                for (i = 0; i <= argPosition; ++i)
                    from += structures[i + 1].getTypeData(type).length * orders[i];
                for (i = 0; i < from; ++i)
                    ib.append(singleType.get(i));
                ib.append(derivativeIndices.getOfType(eType));
                for (; i < singleType.size(); ++i)
                    ib.append(singleType.get(i));
            }
            totalIndices = ib.getIndices();
        }

        return new TensorField(derivativeDescriptor.getId(),
                UnsafeIndicesFactory.createOfTensor(derivativeDescriptor.getSymmetries(), totalIndices),
                parent.args, parent.argIndices);
    }

    /**
     * Returns new tensor field derivative with specified string name, indices, arguments, derivative orders and
     * explicit argument indices bindings.
     *
     * @param name       string name of the corresponding tensor field
     * @param indices    total indices of resulting derivative (field indices + indices of vars)
     * @param argIndices argument indices bindings
     * @param arguments  arguments list
     * @param orders     orders of derivatives
     * @return new instance of {@link TensorField} object
     */
    public static TensorField fieldDerivative(String name, SimpleIndices indices, final SimpleIndices[] argIndices,
                                              final Tensor[] arguments, final int[] orders) {
        if (argIndices.length != arguments.length)
            throw new IllegalArgumentException("Argument indices array and arguments array have different length.");
        if (arguments.length == 0)
            throw new IllegalArgumentException("No arguments in field.");
        for (int i = 0; i < argIndices.length; ++i)
            if (!arguments[i].getIndices().getFree().equalsRegardlessOrder(argIndices[i]))
                throw new IllegalArgumentException("Arguments indices are inconsistent with arguments.");

        try {
            StructureOfIndices[] structures = new StructureOfIndices[argIndices.length + 1];
            StructureOfIndices structureOfIndices = indices.getStructureOfIndices();
            int i, j;
            for (i = argIndices.length - 1; i >= 0; --i) {
                structures[i + 1] = argIndices[i].getStructureOfIndices();
                for (j = orders[i]; j > 0; --j)
                    structureOfIndices = structureOfIndices.subtract(structures[i + 1]);
            }

            structures[0] = structureOfIndices;

            NameDescriptorForTensorField fieldDescriptor =
                    (NameDescriptorForTensorField) CC.getNameManager().mapNameDescriptor(name, structures);

            NameDescriptor derivativeDescriptor = fieldDescriptor.getDerivative(orders);

            return new TensorField(derivativeDescriptor.getId(),
                    UnsafeIndicesFactory.createOfTensor(derivativeDescriptor.getSymmetries(), indices),
                    arguments, argIndices);
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("Inconsistent derivative orders/indices.", re);
        }
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
     * Returns new tensor field with specified string name, indices and
     * arguments list. Free indices of arguments assumed as arguments indices
     * bindings of this field bindings.
     *
     * @param name      int name of the field
     * @param indices   indices
     * @param arguments arguments list
     * @return new instance of {@link TensorField} object
     */
    public static TensorField field(String name, SimpleIndices indices, Collection<Tensor> arguments) {
        return field(name, indices, arguments.toArray(new Tensor[arguments.size()]));
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

        StructureOfIndices[] structures = new StructureOfIndices[argIndices.length + 1];
        structures[0] = indices.getStructureOfIndices();
        for (int i = 0; i < argIndices.length; ++i)
            structures[i + 1] = argIndices[i].getStructureOfIndices();
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
        if (descriptor.getStructuresOfIndices().length - 1 != argIndices.length)
            throw new IllegalArgumentException("This name corresponds to field with different number of arguments.");
        if (!descriptor.getStructureOfIndices().isStructureOf(indices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        for (int i = 0; i < argIndices.length; ++i) {
            if (!descriptor.getStructuresOfIndices()[i + 1].isStructureOf(argIndices[i]))
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
        if (!descriptor.getStructureOfIndices().isStructureOf(indices))
            throw new IllegalArgumentException("Specified indices are not indices of specified tensor.");
        SimpleIndices[] argIndices = new SimpleIndices[arguments.length];
        for (int i = 0; i < arguments.length; ++i)
            argIndices[i] = IndicesFactory.createSimple(null, arguments[i].getIndices().getFree());
        return new TensorField(name,
                UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                arguments, argIndices);
    }

    /**
     * Returns an instance of specified simple tensor with specified indices
     *
     * @param tensor  simple tensor
     * @param indices indices
     * @return instance of specified simple tensor with specified indices
     */
    public static SimpleTensor setIndices(SimpleTensor tensor, int[] indices) {
        return setIndices(tensor, IndicesFactory.createSimple(null, indices));
    }

    /**
     * Returns an instance of specified simple tensor with specified indices
     *
     * @param tensor  simple tensor
     * @param indices indices
     * @return instance of specified simple tensor with specified indices
     */
    public static SimpleTensor setIndices(SimpleTensor tensor, SimpleIndices indices) {
        if (tensor.getIndices() == indices) return tensor;

        NameDescriptor descriptor = tensor.getNameDescriptor();
        if (!descriptor.getStructureOfIndices().isStructureOf(indices))
            throw new IllegalArgumentException(String.format("Illegal structure of indices (tensor = %s, indices = %s).", tensor, indices));

        if (indices.size() == 0)
            return tensor;

        if (descriptor.isField())
            return new TensorField(tensor.name,
                    UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(), indices),
                    ((TensorField) tensor).args, ((TensorField) tensor).argIndices);
        else
            return new SimpleTensor(tensor.name,
                    UnsafeIndicesFactory.createOfTensor(descriptor.getSymmetries(),
                            indices));
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

    /*********************************************************************************
     ****************************** Scalar functions *********************************
     *********************************************************************************/

    /**
     * Creates a sinus object from scalar argument.
     *
     * @param argument scalar argument of sinus
     * @return sinus of argument
     */
    public static Tensor sin(Tensor argument) {
        return Sin.SinFactory.FACTORY.create(argument);
    }

    /**
     * Creates a cosine object from scalar argument.
     *
     * @param argument scalar argument of cosine
     * @return cosine of argument
     */
    public static Tensor cos(Tensor argument) {
        return Cos.CosFactory.FACTORY.create(argument);
    }

    /**
     * Creates a tangent object from scalar argument
     *
     * @param argument scalar argument of tangent
     * @return tangent of argument
     */
    public static Tensor tan(Tensor argument) {
        return Tan.TanFactory.FACTORY.create(argument);
    }

    /**
     * Creates a cotangent object from scalar argument.
     *
     * @param argument scalar argument of cotangent
     * @return cotangent of argument
     */
    public static Tensor cot(Tensor argument) {
        return Cot.CotFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arcsinus object from scalar argument.
     *
     * @param argument scalar argument of arcsinus
     * @return arcsinus of argument
     */
    public static Tensor arcsin(Tensor argument) {
        return ArcSin.ArcSinFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arccosine object from scalar argument.
     *
     * @param argument scalar argument of arccosine
     * @return arccosine of argument
     */
    public static Tensor arccos(Tensor argument) {
        return ArcCos.ArcCosFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arctangent object from scalar argument.
     *
     * @param argument scalar argument of arctangent
     * @return arctangent of argument
     */
    public static Tensor arctan(Tensor argument) {
        return ArcTan.ArcTanFactory.FACTORY.create(argument);
    }

    /**
     * Creates a arcotangent object from scalar argument.
     *
     * @param argument scalar argument of arccotangent
     * @return arcotangent of argument
     */
    public static Tensor arccot(Tensor argument) {
        return ArcCot.ArcCotFactory.FACTORY.create(argument);
    }

    /**
     * Creates a natural logarithm object from scalar argument.
     *
     * @param argument scalar argument of logarithm
     * @return natural logarithm of argument
     */
    public static Tensor log(Tensor argument) {
        return Log.LogFactory.FACTORY.create(argument);
    }

    /**
     * Creates a exponent object from scalar argument.
     *
     * @param argument scalar argument of exponent
     * @return exponent of argument
     */
    public static Tensor exp(Tensor argument) {
        return Exp.ExpFactory.FACTORY.create(argument);
    }


    /*********************************************************************************
     ******************************* Metric and Kronecker ****************************
     *********************************************************************************/

    /**
     * Returns Kronecker tensor with specified upper and lower indices.
     *
     * @param index1 first index
     * @param index2 second index
     * @return Kronecker tensor with specified upper and lower indices
     * @throws IllegalArgumentException if indices have same states
     * @throws IllegalArgumentException if indices have different types
     */
    public static SimpleTensor createKronecker(int index1, int index2) {
        return CC.current().createKronecker(index1, index2);
    }

    /**
     * Returns metric tensor with specified indices.
     *
     * @param index1 first index
     * @param index2 second index
     * @return metric tensor with specified indices
     * @throws IllegalArgumentException if indices have different states
     * @throws IllegalArgumentException if indices have different types
     * @throws IllegalArgumentException if indices have non metric types
     */
    public static SimpleTensor createMetric(int index1, int index2) {
        return CC.current().createMetric(index1, index2);
    }

    /**
     * Returns metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states.
     *
     * @param index1 first index
     * @param index2 second index
     * @return metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states
     * @throws IllegalArgumentException if indices have different types
     * @throws IllegalArgumentException if indices have same states and non metric types
     */
    public static SimpleTensor createMetricOrKronecker(int index1, int index2) {
        return CC.current().createMetricOrKronecker(index1, index2);
    }


    /**
     * Returns metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states.
     *
     * @param indices indices
     * @return metric tensor if specified indices have same states and
     * Kronecker tensor if specified indices have different states
     * @throws IllegalArgumentException if indices have different types
     * @throws IllegalArgumentException if indices have same states and non metric types
     * @throws IllegalArgumentException if indices.size() != 2
     */
    public static SimpleTensor createMetricOrKronecker(Indices indices) {
        if (indices.size() != 2)
            throw new IllegalArgumentException();
        return CC.current().createMetricOrKronecker(indices.get(0), indices.get(1));
    }

    /**
     * Returns {@code true} if specified tensor is Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is Kronecker tensor
     */
    public static boolean isKronecker(Tensor t) {
        if (!(t instanceof SimpleTensor))
            return false;
        return CC.current().isKronecker((SimpleTensor) t);
    }

    /**
     * Returns {@code true} if specified tensor is metric tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is metric tensor
     */
    public static boolean isMetric(Tensor t) {
        if (!(t instanceof SimpleTensor))
            return false;
        return CC.current().isMetric((SimpleTensor) t);
    }

    /**
     * Returns {@code true} if specified tensor is metric or Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is metric or Kronecker tensor
     */
    public static boolean isKroneckerOrMetric(Tensor t) {
        if (!(t instanceof SimpleTensor))
            return false;
        return CC.current().isKroneckerOrMetric((SimpleTensor) t);
    }

    /**
     * Returns {@code true} if specified tensor is metric or Kronecker tensor
     *
     * @param t tensor
     * @return {@code true} if specified tensor is metric or Kronecker tensor
     */
    public static boolean isKroneckerOrMetric(SimpleTensor t) {
        return CC.current().isKroneckerOrMetric(t);
    }


    /*********************************************************************************
     ********************************* Parse methods *********************************
     *********************************************************************************/


    /**
     * Converts string expression into tensor.
     *
     * @param expression string to be parsed
     * @return result of parsing
     * @throws cc.redberry.core.parser.ParserException if expression does not satisfy correct Redberry
     *                                                 input notation for tensors
     */
    public static Tensor parse(String expression) {
        return CC.current().getParseManager().parse(expression);
    }

    /**
     * Dummy method
     */
    public static Tensor parse(Tensor expression) {
        return expression;
    }

    /**
     * Converts int value to Complex.
     *
     * @param i integer
     * @return same Complex
     */
    public static Tensor parse(int i) {
        return new Complex(i);
    }

    /**
     * Converts long value to Complex.
     *
     * @param i long integer
     * @return same Complex
     */
    public static Tensor parse(long i) {
        return new Complex(i);
    }

    /**
     * Converts double value to Complex.
     *
     * @param i double
     * @return same Complex
     */
    public static Tensor parse(double i) {
        return new Complex(i);
    }

    /**
     * Converts float value to Complex.
     *
     * @param i float
     * @return same Complex
     */
    public static Tensor parse(float i) {
        return new Complex(i);
    }

    /**
     * Converts array of string expressions into array of tensors.
     *
     * @param expressions array of strings to be parsed
     * @return array of parsed tensors
     * @throws cc.redberry.core.parser.ParserException if expression does not satisfy correct Redberry
     *                                                 input notation for tensors
     */
    public static Tensor[] parse(final String... expressions) {
        Tensor[] r = new Tensor[expressions.length];
        for (int i = 0; i < expressions.length; ++i)
            r[i] = parse(expressions[i]);
        return r;
    }

    /**
     * Converts string expression into tensor, additionally transforming AST according to specified
     * AST transformers.
     *
     * @param expression    string to be parsed
     * @param preprocessors AST transformers
     * @return result of parsing
     * @throws cc.redberry.core.parser.ParserException if expression does not satisfy correct Redberry
     *                                                 input notation for tensors
     */
    public static Tensor parse(String expression, ParseTokenTransformer... preprocessors) {
        return CC.current().getParseManager().parse(expression, preprocessors);
    }

    /**
     * Converts a string into simple tensor.
     *
     * @param expression string to be parsed
     * @return simple tensor
     * @throws IllegalArgumentException                if expression does not represents simple tensor
     * @throws cc.redberry.core.parser.ParserException if expression does not satisfy correct Redberry
     *                                                 input notation for tensors
     */
    public static SimpleTensor parseSimple(String expression) {
        Tensor t = parse(expression);
        if (!(t instanceof SimpleTensor))
            throw new IllegalArgumentException("Input tensor is not SimpleTensor.");
        return (SimpleTensor) t;
    }

    /**
     * Converts a string into {@link Expression}.
     *
     * @param expression string to be parsed
     * @return simple tensor
     * @throws IllegalArgumentException                if string expression does not represents {@link Expression}
     * @throws cc.redberry.core.parser.ParserException if expression does not satisfy correct Redberry
     *                                                 input notation for tensors
     */
    public static Expression parseExpression(String expression) {
        Tensor t = parse(expression);
        if (!(t instanceof Expression))
            throw new IllegalArgumentException("Input tensor is not Expression.");
        return (Expression) t;
    }


    /*********************************************************************************
     ********************************* Symmetries ************************************
     *********************************************************************************/

    /**
     * Attaches symmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      string representation of simple tensor
     * @param type        type of indices
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if degree of permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addSymmetry(String tensor, IndexType type, Permutation permutation) {
        addSymmetry(parseSimple(tensor), type, permutation);
    }

    /**
     * Attaches symmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      simple tensor
     * @param type        type of indices
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addSymmetry(SimpleTensor tensor, IndexType type, Permutation permutation) {
        tensor.getIndices().getSymmetries().addSymmetry(type.getType(), permutation);
    }

    /**
     * Attaches symmetry to simple tensor.
     *
     * @param tensor      simple tensor
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addSymmetry(SimpleTensor tensor, Permutation permutation) {
        tensor.getIndices().getSymmetries().addSymmetry(permutation);
    }

    /**
     * Attaches symmetry to simple tensor.
     *
     * @param tensor      string representation of simple tensor
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addSymmetry(String tensor, Permutation permutation) {
        addSymmetry(parseSimple(tensor), permutation);
    }

    /**
     * Attaches symmetries to simple tensor.
     *
     * @param tensor       simple tensor
     * @param permutations permutations
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if degree of some permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addSymmetries(SimpleTensor tensor, Permutation... permutations) {
        for (Permutation p : permutations)
            tensor.getIndices().getSymmetries().addSymmetry(p);
    }

    /**
     * Attaches symmetris to simple tensor.
     *
     * @param tensor       string representation of simple tensor
     * @param permutations permutations
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if degree of some permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addSymmetries(String tensor, Permutation... permutations) {
        addSymmetries(parseSimple(tensor), permutations);
    }

    /**
     * Attaches symmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      string representation of simple tensor
     * @param permutation permutation
     * @param sign        {@code true} for antisymmetry, {@code false} for symmetry
     * @param type        type of indices
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd and sign is {@code true}
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addSymmetry(String tensor, IndexType type, boolean sign, int... permutation) {
        parseSimple(tensor).getIndices().getSymmetries().add(type.getType(), sign, permutation);
    }

    /**
     * Attaches symmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      simple tensor
     * @param permutation permutation
     * @param sign        {@code true} for antisymmetry, {@code false} for symmetry
     * @param type        type of indices
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd and sign is {@code true}
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addSymmetry(SimpleTensor tensor, IndexType type, boolean sign, int... permutation) {
        tensor.getIndices().getSymmetries().add(type.getType(), sign, permutation);
    }

    /**
     * Attaches symmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      string representation of simple tensor
     * @param permutation permutation
     * @param type        type of indices
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addSymmetry(String tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, false, permutation);
    }

    /**
     * Attaches symmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      simple tensor
     * @param permutation permutation
     * @param type        type of indices
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addSymmetry(SimpleTensor tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, false, permutation);
    }

    /**
     * Attaches antisymmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      string representation of simple tensor
     * @param permutation permutation
     * @param type        type of indices
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addAntiSymmetry(String tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, true, permutation);
    }

    /**
     * Attaches antisymmetry to simple tensor with respect to indices of specified type.
     *
     * @param tensor      simple tensor
     * @param permutation permutation
     * @param type        type of indices
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public static void addAntiSymmetry(SimpleTensor tensor, IndexType type, int... permutation) {
        addSymmetry(tensor, type, true, permutation);
    }

    /**
     * Attaches symmetry to simple tensor.
     *
     * @param tensor      string representation of simple tensor
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addSymmetry(String tensor, int... permutation) {
        parseSimple(tensor).getIndices().getSymmetries().addSymmetry(permutation);
    }

    /**
     * Attaches symmetry to simple tensor.
     *
     * @param tensor      simple tensor
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addSymmetry(SimpleTensor tensor, int... permutation) {
        tensor.getIndices().getSymmetries().addSymmetry(permutation);
    }

    /**
     * Attaches antisymmetry to simple tensor.
     *
     * @param tensor      string representation of simple tensor
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addAntiSymmetry(String tensor, int... permutation) {
        parseSimple(tensor).getIndices().getSymmetries().addAntiSymmetry(permutation);
    }

    /**
     * Attaches antisymmetry to simple tensor.
     *
     * @param tensor      simple tensor
     * @param permutation permutation
     * @throws java.lang.IllegalStateException    if this tensor is already in use (it's permutation group calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd
     * @throws java.lang.IllegalArgumentException if degree of specified permutation differs from the size of indices
     *                                            of specified tensor
     */
    public static void addAntiSymmetry(SimpleTensor tensor, int... permutation) {
        tensor.getIndices().getSymmetries().addAntiSymmetry(permutation);
    }

    /**
     * Makes simple tensor antisymmetric with respect to indices of specified type.
     *
     * @param tensor simple tensor
     * @param type   type of indices
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setAntiSymmetric(SimpleTensor tensor, IndexType type) {
        int dimension = tensor.getIndices().size(type);
        addSymmetry(tensor, type, true, Permutations.createTransposition(dimension));
        if (dimension > 2)
            tensor.getIndices().getSymmetries().addSymmetry(type.getType(), Permutations.createPermutation(dimension % 2 == 0 ? true : false, Permutations.createCycle(dimension)));
    }

    /**
     * Makes simple tensor antisymmetric.
     *
     * @param tensor simple tensor
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setAntiSymmetric(SimpleTensor tensor) {
        tensor.getIndices().getSymmetries().setAntiSymmetric();
    }

    /**
     * Makes simple tensor antisymmetric.
     *
     * @param tensor string representation of simple tensor
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setAntiSymmetric(String tensor) {
        setAntiSymmetric(parseSimple(tensor));
    }

    /**
     * Makes simple tensors antisymmetric.
     *
     * @param tensors string representation of simple tensor
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setAntiSymmetric(Object... tensors) {
        for (Object tensor : tensors) {
            if (tensor instanceof SimpleTensor)
                setAntiSymmetric((SimpleTensor) tensor);
            else if (tensor instanceof String)
                setAntiSymmetric((String) tensor);
            else
                throw new IllegalArgumentException("Not a tensor " + tensor);
        }
    }

    /**
     * Makes simple tensor symmetric with respect to indices of specified type.
     *
     * @param tensor simple tensor
     * @param type   type of indices
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setSymmetric(SimpleTensor tensor, IndexType type) {
        int dimension = tensor.getIndices().size(type);
        tensor.getIndices().getSymmetries().addSymmetry(type, Permutations.createCycle(dimension));
        tensor.getIndices().getSymmetries().addSymmetry(type, Permutations.createTransposition(dimension));
    }

    /**
     * Makes simple tensor symmetric.
     *
     * @param tensor simple tensor
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setSymmetric(SimpleTensor tensor) {
        tensor.getIndices().getSymmetries().setSymmetric();
    }

    /**
     * Makes simple tensor symmetric.
     *
     * @param tensor string representation of simple tensor
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setSymmetric(String tensor) {
        setSymmetric(parseSimple(tensor));
    }

    /**
     * Makes simple tensors symmetric.
     *
     * @param tensors string representation of simple tensors
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setSymmetric(Object... tensors) {
        for (Object tensor : tensors) {
            if (tensor instanceof SimpleTensor)
                setSymmetric((SimpleTensor) tensor);
            else if (tensor instanceof String)
                setSymmetric((String) tensor);
            else
                throw new IllegalArgumentException("Not a tensor " + tensor);
        }
    }


    /**
     * Makes simple tensors symmetric.
     *
     * @param tensors string representation of simple tensors
     * @throws java.lang.IllegalStateException if this tensor is already in use (it's permutation group calculated)
     */
    public static void setAntiSymmetric(String... tensors) {
        for (String tensor : tensors)
            setAntiSymmetric(tensor);
    }

}
