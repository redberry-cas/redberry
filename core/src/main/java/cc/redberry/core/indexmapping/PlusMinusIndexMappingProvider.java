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
package cc.redberry.core.indexmapping;

import cc.redberry.core.utils.OutputPort;

/**
 * Provides two index mappings for each input mapping: the same as input mapping and with opposite sign.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
class PlusMinusIndexMappingProvider extends IndexMappingProviderAbstract {
    private boolean state = false;

    public PlusMinusIndexMappingProvider(OutputPort<IndexMappingBuffer> opu) {
        super(opu);
    }

    @Override
    public IndexMappingBuffer take() {
        if (currentBuffer == null)
            return null;
        IndexMappingBuffer buf = currentBuffer;
        if (state) {
            currentBuffer = null;
            buf.addSign(true);
        } else {
            buf = buf.clone();
            state = true;
        }
        return buf;
    }
}
