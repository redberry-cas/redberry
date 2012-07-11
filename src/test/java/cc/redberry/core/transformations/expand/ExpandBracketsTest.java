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

import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandBracketsTest {

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*c+a*c");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("2*a*c+b*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test2() {
        Tensor t = parse("(a+b)*c-a*c");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("b*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test3() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*p_i");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("b*c*p_i");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test4() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*k_i");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("(a*c+c*b)*p_i-a*c*k_i");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test5() {
        Tensor actual = parse("c*(a*(c+n)+b)");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("c*a*c+c*a*n+c*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test6() {
        Tensor actual = parse("a*(c+b)");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("a*c+a*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test7() {
        Tensor actual = parse("Power[a+b,2]");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("a*a+b*b+2*a*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test8() {
        Tensor actual = parse("Power[a+b,3]");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("a*a*a+b*b*b+3*a*a*b+3*a*b*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test(timeout = 200)
    public void test9Concurrent() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("Power[a+b,30]");
            actual = ExpandBrackets.expandBrackets(actual, 4);
            Assert.assertTrue(actual.size() == 31);
        }
    }

    @Test
    public void test10() {
        for (int i = 2; i < 30; ++i) {
            Tensor actual = Tensors.pow(parse("a+b"), i);
            actual = ExpandBrackets.expandBrackets(actual);
            Assert.assertTrue(actual.size() == i + 1);
        }
    }

    @Test
    public void test11() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor actual = parse("Power[a_i^i+b_i^i,2]");
            actual = ExpandBrackets.expandBrackets(actual);
            Tensor expected = parse("2*b_{i}^{i}*a_{a}^{a}+a_{i}^{i}*a_{a}^{a}+b_{i}^{i}*b_{a}^{a}");
            Assert.assertTrue(TensorUtils.compare(actual, expected));
        }
    }

    @Test
    public void test12() {
        Tensor actual = parse("f_mn*(f^mn+r^mn)-r_ab*f^ab");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("f_{mn}*f^{mn}");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test13() {
        Tensor actual = parse("((a+b)*(c+a)-b*a)*f_mn*(f^mn+r^mn)");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("(a*a+b*c+a*c)*f_mn*f^mn+(a*a+b*c+a*c)*f_mn*r^mn");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test14() {
        Tensor actual = parse("(a+b*(c+d))*f_mn*(f^mn+r^mn)-(a+b*(c+d))*r_ab*(f^ab+r^ab)");
        System.out.println(actual);
        actual = ExpandBrackets.expandBrackets(actual);
        System.out.println(actual);
    }

    @Test
    public void test15() {
        Tensor actual = parse("(a+b)*(a*f_m+b*g_m)*(b*f^m+a*g^m)");
        System.out.println(actual);
        actual = ExpandBrackets.expandBrackets(actual);
        System.out.println(actual);
    }

    @Test
    public void test16() {
        Tensor actual = parse("((a+b)*(c+a)-b*a)*f_mn*(f^mn+r^mn)-((a-b)*(c-a)+b*a)*r_ab*(f^ab+r^ab)");
        System.out.println(actual);
        actual = ExpandBrackets.expandBrackets(actual);
        System.out.println(actual);
    }
}