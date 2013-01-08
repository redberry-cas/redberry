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
package cc.redberry.core.combinatorics;

import cc.redberry.core.TAssert;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DistinctCombinationsPortTest {
    @Test
    public void test1() {
        int[] a1 = {1, 1};
        int[] a2 = {1, 1};
        int[] a3 = {1};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        Assert.assertEquals(dcp.take(), null);
    }

    @Test
    public void test2() {
        int[] a1 = {1, 2};
        int[] a2 = {2, 4};
        int[] a3 = {3, 4, 5};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        int[] c;
        Set<int[]> expected = new HashSet<>();
        expected.add(new int[]{1, 2, 3});
        expected.add(new int[]{1, 2, 4});
        expected.add(new int[]{1, 2, 5});
        expected.add(new int[]{1, 4, 3});
        expected.add(new int[]{1, 4, 5});
        expected.add(new int[]{2, 4, 3});
        expected.add(new int[]{2, 4, 5});
        Set<int[]> actual = new HashSet<>();
        while ((c = dcp.take()) != null)
            actual.add(c.clone());
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test3() {
        int[] a1 = {1, 2, 3};
        int[] a2 = {1, 2, 3};
        int[] a3 = {1, 2, 3};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        int[] c;
        Set<int[]> actual = new HashSet<>();
        while ((c = dcp.take()) != null)
            actual.add(c.clone());

        int[] arr = {1, 2, 3};
        Set<int[]> expected = new HashSet<>();
        for (int[] a : Combinatorics.createIntGenerator(3, 3))
            expected.add(Combinatorics.shuffle(arr, a));

        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test4() {
        int[] a1 = {1};
        int[] a2 = {3};
        int[] a3 = {1, 2};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        Assert.assertTrue(Arrays.equals(new int[]{1, 3, 2}, dcp.take()));
        Assert.assertTrue(dcp.take() == null);
    }
}
