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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class PrimitiveTensorFieldSubstitution extends PrimitiveSubstitution {
    public PrimitiveTensorFieldSubstitution(Tensor from, Tensor to) {
        super(from, to);
    }

    @Override
    Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator) {
        TensorField currentField = (TensorField) currentNode;
        TensorField from = (TensorField) this.from;
        IndexMappingBuffer buffer = IndexMappings.simpleTensorsPort(from, currentField).take();
        if (buffer == null)
            return currentNode;

        Indices[] fromIndices = from.getArgIndices(), currentIndices = currentField.getArgIndices();

        List<Tensor> argFrom = new ArrayList<>(), argTo = new ArrayList<>();
        Tensor fArg;
        int[] cIndices, fIndices;
        int i;
        for (i = from.size() - 1; i >= 0; --i) {
            if (IndexMappings.mappingExists(currentNode.get(i), from.get(i)))
                continue;
            fIndices = fromIndices[i].getAllIndices().copy();
            cIndices = currentIndices[i].getAllIndices().copy();

            assert cIndices.length == fIndices.length;

            fArg = ApplyIndexMapping.applyIndexMapping(from.get(i), fIndices, cIndices, new int[0]);

            argFrom.add(fArg);
            argTo.add(currentNode.get(i));
        }

        Tensor newTo = to;
        newTo = new Substitution(
                argFrom.toArray(new Tensor[argFrom.size()]),
                argTo.toArray(new Tensor[argTo.size()]),
                false).transform(newTo);
        if (!TensorUtils.isSymbolic(newTo))
            newTo = ApplyIndexMapping.applyIndexMapping(newTo, buffer, iterator.getForbidden());
        else if (buffer.getSignum())
            newTo = Tensors.negate(newTo);
        return newTo;
    }
}
