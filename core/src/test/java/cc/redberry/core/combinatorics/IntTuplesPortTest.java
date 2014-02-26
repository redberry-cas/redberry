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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IntTuplesPortTest {

    @Test
    public void test1() {
        IntTuplesPort port = new IntTuplesPort(3, 3, 3);
        int count = 0;
        while (port.take() != null)
            ++count;
        Assert.assertEquals(count, 27);
    }

    @Test
    public void test2() {
        IntTuplesPort port = new IntTuplesPort(4, 4, 4, 4);
        int count = 0;
        while (port.take() != null)
            ++count;
        Assert.assertEquals(count, 256);
    }

    @Test
    public void testLUD() throws Exception {
        int[][] results = {
                {0, 0, 0},
                {0, 0, 1},
                {0, 1, 0},
                {0, 1, 1},
                {0, 2, 0},
                {0, 2, 1},
                {1, 0, 0},
                {1, 0, 1},
                {1, 1, 0},
                {1, 1, 1},
                {1, 2, 0},
                {1, 2, 1}
        };

        int[] luds = {0, 2, 1, 2, 1, 2, 0, 2, 1, 2, 1, 2};

        IntTuplesPort port = new IntTuplesPort(2, 3, 2);
        int i = 0;
        int[] r;
        while ((r = port.take()) != null) {
            Assert.assertArrayEquals(results[i], r);
            Assert.assertEquals(luds[i++], port.getLastUpdateDepth());
        }
    }

    @Test
    public void test3() {
        IntTuplesPort port = new IntTuplesPort(3, 2, 2);
        int[] c;
        while ((c = port.take()) != null) {
            System.out.println(Arrays.toString(c));
        }
    }
}
