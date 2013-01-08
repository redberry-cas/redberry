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

import cc.redberry.core.utils.IntArray;
import junit.framework.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IntPermutationsGeneratorTest {

    @Test
    public void test1() {
        IntPermutationsGenerator ig = new IntPermutationsGenerator(8);
        int num = 0;
        Set<IntArray> set = new HashSet<>(40320);
        IntArray a;
        while (ig.hasNext()) {
            ++num;
            a = new IntArray(ig.next().clone());
            Assert.assertTrue(!set.contains(a));
            set.add(a);
        }
        Assert.assertTrue(num == 40320);
    }

    @Test
    public void test2() {
        IntPermutationsGenerator ig = new IntPermutationsGenerator(0);
        Assert.assertTrue(ig.hasNext());
        Assert.assertTrue(ig.next().length == 0);
        Assert.assertTrue(!ig.hasNext());
    }
}
