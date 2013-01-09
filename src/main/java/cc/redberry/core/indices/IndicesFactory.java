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
 * Factory methods for indices creation.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
//TODO discuss with Stas. Final revision of indices types....
public class IndicesFactory {

    /**
     * Singleton object for empty indices.
     */
    public static final Indices EMPTY_INDICES = EmptyIndices.EMPTY_INDICES_INSTANCE;
    /**
     * Singleton object for empty simple indices.
     */
    public static final SimpleIndices EMPTY_SIMPLE_INDICES = EmptySimpleIndices.EMPTY_SIMPLE_INDICES_INSTANCE;

    /**
     * Creates simple indices from specified integer array and with specified symmetries.
     *
     * @param symmetries symmetries of indices
     * @param data       integer array of indices
     * @return simple indices
     * @throws InconsistentIndicesException if array contains more then one same integer
     */
    public static SimpleIndices createSimple(IndicesSymmetries symmetries, int... data) {
        if (data.length == 0)
            return EmptySimpleIndices.EMPTY_SIMPLE_INDICES_INSTANCE;
        return new SimpleIndicesIsolated(data.clone(), symmetries);
    }

    /**
     * Creates simple indices from specified {@link Indices} object and with specified symmetries.
     *
     * @param symmetries symmetries of indices
     * @param indices    {@link Indices} object
     * @return simple indices
     */
    public static SimpleIndices createSimple(IndicesSymmetries symmetries, Indices indices) {
        if (indices.size() == 0)
            return EmptySimpleIndices.EMPTY_SIMPLE_INDICES_INSTANCE;
        if (indices instanceof SimpleIndicesAbstract)
            return new SimpleIndicesIsolated(((SimpleIndicesAbstract) indices).data, symmetries);
        return new SimpleIndicesIsolated(indices.getAllIndices().copy(), symmetries);
    }

    /**
     * Creates unordered indices from specified {@code Indices} object. The resulting indices
     * will contain exactly the same indices as specified {@code Indices} object, by may have different
     * terms of ordering, in case when the specified indices are {@link SimpleIndices}.
     *
     * @param indices {@code Indices} object
     * @return unordered indices created from specified {@code Indices} object
     */
    public static Indices create(Indices indices) {
        if (indices.size() == 0)
            return EMPTY_INDICES;
        if (indices instanceof SortedIndices)
            return indices;
        return new SortedIndices(indices.getAllIndices().copy());
    }

    /**
     * Creates unordered indices from specified integer array of indices.
     *
     * @return unordered indices from specified integer array of indices
     * @throws InconsistentIndicesException if array contains more then one same integer
     */
    public static Indices create(int... data) {
        if (data.length == 0)
            return EMPTY_INDICES;
        return new SortedIndices(data.clone());
    }
}
