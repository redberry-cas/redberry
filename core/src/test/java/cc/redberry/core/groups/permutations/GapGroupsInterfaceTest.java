/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GapGroupsInterfaceTest extends AbstractTestClass {
    @Before
    public void beforeMethod() {
        Assume.assumeTrue(getGapInterface() != null);
    }

    @Test
    public void test1() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        String r = gap.evaluate("GeneratorsOfGroup(PrimitiveGroup(14, 1));");
        r = gap.evaluate("GeneratorsOfGroup(PrimitiveGroup(17, 1));");
        gap.primitiveGenerators(12, 1);
    }

    @Test
    public void test2() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        System.out.println(gap.evaluate("12/3;"));
    }

    @Test
    public void test3() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        PermutationGroup g = gap.primitiveGroup(99, 1);
        gap.evaluate("g:= PrimitiveGroup(12, 1);");
    }

    @Test
    public void testEvaluateToBigInteger1_WithGap() throws Exception {
        GapGroupsInterface gap = getGapInterface();
        gap.evaluateToBigInteger("Order(PrimitiveGroup(58,1));");
    }

    @Test
    public void testSetwiseStabilizer1() {
        GapGroupsInterface gap = getGapInterface();
        gap.evaluate("g:= PrimitiveGroup(12,1);");
        gap.evaluate("v:= SetwiseStabilizer(g, OnPoints, [1,2,3]);");
    }

    @Test
    public void testEvaluateRedberryGroup() {
        GapGroupsInterface gap = getGapInterface();
        PermutationGroup g = gap.primitiveGroup(12, 0);
        gap.evaluateRedberryGroup("g", g.generators());
        Assert.assertEquals(g.order(), gap.evaluateToBigInteger("Order(g)"));
    }
}
