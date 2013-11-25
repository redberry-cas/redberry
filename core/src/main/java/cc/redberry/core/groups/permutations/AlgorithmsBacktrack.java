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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.combinatorics.IntTuplesPort;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntComparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Basic algorithms for backtrack search in permutation groups.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class AlgorithmsBacktrack {
    private AlgorithmsBacktrack() {
    }

    /**
     * An iterator over group elements, that scans group in increasing order of base images; to be precise: if base
     * B = [b1, b2, b3,..,bn], then element g <i>which maps all base points into themselves</i>
     * (g(b) ∈ B for each b ∈ B) is guaranteed to precedes (≺) an element h, which base image succeeds (≻) the base
     * image of g according to ordering specified by {@link InducedOrderingOfSet}.
     */
    public static class BacktrackIterator implements Iterator<Permutation> {
        final List<BSGSElement> bsgs;
        //tuples generator
        final IntTuplesPort tuples;
        //tuple[i] - current transversal of i-th base point
        int[] tuple;
        //orbit images; sortedOrbits[i] - is an i-th orbit image under permutation word
        //u_{i_indexInBase}*u_{i_{indexInBase - 1}}*...*u_{i_0}, where each i_{j} = tuple[i]
        int[][] sortedOrbits;
        //last first changed position in tuple
        int lastUpdatedIndex;
        //permutation word
        Permutation[] word;
        //bsgs size
        final int size;
        //comparator
        final IntComparator baseComparator;
        //sorted orbit of first base point
        final int[][] cachedSortedOrbits;

        public BacktrackIterator(List<BSGSElement> bsgs) {
            this.bsgs = bsgs;
            this.size = bsgs.size();
            //sizes of orbits, i.e. number of transversals for each base point
            final int[] orbitSizes = new int[bsgs.size()];
            for (int i = 0; i < orbitSizes.length; ++i)
                orbitSizes[i] = bsgs.get(i).orbitSize();
            //initializing tuples generator
            this.tuples = new IntTuplesPort(orbitSizes);
            //comparator of points in Ω(n)
            this.baseComparator = new InducedOrderingOfSet(AlgorithmsBase.getBaseAsArray(bsgs));
            //permutation word
            this.word = new Permutation[bsgs.size()];
            this.sortedOrbits = new int[bsgs.size()][];
            //cached sorted orbits of base points
            this.cachedSortedOrbits = new int[bsgs.size()][];
            for (int i = bsgs.size() - 1; i >= 0; --i) {
                this.cachedSortedOrbits[i] = bsgs.get(i).orbitList.toArray();
                ArraysUtils.quickSort(this.cachedSortedOrbits[i], this.baseComparator);
            }
            this.sortedOrbits[0] = cachedSortedOrbits[0];
            //first iteration
            _next();
        }

        @Override
        public boolean hasNext() {
            return tuple != null;
        }

        @Override
        public Permutation next() {
            Permutation p = word[size - 1];
            _next();
            return p;
        }

        private void _next() {
            tuple = tuples.take();
            if (tuple == null)
                return;

            lastUpdatedIndex = tuples.getLastUpdateDepth();

            if (lastUpdatedIndex == 0)
                word[0] = bsgs.get(0).getTransversalOf(sortedOrbits[0][tuple[0]]);
            else
                word[lastUpdatedIndex] =
                        bsgs.get(lastUpdatedIndex).getTransversalOf(
                                word[lastUpdatedIndex - 1].newIndexOfUnderInverse(sortedOrbits[lastUpdatedIndex][tuple[lastUpdatedIndex]])
                        ).composition(word[lastUpdatedIndex - 1]);


            for (int i = lastUpdatedIndex + 1; i < size; ++i) {
                assert tuple[i] == 0;
                //recalculate sorted orbit after adding a new element in word
                calculateSortedOrbit(i);
                word[i] =
                        bsgs.get(i).getTransversalOf(
                                word[i - 1].newIndexOfUnderInverse(sortedOrbits[i][0])
                        ).composition(word[i - 1]);
            }
        }

        private void calculateSortedOrbit(int i) {
            if (word[i - 1].isIdentity()) {
                sortedOrbits[i] = cachedSortedOrbits[i];
            } else {
                sortedOrbits[i] = word[i - 1].imageOf(bsgs.get(i).orbitList.toArray());
                ArraysUtils.quickSort(sortedOrbits[i], baseComparator);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An ordering of permutations induced by an ordering on Ω(n) (see {@link InducedOrderingOfSet}).
     */
    public static class InducedOrderingOfPermutations implements Comparator<Permutation> {
        final InducedOrderingOfSet inducedOrderingOfSet;
        final int[] base;

        public InducedOrderingOfPermutations(final int[] base) {
            this.inducedOrderingOfSet = new InducedOrderingOfSet(base);
            this.base = base;
        }

        @Override
        public int compare(Permutation a, Permutation b) {
            if (a.degree() != b.degree())
                throw new IllegalArgumentException("Not same degree.");
            int compare;
            for (int i : base)
                if ((compare = inducedOrderingOfSet.compare(a.newIndexOf(i), b.newIndexOf(i))) != 0)
                    return compare;
            return 0;
        }
    }


    /**
     * An ordering of points Ω(n) induced by a base B: if b<sub>i</sub>, b<sub>j</sub> ∈ B then
     * b<sub>i</sub> ≺ b<sub>j</sub> if and only if i < j, and b ≺ a for any b ∈ B and a ∉ B .
     */
    public static class InducedOrderingOfSet implements IntComparator {
        private int[] base;
        private int[] positions;

        public InducedOrderingOfSet(final int[] base) {
            this.base = base.clone();
            this.positions = new int[base.length];
            for (int i = 1; i < base.length; ++i)
                positions[i] = i;
            ArraysUtils.quickSort(this.base, positions);
        }

        private int positionOf(int a) {
            int p = Arrays.binarySearch(base, a);
            if (p < 0)
                return Integer.MAX_VALUE;
            return positions[p];
        }

        @Override
        public int compare(int a, int b) {
            int pa = positionOf(a), pb = positionOf(b);
            if (pa == Integer.MAX_VALUE && pb == Integer.MAX_VALUE)//not base points
                return 0;
            return Integer.compare(pa, pb);
        }
    }
}
