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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IntPermutationsGeneratorTest {
    public IntPermutationsGeneratorTest() {
    }

    @Test
    public void time_test() {
        IntPermutationsGenerator ig = new IntPermutationsGenerator(10);
        while (ig.hasNext())
            ig.next();
//            System.out.println(Arrays.toString(ig.next()));
    }

    @Test
    public void test() {
        IntPermutationsGenerator ig = new IntPermutationsGenerator(3);
        while (ig.hasNext())
            System.out.println(Arrays.toString(ig.next()));
    }

    @Test
    public void test1() {
        IntPermutationsGenerator ig = new IntPermutationsGenerator(3);
        List<int[]> list = new ArrayList<>();
        while (ig.hasNext())
            list.add(ig.next().clone());
        for (int[] arr : list)
            System.out.println(Arrays.toString(arr));
    }

    @Test
    public void test2() {
        IntPermutationsGenerator ig = new IntPermutationsGenerator(0);
        while (ig.hasNext())
            System.out.println(Arrays.toString(ig.next()));
    }

    @Test
    public void test3() {
        final IntPermutationsGenerator ig = new IntPermutationsGenerator(2);
        int[] p;
        ig.reset();


        while (true) {
            if (!ig.hasNext())
                break;
            p = ig.next();
            System.out.println(Arrays.toString(p));
        }
//        for (;;) {
//            System.out.println(Arrays.toString(p = ig.next()));
//            if (!ig.hasNext())
//                break;
//        }
    }

    @Test
    public void hardcore() {
        final int[] permutation = {0, 1};
        int size = 2;
        final int end = size - 1;
        int p = end, low, high, med, s;
        while ((p > 0) && (permutation[p] < permutation[p - 1]))
            p--;
        if (p > 0) //if p==0 then it's the last one
        {
            s = permutation[p - 1];
            if (permutation[end] > s)
                low = end;
            else {
                high = end;
                low = p;
                while (high > low + 1) {
                    med = (high + low) >> 1;
                    if (permutation[med] < s)
                        high = med;
                    else
                        low = med;
                }
            }
            permutation[p - 1] = permutation[low];
            permutation[low] = s;
        }
        high = end;
        while (high > p) {
            med = permutation[high];
            permutation[high] = permutation[p];
            permutation[p] = med;
            p++;
            high--;
        }
        System.out.println(Arrays.toString(permutation));
    }
}
