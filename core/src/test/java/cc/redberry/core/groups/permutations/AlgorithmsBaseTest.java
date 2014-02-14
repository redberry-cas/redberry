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

import cc.redberry.core.context.CC;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.utils.Timing;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.createRawBSGSCandidate;
import static cc.redberry.core.groups.permutations.RandomPermutation.random;
import static cc.redberry.core.groups.permutations.RandomPermutation.randomness;
import static cc.redberry.core.utils.Timing.timing;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBaseTest extends AbstractTestClass {

///////////////////////////////////////////// TEST FACTORIES ///////////////////////////////////////////////////////////

    @Test
    public void testCreateSymmetricGroup1_small_degree() throws Exception {
        for (int cc = 2; cc < 10; ++cc) {
            int degree = cc;
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);
            for (BSGSElement element : bsgs) {
                for (int i = 0; i < element.orbitSize(); ++i) {
                    assertEquals(element.orbitList.get(i),
                            element.getTransversalOf(element.orbitList.get(i)).newIndexOf(element.basePoint)
                    );
                }
            }
            assertTrue(isBSGS(bsgs));
            assertEquals(NumberUtils.factorial(degree), calculateOrder(bsgs));
        }
    }

    @Test
    public void testCreateSymmetricGroup2_small_degree() throws Exception {
        for (int degree = 2; degree < 15; ++degree) {
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);
            for (int i = 0; i < bsgs.size(); ++i) {
                PermutationGroup pg = PermutationGroup.createPermutationGroup(bsgs.get(i).stabilizerGenerators);
                assertEquals(NumberUtils.factorial(degree - i), pg.order());
            }
        }
    }


    @Test
    public void testCreateSymmetricGroup3_small_degree() throws Exception {
        for (int degree = 2; degree < 15; ++degree) {
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);
            for (int i = 0; i < bsgs.size(); ++i) {
                List<BSGSElement> sub_bsgs = bsgs.subList(i, bsgs.size());
                assertEquals(NumberUtils.factorial(degree - i), calculateOrder(sub_bsgs));
                assertTrue(isBSGS(sub_bsgs));
            }
        }
    }


    @Test
    public void testCreateSymmetricGroup4_large_degree() throws Exception {
        int[] degrees = {107, 109, 110, 112};
        for (int degree : degrees) {
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);
            for (BSGSElement element : bsgs) {
                for (int i = 0; i < element.orbitSize(); ++i) {
                    assertEquals(element.orbitList.get(i),
                            element.getTransversalOf(element.orbitList.get(i)).newIndexOf(element.basePoint)
                    );
                }
            }
            assertTrue(isBSGS(bsgs, 1 - 1E-9, CC.getRandomGenerator()));
            assertEquals(NumberUtils.factorial(degree), calculateOrder(bsgs));
        }
    }

    @Test
    public void testCreateSymmetricGroup4_large_degree_longtest() throws Exception {
        int[] degrees = {107, 109, 110, 112};
        for (int degree : degrees) {
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);

            for (BSGSElement element : bsgs) {
                for (int i = 0; i < element.orbitSize(); ++i) {
                    assertEquals(element.orbitList.get(i),
                            element.getTransversalOf(element.orbitList.get(i)).newIndexOf(element.basePoint)
                    );
                }
            }
            assertTrue(isBSGS(bsgs));
            assertEquals(NumberUtils.factorial(degree), calculateOrder(bsgs));
            System.out.println(degree);
        }
    }

    @Test
    public void testCreateSymmetricGroup5_large_degree() throws Exception {
        int[] degrees = {AlgorithmsBase.SMALL_DEGREE_THRESHOLD + 11, AlgorithmsBase.SMALL_DEGREE_THRESHOLD + 12};
        for (int degree : degrees) {
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);
            int ii[] = {30, 50, 71, 92, 100};
            for (int i : ii) {
                List<BSGSElement> sub_bsgs = bsgs.subList(i, bsgs.size());
                assertEquals(NumberUtils.factorial(degree - i), calculateOrder(sub_bsgs));
                assertTrue(isBSGS(sub_bsgs));
            }
        }
    }

    @Test
    public void testCreateSymmetricGroup5_large_degree_longtest() throws Exception {
        int[] degrees = {AlgorithmsBase.SMALL_DEGREE_THRESHOLD + 11, AlgorithmsBase.SMALL_DEGREE_THRESHOLD + 12};
        for (int degree : degrees) {
            List<BSGSElement> bsgs = createSymmetricGroupBSGS(degree);
            for (int i = 1; i < bsgs.size(); i += 1 + 9 / Math.log(2 + i / 9)) {
                List<BSGSElement> sub_bsgs = bsgs.subList(i, bsgs.size());
                assertEquals(NumberUtils.factorial(degree - i), calculateOrder(sub_bsgs));
                assertTrue(isBSGS(sub_bsgs));
            }
        }
    }

    @Ignore
    @Test
    public void testCreateSymmetricGroup6_large_degree() throws Exception {
        int degree = 130;
        PermutationGroup pg = PermutationGroup.createPermutationGroup(createSymmetricGroupBSGS(degree).get(0).stabilizerGenerators);
        assertEquals(NumberUtils.factorial(degree), pg.order());
    }

    @Test
    public void testCreateAlternatingGroup1_small_degree() throws Exception {
        for (int cc = 2; cc < 20; ++cc) {
            int degree = cc;
            List<BSGSElement> bsgs = createAlternatingGroupBSGS(degree);
            for (BSGSElement element : bsgs) {
                for (int i = 0; i < element.orbitSize(); ++i) {
                    assertEquals(element.orbitList.get(i),
                            element.getTransversalOf(element.orbitList.get(i)).newIndexOf(element.basePoint)
                    );
                }
            }
            assertTrue(isBSGS(bsgs));
            assertEquals(NumberUtils.factorial(degree).divide(BigInteger.valueOf(2)), calculateOrder(bsgs));
        }
    }

    @Test
    public void testCreateAlternatingGroup2_small_degree() throws Exception {
        for (int degree = 3; degree < 20; ++degree) {
            PermutationGroup pg = PermutationGroup.createPermutationGroup(createAlternatingGroupBSGS(degree).get(0).stabilizerGenerators);
            assertEquals(NumberUtils.factorial(degree).divide(BigInteger.valueOf(2)), pg.order());
        }
    }


    @Test
    public void testCreateAlternatingGroup2_large_degree() throws Exception {
        int[] degrees = {127, 128};
        for (int degree : degrees) {
            List<BSGSElement> bsgs = AlgorithmsBase.createAlternatingGroupBSGS(degree);

            for (BSGSElement element : bsgs)
                for (int i = 0; i < element.orbitSize(); ++i)
                    assertEquals(element.orbitList.get(i),
                            element.getTransversalOf(element.orbitList.get(i)).newIndexOf(element.basePoint));
            assertTrue(isBSGS(bsgs, 1 - 1E-9, CC.getRandomGenerator()));
            assertEquals(NumberUtils.factorial(degree).divide(BigInteger.valueOf(2)), calculateOrder(bsgs));
        }
    }

    @Test
    public void testCreateAlternatingGroup2_large_degree_longtest() throws Exception {
        int[] degrees = {127, 128};
        for (int degree : degrees) {
            List<BSGSElement> bsgs = AlgorithmsBase.createAlternatingGroupBSGS(degree);

            for (BSGSElement element : bsgs)
                for (int i = 0; i < element.orbitSize(); ++i)
                    assertEquals(element.orbitList.get(i),
                            element.getTransversalOf(element.orbitList.get(i)).newIndexOf(element.basePoint));
            assertTrue(isBSGS(bsgs));
            assertEquals(NumberUtils.factorial(degree).divide(BigInteger.valueOf(2)), calculateOrder(bsgs));
        }
    }

////////////////////////////////////////// TEST REMOVE REDUNDANT GENERATORS  ///////////////////////////////////////////

    @Test
    public void testRemoveRedundant0_longtest() {
        int degree = 20;
        int COUNT = 500;
        DescriptiveStatistics removed = new DescriptiveStatistics();
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(Permutations.createPermutation(Permutations.randomPermutation(degree)));
        randomness(source, 10, 50, CC.getRandomGenerator());

        ArrayList<BSGSCandidateElement> bsgs;
        List<Permutation> generators = new ArrayList<>();
        for (int tt = 0; tt < COUNT; ++tt) {
            generators.clear();
            for (int i = 0; i < 1 + CC.getRandomGenerator().nextInt(7); ++i)
                generators.add(random(source));

            //create BSGS
            List bsgs_ = createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            if (!(bsgs_ instanceof ArrayList))
                continue;
            bsgs = (ArrayList) bsgs_;
            SchreierSimsAlgorithm(bsgs);
            long in = AlgorithmsBase.numberOfStrongGenerators(bsgs);
            //remove redundant
            removeRedundantGenerators(bsgs);
            removed.addValue(in - AlgorithmsBase.numberOfStrongGenerators(bsgs));
            //check!
            assertTrue(isBSGS(bsgs));
        }
        System.out.println("Removed strong generators statistics:");
        System.out.println(removed);
    }


    @Test
    public void testRemoveRedundant0b() {
        List<Permutation> generators = new ArrayList<>();
        generators.add(Permutations.createPermutation(0, 2, 1, 3, 4));
        generators.add(Permutations.createPermutation(3, 2, 4, 0, 1));

        ArrayList<BSGSCandidateElement> bsgs
                = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));

        SchreierSimsAlgorithm(bsgs);
        long rem = numberOfStrongGenerators(bsgs);
        //remove redundant
        removeRedundantGenerators(bsgs);
        Assert.assertEquals(0, rem - numberOfStrongGenerators(bsgs));
        assertTrue(isBSGS(bsgs));
    }

    @Test
    public void testRemoveRedundant1() {
        final int COUNT = 50;//100;
        testRemoveRedundant(COUNT, false, false);
    }

    @Test
    public void testRemoveRedundant2() {
        final int COUNT = 10;//50;//100;
        testRemoveRedundant(COUNT, true, false);
    }

    @Test
    public void testRemoveRedundant3() {
        final int COUNT = 10;//50;//100;
        testRemoveRedundant(COUNT, true, true);
    }

    @Test
    public void testRemoveRedundant2_longtest() {
        final int COUNT = 50;//100;
        testRemoveRedundant(COUNT, true, false);
    }

    @Test
    public void testRemoveRedundant3_longtest() {
        final int COUNT = 50;//100;
        testRemoveRedundant(COUNT, true, true);
    }


    private static void testRemoveRedundant(int COUNT, boolean applySchreierSims, boolean bringRandomness) {
        DescriptiveStatistics removed = new DescriptiveStatistics();
        Random r = new Random();
        for (int tt = 0; tt < COUNT; ++tt) {
            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 20 + r.nextInt(6); ++i)
                source.add(Permutations.createPermutation(Permutations.randomPermutation(20)));
            if (bringRandomness)
                randomness(source);

            ArrayList<BSGSCandidateElement> BSGSCandidate =
                    new ArrayList<>(createRawBSGSCandidate(source.toArray(new Permutation[0])));
            if (applySchreierSims)
                SchreierSimsAlgorithm(BSGSCandidate);

            long before = numberOfStrongGenerators(BSGSCandidate);

            ArrayList<BSGSCandidateElement> BSGSCandidateCopy = AlgorithmsBase.clone(BSGSCandidate);
            AlgorithmsBase.removeRedundantGenerators(BSGSCandidateCopy);
            long after = numberOfStrongGenerators(BSGSCandidateCopy);

            removed.addValue(before - after);

            //make sure that this is same BSGS
            SchreierSimsAlgorithm(BSGSCandidate);
            SchreierSimsAlgorithm(BSGSCandidateCopy);

            Assert.assertTrue(PermutationGroup.createPermutationGroupFromBSGS(AlgorithmsBase.asBSGSList(BSGSCandidate)).equals(
                    PermutationGroup.createPermutationGroupFromBSGS(AlgorithmsBase.asBSGSList(BSGSCandidateCopy))));
        }
        System.out.println("Removed strong generators statistics:");
        System.out.println(removed);
    }

    @Test
    public void testRemoveRedundant4() {
        int[][] a = {{0, 4}, {1, 3}, {2, 9}, {6, 10}}, b = {{2, 10, 4}, {3, 6, 8}, {5, 7, 9}};
        PermutationGroup pg = PermutationGroup.createPermutationGroup(Permutations.createPermutation(a),
                Permutations.createPermutation(b));
        ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();

        for (int i = 0; i < bsgs.size() - 1; ++i) {
            PermutationGroup g = PermutationGroup.createPermutationGroup(bsgs.get(i).stabilizerGenerators);
            for (Permutation p : bsgs.get(i + 1).stabilizerGenerators)
                assertTrue(g.membershipTest(p));
        }

        removeRedundantGenerators(bsgs);

        for (int i = 0; i < bsgs.size() - 1; ++i) {
            PermutationGroup g = PermutationGroup.createPermutationGroup(bsgs.get(i).stabilizerGenerators);
            for (Permutation p : bsgs.get(i + 1).stabilizerGenerators)
                assertTrue(g.membershipTest(p));
        }
    }

///////////////////////////////////////////// TEST SCHREIER SIMS ///////////////////////////////////////////////////////


    @Test
    public void testSchreierSims1_WithGap() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 100; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                BigInteger expectedOrder = gap.evaluateToBigInteger("Order(g);");
                PermutationGroup group = gap.primitiveGroup(degree, i);
                Assert.assertEquals(expectedOrder, group.order());
            }
        }
    }

    @Test
    public void testSchreierSims2_WithGap_PerformanceTest() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 100; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                BigInteger expectedOrder = gap.evaluateToBigInteger("Order(g);");
                PermutationGroup group = gap.primitiveGroup(degree, i);
                Assert.assertEquals(expectedOrder, group.order());
            }
        }
        DescriptiveStatistics gap_stat = new DescriptiveStatistics(),
                redberry_stat = new DescriptiveStatistics();
        long start;
        for (int degree = 200; degree < 400; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;
                start = System.currentTimeMillis();
                gap.evaluate("g:= Group(GeneratorsOfGroup(g));");
                BigInteger expectedOrder = gap.evaluateToBigInteger("Order(g);");
                gap_stat.addValue(System.currentTimeMillis() - start);

                start = System.currentTimeMillis();
                PermutationGroup group = gap.primitiveGroup(degree, i);
                Assert.assertEquals(expectedOrder, group.order());
                redberry_stat.addValue(System.currentTimeMillis() - start);
            }
        }
        System.out.println("GAP time statistics:");
        System.out.println(gap_stat);
        System.out.println("Redberry time statistics:");
        System.out.println(redberry_stat);
    }

    @Test
    public void testRandomSchreierSim_WithGap() {
        double CL = 0.999;
        int trueBsgs = 0, total = 0;
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 70; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                Permutation[] generators = gap.primitiveGenerators(degree, i);
                ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(generators);
                RandomSchreierSimsAlgorithm(bsgs, CL, CC.getRandomGenerator());

                if (isBSGS(bsgs, 1 - 1E-9, CC.getRandomGenerator()))
                    ++trueBsgs;

                ++total;
            }
        }
        System.out.println("Total number of groups: " + total + ". BSGS constructed for " + trueBsgs);

        Assert.assertTrue((double) trueBsgs >= CL * 0.9 * (double) total);
    }

    @Test
    public void testRandomSchreierSim_WithGap_longtest() {
        double CL = 0.999;
        int trueBsgs = 0, total = 0;
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 100; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                Permutation[] generators = gap.primitiveGenerators(degree, i);
                ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(generators);
                RandomSchreierSimsAlgorithm(bsgs, CL, CC.getRandomGenerator());

                if (isBSGS(bsgs))
                    ++trueBsgs;

                ++total;
            }
        }
        System.out.println("Total number of groups: " + total + ". BSGS constructed for " + trueBsgs);

        Assert.assertTrue((double) trueBsgs >= CL * 0.9 * (double) total);
    }

    @Test
    public void testRandomSchreierSimsWithOrder_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 70; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                BigInteger order = gap.evaluateToBigInteger("Order(g);");

                Permutation[] generators = gap.primitiveGenerators(degree, i);
                ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(generators);
                RandomSchreierSimsAlgorithmForKnownOrder(bsgs, order, CC.getRandomGenerator());

                assertTrue(isBSGS(bsgs, 1 - 1E-9, CC.getRandomGenerator()));
                assertEquals(order, calculateOrder(bsgs));
            }
        }
    }


    @Test
    public void testRandomSchreierSimsWithOrder_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 100; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                BigInteger order = gap.evaluateToBigInteger("Order(g);");

                Permutation[] generators = gap.primitiveGenerators(degree, i);
                ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(generators);
                RandomSchreierSimsAlgorithmForKnownOrder(bsgs, order, CC.getRandomGenerator());

                assertTrue(isBSGS(bsgs));
                assertEquals(order, calculateOrder(bsgs));
            }
        }
    }


    @Test
    public void testSchreierSims1() {
        Permutation a = Permutations.createPermutation(1, 2, 3, 4, 5, 6, 0, 7),
                b = Permutations.createPermutation(0, 2, 4, 6, 1, 3, 5, 7),
                c = Permutations.createPermutation(7, 6, 3, 2, 5, 4, 1, 0);

        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b, c);
        SchreierSimsAlgorithm(bsgs);
        removeRedundantBaseRemnant(bsgs);
        assertEquals(3, bsgs.size());
        assertTrue(isBSGS(bsgs));
    }

    @Test
    public void testSchreierSims2() {
        Permutation a = Permutations.createPermutation(0, 1, 2, 3, 4, 5),
                b = Permutations.createPermutation(0, 3, 2, 1, 4, 5),
                c = Permutations.createPermutation(2, 1, 0, 3, 4, 5);
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b, c);
        SchreierSimsAlgorithm(bsgs);
        int order = calculateOrder(bsgs).intValue();

        int _order_ = 0;
        BruteForcePermutationIterator it = new BruteForcePermutationIterator(Arrays.asList(a, b, c));
        while (it.hasNext()) {
            ++_order_;
            it.next();
        }
        assertEquals(_order_, order);
    }

////////////////////////////////////////////// TEST BASE CHANGE ////////////////////////////////////////////////////////

    @Test
    public void testSwapAdjacentBasePoints1_longtest() {
        int degree = 15;
        int COUNT = 10;
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(Permutations.createPermutation(Permutations.randomPermutation(degree)));
        randomness(source, 10, 50, CC.getRandomGenerator());

        ArrayList<BSGSCandidateElement> bsgs1, bsgs2;
        List<Permutation> generators = new ArrayList<>();
        for (int tt = 0; tt < COUNT; ++tt) {
            generators.clear();
            for (int i = 0; i < 1 + CC.getRandomGenerator().nextInt(7); ++i)
                generators.add(random(source));

            bsgs1 = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            SchreierSimsAlgorithm(bsgs1);
            for (int i = 0; i < bsgs1.size() - 1; ++i) {
                bsgs2 = AlgorithmsBase.clone(bsgs1);
                swapAdjacentBasePoints(bsgs2, i, degree);
                assertTrue(isBSGS(bsgs2));
                int[] p = Permutations.randomPermutation(degree);
                for (int pp : p) {
                    if (pp > bsgs2.size() - 2)
                        continue;
                    swapAdjacentBasePoints(bsgs2, pp, degree);
                    assertTrue(isBSGS(bsgs2));
                }
                for (int pp : p) {
                    if (pp > bsgs2.size() - 2)
                        continue;
                    swapAdjacentBasePoints(bsgs2, pp, degree);
                    assertTrue(isBSGS(bsgs2));
                }
            }
        }
    }

    @Test
    public void testSwapAdjacentBasePoints1a() {
        List<Permutation> generators = new ArrayList<>();
        generators.add(Permutations.createPermutation(0, 2, 1, 3, 4));
        generators.add(Permutations.createPermutation(3, 2, 4, 0, 1));
        ArrayList<BSGSCandidateElement> bsgs
                = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
        SchreierSimsAlgorithm(bsgs);
        ArrayList<BSGSCandidateElement> bsgs1;

        for (int i = 0; i < bsgs.size() - 1; ++i) {
            bsgs1 = AlgorithmsBase.clone(bsgs);
            swapAdjacentBasePoints(bsgs1, i);
            assertTrue(isBSGS(bsgs1));
        }
    }


    @Test
    public void testSwapAdjacentBasePoints2a() {
        int[][] a = {{0, 4}, {1, 3}, {2, 9}, {6, 10}}, b = {{2, 10, 4}, {3, 6, 8}, {5, 7, 9}};
        PermutationGroup pg = PermutationGroup.createPermutationGroup(Permutations.createPermutation(a),
                Permutations.createPermutation(b));
        ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();
        removeRedundantGenerators(bsgs);
        assertTrue(isBSGS(bsgs));

        swapAdjacentBasePoints(bsgs, 2, pg.degree());
        assertTrue(isBSGS(bsgs));
        removeRedundantGenerators(bsgs);
        assertTrue(isBSGS(bsgs));

        swapAdjacentBasePoints(bsgs, 0, pg.degree());
        assertTrue(isBSGS(bsgs));
        removeRedundantGenerators(bsgs);
        assertTrue(isBSGS(bsgs));

        swapAdjacentBasePoints(bsgs, 0, pg.degree());
        assertTrue(isBSGS(bsgs));
        removeRedundantGenerators(bsgs);
        assertTrue(isBSGS(bsgs));

        swapAdjacentBasePoints(bsgs, 1, pg.degree());
        assertTrue(isBSGS(bsgs));
        removeRedundantGenerators(bsgs);
        assertTrue(isBSGS(bsgs));

        swapAdjacentBasePoints(bsgs, 2, pg.degree());
        assertTrue(isBSGS(bsgs));
        removeRedundantGenerators(bsgs);
        assertTrue(isBSGS(bsgs));
    }

    @Test
    public void testSwapAdjacentBasePoints2_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 50; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;
                Permutation[] generators = gap.evaluateToGenerators("g");

                ArrayList<BSGSCandidateElement> bsgs1 = (ArrayList) createRawBSGSCandidate(generators);
                SchreierSimsAlgorithm(bsgs1);
                removeRedundantGenerators(bsgs1);
                for (int j = 0; j < bsgs1.size() - 1; j += 2) {
                    ArrayList<BSGSCandidateElement> bsgs2 = bsgs1;
                    swapAdjacentBasePoints(bsgs2, j, degree);
                    removeRedundantBaseRemnant(bsgs2);
                    removeRedundantGenerators(bsgs2);
                    assertTrue(isBSGS(bsgs2, 1 - 1E-9, CC.getRandomGenerator()));
                    int[] p = Permutations.randomPermutation(degree, CC.getRandomGenerator());
                    for (int pp : p) {
                        if (pp > bsgs2.size() - 2)
                            continue;
                        swapAdjacentBasePoints(bsgs2, pp, degree);
                        removeRedundantBaseRemnant(bsgs2);
                        removeRedundantGenerators(bsgs2);
                        assertTrue(isBSGS(bsgs2, 1 - 1E-9, CC.getRandomGenerator()));
                    }
                }
            }
        }
    }

    @Test
    public void testSwapAdjacentBasePoints2_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 20; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;
                Permutation[] generators = gap.evaluateToGenerators("g");

                ArrayList<BSGSCandidateElement> bsgs1 = (ArrayList) createRawBSGSCandidate(generators);
                SchreierSimsAlgorithm(bsgs1);
                for (int j = 0; j < bsgs1.size() - 1; ++j) {
                    ArrayList<BSGSCandidateElement> bsgs2 = AlgorithmsBase.clone(bsgs1);
                    swapAdjacentBasePoints(bsgs2, j, degree);
                    assertTrue(isBSGS(bsgs2));
                    int[] p = Permutations.randomPermutation(degree, CC.getRandomGenerator());
                    for (int pp : p) {
                        if (pp > bsgs2.size() - 2)
                            continue;
                        swapAdjacentBasePoints(bsgs2, pp, degree);
                        assertTrue(isBSGS(bsgs2));
                    }
                    for (int pp : p) {
                        if (pp > bsgs2.size() - 2)
                            continue;
                        swapAdjacentBasePoints(bsgs2, pp, degree);
                        assertTrue(isBSGS(bsgs2));
                    }
                }
            }
        }
    }

    @Test
    public void testSwapAdjacentBasePoints3() {
        Permutation a = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = Permutations.createPermutation(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
        SchreierSimsAlgorithm(bsgs);
        assertEquals(calculateOrder(bsgs).intValue(), 120);

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
    public void testSwapAdjacentBasePoints4() {
        Permutation a = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = Permutations.createPermutation(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        int degree = a.length();
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
        SchreierSimsAlgorithm(bsgs);
        assertEquals(calculateOrder(bsgs).intValue(), 120);

        //0,1,2
        int[] oldBase = {0, 1, 2};
        assertArrayEquals(oldBase, getBaseAsArray(bsgs));
        bsgs.add(new BSGSCandidateElement(2, new ArrayList<Permutation>(), degree));
        assertTrue(isBSGS(bsgs));
        swapAdjacentBasePoints(bsgs, 2, degree);
        assertTrue(isBSGS(bsgs));
        assertEquals(0, bsgs.get(3).stabilizerGenerators.size());
    }

    @Test
    public void testSwapAdjacentBasePoints5_redundant_points() {
        Permutation a = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = Permutations.createPermutation(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);
        int degree = a.length();
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) createRawBSGSCandidate(a, b);
        SchreierSimsAlgorithm(bsgs);
        assertEquals(calculateOrder(bsgs).intValue(), 120);
        //real base: 0,1,2
        int[] oldBase = {0, 1, 2};

        //add redundant point 5
        bsgs.add(new BSGSCandidateElement(5, new ArrayList<Permutation>(), degree));
        assertTrue(isBSGS(bsgs));
        SchreierSimsAlgorithm(bsgs);
        assertTrue(isBSGS(bsgs));
        assertArrayEquals(new int[]{0, 1, 2, 5}, getBaseAsArray(bsgs));

        //add redundant point 3
        bsgs.add(new BSGSCandidateElement(3, new ArrayList<Permutation>(), degree));
        assertTrue(isBSGS(bsgs));
        SchreierSimsAlgorithm(bsgs);
        assertTrue(isBSGS(bsgs));
        assertArrayEquals(new int[]{0, 1, 2, 5, 3}, getBaseAsArray(bsgs));

        //add redundant point 6
        bsgs.add(new BSGSCandidateElement(6, new ArrayList<Permutation>(), degree));
        assertTrue(isBSGS(bsgs));
        SchreierSimsAlgorithm(bsgs);
        assertTrue(isBSGS(bsgs));
        assertArrayEquals(new int[]{0, 1, 2, 5, 3, 6}, getBaseAsArray(bsgs));

        //swap redundant points
        swapAdjacentBasePoints(bsgs, 3, degree);
        assertTrue(isBSGS(bsgs));

        for (int i = bsgs.size() - 2; i >= 0; --i) {
            ArrayList<BSGSCandidateElement> copy = AlgorithmsBase.clone(bsgs);
            swapAdjacentBasePoints(copy, i, degree);
            assertTrue(isBSGS(copy));
        }

        for (int i = bsgs.size() - 2; i >= 0; --i) {
            swapAdjacentBasePoints(bsgs, i, degree);
            assertTrue(isBSGS(bsgs));
        }

        for (int i = 0; i <= 100; ++i) {
            swapAdjacentBasePoints(bsgs, CC.getRandomGenerator().nextInt(bsgs.size() - 2), degree);
            assertTrue(isBSGS(bsgs));
        }
    }

    @Test
    public void testRebaseWithTranspositions1() {
        Permutation a = Permutations.createPermutation(4, 8, 7, 1, 6, 5, 0, 9, 3, 2),
                b = Permutations.createPermutation(7, 4, 1, 8, 5, 2, 9, 0, 6, 3);

        ArrayList<BSGSCandidateElement> bsgs = PermutationGroup.createPermutationGroup(a, b).getBSGSCandidate();
        int[] newBase = {1, 2, 0};

        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);

        assertTrue(isBSGS(bsgs));
        assertArrayEquals(newBase, getBaseAsArray(bsgs));
    }

    @Test
    public void testRebaseWithTranspositions2_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 55; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;
                ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();
                for (int j = 0; j < 5; ++j) {
                    int[] oldBase = getBaseAsArray(bsgs);
                    int[] newBase = Permutations.createPermutation(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                    AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase, degree);
                    assertTrue(isBSGS(bsgs, 1 - 1E-9, CC.getRandomGenerator()));
                    final int[] _newBase = getBaseAsArray(bsgs);
                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                        assertEquals(newBase[r], _newBase[r]);
                }
            }
        }
    }

    @Test
    public void testRebaseWithTranspositions2_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 55; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;
                ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();
                for (int j = 0; j < 50; ++j) {
                    int[] oldBase = getBaseAsArray(bsgs);
                    int[] newBase = Permutations.createPermutation(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                    AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase, degree);
                    assertTrue(isBSGS(bsgs));
                    final int[] _newBase = getBaseAsArray(bsgs);
                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                        assertEquals(newBase[r], _newBase[r]);
                }
            }
        }
    }

    @Test
    public void testRebaseWithTranspositions3_WithGap_PerformanceTest() {
        DescriptiveStatistics timings = new DescriptiveStatistics();
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 150; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();
                Object[] r = timing(
                        new Timing.TimingJob() {
                            @Override
                            public Object doJob() {
                                for (int i = 0; i < 50; ++i) {
                                    int[] oldBase = getBaseAsArray(bsgs);
                                    int[] newBase = Permutations.createPermutation(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                                    AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);
                                    assertTrue(isBSGS(bsgs));
                                    final int[] _newBase = getBaseAsArray(bsgs);
                                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                                        assertEquals(newBase[r], _newBase[r]);
                                }
                                return null;
                            }
                        }, false);
                timings.addValue((Long) r[0]);
            }
        }
        System.out.println(timings);
    }

    @Test
    public void testRebaseWithTranspositions4() {
        Permutation a = Permutations.createPermutation(6, 7, 8, 9, 10, 0, 11, 12, 13, 14, 1, 15, 16, 17, 2, 18, 19, 3, 20, 4, 5),
                b = Permutations.createPermutation(0, 13, 6, 20, 10, 8, 11, 1, 4, 3, 15, 12, 18, 17, 9, 5, 14, 19, 2, 7, 16),
                c = Permutations.createPermutation(0, 9, 12, 10, 17, 5, 6, 14, 16, 1, 3, 11, 2, 19, 7, 15, 8, 4, 18, 13, 20);
        PermutationGroup pg = PermutationGroup.createPermutationGroup(a, b, c);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();
        final int[] newBase = {3, 4, 0, 1, 2};
        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);


        assertTrue(isBSGS(bsgs));
        assertTrue(getBaseAsArray(bsgs).length < newBase.length);
    }

    @Test
    public void testRebaseWithTranspositions5() {
        Permutation gen0 = Permutations.createPermutation(0, 5, 10, 15, 20, 1, 6, 11, 16, 21, 2, 7, 12, 17, 22, 3, 8, 13, 18, 23, 4, 9, 14, 19, 24);
        Permutation gen1 = Permutations.createPermutation(5, 6, 8, 9, 7, 10, 11, 13, 14, 12, 15, 16, 18, 19, 17, 20, 21, 23, 24, 22, 0, 1, 3, 4, 2);
        Permutation gen2 = Permutations.createPermutation(1, 2, 3, 4, 0, 6, 7, 8, 9, 5, 16, 17, 18, 19, 15, 21, 22, 23, 24, 20, 11, 12, 13, 14, 10);
        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();
        int[] oldBase = {0, 1, 2, 3, 4, 5, 7, 8, 9, 10};
        final int[] newBase = {0, 1, 7, 5, 10, 8, 3, 9, 2, 4};

        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);

        assertTrue(isBSGS(bsgs));
        int[] _newBase = getBaseAsArray(bsgs);//[0, 1, 7, 5, 10]

        for (int i = 0; i < _newBase.length; ++i)
            assertEquals(_newBase[i], newBase[i]);
    }


    @Test
    public void testRebaseWithTranspositions6() {
        //PermutationGroup pg = GapPrimitiveGroupsReader.readGroupFromGap("/home/stas/gap4r6/prim/grps/gps1.g", 41);

        Permutation gen0 = Permutations.createPermutation(5, 6, 8, 9, 7, 10, 11, 13, 14, 12, 15, 16, 18, 19, 17, 20, 21, 23, 24, 22, 0, 1, 3, 4, 2);
        Permutation gen1 = Permutations.createPermutation(1, 2, 3, 4, 0, 6, 7, 8, 9, 5, 16, 17, 18, 19, 15, 21, 22, 23, 24, 20, 11, 12, 13, 14, 10);
        Permutation gen2 = Permutations.createPermutation(0, 5, 10, 20, 15, 1, 6, 11, 21, 16, 2, 7, 12, 22, 17, 3, 8, 13, 23, 18, 4, 9, 14, 24, 19);
        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();
        int[] oldBase = {1, 0, 2, 3, 5, 10};
        final int[] newBase = {2, 3, 10, 0, 5, 1};

        AlgorithmsBase.rebaseWithTranspositions(bsgs, newBase);

        int[] _newBase = getBaseAsArray(bsgs);//[0, 1, 7, 5, 10]

        assertTrue(isBSGS(bsgs));

        for (int i = 0; i < _newBase.length; ++i)
            assertEquals(_newBase[i], newBase[i]);
    }

    @Test
    public void testRebaseWithConjugationAndTranspositions1_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 55; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();

                for (int j = 0; j < 5; ++j) {
                    int[] oldBase = getBaseAsArray(bsgs);
                    int[] newBase = Permutations.createPermutation(
                            Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                    rebaseWithConjugationAndTranspositions(bsgs, newBase, degree);
                    assertTrue(isBSGS(bsgs, 1 - 1E-9, CC.getRandomGenerator()));
                    final int[] _newBase = getBaseAsArray(bsgs);
                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                        assertEquals(newBase[r], _newBase[r]);
                }
            }
        }
    }

    @Test
    public void testRebaseWithConjugationAndTranspositions1_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 55; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();

                for (int j = 0; j < 50; ++j) {
                    int[] oldBase = getBaseAsArray(bsgs);
                    int[] newBase = Permutations.createPermutation(
                            Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                    rebaseWithConjugationAndTranspositions(bsgs, newBase, degree);
                    assertTrue(isBSGS(bsgs));
                    final int[] _newBase = getBaseAsArray(bsgs);
                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                        assertEquals(newBase[r], _newBase[r]);
                }
            }
        }
    }

    @Test
    public void testRebaseWithConjugationAndTranspositions2_WithGap_PerformanceTest() {

        DescriptiveStatistics timings = new DescriptiveStatistics();
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 150; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();
                final int deg = degree;
                Object[] r = timing(
                        new Timing.TimingJob() {
                            @Override
                            public Object doJob() {
                                for (int i = 0; i < 50; ++i) {
                                    int[] oldBase = getBaseAsArray(bsgs);
                                    int[] newBase = Permutations.createPermutation(
                                            Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                                    rebaseWithConjugationAndTranspositions(bsgs, newBase, deg);
                                    final int[] _newBase = getBaseAsArray(bsgs);
                                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                                        assertEquals(newBase[r], _newBase[r]);
                                }
                                return null;
                            }
                        }, false);
                timings.addValue((Long) r[0]);
            }
        }
        System.out.println(timings);
    }

    @Test
    public void testRebaseWithConjugationAndTranspositions3() {
        Permutation gen0 = Permutations.createPermutation(1, 2, 3, 4, 5, 6, 0, 7);
        Permutation gen1 = Permutations.createPermutation(0, 2, 4, 6, 1, 3, 5, 7);
        Permutation gen2 = Permutations.createPermutation(7, 6, 3, 2, 5, 4, 1, 0);
        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2);

        final ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();
        int[] oldBase = {0, 1, 2};
        int[] newBase = {2, 0, 1};

        rebaseWithConjugationAndTranspositions(bsgs, newBase);

        assertTrue(isBSGS(bsgs));
        final int[] _newBase = getBaseAsArray(bsgs);
        for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
            assertEquals(newBase[r], _newBase[r]);

    }

    @Test
    public void testRebaseWithConjugationAndTranspositions4() {
        Permutation gen0 = Permutations.createPermutation(6, 7, 8, 9, 10, 0, 11, 12, 13, 14, 1, 15, 16, 17, 2, 18, 19, 3, 20, 4, 5, 21);
        Permutation gen1 = Permutations.createPermutation(0, 13, 6, 20, 10, 8, 11, 1, 4, 3, 15, 12, 18, 17, 9, 5, 14, 19, 2, 7, 16, 21);
        Permutation gen2 = Permutations.createPermutation(0, 9, 12, 10, 17, 5, 6, 14, 16, 1, 3, 11, 2, 19, 7, 15, 8, 4, 18, 13, 20, 21);
        Permutation gen3 = Permutations.createPermutation(21, 9, 13, 16, 4, 5, 6, 14, 10, 1, 8, 11, 19, 2, 7, 15, 3, 17, 20, 12, 18, 0);

        PermutationGroup pg = PermutationGroup.createPermutationGroup(gen0, gen1, gen2, gen3);

        ArrayList<BSGSCandidateElement> bsgs = pg.getBSGSCandidate();
        assertTrue(isBSGS(bsgs));

        int[] newBase = {2, 3};
        rebaseWithConjugationAndTranspositions(bsgs, newBase);

        assertTrue(isBSGS(bsgs));
    }

    @Test
    public void testRebaseFromScratch1_WithGap() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 30; degree < 45; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();

                for (int j = 0; j < 5; ++j) {
                    int[] oldBase = getBaseAsArray(bsgs);
                    int[] newBase = Permutations.createPermutation(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                    rebaseFromScratch(bsgs, newBase);
                    assertTrue(isBSGS(bsgs));
                    final int[] _newBase = getBaseAsArray(bsgs);
                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                        assertEquals(newBase[r], _newBase[r]);
                }
            }
        }
    }

    @Test
    public void testRebaseFromScratch1_WithGap_longtest() {
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 30; degree < 45; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();

                for (int j = 0; j < 50; ++j) {
                    int[] oldBase = getBaseAsArray(bsgs);
                    int[] newBase = Permutations.createPermutation(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                    rebaseFromScratch(bsgs, newBase);
                    assertTrue(isBSGS(bsgs));
                    final int[] _newBase = getBaseAsArray(bsgs);
                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                        assertEquals(newBase[r], _newBase[r]);
                }
            }
        }
    }

    @Test
    public void rebaseFromScratch2_WithGap_PerformanceTest() {
        DescriptiveStatistics stat = new DescriptiveStatistics();
        GapGroupsInterface gap = getGapInterface();
        for (int degree = 2; degree < 55; ++degree) {
            int nrPrimitiveGroups = gap.nrPrimitiveGroups(degree);
            for (int i = 0; i < nrPrimitiveGroups; ++i) {
                gap.evaluate("g:= PrimitiveGroup( " + degree + ", " + (i + 1) + ");");
                if ((gap.evaluateToBoolean("IsNaturalSymmetricGroup(g);") ||
                        gap.evaluateToBoolean("IsNaturalAlternatingGroup(g);")) && degree > 15)
                    continue;

                final ArrayList<BSGSCandidateElement> bsgs = gap.primitiveGroup(degree, i).getBSGSCandidate();
                Object[] ttt = timing(
                        new Timing.TimingJob() {
                            @Override
                            public Object doJob() {
                                for (int i = 0; i < 50; ++i) {
                                    int[] oldBase = getBaseAsArray(bsgs);
                                    int[] newBase = Permutations.createPermutation(Permutations.randomPermutation(oldBase.length)).permute(oldBase);
                                    rebaseFromScratch(bsgs, newBase);
                                    assertTrue(isBSGS(bsgs));
                                    final int[] _newBase = getBaseAsArray(bsgs);
                                    for (int r = 0; r < _newBase.length && r < newBase.length; ++r)
                                        assertEquals(newBase[r], _newBase[r]);
                                }
                                return null;
                            }
                        });
                stat.addValue((Long) ttt[0]);
            }
        }
        System.out.println(stat);
    }

    @Test
    public void testExample1() {
        Permutation perm1 = Permutations.createPermutation(1, 2, 3, 4, 0);
        Permutation perm2 = Permutations.createPermutation(1, 3, 0, 4, 2);
//create a candidate BSGS
        ArrayList<BSGSCandidateElement> candidate = (ArrayList) AlgorithmsBase.createRawBSGSCandidate(perm1, perm2);
//apply randomized Schreier-Sims algorithm to candidate BSGS (add missing base points and basic stabilizers)
        AlgorithmsBase.RandomSchreierSimsAlgorithm(candidate, 0.9999, new Well1024a());
//if our random Schreier-Sims was not enough
        if (!AlgorithmsBase.isBSGS(candidate))
            AlgorithmsBase.SchreierSimsAlgorithm(candidate);
        List<BSGSElement> bsgs = AlgorithmsBase.asBSGSList(candidate);
    }
}
