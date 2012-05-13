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

import cc.redberry.core.combinatorics.Symmetries;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;

/**
 * This class provides functionality to construct {@code Indices} object by
 * merging other {@code Indices} objects. For example, if we have a product
 * {@code X_mn*Y_ab} we can construct products indices by appending
 * consequentially indices of tensor {@code X} and {@code Y} to
 * {@code IndicesBuilder}. As the result this class returns {@code Indices}
 * instance.
 *
 * @see Indices
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @author Konstantin Kiselev
 */
public interface IndicesBuilder {

    /**
     * Appends specified {@code Indices} to {@code IndicesBuilder} and returns
     * this (similarly to {@link StringBuilder}).
     *
     * @param indices indices to be appended
     *
     * @return this
     */
    IndicesBuilder append(Indices indices);

    /**
     * Appends specified index to {@code IndicesBuilder} and returns this
     * (similarly to {@link StringBuilder}).
     *
     * @param index index to be appended
     *
     * @return this
     */
    IndicesBuilder append(int index);

    /**
     * Appends specified indices, represented by {@code indices} integer array
     * and returns this (similarly to {@link StringBuilder}).
     *
     * @param indices specified indices array to be appended
     *
     * @return this
     */
    IndicesBuilder append(int[] indices);

    /**
     * Appends specified indices, represented by {@code indices} integer array
     * and returns this (similarly to {@link StringBuilder}).
     *
     * @param indices specified indices array to be appended
     *
     * @return this
     */
    IndicesBuilder append(IntArray indices);

    /**
     * Appends specified indices, represented by {@code indices} integer array
     * and returns this (similarly to {@link StringBuilder}).
     *
     * @param indices specified indices array list to be appended
     *
     * @return this
     */
    IndicesBuilder append(IntArrayList indices);

    /**
     * Appends specified IndicesBuilder and returns this (similarly to {@link StringBuilder}).
     *
     * @param ib IndicesBuilder to be appended
     *
     * @return this
     */
    IndicesBuilder append(IndicesBuilder ib);

    /**
     * Appends indices of specified tensor and returns this (similarly to {@link StringBuilder}).
     *
     * @param ib IndicesBuilder to be appended
     *
     * @return this
     */
    IndicesBuilder append(Tensor tensor);

    /**
     * Appends indices of specified tensors and returns this (similarly to {@link StringBuilder}).
     *
     * @param ib IndicesBuilder to be appended
     *
     * @return this
     */
    IndicesBuilder append(Tensor... tensor);

    /**
     * Returns result {@code Indices}. It returns {@code Indices }
     * instance with {@link Symmetries#EMPTY_SYMMETRIES}
     *
     * @return result {@code Indices} with empty symmetries
     */
    Indices getIndices();

    int[] toArray();

    @Override
    String toString();

    IndicesBuilder clone();
}
