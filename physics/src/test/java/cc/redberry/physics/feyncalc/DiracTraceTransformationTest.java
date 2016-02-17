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
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.test.LongTest;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.transformations.ExpandAndEliminateTransformation.expandAndEliminate;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DiracTraceTransformationTest extends AbstractFeynCalcTest {

    @Test
    public void test1() throws Exception {
        for (int i = 0; i < 100; ++i) {
            setUp();
            Tensor t = parse("Tr[G_a*G_b*G_c*G_d]");
            t = dTrace.transform(t);
            Tensor expected = parse("-4*g_{ac}*g_{bd}+4*g_{ad}*g_{bc}+4*g_{ab}*g_{cd}");
            TAssert.assertEquals(t, expected);
        }
    }

    @Test
    public void test2() throws Exception {
        for (int i = 0; i < 100; ++i) {
            setUp();
            Tensor t = parse("Tr[G^a*G^b*G^c*G^d*G^e*G^f]");
            t = dTrace.transform(t);
            Tensor expected = parse("4*g^{af}*g^{be}*g^{cd}-4*g^{ae}*g^{bf}*g^{cd}+4*g^{ab}*g^{cd}*g^{ef}-4*g^{af}*g^{bd}*g^{ce}+4*g^{ad}*g^{bf}*g^{ce}+4*g^{ae}*g^{bd}*g^{cf}-4*g^{ad}*g^{be}*g^{cf}+4*g^{af}*g^{bc}*g^{de}-4*g^{ac}*g^{bf}*g^{de}+4*g^{ab}*g^{cf}*g^{de}-4*g^{ae}*g^{bc}*g^{df}+4*g^{ac}*g^{be}*g^{df}-4*g^{ab}*g^{ce}*g^{df}+4*g^{ad}*g^{bc}*g^{ef}-4*g^{ac}*g^{bd}*g^{ef}");
            TAssert.assertEquals(t, expected);
        }
    }

    @Test
    public void test4() {
        Tensor t = parse("Tr[G^a*G^b*G^c*G^d*G^e*G^f*G^g*G^h]");
        t = dTrace.transform(t);
        TAssert.assertEquals(t, "-4*g^{eh}*g^{ac}*g^{fg}*g^{bd}+4*g^{ad}*g^{gh}*g^{ef}*g^{bc}+4*g^{bh}*g^{af}*g^{cd}*g^{eg}+4*g^{eh}*g^{ab}*g^{cg}*g^{df}-4*g^{dh}*g^{ag}*g^{ce}*g^{bf}+4*g^{ah}*g^{dg}*g^{ce}*g^{bf}-4*g^{af}*g^{dh}*g^{cg}*g^{be}-4*g^{af}*g^{gh}*g^{bd}*g^{ce}+4*g^{ab}*g^{gh}*g^{cf}*g^{de}-4*g^{ah}*g^{cg}*g^{de}*g^{bf}-4*g^{ch}*g^{af}*g^{bg}*g^{de}+4*g^{fg}*g^{bc}*g^{ah}*g^{de}-4*g^{ag}*g^{eh}*g^{bd}*g^{cf}-4*g^{ah}*g^{df}*g^{bg}*g^{ce}-4*g^{bh}*g^{ad}*g^{eg}*g^{cf}-4*g^{ag}*g^{fh}*g^{cd}*g^{be}-4*g^{dh}*g^{ab}*g^{fg}*g^{ce}+4*g^{cd}*g^{ef}*g^{ah}*g^{bg}-4*g^{af}*g^{eh}*g^{cd}*g^{bg}-4*g^{fh}*g^{ad}*g^{bc}*g^{eg}-4*g^{dh}*g^{ag}*g^{ef}*g^{bc}+4*g^{ef}*g^{bc}*g^{ah}*g^{dg}-4*g^{ch}*g^{ab}*g^{eg}*g^{df}-4*g^{af}*g^{eh}*g^{bc}*g^{dg}-4*g^{ae}*g^{fh}*g^{bd}*g^{cg}-4*g^{ae}*g^{ch}*g^{dg}*g^{bf}+4*g^{eh}*g^{ad}*g^{cf}*g^{bg}+4*g^{ae}*g^{ch}*g^{df}*g^{bg}-4*g^{dh}*g^{ab}*g^{ef}*g^{cg}-4*g^{cd}*g^{eg}*g^{ah}*g^{bf}+4*g^{af}*g^{dh}*g^{bg}*g^{ce}-4*g^{ch}*g^{ad}*g^{fg}*g^{be}+4*g^{bh}*g^{ad}*g^{fg}*g^{ce}-4*g^{bh}*g^{ac}*g^{fg}*g^{de}-4*g^{bc}*g^{eg}*g^{ah}*g^{df}-4*g^{bh}*g^{ag}*g^{cf}*g^{de}-4*g^{bh}*g^{ac}*g^{ef}*g^{dg}+4*g^{af}*g^{gh}*g^{cd}*g^{be}-4*g^{ag}*g^{fh}*g^{bc}*g^{de}+4*g^{bh}*g^{ad}*g^{ef}*g^{cg}+4*g^{ae}*g^{dh}*g^{cg}*g^{bf}-4*g^{ae}*g^{dh}*g^{fg}*g^{bc}+4*g^{ab}*g^{fh}*g^{dg}*g^{ce}+4*g^{bd}*g^{eg}*g^{cf}*g^{ah}-4*g^{ad}*g^{gh}*g^{cf}*g^{be}-4*g^{ab}*g^{fh}*g^{cg}*g^{de}+4*g^{dh}*g^{ac}*g^{fg}*g^{be}+4*g^{ch}*g^{ag}*g^{bd}*g^{ef}-4*g^{ab}*g^{gh}*g^{df}*g^{ce}+4*g^{af}*g^{dh}*g^{bc}*g^{eg}+4*g^{bh}*g^{ac}*g^{eg}*g^{df}+4*g^{ab}*g^{gh}*g^{cd}*g^{ef}+4*g^{dh}*g^{ag}*g^{cf}*g^{be}-4*g^{cf}*g^{ah}*g^{dg}*g^{be}+4*g^{eh}*g^{ac}*g^{dg}*g^{bf}-4*g^{ac}*g^{gh}*g^{bd}*g^{ef}-4*g^{ch}*g^{ad}*g^{ef}*g^{bg}-4*g^{eh}*g^{ad}*g^{cg}*g^{bf}+4*g^{eh}*g^{ad}*g^{fg}*g^{bc}-4*g^{eh}*g^{ac}*g^{df}*g^{bg}-4*g^{fg}*g^{bd}*g^{ah}*g^{ce}-4*g^{ab}*g^{fh}*g^{cd}*g^{eg}+4*g^{af}*g^{gh}*g^{bc}*g^{de}+4*g^{ae}*g^{bh}*g^{cf}*g^{dg}+4*g^{fh}*g^{ac}*g^{bd}*g^{eg}+4*g^{ae}*g^{fh}*g^{cd}*g^{bg}+4*g^{ae}*g^{fh}*g^{bc}*g^{dg}-4*g^{ae}*g^{gh}*g^{cd}*g^{bf}-4*g^{eh}*g^{ab}*g^{cf}*g^{dg}-4*g^{bd}*g^{ef}*g^{ah}*g^{cg}+4*g^{ch}*g^{ad}*g^{eg}*g^{bf}+4*g^{af}*g^{eh}*g^{bd}*g^{cg}+4*g^{ae}*g^{ch}*g^{fg}*g^{bd}-4*g^{ch}*g^{ag}*g^{be}*g^{df}+4*g^{bh}*g^{ag}*g^{df}*g^{ce}-4*g^{ae}*g^{gh}*g^{bc}*g^{df}+4*g^{cf}*g^{ah}*g^{bg}*g^{de}-4*g^{fh}*g^{ac}*g^{dg}*g^{be}+4*g^{dh}*g^{ac}*g^{ef}*g^{bg}-4*g^{bh}*g^{ag}*g^{cd}*g^{ef}+4*g^{fh}*g^{ad}*g^{cg}*g^{be}-4*g^{ch}*g^{af}*g^{bd}*g^{eg}+4*g^{ac}*g^{gh}*g^{be}*g^{df}+4*g^{ae}*g^{gh}*g^{bd}*g^{cf}-4*g^{dh}*g^{ac}*g^{eg}*g^{bf}+4*g^{ag}*g^{fh}*g^{bd}*g^{ce}+4*g^{fg}*g^{cd}*g^{ah}*g^{be}+4*g^{ch}*g^{af}*g^{dg}*g^{be}-4*g^{bh}*g^{af}*g^{dg}*g^{ce}+4*g^{ch}*g^{ag}*g^{de}*g^{bf}+4*g^{ah}*g^{cg}*g^{be}*g^{df}+4*g^{bh}*g^{af}*g^{cg}*g^{de}+4*g^{ag}*g^{eh}*g^{cd}*g^{bf}-4*g^{fh}*g^{ad}*g^{bg}*g^{ce}+4*g^{fh}*g^{ac}*g^{bg}*g^{de}+4*g^{ch}*g^{ab}*g^{fg}*g^{de}+4*g^{dh}*g^{ab}*g^{eg}*g^{cf}-4*g^{ae}*g^{bh}*g^{fg}*g^{cd}+4*g^{ad}*g^{gh}*g^{ce}*g^{bf}-4*g^{ac}*g^{gh}*g^{de}*g^{bf}+4*g^{ag}*g^{eh}*g^{bc}*g^{df}+4*g^{eh}*g^{ab}*g^{fg}*g^{cd}-4*g^{ae}*g^{dh}*g^{cf}*g^{bg}+4*g^{ch}*g^{ab}*g^{ef}*g^{dg}-4*g^{ae}*g^{bh}*g^{cg}*g^{df}");
    }

    @Test
    public void test5() {
        Tensor t = parse("Tr[G^a*G^b*G^c*G^d*G^e*G^f*G^g*G^h*G^i*G^j]");
        t = dTrace.transform(t);
        Assert.assertEquals(t.size(), 945);
    }

    @Test
    public void test6() {
        Tensor t = parse("Tr[G_a*G_b*G^b*G_d] + Tr[G_a*G_d]");
        t = dTrace.transform(t);
        t = parseExpression("d^a_a = 4").transform(t);
        Tensor expected = parse("20*g_ad");
        TAssert.assertEquals(t, expected);
    }

    @Test
    public void test7() {
        Tensor t = parse("(4*M**(-4)*N*m+M**(-3)*N+(1/8)*((1/16)*M**4+m**4+(1/2)*M**2*m**2)**(-1)*M*N)*G^{ge'}_{a'}*G_{a}^{b'}_{d'}*G_{c}^{d'}_{e'}*G^{fh'}_{i'}*G_{m}^{i'}_{b'}*G_{n}^{a'}_{h'}*k2_{f}*k2^{a}*k2^{c}*k2_{g}*g_{AB}*k1^{m}");
        t = dTrace.transform(t);
        assertContainsGamma(t);
    }

    @Test
    public void test8() {
        Tensor t = parse("g_{AB}*d^{A'}_{A'}*k2_{b}*k2_{f}*P^{b}*P^{a}*P_{g}*P_{d}*G^{fh'}_{i'}*G^{db'}_{c'}*G_{a}^{c'}_{d'}*G^{ge'}_{a'}*G_{m}^{i'}_{b'}*G_{n}^{a'}_{h'}*d^{d'}_{e'}");
        t = dTrace.transform(t);
        assertContainsGamma(t);
    }

    @Test
    public void test9() {
        Tensor t;
        t = parse("Tr[p2_a*G^a*e2_b*G^b*p1_c*G^c*e1_d*G^d*p1_e*G^e*e2_f*G^f*p1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(trEE(t), "-32*p1_{g}*p1^{g}*p1^{a}*p2_{a}");
        t = parse("Tr[p2_a*G^a*e2_b*G^b*p1_c*G^c*e1_d*G^d*p1_e*G^e*e2_f*G^f*k1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(trEE(t), "-32*p1_{g}*k1^{g}*p1^{a}*p2_{a}");
        t = parse("Tr[p2_a*G^a*e2_b*G^b*k1_c*G^c*e1_d*G^d*p1_e*G^e*e2_f*G^f*p1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(trEE(t), "-32*p1_{g}*k1^{g}*p1^{a}*p2_{a}");
        t = parse("Tr[p2_a*G^a*e2_b*G^b*k1_c*G^c*e1_d*G^d*p1_e*G^e*e2_f*G^f*k1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(parseExpression("k1_a*k1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e2_b*G^b*k1_c*G^c*e1_d*G^d*p1_e*G^e*e1_f*G^f*k2_g*G^g*e2_h*G^h]");
        TAssert.assertEquals(trEE(t), trEE("4*Tr[p2_a*G^a*k2_b*G^b*p1_c*G^c*k1_d*G^d]"));
        t = parse("Tr[p2_a*G^a*e2_b*G^b*p1_c*G^c*e1_d*G^d*p1_e*G^e*e1_f*G^f*k2_g*G^g*e2_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e2_b*G^b*k1_c*G^c*e1_d*G^d*p1_e*G^e*e1_f*G^f*p1_g*G^g*e2_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e1_b*G^b*p1_c*G^c*e2_d*G^d*p1_e*G^e*e2_f*G^f*p1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e1_b*G^b*p1_c*G^c*e2_d*G^d*p1_e*G^e*e2_f*G^f*k1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e1_b*G^b*k2_c*G^c*e2_d*G^d*p1_e*G^e*e2_f*G^f*p1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e1_b*G^b*k2_c*G^c*e2_d*G^d*p1_e*G^e*e2_f*G^f*k1_g*G^g*e1_h*G^h]");
        TAssert.assertEquals(trEE(t), trEE("4*Tr[p2_a*G^a*k2_b*G^b*p1_c*G^c*k1_d*G^d]"));
        t = parse("Tr[p2_a*G^a*e1_b*G^b*p1_c*G^c*e2_d*G^d*p1_e*G^e*e1_f*G^f*p1_g*G^g*e2_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "0");
        t = parse("Tr[p2_a*G^a*e1_b*G^b*p1_c*G^c*e2_d*G^d*p1_e*G^e*e1_f*G^f*k2_g*G^g*e2_h*G^h]");
        TAssert.assertEquals(parseExpression("p1_a*p1^a = 0").transform(trEE(t)), "-32*p1_{g}*k2^{g}*p1^{a}*p2_{a}");
        t = parse("Tr[p2_a*G^a*e1_b*G^b*k2_c*G^c*e2_d*G^d*p1_e*G^e*e1_f*G^f*k2_g*G^g*e2_h*G^h]");
        TAssert.assertEquals(parseExpression("k2_a*k2^a = 0").transform(trEE(t)), "0");
    }

    @Test
    public void test9a() {
        Tensor t;
        t = parse("4*Tr[p2_a*G^a*k2_b*G^b*p1_c*G^c*k1_d*G^d]");
        TAssert.assertEquals(trEE(t), "16*k2_{f}*k1^{a}*p1^{f}*p2_{a}+16*k2^{a}*k1^{e}*p1_{e}*p2_{a}-16*k2^{c}*k1_{c}*p1^{a}*p2_{a}");
    }

    private Tensor trEE(String t) {
        return trEE(parse(t));
    }

    private Tensor trEE(Tensor t) {
        t = dTrace.transform(t);
        t = parseExpression("e1_m*e1_n = g_mn").transform(t);
        t = parseExpression("e2_m*e2_n = g_mn").transform(t);
        t = EliminateMetricsTransformation.eliminate(t);
        t = parseExpression("d^m_m = 4").transform(t);
        return t;
    }

    @Test
    public void test10() {
        Tensor t;
        t = parse("Tr[G_a*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[G_a*G_b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[G_a*G_b*G_c*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G_h*G_f*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
    }

    @Test
    public void test11() throws Exception {
        for (int i = 0; i < 50; ++i) {
            setUp();
            Tensor t;
            t = parse("Tr[G_a*G_b*G_c*G_d*G5]");
            TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
            t = parse("Tr[G5*G_a*G_b*G5*G_c*G_d*G5]");
            TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
            t = parse("Tr[G5*G_a*G_b*G5*G_c*G_d*G5*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
            t = parse("Tr[G5*G_a*G5*G5*G_b*G5*G_c*G5*G5*G_d*G5*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
            t = parse("Tr[G5*G5*G_a*G5*G5*G_b*G5*G_c*G5*G5*G_d*G5*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), dTrace.transform(parse("Tr[G_a*G_b*G_c*G_d]")));
            t = parse("a*Tr[G5*G5*G_a*G5*G5*G_b*G5*G_c*G5*G5*b*G_d*G5*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), dTrace.transform(parse("a*b*Tr[G_a*G_b*G_c*G_d]")));
            t = parse("Tr[G5*G5*G5*G5*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), "4");
            t = parse("Tr[G5*G5*G5*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
        }
    }

    @Test
    public void test11a() {
        Tensor t;
        t = parse("Tr[G5*G5*G5*G5*G5*G5]");
        TAssert.assertEquals(dTrace.transform(t), "4");
    }

    @Test
    public void test11b() throws Exception {
        setUp(-2969995398048215680L);
        Tensor t;
        t = parse("Tr[G5*G_a*G5*G5*G_b*G5*G_c*G5*G5*G_d*G5*G5*G5]");
        TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
        t = parse("Tr[G5*G_a*G_b*G5*G_c*G_d*G5*G5*G5]");
        TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
    }

    @Test
    public void test11c() throws Exception {
        setUp(123);
        Tensor t;
        t = parse("Tr[G5*G_a*G_b*G5*G_c*G_d*G5]");
        System.out.println(dTrace.transform(t));
        TAssert.assertEquals(dTrace.transform(t), "(-4*I)*e_{abcd}");
    }

    @Test
    public void test12() throws Exception {
        for (int i = 0; i < 50; ++i) {
            setUp();

            Tensor t;
            t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^d*G5]");
            TAssert.assertEquals(dTrace.transform(t), "8*I*e_abce");
            t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^a*G5]");
            TAssert.assertEquals(dTrace.transform(t), "16*I*e_bcde");
            t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-8*I*e_acde");
            t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^c*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^e*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-16*I*e_abcd");
            t = parse("-Tr[G5*G_a*G5*G_b*G_c*G_d*G_e*G^d*G5]");
            TAssert.assertEquals(dTrace.transform(t), "8*I*e_abce");
            t = parse("Tr[G_a*G5*G_b*G_c*G5*G5*G5*G_d*G_e*G^a*G5]");
            TAssert.assertEquals(dTrace.transform(t), "16*I*e_bcde");
            t = parse("Tr[G_a*G5*G_b*G_c*G_d*G5*G_e*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "8*I*e_acde");
            t = parse("Tr[-G5*G_a*G_b*G_c*G5*G_d*G_e*G^c*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("-Tr[G5*G_a*G5*G_b*G5*G_c*G_d*G_e*G^e*G5*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-16*I*e_abcd");
        }
    }

    @Test
    public void test13() throws Exception {
        for (int i = 0; i < 5; ++i) {
            setUp();
            Tensor t, expected;

            //todo apply Shouten identity
            //t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G_f*G_g*G^a*G5]");
            //expected = parse("16*I*g_{bf}*e_{cdeg}-16*I*g_{bg}*e_{cdef}+16*I*g_{cd}*e_{befg}-16*I*g_{ce}*e_{bdfg}+16*I*g_{de}*e_{bcfg}+16*I*g_{fg}*e_{bcde}");
            //TAssert.assertEquals(dTrace.transform(t), expected);
            t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^d*G_g*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "(16*I)*e_{aceg}");
            t = parse("Tr[(g_ab*G_c-g_ac*G_b+g_bc*G_a-I*e_abcd*G5*G^d)*G_d*(d_e^d*G_g-g_eg*G^d+d_g^d*G_e-I*e_e^d_gf*G5*G^f)*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "(16*I)*e_{aceg}");

            t = parse("Tr[G_c*G_e*G_g*G_a*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-4*I*e_cega");
            t = parse("Tr[-g_eg*G_c*G_d*G^d*G_b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[g_ab*G_c*G_d*d_g^d*G_e*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-4*I*e_cgea");

            t = parse("Tr[-I*g_ab*G_c*G_d*e_e^d_gf*G5*G^f*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-8*I*e_ecga");
            t = parse("Tr[I*g_ac*G_b*G_d*e_e^d_gf*G5*G^f*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[-I*g_bc*G_a*G_d*e_e^d_gf*G5*G^f*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-8*I*e_eagc");
            t = parse("Tr[(-I)*(-I)*e_abcj*G5*G^j*G^d*e_edgf*G5*G^f*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), simplifyLeviCivita.transform(parse("4*I*e_abcj*e_edgf*e^jdfb")));
            t = parse("Tr[-I*e_abcj*G5*G^j*G_d*d_e^d*G_g*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-8*I*e_agce");
            t = parse("Tr[-I*e_abcj*G5*G^j*G_d*g_eg*G^d*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[-I*e_abcj*G5*G^j*G_d*d^d_g*G_e*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-8*I*e_aecg");
            t = parse("Tr[-g_ac*G_b*G_d*g_eg*G^d*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[-g_ac*G_b*G_d*g^d_g*G_e*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[g_bc*G_a*G_d*g^d_e*G_g*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-4*I*e_aegc");
            t = parse("Tr[-g_bc*G_a*G_d*g_eg*G^d*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "0");
            t = parse("Tr[g_bc*G_a*G_d*g^d_g*G_e*G^b*G5]");
            TAssert.assertEquals(dTrace.transform(t), "-4*I*e_agec");
        }
    }

    @Test
    public void test13a() throws Exception {
        setUp(8996284584077168957L);

        Tensor t;

        t = parse("Tr[G_a*G_b*G_c*G_d*G_e*G^d*G_g*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "(16*I)*e_{aceg}");
        t = parse("Tr[(g_ab*G_c-g_ac*G_b+g_bc*G_a-I*e_abcd*G5*G^d)*G_d*(d_e^d*G_g-g_eg*G^d+d_g^d*G_e-I*e_e^d_gf*G5*G^f)*G^b*G5]");
        TAssert.assertEquals(simplifyLeviCivita.transform(dTrace.transform(t)), "(16*I)*e_{aceg}");

        t = parse("Tr[G_c*G_e*G_g*G_a*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-4*I*e_cega");
        t = parse("Tr[-g_eg*G_c*G_d*G^d*G_b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[g_ab*G_c*G_d*d_g^d*G_e*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-4*I*e_cgea");

        t = parse("Tr[-I*g_ab*G_c*G_d*e_e^d_gf*G5*G^f*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-8*I*e_ecga");
        t = parse("Tr[I*g_ac*G_b*G_d*e_e^d_gf*G5*G^f*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[-I*g_bc*G_a*G_d*e_e^d_gf*G5*G^f*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-8*I*e_eagc");
        t = parse("Tr[(-I)*(-I)*e_abcj*G5*G^j*G^d*e_edgf*G5*G^f*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), simplifyLeviCivita.transform(parse("4*I*e_abcj*e_edgf*e^jdfb")));
        t = parse("Tr[-I*e_abcj*G5*G^j*G_d*d_e^d*G_g*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-8*I*e_agce");
        t = parse("Tr[-I*e_abcj*G5*G^j*G_d*g_eg*G^d*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[-I*e_abcj*G5*G^j*G_d*d^d_g*G_e*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-8*I*e_aecg");
        t = parse("Tr[-g_ac*G_b*G_d*g_eg*G^d*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[-g_ac*G_b*G_d*g^d_g*G_e*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[g_bc*G_a*G_d*g^d_e*G_g*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-4*I*e_aegc");
        t = parse("Tr[-g_bc*G_a*G_d*g_eg*G^d*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "0");
        t = parse("Tr[g_bc*G_a*G_d*g^d_g*G_e*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), "-4*I*e_agec");
    }

    @Test
    public void test13b() throws Exception {
        setUp(8996284584077168957L);

        Tensor t;

        t = parse("Tr[(-I)*(-I)*e_abcj*G5*G^j*G^d*e_edgf*G5*G^f*G^b*G5]");
        TAssert.assertEquals(dTrace.transform(t), simplifyLeviCivita.transform(parse("4*I*e_abcj*e_edgf*e^jdfb")));
    }

    //Expression schouten = parseExpression("g_fa*e_bcde = -(g_fb*e_cdea + g_fc*e_deab+g_fd*e_eabc+g_fe*e_abcd)");
    private static final SimpleTensor defaultGamma = parseSimple("G^a'_b'a");

    private static void assertContainsGamma(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null) {
            if (c instanceof SimpleTensor && ((SimpleTensor) c).getName() == defaultGamma.getName())
                throw new AssertionError();
        }
    }


    @Test
    public void test14() {
        Tensor t;
        t = parse("Tr[(G_a*p^a + m)*G_b + (G_a*k^a + m)*G_b]");
        TAssert.assertEquals(dTrace.transform(t), "4*p_{b}+4*k_{b}");

        t = parse("Tr[G_a*G_b]*Tr[G_c*G_d] + Tr[G_a*G_b]*g_cd + f_abcd");
        TAssert.assertEquals(dTrace.transform(t), "20*g_ab*g_cd + f_abcd");
    }

    @Test
    public void test15() {
        Tensor t;
        t = parse("Tr[G_a*G_b + g_ab]");
        TAssert.assertEquals(dTrace.transform(t), "8*g_ab");
    }

    @Test
    public void test16() {
        DiracOptions dOpts = new DiracOptions();
        dOpts.dimension = parse("6");
        Transformation dTrace = new DiracTraceTransformation(dOpts);
        Tensor t;
        t = parse("Tr[G_a*G_b + g_ab]");
        TAssert.assertEquals(dTrace.transform(t), "16*g_ab");
    }

    @Test
    public void test18() throws Exception {
        testFeynCalcData("DiracTrace_aabbcd");
    }

    @Test
    public void test19() throws Exception {
        testFeynCalcData("DiracTrace_aabcde5");
    }

    @Test
    @LongTest
    public void test20() throws Exception {
        testFeynCalcData("DiracTrace_abcdef5");
    }

    @Ignore
    @Test
    public void test21() throws Exception {
        testFeynCalcData("DiracTrace_abcdefgg5");
    }

    @Ignore
    @Test
    public void testCache1() throws Exception {
        DiracOptions dOpts = new DiracOptions();
        Transformation dTrace = new DiracTraceTransformation(dOpts);

        for (int i = 0; i < 10; ++i) {
            long start = System.currentTimeMillis();
            dTrace.transform(parse("Tr[G_a*G_b*G_c*G_d*G_e*G_f*G_g*G_h*G_k*G_l]"));
            Assert.assertTrue(i < 5 || System.currentTimeMillis() - start < 100);
        }
    }

    void testFeynCalcData(String resourceFile) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                DiracSimplify0.class.getResourceAsStream(resourceFile)));

        Tensor test, expected;
        String line;
        int totalTests = 0;
        while ((line = reader.readLine()) != null) {
            ++totalTests;
            String initial = line;
            test = parse(initial);

            String answer = reader.readLine();
            expected = parse(answer);
            expected = delDummy.transform(expected);
            expected = expandAndEliminate(expected);
            expected = traceOfOne.transform(expected);
            expected = schouten4.transform(expected);

            Tensor result = dTrace.transform(test);
            result = schouten4.transform(result);

            Tensor diff = subtract(result, expected);
            diff = expandAndEliminate(diff);
            diff = traceOfOne.transform(diff);
            diff = deltaTrace.transform(diff);
            diff = schouten4.transform(diff);
            try {
                TAssert.assertEquals(Complex.ZERO, diff);
            } catch (Throwable err) {
                System.out.println("Original:");
                System.out.println(initial);
                System.out.println("Answer:");
                System.out.println(answer);
                System.out.println("Redberry:");
                System.out.println(result);
                throw err;
            }
        }

        System.out.println(totalTests + " tests passed");
    }
}
