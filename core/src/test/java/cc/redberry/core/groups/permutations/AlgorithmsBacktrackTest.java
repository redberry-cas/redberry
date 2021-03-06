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

import cc.redberry.core.context.CC;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.test.LongTest;
import cc.redberry.core.test.PerformanceTest;
import cc.redberry.core.utils.Indicator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.groups.permutations.PermutationGroup.createPermutationGroup;
import static cc.redberry.core.groups.permutations.Permutations.createPermutation;
import static cc.redberry.core.groups.permutations.PermutationsTestUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBacktrackTest extends AbstractPermutationTest {

    @Test
    public void testSameGroupSearch() {
        PermutationGroup s30_known = PermutationGroup.symmetricGroup(30);
        ArrayList<BSGSCandidateElement> s30_bsgs = new ArrayList<>();
        AlgorithmsBacktrack.subgroupSearch(s30_known.getBSGS(),
                s30_bsgs, BacktrackSearchTestFunction.TRUE, Indicator.TRUE_INDICATOR);
        PermutationGroup s30 = PermutationGroup.createPermutationGroupFromBSGS(asBSGSList(s30_bsgs));
        assertEquals(s30_known, s30);
    }

    @Test
    public void test1() {
        //no any pruning, just property test
        Permutation
                a = Permutations.createPermutation(1, 2, 3, 0, 4, 5),
                b = Permutations.createPermutation(0, 3, 2, 1, 4, 5),
                c = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>(Arrays.asList(a, b, c));
        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        final int point = 5;
        Indicator<Permutation> stabilizer = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(point) == point;
            }
        };

        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, BacktrackSearchTestFunction.TRUE, stabilizer);

        assertTrue(isBSGS(subgroup));
        BacktrackSearch all = new BacktrackSearch(subgroup);
        Permutation current;
        while ((current = all.take()) != null)
            assertEquals(point, current.newIndexOf(point));
    }

    @Test
    public void test2() throws Exception {
        //no any pruning, just property test
        Permutation
                a = Permutations.createPermutation(1, 2, 3, 0, 4, 5),
                b = Permutations.createPermutation(0, 3, 2, 1, 4, 5),
                c = Permutations.createPermutation(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>(Arrays.asList(a, b, c));

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        final int point = 5;
        Indicator<Permutation> stabilizer = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(point) == point;
            }
        };

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, BacktrackSearchTestFunction.TRUE, stabilizer);

        int[] base;
        assertTrue(isBSGS(subgroup));
        removeRedundantBaseRemnant(subgroup);
        assertTrue(isBSGS(subgroup));
        base = AlgorithmsBase.getBaseAsArray(subgroup);

        Permutation a0 = Permutations.createPermutation(0, 1, 2, 3, 4, 5);
        Permutation a1 = Permutations.createPermutation(0, 3, 2, 1, 4, 5);
        Permutation a2 = Permutations.createPermutation(1, 0, 3, 2, 4, 5);
        Permutation a3 = Permutations.createPermutation(1, 2, 3, 0, 4, 5);
        Permutation a4 = Permutations.createPermutation(2, 1, 0, 3, 4, 5);
        Permutation a5 = Permutations.createPermutation(2, 3, 0, 1, 4, 5);
        Permutation a6 = Permutations.createPermutation(3, 0, 1, 2, 4, 5);
        Permutation a7 = Permutations.createPermutation(3, 2, 1, 0, 4, 5);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7};

        Comparator<Permutation> permutationComparator = new InducedOrderingOfPermutations(base);
        Arrays.sort(expected, permutationComparator);

        assertEquals(BigInteger.valueOf(8), calculateOrder(subgroup));

        Permutation[] actual = new Permutation[expected.length];
        BacktrackSearch search = new BacktrackSearch(subgroup);
        Permutation current;
        int ii = 0;
        while ((current = search.take()) != null)
            actual[ii++] = current;

        assertEquals(expected.length, ii);
        Arrays.sort(actual, permutationComparator);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void test3() {
        PermutationGroup sym2 = PermutationGroup.symmetricGroup(2);
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        ArrayList<BSGSCandidateElement> bsgs = sym2.getBSGSCandidate();
        AlgorithmsBacktrack.rebaseWithRedundancy(bsgs, new int[]{0, 1, 2}, sym2.degree());
        assertTrue(isBSGS(bsgs));
        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, BacktrackSearchTestFunction.TRUE, Indicator.TRUE_INDICATOR);
        assertEquals(sym2, PermutationGroup.createPermutationGroupFromBSGS(asBSGSList(subgroup)));
    }

    @Test
    @TestWithGAP
    @LongTest
    public void testSetwiseStabilizer1_raw() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {

                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;
                //System.out.println("  " + i);
                List<BSGSElement> group = createBSGSList(Arrays.asList(gap.primitiveGenerators(degree, i)));
                int nl = 1 + CC.getRandomGenerator().nextInt(degree / 4);
                int[] set = Permutations.getRandomSortedDistinctArray(0, degree, nl, CC.getRandomGenerator());
                //System.out.println(Arrays.toString(set));
                RawSetwiseStabilizerCriteria criteria = new RawSetwiseStabilizerCriteria(set, getBaseAsArray(group));

                ArrayList<BSGSCandidateElement> stabilizer = new ArrayList<>();
                AlgorithmsBacktrack.subgroupSearch(group, stabilizer, criteria, criteria);
                assertTrue(isBSGS(stabilizer));


                //not works because of the bug in genss
                //gap.evaluateRedberryGroup("rg", stabilizer.get(0).stabilizerGenerators);
                //gap.evaluate("swrec:= SetwiseStabilizer(g, OnPoints, " + GapGroupsInterface.convertToGapList(set) + ");");
                //assertTrue(gap.evaluateToBoolean("rg = swrec.setstab;"));

                int order = calculateOrder(stabilizer).intValue();
                if (order < 100_000 && order > 1) {
                    int[] c_set = set.clone();
                    Arrays.sort(c_set);
                    BacktrackSearch it = new BacktrackSearch(stabilizer, BacktrackSearchTestFunction.TRUE, Indicator.TRUE_INDICATOR);
                    Permutation c;
                    while ((c = it.take()) != null) {
                        for (int point : set)
                            assertTrue(Arrays.binarySearch(c_set, c.newIndexOf(point)) >= 0);
                    }
                }

                if (set.length == 1) {
                    gap.evaluateRedberryGroup("rg", stabilizer.get(0).stabilizerGenerators);
                    gap.evaluate("stab:= Stabilizer(g, " + (set[0] + 1) + ");");
                    assertTrue(gap.evaluateToBoolean("rg = stab;"));
                }
            }
        }
    }

    @Test
    @TestWithGAP
    @PerformanceTest
    public void testSetwiseStabilizer2_raw() throws Exception {

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        int scanned = 0;

        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {

                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                PermutationGroup g = gap.primitiveGroup(degree, i);
                if (g.order().compareTo(BigInteger.valueOf(1_000_000)) > 0)
                    continue;

                ++scanned;

                int setl;
                if (degree == 4)
                    setl = 2;
                else if (degree == 5)
                    setl = CC.getRandomGenerator().nextBoolean() ? 2 : 3;
                else if (degree < 10)
                    setl = degree / 2 - 1;
                else if (degree < 30)
                    setl = degree / 3 - 1;
                else if (degree < 60)
                    setl = degree / 4 - 1;
                else if (degree < 100)
                    setl = degree / 6 - 1;
                else
                    setl = degree / 10 - 1;

                int[] set = Permutations.getRandomSortedDistinctArray(0, degree,
                        setl,
                        CC.getRandomGenerator());
                PermutationGroup stabilizer = testSearchStabilizerRaw(g, set);
                statistics.addValue(stabilizer.order().intValue());
            }
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of stabilizer orders: ");
        System.out.println(statistics);
    }


    @Test
    @TestWithGAP
    @PerformanceTest
    public void testSetwiseStabilizer2_raw_visited_nodes_stat() throws Exception {
        DescriptiveStatistics orders = new DescriptiveStatistics();
        DescriptiveStatistics visited = new DescriptiveStatistics();
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

                PermutationGroup g = gap.primitiveGroup(degree, i);
                if (g.order().compareTo(BigInteger.valueOf(1_000_000)) > 0)
                    continue;

                //System.out.println("  " + i);

                ++scanned;

                int setl;
                if (degree == 4)
                    setl = 2;
                else if (degree == 5)
                    setl = CC.getRandomGenerator().nextBoolean() ? 2 : 3;
                else if (degree < 10)
                    setl = degree / 2 - 1;
                else if (degree < 60)
                    setl = degree / 5 - 1;
                else if (degree < 100)
                    setl = degree / 7 - 1;
                else
                    setl = degree / 15 - 1;
                if (setl == 0)
                    setl = 1;
                int[] set = Permutations.getRandomSortedDistinctArray(0, degree,
                        setl,
                        CC.getRandomGenerator());

                PermutationGroup stabilizer = testSearchStabilizerRaw(g, set);

                //System.out.println("Group order: " + g.order() + "  Visited: " + AlgorithmsBacktrack.____VISITED_NODES___[0] + "  Stabilizer order: " + stabilizer.order());
                visited.addValue((((double) AlgorithmsBacktrack.____VISITED_NODES___[0]) / g.order().doubleValue()) * 100.0);
                orders.addValue(stabilizer.order().intValue());
            }
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of stabilizer orders: ");
        System.out.println(orders);
        System.out.println("Statistic of percent of visited nodes: ");
        System.out.println(visited);
    }

    @Test
    @TestWithGAP
    public void testSetwiseStabilizer3_raw_all_set() throws Exception {
        DescriptiveStatistics visited = new DescriptiveStatistics();
        int scanned = 0;
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                ++scanned;
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                    continue;

                PermutationGroup g = gap.primitiveGroup(degree, i);

                int[] set = new int[degree];
                for (int s = 1; s < degree; ++s)
                    set[s] = s;

                PermutationGroup stabilizer = calculateRawSetwiseStabilizer(g, set);
                assertEquals(g.order(), stabilizer.order());

                //System.out.println("Group order: " + g.order() + "  Visited: " + AlgorithmsBacktrack.____VISITED_NODES___[0]);

                visited.addValue((((double) AlgorithmsBacktrack.____VISITED_NODES___[0]) / g.order().doubleValue()) * 100.0);
            }
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of percent of visited nodes: ");
        System.out.println(visited);
    }

    @Test
    public void testSetwiseStabilizer1a_raw() throws Exception {
        Permutation gen0 = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = Permutations.createPermutation(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1);

        int[] set = {4, 9};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testSetwiseStabilizer1b_raw() throws Exception {
        Permutation gen0 = Permutations.createPermutation(1, 2, 0, 4, 5, 3, 7, 8, 6, 9);
        Permutation gen1 = Permutations.createPermutation(0, 1, 2, 6, 7, 8, 3, 4, 5, 9);
        Permutation gen2 = Permutations.createPermutation(0, 5, 7, 8, 1, 3, 4, 6, 2, 9);
        Permutation gen3 = Permutations.createPermutation(9, 1, 2, 6, 5, 4, 3, 8, 7, 0);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2, gen3);
        int[] set = {0, 3};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testSetwiseStabilizer1c_raw() throws Exception {
        Permutation gen0 = Permutations.createPermutation(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = Permutations.createPermutation(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1);
        int[] set = {3, 7};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testLeftCosetRepresentatives1() {
        Permutation gen0 = Permutations.createPermutation(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = Permutations.createPermutation(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1);
        int[] set = {3, 7};

        PermutationGroup stabilizer = testSearchStabilizerRaw(pg, set);

        Permutation[] coset_reps = AlgorithmsBacktrack.leftCosetRepresentatives(
                pg.getBSGSCandidate(), stabilizer.getBSGSCandidate());
        assertHaveNoNullElements(coset_reps);

        Permutation[] subgroup_elements = new Permutation[stabilizer.order().intValue()];
        int i = 0;
        for (Permutation p : stabilizer)
            subgroup_elements[i++] = p;

        Permutation[][] cosets = new Permutation[coset_reps.length][];
        for (i = 0; i < coset_reps.length; ++i) {
            cosets[i] = new Permutation[subgroup_elements.length];
            int j = 0;
            for (Permutation e : subgroup_elements)
                cosets[i][j++] = coset_reps[i].composition(e);

            for (j = 0; j < i; ++j)
                assertHaveNoIntersections(cosets[j], cosets[i]);
        }
    }

    @Test
    public void testLeftCosetRepresentative1() {
        Permutation gen0 = Permutations.createPermutation(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = Permutations.createPermutation(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1);
        int[] base = pg.getBase();
        InducedOrderingOfPermutations pordering = new InducedOrderingOfPermutations(base);
        int[] set = {3, 7};

        PermutationGroup stabilizer = testSearchStabilizerRaw(pg, set);

        Permutation[] coset_reps = AlgorithmsBacktrack.leftCosetRepresentatives(
                pg.getBSGS(), stabilizer.getBSGS());
        assertHaveNoNullElements(coset_reps);

        Permutation[] subgroup_elements = new Permutation[stabilizer.order().intValue()];
        int i = 0;
        for (Permutation p : stabilizer)
            subgroup_elements[i++] = p;

        Permutation[][] cosets = new Permutation[coset_reps.length][];
        for (i = 0; i < coset_reps.length; ++i) {
            cosets[i] = new Permutation[subgroup_elements.length];
            int j = 0;
            for (Permutation e : subgroup_elements)
                cosets[i][j++] = coset_reps[i].composition(e);

            for (j = 0; j < i; ++j)
                assertHaveNoIntersections(cosets[j], cosets[i]);

            Permutation rep = null;
            for (Permutation p : cosets[i]) {
                if (rep == null)
                    rep = AlgorithmsBacktrack.leftTransversalOf(p, pg.getBSGS(), stabilizer.getBSGS());
                else
                    assertTrue(
                            pordering.compare(rep, AlgorithmsBacktrack.leftTransversalOf(p, pg.getBSGS(), stabilizer.getBSGS())) == 0);
            }
        }
    }


    @Test
    public void testLeftCosetRepresentatives2() {
        Permutation gen0 = Permutations.createPermutation(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = Permutations.createPermutation(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1);
        int[] set = {3, 7};

        PermutationGroup stabilizer = testSearchStabilizerRaw(pg, set);

        Permutation[] coset_reps = AlgorithmsBacktrack.leftCosetRepresentatives(
                pg.getBSGS(), stabilizer.getBSGS());
        assertHaveNoNullElements(coset_reps);

        Permutation[] subgroup_elements = new Permutation[stabilizer.order().intValue()];
        int i = 0;
        for (Permutation p : stabilizer)
            subgroup_elements[i++] = p;

        Permutation[][] cosets = new Permutation[coset_reps.length][];
        for (i = 0; i < coset_reps.length; ++i) {
            cosets[i] = new Permutation[subgroup_elements.length];
            int j = 0;
            for (Permutation e : subgroup_elements)
                cosets[i][j++] = coset_reps[i].composition(e);

            for (j = 0; j < i; ++j)
                assertHaveNoIntersections(cosets[j], cosets[i]);
        }
    }

    @Test
    @TestWithGAP
    public void testLeftCosetRepresentatives3() {
        DescriptiveStatistics visited = new DescriptiveStatistics();
        int scanned = 0;
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            //System.out.println("DEGREE: " + degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                ++scanned;
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 7)
                    continue;

                PermutationGroup g = gap.primitiveGroup(degree, i);

                BigInteger index = NumberUtils.factorial(degree).divide(g.order());

                if (index.compareTo(BigInteger.valueOf(1_000_001)) > 0)
                    continue;

                ++scanned;

                ArrayList<BSGSElement> symmetricGroup = AlgorithmsBase.createSymmetricGroupBSGS(degree);
                Permutation[] transversals = AlgorithmsBacktrack.leftCosetRepresentatives(symmetricGroup, g.getBSGS());


                assertEquals(index.intValue(), transversals.length);
                assertHaveNoNullElements(transversals);
             /*
                System.out.println(
                        "Group order: " + calculateOrder(symmetricGroup) +
                                "  Subgroup order: " + calculateOrder(g.getBSGS()) +
                                "  Subgroup index: " + transversals.length +
                                "  Visited: " + AlgorithmsBacktrack.____VISITED_NODES___[0]);

               */
                visited.addValue((((double) AlgorithmsBacktrack.____VISITED_NODES___[0]) / g.order().doubleValue()) * 100.0);
            }
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of percent of visited nodes: ");
        System.out.println(visited);
    }

    @Test
    public void testIntersection1() {
        Permutation gen0 = Permutations.createPermutation(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = Permutations.createPermutation(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        List<BSGSElement> pg = PermutationGroup.createPermutationGroup(gen0, gen1).getBSGS();
        System.out.println(calculateOrder(pg));
        List<BSGSElement> sym = AlgorithmsBase.createSymmetricGroupBSGS(gen0.length());
        ArrayList<BSGSCandidateElement> intersection = new ArrayList<>();

        AlgorithmsBacktrack.intersection(pg, sym, intersection);
        System.out.println(intersection.get(0).stabilizerGenerators);
        System.out.println(calculateOrder(intersection));
    }

    @Test
    public void testIntersection2() {
        PermutationGroup sym = PermutationGroup.symmetricGroup(6);
        PermutationGroup alt = PermutationGroup.alternatingGroup(3);
        Assert.assertEquals(alt, sym.intersection(alt));
    }

    @Test
    public void testIntersection3() {
        //PermutationGroup g1 = gap.primitiveGroup(17, 5);
        //PermutationGroup g2 = gap.primitiveGroup(16, 17);

        PermutationGroup g1 = createPermutationGroup(
                createPermutation(new int[][]{{2, 12, 8, 9, 10, 6, 16, 4, 3, 11, 13, 5, 7, 15, 14}}),
                createPermutation(new int[][]{{0, 8, 1}, {2, 13, 5}, {3, 11, 10}, {6, 15, 9}, {12, 16, 14}}));
        PermutationGroup g2 = createPermutationGroup(
                createPermutation(new int[][]{{1, 15, 8, 4, 2}, {3, 14, 7, 12, 6}, {5, 13, 9, 11, 10}}),
                createPermutation(new int[][]{{1, 15}, {3, 13}, {5, 11}, {7, 9}}),
                createPermutation(new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 9}, {10, 11}, {12, 13}, {14, 15}}));

        PermutationGroup expected = createPermutationGroup(
                createPermutation(new int[][]{{0, 15}, {1, 14}, {2, 13}, {3, 12}, {4, 11}, {5, 10}, {6, 9}, {7, 8}}));

        assertTrue(g1.containsSubgroup(expected));
        assertTrue(g2.containsSubgroup(expected));

        assertEquals(expected, g1.intersection(g2));
        assertEquals(expected, g2.intersection(g1));
    }

    @Test
    @TestWithGAP
    @LongTest
    public void testIntersection5() {
        long redberryTiming = 0, gapTiming = 0, start;
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 4; degree < 25; ++degree) {
            //System.out.println("Degree: " + degree);
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g1:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g1);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g1);")) && degree > 7)
                    continue;
                PermutationGroup g1 = gap.primitiveGroup(degree, i);
                for (int d = 4; d < degree; ++d) {
                    int nrPG = gap.nrPrimitiveGroups(d);
                    for (int j = 0; j < nrPG; ++j) {
                        gap.evaluate("g2:= PrimitiveGroup( " + d + ", " + (j + 1) + ");");
                        if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g2);") ||
                                gap.evaluateToBoolean("IsNaturalAlternatingGroup(g2);")) && degree > 7)
                            continue;

                        PermutationGroup g2 = gap.primitiveGroup(d, j);
                        //initialize BSGS
                        g1.order();
                        g2.order();
                        gap.evaluate("Order(g1); Order(g2);");

                        start = System.currentTimeMillis();
                        PermutationGroup intersection = g1.intersection(g2);
                        redberryTiming += (System.currentTimeMillis() - start);

                        start = System.currentTimeMillis();
                        gap.evaluate("intr:= Intersection(g1,g2);");
                        gapTiming += (System.currentTimeMillis() - start);

                        gap.evaluateRedberryGroup("actual", intersection.generators());
                        boolean test = gap.evaluateToBoolean("actual = intr;");
                        if (!test) {
                            System.out.println("" + degree + "  " + d + ",  " + i + "  " + j);
                            System.out.println(gap.evaluate("Intersection(g1,g2);"));
                            System.out.println(intersection);
                        }
                        Assert.assertTrue(test);
                    }
                }
            }
        }
        System.out.println("Redberry timing: " + redberryTiming);
        System.out.println("GAP timing: " + gapTiming);
    }


    public static PermutationGroup testSearchStabilizerRaw(PermutationGroup pg, int[] set) {
        return testSearchStabilizerRaw(pg.getBSGS(), set);
    }

    public static PermutationGroup testSearchStabilizerRaw(List<? extends BSGSElement> bsgs, int[] set) {
        int degree = bsgs.get(0).internalDegree();
        int[] base = getBaseAsArray(bsgs);
        RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, base);

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), degree));
        subgroup.get(0).stabilizerGenerators.add(Permutations.createIdentityPermutation(degree));

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, rw, rw);

        ArrayList<Permutation> expected = new ArrayList<>(calculateOrder(subgroup).intValue());
        Iterator<Permutation> allIterator = PermutationGroup.createPermutationGroupFromBSGS(asBSGSList(bsgs)).iterator();
        Permutation c;
        while (allIterator.hasNext()) {
            c = allIterator.next();
            if (rw.is(c))
                expected.add(c);
        }

        ArrayList<Permutation> actual = new ArrayList<>(calculateOrder(subgroup).intValue());
        allIterator = PermutationGroup.createPermutationGroupFromBSGS(asBSGSList(subgroup)).iterator();
        while (allIterator.hasNext()) {
            c = allIterator.next();
            actual.add(c);
        }

        Collections.sort(expected);
        Collections.sort(actual);

        assertEquals(expected, actual);
        return PermutationGroup.createPermutationGroupFromBSGS(AlgorithmsBase.asBSGSList(subgroup));
    }

}
