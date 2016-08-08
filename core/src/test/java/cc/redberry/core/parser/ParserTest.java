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
package cc.redberry.core.parser;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.context.VarDescriptor;
import cc.redberry.core.context.VarIndicesProvider;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserTest {

    @After
    public void after() {
        CC.setParserAllowsSameVariance(false);
    }

    @Test
    public void test1() {
        ParseToken node = Parser.DEFAULT.parse("2*a_\\mu-b_\\mu/(c*x)*x[x,y]");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("_\\mu")));
    }

//    @Test
//    public void test2() {
//        ParseToken node = Parser.DEFAULT.parse("f[a_\\mu] - f[b_\\mu/ (c * g) * g[x, y]]");
//        ParseToken expected = new ParseToken(TokenType.Sum,
//                new ParseTokenTensorField(IndicesFactory.EMPTY_SIMPLE_INDICES, "f", new ParseToken[]{new ParseTokenSimpleTensor(ParserIndices.parseSimple("_\\mu"), "a")}, new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES}),
//                new ParseToken(TokenType.Product,
//                        new ParseTokenNumber(Complex.MINUS_ONE),
//                        new ParseTokenTensorField(IndicesFactory.EMPTY_SIMPLE_INDICES, "f",
//                                new ParseToken[]{new ParseToken(TokenType.Product,
//                                        new ParseTokenSimpleTensor(ParserIndices.parseSimple("_\\mu"), "b"),
//                                        new ParseToken(TokenType.Power, new ParseToken(TokenType.Product, new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "c"), new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "g")),
//                                                new ParseTokenNumber(Complex.MINUS_ONE)),
//                                        new ParseTokenTensorField(IndicesFactory.EMPTY_SIMPLE_INDICES, "g",
//                                                new ParseToken[]{new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "x"),
//                                                        new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "y")},
//                                                new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES, IndicesFactory.EMPTY_SIMPLE_INDICES}))},
//                                new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES})));
//        Assert.assertEquals(expected, node);
//        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("")));
//    }

    @Test
    public void test3() {
        ParseToken node = Parser.DEFAULT.parse("f[b_\\mu/(c*g)*g[x,y]]");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("")));
    }

    @Test
    public void test4() {
        ParseToken node = Parser.DEFAULT.parse("a-b");
        ParseToken expected = new ParseToken(TokenType.Sum,
                new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "a"),
                new ParseToken(TokenType.Product,
                        new ParseTokenNumber(Complex.MINUS_ONE),
                        new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "b")));
        Assert.assertEquals(expected, node);
    }

    @Test
    public void testReallySimpleTensor() {
        ParseToken node = Parser.DEFAULT.parse("S^k*(c_k*Power[a,1]/a-b_k)");
        Assert.assertTrue(node.getIndices().equalsRegardlessOrder(ParserIndices.parseSimple("^k_k")));
    }

    @Test
    public void testProductPowers() {
        ParseToken node = Parser.DEFAULT.parse("a/b");
        Tensor tensor = node.toTensor();
        ParseToken expectedNode = new ParseToken(TokenType.Product,
                new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "a"),
                new ParseToken(TokenType.Power,
                        new ParseTokenSimpleTensor(IndicesFactory.EMPTY_SIMPLE_INDICES, "b"),
                        new ParseTokenNumber(Complex.MINUS_ONE)));
        Assert.assertEquals(expectedNode, node);
        Assert.assertTrue(tensor instanceof Product);
        Assert.assertTrue(tensor.getIndices().size() == 0);
        Assert.assertTrue(tensor.size() == 2);
        Assert.assertTrue(tensor.get(0) instanceof Power || tensor.get(1) instanceof Power);
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, Tensors.parse("a*1/b")));
    }

    @Test
    public void testProductPowers0() {
        ParseToken node = Parser.DEFAULT.parse("1/0*a");
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
        Tensor u = Tensors.parse("Power[2-3*I,2]*2*a*a*c/b*1/4*b/a/a/(2-3*I)");
        Tensor v = Tensors.parse("c*(1+3/(-2)*I)");
        Assert.assertTrue(TensorUtils.equalsExactly(u, v));
        Assert.assertTrue(v instanceof Product);
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
    public void testPower7() {
        Tensor t = Tensors.parse("a**b/c");
        Assert.assertEquals(Product.class, t.getClass());
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
        System.out.println(t);
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
        ParseToken node = Parser.DEFAULT.parse("f_a*f^a*j_nm^n");
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
    public void testPowerAsp1() {
        TAssert.assertTensorEquals("25**2", "625");
    }

    @Test
    public void testPowerAsp2() {
        TAssert.assertTensorEquals("1/25**2", "1/625");
    }

    @Test
    public void testPowerAsp3() {
        TAssert.assertTensorEquals("(1/25**2)**(1/2)", "1/25");
    }

    @Test
    public void testPowerAsp4() {
        TAssert.assertTensorEquals("(1/25**2)**(1/2)", "1/25");
    }

    @Test
    public void testPowerAsp5() {
        TAssert.assertTensorEquals("((1/(5+25-5))**2)**(1/2)", "1/25");
    }

    @Test
    public void testPowerAsp6() {
        Tensor t = Tensors.parse("Power[1/2,1/2]");
        TAssert.assertEquals(t, Tensors.parse(t.toString()));
    }

    @Ignore
    @Test
    public void testConflictingIndices1() {
        Tensors.parse("(A_i^i*A_m^n+A_k^k*A_m^n)*(A_i^i*A_d^c+A_k^k*A_d^c)");
    }

    @Test
    public void testSubscripted1() {
        Assert.assertTrue(Tensors.parse("F_{BA_{21}C\\mu\\nu}").getIndices().size() == 5);
    }

    @Test
    public void testSubscripted2() {
        Assert.assertTrue(Tensors.parse("F_{a_{1}b_{1}}").getIndices().size() == 2);
    }

    @Test
    public void blankBrace1() {
        Assert.assertTrue(Tensors.parseSimple("F{}_{BA_{21}C\\mu\\nu}").getName()
                == Tensors.parseSimple("F_{BA_{21}C\\mu\\nu}").getName());
    }

    @Test(expected = InconsistentIndicesException.class)
    public void testII1() {
        parse("A_mn*B^mn*A_mn*B^mn");
    }

    @Test(expected = BracketsError.class)
    public void testBacketsCons1() {
        parse("(1/2*(a+b)");
    }

    @Test
    public void testStrokeIndices1() {
        Tensor t = parse("T_{a'}");
        Assert.assertTrue(IndicesUtils.getType(t.getIndices().get(0))
                == IndexType.Matrix1.getType());
    }

    @Test
    public void testStrokeIndices2() {
        Tensor t = parse("T_{\\alpha'}");
        Assert.assertTrue(IndicesUtils.getType(t.getIndices().get(0))
                == IndexType.Matrix3.getType());
    }

    @Ignore
    @Test(expected = RuntimeException.class)
    public void testFieldND() {
        SimpleTensor field = parseSimple("f[x]");
        SimpleTensor nonField = parseSimple("f");
    }

//    @Test(expected = ParserException.class)
//    public void testKronecker1() {
//        parse("d_a'^b'");
//    }
//
//    @Test(expected = ParserException.class)
//    public void testMetric1() {
//        parse("g_a'b'");
//    }

    @Test(expected = ParserException.class)
    public void testFieldDerivative1() throws Exception {
        Tensor t = parse("F~(1, 2)[x]");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDerivative2() throws Exception {
        Tensor t = parse("F~(1,2)[x_y,y]");
    }

    @Test
    public void testFieldDerivative3() throws Exception {
        SimpleTensor t1 = parseSimple("F~(1,2)_y[x_y,y]");
        SimpleTensor t2 = parseSimple("F~(1,1)_y[x_y,y]");

        Assert.assertTrue(t1.getVarDescriptor() == ((VarDescriptor) t2.getVarDescriptor()));//.getDerivative(0, 1));
    }

    @Test
    public void testFieldDerivative4() throws Exception {
        SimpleTensor t1 = parseSimple("F~(1,2)_y[x_y,y]");
        SimpleTensor t2 = parseSimple("F~(1,2)^w[x_s,y]");

        Assert.assertTrue(t1.getVarDescriptor() == t2.getVarDescriptor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDerivative5() throws Exception {
        SimpleTensor t2 = parseSimple("F~(1,2)^w'[x_s',y]");
    }

    @Test
    public void testFieldDerivative6() throws Exception {
        SimpleTensor t1 = parseSimple("F~(1,2)_y[x_y,y]");
        SimpleTensor t2 = parseSimple("F[x_s,y]");

        Assert.assertTrue(false);
        //Assert.assertTrue(t1.getNameDescriptor() == ((NameDescriptorForTensorField) t2.getNameDescriptor()).getDerivative(1, 2));
    }

    @Test
    public void testFieldDerivative7() throws Exception {
        SimpleTensor t2 = parseSimple("F[x_s,y]");
        SimpleTensor t1 = parseSimple("F~(1,2)_y[x_y,y]");

        Assert.assertTrue(false);
        //Assert.assertTrue(t1.getNameDescriptor() == ((NameDescriptorForTensorField) t2.getNameDescriptor()).getDerivative(1, 2));

        SimpleTensor t3 = parseSimple("F~(1,2)_yz[x_y,y]");
        SimpleTensor t4 = parseSimple("F_k[x_s,y]");

        //Assert.assertTrue(t3.getNameDescriptor() == ((NameDescriptorForTensorField) t4.getNameDescriptor()).getDerivative(1, 2));
        //Assert.assertTrue(t1.getNameDescriptor() != ((NameDescriptorForTensorField) t4.getNameDescriptor()).getDerivative(1, 2));
        //Assert.assertTrue(t3.getNameDescriptor() != ((NameDescriptorForTensorField) t2.getNameDescriptor()).getDerivative(1, 2));
    }

    @Test
    public void testFieldDerivative8() throws Exception {
        SimpleTensor t2 = parseSimple("F^er[x_s,y_Ss]");
        SimpleTensor t1 = parseSimple("F~(1,2)_pqab^qDR[x_y,y_Ss]");

        Assert.assertTrue(false);
        //Assert.assertTrue(t1.getNameDescriptor() == ((NameDescriptorForTensorField) t2.getNameDescriptor()).getDerivative(1, 2));
    }

    @Test
    public void testDerivative() {
        Tensor t;
        t = parse("D[x][x**2]");
        TAssert.assertEquals(t, "2*x");
        t = parse("x*D[x, x][x**2]");
        TAssert.assertEquals(t, "2*x");
        t = parse("D[x, x][x**2]*x");
        TAssert.assertEquals(t, "2*x");

        t = parse("D[x, y][x**2]");
        TAssert.assertEquals(t, "0");

        t = parse("D[x, x][x**2]");
        TAssert.assertEquals(t, "2");

        t = parse("D[x, x][x**2 + (y)]*x + z");
        TAssert.assertEquals(t, "2*x + z");

        t = parse("D[x, (y)][x**2*y + (y)]*f[x] + z");
        TAssert.assertEquals(t, "2*x*f[x] + z");
    }

    @Test
    public void testMetric2() {
        SimpleTensor a = parseSimple("g_mn");
        TensorField b = (TensorField) parse("g_mn[x_m]");
        Assert.assertTrue(a.getName() == b.getHead().getName());
    }

    @Test
    public void testComments1() {
        CC.setDefaultOutputFormat(OutputFormat.Redberry);
        String actual, expected;

        actual = parse(" a + /* xyz */ b").toString();
        expected = parse("a+b").toString();
        Assert.assertEquals(expected, actual);

        actual = parse(" a + /* xyz */ b*c").toString();
        expected = parse("a + b*c").toString();
        Assert.assertEquals(expected, actual);

        actual = parse(" a + /* x*yz */ b*d/c").toString();
        expected = parse("a + b*d/c").toString();
        Assert.assertEquals(expected, actual);

        actual = parse(" a + // /* x*yz */ b*d/c\n + c").toString();
        expected = parse("a + c").toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testGreek1() {
        Tensor t = parse("A_{\\alpha}");
        TAssert.assertEquals(IndicesUtils.getNameWithoutType(t.getIndices().get(0)), 0);
    }

    @Test
    public void testGreek2() {
        Tensor t = parse("A_{\\Xi}");
        TAssert.assertEquals(IndicesUtils.getNameWithoutType(t.getIndices().get(0)), 4);
    }

    @Test
    public void testPreprocessing1() {
        parse(" x:= y");
        TAssert.assertEquals(parse("x**2"), "y**2");
    }

    @Test(expected = BracketsError.class)
    public void testUnbalancedBrackets1() throws Exception {
        parse("(a");
    }

    @Test
    public void testD1() throws Exception {
        parse("x*D[x_c][h_cb[x_a]]*D[x_e][h_e^b[x_a]]");
    }

    @Test
    public void testD2() throws Exception {
        parse("x*D[x_o][h_pq[x_a]]*D[x_z][h_tr[x_a]]");
    }

    @Test
    public void testD3() throws Exception {
        parse("x*D[x_ab, y_cd][h_pq[x_pq, y_cd]*h^pw[y_dc, x_pq]*h_w^q[x_eb, y_eb*x^eb]]" +
                "*D[x^ab, y^cd][h_pq[x_pq, y_cd]*h^pw[y_dc, x_pq]*h_w^q[x_eb, y_eb*x^eb]]");
    }

    @Test
    public void test25() {
        addSymmetry("x_mn", 1, 0);
        parse("D[x_mn][f[x_mn*x^nm]]");
    }

    @Test
    public void testSet1() throws Exception {
        Tensor t = parse("D[x] := x");
        Assert.assertEquals("D[x] = x", t.toString());
    }

    @Test
    public void testUnicode1() throws Exception {
        TAssert.assertEquals(parse("F_\\mu\\nu*(A^\\alpha\\beta + M_\\mu * N^\\mu\\alpha\\beta)"),
                parse("F_μν*(A^αβ + M_μ * N^μαβ)"));
    }

    @Test
    public void testSameVariance1() throws Exception {
        CC.setParserAllowsSameVariance(true);

        TAssert.assertEquals("t_a^a", parse("t_a^a"));
        TAssert.assertEquals("t_a*t^a", parse("t_a*t_a"));
        TAssert.assertEquals("(t_a + b_a)*t^a", parse("(t_a + b_a)*t_a"));
    }

    @Test
    public void testSameVariance2() throws Exception {
        CC.setParserAllowsSameVariance(true);
        parse("A_mn*(B_m^m+C)*U^mn");
    }

    @Test
    public void testSameVariance3() throws Exception {
        CC.setParserAllowsSameVariance(true);
        String expr = "(a + b_m*(k^m + p^m + b_a*(t^am + v^abc*(t^m_bc + v^m_bc))))" +
                "*(a + b_n*(k^n + p^n + b_d*(t^dn + v^def*(t^n_ef + v^n_ef))))" +
                "*(a*(f_qwertyuioplkjhgfdsazxcvbnm^qwertyuioplkjhgfdsazxcvbnm)**2344 + b_i*(k^i + p^i + b_gxy*(t^gixy + v^gqr*(t^xyi_qr + v_qr*(f^xyi*(t_qwtu*o^qwtu)**2 + d^xyi*(t_qwtu*o^qwtu)**2 + x*((t_qwthu*o^qhwtu)**2*d^xyi + k^xyi*(t_qwtus*o^sqwtu)**2))))))";

        expr = expr.replace("^", "_");
        parse(expr);
    }

    @Test
    public void testSameVariance4() throws Exception {
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.JoinAll);
        CC.setParserAllowsSameVariance(true);
        parse("Expand[A_a, A_a]");
        parse("Expand[A_aa + B_aa, A_a]");
    }

    @Test
    public void testSameVariance5() throws Exception {
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.JoinAll);
        CC.setParserAllowsSameVariance(true);
        parse("A_mn*Expand[U_mn]");
        parse("Expand[U_mn]*A_mn");
    }
}
