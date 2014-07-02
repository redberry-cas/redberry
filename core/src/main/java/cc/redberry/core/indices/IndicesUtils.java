/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
import cc.redberry.core.context.Context;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.MathUtils;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * This class provides static methods to work with individual index and indices
 * objects. <h5>Index representation</h5> All information about single index is
 * enclosed in 32-bit word (int). The following bit structure is used: <p
 * style='font-family: monospace;font-length:13px;'>
 * <pre> Index: stttttttXXXXXXXXcccccccccccccccc  -  per-bit representetion
 * |       |       |       |      |
 *        31      23      15      7      0  -  bit index<br>
 * s - one bit representing index state (0 - lower; 1 - upper)
 * t - 7-bits representing index type (lower latin, upper latin, etc...) [for concrete codes see below]
 * c - code of concrete index (a - 0, b - 1, c - 2, etc...) [index name]
 * X - reserved (always 0)</pre></p> <h5>Index types</h5> By default there are
 * four different index types:
 * <pre>
 * <TABLE CELLSPACING="0" CELLPADDING="5">
 * <CAPTION>  </CAPTION>
 * <TH> HexCode </TH>
 * <TH> BitCode </TH>
 * <TH> Description </TH>
 * <TR>
 *   <TD> 0x00 </TD>
 *   <TD> 00000000 </TD>
 *   <TD> Latin lower case symbols </TD>
 * </TR>
 * <TR>
 *   <TD> 0x01 </TD>
 *   <TD> 00000001 </TD>
 *   <TD> Latin upper case symbols </TD>
 * </TR>
 * <TR>
 *   <TD> 0x02 </TD>
 *   <TD> 00000010 </TD>
 *   <TD> Greek lower case symbols </TD>
 * </TR>
 * <TR>
 *   <TD> 0x03 </TD>
 *   <TD> 00000011 </TD>
 *   <TD> Greek upper case symbols </TD>
 * </TR>
 * </TABLE>
 * </pre> <h5>Examples</h5> <p>Here are some examples of how concrete indices
 * are presented in Redberry.
 * <pre>
 * <TABLE CELLSPACING="0" CELLPADDING="5">
 * <CAPTION>  </CAPTION>
 * <TH> Index </TH>
 * <TH> Hex </TH>
 * <TR>
 *   <TD> _a </TD>
 *   <TD> 0x00000000 </TD>
 * </TR>
 * <TR>
 *   <TD> _C </TD>
 *   <TD> 0x01000002 </TD>
 * </TR>
 * <TR>
 *   <TD> ^{\beta} </TD>
 *   <TD> 0x82000001 </TD>
 * </TR>
 * <TR>
 *   <TD> ^{\Chi} </TD>
 *   <TD> 0x83000015 </TD>
 *  </TR>
 * </TABLE>
 * </pre>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Indices
 * @since 1.0
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
     * @return index name (code) with type (first 30 bits), but without state
     * bit (in other words with state bit set to zero)
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
     * @return index with new type
     */
    public static int setType(byte type, int index) {
        return (0x80FFFFFF & index) | ((0x7F & type) << 24);
    }

    /**
     * Changes index type to specified, represented by byte.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(0x80FFFFFF & index) | ((0x7F & type) << 24)</code></b>
     *
     * @param type  IndexType
     * @param index index to change type in
     * @return index with new type
     */
    public static int setType(IndexType type, int index) {
        return setType(type.getType(), index);
    }

    /**
     * Changes index state to specified state of the form 0b1(0)000000.....
     * <p/>
     * <br/>Expression used by this method is: <b><code>rawState | index</code></b>
     *
     * @param rawState raw state
     * @param index    index
     * @return index with new state
     */
    public static int setRawState(int rawState, int index) {
        return rawState | (index & 0x7FFFFFFF);
    }

    /**
     * Changes index state to specified state (true - upper, false - lower).
     *
     * @param state index state: true - upper, false - lower)
     * @param index index to change type in
     * @return index with new state
     */
    public static int setState(boolean state, int index) {
        return setRawState(state ? 0x80000000 : 0, index);
    }

    /**
     * Returns index name (code) without type.
     * <p/>
     * <br/>Expression used by this method is: <b><code>index & 0xFFFF</code></b>
     *
     * @param index index
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
     * @return index type
     */
    public static byte getType(int index) {
        return ((byte) ((index & 0x7FFFFFFF) >>> 24));
    }

    /**
     * Returns index type enum value.
     *
     * @param index index
     * @return index type enum value
     */
    public static IndexType getTypeEnum(int index) {
        return IndexType.getType(getType(index));
    }

    /**
     * Returns index type in form 0000000000000000000000000xxxxxxx, where x -
     * type bits.
     * <p/>
     * <br/>Expression used by this method is: <b><code>(index & 0x7FFFFFFF) >>> 24</code></b>
     *
     * @param index index
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
     * @return see description
     */
    public static int[] getSortedDistinctIndicesNames(Indices indices) {
        int[] indsArray = indices.getAllIndices().copy();
        for (int i = 0; i < indsArray.length; ++i)
            indsArray[i] = IndicesUtils.getNameWithType(indsArray[i]);
        return MathUtils.getSortedDistinct(indsArray);
    }

    public static String toString(int index, OutputFormat mode) {
        return (getState(index) ? "^" : "_") + Context.get().getIndexConverterManager().getSymbol(index, mode) + "";
    }

    public static String toString(int index) {
        return toString(index, Context.get().getDefaultOutputFormat());
    }

    public static String toString(int[] indices, OutputFormat mode) {
        //todo refactor using StringBuilder since InconsistensIndicesException can be thrown
        return IndicesFactory.createSimple(null, indices).toString(mode);
    }

    public static String toString(int[] indices) {
        return toString(indices, CC.getDefaultOutputFormat());
    }

    /**
     * Parse single index.
     *
     * @param string string representation of index
     * @return integer representation of index
     */
    public static int parseIndex(String string) {
        string = string.trim();
        boolean state = string.charAt(0) == '^';
        int start = 0;
        if (string.charAt(0) == '^' || string.charAt(0) == '_')
            start = 1;
        int nameWithType;
        if (string.charAt(start) == '{')
            nameWithType = Context.get().getIndexConverterManager().getCode(string.substring(start + 1, string.length() - 1));
        else
            nameWithType = Context.get().getIndexConverterManager().getCode(string.substring(start));
        return state ? (0x80000000 ^ nameWithType) : nameWithType;
    }

    /**
     * Returns an array of indices names (with types), presented in specified {@code Indices}
     * object with the same ordering.
     *
     * @param indices
     * @return array of indices names (with types)
     */
    public static int[] getIndicesNames(Indices indices) {
        int a[] = new int[indices.size()];
        for (int i = indices.size() - 1; i >= 0; --i)
            a[i] = getNameWithType(indices.get(i));
        return a;
    }

    /**
     * Returns an array of indices names (with types)
     *
     * @param indices
     * @return array of indices names (with types)
     */
    public static int[] getIndicesNames(int[] indices) {
        int a[] = new int[indices.length];
        for (int i = a.length - 1; i >= 0; --i)
            a[i] = getNameWithType(indices[i]);
        return a;
    }

    /**
     * Returns an array of indices names (with types)
     *
     * @param indices
     * @return array of indices names (with types)
     */
    public static int[] getIndicesNames(IntArray indices) {
        int a[] = new int[indices.length()];
        for (int i = a.length - 1; i >= 0; --i)
            a[i] = getNameWithType(indices.get(i));
        return a;
    }

    /**
     * Returns an array of free indices only
     *
     * @param indices
     * @return array of free indices only
     */
    public static int[] getFree(int[] indices) {
        return IndicesFactory.createSimple(null, indices).getFree().getAllIndices().copy();
    }

    public static boolean haveEqualStates(int index1, int index2) {
        return getRawStateInt(index1) == getRawStateInt(index2);
    }

    /**
     * This method checks whether specified permutation is consistent with
     * specified indices. Permutation considered to be consistent if it has
     * similar length and does not permutes indices with different types.
     *
     * @param indices     indices array to be checked
     * @param permutation permutation in one-line notation
     * @return {@code false} if permutation permutes indices with different
     * types or have different length and true in other case
     */
    public static boolean isPermutationConsistentWithIndices(final int[] indices, final int[] permutation) {
        if (indices.length != permutation.length)
            return false;
        for (int i = 0; i < permutation.length; ++i)
            if (getRawTypeInt(indices[i]) != getRawTypeInt(indices[permutation[i]]))
                return false;
        return true;
    }

    /**
     * This method checks whether specified permutation is consistent with
     * specified indices. Permutation considered to be consistent if it has
     * similar length and does not permutes indices with different types.
     *
     * @param indices     indices array to be checked
     * @param permutation permutation in one-line notation
     * @return {@code false} if permutation permutes indices with different
     * types or have different length and true in other case
     */
    public static boolean isPermutationConsistentWithIndices(final int[] indices, Permutation permutation) {
        if (indices.length < permutation.internalDegree())
            return false;
        for (int i = 0, s = permutation.internalDegree(); i < s; ++i)
            if (getRawTypeInt(indices[i]) != getRawTypeInt(indices[permutation.newIndexOf(i)]))
                return false;
        return true;
    }

    public static boolean equalsRegardlessOrder(Indices indices1, int[] indices2) {
        if (indices1 instanceof EmptyIndices)
            return indices2.length == 0;
        if (indices1.size() != indices2.length)
            return false;
        int[] temp = indices2.clone();
        Arrays.sort(temp);
        return Arrays.equals(((AbstractIndices) indices1).getSortedData(), temp);
    }

    public static boolean equalsRegardlessOrder(int[] indices1, int[] indices2) {
        if (indices1.length != indices2.length)
            return false;
        int[] temp1 = indices1.clone(), temp2 = indices2.clone();
        Arrays.sort(temp1);
        Arrays.sort(temp2);
        return Arrays.equals(temp1, temp2);
    }

    /**
     * Returns true if at least one free index of {@code u} is contracted
     * with some free index of {@code v}.
     *
     * @param u indices
     * @param v indices
     * @return true if at least one free index of {@code u} is contracted
     * with some free index of {@code v}
     */
    public static boolean haveIntersections(Indices u, Indices v) {
        //todo can be improved
        Indices uFree = u.getFree(),
                vFree = v.getFree();
        //micro optimization
        if (uFree.size() > vFree.size()) {
            Indices temp = uFree;
            uFree = vFree;
            vFree = temp;
        }
        for (int i = 0; i < uFree.size(); ++i)
            for (int j = 0; j < vFree.size(); ++j)
                if (vFree.get(j) == inverseIndexState(uFree.get(i)))
                    return true;
        return false;
    }

    /**
     * Returns an array of contracted indices between specified free indices.
     *
     * @param freeIndices1 free indices
     * @param freeIndices2 free indices
     * @return an array of contracted indices
     */
    public static int[] getIntersections(int[] freeIndices1, int[] freeIndices2) {

        //micro optimization
        if (freeIndices1.length > freeIndices2.length) {
            int[] temp = freeIndices1;
            freeIndices1 = freeIndices2;
            freeIndices2 = temp;
        }
        IntArrayList contracted = new IntArrayList();
        for (int i = 0; i < freeIndices1.length; ++i)
            for (int j = 0; j < freeIndices2.length; ++j)
                if (freeIndices2[j] == inverseIndexState(freeIndices1[i]))
                    contracted.add(getNameWithType(freeIndices2[j]));
        return contracted.toArray();
    }

    /**
     * Returns an array of contracted indices between specified indices.
     *
     * @param u indices
     * @param v indices
     * @return an array of contracted indices
     */
    public static int[] getIntersections(Indices u, Indices v) {
        if (u.size() == 0 || v.size() == 0)
            return new int[0];
        Indices freeU = u.getFree(), freeV = v.getFree();
        if (freeU.size() == 0 || freeV.size() == 0)
            return new int[0];
        return getIntersections(((AbstractIndices) freeU).data, ((AbstractIndices) freeV).data);
    }

    /**
     * Returns {@code true} if specified indices contain any index with non metric type.
     *
     * @param indices indices
     * @return {@code true} if specified indices contain any index with non metric type
     */
    public static boolean containsNonMetric(final Indices indices) {
        for (int i = 0; i < indices.size(); ++i) {
            if (!CC.isMetric(getType(indices.get(i))))
                return true;
        }
        return false;
    }

    /**
     * Returns all non metric types that present in specified indices.
     *
     * @param indices indices
     * @return all non metric types that present in specified indices
     */
    public static EnumSet<IndexType> nonMetricTypes(final Indices indices) {
        EnumSet<IndexType> types = EnumSet.noneOf(IndexType.class);
        for (int i = 0; i < indices.size(); ++i) {
            int index = indices.get(i);
            if (!CC.isMetric(getType(index)))
                types.add(getTypeEnum(index));
        }
        return types;
    }
}
