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

import java.util.Arrays;
import org.junit.Test;

/**
 *
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
        int[] c;
        while ((c = dcp.take()) != null)
            System.out.println(Arrays.toString(c));
    }

    @Test
    public void test2() {
        int[] a1 = {1, 2};
        int[] a2 = {2, 4};
        int[] a3 = {3, 4, 5};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        int[] c;
        while ((c = dcp.take()) != null)
            System.out.println(Arrays.toString(c));
    }

    @Test
    public void test3() {
        int[] a1 = {1, 2, 3};
        int[] a2 = {1, 2, 3};
        int[] a3 = {1, 2, 3};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        int[] c;
        while ((c = dcp.take()) != null)
            System.out.println(Arrays.toString(c));
    }

    @Test
    public void test4() {
        int[] a1 = {1};
        int[] a2 = {3};
        int[] a3 = {1,2};
        int[][] aa = {a1, a2, a3};
        DistinctCombinationsPort dcp = new DistinctCombinationsPort(aa);
        int[] c;
        while ((c = dcp.take()) != null)
            System.out.println(Arrays.toString(c));
    }
}