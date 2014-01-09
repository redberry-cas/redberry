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

import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.utils.Indicator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.groups.permutations.PermutationsTestUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBacktrackTest extends AbstractTestClass {

    @Test
    public void test1() {
        //no any pruning, just property test
        Permutation
                a = new PermutationOneLine(1, 2, 3, 0, 4, 5),
                b = new PermutationOneLine(0, 3, 2, 1, 4, 5),
                c = new PermutationOneLine(0, 1, 2, 3, 5, 4);
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
                a = new PermutationOneLine(1, 2, 3, 0, 4, 5),
                b = new PermutationOneLine(0, 3, 2, 1, 4, 5),
                c = new PermutationOneLine(0, 1, 2, 3, 5, 4);
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

        Permutation a0 = new PermutationOneLine(0, 1, 2, 3, 4, 5);
        Permutation a1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation a2 = new PermutationOneLine(1, 0, 3, 2, 4, 5);
        Permutation a3 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation a4 = new PermutationOneLine(2, 1, 0, 3, 4, 5);
        Permutation a5 = new PermutationOneLine(2, 3, 0, 1, 4, 5);
        Permutation a6 = new PermutationOneLine(3, 0, 1, 2, 4, 5);
        Permutation a7 = new PermutationOneLine(3, 2, 1, 0, 4, 5);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7};

        Comparator<Permutation> permutationComparator = new InducedOrderingOfPermutations(base, a.degree());
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
    public void testSetwiseStabilizer1_raw_WithGap_longtest() throws Exception {
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
                int[] set = Combinatorics.getRandomSortedDistinctArray(0, degree, nl, CC.getRandomGenerator());
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
    public void testSetwiseStabilizer2_raw_WithGap_PerformanceTest() throws Exception {

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

                int[] set = Combinatorics.getRandomSortedDistinctArray(0, degree,
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
    public void testSetwiseStabilizer2_raw_visited_nodes_stat_WithGap_PerformanceTest() throws Exception {
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
                int[] set = Combinatorics.getRandomSortedDistinctArray(0, degree,
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
    public void testSetwiseStabilizer3_raw_all_set_WithGap() throws Exception {
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
        Permutation gen0 = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        PermutationGroup pg = new PermutationGroup(gen0, gen1);

        int[] set = {4, 9};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testSetwiseStabilizer1b_raw() throws Exception {
        Permutation gen0 = new PermutationOneLine(1, 2, 0, 4, 5, 3, 7, 8, 6, 9);
        Permutation gen1 = new PermutationOneLine(0, 1, 2, 6, 7, 8, 3, 4, 5, 9);
        Permutation gen2 = new PermutationOneLine(0, 5, 7, 8, 1, 3, 4, 6, 2, 9);
        Permutation gen3 = new PermutationOneLine(9, 1, 2, 6, 5, 4, 3, 8, 7, 0);

        PermutationGroup pg = new PermutationGroup(gen0, gen1, gen2, gen3);
        int[] set = {0, 3};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testSetwiseStabilizer1c_raw() throws Exception {
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = new PermutationGroup(gen0, gen1);
        int[] set = {3, 7};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testLeftCosetRepresentatives1() {
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = new PermutationGroup(gen0, gen1);
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
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = new PermutationGroup(gen0, gen1);
        int[] base = pg.getBase();
        InducedOrderingOfPermutations pordering = new InducedOrderingOfPermutations(base, pg.degree());
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
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = new PermutationGroup(gen0, gen1);
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
    public void testLeftCosetRepresentatives3_WithGap() {
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
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        List<BSGSElement> pg = new PermutationGroup(gen0, gen1).getBSGS();
        System.out.println(calculateOrder(pg));
        List<BSGSElement> sym = AlgorithmsBase.createSymmetricGroupBSGS(gen0.degree());
        ArrayList<BSGSCandidateElement> intersection = new ArrayList<>();

        AlgorithmsBacktrack.intersection(pg, sym, intersection);
        System.out.println(intersection.get(0).stabilizerGenerators);
        System.out.println(calculateOrder(intersection));
    }

    public static PermutationGroup testSearchStabilizerRaw(PermutationGroup pg, int[] set) {
        return testSearchStabilizerRaw(pg.getBSGS(), set);
    }

    public static PermutationGroup testSearchStabilizerRaw(List<? extends BSGSElement> bsgs, int[] set) {
        int degree = bsgs.get(0).degree();
        int[] base = getBaseAsArray(bsgs);
        RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, base);

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[degree]));
        subgroup.get(0).stabilizerGenerators.add(Permutations.getIdentityOneLine(degree));

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, rw, rw);

        ArrayList<Permutation> expected = new ArrayList<>(calculateOrder(subgroup).intValue());
        Iterator<Permutation> allIterator = new PermutationGroup(asBSGSList(bsgs), true).iterator();
        Permutation c;
        while (allIterator.hasNext()) {
            c = allIterator.next();
            if (rw.is(c))
                expected.add(c);
        }

        ArrayList<Permutation> actual = new ArrayList<>(calculateOrder(subgroup).intValue());
        allIterator = new PermutationGroup(asBSGSList(subgroup), true).iterator();
        while (allIterator.hasNext()) {
            c = allIterator.next();
            actual.add(c);
        }

        Collections.sort(expected);
        Collections.sort(actual);

        assertEquals(expected, actual);
        return new PermutationGroup(AlgorithmsBase.asBSGSList(subgroup), true);
    }

}
