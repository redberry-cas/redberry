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

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;

import static cc.redberry.core.utils.ArraysUtils.short2int;

/**
 * The implementation of {@link Permutation} based on the one-line notation. The instances of this class are immutable.
 * This class represents permutations of the degree not larger than {@link Short#MAX_VALUE}.
 * <p>
 * The implementation is based on {@code short[]} array in one-line notation and provides O(1) complexity for
 * {@code imageOf(int)} and O(degree) complexity for composition.
 * </p>
 * <p>
 * When multiplying instances of this by permutations of degree larger than {@code Short.MAX_VALUE}, the new instance
 * with enlarged array capacity will be constructed and returned (i.e. {@link cc.redberry.core.groups.permutations.PermutationOneLineInt}).
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.groups.permutations.Permutation
 * @see cc.redberry.core.groups.permutations.Permutations#createPermutation(boolean, int[])
 * @see cc.redberry.core.groups.permutations.Permutations#createPermutation(boolean, int[][])
 * @since 1.0
 */
public final class PermutationOneLineShort implements Permutation {
    final short[] permutation;
    final short internalDegree;
    final boolean isIdentity;
    final boolean antisymmetry;

    /**
     * Creates permutation with antisymmetry property from given array in one-line notation and boolean value of
     * antisymmetry ({@code true} means antisymmetry)
     *
     * @param antisymmetry antisymmetry (true - antisymmetry, false - symmetry)
     * @param permutation  permutation in one-line notation
     * @throws IllegalArgumentException if permutation is inconsistent with one-line notation
     * @throws IllegalArgumentException if antisymmetry is true and permutation order is odd
     */
    public PermutationOneLineShort(boolean antisymmetry, short... permutation) {
        if (!Permutations.testPermutationCorrectness(permutation, antisymmetry))
            throw new IllegalArgumentException("Inconsistent permutation.");
        this.permutation = permutation.clone();
        this.antisymmetry = antisymmetry;
        this.isIdentity = Permutations.isIdentity(permutation);
        this.internalDegree = Permutations.internalDegree(permutation);
    }

    //!no check for one-line notation => unsafe constructor
    PermutationOneLineShort(boolean isIdentity, boolean antisymmetry, short internalDegree, short[] permutation) {
        this.isIdentity = isIdentity;
        this.permutation = permutation;
        this.antisymmetry = antisymmetry;
        this.internalDegree = internalDegree;
        if (antisymmetry && Permutations.orderOfPermutationIsOdd(permutation))
            throw new InconsistentGeneratorsException();
    }

    //!!no any checks, used only to create inverse or identity permutation
    PermutationOneLineShort(boolean isIdentity, boolean antisymmetry, short internalDegree, short[] permutation, boolean identity) {
        assert identity;
        this.permutation = permutation;
        this.antisymmetry = antisymmetry;
        this.isIdentity = isIdentity;
        this.internalDegree = internalDegree;
    }

    /**
     * Converts this to int-based representation {@link cc.redberry.core.groups.permutations.PermutationOneLineInt}
     *
     * @return int-based representation of permutation
     */
    public PermutationOneLineInt toIntRepresentation() {
        return new PermutationOneLineInt(isIdentity, antisymmetry, internalDegree, short2int(permutation), true);
    }

    @Override
    public int length() {
        return permutation.length;
    }

    @Override
    public boolean antisymmetry() {
        return antisymmetry;
    }

    @Override
    public int[] oneLine() {
        return short2int(permutation);
    }

    @Override
    public IntArray oneLineImmutable() {
        return new IntArray(short2int(permutation));
    }

    @Override
    public int[][] cycles() {
        return Permutations.convertOneLineToCycles(permutation);
    }

    @Override
    public int newIndexOf(int i) {
        return i < internalDegree ? permutation[i] : i;
    }

    @Override
    public int imageOf(int i) {
        return i < internalDegree ? permutation[i] : i;
    }

    @Override
    public int[] imageOf(int[] set) {
        if (isIdentity)
            return set.clone();
        final int[] result = new int[set.length];
        for (int i = 0; i < set.length; ++i)
            result[i] = newIndexOf(set[i]);
        return result;
    }

    @Override
    public int[] permute(int[] array) {
        if (isIdentity)
            return array.clone();
        final int[] result = new int[array.length];
        for (int i = 0; i < array.length; ++i)
            result[i] = array[newIndexOf(i)];
        return result;
    }

    @Override
    public char[] permute(char[] array) {
        if (isIdentity)
            return array.clone();
        final char[] result = new char[array.length];
        for (int i = 0; i < array.length; ++i)
            result[i] = array[newIndexOf(i)];
        return result;
    }

    @Override
    public <T> T[] permute(T[] array) {
        if (isIdentity)
            return array.clone();
        @SuppressWarnings("unchecked")
        final T[] result = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
        for (int i = 0; i < array.length; ++i)
            result[i] = array[newIndexOf(i)];
        return result;
    }

    @Override
    public int newIndexOfUnderInverse(int i) {
        if (i >= permutation.length)
            return i;
        for (int j = permutation.length - 1; j >= 0; --j)
            if (permutation[j] == i)
                return j;
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Permutation conjugate(Permutation p) {
        return inverse().composition(p, this);
    }

    @Override
    public Permutation commutator(Permutation p) {
        return inverse().composition(p.inverse(), this, p);
    }

    @Override
    public Permutation composition(final Permutation other) {
        if (this.isIdentity)
            return other;
        if (other.isIdentity())
            return this;

        final int newLength = Math.max(internalDegree(), other.internalDegree());
        if (newLength > Short.MAX_VALUE)
            return toIntRepresentation().composition(other);

        short newInternalDegree = -1;
        final short[] result = new short[newLength];
        boolean resultIsIdentity = true;
        for (short i = 0; i < newLength; ++i) {
            result[i] = (short) other.newIndexOf(newIndexOf(i));
            resultIsIdentity &= result[i] == i;
            newInternalDegree = result[i] == i ? newInternalDegree : i;
        }

        try {
            return new PermutationOneLineShort(resultIsIdentity, antisymmetry ^ other.antisymmetry(),
                    (short) (newInternalDegree + 1), result);
        } catch (InconsistentGeneratorsException ex) {
            throw new InconsistentGeneratorsException(this + " and " + other);
        }
    }

    @Override
    public Permutation composition(Permutation a, Permutation b) {
        if (this.isIdentity)
            return a.composition(b);
        if (a.isIdentity())
            return composition(b);
        if (b.isIdentity())
            return composition(a);

        final int newLength = Math.max(Math.max(internalDegree(), a.internalDegree()), b.internalDegree());
        if (newLength > Short.MAX_VALUE)
            return toIntRepresentation().composition(a, b);

        short newInternalDegree = -1;
        final short[] result = new short[newLength];
        boolean resultIsIdentity = true;
        for (short i = 0; i < newLength; ++i) {
            result[i] = (short) b.newIndexOf(a.newIndexOf(newIndexOf(i)));
            resultIsIdentity &= result[i] == i;
            newInternalDegree = result[i] == i ? newInternalDegree : i;
        }

        try {
            return new PermutationOneLineShort(resultIsIdentity,
                    antisymmetry ^ a.antisymmetry() ^ b.antisymmetry(), (short) (newInternalDegree + 1), result);
        } catch (InconsistentGeneratorsException ex) {
            throw new InconsistentGeneratorsException(this + " and " + a + " and " + b);
        }
    }

    @Override
    public Permutation composition(Permutation a, Permutation b, Permutation c) {
        if (this.isIdentity)
            return a.composition(b, c);
        if (a.isIdentity())
            return composition(b, c);
        if (b.isIdentity())
            return composition(a, c);
        if (c.isIdentity())
            return composition(b, c);

        final int newLength = Math.max(c.internalDegree(), Math.max(
                Math.max(internalDegree(), a.internalDegree()), b.internalDegree()));
        if (newLength > Short.MAX_VALUE)
            return toIntRepresentation().composition(a, b, c);

        final short[] result = new short[newLength];
        short newInternalDegree = -1;
        boolean resultIsIdentity = true;
        for (short i = 0; i < newLength; ++i) {
            result[i] = (short) c.newIndexOf(b.newIndexOf(a.newIndexOf(newIndexOf(i))));
            resultIsIdentity &= result[i] == i;
            newInternalDegree = result[i] == i ? newInternalDegree : i;
        }

        try {
            return new PermutationOneLineShort(resultIsIdentity,
                    antisymmetry ^ a.antisymmetry() ^ b.antisymmetry() ^ c.antisymmetry(), (short) (newInternalDegree + 1), result);
        } catch (InconsistentGeneratorsException ex) {
            throw new InconsistentGeneratorsException(this + " and " + a + " and " + b + " and " + c);
        }
    }

    @Override
    public Permutation compositionWithInverse(final Permutation other) {
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
        final short[] inv = new short[permutation.length];
        for (short i = (short) (permutation.length - 1); i >= 0; --i)
            inv[permutation[i]] = i;

        return new PermutationOneLineShort(false, antisymmetry, internalDegree, inv, true);
    }


    @Override
    public boolean isIdentity() {
        return isIdentity;
    }

    @Override
    public Permutation getIdentity() {
        if (isIdentity)
            return this;
        return Permutations.createIdentityPermutation(permutation.length);
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
    public int internalDegree() {
        return internalDegree;
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
        final int max = Math.max(internalDegree(), that.internalDegree());
        for (int i = 0; i < max; ++i)
            if (newIndexOf(i) != that.newIndexOf(i))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < internalDegree; ++i)
            result = 31 * result + permutation[i];
        result = 31 * result + (antisymmetry ? 1 : 0);
        return result;
    }

    @Override
    public int parity() {
        return Permutations.parity(permutation);
    }

    @Override
    public Permutation moveRight(final int size) {
        if (size == 0)
            return this;
        if (size + permutation.length > Short.MAX_VALUE)
            return toIntRepresentation().moveRight(size);

        final short[] p = new short[size + permutation.length];
        short i = 1;
        for (; i < size; ++i)
            p[i] = i;
        int k = i;
        for (; i < p.length; ++i)
            p[i] = (short) (permutation[i - k] + size);
        return new PermutationOneLineShort(isIdentity, antisymmetry, (short) (size + internalDegree), p, true);
    }

    @Override
    public int[] lengthsOfCycles() {
        return Permutations.lengthsOfCycles(permutation);
    }

    @Override
    public String toString() {
        return toStringCycles();
    }

    @Override
    public String toStringOneLine() {
        return (antisymmetry ? "-" : "+") + Arrays.toString(permutation);
    }

    @Override
    public String toStringCycles() {
        //String cycles = Arrays.deepToString(cycles()).replace("[", "{").replace("]", "}");
        String cycles = Arrays.deepToString(cycles());
        return (antisymmetry ? "-" : "+") + cycles;
    }


    @Override
    public int compareTo(Permutation t) {
        final int max = Math.max(internalDegree(), t.internalDegree());
        if (antisymmetry != t.antisymmetry())
            return antisymmetry ? -1 : 1;
        for (int i = 0; i < max; ++i)
            if (newIndexOf(i) < t.newIndexOf(i))
                return -1;
            else if (newIndexOf(i) > t.newIndexOf(i))
                return 1;
        return 0;
    }
}
