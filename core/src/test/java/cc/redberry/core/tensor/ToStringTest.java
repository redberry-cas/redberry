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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensor.random.RandomTensor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

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

    @Ignore//random not working with matrices
    @Test
    public void test9Random() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);

        SimpleTensor[] matrices = {
                parseSimple("A^a'_b'"),
                parseSimple("B^a'_b'"),
                parseSimple("C^a'_b'"),
                parseSimple("D^A'_B'"),
                parseSimple("E^A'_B'"),
        };
        for (SimpleTensor st : matrices)
            indicesInsertion.addInsertionRule(st, IndicesUtils.getTypeEnum(st.getIndices().get(0)));

        RandomTensor randomTensor = new RandomTensor();
        randomTensor.clearNamespace();
        randomTensor.reset(123);
        randomTensor.addToNamespace(matrices);
        randomTensor.addToNamespace(parse("F_ab"));
        randomTensor.addToNamespace(parse("J_cd"));
        for (int i = 0; i < 100; ++i)
            assertSimpleRedberryString(
                    randomTensor.nextTensorTree(4, 10, 10, IndicesFactory.EMPTY_INDICES));


    }

    @Test
    public void test10Random() {
        CC.resetTensorNames(123);
        SimpleTensor[] matrices = {
                parseSimple("A_m^a'_b'"),
                parseSimple("B_mn^a'_b'"),
                parseSimple("C_a^a'_b'"),
                parseSimple("D_ab^A'_B'"),
                parseSimple("E_c^A'_B'"),
        };

        SimpleTensor[] matricesSimple = new SimpleTensor[matrices.length];
        for (int i = 0; i < matrices.length; ++i)
            matricesSimple[i] = parseSimple(matrices[i].toString(OutputFormat.SimpleRedberry));
        System.out.println(Arrays.toString(matricesSimple));
        //simple random
        RandomTensor randomTensor = new RandomTensor();
        randomTensor.reset(123);
        randomTensor.clearNamespace();
        randomTensor.addToNamespace(matricesSimple);
        randomTensor.addToNamespace(parse("F_ab"));
        randomTensor.addToNamespace(parse("J_cd"));

        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        for (SimpleTensor st : matrices) {
            indicesInsertion.addInsertionRule(st, extractMatrixType(st));
        }

        //System.out.println(parse("A_m*A_n"));
        for (int i = 0; i < 100; ++i) {
            System.out.println(i);
            Tensor r = randomTensor.nextTensorTree(4, 10, 10, IndicesFactory.EMPTY_INDICES);
            assertSingleType(r, IndexType.LatinLower);
            System.out.println(r);
            assertSimpleRedberryString(parse(r.toString()));
        }

    }

    private static IndexType extractMatrixType(SimpleTensor st) {
        for (IndexType type : IndexType.values())
            if (!CC.isMetric(type.getType()) && st.getIndices().size(type) != 0)
                return type;
        return null;
    }

    private static void assertSimpleRedberryString(Tensor t) {
        TAssert.assertEquals(t, parse(t.toString(OutputFormat.SimpleRedberry)));
    }

    private static void assertSimpleRedberryString(String tensor) {
        Tensor t = parse(tensor);
        TAssert.assertEquals(t, parse(t.toString(OutputFormat.SimpleRedberry)));
    }

    private static void assertSingleType(Tensor t, IndexType type) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            Assert.assertEquals(c.getIndices().size(), c.getIndices().size(type));
    }

}
