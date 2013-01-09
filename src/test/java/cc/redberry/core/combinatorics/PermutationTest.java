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
package cc.redberry.core.combinatorics;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationTest {
    public PermutationTest() {
    }

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
        assertTrue(a.composition(a.inverse()).equals(a.getOne()));
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
}
