/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.indexmapping;

import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.tensor.Tensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
final class PermutatorProvider extends IndexMappingProviderAbstract {
    private final IntPermutationsGenerator generator;
    private final Tensor[] from, to;
    private SimpleProductMappingsPort currentProvider = null;

    PermutatorProvider(final MappingsPort opu,
                       final Tensor[] from, final Tensor[] to) {
        super(opu);
        this.from = from;
        this.to = to;
        generator = new IntPermutationsGenerator(from.length);
    }

    @Override
    public void _tick() {
        generator.reset();
    }

    @Override
    public IndexMappingBuffer take() {
        if (currentBuffer == null)
            return null;
        while (currentProvider == null) {
            if (!generator.hasNext())
                return null;
            final int[] permutation = generator.next();
            final Tensor[] newTo = new Tensor[to.length];
            for (int i = 0; i < to.length; ++i)
                newTo[i] = to[permutation[i]];
            currentProvider = new SimpleProductMappingsPort(IndexMappingProvider.Util.singleton(currentBuffer.clone()), from, newTo);
            final IndexMappingBuffer buffer = currentProvider.take();
            if (buffer != null)
                return buffer;
            currentProvider = null;
        }
        final IndexMappingBuffer buffer = currentProvider.take();
        if (buffer == null) {
            currentProvider = null;
            return take();
        }
        return buffer;
    }
}
