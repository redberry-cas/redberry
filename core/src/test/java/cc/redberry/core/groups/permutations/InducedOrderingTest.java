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
import cc.redberry.core.utils.IntComparator;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class InducedOrderingTest {
    @Test
    public void testBaseComparator1() {
        int[] base = {0, 2, 1};
        InducedOrdering comparator = new InducedOrdering(base);
        int[] array = {1, 0, 2};
        ArraysUtils.quickSort(array, comparator);
        assertArrayEquals(base, array);
    }

    @Test
    public void testBaseComparator2() {
        int[] base = {0, 2, 1};
        InducedOrdering comparator = new InducedOrdering(base);
        int[] array = {5, 1, 0, 2};
        ArraysUtils.quickSort(array, comparator);
        int[] expected = {0, 2, 1, 5};
        assertArrayEquals(expected, array);
    }


    @Test
    public void testBaseComparator3() {
        int[] base = {6, 7, 1};
        InducedOrdering comparator = new InducedOrdering(base);
        int[] array = {5, 1, 0, 2, 7, 8, 9, 10, 6};
        ArraysUtils.quickSort(array, comparator);
        assertSetIsSorted(comparator, array);
    }

    public static void assertSetIsSorted(IntComparator comparator, int[] set) {
        if (set.length < 2)
            return;
        for (int i = 1; i < set.length; ++i)
            assertTrue(comparator.compare(set[i - 1], set[i]) <= 0);
    }
}
