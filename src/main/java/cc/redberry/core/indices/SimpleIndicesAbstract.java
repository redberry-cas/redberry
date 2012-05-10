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

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class SimpleIndicesAbstract extends AbstractIndices implements SimpleIndices {
    protected IndicesSymmetries symmetries = null;

    /**
     * Construct {@code SimpleIndicesOfTensor} instance from specified indices array
     * and with specified symmetries.
     *
     * @param data       array of indices
     * @param symmetries symmetries of this indices
     */
    protected SimpleIndicesAbstract(int[] data, IndicesSymmetries symmetries) {
        super(data);
        int[] toSort = new int[data.length];
        for (int i = 0; i < data.length; ++i)
            toSort[i] = data[i] & 0x7F000000;
        if (toSort.length > 100)
            ArraysUtils.timSort(toSort, data);
        else
            ArraysUtils.insertionSort(toSort, data);
        this.symmetries = symmetries;
        testConsistentWithException();
    }

    protected SimpleIndicesAbstract(boolean notResort, int[] data, IndicesSymmetries symmetries) {
        super(data);
        this.symmetries = symmetries;
    }

    @Override
    protected UpperLowerIndices calculateUpperLower() {
        int upperCount = 0;
        for (int index : data)
            if (index >>> 31 == 1)
                upperCount++;
        int[] lower = new int[data.length - upperCount];
        int[] upper = new int[upperCount];
        int ui = 0, li = 0;
        for (int index : data)
            if (index >>> 31 == 1)
                upper[ui++] = index;
            else
                lower[li++] = index;
        return new UpperLowerIndices(upper, lower);
    }

    @Override
    public SimpleIndices getInverseIndices() {
        int[] dataInv = new int[data.length];
        for (int i = 0; i < data.length; ++i)
            dataInv[i] = data[i] ^ 0x80000000;
        return create(dataInv, symmetries);
    }

    @Override
    public SimpleIndices getFreeIndices() {
        IntArrayList dataList = new IntArrayList();
        boolean y;
        for (int i = 0; i < data.length; i++) {
            y = true;
            for (int j = 0; j < data.length; j++)
                if (i != j && (data[i] ^ data[j]) == 0x80000000) {
                    y = false;
                    break;
                }
            if (y)
                dataList.add(data[i]);
        }
        //FAIL!
        return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort(null, dataList.toArray());
    }

    @Override
    public SimpleIndices applyIndexMapping(IndexMapping mapping) {
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
        SimpleIndices si = create(data_, symmetries);
        si.testConsistentWithException();
        return si;
    }

    protected abstract SimpleIndices create(int[] data, IndicesSymmetries symmetries);

    @Override
    protected int[] getSortedData() {
        int[] sorted = data.clone();
        Arrays.sort(sorted);
        return sorted;
    }

    @Override
    public final void testConsistentWithException() {
        for (int i = 0; i < data.length - 1; ++i)
            for (int j = i + 1; j < data.length; ++j)
                if (data[i] == data[j])
                    throw new InconsistentIndicesException(data[i]);
    }

    @Override
    public boolean equalsWithSymmetries(Indices indices) {
        return _equalsWithSymmetries(indices) == Boolean.FALSE;
    }

    /**
     * More informative method, comparing indices using their symmetries lists.
     * It returns
     * <code>Boolean.FALSE</code> if indices are equals this,
     * <code>Boolean.TRUE</code> if indices differs from this on -1 (i.e. on odd
     * transposition) and
     * <code>null</code> in other case.
     *
     * @param indices indices to compare with this
     * @return <code>Boolean.FALSE</code> if indices are equals this,
     *         <code>Boolean.TRUE</code> if indices differs from this on -1 (i.e. on odd
     *         transposition) and
     *         <code>null</code> in other case.
     */
    public Boolean _equalsWithSymmetries(Indices indices) {
        if (indices.getClass() != this.getClass())
            return null;
        if (data.length != indices.size())
            return null;
        SimpleIndicesOfTensor _indices = (SimpleIndicesOfTensor) indices;
        boolean sign1;
        out_level_0:
        for (Symmetry s1 : symmetries) {
            sign1 = s1.isAntiSymmetry();
            for (int i = 0; i < data.length; ++i)
                if (data[s1.newIndexOf(i)] != (_indices).data[i])
                    continue;
            return Boolean.valueOf(sign1);
        }
        return null;
    }

    @Override
    public short[] getDiffIds() {
        return symmetries.getDiffIds();
    }
}