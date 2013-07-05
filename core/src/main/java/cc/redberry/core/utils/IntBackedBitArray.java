package cc.redberry.core.utils;

import java.util.Arrays;

import static java.lang.Math.min;

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

    public IntBackedBitArray(boolean[] array) {
        this(array.length);
        for (int i = 0; i < array.length; ++i)
            if (array[i])
                set(i);
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

    int lastElementMask() {
        if ((size & 0x1F) == 0)
            return 0xFFFFFFFF;
        else
            return 0xFFFFFFFF >>> ((data.length << 5) - size);
        //return ~(0xFFFFFFFF << (32 - ((data.length << 5) - size)));
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

    /**
     * Analog of {@link System#arraycopy(Object, int, Object, int, int)}, where src is {@code bitArray}.
     *
     * @param bitArray     source
     * @param sourceOffset source offset
     * @param thisOffset   destination offset
     * @param length       number of bits to copy
     */
    public void loadValueFrom(IntBackedBitArray bitArray, int sourceOffset, int thisOffset, int length) {
        if (bitArray == this)
            throw new IllegalArgumentException("Can't copy from itself.");

        //Forcing alignment on other (bitArray)
        int alignmentOffset = (sourceOffset & 0x1F);

        //Now in words (not in bits)
        sourceOffset = sourceOffset >>> 5;

        if (alignmentOffset != 0) {
            int l = min(32 - alignmentOffset, length);
            loadValueFrom(
                    bitArray.data[sourceOffset] >>> alignmentOffset,
                    thisOffset,
                    l);

            thisOffset += l;
            ++sourceOffset;
            length -= l;
        }

        //Bulk copy
        while (length > 0) {
            loadValueFrom(bitArray.data[sourceOffset], thisOffset, min(32, length));
            length -= 32;
            thisOffset += 32;
            ++sourceOffset;
        }
    }

    /**
     * Load 32 bits or less from single integer
     *
     * @param d        integer with bits
     * @param position offset
     * @param length   length
     */
    void loadValueFrom(int d, int position, int length) {
        if (length == 0)
            return;

        int res = position & 0x1F;
        position = position >>> 5;

        //mask for d
        int mask = 0xFFFFFFFF >>> (32 - length);

        if (res == 0) {
            if (length == 32)
                data[position] = d;
            else {
                data[position] &= ~mask;
                data[position] |= d & mask;
            }
            return;
        }

        data[position] &= ~(mask << res);
        data[position] |= (d & mask) << res;

        length -= (32 - res);
        if (length > 0)
            loadValueFrom(d >>> (32 - res), (position + 1) << 5, length);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntBackedBitArray that = (IntBackedBitArray) o;

        if (size != that.size) return false;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + size;
        return result;
    }
}
