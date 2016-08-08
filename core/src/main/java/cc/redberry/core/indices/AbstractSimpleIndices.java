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
package cc.redberry.core.indices;

import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.areContracted;
import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
abstract class AbstractSimpleIndices extends AbstractIndices implements SimpleIndices {

    IndicesSymmetries symmetries = null;

    /**
     * Construct {@code SimpleIndicesOfTensor} instance from specified indices
     * array and with specified symmetries.
     *
     * @param data       array of indices
     * @param symmetries symmetries of this indices
     */
    AbstractSimpleIndices(int[] data, IndicesSymmetries symmetries) {
        super(data);

        assert data.length != 0;

        int[] toSort = new int[data.length];
        for (int i = 0; i < data.length; ++i)
            toSort[i] = data[i] & 0x7F000000;
        ArraysUtils.stableSort(toSort, data);
        this.symmetries = symmetries;
        testConsistentWithException();
    }

    AbstractSimpleIndices(boolean notResort, int[] data, IndicesSymmetries symmetries) {
        super(data);
        assert data.length != 0;
        this.symmetries = symmetries;
    }

    @Override
    public final SimpleIndices getUpper() {
        UpperLowerIndices ul = getUpperLowerIndices();
        if (ul.upper.length == 0)
            return EmptySimpleIndices.EMPTY_SIMPLE_INDICES_INSTANCE;
        return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort0(null,
                ul.upper, new UpperLowerIndices(ul.upper, new int[0]));
    }

    @Override
    public final SimpleIndices getLower() {
        UpperLowerIndices ul = getUpperLowerIndices();
        if (ul.lower.length == 0)
            return EmptySimpleIndices.EMPTY_SIMPLE_INDICES_INSTANCE;
        return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort0(null,
                ul.lower, new UpperLowerIndices(new int[0], ul.lower));
    }

    @Override
    protected final UpperLowerIndices calculateUpperLower() {
        int upperCount = 0;
        for (int index : data)
            if ((index & 0x80000000) == 0x80000000)
                upperCount++;
        int[] lower = new int[data.length - upperCount];
        int[] upper = new int[upperCount];
        int ui = 0, li = 0;
        for (int index : data)
            if ((index & 0x80000000) == 0x80000000)
                upper[ui++] = index;
            else
                lower[li++] = index;
        return new UpperLowerIndices(upper, lower);
    }

    @Override
    public final int size(IndexType type) {
        int type_ = type.getType() << 24;
        int i = 0;
        for (; i < data.length && (data[i] & 0x7F000000) != type_; ++i) ;
        int size = 0;
        for (; i + size < data.length && (data[i + size] & 0x7F000000) == type_; ++size) ;
        return size;
    }

    @Override
    public final int get(IndexType type, int position) {
        int type_ = type.getType() << 24;
        int i;
        for (i = 0; i < data.length && (data[i] & 0x7F000000) != type_; ++i) ;
        int index = data[i + position];
        if ((index & 0x7F000000) != type_)
            throw new IndexOutOfBoundsException();
        return index;
    }

    @Override
    public final SimpleIndices getInverted() {
        int[] dataInv = new int[data.length];
        for (int i = 0; i < data.length; ++i)
            dataInv[i] = data[i] ^ 0x80000000;
        return create(dataInv, symmetries);
    }

    @Override
    public final SimpleIndices getFree() {
        if (data.length == 1)
            return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort(null, data);
        final IntArrayList dataList = new IntArrayList();
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
        if (dataList.size() == size())//prevent additional memory allocation
            return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort(null, data);
        return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort(null, dataList.toArray());
    }

    @Override
    public final int[] getNamesOfDummies() {
        IntArrayList dataList = new IntArrayList();
        for (int i = 0; i < data.length; i++)
            for (int j = i + 1; j < data.length; ++j) {
                if ((data[i] ^ data[j]) == 0x80000000) {
                    dataList.add(data[i] & 0x7FFFFFFF);
                    break;
                }
            }
        return dataList.toArray();
    }

    @Override
    public final SimpleIndices getOfType(IndexType type) {
        int type_ = type.getType() << 24;
        int i = 0;
        for (; i < data.length && (data[i] & 0x7F000000) != type_; ++i) ;
        int start = i;
        for (; i < data.length && (data[i] & 0x7F000000) == type_; ++i) ;
        int[] newData;
        if (start == 0 && i == data.length)
            newData = data;
        else
            newData = Arrays.copyOfRange(data, start, i);
        return UnsafeIndicesFactory.createIsolatedUnsafeWithoutSort(null, newData);
    }

    @Override
    public final SimpleIndices applyIndexMapping(IndexMapping mapping) {
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
        //FUTURE we really need this check?
        si.testConsistentWithException();
        return si;
    }

    abstract SimpleIndices create(int[] data, IndicesSymmetries symmetries);

    @Override
    final int[] getSortedData() {
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
    public final boolean equalsWithSymmetries(SimpleIndices indices) {
        if (indices.getClass() != this.getClass())
            return false;
        if (data.length != indices.size())
            return false;

        int[] permutation = new int[data.length];
        out:
        for (int i = 0; i < data.length; ++i) {
            int from = data[i];
            for (int j = 0; j < data.length; ++j)
                if (indices.get(j) == from) {
                    permutation[j] = i;
                    continue out;
                }
            return false;
        }
        if (!Permutations.testPermutationCorrectness(permutation))
            return false;

        return this.getSymmetries().getPermutationGroup().membershipTest(
                Permutations.createPermutation(permutation));
    }

    @Override
    public final short[] getPositionsInOrbits() {
        return symmetries.getPositionsInOrbits();
    }

    @Override
    public final StructureOfIndices getStructureOfIndices() {
        return StructureOfIndices.create(this);
    }

    @Override
    public final int contractionsHash() {
        if (data.length == 1)
            return 0;
        if (data.length == 2) {
            if (areContracted(data[0], data[1]))
                return 104729;
            else
                return 0;
        }

        final short[] orbits = getPositionsInOrbits();
        int hash = 0;
        for (int i = 0; i < data.length - 1; ++i)
            for (int j = i + 1; j < data.length; ++j)
                if (areContracted(data[i], data[j]))
                    hash += JenkinWang32shift(orbits[i] + 19 * orbits[j]);
        return hash;
    }
}
