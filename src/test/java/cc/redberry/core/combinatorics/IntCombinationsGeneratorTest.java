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
public class IntCombinationsGeneratorTest {
    @Test
    public void test0() {
        Set<int[]> expected = new HashSet<>();
        expected.add(new int[]{0, 1, 2});
        expected.add(new int[]{0, 1, 3});
        expected.add(new int[]{0, 1, 4});
        expected.add(new int[]{0, 2, 3});
        expected.add(new int[]{0, 2, 4});
        expected.add(new int[]{0, 3, 4});
        expected.add(new int[]{1, 2, 3});
        expected.add(new int[]{1, 2, 4});
        expected.add(new int[]{1, 3, 4});
        expected.add(new int[]{2, 3, 4});

        Set<int[]> actual = new HashSet<>();
        for (int[] combination : new IntCombinationsGenerator(5, 3))
            actual.add(combination.clone());

        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test1() {
        IntCombinationsGenerator gen = new IntCombinationsGenerator(1, 1);
        Assert.assertTrue(gen.hasNext());
        Assert.assertTrue(Arrays.equals(new int[]{0}, gen.next()));
        Assert.assertTrue(!gen.hasNext());
    }
}
