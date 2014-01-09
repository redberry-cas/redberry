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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getBaseAsArray;
import static cc.redberry.core.number.NumberUtils.factorial;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsTestUtils {

    /**
     * Very raw check for set stabilizer
     */
    public static class RawSetwiseStabilizerCriteria
            implements Indicator<Permutation>, BacktrackSearchTestFunction {
        final int[] set;
        final int[] base;

        public RawSetwiseStabilizerCriteria(int[] set, int[] base) {
            this.set = set;
            Arrays.sort(set);
            this.base = base;
        }

        @Override
        public boolean test(Permutation permutation, int level) {
            if (Arrays.binarySearch(set, base[level]) < 0)
                return true;
            return Arrays.binarySearch(set, permutation.newIndexOf(base[level])) >= 0;
        }

        @Override
        public boolean is(Permutation p) {
            for (int i : set)
                if (Arrays.binarySearch(set, p.newIndexOf(i)) < 0)
                    return false;
            return true;

        }
    }

    /**
     * Calculates setwise stabilizer (brute-force algorithm).
     *
     * @param pg
     * @param set
     * @return
     */
    public static PermutationGroup calculateRawSetwiseStabilizer(PermutationGroup pg, int[] set) {
        List<BSGSElement> bsgs = pg.getBSGS();
        int[] base = getBaseAsArray(bsgs);
        RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, base);

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, rw, rw);
        return new PermutationGroup(AlgorithmsBase.asBSGSList(subgroup), true);
    }

    public static void assertHaveNoIntersections(Permutation[] a, Permutation[] b) {
        if (a.length == 0 || b.length == 0)
            return;
        Arrays.sort(a);
        Arrays.sort(b);

        if (b.length < a.length) {
            Permutation[] c = b;
            b = a;
            a = c;
        }

        int j = 0;
        for (int i = 0; i < b.length; ++i) {
            while (j < a.length - 1 && b[i].compareTo(a[j]) > 0)
                ++j;
            if (b[i].compareTo(a[j]) == 0)
                throw new AssertionError(a[j] + " and " + b[i] + "  are equal.");
        }
    }

    public static void assertHaveNoNullElements(final Object[] array) {
        for (int i = 0; i < array.length; ++i)
            if (array[i] == null)
                throw new AssertionError("Null element at " + i + " position.");
    }

    @Test
    public void testHaveNoIntersections1() {
        Permutation[] a = new Permutation[24];
        IntPermutationsGenerator gen = new IntPermutationsGenerator(4);
        int i = 0;
        for (int[] p : gen)
            a[i++] = new PermutationOneLine(ArraysUtils.addAll(p, 4, 5, 6, 7));

        gen = new IntPermutationsGenerator(4);
        Permutation[] b = new Permutation[12];
        i = 0;
        for (int[] p : gen) {
            if (i >= 8 && i < 20)
                b[i - 8] = new PermutationOneLine(ArraysUtils.addAll(p, 4, 5, 6, 7));
            ++i;
        }
        try {
            assertHaveNoIntersections(a, b);
            assertTrue(false);
        } catch (AssertionError e) {
            assertTrue(true);
        }
        try {
            assertHaveNoIntersections(b, a);
            assertTrue(false);
        } catch (AssertionError e) {
            assertTrue(true);
        }
    }

    @Test
    public void testHaveNoIntersections2() {

        int degree = 4;
        int total = NumberUtils.factorial(degree).intValue();

        IntPermutationsGenerator gen = new IntPermutationsGenerator(degree);
        Permutation[] all = new Permutation[total];

        int i = 0;
        for (int[] p : gen)
            all[i++] = new PermutationOneLine(p);

        IntCombinationsGenerator combGen;
        Permutation[] a, b;
        for (int k = 0; k < total; ++k) {
            if (factorial(total).divide(factorial(total - k).multiply(factorial(k))).intValue() > 50000)
                continue;
            combGen = new IntCombinationsGenerator(total, k);
            for (int[] comb : combGen) {
                a = new Permutation[k];
                b = new Permutation[total - k];
                int p = 0, q = 0;
                boolean selected;
                for (i = 0; i < total; ++i) {
                    selected = false;
                    for (int f : comb)
                        if (i == f) {
                            selected = true;
                            break;
                        }
                    if (selected)
                        a[p++] = all[i];
                    else
                        b[q++] = all[i];
                }
                assertHaveNoIntersections(a, b);
                assertHaveNoIntersections(b, a);
            }
        }
    }

    @Test
    public void testHaveNoIntersections3() {

        int degree = 4;
        int total = NumberUtils.factorial(degree).intValue();

        IntPermutationsGenerator gen = new IntPermutationsGenerator(degree);
        Permutation[] all = new Permutation[total];

        int i = 0;
        for (int[] p : gen)
            all[i++] = new PermutationOneLine(p);

        IntCombinationsGenerator combGen;
        Permutation[] a, b;
        for (int k = 1; k < total; ++k) {
            if (factorial(total).divide(factorial(total - k).multiply(factorial(k))).intValue() > 50000)
                continue;
            combGen = new IntCombinationsGenerator(total, k);
            for (int[] comb : combGen) {
                a = new Permutation[k];
                b = new Permutation[total - k + 1];
                int p = 0, q = 0;
                boolean selected, taken = false;
                for (i = 0; i < total; ++i) {
                    selected = false;
                    for (int f : comb)
                        if (i == f) {
                            selected = true;
                            break;
                        }
                    if (!taken && selected) {
                        taken = true;
                        b[q++] = all[i];
                    }

                    if (selected)
                        a[p++] = all[i];
                    else
                        b[q++] = all[i];
                }
                try {
                    assertHaveNoIntersections(a, b);
                    throw new RuntimeException();
                } catch (AssertionError e) {
                }
                try {
                    assertHaveNoIntersections(b, a);
                    Arrays.toString(a);
                    Arrays.toString(b);
                    throw new RuntimeException();
                } catch (AssertionError e) {
                }
            }
        }
    }

//    @Test
//    public void testName() throws Exception {
//        for (int i = 0; i < 24; ++i) {
//            if (factorial(24).divide(factorial(24 - i).multiply(factorial(i))).intValue() < 50000)
//                System.out.println(i + "   " + factorial(24).divide(factorial(24 - i).multiply(factorial(i))));
//        }
//    }
}
