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

import cc.redberry.core.groups.permutations.gap.GapPrimitiveGroupsReader;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntComparator;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getBaseAsArray;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getOrder;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BacktrackIteratorTest {

    @Test
    public void testAll1() throws Exception {
        List<Permutation> generators = new ArrayList<>();
        generators.add(new PermutationOneLine(0, 2, 1, 3, 4));
        generators.add(new PermutationOneLine(3, 2, 4, 0, 1));

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGS(generators).getBSGSList();


        BacktrackIterator iterator = new BacktrackIterator(bsgs);
        InducedOrderingOfPermutations comparator = new InducedOrderingOfPermutations(getBaseAsArray(bsgs));

        Permutation previous = null, current;
        int i = 0;
        while (iterator.hasNext()) {
            current = iterator.next();
            if (i != 0) {
                assertTrue(comparator.compare(previous, current) < 0);
                assertTrue(comparator.compare(current, previous) > 0);
            }
            previous = current;
            ++i;
        }
        assertEquals(getOrder(bsgs).intValue(), i);
    }

    @Test
    public void testAll2() throws Exception {
        Permutation gen0 = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = new PermutationOneLine(0, 5, 4, 6, 2, 1, 3, 7, 9, 8);

        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGS(generators).getBSGSList();


        PermutationLessThenTestComparator comparator = new PermutationLessThenTestComparator(getBaseAsArray(bsgs));
        BacktrackIterator iterator = new BacktrackIterator(bsgs);

        Permutation previous = null, current;
        int i = 0;

        while (iterator.hasNext()) {
            current = iterator.next();
            if (i != 0) {
                assertTrue(comparator.compare(previous, current) <= 0);
            }
            previous = current;
            ++i;
        }
        assertEquals(getOrder(bsgs).intValue(), i);
    }


    @Test
    public void testAllPrimitive() throws Exception {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");

        int s = 0;
        for (int i = 0; i < pgs.length; ++i) {
            if (pgs[i].order().compareTo(BigInteger.valueOf(10000)) > 0)
                continue;
            ++s;
            List<BSGSElement> bsgs = pgs[i].getBSGS().getBSGSList();


            BacktrackIterator iterator = new BacktrackIterator(bsgs);
            PermutationLessThenTestComparator comparator = new PermutationLessThenTestComparator(getBaseAsArray(bsgs));

            Permutation previous = null, current;
            int count = 0;
            while (iterator.hasNext()) {
                current = iterator.next();
                if (count != 0) {
                    assertTrue(comparator.compare(previous, current) <= 0);
                }
                previous = current;
                ++count;
            }
            assertEquals(getOrder(bsgs).intValue(), count);
        }
    }


    @Test
    public void testPrintElements2() {
        //an example from Sec. 4.6.1 in [Holt05]
        Permutation a0 = new PermutationOneLine(0, 1, 2, 3, 4, 5);
        Permutation a1 = new PermutationOneLine(0, 1, 2, 3, 5, 4);
        Permutation a2 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation a3 = new PermutationOneLine(0, 3, 2, 1, 5, 4);
        Permutation a4 = new PermutationOneLine(1, 0, 3, 2, 4, 5);
        Permutation a5 = new PermutationOneLine(1, 0, 3, 2, 5, 4);
        Permutation a6 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation a7 = new PermutationOneLine(1, 2, 3, 0, 5, 4);
        Permutation a8 = new PermutationOneLine(2, 1, 0, 3, 4, 5);
        Permutation a9 = new PermutationOneLine(2, 1, 0, 3, 5, 4);
        Permutation a10 = new PermutationOneLine(2, 3, 0, 1, 4, 5);
        Permutation a11 = new PermutationOneLine(2, 3, 0, 1, 5, 4);
        Permutation a12 = new PermutationOneLine(3, 0, 1, 2, 4, 5);
        Permutation a13 = new PermutationOneLine(3, 0, 1, 2, 5, 4);
        Permutation a14 = new PermutationOneLine(3, 2, 1, 0, 4, 5);
        Permutation a15 = new PermutationOneLine(3, 2, 1, 0, 5, 4);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15};

        Permutation gen0 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation gen1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation gen2 = new PermutationOneLine(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        //PRINTELEMENTS
        final int[] i = {0};
        printElements(bsgs, new PFunction() {
            @Override
            public void dosmth(Permutation p) {
                assertEquals(expected[i[0]++], p);
            }
        });

        //ITERATOR
        BacktrackIterator iterator = new BacktrackIterator(bsgs);
        int ii = 0;
        while (iterator.hasNext()) {
            assertEquals(expected[ii++], iterator.next());
        }
    }

    public static interface PFunction {
        void dosmth(Permutation p);
    }

    /**
     * Algorithm PRINTELEMENTS(G) described in Sec. 4.6.1 of [Holt05]
     *
     * @param bsgs     BSGS
     * @param function some function that will be executes on each element
     */
    public static void printElements(List<BSGSElement> bsgs, final PFunction function) {
        IntComparator comparator = new InducedOrdering(getBaseAsArray(bsgs));

        int k = bsgs.size();
        int[] c = new int[k];
        int[][] orbits = new int[k][];
        Permutation[] word = new Permutation[k];

        int l = 0;
        c[l] = 0;
        orbits[l] = bsgs.get(l).orbitList.toArray();
        ArraysUtils.quickSort(orbits[l], comparator);
        word[l] = bsgs.get(0).stabilizerGenerators.get(0).getIdentity();

        while (true) {

            while (l < k - 1) {
                ++l;
                orbits[l] = word[l - 1].imageOf(bsgs.get(l).orbitList.toArray());
                ArraysUtils.quickSort(orbits[l], comparator);
                InducedOrderingTest.assertSetIsSorted(comparator, orbits[l]);
                c[l] = 0;
                word[l] = bsgs.get(l).getTransversalOf(
                        word[l - 1].newIndexOfUnderInverse(orbits[l][c[l]])
                );
                word[l] = word[l].composition(word[l - 1]);
            }

            function.dosmth(word[l]);

            while (l >= 0 && c[l] == bsgs.get(l).orbitList.size() - 1)
                --l;

            if (l == -1)
                return;

            ++c[l];
            if (l == 0) {
                word[l] = bsgs.get(l).getTransversalOf(orbits[l][c[l]]);
            } else {
                word[l] = bsgs.get(l).getTransversalOf(
                        word[l - 1].newIndexOfUnderInverse(orbits[l][c[l]])
                );
                word[l] = word[l].composition(word[l - 1]);
            }
        }
    }


    /**
     * Use only for tests
     */
    public static final class PermutationLessThenTestComparator implements Comparator<Permutation> {
        final int[] base, sortedBase;
        final IntComparator baseComparator;

        public PermutationLessThenTestComparator(int[] base) {
            this.base = base;
            this.sortedBase = base.clone();
            Arrays.sort(sortedBase);
            this.baseComparator = new InducedOrdering(base);
        }

        @Override
        public int compare(Permutation a, Permutation b) {

            int compare;
            for (int i = 0; i < base.length; ++i) {
                if (Arrays.binarySearch(sortedBase, a.newIndexOf(base[i])) < 0) return 0;
                if ((compare = baseComparator.compare(a.newIndexOf(base[i]), b.newIndexOf(base[i]))) != 0)
                    return compare;
            }
            return 0;
        }
    }
}
