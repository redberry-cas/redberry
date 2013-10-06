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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.transformations.CollectScalarFactorsTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.fractions.TogetherTransformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseExpression;
import static cc.redberry.core.transformations.expand.ExpandPort.expandUsingPort;
import static cc.redberry.core.transformations.expand.ExpandTransformation.expand;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//@Ignore
public class ExpandTest {

    @Test
    public void test0() {
        Tensor t = parse("a*c");
        Tensor actual = ExpandTransformation.expand(t);
        Tensor expected = Tensors.parse("a*c");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*c+a*c");
        Tensor actual = ExpandTransformation.expand(t);
        Tensor expected = Tensors.parse("2*a*c+b*c");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test2() {
        Tensor t = parse("(a+b)*c-a*c");
        Tensor actual = ExpandTransformation.expand(t);
        Tensor expected = Tensors.parse("b*c");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test3() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*p_i");
        Tensor actual = ExpandTransformation.expand(t);
        Tensor expected = Tensors.parse("b*c*p_i");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test4() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*k_i");
        Tensor actual = ExpandTransformation.expand(t);
        Tensor expected = Tensors.parse("(a*c+c*b)*p_i-a*c*k_i");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test5() {
        Tensor actual = parse("c*(a*(c+n)+b)");
        actual = ExpandTransformation.expand(actual);
        Tensor expected = parse("c*a*c+c*a*n+c*b");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test6() {
        Tensor actual = parse("a*(c+b)");
        actual = ExpandTransformation.expand(actual);
        Tensor expected = parse("a*c+a*b");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test7() {
        Tensor actual = parse("Power[a+b,2]");
        actual = ExpandTransformation.expand(actual);
        Tensor expected = parse("a*a+b*b+2*a*b");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    //    @Ignore
    @Test
    public void test8() {
        Tensor actual = parse("Power[a+b,30]");
        actual = ExpandTransformation.expand(actual);
        System.out.println(actual);
//        Tensor expected = parse("a*a*a+b*b*b+3*a*a*b+3*a*b*b");
//        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    //    @Ignore
    @Test
    public void test10() {
        for (int i = 2; i < 30; ++i) {
            Tensor actual = Tensors.pow(parse("a+b"), i);
            actual = ExpandTransformation.expand(actual);
            Assert.assertTrue(actual.size() == i + 1);
        }
    }

    @Test
    public void test11() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("Power[a_i^i+b_i^i,2]");
            actual = ExpandTransformation.expand(actual);
            Tensor expected = parse("2*b_{i}^{i}*a_{a}^{a}+a_{i}^{i}*a_{a}^{a}+b_{i}^{i}*b_{a}^{a}");
            Assert.assertTrue(TensorUtils.equals(actual, expected));
        }
    }

    @Test
    public void test11a() {
        Tensor actual = parse("(a_i^i+b_i^i)**2");
        actual = ExpandTransformation.expand(actual);
        System.out.println(actual);
    }

    @Test
    public void test12() {
        Tensor actual = parse("f_mn*(f^mn+r^mn)-r_ab*f^ab");
        actual = ExpandTransformation.expand(actual);
        Tensor expected = parse("f_{mn}*f^{mn}");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test13() {
        Tensor actual = parse("((a+b)*(c+a)-b*a)*f_mn*(f^mn+r^mn)");
        actual = ExpandTransformation.expand(actual);
        Tensor expected = parse("(a*a+b*c+a*c)*f_mn*f^mn+(a*a+b*c+a*c)*f_mn*r^mn");
        Assert.assertTrue(TensorUtils.equalsExactly(actual, expected));
    }

    @Test
    public void test14() {
        Tensor actual = parse("(a+b)*f_mn*(f^mn+r^mn)-(a+b*(c+d))*r_ab*(f^ab+r^ab)");
        actual = ExpandTransformation.expand(actual);
        assertAllBracketsExpanded(actual);
    }

    @Test
    public void test15() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("(a+b)*(a*f_m+b*g_m)*(b*f^m+a*g^m)");
            actual = ExpandTransformation.expand(actual);
            Tensor expected = parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
            Assert.assertTrue(TensorUtils.equals(actual, expected));
        }
    }

    @Test
    public void test16() {
        Tensor actual = parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)");
        actual = ExpandTransformation.expand(actual);
        Tensor expected = Tensors.parse("(Power[a, 2]+c*b+-1*a+c*a+b*a)*f^{mn}*f_{mn}+(-2*a+2*Power[a, 2]+2*c*b)*r^{mn}*f_{mn}+(Power[a, 2]+c*b+-1*a+-1*c*a+-1*b*a)*r^{ab}*r_{ab}");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test17() {
        Tensor actual = ExpandTransformation.expand(parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)"));
        assertAllBracketsExpanded(actual);
        Tensor expected = parse("(2*c*b+2*Power[a, 2]+-2*a)*r_{ab}*f^{ab}+(-1*b*a+c*b+-1*c*a+Power[a, 2]+-1*a)*r^{ab}*r_{ab}+(b*a+c*b+c*a+Power[a, 2]+-1*a)*f^{mn}*f_{mn}");
        TAssert.assertEquals(actual, expected);
    }

    public static void assertAllBracketsExpanded(Tensor tensor) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            Tensor current = iterator.current();
            if (current instanceof Power && current.get(0) instanceof Sum)
                Assert.assertTrue(!TensorUtils.isNaturalNumber(current.get(1)));
            else if (current instanceof Product) {
                int i1 = 0, i2 = 0;
                for (Tensor t : current)
                    if (t instanceof Sum)
                        if (t.getIndices().size() == 0)
                            ++i1;
                        else
                            ++i2;
                Assert.assertTrue(i2 == 0 && i1 < 2);
            }
        }
    }

    @Test
    public void test19() {
        Tensor tensor = parse("T_ij^ij*N_as^sa*K^fd_df");
        Tensor result = ExpandTransformation.expand(tensor);
        Assert.assertTrue(tensor == result);
    }

    @Test
    public void test20() {
        Tensor tensor = parse("(a+b)*T_ij^ij*N_as^sa*K^fd_df+a*b*F_m^m");
        Tensor result = ExpandTransformation.expand(tensor);
        Assert.assertTrue(tensor == result);
    }

    @Test
    public void test21() {
        Tensor tensor = parse("(1/2*(a+b)*f_mn+g_mn)*((a+b)*(a+b)*3*g_ij+(a+b)*h_ij)");
        System.out.println(ExpandTransformation.expand(tensor));
        assertAllBracketsExpanded(ExpandTransformation.expand(tensor));
    }

    @Test
    public void test21a() {
        Tensor tensor = parse("2*(a+b)");
        TAssert.assertEquals(ExpandTransformation.expand(tensor), "2*a+2*b");
    }

    @Test
    public void test22() {
        Tensor tensor = parse("((a+b)*f_mn+g_mn)*((a+b)*g_ij+h_ij)");
        assertAllBracketsExpanded(ExpandTransformation.expand(tensor));
    }

    @Test
    public void test23() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = parse("WR^{\\rho_5 }_{\\rho_5 } = 1/1080*Power[gamma, 3]*g_{\\gamma \\eta }*g^{\\gamma \\eta }*d^{\\beta }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/2160*Power[gamma, 3]*d^{\\eta }_{\\eta }*d^{\\gamma }_{\\gamma }*d^{\\beta }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/120*Power[gamma, 2]*d^{\\gamma }_{\\gamma }*d^{\\beta }_{\\zeta }*g^{\\alpha \\nu }*d^{\\mu }_{\\sigma }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/120960*Power[gamma, 4]*d^{\\rho_5 }_{\\rho_5 }*d^{\\eta }_{\\eta }*d^{\\epsilon }_{\\epsilon }*d^{\\beta }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/360*Power[gamma, 3]*d^{\\zeta }_{\\gamma }*g^{\\alpha \\gamma }*g_{\\zeta \\sigma }*d^{\\beta }_{\\eta }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/18*gamma*d^{\\beta }_{\\gamma }*g^{\\alpha \\nu }*d^{\\mu }_{\\sigma }*P^{\\gamma }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/2520*Power[gamma, 4]*g^{\\beta \\rho_5 }*g_{\\zeta \\rho_5 }*g_{\\epsilon \\sigma }*g^{\\epsilon \\mu }*g^{\\eta \\nu }*d^{\\alpha }_{\\eta }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/48*Power[gamma, 2]*g^{\\beta \\epsilon }*g_{\\epsilon \\eta }*d^{\\alpha }_{\\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/5040*Power[gamma, 4]*d^{\\epsilon }_{\\rho_5 }*d^{\\eta }_{\\eta }*g^{\\beta \\rho_5 }*g_{\\epsilon \\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/270*Power[gamma, 3]*g^{\\gamma \\eta }*d^{\\beta }_{\\gamma }*g_{\\zeta \\eta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/135*Power[gamma, 3]*g^{\\gamma \\eta }*d^{\\mu }_{\\gamma }*g_{\\eta \\sigma }*d^{\\beta }_{\\zeta }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/2520*Power[gamma, 4]*d^{\\epsilon }_{\\rho_5 }*g_{\\epsilon \\eta }*g^{\\beta \\rho_5 }*d^{\\eta }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+(1/6*gamma+1/1440*Power[gamma, 3]*g_{\\gamma \\zeta }*g^{\\gamma \\zeta }+1/2880*Power[gamma, 3]*d^{\\zeta }_{\\zeta }*d^{\\gamma }_{\\gamma })*g^{\\alpha \\beta }*g_{\\gamma \\sigma }*P^{\\gamma }_{\\beta }*R_{\\alpha }^{\\sigma }+1/720*Power[gamma, 3]*d^{\\zeta }_{\\zeta }*g^{\\beta \\gamma }*g_{\\gamma \\eta }*d^{\\alpha }_{\\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+(1/12*gamma+1/1440*Power[gamma, 3]*g_{\\gamma \\zeta }*g^{\\gamma \\zeta }+1/2880*Power[gamma, 3]*d^{\\zeta }_{\\zeta }*d^{\\gamma }_{\\gamma })*d^{\\beta }_{\\gamma }*d^{\\alpha }_{\\sigma }*P^{\\gamma }_{\\beta }*R_{\\alpha }^{\\sigma }+1/540*Power[gamma, 3]*d^{\\eta }_{\\eta }*d^{\\beta }_{\\gamma }*d^{\\gamma }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/48*Power[gamma, 2]*g^{\\alpha \\epsilon }*g_{\\epsilon \\sigma }*d^{\\beta }_{\\eta }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/20160*Power[gamma, 4]*g_{\\eta \\rho_5 }*g^{\\eta \\rho_5 }*d^{\\epsilon }_{\\epsilon }*d^{\\beta }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/20160*Power[gamma, 4]*d^{\\epsilon }_{\\epsilon }*d^{\\eta }_{\\eta }*g^{\\beta \\rho_5 }*g_{\\zeta \\rho_5 }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/5040*Power[gamma, 4]*d^{\\rho_5 }_{\\rho_5 }*d^{\\alpha }_{\\eta }*g^{\\epsilon \\mu }*g^{\\eta \\nu }*g_{\\epsilon \\sigma }*d^{\\beta }_{\\zeta }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+-1/1260*Power[gamma, 4]*d^{\\eta }_{\\epsilon }*g^{\\beta \\rho_5 }*d^{\\alpha }_{\\eta }*d^{\\mu }_{\\rho_5 }*g^{\\epsilon \\nu }*g_{\\zeta \\sigma }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/270*Power[gamma, 3]*d^{\\alpha }_{\\eta }*g^{\\gamma \\mu }*g^{\\eta \\nu }*g_{\\gamma \\sigma }*d^{\\beta }_{\\zeta }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/96*Power[gamma, 2]*d^{\\epsilon }_{\\epsilon }*d^{\\beta }_{\\eta }*d^{\\alpha }_{\\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/1440*Power[gamma, 3]*g_{\\gamma \\zeta }*g^{\\gamma \\zeta }*g^{\\alpha \\beta }*g_{\\eta \\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/360*Power[gamma, 3]*d^{\\zeta }_{\\gamma }*g^{\\beta \\gamma }*g_{\\zeta \\eta }*d^{\\alpha }_{\\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/9*P*g^{\\alpha \\nu }*d^{\\mu }_{\\sigma }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/60*Power[gamma, 2]*g^{\\beta \\gamma }*g_{\\gamma \\zeta }*g^{\\alpha \\nu }*d^{\\mu }_{\\sigma }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/15120*Power[gamma, 4]*d^{\\rho_5 }_{\\epsilon }*g_{\\eta \\rho_5 }*g^{\\epsilon \\eta }*d^{\\beta }_{\\zeta }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/180*Power[gamma, 3]*d^{\\zeta }_{\\zeta }*g_{\\gamma \\eta }*g^{\\alpha \\gamma }*d^{\\beta }_{\\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/48*Power[gamma, 2]*d^{\\epsilon }_{\\epsilon }*g^{\\alpha \\beta }*g_{\\eta \\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/2880*Power[gamma, 3]*d^{\\zeta }_{\\zeta }*d^{\\gamma }_{\\gamma }*g^{\\alpha \\beta }*g_{\\eta \\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/720*Power[gamma, 3]*d^{\\zeta }_{\\zeta }*g^{\\alpha \\gamma }*g_{\\gamma \\sigma }*d^{\\beta }_{\\eta }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/12*Power[gamma, 2]*g_{\\epsilon \\eta }*g^{\\alpha \\epsilon }*d^{\\beta }_{\\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/360*Power[gamma, 3]*g^{\\beta \\zeta }*g_{\\zeta \\eta }*g^{\\alpha \\gamma }*g_{\\gamma \\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/90*Power[gamma, 3]*g^{\\gamma \\zeta }*g_{\\zeta \\eta }*g_{\\gamma \\sigma }*g^{\\alpha \\beta }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/6*P*d^{\\alpha }_{\\sigma }*R_{\\alpha }^{\\sigma }+2/135*Power[gamma, 3]*g^{\\gamma \\eta }*d^{\\mu }_{\\gamma }*g_{\\eta \\sigma }*g^{\\beta \\nu }*d^{\\alpha }_{\\zeta }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }+1/180*Power[gamma, 3]*d^{\\beta }_{\\gamma }*d^{\\zeta }_{\\eta }*g^{\\alpha \\gamma }*g_{\\zeta \\sigma }*P^{\\eta }_{\\beta }*R_{\\alpha }^{\\sigma }+1/10080*Power[gamma, 4]*d^{\\epsilon }_{\\eta }*d^{\\eta }_{\\epsilon }*g^{\\beta \\rho_5 }*g_{\\zeta \\rho_5 }*d^{\\mu }_{\\sigma }*g^{\\alpha \\nu }*P^{\\zeta }_{\\beta }*R^{\\sigma }_{\\mu \\alpha \\nu }");
            TAssert.assertIndicesConsistency(t);
            t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS, Tensors.parseExpression("d_\\mu^\\mu=4"));
            TAssert.assertIndicesConsistency(t);
        }
    }

    @Test
    public void test24() {
        Tensor t = parse("ACTION = 4669/5760*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-497/1152*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1409/2880*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+53/720*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1391/1440*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]+1/480*Power[R, 2]*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/36*P*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+29/120*Power[R, 2]*gamma+-19/120*Power[R, 2]*gamma*Power[gamma+1, -1]+829/5760*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+9/80*Power[R, 2]*Power[gamma, 4]+17/40*Power[R, 2]*Power[gamma, 2]+-271/480*Power[R, 2]*Power[gamma, 2]*Power[gamma+1, -1]+47/180*Power[R, 2]*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+83/240*Power[R, 2]*Power[gamma, 3]+49/720*Power[R, 2]*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/18*P*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+-37/120*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]+929/5760*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-43/40*Power[R, 2]*Power[gamma, 3]*Power[gamma+1, -1]+-37/240*Power[R, 2]*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/12*P*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+(1/4*gamma+1/2+1/24*Power[gamma, 2])*P^{\\alpha }_{\\rho_5 }*P^{\\rho_5 }_{\\alpha }+1439/5760*Power[R, 2]*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+7/60*Power[R, 2]+-203/3840*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/72*P*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+1/48*Power[gamma, 2]*Power[P, 2]+-13/144*P*Power[gamma, 3]*Power[gamma+1, -1]*R+1/6*P*R+-403/5760*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1453/1920*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+(329/960*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1427/720*Power[gamma, 4]*Power[gamma+1, -1]+127/720*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-125/576*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-127/240*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]+31/64*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-169/576*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+179/192*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-287/720*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1289/576*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1*(101/240*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/30*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+15/8*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-2/15*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-73/240*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-113/80*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+3/20*Power[gamma, 4]*Power[gamma+1, -1]+11/20*Power[gamma, 3]*Power[gamma+1, -1]+23/60*Power[gamma, 2]*Power[gamma+1, -1]+1/5*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/40*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-9/20*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/15*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+187/240*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-173/80*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-3/40*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+13/6*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+7/240*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-7/40*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/30*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-4/5*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1])+17/240*Power[gamma, 10]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1427/2880*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/20*Power[gamma, 5]*Power[gamma+1, -1]+-4/15*gamma+11/15+107/1440*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-3121/960*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-817/360*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/60*gamma*Power[gamma+1, -1]+241/90*Power[gamma, 3]*Power[gamma+1, -1]+29/320*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1129/5760*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+383/2880*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-23/120*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-17/30*Power[gamma, 2]+-23/120*Power[gamma, 4]+-67/120*Power[gamma, 3]+-1*(-77/192*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-43/48*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+-47/96*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-29/192*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+49/64*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+5/16*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/12*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/3*Power[gamma, 2]*Power[gamma+1, -1]+-1/8*Power[gamma, 3]*Power[gamma+1, -1]+5/12*gamma+1+-3/4*gamma*Power[gamma+1, -1]+-5/24*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/24*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/4*Power[gamma, 2]+-1/4*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/24*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/16*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-13/96*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/32*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/12*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1])+353/2880*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+625/1152*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+349/288*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+97/320*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/8*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+137/1920*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/6*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1313/720*Power[gamma, 2]*Power[gamma+1, -1])*R_{\\delta \\zeta }*R^{\\zeta \\delta }+1789/5760*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/144*P*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+167/3840*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/12*P*Power[gamma, 2]*R+1/36*P*Power[gamma, 3]*R+-337/5760*Power[R, 2]*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-109/5760*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+319/1440*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/144*P*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+1/12*P*gamma*R+(1/36*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/36*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/36*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/36*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/36*Power[gamma, 4]*Power[gamma+1, -1]+-7/12*Power[gamma, 2]*Power[gamma+1, -1]+-37/72*Power[gamma, 3]*Power[gamma+1, -1]+73/72*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/6*gamma+1/6*gamma*Power[gamma+1, -1]+1/6*Power[gamma, 2]+1/18*Power[gamma, 3]+1/9*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+2/3*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/24*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1])*P_{\\sigma }^{\\alpha }*R_{\\alpha }^{\\sigma }+-19/1440*Power[R, 2]*Power[gamma, 10]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/72*P*Power[gamma, 4]*Power[gamma+1, -1]*R+29/1920*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/24*P*Power[gamma, 2]*Power[gamma+1, -1]*R+19/288*Power[R, 2]*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/36*P*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+2761/11520*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/20*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-37/384*Power[R, 2]*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]");
        t = ExpandTransformation.expand(t);
        assertAllBracketsExpanded(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void test25() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("(b+a)*(c+a)+(a+b)*(c+b)");
            TAssert.assertEquals(Tensors.parse("2*c*a+2*b*a+a**2+2*c*b+b**2"), expandUsingPort(t));
        }
    }

    @Test
    public void test26() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)");
            Tensor expected = ExpandTransformation.expand(t), actual = expandUsingPort(t);
            TAssert.assertEquals(expected, actual);
        }
    }

    @Test
    public void test27() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)*(f_a^a+r_b^b)**5");
            Tensor expected = ExpandTransformation.expand(t), actual = expandUsingPort(t);
            assertAllBracketsExpanded(expected);
            assertAllBracketsExpanded(actual);
            TAssert.assertEquals(expected, actual);
        }
    }

    @Test
    public void test28() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("(a_i^i+b_i^i)**5");
            Tensor expected = ExpandTransformation.expand(t);
            TAssert.assertIndicesConsistency(expected);
        }
    }

    @Test
    public void test29() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("(a+b)*(a_b^b+b_a^a)**5");
            Tensor expected = ExpandTransformation.expand(t),
                    actual = expandUsingPort(t);
            TAssert.assertEquals(expected, actual);
        }
    }

    @Test
    public void test30() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("1/21*((a_mn+b_mn)*2*(3*a_b^b+b_a^a)**2+c*(2*((d_m+3*(i_m+n_m))*(f_n+2*h_n)-e_m*e_n)+4*(a_i^i-n_i^i)*h_m*h_n))*(d+h)+2*(b_m*b_n+R_nmiu^ui)");
            Tensor e1 = expand(t), e2 = expandUsingPort(t);
            assertAllBracketsExpanded(e1);
            assertAllBracketsExpanded(e2);
        }
    }

    @Test
    public void test31() {
        Tensor t = parse("(p_{a}*k_{b}+(k^{d}*k_{d}-m**2)**2*k_{a}*k_{b})");
        t = expand(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void test32() {
        Tensor t = expand(parse("(a*b+(c*d-m**2)**2*a*b)"));
        assertAllBracketsExpanded(t);
    }

    @Test
    public void test33() {
        CC.resetTensorNames(-1920349242311093308L);
        Tensors.parse("k_a*k_b/(k_a*k^a-m**2)+p_a*k_b/(k_a*k^a-m**2)**3");//for debug in order to restore tensors hashes
        Tensor t = Tensors.parse("(-m**2+k_{d}*k^{d})**2*k_{a}*k_{b}+p_{a}*k_{b}");
        t = expand(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void test34() {
        CC.resetTensorNames(-1920349242311093308L);
        Tensor t = Tensors.parse("k_a*k_b/(k_a*k^a-m**2)+p_a*k_b/(k_a*k^a-m**2)**3");
        t = TogetherTransformation.together(t);
        TAssert.assertIndicesConsistency(t);
        Tensor tt = Tensors.parse("(-m**2+k_{d}*k^{d})**(-3)*((-m**2+k_{c}*k^{c})**2*k_{a}*k_{b}+p_{a}*k_{b})");
        TAssert.assertEqualsExactly(t, tt);
        tt = expand(tt);
        TAssert.assertIndicesConsistency(tt);
        t = expand(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void test35() {
        Tensor t1 = expand(parse("a_a^a*F_mn+(b_m^m-a_m^m)*F_mn"));
        Tensor t2 = expandUsingPort(t1);
        TAssert.assertEquals(t1, t2);
    }

    @Test
    public void test36() {
        Tensor t1 = expand(parse("(A_abcd+B_abcd)*(A^ab + F^ab*(A_e^e+B_e^e)**2)"));
        System.out.println(t1);
        //todo add assert
//        Tensor t2 = expandUsingPort(t1);
//        TAssert.assertEquals(t1, t2);
    }

    @Test
    public void test37() {
        Tensor t = parse("Sin[(a+b)*(c+d)]");
        TAssert.assertTrue(t == expand(t));
    }

    @Test
    public void test38() {
        Tensor t = parse("1/(a+b)**2 + Sin[(a+b)*(c+d)]");
        TAssert.assertTrue(t == expand(t));
    }

    @Test
    public void test39() {
        Tensor t;

        t = parse("(a+b)*g_mn");
        Assert.assertTrue(t == expand(t));
        t = parse("(a+b)*g_mn*f^ab");
        Assert.assertTrue(t == expand(t));
        t = parse("(a+b*c)*g_mn*f^ab");
        Assert.assertTrue(t == expand(t));
        t = parse("a+b*c");
        Assert.assertTrue(t == expand(t));
        t = parse("a_i^i+b*c");
        Assert.assertTrue(t == expand(t));
        t = parse("a_ij+b*c_ij");
        Assert.assertTrue(t == expand(t));
        t = parse("a_ij+(b+a)*c_ij");
        Assert.assertTrue(t == expand(t));
        t = parse("(c+d)*a_ij+(b+a)*c_ij");
        Assert.assertTrue(t == expand(t));
        t = parse("(c+d_f^f)*a_ij+(b+a)*c_ij");
        Assert.assertTrue(t != expand(t));
    }

    @Test
    public void test40() {
        Tensor t = parse("((a+b)*f_mn+(c+d*(a+b))*l_mn)*(a+b)");
        Tensor exp = expand(t);
        TAssert.assertEquals(exp, "(a**2+2*a*b+b**2)*f_{mn}+(c*a+2*a*d*b+c*b+a**2*d+b**2*d)*l_{mn}");
        assertAllBracketsExpanded(expand(t));
    }

    @Test
    public void testExpandPortWithPower1() {
        Tensor t = parse("1/(x+y)**2 + a*(c + d)"),
                expected = parse("1/(x+y)**2 + a*c + a*d"),
                actual = expandUsingPort(t);
        TAssert.assertEquals(expected, actual);

    }

    @Test
    public void test41() {
        Tensor t = parse("a*d + b*c + f");
        TAssert.assertTrue(t == expand(t));
    }

    @Test
    public void test42() {
        Tensor t = parse("(A_m*A^m)**2");
        TAssert.assertTrue(t == expand(t));
        TAssert.assertTrue(t == expandUsingPort(t));
    }

    @Test
    public void test43() {
        CC.resetTensorNames(-6102230255296942693L);
        Tensor t = parse("(x + a^a_a*x)*(x + a^b_b)*(x + a^c_c)");
        t = ExpandTransformation.expand(t, parseExpression("x=a_a^a"));
    }

    @Test(timeout = 1000)
    public void test44() {
        Tensor t = parse("(-2050*(f^{q}+57*f_{n}*f^{n}*f^{q})*f_{q}*(f_{a}-33*f_{j}*f^{j}*f_{a})-96*(25*f_{l}*f^{l}*f_{j}*f^{j}-75*f_{l}*f^{l})*f_{a})*(67*f_{v}*f^{v}*(-81*f_{f}*f^{f}*f_{b}+30*f_{b})+5734*f_{f}*f^{f}*(-67*f_{e}*f^{e}*f_{h}*f^{h}*f_{b}-48*f_{b}))*(4032*f^{d}*f_{d}*f_{c}+33440*f_{g}*f^{g}*f^{k}*f_{c}*(f_{k}+f_{d}*f^{d}*f_{k}))");
        t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS, CollectScalarFactorsTransformation.COLLECT_SCALAR_FACTORS);
        t = EliminateMetricsTransformation.eliminate(t);
    }

    @Test
    public void test45() {
//        for (int i = 0; i < 10000; ++i) {
        CC.resetTensorNames(8949527067673720970L);
//        CC.resetTensorNames();
        System.out.println(CC.getNameManager().getSeed());
        Tensors.parse("f_n");
        Tensors.parse("g_mn");
        Tensor t = parse("(((-86*f_{o_{1}}*f^{o_{1}}-84*d^{n_{1}}_{n_{1}}*d^{o_{1}}_{o_{1}})*(f_{m_{1}}*f^{m_{1}}*f_{a}+d_{l_{1}}^{l_{1}}*f_{a})+15*(d_{m_{1}}^{m_{1}}*f_{k_{1}}+d^{n_{1}}_{k_{1}}*f_{n_{1}})*(g_{al_{1}}*f^{l_{1}}+9*f^{l_{1}}*f_{l_{1}}*f_{a})*(-73*g^{p_{1}k_{1}}*f_{p_{1}}+g_{o_{1}p_{1}}*g^{k_{1}p_{1}}*f^{o_{1}}))*((d^{c_{1}}_{j_{1}}*f^{j_{1}}+4*f_{j_{1}}*f^{j_{1}}*f^{c_{1}})*(f_{e_{1}}*f^{e_{1}}*f^{v_{1}}-63*g^{e_{1}d_{1}}*d_{e_{1}}^{v_{1}}*f_{d_{1}})*(g_{i_{1}h_{1}}*g^{h_{1}i_{1}}*f_{c_{1}}+d_{g_{1}}^{g_{1}}*d_{f_{1}}^{f_{1}}*f_{c_{1}})+(78*g^{f_{1}c_{1}}*f_{f_{1}}*f^{v_{1}}+f^{c_{1}}*f^{v_{1}})*(37*g_{c_{1}e_{1}}*f^{e_{1}}+g^{e_{1}d_{1}}*g_{e_{1}c_{1}}*f_{d_{1}}))*(-84*(g_{br_{1}}*f^{r_{1}}-13*f_{r_{1}}*f^{r_{1}}*f_{b})*(93*g^{u_{1}q_{1}}*g_{v_{1}u_{1}}-62*f^{q_{1}}*f_{v_{1}})*(f^{s_{1}}*f_{s_{1}}*f_{q_{1}}+d_{t_{1}}^{t_{1}}*f_{q_{1}})+840*d_{r_{1}}^{r_{1}}*g_{bu_{1}}*f^{u_{1}}*f^{q_{1}}*(-8*g_{q_{1}s_{1}}*d_{v_{1}}^{s_{1}}-27*d_{t_{1}}^{t_{1}}*f_{q_{1}}*f_{v_{1}}))-9*((40*d_{j_{1}}^{j_{1}}*d^{k_{1}}_{h_{1}}*f_{k_{1}}+39*f_{l_{1}}*f^{l_{1}}*f_{h_{1}})*(d_{m_{1}}^{m_{1}}*f_{a}-6*d^{n_{1}}_{a}*f_{n_{1}})*(d_{i_{1}}^{i_{1}}*f^{h_{1}}*f^{v_{1}}+15*f^{h_{1}}*f^{v_{1}})-79*(44*d^{i_{1}}_{i_{1}}*d_{a}^{h_{1}}+95*f_{a}*f^{h_{1}})*(d^{k_{1}}_{h_{1}}*d^{v_{1}}_{k_{1}}+d_{j_{1}}^{j_{1}}*f_{h_{1}}*f^{v_{1}}))*(-29*(f_{e_{1}}*f^{e_{1}}*f_{b}-40*d^{d_{1}}_{d_{1}}*d^{c_{1}}_{c_{1}}*f_{b})*(88*d^{z}_{b_{1}}*f^{b_{1}}-14*g^{a_{1}b_{1}}*d_{a_{1}}^{z}*f_{b_{1}})*(d^{g_{1}}_{z}*f_{g_{1}}+d^{g_{1}}_{f_{1}}*d_{g_{1}}^{f_{1}}*f_{z})+116*(-98*f_{d_{1}}*f^{d_{1}}+d^{d_{1}}_{d_{1}}*f^{c_{1}}*f_{c_{1}})*d^{e_{1}}_{z}*f_{e_{1}}*(d^{z}_{b}*f_{b_{1}}*f^{b_{1}}+44*d_{b}^{b_{1}}*d_{b_{1}}^{z}))*((-97*d_{u_{1}}^{o_{1}}*f^{u_{1}}*f_{v_{1}}+81*f^{o_{1}}*f_{v_{1}})*(36*g_{o_{1}p_{1}}*f^{p_{1}}+90*f^{p_{1}}*f_{p_{1}}*f_{o_{1}})+120*(-18*f^{r_{1}}*f_{r_{1}}+g^{s_{1}r_{1}}*f_{s_{1}}*f_{r_{1}})*g_{p_{1}q_{1}}*f^{q_{1}}*(86*d_{u_{1}}^{u_{1}}*d_{v_{1}}^{p_{1}}+g^{t_{1}p_{1}}*g_{v_{1}t_{1}})))*((-80*(f_{r}*f^{r}*f^{u}-79*g^{ru}*f_{r})*(d_{s}^{s}*g_{yu}+g_{tu}*f^{t}*f_{y})+(30*d_{y}^{s}*f_{s}*f_{t}+100*d_{t}^{s}*f_{s}*f_{y})*(g^{tr}*f_{r}*f_{u}+f^{t}*f_{u})*(d_{p}^{p}*f^{u}+d_{q}^{q}*d^{u}_{p}*f^{p}))*(-89*(g_{xd}*g^{dx}-73*d_{d}^{d}*f_{x}*f^{x})*(-41*d_{g}^{g}*f^{y}+d_{f}^{f}*d_{e}^{e}*f^{y})+(g_{hd}*f^{d}-84*f_{d}*f^{d}*f_{h})*(-27*d_{x}^{y}*f^{x}*f^{h}-48*d^{h}_{x}*g^{xy}))*(51*(-11*d_{m}^{n}*d^{m}_{n}+d^{m}_{n}*f^{n}*f_{m})*(g_{ck}*f^{k}+73*d^{l}_{l}*d_{c}^{k}*f_{k})-64*(f_{m}*f^{m}*f_{o}+89*d_{n}^{n}*f_{o})*(g_{ci}*f^{i}+d^{i}_{j}*d^{j}_{c}*f_{i})*(16*d_{l}^{l}*f^{o}+80*d^{k}_{l}*d^{o}_{k}*f^{l}))+45*(86*(d^{s}_{s}*f_{t}*f^{t}+f^{s}*f_{s})*g^{vu}*(d_{r}^{r}*f_{v}-72*d^{q}_{v}*f_{q})*f_{u}*f_{c}-5040*d^{t}_{u}*f^{u}*f_{t}*f^{w}*f_{c}*(g^{sr}*g_{sw}*f_{r}+42*f^{r}*f_{r}*f_{w}))*((g^{jp}*f_{j}-8*d_{k}^{k}*d_{l}^{l}*f^{p})*(-61*d^{m}_{p}*f_{m}*f^{y}+96*f_{p}*f^{y})*(11*d_{o}^{o}*f_{x}+47*f_{n}*f^{n}*f_{x})+2*(-3*d_{p}^{o}*g_{ox}+26*f_{p}*f_{x})*f^{p}*f^{y})*(-56*(-12*d_{g}^{f}*f_{f}*f^{g}-25*f_{g}*f^{g})*(d^{x}_{e}*f^{e}+50*d^{e}_{d}*d_{e}^{d}*f^{x})*(-49*g_{yh}*f^{h}-71*f_{h}*f^{h}*f_{y})-2968*f_{d}*f^{d}*f^{x}*(f_{i}*f^{i}*f_{y}+g_{yi}*f^{i})))");
        Transformation tr = new TransformationCollection(new Transformation[]{EliminateMetricsTransformation.ELIMINATE_METRICS, Tensors.parseExpression("d^{g}_{g} = f_{f}*f^{f}")});
        t = tr.transform(t);

        TAssert.assertIndicesConsistency(t);
        Tensor a = parse("(((4*f_{j_{1}}*f^{j_{1}}*f^{c_{1}}+f^{c_{1}})*(-63*f^{v_{1}}+f_{e_{1}}*f^{e_{1}}*f^{v_{1}})*(f_{d_{1}}*f^{d_{1}}*f_{f_{1}}*f^{f_{1}}*f_{c_{1}}+f_{d_{1}}*f^{d_{1}}*f_{c_{1}})+3002*f^{c_{1}}*f_{c_{1}}*f^{v_{1}})*(-168*(93*d^{q_{1}}_{v_{1}}-62*f^{q_{1}}*f_{v_{1}})*f_{q_{1}}*f_{j}*f^{j}*(-13*f_{r_{1}}*f^{r_{1}}*f_{b}+f_{b})+840*f_{j}*f^{j}*f^{q_{1}}*f_{b}*(-8*g_{q_{1}v_{1}}-27*f_{v}*f^{v}*f_{q_{1}}*f_{v_{1}}))*(-1080*f^{k_{1}}*(9*f^{l_{1}}*f_{l_{1}}*f_{a}+f_{a})*(f_{k_{1}}+f_{z}*f^{z}*f_{k_{1}})+2*(-86*f_{o_{1}}*f^{o_{1}}-84*f_{z}*f^{z}*f_{a_{1}}*f^{a_{1}})*f_{b_{1}}*f^{b_{1}}*f_{a})-9*(116*(-98*f_{d_{1}}*f^{d_{1}}+f_{d_{1}}*f^{d_{1}}*f^{c_{1}}*f_{c_{1}})*f_{z}*(44*d_{b}^{z}+d^{z}_{b}*f_{b_{1}}*f^{b_{1}})-2146*f^{z}*(f_{z}+f_{c_{1}}*f^{c_{1}}*f_{z})*(-40*f_{b_{1}}*f^{b_{1}}*f_{c_{1}}*f^{c_{1}}*f_{b}+f_{e_{1}}*f^{e_{1}}*f_{b}))*(79*(15*f^{h_{1}}*f^{v_{1}}+f_{g_{1}}*f^{g_{1}}*f^{h_{1}}*f^{v_{1}})*(-6*f_{a}+f_{v}*f^{v}*f_{a})*f_{h_{1}}*f_{l_{1}}*f^{l_{1}}-79*(d_{h_{1}}^{v_{1}}+f_{v}*f^{v}*f_{h_{1}}*f^{v_{1}})*(44*d_{a}^{h_{1}}*f_{a_{1}}*f^{a_{1}}+95*f_{a}*f^{h_{1}}))*(-16*(90*f^{p_{1}}*f_{p_{1}}*f_{o_{1}}+36*f_{o_{1}})*f^{o_{1}}*f_{v_{1}}-2040*(86*g_{v_{1}q_{1}}*f_{j}*f^{j}+g_{v_{1}q_{1}})*f^{q_{1}}*f_{r_{1}}*f^{r_{1}}))*(45*(2*(-3*g_{px}+26*f_{p}*f_{x})*f^{p}*f^{y}+2030*(-8*f_{f}*f^{f}*f_{e}*f^{e}*f^{p}+f^{p})*f_{p}*f_{n}*f^{n}*f_{x}*f^{y})*(-5040*f_{t}*f^{t}*f^{w}*f_{c}*(42*f^{r}*f_{r}*f_{w}+f_{w})+86*(f^{s}*f_{s}+f_{k}*f^{k}*f_{t}*f^{t})*(-72*f^{u}+f_{l}*f^{l}*f^{u})*f_{u}*f_{c})*(-2968*f^{d}*f_{d}*f^{x}*(f_{y}+f_{i}*f^{i}*f_{y})+2072*(50*f_{d}*f^{d}*f^{x}+f^{x})*(-71*f_{h}*f^{h}*f_{y}-49*f_{y})*f_{g}*f^{g})+(-11520*f_{i}*f^{i}*f_{o}*f_{c}*(16*f_{k}*f^{k}*f^{o}+80*f^{o})-510*(73*f_{k}*f^{k}*f_{c}+f_{c})*f_{i}*f^{i})*((-84*f_{d}*f^{d}*f_{h}+f_{h})*(-48*g^{hy}-27*f^{h}*f^{y})-89*(-73*f_{d}*f^{d}*f_{x}*f^{x}+f_{d}*f^{d})*(f_{e}*f^{e}*f_{f}*f^{f}*f^{y}-41*f_{f}*f^{f}*f^{y}))*(-80*(f_{u}*f_{y}+g_{yu}*f_{l}*f^{l})*(-79*f^{u}+f_{r}*f^{r}*f^{u})+520*f_{u}*f^{l}*f^{u}*f_{l}*f^{t}*f_{t}*f_{y}))");
        System.out.println(TensorUtils.compare1(t, a));
        t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS, CollectScalarFactorsTransformation.COLLECT_SCALAR_FACTORS);
        t = EliminateMetricsTransformation.eliminate(t);
//        }
    }
}
