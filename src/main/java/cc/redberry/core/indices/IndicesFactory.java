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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesFactory {

    public static SimpleIndices createSimple(IndicesSymmetries symmetries, int... data) {
        if (data.length == 0)
            return EmptyIndices.INSTANCE;
        return new SimpleIndicesIsolated(data.clone(), symmetries);
    }

    public static SimpleIndices createSimple(IndicesSymmetries symmetries, Indices indices) {
        if (indices.size() == 0)
            return EmptyIndices.INSTANCE;
        if (indices instanceof SimpleIndicesAbstract)
            return new SimpleIndicesIsolated(((SimpleIndicesAbstract) indices).data, symmetries);
        return new SimpleIndicesIsolated(indices.getAllIndices().copy(), symmetries);
    }

    public static SortedIndices createSorted(Indices indices) {
        if (indices.size() == 0)
            return LazyHolder.INSTANCE;
        if (indices instanceof SortedIndices)
            return (SortedIndices) indices;
        return new SortedIndices(indices.getAllIndices().copy());
    }

    public static Indices createSorted(int... data) {
        if (data.length == 0)
            return LazyHolder.INSTANCE;
        return new SortedIndices(data.clone());
    }

    public static SortedIndices getEmptySortedIndices() {
        return LazyHolder.INSTANCE;
    }

    /**
     * Holder for the instance. <p>We use here the Initialization On Demand
     * Holder Idiom!</p>
     */
    private static class LazyHolder {

        /**
         * Cached field instance.
         */
        private static final SortedIndices INSTANCE = new SortedIndices(new int[0]);
    }
}