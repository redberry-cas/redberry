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

/**
 * The implementation of {@link Permutation} based on the one-line notation. The instances of this class are immutable.
 * <p><b>Construction</b>
 * To construct a single permutation one can use either disjoint cycles notation either one-line notation (in both cases
 * the numeration of points starts from 0):
 * <br>
 * <pre style="background:#f1f1f1;color:#000"><span style="color:#406040"> //same permutation</span>
 * <span style="color:#406040">//in one-line notation</span>
 * <span style="color:#a08000">Permutation</span> a <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">1</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">2</span>);
 * <span style="color:#406040">//in disjoint cycles notation</span>
 * <span style="color:#a08000">Permutation</span> b <span style="color:#2060a0">=</span> <span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(<span style="color:#0080a0">5</span>, <span style="color:#2060a0">new</span> <span style="color:#a08000">int</span>[][]{{<span style="color:#0080a0">2</span>, <span style="color:#0080a0">4</span>}, {<span style="color:#0080a0">3</span>, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">1</span>}});
 * <span style="color:#2060a0">assert</span> a<span style="color:#2060a0">.</span>equals(b);
 * </pre>
 * In the case of antisymmetry, an {@code IllegalArgumentException} can be thrown, since if the {@link #order()} of
 * permutation is odd, then it cannot represent a valid antisymmetry. For example, both
 * <span style="background:#f1f1f1;color:#000"><span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(true, <span style="color:#0080a0">0</span>, <span style="color:#0080a0">1</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">4</span>, <span style="color:#0080a0">2</span>)</span>
 * or
 * <span style="background:#f1f1f1;color:#000"><span style="color:#2060a0">new</span> <span style="color:#a08000">PermutationOneLine</span>(true, <span style="color:#0080a0">5</span>, <span style="color:#2060a0">new</span> <span style="color:#a08000">int</span>[][]{{<span style="color:#0080a0">2</span>, <span style="color:#0080a0">3</span>, <span style="color:#0080a0">4</span>}})</span>
 * will throw exception.
 * </p>
 * <p><b>Implementation</b>
 * The implementation is based on the one-line notation; this class holds an array that
 * represents permutation in one-line notation thereby providing O(1) complexity for {@code imageOf(int)} and O(degree)
 * complexity for composition.
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.groups.permutations.Permutation
 * @since 1.0
 */
public final class PermutationOneLine implements Permutation {
    final int[] permutation;
    final boolean isIdentity;
    final boolean antisymmetry;

    /**
     * Creates permutation with antisymmetry property from given array of disjoint cycles and boolean value of
     * antisymmetry ({@code true} means antisymmetry)
     *
     * @param antisymmetry antisymmetry (true - antisymmetry, false - symmetry)
     * @param cycles       disjoint cycles
     * @throws IllegalArgumentException if permutation is inconsistent with disjoint cycles notation
     * @throws IllegalArgumentException if antisymmetry is true and permutation order is odd
     */
    public PermutationOneLine(boolean antisymmetry, int[][] cycles) {
        this(antisymmetry, Permutations.convertCyclesToOneLine(cycles));
    }

    /**
     * Creates permutation from a given array of disjoint cycles.
     *
     * @param cycles disjoint cycles
     * @throws IllegalArgumentException if permutation is inconsistent with disjoint cycles notation
     */
    public PermutationOneLine(int[][] cycles) {
        this(Permutations.convertCyclesToOneLine(cycles));
    }

    /**
     * Creates permutation with antisymmetry property from given array in one-line notation and boolean value of
     * antisymmetry ({@code true} means antisymmetry)
     *
     * @param antisymmetry antisymmetry (true - antisymmetry, false - symmetry)
     * @param permutation  permutation in one-line notation
     * @throws IllegalArgumentException if permutation is inconsistent with one-line notation
     * @throws IllegalArgumentException if antisymmetry is true and permutation order is odd
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
     * @throws IllegalArgumentException if permutation array is inconsistent with one-line notation
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
    public int length() {
        return permutation.length;
    }

    @Override
    public boolean antisymmetry() {
        return antisymmetry;
    }

    @Override
    public int[] oneLine() {
        return permutation.clone();
    }

    @Override
    public IntArray oneLineImmutable() {
        return new IntArray(permutation);
    }

    @Override
    public int[][] cycles() {
        return Permutations.convertOneLineToCycles(permutation);
    }

    @Override
    public int newIndexOf(int i) {
        return i < permutation.length ? permutation[i] : i;
    }

    @Override
    public int imageOf(int i) {
        return i < permutation.length ? permutation[i] : i;
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

        final int new_degree = Math.max(length(), other.length());
        final int[] result = new int[new_degree];
        boolean resultIsIdentity = true;
        for (int i = new_degree - 1; i >= 0; --i) {
            result[i] = other.newIndexOf(newIndexOf(i));
            resultIsIdentity &= result[i] == i;
        }

        try {
            return new PermutationOneLine(resultIsIdentity, antisymmetry ^ other.antisymmetry(), result);
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

        final int new_degree = Math.max(Math.max(length(), a.length()), b.length());
        final int[] result = new int[new_degree];
        boolean resultIsIdentity = true;
        for (int i = new_degree - 1; i >= 0; --i) {
            result[i] = b.newIndexOf(a.newIndexOf(newIndexOf(i)));
            resultIsIdentity &= result[i] == i;
        }

        try {
            return new PermutationOneLine(resultIsIdentity,
                    antisymmetry ^ a.antisymmetry() ^ b.antisymmetry(), result);
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

        final int new_degree = Math.max(c.length(), Math.max(
                Math.max(length(), a.length()), b.length()));
        final int[] result = new int[new_degree];
        boolean resultIsIdentity = true;
        for (int i = new_degree - 1; i >= 0; --i) {
            result[i] = c.newIndexOf(b.newIndexOf(a.newIndexOf(newIndexOf(i))));
            resultIsIdentity &= result[i] == i;
        }

        try {
            return new PermutationOneLine(resultIsIdentity,
                    antisymmetry ^ a.antisymmetry() ^ b.antisymmetry() ^ c.antisymmetry(), result);
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

//    @Override
//    public int degree() {
//        return permutation.length;
//    }


    @Override
    public int internalDegree() {
        return Permutations.internalDegree(permutation);
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
        final int max = Math.max(length(), that.length());
        for (int i = 0; i < max; ++i)
            if (newIndexOf(i) != that.newIndexOf(i))
                return false;
        return true;
    }

    @Override
    public int parity() {
        return Permutations.parity(permutation);
    }

    @Override
    public PermutationOneLine moveRight(final int size) {
        if (size == 0)
            return this;
        final int[] p = new int[size + permutation.length];
        int i = 1;
        for (; i < size; ++i)
            p[i] = i;
        int k = i;
        for (; i < p.length; ++i)
            p[i] = permutation[i - k] + size;
        return new PermutationOneLine(isIdentity, antisymmetry, p, true);
    }

    @Override
    public int[] lengthsOfCycles() {
        return Permutations.lengthsOfCycles(permutation);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(permutation);
        result = 31 * result + (antisymmetry ? 1 : 0);
        return result;
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
        final int max = Math.max(length(), t.length());
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
