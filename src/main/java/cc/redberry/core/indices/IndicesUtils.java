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

import cc.redberry.core.context.Context;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.math.MathUtils;

/**
 * This class provides static methods to work with individual indices. <h5>Index
 * representation</h5> All information about single index is enclosed in 32-bit
 * word (int). The following bit structure is used: <p style='font-family: monospace;font-length:13px;'><pre> Index: stttttttXXXXXXXXcccccccccccccccc  -  per-bit representetion
 *      |       |       |       |      |
 *      31      23      15      7      0  -  bit index
 * <p/>
 * s - one bit representing index state (0 - lower; 1 - upper) t - 7-bits
 * representing index type (lower latin, upper latin, etc...) [for concrete
 * codes see below] c - code of concrete index (a - 0, b - 1, c - 2, etc...)
 * [index name] X - reserved (always 0)</pre></p> <h5>Index types</h5> By
 * default there are four different index types: <p style='font-family:
 * monospace;font-length:13px;'> <b><pre> HexCode   BitCode    Description</pre></b><pre>
 * 0x00      00000000   Latin lower case symbols
 * 0x01      00000001   Latin upper case symbols
 * 0x02      00000010   Greek lower case symbols
 * 0x03      00000011   Greek upper case symbols
 * </pre> </p> <h5>Examples</h5> Here are some examples of how concrete indices
 * are presented in Redberry. <p style='font-family:
 * monospace;font-length:13px;'> <b><pre> Index        Hex </pre></b><pre>
 * _a           0x00000000
 * _C           0x01000002
 * ^{\beta}     0x82000001
 * ^{\Chi}      0x83000015
 * <p/>
 * </pre> </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Indices
 */
public final class IndicesUtils {

    private IndicesUtils() {
    }

    /**
     * Creates single index with specified name, type and state.
     *
     * @param name  index name
     * @param type  index type
     * @param state index state
     *
     * @return index
     */
    public static int createIndex(int name, IndexType type, boolean state) {
        return createIndex(name, type.getType(), state);
    }

    /**
     * Creates single index with specified name, type and state.
     *
     * @param name  index name
     * @param type  index type
     * @param state index state
     *
     * @return index
     */
    public static int createIndex(int name, byte type, boolean state) {
        return (name & 0xFFFF) | ((0x7F & type) << 24) | (state ? 0x80000000 : 0);
    }

    /**
     * Returns only state (31-th) bit of index without shift.
     * <p/>
     * <br/>Expression used by this method is: <b><code>index & 0x80000000</code></b>
     *
     * @param index index
     *
     * @return (1 << 31) for upper index & 0 for lower index
     */
    public static int getRawStateInt(int index) {
        return index & 0x80000000;
    }

    /**
     * Returns state (31-th) bit of index shifted to 0-th position.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index & 0x80000000) >>> 31</code></b>
     *
     * @param index index
     *
     * @return 1 for upper index & 0 for lower index
     */
    public static int getStateInt(int index) {
        return (index & 0x80000000) >>> 31;
    }

    /**
     * Returns state (31-th) bit of index.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index & 0x80000000) == 0x80000000</code></b>
     *
     * @param index index
     *
     * @return 1 for upper index & 0 for lower index
     */
    public static boolean getState(int index) {
        return (index & 0x80000000) == 0x80000000;
    }

    /**
     * Returns index with inverse index state. So it raising lower indices and
     * lowering upper indices.
     * <p/>
     * <br/>Expression used by this method is: <b><code>0x80000000 ^ index</code></b>
     *
     * @param index index
     *
     * @return index with inverse state bit
     */
    public static int inverseIndexState(int index) {
        return 0x80000000 ^ index;
    }

    /**
     * Returns index name (code) with type (first 30 bits), but without state
     * bit (in other words with state bit set to zero).
     * <p/>
     * <br/>Expression used by this method is: <b><code>index & 0x7FFFFFFF</code></b>
     *
     * @param index specified index
     *
     * @return index name (code) with type (first 30 bits), but without state
     *         bit (in other words with state bit set to zero)
     */
    public static int getNameWithType(int index) {
        return index & 0x7FFFFFFF;
    }

    /**
     * Changes index type to specified, represented by byte.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(0x80FFFFFF & index) | ((0x7F & type) << 24)</code></b>
     *
     * @param type  type
     * @param index index to change type in
     *
     * @return index with new type
     */
    public static int setType(byte type, int index) {
        return (0x80FFFFFF & index) | ((0x7F & type) << 24);
    }

    /**
     * Changes index state to specified state of the form 0b1(0)000000.....
     * <p/>
     * <br/>Expression used by this method is: <b><code>rawState | index</code></b>
     *
     * @param rawState raw state
     * @param index    index to change type in
     *
     * @return index with new type
     */
    public static int setRawState(int rawState, int index) {
        return rawState | index;
    }

    /**
     * Returns index name (code) without type.
     * <p/>
     * <br/>Expression used by this method is: <b><code>index & 0xFFFF</code></b>
     *
     * @param index index
     *
     * @return index name
     */
    public static int getNameWithoutType(int index) {
        return index & 0xFFFF;
    }

    /**
     * Returns index type.
     * <p/>
     * <br/>Expression used by this method is: <b><code>((byte) ((index & 0x7FFFFFFF) >>> 24))</code></b>
     *
     * @param index index
     *
     * @return index type
     */
    public static byte getType(int index) {
        return ((byte) ((index & 0x7FFFFFFF) >>> 24));
    }

    /**
     * Returns index type enum value.
     * <p/>
     * <b>NOTE:</b> this method is low-performance, so use it only when you are
     * sure, that need exactly enum value, otherwise use method {@link #getType(int)
     * }.
     *
     * @param index index
     *
     * @return index type enum value
     */
    public static IndexType getTypeEnum(int index) {
        for (IndexType type : IndexType.values())
            if (type.getType() == getType(index))
                return type;
        throw new RuntimeException("Unknown type");
    }

    /**
     * Returns index type in form 0000000000000000000000000xxxxxxx, where x -
     * type bits.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index & 0x7FFFFFFF) >>> 24</code></b>
     *
     * @param index index
     *
     * @return index type
     */
    public static int getTypeInt(int index) {
        return (index & 0x7FFFFFFF) >>> 24;
    }

    /**
     * Returns index type in form 0xxxxxxx000000000000000000000000, where x -
     * type bits.
     * <p/>
     * <br/>Expression used by this method is: <b><code>index & 0x7F000000</code></b>
     *
     * @param index index
     *
     * @return index type
     */
    public static int getRawTypeInt(int index) {
        return index & 0x7F000000;
    }

    /**
     * Returns index type with state bit.
     * <p/>
     * <br/>Expression used by this method is: <b><code>((byte) (index >>> 24))</code></b>
     *
     * @param index index
     *
     * @return index type with state
     */
    public static byte getTypeWithState(int index) {
        return ((byte) (index >>> 24));
    }

    /*
     * Test methods
     */
    /**
     * Indicates whether two indices has the same type and name.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index0 & 0x7FFFFFFF) == (index1 & 0x7FFFFFFF)</code></b>
     *
     * @param index0 first index
     * @param index1 second index
     *
     * @return whether all bits except state bit are equals
     */
    public static boolean hasEqualTypeAndName(int index0, int index1) {
        return (index0 & 0x7FFFFFFF) == (index1 & 0x7FFFFFFF);
    }

    /**
     * Indicates whether two indices has the same type.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index0 & 0x7F000000) == (index1 & 0x7F000000)</code></b>
     *
     * @param index0 first index
     * @param index1 second index
     *
     * @return true if type bits in indices are the same
     */
    public static boolean hasEqualTypes(int index0, int index1) {
        return (index0 & 0x7F000000) == (index1 & 0x7F000000);
    }

    /**
     * Indicates whether two indices has the same type and state.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index0 & 0xFF000000) == (index1 & 0xFF000000)</code></b>
     *
     * @param index0 first index
     * @param index1 second index
     *
     * @return true if type and state bits in indices are the same
     */
    public static boolean hasEqualTypesAndStates(int index0, int index1) {
        return (index0 & 0xFF000000) == (index1 & 0xFF000000);
    }

    /**
     * Indicates whether two indices are contracted (has the same type & name
     * but different states).
     * <p/>
     * <br/>This method is very fast, because it performs only two operations:
     * bitwise xor + testing for equality with 32-bit constant. <br/>Expression
     * used by this method is: <b><code>(index0 ^ index1) == 0x80000000</code></b>
     *
     * @param index0 first index
     * @param index1 second index
     *
     * @return true if type and state bits in indices are the same
     */
    public static boolean areContracted(int index0, int index1) {
        return (index0 ^ index1) == 0x80000000;
    }

    /**
     * This method returns array of integers representing set of indices names
     * present in the {@link Indices} object.
     *
     * @param indices object to process
     *
     * @return see description
     */
    public static int[] getSortedDistinctIndicesNames(Indices indices) {
        int[] indsArray = indices.getAllIndices().copy();
        for (int i = 0; i < indsArray.length; ++i)
            indsArray[i] = IndicesUtils.getNameWithType(indsArray[i]);
        return MathUtils.getSortedDistinct(indsArray);
    }

    public static String toString(int index, ToStringMode mode) {
        return (getState(index) == true ? "^{" : "_{") + Context.get().getIndexConverterManager().getSymbol(index, mode) + "}";
    }

    public static String toString(int index) {
        return toString(index, Context.get().getDefaultPrintMode());
    }

    public static int parseIndex(String string) {
        boolean state = string.charAt(0) == '^';
        int nameWithType;
        if (string.charAt(1) == '{')
            nameWithType = Context.get().getIndexConverterManager().getCode(string.substring(2, string.length() - 1));
        else
            nameWithType = Context.get().getIndexConverterManager().getCode(string.substring(1));
        return state ? (0x80000000 ^ nameWithType) : nameWithType;
    }

    /**
     * Returns an array of indices names (with types), presented in specified {@code Indices}
     * object with the same ordering.
     *
     * @param indices
     *
     * @return array of indices names (with types)
     */
    public static int[] getIndicesNames(Indices indices) {
        int a[] = new int[indices.size()];
        for (int i = indices.size() - 1; i >= 0; --i)
            a[i] = getNameWithType(indices.get(i));
        return a;
    }

    public static boolean haveEqualStates(int index1, int index2) {
        return getRawStateInt(index1) == getRawStateInt(index2);
    }
}
