/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseExpression;
import static cc.redberry.core.transformations.Transformation.Util.applySequentially;
import static cc.redberry.core.transformations.Transformation.Util.applyUntilUnchanged;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandTensorsTransformationTest {
    @Test
    public void test1() throws Exception {
        Tensor t = parse("((a+b)*f_a + (a+c)*t_a)*c*k^a");
        TAssert.assertEquals("c*(a+b)*f_a*k^a + c*(a+c)*t_a*k^a", expand(t));
    }

    @Test
    public void test2() throws Exception {
        Tensor t = parse("((a+b)*(f_a + r_a) + (a + c)*t_a)*c*k^a");
        TAssert.assertEquals("c*(a+b)*f_a*k^a + c*(a+b)*r_a*k^a + c*(a+c)*t_a*k^a", expand(t));
    }

    @Test
    public void test3() throws Exception {
        Tensor t = parse("((a+b)*(f_a + r_a) + (a + c)*t_a)*(c+r)*k^a");
        TAssert.assertEquals("(c+r)*(a+b)*f_a*k^a + (c+r)*(a+b)*r_a*k^a + (c+r)*(a+c)*t_a*k^a", expand(t));
    }

    @Test
    public void test4() throws Exception {
        Tensor t = parse("((a+b)*(c+d)*(f_a + (k+i)*r_a) + (a + c)*t_a)*(c+r)*k^a");
        TAssert.assertEquals("(c+r)*(a+b)*(c+d)*f_a*k^a + (c+r)*(a+b)*(c+d)*(k+i)*r_a*k^a + (c+r)*(a+c)*t_a*k^a", expand(t));
    }

    @Test
    public void test5() throws Exception {
        Tensor t = parse("((a+b)*(c+d)*(f_a + (k+i)*r_a) + (a + c)*t_a)*(c+r)*((a+b)*k^a + (c+d)*t^a)");
        TAssert.assertEquals("(a+b)*(c+r)*(a+b)*(c+d)*f_a*k^a + (a+b)*(c+r)*(a+b)*(c+d)*(k+i)*r_a*k^a + (a+b)*(c+r)*(a+c)*t_a*k^a + (c+d)*(c+r)*(a+b)*(c+d)*f_a*t^a + (c+d)*(c+r)*(a+b)*(c+d)*(k+i)*r_a*t^a + (c+d)*(c+r)*(a+c)*t_a*t^a", expand(t));
    }

    @Test
    public void test6() throws Exception {
        Tensor t = parse("((a+b)*(c+d)*(f_a + (k+i)*t_a) + (a + c)*t_a)*(c+r)*((a+b)*f^a + (c+d)*t^a)");
        TAssert.assertEquals(ExpandTransformation.expand(t), ExpandTransformation.expand(expand(t)));
        TAssert.assertEquals("(c+r)*(c+d)*(a+b)**2*f_a*f^a + (c+r)*((a+b)*(c+d)*(k+i) + (a + c))*t_a*(a+b)*f^a + (c+r)*(a+b)*(c+d)*f_a*(c+d)*t^a + (c+r)*((a+b)*(c+d)*(k+i) + (a + c))*t_a*(c+d)*t^a", expand(t));
    }


    @Test
    public void test7() throws Exception {
        Tensor t = parse("((a+b)*(c+d)*(f_a + (k+i)*t_a) + (a + c)*t_a)*(c+r)*((a+b)*f^a + (c+d)*t^a)");
        Transformation[] subs = {parseExpression("f_a*f^a = 1"), parseExpression("f_a*t^a = 2"), parseExpression("t_a*t^a = 3")};
        TAssert.assertEquals(ExpandTransformation.expand(t, subs), ExpandTransformation.expand(expand(t, subs)));
        TAssert.assertEquals("(c+r)*(c+d)*(a+b)**2 + (c+r)*((a+b)*(c+d)*(k+i) + (a + c))*(a+b)*2 + (c+r)*(a+b)*(c+d)*(c+d)*2 + (c+r)*((a+b)*(c+d)*(k+i) + (a + c))*(c+d)*3", expand(t, subs));
    }


    @Test
    public void test8() throws Exception {
        RandomTensor rnd = new RandomTensor(false);
        rnd.addToNamespace(parse("a"), parse("b"), parse("c"), parse("f_a"), parse("t_a"));
        for (int i = 0; i < 100; ++i) {
            Tensor t = rnd.nextTensorTree(3, new RandomTensor.Parameters(3, 5, 1, 4), IndicesFactory.EMPTY_INDICES);
            TAssert.assertEquals(ExpandTransformation.expand(t), ExpandTransformation.expand(expand(t)));
        }
    }

    @Test
    public void test9() throws Exception {
        RandomTensor rnd = new RandomTensor(false);
        rnd.addToNamespace(parse("a"), parse("b"), parse("c"), parse("f_a"), parse("t_a"));
        Transformation[] subs = {parseExpression("f_a*f^a = a"), parseExpression("f_a*t^a = b"), parseExpression("t_a*t^a = c")};
        for (int i = 0; i < 1000; ++i) {
            Tensor t = rnd.nextTensorTree(3, new RandomTensor.Parameters(3, 5, 1, 4), IndicesFactory.EMPTY_INDICES);
            t = applyUntilUnchanged(t, new TransformationCollection(subs));
            TAssert.assertEquals(ExpandTransformation.expand(applySequentially(ExpandTransformation.expand(applySequentially(t, subs), subs), subs)), ExpandTransformation.expand(expand(t, subs)));
        }
    }

    @Test
    public void test10() throws Exception {
        Tensor t = parse("(2*(c+a)-164*a)*(f_{a}+t_{a})*f^{a}");
        Transformation[] subs = {parseExpression("f_a*f^a = a"), parseExpression("f_a*t^a = b"), parseExpression("t_a*t^a = c")};
        TAssert.assertEquals(ExpandTransformation.expand(t, subs), ExpandTransformation.expand(expand(t, subs)));
    }

    @Test
    public void test11() throws Exception {
        Tensor t = parse("-31*(69*c*f_{a}*t^{a}+c*a)*(-7*b*f^{b}*t_{b}+c*f^{b}*f_{b})");
        Transformation[] subs = {parseExpression("f_a*f^a = a"), parseExpression("f_a*t^a = b"), parseExpression("t_a*t^a = c")};
        TAssert.assertSymbolic(expand(t, subs));
    }

    @Test
    public void test12() throws Exception {
        Tensor t = parse("-2*(b+a)*f^{b}*f^{a}*t_{b}*(-89*a*t_{a}-26*b*f_{a})");
        Transformation[] subs = {parseExpression("f_a*f^a = a"), parseExpression("f_a*t^a = b"), parseExpression("t_a*t^a = c")};
        TAssert.assertSymbolic(expand(t, subs));
        TAssert.assertEquals(ExpandTransformation.expand(ExpandTransformation.expand(applySequentially(t, subs), subs)), ExpandTransformation.expand(expand(t, subs)));
    }

    @Test
    public void test13() throws Exception {
        Tensor t = parse("-80*(-94*a*b-37*b*f^{c}*f_{c})*(-58*c*f_{a}+t^{d}*f_{d}*f_{a})*(t^{b}*f_{b}*f^{a}+c*a*b*f^{a})");
        Transformation[] subs = {parseExpression("f_a*f^a = a"), parseExpression("f_a*t^a = b"), parseExpression("t_a*t^a = c")};
        TAssert.assertSymbolic(expand(t, subs));
        TAssert.assertEquals(ExpandTransformation.expand(applySequentially(ExpandTransformation.expand(applySequentially(t, subs), subs), subs)), ExpandTransformation.expand(expand(t, subs)));
    }

    @Test
    public void test14() throws Exception {
        Tensor t = parse("2*((a+b)*(a_i*a^i + b_i*b^i) + (c+d)*(a_i*a^i + b_i*b^i))*((a+b)*(a_i*a^i + b_i*b^i) + (c+d)*(a_i*a^i + b_i*b^i))");
        TAssert.assertEquals("4*(d+b+c+a)**2*a_{i}*a^{i}*b_{a}*b^{a}+2*(d+b+c+a)**2*a_{i}*a^{i}*a_{a}*a^{a}+2*(d+b+c+a)**2*b_{i}*b^{i}*b_{a}*b^{a}",expand(t));
    }

    @Ignore
    @Test
    public void testPerformance() throws Exception {
        RandomTensor rnd = new RandomTensor(false);
        rnd.addToNamespace(parse("a"), parse("b"), parse("c"), parse("f_a"), parse("t_a"));
        long start, etTiming, feTiming;
        for (int i = 0; i < 1000; ++i) {
            Tensor t = rnd.nextTensorTree(4, new RandomTensor.Parameters(3, 4, 3, 4), IndicesFactory.EMPTY_INDICES);
            start = System.currentTimeMillis();
            Tensor et = expand(t);
            etTiming = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            Tensor fe = ExpandTransformation.expand(t);
            feTiming = System.currentTimeMillis() - start;

            System.out.println("ExpandTensors: " + etTiming + ", Expand: " + feTiming);
            TAssert.assertEquals(fe, ExpandTransformation.expand(et));
        }
    }

    private static Tensor expand(Tensor t) {
        return ExpandTensorsTransformation.EXPAND_TENSORS.transform(t);
    }

    private static Tensor expand(Tensor t, Transformation[] transformations) {
        return new ExpandTensorsTransformation(transformations).transform(t);
    }
}