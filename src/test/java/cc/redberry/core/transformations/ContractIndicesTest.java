/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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

package cc.redberry.core.transformations;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.ProductBuilder;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContractIndicesTest {

    private static Tensor contract(String tensor) {
        return contract(parse(tensor));
    }

    private static Tensor contract(Tensor tensor) {
        return ContractIndices.ContractIndices.transform(tensor);
    }

    @Test
    public void test01() {
        Tensor t = contract("g_mn*A^mn");
        Tensor e = parse("A^n_n");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test02() {
        Tensor t = contract("d^n_m*A^m_n");
        Tensor e = parse("A^n_n");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test03() {
        Tensor t = contract("d_m^n*A^m_n");
        Tensor e = parse("A^n_n");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test04() {
        Tensor t = contract("d_m^n*d^m_n");
        Tensor e = parse("d^n_n");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test05() {
        Tensor t = contract("g_mn*g^mn");
        Tensor e = parse("d^n_n");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test051() {
        Tensor t = contract("g_\\mu\\nu*g^\\mu\\nu");
        Tensor e = parse("d^\\mu_\\mu");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test06() {
        Tensor t = contract("2*a*g_mn*g^mn");
        Tensor e = parse("2*a*d^n_n");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test07() {
        Tensor t = contract("B^ma*g_mn*A^nb");
        Tensor e = parse("B^ma*A_m^b");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test08() {
        Tensor t = contract("B^ma*d_m^n*A_n^b");
        Tensor e = parse("B^ma*A_m^b");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test09() {
        Tensor t = contract("g^mx*g_xa");
        Tensor e = parse("d^m_a");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test010() {
        Tensor t = contract("d^m_x*g^xa");
        Tensor e = parse("g^ma");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test011() {
        Tensor t = contract("d^m_x*d^x_a");
        Tensor e = parse("d^m_a");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test012() {
        Tensor t;
        t = contract("g_mn*g^na*g_ab");
        Tensor e = parse("g_mb");
        TAssert.assertEquals(t, e);
        t = contract("g^na*g_mn*g_ab");
        TAssert.assertEquals(t, e);
        t = contract("g^na*g_ab*g_mn");
        TAssert.assertEquals(t, e);
        t = contract("g_ab*g^na*g_mn");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test013() {
        Tensor t = contract("g_mn*g^mn*g_ab*g^ab");
        Tensor e = parse("d_m^m*d_a^a");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void test014() {
        Tensor t = contract("g_mn*g^ma*g_ab*g^bn");
        System.out.println(t);
        Tensor e = parse("d_m^m");
        TAssert.assertEquals(t, e);
    }

    @Test
    public void testProduct1() {
        Tensor t = parse("g_mn*F^n*k");
        t = contract(t);
        System.out.println(t);
        Tensor expected = parse("F_m*k");
        assertTrue(TensorUtils.equalsExactly(t, expected));
    }

    @Test
    public void testProduct2() {
        Tensor t = parse("g_mn*F^n");
        t = contract(t);
        Tensor expected = parse("F_m");
        assertTrue(TensorUtils.equalsExactly(t, expected));
    }

    @Test
    public void testProduct3() {
        Tensor t = parse("g_mn*g_ab*F^n*F^m*F^ab");
        t = contract(t);
        Tensor expected = parse("F^n*F_n*F^a_a");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct4() {
        Tensor t = parse("F^n*F^m*F^ab");
        t = contract(t);
        Tensor expected = parse("F^n*F^m*F^ab");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct6() {
        Tensor t = parse("g^mc*g_am");
        t = contract(t);
        Tensor expected = parse("d_a^c");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct7() {
        Tensor t = parse("g_ab*F^ab");
        t = contract(t);
        Tensor expected = parse("F^a_a");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct8() {
        Tensor t = parse("g_ab*g^bc*(d_c^f*F_f+g_cd*g^de*X_e+g_cj*d^j_k*(X^k+X_l*g^lk))");
        t = contract(t);
        Tensor expected = parse("F_{a}+X_{a}+X_{a}+X_{a}");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct9() {
        Tensor t = parse("g^mn*g^ab*g^gd*(p_g*g_ba+p_a*g_bg)*(p_m*g_dn+p_n*g_dm)");
        //                (p^d*d^b_b+p_g)*(p_d+p_d)
        t = contract(t);

        Tensor expected = parse("(p^{d}*d^{b}_{b}+p^{d})*(p_{d}+p_{d})");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct10() {
        Tensor t = parse("g^ab*g^gd*(p_g*g_ba+p_a*g_bg)");
        t = contract(t);
        Tensor expected = parse("p^{d}*d^{b}_{b}+p^{d}");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testProduct11() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = parse("g_bg*(p^g*g^ba*p_a+p_a*g^ab*g^gd*p_d)");
            t = contract(t);
            Tensor expected = parse("p^{a}*p_{a}+p^{d}*p_{d}");
            assertTrue(TensorUtils.equals(t, expected));
        }
    }

    @Test
    public void testSum1() {
        Tensor t = parse("g_mn*g_ab*(F^n*F^m*F^ab+F^n*F^m*F^ab)");
        t = contract(t);
        Tensor expected = parse("F^n*F_n*F^a_a+F^r*F_r*F_x^x");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum2() {
        Tensor t = parse("(F^n*F^m*F^ab+F^n*F^m*F^ab)*X_b");
        t = contract(t);
        Tensor expected = parse("(F^n*F^m*F^ab+F^n*F^m*F^ab)*X_b");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum3() {
        Tensor t = parse("g_mn*(F^m_b+g_ab*(F^am+g_xy*F^xyam))");
        t = contract(t);
        System.out.println(t);
        Tensor expected = parse("F_{nb}+F_{bn}+F_{y}^{y}_{bn}");
        System.out.println(expected);
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum4() {
        Tensor t = parse("g^nb*A_nb+g^nb*g_mn*(F^m_b+g_ab*(F^am+g_xy*F^xyam))");
        t = contract(t);
        Tensor expected = parse("A_n^n+F_n^n+F_n^n+F^{x}_{x}_n^n");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum5() {
        Tensor t = parse("A_mn+g_mn*h");
        t = contract(t);
        Tensor expected = parse("A_mn+g_mn*h");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum6() {
        Tensor t = parse("g^mc*(A_mn+g_mn*h)");
        t = contract(t);
        Tensor expected = parse("A^c_n+d^c_n*h");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum7() {
        Tensor t = parse("g^ab*(g_mn*F_zxab^m+g^cd*g_mn*F_zxab^m*K_cd+g_zx*g_ab*X_n)");
        t = contract(t);
        Tensor expected = parse("F_zx^b_bn+F_zx^b_bn*K^d_d+X_n*g_zx*d^b_b");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum8() {
        Tensor t = parse("X_a+g_ab*(X^b+g^bc*(X_c+d_c^f*F_f+g_cd*g^de*X_e+g_cj*d^j_k*(X^k+X_l*g^lk)))");
        t = contract(t);
        Tensor expected = parse("X_{a}+X_{a}+X_{a}+F_{a}+X_{a}+X_{a}+X_{a}");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testSum9() {
        Tensor t = parse("A_mn+g_ma*B^a_n");
        t = contract(t);
        Tensor expected = parse("A_mn+B_mn");
        assertTrue(TensorUtils.equalsExactly(t, expected));
    }

    @Test
    public void testSum10() {
        Tensor t = parse("g^ad*(g_ab*X^b+X_a)");
        t = contract(t);
        Tensor expected = parse("X^{d}+X^{d}");
        assertTrue(TensorUtils.equalsExactly(t, expected));
    }

    @Test
    public void testSum11() {
        Tensor t = parse("g^ed*(g_a*X+X_a)"), v = contract(t);
        assertTrue(t == v);
    }

    @Test
    public void testMK1() {
        Tensor t = parse("g^mn*g_mn");
        t = contract(t);
        Tensor expected = parse("d^n_n");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testMK2() {
        Tensor t = parse("g^ma*g_mn*g_ab*g^bc*d_c^n");
        t = contract(t);
        Tensor expected = parse("d^n_n");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testMK3() {
        Tensor t = parse("d^c_a*d^a_b*d_o^b*g^ox");
        t = contract(t);
        Tensor expected = parse("g^cx");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testMK4() {
        Tensor t = parse("d^c_o*g^ox");
        t = contract(t);
        Tensor expected = parse("g^cx");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testMK5() {
        Tensor t = parse("p^n*d^a_d*g^db");
        t = contract(t);
        Tensor expected = parse("p^n*g^ab");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testMK6() {
        Tensor t = parse("g^ab*(g_am*F_b+d_m^x*Y_xab+X_abm*g_pq*g^pq)");
        t = contract(t);
        System.out.println(t);
        Tensor expected = parse("F_m+Y_m^b_b+X^b_bm*d_q^q");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testK1() {
        Tensor t = parse("d^m_n*d^a_b*(F^nb+d^A_B*(M^B_A*X^n*X^b+M^Bnb_A))");
        t = contract(t);
        Tensor expected = parse("F^{ma}+M^{A}_{A}*X^{m}*X^{a}+M^{Ama}_{A}");
        assertTrue(TensorUtils.equals(t, expected));
    }

//    @Test
//    public void testDerivative1() {
//        Tensor t = parse("g_mn*D[F_ab,x_mp]*d^a_p*g^bq");
//        t = contract(t);
//        Tensor expected = parse("D[F^aq,x^na]");
//        assertTrue(TensorUtils.equals(t, expected));
//    }
    @Test
    public void testGreek1() {
        Tensor t = parse("g_{\\alpha \\beta}*(F^{\\alpha}+g^{\\gamma \\alpha}*U_{\\gamma})");
        t = contract(t);
        Tensor expected = parse("F_{\\beta}+U_{\\beta}");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testGreek2() {
        Tensor t = parse("g^{\\alpha \\beta}*(F_{\\alpha}+g_{\\gamma \\alpha}*U^{\\gamma})");
        t = contract(t);
        Tensor expected = parse("F^{\\beta}+U^{\\beta}");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testGreek3() {
        Tensor t = parse("g^{\\alpha \\beta}*g_{\\beta \\alpha}");
        t = contract(t);
        Tensor expected = parse("d^{\\alpha}_{\\alpha}");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Ignore
    @Test(timeout = 3000L)
    public void performanceTest1() {
        long start, stop;
        start = System.currentTimeMillis();
        Tensor target = parse("g^ca*g^db*(p_g*(1/2)*(p_c*g_id+p_d*g_ic+(-1)*p_i*g_cd)*g^gm*g^in+p_g*(1/2)*g^gi*(p_c*d_i^m*d_d^n+p_d*d_i^m*d_c^n+(-1)*p_i*d_c^m*d_d^n)+p_d*(1/2)*(p_c*g_eg+p_g*g_ec+(-1)*p_e*g_cg)*g^gm*g^en+p_d*(1/2)*g^ge*(p_c*d_e^m*d_g^n+p_g*d_e^m*d_c^n+(-1)*p_e*d_c^m*d_g^n)+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^gm*g^fn+(1/2)*g^gf*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*(p_h*d_f^m*d_g^n+p_g*d_f^m*d_h^n+(-1)*p_f*d_h^m*d_g^n)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^hm*g^kn+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*d_k^m*d_d^n+p_d*d_k^m*d_c^n+(-1)*p_k*d_c^m*d_d^n)+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^gm*g^ln+(-1)*(1/2)*g^gl*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*(p_h*d_l^m*d_d^n+p_d*d_l^m*d_h^n+(-1)*p_l*d_h^m*d_d^n)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^hm*g^on+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*d_o^m*d_g^n+p_g*d_o^m*d_c^n+(-1)*p_o*d_c^m*d_g^n))+(p_g*(1/2)*g^gi*(p_c*g_id+p_d*g_ic+(-1)*p_i*g_cd)+p_d*(1/2)*g^ge*(p_c*g_eg+p_g*g_ec+(-1)*p_e*g_cg)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg))*g^db*g^cm*g^an+(p_g*(1/2)*g^gi*(p_c*g_id+p_d*g_ic+(-1)*p_i*g_cd)+p_d*(1/2)*g^ge*(p_c*g_eg+p_g*g_ec+(-1)*p_e*g_cg)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg))*g^ca*g^dm*g^bn+(p_g*(1/2)*(p_c*g_id+p_d*g_ic+(-1)*p_i*g_cd)*g^ga*g^ib+p_g*(1/2)*g^gi*(p_c*d_i^a*d_d^b+p_d*d_i^a*d_c^b+(-1)*p_i*d_c^a*d_d^b)+p_d*(1/2)*(p_c*g_eg+p_g*g_ec+(-1)*p_e*g_cg)*g^ga*g^eb+p_d*(1/2)*g^ge*(p_c*d_e^a*d_g^b+p_g*d_e^a*d_c^b+(-1)*p_e*d_c^a*d_g^b)+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ga*g^fb+(1/2)*g^gf*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*(p_h*d_f^a*d_g^b+p_g*d_f^a*d_h^b+(-1)*p_f*d_h^a*d_g^b)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ha*g^kb+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*d_k^a*d_d^b+p_d*d_k^a*d_c^b+(-1)*p_k*d_c^a*d_d^b)+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ga*g^lb+(-1)*(1/2)*g^gl*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*(p_h*d_l^a*d_d^b+p_d*d_l^a*d_h^b+(-1)*p_l*d_h^a*d_d^b)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ha*g^ob+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*d_o^a*d_g^b+p_g*d_o^a*d_c^b+(-1)*p_o*d_c^a*d_g^b))*g^cm*g^dn+g^cd*(p_g*(1/2)*g^ga*g^ib*(p_c*d_i^m*d_d^n+p_d*d_i^m*d_c^n+(-1)*p_i*d_c^m*d_d^n)+p_g*(1/2)*(p_c*g_id+p_d*g_ic+(-1)*p_i*g_cd)*g^ib*g^gm*g^an+p_g*(1/2)*(p_c*g_id+p_d*g_ic+(-1)*p_i*g_cd)*g^ga*g^im*g^bn+p_g*(1/2)*(p_c*d_i^a*d_d^b+p_d*d_i^a*d_c^b+(-1)*p_i*d_c^a*d_d^b)*g^gm*g^in+p_d*(1/2)*g^ga*g^eb*(p_c*d_e^m*d_g^n+p_g*d_e^m*d_c^n+(-1)*p_e*d_c^m*d_g^n)+p_d*(1/2)*(p_c*g_eg+p_g*g_ec+(-1)*p_e*g_cg)*g^eb*g^gm*g^an+p_d*(1/2)*(p_c*g_eg+p_g*g_ec+(-1)*p_e*g_cg)*g^ga*g^em*g^bn+p_d*(1/2)*(p_c*d_e^a*d_g^b+p_g*d_e^a*d_c^b+(-1)*p_e*d_c^a*d_g^b)*g^gm*g^en+(1/2)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ga*g^fb*(p_h*d_f^m*d_g^n+p_g*d_f^m*d_h^n+(-1)*p_f*d_h^m*d_g^n)+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ga*g^fb*g^hm*g^kn+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*g^ga*g^fb*(p_c*d_k^m*d_d^n+p_d*d_k^m*d_c^n+(-1)*p_k*d_c^m*d_d^n)+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^fb*g^gm*g^an+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ga*g^fm*g^bn+(1/2)*(1/2)*g^hk*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*(p_h*d_f^a*d_g^b+p_g*d_f^a*d_h^b+(-1)*p_f*d_h^a*d_g^b)*g^gm*g^fn+(1/2)*g^gf*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*(p_h*d_f^a*d_g^b+p_g*d_f^a*d_h^b+(-1)*p_f*d_h^a*d_g^b)*g^hm*g^kn+(1/2)*g^gf*(1/2)*g^hk*(p_h*d_f^a*d_g^b+p_g*d_f^a*d_h^b+(-1)*p_f*d_h^a*d_g^b)*(p_c*d_k^m*d_d^n+p_d*d_k^m*d_c^n+(-1)*p_k*d_c^m*d_d^n)+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ha*g^kb*g^gm*g^fn+(1/2)*g^gf*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ha*g^kb*(p_h*d_f^m*d_g^n+p_g*d_f^m*d_h^n+(-1)*p_f*d_h^m*d_g^n)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^ha*g^kb*(p_c*d_k^m*d_d^n+p_d*d_k^m*d_c^n+(-1)*p_k*d_c^m*d_d^n)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^kb*g^hm*g^an+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*g_kd+p_d*g_kc+(-1)*p_k*g_cd)*g^ha*g^km*g^bn+(1/2)*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*g^hk*(p_c*d_k^a*d_d^b+p_d*d_k^a*d_c^b+(-1)*p_k*d_c^a*d_d^b)*g^gm*g^fn+(1/2)*g^gf*(1/2)*g^hk*(p_c*d_k^a*d_d^b+p_d*d_k^a*d_c^b+(-1)*p_k*d_c^a*d_d^b)*(p_h*d_f^m*d_g^n+p_g*d_f^m*d_h^n+(-1)*p_f*d_h^m*d_g^n)+(1/2)*g^gf*(p_h*g_fg+p_g*g_fh+(-1)*p_f*g_hg)*(1/2)*(p_c*d_k^a*d_d^b+p_d*d_k^a*d_c^b+(-1)*p_k*d_c^a*d_d^b)*g^hm*g^kn+(-1)*(1/2)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ga*g^lb*(p_h*d_l^m*d_d^n+p_d*d_l^m*d_h^n+(-1)*p_l*d_h^m*d_d^n)+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ga*g^lb*g^hm*g^on+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*g^ga*g^lb*(p_c*d_o^m*d_g^n+p_g*d_o^m*d_c^n+(-1)*p_o*d_c^m*d_g^n)+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^lb*g^gm*g^an+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ga*g^lm*g^bn+(-1)*(1/2)*(1/2)*g^ho*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*(p_h*d_l^a*d_d^b+p_d*d_l^a*d_h^b+(-1)*p_l*d_h^a*d_d^b)*g^gm*g^ln+(-1)*(1/2)*g^gl*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*(p_h*d_l^a*d_d^b+p_d*d_l^a*d_h^b+(-1)*p_l*d_h^a*d_d^b)*g^hm*g^on+(-1)*(1/2)*g^gl*(1/2)*g^ho*(p_h*d_l^a*d_d^b+p_d*d_l^a*d_h^b+(-1)*p_l*d_h^a*d_d^b)*(p_c*d_o^m*d_g^n+p_g*d_o^m*d_c^n+(-1)*p_o*d_c^m*d_g^n)+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ha*g^ob*g^gm*g^ln+(-1)*(1/2)*g^gl*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ha*g^ob*(p_h*d_l^m*d_d^n+p_d*d_l^m*d_h^n+(-1)*p_l*d_h^m*d_d^n)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ha*g^ob*(p_c*d_o^m*d_g^n+p_g*d_o^m*d_c^n+(-1)*p_o*d_c^m*d_g^n)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ob*g^hm*g^an+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*g_og+p_g*g_oc+(-1)*p_o*g_cg)*g^ha*g^om*g^bn+(-1)*(1/2)*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*g^ho*(p_c*d_o^a*d_g^b+p_g*d_o^a*d_c^b+(-1)*p_o*d_c^a*d_g^b)*g^gm*g^ln+(-1)*(1/2)*g^gl*(1/2)*g^ho*(p_c*d_o^a*d_g^b+p_g*d_o^a*d_c^b+(-1)*p_o*d_c^a*d_g^b)*(p_h*d_l^m*d_d^n+p_d*d_l^m*d_h^n+(-1)*p_l*d_h^m*d_d^n)+(-1)*(1/2)*g^gl*(p_h*g_ld+p_d*g_lh+(-1)*p_l*g_hd)*(1/2)*(p_c*d_o^a*d_g^b+p_g*d_o^a*d_c^b+(-1)*p_o*d_c^a*d_g^b)*g^hm*g^on)");
        stop = System.currentTimeMillis();
        System.out.println("Parse: " + (stop - start));
        start = System.currentTimeMillis();
        target = Expand.expand(target);
        stop = System.currentTimeMillis();
        System.out.println("Expand: " + (stop - start) + " , size: " + target.size());
        start = System.currentTimeMillis();
        target = contract(target);
        stop = System.currentTimeMillis();
        System.out.println("Contract: " + (stop - start) + " , size: " + target.size());
        assertTrue(target.size() == 19);
    }

    private static Tensor generateContractedMetricSequence(int length) {
        ProductBuilder builder = new ProductBuilder();
        IndexGenerator generator = new IndexGenerator();
        byte type = 0;
        int a = generator.generate(type), b = generator.generate(type);
        for (int i = 0; i < length; ++i) {
            builder.put(Tensors.createMetric(a, b));
            a = IndicesUtils.inverseIndexState(b);
            b = IndicesUtils.setRawState(IndicesUtils.getRawStateInt(a), generator.generate(type));
        }
        return builder.build();
    }

    private static Tensor generateNotContractedMetricSequence(int length) {
        ProductBuilder builder = new ProductBuilder();
        IndexGenerator generator = new IndexGenerator();
        byte type = 0;
        for (int i = 0; i < length; ++i)
            builder.put(Tensors.createMetric(generator.generate(type), generator.generate(type)));
        return builder.build();
    }

    @Ignore
    @Test(timeout = 2000)
    public void testPerformance2() {
        long stop, start;
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; ++i)
            assertTrue(Tensors.isKroneckerOrMetric(contract(generateContractedMetricSequence(1000))));
        stop = System.currentTimeMillis();
        System.out.println(stop - start);
    }

    @Test
    public void testPerformance3() {
        long stop, start;
        start = System.currentTimeMillis();
        Tensor t;
        for (int i = 0; i < 100; ++i) {
            t = generateNotContractedMetricSequence(1000);
            assertTrue(t == contract(t));
        }
        stop = System.currentTimeMillis();
        System.out.println(stop - start);
    }
//    @Test
//    public void testRimanDerivative() {
//        Tensor riman = parse("g^{\\mu \\nu}*R_{\\mu \\nu}");
//        Transformations.substitute(riman,
//                "R_{\\mu \\nu}=R^{\\alpha}_{\\mu \\alpha \\nu}");
//        Transformations.substitute(riman,
//                "R^{\\alpha}_{\\beta \\mu \\nu}="
//                + "p_{\\mu}*G^{\\alpha}_{\\beta \\nu}-p_{\\nu}*G^{\\alpha}_{\\beta \\mu}"
//                + "+G^{\\alpha}_{\\gamma \\mu}*G^{\\gamma}_{\\beta \\nu}-G^{\\alpha}_{\\gamma \\nu}*G^{\\gamma}_{\\beta \\mu}");
//        riman = Transformations.substitute(riman, "G^{\\alpha}_{\\mu \\nu}="
//                + "g^{\\alpha \\gamma}*(p_{\\mu}*g_{\\gamma \\nu}+p_{\\nu}*g_{\\gamma \\mu}-p_{\\gamma}*g_{\\mu \\nu})");
//        Tensor rimanClone = riman.clone();
//        rimanClone = Transformations.renameConflictingIndices(rimanClone);
//        assertTrue(TensorUtils.testIndicesConsistent(rimanClone));
//
//        contract(rimanClone);
//        assertTrue(TensorUtils.testIndicesConsistent(rimanClone));
//
//        Tensor derivative = Derivative.create(riman,
//                new SimpleTensor[]{
//                    (SimpleTensor) parse("g_{\\alpha \\beta}"),
//                    (SimpleTensor) parse("g_{\\mu \\nu}")});
//        derivative = Transformations.renameConflictingIndices(derivative);
//        derivative = GetDerivative1.ContractIndices.transform(derivative);
//        assertTrue(TensorUtils.testIndicesConsistent(derivative));
//        derivative = contract(derivative);
//        assertTrue(TensorUtils.testIndicesConsistent(derivative));
//    }

    @Test
    public void testAbstractScalarFunction2() {
        Tensor t = parse("Sin[g^am*(X_a+g_ab*(X^b+g^bc*(X_c+d_c^f*F_f+g_cd*g^de*X_e+g_cj*d^j_k*(X^k+X_l*g^lk))))*J_m]");
        t = contract(t);
        Tensor expected = parse("Sin[(X_{a}+X_{a}+X_{a}+F_{a}+X_{a}+X_{a}+X_{a})*J^a]");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testAbstractScalarFunction3() {
        Tensor t = parse("Sin[g^ac*(X_a+g_ab*(X^b+g^bc*(X_c+d_c^f*F_f+g_cd*g^de*X_e+g_cj*d^j_k*(X^k+X_l*g^lk))))*J_c]");
        t = contract(t);
        Tensor expected = parse("Sin[(X_{a}+X_{a}+X_{a}+F_{a}+X_{a}+X_{a}+X_{a})*J^a]");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Ignore
    @Test
    public void testAbstractScalarFunction4() {
        Tensor t = parse("Sin[g_mn*g^mn]*Sin[g^ac*(X_a+g_ab*(X^b+g^bc*(X_c+d_c^f*F_f+g_cd*g^de*X_e+g_cj*d^j_k*(X^k+X_l*g^lk))))*J_c]+d^y_x*d^x_y");
        t = contract(t);
        Tensor expected = parse("Sin[d^m_m]*Sin[(X_{a}+X_{a}+X_{a}+F_{a}+X_{a}+X_{a}+X_{a})*J^a]+d^m_m");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void testFieldArg() {
        Tensor t = parse("F[g_mn*A^m]");
        t = contract(t);
        Tensor expected = parse("F[A_n]");
        assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void test1() {
        Tensor t = parse("1/48*(16+2*g^{\\mu \\nu }*g_{\\mu \\nu })");
        Expression d = Tensors.parseExpression("d_\\mu^\\mu=4");
        t = contract(t);
        t = d.transform(t);
        assertTrue(TensorUtils.equals(t, parse("1/2")));
    }
}
