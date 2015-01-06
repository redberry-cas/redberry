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

import cc.redberry.core.combinatorics.IntTuplesPort;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.NumberUtils;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static cc.redberry.core.TAssert.assertTrue;
import static java.lang.System.currentTimeMillis;

import static cc.redberry.core.groups.permutations.RandomPermutation.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@Ignore
public class RandomPermutationTest {


    @Test
    public void testRandomGet1() {

        for (int tt = 0; tt < 10; ++tt) {
            int n = 10;
            List<Permutation> source = new ArrayList<>();
            source.add(Permutations.createPermutation(new int[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9}));
            source.add(Permutations.createPermutation(new int[]{2, 3, 4, 5, 6, 7, 8, 9, 0, 1}));
            randomness(source);

            int[] histo = new int[n];
            int COUNT = 1000;
            Permutation temp;
            for (int i = 0; i < COUNT; ++i) {
                temp = random(source);
                for (int k = 0; k < n; ++k) {
                    if (((PermutationOneLineInt) temp).permutation[k] == k)
                        ++histo[k];
                }
            }

            double expected = 1.0 / ((double) n);
            double[] probabilities = new double[n];
            for (int i = 0; i < n; ++i) {
                probabilities[i] = ((double) histo[i]) / ((double) COUNT);
                assertTrue(expected - 0.2 <= probabilities[i] && probabilities[i] <= expected + 0.2);
            }
        }
    }

    @Test
    public void testRandomGet2() {

        for (int tt = 0; tt < 10; ++tt) {
            //symmetric group
            int n = 10;
            List<Permutation> source = new ArrayList<>();
            source.add(Permutations.createPermutation(new int[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9}));
            source.add(Permutations.createPermutation(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0}));
            randomness(source);

            int[] histo = new int[n];
            int COUNT = 1000;
            Permutation temp;
            for (int i = 0; i < COUNT; ++i) {
                temp = random(source);
                for (int k = 0; k < n; ++k) {
                    if (((PermutationOneLineInt) temp).permutation[k] == k)
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

    private static class PrimeFactor {
        final long prime;
        final int count;

        private PrimeFactor(long prime, int count) {
            this.prime = prime;
            this.count = count;
        }

        @Override
        public String toString() {
            return "(" + prime + ", " + count + ")";
        }
    }

    public static List<PrimeFactor> primes(long n) {
        List<PrimeFactor> primes = new ArrayList<>();
        for (long i = 2; i * i <= n; i++) {
            long prime = i;
            int count = 0;
            while (n % i == 0) {
                ++count;
                n = n / i;
            }
            if (count > 0)
                primes.add(new PrimeFactor(prime, count));
        }
        if (n > 1) primes.add(new PrimeFactor(n, 1));
        return primes;
    }

    public static long numberOfDivisors(List<PrimeFactor> primes) {
        long r = 1;
        for (PrimeFactor factor : primes)
            r *= factor.count;
        return r;
    }

    @Test
    public void as() {
        List<PrimeFactor> primes = primes(ArithmeticUtils.factorial(20));
        System.out.println(primes);
        int[] counts = new int[primes.size()];
        for (int i = 0; i < primes.size(); ++i)
            counts[i] = primes.get(i).count;
        IntTuplesPort tuples = new IntTuplesPort(counts);
        int[] tuple;
        while ((tuple = tuples.take()) != null) {
            long val = 1;
            for (int i = 0; i < primes.size(); ++i) {
                val *= NumberUtils.pow(primes.get(i).prime, tuple[i]);
            }
            System.out.println(val);
        }
    }

    @Ignore
    @Test
    public void combFind() {
        long seed = currentTimeMillis();
        RandomGenerator randomGenerator = new Well1024a(seed);

        System.out.println(seed);
        int n = 15;
        List<PrimeFactor> primes;
        List<Permutation> generators = new ArrayList<>();
        List<Permutation> init = new ArrayList<>();
        long comb = Long.MAX_VALUE;
        do {
            init.clear();
            for (int i = 0; i < 2; ++i)
                init.add(Permutations.createPermutation(Permutations.randomPermutation(n, randomGenerator)));
            randomness(init, 20, 50, CC.getRandomGenerator());

            generators.clear();
            for (int i = 0; i < 3; ++i)
                generators.add(random(init));

            long bound = PermutationGroup.createPermutationGroup(generators).order().longValue();
            primes = primes(bound);
            long old = comb;
            comb = Math.min(comb, numberOfDivisors(primes));
            if (old != comb) {
                System.out.println(comb);
                System.out.println(primes);
                for (Permutation p : generators)
                    System.out.println("allgenerators.add(new Permutation(" + p.toString().substring(2, p.toString().length() - 1) + "));");
            }

        } while (primes.size() < 8 || numberOfDivisors(primes) > 100000 || primes.get(0).count > 10);
    }

    @Test
    public void testRandomGet3() {
        int n = 15;
        long seed = currentTimeMillis();
        List<Permutation> allgenerators = new ArrayList<>();
        allgenerators.add(Permutations.createPermutation(5, 0, 9, 3, 7, 11, 2, 8, 14, 6, 12, 10, 1, 4, 13));
        allgenerators.add(Permutations.createPermutation(11, 5, 6, 3, 4, 1, 13, 7, 9, 2, 12, 10, 0, 8, 14));
        allgenerators.add(Permutations.createPermutation(10, 0, 2, 3, 8, 5, 6, 14, 13, 7, 11, 12, 1, 9, 4));

        TLongSet orders = new TLongHashSet();
        List<Permutation> generators = new ArrayList<>();
        while (orders.size() < 6) {
            randomness(allgenerators, 20, 50, CC.getRandomGenerator());
            generators.clear();
            for (int i = 0; i < 2; ++i)
                generators.add(random(allgenerators));

            PermutationGroup pg = PermutationGroup.createPermutationGroup(generators);
            long order = pg.order().longValue();
            if (order > 10_000_000_000L)
                continue;

            if (orders.contains(order))
                continue;

            System.out.println("Order " + order);
            orders.add(order);


            long[][] counts = new long[n][n];
            for (Permutation pp : pg) {
                for (int i = 0; i < n; ++i) {
                    ++counts[i][pp.newIndexOf(i)];
                }
            }
            double[][] expectedProbabilities = new double[n][n];
            for (int i = 0; i < n; ++i) {
                for (int j = 0; j < n; ++j)
                    expectedProbabilities[i][j] = ((double) counts[i][j]) / ((double) order);
            }

//            System.out.println("Expected probabilities:");
//            for (int i = 0; i < n; ++i) {
//                System.out.println("" + i + " " + Arrays.toString(expectedProbabilities[i]));
//            }

            List<Permutation> source = new ArrayList<>(allgenerators);
            randomness(source);
            int[][] histo = new int[n][n];
            int COUNT = 100000;
            Permutation temp;
            for (int i = 0; i < COUNT; ++i) {
                temp = random(source);
                for (int k = 0; k < n; ++k) {
                    ++histo[k][temp.newIndexOf(k)];
                }
            }

            double[][] var = new double[n][n];

            for (int i = 0; i < n; ++i)
                for (int j = 0; j < n; ++j)
                    if (expectedProbabilities[i][j] != 0)
                        var[i][j] = Math.abs((((double) histo[i][j]) / ((double) COUNT) - expectedProbabilities[i][j]) / expectedProbabilities[i][j]);
            System.out.println("Deviations:");
            for (int i = 0; i < n; ++i)
                System.out.println("" + i + " " + Arrays.toString(var[i]));

            for (double[] dev : var)
                for (double d : dev)
                    assertTrue(d < 0.1);


            System.out.println();
        }
    }

    @Test
    public void testExample1() {
//primitive permutation group with 5616 elements
Permutation perm1 = Permutations.createPermutation(9, 1, 2, 0, 4, 8, 5, 11, 6, 3, 10, 12, 7);
Permutation perm2 = Permutations.createPermutation(2, 0, 1, 8, 3, 5, 7, 11, 4, 12, 9, 6, 10);
ArrayList<Permutation> generators = new ArrayList<>(Arrays.asList(perm1, perm2));
//we'll use a list of generators as a source of randomness
//this brings some randomization in generators list
RandomPermutation.randomness(generators);
Set<Permutation> set = new HashSet<>();
int k = 5616;
//choosing 5616 random elements
for (; k > 0; --k)
    set.add(RandomPermutation.random(generators));
//uniform
System.out.println(set.size());//~3500
    }
}
