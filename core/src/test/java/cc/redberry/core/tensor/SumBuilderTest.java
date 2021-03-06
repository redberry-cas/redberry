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
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseExpression;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumBuilderTest {

    @Test
    public void test1() {
        SumBuilder isb = new SumBuilder();
        isb.put(parse("a"));
        isb.put(parse("2*a"));
        isb.put(parse("-3*a"));
        isb.put(parse("a*b"));
        isb.put(parse("7*a*b"));
        isb.put(parse("Sin[c]"));
        isb.put(parse("d"));
        isb.put(parse("Sin[-c]"));

        Tensor expected = Tensors.parse("8*a*b+d");
        System.out.println(isb.build());
        Assert.assertTrue(TensorUtils.equalsExactly(expected, isb.build()));
    }

    @Test
    public void test2() {
        SumBuilder isb = new SumBuilder();
        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(TensorUtils.equalsExactly(expected, isb.build()));
    }

    @Test
    public void test3() {
        SumBuilder isb = new SumBuilder();
        isb.put(parse("a"));
        isb.put(parse("2*a"));
        isb.put(parse("-3*a"));
        isb.put(parse("0"));
        isb.put(parse("-Power[d,2]"));
        isb.put(parse("Sin[c]"));
        isb.put(parse("Power[d,2]"));
        isb.put(parse("Sin[-c]"));
        isb.put(parse("(1/2)*Cos[-c]"));
        isb.put(parse("(1/2)*Cos[-c]"));

        Tensor expected = Tensors.parse("Cos[c]");
        Assert.assertTrue(IndexMappings.mappingExists(expected, isb.build()));
    }

    @Test
    public void test5() {
        SumBuilder isb = new SumBuilder();
        isb.put(parse("a_mn"));
        isb.put(parse("2*a_mn"));
        isb.put(parse("-3*a_mn"));


        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(IndexMappings.mappingExists(expected, isb.build()));
    }

    @Test
    public void test6() {
        Tensor t = Tensors.parse("0+a_m^m+2*a_m^m-3*a_m^m+3-3+Sin[x]");
        Tensor expected = Tensors.parse("Sin[x]");
        Assert.assertTrue(IndexMappings.mappingExists(expected, t));
    }

    @Test
    public void test7() {
        Tensor t = Tensors.parse("0*(a_m^m+2*a_m^m-3*a_m^m)+3-3+Sin[x]+Sin[-x]");
        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(IndexMappings.mappingExists(expected, t));
    }

    @Test
    public void test8() {
        Tensor t = Tensors.parse("1/(0*(a_m^m+2*a_m^m-3*a_m^m))+3-3+Sin[x]");
        Tensor expected = Tensors.parse("NaN+I*NaN");
        Assert.assertTrue(IndexMappings.mappingExists(expected, t));
    }

    @Test
    public void test9() {
        Tensors.addSymmetry("F_{ab}", IndexType.LatinLower, true, new int[]{1, 0});
        Tensor e = Tensors.parse("F_{mn}*F^{mn}+F_{mn}*F^{nm}");
        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test10() {
        Tensor e = Tensors.parse("2*f_m+a*f_m");
        Tensor expected = Tensors.parse("(2+a)*f_m");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test11() {
        Tensor e = Tensors.parse("0+2*f_m+a*f_m-a*b/b*f_m-f_m");
        Tensor expected = Tensors.parse("f_m");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test12() {
        Tensor e = Tensors.parse("2*(A_M+A_M)+A_M");
        Tensor expected = Tensors.parse("5*A__M");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test13() {
        CC.resetTensorNames(-5181122168523247566L);
        Tensor t = parse("((f_p^a + d_p^a)*d_a^p*T^y_yb + T^a_ab)*(1 + f_c^c)");
        t = EliminateMetricsTransformation.eliminate(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void test14() {
        CC.resetTensorNames(-4602990689951758559L);
        Tensor r = parse("F_b*(f_{g}^{g}*d^{k}_{k}*f_{c}+(1 + d^{j}_{j})*f_{g}^{g}*f_{h}^{h}*f_{c})");
        FromChildToParentIterator it = new FromChildToParentIterator(r);
        Tensor c;
        while ((c = it.next()) != null) {
            if (TensorUtils.equalsExactly(c, parse("d^{k}_{k}")))
                it.set(parse("f_{k}^{k}"));
            if (TensorUtils.equalsExactly(c, parse("d^{j}_{j}")))
                it.set(parse("f_{j}^{j}"));
        }
        r = it.result();
        TAssert.assertIndicesConsistency(r);
        TAssert.assertEquals(r, "(f^{j}_{j}+2)*f_{c}*F_{b}*f_{k}^{k}*f_{g}^{g}");
    }

    @Test
    public void test15() {
        CC.resetTensorNames(-4602990689951758559L);
        Tensor r = parse("F_b*(f_{g}^{g}*d^{k}_{k}*f_{c}+(1 + d^{j}_{j})*f_{g}^{g}*f_{h}^{h}*f_{c})");
        FromChildToParentIterator it = new FromChildToParentIterator(r);
        Tensor c;
        while ((c = it.next()) != null) {
            if (TensorUtils.equalsExactly(c, parse("d^{k}_{k}")))
                it.set(parse("f_{a}^{a}"));
            if (TensorUtils.equalsExactly(c, parse("d^{j}_{j}")))
                it.set(parse("f_{a}^{a}"));
        }
        r = it.result();
        TAssert.assertIndicesConsistency(r);
        TAssert.assertEquals(r, "(f^{j}_{j}+2)*f_{c}*F_{b}*f_{k}^{k}*f_{g}^{g}");
    }

    @Test
    public void test16() {
        Tensor r = parse("f_{g}^{g}*f^{a}_{a}*f_{c}+(1 + f^{a}_{a})*f_{g}^{g}*f_{h}^{h}*f_{c}");
        TAssert.assertIndicesConsistency(r);
    }

    @Test
    public void test17() {
        Tensor r = parse("(A_r*B^rg + C_b*D^bg)*2 + (A_a*B^ag + C_a*D^ag)*(S^br_br + 1)");
        TAssert.assertIndicesConsistency(r);
    }

    @Test
    public void test18() {
        CC.resetTensorNames(-4473598700807087040L);
        Tensor r = parse("(A_a*B^ag + X_a*D^ag)*(S^br_br + 1) + (A_r*B^rg + C_b*D^bg)*2");
        TAssert.assertIndicesConsistency(r);
        r = parseExpression("X_a = C_a").transform(r);
        TAssert.assertIndicesConsistency(r);
    }

    @Test
    public void test19() throws Exception {
        Tensors.setSymmetric("T_abcd", "T_abc");
        Tensor A = parse("T_abmj*T_defi*T^ab_c*T^cdj*T^i_n^m*T^efn");
        Tensor B = parse("T_mnij*T_ebfa*T^mi_c*T^a_d^b*T^dne*T^fcj");
        Tensor C = parse("T_abmj*T_defi*T^ab_c*T^cef*T^i_n^m*T^djn");

        Assert.assertEquals(A.hashCode(), B.hashCode());
        //Assert.assertEquals(A.hashCode(), C.hashCode());
        TAssert.assertNotEquals(A, C);

        SumBuilder sb = new SumBuilder();
        sb.put(parse("2"));
        sb.put(A); sb.put(B); sb.put(C);
        Assert.assertEquals(3, sb.size());
        Assert.assertEquals(2, sb.sizeOfMap());
    }
}
