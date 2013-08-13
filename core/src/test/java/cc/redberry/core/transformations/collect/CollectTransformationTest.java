/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.transformations.collect;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.factor.FactorTransformation;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class CollectTransformationTest {
    @Test
    public void test1() {
        SimpleTensor[] simpleTensors = {parseSimple("a")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("a*b + a*c");
        TAssert.assertEquals(ct.transform(t), "a*(b+c)");
    }

    @Test
    public void test2() {
        SimpleTensor[] simpleTensors = {parseSimple("a"), parseSimple("b")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("a*b + a*c + a*d + b*e + b*r");
        TAssert.assertEquals(ct.transform(t), "a*b + a*(c+d) + b*(e+r)");
    }

    @Ignore
    @Test
    public void test3() {
        SimpleTensor[] simpleTensors = {parseSimple("A_m")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_m*B_n + A_m*C_n");
        TAssert.assertEquals(ct.transform(t), "A_m*(B_n + C_n)");
    }


    @Test
    public void test4() {
        SimpleTensor[] simpleTensors = {parseSimple("A_m")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_m*B_n + A_n*C_m");
        TAssert.assertEquals(ct.transform(t), "A_i*(d^i_m*B_n + d^i_n*C_m)");
    }

    @Test
    public void test5() {
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A_nq*C_m^q");
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^i_n*C_m^q)");
    }

    @Test
    public void test6() {
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A_nq*C_m^q");
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^i_n*C_m^q)");
    }

    @Test
    public void test7() {
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A_qn*C_m^q");
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^q_n*C_m^i)");
    }

    @Test
    public void test8() {
        CC.resetTensorNames(8816281755326274707L);
        SimpleTensor[] simpleTensors = {parseSimple("A_mn")};
        CollectTransformation ct = new CollectTransformation(simpleTensors);

        Tensor t = parse("A_mq*B_n^q + A^q_n*C_mq");
        System.out.println(t);
        TAssert.assertEquals(ct.transform(t), "A_iq*(d^i_m*B_n^q + d^q_n*C_m^i)");
    }


    @Test
    public void test9() {
        SimpleTensor[] patterns;
        Tensor t;

        patterns = new SimpleTensor[]{parseSimple("A_mnpq")};
        t = parse("A_mnpq*B^np_ac + A_abcd*B^ndb_nmq");
        assertCollectExpand(t, patterns);

        t = parse("A_mnpq*B^np_ac + A_acmq ");
        assertCollectExpand(t, patterns);

        t = parse("A_mnpq*B^np_ac + A_abcd*B^ndb_nmq + A_acmq + A_amqc + A_rsmq*C^rs_ac");
        assertCollectExpand(t, patterns);
    }


    @Test
    public void test10() {
        SimpleTensor[] patterns;
        Tensor t;

        //Riemann with diff states
        t = parse("g_{mn}*R^{mn}");
        t = Tensors.parseExpression("R_{mn}=g^ab*R_{bman}").transform(t);
        t = Tensors.parseExpression("R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm").transform(t);
        t = Tensors.parseExpression("G_gmn=(1/2)*(p_m*h_gn+p_n*h_gm-p_g*h_mn)").transform(t);

        patterns = new SimpleTensor[]{parseSimple("h_ab")};
        assertCollectExpand(t, patterns);
        patterns = new SimpleTensor[]{parseSimple("p_a")};
        assertCollectExpand(t, patterns);
        patterns = new SimpleTensor[]{parseSimple("p_a"), parseSimple("h_ab")};
        assertCollectExpand(t, patterns);
    }

    @Test
    public void test11() {
        SimpleTensor[] patterns;
        Tensor t;

        t = parse("Rf[h_mn]");
        t = Tensors.parseExpression("Rf[g_ab]=g^ab*Rf_ab[g_mn]").transform(t);
        t = Tensors.parseExpression("Rf_{mn}[g^mn]=Rf^{a}_{man}[g_pq]").transform(t);
        t = Tensors.parseExpression("Rf^a_bmn[g^pq]=p_m*Gf^a_bn[g_ab]+p_n*Gf^a_bm[g_ab]+Gf^a_gm[g_ab]*Gf^g_bn[g_ab]-Gf^a_gn[g_ab]*Gf^g_bm[g_ab]").transform(t);
        t = Tensors.parseExpression("Gf^a_mn[r^mn]=(1/2)*r^ag*(p_m*r_gn[x_a]+p_n*r_gm[x_z]-p_g*r_mn[x_z])").transform(t);

        patterns = new SimpleTensor[]{parseSimple("r_ab[x_a]")};
        assertCollectExpand(t, patterns);
        patterns = new SimpleTensor[]{parseSimple("p_a")};
        assertCollectExpand(t, patterns);
        patterns = new SimpleTensor[]{parseSimple("p_a"), parseSimple("r_ab[x_a]")};
        assertCollectExpand(t, patterns);
    }

    @Test
    public void test12() {
//        CC.resetTensorNames(3679148909490820491L);
        SimpleTensor[] patterns;
        Tensor t;

        t = parse("sqrt*(g^ab*Ric_ab+(e1*g_ab*G^xp*G^yq+e2*E^x_a*E^p_b*G^yq+e3*E^x_b*E^p_a*G^yq)*T^a_xy*T^b_pq+e6*Ric_ab*Ric_cd*g^ab*g^cd+e5*Ric_ab*Ric_cd*g^ac*g^bd+Gf_a*Gf_b*g^ab)+f*g^pq*g_ab*i*h^b_q*p_p*g^cd*I*h^a_d*p_c");
        /*Ric*/
        t = parseExpression("Ric_ab=E^r_a*E^d_c*R^c_bdr").transform(t);
        /*Riman*/
        t = parseExpression("R^a_bcd=i*w^a_db*p_c-i*w^a_cb*p_d+w^a_cr*w^r_db-w^a_dr*w^r_cb").transform(t);
        /*Torsion*/
        t = parseExpression("T^a_bc=i*h^a_c*p_b-i*h^a_b*p_c+w^a_bd*e^d_c-w^a_cd*e^d_b").transform(t);
        /*eTetrad*/
        t = parseExpression("e^a_b=d^a_b+h^a_b").transform(t);
        /*ETetrad*/
        t = parseExpression("E^a_b=d^a_b-h^a_b+h^a_c*h^c_b").transform(t);
        /*metricUP*/
        t = parseExpression("G^ab=g^ab-g^ca*h^b_c-g^cb*h^a_c+g^cb*h^a_d*h^d_c+g^ca*h^b_d*h^d_c+g^cd*h^a_c*h^b_d").transform(t);
        /*sqrt*/
        t = parseExpression("sqrt=1+h^a_a+(1/2)*(h^s_s*h^l_l-h^s_l*h^l_s)").transform(t);
        /*tetradGaugeFix*/
        t = parseExpression("Gf_a=f1*h^b_a*p_b+f2*g^pq*g_ab*h^b_q*p_p+f3*h^q_q*p_a").transform(t);

        patterns = new SimpleTensor[]{parseSimple("h^a_b"), parseSimple("w^a_bc")};
        assertCollectExpand(t, patterns);
    }

    @Test
    public void test13() {
        SimpleTensor[] patterns;
        Tensor t;
        t = parse("a*f[x]*f[-x] + b*f[x]*f[-x] + x*f[x]*f[y] + y*f[y]*f[x]");
        patterns = new SimpleTensor[]{parseSimple("f[x]")};
        CollectTransformation collect = new CollectTransformation(patterns);
        TAssert.assertEquals(collect.transform(t), "(y+x)*f[x]*f[y]+(a+b)*f[-x]*f[x]");
    }

    private static void assertCollectExpand(Tensor t, SimpleTensor[] patterns) {
        t = ExpandTransformation.expand(t);
        t = EliminateMetricsTransformation.eliminate(t);
        CollectTransformation collect = new CollectTransformation(patterns);
        Tensor collected = collect.transform(t);
        if (collected instanceof Sum)
            for (Tensor summand : collected)
                assertCollectedSummand(summand, patterns);
        else assertCollectedSummand(collected, patterns);

        collected = ExpandTransformation.expand(collected, EliminateMetricsTransformation.ELIMINATE_METRICS);
        collected = EliminateMetricsTransformation.eliminate(collected);
        TAssert.assertEquals(collected, t);
    }

    private static void assertCollectedSummand(Tensor summand, SimpleTensor[] patterns) {
        if (!(summand instanceof Product)) return;

        for (Tensor t : summand)
            if (t instanceof Sum) {
                FromChildToParentIterator it = new FromChildToParentIterator(t);
                Tensor c;
                while ((c = it.next()) != null)
                    if (c instanceof SimpleTensor)
                        for (SimpleTensor p : patterns)
                            Assert.assertFalse(((SimpleTensor) c).getName() == p.getName());
            }
    }

    @Test
    public void testMatch() {
        for (int i = 0; i < 100; ++i) {
            Random rnd = new Random();
            CC.resetTensorNames();
            SimpleTensor[] a = {parseSimple("f_a[-x_a-y_a]"), parseSimple("f_c[x_b]"), parseSimple("f_d[y_d]"),
                    parseSimple("g[x]"), parseSimple("g[-f-x]"), parseSimple("g[f]")};
            SimpleTensor[] b = a.clone();
            Combinatorics.shuffle(b, rnd);
            Arrays.sort(a);
            Arrays.sort(b);
            int[] match = CollectTransformation.matchFactors(a, b);
            Assert.assertArrayEquals(a, Combinatorics.reorder(b, match));
        }
    }

    @Test
    public void testDerivatives1() {
        Tensor t = parse("f~(1)[x] + f[x]");
        SimpleTensor[] patterns = new SimpleTensor[]{parseSimple("f[x]")};
        CollectTransformation collect = new CollectTransformation(patterns);
        TAssert.assertEquals(collect.transform(t), t);
    }

    @Test
    public void testDerivatives2() {
        Tensor t = parse("D[x][x*f[x, x**2] + f[x, x**2]]");
        SimpleTensor[] patterns = {
                parseSimple("f~(0,1)[x, y]"),
                parseSimple("f~(1,0)[x, y]")};
        CollectTransformation collect = new CollectTransformation(patterns, new Transformation[]{FactorTransformation.FACTOR});
        TAssert.assertEquals(collect.transform(t), "f[x,x**2]+(x+1)*f~(1,0)[x,x**2]+2*x*(x+1)*f~(0,1)[x,x**2]");
    }

    @Test
    public void testPower1() {
        Tensor t = parse("x**2 + x**2*a");
        SimpleTensor[] pattern = {parseSimple("x")};
        CollectTransformation tr = new CollectTransformation(pattern);
        TAssert.assertEquals(tr.transform(t), "x**2*(1+a)");
    }

    @Test
    public void testPower2() {
        Tensor t = parse("y**3*x**2*b*c + y**3*x**2*a**2");
        SimpleTensor[] pattern = {parseSimple("x"), parseSimple("y")};
        CollectTransformation tr = new CollectTransformation(pattern);
        TAssert.assertEquals(tr.transform(t), "y**3*x**2*(b*c+a**2)");
    }


    @Test
    public void testPower3() {
        Tensor t = parse("(A_m*A^m*c)**2 + A_m*A^m*A_i*A^i");
        SimpleTensor[] pattern = {parseSimple("A_m")};
        CollectTransformation tr = new CollectTransformation(pattern);
        TAssert.assertEquals(EliminateMetricsTransformation.eliminate(tr.transform(t)), "A_m*A^m*A_i*A^i*(c**2 + 1)");
    }

    @Test
    public void testPower4() {
        Tensor t = parse("x**2*y**3*(a + b + c) + x*y*(c + d) + x*(a+b) + y*(c+e) + r");
        SimpleTensor[] pattern = {parseSimple("x"), parseSimple("y")};
        CollectTransformation tr = new CollectTransformation(pattern);
        TAssert.assertEquals(tr.transform(ExpandTransformation.expand(t)), t);
    }

    @Test
    public void testPower5() {
        Tensor t = parse("x_m*y_n*x_a*(a^a + b^a + c^a) + x_m*y_n*(c + d) + x_m*(a_n+b_n) + y_n*(c_m+e_m) + r_mn");
        SimpleTensor[] pattern = {parseSimple("x_m"), parseSimple("y_m")};
        CollectTransformation tr = new CollectTransformation(pattern);
        Tensor e = EliminateMetricsTransformation.eliminate(tr.transform(ExpandTransformation.expand(t)));
        e = ExpandTransformation.expand(e);
        e = EliminateMetricsTransformation.eliminate(e);
        TAssert.assertEquals(e, ExpandTransformation.expand(t));
    }

}
