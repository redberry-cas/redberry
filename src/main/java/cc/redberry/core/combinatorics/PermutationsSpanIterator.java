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
 * This class provides generating all compositions of given set of combinatorics.
 * So, if we have some set of combinatorics S={p1,p2,...,pN}, this class can iterate
 * over all possible compositions in all possible orders of combinatorics p1,p2,....
 * In this way, we iterating over combinations 
 * <var>p<sub>i<sub>1</sub></sub><sup>n<sub>1</sub><sup></var>
 * <var>p<sub>i<sub>2</sub></sub><sup>n<sub>2</sub><sup></var>....
 * <var>p<sub>i<sub>N</sub></sub><sup>n<sub>N</sub><sup></var>
 * throughout all possible combinations of 
 * {<var>i<sub>1</sub></var>,<var>i<sub>2</sub></var>,...<var>i<sub>N</sub></var>}
 * and {<var>n<sub>1</sub></var>,<var>n<sub>2</sub></var>,...<var>n<sub>N</sub></var>}, 
 * where each i index runs 0...N and each n index runs 0...&#8734;, until the 
 * result is unique. Algorithm, witch provides such iterating, uses {@link TreeSet}
 * for store generated combinatorics.
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * 
 * @param <T> some inheritor of Permutation, e.g. {@code Permutation} or {@code Symmetry}
 */
public class PermutationsSpanIterator<T extends Permutation> implements Iterator<T> {
    private TreeSet<T> set = null;
    public Iterator<T> iterator = null;
    private List<T> upperLayer;
    private List<T> lowerLayer = new ArrayList<>();
    private List<T> nextLayer = new ArrayList<>();
    private boolean forward = false;
    T setPermutation, layerPermutation;
    private int upperIndex = 0, lowerIndex = 0;

    public PermutationsSpanIterator(List<T> permutations) {
        set = new TreeSet<>();
        this.upperLayer = new ArrayList<>();
        this.upperLayer.add((T) permutations.get(0).getOne());
        this.lowerLayer = permutations;
    }
    T current;

    /**
     * 
     * {@inheritDoc }
     * 
     * @return {@inheritDoc}
     * @throws InconsistentGeneratorsException if combinatorics are inconsistent,
     * e.g. it can happens when symmetries represents identical permutation but 
     * has different {@code signums}
     */
    @Override
    public boolean hasNext() {
        return (current = next1()) != null;
    }

    /**
     * 
     * {@inheritDoc }
     * 
     * @return {@inheritDoc}
     */
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
                continue;
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
        T composition = (T) p0.composition(p1);
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
