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

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Interface representing permutation.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see PermutationOneLine
 */
public interface Permutation extends Comparable<Permutation> {
    /**
     * Returns array representing this permutation in one-line notation.
     *
     * @return
     */
    int[] oneLine();

    /**
     * Return the new position of specified element under this permutation
     *
     * @param i element
     * @return new position of specified element under this permutation
     */
    public int newIndexOf(int i);

    /**
     * Returns specified element conjugated by this, i.e. this^-1 * p * this
     *
     * @param p permutation
     * @return specified element conjugated by this, i.e. this^-1 * p * this
     */
    public Permutation conjugate(Permutation p);

    /**
     * Returns a commutator of this and specified permutation, i.e. this^-1 * p^-1 * this * p.
     *
     * @param p permutation
     * @return a commutator of this and specified permutation, i.e. this^-1 * p^-1 * this * p
     */
    public Permutation commutator(Permutation p);

    /**
     * Returns the image of specified set under this permutation.
     *
     * @param set set
     * @return image of specified set under this permutation
     */
    public int[] imageOf(int[] set);

    /**
     * Return the new position of specified element under inverse this permutation
     *
     * @param i position of element in set
     * @return new position of specified element under inverse this permutation
     */
    public int newIndexOfUnderInverse(int i);

    /**
     * Returns true if this permutation represents antisymmetry and false otherwise.
     *
     * @return true if this permutation represents antisymmetry and false otherwise
     */
    public boolean antisymmetry();

    /**
     * Returns the result of  {@code this * other}. Applying the resulting permutation is equivalent to applying
     * {@code other} after {@code this}.
     *
     * @param other other permutation
     * @return the result of  {@code this * other}
     * @throws IllegalArgumentException        if {@code other.degree() != this.degree()}
     * @throws InconsistentGeneratorsException if the result of composition is inconsistent symmetry (antisymmetry with odd
     *                                         permutation parity)
     */
    public Permutation composition(Permutation other);

    /**
     * Returns the result of  {@code this * a * b}. Applying the resulting permutation is equivalent to applying
     * {@code b} after {@code a} after {@code this}.
     *
     * @param a other permutation
     * @param b other permutation
     * @return the result of  {@code this * a * b}
     * @throws IllegalArgumentException        if {@code a.degree() != this.degree() || b.degree() != this.degree()}
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
     * @throws IllegalArgumentException        if {@code a.degree() != this.degree() || b.degree() != this.degree() || c.degree() != this.degree()}
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
     * @throws IllegalArgumentException        if {@code other.degree() != this.degree()}
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
     * Returns the image of specified array under this permutation
     *
     * @param array specified array
     * @return image of specified array under this permutation
     * @throws IllegalArgumentException if this.degree() != array.length
     */
    public int[] permute(int[] array);


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
     * Returns the degree of this permutation, or, in other words, the number of elements in set on which this
     * permutation acts (the length of permutation written in one-line notation).
     *
     * @return degree of this permutation
     */
    public int degree();

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
     * Extends this permutation to specified degree, by inserting identity action in the initial segment of this
     * permutation.
     *
     * @param newDegree new degree
     * @return equivalent permutation with specified degree
     */
    public Permutation extendBefore(int newDegree);

    /**
     * Extends this permutation to specified degree, by inserting identity action at the end of this permutation (if
     * written in one-line notation).
     *
     * @param newDegree new degree
     * @return equivalent permutation with specified degree
     */
    public Permutation extendAfter(int newDegree);

    /**
     * Returns lengths of cycles in disjoint cycle notation.
     *
     * @return lengths of cycles in disjoint cycle notation
     */
    int[] lengthsOfCycles();


}
