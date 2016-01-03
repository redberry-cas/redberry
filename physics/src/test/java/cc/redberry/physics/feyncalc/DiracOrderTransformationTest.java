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


import cc.redberry.core.tensor.Tensor;
import junit.framework.Assert;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.ExpandAndEliminateTransformation.expandAndEliminate;

/**
 * Created by poslavsky on 03/08/15.
 */
public class DiracOrderTransformationTest extends AbstractFeynCalcTest {

    @Test
    public void test1() throws Exception {
        Tensor t;
        t = parse("G_a*G_b");
        assertTrue(t == dOrder.transform(t));

        t = parse("G_a*G^a");
        assertTrue(t == dOrder.transform(t));

        t = parse("G_b*G_a");
        assertEquals("2*g_{ba}-G_{a}*G_{b}", dOrder.transform(t));

        t = parse("G_b*G^a");
        assertEquals("2*d_b^a-G^{a}*G_{b}", dOrder.transform(t));

        t = parse("G^b*G_a");
        assertEquals("2*g^b_a-G_{a}*G^{b}", dOrder.transform(t));

        dOrder = new DiracOrderTransformation(new DiracOptions());

        t = parse("G^b*G_a");
        assertEquals("2*g^b_a-G_{a}*G^{b}", dOrder.transform(t));

        t = parse("G_c*G_b*G_a");
        assertEquals("-G_{a}*G_{b}*G_{c}+2*g_{bc}*G_{a}-2*g_{ac}*G_{b}+2*g_{ab}*G_{c}",
                dOrder.transform(t));
    }

    @Test
    public void test2() throws Exception {
        Tensor t;
        t = parse("G_d*G_c*G_b*G_a");
        assertEquals("G_{a}*G_{b}*G_{c}*G_{d}-2*G_{c}*G_{d}*g_{ab}+2*G_{b}*G_{d}*g_{ac}-2*G_{b}*G_{c}*g_{ad}-2*G_{a}*G_{d}*g_{bc}+4*g_{ad}*g_{bc}+2*G_{a}*G_{c}*g_{bd}-4*g_{ac}*g_{bd}-2*G_{a}*G_{b}*g_{cd}+4*g_{ab}*g_{cd}",
                dOrder.transform(t));
    }

    @Test
    public void test3() throws Exception {
        Tensor t;
        t = parse("G_e*G_d*G_c*G_b*G_a");
        assertEquals("G_{a}*G_{b}*G_{c}*G_{d}*G_{e}-2*G_{c}*G_{d}*G_{e}*g_{ab}+2*G_{b}*G_{d}*G_{e}*g_{ac}-2*G_{b}*G_{c}*G_{e}*g_{ad}+2*G_{b}*G_{c}*G_{d}*g_{ae}-2*G_{a}*G_{d}*G_{e}*g_{bc}+2*G_{a}*G_{c}*G_{e}*g_{bd}-2*G_{a}*G_{c}*G_{d}*g_{be}-2*G_{a}*G_{b}*G_{e}*g_{cd}+2*G_{a}*G_{b}*G_{d}*g_{ce}-2*G_{a}*G_{b}*G_{c}*g_{de}+4*g_{be}*g_{cd}*G_{a}-4*g_{bd}*g_{ce}*G_{a}+4*g_{bc}*g_{de}*G_{a}-4*g_{ae}*g_{cd}*G_{b}+4*g_{ad}*g_{ce}*G_{b}-4*g_{ac}*g_{de}*G_{b}+4*g_{ae}*g_{bd}*G_{c}-4*g_{ad}*g_{be}*G_{c}+4*g_{ab}*g_{de}*G_{c}-4*g_{ae}*g_{bc}*G_{d}+4*g_{ac}*g_{be}*G_{d}-4*g_{ab}*g_{ce}*G_{d}+4*g_{ad}*g_{bc}*G_{e}-4*g_{ac}*g_{bd}*G_{e}+4*g_{ab}*g_{cd}*G_{e}",
                dOrder.transform(t));
    }

    @Test
    public void test4() throws Exception {
        Tensor t;
        t = parse("G_b*G_a*A*G_d*G_c");
        assertEquals("-2*G_{a}*G_{b}*A*g_{dc}+4*A*g_{ba}*g_{dc}+G_{a}*G_{b}*A*G_{c}*G_{d}-2*A*G_{c}*G_{d}*g_{ba}",
                dOrder.transform(t));
    }

    @Test
    public void test5() throws Exception {
        Tensor t;
        t = parse("G5*G_d*G_c*G5*G_b*G_a");
        assertEquals("G_{a}*G_{b}*G_{c}*G_{d}-2*G_{c}*G_{d}*g_{ab}+2*G_{b}*G_{d}*g_{ac}-2*G_{b}*G_{c}*g_{ad}-2*G_{a}*G_{d}*g_{bc}+4*g_{ad}*g_{bc}+2*G_{a}*G_{c}*g_{bd}-4*g_{ac}*g_{bd}-2*G_{a}*G_{b}*g_{cd}+4*g_{ab}*g_{cd}",
                dOrder.transform(t));
    }


    @Test
    public void test6() throws Exception {
        Tensor t;
        t = parse("G5*G_d*G_c*G5*G_b*G5*G_a");
        assertEquals(
                expandAndEliminate(parse("-(G_{a}*G_{b}*G_{c}*G_{d}-2*G_{c}*G_{d}*g_{ab}+2*G_{b}*G_{d}*g_{ac}-2*G_{b}*G_{c}*g_{ad}-2*G_{a}*G_{d}*g_{bc}+4*g_{ad}*g_{bc}+2*G_{a}*G_{c}*g_{bd}-4*g_{ac}*g_{bd}-2*G_{a}*G_{b}*g_{cd}+4*g_{ab}*g_{cd})*G5")),
                dOrder.transform(t));
    }

    @Test
    public void test7() throws Exception {
        Tensor t;
        t = parse("G_{c}*G_{a}*G_{b}");
        assertEquals(parse("G_a*G_b*G_c-2*g_bc*G_a+2*g_ac*G_b"),
                dOrder.transform(t));
    }

    @Test
    public void test8() throws Exception {
        Tensor t;
        t = parse("G_a*p^a*G_b");
        assertTrue(t == dOrder.transform(t));

        t = parse("G_a*p^b*G_b");
        assertEquals(parse("2*p_{a}-G_{b}*p^{b}*G_{a}"),
                dOrder.transform(t));

        t = parse("G_a*p^a*G_b*q^b");
        assertTrue(t == dOrder.transform(t));

        t = parse("G_a*q^a*G_b*p^b");
        assertEquals(parse("2*p_a*q^a-G_a*p^a*G_b*q^b"),
                dOrder.transform(t));

        t = parse("G_a*q^a*G_b*p^b*G_c*f^c");
        assertEquals(parse("-f_a*G^a*G^b*p_b*G^c*q_c-2*f^b*G^a*p_a*q_b+2*f^a*G^b*p_a*q_b+2*f_a*G^a*p^b*q_b"),
                dOrder.transform(t));

        t = parse("-f_a*G^a*G^b*p_b*G^c*q_c-2*f^b*G^a*p_a*q_b+2*f^a*G^b*p_a*q_b+2*f_a*G^a*p^b*q_b");
        assertTrue(t == dOrder.transform(t));

        t = parse("G^{d}*q_{d}*G^{g}*f_{g}*G^{c}*p_{c}");
        assertEquals(parse("f_a*G^a*G^b*p_b*G^c*q_c+2*f^b*G^a*p_a*q_b-2*f_a*G^a*p^b*q_b"),
                dOrder.transform(t));

        t = parse("2*k_{z}*k^{z}*p^{y}*q_{y}*G^{d}*q_{d}*G^{g}*f_{g}*G^{c}*p_{c}");
        assertEquals(parse("2*k_{z}*k^{z}*p^{y}*q_{y}*f_a*G^a*G^b*p_b*G^c*q_c+4*k_{z}*k^{z}*p^{y}*q_{y}*f^b*G^a*p_a*q_b-+4*k_{z}*k^{z}*p^{y}*q_{y}*f_a*G^a*p^b*q_b"),
                dOrder.transform(t));
    }

    @Test
    public void test9() throws Exception {
        Tensor t;
        t = parse("cu*G_a*p^a*G_b*u");
        assertTrue(t == dOrder.transform(t));

        t = parse("cu*G_a*p^b*G_b*u");
        assertEquals(parse("2*p_{a}*cu*u-cu*G_{b}*p^{b}*G_{a}*u"),
                dOrder.transform(t));
    }

    @Test
    public void test10() throws Exception {
        Tensor t;
        t = parse("Tr[G_c*G_b*G_a*G5]");
        t = dOrder.transform(t);
        Assert.assertTrue(t == dOrder.transform(t));
    }

    @Test
    public void test11_abcd() throws Exception {
        testFeynCalcData("DiracOrder_abcd");
    }

    @Test
    public void test12_abcd5() throws Exception {
        testFeynCalcData("DiracOrder_abcd5");
    }

    @Test
    public void test13_abcd5tr() throws Exception {
        testFeynCalcData("DiracOrder_abcd5", true);
    }

    void testFeynCalcData(String resource) throws Exception {
        super.testFeynCalcData(dOrder, resource);
    }

    void testFeynCalcData(String resource, boolean doTrace) throws Exception {
        super.testFeynCalcData(dOrder, resource, doTrace, false);
    }
}