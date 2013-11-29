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
import cc.redberry.core.groups.permutations.gap.GapPrimitiveGroupsReader;
import cc.redberry.core.utils.Indicator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getBaseAsArray;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getOrder;
import static cc.redberry.core.groups.permutations.PermutationsTestUtils.calculateRawSetwiseStabilizer;
import static cc.redberry.core.groups.permutations.PermutationsTestUtils.RawSetwiseStabilizerCriteria;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBacktrackTest {

    @Test
    public void test1() throws Exception {

        Permutation gen0 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation gen1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation gen2 = new PermutationOneLine(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        final int point = 5;
        Indicator<Permutation> stabilizer = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(point) == point;
            }
        };
        BacktrackSearchTestFunction test = new BacktrackSearchTestFunction() {
            @Override
            public boolean test(Permutation permutation, int level) {
//                if (level == 0)
//                    return permutation.newIndexOf(point ) == point ;
                return true;
            }
        };

        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[gen0.degree()]));
        subgroup.get(0).stabilizerGenerators.add(gen0.getIdentity());

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, test, stabilizer);

        System.out.println("Is BSGS: " + AlgorithmsBase.isBSGS(subgroup));
        AlgorithmsBase.SchreierSimsAlgorithm(subgroup);
        BacktrackSearch all = new BacktrackSearch(subgroup);
        Permutation current;
        while ((current = all.take()) != null) {
            System.out.println(current);
        }
    }

    @Test
    public void test2() throws Exception {
        //no any pruning, just property test

        Permutation gen0 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation gen1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation gen2 = new PermutationOneLine(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        final int point = 5;
        Indicator<Permutation> stabilizer = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(point) == point;
            }
        };
        BacktrackSearchTestFunction test = BacktrackSearchTestFunction.TRUE;

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[gen0.degree()]));
        subgroup.get(0).stabilizerGenerators.add(gen0.getIdentity());

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, test, stabilizer);

        int[] base;
        System.out.println("Subgroup BSGS complete: " + AlgorithmsBase.isBSGS(subgroup));
        System.out.println("Subgroup base: " + Arrays.toString(AlgorithmsBase.getBaseAsArray(subgroup)));
        AlgorithmsBase.removeRedundantBaseRemnant(subgroup);
        System.out.println("Subgroup real base: " + Arrays.toString(base = AlgorithmsBase.getBaseAsArray(subgroup)));
        System.out.println("Subgroup generators: " + subgroup.get(0).stabilizerGenerators);

        Permutation a0 = new PermutationOneLine(0, 1, 2, 3, 4, 5);
        Permutation a1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation a2 = new PermutationOneLine(1, 0, 3, 2, 4, 5);
        Permutation a3 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation a4 = new PermutationOneLine(2, 1, 0, 3, 4, 5);
        Permutation a5 = new PermutationOneLine(2, 3, 0, 1, 4, 5);
        Permutation a6 = new PermutationOneLine(3, 0, 1, 2, 4, 5);
        Permutation a7 = new PermutationOneLine(3, 2, 1, 0, 4, 5);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7};

        Comparator<Permutation> permutationComparator = new InducedOrderingOfPermutations(base, gen0.degree());
        Arrays.sort(expected, permutationComparator);

        AlgorithmsBase.SchreierSimsAlgorithm(subgroup);
        System.out.println("Subgroup order: " + AlgorithmsBase.getOrder(subgroup));

        Permutation[] actual = new Permutation[expected.length];
        BacktrackSearch search = new BacktrackSearch(subgroup);
        Permutation current;
        int ii = 0;
        while ((current = search.take()) != null) {
            actual[ii++] = current;
        }
        assertEquals(expected.length, ii);
        Arrays.sort(actual, permutationComparator);
        Assert.assertArrayEquals(expected, actual);
    }

    @Test
    public void testSetwiseStabilizer1_raw() throws Exception {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        int scanned = 0;
        for (int i = 0; i < pgs.length; ++i) {
            if (pgs[i].order().compareTo(BigInteger.valueOf(100000)) > 0)
                continue;

            ++scanned;

            int degree = pgs[i].degree();

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
            PermutationGroup stabilizer = testSearchStabilizerRaw(pgs[i], set);
            statistics.addValue(stabilizer.order().intValue());
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of stabilizer orders: ");
        System.out.println(statistics);
    }


    @Test
    public void testSetwiseStabilizer2_raw_visited_nodes_stat() throws Exception {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");
        System.out.println("Read groups from GAP.");

        DescriptiveStatistics orders = new DescriptiveStatistics();
        DescriptiveStatistics visited = new DescriptiveStatistics();
        int scanned = 0;
        for (int i = 0; i < pgs.length; ++i) {
            if (pgs[i].order().compareTo(BigInteger.valueOf(100000)) > 0)
                continue;

            ++scanned;

            int degree = pgs[i].degree();

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

            int[] set = Combinatorics.getRandomSortedDistinctArray(0, degree,
                    setl,
                    CC.getRandomGenerator());

            PermutationGroup stabilizer = testSearchStabilizerRaw(pgs[i], set);
            System.out.println("Group order: " + pgs[i].order() + "  Visited: " + AlgorithmsBacktrack.____VISITED_NODES___[0] + "  Stabilizer order: " + stabilizer.order());
            visited.addValue((((double) AlgorithmsBacktrack.____VISITED_NODES___[0]) / pgs[i].order().doubleValue()) * 100.0);
            orders.addValue(stabilizer.order().intValue());
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of stabilizer orders: ");
        System.out.println(orders);
        System.out.println("Statistic of percent of visited nodes: ");
        System.out.println(visited);
    }

    @Test
    public void testSetwiseStabilizer3_raw_all_set() throws Exception {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");

        DescriptiveStatistics visited = new DescriptiveStatistics();
        int scanned = 0;
        for (int i = 0; i < pgs.length; ++i) {
            ++scanned;

            int degree = pgs[i].degree();

            int[] set = new int[degree];
            for (int g = 1; g < degree; ++g)
                set[g] = g;

            PermutationGroup stabilizer = calculateRawSetwiseStabilizer(pgs[i], set);
            assertEquals(pgs[i].order(), stabilizer.order());

            System.out.println("Group order: " + pgs[i].order() + "  Visited: " + AlgorithmsBacktrack.____VISITED_NODES___[0]);

            visited.addValue((((double) AlgorithmsBacktrack.____VISITED_NODES___[0]) / pgs[i].order().doubleValue()) * 100.0);
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);
        System.out.println("Statistic of percent of visited nodes: ");
        System.out.println(visited);
    }

    @Test
    public void testSetwiseStabilizer1a_raw() throws Exception {
        Permutation gen0 = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1);

        int[] set = {4, 9};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testSetwiseStabilizer1b_raw() throws Exception {
        Permutation gen0 = new PermutationOneLine(1, 2, 0, 4, 5, 3, 7, 8, 6, 9);
        Permutation gen1 = new PermutationOneLine(0, 1, 2, 6, 7, 8, 3, 4, 5, 9);
        Permutation gen2 = new PermutationOneLine(0, 5, 7, 8, 1, 3, 4, 6, 2, 9);
        Permutation gen3 = new PermutationOneLine(9, 1, 2, 6, 5, 4, 3, 8, 7, 0);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1, gen2, gen3);
        int[] set = {0, 3};

        testSearchStabilizerRaw(pg, set);
    }

    @Test
    public void testSetwiseStabilizer1c_raw() throws Exception {
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1);
        int[] set = {3, 7};

        testSearchStabilizerRaw(pg, set);
    }


    @Test
    public void testCosetRepresentatives1() {
        Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
        Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1);
        int[] set = {3, 7};

        PermutationGroup stabilizer = testSearchStabilizerRaw(pg, set);
        System.out.println(pg.order());
        System.out.println(stabilizer.order());

        Permutation[] coset = AlgorithmsBacktrack.cosetRepresentatives(
                pg.getBSGS().getBSGSCandidateList(), stabilizer.getBSGS().getBSGSCandidateList());

        System.out.println(Arrays.toString(coset));
        int index = notNullSize(coset);
        System.out.println(index);
        for (int i = 0; i < index; ++i)
            System.out.println(stabilizer.isMember(coset[i]));
    }

    public static PermutationGroup testSearchStabilizerRaw(PermutationGroup pg, int[] set) {
        List<BSGSElement> bsgs = pg.getBSGS().getBSGSList();
        int degree = pg.degree();
        int[] base = getBaseAsArray(bsgs);
        RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, base);

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[degree]));
        subgroup.get(0).stabilizerGenerators.add(Permutations.getIdentityOneLine(degree));

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, rw, rw);

        ArrayList<Permutation> expected = new ArrayList<>(getOrder(subgroup).intValue());
        Iterator<Permutation> allIterator = new BaseAndStrongGeneratingSet.PermIterator(bsgs);
        Permutation c;
        while (allIterator.hasNext()) {
            c = allIterator.next();
            if (rw.is(c))
                expected.add(c);
        }

        ArrayList<Permutation> actual = new ArrayList<>(getOrder(subgroup).intValue());
        allIterator = new BaseAndStrongGeneratingSet.PermIterator(subgroup);
        while (allIterator.hasNext()) {
            c = allIterator.next();
            actual.add(c);
        }

        Collections.sort(expected);
        Collections.sort(actual);

        assertEquals(expected, actual);
        return new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(subgroup)));
    }


    private static int notNullSize(Object[] array) {
        int s = 0;
        for (Object o : array)
            if (o != null)
                ++s;
        return s;
    }
}
