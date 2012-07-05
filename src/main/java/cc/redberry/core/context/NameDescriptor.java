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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndicesSymmetries;
import cc.redberry.core.indices.IndicesTypeStructure;
import java.util.Arrays;
import java.util.Objects;

public final class NameDescriptor {

    //first element is simple tensor indexTypeStructure, other apperars for tensor fields
    private final IndicesTypeStructure[] indexTypeStructures;
    private final String name;
    private final IndicesTypeStructureAndName key;
    private int id = -1;
    private final IndicesSymmetries symmetries;

    NameDescriptor(String name, IndicesTypeStructure... indexTypeStructures) {
        if (indexTypeStructures.length == 0)
            throw new IllegalArgumentException();
        this.indexTypeStructures = indexTypeStructures;
        this.name = name;
        this.key = new IndicesTypeStructureAndName(name, indexTypeStructures);
        this.symmetries = IndicesSymmetries.create(indexTypeStructures[0]);
    }

    public boolean isField() {
        return indexTypeStructures.length != 1;
    }

    public String getName() {
        return name;
    }

    public IndicesSymmetries getSymmetries() {
        return symmetries;
    }

    public IndicesTypeStructure getIndicesTypeStructure() {
        return indexTypeStructures[0];
    }

    public IndicesTypeStructure[] getIndicesTypeStructures() {
        return indexTypeStructures;
    }

    IndicesTypeStructureAndName getKey() {
        return key;
    }

    void setId(int id) {
        assert this.id == -1;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name + ":" + Arrays.toString(indexTypeStructures) + ": is field " + isField();
    }

    static class IndicesTypeStructureAndName {

        private String name;
        private IndicesTypeStructure[] structure;

        public IndicesTypeStructureAndName(String name, IndicesTypeStructure[] structure) {
            this.name = name;
            this.structure = structure;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final IndicesTypeStructureAndName other = (IndicesTypeStructureAndName) obj;
            if (!Objects.equals(this.name, other.name))
                return false;
            return Arrays.equals(structure, structure);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + Objects.hashCode(this.name);
            hash = 37 * hash + Arrays.hashCode(this.structure);
            return hash;
        }
    }
}
