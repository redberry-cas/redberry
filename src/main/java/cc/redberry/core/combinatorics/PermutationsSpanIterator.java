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
package cc.redberry.core.combinatorics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * This class allows to iterate over all compositions of a given set of permutations.
 * More formally, for a given set of permutations S={p<sub>1</sub>,p<sub>2</sub>,...,p<sub>N</sub>},
 * it will iterate over all possible different compositions
 * p<sub>i1</sub>*p<sub>i2</sub>...*p<sub>iN</sub>*p<sub>iN+1</sub>...*p<sub>iM</sub>. Mathematically say,
 * this class allows to enumerate all elements of the subgroup of a symmetric group, which is defined by
 * a generating set.
 *
 * @param <T> {@code Permutation} type
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class PermutationsSpanIterator<T extends Permutation> implements Iterator<T> {
    private TreeSet<T> set = null;
    public Iterator<T> iterator = null;
    private List<T> upperLayer;
    private List<T> lowerLayer = new ArrayList<>();
    private List<T> nextLayer = new ArrayList<>();
    private boolean forward = false;
    private int upperIndex = 0, lowerIndex = 0;

    public PermutationsSpanIterator(List<T> permutations) {
        set = new TreeSet<>();
        this.upperLayer = new ArrayList<>();
        //noinspection unchecked
        this.upperLayer.add((T) permutations.get(0).getOne());
        this.lowerLayer = permutations;
    }

    T current;

    /**
     * @throws InconsistentGeneratorsException
     *          if combinatorics are inconsistent,
     *          e.g. it can happens when symmetries represents identical permutation but
     *          has different {@code signums}
     */
    @Override
    public boolean hasNext() {
        return (current = next1()) != null;
    }

    @Override
    public T next() {
        return current;
    }

    private T next1() {
        T composition = null;
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

    private T tryPair(T p0, T p1) {
        @SuppressWarnings("unchecked") T composition = (T) p0.composition(p1);
        T setComposition = set.ceiling(composition);
        if (setComposition != null && setComposition.compareTo(composition) == 0)
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
