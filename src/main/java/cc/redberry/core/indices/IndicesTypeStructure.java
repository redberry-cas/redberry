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

import cc.redberry.core.context.CC;
import cc.redberry.core.utils.ByteBackedBitArray;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndicesTypeStructure {

    public static final IndicesTypeStructure EMPTY = new IndicesTypeStructure((byte) 0, 0);
    private final int[] typesCounts = new int[IndexType.TYPES_COUNT];
    private final ByteBackedBitArray[] states = new ByteBackedBitArray[IndexType.TYPES_COUNT];
    private final int size;

    public IndicesTypeStructure(byte type, int count, boolean... states) {
        typesCounts[type] = count;
        size = count;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                this.states[i] = i == type ? new ByteBackedBitArray(states) : ByteBackedBitArray.EMPTY;
    }

    public IndicesTypeStructure(byte type, int count) {
        if (!CC.isMetric(type))
            throw new IllegalArgumentException("No states information provided for non metric type.");
        typesCounts[type] = count;
        size = count;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                states[i] = ByteBackedBitArray.EMPTY;
    }


    public IndicesTypeStructure(IndexType type, int count) {
        this(type.getType(), count);
    }

    public IndicesTypeStructure(final byte[] types, int[] count) {
        for (int i = 0; i < types.length; ++i)
            if (count[i] != 0 && !CC.isMetric(types[i]))
                throw new IllegalArgumentException("No states information provided for non metric type.");

        int size = 0;
        for (int i = 0; i < types.length; ++i) {
            typesCounts[types[i]] = count[i];
            size += count[i];
        }
        this.size = size;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                states[i] = ByteBackedBitArray.EMPTY;
    }

    public IndicesTypeStructure(SimpleIndices indices) {
        size = indices.size();
        int i;
        for (i = 0; i < size; ++i)
            ++typesCounts[IndicesUtils.getType(indices.get(i))];
        int[] pointers = new int[IndexType.TYPES_COUNT];
        for (i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                states[i] = createBBBA(typesCounts[i]);
            else
                pointers[i] = -1;
        byte type;
        for (i = 0; i < size; ++i) {
            type = IndicesUtils.getType(indices.get(i));
            if (pointers[type] != -1) {
                if (IndicesUtils.getState(indices.get(i)))
                    states[type].set(pointers[type]);
                ++pointers[type];
            }
        }
    }

    /**
     * @param indices sorted by type array of indices
     */
    IndicesTypeStructure(int[] indices) {
        size = indices.length;
        int i;
        for (i = 0; i < size; ++i)
            ++typesCounts[IndicesUtils.getType(indices[i])];
        int[] pointers = new int[IndexType.TYPES_COUNT];
        for (i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                states[i] = createBBBA(typesCounts[i]);
            else
                pointers[i] = -1;
        byte type;
        for (i = 0; i < size; ++i) {
            type = IndicesUtils.getType(indices[i]);
            if (pointers[type] != -1) {
                if (IndicesUtils.getState(indices[i]))
                    states[type].set(pointers[type]);
                ++pointers[type];
            }
        }
    }

    private static ByteBackedBitArray createBBBA(int size) {
        if (size == 0)
            return ByteBackedBitArray.EMPTY;
        return new ByteBackedBitArray(size);
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
        return Arrays.equals(this.typesCounts, other.typesCounts)
                && Arrays.deepEquals(this.states, other.states);
    }

    @Override
    public int hashCode() {
        return 469 + Arrays.hashCode(this.typesCounts) + Arrays.hashCode(states);
    }

    public TypeData getTypeData(byte type) {
        int from = 0;
        for (int i = 0; i < type; ++i)
            from += typesCounts[i];
        return new TypeData(from, typesCounts[type], states[type]);
    }

    public int typeCount(byte type) {
        return typesCounts[type];
    }

    public boolean isStructureOf(SimpleIndices indices) {
        if (size != indices.size())
            return false;
        return equals(indices.getIndicesTypeStructure());
    }

    @Override
    public String toString() {
        return "IndicesTypeStructure{" +
                "typesCounts=" + typesCounts +
                ", states=" + (states == null ? null : Arrays.asList(states)) +
                ", size=" + size +
                '}';
    }

    public static class TypeData {

        public final int from;
        public final int length;
        public final ByteBackedBitArray states;

        public TypeData(int from, int length, ByteBackedBitArray states) {
            this.from = from;
            this.length = length;
            if (states != null)
                this.states = states.clone();
            else
                this.states = null;
        }
    }
}
