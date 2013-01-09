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
package cc.redberry.core.indices;

import cc.redberry.core.math.MathUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;

import java.util.Collection;

/**
 * Builder of unordered indices. This class provides functionality to construct unordered {@code Indices}
 * object by combining other {@code Indices} objects. For example, if we have a
 * product {@code X_mn*Y_ab} we can construct products indices by appending
 * consequentially indices of tensor {@code X} and {@code Y} to
 * {@code IndicesBuilder}. As the result this class returns
 * {@code SortedIndices} instance.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Indices
 */
public final class IndicesBuilder {

    private final IntArrayList data;

    public IndicesBuilder() {
        data = new IntArrayList();
    }

    private IndicesBuilder(IntArrayList data) {
        this.data = data;
    }

    public IndicesBuilder(int capacity) {
        data = new IntArrayList(capacity);
    }

    /**
     * Appends index representation of specified {@code int}.
     *
     * @param index index to be appended
     * @return a reference to this object
     */
    public IndicesBuilder append(int index) {
        data.add(index);
        return this;
    }

    /**
     * Appends indices representation of specified {@code int[]}.
     *
     * @param indices indices to be appended
     * @return a reference to this object
     */
    public IndicesBuilder append(int[] indices) {
        data.addAll(indices);
        return this;
    }

    /**
     * Appends indices representation of specified {@code IntArray}.
     *
     * @param indices indices to be appended
     * @return a reference to this object
     */
    public IndicesBuilder append(IntArray indices) {
        data.addAll(indices);
        return this;
    }

    /**
     * Appends indices representation of specified {@code IntArrayList}.
     *
     * @param indices indices to be appended
     * @return a reference to this object
     */
    public IndicesBuilder append(IntArrayList indices) {
        data.addAll(indices);
        return this;
    }

    /**
     * Appends specified {@code Indices}.
     *
     * @param indices indices to be appended
     * @return a reference to this object
     */
    public IndicesBuilder append(Indices indices) {
        return append(indices.getAllIndices());
    }

    /**
     * Appends specified {@code IndicesBuilder}.
     *
     * @param ib IndicesBuilder
     * @return a reference to this object
     */
    public IndicesBuilder append(IndicesBuilder ib) {
        return append(ib.toArray());
    }

    /**
     * Appends indices of specified {@code Tensor}.
     *
     * @param tensor a tensor
     * @return a reference to this object
     */
    public IndicesBuilder append(Tensor tensor) {
        return append(tensor.getIndices());
    }

    /**
     * Appends consequentially indices of {@code tensors} in specified array.
     *
     * @param tensors a collection of tensors
     * @return a reference to this object
     */
    public IndicesBuilder append(Collection<? extends Tensor> tensors) {
        for (Tensor t : tensors)
            append(t);
        return this;
    }

    /**
     * Appends consequentially indices of {@code tensors} in specified array.
     *
     * @param tensors an array of tensors
     * @return a reference to this object
     */
    public IndicesBuilder append(Tensor... tensors) {
        for (Tensor t : tensors)
            append(t);
        return this;
    }

    /**
     * Returns resulting {@code Indices}.
     *
     * @return resulting {@code Indices}
     * @throws InconsistentIndicesException if there was more then one same index (with same names, types and states)
     */
    public Indices getIndices() {
        return IndicesFactory.create(data.toArray());
    }

    /**
     * Returns integer array, representing indices, constructed in this
     * {@code IndicesBuilder}.
     *
     * @return integer array, representing indices, constructed in this
     *         {@code IndicesBuilder}
     */
    public int[] toArray() {
        return data.toArray();
    }

   /* public Indices getDistinct() {
        //TODO review performance
        return IndicesFactory.create(MathUtils.getSortedDistinct(data.toArray()));
    }*/

    @Override
    public String toString() {
        return getIndices().toString();
    }

    @Override
    public IndicesBuilder clone() {
        return new IndicesBuilder(data.clone());
    }
}
