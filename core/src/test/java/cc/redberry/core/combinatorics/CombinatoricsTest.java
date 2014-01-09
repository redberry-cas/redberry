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
package cc.redberry.core.combinatorics;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.combinatorics.Combinatorics.convertPermutation;
import static cc.redberry.core.combinatorics.Combinatorics.createBlockTransposition;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class CombinatoricsTest {

    @Test
    public void testConvert1() {
        int[] p = {3, 2, 1, 0};
        int[] map = {1, 2, 4, 3};

        int[] expected = {0, 3, 4, 1, 2, 5, 6, 7, 8, 9};
        Assert.assertArrayEquals(convertPermutation(p, map, 10), expected);

        map = new int[]{-1, 0, 1, 3, 2, -1, -1, -1, -1, -1};
        Assert.assertArrayEquals(convertPermutation(expected, map, 4), p);
    }

    @Test
    public void testCycle() {
        int[] c = Combinatorics.createBlockCycle(3, 4);
        Assert.assertArrayEquals(c, new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1, 2});
        int[] perm = {3, 4, 5, 0, 1, 2, 6, 7, 8, 9, 10, 11};

        PermutationsSpanIterator sp = new PermutationsSpanIterator(Arrays.asList(
                new Permutation(c), new Permutation(perm)
        ));

        int i = 0;
        while (sp.hasNext()) {
            ++i;
            sp.next();
        }
        Assert.assertEquals(24, i);
    }

    @Test
    public void testBlockTransposition() {
        Assert.assertArrayEquals(createBlockTransposition(2, 3), new int[]{2, 3, 4, 0, 1});
        Assert.assertArrayEquals(createBlockTransposition(0, 3), new int[]{0, 1, 2});
        Assert.assertArrayEquals(createBlockTransposition(3, 0), new int[]{0, 1, 2});
        Assert.assertArrayEquals(createBlockTransposition(3, 3), new int[]{3, 4, 5, 0, 1, 2});
    }
}
