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
 * Implements "register" paradigm (see description of parent interface).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
abstract class IndexMappingProviderAbstract implements IndexMappingProvider {

    protected IndexMappingBuffer currentBuffer = null;
    private final OutputPort<IndexMappingBuffer> opu;

    public IndexMappingProviderAbstract(OutputPort<IndexMappingBuffer> opu) {
        this.opu = opu;
    }

    @Override
    public boolean tick() {
        _tick();
        currentBuffer = opu.take();
        return currentBuffer != null;
    }

    protected void _tick() {
    }
}
