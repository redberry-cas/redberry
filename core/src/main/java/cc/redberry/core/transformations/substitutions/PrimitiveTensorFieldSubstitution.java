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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class PrimitiveTensorFieldSubstitution extends PrimitiveSubstitution {
    private final SimpleTensor fromHead;

    public PrimitiveTensorFieldSubstitution(Tensor from, Tensor to) {
        super(from, to);
        fromHead = ((TensorField) from).getHead();
    }

    @Override
    Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator) {
        TensorField currentField = (TensorField) currentNode;
        if (fromHead.getName() != currentField.getHead().getName())
            return currentField;

        if (from.size() != currentField.size())
            return currentField;

//        //check whether derivative orders in from less or equal to orders of current node
//        for (int i = currentNode.size() - 1; i >= 0; --i)
//            if (currentDescriptor.getDerivativeOrder(i) < fromDescriptor.getDerivativeOrder(i))
//                return currentNode;
//
//        IntArray orders = new IntArray(currentDescriptor.getDerivativeOrders());
//        DFromTo derivative = derivatives.get(orders);
//        if (derivative == null) {
//            int order, j;
//            SimpleTensor var;
//            int[] indices;
//            SimpleIndices varIndices;
//            TensorField __from = (TensorField) this.from;
//            Tensor __to = this.to;
//            IndexGeneratorImpl ig = null;
//            for (int i = orders.length() - 1; i >= 0; --i) {
//                order = orders.get(i) - this.orders.get(i);
//                while (order > 0) {
//                    var = (SimpleTensor) from.get(i);
//                    indices = new int[var.getIndices().size()];
//
//                    //lazy initialization
//                    if (indices.length != 0 && ig == null) {
//                        TIntHashSet forbidden = new TIntHashSet(iterator.getForbidden());
//                        forbidden.addAll(TensorUtils.getAllIndicesNamesT(this.from));
//                        forbidden.addAll(TensorUtils.getAllIndicesNamesT(this.to));
//                        ig = new IndexGeneratorImpl(forbidden.toArray());
//
//                    }
//
//                    for (j = indices.length - 1; j >= 0; --j)
//                        indices[j] = setRawState(getRawStateInt(var.getIndices().get(j)),
//                                ig.generate(getType(var.getIndices().get(j))));
//                    varIndices = UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort(null, indices);
//                    var = Tensors.setIndices(var, varIndices);
//                    __from = Tensors.fieldDerivative(__from, varIndices.getInverted(), i);
//                    __to = new DifferentiateTransformation(var).transform(__to);
//                    --order;
//                }
//            }
//            derivative = new DFromTo(__from, __to);
//            derivatives.put(orders, derivative);
//        }

        return __newTo(new DFromTo((TensorField) this.from, this.to), currentField, currentNode, iterator);
    }

    private Tensor __newTo(DFromTo fromTo, TensorField currentField,
                           Tensor currentNode, SubstitutionIterator iterator) {

        TensorField from = fromTo.from;
        Mapping mapping = IndexMappings.simpleTensorsPort(from.getHead(), currentField.getHead()).take();
        if (mapping == null)
            return currentNode;

        Indices[] fromIndices = from.getArgIndices(),
                currentIndices = currentField.getArgIndices();

        List<Tensor> argFrom = new ArrayList<>(), argTo = new ArrayList<>();
        Tensor fArg;
        int[] cIndices, fIndices;
        int i;
        for (i = from.size() - 1; i >= 0; --i) {
            if (IndexMappings.positiveMappingExists(currentNode.get(i), from.get(i)))
                continue;

            fIndices = fromIndices[i].getAllIndices().copy();
            cIndices = currentIndices[i].getAllIndices().copy();

            assert cIndices.length == fIndices.length;

            fArg = ApplyIndexMapping.applyIndexMapping(from.get(i), new Mapping(fIndices, cIndices), new int[0]);

            argFrom.add(fArg);
            argTo.add(currentNode.get(i));
        }

        Tensor newTo = fromTo.to;
        newTo = new SubstitutionTransformation(
                argFrom.toArray(new Tensor[argFrom.size()]),
                argTo.toArray(new Tensor[argTo.size()]),
                false).transform(newTo);
        return applyIndexMappingToTo(currentNode, newTo, mapping, iterator);
    }

    private static class DFromTo {
        final TensorField from;
        final Tensor to;

        private DFromTo(TensorField from, Tensor to) {
            this.from = from;
            this.to = to;
        }
    }
}
