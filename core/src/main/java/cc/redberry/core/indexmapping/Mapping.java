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
package cc.redberry.core.indexmapping;

import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Arrays;

/**
 * This is the main class which represents the mapping from one tensor to another.
 * <p>
 * {@code Mapping} implements {@code Transformation} and perform transformation by simple invocation of
 * {@link ApplyIndexMapping#applyIndexMappingAutomatically(cc.redberry.core.tensor.Tensor, Mapping)} ))} with
 * {@code this} as a second argument.
 * </p>
 * <p>
 * The underlying data structure is a
 * simple pair of sorted arrays {@code fromNames} and {@code toData}, such that <i>i</i>-th mapping entry is
 * {@code fromNames[i] -> toData[i]}. The array {@code fromNames} contains from indices names. The array {@code toData}
 * contains names of to indices where the first bit indicates whether the state of from index should be inverted.
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.5
 */
public final class Mapping implements Transformation {
    final int[] fromNames, toData;
    final boolean sign;
    /**
     * Identity mapping
     */
    public static final Mapping IDENTITY = new Mapping(new int[0], new int[0], false, false);

    /**
     * Creates mapping from given {@code from} and {@code to} arrays of indices.
     *
     * @param from indices from
     * @param to   indices to
     */
    public Mapping(int[] from, int[] to) {
        this(from, to, false);
    }

    /**
     * Creates mapping from given {@code from} and {@code to} arrays of indices.
     *
     * @param from indices from
     * @param to   indices to
     * @param sign the sign of mapping
     */
    public Mapping(final int[] from, final int[] to, final boolean sign) {
        if (from.length != to.length)
            throw new IllegalArgumentException("From length != to length.");

        fromNames = new int[from.length];
        toData = new int[from.length];
        for (int i = 0; i < from.length; ++i) {
            fromNames[i] = IndicesUtils.getNameWithType(from[i]);
            toData[i] = IndicesUtils.getRawStateInt(from[i]) ^ to[i];
        }
        ArraysUtils.quickSort(fromNames, toData);
        this.sign = sign;
    }

    Mapping(IndexMappingBuffer buffer) {
        TIntObjectHashMap<IndexMappingBufferRecord> map = buffer.getMap();
        fromNames = new int[map.size()];
        toData = new int[map.size()];
        TIntObjectIterator<IndexMappingBufferRecord> iterator = map.iterator();
        int i = 0;
        IndexMappingBufferRecord record;
        while (iterator.hasNext()) {
            iterator.advance();
            record = iterator.value();
            fromNames[i] = iterator.key();
            toData[i] = record.getRawDiffStateBit() | record.getIndexName();
            ++i;
        }
        ArraysUtils.quickSort(fromNames, toData);
        sign = buffer.getSign();
    }

    private Mapping(int[] fromNames, int[] toData, boolean sign, boolean o) {
        this.fromNames = fromNames;
        this.toData = toData;
        this.sign = sign;
    }

    @Override
    public Tensor transform(Tensor t) {
        return ApplyIndexMapping.applyIndexMappingAutomatically(t, this);
    }

    /**
     * Returns {@code true} if this mapping does not contain any entries (however, it can have negative sign).
     *
     * @return {@code true} if this mapping does not contain any entries (however, it can have negative sign)
     * @see #isIdentity()
     */
    public boolean isEmpty() {return fromNames.length == 0;}

    /**
     * Returns {@code true} if this mapping does not contain any entries and have positive sign.
     *
     * @return {@code true} if this mapping does not contain any entries and have positive sign.
     */
    public boolean isIdentity() {return fromNames.length == 0 && !sign;}

    /**
     * Returns the sign of this mapping
     *
     * @return sign of this mapping
     */
    public boolean getSign() {return sign;}

    /**
     * Returns mapping, formed from this mapping by multiplying by specified sign.
     *
     * @param sign false - plus, true - minus one
     * @return a new mapping, formed from this mapping by multiplying by specified sign
     */
    public Mapping addSign(boolean sign) {
        return new Mapping(fromNames, toData, sign ^ this.sign, true);
    }

    /**
     * Returns the number of entries in this mapping.
     *
     * @return the number of entries in this mapping
     */
    public int size() {return fromNames.length;}

    /**
     * Returns the names of from indices.
     *
     * @return names of from indices
     */
    public IntArray getFromNames() {
        return new IntArray(fromNames);
    }

    /**
     * Returns the data array that represents to indices.
     *
     * @return data array that represents to indices.
     */
    public IntArray getToData() {
        return new IntArray(toData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mapping mapping = (Mapping) o;

        if (sign != mapping.sign) return false;
        if (!Arrays.equals(fromNames, mapping.fromNames)) return false;
        if (!Arrays.equals(toData, mapping.toData)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(fromNames);
        result = 31 * result + Arrays.hashCode(toData);
        result = 31 * result + (sign ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        int imax = fromNames.length - 1;
        if (imax == -1) {
            if (sign)
                return "-{}";
            else return "{}";
        }

        StringBuilder sb = new StringBuilder();
        if (sign)
            sb.append("-");
        sb.append("{");
        for (int i = 0; ; ++i) {
            sb.append(IndicesUtils.toString(fromNames[i]));
            sb.append("->");
            sb.append(IndicesUtils.toString(toData[i]));
            if (i == imax)
                break;
            sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Parses string representation of Mapping in the form -{a->b, ^c->_d} etc.
     *
     * @param string string representation of mapping in the form -{a->b, ^c->_d} etc.
     * @return mapping
     */
    public static Mapping valueOf(String string) {
        string = string.trim();
        int start = 0;
        boolean sign = false;
        if (string.charAt(0) == '-') {
            sign = true;
            start = 1;
        } else if (string.charAt(0) == '+')
            start = 1;

        if (string.charAt(start) != '{'
                || string.charAt(string.length() - 1) != '}')
            throw new IllegalArgumentException("Not valid syntax for mapping: " + string);

        string = string.substring(start + 1, string.length() - 1).trim();
        int fromIndex;
        final String[] split = string.split(",");
        int[] from = new int[split.length], to = new int[split.length];
        for (int i = 0; i < split.length; ++i) {
            String[] fromTo = split[i].split("->");
            if (fromTo.length != 2)
                throw new IllegalArgumentException("Not valid syntax for mapping: " + string);

            fromIndex = IndicesUtils.parseIndex(fromTo[0]);
            from[i] = IndicesUtils.getNameWithType(fromIndex);
            to[i] = IndicesUtils.getRawStateInt(fromIndex) ^ IndicesUtils.parseIndex(fromTo[1]);
        }
        return new Mapping(from, to, sign);
    }

}
