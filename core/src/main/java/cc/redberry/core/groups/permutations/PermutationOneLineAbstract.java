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

import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.IntArrayList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class PermutationOneLineAbstract implements Permutation {
    private final boolean isIdentity;
    private final boolean antisymmetry;

    protected PermutationOneLineAbstract(boolean isIdentity, boolean antisymmetry) {
        this.isIdentity = isIdentity;
        this.antisymmetry = antisymmetry;
    }

    @Override
    public boolean antisymmetry() {
        return antisymmetry;
    }

    @Override
    public boolean isIdentity() {
        return isIdentity;
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
    public Permutation getIdentity() {
        if (isIdentity)
            return this;
        return Permutations.createIdentityPermutation(length());
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
    public Permutation conjugate(Permutation p) {
        return inverse().composition(p, this);
    }

    @Override
    public Permutation commutator(Permutation p) {
        return inverse().composition(p.inverse(), this, p);
    }

    @Override
    public int[] imageOf(int[] set) {
        if (isIdentity())
            return set.clone();
        final int[] result = new int[set.length];
        for (int i = 0; i < set.length; ++i)
            result[i] = newIndexOf(set[i]);
        return result;
    }

    @Override
    public int[] permute(int[] array) {
        if (isIdentity())
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Permutation)) return false;

        Permutation that = (Permutation) o;
        if (antisymmetry != that.antisymmetry())
            return false;
        if (degree() != that.degree())
            return false;
        for (int i = 0; i < degree(); ++i)
            if (newIndexOf(i) != that.newIndexOf(i))
                return false;
        return true;
    }

    @Override
    public int[][] cycles() {
        ArrayList<int[]> cycles = new ArrayList<>();
        BitArray seen = new BitArray(degree());
        int counter = 0;
        while (counter < degree()) {
            int start = seen.nextZeroBit(0);
            if (newIndexOf(start) == start) {
                ++counter;
                seen.set(start);
                continue;
            }
            IntArrayList cycle = new IntArrayList();
            while (!seen.get(start)) {
                seen.set(start);
                ++counter;
                cycle.add(start);
                start = newIndexOf(start);
            }
            cycles.add(cycle.toArray());
        }
        return cycles.toArray(new int[cycles.size()][]);
    }


    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < degree(); ++i)
            result = 31 * result + newIndexOf(i);
        result = 31 * result + (antisymmetry ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return toStringCycles();
    }

    @Override
    public String toStringOneLine() {
        return (antisymmetry ? "-" : "+") + toStringArray();
    }

    @Override
    public String toStringCycles() {
        String cycles = Arrays.deepToString(cycles());
        return (antisymmetry ? "-" : "+") + cycles;
    }

    private String toStringArray() {
        int iMax = degree() - 1;
        if (iMax == -1)
            return "[]";

        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            b.append(newIndexOf(i));
            if (i == iMax)
                return b.append(']').toString();
            b.append(", ");
        }
    }

    @Override
    public int compareTo(Permutation t) {
        int c = Integer.compare(degree(), t.degree());
        if (c != 0)
            return c;
        if (antisymmetry != t.antisymmetry())
            return antisymmetry ? -1 : 1;
        for (int i = 0; i < degree(); ++i)
            if (newIndexOf(i) < t.newIndexOf(i))
                return -1;
            else if (newIndexOf(i) > t.newIndexOf(i))
                return 1;
        return 0;
    }
}
