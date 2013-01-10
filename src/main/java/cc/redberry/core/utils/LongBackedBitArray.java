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
package cc.redberry.core.utils;

import java.util.Arrays;

/**
 * Bit array based on {@code long}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class LongBackedBitArray implements BitArray {
    private final long[] data;
    private final int size;

    public LongBackedBitArray(int size) {
        this.size = size;
        this.data = new long[(size + 63) >> 6];
    }

    private LongBackedBitArray(long[] data, int size) {
        this.data = data;
        this.size = size;
    }

    @Override
    public void and(BitArray bitArray_) {
        LongBackedBitArray bitArray = (LongBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < data.length; ++i)
            data[i] &= bitArray.data[i];
    }

    @Override
    public int bitCount() {
        int count = 0;
        for (long value : data)
            count += Long.bitCount(value);
        return count;
    }

    @Override
    public void clear(int i) {
        data[i >> 6] &= ~(1L << (i & 0x3F));
    }

    @Override
    public void clearAll() {
        Arrays.fill(data, 0);
    }

    @Override
    public BitArray clone() {
        return new LongBackedBitArray(data.clone(), size);
    }

    @Override
    public boolean get(int i) {
        return (data[i >> 6] & (1L << (i & 0x3F))) != 0;
    }

    @Override
    public int[] getBits() {
        int[] bits = new int[bitCount()];
        int n = 0;
        for (int i = 0; i < size; ++i)
            if (get(i))
                bits[n++] = i;
        return bits;
    }

    @Override
    public boolean intersects(BitArray bitArray_) {
        LongBackedBitArray bitArray = (LongBackedBitArray) bitArray_;
        if (bitArray.size != this.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < this.data.length; ++i)
            if ((this.data[i] & bitArray.data[i]) != 0)
                return true;
        return false;
    }

    @Override
    public void loadValueFrom(BitArray bitArray_) {
        LongBackedBitArray bitArray = (LongBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        System.arraycopy(bitArray.data, 0, data, 0, data.length);
    }

    @Override
    public void or(BitArray bitArray_) {
        LongBackedBitArray bitArray = (LongBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < data.length; ++i)
            data[i] |= bitArray.data[i];
    }

    @Override
    public void set(int i) {
        data[i >> 6] |= (1L << (i & 0x3F));
    }

    @Override
    public void set(int i, boolean value) {
        if (value)
            set(i);
        else
            clear(i);
    }

    @Override
    public void setAll() {
        Arrays.fill(data, 0xFFFFFFFFFFFFFFFFL);
        data[data.length - 1] &= (0xFFFFFFFFFFFFFFFFL >>> ((data.length << 6) - size));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void xor(BitArray bitArray_) {
        LongBackedBitArray bitArray = (LongBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < data.length; ++i)
            data[i] ^= bitArray.data[i];
    }

    @Override
    public int nextTrailingBit(int position) {
        if (position < 0)
            throw new IllegalArgumentException();
        final int firstShift = position & 0x3F;
        int pointer = position >>> 6;
        int result;
        if ((result = Long.numberOfTrailingZeros(data[pointer++] >>> firstShift)) != 64)
            return position + result;
        while (pointer < data.length && (result = Long.numberOfTrailingZeros(data[pointer++])) == 64) ;
        if (result == 64)
            return -1;
        return (pointer - 1) * 64 + result;
    }

    @Override
    public String toString() {
        char[] c = new char[size];
        for (int i = 0; i < size; ++i)
            if (get(i))
                c[i] = '1';
            else
                c[i] = '0';
        return new String(c);
    }
}
