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

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndicesTypeStructure {

    public static final IndicesTypeStructure EMPTY = new IndicesTypeStructure((byte) 0, 0);
    private final int[] typesCounts = new int[IndexType.TYPES_COUNT];
    private final int size;

    public IndicesTypeStructure(byte type, int count) {
        typesCounts[type] = count;
        size = count;
    }

    public IndicesTypeStructure(IndexType type, int count) {
        this(type.getType(), count);
    }

    public IndicesTypeStructure(final byte[] types, int[] count) {
        int size = 0;
        for (int i = 0; i < types.length; ++i) {
            typesCounts[types[i]] = count[i];
            size += count[i];
        }
        this.size = size;
    }

    public IndicesTypeStructure(SimpleIndices indices) {
        size = indices.size();
        for (int i = 0; i < size; ++i)
            ++typesCounts[IndicesUtils.getType(indices.get(i))];
    }

    /**
     *
     * @param indices sorted by type array of indices
     */
    IndicesTypeStructure(int[] indices) {
        size = indices.length;
        for (int i = 0; i < size; ++i)
            ++typesCounts[IndicesUtils.getType(indices[i])];
    }

    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IndicesTypeStructure other = (IndicesTypeStructure) obj;
        if (!Arrays.equals(this.typesCounts, other.typesCounts))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 469 + Arrays.hashCode(this.typesCounts);
    }

    public TypeData getTypeData(byte type) {
        int from = 0;
        for (int i = 0; i < type; ++i)
            from += typesCounts[i];
        return new TypeData(from, typesCounts[type]);
    }

    public int typeCount(byte type) {
        return typesCounts[type];
    }

    public boolean isStructureOf(SimpleIndices indices) {
        if (size != indices.size())
            return false;
        return Arrays.equals(typesCounts, indices.getIndicesTypeStructure().typesCounts);
    }

    public static class TypeData {

        public final int from;
        public final int length;

        public TypeData(int from, int length) {
            this.from = from;
            this.length = length;
        }
    }
}
