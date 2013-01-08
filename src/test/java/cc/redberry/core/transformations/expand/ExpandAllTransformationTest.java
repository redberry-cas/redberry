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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import junit.framework.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseExpression;
import static cc.redberry.core.transformations.expand.ExpandAllTransformation.expandAll;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandAllTransformationTest {
    @Test
    public void test1() {
        Tensor t = parse("1/(a+b)");
        Tensor a = expandAll(t);
        System.out.println(a);
        Assert.assertTrue(a == t);
    }

    @Test
    public void test2() {
        Tensor t = expandAll(parse("1/(a+b)**2"));
        Tensor a = parse("1/(a**2+2*a*b+b**2)");
        TAssert.assertEquals(a, t);
    }

    @Test
    public void test3() {
        Tensor t = expandAll(parse("(c+d)/(a+b)**2"));
        Tensor a = parse("c/(a**2+2*a*b+b**2)+d/(a**2+2*a*b+b**2)");
        TAssert.assertEquals(a, t);
    }

    @Test
    public void test4() {
        Tensor actual = expandAll(parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)"));
        ExpandTest.assertAllBracketsExpanded(actual);
        Tensor expected = parse("(2*c*b+2*Power[a, 2]+-2*a)*r_{ab}*f^{ab}+(-1*b*a+c*b+-1*c*a+Power[a, 2]+-1*a)*r^{ab}*r_{ab}+(b*a+c*b+c*a+Power[a, 2]+-1*a)*f^{mn}*f_{mn}");
        TAssert.assertEquals(actual, expected);
    }


    @Test
    public void test5() {
        Tensor t = expandAll(parse("1/((a + b)*(c + a)) + ((a + b)**2/(v +i)**2)*(1/((a + b)*(c + a)) + (a + c)**2/(v + i)**2)"));
        Tensor a = parse("b**2*(i**2+2*v*i+v**2)**(-2)*c**2+2*(i**2+2*v*i+v**2)**(-2)*a**3*b+2*(i**2+2*v*i+v**2)**(-2)*a**3*c+(i**2+2*v*i+v**2)**(-1)*a**2*(c*a+a**2+b*c+b*a)**(-1)+2*b**2*(i**2+2*v*i+v**2)**(-2)*c*a+2*(i**2+2*v*i+v**2)**(-2)*b*a*c**2+(i**2+2*v*i+v**2)**(-2)*a**4+(i**2+2*v*i+v**2)**(-2)*a**2*c**2+(c*a+a**2+b*c+b*a)**(-1)+2*(i**2+2*v*i+v**2)**(-1)*b*(c*a+a**2+b*c+b*a)**(-1)*a+b**2*(i**2+2*v*i+v**2)**(-2)*a**2+b**2*(i**2+2*v*i+v**2)**(-1)*(c*a+a**2+b*c+b*a)**(-1)+4*(i**2+2*v*i+v**2)**(-2)*a**2*b*c\n");
        TAssert.assertEquals(a, t);
    }

    @Test
    public void test6() {
        Tensor t = parse("Sin[R_abcd*R^abcd]");
        t = parseExpression("R_abcd = 1/3*(g_ac*g_bd - g_bc*g_ad)").transform(t);
        t = expandAll(t, EliminateMetricsTransformation.ELIMINATE_METRICS, parseExpression("d_i^i = 4"));
        TAssert.assertEquals(t, "Sin[8/3]");
    }

    @Test
    public void test7() {
        Tensor t = parse("Sin[1/la**2*R_abcd*R^abcd]");
        t = parseExpression("R_abcd = 1/3*(g_ac*g_bd - g_bc*g_ad)").transform(t);
        t = expandAll(t, EliminateMetricsTransformation.ELIMINATE_METRICS, parseExpression("d_i^i = 4"));
        System.out.println(t);
        TAssert.assertEquals(t, "Sin[1/la**2*8/3]");
    }
}
