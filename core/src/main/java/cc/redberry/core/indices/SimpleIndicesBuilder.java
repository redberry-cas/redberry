/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builder of simple indices. Constructs simple indices (correctly handling possible symmetries) by
 * sequential append of other indices.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class SimpleIndicesBuilder {

    private final IntArrayList data;
    private final List<SymmetriesHolder> symmetries;

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
        symmetries.add(new SymmetriesHolder(indices.size(), indices.getSymmetries().getGenerators()));
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
        symmetries.add(new SymmetriesHolder(indices.length, Collections.EMPTY_LIST));
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
        symmetries.add(new SymmetriesHolder(indices.size(), Collections.EMPTY_LIST));
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
        List<Permutation> resultingSymmetries = new ArrayList<>();

        int[] c;
        int position = 0;

        //rescaling symmetries to the actual length and positions corresponding
        //to the sorted indices
        for (SymmetriesHolder holder : this.symmetries) {
            for (Permutation s : holder.generators) {
                c = new int[data.length];
                for (j = 0; j < data.length; ++j)
                    if (cosort[j] < position || cosort[j] >= position + s.degree())
                        c[j] = j;
                    else
                        c[j] = cosortInv[s.newIndexOf(cosort[j] - position) + position];
                resultingSymmetries.add(new PermutationOneLine(s.antisymmetry(), c));
            }
            //increasing position in the total symmetry array
            position += holder.length;
        }

        return IndicesFactory.createSimple(IndicesSymmetries.create(new StructureOfIndices(data),
                resultingSymmetries), data);
    }

    private static final class SymmetriesHolder {
        final int length;
        final List<Permutation> generators;

        private SymmetriesHolder(int length, List<Permutation> generators) {
            this.length = length;
            this.generators = generators;
        }
    }
}
