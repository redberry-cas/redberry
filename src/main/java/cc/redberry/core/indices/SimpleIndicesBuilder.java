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

import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.UnsafeCombinatorics;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder of simple indices. Constructs simple indices (correctly handling possible symmetries) by
 * sequential append of other indices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SimpleIndicesBuilder {

    private final IntArrayList data;
    private final List<Symmetries> symmetries;

    /**
     * Construct builder with specified initial capacity.
     *
     * @param initialCapacity initial capacity
     */
    public SimpleIndicesBuilder(int initialCapacity) {
        data = new IntArrayList(initialCapacity);
        symmetries = new ArrayList<>(initialCapacity);
    }

    /**
     * Constructs empty builder.
     */
    public SimpleIndicesBuilder() {
        this(7);
    }

    /**
     * Appends specified simple indices to this taking into account symmetries of passing indices.
     *
     * @param indices simple indices
     * @return this
     */
    public SimpleIndicesBuilder append(SimpleIndices indices) {
        if (indices.size() == 0)
            return this;
        data.addAll(((SimpleIndicesAbstract) indices).data);
        symmetries.add(indices.getSymmetries().getInnerSymmetries());
        return this;
    }

    /**
     * Appends specified indices, represented as integer array to this. The passing indices are considered
     * to have no any symmetries.
     *
     * @param indices integer array of indices
     * @return this
     */
    public SimpleIndicesBuilder append(int... indices) {
        data.addAll(indices);
        symmetries.add(SymmetriesFactory.createSymmetries(indices.length));
        return this;
    }

    /**
     * Appends specified indices. The passing indices are considered
     * to have no any symmetries.
     *
     * @param indices indices
     * @return this
     */
    public SimpleIndicesBuilder appendWithoutSymmetries(Indices indices) {
        if (indices.size() == 0)
            return this;
        data.addAll(((AbstractIndices) indices).data);
        symmetries.add(SymmetriesFactory.createSymmetries(indices.size()));
        return this;
    }

    /**
     * Returns resulting {@code SimpleIndices}.
     *
     * @return resulting {@code SimpleIndices}
     * @throws InconsistentIndicesException if there was more then one same index (with same names, types and states)
     */
    public SimpleIndices getIndices() {
        final int[] data = this.data.toArray();

        //Sorting indices by type
        int j;
        int[] types = new int[data.length];
        for (j = 0; j < data.length; ++j)
            types[j] = data[j] & 0x7F000000;

        int[] cosort = Combinatorics.createIdentity(data.length);
        //only stable sort
        ArraysUtils.stableSort(types, cosort);
        int[] cosortInv = Combinatorics.inverse(cosort);

        //Allocating resulting symmetries object
        //it already contains identity symmetry
        Symmetries resultingSymmetries =
                SymmetriesFactory.createSymmetries(data.length);

        int[] c;
        int position = 0, k;

        //rescaling symmetries to the actual length and positions corresponding
        //to the sorted indices
        for (Symmetries ss : this.symmetries) {
            final List<Symmetry> basis = ss.getBasisSymmetries();
            //iterating from 1 because zero'th element is always identity symmetry 
            for (k = 1; k < basis.size(); ++k) {
                c = new int[data.length];
                Symmetry s = basis.get(k);
                for (j = 0; j < data.length; ++j)
                    if (cosort[j] < position || cosort[j] >= position + s.dimension())
                        c[j] = j;
                    else
                        c[j] = cosortInv[s.newIndexOf(cosort[j] - position) + position];
                resultingSymmetries.addUnsafe(UnsafeCombinatorics.createUnsafe(c, s.isAntiSymmetry()));
            }
            //increasing position in the total symmetry array
            position += ss.dimension();
        }

        return IndicesFactory.createSimple(
                new IndicesSymmetries(new StructureOfIndices(data), resultingSymmetries), data);
    }
}
