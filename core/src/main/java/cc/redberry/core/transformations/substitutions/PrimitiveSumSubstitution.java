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

import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class PrimitiveSumSubstitution extends PrimitiveSubstitution {
    public PrimitiveSumSubstitution(Tensor from, Tensor to) {
        super(from, to);
    }

    @Override
    Tensor newTo_(Tensor current, SubstitutionIterator iterator) {
        //early termination
        if (current.get(0).hashCode() > from.get(0).hashCode()
                || current.get(current.size() - 1).hashCode() < from.get(from.size() - 1).hashCode())
            return current;

        Tensor old = null;
        while (old != current) {
            old = current;

            SumBijectionPort.BijectionContainer bc = new SumBijectionPort(from, current).take();
            if (bc == null)
                return current;

            Mapping mapping = bc.mapping;
            Tensor newTo = applyIndexMappingToTo(current, to, mapping, iterator);

            SumBuilder builder = new SumBuilder();
            int[] bijection = bc.bijection;
            builder.put(newTo);

            Arrays.sort(bijection);
            int pivot = 0;
            for (int i = 0, size = current.size(); i < size; ++i) {
                if (pivot >= bijection.length || i != bijection[pivot])
                    builder.put(current.get(i));
                else
                    ++pivot;
            }
            current = builder.build();
        }
        return current;
    }
}
