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

import cc.redberry.core.context.CC;
import cc.redberry.core.groups.permutations.gap.GapPrimitiveGroupsReader;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.groups.permutations.RandomPermutation.random;
import static cc.redberry.core.groups.permutations.RandomPermutation.randomness;
import static cc.redberry.core.utils.Timing.TimingJob;
import static cc.redberry.core.utils.Timing.timing;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBaseTest {

    @Test
    public void testRemoveRedundant0() {
        long seed = currentTimeMillis();
        int n = 20;
        int COUNT = 1000;
        DescriptiveStatistics removed = new DescriptiveStatistics();
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(new PermutationOneLine(Permutations.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        ArrayList<BSGSCandidateElement> bsgs;
        List<Permutation> generators = new ArrayList<>();
        for (int tt = 0; tt < COUNT; ++tt) {
            generators.clear();
            for (int i = 0; i < 1 + randomGenerator.nextInt(7); ++i)
                generators.add(random(source, randomGenerator));

            //create BSGS
            bsgs = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            SchreierSimsAlgorithm(bsgs);
            long in = numOfGenerators(bsgs);
            //remove redundant
            removeRedundantGenerators(bsgs);
            removed.addValue(in - numOfGenerators(bsgs));
            //check!
            assertTrue(isBSGS(bsgs));
        }
        System.out.println("Removed strong generators statistics:");
        System.out.println(removed);
    }

    @Test
    public void testRemoveRedundant0a() {
        for (int i = 0; i < 1000; ++i) {
            List<Permutation> generators = new ArrayList<>();
            generators.add(new PermutationOneLine(Permutations.randomPermutation(5)));
            generators.add(new PermutationOneLine(Permutations.randomPermutation(5)));
            ArrayList<BSGSCandidateElement> bsgs;
            //create BSGS
            bsgs = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            SchreierSimsAlgorithm(bsgs);
            //remove redundant
            removeRedundantGenerators(bsgs);
            //check!
            assertTrue(isBSGS(bsgs));
        }

    }

    @Test
    public void testRemoveRedundant0b() {
        List<Permutation> generators = new ArrayList<>();
        generators.add(new PermutationOneLine(0, 2, 1, 3, 4));
        generators.add(new PermutationOneLine(3, 2, 4, 0, 1));

        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(generators);

        ArrayList<BSGSCandidateElement> bsgs
                = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));

        SchreierSimsAlgorithm(bsgs);
        long rem = numOfGenerators(bsgs);
        //remove redundant
        removeRedundantGenerators(bsgs);
        System.out.println("Removed: " + (rem - numOfGenerators(bsgs)));

        //check!
        assertTrue(isBSGS(bsgs));
    }

    @Test
    public void testRemoveRedundant1() {
        final int COUNT = 50;//100;
        testRemoveRedundant(COUNT, false, false);
    }

    @Test
    public void testRemoveRedundant2() {
        final int COUNT = 50;//100;
        testRemoveRedundant(COUNT, true, false);
    }

    @Test
    public void testRemoveRedundant3() {
        final int COUNT = 50;//100;
        testRemoveRedundant(COUNT, true, true);
    }

    private static void testRemoveRedundant(int COUNT, boolean applySchreierSims, boolean bringRandomness) {
        DescriptiveStatistics removed = new DescriptiveStatistics();
        Random r = new Random();
        for (int tt = 0; tt < COUNT; ++tt) {
            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 20 + r.nextInt(6); ++i)
                source.add(new PermutationOneLine(Permutations.randomPermutation(20)));
            if (bringRandomness)
                randomness(source);

            ArrayList<BSGSCandidateElement> BSGSCandidate =
                    new ArrayList<>(createRawBSGSCandidate(source.toArray(new Permutation[0])));
            if (applySchreierSims)
                SchreierSimsAlgorithm(BSGSCandidate);

            long before = numOfGenerators(BSGSCandidate);

            ArrayList<BSGSCandidateElement> BSGSCandidateCopy = AlgorithmsBase.clone(BSGSCandidate);
            AlgorithmsBase.removeRedundantGenerators(BSGSCandidateCopy);
            long after = numOfGenerators(BSGSCandidateCopy);

            removed.addValue(before - after);

            //make sure that this is same BSGS
            SchreierSimsAlgorithm(BSGSCandidate);
            SchreierSimsAlgorithm(BSGSCandidateCopy);

            assertSameGroups(
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate))),
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidateCopy))));
        }
        System.out.println(removed);
    }

    private static long numOfGenerators(List<? extends BSGSElement> BSGS) {
        long num = 0;
        for (BSGSElement el : BSGS)
            num += el.stabilizerGenerators.size();
        return num;
    }


    @Test
    public void testRandomSchreierSimsWithOrder1() {
        long seed = currentTimeMillis();
        int n = 20;
        int COUNT = 1000;
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(new PermutationOneLine(Permutations.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        ArrayList<BSGSCandidateElement> bsgs1, bsgs2;
        List<Permutation> generators = new ArrayList<>();
        for (int tt = 0; tt < COUNT; ++tt) {
            generators.clear();
            for (int i = 0; i < 1 + randomGenerator.nextInt(7); ++i)
                generators.add(random(source, randomGenerator));

            bsgs1 = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            bsgs2 = AlgorithmsBase.clone(bsgs1);

            SchreierSimsAlgorithm(bsgs1);
            BigInteger order = getOrder(bsgs1);

            RandomSchreierSimsAlgorithmForKnownOrder(bsgs2, order, randomGenerator);
            removeRedundantGenerators(bsgs2);
            assertTrue(isBSGS(bsgs2));
        }
    }

    @Test
    public void testSchreierSims1() {
        Permutation a = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0, 7),
                b = new PermutationOneLine(0, 2, 4, 6, 1, 3, 5, 7),
                c = new PermutationOneLine(7, 6, 3, 2, 5, 4, 1, 0);

        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b, c);
        SchreierSimsAlgorithm(bsgs);
        removeRedundantBaseRemnant(bsgs);
        assertEquals(3, bsgs.size());
        assertTrue(isBSGS(bsgs));
    }

    @Test
    public void testSchreierSims2() {
        PermutationOneLine a = new PermutationOneLine(0, 1, 2, 3, 4, 5),
                b = new PermutationOneLine(0, 3, 2, 1, 4, 5),
                c = new PermutationOneLine(2, 1, 0, 3, 4, 5);
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b, c);
        SchreierSimsAlgorithm(bsgs);
        int order = getOrder(bsgs).intValue();

        int _order_ = 0;
        BruteForcePermutationIterator it = new BruteForcePermutationIterator(Arrays.asList(a, b, c));
        while (it.hasNext()) {
            ++_order_;
            it.next();
        }
        assertEquals(_order_, order);
    }

//////////////////////////////////////////// BASE CHANGE ////////////////////////////////////////////////////////////

    @Test
    public void testBaseSwap1() {
        long seed = currentTimeMillis();
        int n = 15;
        int COUNT = 10;
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(new PermutationOneLine(Permutations.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        ArrayList<BSGSCandidateElement> bsgs1, bsgs2;
        List<Permutation> generators = new ArrayList<>();
        for (int tt = 0; tt < COUNT; ++tt) {
            System.out.println(tt);
            generators.clear();
            for (int i = 0; i < 1 + randomGenerator.nextInt(7); ++i)
                generators.add(random(source, randomGenerator));

            bsgs1 = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            SchreierSimsAlgorithm(bsgs1);
            for (int i = 0; i < bsgs1.size() - 1; ++i) {
                bsgs2 = AlgorithmsBase.clone(bsgs1);
                swapAdjacentBasePoints(bsgs2, i);
                assertTrue(isBSGS(bsgs2));
                int[] p = Permutations.randomPermutation(n, randomGenerator);
                for (int pp : p) {
                    if (pp > bsgs2.size() - 2)
                        continue;
                    swapAdjacentBasePoints(bsgs2, pp);
                    assertTrue(isBSGS(bsgs2));
                }
                for (int pp : p) {
                    if (pp > bsgs2.size() - 2)
                        continue;
                    swapAdjacentBasePoints(bsgs2, pp);
                    assertTrue(isBSGS(bsgs2));
                }
            }
        }
    }

    @Test
    public void testBaseSwap1a() {
        List<Permutation> generators = new ArrayList<>();
        generators.add(new PermutationOneLine(0, 2, 1, 3, 4));
        generators.add(new PermutationOneLine(3, 2, 4, 0, 1));
        ArrayList<BSGSCandidateElement> bsgs
                = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
        SchreierSimsAlgorithm(bsgs);
        ArrayList<BSGSCandidateElement> bsgs1;

        for (int i = 0; i < bsgs.size() - 1; ++i) {
            System.out.println(i);
            bsgs1 = AlgorithmsBase.clone(bsgs);
            swapAdjacentBasePoints(bsgs1, i);
            assertTrue(isBSGS(bsgs1));
        }
    }

    @Test
    public void testBaseSwap2() {
        Permutation a = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
        SchreierSimsAlgorithm(bsgs);
        assertEquals(getOrder(bsgs).intValue(), 120);

        //0,1,2
        int[] oldBase = {0, 1, 2};
        assertArrayEquals(oldBase, getBaseAsArray(bsgs));
        //1,2,0
        int[] newBase = {1, 2, 0};

        //1,0,2
        swapAdjacentBasePoints(bsgs, 0);
        //1,2,0
        swapAdjacentBasePoints(bsgs, 1);
        assertArrayEquals(newBase, getBaseAsArray(bsgs));
    }

    @Test
    public void testBaseSwap3() {
        Permutation a = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        int degree = a.degree();
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
        SchreierSimsAlgorithm(bsgs);
        assertEquals(getOrder(bsgs).intValue(), 120);

        //0,1,2
        int[] oldBase = {0, 1, 2};
        assertArrayEquals(oldBase, getBaseAsArray(bsgs));
        bsgs.add(new BSGSCandidateElement(2, new ArrayList<Permutation>(), new int[degree]));
        assertTrue(isBSGS(bsgs));
        swapAdjacentBasePoints(bsgs, 2);
        assertTrue(isBSGS(bsgs));
        assertEquals(0, bsgs.get(3).stabilizerGenerators.size());
    }

    @Test
    public void testBaseSwap4_redundant_points() {
        Permutation a = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        int degree = a.degree();
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
        SchreierSimsAlgorithm(bsgs);
        assertEquals(getOrder(bsgs).intValue(), 120);
        //real base: 0,1,2
        int[] oldBase = {0, 1, 2};

        //add redundant point 5
        bsgs.add(new BSGSCandidateElement(5, new ArrayList<Permutation>(), new int[degree]));
        assertTrue(isBSGS(bsgs));
        SchreierSimsAlgorithm(bsgs);
        assertTrue(isBSGS(bsgs));
        assertArrayEquals(new int[]{0, 1, 2, 5}, getBaseAsArray(bsgs));

        //add redundant point 3
        bsgs.add(new BSGSCandidateElement(3, new ArrayList<Permutation>(), new int[degree]));
        assertTrue(isBSGS(bsgs));
        SchreierSimsAlgorithm(bsgs);
        assertTrue(isBSGS(bsgs));
        assertArrayEquals(new int[]{0, 1, 2, 5, 3}, getBaseAsArray(bsgs));

        //add redundant point 6
        bsgs.add(new BSGSCandidateElement(6, new ArrayList<Permutation>(), new int[degree]));
        assertTrue(isBSGS(bsgs));
        SchreierSimsAlgorithm(bsgs);
        assertTrue(isBSGS(bsgs));
        assertArrayEquals(new int[]{0, 1, 2, 5, 3, 6}, getBaseAsArray(bsgs));

        //swap redundant points
        swapAdjacentBasePoints(bsgs, 3);
        assertTrue(isBSGS(bsgs));

        for (int i = bsgs.size() - 2; i >= 0; --i) {
            ArrayList<BSGSCandidateElement> copy = AlgorithmsBase.clone(bsgs);
            swapAdjacentBasePoints(copy, i);
            assertTrue(isBSGS(copy));
        }

        for (int i = bsgs.size() - 2; i >= 0; --i) {
            swapAdjacentBasePoints(bsgs, i);
            assertTrue(isBSGS(bsgs));
        }

        for (int i = 0; i <= 100; ++i) {
            swapAdjacentBasePoints(bsgs, CC.getRandomGenerator().nextInt(bsgs.size() - 2));
            assertTrue(isBSGS(bsgs));
        }
    }

//    @Test
//    public void testRebaseWithTranspositions1() {
//        Permutation a = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
//                b = new PermutationOneLine(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
//        int degree = a.length();
//
//        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
//        SchreierSimsAlgorithm(bsgs);
//        assertEquals(getOrder(bsgs).intValue(), 120);
//
//        //0,1,2
//        int[] oldBase = {0, 1, 2};
//        assertArrayEquals(oldBase, getBaseAsArray(bsgs));
//        //1,2,0
//        int[] newBase = {1, 2, 0};
//
//
//        int i = 0;
//        int newBasePoint = newBase[i];// =  1;
//        int j = 0;
//        out:
//        for (; j < bsgs.size(); ++j) {
//            for (Permutation p : bsgs.get(j).stabilizerGenerators)
//                if (p.newIndexOf(newBasePoint) != newBasePoint)
//                    continue out;
//            break;
//        }
//        //add new redundant point
//        assertEquals(2, j);
//        bsgs.add(j, new BSGSCandidateElement(newBasePoint, new ArrayList<Permutation>(), new int[degree]));
//        --j;
//        //base now: 0,1,2,1
//        while (j > i)
//            swapAdjacentBasePoints(bsgs, --j);
//        //base now: 1,0,1,2
//
//        assertArrayEquals(getBaseAsArray(bsgs), new int[]{1, 0, 1, 2});
//        assertTrue(isBSGS(bsgs));
//
//        i = 1;
//        newBasePoint = newBase[i];// = 2;
//        j = 0;
//        out:
//        for (; j < bsgs.size(); ++j) {
//            for (Permutation p : bsgs.get(j).stabilizerGenerators)
//                if (p.newIndexOf(newBasePoint) != newBasePoint)
//                    continue out;
//            break;
//        }
//        --j;
//
//        assertEquals(2, j);
//    }

    @Test
    public void testRebaseWithTranspositions2() {

        PermutationGroup pg = GapPrimitiveGroupsReader.readGroupFromGap("/home/stas/gap4r6/prim/grps/gps1.g", 6);
        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGS().getBSGSCandidateList();
        final int[] newBase = {1, 2, 0};


        timing(
                new TimingJob() {
                    @Override
                    public Object doJob() {
                        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);
                        return null;
                    }
                });


        assertTrue(isBSGS(bsgs));
        assertArrayEquals(newBase, getBaseAsArray(bsgs));
    }

    @Test
    public void testRebaseWithTranspositions3() {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");
        DescriptiveStatistics timings = new DescriptiveStatistics();
        for (int i = 0; i < pgs.length; ++i) {
            final ArrayList<BSGSCandidateElement> bsgs = pgs[i].getBSGS().getBSGSCandidateList();
            Object[] r = timing(
                    new TimingJob() {
                        @Override
                        public Object doJob() {
                            for (int i = 0; i < 50; ++i) {
                                int[] oldBase = getBaseAsArray(bsgs);
                                int[] newBase = new PermutationOneLine(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                                AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);
                                assertTrue(isBSGS(bsgs));
                                final int[] _newBase = getBaseAsArray(bsgs);
                                for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                                    assertEquals(newBase[r], _newBase[r]);
                            }
                            return null;
                        }
                    });
            timings.addValue((Long) r[0]);
        }
        System.out.println(timings);
        //DescriptiveStatistics:
        //n: 372
        //min: 3.0
        //max: 5834.0
        //mean: 384.02150537634253
        //std dev: 768.4659760910612
        //median: 172.5
        //skewness: 4.603626801745278
        //kurtosis: 24.35082606084427
    }

    @Test
    public void testRebaseWithTranspositions3a() {
        Permutation a = new PermutationOneLine(6, 7, 8, 9, 10, 0, 11, 12, 13, 14, 1, 15, 16, 17, 2, 18, 19, 3, 20, 4, 5),
                b = new PermutationOneLine(0, 13, 6, 20, 10, 8, 11, 1, 4, 3, 15, 12, 18, 17, 9, 5, 14, 19, 2, 7, 16),
                c = new PermutationOneLine(0, 9, 12, 10, 17, 5, 6, 14, 16, 1, 3, 11, 2, 19, 7, 15, 8, 4, 18, 13, 20);
        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(a, b, c);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGS().getBSGSCandidateList();
        final int[] newBase = {3, 4, 0, 1, 2};
        timing(
                new TimingJob() {
                    @Override
                    public Object doJob() {
                        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);
                        return null;
                    }
                });


        assertTrue(isBSGS(bsgs));
        assertTrue(getBaseAsArray(bsgs).length < newBase.length);
    }

    @Test
    public void testRebaseWithTranspositions3b() {
        Permutation gen0 = new PermutationOneLine(0, 5, 10, 15, 20, 1, 6, 11, 16, 21, 2, 7, 12, 17, 22, 3, 8, 13, 18, 23, 4, 9, 14, 19, 24);
        Permutation gen1 = new PermutationOneLine(5, 6, 8, 9, 7, 10, 11, 13, 14, 12, 15, 16, 18, 19, 17, 20, 21, 23, 24, 22, 0, 1, 3, 4, 2);
        Permutation gen2 = new PermutationOneLine(1, 2, 3, 4, 0, 6, 7, 8, 9, 5, 16, 17, 18, 19, 15, 21, 22, 23, 24, 20, 11, 12, 13, 14, 10);
        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1, gen2);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGS().getBSGSCandidateList();
        int[] oldBase = {0, 1, 2, 3, 4, 5, 7, 8, 9, 10};
        final int[] newBase = {0, 1, 7, 5, 10, 8, 3, 9, 2, 4};

        timing(
                new TimingJob() {
                    @Override
                    public Object doJob() {
                        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);
                        return null;
                    }
                });


        assertTrue(isBSGS(bsgs));
        int[] _newBase = getBaseAsArray(bsgs);//[0, 1, 7, 5, 10]

        for (int i = 0; i < _newBase.length; ++i)
            assertEquals(_newBase[i], newBase[i]);
    }


    @Test
    public void testRebaseWithTranspositions3c() {
        //PermutationGroup pg = GapPrimitiveGroupsReader.readGroupFromGap("/home/stas/gap4r6/prim/grps/gps1.g", 41);

        Permutation gen0 = new PermutationOneLine(5, 6, 8, 9, 7, 10, 11, 13, 14, 12, 15, 16, 18, 19, 17, 20, 21, 23, 24, 22, 0, 1, 3, 4, 2);
        Permutation gen1 = new PermutationOneLine(1, 2, 3, 4, 0, 6, 7, 8, 9, 5, 16, 17, 18, 19, 15, 21, 22, 23, 24, 20, 11, 12, 13, 14, 10);
        Permutation gen2 = new PermutationOneLine(0, 5, 10, 20, 15, 1, 6, 11, 21, 16, 2, 7, 12, 22, 17, 3, 8, 13, 23, 18, 4, 9, 14, 24, 19);
        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1, gen2);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGS().getBSGSCandidateList();
        int[] oldBase = {1, 0, 2, 3, 5, 10};
        final int[] newBase = {2, 3, 10, 0, 5, 1};

        timing(
                new TimingJob() {
                    @Override
                    public Object doJob() {
                        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);
                        return null;
                    }
                });

        int[] _newBase = getBaseAsArray(bsgs);//[0, 1, 7, 5, 10]
        System.out.println(Arrays.toString(_newBase));

        assertTrue(isBSGS(bsgs));

        for (int i = 0; i < _newBase.length; ++i)
            assertEquals(_newBase[i], newBase[i]);
    }

    @Test
    public void rebaseWithConjugationAndTranspositions1() {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");

        DescriptiveStatistics timings = new DescriptiveStatistics();
        for (int i = 0; i < pgs.length; ++i) {
            final ArrayList<BSGSCandidateElement> bsgs = pgs[i].getBSGS().getBSGSCandidateList();
            Object[] r = timing(
                    new TimingJob() {
                        @Override
                        public Object doJob() {
                            for (int i = 0; i < 50; ++i) {
                                int[] oldBase = getBaseAsArray(bsgs);
                                int[] newBase = new PermutationOneLine(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                                rebaseWithConjugationAndTranspositions(bsgs, newBase);
                                assertTrue(isBSGS(bsgs));
                                final int[] _newBase = getBaseAsArray(bsgs);
                                for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                                    assertEquals(newBase[r], _newBase[r]);
                            }
                            return null;
                        }
                    });
            timings.addValue((Long) r[0]);
        }
        System.out.println(timings);
        //DescriptiveStatistics:
        //n: 372
        //min: 3.0
        //max: 3855.0
        //mean: 286.7446236559146
        //std dev: 533.389150452157
        //median: 151.0
        //skewness: 4.336006566090981
        //kurtosis: 21.27268547404619
    }

    @Test
    public void rebaseWithConjugationAndTranspositions1a() {
        //PermutationGroup pg = GapPrimitiveGroupsReader.readGroupFromGap("/home/stas/gap4r6/prim/grps/gps1.g", 1);
        Permutation gen0 = new PermutationOneLine(1, 2, 3, 4, 5, 6, 0, 7);
        Permutation gen1 = new PermutationOneLine(0, 2, 4, 6, 1, 3, 5, 7);
        Permutation gen2 = new PermutationOneLine(7, 6, 3, 2, 5, 4, 1, 0);
        PermutationGroup pg = PermutationGroupFactory.createPermutationGroup(gen0, gen1, gen2);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGS().getBSGSCandidateList();
        int[] oldBase = {0, 1, 2};
        int[] newBase = {2, 0, 1};

        rebaseWithConjugationAndTranspositions(bsgs, newBase);

        assertTrue(isBSGS(bsgs));
        final int[] _newBase = getBaseAsArray(bsgs);
        for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
            assertEquals(newBase[r], _newBase[r]);

    }

    @Test
    public void rebaseFromScratch1() {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");

        for (int i = 0; i < pgs.length; ++i) {
            final ArrayList<BSGSCandidateElement> bsgs = pgs[i].getBSGS().getBSGSCandidateList();
            timing(
                    new TimingJob() {
                        @Override
                        public Object doJob() {
                            for (int i = 0; i < 50; ++i) {
                                int[] oldBase = getBaseAsArray(bsgs);
                                int[] newBase = new PermutationOneLine(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                                rebaseFromScratch(bsgs, newBase);
                                assertTrue(isBSGS(bsgs));
                                final int[] _newBase = getBaseAsArray(bsgs);
                                for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                                    assertEquals(newBase[r], _newBase[r]);
                            }
                            return null;
                        }
                    });
        }
    }

    //---------------------------Performance tests------------------------------------//

    @Test
    public void testRandomSchreierSimsPerformance1() {
        int COUNT = 50;
        int n = 40;
        long seed = currentTimeMillis();

        burnJvm(seed);

        DescriptiveStatistics statSchreierSims = new DescriptiveStatistics(),
                statRandomSchreierSims = new DescriptiveStatistics(),
                statRandomSchreierSimsKnownOrder = new DescriptiveStatistics();

        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 2 + randomGenerator.nextInt(6); ++i)
            source.add(new PermutationOneLine(Permutations.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        int randomSchreierFails = 0;

        long start;
        for (int tt = 0; tt < COUNT; ++tt) {

            List<Permutation> generators = new ArrayList<>();
            for (int i = 0; i < 6; ++i)
                generators.add(random(source, randomGenerator));

            ArrayList<BSGSCandidateElement> BSGSCandidate1 =
                    (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0])),
                    BSGSCandidate2 = AlgorithmsBase.clone(BSGSCandidate1),
                    BSGSCandidate3 = AlgorithmsBase.clone(BSGSCandidate1);

            //--------------------------Schreier-Sims---------------------------------------//
            start = currentTimeMillis();
            SchreierSimsAlgorithm(BSGSCandidate1);
            statSchreierSims.addValue(currentTimeMillis() - start);
            BigInteger order = getOrder(BSGSCandidate1);

            //----------------------Random Schreier-Sims------------------------------------//
            start = currentTimeMillis();
            RandomSchreierSimsAlgorithm(BSGSCandidate2, 0.999, randomGenerator);
            if (!isBSGS(BSGSCandidate2)) {
                ++randomSchreierFails;
                SchreierSimsAlgorithm(BSGSCandidate2);
            }
            statRandomSchreierSims.addValue(currentTimeMillis() - start);

            //----------------Random Schreier-Sims with known order-------------------------//
            start = currentTimeMillis();
            RandomSchreierSimsAlgorithmForKnownOrder(BSGSCandidate3, order, randomGenerator);
            statRandomSchreierSimsKnownOrder.addValue(currentTimeMillis() - start);
            assertTrue(isBSGS(BSGSCandidate3));

            //------------------------------assertions-------------------------------------//
            assertSameGroups(
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate2))));

            assertSameGroups(
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate3))));
        }

        System.out.println("Schreier-sims:");
        System.out.println(statSchreierSims);

        System.out.println("Schreier-sims with randomness:");
        System.out.println(statRandomSchreierSims);
        System.out.println("Random Schreier-Sims produced not a real BSGS " + randomSchreierFails + " times.\n");

        System.out.println("Schreier-sims with randomness for known order:");
        System.out.println(statRandomSchreierSimsKnownOrder);
    }

    @Test
    public void testRandomSchreierSimsPerformance2() {
        int COUNT = 1;
        int n = 100;
        long seed = currentTimeMillis();

        burnJvm(seed);


        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 2 + randomGenerator.nextInt(6); ++i)
            source.add(new PermutationOneLine(Permutations.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        int randomSchreierFails = 0;
        double confidenceLevel = 1 - 1E-6;
        long start;
        for (int tt = 0; tt < COUNT; ++tt) {

            List<Permutation> generators = new ArrayList<>();
            for (int i = 0; i < 6; ++i)
                generators.add(random(source, randomGenerator));

            ArrayList<BSGSCandidateElement> BSGSCandidate1 =
                    (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0])),
                    BSGSCandidate2 = AlgorithmsBase.clone(BSGSCandidate1),
                    BSGSCandidate3 = AlgorithmsBase.clone(BSGSCandidate1);

            //--------------------------Schreier-Sims---------------------------------------//
            start = currentTimeMillis();
            SchreierSimsAlgorithm(BSGSCandidate1);
            System.out.println("Schreier-Sims :" + (currentTimeMillis() - start));
            BigInteger order = getOrder(BSGSCandidate1);

            //----------------------Random Schreier-Sims------------------------------------//
            System.out.println("Random Screier-Sims CL: " + (int) (-FastMath.log(2, 1 - confidenceLevel)));
            start = currentTimeMillis();
            RandomSchreierSimsAlgorithm(BSGSCandidate2, confidenceLevel, randomGenerator);
            System.out.println("Random Schreier-Sims :" + (currentTimeMillis() - start));
            removeRedundantGenerators(BSGSCandidate2);
            if (!isBSGS(BSGSCandidate2)) {
                ++randomSchreierFails;
                System.out.println("Not a BSGS.");
                SchreierSimsAlgorithm(BSGSCandidate2);
            }
            System.out.println("Random Schreier-Sims with check:" + (currentTimeMillis() - start));

            //----------------Random Schreier-Sims with known order-------------------------//
            start = currentTimeMillis();
            RandomSchreierSimsAlgorithmForKnownOrder(BSGSCandidate3, order, randomGenerator);
            System.out.println("Random Schreier-Sims with order:" + (currentTimeMillis() - start));
            removeRedundantGenerators(BSGSCandidate3);
            assertTrue(isBSGS(BSGSCandidate3));
            System.out.println("Random Schreier-Sims with order with check:" + (currentTimeMillis() - start));

            System.out.println("Group order: " + order);

            //------------------------------assertions-------------------------------------//
            assertSameGroups(
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate2))));

            assertSameGroups(
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(BSGSCandidate3))));
        }
    }

    private static void burnJvm(long seed) {
        int n = 20;
        int COUNT = 1000;
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(new PermutationOneLine(Permutations.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        ArrayList<BSGSCandidateElement> bsgs;
        for (int tt = 0; tt < COUNT; ++tt) {

            List<Permutation> generators = new ArrayList<>();
            for (int i = 0; i < 6; ++i)
                generators.add(random(source, randomGenerator));

            SchreierSimsAlgorithm(bsgs = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0])));
            RandomSchreierSimsAlgorithm((ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0])), 0.99, randomGenerator);
            RandomSchreierSimsAlgorithmForKnownOrder(bsgs, getOrder(bsgs), randomGenerator);
        }
        System.out.println("JVM warmed up.");
    }


    private static void assertSameGroups(PermutationGroup p1, PermutationGroup p2) {
        assertTrue(p1.order().equals(p2.order()));
        for (Permutation a : p1.generators())
            assertTrue(p2.isMember(a));
        for (Permutation a : p2.generators())
            assertTrue(p1.isMember(a));
    }

    static void soutGenerators(List<Permutation> generators) {
        for (int i = 0; i < generators.size(); ++i) {
            String str = generators.get(i).toString();
            str = str.substring(2, str.length() - 1);
            System.out.println("Permutation gen" + i + " = new PermutationOneLine(" + str + ");");
        }
        System.out.print("\nPermutationGroup pg = PermutationGroupFactory.createPermutationGroup(");
        for (int i = 0; ; ++i) {
            System.out.print("gen" + i);
            if (i == generators.size() - 1) {
                System.out.print(");");
                break;
            }
            System.out.print(",");
        }
        System.out.println();
    }
}