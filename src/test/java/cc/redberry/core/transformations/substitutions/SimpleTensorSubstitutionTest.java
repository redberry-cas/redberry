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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import org.junit.Test;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.*;
import cc.redberry.core.transformations.expand.*;
import cc.redberry.core.utils.TensorUtils;

import static org.junit.Assert.assertTrue;
import static cc.redberry.core.TAssert.assertParity;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimpleTensorSubstitutionTest {

    public SimpleTensorSubstitutionTest() {
    }

    private static Tensor contract(Tensor tensor) {
        return ContractIndices.CONTRACT_INDICES.transform(tensor);
    }

    private static Tensor expand(Tensor tensor) {
        return ExpandBrackets.expandBrackets(tensor);
    }

    private static Tensor substitute(Tensor tensor, String substitution) {
        Expression e = (Expression) parse(substitution);
        return SimpleSubstitution.SIMPLE_SUBSTITUTION_PROVIDER.createSubstitution(e.get(0), e.get(1), true).transform(tensor);
    }

    @Test
    public void subs0() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs1() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab*d*A_mn");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}*d*B_{m}*C_{n}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subsDiff1() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_mn");
        Tensor target = parse("A^mn");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        target = contract(target);
        Tensor expected = parse("B^mn");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subsDiff2() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_mn");
        Tensor target = parse("A_ab*A^mn");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        target = contract(target);
        Tensor expected = parse("B_{ab}*B^{mn}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs2() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_ma*C^a_n");
        Tensor target = parse("A_ab*d*A_mn");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{ac}*C^{c}_{b}*d*B_{md}*C^{d}_{n}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs3() {
        Tensor target = parse("g^mn*R_mn");
        target = substitute(target, "R_mn=R^a_man");
        target = substitute(target, "R^a_bcd=A^a*A_b*B_c*B_d");
        Tensor expected = parse("g^{mn}*A^{a}*A_{m}*B_{a}*B_{n}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs5_minusone() {
        SimpleTensor from = (SimpleTensor) parse("A_m^n");
        addSymmetry("A_a^b", IndexType.LatinLower, true, 1, 0);
        Tensor to = parse("B_m*C^n");
        Tensor target = parse("A^a_b");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        Tensor expected = parse("-B_b*C^a");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs6_equalsSubs() {
        SimpleTensor from = (SimpleTensor) parse("g_ab");
        Tensor to = parse("g_ab");
        Tensor target = parse("1/2*g^{ag}*(p_{m}*g_{gn}+p_{n}*g_{gm}+-1*p_{g}*g_{mn})");
        Tensor expected = target;

        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        target = contract(target);
        expected = contract(expected);
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs7_equalsSubs() {
        SimpleTensor from = (SimpleTensor) parse("g_ab");
        Tensor to = parse("g_ab");
        Tensor target = parse("1/2*g^{ag}*(p_{m}*g_{gn}+p_{n}*g_{gm}+-1*p_{g}*g_{mn})");
        Tensor expected = target;

        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        target = sp.transform(target);
        target = sp.transform(target);
        target = contract(target);
        expected = contract(expected);
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs8_equalsSubs() {
        SimpleTensor from = (SimpleTensor) parse("g_ab");
        Tensor to = parse("g_ab");
        Tensor target = parse("g^ag");
        Tensor expected = target;

        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        sp.transform(target);
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs9() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor target = parse("L");
            target = substitute(target,
                                "L=(1/4)*F_{a \\mu \\nu}*F^{a \\mu \\nu}");
            target = substitute(target,
                                "F^a_{\\mu \\nu}=D[A^a_{\\mu}[x^{\\alpha}],x^{\\nu}]-D[A^a_{\\nu}[x^{\\alpha}],x^{\\mu}]+"
                    + "g*f^{abc}*A_{b \\mu}[x^{\\alpha}]*A_{c \\nu}[x^{\\alpha}]");
            target = contract(target);
            Tensor expected = parse("(1/4)*(D[A_{a}^{\\alpha }[x^{\\alpha }],x_{\\beta }]-D[A_{a}^{\\beta }[x^{\\alpha }],x_{\\alpha }]+g*f_{a}^{bc}*A_{b}^{\\alpha }[x^{\\alpha }]*A_{c}^{\\beta }[x^{\\alpha }])*(D[A^{a}_{\\alpha }[x^{\\alpha }],x^{\\beta }]-D[A^{a}_{\\beta }[x^{\\alpha }],x^{\\alpha }]+g*f^{aef}*A_{e\\alpha }[x^{\\alpha }]*A_{f\\beta }[x^{\\alpha }])");
            assertParity(target, expected);
        }
    }

    @Test
    public void subs9s0() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor target = parse("L");
            target = substitute(target,
                                "L=(1/4)*F_{a \\mu \\nu}*F^{a \\mu \\nu}");
            target = substitute(target,
                                "F^a_{\\mu \\nu}=D[A^a_{\\mu}[x^{\\alpha}],x^{\\nu}]");
            target = contract(target);
            Tensor expected = parse("(1/4)*(D[A_{a}^{\\alpha }[x^{\\alpha }],x_{\\beta }])*(D[A^{a}_{\\alpha }[x^{\\alpha }],x^{\\beta }])");
            assertParity(target, expected);
        }
    }

    @Test(timeout = 10000)
    public void subs10() {
        Transformation Ric = (Transformation) parse("Ric_ab=E^r_a*E^d_c*R^c_bdr");
        Transformation Riman = (Transformation) parse("R^a_bcd=i*D[w^a_db,x^c]-i*D[w^a_cb,x^d]+w^a_cr*w^r_db-w^a_dr*w^r_cb");
        Transformation Torsion = (Transformation) parse("T^a_bc=i*D[h^a_c,x^b]-i*D[h^a_b,x^c]+w^a_bd*e^d_c-w^a_cd*e^d_b");
        Transformation eTetrad = (Transformation) parse("e^a_b=d^a_b+h^a_b");
        Transformation ETetrad = (Transformation) parse("E^a_b=d^a_b-h^a_b+h^a_c*h^c_b");
        Transformation metricUP = (Transformation) parse("G^ab=g^ab-g^ca*h^b_c-g^cb*h^a_c+g^cb*h^a_d*h^d_c+g^ca*h^b_d*h^d_c+g^cd*h^a_c*h^b_d");
        Transformation sqrt = (Transformation) parse("sqrt=1+h^a_a+(1/2)*(h^s_s*h^l_l-h^s_l*h^l_s)");
        Transformation tetradGaugeFix = (Transformation) parse("Gf_a=f1*D[h^b_a,x^b]+f2*g^pq*g_ab*D[h^b_q,x^p]+f3*D[h^q_q,x^a]");
        Tensor Lagrangian = parse("sqrt*(g^ab*Ric_ab+(e1*g_ab*G^xp*G^yq+e2*E^x_a*E^p_b*G^yq+e3*E^x_b*E^p_a*G^yq)*T^a_xy*T^b_pq+e6*Ric_ab*Ric_cd*g^ab*g^cd+e5*Ric_ab*Ric_cd*g^ac*g^bd+Gf_a*Gf_b*g^ab)+f*g^pq*g_ab*i*D[h^b_q,x^p]*g^cd*I*D[h^a_d,x^c]");
        Transformation[] substitutions = {
            Ric,
            Riman,
            Torsion,
            eTetrad,
            ETetrad,
            metricUP,
            sqrt,
            tetradGaugeFix
        };
        for (int i = 0; i < substitutions.length; i++)
            Lagrangian = substitutions[i].transform(Lagrangian);
        Tensor clone = Lagrangian;
        Lagrangian = contract(Lagrangian);
        Lagrangian = expand(Lagrangian);
        Lagrangian = contract(Lagrangian);
        assertTrue(Lagrangian.getIndices().getFreeIndices().size() == 0);
    }

    @Test(timeout = 5000)
    public void subs11() {
        Transformation Ric = (Transformation) parse("Ric_ab=E^r_a*E^d_c*R^c_bdr");
        Transformation Riman = (Transformation) parse("R^a_bcd=i*D[w^a_db,x^c]-i*D[w^a_cb,x^d]+w^a_cr*w^r_db-w^a_dr*w^r_cb");
        Transformation Torsion = (Transformation) parse("T^a_bc=i*D[h^a_c,x^b]-i*D[h^a_b,x^c]+w^a_bd*e^d_c-w^a_cd*e^d_b");
        Transformation eTetrad = (Transformation) parse("e^a_b=d^a_b+h^a_b");
        Transformation ETetrad = (Transformation) parse("E^a_b=d^a_b-h^a_b+h^a_c*h^c_b");
        Transformation metricUP = (Transformation) parse("G^ab=g^ab-g^ca*h^b_c-g^cb*h^a_c+g^cb*h^a_d*h^d_c+g^ca*h^b_d*h^d_c+g^cd*h^a_c*h^b_d");
        Transformation sqrt = (Transformation) parse("sqrt=1+h^a_a+(1/2)*(h^s_s*h^l_l-h^s_l*h^l_s)");
        Transformation tetradGaugeFix = (Transformation) parse("Gf_a=f1*D[h^b_a,x^b]+f2*g^pq*g_ab*D[h^b_q,x^p]+f3*D[h^q_q,x^a]");
        Tensor Lagrangian = parse("sqrt*(g^ab*Ric_ab+(e1*g_ab*G^xp*G^yq+e2*E^x_a*E^p_b*G^yq+e3*E^x_b*E^p_a*G^yq)*T^a_xy*T^b_pq+e6*Ric_ab*Ric_cd*g^ab*g^cd+e5*Ric_ab*Ric_cd*g^ac*g^bd+Gf_a*Gf_b*g^ab)+f*g^pq*g_ab*i*D[h^b_q,x^p]*g^cd*I*D[h^a_d,x^c]");
        Transformation[] substitutions = {
            Ric,
            Riman,
            Torsion,
            eTetrad,
            ETetrad,
            metricUP,
            sqrt,
            tetradGaugeFix
        };
        for (int i = 0; i < substitutions.length; i++)
            Lagrangian = substitutions[i].transform(Lagrangian);

        Lagrangian = expand(Lagrangian);
        Lagrangian = contract(Lagrangian);
        assertTrue(Lagrangian.getIndices().getFreeIndices().size() == 0);
    }

    @Test
    public void subsField1() {
        SimpleTensor from = (SimpleTensor) parse("A_m^n");
        addSymmetry("A_a^b", IndexType.LatinLower, true, 1, 0);
        Tensor to = parse("B_m*C^n");
        Tensor target = parse("A^a_b+F^a_b[A_m^n]");
        SimpleSubstitution sp = new SimpleSubstitution(from, to);
        target = sp.transform(target);
        System.out.println(target);
        Tensor expected = parse("-1*B_{b}*C^{a}+F^{a}_{b}[B_{m}*C^{n}]");
        assertTrue(TensorUtils.compare(target, expected));

    }

    @Test
    public void rimanTensorSubstitution_diffStates1() {

        //Riman with diff states
        Tensor target = parse("g^{mn}*g^{ab}*R_{bman}");
        target = substitute(target,
                            "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target = substitute(target,
                            "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(target);
        assertTrue(true);
    }

    @Test
    public void rimanTensorSubstitution_diffStates2() {

        //Riman without diff states
        Tensor target = parse("g^{mn}*R_{mn}");
        substitute(target,
                   "R_{mn}=R^{a}_{man}");
        target = substitute(target,
                            "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target = substitute(target,
                            "G^a_mn=(1/2)*g^ag*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(target);

        //Riman with diff states
        Tensor target1 = parse("g_{mn}*R^{mn}");
        target1 = substitute(target1,
                             "R_{mn}=g^ab*R_{bman}");
        target1 = substitute(target1,
                             "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target1 = substitute(target1,
                             "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target1 = contract(target1);

        assertTrue(TensorUtils.compare(target, target1));
    }

    @Test
    public void subs12() {
        Tensor target = parse("N*(N-1)+N+1/N");
        target = substitute(target, "N=3");
        Tensor expacted = parse("3*(3-1)+3+1/3");
        assertTrue(TensorUtils.equals(target, expacted));
    }

    @Test
    public void subs13() {
        Tensor target = parse("L*(L-1)*F");
        target = substitute(target, ("L=1"));
        assertTrue(TensorUtils.equals(target, Complex.ZERO));
    }

    @Test
    public void subs16() {
        Tensor t = parse("H^{\\sigma\\lambda\\epsilon\\zeta }_{\\alpha\\beta}*E_{\\mu\\nu}^{\\alpha\\beta}_{\\delta\\gamma }*n_{\\sigma}*n_{\\lambda}*H^{\\mu\\nu\\delta\\gamma}_{\\epsilon\\zeta}");
        assertTrue(t.getIndices().getFreeIndices().size() == 0);
        System.out.println(t.toString(ToStringMode.UTF8));
        Expression ex = (Expression) parse("E^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }=H^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }+4*H^{\\mu \\gamma \\delta }_{\\eta \\theta }*H^{\\nu \\eta \\theta }_{\\epsilon \\zeta }+4*H^{\\nu \\gamma \\delta }_{\\lambda \\xi }*H^{\\mu \\lambda \\xi }_{\\epsilon \\zeta }");
        System.out.println(ex.toString(ToStringMode.UTF8));
        t = substitute(t, "E^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }=H^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }+4*H^{\\mu \\gamma \\delta }_{\\eta \\theta }*H^{\\nu \\eta \\theta }_{\\epsilon \\zeta }+4*H^{\\nu \\gamma \\delta }_{\\lambda \\xi }*H^{\\mu \\lambda \\xi }_{\\epsilon \\zeta }");
        System.out.println(t.toString(ToStringMode.UTF8));
        t = expand(t);
        assertTrue(true);
    }

    @Test
    public void subs17_() {
        Tensor t = parse("H^{rkef}_{ab}*E_{lm}^{ab}_{dc}*n_{r}*n_{k}*H^{lmdc}_{ef}");
        assertTrue(t.getIndices().getFreeIndices().size() == 0);
        Expression ex = (Expression) parse("E^{lmcd}_{ef}=H^{lmcd}_{ef}+4*H^{lcd}_{gh}*H^{mgh}_{ef}+4*H^{mcd}_{kn}*H^{lkn}_{ef}");
        System.out.println(ex);
        t = substitute(t, "E^{lmcd}_{ef}=H^{lmcd}_{ef}+4*H^{lcd}_{gh}*H^{mgh}_{ef}+4*H^{mcd}_{kn}*H^{lkn}_{ef}");
        System.out.println(t);
        t = expand(t);
        System.out.println(t);
    }

    @Test
    public void subs17() {
        Tensor t = parse("H^{slez}_{ab}*E_{mn}^{ab}_{dg}*n_{s}*n_{l}*H^{mndg}_{ez}");
        t = substitute(t, "E^{mngd}_{ez}=H^{mngd}_{ez}+4*H^{mgd}_{yt}*H^{nyt}_{ez}+4*H^{ngd}_{lx}*H^{mlx}_{ez}");
        System.out.println(t);
        t = expand(t);
        System.out.println(t);
    }

    @Test
    public void test18() {
        Tensor target = parse("g_{ax}*D[(1/2)*g^{xe}*D[g_{de},x^{b}],x^{c}]");

        Tensor target1 = parse("g_ax*D[G^x_bd,x^c]");
        substitute(target1, "G^a_bc="
                + "(1/2)*g^ae*D[g_ce,x^b]");

        System.out.println(target);
        System.out.println(target1);
        System.out.println(target.getIndices());
        System.out.println(target1.getIndices());
        assertTrue(target.getIndices().equals(target1.getIndices()));
    }

    @Test
    public void subs19() {
        Tensor target = parse("f_mn^mn");
        target = substitute(target,
                            "f_ab^cd=a_ab*z^cd");
        Tensor expected = parse("a_ab*z^ab");
        assertTrue(TensorUtils.compare(target, expected));
    }
}
