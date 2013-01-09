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
package cc.redberry.core.indices;

import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.Arrays;

/**
 * This class represents sorted indices, i.e. it stores indices as sorted array.
 * Some common methods can be implemented with rather fast algorithms, working
 * with sorted array. {@code SortedIndices} are using to represent, for example
 * {@code Product} indices. Really, if we consider tensor A_m*B_n, we cannot
 * prefer what index is first and what is the second (m or n). So it is useful
 * to quickSort indices array to provide fast algorithms, for example for
 * methods {@code getFree(), getUpper(), testConsistent()} and so on.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Indices
 */
final class SortedIndices extends AbstractIndices {

    //position of the first lower index in array
    private final int firstLower;

    private SortedIndices(int[] data, int firstLower) {
        super(data);
        this.firstLower = firstLower;
    }

    /**
     * Constructs {@code SortedIndices} instance from specified indices array
     * and with specified symmetries. {@code data} will be sorted in
     * constructor.
     *
     * @param data array of indices
     */
    SortedIndices(int[] data) {
        super(data);
        Arrays.sort(this.data);
        firstLower = ArraysUtils.binarySearch1(data, 0);
        testConsistentWithException();
    }

    @Override
    protected UpperLowerIndices calculateUpperLower() {
        int[] upper = Arrays.copyOfRange(data, 0, firstLower);
        int[] lower = Arrays.copyOfRange(data, firstLower, data.length);
        return new UpperLowerIndices(upper, lower);
    }

    @Override
    public int size(IndexType type) {
        int type_ = type.getType(), size = 0;

        int lowerPosition = Arrays.binarySearch(data, 0, firstLower, (type_ << 24) | 0x80000000);
        if (lowerPosition < 0) lowerPosition = ~lowerPosition;
        int upperPosition = Arrays.binarySearch(data, lowerPosition, firstLower, ((type_ + 1) << 24) | 0x80000000);
        if (upperPosition < 0) upperPosition = ~upperPosition;
        size += upperPosition - lowerPosition;

        lowerPosition = Arrays.binarySearch(data, firstLower, data.length, type_ << 24);
        if (lowerPosition < 0) lowerPosition = ~lowerPosition;
        upperPosition = Arrays.binarySearch(data, lowerPosition, data.length, (type_ + 1) << 24);
        if (upperPosition < 0) upperPosition = ~upperPosition;
        size += upperPosition - lowerPosition;
        return size;
    }

    @Override
    public int get(IndexType type, int position) {
        int type_ = type.getType();

        int lowerPosition = Arrays.binarySearch(data, 0, firstLower, (type_ << 24) | 0x80000000);
        if (lowerPosition < 0) lowerPosition = ~lowerPosition;
        int upperPosition = Arrays.binarySearch(data, lowerPosition, firstLower, ((type_ + 1) << 24) | 0x80000000);
        if (upperPosition < 0) upperPosition = ~upperPosition;
        if (lowerPosition + position < upperPosition)
            return data[lowerPosition + position];
        position = position - (upperPosition - lowerPosition);

        lowerPosition = Arrays.binarySearch(data, firstLower, data.length, type_ << 24);
        if (lowerPosition < 0) lowerPosition = ~lowerPosition;
        upperPosition = Arrays.binarySearch(data, lowerPosition, data.length, (type_ + 1) << 24);
        if (upperPosition < 0) upperPosition = ~upperPosition;
        if (lowerPosition + position < upperPosition)
            return data[lowerPosition + position];
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Indices getFree() {
        IntArrayList list = new IntArrayList();
        int u, l;
        int iLower = firstLower, iUpper = 0;
        for (; iUpper < firstLower && iLower < data.length; ++iLower, ++iUpper) {
            u = data[iUpper] & 0x7FFFFFFF; //taking name with type
            l = data[iLower];
            if (u < l) {
                list.add(data[iUpper]);
                --iLower;
            } else if (l < u) {
                list.add(l);
                --iUpper;
            }
        }
        list.add(data, iUpper, firstLower - iUpper);
        list.add(data, iLower, data.length - iLower);
        return IndicesFactory.create(list.toArray());
    }

    @Override
    public Indices getOfType(IndexType type) {
        int type_ = type.getType();

        int lowerPositionU = Arrays.binarySearch(data, 0, firstLower, (type_ << 24) | 0x80000000);
        if (lowerPositionU < 0) lowerPositionU = ~lowerPositionU;
        int upperPositionU = Arrays.binarySearch(data, lowerPositionU, firstLower, ((type_ + 1) << 24) | 0x80000000);
        if (upperPositionU < 0) upperPositionU = ~upperPositionU;
        int sizeU = upperPositionU - lowerPositionU;

        int lowerPositionL = Arrays.binarySearch(data, firstLower, data.length, type_ << 24);
        if (lowerPositionL < 0) lowerPositionL = ~lowerPositionL;
        int upperPositionL = Arrays.binarySearch(data, lowerPositionL, data.length, (type_ + 1) << 24);
        if (upperPositionL < 0) upperPositionL = ~upperPositionL;
        int sizeL = upperPositionL - lowerPositionL;

        if (sizeU + sizeL == data.length)
            return this;
        else if (sizeU + sizeL == 0)
            return IndicesFactory.EMPTY_INDICES;

        int[] indices = new int[sizeU + sizeL];
        System.arraycopy(data, lowerPositionU, indices, 0, sizeU);
        System.arraycopy(data, lowerPositionL, indices, sizeU, sizeL);
        return new SortedIndices(indices, sizeU);
    }

    @Override
    int[] getSortedData() {
        return data;
    }

    @Override
    public Indices getInverted() {
        int[] dataInv = new int[data.length];
        int fl = data.length - firstLower, i = 0;
        for (; i < firstLower; ++i)
            dataInv[fl + i] = data[i] ^ 0x80000000;
        for (; i < data.length; ++i)
            dataInv[i - firstLower] = data[i] ^ 0x80000000;
        return new SortedIndices(dataInv, fl);
    }

    @Override
    public void testConsistentWithException() {
        int i = 0;
        for (; i < firstLower - 1; ++i)
            if (data[i] == data[i + 1])
                throw new InconsistentIndicesException(data[i]);
        for (i = firstLower; i < data.length - 1; ++i)
            if (data[i] == data[i + 1])
                throw new InconsistentIndicesException(data[i]);
    }

    @Override
    public Indices applyIndexMapping(IndexMapping mapping) {
        boolean changed = false;
        int newIndex;
        int[] data_ = data.clone();
        for (int i = 0; i < data.length; ++i)
            if (data_[i] != (newIndex = mapping.map(data_[i]))) {
                data_[i] = newIndex;
                changed = true;
            }
        if (!changed)
            return this;
        return new SortedIndices(data_);
    }

    @Override
    public short[] getDiffIds() {
        return ShortArrayFactory.getZeroFilledShortArray(data.length);
    }
}
