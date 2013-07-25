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
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Mapping implements Transformation {
    final int[] fromNames, toData;
    final boolean sign;

    public static final Mapping EMPTY = new Mapping(new int[0], new int[0], false, false);

    public Mapping(int[] from, int[] to) {
        this(from, to, false);
    }

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

    public boolean isEmpty() {return fromNames.length == 0;}

    public boolean getSign() {return sign;}

    public Mapping addSign(boolean sign) {
        return new Mapping(fromNames, toData, sign ^ this.sign, true);
    }

    public int size() {return fromNames.length;}

    public IntArray getFromNames() {
        return new IntArray(fromNames);
    }

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
            sb.append(",");
        }
        sb.append("}");
        return sb.toString();
    }
}
