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
package cc.redberry.core.parser;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.TensorUtils;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserTest {

    @Test
    public void test1() {
        ParseNode node = Parser.DEFAULT.parse("2*a_\\mu-b_\\mu/(c*x)*x[x,y]");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("_\\mu")));
    }

    @Test
    public void test2() {
        ParseNode node = Parser.DEFAULT.parse("f[a_\\mu] - f[b_\\mu/ (c * g) * g[x, y]]");
        ParseNode expected = new ParseNode(TensorType.Sum,
                                           new ParseNodeTensorField(IndicesFactory.EMPTY_SIMPLE_INDICES, "f", new ParseNode[]{new ParseNodeSimpleTensor(ParserIndices.parseSimple("_\\mu"), "a")}, new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES}),
                                           new ParseNode(TensorType.Product,
                                                         new ParseNodeNumber(Complex.MINUSE_ONE),
                                                         new ParseNodeTensorField(IndicesFactory.EMPTY_SIMPLE_INDICES, "f",
                                                                                  new ParseNode[]{new ParseNode(TensorType.Product,
                                                                                                                new ParseNodeSimpleTensor(ParserIndices.parseSimple("_\\mu"), "b"),
                                                                                                                new ParseNode(TensorType.Power, new ParseNode(TensorType.Product, new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "c"), new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "g")),
                                                                                                                              new ParseNodeNumber(Complex.MINUSE_ONE)),
                                                                                                                new ParseNodeTensorField(IndicesFactory.EMPTY_SIMPLE_INDICES, "g",
                                                                                                                                         new ParseNode[]{new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "x"),
                                                                                                                                                         new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "y")},
                                                                                                                                         new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES, IndicesFactory.EMPTY_SIMPLE_INDICES}))},
                                                                                  new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES})));
        Assert.assertEquals(expected, node);
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("")));
    }

    @Test
    public void test3() {
        ParseNode node = Parser.DEFAULT.parse("f[b_\\mu/(c*g)*g[x,y]]");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("")));
    }

    @Test
    public void test4() {
        ParseNode node = Parser.DEFAULT.parse("a-b");
        ParseNode expected = new ParseNode(TensorType.Sum,
                                           new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "a"),
                                           new ParseNode(TensorType.Product,
                                                         new ParseNodeNumber(Complex.MINUSE_ONE),
                                                         new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "b")));
        Assert.assertEquals(expected, node);
    }

    @Test
    public void testReallySimpleTensor() {
        ParseNode node = Parser.DEFAULT.parse("S^k*(c_k*Power[a,1]/a-b_k)");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("^k_k")));
    }

    @Test
    public void testProductPowers() {
        ParseNode node = Parser.DEFAULT.parse("a/b");
        Tensor tensor = node.toTensor();
        ParseNode expectedNode = new ParseNode(TensorType.Product,
                                               new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "a"),
                                               new ParseNode(TensorType.Power,
                                                             new ParseNodeSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "b"),
                                                             new ParseNodeNumber(Complex.MINUSE_ONE)));
        Assert.assertEquals(expectedNode, node);
        Assert.assertTrue(tensor instanceof Product);
        Assert.assertTrue(tensor.getIndices().size() == 0);
        Assert.assertTrue(tensor.size() == 2);
        Assert.assertTrue(tensor.get(0) instanceof Power || tensor.get(1) instanceof Power);
        Assert.assertTrue(TensorUtils.equals(tensor, Tensors.parse("a*1/b")));
    }

    @Test
    public void testProductPowers0() {
        ParseNode node = Parser.DEFAULT.parse("1/0*a");
        System.out.println(node);
    }

    @Test
    public void testProductPowers1() {
        Tensor u = Tensors.parse("a*c/b*1/4");
        Tensor v = Tensors.parse("(a*c)/(4*b)");
        Assert.assertTrue(TensorUtils.equals(u, v));
        Assert.assertTrue(v instanceof Product);
    }

    @Test
    public void testProductPowers2() {
        Tensor u = Tensors.parse("2*a*a*c/b*1/4*b/a/a");
        Tensor v = Tensors.parse("c/2");
        System.out.println(u);
        Assert.assertTrue(TensorUtils.equals(u, v));
        Assert.assertTrue(v instanceof Product);
    }

    @Test
    public void testProductPowers3() {
        Tensor u = Tensors.parse("Power[2-3*i,2]*2*a*a*c/b*1/4*b/a/a/(2-3*i)");
        Tensor v = Tensors.parse("c*(1+3/(-2)*i)");
        Assert.assertTrue(TensorUtils.equals(u, v));
        Assert.assertTrue(v instanceof Product);
    }

    @Test
    public void test123() {
        CC.resetTensorNames(1202870200402377417L);
        Tensor t = Tensors.parse("Power[Power[pT,2] - s, 4]*Power[s, 4]");
        Assert.assertEquals(t.toString(), "Power[-1*s+Power[pT, 2], 4]*Power[s, 4]");
    }

    @Test
    public void testPower1() {
        Tensor t = Tensors.parse("Power[x,y]");
        Assert.assertEquals(Power.class, t.getClass());
    }

    @Test
    public void testPower2() {
        Tensor t = Tensors.parse("Power[x,x]+Power[y,x]");
        Assert.assertEquals(Sum.class, t.getClass());
        Assert.assertEquals(t.get(0).getClass(), Power.class);
    }

    @Test
    public void testPower3() {
        Tensor t = Tensors.parse("Power[Power[x,z],y]");
        Assert.assertEquals(Power.class, t.getClass());
    }

    @Test(expected = ParserException.class)
    public void testPower4() {
        Tensors.parse("Power[Power[x,z,z],y]");
    }

    @Test(expected = ParserException.class)
    public void testSin1() {
        Tensors.parse("Sin[x,x]+Sin[y,x]");
    }

    @Test
    public void testSin2() {
        Tensor t = Tensors.parse("Sin[x]+Sin[y]");
        Assert.assertEquals(Sum.class, t.getClass());
    }

    @Test
    public void testSin3() {
        Tensor t = Tensors.parse("Sin[ArcSin[x]]");
        Tensor e = Tensors.parse("x");
        Assert.assertTrue(TensorUtils.equals(e, t));
    }

    @Test
    public void testSin4() {
        Tensor t = Tensors.parse("Sin[0]");
        Tensor e = Tensors.parse("0");
        Assert.assertTrue(TensorUtils.equals(e, t));
    }

    @Test(expected = RuntimeException.class)
    public void testSim1() {
        Tensor t = Tensors.parse("1^3");
        Tensor e = Tensors.parse("x");
        Assert.assertTrue(TensorUtils.equals(e, t));
    }

    @Test
    public void testExpression1() {
        Tensor e = Tensors.parse("a = x+y");
        Assert.assertEquals(Expression.class, e.getClass());
    }

    @Test(expected = TensorException.class)
    public void testExpression2() {
        Tensors.parse("a_m = x+y");
    }

    @Test
    public void testExpression4() {
        Tensor e = Tensors.parse("(a = x+y)*7");
        Assert.assertEquals(Product.class, e.getClass());
    }

    @Test
    public void testIndices1() {
        ParseNode node = Parser.DEFAULT.parse("f_a*f^a*j_nm^n");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("_a^a_nm^n")));
    }

    @Test
    public void test15() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensors.parse("(a+b)*(a*f_m+b*g_m)*(b*f^m+a*g^m)");
            Tensors.parse("(Power[a, 2]*b+a*Power[b, 2])*g_{m}*g^{m}+(Power[a, 3]+Power[a, 2]*b+a*Power[b, 2]+Power[b, 3])*f^{m}*g_{m}+(Power[a, 2]*b+a*Power[b, 2])*f_{m}*f^{m}");
        }
    }
}
