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
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.IntArrayList;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937a;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Permutations {
    /**
     * Calculates parity of specified permutation
     *
     * @param permutation permutation
     * @return parity
     */
    public static int parity(int[] permutation) {
        //we shall decompose this permutation into product of cycles and calculate l.c.m. of their sizes

        //to mark viewed points
        BitArray used = new BitArray(permutation.length);
        //lcm
        int start, pointer, currentSize, counter = 0;
        int numOfTranspositions = 0;
        //while not all points are seen
        //loop over cycles
        while (counter < permutation.length) {
            //get first point that was not already traversed
            start = pointer = used.nextZeroBit(0);
            currentSize = 0;
            //processing current cycle
            //loop over current cycle
            do {
                assert !used.get(pointer);
                used.set(pointer);
                pointer = permutation[pointer];
                ++currentSize;
            } while (pointer != start);
            counter += currentSize;
            numOfTranspositions += currentSize - 1;
        }
        return numOfTranspositions % 2;
    }

    /**
     * Returns true if specified permutation, written in one-line notation, is identity
     *
     * @param permutation permutation in one-line notation
     * @return true if specified permutation is identity
     */
    public static boolean isIdentity(final int[] permutation) {
        for (int i = 0; i < permutation.length; ++i)
            if (i != permutation[i])
                return false;
        return true;
    }

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     * and in case of negative sign that its order is even
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and if sign is true
     * that its order is even
     */
    public static boolean testPermutationCorrectness(int[] permutation, boolean sign) {
        return testPermutationCorrectness(permutation) && (sign ? !orderOfPermutationIsOdd(permutation) : true);
    }

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and {@code false} if
     * not
     */
    public static boolean testPermutationCorrectness(int[] permutation) {
        int length = permutation.length;
        BitArray checked = new BitArray(length);
        for (int i = 0; i < length; ++i) {
            if (permutation[i] >= length || permutation[i] < 0)
                return false;
            if (checked.get(permutation[i]))
                return false;
            checked.set(permutation[i]);
        }
        return checked.isFull();
    }

    /**
     * Calculates the order of specified permutation. Since the maximum order g(n) of permutation in symmetric group
     * S(n) is about log(g(n)) <= sqrt(n log(n))* (1 + log log(n) / (2 log(n))), then g(n) can be very big (e.g.
     * for n = 1000, g(n) ~1e25). The algorithm decomposes permutation into product of cycles and returns l.c.m. of their sizes.
     *
     * @param permutation
     * @return order of specified permutation
     */
    public static BigInteger orderOfPermutation(int[] permutation) {
        //we shall decompose this permutation into product of cycles and calculate l.c.m. of their sizes

        //to mark viewed points
        BitArray used = new BitArray(permutation.length);
        //lcm
        BigInteger lcm = BigInteger.ONE, temp;

        int start, pointer, currentSize, counter = 0;
        //while not all points are seen
        //loop over cycles
        while (counter < permutation.length) {
            //get first point that was not already traversed
            start = pointer = used.nextZeroBit(0);
            currentSize = 0;
            //processing current cycle
            //loop over current cycle
            do {
                assert !used.get(pointer);
                used.set(pointer);
                pointer = permutation[pointer];
                ++currentSize;
            } while (pointer != start);
            counter += currentSize;
            temp = BigInteger.valueOf(currentSize);
            //calculate l.c.m.
            lcm = (lcm.divide(lcm.gcd(temp))).multiply(temp);
        }
        return lcm;
    }

    /**
     * Returns true if order of specified permutation is odd and false otherwise. This algorithm is very fast since
     * it does not compute order of element, but calculates just its parity without use of any "hard" computations
     * with g.c.d./l.c.m./BigInteger arithmetics etc.
     *
     * @param permutation permutation
     * @return true if order of specified permutation is odd and false otherwise
     */
    public static boolean orderOfPermutationIsOdd(final int[] permutation) {
        //decompose this permutation into product of cycles and calculate parity of l.c.m. of their sizes

        //to mark viewed points
        BitArray used = new BitArray(permutation.length);
        int start, pointer, currentSize, counter = 0;
        //while not all points are seen
        //loop over cycles
        while (counter < permutation.length) {
            //get first point that was not already traversed
            start = pointer = used.nextZeroBit(0);
            currentSize = 0;
            //processing current cycle
            //loop over current cycle
            do {
                assert !used.get(pointer);
                used.set(pointer);
                pointer = permutation[pointer];
                ++currentSize;
            } while (pointer != start);
            if (currentSize % 2 == 0)
                return false;
            counter += currentSize;
        }

        //all sizes are odd
        return true;
    }

    /**
     * Returns an orbit of specified point
     *
     * @param generators a list of group generators
     * @param point      point
     * @return orbit of specified point
     */
    public static IntArrayList getOrbitList(Collection<Permutation> generators, int point) {
        //orbit as list
        IntArrayList orbitList = new IntArrayList();
        orbitList.add(point);
        if (generators.isEmpty())
            return orbitList;//throw new IllegalArgumentException("Empty generators.");
        //seen points
        BitArray seen = new BitArray(generators.iterator().next().degree());
        seen.set(point);
        int imageOfPoint;
        //main loop over all points in orbit
        for (int orbitIndex = 0; orbitIndex < orbitList.size(); ++orbitIndex) {
            //loop over all generators of a group
            for (Permutation generator : generators) {
                //image of point under permutation
                imageOfPoint = generator.newIndexOf(orbitList.get(orbitIndex));
                //testing whether current permutation maps orbit point into orbit or not
                if (!seen.get(imageOfPoint)) {
                    //adding new point to orbit
                    orbitList.add(imageOfPoint);
                    //filling Schreier vector
                    seen.set(imageOfPoint);
                }
            }
        }
        return orbitList;
    }


    /**
     * Returns a size of specified point orbit
     *
     * @param generators a list of group generators
     * @param point      point
     * @return size of point orbit
     */
    public static int getOrbitSize(Collection<Permutation> generators, int point) {
        return getOrbitList(generators, point).size();
    }

    public static int[][] orbits(List<Permutation> generators, final int[] positionsInOrbit) {
        if (generators.isEmpty())
            return new int[0][0];//throw new IllegalArgumentException("Empty generators.");

        ArrayList<int[]> orbits = new ArrayList<>();
        Arrays.fill(positionsInOrbit, -1);
        int seenCount = 0, orbitsIndex = 0;
        while (seenCount < positionsInOrbit.length) {
            //orbit as list
            IntArrayList orbitList = new IntArrayList();
            int point = -1;
            //first not seen point
            for (int i = 0; i < positionsInOrbit.length; ++i)
                if (positionsInOrbit[i] == -1) {
                    point = i;
                    break;
                }
            assert point != -1;
            orbitList.add(point);
            ++seenCount;
            positionsInOrbit[point] = orbitsIndex;
            int imageOfPoint;
            //main loop over all points in orbit
            for (int orbitIndex = 0; orbitIndex < orbitList.size(); ++orbitIndex) {
                //loop over all generators of a group
                for (Permutation generator : generators) {
                    //image of point under permutation
                    imageOfPoint = generator.newIndexOf(orbitList.get(orbitIndex));
                    //testing whether current permutation maps orbit point into orbit or not
                    if (positionsInOrbit[imageOfPoint] == -1) {
                        ++seenCount;
                        positionsInOrbit[imageOfPoint] = orbitsIndex;
                        //adding new point to orbit
                        orbitList.add(imageOfPoint);
                    }
                }
            }
            orbits.add(orbitList.toArray());
            ++orbitsIndex;
        }
        return orbits.toArray(new int[orbits.size()][]);
    }


    private static final int[][] cachedIdentities = new int[64][];

    private static int[] createIdentityPermutationArray(int length) {
        int[] array = new int[length];
        for (int i = 0; i < length; ++i)
            array[i] = i;
        return array;
    }

    public static int[] getIdentityPermutationArray(int length) {
        if (cachedIdentities.length <= length)
            return createIdentityPermutationArray(length);
        if (cachedIdentities[length] == null)
            synchronized (cachedIdentities) {
                if (cachedIdentities[length] == null)
                    cachedIdentities[length] = createIdentityPermutationArray(length);
            }
        return cachedIdentities[length];
    }

    public static PermutationOneLine getIdentityOneLine(int degree) {
        return new PermutationOneLine(getIdentityPermutationArray(degree));
    }

    /**
     * Creates random permutation of specified dimension
     *
     * @param n    dimension
     * @param seed random seed
     * @return random permutation of specified dimension
     */
    public static int[] randomPermutation(final int n, long seed) {
        return randomPermutation(n, new Well19937a(seed));
    }

    /**
     * Creates random permutation of specified dimension
     *
     * @param n   dimension
     * @param rnd random generator
     * @return random permutation of specified dimension
     */
    public static int[] randomPermutation(final int n, RandomGenerator rnd) {
        int[] p = new int[n];
        for (int i = 0; i < n; ++i)
            p[i] = i;
        for (int i = n; i > 1; --i)
            ArraysUtils.swap(p, i - 1, rnd.nextInt(i));
        for (int i = n; i > 1; --i)
            ArraysUtils.swap(p, i - 1, rnd.nextInt(i));
        return p;
    }

    /**
     * Creates random permutation of specified dimension
     *
     * @param n dimension
     * @return random permutation of specified dimension
     */
    public static int[] randomPermutation(final int n) {
        return randomPermutation(n, CC.getRandomGenerator());
    }

    /**
     * Converts cycles to one-line notation.
     *
     * @param degree degree of permutation
     * @param cycles disjoint cycles
     * @return permutation written in one-line notation
     */
    public static int[] convertCyclesToOneLine(final int degree, final int[][] cycles) {
        final int[] permutation = new int[degree];
        for (int i = 1; i < degree; ++i)
            permutation[i] = i;
        for (int[] cycle : cycles) {
            if (cycle.length == 0)
                continue;
            if (cycle.length == 1)
                throw new IllegalArgumentException("Illegal use of cycle notation: " + Arrays.toString(cycle));
            for (int k = 0, s = cycle.length - 1; k < s; ++k)
                permutation[cycle[k]] = cycle[k + 1];
            permutation[cycle[cycle.length - 1]] = cycle[0];
        }
        return permutation;
    }

    /**
     * Converts permutation written in one-line notation to disjoint cycles notation.
     *
     * @param permutation permutation written in one-line notation
     * @return permutation written in disjoint cycles notation
     */
    public static int[][] convertOneLineToCycles(final int[] permutation) {
        ArrayList<int[]> cycles = new ArrayList<>();
        BitArray seen = new BitArray(permutation.length);
        int counter = 0;
        while (counter < permutation.length) {
            int start = seen.nextZeroBit(0);
            if (permutation[start] == start) {
                ++counter;
                seen.set(start);
                continue;
            }
            IntArrayList cycle = new IntArrayList();
            while (!seen.get(start)) {
                seen.set(start);
                ++counter;
                cycle.add(start);
                start = permutation[start];
            }
            cycles.add(cycle.toArray());
        }
        return cycles.toArray(new int[cycles.size()][]);
    }

    /**
     * Returns an array of cycles lengths.
     *
     * @param permutation permutation written in one-line notation
     * @return an array of cycles lengths
     */
    public static int[] sizesOfCycles(final int[] permutation) {
        IntArrayList sizes = new IntArrayList();
        BitArray seen = new BitArray(permutation.length);
        int counter = 0;
        while (counter < permutation.length) {
            int start = seen.nextZeroBit(0);
            if (permutation[start] == start) {
                ++counter;
                seen.set(start);
                continue;
            }
            int size = 0;
            while (!seen.get(start)) {
                seen.set(start);
                ++counter;
                ++size;
                start = permutation[start];
            }
            sizes.add(size);
        }
        return sizes.toArray();
    }

    /**
     * Randomly permutes the specified list.
     *
     * @param a - the array to be shuffled.
     */
    public static void shuffle(int[] a) {
        shuffle(a, CC.getRandomGenerator());
    }


    /**
     * Randomly permute the specified list using the specified source of randomness.
     *
     * @param a   - the array to be shuffled.
     * @param rnd - the source of randomness to use to shuffle the list.
     */
    public static void shuffle(int[] a, RandomGenerator rnd) {
        for (int i = a.length; i > 1; --i)
            ArraysUtils.swap(a, i - 1, rnd.nextInt(i));
    }


    /**
     * Throws exception if p.length() != size.
     *
     * @param p    permutation
     * @param size size
     */
    public static void checkSizeWithException(Permutation p, int size) {
        if (p.degree() != size)
            throw new IllegalArgumentException("Different size of permutation.");
    }

    /**
     * Throws exception if a != size.
     *
     * @param a
     * @param size size
     */
    public static void checkSizeWithException(int a, int size) {
        if (a != size)
            throw new IllegalArgumentException("Different size of permutation.");
    }
}
