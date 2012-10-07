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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class TensorFieldSubstitution implements Transformation {

    final static SubstitutionProvider TENSOR_FIELD_PROVIDER = new SubstitutionProvider() {

        @Override
        public Transformation createSubstitution(Tensor from, Tensor to) {
            return new TensorFieldSubstitution((TensorField) from, to);
        }
    };
    private final TensorField from;
    private final Tensor to;
    private final boolean symbolic;

    private TensorFieldSubstitution(TensorField from, Tensor to) {
        this.from = from;
        this.to = to;
        this.symbolic = TensorUtils.isSymbolic(to);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor current;
        OUT:
        while ((current = iterator.next()) != null) {
            if (!(current instanceof TensorField))
                continue;
            TensorField currentField = (TensorField) current;
            IndexMappingBuffer buffer = IndexMappings.simpleTensorsPort(from, currentField).take();
            if (buffer == null)
                continue;

            Indices[] fromIndices = from.getArgIndices(), currentIndices = currentField.getArgIndices();

            List<Transformation> transformations = new ArrayList<>();
            Tensor fArg;
            int[] cIndices, fIndices;
            int i;
            for (i = from.size() - 1; i >= 0; --i) {
                if (IndexMappings.mappingExists(current.get(i), from.get(i)))
                    continue;
                fIndices = fromIndices[i].getAllIndices().copy();
                cIndices = currentIndices[i].getAllIndices().copy();

                assert cIndices.length == fIndices.length;

                fArg = ApplyIndexMapping.applyIndexMapping(from.get(i), fIndices, cIndices, new int[0]);

                transformations.add(Substitutions.getTransformation(fArg, current.get(i)));
            }

            Tensor newTo = to;

            for (Transformation transformation : transformations)
                newTo = transformation.transform(newTo);
            if (!TensorUtils.isSymbolic(newTo))
                newTo = ApplyIndexMapping.applyIndexMapping(newTo, buffer, iterator.getForbidden());

            iterator.set(newTo);
        }
        return iterator.result();
    }
}
