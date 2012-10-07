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

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.utils.IntArray;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class EmptyIndices implements Indices {

    static final EmptyIndices EMPTY_INDICES_INSTANCE = new EmptyIndices();

    EmptyIndices() {
    }

    /**
     * Always throws {@code IndexOutOfBoundsException} because size is zero.
     *
     * @return throws IndexOutOfBoundsException
     *
     * @throws IndexOutOfBoundsException always
     */
    @Override
    public int get(int position) {
        throw new IndexOutOfBoundsException();
    }

    /**
     * Returns {@code EmptySimpleIndices} instance
     *
     * @return {@code EmptySimpleIndices} instance
     */
    @Override
    public Indices getInverse() {
        return this;
    }

    /**
     * Returns {@code EmptySimpleIndices} instance
     *
     * @return {@code EmptySimpleIndices} instance
     */
    @Override
    public Indices getFree() {
        return this;
    }

    /**
     * Returns {@code IntArray.EMPTY_ARRAY}
     *
     * @return {@code IntArray.EMPTY_ARRAY}
     */
    @Override
    public IntArray getUpper() {
        return IntArray.EMPTY_ARRAY;
    }

    /**
     * Returns {@code IntArray.EMPTY_ARRAY}
     *
     * @return {@code IntArray.EMPTY_ARRAY}
     */
    @Override
    public IntArray getLower() {
        return IntArray.EMPTY_ARRAY;
    }

    /**
     * Returns {@code IntArray.EMPTY_ARRAY}
     *
     * @return {@code IntArray.EMPTY_ARRAY}
     */
    @Override
    public IntArray getAllIndices() {
        return IntArray.EMPTY_ARRAY;
    }

    /**
     * Returns 0.
     *
     * @return 0
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Do nothing.
     *
     * @return false
     *
     * @param mapping
     */
    @Override
    public Indices applyIndexMapping(IndexMapping mapping) {
        return this;
    }

    /**
     * Do nothing
     */
    @Override
    public void testConsistentWithException() {
    }

    /**
     * Returns empty string.
     *
     * @param mode
     *
     * @return empty string
     */
    @Override
    public String toString(ToStringMode mode) {
        return "";
    }

    /**
     * Returns empty string.
     *
     * @return empty string
     */
    @Override
    public String toString() {
        return "";
    }

    /**
     * {@inheritDoc}
     *
     * @param obj {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    /**
     * Returns {@code indices.size() == 0}.
     *
     * @param indices indices to testing similarity
     *
     * @return {@code indices.size() == 0}
     */
    @Override
    public boolean equalsRegardlessOrder(Indices indices) {
        return indices.size() == 0;
    }

    /**
     * Returns 1
     *
     * @return 1
     */
    @Override
    public int hashCode() {
        return 8758765;
    }

    /**
     * Returns {@code short[0]}
     *
     * @return short[0]
     */
    @Override
    public short[] getDiffIds() {
        return new short[0];
    }
}
