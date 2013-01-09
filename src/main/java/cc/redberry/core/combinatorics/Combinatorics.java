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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * This class provides static factory and utility combinatorial methods.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class Combinatorics {

    private Combinatorics() {
    }

    /**
     * <p>Returns an {@link IntCombinatorialGenerator} object, which allows to iterate over
     * all possible unique combinations (with permutations, i.e. {0,1} and {1,0} both appears in the iteration) of
     * {@code k} numbers, which can be chosen from the set of {@code n} numbers, numbered in the order
     * 0,1,2,...,{@code n}. The total number of such combinations will be {@code n!/(n-k)!}.</p>
     * <p/>
     * <p>For example, for {@code k=2} and {@code n=3}, this method will produce an iterator over
     * the following arrays: [0,1], [1,0], [0,2], [2,0], [1,2], [2,1].</p>
     *
     * @param n
     * @param k
     * @return an iterator over all combinations (with permutations) to choose k numbers from n numbers.
     * @see IntCombinatorialGenerator
     */
    public static IntCombinatorialGenerator createIntGenerator(int n, int k) {
        if (n < k)
            throw new IllegalArgumentException();
        if (n == k)
            return new IntPermutationsGenerator(n);
        else
            return new IntCombinationPermutationGenerator(n, k);
    }

    /**
     * Checks whether specified permutation written in one-line notation is identity.
     *
     * @param permutation permutation in one-line notation
     * @return {@code true} if permutation is identity, {@code false} if not
     */
    public static boolean isIdentity(final int[] permutation) {
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] != i)
                return false;
        return true;

    }

    /**
     * Checks whether specified permutation is identity.
     *
     * @param permutation permutation
     * @return {@code true} if permutation is identity, {@code false} if not
     */
    public static boolean isIdentity(Permutation permutation) {
        return isIdentity(permutation.permutation);
    }

    /**
     * Checks whether specified symmetry is identity, i.e. it
     * represents an identity permutation and have positive sign.
     *
     * @param symmetry symmetry
     * @return {@code true} if symmetry is identity and have positive sign, and {@code false} if not
     */
    public static boolean isIdentity(Symmetry symmetry) {
        return !symmetry.isAntiSymmetry() && isIdentity(symmetry.permutation);
    }

    /**
     * Returns an identity permutation written in one-line notation,
     * i.e. an array of length {@code dimension} filled with consecutive numbers.
     *
     * @param dimension dimension
     * @return identity permutation written in one-line notation
     */
    public static int[] createIdentity(final int dimension) {
        int[] perm = new int[dimension];
        for (int i = 0; i < dimension; ++i)
            perm[i] = i;
        return perm;
    }

    /**
     * Creates transposition of first two elements written in one-line notation
     * with specified dimension, i.e. an array of form [1,0,2,3,4,...,{@code dimension - 1}].
     *
     * @param dimension dimension of the resulting permutation, e.g. the array length
     * @return transposition permutation in one-line notation
     */
    public static int[] createTransposition(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException("Dimension is negative.");
        if (dimension > 1)
            return createTransposition(dimension, 0, 1);
        return new int[dimension];
    }

    /**
     * Creates transposition in one-line notation
     *
     * @param dimension dimension of the resulting permutation, e.g. the array length
     * @param position1 first position
     * @param position2 second position
     * @return transposition
     */
    public static int[] createTransposition(int dimension, int position1, int position2) {
        if (dimension < 0)
            throw new IllegalArgumentException("Dimension is negative.");
        if (position1 < 0 || position2 < 0)
            throw new IllegalArgumentException("Negative index.");
        if (position1 >= dimension || position2 >= dimension)
            throw new IndexOutOfBoundsException();

        int[] transposition = new int[dimension];
        int i = 1;
        for (; i < dimension; ++i)
            transposition[i] = i;
        i = transposition[position1];
        transposition[position1] = transposition[position2];
        transposition[position2] = i;
        return transposition;
    }

    /**
     * Creates cycle permutation written in one-line notation,
     * i.e. an array of form [{@code dimension-1},0,1, ...,{@code dimension-2}].
     *
     * @param dimension dimension of the resulting permutation, e.g. the array length
     * @return cycle permutation in one-line notation
     */
    public static int[] createCycle(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException("Negative dimension");

        int[] cycle = new int[dimension];
        for (int i = 0; i < dimension - 1; ++i)
            cycle[i + 1] = i;
        cycle[0] = dimension - 1;
        return cycle;
    }

    /**
     * Returns the inverse permutation to the specified one both written in one-line notation.
     *
     * @param permutation permutation in one-line notation
     * @return inverse permutation to the specified one
     */
    public static int[] inverse(int[] permutation) {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            inverse[permutation[i]] = i;
        return inverse;
    }

    /**
     * Shuffles the specified array according to the specified permutation and returns the result.
     * The inputting array will be cloned.
     *
     * @param array       array
     * @param permutation permutation in one-line notation
     * @param <T>         any type
     * @return new shuffled array
     * @throws IllegalArgumentException if array length not equals to permutation length
     * @throws IllegalArgumentException if permutation is not consistent with one-line notation
     */
    public static <T> T[] shuffle(T[] array, final int[] permutation) {
        if (array.length != permutation.length)
            throw new IllegalArgumentException();
        if (!testPermutationCorrectness(permutation))
            throw new IllegalArgumentException();
        Class<?> type = array.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
                T[] newArray = (T[]) Array.newInstance(type, array.length);
        for (int i = 0; i < permutation.length; ++i)
            newArray[i] = array[permutation[i]];
        return newArray;
    }

    /**
     * Shuffles the specified array according to the specified permutation and returns the result.
     * The inputting array will be cloned.
     *
     * @param array       array
     * @param permutation permutation in one-line notation
     * @return new shuffled array
     * @throws IllegalArgumentException if array length not equals to permutation length
     * @throws IllegalArgumentException if permutation is not consistent with one-line notation
     */
    public static int[] shuffle(int[] array, final int[] permutation) {
        if (array.length != permutation.length)
            throw new IllegalArgumentException();
        if (!testPermutationCorrectness(permutation))
            throw new IllegalArgumentException();
        int[] newArray = new int[array.length];
        for (int i = 0; i < permutation.length; ++i)
            newArray[i] = array[permutation[i]];
        return newArray;
    }

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line
     *         notation for permutations and {@code false} if not
     */
    public static boolean testPermutationCorrectness(int[] permutation) {
        int[] _permutation = new int[permutation.length];
        System.arraycopy(permutation, 0, _permutation, 0, permutation.length);
        Arrays.sort(_permutation);
        for (int i = 0; i < _permutation.length; ++i)
            if (_permutation[i] != i)
                return false;
        return true;
    }

    /**
     * Check that fromIndex and toIndex are in range, and throw an appropriate
     * exception if they aren't.
     */
    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
        if (fromIndex < 0)
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLen)
            throw new ArrayIndexOutOfBoundsException(toIndex);
    }

    /**
     * Check that all positions are less than dimension, and throw an
     * appropriate exception if they aren't.
     */
    private static void rangeCheck1(int dimension, int... positions) {
        if (dimension < 0)
            throw new IllegalArgumentException("Dimension is negative.");
        for (int i : positions) {
            if (i < 0)
                throw new IllegalArgumentException("Negative index " + i + ".");
            if (i >= dimension)
                throw new IndexOutOfBoundsException();
        }
    }
}
