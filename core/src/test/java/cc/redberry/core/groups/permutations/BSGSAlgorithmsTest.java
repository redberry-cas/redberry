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
import org.apache.commons.math3.util.ArithmeticUtils;
import org.apache.commons.math3.util.FastMath;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static cc.redberry.core.TAssert.assertTrue;
import static java.lang.System.currentTimeMillis;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class BSGSAlgorithmsTest {

    @Test
    public void testRandomness1() {
        final int COUNT = 50;//100;
        DescriptiveStatistics p1Create = new DescriptiveStatistics();
        DescriptiveStatistics p2Create = new DescriptiveStatistics();
        DescriptiveStatistics compare = new DescriptiveStatistics();
        DescriptiveStatistics orders = new DescriptiveStatistics();
        Random r = new Random();
        long start;
        for (int tt = 0; tt < COUNT; ++tt) {

            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 1 + r.nextInt(6); ++i)
                source.add(new Permutation(Combinatorics.randomPermutation(20)));
            BSGSAlgorithms.randomness(source);


            List<Permutation> generators = new ArrayList<>();
            for (int i = 0; i < 1 + r.nextInt(source.size()); ++i)
                generators.add(BSGSAlgorithms.random(source));

            start = currentTimeMillis();
            PermutationGroup p1 = PermutationGroupFactory.createPermutationGroup(generators);
            p1Create.addValue(currentTimeMillis() - start);

            BSGSAlgorithms.randomness(generators);

            start = currentTimeMillis();
            PermutationGroup p2 = PermutationGroupFactory.createPermutationGroup(generators);
            p2Create.addValue(currentTimeMillis() - start);

            start = currentTimeMillis();
            assertSameGroups(p1, p2);
            compare.addValue(currentTimeMillis() - start);

            orders.addValue(p1.order().longValue());
        }
        System.out.println("\nCreate p1:");
        System.out.println(p1Create);
        System.out.println("\nCreate p2:");
        System.out.println(p2Create);
        System.out.println("\nCompare:");
        System.out.println(compare);
        System.out.println("\nOrders:");
        System.out.println(orders);
    }

    @Ignore
    @Test
    public void testRemoveRedundant0() {
        int COUNT = 1;

        DescriptiveStatistics removed = new DescriptiveStatistics();
        Random r = new Random();
        for (int tt = 0; tt < COUNT; ++tt) {
            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 500 + r.nextInt(6); ++i)
                source.add(new Permutation(Combinatorics.randomPermutation(100)));
            BSGSAlgorithms.randomness(source);

            ArrayList<BSGSCandidateElement> BSGSCandidate =
                    new ArrayList<>(BSGSAlgorithms.createRawBSGSCandidate(source.toArray(new Permutation[0])));

            long before = numOfGenerators(BSGSCandidate);
            System.out.println(before);
            ArrayList<BSGSCandidateElement> BSGSCandidateCopy = BSGSAlgorithms.clone(BSGSCandidate);
            BSGSAlgorithms.removeRedundantGenerators(BSGSCandidateCopy);
            long after = numOfGenerators(BSGSCandidateCopy);
            System.out.println(after);
            System.out.println();
            removed.addValue(before - after);

            //make sure that this is same BSGS
            BSGSAlgorithms.RandomSchreierSimsAlgorithm(BSGSCandidate, 0.999, new Well1024a());
            BSGSAlgorithms.RandomSchreierSimsAlgorithm(BSGSCandidateCopy, 0.999, new Well1024a());
            System.out.println("RANDOM SCHREIER");
            BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidate);
            BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidateCopy);
            System.out.println("SCHREIER");
            assertSameGroups(
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidateCopy))));
            System.out.println(tt);
        }
        System.out.println(removed);
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

    public static void testRemoveRedundant(int COUNT, boolean applySchreierSims, boolean bringRandomness) {
        DescriptiveStatistics removed = new DescriptiveStatistics();
        Random r = new Random();
        for (int tt = 0; tt < COUNT; ++tt) {
            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 20 + r.nextInt(6); ++i)
                source.add(new Permutation(Combinatorics.randomPermutation(20)));
            if (bringRandomness)
                BSGSAlgorithms.randomness(source);

            ArrayList<BSGSCandidateElement> BSGSCandidate =
                    new ArrayList<>(BSGSAlgorithms.createRawBSGSCandidate(source.toArray(new Permutation[0])));
            if (applySchreierSims)
                BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidate);

            long before = numOfGenerators(BSGSCandidate);

            ArrayList<BSGSCandidateElement> BSGSCandidateCopy = BSGSAlgorithms.clone(BSGSCandidate);
            BSGSAlgorithms.removeRedundantGenerators(BSGSCandidateCopy);
            long after = numOfGenerators(BSGSCandidateCopy);

            removed.addValue(before - after);

            //make sure that this is same BSGS
            BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidate);
            BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidateCopy);

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
    public void testRandomSchreierSims1() {
        int COUNT = 50;
        DescriptiveStatistics scrsims = new DescriptiveStatistics();
        DescriptiveStatistics rndmscrsims = new DescriptiveStatistics();
        RandomGenerator randomGenerator = new Well1024a();
        long start;
        for (int tt = 0; tt < COUNT; ++tt) {
            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 20 + randomGenerator.nextInt(6); ++i)
                source.add(new Permutation(Combinatorics.randomPermutation(20)));

            ArrayList<BSGSCandidateElement> BSGSCandidate =
                    new ArrayList<>(BSGSAlgorithms.createRawBSGSCandidate(source.toArray(new Permutation[0])));
            ArrayList<BSGSCandidateElement> BSGSCandidateCopy = BSGSAlgorithms.clone(BSGSCandidate);

            start = currentTimeMillis();
            BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidate);
            scrsims.addValue(currentTimeMillis() - start);
//            System.out.println("ss");

            start = currentTimeMillis();
            BSGSAlgorithms.RandomSchreierSimsAlgorithm(BSGSCandidateCopy, 0.9, randomGenerator);
//            System.out.println("random done");
//            BSGSAlgorithms.removeRedundantGenerators(BSGSCandidateCopy);
            BSGSAlgorithms.SchreierSimsAlgorithm(BSGSCandidateCopy);
            rndmscrsims.addValue(currentTimeMillis() - start);
//            System.out.println("rss");


            assertSameGroups(
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidate))),
                    new PermutationGroupImpl(new BSGS(BSGSAlgorithms.asBSGSList(BSGSCandidateCopy))));
//            System.out.println(tt);
//            System.out.println();
        }

        System.out.println("Schreier-sims:");
        System.out.println(scrsims);
        System.out.println("Schreier-sims with randomness:");
        System.out.println(rndmscrsims);
    }

    @Test
    public void testRandomGet() {

        for (int tt = 0; tt < 10; ++tt) {
            //symmetric group
            int n = 10;
            List<Permutation> source = new ArrayList<>();
            source.add(new Permutation(new int[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9}));
            source.add(new Permutation(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 0, 1}));
            BSGSAlgorithms.randomness(source);

            int[] histo = new int[n];
            int COUNT = 1000;
            Permutation temp;
            for (int i = 0; i < COUNT; ++i) {
                temp = BSGSAlgorithms.random(source);
                for (int k = 0; k < n; ++k) {
                    if (temp.permutation[k] == k)
                        ++histo[k];
                }
            }

            double expected = 1.0 / ((double) n);
            double[] probabilities = new double[n];
            for (int i = 0; i < n; ++i) {
                probabilities[i] = ((double) histo[i]) / ((double) COUNT);
                assertTrue(expected - 0.2 <= probabilities[i] && probabilities[i] <= expected + 0.2);
            }
            //System.out.println(Arrays.toString(probabilities));
        }
    }

    private static void assertSameGroups(PermutationGroup p1, PermutationGroup p2) {
        for (Permutation a : p1.generators())
            assertTrue(p2.isMember(a));
        for (Permutation a : p2.generators())
            assertTrue(p1.isMember(a));
    }

}


