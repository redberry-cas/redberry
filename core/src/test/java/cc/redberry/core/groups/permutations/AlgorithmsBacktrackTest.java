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

import cc.redberry.core.groups.permutations.gap.GapPrimitiveGroupsReader;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.Timing;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.groups.permutations.AlgorithmsBacktrack.BacktrackIterator;
import static cc.redberry.core.groups.permutations.AlgorithmsBacktrack.BaseComparator;
import static cc.redberry.core.groups.permutations.AlgorithmsBacktrack.InducedPermutationsComparator;
import static cc.redberry.core.groups.permutations.AlgorithmsBase.*;
import static cc.redberry.core.utils.Timing.timing;
import static org.junit.Assert.assertArrayEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBacktrackTest {
    @Test
    public void testBaseComparator1() {
        int[] base = {0, 2, 1};
        BaseComparator comparator = new BaseComparator(base);
        int[] array = {1, 0, 2};
        ArraysUtils.quickSort(array, comparator);
        assertArrayEquals(base, array);
    }

    @Test
    public void testBaseComparator2() {
        int[] base = {0, 2, 1};
        BaseComparator comparator = new BaseComparator(base);
        int[] array = {5, 1, 0, 2};
        ArraysUtils.quickSort(array, comparator);
        int[] expected = {0, 2, 1, 5};
        assertArrayEquals(expected, array);
    }

    @Test
    public void testAll1() throws Exception {
        List<Permutation> generators = new ArrayList<>();
        generators.add(new PermutationOneLine(0, 2, 1, 3, 4));
        generators.add(new PermutationOneLine(3, 2, 4, 0, 1));

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGS(generators).getBSGSList();


        BacktrackIterator iterator = new BacktrackIterator(bsgs);
        InducedPermutationsComparator comparator = new InducedPermutationsComparator(getBaseAsArray(bsgs));

        Permutation previous = null, current;
        int i = 0;
        while (iterator.hasNext()) {
            current = iterator.next();
            if (i != 0) {
                assertTrue(comparator.compare(previous, current) < 0);
                assertTrue(comparator.compare(current, previous) > 0);
            }
            previous = current;
            ++i;
        }
        assertEquals(getOrder(bsgs).intValue(), i);
    }

    @Test
    public void testAll2() throws Exception {
        Permutation gen0 = new PermutationOneLine(4, 8, 7, 1, 6, 5, 0, 9, 3, 2);
        Permutation gen1 = new PermutationOneLine(0, 5, 4, 6, 2, 1, 3, 7, 9, 8);

        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGS(generators).getBSGSList();


        BacktrackIterator iterator = new BacktrackIterator(bsgs);
        InducedPermutationsComparator comparator = new InducedPermutationsComparator(getBaseAsArray(bsgs));

        System.out.println(Arrays.toString(getBaseAsArray(bsgs)));
        System.out.println();
        Permutation previous = null, current;
        int i = 0;
        while (iterator.hasNext()) {
            if(i == 18){
                int as = 0;
            }
            current = iterator.next();
            System.out.println("permute: " + current);
            if (i != 0) {
                System.out.println("compare: " + comparator.compare(previous, current));
                assertTrue(comparator.compare(previous, current) <= 0);
                assertTrue(comparator.compare(current, previous) >= 0);
            }
            previous = current;
            ++i;
        }
        assertEquals(getOrder(bsgs).intValue(), i);
    }


    @Test
    public void testAllPrimitive() throws Exception {
        PermutationGroup[] pgs = GapPrimitiveGroupsReader.readGroupsFromGap("/home/stas/gap4r6/prim/grps/gps1.g");

        int s = 0;
        for (int i = 0; i < pgs.length; ++i) {
            if (pgs[i].order().compareTo(BigInteger.valueOf(10000)) > 0)
                continue;
            ++s;
            try {
                List<BSGSElement> bsgs = pgs[i].getBSGS().getBSGSList();


                BacktrackIterator iterator = new BacktrackIterator(bsgs);
                InducedPermutationsComparator comparator = new InducedPermutationsComparator(getBaseAsArray(bsgs));

                Permutation previous = null, current;
                int count = 0;
                while (iterator.hasNext()) {
                    current = iterator.next();
                    if (count != 0) {
                        assertTrue(comparator.compare(previous, current) <= 0);
                        assertTrue(comparator.compare(current, previous) >= 0);
                    }
                    previous = current;
                    ++count;
                }
                assertEquals(getOrder(bsgs).intValue(), count);
            } catch (AssertionError err) {
                if (pgs[i].order().intValue() == 60)
                    AlgorithmsBaseTest.soutGenerators(pgs[i].generators());
                System.out.println(pgs[i].order());
//                System.out.println(pgs[i].generators());
            }
        }
        System.out.println(s);
    }
}
