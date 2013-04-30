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
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

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

    private static void assertCollectExpand(Tensor t, SimpleTensor[] patterns) {
        t = ExpandTransformation.expand(t);
        t = EliminateMetricsTransformation.eliminate(t);
        CollectTransformation collect = new CollectTransformation(patterns);
        Tensor collected = collect.transform(t);
        collected = ExpandTransformation.expand(collected);
        collected = EliminateMetricsTransformation.eliminate(collected);

        TAssert.assertEquals(collected, t);
    }

}
