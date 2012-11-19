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
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class PrimitiveSumSubstitution extends PrimitiveSubstitution {
    public PrimitiveSumSubstitution(Tensor from, Tensor to) {
        super(from, to);
    }

    @Override
    Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator) {
        BijectionContainer bc = new SumBijectionPort(from, currentNode).take();
        if (bc == null)
            return currentNode;

        IndexMappingBuffer buffer = bc.buffer;
        Tensor newTo;
        if (toIsSymbolic)
            newTo = buffer.getSignum() ? Tensors.negate(to) : to;
        else
            newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, iterator.getForbidden());

        SumBuilder builder = new SumBuilder();
        int[] bijection = bc.bijection;
        Arrays.sort(bijection);
        builder.put(newTo);
        for (int i = currentNode.size() - 1; i >= 0; --i)
            if (Arrays.binarySearch(bijection, i) < 0) //todo may be improved
                builder.put(currentNode.get(i));
        return builder.build();
    }
}
