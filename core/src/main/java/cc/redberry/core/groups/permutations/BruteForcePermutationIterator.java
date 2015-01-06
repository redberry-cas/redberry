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
package cc.redberry.core.groups.permutations;


import java.util.*;

/**
 * Brute-force iterator over all permutations in group, specified by generation set
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
final class BruteForcePermutationIterator implements Iterator<Permutation> {
    static final Comparator<Permutation> JUST_PERMUTATION_COMPARATOR = new Comparator<Permutation>() {
        @Override
        public int compare(Permutation o1, Permutation o2) {
            return o1.compareTo(o2);
        }
    };
    private TreeSet<Permutation> set = null;
    private List<Permutation> upperLayer;
    private List<Permutation> lowerLayer = new ArrayList<>();
    private List<Permutation> nextLayer = new ArrayList<>();
    private boolean forward = false;
    private int upperIndex = 0, lowerIndex = 0;

    public BruteForcePermutationIterator(List<Permutation> permutations) {
        set = new TreeSet<>(JUST_PERMUTATION_COMPARATOR);
        this.upperLayer = new ArrayList<>();
        //noinspection unchecked
        this.upperLayer.add(Permutations.createIdentityPermutation(Permutations.internalDegree(permutations)));
        this.lowerLayer = permutations;
    }

    Permutation current;

    /**
     * @throws cc.redberry.core.combinatorics.InconsistentGeneratorsException if combinatorics are inconsistent,
     *                                                                        e.g. it can happens when symmetries represents identical permutation but
     *                                                                        has different {@code signums}
     */
    @Override
    public boolean hasNext() {
        return (current = next1()) != null;
    }

    @Override
    public Permutation next() {
        return current;
    }

    private Permutation next1() {
        Permutation composition = null;
        while (composition == null)
            if (forward) {
                composition = tryPair(upperLayer.get(upperIndex), lowerLayer.get(lowerIndex));
                nexIndices();
                if (lowerLayer.isEmpty())
                    break;
                forward = !forward;
            } else {
                composition = tryPair(lowerLayer.get(lowerIndex), upperLayer.get(upperIndex));
                forward = !forward;
            }
        return composition;
    }

    private void nexIndices() {
        if (++upperIndex < upperLayer.size())
            return;
        upperIndex = 0;
        if (++lowerIndex < lowerLayer.size())
            return;
        lowerIndex = 0;
        upperLayer = new ArrayList<>(set);
        lowerLayer = nextLayer;
        nextLayer = new ArrayList<>();
    }

    private Permutation tryPair(Permutation p0, Permutation p1) {
        Permutation composition =  p0.composition(p1);
        Permutation setComposition = set.ceiling(composition);
        if (setComposition != null && JUST_PERMUTATION_COMPARATOR.compare(setComposition, composition) == 0)
            if (setComposition.equals(composition))
                return null;
            else
                throw new InconsistentGeneratorsException(composition + " and " + setComposition);
        set.add(composition);
        nextLayer.add(composition);
        return composition;
    }

    /**
     * Throws UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
