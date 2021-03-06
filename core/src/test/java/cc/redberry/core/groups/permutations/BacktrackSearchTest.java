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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntComparator;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.calculateOrder;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getBaseAsArray;
import static cc.redberry.core.groups.permutations.PermutationsTestUtils.RawSetwiseStabilizerCriteria;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BacktrackSearchTest extends AbstractPermutationTest {

    @Test
    public void testAll1() throws Exception {
        List<Permutation> generators = new ArrayList<>();
        generators.add(Permutations.createPermutation(0, 2, 1, 3, 4));
        generators.add(Permutations.createPermutation(3, 2, 4, 0, 1));

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(generators);


        BacktrackSearch search = new BacktrackSearch(bsgs);
        InducedOrderingOfPermutations comparator = new InducedOrderingOfPermutations(getBaseAsArray(bsgs));

        Permutation previous = null, current;
        int i = 0;
        while ((current = search.take()) != null) {
            if (i != 0) {
                assertTrue(comparator.compare(previous, current) < 0);
                assertTrue(comparator.compare(current, previous) > 0);
            }
            previous = current;
            ++i;
        }
        assertEquals(calculateOrder(bsgs).intValue(), i);
    }

    @Test
    public void testAll2() throws Exception {
        Permutation gen0 = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = Permutations.createPermutation(0, 5, 4, 6, 2, 1, 3, 7, 9, 8);

        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(generators);


        PermutationLessThenTestComparator comparator = new PermutationLessThenTestComparator(getBaseAsArray(bsgs),
                bsgs.get(0).internalDegree());
        BacktrackSearch search = new BacktrackSearch(bsgs);

        Permutation previous = null, current;
        int i = 0;

        while ((current = search.take()) != null) {
            if (i != 0) {
                assertTrue(comparator.compare(previous, current) <= 0);
            }
            previous = current;
            ++i;
        }
        assertEquals(calculateOrder(bsgs).intValue(), i);
    }

    @Test
    public void testAll3() {
        //an example from Sec. 4.6.1 in [Holt05]
        Permutation a0 = Permutations.createPermutation(0, 1, 2, 3, 4, 5);
        Permutation a1 = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
        Permutation a2 = Permutations.createPermutation(0, 3, 2, 1, 4, 5);
        Permutation a3 = Permutations.createPermutation(0, 3, 2, 1, 5, 4);
        Permutation a4 = Permutations.createPermutation(1, 0, 3, 2, 4, 5);
        Permutation a5 = Permutations.createPermutation(1, 0, 3, 2, 5, 4);
        Permutation a6 = Permutations.createPermutation(1, 2, 3, 0, 4, 5);
        Permutation a7 = Permutations.createPermutation(1, 2, 3, 0, 5, 4);
        Permutation a8 = Permutations.createPermutation(2, 1, 0, 3, 4, 5);
        Permutation a9 = Permutations.createPermutation(2, 1, 0, 3, 5, 4);
        Permutation a10 = Permutations.createPermutation(2, 3, 0, 1, 4, 5);
        Permutation a11 = Permutations.createPermutation(2, 3, 0, 1, 5, 4);
        Permutation a12 = Permutations.createPermutation(3, 0, 1, 2, 4, 5);
        Permutation a13 = Permutations.createPermutation(3, 0, 1, 2, 5, 4);
        Permutation a14 = Permutations.createPermutation(3, 2, 1, 0, 4, 5);
        Permutation a15 = Permutations.createPermutation(3, 2, 1, 0, 5, 4);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15};

        Permutation gen0 = Permutations.createPermutation(1, 2, 3, 0, 4, 5);
        Permutation gen1 = Permutations.createPermutation(0, 3, 2, 1, 4, 5);
        Permutation gen2 = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
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
        int count = 0;
        BacktrackSearch search = new BacktrackSearch(bsgs);
        Permutation current;
        int ii = 0;
        while ((current = search.take()) != null) {
            assertEquals(expected[ii++], current);
        }
        assertEquals(expected.length, ii);
    }


    @Test
    @TestWithGAP
    public void testAllPrimitive() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        int scanned = 0;
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                    continue;

                PermutationGroup pg = gap.primitiveGroup(degree, i);
                if (pg.order().compareTo(BigInteger.valueOf(100000)) > 0)
                    continue;

                ++scanned;
                List<BSGSElement> bsgs = pg.getBSGS();


                BacktrackSearch search = new BacktrackSearch(bsgs);
                PermutationLessThenTestComparator comparator = new PermutationLessThenTestComparator(getBaseAsArray(bsgs),
                        bsgs.get(0).internalDegree());

                Permutation previous = null, current;
                int count = 0;
                while ((current = search.take()) != null) {
                    if (count != 0) {
                        assertTrue(comparator.compare(previous, current) <= 0);
                    }
                    previous = current;
                    ++count;
                }
                assertEquals(calculateOrder(bsgs).intValue(), count);
            }
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
    }

    @Test
    public void testPrune1() {
        //an example from Sec. 4.6.2 in [Holt05]
        //find all permutations that satisfies the following conditions:
        //g(0) = 0 or 2, g(1) = 1

        Permutation a0 = Permutations.createPermutation(0, 1, 2, 3, 4, 5);
        Permutation a1 = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
        Permutation a2 = Permutations.createPermutation(2, 1, 0, 3, 4, 5);
        Permutation a3 = Permutations.createPermutation(2, 1, 0, 3, 5, 4);
        final Permutation[] expected = {a0, a1, a2, a3};

        Permutation gen0 = Permutations.createPermutation(1, 2, 3, 0, 4, 5);
        Permutation gen1 = Permutations.createPermutation(0, 3, 2, 1, 4, 5);
        Permutation gen2 = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        Permutation current;
        //ITERATOR
        BacktrackSearch search = new BacktrackSearch(bsgs);
        search.setTestFunction(new BacktrackSearchTestFunction() {
            @Override
            public boolean test(Permutation permutation, int level) {
                if (level == 0)
                    return permutation.newIndexOf(0) == 0 || permutation.newIndexOf(0) == 2;
                if (level == 1)
                    return permutation.newIndexOf(1) == 1;
                return true;
            }
        });
        int ii = 0;
        while ((current = search.take()) != null) {
            assertEquals(expected[ii++], current);
        }
        assertEquals(expected.length, ii);
    }

    @Test
    public void testPrune2() {
        //no any pruning, just property test

        Permutation a0 = Permutations.createPermutation(0, 1, 2, 3, 4, 5);
        Permutation a1 = Permutations.createPermutation(0, 3, 2, 1, 4, 5);
        Permutation a2 = Permutations.createPermutation(1, 0, 3, 2, 4, 5);
        Permutation a3 = Permutations.createPermutation(1, 2, 3, 0, 4, 5);
        Permutation a4 = Permutations.createPermutation(2, 1, 0, 3, 4, 5);
        Permutation a5 = Permutations.createPermutation(2, 3, 0, 1, 4, 5);
        Permutation a6 = Permutations.createPermutation(3, 0, 1, 2, 4, 5);
        Permutation a7 = Permutations.createPermutation(3, 2, 1, 0, 4, 5);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7};

        Permutation gen0 = Permutations.createPermutation(1, 2, 3, 0, 4, 5);
        Permutation gen1 = Permutations.createPermutation(0, 3, 2, 1, 4, 5);
        Permutation gen2 = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        Permutation current;
        //ITERATOR
        BacktrackSearch search = new BacktrackSearch(bsgs);
        search.setProperty(new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(5) == 5;
            }
        });
        int ii = 0;
        while ((current = search.take()) != null) {
            assertEquals(expected[ii++], current);
        }
        assertEquals(expected.length, ii);
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

    @Test
    public void testSearchStabilizer1() throws Exception {
        Permutation gen0 = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = Permutations.createPermutation(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1);
        int[] set = {4, 9};
        testBruteForceSearchStabilizer(pg, set);
    }

    @Test
    public void testSearchStabilizer2() throws Exception {
        Permutation gen0 = Permutations.createPermutation(1, 2, 0, 4, 5, 3, 7, 8, 6, 9);
        Permutation gen1 = Permutations.createPermutation(0, 1, 2, 6, 7, 8, 3, 4, 5, 9);
        Permutation gen2 = Permutations.createPermutation(0, 5, 7, 8, 1, 3, 4, 6, 2, 9);
        Permutation gen3 = Permutations.createPermutation(9, 1, 2, 6, 5, 4, 3, 8, 7, 0);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2, gen3);
        int[] set = {0, 3};
        testBruteForceSearchStabilizer(pg, set);
    }

    @Test
    public void testSearchStabilizer3() throws Exception {
        Permutation gen0 = Permutations.createPermutation(0, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 11, 19, 20, 12, 21, 13, 14, 22, 23, 15, 16, 24, 17, 25, 26, 18);
        Permutation gen1 = Permutations.createPermutation(11, 1, 18, 3, 22, 26, 9, 25, 7, 5, 24, 12, 13, 14, 0, 2, 15, 16, 17, 4, 19, 20, 21, 6, 8, 10, 23);
        Permutation gen2 = Permutations.createPermutation(0, 1, 2, 3, 4, 5, 6, 9, 10, 7, 8, 11, 12, 14, 13, 16, 15, 17, 18, 20, 19, 21, 22, 23, 24, 26, 25);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2);
        int[] set = {5, 7, 10, 11, 14, 16, 17, 19};
        testBruteForceSearchStabilizer(pg, set);
    }

    public static void testBruteForceSearchStabilizer(PermutationGroup pg, int[] set) {
        List<BSGSElement> bsgs = pg.getBSGS();
        int[] base = getBaseAsArray(bsgs);
        int order = calculateOrder(bsgs).intValue();
        RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, base);

        //empty initial subgroup
        BacktrackSearch search = new BacktrackSearch(bsgs, rw, rw);


        ArrayList<Permutation> expected = new ArrayList<>(order);
        Iterator<Permutation> allIterator = PermutationGroup.createPermutationGroupFromBSGS(bsgs).iterator();
        Permutation c;
        while (allIterator.hasNext()) {
            c = allIterator.next();
            if (rw.is(c))
                expected.add(c);
        }

        ArrayList<Permutation> actual = new ArrayList<>(order);
        while ((c = search.take()) != null) {
            if (rw.is(c))
                actual.add(c);
        }

        Collections.sort(actual);
        Collections.sort(expected);

        assertEquals(expected, actual);
    }

    /**
     * Use only for tests
     */
    public static final class PermutationLessThenTestComparator implements Comparator<Permutation> {
        final int[] base, sortedBase;
        final IntComparator baseComparator;

        public PermutationLessThenTestComparator(int[] base, int degree) {
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
