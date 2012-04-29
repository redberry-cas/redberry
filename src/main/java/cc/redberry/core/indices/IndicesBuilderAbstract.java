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
package cc.redberry.core.indices;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.combinatorics.Symmetries;
import cc.redberry.core.utils.IntArrayList;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class IndicesBuilderAbstract implements IndicesBuilder {
    protected IntArrayList data = new IntArrayList();
    private Indices indices = null;
    private boolean changedFlag = false;

    /**
     * Appends specified {@code Indices} to {@code IndicesBuilderSorted} and returns 
     * this (similarly as {@link StringBuilder}).
     * 
     * @param indices indices to be appended
     * @return this
     */
    @Override
    public IndicesBuilderAbstract append(Indices indices) {
        if (indices instanceof EmptyIndices)
            return this;
        data.addAll(((AbstractIndices) indices).data);
        changedFlag = true;
        return this;
    }

    /**
     * Appends specified index to {@code IndicesBuilderSorted} and returns 
     * this (similarly as {@link StringBuilder}).
     * 
     * @param index index to be appended
     * @return this
     */
    @Override
    public IndicesBuilderAbstract append(int index) {
        data.add(index);
        changedFlag = true;
        return this;
    }

    /**
     * Appends specified indices, represented by {@code indices} integer array
     * and returns this (similarly as {@link StringBuilder}).
     * 
     * @param indices specified indices array to be appended
     * @return this
     */
    @Override
    public IndicesBuilderAbstract append(int[] indices) {
        if (indices.length == 0)
            return this;
        data.addAll(indices);
        changedFlag = true;
        return this;
    }

    /**
     * Appends specified IndicesBuilderSorted
     * and returns this (similarly as {@link StringBuilder}).
     * 
     * @param ib IndicesBuilderSorted to be appended
     * @return this
     */
    @Override
    public IndicesBuilderAbstract append(IndicesBuilder ib) {
        if (!(ib instanceof IndicesBuilderSorted))
            append(ib.getIndices());
        IntArrayList newData = ((IndicesBuilderAbstract) ib).data;
        if (newData.size() == 0)
            return this;
        data.addAll(newData);
        changedFlag = true;
        return this;
    }

    @Override
    public IndicesBuilder append(Tensor tensor) {
        return append(tensor.getIndices());
    }

    @Override
    public IndicesBuilder append(Tensor... tensor) {
        for (Tensor t : tensor)
            append(t);
        return this;
    }

    /**
     * Returns result {@code Indices}. It returns {@code SortedIndices } 
     * instance with {@link Symmetries#EMPTY_SYMMETRIES}
     * 
     * @return result {@code Indices} with empty symmetries
     */
    @Override
    public Indices getIndices() {
        changedFlag = false;
        if (indices == null || changedFlag)
            if (data.size() == 0)
                indices = EmptyIndices.INSTANCE;
            else
                indices = getIndices(data.toArray());
        return indices;
    }

    /**
     * Returns integer array, representing indices, constructed in this 
     * {@code IndicesBuilderSorted}.
     * 
     * @return integer array, representing indices, constructed in this 
     * {@code IndicesBuilderSorted}
     */
    public int[] asArray() {
        return data.toArray();
    }

    protected abstract Indices getIndices(int[] data);
}
