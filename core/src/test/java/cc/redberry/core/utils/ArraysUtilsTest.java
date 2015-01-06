/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.core.utils;

import cc.redberry.core.context.CC;
import cc.redberry.core.groups.permutations.InducedOrdering;
import cc.redberry.core.groups.permutations.Permutations;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ArraysUtilsTest {

    public ArraysUtilsTest() {
    }

    @Test
    public void testShort1() {
        short[] target = {2, 1, 0};
        assertArrayEquals(new int[]{2, 1, 0}, ArraysUtils.quickSortP(target));
    }

    @Test
    public void testShort2() {
        short[] target = {2};
        assertArrayEquals(new int[]{0}, ArraysUtils.quickSortP(target));
    }

    @Test
    public void testShort3() {
        short[] target = new short[0];
        assertArrayEquals(new int[0], ArraysUtils.quickSortP(target));
    }

    @Test
    public void testSortPermutation1() {
        for (int i = 0; i < 100; ++i) {
            int[] a = Permutations.randomPermutation(10);
            int[] sorted = a.clone();
            int[] permutation = ArraysUtils.quickSortP(sorted);
            assertArrayEquals(Permutations.permute(a, permutation), sorted);
        }
    }

    @Test
    public void testBijection1() {
        Integer[] from = {1, 3, 1};
        Integer[] to = {1, 3, 1};
        int[] bijection = {0, 1, 2};
        Assert.assertArrayEquals(bijection, ArraysUtils.bijection(from, to));
    }

    @Test
    public void testBijection2() {
        Integer[] from = {1, 3, 1};
        Integer[] to = {3, 1, 1};
        int[] bijection = {1, 0, 2};
        Assert.assertArrayEquals(bijection, ArraysUtils.bijection(from, to));
    }

    @Test
    public void testQuickSortComparator1() {
        IntComparator comparator = new IntComparator() {
            @Override
            public int compare(int a, int b) {
                return Integer.compare(b, a);
            }
        };
        int[] array = new int[1000];
        for (int t = 0; t < 100; ++t) {
            for (int i = 0; i < array.length; ++i)
                array[i] = CC.getRandomGenerator().nextInt(10000);

            ArraysUtils.quickSort(array, comparator);
            for (int i = 1; i < array.length; ++i)
                assertTrue(array[i - 1] >= array[i]);
        }
    }

    @Test
    public void testQuickSortWithCosortAndIntComparator1() {
        final int degree = 1000;
        final int[] base = new int[50];
        for (int i = 0; i < base.length; ++i)
            base[i] = CC.getRandomGenerator().nextInt(base.length);

        IntComparator comparator = new InducedOrdering(base);

        final int[] array = new int[1000];
        final int[] cosort = new int[1000];

        for (int i = 1; i < array.length; ++i)
            cosort[i] = i;

        for (int t = 0; t < 100; ++t) {
            for (int i = 0; i < array.length; ++i)
                array[i] = CC.getRandomGenerator().nextInt(degree);

            final int[] _array_ = array.clone(),
                    _cosort_ = cosort.clone();

            ArraysUtils.quickSort(_array_, _cosort_, comparator);

            for (int i = 0; i < array.length; ++i)
                assertEquals(array[_cosort_[i]], _array_[i]);
        }
    }
}
