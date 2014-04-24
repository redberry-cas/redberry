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

import cc.redberry.core.context.CC;
import cc.redberry.core.utils.ArraysUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationOneLineIntTest {
    @Test
    public void testComposition() {
        Permutation a = Permutations.createPermutation(new int[]{2, 1, 0});
        Permutation b = Permutations.createPermutation(new int[]{1, 0, 2});

        Permutation ba = Permutations.createPermutation(new int[]{1, 2, 0});
        assertTrue(b.composition(a).equals(ba));

        Permutation ab = Permutations.createPermutation(new int[]{2, 0, 1});
        assertTrue(a.composition(b).equals(ab));
    }

    @Test
    public void testInverse() {
        Permutation a = Permutations.createPermutation(new int[]{2, 1, 0, 3});
        assertTrue(a.composition(a.inverse()).equals(a.getIdentity()));
    }

    @Test
    public void testCompareTo() {
        Permutation a = Permutations.createPermutation(new int[]{0, 2, 1});
        Permutation b = Permutations.createPermutation(new int[]{2, 0, 1});
        Permutation c = Permutations.createPermutation(new int[]{1, 0, 2});
        Permutation d = Permutations.createPermutation(new int[]{0, 1, 2});
        Permutation[] arr1 = {a, b, c, d};
        Permutation[] arr2 = {d, a, c, b};
        Arrays.sort(arr1);
        assertArrayEquals(arr1, arr2);
    }

    @Test
    public void testPermute() {
        int[] arr = new int[]{5, 6};
        int[] expected = new int[]{6, 5};
        Permutation p = Permutations.createPermutation(new int[]{1, 0});
        assertArrayEquals(expected, p.permute(arr));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException1() {
        Permutations.createPermutation(new int[]{0, 3, 1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException2() {
        Permutations.createPermutation(new int[]{0, -2, 1});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorException3() {
        Permutations.createPermutation(new int[]{0, 1, 1});
    }

    @Test
    public void testOrder1() {
        Permutation p = Permutations.createPermutation(1, 0);
        assertEquals(2, p.order().intValue());
    }

    @Test
    public void testOrder2() {
        Permutation p = Permutations.createPermutation(2, 0, 1);
        assertEquals(3, p.order().intValue());
    }

    @Test
    public void testOrder3() {
        Permutation p = Permutations.createPermutation(2, 0, 1);
        assertEquals(3, p.order().intValue());
        assertEquals(3, bruteForceOrder(p));
        assertTrue(p.pow(3).isIdentity());
    }

    @Test
    public void testOrder4() {
        for (int i = 0; i < 1000; ++i) {
            Permutation p = Permutations.createPermutation(randomPermutation(30));
            assertEquals(bruteForceOrder(p), p.order().intValue());
            assertTrue(p.pow(p.order().intValue()).isIdentity());
        }
    }

    @Test(timeout = 15000)
    public void testOrder5() {
        BigInteger veryBigNumber = BigInteger.valueOf(Integer.MAX_VALUE).pow(3);
        int n = 1000000;
        Permutation p = Permutations.createPermutation(randomPermutation(n));
        while (p.order().compareTo(veryBigNumber) < 0) {
            p = Permutations.createPermutation(randomPermutation(n));
        }
    }

    @Test
    public void testOrderParity1() {
        Permutation p = Permutations.createPermutation(15, 10, 1, 22, 14, 9, 19, 7, 2, 16, 25, 28, 4, 23, 18, 29, 26, 12, 21, 3, 6, 13, 8, 17, 20, 11, 0, 5, 24, 27);
        assertFalse(Permutations.orderOfPermutationIsOdd(p.oneLine()));
    }

    @Test
    public void testOrderParity2() {
        for (int i = 0; i < 10000; ++i) {
            Permutation p = Permutations.createPermutation(randomPermutation(100));
            assertEquals(p.order().testBit(0), Permutations.orderOfPermutationIsOdd(p.oneLine()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInconsistentSign1() {
        Permutations.createPermutation(true, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInconsistentSign2() {
        Permutations.createPermutation(true, 3, 4, 0, 1, 2);
    }

    @Test
    public void testNewIndexOfUnderInverse1() {
        int n = 100;
        for (int i = 0; i < 1000; ++i) {
            Permutation p = Permutations.createPermutation(Permutations.randomPermutation(n, CC.getRandomGenerator()));
            Permutation inv = p.inverse();
            for (int j = 0; j < n; ++j)
                assertEquals(inv.newIndexOf(j), p.newIndexOfUnderInverse(j));
        }
    }

    @Test
    public void testComposition1() {
        RandomGenerator rnd = CC.getRandomGenerator();
        for (int C = 0; C < 100; ++C) {
            int[] p1 = Permutations.randomPermutation(rnd.nextInt(100), rnd);
            int[] p2 = Permutations.randomPermutation(rnd.nextInt(100), rnd);
            int maxL = Math.max(p1.length, p2.length);
            int[] ap1 = new int[maxL], ap2 = new int[maxL];
            System.arraycopy(p1, 0, ap1, 0, p1.length);
            System.arraycopy(p2, 0, ap2, 0, p2.length);
            for (int i = p1.length; i < maxL; ++i)
                ap1[i] = i;
            for (int i = p2.length; i < maxL; ++i)
                ap2[i] = i;

            Permutation pp1 = Permutations.createPermutation(p1), pp2 = Permutations.createPermutation(p2),
                    pp3 = pp1.composition(pp2);

            int[] actual = pp3.oneLine();
            int[] expected = new int[maxL];
            for (int i = 0; i < maxL; ++i)
                expected[i] = ap2[ap1[i]];
            Assert.assertArrayEquals(Arrays.copyOfRange(expected, 0, actual.length), actual);
            Assert.assertEquals(pp3.internalDegree(), Permutations.internalDegree(expected));
        }
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
