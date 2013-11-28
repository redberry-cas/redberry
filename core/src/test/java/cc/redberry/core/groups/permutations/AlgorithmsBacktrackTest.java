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

import cc.redberry.core.utils.Indicator;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class AlgorithmsBacktrackTest {

    @Test
    public void test1() throws Exception {

        Permutation gen0 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation gen1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation gen2 = new PermutationOneLine(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        final int point = 5;
        Indicator<Permutation> stabilizer = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(point) == point;
            }
        };
        BacktrackSearchTestFunction test = new BacktrackSearchTestFunction() {
            @Override
            public boolean test(Permutation permutation, int level) {
//                if (level == 0)
//                    return permutation.newIndexOf(point ) == point ;
                return true;
            }
        };

        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[gen0.degree()]));
        subgroup.get(0).stabilizerGenerators.add(gen0.getIdentity());

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, test, stabilizer);

        System.out.println("Is BSGS: " + AlgorithmsBase.isBSGS(subgroup));
        AlgorithmsBase.SchreierSimsAlgorithm(subgroup);
        BacktrackSearch all = new BacktrackSearch(subgroup);
        Permutation current;
        while ((current = all.take()) != null) {
            System.out.println(current);
        }
    }

    @Test
    public void test2() throws Exception {
        //no any pruning, just property test

        Permutation gen0 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation gen1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation gen2 = new PermutationOneLine(0, 1, 2, 3, 5, 4);
        ArrayList<Permutation> generators = new ArrayList<>();
        generators.add(gen0);
        generators.add(gen1);
        generators.add(gen2);

        List<BSGSElement> bsgs = AlgorithmsBase.createBSGSList(new int[]{0, 1, 4}, generators);

        final int point = 5;
        Indicator<Permutation> stabilizer = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation p) {
                return p.newIndexOf(point) == point;
            }
        };
        BacktrackSearchTestFunction test = BacktrackSearchTestFunction.TRUE;

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[gen0.degree()]));
        subgroup.get(0).stabilizerGenerators.add(gen0.getIdentity());

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, test, stabilizer);

        int[] base;
        System.out.println("Subgroup BSGS complete: " + AlgorithmsBase.isBSGS(subgroup));
        System.out.println("Subgroup base: " + Arrays.toString(AlgorithmsBase.getBaseAsArray(subgroup)));
        AlgorithmsBase.removeRedundantBasePoints(subgroup);
        System.out.println("Subgroup real base: " + Arrays.toString(base = AlgorithmsBase.getBaseAsArray(subgroup)));
        System.out.println("Subgroup generators: " + subgroup.get(0).stabilizerGenerators);

        Permutation a0 = new PermutationOneLine(0, 1, 2, 3, 4, 5);
        Permutation a1 = new PermutationOneLine(0, 3, 2, 1, 4, 5);
        Permutation a2 = new PermutationOneLine(1, 0, 3, 2, 4, 5);
        Permutation a3 = new PermutationOneLine(1, 2, 3, 0, 4, 5);
        Permutation a4 = new PermutationOneLine(2, 1, 0, 3, 4, 5);
        Permutation a5 = new PermutationOneLine(2, 3, 0, 1, 4, 5);
        Permutation a6 = new PermutationOneLine(3, 0, 1, 2, 4, 5);
        Permutation a7 = new PermutationOneLine(3, 2, 1, 0, 4, 5);
        final Permutation[] expected = {a0, a1, a2, a3, a4, a5, a6, a7};

        Comparator<Permutation> permutationComparator = new InducedOrderingOfPermutations(base, gen0.degree());
        Arrays.sort(expected, permutationComparator);

        AlgorithmsBase.SchreierSimsAlgorithm(subgroup);
        System.out.println("Subgroup order: " + AlgorithmsBase.getOrder(subgroup));

        Permutation[] actual = new Permutation[expected.length];
        BacktrackSearch search = new BacktrackSearch(subgroup);
        Permutation current;
        int ii = 0;
        while ((current = search.take()) != null) {
            actual[ii++] = current;
        }
        assertEquals(expected.length, ii);
        Arrays.sort(actual, permutationComparator);
        Assert.assertArrayEquals(expected, actual);
    }

}
