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

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Combinatorics {

    private Combinatorics() {
    }

    public static IntCombinatoricGenerator createIntGenerator(int n, int k) {
        if (n < k)
            throw new IllegalArgumentException();
        if (n == k)
            return new IntPermutationsGenerator(n);
        else
            return new IntCombinationPermutationGenerator(n, k);
    }

    public static boolean isIdentity(final int[] permutation) {
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] != i)
                return false;
        return true;

    }

    public static boolean isIdentity(Permutation permutation) {
        return isIdentity(permutation.permutation);
    }

    public static boolean isIdentity(Symmetry symmetry) {
        return !symmetry.isAntiSymmetry() && isIdentity(symmetry.permutation);
    }

    public static int[] createIdentity(final int dimension) {
        int[] perm = new int[dimension];
        for (int i = 0; i < dimension; ++i)
            perm[i] = i;
        return perm;
    }

    public static int[] createTransposition(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException("Dimension is negative.");
        if (dimension > 1)
            return createTransposition(dimension, 0, 1);
        return new int[dimension];
    }

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

    public static int[] createCycle(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException("Negative dimension");

        int[] cycle = new int[dimension];
        for (int i = 0; i < dimension - 1; ++i)
            cycle[i + 1] = i;
        cycle[0] = dimension - 1;
        return cycle;
    }

    public static int[] inverse(int[] permutation) {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            inverse[permutation[i]] = i;
        return inverse;
    }

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
