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

import cc.redberry.core.math.MathUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;
import java.util.*;

/**
 * This class provides functionality to construct {@code Indices} object by
 * combination other {@code Indices} objects. For example, if we have a product
 * {@code X_mn*Y_ab} we can construct products indices by appending
 * consequentially indices of tensor {@code X} and {@code Y} to
 * {@code IndicesBuilderSorted}. As the result this class returns {@code SortedIndices}
 * instance.
 *
 * @see Indices
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndicesBuilderSorted
        implements IndicesBuilder {

    private final IntArrayList data;

    public IndicesBuilderSorted() {
        data = new IntArrayList();
    }

    private IndicesBuilderSorted(IntArrayList data) {
        this.data = data;
    }

    public IndicesBuilderSorted(int capacity) {
        data = new IntArrayList(capacity);
    }

    @Override
    public IndicesBuilderSorted append(int index) {
        data.add(index);
//        PriorityQueue
        return this;
    }

    @Override
    public IndicesBuilderSorted append(int[] indices) {
        data.addAll(indices);
        return this;
    }

    @Override
    public IndicesBuilderSorted append(IntArray indices) {
        data.addAll(indices);
        return this;
    }

    @Override
    public IndicesBuilderSorted append(IntArrayList indices) {
        data.addAll(indices);
        return this;
    }

    @Override
    public IndicesBuilderSorted append(Indices indices) {
        return append(indices.getAllIndices());
    }

    @Override
    public IndicesBuilderSorted append(IndicesBuilder ib) {
        return append(ib.toArray());
    }

    @Override
    public IndicesBuilderSorted append(Tensor tensor) {
        return append(tensor.getIndices());
    }

    @Override
    public IndicesBuilderSorted append(Tensor... tensor) {
        for (Tensor t : tensor)
            append(t);
        return this;
    }

    @Override
    public Indices getIndices() {
        return IndicesFactory.createSorted(data.toArray());
    }

    /**
     * Returns integer array, representing indices, constructed in this
     * {@code IndicesBuilderSorted}.
     *
     * @return integer array, representing indices, constructed in this
     * {@code IndicesBuilderSorted}
     */
    @Override
    public int[] toArray() {
        return data.toArray();
    }

    public SortedIndices getDistinct() {
        //TODO review performance
        return new SortedIndices(MathUtils.getSortedDistinct(data.toArray()));
    }

    @Override
    public String toString() {
        return getIndices().toString();
    }

    @Override
    public IndicesBuilderSorted clone() {
        return new IndicesBuilderSorted(data.clone());
    }
}
