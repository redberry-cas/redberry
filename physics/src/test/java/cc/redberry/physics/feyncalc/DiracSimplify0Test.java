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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import org.junit.Test;

import static cc.redberry.core.indices.IndexType.Matrix1;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.ExpandAndEliminateTransformation.EXPAND_AND_ELIMINATE;

/**
 * Created by poslavsky on 07/08/15.
 */
public class DiracSimplify0Test extends AbstractFeynCalcTest {

    @Test
    public void test1() throws Exception {
        Tensor t;
        t = parse("G_a*G_b*G_c*k^a*k^b");
        assertEquals("k_a*k^a*G_c", dSimplify0.transform(t));

        t = parse("2*G_a*G_b*G_c*k^a*k^b");
        assertEquals("2*k_a*k^a*G_c", dSimplify0.transform(t));

        t = parse("2*a*G_a*G_b*G_c*k^a*k^b");
        assertEquals("2*a*k_a*k^a*G_c", dSimplify0.transform(t));

        t = parse("2*a*G_c*G_a*G_b*k^a*k^b");
        assertEquals("2*a*k_a*k^a*G_c", dSimplify0.transform(t));

        t = parse("G_a*G_b*k^a*k^b");
        assertEquals("k_a*k^a*d^a'_b'", dSimplify0.transform(t));
    }

    @Test
    public void test2() throws Exception {
        Tensor t;
        t = parse("G_a*G_c*G_b*k^a*k^b");
        assertEquals("2*G_{b}*k^{b}*k_{c}-G_{c}*k^{b}*k_{b}", dSimplify0.transform(t));

        t = parse("G_d*G_a*G_c*G_b*k^a*k^b");
        assertEquals("2*G_d*G_{b}*k^{b}*k_{c}-G_d*G_{c}*k^{b}*k_{b}", dSimplify0.transform(t));

        t = parse("G_d*G_a*G_c*G_b*G_e*k^a*k^b");
        assertEquals("2*G_d*G_{b}*G_e*k^{b}*k_{c}-G_d*G_{c}*G_e*k^{b}*k_{b}", dSimplify0.transform(t));

        t = parse("G_a*G_c*G_b*q^a*k^b");
        TAssert.assertTrue(t == dSimplify0.transform(t));

        t = parse("G_a*G_c*G_b*(k^a + q^a)*(k^b + q^b)");
        TAssert.assertTrue(t == dSimplify0.transform(t));
    }

    @Test
    public void test3() throws Exception {
        Tensor t;
        t = parse("G_a*G^c*G^d*G_b*k^a*k^b");
        assertEquals("2*k^c*G^d*G_i*k^i-2*k^d*G^c*G_i*k^i+k_i*k^i*G^c*G^d", dSimplify0.transform(t));

        t = parse("G_d*G_a*G_c*G_b*k^a*k^b");
        assertEquals("2*G_d*G_{b}*k^{b}*k_{c}-G_d*G_{c}*k^{b}*k_{b}", dSimplify0.transform(t));

        t = parse("G_d*G_a*G_c*G_b*G_e*k^a*k^b");
        assertEquals("2*G_d*G_{b}*G_e*k^{b}*k_{c}-G_d*G_{c}*G_e*k^{b}*k_{b}", dSimplify0.transform(t));

        t = parse("G_a*G_c*G_b*q^a*k^b");
        TAssert.assertTrue(t == dSimplify0.transform(t));

        t = parse("G_a*G_c*G_b*(k^a + q^a)*(k^b + q^b)");
        TAssert.assertTrue(t == dSimplify0.transform(t));
    }

    @Test
    public void test4() throws Exception {
        Tensor t;
        t = parse("G_a*G_b*G_c*G_d*q^a*q^b*k^c*k^d");
        assertEquals("q_a*q^a*k_b*k^b*d^a'_b'", dSimplify0.transform(t));

        t = parse("G_a*G_c*G_b*G_d*q^a*q^b*k^c*k^d");
        assertEquals("2*G_{b}*q^{b}*G_{d}*k^{d}*k^{c}*q_{c}-k^{d}*k_{d}*q^{b}*q_{b}", dSimplify0.transform(t));

        t = parse("G^a*q_a*G^b*k_b*G^c*p_c*G^d*q_d*G^e*k_e*G^f*p_f");
        assertEquals("-4*G^a*q_a*G^b*p_b*k^c*k^d*p_c*q_d+2*G^e*q_e*G^f*k_f*k^h*p_g*p^g*q_h+2*G^j*q_j*G^k*p_k*k_l*k^l*p^m*q_m+2*G^o*k_o*G^p*p_p*k^q*p_q*q_s*q^s-k_t*k^t*p_u*p^u*q_v*q^v", dSimplify0.transform(t));

        t = parse("G^a*p_a*G^b*q_b*G^c*k_c*G^d*q_d*G^e*p_e*G^f*k_f");
        assertEquals("-2*G^a*q_a*G^b*k_b*k^d*p_c*p^c*q_d+4*G^e*p_e*G^f*k_f*k^g*p^h*q_g*q_h-2*G^j*p_j*G^k*k_k*k^l*p_l*q_m*q^m+k_o*k^o*p_p*p^p*q_q*q^q", dSimplify0.transform(t));
    }

    @Test
    public void test5a() throws Exception {
        Tensor t;
        t = parse("G^a*p_a*G^b*q_b*G^c*p_c*G5");
        assertEquals("-G^a*q_a*G5*p_b*p^b+2*G^a*p_a*G5*p^b*q_b", dSimplify0.transform(t));
        t = parse("G^a*p_a*G^b*q_b*G5*G^c*p_c");
        assertEquals("G^a*q_a*G5*p_b*p^b-2*G^a*p_a*G5*p^b*q_b", dSimplify0.transform(t));
        t = parse("G^a*p_a*G^b*p_b*G^c*q_c*G5");
        assertEquals("G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G^a*p_a*G^b*p_b*G5*G^c*q_c");
        assertEquals("-G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G^a*p_a*G5*G^b*q_b*G^c*p_c");
        assertEquals("-G^a*q_a*G5*p_b*p^b+2*G^a*p_a*G5*p^b*q_b", dSimplify0.transform(t));
        t = parse("G^a*p_a*G5*G^b*p_b*G^c*q_c");
        assertEquals("G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G^a*q_a*G^b*p_b*G^c*p_c*G5");
        assertEquals("G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G^a*q_a*G^b*p_b*G5*G^c*p_c");
        assertEquals("-G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G^a*q_a*G5*G^b*p_b*G^c*p_c");
        assertEquals("G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G5*G^a*p_a*G^b*q_b*G^c*p_c");
        assertEquals("G^a*q_a*G5*p_b*p^b-2*G^a*p_a*G5*p^b*q_b", dSimplify0.transform(t));
        t = parse("G5*G^a*p_a*G^b*p_b*G^c*q_c");
        assertEquals("-G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
        t = parse("G5*G^a*q_a*G^b*p_b*G^c*p_c");
        assertEquals("-G^a*q_a*G5*p_b*p^b", dSimplify0.transform(t));
    }

    @Test
    public void test5() throws Exception {
        testFeynCalcData("DiracSimplify0_ppkkqq");
    }

    @Test
    public void test6() throws Exception {
        testFeynCalcData("DiracSimplify0_ppkqf");
    }

    @Test
    public void test7() throws Exception {
        testFeynCalcData("DiracSimplify0_ppqqkf");
    }

    @Test
    public void test567_trace() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.Redberry);
        testFeynCalcData("DiracSimplify0_ppkkqq", true, false);
        testFeynCalcData("DiracSimplify0_ppkqf", true, false);
        testFeynCalcData("DiracSimplify0_ppqqkf", true, false);
    }

    @Test
    public void test567_g5() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.Redberry);
        testFeynCalcData("DiracSimplify0_ppkkqq", false, true);
        testFeynCalcData("DiracSimplify0_ppkqf", false, true);
        testFeynCalcData("DiracSimplify0_ppqqkf", false, true);
    }

    @Test
    public void test567_g5_trace() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.Redberry);
        testFeynCalcData("DiracSimplify0_ppkkqq", true, true);
        testFeynCalcData("DiracSimplify0_ppqqkf", true, true);
    }

    @Test
    public void test6b() throws Exception {
        Transformation tr = new TransformationCollection(dSimplify0, dOrder);
        Tensor t;
        t = parse("G^a*p_a*G^b*q_b*G^c*p_c*G^d*q_d*G^e*k_e*G^f*k_f*f_g*G^g");
        TAssert.assertEquals("4*f^c*G^b*k_a*k^a*p_b*p^d*q_c*q_d-4*f^b*G^c*k_a*k^a*p_b*p^d*q_c*q_d+2*f_a*G^a*G^b*p_b*G^c*q_c*k_d*k^d*p^e*q_e-f_a*G^a*k_b*k^b*p_c*p^c*q_d*q^d",
                tr.transform(t));
    }

    @Test
    public void test8() throws Exception {
        Tensor t;
        t = parse("G^a*p_a*G^b*p_b*G_c*G5*p^c");
        TAssert.assertEquals("p_a*p^a*G_c*G5*p^c", dSimplify0.transform(t));
    }

    void testFeynCalcData(String resource) throws Exception {
        super.testFeynCalcData(dSimplify0, resource);
    }

    void testFeynCalcData(String resource, boolean doTrace, boolean addG5) throws Exception {
        super.testFeynCalcData(dSimplify0, resource, doTrace, addG5);
    }
}