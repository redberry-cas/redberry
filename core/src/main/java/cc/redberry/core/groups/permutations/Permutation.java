/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.utils.IntArray;

import java.math.BigInteger;

/**
 * Interface describing a single permutation. See {@link cc.redberry.core.groups.permutations.Permutations#createPermutation(boolean, int[])}
 * and {@link cc.redberry.core.groups.permutations.Permutations#createPermutation(boolean, int[][])} on how to construct permutations.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.groups.permutations.Permutations#createPermutation(boolean, int[])
 * @see cc.redberry.core.groups.permutations.Permutations#createPermutation(boolean, int[][])
 * @see cc.redberry.core.groups.permutations.PermutationOneLineInt
 * @see cc.redberry.core.groups.permutations.PermutationOneLineShort
 * @see cc.redberry.core.groups.permutations.PermutationOneLineByte
 * @since 1.0
 */
public interface Permutation extends Comparable<Permutation> {
    /**
     * Returns array that represents this permutation in one-line notation.
     *
     * @return array that represents this permutation in one-line notation
     */
    int[] oneLine();

    /**
     * Returns immutable array that represents this permutation in one-line notation.
     *
     * @return immutable array that represents this permutation in one-line notation
     */
    IntArray oneLineImmutable();

    /**
     * Returns an array of disjoint cycles that represent this permutation.
     *
     * @return array of disjoint cycles that represent this permutation
     */
    int[][] cycles();

    /**
     * Returns image of specified point under the action of this permutation.
     *
     * @param i point
     * @return image of specified point under the action of this permutation
     */
    public int newIndexOf(int i);

//    /**
//     * Returns image of specified point under the action of this permutation.This method is same as
//     * {@link #newIndexOf(int)}, but throws exception if specified point greater then the internal low-level array that
//     * represents this permutation.
//     *
//     * @param i point
//     * @return image of specified point under the action of this permutation
//     * @throws java.lang.IndexOutOfBoundsException if {@code i > length()}
//     */
//    public int newIndexOfInternal(int i);

    /**
     * Returns image of specified point under the action of this permutation. This method is absolutely same as
     * {@link #newIndexOf(int)} without any difference.
     *
     * @param i point
     * @return image of specified point under the action of this permutation
     */
    public int imageOf(int i);

    /**
     * Returns image of specified set of points under the action of this permutation.
     *
     * @param set set
     * @return image of specified set under this permutation
     */
    public int[] imageOf(int[] set);

    /**
     * Permutes array and returns the result.
     *
     * @param array array
     * @return permuted array
     */
    public int[] permute(int[] array);

    /**
     * Permutes array and returns the result.
     *
     * @param array array
     * @return permuted array
     */
    public char[] permute(char[] array);

    /**
     * Permutes array and returns the result.
     *
     * @param array array
     * @return permuted array
     */
    public <T> T[] permute(T[] array);

    /**
     * Returns conjugation of specified element by this permutation, i.e. this^-1 * p * this
     *
     * @param p permutation
     * @return conjugation of specified element by this permutation, i.e. this^-1 * p * this
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         parity of permutation)
     */
    public Permutation conjugate(Permutation p);

    /**
     * Returns commutator of this and specified permutation, i.e. this^-1 * p^-1 * this * p.
     *
     * @param p permutation
     * @return commutator of this and specified permutation, i.e. this^-1 * p^-1 * this * p
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         parity of permutation)
     */
    public Permutation commutator(Permutation p);

    /**
     * Returns image of specified point under the action of inverse of this permutation.
     *
     * @param i point
     * @return image of specified point under the action of inverse of this permutation
     */
    public int newIndexOfUnderInverse(int i);

    /**
     * Returns true if this permutation is antisymmetry and false otherwise.
     *
     * @return true if this permutation is antisymmetry and false otherwise
     */
    public boolean antisymmetry();

    /**
     * If this is antisymmetry, then converts this permutation to symmetry.
     *
     * @return same permutation with {@code false} antisymmetry
     */
    public Permutation toSymmetry();

    /**
     * Returns the result of  {@code this * other}. Applying the resulting permutation is equivalent to applying
     * {@code other} after {@code this}.
     *
     * @param other other permutation
     * @return the result of  {@code this * other}
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         parity of permutation)
     */
    public Permutation composition(Permutation other);

    /**
     * Returns the result of  {@code this * a * b}. Applying the resulting permutation is equivalent to applying
     * {@code b} after {@code a} after {@code this}.
     *
     * @param a other permutation
     * @param b other permutation
     * @return the result of  {@code this * a * b}
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         permutation parity)
     */
    public Permutation composition(Permutation a, Permutation b);

    /**
     * Returns the result of  {@code this * a * b * c}. Applying the resulting permutation is equivalent to applying
     * {@code c} after {@code b} after {@code a} after {@code this}.
     *
     * @param a other permutation
     * @param b other permutation
     * @param c other permutation
     * @return the result of  {@code this * a * b * c}
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         permutation parity)
     */
    public Permutation composition(Permutation a, Permutation b, Permutation c);

    /**
     * Returns the result of  {@code this * other.inverse()}. Applying the resulting permutation is equivalent
     * to applying {@code other.inverse()} after {@code this}.
     *
     * @param other other permutation
     * @return the result of  {@code this * other.inverse()}
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         permutation parity)
     */
    public Permutation compositionWithInverse(Permutation other);

    /**
     * Returns the inverse permutation of this.
     *
     * @return the inverse permutation of this
     */
    public Permutation inverse();

    /**
     * Returns {@code true} if this represents identity permutation
     *
     * @return {@code true} if this is identity permutation
     */
    public boolean isIdentity();

    /**
     * Returns the identity permutation with the degree of this permutation
     *
     * @return identity permutation with the degree of this permutation
     */
    public Permutation getIdentity();

    /**
     * Calculates and returns the order of this permutation.
     *
     * @return order of this permutation
     * @see Permutations#orderOfPermutation(int[])
     */
    public BigInteger order();

    /**
     * Returns true if order of this permutation is odd and false otherwise.
     *
     * @return true if order of this permutation is odd and false otherwise
     */
    public boolean orderIsOdd();

    /**
     * Returns a largest moved point plus one.
     *
     * @return largest moved point plus one
     */
    public int internalDegree();

    /**
     * Returns length of the underlying array (at low-level).
     *
     * @return length of the underlying array (at low-level)
     */
    public int length();

    /**
     * Returns this raised to the specified exponent.
     *
     * @param exponent exponent
     * @return this raised to the specified exponent
     */
    public Permutation pow(int exponent);

    /**
     * Returns parity of this permutations.
     *
     * @return parity of this permutations
     */
    public int parity();

    /**
     * Inserts identity action on the set [0, 1, ..., size - 1]; as result the degree of resulting permutation will be
     * size + degree of this.
     *
     * @param size size of the set
     */
    public Permutation moveRight(int size);

    /**
     * Returns lengths of cycles in disjoint cycle notation.
     *
     * @return lengths of cycles in disjoint cycle notation
     */
    int[] lengthsOfCycles();

    /**
     * Returns a string representation of this permutation in one-line notation.
     *
     * @return a string representation of this permutation in one-line notation
     */
    String toStringOneLine();

    /**
     * Returns a string representation of this permutation in disjoint cycles notation.
     *
     * @return a string representation of this permutation in disjoint cycles notation
     */
    String toStringCycles();
}
