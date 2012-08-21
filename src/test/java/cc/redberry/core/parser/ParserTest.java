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
import org.junit.Ignore;
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
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, Tensors.parse("a*1/b")));
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
        Assert.assertTrue(TensorUtils.equalsExactly(u, v));
        Assert.assertTrue(v instanceof Product);
    }

    @Test
    public void testProductPowers2() {
        Tensor u = Tensors.parse("2*a*a*c/b*1/4*b/a/a");
        Tensor v = Tensors.parse("c/2");
        System.out.println(u);
        Assert.assertTrue(TensorUtils.equalsExactly(u, v));
        Assert.assertTrue(v instanceof Product);
    }

    @Test
    public void testProductPowers3() {
        Tensor u = Tensors.parse("Power[2-3*i,2]*2*a*a*c/b*1/4*b/a/a/(2-3*i)");
        Tensor v = Tensors.parse("c*(1+3/(-2)*i)");
        Assert.assertTrue(TensorUtils.equalsExactly(u, v));
        Assert.assertTrue(v instanceof Product);
    }

    @Test
    public void test123() {
        CC.resetTensorNames(1202870200402377417L);
        Tensor t = Tensors.parse("(-s+pT**2)**4*s**4");
        Assert.assertEquals(t.toString(), "(-s+pT**2)**4*s**4");
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

    @Test
    public void testPower5() {
        Tensor t = Tensors.parse("a**(-1)");
        Assert.assertEquals(Power.class, t.getClass());
    }

    @Test
    public void testPower6() {
        Tensor t = Tensors.parse("a**b**c");
        Assert.assertEquals(Power.class, t.getClass());
        Assert.assertEquals(Power.class, t.get(1).getClass());
    }

    @Test
    public void testSum() {
        Tensor t = Tensors.parse("Power[M, 14] - 135*s*Power[M, 14] + 27*Power[M, 16] + 45*Power[M, 20] - 211*s*Power[M, 12]*Power[pT, 2] + 38*Power[M, 14]*Power[pT, 2] + 2*Power[M, 12]*(-3*s + Power[pT, 2]) + 45*Power[M, 18]*(-2*s + Power[pT, 2]) + 60*Power[M, 18]*(-3*s + 2*Power[pT, 2]) - 374*s*Power[M, 10]*Power[pT, 4] + 38*Power[M, 12]*Power[pT, 4] - 92*s*Power[M, 8]*Power[pT, 6] + 27*Power[M, 10]*Power[pT, 6] - 54*s*Power[M, 6]*Power[pT, 8] + 324*Power[M, 12]*Power[s, 2] + 616*Power[M, 10]*Power[pT, 2]*Power[s, 2] + 882*Power[M, 8]*Power[pT, 4]*Power[s, 2] + 698*Power[M, 6]*Power[pT, 6]*Power[s, 2] + 135*Power[M, 4]*Power[pT, 8]*Power[s, 2] + 27*Power[M, 2]*Power[pT, 10]*Power[s, 2] + Power[M, 10]*(-11*s*Power[pT, 2] + 3*Power[pT, 4] + 16*Power[s, 2]) + 12*Power[M, 16]*(-24*s*Power[pT, 2] + 6*Power[pT, 4] + 17*Power[s, 2]) + 3*Power[M, 16]*(-41*s*Power[pT, 2] + 8*Power[pT, 4] + 34*Power[s, 2]) + 3*Power[M, 16]*(-59*s*Power[pT, 2] + 12*Power[pT, 4] + 51*Power[s, 2]) + 6*Power[M, 16]*(-70*s*Power[pT, 2] + 18*Power[pT, 4] + 51*Power[s, 2]) + Power[M, 14]*(-456*s*Power[pT, 4] + 48*Power[pT, 6] + 771*Power[pT, 2]*Power[s, 2] - 360*Power[s, 3]) + 3*Power[M, 14]*(-55*s*Power[pT, 4] + 6*Power[pT, 6] + 118*Power[pT, 2]*Power[s, 2] - 60*Power[s, 3]) + 12*Power[M, 14]*(-54*s*Power[pT, 4] + 6*Power[pT, 6] + 98*Power[pT, 2]*Power[s, 2] - 45*Power[s, 3]) + 9*Power[M, 14]*(-27*s*Power[pT, 4] + 3*Power[pT, 6] + 59*Power[pT, 2]*Power[s, 2] - 30*Power[s, 3]) + 2*Power[M, 8]*(-5*s*Power[pT, 4] + Power[pT, 6] + 15*Power[pT, 2]*Power[s, 2] - 12*Power[s, 3]) - 486*Power[M, 10]*Power[s, 3] - 1091*Power[M, 8]*Power[pT, 2]*Power[s, 3] - 1557*Power[M, 6]*Power[pT, 4]*Power[s, 3] - 1087*Power[M, 4]*Power[pT, 6]*Power[s, 3] - 346*Power[M, 2]*Power[pT, 8]*Power[s, 3] - 27*Power[pT, 10]*Power[s, 3] + s*Power[M, 10]*(616*s*Power[pT, 6] - 87*Power[pT, 8] - 1365*Power[pT, 4]*Power[s, 2] + 1089*Power[pT, 2]*Power[s, 3] - 270*Power[s, 4]) + s*Power[M, 10]*(310*s*Power[pT, 6] - 63*Power[pT, 8] - 696*Power[pT, 4]*Power[s, 2] + 651*Power[pT, 2]*Power[s, 3] - 180*Power[s, 4]) + s*Power[M, 4]*(13*s*Power[pT, 6] - 3*Power[pT, 8] - 33*Power[pT, 4]*Power[s, 2] + 39*Power[pT, 2]*Power[s, 3] - 10*Power[s, 4]) + 486*Power[M, 8]*Power[s, 4] + 1134*Power[M, 6]*Power[pT, 2]*Power[s, 4] + 1340*Power[M, 4]*Power[pT, 4]*Power[s, 4] + 665*Power[M, 2]*Power[pT, 6]*Power[s, 4] - 27*Power[pT, 8]*Power[s, 4] + Power[M, 6]*(-6*s*Power[pT, 6] + Power[pT, 8] + 22*Power[pT, 4]*Power[s, 2] - 47*Power[pT, 2]*Power[s, 3] + 21*Power[s, 4]) - 4*s*Power[M, 10]*(-377*s*Power[pT, 6] + 51*Power[pT, 8] + 753*Power[pT, 4]*Power[s, 2] - 561*Power[pT, 2]*Power[s, 3] + 135*Power[s, 4]) - 2*s*Power[M, 10]*(-391*s*Power[pT, 6] + 72*Power[pT, 8] + 747*Power[pT, 4]*Power[s, 2] - 651*Power[pT, 2]*Power[s, 3] + 180*Power[s, 4]) + 2*Power[M, 12]*(-180*s*Power[pT, 6] + 6*Power[pT, 8] + 557*Power[pT, 4]*Power[s, 2] - 612*Power[pT, 2]*Power[s, 3] + 216*Power[s, 4]) + Power[M, 12]*(-129*s*Power[pT, 6] + 6*Power[pT, 8] + 463*Power[pT, 4]*Power[s, 2] - 591*Power[pT, 2]*Power[s, 3] + 216*Power[s, 4]) + 2*Power[M, 12]*(-252*s*Power[pT, 6] + 9*Power[pT, 8] + 920*Power[pT, 4]*Power[s, 2] - 1008*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 12]*(-189*s*Power[pT, 6] + 9*Power[pT, 8] + 769*Power[pT, 4]*Power[s, 2] - 951*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + Power[M, 6]*Power[s, 2]*(-218*s*Power[pT, 8] + 75*Power[pT, 10] + 513*Power[pT, 6]*Power[s, 2] - 842*Power[pT, 4]*Power[s, 3] + 483*Power[pT, 2]*Power[s, 4] - 72*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-439*s*Power[pT, 8] + 81*Power[pT, 10] + 898*Power[pT, 6]*Power[s, 2] - 844*Power[pT, 4]*Power[s, 3] + 357*Power[pT, 2]*Power[s, 4] - 54*Power[s, 5]) + Power[M, 6]*Power[s, 2]*(-115*s*Power[pT, 8] + 39*Power[pT, 10] + 271*Power[pT, 6]*Power[s, 2] - 433*Power[pT, 4]*Power[s, 3] + 243*Power[pT, 2]*Power[s, 4] - 36*Power[s, 5]) + 4*Power[M, 6]*Power[s, 2]*(-227*s*Power[pT, 8] + 42*Power[pT, 10] + 456*Power[pT, 6]*Power[s, 2] - 425*Power[pT, 4]*Power[s, 3] + 180*Power[pT, 2]*Power[s, 4] - 27*Power[s, 5]) - 324*Power[M, 6]*Power[s, 5] - 702*Power[M, 4]*Power[pT, 2]*Power[s, 5] - 697*Power[M, 2]*Power[pT, 4]*Power[s, 5] - 27*Power[pT, 6]*Power[s, 5] + s*Power[M, 8]*(133*s*Power[pT, 8] - 12*Power[pT, 10] - 381*Power[pT, 6]*Power[s, 2] + 665*Power[pT, 4]*Power[s, 3] - 492*Power[pT, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(149*s*Power[pT, 8] - 12*Power[pT, 10] - 407*Power[pT, 6]*Power[s, 2] + 663*Power[pT, 4]*Power[s, 3] - 486*Power[pT, 2]*Power[s, 4] + 102*Power[s, 5]) + 2*s*Power[M, 8]*(344*s*Power[pT, 8] - 18*Power[pT, 10] - 1142*Power[pT, 6]*Power[s, 2] + 1476*Power[pT, 4]*Power[s, 3] - 810*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + s*Power[M, 8]*(301*s*Power[pT, 8] - 18*Power[pT, 10] - 1041*Power[pT, 6]*Power[s, 2] + 1415*Power[pT, 4]*Power[s, 3] - 798*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + 135*Power[M, 4]*Power[s, 6] + 243*Power[M, 2]*Power[pT, 2]*Power[s, 6] + 108*Power[pT, 4]*Power[s, 6] + 2*Power[M, 4]*Power[s, 2]*(-23*s*Power[pT, 10] + 6*Power[pT, 12] + 32*Power[pT, 8]*Power[s, 2] - 122*Power[pT, 6]*Power[s, 3] + 173*Power[pT, 4]*Power[s, 4] - 66*Power[pT, 2]*Power[s, 5] + 6*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-25*s*Power[pT, 10] + 6*Power[pT, 12] + 42*Power[pT, 8]*Power[s, 2] - 127*Power[pT, 6]*Power[s, 3] + 175*Power[pT, 4]*Power[s, 4] - 66*Power[pT, 2]*Power[s, 5] + 6*Power[s, 6]) + 2*Power[M, 4]*Power[s, 2]*(-86*s*Power[pT, 10] + 9*Power[pT, 12] + 269*Power[pT, 8]*Power[s, 2] - 374*Power[pT, 6]*Power[s, 3] + 263*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) + Power[M, 4]*Power[s, 2]*(-88*s*Power[pT, 10] + 9*Power[pT, 12] + 276*Power[pT, 8]*Power[s, 2] - 382*Power[pT, 6]*Power[s, 3] + 265*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) - 27*Power[M, 2]*Power[s, 7] - 27*Power[pT, 2]*Power[s, 7] - s*Power[M, 2]*(-(s*Power[pT, 4]) + 2*Power[pT, 6] + 10*Power[pT, 2]*Power[s, 2] - 2*Power[s, 3])*Power[-s + Power[pT, 2], 2] + Power[pT, 2]*Power[s, 2]*Power[-s + Power[pT, 2], 4] + 6*Power[pT, 4]*Power[s, 4]*Power[-s + Power[pT, 2], 4] - Power[M, 2]*(17*s*Power[pT, 4] + 12*Power[pT, 6] + 44*Power[pT, 2]*Power[s, 2] - 12*Power[s, 3])*Power[s, 3]*Power[-(pT*s) + Power[pT, 3], 2] - Power[M, 2]*(9*s*Power[pT, 4] + 6*Power[pT, 6] + 22*Power[pT, 2]*Power[s, 2] - 6*Power[s, 3])*Power[s, 3]*Power[-(pT*s) + Power[pT, 3], 2] + 4*Power[M, 2]*Power[s, 3]*(10*s*Power[pT, 4] - 3*Power[pT, 6] - 11*Power[pT, 2]*Power[s, 2] + 3*Power[s, 3])* Power[-(pT*s) + Power[pT, 3], 2] + Power[M, 2]*Power[s, 3]*(21*s*Power[pT, 4] - 6*Power[pT, 6] - 22*Power[pT, 2]*Power[s, 2] + 6*Power[s, 3])*Power[-(pT*s) + Power[pT, 3], 2]");
        for (Tensor a : t)
            Assert.assertTrue(a instanceof Product || a instanceof Power);
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
        Assert.assertTrue(TensorUtils.equalsExactly(e, t));
    }

    @Test
    public void testSin4() {
        Tensor t = Tensors.parse("Sin[0]");
        Tensor e = Tensors.parse("0");
        Assert.assertTrue(TensorUtils.equalsExactly(e, t));
    }

    @Test(expected = RuntimeException.class)
    public void testSim1() {
        Tensor t = Tensors.parse("1^3");
        Tensor e = Tensors.parse("x");
        Assert.assertTrue(TensorUtils.equalsExactly(e, t));
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

    @Ignore
    @Test
    public void testExpression4() {
        Tensor e = Tensors.parse("(a = x+y)*7");
        Assert.assertEquals(Product.class, e.getClass());
    }

    @Test
    public void testMinusMinus1() {
        Tensor t = Tensors.parse("--a");
        Tensor e = Tensors.parse("a");
        Assert.assertTrue(TensorUtils.equals(t, e));
    }

    @Test
    public void testMinusMinus2() {
        Tensor t = Tensors.parse("1--a*(b+--c)");
        Tensor e = Tensors.parse("1+a*(b+c)");
        Assert.assertTrue(TensorUtils.equals(t, e));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyStatement() {
        Tensors.parse("");
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

    @Test
    public void testIndices2() {
        SimpleIndices indices = ParserIndices.parseSimple("_{\\mu_9}");
        Assert.assertTrue(indices.size() == 1);
    }

    @Test
    public void testIndices3() {
        SimpleIndices indices = ParserIndices.parseSimple("_{m_{0}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{9}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{8}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{7}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{6}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{5}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{4}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{3}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{2}}");
        Assert.assertTrue(indices.size() == 1);
        indices = ParserIndices.parseSimple("_{m_{1}}");
        Assert.assertTrue(indices.size() == 1);
    }

    @Test
    public void testPowerAsp() {
        System.out.println(Tensors.parse("24**2"));
    }
}
