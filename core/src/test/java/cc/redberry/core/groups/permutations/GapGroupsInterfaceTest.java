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
import cc.redberry.core.groups.permutations.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GapGroupsInterfaceTest {

    @Test
    public void test1() throws Exception {
        GapGroupsInterface gap = new GapGroupsInterface("gap");
        String r = gap.evaluate("GeneratorsOfGroup(PrimitiveGroup(14, 1));");
        System.out.println(r);
        r = gap.evaluate("GeneratorsOfGroup(PrimitiveGroup(17, 1));");
        System.out.println(r);
        System.out.println(gap.nrPrimitiveGroups(127));

        gap.primitiveGenerators(12, 1);
        gap.close();
    }

    @Test
    public void test2() throws Exception {
        GapGroupsInterface gap = new GapGroupsInterface("gap");
        System.out.println(gap.evaluate("12/3;"));
        gap.close();
    }

    @Test
    public void test3() throws Exception {
        GapGroupsInterface gap = new GapGroupsInterface("gap");

        PermutationGroup g = gap.primitiveGroup(99, 1);
        gap.evaluate("g:= PrimitiveGroup(12, 1);");
        System.out.println(gap.evaluate("IsTransitive(g);"));
        System.out.println(g.degree());
        System.out.println(g.order());
        System.out.println(g.isAlternating());
        System.out.println(g.isRegular());
        System.out.println(g.isSymmetric());
        System.out.println(g.isTransitive());

        gap.close();
    }

    @Test
    public void test4() throws Exception {
        GapGroupsInterface gap = new GapGroupsInterface("gap");

        Permutation[] g = gap.primitiveGenerators(99, 2);
        System.out.println(Arrays.toString(g));
        ArrayList<BSGSCandidateElement> bsgs = (ArrayList) AlgorithmsBase.createRawBSGSCandidate(g);
        AlgorithmsBase.RandomSchreierSimsAlgorithm(bsgs, 0.9999, CC.getRandomGenerator());
        System.out.println("random");
        AlgorithmsBase.SchreierSimsAlgorithm(bsgs);
        System.out.println("Schreier-Sims");

//        PermutationGroup pg = PermutationGroupFactory.create(g);
        System.out.println(AlgorithmsBase.calculateOrder(bsgs));
//        System.out.println(pg.isRegular());
        gap.close();
    }
}
