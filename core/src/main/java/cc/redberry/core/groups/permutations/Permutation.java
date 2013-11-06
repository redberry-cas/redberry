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
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Permutation implements Comparable<Permutation> {
    final int[] permutation;
    final boolean sign;

    /**
     * Creates permutation with sign from given array in one-line notation and boolean value of
     * sign ({@code true} means minus)
     *
     * @param sign        sign of permutation ({@code true} means minus)
     * @param permutation permutation in one-line notation
     * @throws IllegalArgumentException if permutation is inconsistent
     */
    public Permutation(boolean sign, int... permutation) {
        if (!Combinatorics.testPermutationCorrectness(permutation, sign))
            throw new IllegalArgumentException("Inconsistent permutation.");
        this.permutation = permutation.clone();
        this.sign = sign;
    }

    /**
     * Creates permutation from given array in one-line notation
     *
     * @param permutation permutation in one-line notation
     * @throws IllegalArgumentException if permutation array is inconsistent
     */
    public Permutation(int... permutation) {
        this(false, permutation);
    }

    //no check for one-line notation => unsafe constructor
    Permutation(boolean unsafe, boolean sign, int... permutation) {
        this.permutation = permutation;
        this.sign = sign;
        if (sign && Combinatorics.orderIsOdd(permutation))
            throw new InconsistentGeneratorsException();
    }

    /**
     * Return the new position of specified element under this permutation (i-th number in one-line notation)
     *
     * @param i position of element in set
     * @return i-th number in one-line notation
     */
    public int newIndexOf(int i) {
        return permutation[i];
    }

    /**
     * Returns the sign of this permutation.
     *
     * @return sign of this permutation
     */
    public boolean isSign() {
        return sign;
    }

    /**
     * Returns the result of  {@code this * other}. Applying the resulting permutation is equivalent to applying
     * {@code other} after {@code this}.
     *
     * @param other other permutation
     * @return the result of  {@code this * other}
     * @throws IllegalArgumentException if {@code other.length != this.length}
     */
    public Permutation composition(final Permutation other) {
        try {
            if (permutation.length != other.permutation.length)
                throw new IllegalArgumentException();
            final int[] result = new int[permutation.length];
            for (int i = permutation.length - 1; i >= 0; --i)
                result[i] = other.permutation[permutation[i]];
            return new Permutation(true, sign ^ other.sign, result);
        } catch (InconsistentGeneratorsException ex) {
            throw new InconsistentGeneratorsException(this + " and " + other);
        }
    }


    /**
     * Returns the result of  {@code this * other.inverse()}. Applying the resulting permutation is equivalent
     * to applying {@code other.inverse()} after {@code this}.
     *
     * @param other other permutation
     * @return the result of  {@code this * other.inverse()}
     * @throws IllegalArgumentException if {@code other.length != this.length}
     */
    public Permutation compositionWithInverse(final Permutation other) {
        //todo improve?
        return composition(other.inverse());
    }

    /**
     * Returns the inverse permutation of this.
     *
     * @return the inverse permutation of this
     */
    public Permutation inverse() {
        final int[] inv = new int[permutation.length];
        for (int i = permutation.length - 1; i >= 0; --i)
            inv[permutation[i]] = i;
        return new Permutation(true, sign, inv);
    }


    /**
     * Returns {@code true} if this represents identity permutation
     *
     * @return {@code true} if this is identity permutation
     */
    public boolean isIdentity() {
        for (int i = permutation.length - 1; i >= 0; --i)
            if (permutation[i] != i) return false;
        return true;
    }

    /**
     * Returns the array permuted by this permutation
     *
     * @param array specified array
     * @return the array permuted by this permutation
     * @throws IllegalArgumentException if this.length != array.length
     */
    public int[] permute(int[] array) {
        if (array.length != permutation.length)
            throw new IllegalArgumentException();
        final int[] perm = new int[array.length];
        for (int i = permutation.length - 1; i >= 0; --i)
            perm[i] = array[permutation[i]];
        return perm;
    }

    public Permutation getIdentity() {
        return Combinatorics.getIdentity(permutation.length);
    }


    /**
     * Calculates the order of this permutation. Since the maximum order g(n) of permutation in symmetric group
     * S(n) is about log(g(n)) <= sqrt(n log(n))* (1 + log log(n) / (2 log(n))) [1], then g(n) can be very big (e.g.
     * for n = 1000, g(n) ~1e25). The algorithm decomposes permutation into product of cycles and returns l.c.m. of their sizes.
     *
     * @return order of this permutation
     * @see Combinatorics#order(int[])
     */
    public BigInteger order() {
        return Combinatorics.order(permutation);
    }

    /**
     * Returns true if order of this permutation is odd and false otherwise.
     *
     * @return true if order of this permutation is odd and false otherwise
     */
    public boolean orderIsOdd() {
        return Combinatorics.orderIsOdd(permutation);
    }

    public int length() {
        return permutation.length;
    }

    /**
     * Returns this raised to the specified exponent.
     *
     * @param exponent exponent
     * @return this raised to the specified exponent
     */
    public Permutation pow(int exponent) {
        Permutation base = this, result = getIdentity();
        while (exponent != 0) {
            if (exponent % 2 == 1)
                result = result.composition(base);
            base = base.composition(base);
            exponent = exponent >> 1;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permutation that = (Permutation) o;
        return (sign == that.sign) && Arrays.equals(permutation, that.permutation);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(permutation);
        result = 31 * result + (sign ? 1 : 0);
        return result;
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
        return (sign ? "-" : "+") + Arrays.toString(permutation);
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
     * @return 1 if this one is "greater" -1 if t is "greater", 0 if this and t
     *         equals.
     * @throws IllegalArgumentException if dimensions of this and t are not '
     *                                  equals
     */
    @Override
    public int compareTo(Permutation t) {
        if (t.permutation.length != permutation.length)
            throw new IllegalArgumentException("different dimensions of comparing combinatorics");
        if (sign != t.sign)
            return sign ? -1 : 1;
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] < t.permutation[i])
                return -1;
            else if (permutation[i] > t.permutation[i])
                return 1;
        return 0;
    }
}
