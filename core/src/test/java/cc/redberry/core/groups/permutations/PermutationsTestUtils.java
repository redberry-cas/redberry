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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.groups.permutations.AlgorithmsBase.getBaseAsArray;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsTestUtils {

    /**
     * Very raw check for set stabilizer
     */
    public static class RawSetwiseStabilizerCriteria
            implements Indicator<Permutation>, BacktrackSearchTestFunction {
        final int[] set;
        final int[] base;

        public RawSetwiseStabilizerCriteria(int[] set, int[] base) {
            this.set = set;
            Arrays.sort(set);
            this.base = base;
        }

        @Override
        public boolean test(Permutation permutation, int level) {
            if (Arrays.binarySearch(set, base[level]) < 0)
                return true;
            return Arrays.binarySearch(set, permutation.newIndexOf(base[level])) >= 0;
        }

        @Override
        public boolean is(Permutation p) {
            for (int i : set)
                if (Arrays.binarySearch(set, p.newIndexOf(i)) < 0)
                    return false;
            return true;

        }
    }

    public static PermutationGroup calculateRawSetwiseStabilizer(PermutationGroup pg, int[] set) {
        List<BSGSElement> bsgs = pg.getBSGS().getBSGSList();
        int degree = pg.degree();
        int[] base = getBaseAsArray(bsgs);
        RawSetwiseStabilizerCriteria rw = new RawSetwiseStabilizerCriteria(set, base);

        //empty initial subgroup
        ArrayList<BSGSCandidateElement> subgroup = new ArrayList<>();
        subgroup.add(new BSGSCandidateElement(0, new ArrayList<Permutation>(), new int[degree]));
        subgroup.get(0).stabilizerGenerators.add(Permutations.getIdentityOneLine(degree));

        AlgorithmsBacktrack.subgroupSearch(bsgs, subgroup, rw, rw);
        return new PermutationGroupImpl(new BaseAndStrongGeneratingSet(AlgorithmsBase.asBSGSList(subgroup)));
    }
}
