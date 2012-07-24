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
package cc.redberry.core.performance.kv;

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Transformation;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class NaiveSubstitution implements Transformation {

    private final Tensor from, to;

    NaiveSubstitution(Tensor from, Tensor to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        TensorLastIterator iterator = new TensorLastIterator(tensor);
        Tensor current;
        while ((current = iterator.next()) != null) {
            IndexMappingBuffer buffer = IndexMappings.getFirst(from, current);
            if (buffer != null) {
                Tensor newFrom = ApplyIndexMapping.applyIndexMapping(from, buffer);
                iterator.set(newFrom);
            }
        }
        return iterator.result();
    }
}
