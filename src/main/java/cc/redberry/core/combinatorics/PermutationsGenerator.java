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

import java.util.Iterator;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsGenerator<T extends Permutation> implements Iterator<T> {

    private IntPermutationsGenerator generator;

    /**
     * Construct iterator over combinatorics with specified dimension. Start
     * permutation is identity permutation.
     *
     * @param length length of combinatorics
     */
    public PermutationsGenerator(int size) {
        this.generator = new IntPermutationsGenerator(size);
    }

    /**
     * Construct iterator over combinatorics with specified permutation at
     * start. In this way, iterator will not iterate over all possible
     * combinatorics, but only from start permutation up to last permutation,
     * witch is [length-1,length-2,....1,0]. The specified permutation is coping
     * in constructor, so it is not destroying during iteration, in contract
     * with
     * {@link IntPermutationsGenerator#IntPermutationsGenerator(int[])}.
     *
     *
     * @param permutation start permutation of iterator.
     *
     * @throws IllegalArgumentException if permutation is inconsistent with
     *                                  <i>single-line</i> notation
     */
    public PermutationsGenerator(Permutation permutation) {
        int[] array = permutation.getPermutation().copy();
        this.generator = new IntPermutationsGenerator(array);
    }

    /**
     * Returns {@code true} if the iteration has more elements. (In other words,
     * returns {@code true} if {@link #next} would return an element rather than
     * throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return generator.hasNext();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     */
    @SuppressWarnings("unchecked")
    @Override
    public T next() {
        generator.next();
        //noinspection unchecked
        return (T) new Symmetry(generator.permutation.clone(), false);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getDimension() {
        return generator.getDimension();
    }

    public void reset() {
        generator.reset();
    }
}
