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

/**
 * Unsafe methods. Do not ever use this class.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class UnsafeIndicesFactory {

    /**
     * Creates simple indices of simple tensor.
     *
     * @param symmetries symmetries
     * @param indices    indices
     * @return simple indices of simple tensor
     */
    public static SimpleIndices createOfTensor(IndicesSymmetries symmetries, SimpleIndices indices) {
        if (indices.size() == 0)
            return IndicesFactory.EMPTY_SIMPLE_INDICES;
        return new SimpleIndicesOfTensor(true, ((AbstractIndices) indices).data, symmetries);
    }

    /**
     * Creates isolated simple indices.
     *
     * @param symmetries symmetries
     * @param data       integer data
     * @return isolated simple indices
     */
    public static SimpleIndices createIsolatedUnsafeWithoutSort(IndicesSymmetries symmetries, int... data) {
        if (data.length == 0)
            return IndicesFactory.EMPTY_SIMPLE_INDICES;
        return new SimpleIndicesIsolated(data, symmetries);
    }
}
