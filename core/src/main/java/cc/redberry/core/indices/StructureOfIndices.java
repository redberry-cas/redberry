/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.utils.BitArray;

import java.util.Arrays;

/**
 * The unique identification information about indices objects. This class contains information about types of indices
 * (number of indices of each type) and about states of non metric indices (if there are any).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.context.NameDescriptor
 * @since 1.0
 */
public final class StructureOfIndices {

    private final int[] typesCounts = new int[IndexType.TYPES_COUNT];
    private final BitArray[] states = new BitArray[IndexType.TYPES_COUNT];
    private final int size;

    //for empty instance
    private StructureOfIndices() {
        this.size = 0;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                this.states[i] = BitArray.EMPTY;
    }

    private StructureOfIndices(int size) {
        this.size = size;
    }

    /**
     * Creates structure of indices, which contains indices only of specified type.
     *
     * @param type   index type
     * @param count  number of indices
     * @param states indices states
     */
    private StructureOfIndices(byte type, int count, boolean... states) {
        typesCounts[type] = count;
        size = count;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                this.states[i] = i == type ? new BitArray(states) : BitArray.EMPTY;
    }

    /**
     * Creates structure of indices, which contains indices only of specified metric type.
     *
     * @param type  index type
     * @param count number of indices
     * @throws IllegalArgumentException if type is non metric
     */
    private StructureOfIndices(byte type, int count) {
        if (!CC.isMetric(type))
            throw new IllegalArgumentException("No states information provided for non metric type.");
        typesCounts[type] = count;
        size = count;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i)
            if (!CC.isMetric((byte) i))
                states[i] = BitArray.EMPTY;
    }


    /**
     * Creates structure of indices from specified data about metric indices.
     *
     * @param types array of types
     * @param count array of sizes of indices of specified types
     * @throws IllegalArgumentException if any type in type is non metric
     */
    private StructureOfIndices(final byte[] types, int[] count) {
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
                states[i] = BitArray.EMPTY;
    }

    /**
     * Creates structure of indices from specified data.
     *
     * @param allCount  array of sizes of indices of all types
     * @param allStates array of states of indices of all types
     * @throws IllegalArgumentException {@code allCount.length() !=  allStates.length()}
     * @throws IllegalArgumentException if length of {@code allCount} not equal the total number of available types of
     *                                  inddices.
     */
    private StructureOfIndices(int[] allCount, BitArray[] allStates) {
        if (allCount.length != IndexType.TYPES_COUNT || allStates.length != IndexType.TYPES_COUNT)
            throw new IllegalArgumentException();
        int i, size = 0;
        for (i = 0; i < IndexType.TYPES_COUNT; ++i) {
            if ((allStates[i] != null && CC.isMetric((byte) i)) ||
                    (allStates[i] == null && !CC.isMetric((byte) i)))
                throw new IllegalArgumentException();
            this.states[i] = allStates[i] == null ? null : allStates[i].clone();
            size += allCount[i];
        }
        System.arraycopy(allCount, 0, this.typesCounts, 0, allCount.length);
        this.size = size;
    }

    /**
     * Returns the structure of specified simple indices.
     *
     * @param indices simple indices
     */
    private StructureOfIndices(SimpleIndices indices) {
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
     * Creates structure of indices, which contains indices only of specified type.
     *
     * @param type   index type
     * @param count  number of indices
     * @param states indices states
     */
    public static StructureOfIndices create(byte type, int count, boolean... states) {
        if (count != states.length)
            throw new IllegalArgumentException();
        if (count == 0)
            return getEmpty();
        return new StructureOfIndices(type, count, states);
    }

    /**
     * Creates structure of indices, which contains indices only of specified metric type.
     *
     * @param type  index type
     * @param count number of indices
     * @throws IllegalArgumentException if type is non metric
     */
    public static StructureOfIndices create(byte type, int count) {
        if (count == 0)
            return getEmpty();
        return new StructureOfIndices(type, count);
    }

    /**
     * Creates structure of indices, which contains indices only of specified metric type.
     *
     * @param type  index type
     * @param count number of indices
     * @throws IllegalArgumentException if type is non metric
     */
    public static StructureOfIndices create(IndexType type, int count) {
        return create(type.getType(), count);
    }

    /**
     * Creates structure of indices from specified data about metric indices.
     *
     * @param types array of types
     * @param count array of sizes of indices of specified types
     * @throws IllegalArgumentException if any type in type is non metric
     */
    public static StructureOfIndices create(final byte[] types, int[] count) {
        int total = 0;
        for (int i = 0; i < count.length; ++i)
            total += count[i];
        if (total == 0)
            return getEmpty();
        return new StructureOfIndices(types, count);
    }

    /**
     * Creates structure of indices from specified data.
     *
     * @param allCount  array of sizes of indices of all types
     * @param allStates array of states of indices of all types
     * @throws IllegalArgumentException {@code allCount.length() !=  allStates.length()}
     * @throws IllegalArgumentException if length of {@code allCount} not equal the total number of available types of
     *                                  inddices.
     */
    public static StructureOfIndices create(int[] allCount, BitArray[] allStates) {
        int total = 0;
        for (int i = 0; i < allCount.length; ++i) {
            if (allStates[i] != null && allCount[i] != allStates[i].size())
                throw new IllegalArgumentException("Count differs from states size.");
            total += allCount[i];
        }
        if (total == 0)
            return getEmpty();
        return new StructureOfIndices(allCount, allStates);
    }

    /**
     * Returns the structure of specified simple indices.
     *
     * @param indices simple indices
     */
    public static StructureOfIndices create(SimpleIndices indices) {
        if (indices.size() == 0)
            return getEmpty();
        return new StructureOfIndices(indices);
    }

    /**
     * Returns states.
     *
     * @return states
     */
    public BitArray[] getStates() {
        BitArray[] statesCopy = new BitArray[states.length];
        for (int i = 0; i < states.length; ++i)
            statesCopy[i] = states[i] == null ? null : states[i].clone();
        return statesCopy;
    }

    /**
     * Returns states of specified type.
     *
     * @return states of specified type
     */
    public BitArray getStates(IndexType type) {
        return states[type.getType()].clone();
    }

    /**
     * Returns whether states of specified type are set.
     *
     * @return whether states of specified type are set
     */
    public boolean fixedStates(IndexType type) {
        return states[type.getType()] != null;
    }

    /**
     * Returns sizes of indices of all types.
     *
     * @return sizes of indices of all types
     */
    public int[] getTypesCounts() {
        return typesCounts.clone();
    }

    /**
     * @param indices sorted by type array of indices
     */
    StructureOfIndices(int[] indices) {
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

    private static BitArray createBBBA(int size) {
        if (size == 0)
            return BitArray.EMPTY;
        return new BitArray(size);
    }

    /**
     * Returns the total number of indices.
     *
     * @return total number of indices
     */
    public int size() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StructureOfIndices other = (StructureOfIndices) obj;
        if (size != other.size())
            return false;
        if (size == 0)
            return true;
        return Arrays.equals(this.typesCounts, other.typesCounts)
                && Arrays.deepEquals(this.states, other.states);
    }

    @Override
    public int hashCode() {
        return 469 + Arrays.hashCode(this.typesCounts) + Arrays.hashCode(states);
    }

    /**
     * Returns the information about specified type.
     *
     * @param type index type
     * @return the information about specified type
     */
    public TypeData getTypeData(byte type) {
        int from = 0;
        for (int i = 0; i < type; ++i)
            from += typesCounts[i];
        return new TypeData(from, typesCounts[type], states[type]);
    }

    /**
     * Returns the number of indices of specified type.
     *
     * @param type index type
     * @return the number of indices of specified type
     */
    public int typeCount(byte type) {
        return typesCounts[type];
    }

    /**
     * Returns {@code true} if this is structure of specified indices.
     *
     * @param indices indices
     * @return {@code true} if this is structure of specified indices
     */
    public boolean isStructureOf(SimpleIndices indices) {
        if (size != indices.size())
            return false;
        return equals(indices.getStructureOfIndices());
    }

    /**
     * Return the new {@code Structure of Indices } with inverted states of indices
     *
     * @return new {@code Structure of Indices } with inverted states of indices
     */
    public StructureOfIndices getInverted() {
        if (size == 0)
            return this;
        StructureOfIndices r = new StructureOfIndices(size);
        System.arraycopy(typesCounts, 0, r.typesCounts, 0, typesCounts.length);
        for (int i = r.states.length - 1; i >= 0; --i) {
            if (states[i] == null) continue;
            if (states[i] == BitArray.EMPTY)
                r.states[i] = BitArray.EMPTY;
            r.states[i] = states[i].clone();
            r.states[i].not();
        }
        return r;
    }

    /**
     * Returns the 'sum' of this and other structures
     *
     * @param oth other structure
     * @return 'sum' of this and other structures
     */
    public StructureOfIndices append(StructureOfIndices oth) {
        if (size == 0)
            return oth;
        if (oth.size == 0)
            return this;
        int size = this.size + oth.size;
        StructureOfIndices r = new StructureOfIndices(size);
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i) {
            r.typesCounts[i] = typesCounts[i] + oth.typesCounts[i];
            if (states[i] == null)
                continue;
            r.states[i] = states[i].append(oth.states[i]);
        }
        return r;
    }

    /**
     * Returns the 'sum' of N-copies of this
     *
     * @param N exponent
     * @return 'sum' of N-copies of this
     */
    public StructureOfIndices pow(int N) {
        if (size == 0 || N == 0) return getEmpty();
        if (N == 1) return this;

        int size = N * this.size;
        StructureOfIndices r = new StructureOfIndices(size);
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i) {
            r.typesCounts[i] = N * typesCounts[i];
            if (states[i] == null)
                continue;
            r.states[i] = states[i].times(N);
        }
        return r;
    }

    /**
     * Subtracts some structure of indices from the right side of current structure. <p/> <p>This operation may fail if
     * states of other structure differs from current.</p>
     *
     * @param other other structure
     * @return result of subtraction
     * @throws IllegalArgumentException nonmetric states are different
     */
    public StructureOfIndices subtract(StructureOfIndices other) {
        int size = this.size - other.size;
        if (size < 0)
            throw new IllegalArgumentException();
        if (other.size == 0)
            return this;

        StructureOfIndices r = new StructureOfIndices(size);
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i) {
            if ((r.typesCounts[i] = typesCounts[i] - other.typesCounts[i]) < 0)
                throw new IllegalArgumentException("Other is larger then this.");

            if (states[i] == null)
                continue;
            if (other.states[i] == null)
                throw new IllegalArgumentException("Inconsistent structures: " + this + " and " + other);

            if (!states[i].copyOfRange(states[i].size() - other.states[i].size()).equals(other.states[i]))
                throw new IllegalArgumentException("Nonmetric states are different");

            r.states[i] = states[i].copyOfRange(0, states[i].size() - other.states[i].size());
        }

        if (size == 0)
            return getEmpty();
        return r;
    }

    /**
     * Calculates partition of this structure by the specified structures and returns the resulting map. This map
     * organized as follows: map[i][j] represents the index of this, which matches j-th index of i-th structure in
     * specified partition
     *
     * @param partition
     * @return map which encodes partition of this structure by the specified structures
     * @throws IllegalArgumentException if specified array does not form a partition
     */
    public int[][] getPartitionMappings(final StructureOfIndices... partition) {
        int c;
        //todo check states
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i) {
            c = 0;
            for (StructureOfIndices str : partition)
                c += str.typesCounts[i];
            if (c != typesCounts[i])
                throw new IllegalArgumentException("Not a partition.");
        }

        int[][] mappings = new int[partition.length][];
        int i, j, k;

        int[] pointers = new int[IndexType.TYPES_COUNT];
        for (j = 0; j < IndexType.TYPES_COUNT; ++j)
            pointers[j] = getTypeData((byte) j).from;

        for (c = 0; c < partition.length; ++c) {
            mappings[c] = new int[partition[c].size];
            i = 0;
            for (j = 0; j < IndexType.TYPES_COUNT; ++j)
                for (k = partition[c].typesCounts[j] - 1; k >= 0; --k)
                    mappings[c][i++] = pointers[j]++;
            assert i == partition[c].size;
        }
        return mappings;
    }

    @Override
    public String toString() {
        if (size == 0)
            return "[]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i) {
            if (typesCounts[i] != 0) {
                sb.append('{');
                if (states[i] == null)
                    for (int t = 0; ; ++t) {
                        sb.append(IndexType.values()[i].getShortString());
                        if (t == typesCounts[i] - 1)
                            break;
                        sb.append(',');
                    }
                else {
                    for (int t = 0; t < typesCounts[i]; ++t) {
                        sb.append('(');
                        sb.append(IndexType.values()[i].getShortString());
                        sb.append(',');
                        sb.append(states[i].get(t) ? 1 : 0);
                        sb.append(')');
                        if (t == typesCounts[i] - 1)
                            break;
                        sb.append(',');
                    }

                }

                sb.append("},");
            }
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append(']');
        return sb.toString();
    }

    /**
     * Singleton for empty structure.
     */
    private static StructureOfIndices EMPTY;

    public static StructureOfIndices getEmpty() {
        if (EMPTY == null)
            EMPTY = new StructureOfIndices();
        return EMPTY;
    }

    /**
     * Container of information about structure of indices of a particular type.
     */
    public static class TypeData {

        /**
         * Position in indices, from which this type of indices begins
         */
        public final int from;
        /**
         * Number of indices of this type
         */
        public final int length;
        /**
         * Information about states of indices
         */
        public final BitArray states;

        TypeData(int from, int length, BitArray states) {
            this.from = from;
            this.length = length;
            if (states != null)
                this.states = states.clone();
            else
                this.states = null;
        }
    }
}
