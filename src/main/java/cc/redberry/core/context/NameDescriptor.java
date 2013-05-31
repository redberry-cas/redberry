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

import cc.redberry.core.indices.IndicesSymmetries;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;

import java.util.Arrays;

/**
 * Object of this class represents unique type of simple tensor or tensor fields (unique name).
 * <p/>
 * <p>It holds the information about string name of simple tensor, structure of its indices and arguments
 * (in case of tensor field). Two simple tensors are considered to have different mathematical nature if and only if
 * their name descriptors are not equal. Each simple tensor with unique mathematical nature have its own unique integer
 * identifier, which is hold in the name descriptor. For example, tensors A_mn and A_ij have the same mathematical
 * origin and thus have the same integer identifier and both have the same name descriptor (the same reference). In
 * contrast, for example, tensors A_mn and A_i have different mathematical origin and different integer identifiers.</p>
 * <p/>
 * <p>This class have no public constructors, since Redberry takes care about its creation (see {@link NameManager}).
 * The only way to receive name descriptor from raw information about tensor is through
 * {@link NameManager#mapNameDescriptor(String, cc.redberry.core.indices.StructureOfIndices...)}.
 * In order to receive the descriptor from a simple tensor object, one should use
 * {@link cc.redberry.core.tensor.SimpleTensor#getNameDescriptor()} method.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public abstract class NameDescriptor {
    //first element is simple tensor indexTypeStructure, other appears for tensor fields
    final StructureOfIndices[] structuresOfIndices;
    private final int id;
    final IndicesSymmetries symmetries;
    NameManager nameManager = null;

    NameDescriptor(StructureOfIndices[] structuresOfIndices, int id) {
        if (structuresOfIndices.length == 0)
            throw new IllegalArgumentException();
        this.id = id;
        this.structuresOfIndices = structuresOfIndices;
        this.symmetries = IndicesSymmetries.create(structuresOfIndices[0]);
    }

    void registerInNameManager(NameManager manager) {
        if (nameManager != null && manager != nameManager)
            throw new IllegalStateException("Already registered in another name manager.");

        this.nameManager = manager;
    }

    /**
     * Returns unique simple tensor identifier
     *
     * @return unique simple tensor identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Returns symmetries of indices of tensors with this name descriptor
     *
     * @return symmetries of indices of tensors with this name descriptor
     */
    public IndicesSymmetries getSymmetries() {
        return symmetries;
    }

    /**
     * Returns {@code true} if this is a descriptor of tensor field
     *
     * @return {@code true} if this is a descriptor of tensor field
     */
    public boolean isField() {
        return structuresOfIndices.length != 1;
    }

    /**
     * Returns structure of indices of tensors with this name descriptor
     *
     * @return structure of indices of tensors with this name descriptor
     */
    public StructureOfIndices getStructureOfIndices() {
        return structuresOfIndices[0];
    }

    /**
     * Returns structure of i-th arg indices of tensors with this name descriptor
     *
     * @return structure of i-th arg indices indices of tensors with this name descriptor
     */
    public StructureOfIndices getStructureOfIndices(int arg) {
        return structuresOfIndices[arg + 1];
    }


    /**
     * Returns structure of indices of tensors with this name descriptor (first element in array) and
     * structures of indices of their arguments (in case of tensor field)
     *
     * @return structure of indices of tensors and their arguments
     */
    public StructureOfIndices[] getStructuresOfIndices() {
        //todo clone() ?
        return structuresOfIndices;
    }

    abstract NameAndStructureOfIndices[] getKeys();

    /**
     * Returns string name of tensor. The argument can be {@code null}.
     *
     * @param indices indices (in case of metric or Kronecker) and null in other cases
     * @return string name of tensor
     */
    public abstract String getName(SimpleIndices indices);

    @Override
    public String toString() {
        return getName(null) + ":" + Arrays.toString(structuresOfIndices);
    }

    /**
     * Returns structure of indices of tensors with specified name descriptor
     *
     * @param nd name descriptor
     * @return structure of indices of tensors with specified name descriptor
     */
    public static NameAndStructureOfIndices extractKey(NameDescriptor nd) {
        return nd.getKeys()[0];
    }
}
