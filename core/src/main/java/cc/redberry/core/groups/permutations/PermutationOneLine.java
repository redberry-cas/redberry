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
 * Implementation of {@link Permutation} based on one-line notation.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Permutation
 * @see PermutationCycles
 */
public class PermutationOneLine implements Permutation {
    final int[] permutation;
    final boolean isIdentity;
    final boolean antisymmetry;

    /**
     * Converts given permutation to one-line notation
     *
     * @param permutation permutation
     */
    public PermutationOneLine(Permutation permutation) {
        if (permutation instanceof PermutationOneLine) {
            PermutationOneLine p = ((PermutationOneLine) permutation);
            this.permutation = p.permutation;
            this.isIdentity = p.isIdentity;
            this.antisymmetry = p.antisymmetry;
        } else {
            this.permutation = new int[permutation.degree()];
            for (int i = this.permutation.length - 1; i >= 0; --i)
                this.permutation[i] = permutation.newIndexOf(i);
            this.antisymmetry = permutation.antisymmetry();
            this.isIdentity = permutation.isIdentity();
        }
    }

    /**
     * Creates permutation with antisymmetry from given array in one-line notation and boolean value of
     * antisymmetry ({@code true} means antisymmetry)
     *
     * @param antisymmetry antisymmetry (true - antisymmetry, false - symmetry)
     * @param permutation  permutation in one-line notation
     * @throws IllegalArgumentException if permutation is inconsistent
     */
    public PermutationOneLine(boolean antisymmetry, int... permutation) {
        if (!Permutations.testPermutationCorrectness(permutation, antisymmetry))
            throw new IllegalArgumentException("Inconsistent permutation.");
        this.permutation = permutation.clone();
        this.antisymmetry = antisymmetry;
        this.isIdentity = Permutations.isIdentity(permutation);
    }

    /**
     * Creates permutation from given array in one-line notation
     *
     * @param permutation permutation in one-line notation
     * @throws IllegalArgumentException if permutation array is inconsistent
     */
    public PermutationOneLine(int... permutation) {
        this(false, permutation);
    }

    //!no check for one-line notation => unsafe constructor
    private PermutationOneLine(boolean isIdentity, boolean antisymmetry, int[] permutation) {
        this.isIdentity = isIdentity;
        this.permutation = permutation;
        this.antisymmetry = antisymmetry;
        if (antisymmetry && Permutations.orderOfPermutationIsOdd(permutation))
            throw new InconsistentGeneratorsException();
    }

    //!!no any checks, used only to create inverse or identity permutation
    private PermutationOneLine(boolean isIdentity, boolean antisymmetry, int[] permutation, boolean identity) {
        assert identity;
        this.permutation = permutation;
        this.antisymmetry = antisymmetry;
        this.isIdentity = isIdentity;
    }

    @Override
    public int newIndexOf(int i) {
        return permutation[i];
    }

    @Override
    public int[] imageOf(int[] set) {
        if (isIdentity)
            return set.clone();
        int[] result = new int[set.length];
        for (int i = 0; i < set.length; ++i)
            result[i] = permutation[set[i]];
        return result;
    }

    @Override
    public int newIndexOfUnderInverse(int i) {
        for (int j = permutation.length - 1; j >= 0; --j)
            if (permutation[j] == i)
                return j;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public boolean antisymmetry() {
        return antisymmetry;
    }

    @Override
    public Permutation composition(final Permutation other) {
        if (permutation.length != other.degree())
            throw new IllegalArgumentException();

        if (this.isIdentity)
            return other;
        if (other.isIdentity())
            return this;

        try {
            final int[] result = new int[permutation.length];
            boolean resultIsIdentity = true;
            for (int i = permutation.length - 1; i >= 0; --i) {
                result[i] = other.newIndexOf(permutation[i]);
                resultIsIdentity &= result[i] == i;
            }

            return new PermutationOneLine(resultIsIdentity, antisymmetry ^ other.antisymmetry(), result);
        } catch (InconsistentGeneratorsException ex) {
            throw new InconsistentGeneratorsException(this + " and " + other);
        }
    }

    @Override
    public Permutation compositionWithInverse(final Permutation other) {
        if (permutation.length != other.degree())
            throw new IllegalArgumentException();

        if (this.isIdentity)
            return other.inverse();
        if (other.isIdentity())
            return this;

        return composition(other.inverse());
    }

    @Override
    public Permutation inverse() {
        if (isIdentity)
            return this;
        final int[] inv = new int[permutation.length];
        for (int i = permutation.length - 1; i >= 0; --i)
            inv[permutation[i]] = i;

        return new PermutationOneLine(false, antisymmetry, inv, true);
    }


    @Override
    public boolean isIdentity() {
        return isIdentity;
    }

    @Override
    public int[] permute(int[] array) {
        if (isIdentity)
            return array.clone();
        if (array.length != permutation.length)
            throw new IllegalArgumentException();
        final int[] perm = new int[array.length];
        for (int i = permutation.length - 1; i >= 0; --i)
            perm[i] = array[permutation[i]];
        return perm;
    }

    @Override
    public Permutation getIdentity() {
        if (isIdentity)
            return this;
        return new PermutationOneLine(true, false, Permutations.getIdentityPermutationArray(permutation.length), true);
    }

    @Override
    public BigInteger order() {
        return Permutations.orderOfPermutation(permutation);
    }

    @Override
    public boolean orderIsOdd() {
        return !isIdentity && Permutations.orderOfPermutationIsOdd(permutation);
    }

    @Override
    public int degree() {
        return permutation.length;
    }

    @Override
    public Permutation pow(int exponent) {
        if (isIdentity)
            return this;
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
        if (o == null || !(o instanceof Permutation)) return false;

        Permutation that = (Permutation) o;
        if (antisymmetry != that.antisymmetry())
            return false;
        if (degree() != that.degree())
            return false;
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] != that.newIndexOf(i))
                return false;

        return true;
    }

    @Override
    public int parity() {
        return Permutations.parity(permutation);
    }

    @Override
    public PermutationOneLine extendAfter(final int newDegree) {
        if (newDegree < permutation.length)
            throw new IllegalArgumentException("New degree is smaller then this degree.");
        if (newDegree == permutation.length)
            return this;
        int[] p = new int[newDegree];
        System.arraycopy(permutation, 0, p, 0, permutation.length);
        for (int i = permutation.length; i < newDegree; ++i)
            p[i] = i;
        return new PermutationOneLine(p);
//        return new PermutationOneLine(isIdentity, antisymmetry, p, true);
    }

    @Override
    public PermutationOneLine extendBefore(final int newDegree) {
        if (newDegree < permutation.length)
            throw new IllegalArgumentException("New degree is smaller then this degree.");
        if (newDegree == permutation.length)
            return this;
        int[] p = new int[newDegree];
        int i = 1;
        for (; i < newDegree - permutation.length; ++i)
            p[i] = i;
        int k = i;
        for (; i < newDegree; ++i)
            p[i] = permutation[i - k] + k;
        return new PermutationOneLine(p);
//        return new PermutationOneLine(isIdentity, antisymmetry, p, true);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(permutation);
        result = 31 * result + (antisymmetry ? 1 : 0);
        return result;
    }

    /**
     * Returns the string representation of this permutation in one-line notation.
     *
     * @return the string representation of this permutation in one-line  notation
     */
    @Override
    public String toString() {
        return (antisymmetry ? "-" : "+") + Arrays.toString(permutation);
    }


    @Override
    public int compareTo(Permutation t) {
        if (t.degree() != permutation.length)
            throw new IllegalArgumentException("Not same degrees.");
        if (antisymmetry != t.antisymmetry())
            return antisymmetry ? -1 : 1;
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] < t.newIndexOf(i))
                return -1;
            else if (permutation[i] > t.newIndexOf(i))
                return 1;
        return 0;
    }
}
