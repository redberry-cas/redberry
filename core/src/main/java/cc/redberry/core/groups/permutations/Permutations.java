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
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.MathUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;

/**
 * Static methods to operate with permutations.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.6
 */
public final class Permutations {
    private Permutations() {
    }

    /**
     * Calculates <i>degree</i> of permutation, i.e.larges point moved by specified permutation plus one.
     *
     * @param permutation permutation
     * @return larges point moved by specified permutation plus one
     */
    public static int internalDegree(final int[] permutation) {
        int i;
        for (i = permutation.length - 1; i >= 0; --i)
            if (permutation[i] != i)
                break;
        return i + 1;
    }

    /**
     * Calculates <i>degree</i> of permutation, i.e.larges point moved by specified permutation plus one.
     *
     * @param permutation permutation
     * @return larges point moved by specified permutation plus one
     */
    public static short internalDegree(final short[] permutation) {
        int i;
        for (i = permutation.length - 1; i >= 0; --i)
            if (permutation[i] != i)
                break;
        return (short) (i + 1);
    }

    /**
     * Calculates <i>degree</i> of permutation, i.e.larges point moved by specified permutation plus one.
     *
     * @param permutation permutation
     * @return larges point moved by specified permutation plus one
     */
    public static byte internalDegree(final byte[] permutation) {
        int i;
        for (i = permutation.length - 1; i >= 0; --i)
            if (permutation[i] != i)
                break;
        return (byte) (i + 1);
    }

    /**
     * Calculates common <i>degree</i> of specified permutations, i.e.larges point moved by specified permutations plus
     * one.
     *
     * @param permutations permutations
     * @return larges point moved by specified permutations plus one
     */
    public static int internalDegree(final List<? extends Permutation> permutations) {
        int r = 0;
        for (Permutation p : permutations)
            r = Math.max(r, p.degree());
        return r;
    }

    /**
     * Calculates parity of specified permutation
     *
     * @param permutation permutation
     * @return parity of permutation
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
     * Calculates parity of specified permutation
     *
     * @param permutation permutation
     * @return parity of permutation
     */
    public static int parity(short[] permutation) {
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
     * Calculates parity of specified permutation
     *
     * @param permutation permutation
     * @return parity of permutation
     */
    public static int parity(byte[] permutation) {
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
     * Returns true if specified permutation, written in one-line notation, is identity and false otherwise.
     *
     * @param permutation permutation in one-line notation
     * @return true if specified permutation is identity and false otherwise
     */
    public static boolean isIdentity(final int[] permutation) {
        for (int i = 0; i < permutation.length; ++i)
            if (i != permutation[i])
                return false;
        return true;
    }

    /**
     * Returns true if specified permutation, written in one-line notation, is identity and false otherwise.
     *
     * @param permutation permutation in one-line notation
     * @return true if specified permutation is identity and false otherwise
     */
    public static boolean isIdentity(final short[] permutation) {
        for (int i = 0; i < permutation.length; ++i)
            if (i != permutation[i])
                return false;
        return true;
    }

    /**
     * Returns true if specified permutation, written in one-line notation, is identity and false otherwise.
     *
     * @param permutation permutation in one-line notation
     * @return true if specified permutation is identity and false otherwise
     */
    public static boolean isIdentity(final byte[] permutation) {
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
     * and in case of negative sign that its order is even
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and if sign is true
     * that its order is even
     */
    public static boolean testPermutationCorrectness(short[] permutation, boolean sign) {
        return testPermutationCorrectness(permutation) && (sign ? !orderOfPermutationIsOdd(permutation) : true);
    }

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     * and in case of negative sign that its order is even
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and if sign is true
     * that its order is even
     */
    public static boolean testPermutationCorrectness(byte[] permutation, boolean sign) {
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
     * Tests whether the specified array satisfies the one-line notation for permutations
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and {@code false} if
     * not
     */
    public static boolean testPermutationCorrectness(short[] permutation) {
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
     * Tests whether the specified array satisfies the one-line notation for permutations
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line notation for permutations and {@code false} if
     * not
     */
    public static boolean testPermutationCorrectness(byte[] permutation) {
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
     * Calculates the order of specified permutation. Since the maximum order g(n) of permutation in symmetric group
     * S(n) is about log(g(n)) <= sqrt(n log(n))* (1 + log log(n) / (2 log(n))), then g(n) can be very big (e.g.
     * for n = 1000, g(n) ~1e25). The algorithm decomposes permutation into product of cycles and returns l.c.m. of their sizes.
     *
     * @param permutation
     * @return order of specified permutation
     */
    public static BigInteger orderOfPermutation(short[] permutation) {
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
     * Calculates the order of specified permutation. Since the maximum order g(n) of permutation in symmetric group
     * S(n) is about log(g(n)) <= sqrt(n log(n))* (1 + log log(n) / (2 log(n))), then g(n) can be very big (e.g.
     * for n = 1000, g(n) ~1e25). The algorithm decomposes permutation into product of cycles and returns l.c.m. of their sizes.
     *
     * @param permutation
     * @return order of specified permutation
     */
    public static BigInteger orderOfPermutation(byte[] permutation) {
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
     * Returns true if order of specified permutation is odd and false otherwise. This algorithm is very fast since
     * it does not compute order of element, but calculates just its parity without use of any "hard" computations
     * with g.c.d./l.c.m./BigInteger arithmetics etc.
     *
     * @param permutation permutation
     * @return true if order of specified permutation is odd and false otherwise
     */
    public static boolean orderOfPermutationIsOdd(final short[] permutation) {
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
     * Returns true if order of specified permutation is odd and false otherwise. This algorithm is very fast since
     * it does not compute order of element, but calculates just its parity without use of any "hard" computations
     * with g.c.d./l.c.m./BigInteger arithmetics etc.
     *
     * @param permutation permutation
     * @return true if order of specified permutation is odd and false otherwise
     */
    public static boolean orderOfPermutationIsOdd(final byte[] permutation) {
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
    public static IntArrayList getOrbitList(List<Permutation> generators, int point) {
        return getOrbitList(generators, point, internalDegree(generators));
    }

    /**
     * Returns an orbit of specified point
     *
     * @param generators a list of group generators
     * @param point      point
     * @param degree     largest integer moved by the generators plus one or bigger
     * @return orbit of specified point
     */
    public static IntArrayList getOrbitList(Collection<Permutation> generators, int point, int degree) {
        //orbit as list
        IntArrayList orbitList = new IntArrayList();
        orbitList.add(point);
        if (generators.isEmpty())
            return orbitList;//throw new IllegalArgumentException("Empty generators.");
        //seen points
        BitArray seen = new BitArray(degree);
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
     * @param degree     largest integer moved by the generators plus one or bigger
     * @return size of point orbit
     */
    public static int getOrbitSize(List<Permutation> generators, int point, int degree) {
        return getOrbitList(generators, point, degree).size();
    }

    /**
     * Returns a size of specified point orbit
     *
     * @param generators a list of group generators
     * @param point      point
     * @return size of point orbit
     */
    public static int getOrbitSize(List<Permutation> generators, int point) {
        return getOrbitList(generators, point, internalDegree(generators)).size();
    }

    /**
     * Calculates orbits of specified generators.
     *
     * @param generators       permutations
     * @param positionsInOrbit an array that will be filled with the indexes in the resulting orbits, such that
     *                         for any point orbits[positionsInOrbit[point]] - is orbit of this point.
     * @return orbits
     */
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

    /**
     * Converts cycles to one-line notation.
     *
     * @param cycles disjoint cycles
     * @return permutation written in one-line notation
     */
    public static int[] convertCyclesToOneLine(final int[][] cycles) {
        int degree = -1;
        for (int[] cycle : cycles)
            degree = Math.max(degree, ArraysUtils.max(cycle));
        ++degree;
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
     * Converts permutation written in one-line notation to disjoint cycles notation.
     *
     * @param permutation permutation written in one-line notation
     * @return permutation written in disjoint cycles notation
     */
    public static int[][] convertOneLineToCycles(final short[] permutation) {
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
     * Converts permutation written in one-line notation to disjoint cycles notation.
     *
     * @param permutation permutation written in one-line notation
     * @return permutation written in disjoint cycles notation
     */
    public static int[][] convertOneLineToCycles(final byte[] permutation) {
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
    public static int[] lengthsOfCycles(final int[] permutation) {
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
     * Returns an array of cycles lengths.
     *
     * @param permutation permutation written in one-line notation
     * @return an array of cycles lengths
     */
    public static int[] lengthsOfCycles(final short[] permutation) {
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
     * Returns an array of cycles lengths.
     *
     * @param permutation permutation written in one-line notation
     * @return an array of cycles lengths
     */
    public static int[] lengthsOfCycles(final byte[] permutation) {
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

    /***************************************** RANDOM *****************************************************/

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
     * Randomly permutes the specified array.
     *
     * @param a - the array to be shuffled.
     */
    public static void shuffle(int[] a) {
        shuffle(a, CC.getRandomGenerator());
    }


    /**
     * Randomly permutes the specified list using the specified source of randomness.
     *
     * @param a   - the array to be shuffled.
     * @param rnd - the source of randomness to use to shuffle the list.
     */
    public static void shuffle(int[] a, RandomGenerator rnd) {
        for (int i = a.length; i > 1; --i)
            ArraysUtils.swap(a, i - 1, rnd.nextInt(i));
    }

    /**
     * Randomly permutes the specified list using the specified source of randomness.
     *
     * @param a   - the array to be shuffled.
     * @param rnd - the source of randomness to use to shuffle the list.
     */
    public static void shuffle(Object[] a, RandomGenerator rnd) {
        for (int i = a.length; i > 1; --i)
            ArraysUtils.swap(a, i - 1, rnd.nextInt(i));
    }


    /**
     * Randomly permute the specified list using the specified source of randomness.
     *
     * @param a - the array to be shuffled.
     */
    public static void shuffle(Object[] a) {
        shuffle(a, CC.getRandomGenerator());
    }

    /**
     * **************************************** FACTORIES **************************************************
     */

    /**
     * Creates permutation instance from a given array that represents permutation in disjoint cycle notation.
     * <p>This method will automatically choose an appropriate underlying implementation of Permutation depending on
     * the permutation length.</p>
     * <p>If order of specified permutation is odd and antisymmetry is specified, then exception will thrown, since
     * such antisymmetry is impossible from the mathematical point of view.</p>
     *
     * @param antisymmetry if true, then antisymmetry will be created
     * @param cycles       array of disjoint cycles
     * @return an instance of {@code Permutation}
     * @throws java.lang.IllegalArgumentException if specified array is inconsistent with disjoint cycle notation
     * @throws IllegalArgumentException           if antisymmetry is true and permutation order is odd
     */
    public static Permutation createPermutation(boolean antisymmetry, int[][] cycles) {
        return createPermutation(antisymmetry, convertCyclesToOneLine(cycles));
    }

    /**
     * Creates permutation instance from a given array that represents permutation in disjoint cycle notation.
     * <p>This method will automatically choose an appropriate underlying implementation of Permutation depending on
     * the permutation length.</p>
     *
     * @param cycles array of disjoint cycles
     * @return an instance of {@code Permutation}
     * @throws java.lang.IllegalArgumentException if specified array is inconsistent with disjoint cycle notation
     */
    public static Permutation createPermutation(int[][] cycles) {
        return createPermutation(false, convertCyclesToOneLine(cycles));
    }

    /**
     * Creates permutation instance from a given array that represents permutation in one-line notation.
     * <p>This method will automatically choose an appropriate underlying implementation of Permutation depending on
     * the permutation length.</p>
     *
     * @param oneLine array that represents permutation in one line notation
     * @return an instance of {@code Permutation}
     * @throws java.lang.IllegalArgumentException if specified array is inconsistent with one-line notation
     */
    public static Permutation createPermutation(int... oneLine) {
        return createPermutation(false, oneLine);
    }

    /**
     * Creates permutation instance from a given array that represents permutation in one-line notation.
     * <p>This method will automatically choose an appropriate underlying implementation of Permutation depending on
     * the permutation length.</p>
     * <p>If order of specified permutation is odd and antisymmetry is specified, then exception will thrown, since
     * such antisymmetry is impossible from the mathematical point of view.</p>
     *
     * @param antisymmetry if true, then antisymmetry will be created
     * @param oneLine      array that represents permutation in one line notation
     * @return an instance of {@code Permutation}
     * @throws java.lang.IllegalArgumentException if specified array is inconsistent with one-line notation
     * @throws IllegalArgumentException           if antisymmetry is true and permutation order is odd
     */
    public static Permutation createPermutation(boolean antisymmetry, int... oneLine) {
        boolean _byte = true, _short = true;
        for (int i : oneLine) {
            if (i > Short.MAX_VALUE - 1) {  //-1 is because internalDegree calculated as largest moved point + 1
                _short = false;
                _byte = false;
            } else if (i > Byte.MAX_VALUE - 1) _byte = false;
        }
        if (_byte)
            return new PermutationOneLineByte(antisymmetry, ArraysUtils.int2byte(oneLine));
        if (_short)
            return new PermutationOneLineShort(antisymmetry, ArraysUtils.int2short(oneLine));
        return new PermutationOneLineInt(antisymmetry, oneLine);
    }

    /**
     * Permutes specified array according to specified permutation and returns the result.
     *
     * @param array       array
     * @param permutation permutation in one-line notation
     * @param <T>         any type
     * @return new array permuted with specified permutation
     * @throws IllegalArgumentException if array length not equals to permutation length
     * @throws IllegalArgumentException if permutation is not consistent with one-line notation
     */
    public static <T> T[] permute(T[] array, final int[] permutation) {
        if (array.length != permutation.length)
            throw new IllegalArgumentException();
        if (!testPermutationCorrectness(permutation))
            throw new IllegalArgumentException();
        Class<?> type = array.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
                T[] newArray = (T[]) Array.newInstance(type, array.length);
        for (int i = 0; i < permutation.length; ++i)
            newArray[i] = array[permutation[i]];
        return newArray;
    }

    /**
     * Permutes specified list according to specified permutation and returns the result.
     *
     * @param array       array
     * @param permutation permutation in one-line notation
     * @param <T>         any type
     * @return new array permuted with specified permutation
     * @throws IllegalArgumentException if array length not equals to permutation length
     * @throws IllegalArgumentException if permutation is not consistent with one-line notation
     */
    public static <T> List<T> permute(List<T> array, final int[] permutation) {
        if (array.size() != permutation.length)
            throw new IllegalArgumentException();
        if (!testPermutationCorrectness(permutation))
            throw new IllegalArgumentException();
        final List<T> list = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); ++i)
            list.add(array.get(permutation[i]));
        return list;
    }

    /**
     * Permutes specified array according to specified permutation and returns the result.
     *
     * @param array       array
     * @param permutation permutation in one-line notation
     * @return new array permuted with specified permutation
     * @throws IllegalArgumentException if array length not equals to permutation length
     * @throws IllegalArgumentException if permutation is not consistent with one-line notation
     */
    public static int[] permute(int[] array, final int[] permutation) {
        if (array.length != permutation.length)
            throw new IllegalArgumentException();
        if (!testPermutationCorrectness(permutation))
            throw new IllegalArgumentException();
        int[] newArray = new int[array.length];
        for (int i = 0; i < permutation.length; ++i)
            newArray[i] = array[permutation[i]];
        return newArray;
    }

    public static int[] getRandomSortedDistinctArray(final int minValue, final int maxvalue, int length, RandomGenerator generator) {
        if (maxvalue - minValue < length)
            throw new IllegalArgumentException("This is not possible.");
        if (length == 0)
            return new int[0];
        if (length == 1)
            return new int[]{minValue + generator.nextInt(maxvalue - minValue)};
        if (length == 2) {
            int a = minValue + generator.nextInt(maxvalue - minValue);
            int b;
            while ((b = minValue + generator.nextInt(maxvalue - minValue)) == a) ;
            return new int[]{a, b};
        }

        int[] res = new int[length + (int) (0.7 * ((double) length))];
        for (int i = 0; i < res.length; ++i)
            res[i] = minValue + generator.nextInt(maxvalue - minValue);
        res = MathUtils.getSortedDistinct(res);
        if (res.length == length)
            return res;
        if (res.length > length)
            return Arrays.copyOf(res, length);

        while (res.length != length) {
            int next;
            while ((Arrays.binarySearch(res, next = minValue + generator.nextInt(maxvalue - minValue))) >= 0) ;
            res = ArraysUtils.addAll(res, next);
        }

        return res;
    }

    /**
     * Creates array that represents identity one-line permutation of specified degree.
     *
     * @param length degree of permutation (array length)
     * @return array that represents identity one-line permutation of specified degree
     */
    public static int[] createIdentityArray(int length) {
        int[] array = new int[length];
        for (int i = 0; i < length; ++i)
            array[i] = i;
        return array;
    }

    /**
     * Cached identities
     */
    private static final Permutation[] cachedIdentities = new Permutation[128];

    /**
     * Creates identity permutation with with specified degree.
     *
     * @param degree size of internal buffer of permutation
     * @return identity permutation
     */
    public static Permutation createIdentityPermutation(int degree) {
        if (degree < cachedIdentities.length) {
            if (cachedIdentities[degree] == null)
                cachedIdentities[degree] = Permutations.createPermutation(createIdentityArray(degree));
            return cachedIdentities[degree];
        }
        return Permutations.createPermutation(createIdentityArray(degree));
    }

    /**
     * Default (optimal for average problem) value of identity permutation length
     */
    public static final int DEFAULT_IDENTITY_LENGTH = 10;

    /**
     * Returns identity permutation.
     *
     * @return identity permutation
     */
    public static Permutation getIdentityPermutation() {
        return createIdentityPermutation(DEFAULT_IDENTITY_LENGTH);
    }

    /**
     * Creates transposition of first two elements written in one-line notation
     * with specified dimension, i.e. an array of form [1,0,2,3,4,...,{@code dimension - 1}].
     *
     * @param dimension dimension of the resulting permutation, e.g. the array length
     * @return transposition permutation in one-line notation
     */
    public static int[] createTransposition(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException("Dimension is negative.");
        if (dimension > 1)
            return createTransposition(dimension, 0, 1);
        return new int[dimension];
    }

    /**
     * Creates transposition in one-line notation
     *
     * @param dimension dimension of the resulting permutation, e.g. the array length
     * @param position1 first position
     * @param position2 second position
     * @return transposition
     */
    public static int[] createTransposition(int dimension, int position1, int position2) {
        if (dimension < 0)
            throw new IllegalArgumentException("Dimension is negative.");
        if (position1 < 0 || position2 < 0)
            throw new IllegalArgumentException("Negative index.");
        if (position1 >= dimension || position2 >= dimension)
            throw new IndexOutOfBoundsException();

        int[] transposition = new int[dimension];
        int i = 1;
        for (; i < dimension; ++i)
            transposition[i] = i;
        i = transposition[position1];
        transposition[position1] = transposition[position2];
        transposition[position2] = i;
        return transposition;
    }

    /**
     * Creates cycle permutation written in one-line notation,
     * i.e. an array of form [{@code dimension-1},0,1, ...,{@code dimension-2}].
     *
     * @param dimension dimension of the resulting permutation, e.g. the array length
     * @return cycle permutation in one-line notation
     */
    public static int[] createCycle(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException("Negative degree");

        int[] cycle = new int[dimension];
        for (int i = 0; i < dimension - 1; ++i)
            cycle[i + 1] = i;
        cycle[0] = dimension - 1;
        return cycle;
    }


    public static int[] createBlockCycle(int blockSize, int numberOfBlocks) {
        final int[] cycle = new int[blockSize * numberOfBlocks];

        int i = blockSize * (numberOfBlocks - 1) - 1;
        for (; i >= 0; --i) cycle[i] = i + blockSize;
        i = blockSize * (numberOfBlocks - 1);
        int k = 0;
        for (; i < cycle.length; ++i)
            cycle[i] = k++;

        return cycle;
    }

    public static int[] createBlockTransposition(final int length1, final int length2) {
        final int[] r = new int[length1 + length2];
        int i = 0;
        for (; i < length2; ++i) {
            r[i] = length1 + i;
        }
        for (; i < r.length; ++i)
            r[i] = i - length2;
        return r;
    }

    /**
     * Returns the inverse permutation for the specified one.
     * <p/>
     * <p>One-line notation for permutations is used.</p>
     *
     * @param permutation permutation in one-line notation
     * @return inverse permutation to the specified one
     */
    public static int[] inverse(int[] permutation) {
        int[] inverse = new int[permutation.length];
        for (int i = 0; i < permutation.length; ++i)
            inverse[permutation[i]] = i;
        return inverse;
    }
}
