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
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.*;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.test.LongTest;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.indices.IndexType.Matrix1;
import static cc.redberry.core.indices.IndicesFactory.createAlphabetical;
import static cc.redberry.core.indices.IndicesUtils.inverseIndexState;
import static cc.redberry.core.tensor.HashingStrategy.iGraphHash;
import static cc.redberry.core.tensor.HashingStrategy.iHash;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.test.TestUtils.its;
import static cc.redberry.core.utils.TensorUtils.getAllDummyIndicesT;
import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class HashingStrategyTest {
    @Before
    public void setUp() throws Exception {
        CC.reset();
    }

    @Test
    public void testSomeMethod() {
        assertTrue(iHash(parse("T^i_j*T^j_k"))
                == iHash(parse("T^i_s*T^s_k")));
    }

    @Test
    public void test1() {
        addSymmetry("T_mnpq", LatinLower, false, 1, 0, 3, 2);
        assertTrue(iHash(parse("T^ijpq*T_pqrs"))
                == iHash(parse("T^jipq*T_pqsr")));
        assertFalse(iHash(parse("T^ijpq*T_pqrs"))
                == iHash(parse("T^jpiq*T_pqsr")));
    }

    @Test
    public void test2() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, 1, 0, 2, 3);
        Tensor a = parse("T_{bdca}");
        Tensor b = parse("T_{cdab}");
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test3() throws Exception {
        Tensor a = parse("(A_abc - A_bac)");
        Tensor b = parse("(A_bac - A_abc)");
        assertEquals(iHash(a), iHash(b));
    }

    @Test
    public void test3a() {
        addSymmetry("R_mnp", IndexType.LatinLower, true, 2, 1, 0);
        Product a = (Product) parse("R_{ijk}*F^{jk}");
        Product b = (Product) parse("R_{kji}*F^{jk}");
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(a.iHashCode(), b.iHashCode());
        Assert.assertEquals(iHash(a), iHash(b));
    }

    @Test
    public void test3b() {
        Tensor a, b;
        a = parse("-f_a");
        b = parse("f_a");
        Assert.assertEquals(iHash(a), iHash(b));

        a = parse("-2*f_a");
        b = parse("2*f_a");
        Assert.assertEquals(iHash(a), iHash(b));

        a = parse("-2*c*f_a");
        b = parse("2*c*f_a");
        Assert.assertEquals(iHash(a), iHash(b));


        a = parse("-2*c*d");
        b = parse("2*c*d");
        Assert.assertEquals(iHash(a), iHash(b));
    }

    @Test(timeout = 5000L)
    public void test8() throws Exception {
        Product a = (Product) parse("g^{hi}*d^{f}_{l}*g_{bm}*g^{aq}*g_{go}*d^{r}_{p}*d^{j}_{u}*d^{k}_{v}*d^{s}_{w}*d^{n}_{x}*d^{d}_{c}*d^{e}_{t}");
        Product b = (Product) parse("d^{r}_{p}*d^{h}_{c}*d^{q}_{g}*d^{e}_{t}*d^{d}_{m}*d^{f}_{o}*d^{i}_{l}*d^{j}_{u}*d^{k}_{v}*d^{s}_{w}*d^{n}_{x}*d^{a}_{b}");
        Assert.assertFalse(a.iHashCode() == b.iHashCode());
        Assert.assertFalse(TensorUtils.equals(a, b));
    }

    @Test
    public void test9() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);


        Product a = (Product) parse("Tr[G_{s}*G_{h}*G_{m}*G_{d}]*epsChi^{m}*d_{GA}^{C}*epsPsi^{d}*eps1^{hG}*eps2^{i}_{C}*eps3^{sA}");
        Product b = (Product) parse("Tr[G_{s}*G_{d}*G_{m}*G_{h}]*epsChi^{m}*d_{GA}^{C}*epsPsi^{d}*eps1^{hG}*eps2^{i}_{C}*eps3^{sA}");

        System.out.println(a.hashCode());
        System.out.println(b.hashCode());

        System.out.println(TensorUtils.compare1(a, b));
    }

    @Test
    public void test4() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);


        Tensor a = parse("Tr[G_{a}*G_{b}*G_{c}]");
        Tensor b = parse("Tr[G_{b}*G_{a}*G_{c}]");

        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test5() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("Y^a'_b'a"), Matrix1);


        Tensor a = parse("Tr[G_{a}*G_{b}*G^{a}*G^{b}]");
        Tensor b = parse("Tr[G_{a}*G^{a}*G_{b}*G^{b}]");


        Assert.assertTrue(a.hashCode() != b.hashCode());
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test6() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("Y^a'_b'a"), Matrix1);

        Tensor a = parse("Tr[G_{o}*G_{m}*G_{n}*G_{p}*G_{r}*G_{f}*G_{h}*G_{c}]");
        Tensor b = parse("Tr[G_{o}*G_{m}*G_{f}*G_{r}*G_{p}*G_{n}*G_{h}*G_{c}]");

        Assert.assertTrue(a.hashCode() == b.hashCode());
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test6a() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("Y^a'_b'a"), Matrix1);

        Tensor a = parse("G_a*G_b");
        Tensor b = parse("G_b*G_a");

        Assert.assertTrue(a.hashCode() == b.hashCode());
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test6b() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("Y^a'_b'a"), Matrix1);

        Tensor a = parse("Tr[G_a*G_b*G_c*G_d]");
        Tensor b = parse("Tr[G_a*G_c*G_b*G_d]");

        Assert.assertTrue(a.hashCode() == b.hashCode());
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test7() {
        Tensor a = parse("(A_abc - A_bac)*T^c");
        Tensor b = parse("(A_bac - A_abc)*T^c");
        TAssert.assertEquals(a.hashCode(), b.hashCode());
        TAssert.assertEquals(
                HashingStrategy.iHash(a, a.getIndices().getFree()),
                HashingStrategy.iHash(b, a.getIndices().getFree()));
    }

    @Test
    public void test10() throws Exception {
        for (int i = 0; i < 100; i++) {
            CC.reset();
            Tensor a = parse("e_{df}*(4*d^{f}_{c}*d_{a}^{d}+4*g^{df}*g_{ac}-4*d_{a}^{f}*d^{d}_{c})");
            Tensor b = parse("e^{d}_{f}*(4*g_{ad}*d^{f}_{c}-4*d_{a}^{f}*g_{dc}+4*g_{ac}*d_{d}^{f})");
            Assert.assertEquals(iHash(a), iHash(b));
        }
    }


    @Test
    public void test11() {
        Tensor a = parse("A_abc");
        Tensor b = parse("A_bac");
        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertTrue(iHash(a, a.getIndices()) != iHash(b, a.getIndices()));
    }

    @Test
    public void test13() throws Exception {
        Tensor a = parse("g_{ah}*g_{bf}*g_{cg}*g_{de}");
        Tensor b = parse("g_{ah}*g_{bg}*g_{cd}*g_{ef}");
        Assert.assertTrue(a.hashCode() == b.hashCode());
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test14() throws Exception {
        Tensor a = parse("h^{b}_{c}*g^{ca}");
        Tensor b = parse("h^{a}_{c}*g^{cb}");
        Assert.assertTrue(a.hashCode() == b.hashCode());
        Assert.assertTrue(iHash(a, a.getIndices()) != iHash(b, a.getIndices()));
    }

    @Test
    public void test12() throws Exception {
        CC.resetTensorNames(1234);
        Product a = (Product) parse("(f_{g_{1}c_{1}b_{1}}^{b_{1}}*f_{da_{1}}^{a_{1}}_{j_{1}}*r_{f_{1}}^{c_{1}}_{b}*r^{j_{1}a}_{h_{1}e_{1}}*f^{g_{1}e_{1}h_{1}}*k^{f_{1}}+95*f^{g_{1}d_{1}}_{d_{1}}^{i_{1}}*r_{i_{1}f_{1}g_{1}}*f_{d}^{e_{1}}_{e_{1}}*k^{f_{1}}*k_{b}*k^{a}+r_{h_{1}d}^{g_{1}}*k_{g_{1}}*k^{h_{1}}*g_{j_{1}i_{1}}*g^{j_{1}a}*d_{b}^{i_{1}}+r_{d_{1}}^{h_{1}}_{b}^{d_{1}}*r^{a}_{h_{1}d}^{e_{1}}*r^{b_{1}}_{e_{1}b_{1}g_{1}}*k^{g_{1}}*d^{c_{1}}_{c_{1}}-38*f_{d_{1}bf_{1}}^{d_{1}}*f_{h_{1}}^{a}_{i_{1}e_{1}}*r^{f_{1}i_{1}}_{g_{1}}*g^{g_{1}h_{1}}*d_{d}^{e_{1}}+r^{j_{1}}_{e_{1}h_{1}}*r_{j_{1}}^{d_{1}}_{d_{1}}*r^{ac_{1}}_{c_{1}}^{f_{1}}*r^{h_{1}}_{d}^{e_{1}}_{f_{1}}*k_{b}+f_{g_{1}}^{e_{1}}_{j_{1}i_{1}}*f_{b}^{i_{1}h_{1}d_{1}}*r_{c_{1}d}^{c_{1}j_{1}}*f_{h_{1}e_{1}}^{g_{1}}*d_{d_{1}}^{a}+r_{di_{1}j_{1}}*r^{aj_{1}h_{1}}*f^{i_{1}}_{bh_{1}}*k^{g_{1}}*k_{g_{1}}-25*k^{j_{1}}*k_{h_{1}}*k^{h_{1}}*d_{i_{1}}^{a}*d^{i_{1}}_{b}*g_{j_{1}d}-94*r^{j_{1}af_{1}}*r_{j_{1}e_{1}h_{1}}*r^{g_{1}h_{1}}_{f_{1}}*r_{g_{1}db}^{e_{1}}*d^{d_{1}}_{d_{1}})*(r_{mp}^{q}*r_{cl}^{lm}*f^{fp}_{q}*k^{i}*k_{o}*k^{o}-18*f^{iln}_{l}*k^{p}*k^{f}*k_{c}*g_{pn}*d^{m}_{m}+r_{c}^{mq}_{o}*f_{qnp}*f^{ipo}*k^{n}*k^{f}*k_{m}+f_{k}^{ipk}*r^{f}_{lq}*r_{p}^{lq}_{j}*f^{j}_{o}^{n}*k_{n}*d^{o}_{c}+r_{qncl}*r_{m}^{f}_{o}^{i}*f^{lm}_{p}*f^{pqn}*k^{o}-60*r^{n}_{pmq}*f^{i}_{n}^{p}*f^{f}_{l}^{l}*k_{c}*g^{qm}+42*f_{pc}^{of}*f^{pln}_{j}*r_{ln}^{m}*f_{q}^{ji}*f_{o}^{q}_{m}+f_{jn}^{jo}*f_{lc}^{i}_{m}*f_{ok}^{k}*f^{fnl}*k^{m}-5*f^{n}_{qm}^{l}*r^{mj}_{j}*r_{nc}^{f}_{o}*k^{o}*k^{q}*d^{i}_{l}-88*r_{l}^{of}*r_{n}^{m}_{q}*r_{pm}^{q}_{o}*f^{npi}*d^{l}_{c})*(-51*f^{uwex}*f_{tuw}^{t}*f^{d}_{x}^{v}*k^{z}*k_{z}*d_{v}^{g}-57*f_{yz}^{e}_{w}*r^{d}_{vx}*r^{vzxw}*k^{y}*k^{g}-36*f_{y}^{x}_{vz}*f^{ezg}*f^{ywd}*k_{x}*d_{w}^{v}+43*f^{d}_{st}^{u}*f_{r}^{grw}*f_{vuyx}*r_{w}^{xy}*f^{sve}*k^{t}+f^{ds}_{ws}*r^{z}_{x}^{e}*r^{ygx}_{z}*d^{t}_{t}*d^{w}_{y}+88*f^{d}_{x}^{uz}*r^{wsg}*r_{uz}^{xv}*r^{e}_{wvt}*f_{r}^{r}_{s}*k^{t}+f_{z}^{xe}*k_{x}*k^{y}*k^{d}*k^{z}*d_{y}^{g}+r_{zuv}*r^{dge}*r^{wux}*r_{w}^{v}_{x}^{y}*k^{z}*k_{y}+r_{vy}^{w}*r_{w}^{g}_{u}^{y}*r^{vtd}_{t}*k^{e}*k_{z}*g^{uz}+76*f_{yx}^{s}_{s}*r_{r}^{xru}*f_{u}^{dg}*d_{t}^{t}*g^{ey})*(r_{m_{1}ep_{1}}*r^{n_{1}p_{1}o_{1}q_{1}}*f^{m_{1}}_{fn_{1}}*k_{g}*k_{i}*g_{o_{1}q_{1}}+46*r^{n_{1}}_{m_{1}}^{q_{1}}*f_{igf}*f^{p_{1}}_{q_{1}}^{m_{1}}*k_{e}*g_{n_{1}o_{1}}*d^{o_{1}}_{p_{1}}-72*f_{igp_{1}o_{1}}*k_{f}*k_{e}*k^{q_{1}}*k_{q_{1}}*g^{p_{1}o_{1}}+f_{m_{1}f}^{n_{1}q_{1}}*r^{l_{1}}_{gp_{1}}*r_{q_{1}}^{o_{1}}_{n_{1}}^{p_{1}}*f_{l_{1}ei}*d_{o_{1}}^{m_{1}}-74*r^{q_{1}}_{fe}*k_{q_{1}}*k_{g}*k^{p_{1}}*d_{i}^{o_{1}}*g_{p_{1}o_{1}}+f_{q_{1}}^{n_{1}}_{l_{1}p_{1}}*r^{m_{1}}_{i}^{q_{1}}*r_{n_{1}}^{k_{1}p_{1}}*r_{o_{1}em_{1}}^{l_{1}}*f_{g}^{o_{1}}_{f}*k_{k_{1}}+f_{g}^{m_{1}n_{1}}_{q_{1}}*f_{p_{1}n_{1}e}^{k_{1}}*r^{q_{1}l_{1}}_{f}*r_{ik_{1}o_{1}}*r_{m_{1}l_{1}}^{o_{1}}*k^{p_{1}}+f_{k_{1}l_{1}e}^{k_{1}}*f_{f}^{q_{1}}_{n_{1}p_{1}}*r^{l_{1}o_{1}}_{g}*f^{p_{1}}_{q_{1}o_{1}}*k_{i}*k^{n_{1}}-62*f_{m_{1}}^{n_{1}l_{1}}_{g}*r_{q_{1}o_{1}}^{m_{1}}*r_{p_{1}n_{1}f}*f_{iel_{1}}*k^{o_{1}}*g^{p_{1}q_{1}}-90*r_{g}^{o_{1}}_{i}*r_{n_{1}}^{q_{1}}_{m_{1}}*f^{m_{1}}_{q_{1}}^{n_{1}}*k_{o_{1}}*d_{l_{1}}^{l_{1}}*g_{ef})*(f^{z_{1}b_{2}}_{x_{1}}^{x_{1}}*r^{c_{2}}_{b_{2}z_{1}}^{y_{1}}*k_{c_{2}}*k_{y_{1}}*d^{c}_{a}+f_{ax_{1}a_{2}z_{1}}*r_{b_{2}}^{cc_{2}}*r^{x_{1}a_{2}}_{y_{1}}^{b_{2}}*k_{c_{2}}*g^{y_{1}z_{1}}+39*f_{u_{1}}^{u_{1}w_{1}}_{b_{2}}*f^{c_{2}}_{a_{2}}^{b_{2}}_{x_{1}}*r^{a_{2}x_{1}y_{1}}*r_{a}^{v_{1}c}*r_{c_{2}w_{1}v_{1}y_{1}}+f^{cc_{2}}_{y_{1}}^{y_{1}}*k^{z_{1}}*k_{c_{2}}*k_{a_{2}}*k^{a_{2}}*g_{z_{1}a}+88*r_{a_{2}c_{2}}^{x_{1}}*r^{w_{1}}_{b_{2}x_{1}w_{1}}*f^{y_{1}c_{2}c}*d^{b_{2}}_{y_{1}}*d_{a}^{a_{2}}+37*f_{w_{1}}^{r_{1}}_{c_{2}r_{1}}*f^{w_{1}v_{1}b_{2}z_{1}}*r_{s_{1}}^{cs_{1}}_{z_{1}}*r^{x_{1}t_{1}}_{b_{2}t_{1}}*f^{c_{2}}_{ax_{1}}*k_{v_{1}}-56*f_{b_{2}c_{2}}^{u_{1}}_{u_{1}}*f_{x_{1}w_{1}a_{2}y_{1}}*r_{t_{1}}^{t_{1}c_{2}c}*f_{a}^{a_{2}w_{1}}*f^{x_{1}y_{1}b_{2}}+r^{w_{1}}_{w_{1}}^{c_{2}b_{2}}*f^{cv_{1}}_{v_{1}}*f^{a_{2}}_{ac_{2}}*k^{x_{1}}*k_{a_{2}}*g_{x_{1}b_{2}}-61*f^{cu_{1}}_{a}^{a_{2}}*r^{x_{1}y_{1}z_{1}}*r_{v_{1}}^{c_{2}}_{z_{1}u_{1}}*r_{t_{1}}^{t_{1}v_{1}}_{x_{1}}*r_{c_{2}y_{1}}^{b_{2}}_{a_{2}}*k_{b_{2}}-83*r^{z_{1}c}_{a_{2}x_{1}}*r_{ay_{1}}^{b_{2}}_{w_{1}}*r_{c_{2}}^{a_{2}y_{1}}_{z_{1}}*f^{c_{2}}_{b_{2}}^{w_{1}}*k^{x_{1}})");
        Product b = (Product) parse("(f_{g_{1}c_{1}b_{1}}^{b_{1}}*f_{g_{2}a_{1}}^{a_{1}}_{j_{1}}*r_{f_{1}}^{c_{1}}_{b}*r^{j_{1}}_{i_{2}h_{1}}^{e_{1}}*f^{g_{1}}_{e_{1}}^{h_{1}}*k^{f_{1}}+95*f^{g_{1}d_{1}}_{d_{1}}^{i_{1}}*r_{i_{1}f_{1}g_{1}}*f_{g_{2}}^{e_{1}}_{e_{1}}*k^{f_{1}}*k_{i_{2}}*k_{b}+r_{h_{1}g_{2}}^{g_{1}}*k_{g_{1}}*k^{h_{1}}*g_{i_{1}j_{1}}*d^{j_{1}}_{i_{2}}*d_{b}^{i_{1}}+r_{d_{1}}^{h_{1}}_{b}^{d_{1}}*r_{i_{2}h_{1}g_{2}e_{1}}*r^{b_{1}e_{1}}_{b_{1}g_{1}}*k^{g_{1}}*d^{c_{1}}_{c_{1}}-38*f_{d_{1}bf_{1}}^{d_{1}}*f_{h_{1}i_{2}i_{1}}^{e_{1}}*r^{f_{1}i_{1}}_{g_{1}}*g^{h_{1}g_{1}}*g_{e_{1}g_{2}}+r^{j_{1}e_{1}}_{h_{1}}*r_{j_{1}}^{d_{1}}_{d_{1}}*r_{i_{2}}^{c_{1}}_{c_{1}}^{f_{1}}*r^{h_{1}}_{g_{2}e_{1}f_{1}}*k_{b}+f_{g_{1}e_{1}j_{1}i_{1}}*f_{b}^{i_{1}h_{1}d_{1}}*r_{c_{1}g_{2}}^{c_{1}j_{1}}*f_{h_{1}}^{e_{1}g_{1}}*g_{i_{2}d_{1}}+r_{g_{2}i_{1}j_{1}}*r_{i_{2}}^{j_{1}h_{1}}*f^{i_{1}}_{bh_{1}}*k^{g_{1}}*k_{g_{1}}-25*k^{j_{1}}*k_{h_{1}}*k^{h_{1}}*d_{b}^{i_{1}}*g_{i_{1}i_{2}}*g_{j_{1}g_{2}}-94*r^{j_{1}}_{i_{2}}^{f_{1}}*r_{j_{1}}^{e_{1}}_{h_{1}}*r^{g_{1}h_{1}}_{f_{1}}*r_{g_{1}g_{2}be_{1}}*d^{d_{1}}_{d_{1}})*(r_{mp}^{q}*r^{h_{2}}_{l}^{lm}*f_{e_{2}}^{p}_{q}*k^{h}*k^{o}*k_{o}-18*f^{hl}_{nl}*k^{p}*k^{h_{2}}*k_{e_{2}}*d^{n}_{p}*d_{m}^{m}+r^{h_{2}mq}_{o}*f_{q}^{n}_{p}*f^{hpo}*k_{n}*k_{e_{2}}*k_{m}+f_{k}^{hpk}*r_{e_{2}lq}*r_{p}^{lqj}*f_{jon}*k^{n}*g^{h_{2}o}+r_{q}^{nh_{2}}_{l}*r_{me_{2}o}^{h}*f^{lm}_{p}*f^{pq}_{n}*k^{o}-60*r_{npmq}*f^{hnp}*f_{e_{2}l}^{l}*k^{h_{2}}*g^{mq}+42*f_{p}^{h_{2}o}_{e_{2}}*f^{pl}_{n}^{j}*r_{l}^{nm}*f_{qj}^{h}*f_{o}^{q}_{m}+f_{j}^{njo}*f_{l}^{h_{2}h}_{m}*f_{ok}^{k}*f_{e_{2}n}^{l}*k^{m}-5*f_{nqm}^{l}*r^{mj}_{j}*r^{nh_{2}}_{e_{2}o}*k^{o}*k^{q}*d_{l}^{h}-88*r_{l}^{o}_{e_{2}}*r^{nm}_{q}*r_{pm}^{q}_{o}*f_{n}^{ph}*g^{lh_{2}})*(-51*f^{uwf_{2}}_{x}*f_{tuw}^{t}*f^{g_{2}xv}*k_{z}*k^{z}*d^{d_{2}}_{v}-57*f_{yz}^{f_{2}}_{w}*r^{g_{2}}_{v}^{x}*r^{vz}_{x}^{w}*k^{y}*k^{d_{2}}-36*f_{yxvz}*f^{f_{2}zd_{2}}*f^{ywg_{2}}*k^{x}*d_{w}^{v}+43*f^{g_{2}}_{st}^{u}*f_{r}^{d_{2}rw}*f_{vuy}^{x}*r_{wx}^{y}*f^{svf_{2}}*k^{t}+f^{g_{2}s}_{ws}*r^{zxf_{2}}*r^{yd_{2}}_{xz}*d_{t}^{t}*d^{w}_{y}+88*f^{g_{2}xuz}*r^{wsd_{2}}*r_{uzx}^{v}*r^{f_{2}}_{wvt}*f_{r}^{r}_{s}*k^{t}+f_{zx}^{f_{2}}*k^{x}*k^{y}*k^{g_{2}}*k^{z}*d^{d_{2}}_{y}+r_{zuv}*r^{g_{2}d_{2}f_{2}}*r^{wu}_{x}*r_{w}^{vxy}*k^{z}*k_{y}+r_{vy}^{w}*r_{w}^{d_{2}}_{u}^{y}*r^{vtg_{2}}_{t}*k^{f_{2}}*k_{z}*g^{uz}+76*f_{y}^{xs}_{s}*r_{rx}^{ru}*f_{u}^{g_{2}d_{2}}*d_{t}^{t}*g^{yf_{2}})*(r_{m_{1}f_{2}p_{1}}*r^{n_{1}p_{1}o_{1}q_{1}}*f^{m_{1}e_{2}}_{n_{1}}*k_{d_{2}}*k_{h}*g_{o_{1}q_{1}}+46*r^{n_{1}}_{m_{1}}^{q_{1}}*f_{hd_{2}}^{e_{2}}*f^{p_{1}}_{q_{1}}^{m_{1}}*k_{f_{2}}*g_{n_{1}o_{1}}*d^{o_{1}}_{p_{1}}-72*f_{hd_{2}p_{1}o_{1}}*k^{e_{2}}*k_{f_{2}}*k_{q_{1}}*k^{q_{1}}*g^{o_{1}p_{1}}+f_{m_{1}}^{e_{2}n_{1}q_{1}}*r^{l_{1}}_{d_{2}p_{1}}*r_{q_{1}}^{o_{1}}_{n_{1}}^{p_{1}}*f_{l_{1}f_{2}h}*d_{o_{1}}^{m_{1}}-74*r^{q_{1}e_{2}}_{f_{2}}*k_{q_{1}}*k_{d_{2}}*k^{p_{1}}*d^{o_{1}}_{h}*g_{p_{1}o_{1}}+f_{q_{1}}^{n_{1}}_{l_{1}p_{1}}*r^{m_{1}}_{h}^{q_{1}}*r_{n_{1}}^{k_{1}p_{1}}*r_{o_{1}f_{2}m_{1}}^{l_{1}}*f_{d_{2}}^{o_{1}e_{2}}*k_{k_{1}}+f_{d_{2}}^{m_{1}n_{1}}_{q_{1}}*f_{p_{1}n_{1}f_{2}}^{k_{1}}*r^{q_{1}l_{1}e_{2}}*r_{hk_{1}o_{1}}*r_{m_{1}l_{1}}^{o_{1}}*k^{p_{1}}+f_{k_{1}l_{1}f_{2}}^{k_{1}}*f^{e_{2}q_{1}}_{n_{1}p_{1}}*r^{l_{1}o_{1}}_{d_{2}}*f^{p_{1}}_{q_{1}o_{1}}*k_{h}*k^{n_{1}}-62*f_{m_{1}}^{n_{1}l_{1}}_{d_{2}}*r_{q_{1}o_{1}}^{m_{1}}*r_{p_{1}n_{1}}^{e_{2}}*f_{hf_{2}l_{1}}*k^{o_{1}}*g^{q_{1}p_{1}}-90*r_{d_{2}}^{o_{1}}_{h}*r_{n_{1}}^{q_{1}}_{m_{1}}*f^{m_{1}}_{q_{1}}^{n_{1}}*k_{o_{1}}*d_{l_{1}}^{l_{1}}*d^{e_{2}}_{f_{2}})*(f^{z_{1}}_{b_{2}x_{1}}^{x_{1}}*r^{c_{2}b_{2}}_{z_{1}}^{y_{1}}*k_{c_{2}}*k_{y_{1}}*d^{i_{2}}_{h_{2}}+f^{i_{2}}_{x_{1}a_{2}z_{1}}*r^{b_{2}}_{h_{2}}^{c_{2}}*r^{x_{1}a_{2}}_{y_{1}b_{2}}*k_{c_{2}}*g^{z_{1}y_{1}}+39*f_{u_{1}}^{u_{1}w_{1}b_{2}}*f^{c_{2}}_{a_{2}b_{2}x_{1}}*r^{a_{2}x_{1}y_{1}}*r^{i_{2}v_{1}}_{h_{2}}*r_{c_{2}w_{1}v_{1}y_{1}}+f_{h_{2}}^{c_{2}}_{y_{1}}^{y_{1}}*k^{z_{1}}*k_{c_{2}}*k^{a_{2}}*k_{a_{2}}*d^{i_{2}}_{z_{1}}+88*r_{a_{2}c_{2}}^{x_{1}}*r^{w_{1}b_{2}}_{x_{1}w_{1}}*f^{y_{1}c_{2}}_{h_{2}}*g_{y_{1}b_{2}}*g^{i_{2}a_{2}}+37*f_{w_{1}}^{r_{1}}_{c_{2}r_{1}}*f^{w_{1}v_{1}}_{b_{2}}^{z_{1}}*r_{s_{1}h_{2}}^{s_{1}}_{z_{1}}*r^{x_{1}t_{1}b_{2}}_{t_{1}}*f^{c_{2}i_{2}}_{x_{1}}*k_{v_{1}}-56*f^{b_{2}}_{c_{2}}^{u_{1}}_{u_{1}}*f_{x_{1}w_{1}a_{2}y_{1}}*r_{t_{1}}^{t_{1}c_{2}}_{h_{2}}*f^{i_{2}a_{2}w_{1}}*f^{x_{1}y_{1}}_{b_{2}}+r^{w_{1}}_{w_{1}}^{c_{2}}_{b_{2}}*f_{h_{2}}^{v_{1}}_{v_{1}}*f^{a_{2}i_{2}}_{c_{2}}*k^{x_{1}}*k_{a_{2}}*d^{b_{2}}_{x_{1}}-61*f_{h_{2}}^{u_{1}i_{2}a_{2}}*r^{x_{1}y_{1}z_{1}}*r_{v_{1}}^{c_{2}}_{z_{1}u_{1}}*r_{t_{1}}^{t_{1}v_{1}}_{x_{1}}*r_{c_{2}y_{1}b_{2}a_{2}}*k^{b_{2}}-83*r^{z_{1}}_{h_{2}a_{2}x_{1}}*r^{i_{2}}_{y_{1}b_{2}w_{1}}*r_{c_{2}}^{a_{2}y_{1}}_{z_{1}}*f^{c_{2}b_{2}w_{1}}*k^{x_{1}})");

        Assert.assertEquals(a.hashCode(), b.hashCode());
        Assert.assertEquals(iHash(a), iHash(b));
        Assert.assertTrue(IndexMappings.equals(a, b));

    }

    @Test
    public void test12a() throws Exception {
        Tensor a = parse("95*r_{i_{1}f_{1}g_{1}}*f_{d}^{e_{1}}_{e_{1}}*k_{b}*k^{a}*k^{f_{1}}*f^{g_{1}d_{1}}_{d_{1}}^{i_{1}}-25*d_{i_{1}}^{a}*d^{i_{1}}_{b}*g_{j_{1}d}*k_{h_{1}}*k^{h_{1}}*k^{j_{1}}-94*d^{d_{1}}_{d_{1}}*r^{j_{1}af_{1}}*r_{j_{1}e_{1}h_{1}}*r^{g_{1}h_{1}}_{f_{1}}*r_{g_{1}db}^{e_{1}}+r_{f_{1}}^{c_{1}}_{b}*f^{g_{1}e_{1}h_{1}}*k^{f_{1}}*r^{j_{1}a}_{h_{1}e_{1}}*f_{g_{1}c_{1}b_{1}}^{b_{1}}*f_{da_{1}}^{a_{1}}_{j_{1}}+g^{j_{1}a}*d_{b}^{i_{1}}*g_{j_{1}i_{1}}*r_{h_{1}d}^{g_{1}}*k_{g_{1}}*k^{h_{1}}+r^{aj_{1}h_{1}}*r_{di_{1}j_{1}}*f^{i_{1}}_{bh_{1}}*k^{g_{1}}*k_{g_{1}}+d^{c_{1}}_{c_{1}}*k^{g_{1}}*r^{a}_{h_{1}d}^{e_{1}}*r_{d_{1}}^{h_{1}}_{b}^{d_{1}}*r^{b_{1}}_{e_{1}b_{1}g_{1}}+r_{j_{1}}^{d_{1}}_{d_{1}}*r^{j_{1}}_{e_{1}h_{1}}*k_{b}*r^{h_{1}}_{d}^{e_{1}}_{f_{1}}*r^{ac_{1}}_{c_{1}}^{f_{1}}-38*g^{g_{1}h_{1}}*d_{d}^{e_{1}}*r^{f_{1}i_{1}}_{g_{1}}*f_{h_{1}}^{a}_{i_{1}e_{1}}*f_{d_{1}bf_{1}}^{d_{1}}+d_{d_{1}}^{a}*f_{h_{1}e_{1}}^{g_{1}}*r_{c_{1}d}^{c_{1}j_{1}}*f_{g_{1}}^{e_{1}}_{j_{1}i_{1}}*f_{b}^{i_{1}h_{1}d_{1}}");
        Tensor b = parse("95*r_{i_{1}f_{1}g_{1}}*f_{g_{2}}^{e_{1}}_{e_{1}}*k_{i_{2}}*k_{b}*k^{f_{1}}*f^{g_{1}d_{1}}_{d_{1}}^{i_{1}}-25*g_{i_{1}i_{2}}*d_{b}^{i_{1}}*g_{j_{1}g_{2}}*k_{h_{1}}*k^{h_{1}}*k^{j_{1}}-94*d^{d_{1}}_{d_{1}}*r^{j_{1}}_{i_{2}}^{f_{1}}*r_{j_{1}}^{e_{1}}_{h_{1}}*r^{g_{1}h_{1}}_{f_{1}}*r_{g_{1}g_{2}be_{1}}+r_{f_{1}}^{c_{1}}_{b}*f^{g_{1}}_{e_{1}}^{h_{1}}*k^{f_{1}}*r^{j_{1}}_{i_{2}h_{1}}^{e_{1}}*f_{g_{1}c_{1}b_{1}}^{b_{1}}*f_{g_{2}a_{1}}^{a_{1}}_{j_{1}}+d^{j_{1}}_{i_{2}}*d_{b}^{i_{1}}*g_{i_{1}j_{1}}*r_{h_{1}g_{2}}^{g_{1}}*k_{g_{1}}*k^{h_{1}}+r_{i_{2}}^{j_{1}h_{1}}*r_{g_{2}i_{1}j_{1}}*f^{i_{1}}_{bh_{1}}*k^{g_{1}}*k_{g_{1}}+d^{c_{1}}_{c_{1}}*k^{g_{1}}*r_{i_{2}h_{1}g_{2}e_{1}}*r_{d_{1}}^{h_{1}}_{b}^{d_{1}}*r^{b_{1}e_{1}}_{b_{1}g_{1}}+r_{j_{1}}^{d_{1}}_{d_{1}}*r^{j_{1}e_{1}}_{h_{1}}*k_{b}*r^{h_{1}}_{g_{2}e_{1}f_{1}}*r_{i_{2}}^{c_{1}}_{c_{1}}^{f_{1}}-38*g^{h_{1}g_{1}}*g_{e_{1}g_{2}}*r^{f_{1}i_{1}}_{g_{1}}*f_{h_{1}i_{2}i_{1}}^{e_{1}}*f_{d_{1}bf_{1}}^{d_{1}}+g_{i_{2}d_{1}}*f_{h_{1}}^{e_{1}g_{1}}*r_{c_{1}g_{2}}^{c_{1}j_{1}}*f_{g_{1}e_{1}j_{1}i_{1}}*f_{b}^{i_{1}h_{1}d_{1}}");
        Assert.assertEquals(iHash(a, ParserIndices.parse("_b")), iHash(b, ParserIndices.parse("_b")));
    }

    @Test
    public void test15() throws Exception {
        CC.resetTensorNames(123);
        setSymmetric("r_abcd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("f_abcd");

        Tensor a = parse("-51*(r_{da}^{e}-5*f_{ad}^{e}+2*r_{a}^{e}_{d}+2*r^{e}_{da})*(r^{k}_{kb}+3*r_{b}^{k}_{k}+3*f_{b}^{k}_{k}+3*r_{kb}^{k})*(-7*f_{c}^{j}_{pj}+3*r^{j}_{pcj})*(r_{iq}^{g}+r^{g}_{iq}+4*f_{iq}^{g}+4*r_{q}^{g}_{i})*(3*f^{ai}_{eg}+7*r^{a}_{ge}^{i})-18*(r_{dbp}+4*f_{bpd}+2*r_{bpd}+3*r_{pdb})*(r^{ef}_{a}+5*f^{f}_{a}^{e}+4*r_{a}^{ef})*(5*f_{e}^{k}_{ki}+5*r^{k}_{eki})*(-7*f^{j}_{cj}^{i}+3*r_{c}^{ji}_{j})*(-f_{q}^{a}_{f}+2*r_{f}^{a}_{q}+3*r^{a}_{qf}+4*r_{qf}^{a})+100000*k^{e}*k_{e}*k_{p}*k_{d}*g_{qb}*(r^{f}_{fc}+5*r_{c}^{f}_{f}+2*f_{fc}^{f}+2*r_{fc}^{f})-78000*(r_{a}^{gf}-3*f_{a}^{fg}+2*r^{gf}_{a}+4*r^{f}_{a}^{g})*k_{c}*g^{eh}*d^{a}_{h}*(r_{gep}-7*f_{gpe}+2*r_{pge})*(-2*f_{bfqd}+8*r_{fdbq})-42*(6*f_{ec}^{h}+2*r^{h}_{ec}+2*r_{ec}^{h})*(-4*f^{ka}_{kd}+6*r^{ak}_{dk})*(-5*f_{g}^{j}_{p}^{e}+5*r^{ej}_{gp})*(7*f_{hb}^{g}_{f}+3*r_{bf}^{g}_{h})*(-8*f_{qja}^{f}+2*r_{a}^{f}_{jq})+1000*(r_{q}^{e}_{c}-5*f_{qc}^{e}+2*r_{cq}^{e}+2*r^{e}_{cq})*k^{g}*k^{f}*g_{pd}*(r_{gb}^{a}+r_{b}^{a}_{g}-6*f_{g}^{a}_{b}+2*r^{a}_{gb})*(r_{efa}+r_{fae}+6*f_{aef}+2*r_{aef})+100*(r^{aif}+4*f^{ifa}+2*r^{fai}+3*r^{ifa})*k^{e}*k_{d}*(-4*f^{gh}_{fb}+6*r^{h}_{b}^{g}_{f})*(-6*f_{qhag}+4*r_{ghqa})*(-7*f_{ecpi}+3*r_{icep})+230000*(2*f^{fg}_{a}+2*r^{g}_{a}^{f}+3*r^{fg}_{a}+3*r_{a}^{fg})*k_{q}*k_{e}*g_{gb}*g_{fc}*(5*f^{a}_{d}^{e}_{p}+5*r^{a}_{pd}^{e})+550000*(3*r_{ch}^{h}+7*f^{h}_{ch})*k_{a}*k_{p}*g^{fa}*d^{g}_{b}*(-5*f_{dgqf}+5*r_{gdfq})+10000*k_{a}*k_{p}*k^{f}*g_{fc}*(3*f^{e}_{db}^{g}+7*r^{e}_{bd}^{g})*(5*f^{a}_{eqg}+5*r_{ge}^{a}_{q})");
        Tensor b = parse("-51*(r_{d}^{a}_{e}+5*f_{d}^{a}_{e}+2*r^{a}_{ed}+2*r_{ed}^{a})*(r^{k}_{kb}+3*r_{b}^{k}_{k}-3*f_{bk}^{k}+3*r_{kb}^{k})*(7*f_{jpc}^{j}+3*r_{jc}^{j}_{p})*(r^{i}_{qg}+r_{g}^{i}_{q}+4*f^{i}_{qg}+4*r_{qg}^{i})*(-3*f^{e}_{ia}^{g}+7*r_{a}^{ge}_{i})-18*(r_{dbp}-4*f_{dpb}+2*r_{bpd}+3*r_{pdb})*(r_{ef}^{a}-5*f_{fe}^{a}+4*r^{a}_{ef})*(5*f_{k}^{eki}+5*r^{e}_{k}^{ki})*(7*f_{i}^{j}_{cj}+3*r_{jic}^{j})*(f_{q}^{f}_{a}+2*r^{f}_{aq}+3*r_{aq}^{f}+4*r_{q}^{f}_{a})+100000*k_{e}*k^{e}*k_{p}*k_{d}*g_{qb}*(r^{f}_{fc}+5*r_{c}^{f}_{f}+2*f_{c}^{f}_{f}+2*r_{fc}^{f})-78000*(r^{a}_{gf}+3*f_{f}^{a}_{g}+2*r_{gf}^{a}+4*r_{f}^{a}_{g})*k_{c}*g_{he}*d^{h}_{a}*(r^{ge}_{p}+7*f_{p}^{ge}+2*r_{p}^{ge})*(2*f_{q}^{f}_{bd}+8*r_{qb}^{f}_{d})-42*(-6*f_{hc}^{e}+2*r_{h}^{e}_{c}+2*r^{e}_{ch})*(-4*f_{k}^{k}_{ad}+6*r_{a}^{k}_{kd})*(5*f_{j}^{g}_{pe}+5*r_{pej}^{g})*(7*f^{fh}_{gb}+3*r^{h}_{gb}^{f})*(-8*f_{qf}^{ja}+2*r_{qf}^{aj})+1000*(r_{qec}+5*f_{ecq}+2*r_{cqe}+2*r_{ecq})*k_{g}*k_{f}*g_{dp}*(r^{g}_{ba}+r_{ba}^{g}+6*f_{ba}^{g}+2*r_{a}^{g}_{b})*(r^{efa}+r^{fae}+6*f^{aef}+2*r^{aef})+100*(r_{aif}+4*f_{aif}+2*r_{fai}+3*r_{ifa})*k_{e}*k_{d}*(-4*f_{bg}^{f}_{h}+6*r_{h}^{f}_{bg})*(6*f_{q}^{gah}+4*r^{gh}_{q}^{a})*(-7*f_{pc}^{ie}+3*r_{pc}^{ei})+230000*(-2*f_{gf}^{a}+2*r_{g}^{a}_{f}+3*r_{fg}^{a}+3*r^{a}_{fg})*k_{q}*k^{e}*d_{b}^{g}*d^{f}_{c}*(-5*f_{epda}+5*r_{dape})+550000*(3*r_{ch}^{h}-7*f_{hc}^{h})*k^{a}*k_{p}*g_{fa}*g_{gb}*(-5*f^{gf}_{qd}+5*r^{g}_{d}^{f}_{q})+10000*k^{a}*k_{p}*k_{f}*d^{f}_{c}*(3*f_{gebd}+7*r_{dbeg})*(-5*f^{g}_{a}^{e}_{q}+5*r_{a}^{eg}_{q})");

        Assert.assertEquals(iHash(a), iHash(b));
        Assert.assertTrue(TensorUtils.equals(a, b));
    }

    @Test
    public void test15a() throws Exception {
        CC.resetTensorNames(123);
        setSymmetric("r_abcd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("f_abcd");

        Product a = (Product) parse("-18*(4*f_{bpd}+2*r_{bpd}+3*r_{pdb}+r_{dbp})*(5*f^{f}_{a}^{e}+4*r_{a}^{ef}+r^{ef}_{a})*(3*r_{c}^{ji}_{j}-7*f^{j}_{cj}^{i})*(5*r^{k}_{eki}+5*f_{e}^{k}_{ki})*(-f_{q}^{a}_{f}+2*r_{f}^{a}_{q}+3*r^{a}_{qf}+4*r_{qf}^{a})");
        Product b = (Product) parse("-18*(-4*f_{dpb}+2*r_{bpd}+3*r_{pdb}+r_{dbp})*(-5*f_{fe}^{a}+4*r^{a}_{ef}+r_{ef}^{a})*(3*r_{jic}^{j}+7*f_{i}^{j}_{cj})*(5*r^{e}_{k}^{ki}+5*f_{k}^{eki})*(f_{q}^{f}_{a}+2*r^{f}_{aq}+3*r_{aq}^{f}+4*r_{q}^{f}_{a})");
        Assert.assertEquals(iHash(a), iHash(b));
        Assert.assertTrue(TensorUtils.equals(a, b));
    }

    @Test
    public void test15b() throws Exception {
        CC.resetTensorNames(123);
        setSymmetric("r_abcd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("f_abcd");


        Tensor a = parse("-f_{q}^{a}_{f}+2*r_{f}^{a}_{q}+3*r^{a}_{qf}+4*r_{qf}^{a}");
        Tensor b = parse("f_{q}^{f}_{a}+2*r^{f}_{aq}+3*r_{aq}^{f}+4*r_{q}^{f}_{a}");
        Indices indices = ParserIndices.parseSimple("_{bcdpq}");
        Assert.assertEquals(iHash(a, indices), iHash(b, indices));
    }

    @Test
    public void test15c() throws Exception {
        CC.resetTensorNames(123);
        setSymmetric("r_abcd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("f_abcd");


        Tensor a = parse("-f_{q}^{a}_{f}");
        Tensor b = parse("f_{q}^{f}_{a}");

        Indices indices = ParserIndices.parseSimple("_{bcdpq}");
        Assert.assertEquals(iHash(a, indices), iHash(b, indices));
    }

    @Test
    public void test16() throws Exception {
        Assert.assertTrue(iGraphHash(parseSimple("f_cd^cd")) != iGraphHash(parseSimple("f_cd^dc")));
    }

    @Test
    public void test17() throws Exception {
        setSymmetric("t_ab");
        setSymmetric("t_abcd");

        final RandomGenerator rnd = CC.getRandomGenerator();
        final RandomTensor rndt = new RandomTensor();
        rndt.clearNamespace();
        rndt.addToNamespace(parse("t_a"));
        rndt.addToNamespace(parse("t_ab"));
        rndt.addToNamespace(parse("t_abc"));
        rndt.addToNamespace(parse("t_abcd"));

        for (int k = 0; k < 100; ++k) {
            Product p = (Product) rndt.nextProduct(10, ParserIndices.parseSimple("_ab"));

            final int size = p.sizeOfDataPart();
            int[] hashCodes = new int[size];
            int[] iHashCodes = new int[size];

            for (int i = 0; i < size; i++) {
                hashCodes[i] = rnd.nextInt();
                iHashCodes[i] = rnd.nextInt();
            }

            final int[] tIHashCodes = iHashCodes.clone();
            Product.iRefine(hashCodes.clone(), tIHashCodes, p.getContent().getStructureOfContractions().contractions, p.data);

            final int[] tIHashCodes2 = iHashCodes.clone();
            Product.iRefineIHashCodesOnly(hashCodes.clone(), tIHashCodes2, p.getContent().getStructureOfContractions().contractions, p.data);

            Assert.assertArrayEquals(tIHashCodes, tIHashCodes2);
        }
    }

    @Test
    public void test18() throws Exception {
        Tensor a = parse("g_ab*g_cd");
        Tensor b = parse("g_ac*g_bd");
        Assert.assertTrue(iHash(a) != iHash(b));
    }

    @Test
    public void test19() throws Exception {
        Assert.assertTrue(iHash(parseSimple("f_a[x,y]")) != iHash(parseSimple("f_a[p,q]")));
    }

    @Test
    public void testRandom1() throws Exception {
        assertAllSame(generateListOfSameTensors(parse("f_abc*(f^apq + r^apq)*f^b_pr*f^rc_q"), its(10, 100)));

        CC.reset();
        setSymmetric("f_abc");
        assertAllSame(generateListOfSameTensors(parse("f_abc*(f^apq + r^apq)*f^b_pr*f^rc_q"), its(10, 100)));

        CC.reset();
        setSymmetric("f_abc");
        assertAllSame(generateListOfSameTensors(parse("f_abc*(f^apq + r^apq)*f^b_pr*f^rc_q"), its(10, 100)));

        CC.reset();
        setSymmetric("f_abc"); setSymmetric("r_abc");
        assertAllSame(generateListOfSameTensors(parse("f_abc*(f^apq + r^apq)*f^b_pr*f^rc_q"), its(10, 100)));
    }

    @Test
    public void testRandom2() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), Matrix1);

        assertAllSame(generateListOfSameTensors(parse("G_a*G_b*G_c*G_d*G_e*G_h"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("G5*G_a*G_b*G5*G_c*G_d*G_e*G_h"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("Tr[G_a*G_b*G_c*G_d*G_e*G_h]"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("Tr[G5*G_a*G_b*G5*G_c*G_d*G_e*G_h]"), its(10, 100)));

        assertAllSame(generateListOfSameTensors(parse("G_a*G_b*G^a*G_d*G^b*G^d"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("G5*G_a*G_b*G5*G^a*G_d*G^b*G_h"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("Tr[G_a*G_b*G_c*G_d*G^b*G^a]"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("Tr[G5*G_a*G_b*G5*G^a*G_d*G_e*G_h]"), its(10, 100)));
    }

    @Test
    public void testRandom3() throws Exception {
        assertAllSame(generateListOfSameTensors(parse("2*x**2*(f_abc - 2*f_bca + f_ace*f^e_br*f^ri_i)*(f^apq + r^apq)*f^b_pr*(-f^rc_q+f^cr_q)"), its(10, 100)));
        assertAllSame(generateListOfSameTensors(parse("2*x**2*(f_abc - 2*f_bca + (f_ace - 21*f_eca)*f^e_br*(f^ri_i + 22*f^ir_i))*(q*f^apq + r^apq)*f^b_pr*(-f^rc_q+f^cr_q + q*f^cr_q*f_xyz*f^xyz - q*f^rc_q*2*f_xyz*f^yxz)"), its(10, 100)));

        CC.reset();
        setAntiSymmetric("r_abc");
        setAntiSymmetric("f_abc");
        assertAllSame(generateListOfSameTensors(parse("2*x**2*(f_abc - 2*r_bca + (f_ace - 21*f_eca)*f^e_br*(f^ri_i + 22*f^ir_i))*(q*f^apq + r^apq)*f^b_pr*(-f^rc_q+f^cr_q + q*f^cr_q*f_xyz*f^xyz - q*f^rc_q*2*f_xyz*f^yxz)"), its(10, 100)));
    }

    @Test
    public void testRandomFull() throws Exception {
        doRandomTest(100, 10);
    }

    @Test
    @LongTest
    public void testRandomFull_long() throws Exception {
        doRandomTest(1000, 10);
        doRandomTest(100, 100);
    }

    @Test
    public void testRandomFull_sym() throws Exception {
        setSymmetric("r_abcd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("f_abcd");
        doRandomTest(100, 10);
    }

    @Test
    @LongTest
    public void testRandomFull_sym_long() throws Exception {
        setSymmetric("r_abcd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("f_abcd");
        doRandomTest(1000, 10);
        doRandomTest(100, 100);
    }

    @Test
    public void testRandom4_RandomMatrices() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), Matrix1);

        for (int pSize : new int[]{5, 8, 10, 13})
            for (int iSize : new int[]{0, 1, 2, 3, 4})
                for (boolean doTrace : new boolean[]{true, false})
                    for (int k = 0; k < its(5, 30); k++)
                        assertAllSame(generateListOfSameTensors(randomMatrixProduct(pSize, iSize, doTrace), its(5, 30)));
    }

    static void doRandomTest(int longTestTries, int longTestRTs) {
        RandomTensor rnd = new RandomTensor();
        rnd.clearNamespace();
        rnd.addToNamespace(parse("k_a"));
        rnd.addToNamespace(parse("g_ab"));
        rnd.addToNamespace(parse("f_abc"));
        rnd.addToNamespace(parse("r_abc"));
        rnd.addToNamespace(parse("f_abcd"));
        rnd.addToNamespace(parse("r_abcd"));

        for (int i = 0; i < its(10, longTestRTs); i++) {
            Tensor t = rnd.nextTensorTree(3, 5, 10, ParserIndices.parseSimple(""));
            List<Tensor> list = generateListOfSameTensors(t, its(10, longTestTries));
            assertAllSame(list);
        }

        for (int i = 0; i < its(10, longTestRTs); i++) {
            Tensor t = rnd.nextTensorTree(3, 5, 10, ParserIndices.parseSimple("ab"));
            List<Tensor> list = generateListOfSameTensors(t, its(10, longTestTries));
            assertAllSame(list);
        }

        for (int i = 0; i < its(10, longTestRTs); i++) {
            Tensor t = rnd.nextTensorTree(3, 5, 10, ParserIndices.parseSimple("abcdpq"));
            List<Tensor> list = generateListOfSameTensors(t, its(10, longTestTries));
            assertAllSame(list);
        }
    }

    static void assertAllSame(final List<Tensor> list) {
        int hash = list.get(0).hashCode(), iHash = iHash(list.get(0));
        for (Tensor tensor : list) {
            try {
                Assert.assertEquals(hash, tensor.hashCode());
                Assert.assertEquals(iHash, iHash(tensor));
            } catch (AssertionError err) {
                System.out.println(tensor);
                System.out.println("\n");
                System.out.println(list.get(0));
                throw err;
            }
        }
//        if (list.get(0) instanceof Product)
//            try {
//                TAssert.assertEquals(multiply(new Complex(list.size()), list.get(0)), sum(list));
//            } catch (AssertionError err) {
//                System.out.println(multiply(new Complex(list.size()), list.get(0)));
//                System.out.println("\n");
//                System.out.println(sum(list));
//                throw err;
//            }
    }

    static List<Tensor> generateListOfSameTensors(Tensor tensor, int size) {
        List<Tensor> tensors = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            tensor = ApplyIndexMapping.renameDummy(tensor, tensor.getIndices().getNamesOfDummies());
            int[] dummies = getAllDummyIndicesT(tensor).toArray();
            IntArrayList latin = new IntArrayList();
            for (int dummy : dummies) {
                if (IndicesUtils.getTypeEnum(dummy) == LatinLower)
                    latin.add(dummy);
            }
            dummies = latin.toArray();
            TIntHashSet seen = new TIntHashSet();
            while (seen.size() < 4 && seen.size() < dummies.length) {
                int name = CC.getRandomGenerator().nextInt(dummies.length);
                seen.add(dummies[name]);
            }
            SimpleIndices invert = IndicesFactory.createSimple(null, seen.toArray());
            tensors.add(shuffle(tensor, new Mapping(invert, invert.getInverted())));
        }
        return tensors;
    }


    static Tensor shuffle(Tensor tensor, Mapping invertDummies) {
        if (tensor instanceof MultiTensor) {
            List<Tensor> arr = new ArrayList<>();
            for (int i = 0; i < tensor.size(); i++)
                arr.add(shuffle(tensor.get(i), invertDummies));
            Tensor[] data = arr.toArray(new Tensor[arr.size()]);
            Permutations.shuffle(data, CC.getRandomGenerator());
            return tensor.getFactory().create(data);
        }
        if (tensor instanceof SimpleTensor) {
            SimpleTensor st = (SimpleTensor) tensor;
            SimpleIndices indices = st.getIndices();
            Permutation p = indices.getSymmetries().getPermutationGroup().randomElement();
            Tensor tr = invertDummies.transform(simpleTensor(st.getName(), IndicesFactory.createSimple(null, p.permute(indices.toArray()))));
            if (p.antisymmetry())
                tr = negate(tr);
            return tr;
        }
        return tensor;
    }

    static Tensor randomMatrixProduct(int pSize, int indices, final boolean doTrace) {
        if (pSize % 2 == 0 && indices % 2 != 0)
            indices = indices - 1;
        if (pSize % 2 != 0 && indices % 2 == 0)
            indices = indices + 1;

        StringBuilder sb = new StringBuilder();
        if (doTrace)
            sb.append("Tr[");
        for (int i = 0; ; i++) {
            sb.append("G").append(IndicesUtils.toString(i));
            if (i == pSize - 1)
                break;
            sb.append("*");
        }
        if (doTrace)
            sb.append("]");
        Tensor line = parse(sb.toString());

        TIntHashSet done = new TIntHashSet();
        while (line.getIndices().getOfType(LatinLower).getFree().size() != indices) {
            int from = CC.getRandomGenerator().nextInt(pSize);
            int to = CC.getRandomGenerator().nextInt(pSize);
            if (from != to && !done.contains(from) && !done.contains(to)) {
                line = new Mapping(
                        new int[]{from},
                        new int[]{inverseIndexState(to)})
                        .transform(line);
                done.add(from); done.add(to);
            }
        }
        int[] free = line.getIndices().getOfType(LatinLower).getFree().toArray();
        Permutations.shuffle(free);
        return new Mapping(free,
                createAlphabetical(LatinLower, free.length).toArray()).transform(line);
    }
}
