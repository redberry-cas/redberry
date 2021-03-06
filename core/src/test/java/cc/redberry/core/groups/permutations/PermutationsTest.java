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
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Timing;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.SchreierSimsAlgorithm;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.createRawBSGSCandidate;
import static cc.redberry.core.groups.permutations.RandomPermutation.random;
import static cc.redberry.core.groups.permutations.RandomPermutation.randomness;
import static java.lang.System.currentTimeMillis;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsTest {

    @Test
    public void testOrbitSize1() {
        long seed = currentTimeMillis();
        int n = 20;
        int COUNT = 100;
        RandomGenerator randomGenerator = new Well1024a(seed);
        List<Permutation> source = new ArrayList<>();
        for (int i = 0; i < 10; ++i)
            source.add(Permutations.createPermutation(Permutations.randomPermutation(n, randomGenerator)));
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
            for (BSGSCandidateElement element : bsgs)
                assertEquals(Permutations.getOrbitSize(element.stabilizerGenerators, element.basePoint), element.orbitSize());
        }
    }

    @Test
    public void testOrbits1() {
        final DescriptiveStatistics stat_timing = new DescriptiveStatistics(), stat_orbits = new DescriptiveStatistics();

        final int degree = 40;
        for (int C = 0; C < 50; ++C) {
            List<Permutation> source = new ArrayList<>();
            for (int i = 0; i < 10; ++i)
                source.add(Permutations.createPermutation(Permutations.randomPermutation(degree, CC.getRandomGenerator())));
            randomness(source, 10, 50, CC.getRandomGenerator());

            for (int t = 0; t < 50; ++t) {
                final List<Permutation> generators = new ArrayList<>();
                for (int i = 0; i < 2; ++i)
                    generators.add(random(source, CC.getRandomGenerator()));
                long timing = (long) Timing.timing(
                        new Timing.TimingJob() {
                            @Override
                            public Object doJob() {
                                int[] positions = new int[degree];
                                int[][] orbits = Permutations.orbits(generators, positions);
                                stat_orbits.addValue(orbits.length);
                                assertOrbits(orbits, positions, degree, generators);
                                return null;
                            }
                        }, false
                )[0];
                stat_timing.addValue(timing);
            }
        }
        System.out.println("Timing:\n " + stat_timing);
        System.out.println("Number of orbits:\n " + stat_orbits);
    }

    @Test
    public void testOrbits2() {
        RandomGenerator rndm = CC.getRandomGenerator();
        final DescriptiveStatistics stat_timing = new DescriptiveStatistics(), stat_orbits = new DescriptiveStatistics();

        final int degree = 60;
        for (int C = 0; C < 50; ++C) {
            List<Permutation> source = new ArrayList<>();


            int avOrbLength = 10;

            for (int i = 0; i < 10; ++i) {
                int[] perm = new int[degree];
                for (int k = 1; k < perm.length; ++k)
                    perm[k] = k;

                int prev = 0, j, s;
                do {
                    s = 1 + avOrbLength;
                    if (prev + s >= degree)
                        s = degree - prev;
                    for (j = 0; j < 5; ++j)
                        ArraysUtils.swap(perm, prev + rndm.nextInt(s), prev + rndm.nextInt(s));
                    prev += s + 1;
                } while (prev < degree);
                source.add(Permutations.createPermutation(perm));
            }

            randomness(source, 10, 50, rndm);

            for (int t = 0; t < 50; ++t) {
                final List<Permutation> generators = new ArrayList<>();
                for (int i = 0; i < 2; ++i)
                    generators.add(random(source, rndm));
                long timing = (long) Timing.timing(
                        new Timing.TimingJob() {
                            @Override
                            public Object doJob() {
                                int[] positions = new int[degree];
                                int[][] orbits = Permutations.orbits(generators, positions);
                                stat_orbits.addValue(orbits.length);
                                assertOrbits(orbits, positions, degree, generators);
                                return null;
                            }
                        }, false
                )[0];
                stat_timing.addValue(timing);
            }
        }
        System.out.println("Timing:\n " + stat_timing);
        System.out.println("Number of orbits:\n " + stat_orbits);
    }

    @Test
    public void testParity() {
        int[] p = {0, 1, 2, 3};
        assertEquals(0, Permutations.parity(p));
        p = new int[]{1, 0, 2, 3};
        assertEquals(1, Permutations.parity(p));
        p = new int[]{1, 0, 3, 2};
        assertEquals(0, Permutations.parity(p));
        p = new int[]{3, 2, 0, 1};
        assertEquals(1, Permutations.parity(p));
    }

    @Test
    public void testConvert() {
        for (int i = 0; i < 1000; ++i) {
            int degree = 1 + CC.getRandomGenerator().nextInt(100);
            int[] p = Permutations.randomPermutation(degree);
            int[][] cycles = Permutations.convertOneLineToCycles(p);
            int[] r = Permutations.convertCyclesToOneLine(cycles);
            int len = Math.min(r.length, p.length);
            int j = 0;
            for (; j < len; ++j)
                Assert.assertEquals(p[j], r[j]);
            int t = j;
            for (; t < r.length; ++t)
                Assert.assertEquals(t, r[t]);
            t = j;
            for (; t < r.length; ++t)
                Assert.assertEquals(t, r[t]);

        }
    }

    @Test
    public void testSizesOfCycles1() {
        for (int i = 0; i < 1000; ++i) {
            int degree = 1 + CC.getRandomGenerator().nextInt(100);
            int[] p = Permutations.randomPermutation(degree);
            int[][] cycles = Permutations.convertOneLineToCycles(p);
            int[] sizes = new int[cycles.length];
            for (int j = 0; j < sizes.length; ++j)
                sizes[j] = cycles[j].length;
            int[] sizes1 = Permutations.lengthsOfCycles(p);
            Arrays.sort(sizes);
            Arrays.sort(sizes1);
            Assert.assertArrayEquals(sizes, sizes1);
        }
    }

    private static void assertOrbits(int[][] orbits, int[] positions, int degree, List<Permutation> generators) {
        for (int i = 0; i < degree; ++i) {
            int[] expected = Permutations.getOrbitList(generators, i).toArray();
            int[] actual = orbits[positions[i]].clone();
            Arrays.sort(actual);
            Arrays.sort(expected);
            Assert.assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void testCycle() {
        int[] c = Permutations.createBlockCycle(3, 4);
        Assert.assertArrayEquals(c, new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11, 0, 1, 2});
        int[] perm = {3, 4, 5, 0, 1, 2, 6, 7, 8, 9, 10, 11};
        PermutationGroup g = PermutationGroup.createPermutationGroup(Permutations.createPermutation(c), Permutations.createPermutation(perm));
        Assert.assertEquals(24, g.order().intValue());
    }

    @Test
    public void testBlockTransposition() {
        Assert.assertArrayEquals(Permutations.createBlockTransposition(2, 3), new int[]{2, 3, 4, 0, 1});
        Assert.assertArrayEquals(Permutations.createBlockTransposition(0, 3), new int[]{0, 1, 2});
        Assert.assertArrayEquals(Permutations.createBlockTransposition(3, 0), new int[]{0, 1, 2});
        Assert.assertArrayEquals(Permutations.createBlockTransposition(3, 3), new int[]{3, 4, 5, 0, 1, 2});
    }
}
