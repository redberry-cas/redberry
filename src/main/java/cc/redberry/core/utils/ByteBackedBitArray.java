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

package cc.redberry.core.utils;

import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 */
public final class ByteBackedBitArray implements BitArray {
    private byte[] data;
    private int size;

    public ByteBackedBitArray(int size) {
        this.size = size;
        this.data = new byte[(size + 7) >> 3];
    }

    ByteBackedBitArray(byte[] data, int size) {
        //if(data.length != ((size + 7) >> 3))
        //    throw new IllegalStateException();
        this.data = data;
        this.size = size;
    }

    @Override
    public boolean get(int i) {
        return (data[i >> 3] & (1 << (i & 7))) != 0;
    }

    @Override
    public void set(int i) {
        data[i >> 3] |= (1 << (i & 7));
    }

    @Override
    public void clear(int i) {
        data[i >> 3] &= ~(1 << (i & 7));
    }

    @Override
    public void set(int i, boolean value) {
        if (value)
            set(i);
        else
            clear(i);
    }

    public void set(ByteBackedBitArray ba) {
        if (ba.size != this.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < this.data.length; ++i)
            this.data[i] = ba.data[i];
    }

    @Override
    public void setAll() {
        //for (int i = 0; i < data.length; ++i)
        //    data[i] = 0xFFFFFFFF;
        Arrays.fill(data, (byte) 0xFF);
        if ((size & 7) != 0)
            data[data.length - 1] = (byte) (0xFF >>> (8 - (size & 0x7)));
    }

    @Override
    public boolean intersects(BitArray bitArray_) {
        ByteBackedBitArray bitArray = (ByteBackedBitArray) bitArray_;
        if (bitArray.size != this.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < this.data.length; ++i)
            if ((this.data[i] & bitArray.data[i]) != 0)
                return true;
        return false;
    }

    @Override
    public int bitCount() {
        int count = 0;
        for (int i = 0; i < data.length; ++i)
            count += Integer.bitCount(0xFF & data[i]);
        return count;
    }

    @Override
    public void or(BitArray bitArray_) {
        ByteBackedBitArray bitArray = (ByteBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < data.length; ++i)
            data[i] |= bitArray.data[i];
    }

    @Override
    public void xor(BitArray bitArray_) {
        ByteBackedBitArray bitArray = (ByteBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < data.length; ++i)
            data[i] ^= bitArray.data[i];
    }

    @Override
    public void and(BitArray bitArray_) {
        ByteBackedBitArray bitArray = (ByteBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        for (int i = 0; i < data.length; ++i)
            data[i] &= bitArray.data[i];
    }

    @Override
    public void loadValueFrom(BitArray bitArray_) {
        ByteBackedBitArray bitArray = (ByteBackedBitArray) bitArray_;
        if (size != bitArray.size)
            throw new IllegalArgumentException();
        System.arraycopy(bitArray.data, 0, data, 0, bitArray.data.length);
    }

    @Override
    public void clearAll() {
        //for (int i = 0; i < data.length; ++i)
        //    data[i] = 0;
        Arrays.fill(data, (byte) 0);
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
    public int size() {
        return size;
    }

    @Override
    public ByteBackedBitArray clone() {
        return new ByteBackedBitArray(Arrays.copyOf(data, data.length), size);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ByteBackedBitArray other = (ByteBackedBitArray) obj;
        if (!Arrays.equals(this.data, other.data))
            return false;
        if (this.size != other.size)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Arrays.hashCode(this.data);
        hash = 19 * hash + this.size;
        return hash;
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

    @Override
    public int nextTrailingBit(int position) {
        if (position < 0)
            throw new IllegalArgumentException();
        final int firstShift = position & 7;
        int pointer = position >>> 3;
        int result;

        if ((result = Integer.numberOfTrailingZeros(data[pointer++] >>> firstShift)) != 32)
            return position + result;
        while (pointer < data.length && (result = Integer.numberOfTrailingZeros(data[pointer++])) == 32);
        if (result == 32)
            return -1;
        return (pointer - 1) * 8 + result;
    }
}