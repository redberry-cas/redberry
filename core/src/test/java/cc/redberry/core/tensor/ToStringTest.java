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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.test.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.context.OutputFormat.Redberry;
import static cc.redberry.core.context.OutputFormat.SimpleRedberry;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ToStringTest {
    @Test
    public void test1() {
        Tensor t = parse("T_{\\mu\\nu}");
        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[-\\[Mu],-\\[Nu]]");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[mu,nu]");
    }

    @Test
    public void test2() {
        Tensor t = parse("T_{\\mu_{1} \\nu_{2}} ");
        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[-Subscript[\\[Mu], 1],-Subscript[\\[Nu], 2]]");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[mu1,nu2]");
    }

    @Test
    public void test3() {
        Tensor t = parse("T_{\\mu_{1} \\nu_{2}}^abc_d");
        Assert.assertEquals(t.toString(OutputFormat.Cadabra), "T_{a b c d \\mu_{1} \\nu_{2}}");
    }

    @Test
    public void test4() {
        Tensor t = parse("T^{\\mu\\nu}");
        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[\\[Mu],\\[Nu]]");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[~mu,~nu]");
        Assert.assertEquals(t.toString(OutputFormat.Redberry), "T^{\\mu\\nu}");
    }

    @Test
    public void test5() {
        Tensor t = parse("g_mn");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "g_[m,n]");
        t = parse("d_m^n");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "KroneckerDelta[m,~n]");
    }

    @Test
    public void test6() {
        TensorField t = (TensorField) parse("f~(1)[x]");
        Assert.assertEquals("Derivative[1][f][x]", t.toString(OutputFormat.WolframMathematica));
        t = (TensorField) parse("f~(1,2,0)[x,y,2]");
        Assert.assertEquals("Derivative[1,2,0][f][x,y,2]", t.toString(OutputFormat.WolframMathematica));
    }

    @Test
    public void test7() {
        TensorField t = (TensorField) parse("f~(1)[x]");
        Assert.assertEquals("D[1](f)(x)", t.toString(OutputFormat.Maple));
        t = (TensorField) parse("f~(1,2,0)[x,y,2]");
        Assert.assertEquals("D[1,2,2](f)(x,y,2)", t.toString(OutputFormat.Maple));
    }

    @Test
    public void test8() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);

        indicesInsertion.addInsertionRule(parseSimple("A^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("B^a'_b'"), IndexType.Matrix1);

        assertSimpleRedberryString("A*B");
        assertSimpleRedberryString("c*A*B");
        assertSimpleRedberryString("2*c*A*B");
        assertSimpleRedberryString("c*Tr[A*B]");
        assertSimpleRedberryString("2*c*Tr[A*B]");
    }

    @Test
    public void test9() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G_a^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("U_a^A'_B'"), IndexType.Matrix2);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        Tensor t = parse("G_a*G_b*U_m*U_n");
        Assert.assertEquals("G_{a}*G_{b}*U_{m}*U_{n}", t.toString(SimpleRedberry));
    }

    @Test
    public void test10() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G_a^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("U_a^A'_B'"), IndexType.Matrix2);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        Tensor t = parse("Tr[G_a*G_b*U_m*U_n]");
        Assert.assertFalse(t.toString(OutputFormat.Redberry).contains("Tr"));
        assertSimpleRedberryString("Tr[G_a*G_b*U_m*U_n]");
    }

    @Test
    public void test11() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G_a^a'_b'"), IndexType.Matrix1);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        Tensor t = parse("G_a*G_b + f_ab*d^a'_a'");
        assertSimpleRedberryString("G_a*G_b + f_ab*d^a'_a'");
        assertSimpleRedberryString("Tr[G_a*G_b] + f_ab*d^a'_a'");
    }

    @Test
    public void test12() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G_a^a'_b'"), IndexType.Matrix1);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        assertSimpleRedberryString("G_a*G^a + 1");
        assertSimpleRedberryString("G_a*G^a + f_a^a");
    }

    @Test
    public void test12a() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G_a^a'_b'"), IndexType.Matrix1);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        System.out.println(parse("G_a*G^a + f_a^a").toString(SimpleRedberry));
    }

    @Test
    public void test13() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("Q^A'_B'"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("P^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("L^A'_B'"), IndexType.Matrix2);

        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        assertSimpleRedberryString("(Q+G)*(P+L)");
    }

    @Test
    public void test14() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("G^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("Q^A'_B'"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("P^A'_B'"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("L^A'_B'"), IndexType.Matrix2);

        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        assertSimpleRedberryString("(Q+G)*(P+L)");
    }

    @Test
    public void test15() {
        CC.resetTensorNames(1234);
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("C^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("S^A'_B'"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("Q^A'_B'"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("R^A'_B'"), IndexType.Matrix2);

        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        assertSimpleRedberryString("(S+Q)*(R+C)");
    }

    @Test
    public void test16() {
        CC.resetTensorNames(1234);
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("F^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("K^a'_b'"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("N^A'_B'"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("L^A'_B'"), IndexType.Matrix2);

        CC.current().getParseManager().defaultParserPreprocessors.add(gii);
        System.out.println(parse("Tr[(N+L)*(F+K)]").toString(SimpleRedberry));
        assertSimpleRedberryString("Tr[(N+L)*(F+K)]");
    }

    @Test
    public void test17Random() throws Exception {
        testRandomRedberry(TestUtils.its(100, 1000));
    }

    @Test
    public void test18() throws Exception {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("cu_{a'A'}"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("cu_{a'A'}"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("v^{a'C'}"), IndexType.Matrix1);
        gii.addInsertionRule(parseSimple("v^{a'C'}"), IndexType.Matrix2);
        gii.addInsertionRule(parseSimple("T_{B}^{B'}_{C'}"), IndexType.Matrix2);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);

        Tensor t = parse("cu[p1_m[charm]]*T_A*T_B*v[p2_m[charm]]");//parse("T_{B}^{B'}_{C'}*T_{A}^{A'}_{B'}*cu_{a'A'}[p1_{m}[charm]]*v^{a'C'}[p2_{m}[charm]]");
        System.out.println(t.toString(SimpleRedberry));
    }

    private static void testRandomRedberry(int n) {
        RandomTensor randomTensor = new RandomTensor(false);
        randomTensor.reset();
        randomTensor.addToNamespace(parse("F_mnl"), parse("F_mc"), parse("F_a"), parse("x"), parse("y"));
        SimpleIndices[] indices = {ParserIndices.parseSimple("_abc"), ParserIndices.parseSimple("_ab"), ParserIndices.parseSimple("_a"), IndicesFactory.EMPTY_SIMPLE_INDICES};
        for (SimpleIndices ii : indices) {
            for (int i = 0; i < n; ++i) {
                Tensor tensor = randomTensor.nextTensorTree(3, 3, 3, ii);
                TAssert.assertEquals(tensor, parse(tensor.toString(Redberry)));
            }
        }
    }

    @Test
    public void test9Random() {
        test(System.currentTimeMillis(), 500, 10, 0, 3, 3, 3);
    }

    @Test
    public void test10Random() {
        test(System.currentTimeMillis(), 500, 10, 2, 3, 3, 3);
    }

    public static void test(long seed, int tries, int latinL, int latinU, int depth, int product, int sum) {
        CC.resetTensorNames(seed);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);

        SimpleTensor[] matrices = new SimpleTensor[latinL];
        char start = 'A';
        int i = 0;
        for (; i < matrices.length; ++i) {
            if (start == 'I')
                ++start;
            matrices[i] = parseSimple(String.valueOf(start++) + "^a'_b'");
        }

        matrices = Arrays.copyOf(matrices, latinL + latinU);
        for (; i < matrices.length; ++i) {
            if (start == 'I')
                ++start;
            matrices[i] = parseSimple(String.valueOf(start++) + "^A'_B'");
        }

        SimpleTensor[] matricesSimple = new SimpleTensor[matrices.length];
        for (i = 0; i < matrices.length; ++i)
            matricesSimple[i] = parseSimple(matrices[i].toString(SimpleRedberry));
        for (SimpleTensor st : matrices)
            indicesInsertion.addInsertionRule(st, extractMatrixType(st));


        RandomTensor randomTensor = new RandomTensor(false);
        randomTensor.clearNamespace();
        randomTensor.reset(seed);
        randomTensor.addToNamespace(matricesSimple);
        randomTensor.addToNamespace(parse("F_ab"));
        randomTensor.addToNamespace(parse("J_cd"));

        int k = 0;
        for (i = 0; i < tries; ++i) {
            Tensor tensor = randomTensor.nextTensorTree(depth, product, sum, IndicesFactory.EMPTY_INDICES);
            if (containsPow(tensor))
                continue;
            assertSimpleRedberryString(tensor.toString(SimpleRedberry));
            assertSimpleRedberryString("Tr[" + tensor.toString(SimpleRedberry) + "]");
            ++k;
        }
        System.out.println(k);
    }


    private static boolean containsPow(Tensor t) {
        FromChildToParentIterator it = new FromChildToParentIterator(t);
        while ((t = it.next()) != null)
            if (t.getClass() == Power.class)
                return true;
        return false;
    }

    private static IndexType extractMatrixType(SimpleTensor st) {
        for (IndexType type : IndexType.values())
            if (!CC.isMetric(type.getType()) && st.getIndices().size(type) != 0)
                return type;
        return null;
    }

    private static void assertSimpleRedberryString(String tensor) {
//        System.out.println(tensor);
        Tensor t = parse(tensor);
//        System.out.println(t.toString(Redberry));
//        System.out.println(t.toString(SimpleRedberry));
//        System.out.println(parse(t.toString(SimpleRedberry)));
//        System.out.println();
        TAssert.assertTrue(IndexMappings.anyMappingExists(t, parse(t.toString(Redberry))));
    }

    private static void assertSingleType(Tensor t, IndexType type) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            Assert.assertEquals(c.getIndices().size(), c.getIndices().size(type));
    }

    @Test
    public void test19Metric() throws Exception {
        final SimpleTensor g = parseSimple("g_ab");
        final SimpleTensor d = parseSimple("d_a^b");
        Assert.assertEquals(g.getName(), d.getName());
    }
}
