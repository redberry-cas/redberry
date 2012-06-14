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

import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.UnsafeCombinatorics;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.utils.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SimpleIndicesBuilder {

    private final IntArrayList data;
    private final List<Symmetries> symmetries;

    public SimpleIndicesBuilder(int initialCapacity) {
        data = new IntArrayList(initialCapacity);
        symmetries = new ArrayList<>(initialCapacity);
    }

    public SimpleIndicesBuilder() {
        this(7);
    }

    public SimpleIndicesBuilder append(SimpleIndices indices) {
        if (indices.size() == 0)
            return this;
        data.addAll(((SimpleIndicesAbstract) indices).data);
        symmetries.add(indices.getSymmetries().getReference());
        return this;
    }

    public SimpleIndicesBuilder appendWithoutSymmetries(Indices indices) {
        if (indices.size() == 0)
            return this;
        data.addAll(((AbstractIndices) indices).data);
        symmetries.add(SymmetriesFactory.createSymmetries(indices.size()));
        return this;
    }

    public SimpleIndices getIndices() {
        final int[] data = this.data.toArray();

        int sCount = 0;
        //calculating total number of symmetries except identities
        for (Symmetries s : symmetries)
            sCount += s.getBasisSymmetries().size() - 1;

        //allocating arrays of resulting symmetries
        final int[][] symmetries = new int[sCount][];
        //allocating bit array which keeps signs of resulting symmetries
        BitArray signs = new LongBackedBitArray(sCount);

        int position = 0;
        sCount = 0;
        int[] c;
        int j, k;
        //rescaling symmetries to the actual length of resulting indices
        for (Symmetries ss : this.symmetries) {
            final List<Symmetry> basis = ss.getBasisSymmetries();
            //iterating from 1 because zero'th element is always identity symmetry 
            for (k = 1; k < basis.size(); ++k) {
                c = symmetries[sCount] = new int[data.length];
                Symmetry s = basis.get(k);
                for (j = 0; j < position; ++j)
                    c[j] = j;
                for (; j < position + s.dimension(); ++j)
                    c[j] = s.newIndexOf(j - position) + position;
                for (; j < data.length; ++j)
                    c[j] = j;
                signs.set(sCount++, s.isAntiSymmetry());
            }
            //increasing position in the total symmetry array
            position += ss.dimension();
        }

        int[] coSort = Combinatorics.createIdentity(data.length);
        int[] types = new int[data.length];
        for (j = 0; j < data.length; ++j)
            types[j] = data[j] & 0x7F000000;
        if (types.length > 100)
            ArraysUtils.timSort(types, coSort);
        else
            ArraysUtils.insertionSort(types, coSort);
        int[] coSortInv = Combinatorics.inverse(coSort);

        Symmetries resultingSymmetries =
                SymmetriesFactory.createSymmetries(data.length);
        for (sCount = 0; sCount < symmetries.length; ++sCount) {
            c = new int[data.length];
            for (j = 0; j < data.length; ++j)
                c[j] = coSortInv[symmetries[sCount][coSort[j]]];
            resultingSymmetries.addUnsafe(UnsafeCombinatorics.createUnsafe(c, signs.get(sCount)));
        }
        return IndicesFactory.createSimple(
                new IndicesSymmetries(new IndicesTypeStructure(data),
                                      resultingSymmetries), data);
    }
}
