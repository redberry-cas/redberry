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

import cc.redberry.core.utils.IntArray;

import java.util.Arrays;
import java.util.TreeMap;

/**
 * This class is a representation of mathematical permutation. Fore information
 * about math combinatorics and it properties see (<a
 * href="http://en.wikipedia.org/wiki/Permutation">http://en.wikipedia.org/wiki/Permutation</a>).
 * For keeping permutation it uses <i>one-line</i> notation. Instances of this
 * class are immutable. {@link Comparable} implementation is necessary for using
 * {@link TreeMap} of combinatorics in {@link PermutationsSpanIterator}
 *
 * @see Comparable
 * @see PermutationsSpanIterator
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Permutation implements Comparable<Permutation> {

    protected final int[] permutation;

    /**
     * Constructs identity permutation with specified dimension.
     *
     * @param dimension dimension of permutation
     */
    public Permutation(int dimension) {
        permutation = new int[dimension];
        for (int i = 0; i < dimension; ++i)
            permutation[i] = i;
    }

    /**
     * Constructs permutation, specified by {@code permutation} integer array in
     * <i>single-line</i> notation.
     *
     * @param permutation <i>single-line</i> notated integer array, representing
     *                    a permutation.
     *
     * @throws IllegalArgumentException if array is inconsistent with
     *                                  <i>one-line</i> notation
     */
    public Permutation(int[] permutation) {
        if (!Combinatorics.testPermutationCorrectness(permutation))
            throw new IllegalArgumentException("Wrong permutation input: input array is not consistent with one-line notation");
        this.permutation = permutation.clone();
    }

   

    protected Permutation(int[] permutation, boolean notClone) {
        this.permutation = permutation;
    }

    /**
     * Returns identity permutation.
     *
     * @return identity permutation
     */
    public Permutation getOne() {
        return new Permutation(permutation.length);
    }

    protected int[] compositionArray(Permutation element) {
        if (permutation.length != element.permutation.length)
            throw new IllegalArgumentException("different dimensions of compositing combinatorics");
        int[] perm = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            perm[i] = element.permutation[permutation[i]];
        return perm;
    }

    /**
     * Returns new permutation, witch is a 'left' composition with specified
     * permutation. So, if this permutation A and specified permutation is B, it
     * returns A*B.
     *
     * @param element is a right multiplicand permutation
     *
     * @return composition of element and this combinatorics
     *
     * @throws IllegalArgumentException if element has different dimension than
     *                                  this one
     */
    public Permutation composition(Permutation element) {
        return new Permutation(compositionArray(element), true);
    }

    /**
     *
     * @param array array to permute
     *
     * @return permuted array copy
     */
    public int[] permute(int[] array) {
        if (array.length != permutation.length)
            throw new IllegalArgumentException("Wrong lenght");
        int[] copy = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            copy[permutation[i]] = array[i];
        return copy;
    }

    public Symmetry asSymmetry() {
        return new Symmetry(permutation, false);
    }

    protected int[] calculateInverse() {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            inverse[permutation[i]] = i;
        return inverse;
    }

    /**
     * Returns new index of specified index, i.e. permutation[index]
     *
     * @param index old index
     *
     * @return new index of specified index, i.e. permutation[index]
     */
    public int newIndexOf(int index) {
        return permutation[index];
    }

    /**
     * Returns dimension of permutation.
     *
     * @return dimension of permutation
     */
    public int dimension() {
        return permutation.length;
    }

    /**
     * Due to immutability of {@code Permuatation} this method returns simple
     * wrapper of integer array, representing one-line notated permutation.
     *
     * @see IntArray
     * @return IntArray representing integer array - one-line notated
     *         permutation
     */
    public IntArray getPermutation() {
        return new IntArray(permutation);
    }

    /**
     * Returns inverse permutation of this one.
     *
     * @return inverse permutation of this one
     */
    public Permutation inverse() {
        return new Permutation(calculateInverse(), true);
    }

    /**
     * Returns true if {@code obj} has the same class and represents the same
     * permutation and false in the other case.
     *
     * @param obj object to be compared with this
     *
     * @return true if {@code obj} has the same class and represents the same
     * permutation and false in the other case
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return Arrays.equals(permutation, ((Permutation) obj).permutation);
    }

    /**
     * Returns hash code of this permutation.
     *
     * @return hash code of this permutation
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(permutation) * 7 + 31;
    }

    /**
     * Returns the string representation of this permutation in one-line
     * notation.
     *
     * @return the string representation of this permutation in one-line
     *         notation
     */
    @Override
    public String toString() {
        return Arrays.toString(permutation);
    }

    /**
     * Compares this permutation with other. The algorithm sequentially compares
     * integers {@code i1} and {@code i2} in arrays, representing this
     * permutation and other permutation relatively. If on some step {@code i1 > i2}
     * returns 1, if one some step {@code i2 > i1 } returns -1, and if on all
     * steps
     * {@code i1 == i2} returns 0 (combinatorics are equals).
     *
     * @param t permutation to compare
     *
     * @return 1 if this one is "greater" -1 if t is "greater", 0 if this and t
     *         equals.
     *
     * @throws IllegalArgumentException if dimensions of this and t are not '
     *                                  equals
     */
    @Override
    public int compareTo(Permutation t) {
        if (t.permutation.length != permutation.length)
            throw new IllegalArgumentException("different dimensions of comparing combinatorics");
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] < t.permutation[i])
                return -1;
            else if (permutation[i] > t.permutation[i])
                return 1;
        return 0;
    }

    public boolean compare(final int[] permutation) {
        return Arrays.equals(this.permutation, permutation);
    }
}
