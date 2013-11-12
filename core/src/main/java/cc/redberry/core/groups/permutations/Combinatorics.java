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

import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.IntArrayList;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937a;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.Collection;
import java.util.Random;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Combinatorics {

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     * and in case of negative sign that its order is even
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and if sign is true
     *         that its order is even
     */
    public static boolean testPermutationCorrectness(int[] permutation, boolean sign) {
        return testPermutationCorrectness(permutation) && (sign ? !orderIsOdd(permutation) : true);
    }

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and {@code false} if
     *         not
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
    public static BigInteger order(int[] permutation) {
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
     * Returns true if order of specified permutation is odd and false otherwise.
     *
     * @param permutation permutation
     * @return true if order of specified permutation is odd and false otherwise
     */
    public static boolean orderIsOdd(final int[] permutation) {
        //we shall decompose this permutation into product of cycles and calculate l.c.m. of their sizes

        //to mark viewed points
        BitArray used = new BitArray(permutation.length);
        //sizes of cycles
        IntArrayList sizes = new IntArrayList();

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
            sizes.add(currentSize);
            counter += currentSize;
        }

        //only one cycle
        if (sizes.size() == 1)
            return sizes.get(0) % 2 == 1;

        //so we need to determine, whether lcm(sizes) if odd or even
        //using the formula:
        //lcm(a1,a2,...,aN) = (a1*a2*...*aN) / gcd( a2*a3*...*aN, a1*a3*...*aN, a1*a2*...*aN-1)

        //first test: if a1*a2*...*aN is odd, then the result is odd
        //if we now know that the numerator is even
        //lets test whether gcd(...) is odd or even
        //gcd is guarantied to be odd if at least one argument is odd (otherwise gcd is even)
        //this can be only if all sizes are odd or all sizes are odd except one

        boolean evenNumerator = false;
        boolean oddDenominator = true;
        for (int i = 0, size = sizes.size(); i < size; ++i) {
            if (sizes.get(i) % 2 == 0) {
                if (evenNumerator) {
                    oddDenominator = false;
                    break;
                }
                evenNumerator = true;
            }

        }
        if (!evenNumerator)
            return true;

        //<- so numerator is even
        if (oddDenominator) {
            //if denominator is odd
            return false;
        }

        //<- both numerator and denominator are even
        // then numerator    = 2^num * some_odd_number
        //      denominator  = 2^den * some_odd_number
        // if (num > den) then result is even, otherwise - odd
        //let's compute num and den

        int den = 0, exponent, num = extractPowerOf2(sizes.get(0));

        for (int i = 1, size = sizes.size(); i < size; ++i) {
            exponent = extractPowerOf2(sizes.get(i));
            den = Math.min(den + exponent, num); //<- trick
            num += exponent;
        }

        return num <= den;
    }

    private static int extractPowerOf2(int n) {
        int r = 0;
        while (n % 2 == 0) {
            ++r;
            n = n / 2;
        }
        return r;
    }


    /**
     * Returns a size of specified point orbit
     *
     * @param generators a list of group generators
     * @param point      point
     * @return size of point orbit
     */
    public static int getOrbitSize(Collection<Permutation> generators, int point) {
        if (generators.isEmpty())
            return 1;//throw new IllegalArgumentException("Empty generators.");
        //orbit as list
        IntArrayList orbitList = new IntArrayList();
        orbitList.add(point);
        //seen points
        BitArray seen = new BitArray(generators.iterator().next().length());
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
        return orbitList.size();
    }


    private static final Permutation[] cachedIdentities = new Permutation[64];

    public static Permutation createIdentity(int length) {
        int[] array = new int[length];
        for (int i = 0; i < length; ++i)
            array[i] = i;
        return new Permutation(true, false, array);
    }

    public static Permutation getIdentity(int length) {
        if (cachedIdentities.length < length)
            return createIdentity(length);
        if (cachedIdentities[length] == null)
            synchronized (cachedIdentities) {
                if (cachedIdentities[length] == null)
                    cachedIdentities[length] = createIdentity(length);
            }
        return cachedIdentities[length];
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
        return randomPermutation(n, new Well19937a());
    }
}
