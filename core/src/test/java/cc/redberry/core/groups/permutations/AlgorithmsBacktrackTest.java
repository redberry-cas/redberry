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
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getBaseAsArray;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.getOrder;
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
        AlgorithmsBase.removeRedundantBasePoints(subgroup);
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

        int scanned = 0;
        for (int i = 0; i < pgs.length; ++i) {
            if (pgs[i].order().compareTo(BigInteger.valueOf(100000)) > 0)
                continue;

            ++scanned;

            List<BSGSElement> bsgs = pgs[i].getBSGS().getBSGSList();
            int degree = bsgs.get(0).groupDegree();

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
//            System.out.println("Stabilizing set " + Arrays.toString(set));

            RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, getBaseAsArray(bsgs));

            //empty initial subgroup
            ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
            subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[degree]));
            subgroup.get(0).stabilizerGenerators.add(Permutations.getIdentityOneLine(degree));

            AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, rw, rw);


            int[] base;
//            System.out.println("Subgroup BSGS complete: " + AlgorithmsBase.isBSGS(subgroup));
//            System.out.println("Subgroup base: " + Arrays.toString(AlgorithmsBase.getBaseAsArray(subgroup)));
            AlgorithmsBase.removeRedundantBasePoints(subgroup);
//            System.out.println("Subgroup real base: " + Arrays.toString(base = AlgorithmsBase.getBaseAsArray(subgroup)));
//            System.out.println("Subgroup generators: " + subgroup.get(0).stabilizerGenerators);

            AlgorithmsBase.SchreierSimsAlgorithm(subgroup);
//            System.out.println("Subgroup order: " + AlgorithmsBase.getOrder(subgroup) + " (order of group = " + getOrder(bsgs) + ")");
//            System.out.println("\n\n");

            //brute-force find stabilizer elements
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

            Comparator<Permutation> comparator = new InducedOrderingOfPermutations(getBaseAsArray(bsgs), degree);
            Collections.sort(expected, comparator);
            Collections.sort(actual, comparator);
            try {
                assertEquals(expected, actual);
            } catch (AssertionError e) {
                System.out.println("\n\n\n/////////////////////////////");
                System.out.println("Generators");
                AlgorithmsBaseTest.soutGenerators(bsgs.get(0).stabilizerGenerators);
                System.out.println("set: " + Arrays.toString(set));
                System.out.println("/////////////////////////////\n\n\n");
            }
        }
        System.out.println("Total number of primitive groups scanned: " + scanned);

    }

    @Test
    public void testSetwiseStabilizer1a_raw() throws Exception {

        Permutation gen0 = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1);

        System.out.println(Arrays.toString(pg.getBSGS().getBaseArray()));

        int[] set = {4, 9};

        testSearchStabilizerRaw(pg, set);
    }

    public static void testSearchStabilizerRaw(PermutationGroup pg, int[] set) {
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
    }

//    /////////////////////////////
//    Generators
//    Permutation gen0 = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
//    Permutation gen1 = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
//
//    PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0,gen1);
//    set: [4, 9]
///////////////////////////////
//
//
//
//
//
//
///////////////////////////////
//    Generators
//    Permutation gen0 = new PermutationOneLine(1, 2, 0, 4, 5, 3, 7, 8, 6, 9);
//    Permutation gen1 = new PermutationOneLine(0, 1, 2, 6, 7, 8, 3, 4, 5, 9);
//    Permutation gen2 = new PermutationOneLine(0, 5, 7, 8, 1, 3, 4, 6, 2, 9);
//    Permutation gen3 = new PermutationOneLine(9, 1, 2, 6, 5, 4, 3, 8, 7, 0);
//
//    PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0,gen1,gen2,gen3);
//    set: [0, 3]
///////////////////////////////
//
//
//
//
//
//
///////////////////////////////
//    Generators
//    Permutation gen0 = new PermutationOneLine(4, 3, 9, 1, 0, 5, 10, 7, 8, 2, 6);
//    Permutation gen1 = new PermutationOneLine(0, 1, 10, 6, 2, 7, 8, 9, 3, 5, 4);
//
//    PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0,gen1);
//    set: [3, 7]
///////////////////////////////
//
//
//
//
//
//
///////////////////////////////
//    Generators
//    Permutation gen0 = new PermutationOneLine(6, 7, 8, 9, 10, 0, 11, 12, 13, 14, 1, 15, 16, 17, 2, 18, 19, 3, 20, 4, 5);
//    Permutation gen1 = new PermutationOneLine(0, 2, 1, 4, 5, 3, 7, 6, 9, 10, 8, 11, 16, 17, 15, 13, 14, 12, 20, 18, 19);
//
//    PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0,gen1);
//    set: [0, 1, 2, 3, 4, 8]
///////////////////////////////
//
//
//
//
//
//
///////////////////////////////
//    Generators
//    Permutation gen0 = new PermutationOneLine(0, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 11, 19, 20, 12, 21, 13, 14, 22, 23, 15, 16, 24, 17, 25, 26, 18);
//    Permutation gen1 = new PermutationOneLine(11, 1, 18, 3, 22, 26, 9, 25, 7, 5, 24, 12, 13, 14, 0, 2, 15, 16, 17, 4, 19, 20, 21, 6, 8, 10, 23);
//    Permutation gen2 = new PermutationOneLine(0, 1, 2, 3, 4, 5, 6, 9, 10, 7, 8, 11, 12, 14, 13, 16, 15, 17, 18, 20, 19, 21, 22, 23, 24, 26, 25);
//
//    PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0,gen1,gen2);
//    set: [5, 7, 10, 11, 14, 16, 17, 19]
///////////////////////////////


}
