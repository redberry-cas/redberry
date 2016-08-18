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
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Stanislav Poslavsky
 */
public class DiracSimplify1Test extends AbstractFeynCalcTest {

    @Test
    public void test1() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.Redberry);
        Tensor t = parse("G_a*G_a*G_b*G_c*G_b*G_c");
        assertEquals("-32", dSimplify1.transform(t));
    }

    @Test
    public void test2() throws Exception {
        Tensor t = parse("G_a*G_b*G_a*G_c*G_b*G_c");
        assertEquals("16", dSimplify1.transform(t));
    }

    @Test
    public void test3() throws Exception {
        Tensor t = parse("G_a*G_b*G_c*G_d*G_b*G_a");
        assertEquals("16*g_cd", dSimplify1.transform(t));
    }

    @Test
    public void test1_aabbcc() throws Exception {
        testFeynCalcData("DiracSimplify1_aabbcc");
    }

    @Test
    public void test1_aabbc() throws Exception {
        testFeynCalcData("DiracSimplify1_aabbc");
    }

    @Test
    public void test1_aabbcd() throws Exception {
        testFeynCalcData("DiracSimplify1_aabbcd");
    }

    @Test
    public void test2_aabbcd_tr() throws Exception {
        testFeynCalcData("DiracSimplify1_aabbcd", true, false);
    }

    @Test
    public void test2_aabbcde_g5_tr() throws Exception {
        testFeynCalcData("DiracSimplify1_aabbcde", true, true);
    }

    @Test
    public void test4a() throws Exception {
        Tensor t = parse("G_{a}*G_{b}*G_c*G^{a}");
        assertEquals("2*G_{c}*G_{b}+2*G_{b}*G_{c}", dSimplify1.transform(t));
    }

    @Test
    public void test4() throws Exception {
        TAssert.assertMappingExists("G_{a}*G^{a} = 4", _dSimplify1.createSubstitution(2));
        Tensor t;

        t = parse("G_{a}*G_{b}*G^{a}");
        assertEquals("-2*G_{b}", dSimplify1.transform(t));

        t = parse("G_{a}*G_{b}*G_c*G^{a}");
        assertEquals("2*G_{c}*G_{b}+2*G_{b}*G_{c}", dSimplify1.transform(t));

        t = parse("G_{a}*G_{b}*G_c*G_d*G^{a}");
        assertEquals("-2*G_d*G_c*G_b", dSimplify1.transform(t));

        t = parse("G_{a}*G_{b}*G_c*G_d*G_e*G^{a}");
        assertEquals("2*G_d*G_c*G_b*G_e+2*G_e*G_b*G_c*G_d", dSimplify1.transform(t));

        t = parse("G_{a}*G_{b}*G_c*G_d*G_e*G_f*G^{a}");
        assertEquals("-2*G_f*G_e*G_d*G_c*G_b", dSimplify1.transform(t));

        t = parse("G_{a}*G_{b}*G_c*G_d*G_e*G_f*G_g*G^{a}");
        assertEquals("2*G_f*G_e*G_d*G_c*G_b*G_g+2*G_g*G_b*G_c*G_d*G_e*G_f", dSimplify1.transform(t));
    }

    @Test
    public void test5() throws Exception {
        DiracOptions dOpts = new DiracOptions();
        dOpts.dimension = parse("D");
        dOpts.traceOfOne = Complex.FOUR;
        dSimplify1 = new DiracSimplify1(dOpts);

        Tensor t = parse("G_{o}*G_{a}*G_{b}*G_{c}*G_{d}*G_{e}*G_{o}");
        assertEquals("(-D+4)*G_{a}*G_{b}*G_{c}*G_{d}*G_{e}+2*G_{e}*G_{a}*G_{b}*G_{c}*G_{d}-2*G_{d}*G_{a}*G_{b}*G_{c}*G_{e}-2*G_{c}*G_{b}*G_{a}*G_{d}*G_{e}",
                dSimplify1.transform(t));
    }

    void testFeynCalcData(String resource) throws Exception {
        super.testFeynCalcData(dSimplify1, resource);
    }

    void testFeynCalcData(String resource, boolean doTrace, boolean addG5) throws Exception {
        super.testFeynCalcData(dSimplify1, resource, doTrace, addG5);
    }
}