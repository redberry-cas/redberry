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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndicesTypeStructure;

import java.util.Arrays;
import java.util.Objects;

/**
 * Container for structure of indices (see {@link IndicesTypeStructure}) of tensor and its string name.
 * Two simple tensors are considered to have different mathematical nature if and only if their
 * {@code IndicesTypeStructureAndName} are not equal.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public class IndicesTypeStructureAndName {

    private String name;
    private IndicesTypeStructure[] structure;

    /**
     * @param name      name of tensor
     * @param structure structure of tensor indices
     */
    public IndicesTypeStructureAndName(String name, IndicesTypeStructure[] structure) {
        this.name = name;
        this.structure = structure;
    }

    /**
     * Returns name of tensor.
     *
     * @return name of tensor
     */
    public String getName() {
        return name;
    }

    /**
     * Returns structure of tensor indices.
     *
     * @return structure of tensor indices
     */
    public IndicesTypeStructure[] getStructure() {
        return structure;
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
