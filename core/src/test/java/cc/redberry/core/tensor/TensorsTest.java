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
import cc.redberry.core.context.VarIndicesProvider;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorsTest {
    @Test
    public void testProduct0() throws Exception {
        Tensor product1 = parse("-1 * a");
        Tensor product2 = parse("I * b");
        Tensor product3 = parse("I * c");
        Product p = (Product) multiply(product3, product1, product2);

        Complex one = Complex.ONE;
        Complex otherOne1 = Complex.MINUS_ONE.multiply(Complex.MINUS_ONE);
        Complex otherOne = p.getFactor();
        System.out.println(p);
    }

    @Test
    public void testRenameConflicts1() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*K_ij^ij");
        Tensor result = ExpandTransformation.expand(tensor);
        Tensor expected = parse("K_{ij}^{ij}*A_{abk}^{ab}+K_{ij}^{ij}*B_{abk}^{ab}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts2() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ij+T)");
        Tensor result = ExpandTransformation.expand(tensor);
        Tensor expected = parse("T*B_{abk}^{ab}+K_{ij}^{ij}*B_{abk}^{ab}+T*A_{abk}^{ab}+K_{ij}^{ij}*A_{abk}^{ab}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts3() {
        Tensor tensor = parse("(A_ijk^ij+B_ijk^ij)*(K_ij^ijk+T^k)*a_ij");
        Tensor result = ExpandTransformation.expand(tensor);
        Tensor expected = parse("T^{k}*A_{abk}^{ab}*a_{ij}+T^{k}*B_{abk}^{ab}*a_{ij}+K_{cd}^{cdk}*A_{abk}^{ab}*a_{ij}+B_{abk}^{ab}*K_{cd}^{cdk}*a_{ij}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts4() {
        Tensor tensor = parse("(A_ij^ijt+B_ijk^ijt*(H_ij^ijk+L_ij^ijk))*(K_ij^ijp+T^p)*a_ijpt");
        Tensor result = ExpandTransformation.expand(tensor);
        Tensor expected = parse("K_{ab}^{abp}*H_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+K_{ab}^{abp}*A_{ef}^{eft}*a_{ijpt}+K_{ab}^{abp}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+T^{p}*L_{cd}^{cdk}*B_{efk}^{eft}*a_{ijpt}+A_{ef}^{eft}*T^{p}*a_{ijpt}+H_{cd}^{cdk}*T^{p}*B_{efk}^{eft}*a_{ijpt}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts5() {
        Tensor[] tensors = new Tensor[]{parse("A_ij^ijk+B^k"), parse("B_ik^i+N_jk^jl*L_l")};
        Tensor result = multiplyAndRenameConflictingDummies(tensors);
        result = ExpandTransformation.expand(result);
        Tensor expected = parse("B^{k}*N_{bk}^{bl}*L_{l}+A_{ij}^{ijk}*B_{ak}^{a}+A_{ij}^{ijk}*N_{bk}^{bl}*L_{l}+B^{k}*B_{ak}^{a}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testRenameConflicts6() {
        Tensor[] tensors = new Tensor[]{parse("A_ij^ijk"), parse("(B_ik^i+Y_k)"), parse("(C_ijk^ijkl+O^l)")};
        Tensor result = multiplyAndRenameConflictingDummies(tensors);
        result = ExpandTransformation.expand(result);
        Tensor expected = parse("Y_{k}*A_{ij}^{ijk}*C_{abc}^{abcl}+A_{ij}^{ijk}*B_{dk}^{d}*C_{abc}^{abcl}+Y_{k}*A_{ij}^{ijk}*O^{l}+A_{ij}^{ijk}*B_{dk}^{d}*O^{l}");
        junit.framework.Assert.assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void testFieldDerivative() {
        Assert.assertTrue(false);
//        TensorField f = (TensorField) parse("f[x]");
//        TensorField d = Tensors.fieldDerivative(f, IndicesFactory.EMPTY_SIMPLE_INDICES, 0);
//        Tensor e = parse("f~1[x]");
//        TAssert.assertEquals(e, d);
//
//        f = (TensorField) parse("f_mn[x_mn, y_a, x]");
//        d = Tensors.fieldDerivative(f, ParserIndices.parseSimple("^ab"), 0);
//        e = parse("f~(1,0,0)_mn^ab[x_mn, y_a, x]");
//        TAssert.assertEquals(e, d);
//
//        d = Tensors.fieldDerivative(f, ParserIndices.parseSimple("^b"), 1);
//        e = parse("f~(0,1,0)_mn^b[x_mn, y_a, x]");
//        TAssert.assertEquals(e, d);
//
//        d = Tensors.fieldDerivative(f, ParserIndices.parseSimple("^b"), 1);
//        d = Tensors.fieldDerivative(d, ParserIndices.parseSimple("^mn"), 0);
//        e = parse("f~(1,1,0)_mn^mnb[x_mn, y_a, x]");
//        TAssert.assertEquals(e, d);
//
//        d = Tensors.fieldDerivative(f, ParserIndices.parseSimple("^b"), 1);
//        d = Tensors.fieldDerivative(d, ParserIndices.parseSimple("^pq"), 0);
//        e = parse("f~(1,1,0)_mn^pqb[x_mn, y_a, x]");
//        TAssert.assertEquals(e, d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDerivative1() {
        Assert.assertTrue(false);
//        TensorField f = (TensorField) parse("f_mn[x_mn, y_a, x]");
//        Tensors.fieldDerivative(f, ParserIndices.parseSimple("^b"), 2);
    }

    @Test
    public void testFieldDerivative2() {
        Assert.assertTrue(false);

//        TensorField f, d;
//        Tensor e;
//        SimpleIndices[] indices;
//
//        f = (TensorField) parse("f_mn[x_mn, y_a, x]");
//        indices = new SimpleIndices[]{
//                ParserIndices.parseSimple("^pq"),
//                ParserIndices.parseSimple("^b"),
//                IndicesFactory.EMPTY_SIMPLE_INDICES};
//
//        e = parse("f~(1,1,1)_mn^pqb[x_mn, y_a, x]");
//
//        IntPermutationsGenerator gen = new IntPermutationsGenerator(3);
//        for (int[] p : gen) {
//            d = f;
//            for (int k : p)
//                d = Tensors.fieldDerivative(d, indices[k], k);
//            TAssert.assertEquals(e, d);
//        }
//
//        f = (TensorField) parse("f_mn[x_mn, y_a, x_c]");
//        indices = new SimpleIndices[]{
//                ParserIndices.parseSimple("^pq"),
//                ParserIndices.parseSimple("^b"),
//                ParserIndices.parseSimple("^c")};
//
//        e = parse("f~(1,1,1)_mn^pqbc[x_mn, y_a, x_c]");
//
//        gen = new IntPermutationsGenerator(3);
//        for (int[] p : gen) {
//            d = f;
//            for (int k : p)
//                d = Tensors.fieldDerivative(d, indices[k], k);
//            TAssert.assertEquals(e, d);
//        }
//
//        f = (TensorField) parse("f_mn[x_mn, y_a, x_c]");
//        indices = new SimpleIndices[]{
//                ParserIndices.parseSimple("^pqrs"),
//                ParserIndices.parseSimple("^bk"),
//                ParserIndices.parseSimple("^ce")};
//
//        e = parse("f~(2, 2, 2)_{mn}^{ {pq rs} bk ce}[x_mn, y_a, x_c]");
//
//        gen = new IntPermutationsGenerator(3);
//        for (int[] p : gen) {
//            d = f;
//            for (int k : p)
//                d = Tensors.fieldDerivative(d, indices[k], k, 2);
//            TAssert.assertEquals(e, d);
//        }
//
//
//        f = (TensorField) parse("f_mnF[x_mnA, y_aA, x_cA]");
//        indices = new SimpleIndices[]{
//                ParserIndices.parseSimple("^pqArsB"),
//                ParserIndices.parseSimple("^bCkD"),
//                ParserIndices.parseSimple("^cMeN")};
//
//        e = parse("f~(2, 2, 2)_{mnF}^{ {pqA rsB} bCkD cMeN}[x_mnA, y_aA, x_cA]");
//
//        gen = new IntPermutationsGenerator(3);
//        for (int[] p : gen) {
//            d = f;
//            for (int k : p)
//                d = Tensors.fieldDerivative(d, indices[k], k, 2);
//            TAssert.assertEquals(e, d);
//        }
    }

    @Test
    public void testFieldDerivative3() {
        Assert.assertTrue(false);
//
//        SimpleTensor t = parseSimple("field1~(1)[x]");
//
//        TensorField tf = field(t.getName(),
//                IndicesFactory.EMPTY_SIMPLE_INDICES,
//                new SimpleIndices[]{IndicesFactory.EMPTY_SIMPLE_INDICES},
//                new Tensor[]{parse("y")});
//
//        TAssert.assertTrue(tf.isDerivative());
//        TAssert.assertEquals(1, tf.getNameDescriptor().getDerivativeOrder(0));
//        TAssert.assertFalse(t.getName() == parseSimple("field1[x]").getName());
//
//        tf = field(t.getName(),
//                IndicesFactory.EMPTY_SIMPLE_INDICES,
//                new Tensor[]{parse("y")});
//
//        TAssert.assertTrue(tf.isDerivative());
//        TAssert.assertEquals(1, tf.getNameDescriptor().getDerivativeOrder(0));
//        TAssert.assertFalse(t.getName() == parseSimple("field1[x]").getName());
    }

    @Test
    public void testFieldDerivative4() {
        Assert.assertTrue(false);
//
//        SimpleTensor t = parseSimple("field1~(1)_{mn {bc}}[x_bc]");
//
//        TensorField tf = field(t.getName(),
//                ParserIndices.parseSimple("_{ab {pq}}"),
//                new SimpleIndices[]{ParserIndices.parseSimple("_mn")},
//                new Tensor[]{parse("x_mn")});
//
//        TAssert.assertTrue(tf.isDerivative());
//        TAssert.assertEquals(1, tf.getNameDescriptor().getDerivativeOrder(0));
//        TAssert.assertFalse(t.getName() == parseSimple("field1_mn[x_mn]").getName());
//
//        tf = field(t.getName(),
//                ParserIndices.parseSimple("_{ab {pq}}"),
//                new Tensor[]{parse("x_mn")});
//
//        TAssert.assertTrue(tf.isDerivative());
//        TAssert.assertEquals(1, tf.getNameDescriptor().getDerivativeOrder(0));
//        TAssert.assertFalse(t.getName() == parseSimple("field1_mn[x_mn]").getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDerivative5() {
        Assert.assertTrue(false);
//        SimpleTensor t = parseSimple("field1~(1)_{mn {bc}}[x_bc]");
//
//        field(t.getName(),
//                ParserIndices.parseSimple("_{ab {pq} {mn}}"),
//                new SimpleIndices[]{ParserIndices.parseSimple("_mn")},
//                new Tensor[]{parse("x_mn")});

    }

    @Test(expected = IllegalArgumentException.class)
    public void testFieldDerivative6() {
        Assert.assertTrue(false);
//        SimpleTensor t = parseSimple("field1~(1)_{mn {bc}}[x_bc]");
//
//        field(t.getName(),
//                ParserIndices.parseSimple("_{ab {n}}"),
//                new SimpleIndices[]{ParserIndices.parseSimple("_mn")},
//                new Tensor[]{parse("x_mn")});
    }

    @Test
    public void testFieldDerivative7() {
        Assert.assertTrue(false);
//        SimpleTensor t = parseSimple("field1~(1)_{mn {bc}}[x_bc]");
//
//        TensorField tf = field(t.getName(),
//                ParserIndices.parseSimple("_{ab {pq}}"),
//                new SimpleIndices[]{ParserIndices.parseSimple("_mn")},
//                new Tensor[]{parse("x_mn")});
//
//        TensorField dtf = fieldDerivative(tf, ParserIndices.parseSimple("_{a_{1}b_{1} a_{2}b_{2} a_{3}b_{3}}"), 0, 3);
//        TAssert.assertEquals(4, dtf.getNameDescriptor().getDerivativeOrder(0));
//        TAssert.assertEquals(dtf, parse("field1~(4)_{ab { pq a_{1}b_{1} a_{2}b_{2} a_{3}b_{3} }  }[x_bc]"));
//        TAssert.assertEquals(dtf, parse("field1~(4)_{ab { a_{2}b_{2} pq a_{1}b_{1} a_{3}b_{3} } }[x_bc]"));
//        TAssert.assertEquals(dtf, parse("field1~(4)_{ab { a_{2}b_{2} a_{1}b_{1} a_{3}b_{3} pq }  }[x_bc]"));
//        TAssert.assertEquals(dtf, parse("field1~(4)_{ab { a_{2}b_{2} a_{3}b_{3} pq a_{1}b_{1} } }[x_bc]"));
//
//        TAssert.assertNotEquals(dtf, parse("field1~(4)_{ { a_{2}b_{2} ab a_{3}b_{3} pq a_{1}b_{1} } }[x_bc]"));
    }


    @Test
    public void testFieldDerivative8() {
        Assert.assertTrue(false);
//        Tensor a1 = parse("field1~(4)_{ab { pq a_{1}b_{1} a_{2}b_{2} a_{3}b_{3} }  }[x_bc]"),
//                a2 = parse("field1~(4)_{ab { a_{2}b_{2} pq a_{1}b_{1} a_{3}b_{3} } }[x_bc]"),
//                a3 = parse("field1~(4)_{ab { a_{2}b_{2} a_{1}b_{1} a_{3}b_{3} pq }  }[x_bc]"),
//                a4 = parse("field1~(4)_{ab { a_{2}b_{2} a_{3}b_{3} pq a_{1}b_{1} } }[x_bc]");
//
//        Tensor sum = sum(a1, a2, a3, a4);
//        TAssert.assertEquals(sum, multiply(new Complex(4), a1));
//        TAssert.assertEquals(sum, multiply(new Complex(4), parse("field1~(4)_{ab { a_{2}b_{2} a_{3}b_{3} pq a_{1}b_{1} } }[x_bc]")));
//
//        TensorField tdf = fieldDerivative("field1",
//                ParserIndices.parseSimple("_{ab a_{2}b_{2} a_{3}b_{3} pq a_{1}b_{1} }"),
//                new SimpleIndices[]{ParserIndices.parseSimple("_pq")},
//                new Tensor[]{parse("x_pq")},
//                new int[]{4});
//
//        TAssert.assertEquals(sum, multiply(new Complex(4), tdf));
//
//        TensorField a = (TensorField) parse("field1_{ab}[x_bc]");
//        tdf = fieldDerivative(a,
//                ParserIndices.parseSimple("_{a_{2}b_{2} a_{3}b_{3} pq a_{1}b_{1} }"),
//                0, 4);
//        TAssert.assertEquals(sum, multiply(new Complex(4), tdf));
//
//        tdf = fieldDerivative("field1",
//                ParserIndices.parseSimple("_{a_{2}b_{2} a_{3}b_{3} pq a_{1}b_{1} ab}"),
//                new SimpleIndices[]{ParserIndices.parseSimple("_pq")},
//                new Tensor[]{parse("x_pq")},
//                new int[]{4});
//        TAssert.assertNotEquals(sum, multiply(new Complex(4), tdf));
    }

    @Test(timeout = 200L)
    public void testSetSymmetric1() {
        setAntiSymmetric("f_{qwertyuioplkjhgfdsazxcvbnm}");
    }

    @Test(timeout = 200L)
    public void testSetSymmetric2() {
        setSymmetric("f_{qwertyuioplkjhgfdsazxcvbnm}");
    }

    @Test(timeout = 300L)
    public void testSetSymmetric3() {
        setSymmetric("f_{qwerty}");
        TAssert.assertEquals(parse("f_{qwerty}"), "f_{qrtwey}");
    }

    @Test(timeout = 300L)
    public void testSetSymmetric4() {
        setAntiSymmetric("e_abcd");
        Iterator<Permutation> it = parseSimple("e_abcd").getIndices().getSymmetries().getPermutationGroup().iterator();
        while (it.hasNext()) it.next();
    }

    @Test
    public void testSymbolsReferences() {
        Object o = parse("a");
        TAssert.assertTrue(parse("a") == parse("a"));
        TAssert.assertTrue(parse("a") == o);
        TAssert.assertTrue(o == Tensors.simpleTensor("a", IndicesFactory.EMPTY_SIMPLE_INDICES));
        TAssert.assertTrue(o == Tensors.simpleTensor(
                CC.getNameManager().resolve("a", StructureOfIndices.getEmpty()).getName(null, OutputFormat.Redberry),
                IndicesFactory.EMPTY_SIMPLE_INDICES));
        TAssert.assertTrue(o == Tensors.setIndices((SimpleTensor) o, new int[0]));
    }

    @Test
    public void testSF1() {
        Tensor expr = parse("(A_abc - A_bac)*T^c + (A_bac - A_abc)*T^c");
        TAssert.assertEquals(expr, Complex.ZERO);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 100_000)
    public void testRenameConflicts7() throws Exception {
        while (true) {
            CC.reset();
            setAntiSymmetric("e_abcd");
            Tensor tAmps = parse("((q_{k}*q^{k}+pPsi1_{k}*pPsi1^{k}+pEta2_{k}*pEta2^{k}+2*pPsi1^{k}*q_{k}+2*pEta2_{k}*pPsi1^{k}+2*pEta2^{k}*q_{k})**(-1)*(pPsi1_{i}*pPsi1^{i}+pEta2_{i}*pEta2^{i}+pEta2_{i}*pPsi1^{i})**(-1)*(pPsi1_{b}*pPsi1^{b}+pEta2_{b}*pEta2^{b}+2*pEta2_{b}*pPsi1^{b})**(-1)*(q_{h}*q^{h})**(-1)*(pEta1_{g}*pEta1^{g}+pPsi1_{g}*pPsi1^{g}+pEta2_{g}*pEta2^{g}+pEta1_{g}*pEta2^{g}+pEta2^{g}*pPsi1_{g}+pEta1^{g}*pPsi1_{g})**(-1)+(q_{k}*q^{k})**(-1)*(q_{l}*q^{l}+pEta2_{l}*pEta2^{l}-pEta2_{l}*q^{l})**(-1)*(q_{e}*q^{e}+pPsi1_{e}*pPsi1^{e}+pPsi1_{e}*q^{e})**(-1)*(pPsi1_{n}*pPsi1^{n}+pEta2_{n}*pEta2^{n}+pEta2_{n}*pPsi1^{n})**(-1)*(pEta1_{i}*pEta1^{i}+pPsi1_{i}*pPsi1^{i}+pEta2_{i}*pEta2^{i}+pEta1_{i}*pEta2^{i}+pEta2^{i}*pPsi1_{i}+pEta1^{i}*pPsi1_{i})**(-1))*e^{c}_{ma}^{j}*pPsi_{c}*eps^{a}*pEta_{j}*epsPsi^{m}");
            Tensor num = parse("e^{c}_{ma}^{j}*pPsi_{c}*eps^{a}*pEta_{j}*epsPsi^{m}");
            Tensor den = parse("(pPsi2^{j}*pEta1_{j}+pEta1_{j}*pEta1^{j}+pPsi2_{j}*pPsi2^{j})" +
                    "*(pPsi2^{i}*pEta1_{i}+pEta1_{i}*pEta1^{i}+pPsi2_{i}*pPsi2^{i})" +
                    "*(pEta2^{g}*pEta1_{g}+pPsi2_{g}*pEta1^{g}+pEta1_{g}*pEta1^{g}+pEta2_{g}*pEta2^{g}+pEta2^{g}*pPsi2_{g}+pPsi2_{g}*pPsi2^{g})" +
                    "*(pPsi2^{k}*pEta1_{k}+q_{k}*pPsi2^{k}+pEta1_{k}*pEta1^{k}+q_{k}*pEta1^{k}+q_{k}*q^{k}+pPsi2_{k}*pPsi2^{k})" +
                    "*q_{h}*q^{h}");

            Tensor sum = Complex.ZERO;
            sum = sum(sum, tAmps);
            sum = sum(sum, divide(num, den));
        }
    }

    @Test
    public void testRenameConflicts8() throws Exception {
        for (int i = 0; i < 100; ++i) {
            CC.reset();
            setAntiSymmetric("e_abcd");
            Tensor tAmps = parse("((q_{k}*q^{k}+pPsi1_{k}*pPsi1^{k}+pEta2_{k}*pEta2^{k}+2*pPsi1^{k}*q_{k}+2*pEta2_{k}*pPsi1^{k}+2*pEta2^{k}*q_{k})**(-1)*(pPsi1_{i}*pPsi1^{i}+pEta2_{i}*pEta2^{i}+pEta2_{i}*pPsi1^{i})**(-1)*(pPsi1_{b}*pPsi1^{b}+pEta2_{b}*pEta2^{b}+2*pEta2_{b}*pPsi1^{b})**(-1)*(q_{h}*q^{h})**(-1)*(pEta1_{g}*pEta1^{g}+pPsi1_{g}*pPsi1^{g}+pEta2_{g}*pEta2^{g}+pEta1_{g}*pEta2^{g}+pEta2^{g}*pPsi1_{g}+pEta1^{g}*pPsi1_{g})**(-1)+(q_{k}*q^{k})**(-1)*(q_{l}*q^{l}+pEta2_{l}*pEta2^{l}-pEta2_{l}*q^{l})**(-1)*(q_{e}*q^{e}+pPsi1_{e}*pPsi1^{e}+pPsi1_{e}*q^{e})**(-1)*(pPsi1_{n}*pPsi1^{n}+pEta2_{n}*pEta2^{n}+pEta2_{n}*pPsi1^{n})**(-1)*(pEta1_{i}*pEta1^{i}+pPsi1_{i}*pPsi1^{i}+pEta2_{i}*pEta2^{i}+pEta1_{i}*pEta2^{i}+pEta2^{i}*pPsi1_{i}+pEta1^{i}*pPsi1_{i})**(-1))*e^{c}_{ma}^{j}*pPsi_{c}*eps^{a}*pEta_{j}*epsPsi^{m}");
            Tensor num = parse("e^{c}_{ma}^{j}*pPsi_{c}*eps^{a}*pEta_{j}*epsPsi^{m}");
            Tensor den = parse("(pPsi2^{j}*pEta1_{j}+pEta1_{j}*pEta1^{j}+pPsi2_{j}*pPsi2^{j})" +
                    "*(pPsi2^{i}*pEta1_{i}+pEta1_{i}*pEta1^{i}+pPsi2_{i}*pPsi2^{i})" +
                    "*(pEta2^{g}*pEta1_{g}+pPsi2_{g}*pEta1^{g}+pEta1_{g}*pEta1^{g}+pEta2_{g}*pEta2^{g}+pEta2^{g}*pPsi2_{g}+pPsi2_{g}*pPsi2^{g})" +
                    "*(pPsi2^{k}*pEta1_{k}+q_{k}*pPsi2^{k}+pEta1_{k}*pEta1^{k}+q_{k}*pEta1^{k}+q_{k}*q^{k}+pPsi2_{k}*pPsi2^{k})" +
                    "*q_{h}*q^{h}");

            Tensor sum = Complex.ZERO;
            sum = sum(sum, tAmps);
            sum = sum(sum, divideAndRenameConflictingDummies(num, den));
        }
    }

    @Test
    public void testSum1() throws Exception {
        Tensor a = parse("f_i + R_ijk*F^kj - R_kij*F^jk +  R_ijk*F^jk ");
        Tensor b = parse("f_i + R_ijk*F^jk + R_ijk*F^kj - R_kij*F^jk");
        TAssert.assertEquals(a, b);
    }

    @Test
    public void testField1() throws Exception {
        //Expand propagates indices
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.FirstArg);
        Tensor t = parse("f_mn*Expand[f^ma]");
        Assert.assertEquals(t.getIndices(), IndicesFactory.create(ParserIndices.parse("_mn^ma")));
    }

    @Test
    public void testField2() throws Exception {
        //Expand propagates indices
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.AllArgs);
        Tensor t = parse("f_mn*Expand[f^ma, f_ab]");
        Assert.assertEquals(t.getIndices(), IndicesFactory.create(ParserIndices.parse("_mn^ma_ab")));
    }

    @Test
    public void testField3() throws Exception {
        //Expand propagates indices
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.JoinFirst);
        Tensor t = parse("f_mn*Expand[f^ma, f_ab]");
        Assert.assertEquals(t.getIndices(), IndicesFactory.create(ParserIndices.parse("_mn^ma")));
    }

    @Test
    public void testField6() {
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.JoinFirst);
        parse("t_ab*t^ab*Expand[A_ab*A^ab, f_ab*f^ab]");
        //assert rename dummies
    }

    @Test
    public void testField7() {
        CC.getNameManager().resolve("Expand", StructureOfIndices.getEmpty(), VarIndicesProvider.JoinFirst);
        final Tensor t = parse("t_ab*t^ab*Expand[A_ab*A^ab + B_ab*B^ab, f_ab*f^ab]");
        Assert.assertEquals(4, IndicesFactory.create(TensorUtils.getAllIndicesNamesT(t).toArray()).size());
        //assert rename dummies
    }
}
