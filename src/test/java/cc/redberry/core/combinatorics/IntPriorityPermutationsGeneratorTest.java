/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
package cc.redberry.core.combinatorics;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomDataImpl;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IntPriorityPermutationsGeneratorTest {
    public IntPriorityPermutationsGeneratorTest() {
    }

    @Test
    public void test1() {
        IntPriorityPermutationsGenerator generator = new IntPriorityPermutationsGenerator(3);
        int[] p;
        while ((p = generator.take()) != null)
            System.out.println(Arrays.toString(p));
        generator.reset();
        while ((p = generator.take()) != null)
            System.out.println(Arrays.toString(p));
    }

    @Test
    public void test2() {
        IntPriorityPermutationsGenerator generator = new IntPriorityPermutationsGenerator(3);
        int[] p;
        while ((p = generator.take()) != null) {
            if (p[0] == 1)
                generator.nice();
            System.out.println(Arrays.toString(p));
        }
        System.out.println("Acc");
        generator.reset();
        while ((p = generator.take()) != null) {
            if (p[1] == 2)
                generator.nice();
            System.out.println(Arrays.toString(p));
        }

        System.out.println("Acc");
        generator.reset();
        while ((p = generator.take()) != null) {
            if (p[0] == 0)
                generator.nice();
            System.out.println(Arrays.toString(p));
        }

        System.out.println("Acc");
        generator.reset();
        while ((p = generator.take()) != null)
            System.out.println(Arrays.toString(p));
        if (true) {
            int is = Integer.MAX_VALUE;
        }
    }

    @Ignore
    @Test
    public void test3() {
        final int dimm = 6;
        final int maxPerms = 100;
        RandomDataImpl rd = new RandomDataImpl(new MersenneTwister());
        for (int count = 0; count < 10000; ++count) {
            //Generate combinatorics
            int pCount = rd.nextInt(1, maxPerms);
            int[][] permutations = new int[pCount][];
            for (int i = 0; i < pCount; ++i)
                OUTER:
                        while (true) {
                            permutations[i] = rd.nextPermutation(dimm, dimm);
                            for (int j = 0; j < i; ++j)
                                if (Arrays.equals(permutations[i], permutations[j]))
                                    continue OUTER;
                            break;
                        }
            IntPriorityPermutationsGenerator generator = new IntPriorityPermutationsGenerator(dimm);
            //adding priorities
            for (int i = 0; i < pCount; ++i)
                for (int j = 0; j <= pCount - i; ++j) {
                    addPriority(generator, permutations[i]);
                    generator.reset();
                }

            for (int i = 0; i < pCount; ++i)
                assertTrue(Arrays.equals(permutations[i], generator.take()));
        }
    }

    private void addPriority(IntPriorityPermutationsGenerator generator, int[] permutation) {
        int[] p;
        while ((p = generator.take()) != null)
            if (Arrays.equals(p, permutation)) {
                generator.nice();
                return;
            }
        throw new RuntimeException();
    }
}
