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
package cc.redberry.core.combinatorics;

import java.util.ArrayList;

/**
 * This class allows to iterate over all compositions of a given set of permutations.
 * More formally, for a given set of permutations S={p<sub>1</sub>,p<sub>2</sub>,...,p<sub>N</sub>},
 * it will iterate over all possible different compositions
 * p<sub>i1</sub>*p<sub>i2</sub>...*p<sub>iN</sub>*p<sub>iN+1</sub>...*p<sub>iM</sub>. Mathematically say,
 * this class allows to enumerate all elements of the subgroup of a symmetric group, which is defined by
 * a generating set.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IntPermutationsSpanGenerator
        extends IntCombinatorialGenerator
        implements IntCombinatorialPort {

    private PermutationsSpanIterator<Permutation> innerIterator;
    private final ArrayList<Permutation> permutations;

    /**
     * Creates generator from a given array of permutations.
     *
     * @param permutations array of permutations in one-line notation
     * @throws IllegalArgumentException if some array does not satisfies one-line notation
     */
    public IntPermutationsSpanGenerator(int[]... permutations) {
        this.permutations = new ArrayList<>(permutations.length);
        for (int[] p : permutations)
            this.permutations.add(new Permutation(p));
        innerIterator = new PermutationsSpanIterator<Permutation>(this.permutations);
    }

    @Override
    public void reset() {
        innerIterator = new PermutationsSpanIterator<Permutation>(this.permutations);
    }

    @Override
    public int[] getReference() {
        return innerIterator.current.permutation;
    }

    @Override
    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    @Override
    public int[] next() {
        return innerIterator.next().permutation;
    }

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] take() {
        return hasNext() ? next() : null;
    }
}
