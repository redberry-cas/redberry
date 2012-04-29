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
package cc.redberry.core.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cc.redberry.core.combinatorics.Permutation;
import cc.redberry.core.combinatorics.PermutationsProvider;
import cc.redberry.core.combinatorics.PermutationsProviderImpl;
import cc.redberry.core.combinatorics.SimplePermutationProvider;

public final class MathUtils {
    private MathUtils() {
    }

    /**
     * Calculates greatest common divisor of two long
     *
     * @param p
     * @param q
     * @return greatest common divisor of p and q
     */
    public static long gcd(final long p, final long q) {
        long u = p;
        long v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Long.MIN_VALUE) || (v == Long.MIN_VALUE))
                throw new RuntimeException("overflow: gcd({0}, {1}) is 2^63");
            return Math.abs(u) + Math.abs(v);
        }
        if (u > 0)
            u = -u;
        if (v > 0)
            v = -v;
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 63) {
            u /= 2;
            v /= 2;
            k++;
        }
        if (k == 63)
            throw new RuntimeException("overflow: gcd({0}, {1}) is 2^63");
        long t = ((u & 1) == 1) ? v : -(u / 2);
        do {
            while ((t & 1) == 0)
                t /= 2;
            if (t > 0)
                u = -t;
            else
                v = t;
            t = (v - u) / 2;
        } while (t != 0);
        return -u * (1L << k);
    }

    /**
     * Calculates greatest common divisor of two integers
     *
     * @param p
     * @param q
     * @return greatest common divisor of p and q
     */
    public static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Integer.MIN_VALUE) || (v == Integer.MIN_VALUE))
                throw new RuntimeException("overflow: gcd({0}, {1}) is 2^31");
            return Math.abs(u) + Math.abs(v);
        }

        if (u > 0)
            u = -u;
        if (v > 0)
            v = -v;
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) {
//                        u >>= 1;
//                        v >>= 1;
            u /= 2;
            v /= 2;
            k++;
        }
        if (k == 31)
            throw new RuntimeException("overflow: gcd({0}, {1}) is 2^31");
        int t = ((u & 1) == 1) ? v : -(u / 2);
        do {
            while ((t & 1) == 0)
                t /= 2;
            if (t > 0)
                u = -t;
            else
                v = t;
            t = (v - u) / 2;
        } while (t != 0);
        return -u * (1 << k);
    }

    /**
     * Sort array & return array with removed repetitive values.
     *
     * @param values input array (this method will quickSort this array)
     * @return sorted array of distinct values
     */
    public static int[] getSortedDistinct(int[] values) {
        if (values.length == 0)
            return values;
        Arrays.sort(values);
        int shift = 0;
        int i = 0;
        while (i + shift + 1 < values.length)
            if (values[i + shift] == values[i + shift + 1])
                ++shift;
            else {
                values[i] = values[i + shift];
                ++i;
            }
        values[i] = values[i + shift];
        return Arrays.copyOf(values, i + 1);
    }

    /**
     * Return the set difference B - A for int sets A and B.<br/> Sets A and B
     * must be represented as two sorted int arrays.<br/> Repetitive values in A
     * or B not allowed.
     *
     * @param a sorted array of distinct values. (set A)
     * @param b sorted array of distinct values. (set B)
     * @return the set of elements in B but not in A
     */
    public static int[] intSetDifference(int[] a, int[] b) {
        int bPointer = 0, aPointer = 0;
        int counter = 0;
        while (aPointer < a.length && bPointer < b.length)
            if (a[aPointer] == b[bPointer]) {
                aPointer++;
                bPointer++;
            } else if (a[aPointer] < b[bPointer])
                aPointer++;
            else if (a[aPointer] > b[bPointer]) {
                counter++;
                bPointer++;
            }
        counter += b.length - bPointer;
        int[] result = new int[counter];
        counter = 0;
        aPointer = 0;
        bPointer = 0;
        while (aPointer < a.length && bPointer < b.length)
            if (a[aPointer] == b[bPointer]) {
                aPointer++;
                bPointer++;
            } else if (a[aPointer] < b[bPointer])
                aPointer++;
            else if (a[aPointer] > b[bPointer])
                result[counter++] = b[bPointer++];
        System.arraycopy(b, bPointer, result, counter, b.length - bPointer);
        return result;
    }

    /**
     * Return the union B + A for integer sets A and B.<br/> Sets A and B must
     * be represented as two sorted integer arrays.<br/> Repetitive values in A
     * or B not allowed.
     *
     * @param a sorted array of distinct values. (set A)
     * @param b sorted array of distinct values. (set B)
     * @return the set of elements from B and from A
     */
    public static int[] intSetUnion(int[] a, int[] b) {
        int bPointer = 0, aPointer = 0;
        int counter = 0;
        while (aPointer < a.length && bPointer < b.length)
            if (a[aPointer] == b[bPointer]) {
                aPointer++;
                bPointer++;
                counter++;
            } else if (a[aPointer] < b[bPointer]) {
                aPointer++;
                counter++;
            } else if (a[aPointer] > b[bPointer]) {
                counter++;
                bPointer++;
            }
        counter += (a.length - aPointer) + (b.length - bPointer); //Assert aPoiner==a.length || bPointer==b.length
        int[] result = new int[counter];
        counter = 0;
        aPointer = 0;
        bPointer = 0;
        while (aPointer < a.length && bPointer < b.length)
            if (a[aPointer] == b[bPointer]) {
                result[counter++] = b[bPointer];
                aPointer++;
                bPointer++;
            } else if (a[aPointer] < b[bPointer])
                result[counter++] = a[aPointer++];
            else if (a[aPointer] > b[bPointer])
                result[counter++] = b[bPointer++];
        if (aPointer == a.length)
            System.arraycopy(b, bPointer, result, counter, b.length - bPointer);
        else
            System.arraycopy(a, aPointer, result, counter, a.length - aPointer);
        return result;
    }

    public static PermutationsProvider generateProvider(final Object[] _array) {
        int begin = 0;
        int i;
        List<PermutationsProvider> disjointProviders = new ArrayList<>();
        for (i = 1; i < _array.length; ++i)
            if (i == _array.length || _array[i].hashCode() != _array[i - 1].hashCode()) {
                if (i - 1 != begin)
                    disjointProviders.add(new SimplePermutationProvider(begin, i));
                begin = i;
            }
        return new PermutationsProviderImpl(disjointProviders);
    }

    public static boolean compareArrays(final Object[] array1, final Object[] array2) {
        int size;
        if ((size = array1.length) != array2.length)
            return false;
        PermutationsProvider provider = generateProvider(array1);
        int[] nonPermutablePositions = PermutationsProvider.Util.getNonpermutablePositions(size, provider);
        for (int i : nonPermutablePositions)
            if (!array1[i].equals(array2[i]))
                return false;
        int[] targetPositions = provider.targetPositions();
        out_for:
        for (Permutation permutation : provider.allPermutations()) {
            for (int i = 0; i < targetPositions.length; ++i)
                if (!array1[targetPositions[i]].equals(array2[targetPositions[permutation.newIndexOf(i)]]))
                    continue out_for;
            return true;
        }
        return false;
    }
}
