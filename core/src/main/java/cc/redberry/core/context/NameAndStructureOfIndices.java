/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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

import cc.redberry.core.indices.StructureOfIndices;

/**
 * @author Stanislav Poslavsky
 */
public final class NameAndStructureOfIndices {
    final String name;
    final StructureOfIndices structureOfIndices;

    public NameAndStructureOfIndices(String name, StructureOfIndices structureOfIndices) {
        this.name = name;
        this.structureOfIndices = structureOfIndices;
    }

    public String getName() {
        return name;
    }

    public StructureOfIndices getStructureOfIndices() {
        return structureOfIndices;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NameAndStructureOfIndices that = (NameAndStructureOfIndices) o;

        if (!name.equals(that.name)) return false;
        if (!structureOfIndices.equals(that.structureOfIndices)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + structureOfIndices.hashCode();
        return result;
    }
}
