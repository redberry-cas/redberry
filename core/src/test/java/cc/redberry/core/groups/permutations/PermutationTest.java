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

import cc.redberry.core.utils.ArraysUtils;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationTest {
    @Test
    public void testComposition() {
        Permutation a = new Permutation(new int[]{2, 1, 0});
        Permutation b = new Permutation(new int[]{1, 0, 2});

        Permutation ba = new Permutation(new int[]{1, 2, 0});
        assertTrue(b.composition(a).equals(ba));

        Permutation ab = new Permutation(new int[]{2, 0, 1});
        assertTrue(a.composition(b).equals(ab));
    }

    @Test
    public void testInverse() {
        Permutation a = new Permutation(new int[]{2, 1, 0, 3});
        assertTrue(a.composition(a.inverse()).equals(a.getIdentity()));
    }

    @Test
    public void testCompareTo() {
        Permutation a = new Permutation(new int[]{0, 2, 1});
        Permutation b = new Permutation(new int[]{2, 0, 1});
        Permutation c = new Permutation(new int[]{1, 0, 2});
        Permutation d = new Permutation(new int[]{0, 1, 2});
        Permutation[] arr1 = {a, b, c, d};
        Permutation[] arr2 = {d, a, c, b};
        Arrays.sort(arr1);
        assertArrayEquals(arr1, arr2);
    }

    @Test
    public void testPermute() {
        int[] arr = new int[]{5, 6};
        int[] expected = new int[]{6, 5};
        Permutation p = new Permutation(new int[]{1, 0});
        assertArrayEquals(expected, p.permute(arr));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException1() {
        new Permutation(new int[]{0, 3, 1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException2() {
        new Permutation(new int[]{0, -2, 1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException3() {
        new Permutation(new int[]{0, 1, 1});
    }

    @Test
    public void testOrder1() {
        Permutation p = new Permutation(1, 0);
        assertEquals(2, p.order().intValue());
    }

    @Test
    public void testOrder2() {
        Permutation p = new Permutation(2, 0, 1);
        assertEquals(3, p.order().intValue());
    }

    @Test
    public void testOrder3() {
        Permutation p = new Permutation(2, 0, 1);
        assertEquals(3, p.order().intValue());
        assertEquals(3, bruteForceOrder(p));
        assertTrue(p.pow(3).isIdentity());
    }

    @Test
    public void testOrder4() {
        for (int i = 0; i < 1000; ++i) {
            Permutation p = new Permutation(randomPermutation(30));
            assertEquals(bruteForceOrder(p), p.order().intValue());
            assertTrue(p.pow(p.order().intValue()).isIdentity());
        }
    }

    @Test(timeout = 15000)
    public void testOrder5() {
        BigInteger veryBigNumber = BigInteger.valueOf(Integer.MAX_VALUE).pow(3);
        int n = 1000000;
        Permutation p = new Permutation(randomPermutation(n));
        while (p.order().compareTo(veryBigNumber) < 0) {
            p = new Permutation(randomPermutation(n));
        }
    }

    @Test
    public void testOrderParity1() {
        Permutation p = new Permutation(15, 10, 1, 22, 14, 9, 19, 7, 2, 16, 25, 28, 4, 23, 18, 29, 26, 12, 21, 3, 6, 13, 8, 17, 20, 11, 0, 5, 24, 27);
        assertFalse(Combinatorics.orderIsOdd(p.permutation));
    }

    @Test
    public void testOrderParity2() {
        for (int i = 0; i < 10000; ++i) {
            Permutation p = new Permutation(randomPermutation(100));
            assertEquals(p.order().testBit(0), Combinatorics.orderIsOdd(p.permutation));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInconsistentSign1() {
        new Permutation(true, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInconsistentSign2() {
        new Permutation(true, 3, 4, 0, 1, 2);
    }

    private static int bruteForceOrder(Permutation p) {
        Permutation t = p;
        for (int i = 1; i < Integer.MAX_VALUE; ++i) {
            if (t.isIdentity())
                return i;
            t = t.composition(p);
        }
        return -1;
    }

    public static int[] randomPermutation(final int n) {
        Random rnd = new Random();
        int[] p = new int[n];
        for (int i = 0; i < n; ++i)
            p[i] = i;
        for (int i = n; i > 1; --i)
            ArraysUtils.swap(p, i - 1, rnd.nextInt(i));
        for (int i = n; i > 1; --i)
            ArraysUtils.swap(p, i - 1, rnd.nextInt(i));
        for (int i = n; i > 1; --i)
            ArraysUtils.swap(p, i - 1, rnd.nextInt(i));
        for (int i = n; i > 1; --i)
            ArraysUtils.swap(p, i - 1, rnd.nextInt(i));
        return p;
    }

}
