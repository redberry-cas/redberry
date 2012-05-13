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

import cc.redberry.core.combinatorics.Permutation;
import cc.redberry.core.combinatorics.Permutations;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndicesBuilderSimple
        implements IndicesBuilder {

    private final IntArrayList data;
    private List<SymmetryContainer> symmetries = new ArrayList<>();

    public IndicesBuilderSimple() {
        data = new IntArrayList();
        //adding identity symmetry
        symmetries.add(new SymmetryContainer());
    }

    public IndicesBuilderSimple(int capacity) {
        data = new IntArrayList(capacity);
        //adding identity symmetry
        symmetries.add(new SymmetryContainer(capacity));
    }

    private IndicesBuilderSimple(IntArrayList data, List<SymmetryContainer> symmetries) {
        this.data = data;
        this.symmetries = symmetries;
    }

    @Override
    public IndicesBuilderSimple append(int index) {
        for (SymmetryContainer arrayList : symmetries)
            arrayList.list.add(data.size());
        data.add(index);
        return this;
    }

    private void addSymmetriesTail(final int length) {
        int[] additionSymmetrytail = new int[length];
        for (int i = 0; i < length; ++i)
            additionSymmetrytail[i] = data.size() + i;
        for (SymmetryContainer arrayList : symmetries)
            arrayList.list.addAll(additionSymmetrytail);
    }

    @Override
    public IndicesBuilderSimple append(int[] indices) {
        addSymmetriesTail(indices.length);
        data.addAll(indices);
        return this;
    }

    @Override
    public IndicesBuilderSimple append(final IntArray indices) {
        addSymmetriesTail(indices.length());
        data.addAll(indices);
        return this;
    }

    @Override
    public IndicesBuilderSimple append(IntArrayList indices) {
        addSymmetriesTail(indices.size());
        data.addAll(indices);
        return this;
    }

    @Override
    public IndicesBuilderSimple append(Indices indices) {

        if (indices instanceof EmptyIndices)
            return this;
        if (indices instanceof SortedIndices)
            return append(indices.getAllIndices());

        //processing symmetries

        final int oldSize = data.size();

        addSymmetriesTail(indices.size());
        List<Symmetry> addingSymmetries = ((SimpleIndices) indices).getSymmetries().getReference().getBaseSymmetries();
        int i;
        for (Symmetry s : addingSymmetries) {
            //without identity
            if (Permutations.isIdentity(s))
                continue;
            IntArrayList newPermutation = new IntArrayList();
            for (i = 0; i < oldSize; ++i)
                newPermutation.add(i);
            for (i = 0; i < indices.size(); ++i)
                newPermutation.add(oldSize + s.newIndexOf(i));
            symmetries.add(new SymmetryContainer(newPermutation, s.isAntiSymmetry()));
        }
        data.addAll(indices.getAllIndices());
        return this;
    }

    @Override
    public IndicesBuilderSimple append(IndicesBuilder ib) {
        return append(ib.getIndices());
    }

    @Override
    public IndicesBuilderSimple append(Tensor tensor) {
        return append(tensor.getIndices());
    }

    @Override
    public IndicesBuilderSimple append(Tensor... tensor) {
        for (Tensor t : tensor)
            append(t);
        return this;
    }

    @Override
    public SimpleIndices getIndices() {
        final int[] indices = data.toArray();

        int[] types = new int[indices.length];
        for (int i = 0; i < indices.length; ++i)
            types[i] = indices[i] & 0x7F000000;

        int[] sortPermutation = Permutations.createIdentity(indices.length);
        if (types.length > 100)
            ArraysUtils.timSort(types, sortPermutation);
        else
            ArraysUtils.insertionSort(types, sortPermutation);

        final int size = data.size();

        Permutation sort = new Permutation(sortPermutation);

        final Symmetry[] resulting = new Symmetry[symmetries.size() - 1];
        int i, j;
        int[] permutation;
        IntArrayList current;
        //0th is identity identity
        for (i = 1; i < resulting.length + 1; ++i) {
            current = symmetries.get(i).list;
            permutation = new int[size];
            for (j = 0; j < size; ++j)
                permutation[j] = sort.inverse().newIndexOf(current.get(sort.newIndexOf(j)));
            resulting[i - 1] = new Symmetry(permutation, symmetries.get(i).sign);
        }
        IndicesSymmetries indicesSymmetries = new IndicesSymmetries(new IndicesTypeStructure(indices));
        indicesSymmetries.addAllUnsafe(resulting);
        SimpleIndices simpleIndices = IndicesFactory.createSimple(indicesSymmetries, indices);
        return simpleIndices;
    }

    @Override
    public int[] toArray() {
        return data.toArray();
    }

    
    @Override
    public String toString() {
        return getIndices().toString();
    }

    @Override
    public IndicesBuilderSimple clone() {
        List<SymmetryContainer> _symmetries = new ArrayList<>(symmetries.size() * 2 / 3);
        for (SymmetryContainer container : symmetries)
            _symmetries.add(container.clone());
        return new IndicesBuilderSimple(data.clone(), _symmetries);
    }

    private static final class SymmetryContainer {

        final IntArrayList list;
        boolean sign;

        SymmetryContainer() {
            list = new IntArrayList();
        }

        SymmetryContainer(int capacity) {
            list = new IntArrayList(capacity);
        }

        SymmetryContainer(IntArrayList list, boolean sign) {
            this.list = list;
            this.sign = sign;
        }

        @Override
        public SymmetryContainer clone() {
            return new SymmetryContainer(list.clone(), sign);
        }
    }
}
