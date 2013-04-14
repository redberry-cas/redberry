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

import cc.redberry.core.combinatorics.Permutation;
import cc.redberry.core.combinatorics.PermutationsSpanIterator;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import junit.framework.Assert;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;
import org.apache.commons.math3.random.Well1024a;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimsStructTest {

    @Test
    public void testName() {

        int[] p1 = {0, 1, 3, 2};
        int[] p2 = {0, 2, 1, 3};
        List<int[]> sc = new ArrayList<>();
        sc.add(p1);
        sc.add(p2);
        SimsStruct str = new SimsStruct(1, sc, 4);

        System.out.println(Arrays.toString(str.getTransversalOf(3)));
    }

    @Test
    public void tetet() {
        int[] g = {0, 2, 5, 4, 3, 1};
        int[] f = {0, 3, 2, 1, 5, 4};
        System.out.println("HUI!!!");
    }


    @Test
    public void testGScxee() {
        int[] g = {0, 2, 5, 4, 3, 1};
        int[] f = {0, 3, 2, 1, 5, 4};

        Symmetries ss = SymmetriesFactory.createSymmetries(6);
        ss.add(new Symmetry(g, false));
        ss.add(new Symmetry(f, false));

        int[][] gen = {g, f};

        List<SimsStruct> lll = SimsStruct.createSGS(gen);

        int counter = 0;
        for (Symmetry sym : ss) {
            System.out.println(SimsStruct.strip(sym.getPermutation().copy(), lll));
            ++counter;
        }
        int coun = 1;
        for (SimsStruct str : lll)
            coun *= str.orbit.size();

        System.out.println(counter);
        System.out.println(coun);


    }

    @Test
    public void testRndsdm() {
        long all = 0;
        long start, stop;
        for (int i = 0; i < 100; ++i) {
            RandomData rd = new RandomDataImpl(new Well1024a());
            int[] perm1 = rd.nextPermutation(6, 6);
            int[] perm2 = rd.nextPermutation(6, 6);
            PermutationsSpanIterator iterator = new PermutationsSpanIterator(
                    Arrays.asList(new Permutation[]{new Permutation(perm1), new Permutation(perm2)}));
            int[] p;

            int[] identity = PermutationGroup.getIdentity(6);
            int[][] gen = {perm1, perm2};

            start = System.nanoTime();
            List<SimsStruct> lll = SimsStruct.createSGS(gen);
            all += (System.nanoTime() - start);

            int order = 0;
            while (iterator.hasNext()) {
                p = iterator.next().getPermutation().copy();
                order++;

                start = System.nanoTime();
                SimsStruct.StripResult as = SimsStruct.strip(p, lll);
                all += (System.nanoTime() - start);
                Assert.assertTrue(Arrays.equals(as.remainderPermutation, identity));
                Assert.assertEquals(lll.size(), as.stopPoint);
            }

            int coun = 1;
            for (SimsStruct str : lll)
                coun *= str.orbit.size();
            System.out.println(order);
            Assert.assertEquals(order, coun);

        }
        System.out.println(all);
    }
}
