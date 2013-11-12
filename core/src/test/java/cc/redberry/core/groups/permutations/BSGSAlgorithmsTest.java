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

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.FastMath;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.BSGSAlgorithms.*;
import static cc.redberry.core.groups.permutations.RandomPermutation.DEFAULT_RANDOMNESS_EXTEND_TO_SIZE;
import static cc.redberry.core.groups.permutations.RandomPermutation.random;
import static cc.redberry.core.groups.permutations.RandomPermutation.randomness;
import static java.lang.System.currentTimeMillis;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BSGSAlgorithmsTest {

    @Test
    public void testRemoveRedundant0() {
        long seed = currentTimeMillis();
        int n = 20;
        int COUNT = 1000;
        DescriptiveStatistics removed = new DescriptiveStatistics();
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(new Permutation(Combinatorics.randomPermutation(n, randomGenerator)));
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
            generators.add(new Permutation(Combinatorics.randomPermutation(5)));
            generators.add(new Permutation(Combinatorics.randomPermutation(5)));
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
        generators.add(new Permutation(0, 2, 1, 3, 4));
        generators.add(new Permutation(3, 2, 4, 0, 1));

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
                source.add(new Permutation(Combinatorics.randomPermutation(20)));
            if (bringRandomness)
                randomness(source);

            ArrayList<BSGSCandidateElement> BSGSCandidate =
                    new ArrayList<>(createRawBSGSCandidate(source.toArray(new Permutation[0])));
            if (applySchreierSims)
                SchreierSimsAlgorithm(BSGSCandidate);

            long before = numOfGenerators(BSGSCandidate);

            ArrayList<BSGSCandidateElement> BSGSCandidateCopy = BSGSAlgorithms.clone(BSGSCandidate);
            BSGSAlgorithms.removeRedundantGenerators(BSGSCandidateCopy);
            long after = numOfGenerators(BSGSCandidateCopy);

            removed.addValue(before - after);

            //make sure that this is same BSGS
            SchreierSimsAlgorithm(BSGSCandidate);
            SchreierSimsAlgorithm(BSGSCandidateCopy);

            assertSameGroups(
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidateCopy))));
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
            source.add(new Permutation(Combinatorics.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        ArrayList<BSGSCandidateElement> bsgs1, bsgs2;
        List<Permutation> generators = new ArrayList<>();
        for (int tt = 0; tt < COUNT; ++tt) {
            generators.clear();
            for (int i = 0; i < 1 + randomGenerator.nextInt(7); ++i)
                generators.add(random(source, randomGenerator));

            bsgs1 = (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0]));
            bsgs2 = BSGSAlgorithms.clone(bsgs1);

            SchreierSimsAlgorithm(bsgs1);
            BigInteger order = getOrder(bsgs1);

            RandomSchreierSimsAlgorithmForKnownOrder(bsgs2, order, randomGenerator);
            removeRedundantGenerators(bsgs2);
            assertTrue(isBSGS(bsgs2));
        }
    }

    private static void burnJvm(long seed) {
        int n = 20;
        int COUNT = 1000;
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(new Permutation(Combinatorics.randomPermutation(n, randomGenerator)));
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
        for (Permutation a : p1.generators())
            assertTrue(p2.isMember(a));
        for (Permutation a : p2.generators())
            assertTrue(p1.isMember(a));
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
            source.add(new Permutation(Combinatorics.randomPermutation(n, randomGenerator)));
        randomness(source, 10, 50, randomGenerator);

        int randomSchreierFails = 0;

        long start;
        for (int tt = 0; tt < COUNT; ++tt) {

            List<Permutation> generators = new ArrayList<>();
            for (int i = 0; i < 6; ++i)
                generators.add(random(source, randomGenerator));

            ArrayList<BSGSCandidateElement> BSGSCandidate1 =
                    (ArrayList) createRawBSGSCandidate(generators.toArray(new Permutation[0])),
                    BSGSCandidate2 = BSGSAlgorithms.clone(BSGSCandidate1),
                    BSGSCandidate3 = BSGSAlgorithms.clone(BSGSCandidate1);

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
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate2))));

            assertSameGroups(
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate3))));
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
            source.add(new Permutation(Combinatorics.randomPermutation(n, randomGenerator)));
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
                    BSGSCandidate2 = BSGSAlgorithms.clone(BSGSCandidate1),
                    BSGSCandidate3 = BSGSAlgorithms.clone(BSGSCandidate1);

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
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate2))));

            assertSameGroups(
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate1))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate3))));
        }
    }

}