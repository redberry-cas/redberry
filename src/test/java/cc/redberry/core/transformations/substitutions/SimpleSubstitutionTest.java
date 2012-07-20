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

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimpleSubstitutionTest {

    public SimpleSubstitutionTest() {
    }

    private static Tensor contract(Tensor tensor) {
        return ContractIndices.CONTRACT_INDICES.transform(tensor);
    }

    private static Tensor expand(Tensor tensor) {
        return Expand.expand(tensor);
    }

    private static Tensor substitute(Tensor tensor, String substitution) {
        Expression e = (Expression) parse(substitution);
        return e.transform(tensor);
    }

    @Test
    public void subs0() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs1() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab*d*A_mn");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}*d*B_{m}*C_{n}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subsDiff1() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_mn");
        Tensor target = parse("A^mn");
        Transformation sp = Substitutions.getTransformation(from, to);
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
        Transformation sp = Substitutions.getTransformation(from, to);
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
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{ac}*C^{c}_{b}*d*B_{md}*C^{d}_{n}");
        assertTrue(TensorUtils.compare(target, expected));
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
        Tensor to = parse("B_m*C^n-B^n*C_m");
        Tensor target = parse("A^a_b");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B^a*C_b-B_b*C^a");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subs6_equalsSubs() {
        SimpleTensor from = (SimpleTensor) parse("g_ab");
        Tensor to = parse("g_ab");
        Tensor target = parse("1/2*g^{ag}*(p_{m}*g_{gn}+p_{n}*g_{gm}+-1*p_{g}*g_{mn})");
        Tensor expected = target;

        Transformation sp = Substitutions.getTransformation(from, to);
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

        Transformation sp = Substitutions.getTransformation(from, to);
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

        Transformation sp = Substitutions.getTransformation(from, to);
        sp.transform(target);
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void subsField1() {
        SimpleTensor from = (SimpleTensor) parse("A_m^n");
        addSymmetry("A_a^b", IndexType.LatinLower, true, 1, 0);
        Tensor to = parse("B_m*C^n-B^n*C_m");
        Tensor target = parse("A^a_b+F^a_b[A_m^n]");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        System.out.println(target);
        Tensor expected = parse("-B_{b}*C^{a}+B^a*C_b+F^{a}_{b}[B_{m}*C^{n}-B^n*C_m]");
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
        target = substitute(target,
                            "R_{mn}=R^{a}_{man}");
        target = substitute(target,
                            "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target = substitute(target,
                            "G^a_mn=(1/2)*g^ag*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(expand(target));

        //Riman with diff states
        Tensor target1 = parse("g_{mn}*R^{mn}");
        target1 = substitute(target1,
                             "R_{mn}=g^ab*R_{bman}");
        target1 = substitute(target1,
                             "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target1 = substitute(target1,
                             "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target1 = contract(expand(target1));

        assertTrue(TensorUtils.compare(target, target1));
        assertTrue(target.getIndices().size() == 0);
        assertTrue(target1.getIndices().size() == 0);
    }

    @Test
    public void rimanTensorSubstitution_diffStates3() {

        //Riman without diff states
        Tensor target = parse("g^{mn}*R_{mn}");
        target = substitute(target,
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
        assertTrue(target.getIndices().size() == 0);
        assertTrue(target1.getIndices().size() == 0);
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
    public void subs19() {
        Tensor target = parse("f_mn^mn");
        target = substitute(target,
                            "f_ab^cd=a_ab*z^cd");
        Tensor expected = parse("a_ab*z^ab");
        assertTrue(TensorUtils.compare(target, expected));
    }
}
