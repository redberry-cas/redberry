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
import cc.redberry.core.tensor.SumBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SumSubstitution implements Transformation {

    final static SubstitutionProvider SUM_SUBSTITUTION_PROVIDER = new SubstitutionProvider() {

        @Override
        public Transformation createSubstitution(Tensor from, Tensor to) {
            return new SumSubstitution(from, to);
        }
    };
    private final Tensor from, to;
    private final boolean symbolic;

    public SumSubstitution(Tensor from, Tensor to) {
        this.from = from;
        this.to = to;
        this.symbolic = TensorUtils.isSymbolic(to);
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor);
        Tensor current;
        while ((current = iterator.next()) != null) {

            BijectionContainer bc = new SumBijectionPort(from, current).take();
            if (bc == null)
                continue;

            IndexMappingBuffer buffer = bc.buffer;
            Tensor newTo;
            if (symbolic)
                newTo = to;
            else {
                int[] forbidden = new int[iterator.forbiddenIndices().size()];
                int c = -1;
                for (Integer f : iterator.forbiddenIndices())
                    forbidden[++c] = f;
                newTo = ApplyIndexMapping.applyIndexMapping(to, buffer, forbidden);
//                if (newTo != to)
                iterator.forbiddenIndices().addAll(TensorUtils.getAllIndicesNames(newTo));
            }

            SumBuilder builder = new SumBuilder();
            int[] bijection = bc.bijection;
            Arrays.sort(bijection);
            builder.put(newTo);
            for (int i = current.size() - 1; i >= 0; --i)
                if (Arrays.binarySearch(bijection, i) >= 0)
                    continue;
                else
                    builder.put(current.get(i));
            iterator.set(builder.build());
        }
        return iterator.result();
    }
}
