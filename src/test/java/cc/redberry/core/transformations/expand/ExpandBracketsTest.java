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

import cc.redberry.core.transformations.Expand;
import cc.redberry.core.*;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//@Ignore
public class ExpandBracketsTest {

    @Test
    public void test0() {
        Tensor t = parse("a*c");
        Tensor actual = Expand.expand(t);
        System.out.println(actual);
        Tensor expected = Tensors.parse("a*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*c+a*c");
        Tensor actual = Expand.expand(t);
        System.out.println(actual);
        Tensor expected = Tensors.parse("2*a*c+b*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test2() {
        Tensor t = parse("(a+b)*c-a*c");
        Tensor actual = Expand.expand(t);
        Tensor expected = Tensors.parse("b*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test3() {
        
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*p_i");
        Tensor actual = Expand.expand(t);
        System.out.println(actual);
        Tensor expected = Tensors.parse("b*c*p_i");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test4() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*k_i");
        Tensor actual = Expand.expand(t);
        Tensor expected = Tensors.parse("(a*c+c*b)*p_i-a*c*k_i");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test5() {
        Tensor actual = parse("c*(a*(c+n)+b)");
        actual = Expand.expand(actual);
        Tensor expected = parse("c*a*c+c*a*n+c*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test6() {
        Tensor actual = parse("a*(c+b)");
        actual = Expand.expand(actual);
        Tensor expected = parse("a*c+a*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test7() {
        Tensor actual = parse("Power[a+b,2]");
        actual = Expand.expand(actual);
        Tensor expected = parse("a*a+b*b+2*a*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test8() {
        Tensor actual = parse("Power[a+b,30]");
        actual = Expand.expand(actual);
        System.out.println(actual);
//        Tensor expected = parse("a*a*a+b*b*b+3*a*a*b+3*a*b*b");
//        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test(timeout = 200)
    public void test9Concurrent() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("Power[a+b,30]");
            actual = Expand.expand(actual, 4);
            Assert.assertTrue(actual.size() == 31);
        }
    }

    @Test
    public void test10() {
        for (int i = 2; i < 30; ++i) {
            Tensor actual = Tensors.pow(parse("a+b"), i);
            actual = Expand.expand(actual);
            Assert.assertTrue(actual.size() == i + 1);
        }
    }

    @Test
    public void test11() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("Power[a_i^i+b_i^i,2]");
            actual = Expand.expand(actual);
            Tensor expected = parse("2*b_{i}^{i}*a_{a}^{a}+a_{i}^{i}*a_{a}^{a}+b_{i}^{i}*b_{a}^{a}");
            Assert.assertTrue(TensorUtils.compare(actual, expected));
        }
    }

    @Test
    public void test12() {
        Tensor actual = parse("f_mn*(f^mn+r^mn)-r_ab*f^ab");
        actual = Expand.expand(actual);
        Tensor expected = parse("f_{mn}*f^{mn}");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test13() {
        Tensor actual = parse("((a+b)*(c+a)-b*a)*f_mn*(f^mn+r^mn)");
        actual = Expand.expand(actual);
        Tensor expected = parse("(a*a+b*c+a*c)*f_mn*f^mn+(a*a+b*c+a*c)*f_mn*r^mn");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test14() {
        Tensor actual = parse("(a+b)*f_mn*(f^mn+r^mn)-(a+b*(c+d))*r_ab*(f^ab+r^ab)");
        System.out.println(actual);
        actual = Expand.expand(actual);
        System.out.println(actual);
    }

    @Test
    public void test15() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("(a+b)*(a*f_m+b*g_m)*(b*f^m+a*g^m)");
            actual = Expand.expand(actual);
            Tensor expected = parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
            Assert.assertTrue(TensorUtils.compare(actual, expected));
        }
    }

    @Test
    public void test16() {
        Tensor actual = parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)");
        System.out.println(actual);
        actual = Expand.expand(actual);
        System.out.println(actual);
    }

    @Test
    public void test17() {
        Tensor actual = Expand.expand(parse("((a+b)*(c+a)-a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+a)*r_ab*(f^ab+r^ab)"));
        assertAllBracketsExpanded(actual);
        Tensor expected = parse("(2*c*b+2*Power[a, 2]+-2*a)*r_{ab}*f^{ab}+(-1*b*a+c*b+-1*c*a+Power[a, 2]+-1*a)*r^{ab}*r_{ab}+(b*a+c*b+c*a+Power[a, 2]+-1*a)*f^{mn}*f_{mn}");
        TAssert.assertParity(actual, expected);
    }

    public static void assertAllBracketsExpanded(Tensor tensor) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            Tensor current = iterator.current();
            if (current instanceof Product) {
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
    public void test19(){
        Tensor tensor = parse("T_ij^ij*N_as^sa*K^fd_df");
        Tensor result =  Expand.expand(tensor);
        Assert.assertTrue(tensor == result);
    }
    
    @Test
    public void test20(){
        Tensor tensor = parse("(a+b)*T_ij^ij*N_as^sa*K^fd_df+a*b*F_m^m");
        Tensor result =  Expand.expand(tensor);
        Assert.assertTrue(tensor == result);
    }
}