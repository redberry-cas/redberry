/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.context;

import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.tensor.SimpleTensor;

/**
 * Implementation of {@link NameDescriptor} for any simple tensor, except Kronecker and metric tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public final class NameDescriptorForSimpleTensor extends NameDescriptor {

    final String name;
    private final NameAndStructureOfIndices[] key;
    /*
     * A cached instance for use in case of pure symbol.
     */
    private SimpleTensor cachedSymbol = null;

    NameDescriptorForSimpleTensor(String name, StructureOfIndices[] indexTypeStructures, int id) {
        super(indexTypeStructures, id);
        this.name = name;
        this.key = new NameAndStructureOfIndices[]{new NameAndStructureOfIndices(name, indexTypeStructures)};
    }

    @Override
    public String getName(SimpleIndices indices) {
        return name;
    }

    @Override
    NameAndStructureOfIndices[] getKeys() {
        return key;
    }

    public void setCachedInstance(SimpleTensor symbol) {
        if (cachedSymbol != null)
            throw new IllegalStateException("Symbol is already created.");
        cachedSymbol = symbol;
    }

    public SimpleTensor getCachedSymbol() {
        return cachedSymbol;
    }
}
