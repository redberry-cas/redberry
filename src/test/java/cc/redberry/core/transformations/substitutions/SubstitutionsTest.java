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

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import junit.framework.Assert;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SubstitutionsTest {

    private static Tensor contract(Tensor tensor) {
        return ContractIndices.INSTANCE.transform(tensor);
    }

    private static Tensor expand(Tensor tensor) {
        return Expand.expand(tensor);
    }

    private static Tensor testSimpletitute(Tensor tensor, String testSimpletitution) {
        Expression e = (Expression) parse(testSimpletitution);
        return e.transform(tensor);
    }

    @Test
    public void testSimple0() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple1() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab*d*A_mn");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}*d*B_{m}*C_{n}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple2() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_ma*C^a_n");
        Tensor target = parse("A_ab*d*A_mn");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{ac}*C^{c}_{b}*d*B_{md}*C^{d}_{n}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void testSimple3() {
        Tensor target = parse("g^mn*R_mn");
        target = testSimpletitute(target, "R_mn=R^a_man");
        target = testSimpletitute(target, "R^a_bcd=A^a*A_b*B_c*B_d");
        Tensor expected = parse("g^{mn}*A^{a}*A_{m}*B_{a}*B_{n}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple4() {
        Tensor target = parse("N*(N-1)+N+1/N");
        target = testSimpletitute(target, "N=3");
        Tensor expacted = parse("3*(3-1)+3+1/3");
        assertTrue(TensorUtils.equalsExactly(target, expacted));
    }

    @Test
    public void testSimple5() {
        Tensor target = parse("L*(L-1)*F");
        target = testSimpletitute(target, ("L=1"));
        assertTrue(TensorUtils.equalsExactly(target, Complex.ZERO));
    }

    @Test
    public void testSimple6() {
        Tensor t = parse("H^{\\sigma\\lambda\\epsilon\\zeta }_{\\alpha\\beta}*E_{\\mu\\nu}^{\\alpha\\beta}_{\\delta\\gamma }*n_{\\sigma}*n_{\\lambda}*H^{\\mu\\nu\\delta\\gamma}_{\\epsilon\\zeta}");
        assertTrue(t.getIndices().getFree().size() == 0);
        System.out.println(t.toString(ToStringMode.UTF8));
        Expression ex = (Expression) parse("E^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }=H^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }+4*H^{\\mu \\gamma \\delta }_{\\eta \\theta }*H^{\\nu \\eta \\theta }_{\\epsilon \\zeta }+4*H^{\\nu \\gamma \\delta }_{\\lambda \\xi }*H^{\\mu \\lambda \\xi }_{\\epsilon \\zeta }");
        System.out.println(ex.toString(ToStringMode.UTF8));
        t = testSimpletitute(t, "E^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }=H^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }+4*H^{\\mu \\gamma \\delta }_{\\eta \\theta }*H^{\\nu \\eta \\theta }_{\\epsilon \\zeta }+4*H^{\\nu \\gamma \\delta }_{\\lambda \\xi }*H^{\\mu \\lambda \\xi }_{\\epsilon \\zeta }");
        System.out.println(t.toString(ToStringMode.UTF8));
        t = expand(t);
        assertTrue(true);
    }

    @Test
    public void testSimple7() {
        Tensor t = parse("H^{rkef}_{ab}*E_{lm}^{ab}_{dc}*n_{r}*n_{k}*H^{lmdc}_{ef}");
        assertTrue(t.getIndices().getFree().size() == 0);
        Expression ex = (Expression) parse("E^{lmcd}_{ef}=H^{lmcd}_{ef}+4*H^{lcd}_{gh}*H^{mgh}_{ef}+4*H^{mcd}_{kn}*H^{lkn}_{ef}");
        System.out.println(ex);
        t = testSimpletitute(t, "E^{lmcd}_{ef}=H^{lmcd}_{ef}+4*H^{lcd}_{gh}*H^{mgh}_{ef}+4*H^{mcd}_{kn}*H^{lkn}_{ef}");
        System.out.println(t);
        t = expand(t);
        System.out.println(t);
    }

    @Test
    public void testSimple8() {
        Tensor t = parse("H^{slez}_{ab}*E_{mn}^{ab}_{dg}*n_{s}*n_{l}*H^{mndg}_{ez}");
        t = testSimpletitute(t, "E^{mngd}_{ez}=H^{mngd}_{ez}+4*H^{mgd}_{yt}*H^{nyt}_{ez}+4*H^{ngd}_{lx}*H^{mlx}_{ez}");
        System.out.println(t);
        t = expand(t);
        System.out.println(t);
    }

    @Test
    public void testSimple9() {
        Tensor target = parse("f_mn^mn");
        target = testSimpletitute(target,
                                  "f_ab^cd=a_ab*z^cd");
        Tensor expected = parse("a_ab*z^ab");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void testSimple10() {
        CC.resetTensorNames(2074334866573507904L);
        Tensor delta = Tensors.parse("DELTA^{ \\alpha \\beta \\mu_{1} }_{\\nu_{1} } "
                + "= 1/2*(X^{ \\alpha \\beta \\mu_{1} }_{\\nu_{1} }+XX^{ \\alpha \\beta \\mu_{1} }_{\\nu_{1} }) +f^{\\mu_{1} }_{\\xi }*g^{ \\xi }*d_{\\gamma }*HATK^{\\alpha \\gamma }_{\\delta }*HATK^{\\beta \\delta }_{\\nu_{1} }");
        Expression hatK = (Expression) contract(Tensors.parse("HATK^{\\beta \\gamma }_{\\delta } = f^{\\gamma \\alpha }*d^{\\beta }_{\\delta }*n_{\\alpha }"));
        System.out.println(hatK);
        delta = hatK.transform(delta);
        junit.framework.Assert.assertTrue(true);
    }

    @Test
    public void testSimple11() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_mn");
        Tensor target = parse("A^mn");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        target = contract(target);
        Tensor expected = parse("B^mn");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple12() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_mn");
        Tensor target = parse("A_ab*A^mn");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        target = contract(target);
        Tensor expected = parse("B_{ab}*B^{mn}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple13() {
        SimpleTensor from = (SimpleTensor) parse("A_m^n");
        addSymmetry("A_a^b", IndexType.LatinLower, true, 1, 0);
        Tensor to = parse("B_m*C^n-B^n*C_m");
        Tensor target = parse("A^a_b");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B^a*C_b-B_b*C^a");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple14() {
        SimpleTensor from = (SimpleTensor) parse("g_ab");
        Tensor to = parse("g_ab");
        Tensor target = parse("1/2*g^{ag}*(p_{m}*g_{gn}+p_{n}*g_{gm}+-1*p_{g}*g_{mn})");
        Tensor expected = target;

        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        target = contract(target);
        expected = contract(expected);
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple15() {
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
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple16() {
        SimpleTensor from = (SimpleTensor) parse("g_ab");
        Tensor to = parse("g_ab");
        Tensor target = parse("g^ag");
        Tensor expected = target;

        Transformation sp = Substitutions.getTransformation(from, to);
        sp.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple17() {
        SimpleTensor from = (SimpleTensor) parse("A_m^n");
        addSymmetry("A_a^b", IndexType.LatinLower, true, 1, 0);
        Tensor to = parse("B_m*C^n-B^n*C_m");
        Tensor target = parse("A^a_b+F^a_b[A_m^n]");
        Transformation sp = Substitutions.getTransformation(from, to);
        target = sp.transform(target);
        System.out.println(target);
        Tensor expected = parse("-B_{b}*C^{a}+B^a*C_b+F^{a}_{b}[B_{m}*C^{n}-B^n*C_m]");
        assertTrue(TensorUtils.equals(target, expected));

    }

    @Test
    public void testSimple18() {

        //Riman with diff states
        Tensor target = parse("g^{mn}*g^{ab}*R_{bman}");
        target = testSimpletitute(target,
                                  "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target = testSimpletitute(target,
                                  "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(target);
        assertTrue(true);
    }

    @Test
    public void testSimple19() {

        //Riman without diff states
        Tensor target = parse("g^{mn}*R_{mn}");
        target = testSimpletitute(target,
                                  "R_{mn}=R^{a}_{man}");
        target = testSimpletitute(target,
                                  "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target = testSimpletitute(target,
                                  "G^a_mn=(1/2)*g^ag*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(expand(target));

        //Riman with diff states
        Tensor target1 = parse("g_{mn}*R^{mn}");
        target1 = testSimpletitute(target1,
                                   "R_{mn}=g^ab*R_{bman}");
        target1 = testSimpletitute(target1,
                                   "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target1 = testSimpletitute(target1,
                                   "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target1 = contract(expand(target1));


        assertTrue(TensorUtils.equals(target, target1));
        assertTrue(target.getIndices().size() == 0);
        assertTrue(target1.getIndices().size() == 0);
    }

    @Test
    public void testSimple20() {

        //Riman without diff states
        Tensor target = parse("g^{mn}*R_{mn}");
        target = testSimpletitute(target,
                                  "R_{mn}=R^{a}_{man}");
        target = testSimpletitute(target,
                                  "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target = testSimpletitute(target,
                                  "G^a_mn=(1/2)*g^ag*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(target);

        //Riman with diff states
        Tensor target1 = parse("g_{mn}*R^{mn}");
        target1 = testSimpletitute(target1,
                                   "R_{mn}=g^ab*R_{bman}");
        target1 = testSimpletitute(target1,
                                   "R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm");
        target1 = testSimpletitute(target1,
                                   "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target1 = contract(target1);

        assertTrue(TensorUtils.equals(target, target1));
        assertTrue(target.getIndices().size() == 0);
        assertTrue(target1.getIndices().size() == 0);
    }

    @Test
    public void testSimple21() {
        Tensor t = parse("(f+g)*(d+h*(d+f)*(k+f))");
        Expression f = parseExpression("f=f_m^m");
        Expression g = parseExpression("g=g_m^m");
        Expression d = parseExpression("d = d_m^m");
        Expression h = parseExpression("h=h_m^m");
        Expression k = parseExpression("k=k_m^m");
        Expression[] es = {f, g, d, h, k};
        IntPermutationsGenerator generator = new IntPermutationsGenerator(es.length);
        int[] permutation;
        Expression[] temp;
        while (generator.hasNext()) {
            permutation = generator.next();
            temp = Combinatorics.shuffle(es, permutation);
            for (Expression e : temp)
                t = e.transform(t);
            TAssert.assertIndicesConsistency(t);
        }
    }

    @Test
    public void testSimple22() {
        CC.resetTensorNames(123);
        Tensor t = parse("(f+g)*(d+h)*(f+h)");
        Expression f = parseExpression("f=f_m^m");
        Expression g = parseExpression("g=g_m^m");
        Expression d = parseExpression("d = d_m^m");
        Expression h = parseExpression("h=h_m^m");
        Expression[] es = {f, g, d, h};
        for (Expression e : es)
            t = e.transform(t);
        TAssert.assertIndicesConsistency(t);

    }

    @Test
    public void testSimple23() {
        Tensor t = parse("f*g*k*(f+g*(k+f))*(d+h*(d+f)*(k*(g+k)+f))+(f+g*(k+f))*(d+h*(d+f)*(k*(g+k)+f))");
        Expression f = parseExpression("f=f_m^m+f1_a^a");
        Expression g = parseExpression("g=g_m^m+g1_b^b");
        Expression d = parseExpression("d = d_m^m+d1_c^c");
        Expression h = parseExpression("h=h_m^m+h1_d^d");
        Expression k = parseExpression("k=k_m^m+k1_e^e");
        Expression[] es = {f, g, d, h, k};
        IntPermutationsGenerator generator = new IntPermutationsGenerator(es.length);
        int[] permutation;
        Expression[] temp;
        while (generator.hasNext()) {
            permutation = generator.next();
            temp = Combinatorics.shuffle(es, permutation);
            for (Expression e : temp)
                t = e.transform(t);
            TAssert.assertIndicesConsistency(t);
        }
    }

    @Test
    public void testField1() {
        TensorField from = (TensorField) parse("f[x]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        System.out.println(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g+y")));
    }

    @Test
    public void testField2() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+y_a")));
    }

    @Test
    public void testField3() {
        TensorField from = (TensorField) parse("f_m[x_i,y_j]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p,k_k]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+k_a")));
    }

    @Test
    public void testField5() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g^p]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        target = contract(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+y_a")));
    }

    @Test
    public void testField6() {
        TensorField from = (TensorField) parse("f_m[x_i,y^k]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f^a[X^i,Y_j]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        target = contract(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("X^a+Y^a")));
    }

    @Test
    public void testField7() {
        TensorField from = (TensorField) parse("f_m[x_i,y^kpq]");
        Tensor to = parse("x_m+y^i_i_m");
        Tensor target = parse("f^a[X^i,Y_jzx]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        target = contract(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("X^a+Y^i_i^a")));
    }

    @Test
    public void testField8() {
        Tensor target = parse("V_mni[p_m-k_m,k_m]*D^mnab[k_a]*V_abj[k_m,p_m-k_m]");
        target = parseExpression("D_mnab[k_p]=(g_ma*g_nb+g_mb*g_an-g_mn*g_ab)*1/(k_p*k^p)").transform(target);
        target = parseExpression("V_mni[k_p,q_p]=k_m*g_ni-q_m*g_ni").transform(target);

        target = expand(target);
        target = contract(target);

        Tensor expected = parse(target.toString(ToStringMode.REDBERRY_SOUT));
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testField9() {
        TensorField from = (TensorField) parse("f[x]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g+y")));
    }

    @Test
    public void testField10() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+y_a")));
    }

    @Test
    public void testField11() {
        TensorField from = (TensorField) parse("f_m[x_i,y_j]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p,k_k]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+k_a")));
    }

//    @Test
//    public void testField12() {
//        TensorField from = (TensorField) parse("f_m[x_i]");
//        Tensor to = parse("x_m+y_m");
//        Tensor target = parse("f_a[g^p]");
//        Transformation transformation = Substitutions.getTransformation(from, to);
//        target = transformation.transform(target);
//        assertTrue(TensorUtils.equalsExactly(target, parse("f_{a}[g^{p}]")));
//    }
    @Test
    public void testField13() {
        TensorField from = (TensorField) parse("f_ab[x_mn]");
        Tensor to = parse("x_ab");
        Tensor target = parse("f_mn[z_i*y_j]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("z_{m}*y_{n}")));
    }

    @Test
    public void testField14() {
        TensorField from = (TensorField) parse("f_ab[x_mn]");
        Tensor to = parse("x_ab");
        Tensor target = parse("f_mn[y_j*z_i]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("z_{m}*y_{n}")));
    }

    @Test
    public void testField15() {

        //Riman without diff states
        Tensor target = parse("Rf[g_mn]");
        target = Tensors.parseExpression("Rf[g_ab]=g^ab*Rf_ab[g_mn]").transform(target);
        target = Tensors.parseExpression("Rf_{mn}[g^mn]=Rf^{a}_{man}[g_pq]").transform(target);
        target = Tensors.parseExpression("Rf^a_bmn[g^pq]=p_m*Gf^a_bn[g_ab]+p_n*Gf^a_bm[g_ab]+Gf^a_gm[g_ab]*Gf^g_bn[g_ab]-Gf^a_gn[g_ab]*Gf^g_bm[g_ab]").transform(target);
        target = Tensors.parseExpression("Gf^a_mn[r^mn]=(1/2)*r^ag*(p_m*r_gn+p_n*r_gm-p_g*r_mn)").transform(target);

        target = contract(target);

        //Riman with diff states
        Tensor target1 = parse("g_{mn}*R^{mn}");
        target1 = Tensors.parseExpression("R_{mn}=g^ab*R_{bman}").transform(target);
        target1 = Tensors.parseExpression("R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm").transform(target);
        target1 = Tensors.parseExpression("G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)").transform(target);

        target1 = contract(target1);

        assertTrue(TensorUtils.equals(target, target1));
    }

    @Test
    public void testField16() {
        Tensor target = parse("Gf^a_gm[g_ab]*Gf^g_bn[g_ab]");
        target = Tensors.parseExpression("Gf^a_mn[r^mn]=(1/2)*r^ag*p_m*r_gn").transform(target);

    }

    @Test
    public void testField17() {

        //Riman without diff states
        Tensor target = parse("g^{ab}*p_{b}*Gf^{c}_{ac}[g_{ab}]");
        target = Tensors.parseExpression("Gf^a_mn[r^mn]=(1/2)*r^ag*(p_m*r_gn+p_n*r_gm-p_g*r_mn)").transform(target);

        target = contract(target);

    }

    @Test
    public void testField18() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^d+w_c^d]");
        target = t.transform(target);
        System.out.println(target);
        assertEquals(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void testField19() {
        Tensor from = parse("f_a[x_a]");
        Tensor to = parse("x_a");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a[x^a]");
        target = t.transform(target);
        System.out.println(target);
        assertEquals(target, "x_a");
    }

    @Test
    public void testField20() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^a+w_c^a:_c^a]");
        target = t.transform(target);
        assertEquals(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void testField21() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^a+w_c^a:_c^a]");
        target = t.transform(target);
        assertEquals(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void testField22() {
        //parsing tensor field
        Expression field = Tensors.parseExpression("F_{ij}[p_a, q_b] = "
                + "g_{ij}*p_a*q^a - (p_i*q_j + p_j*q_i)");

        //parsing some expression 
        Tensor e = Tensors.parse("E = F_ab[k^n - p^n, q_n] * F^ab[q_n, k_n]");

        //substituting field value in expression
        e = field.transform(e);
        System.out.println(e);
        e = Expand.expand(e);

        //sipmlifying
        System.out.println(e);
    }
    
    
    @Test
    public void testField22a() {
        //parsing tensor field
        Expression field = Tensors.parseExpression("F_{ij}[p_a, q_b] = "
                + "g_{ij}*p_a*q^a - (p_i*q_j + p_j*q_i)");

        //parsing some expression 
        Tensor e = Tensors.parse("E = F_ab[p^n, q_n] * F^ab[p_n, q_n]");

        //substituting field value in expression
        e = field.transform(e);
        System.out.println(e);
        e = Expand.expand(e);

        //sipmlifying
        System.out.println(e);
    }
    //TODO additional tests with specified field arguments indices

    @Test
    public void testSum1() {
        Tensor target = parse("a+b+c+d");
        target = parseExpression("c+d=-a-b").transform(target);
        Assert.assertTrue(TensorUtils.isZero(target));
    }
    //TODO tests for Sum

    @Test
    public void testProduct1() {
        Tensor target = parse("R^a_bmn*R_a^bmn");
        target = parseExpression("R^a_bmn*R_a^bmn = 1").transform(target);
        Assert.assertTrue(TensorUtils.isOne(target));
    }

    @Test
    public void testProduct2() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor target = parse("R^a_bmn*R_a^bmn*R^p_qrs*R_p^qrs");
            target = parseExpression("R^a_bmn*R_a^bmn = 1").transform(target);
            Assert.assertTrue(TensorUtils.isOne(target));
        }
    }

    @Test
    public void testProduct3() {
        for (int i = 0; i < 30; ++i) {
            CC.resetTensorNames();
            Tensor target = parse("a*b*c*n_i*n^i*n_j*n^j*n_k*n^k*n_h*n^h*n_c*n^c*n_d*n^d*n_a*n^a");
            target = parseExpression("n_i*n^i = 1").transform(target);
            target = parseExpression("b*c = 1/a").transform(target);
            Assert.assertTrue(TensorUtils.isOne(target));
        }
    }
    //TODO tests for Product
}
