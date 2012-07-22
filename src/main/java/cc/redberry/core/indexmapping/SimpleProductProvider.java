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
package cc.redberry.core.indexmapping;

import cc.redberry.core.tensor.Tensor;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SimpleProductProvider implements MappingsPort {

    private final IndexMappingProvider[] providers;
    private boolean inited = false;

    SimpleProductProvider(final IndexMappingProvider[] providers) {
        this.providers = providers;
    }

    SimpleProductProvider(final IndexMappingProvider opu,
                          final Tensor[] from, final Tensor[] to) {
        providers = new IndexMappingProvider[from.length];
        providers[0] = IndexMappings.createPort(opu, from[0], to[0]);
        for (int i = 1; i < from.length; ++i)
            providers[i] = IndexMappings.createPort(providers[i - 1], from[i], to[i]);
    }

    @Override
    public IndexMappingBuffer take() {
        if (!inited) {
            for (int i = 0; i < providers.length; ++i)
                providers[i].tick();
            inited = true;
        }
        int i = providers.length - 1;
        IndexMappingBuffer buffer = providers[i].take();
        if (buffer != null)
            return buffer;
        OUTER:
        while (true) {
            boolean r;
            while ((r = !(providers[i--].tick())) && i >= 0);
            if (i == -1 && r)
                return null;
            i += 2;
            for (; i < providers.length; ++i)
                if (!providers[i].tick()) {
                    i--;
                    continue OUTER;
                }
            assert i == providers.length;
            i--;
            buffer = providers[i].take();
            if (buffer != null)
                return buffer;
        }
    }
}