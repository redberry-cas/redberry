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
 * This class is a simple wrapper of {@link IntPermutationsGenerator}. It returns the objects of
 * type {@code Permutation} instead of integer arrays.
 * <p/>
 *
 * @param <T> {@code Permutation} subtype
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class PermutationsGenerator<T extends Permutation> implements Iterator<T> {

    private IntPermutationsGenerator generator;

    /**
     * Construct iterator over all permutations with specified dimension starting with identity.
     *
     * @param dimension dimension of permutations
     */
    public PermutationsGenerator(int dimension) {
        this.generator = new IntPermutationsGenerator(dimension);
    }

    /**
     * Construct iterator over permutations with specified permutation at
     * the start. If starting permutation is not identity, the iterator will not
     * iterate over all possible combinatorics, but only from starting permutation up to the
     * last permutation, which is [size-1,size-2,....1,0]. <b>Note:</b> parameter {@code permutation} is
     * not coping in constructor and will change during iteration.
     *
     * @param permutation starting permutation
     * @throws IllegalArgumentException if permutation is inconsistent with
     *                                  <i>one-line</i> notation
     */
    public PermutationsGenerator(Permutation permutation) {
        int[] array = permutation.getPermutation().copy();
        this.generator = new IntPermutationsGenerator(array);
    }

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

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Resets the iteration
     */
    public void reset() {
        generator.reset();
    }
}
