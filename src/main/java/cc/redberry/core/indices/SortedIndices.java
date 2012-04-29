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
 * methods {@code getFreeIndices(), getUpper(), testConsistent()} and so on.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Indices
 * @see OrderedIndices
 */
public final class SortedIndices extends AbstractIndices {
    //position of the first lower index in array
    private final int firstLower;

    private SortedIndices(int[] data, int firstLower) {
        super(data);
        this.firstLower = firstLower;
    }

    /**
     * Constructs {@code SortedIndices} instance from specified indices array
     * and with specified symmetries. {@code data} will be quickSort in
     * constructor, using {@link cc.redberry.core.math.MathUtils#quickSort(int[])} to refactoring
     * symmetries after sorting.
     *
     * @param data array of indices
     * @param symmetries symmetries of this indices
     */
    SortedIndices(int[] data) {
        super(data);
        Arrays.sort(this.data);
        firstLower = ArraysUtils.binarySearch1(data, 0);
        testConsistentWithException();
    }

    SortedIndices(Indices indices) {
        this(indices.getAllIndices().copy());
    }

    @Override
    protected UpperLowerIndices calculateUpperLower() {
        int[] upper = Arrays.copyOfRange(data, 0, firstLower);
        int[] lower = Arrays.copyOfRange(data, firstLower, data.length);
        return new UpperLowerIndices(upper, lower);
    }

    @Override
    public SortedIndices getFreeIndices() {
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
        return new SortedIndices(list.toArray());
    }

    @Override
    protected int[] getSortedData() {
        return data;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public SortedIndices getInverseIndices() {
        int[] dataInv = new int[data.length];
        for (int i = 0; i < data.length; ++i)
            dataInv[i] = data[i] ^ 0x80000000;
        return new SortedIndices(dataInv);
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
