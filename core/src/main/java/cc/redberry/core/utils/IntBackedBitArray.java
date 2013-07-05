package cc.redberry.core.utils;

import java.util.Arrays;

public class IntBackedBitArray {
    final int[] data;
    final int size;

    private IntBackedBitArray(int[] data, int size) {
        this.data = data;
        this.size = size;
    }

    public IntBackedBitArray(int size) {
        this.size = size;
        this.data = new int[(size + 31) >>> 5];
    }

    public void and(IntBackedBitArray bitArray) {
        if (bitArray.size != size)
            throw new IllegalArgumentException();

        for (int i = data.length - 1; i >= 0; --i)
            data[i] &= bitArray.data[i];
    }

    public void or(IntBackedBitArray bitArray) {
        if (bitArray.size != size)
            throw new IllegalArgumentException();

        for (int i = data.length - 1; i >= 0; --i)
            data[i] |= bitArray.data[i];
    }

    public void xor(IntBackedBitArray bitArray) {
        if (bitArray.size != size)
            throw new IllegalArgumentException();

        for (int i = data.length - 1; i >= 0; --i)
            data[i] ^= bitArray.data[i];
    }

    public void not() {
        for (int i = data.length - 1; i >= 0; --i)
            data[i] = ~data[i];

        data[data.length - 1] &= lastElementMask();
    }

    private int lastElementMask() {
        return ~(0xFFFFFFFF << (32 - ((data.length << 5) - size)));
    }

    public int bitCount() {
        int bits = 0;
        for (int i : data)
            bits += Integer.bitCount(i);
        return bits;
    }

    public void clearAll() {
        Arrays.fill(data, 0);
    }

    public IntBackedBitArray clone() {
        return new IntBackedBitArray(data.clone(), size);
    }

    //public int[] getBits() {
    //    /*IntArrayList ial = new IntArrayList();
    //    for (int i = 0; i < data.length; ++i)
    //        nextTrailingBit() */
    //    return new int[0];
    //}

    public boolean intersects(IntBackedBitArray bitArray) {
        if (bitArray.size != size)
            throw new IllegalArgumentException();

        for (int i = data.length - 1; i >= 0; --i)
            if ((bitArray.data[i] & data[i]) != 0)
                return true;

        return false;
    }

    public void loadValueFrom(IntBackedBitArray bitArray) {
        System.arraycopy(bitArray.data, 0, data, 0, data.length);
    }

    public boolean get(int i) {
        return (data[i >> 5] & (1 << (i & 0x1F))) != 0;
    }

    public void set(int i) {
        data[i >> 5] |= (1 << (i & 0x1F));
    }

    public void clear(int i) {
        data[i >> 5] &= ~(1 << (i & 0x1F));
    }

    public void set(int i, boolean value) {
        if (value)
            set(i);
        else
            clear(i);
    }

    public void setAll() {
        for (int i = data.length - 2; i >= 0; --i)
            data[i] = 0xFFFFFFFF;

        data[data.length - 1] = lastElementMask();
    }

    public int size() {
        return size;
    }

    public boolean isFull() {
        for (int i = data.length - 2; i >= 0; --i)
            if (data[i] != 0xFFFFFFFF)
                return false;

        return data[data.length - 1] == lastElementMask();
    }

    public boolean isEmpty() {
        for (int i = data.length - 1; i >= 0; --i)
            if (data[i] != 0)
                return false;

        return true;
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
