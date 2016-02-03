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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.test.RedberryTest;
import cc.redberry.core.test.TestUtils;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.TAssert.*;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SubstitutionsTest extends RedberryTest {

    private static Tensor contract(Tensor tensor) {
        return EliminateMetricsTransformation.ELIMINATE_METRICS.transform(tensor);
    }

    private static Tensor expand(Tensor tensor) {
        return ExpandTransformation.expand(tensor);
    }

    private static Tensor substitute(Tensor tensor, String testSimpletitution) {
        Expression e = (Expression) parse(testSimpletitution);
        return e.transform(tensor);
    }

    @Before
    public void setUp() throws Exception {
        CC.reset();
    }

    @Test
    public void testSimple0() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab");
        Transformation sp = new SubstitutionTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple1() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_m*C_n");
        Tensor target = parse("A_ab*d*A_mn");
        Transformation sp = new SubstitutionTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{a}*C_{b}*d*B_{m}*C_{n}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple2() {
        SimpleTensor from = (SimpleTensor) parse("A_mn");
        Tensor to = parse("B_ma*C^a_n");
        Tensor target = parse("A_ab*d*A_mn");
        Transformation sp = new SubstitutionTransformation(from, to);
        target = sp.transform(target);
        Tensor expected = parse("B_{ac}*C^{c}_{b}*d*B_{md}*C^{d}_{n}");
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void testSimple3() {
        Tensor target = parse("g^mn*R_mn");
        target = substitute(target, "R_mn=R^a_man");
        target = substitute(target, "R^a_bcd=A^a*A_b*B_c*B_d");
        Tensor expected = parse("g^{mn}*A^{a}*A_{m}*B_{a}*B_{n}");
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple4() {
        Tensor target = parse("N*(N-1)+N+1/N");
        target = substitute(target, "N=3");
        System.out.println(target);
        Tensor expacted = parse("3*(3-1)+3+1/3");
        assertTrue(TensorUtils.equalsExactly(target, expacted));
    }

    @Test
    public void testSimple5() {
        Tensor target = parse("L*(L-1)*F");
        target = substitute(target, ("L=1"));
        assertTrue(TensorUtils.equalsExactly(target, Complex.ZERO));
    }

    @Test
    public void testSimple6() {
        Tensor t = parse("H^{\\sigma\\lambda\\epsilon\\zeta }_{\\alpha\\beta}*E_{\\mu\\nu}^{\\alpha\\beta}_{\\delta\\gamma }*n_{\\sigma}*n_{\\lambda}*H^{\\mu\\nu\\delta\\gamma}_{\\epsilon\\zeta}");
        assertTrue(t.getIndices().getFree().size() == 0);
        System.out.println(t.toString(OutputFormat.UTF8));
        Expression ex = (Expression) parse("E^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }=H^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }+4*H^{\\mu \\gamma \\delta }_{\\eta \\theta }*H^{\\nu \\eta \\theta }_{\\epsilon \\zeta }+4*H^{\\nu \\gamma \\delta }_{\\lambda \\xi }*H^{\\mu \\lambda \\xi }_{\\epsilon \\zeta }");
        System.out.println(ex.toString(OutputFormat.UTF8));
        t = substitute(t, "E^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }=H^{\\mu \\nu \\gamma \\delta }_{\\epsilon \\zeta }+4*H^{\\mu \\gamma \\delta }_{\\eta \\theta }*H^{\\nu \\eta \\theta }_{\\epsilon \\zeta }+4*H^{\\nu \\gamma \\delta }_{\\lambda \\xi }*H^{\\mu \\lambda \\xi }_{\\epsilon \\zeta }");
        System.out.println(t.toString(OutputFormat.UTF8));
        t = expand(t);
        assertTrue(true);
    }

    @Test
    public void testSimple7() {
        Tensor t = parse("H^{rkef}_{ab}*E_{lm}^{ab}_{dc}*n_{r}*n_{k}*H^{lmdc}_{ef}");
        assertTrue(t.getIndices().getFree().size() == 0);
        Expression ex = (Expression) parse("E^{lmcd}_{ef}=H^{lmcd}_{ef}+4*H^{lcd}_{gh}*H^{mgh}_{ef}+4*H^{mcd}_{kn}*H^{lkn}_{ef}");
        System.out.println(ex);
        t = substitute(t, "E^{lmcd}_{ef}=H^{lmcd}_{ef}+4*H^{lcd}_{gh}*H^{mgh}_{ef}+4*H^{mcd}_{kn}*H^{lkn}_{ef}");
        System.out.println(t);
        t = expand(t);
        System.out.println(t);
    }

    @Test
    public void testSimple8() {
        Tensor t = parse("H^{slez}_{ab}*E_{mn}^{ab}_{dg}*n_{s}*n_{l}*H^{mndg}_{ez}");
        t = substitute(t, "E^{mngd}_{ez}=H^{mngd}_{ez}+4*H^{mgd}_{yt}*H^{nyt}_{ez}+4*H^{ngd}_{lx}*H^{mlx}_{ez}");
        System.out.println(t);
        t = expand(t);
        System.out.println(t);
    }

    @Test
    public void testSimple9() {
        Tensor target = parse("f_mn^mn");
        target = substitute(target,
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
        Transformation sp = new SubstitutionTransformation(from, to);
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
        Transformation sp = new SubstitutionTransformation(from, to);
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
        Transformation sp = new SubstitutionTransformation(from, to);
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

        Transformation sp = new SubstitutionTransformation(from, to, false);
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

        Transformation sp = new SubstitutionTransformation(from, to, false);
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

        Transformation sp = new SubstitutionTransformation(from, to, false);
        sp.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testSimple17() {
        SimpleTensor from = (SimpleTensor) parse("A_m^n");
        addSymmetry("A_a^b", IndexType.LatinLower, true, 1, 0);
        Tensor to = parse("B_m*C^n-B^n*C_m");
        Tensor target = parse("A^a_b+F^a_b[A_m^n]");
        Transformation sp = new SubstitutionTransformation(from, to);
        target = sp.transform(target);
        System.out.println(target);
        Tensor expected = parse("-B_{b}*C^{a}+B^a*C_b+F^{a}_{b}[B_{m}*C^{n}-B^n*C_m]");
        assertTrue(TensorUtils.equals(target, expected));

    }

    @Test
    public void testSimple19() {

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


        assertTrue(TensorUtils.equals(target, target1));
        assertTrue(target.getIndices().size() == 0);
        assertTrue(target1.getIndices().size() == 0);
    }

    @Test
    public void testSimple18() {
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
    public void testSimple18a() {
        CC.resetTensorNames(-2492126546111636082L);
        //Riman with diff states
        Tensor target = parse("(-G^{g}_{ma}*G_{bgn}+G^{g}_{mn}*G_{bga})*g^{ab}*g^{mn}");
        target = substitute(target,
                "G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)");

        target = contract(target);
        target = expand(target);
        assertIndicesConsistency(target);
    }

    @Test
    public void testSimple20() {

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
            temp = Permutations.permute(es, permutation);
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
            temp = Permutations.permute(es, permutation);
            for (Expression e : temp)
                t = e.transform(t);
            TAssert.assertIndicesConsistency(t);
        }
    }

    @Test
    public void testSimple23a() {
        CC.resetTensorNames(-1030130556496293426L);
        Tensor t = parse("(f+g*k)*(d+h*f*f)");
        Expression f = parseExpression("f=f_m^m+f1_a^a");
        t = f.transform(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void testSimple24() {
        CC.resetTensorNames(-1030130556496293426L);
        Tensor t = parse("(f+g*k)*(d+h*f*f)");
        //System.out.println(t);
        Expression f = parseExpression("f=f_m^m+f1_a^a");
        t = f.transform(t);
        //System.out.println(t);
        TAssert.assertIndicesConsistency(t);
    }


    @Test
    public void testField1() {
        TensorField from = (TensorField) parse("f[x]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        System.out.println(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g+y")));
    }

    @Test
    public void testField2() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+y_a")));
    }

    @Test
    public void testField3() {
        TensorField from = (TensorField) parse("f_m[x_i,y_j]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p,k_k]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+k_a")));
    }

    @Test
    public void testField3a() {
        TensorField from = (TensorField) parse("f[x,y]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g,k]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g+k")));
    }

    @Test
    public void testField5() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g^p]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        target = contract(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+y_a")));
    }

    @Test
    public void testField6() {
        TensorField from = (TensorField) parse("f_m[x_i,y^k]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f^a[X^i,Y_j]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        target = contract(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("X^a+Y^a")));
    }

    @Test
    public void testField7() {
        TensorField from = (TensorField) parse("f_m[x_i,y^kpq]");
        Tensor to = parse("x_m+y^i_i_m");
        Tensor target = parse("f^a[X^i,Y_jzx]");
        Transformation transformation = new SubstitutionTransformation(from, to);
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

        Tensor expected = parse(target.toString(OutputFormat.Redberry));
        assertTrue(TensorUtils.equalsExactly(target, expected));
    }

    @Test
    public void testField9() {
        TensorField from = (TensorField) parse("f[x]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g+y")));
    }

    @Test
    public void testField10() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+y_a")));
    }

    @Test
    public void testField11() {
        TensorField from = (TensorField) parse("f_m[x_i,y_j]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p,k_k]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("g_a+k_a")));
    }

    //    @Test
//    public void testField12() {
//        TensorField from = (TensorField) parse("f_m[x_i]");
//        Tensor to = parse("x_m+y_m");
//        Tensor target = parse("f_a[g^p]");
//        Transformation transformation = new Substitution(from, to);
//        target = transformation.transform(target);
//        assertTrue(TensorUtils.equalsExactly(target, parse("f_{a}[g^{p}]")));
//    }
    @Test
    public void testField13() {
        TensorField from = (TensorField) parse("f_ab[x_mn]");
        Tensor to = parse("x_ab");
        Tensor target = parse("f_mn[z_i*y_j]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("z_{m}*y_{n}")));
    }

    @Test
    public void testField14() {
        TensorField from = (TensorField) parse("f_ab[x_mn]");
        Tensor to = parse("x_ab");
        Tensor target = parse("f_mn[y_j*z_i]");
        Transformation transformation = new SubstitutionTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equalsExactly(target, parse("z_{m}*y_{n}")));
    }

    @Test
    public void testField15() {

        //Riemann without diff states
        Tensor target = parse("Rf[g_mn]");
        target = Tensors.parseExpression("Rf[g_ab]=g^ab*Rf_ab[g_mn]").transform(target);
        target = Tensors.parseExpression("Rf_{mn}[g^mn]=Rf^{a}_{man}[g_pq]").transform(target);
        target = Tensors.parseExpression("Rf^a_bmn[g^pq]=p_m*Gf^a_bn[g_ab]+p_n*Gf^a_bm[g_ab]+Gf^a_gm[g_ab]*Gf^g_bn[g_ab]-Gf^a_gn[g_ab]*Gf^g_bm[g_ab]").transform(target);
        target = Tensors.parseExpression("Gf^a_mn[r^mn]=(1/2)*r^ag*(p_m*r_gn+p_n*r_gm-p_g*r_mn)").transform(target);

        target = contract(target);

        //Riemann with diff states
        Tensor target1 = parse("g_{mn}*R^{mn}");
        target1 = Tensors.parseExpression("R_{mn}=g^ab*R_{bman}").transform(target1);
        target1 = Tensors.parseExpression("R^a_bmn=p_m*G^a_bn+p_n*G^a_bm+G^a_gm*G^g_bn-G^a_gn*G^g_bm").transform(target1);
        target1 = Tensors.parseExpression("G_gmn=(1/2)*(p_m*g_gn+p_n*g_gm-p_g*g_mn)").transform(target1);

        target1 = contract(target1);

        assertTrue(TensorUtils.equals(target, target1));
    }

    @Test
    public void testField16() {
        Tensor target = parse("Gf^a_gm[g_ab]*Gf^g_bn[g_ab]");
        target = Tensors.parseExpression("Gf^a_mn[r^mn]=(1/2)*r^ag*p_m*r_gn").transform(target);
        TAssert.assertIndicesConsistency(target);

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
        Transformation t = new SubstitutionTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^d+w_c^d]");
        target = t.transform(target);
        System.out.println(target);
        assertEquals(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void testField19() {
        Tensor from = parse("f_a[x_a]");
        Tensor to = parse("x_a");
        Transformation t = new SubstitutionTransformation(from, to);
        Tensor target = parse("f_a[x^a]");
        target = t.transform(target);
        System.out.println(target);
        assertEquals(target, "x_a");
    }

    @Test
    public void testField20() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = new SubstitutionTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^a+w_c^a:_c^a]");
        target = t.transform(target);
        assertEquals(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void testField21() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = new SubstitutionTransformation(from, to);
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
        e = ExpandTransformation.expand(e, EliminateMetricsTransformation.ELIMINATE_METRICS, Tensors.parseExpression("d_a^a=4"));
        TAssert.assertIndicesConsistency(e);
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
        e = ExpandTransformation.expand(e);
        TAssert.assertIndicesConsistency(e);
    }

    @Test
    public void testField23() {
        Expression field = parseExpression("a = a_a^a");
        Tensor target = parse("f[a]*g_a");
        target = field.transform(target);
        Tensor expected = parse("f[a_a^a]*g_a");
        System.out.println(target);
        assertEqualsExactly(target, expected);
    }

    @Test
    public void testField24() {
        Expression field = parseExpression("a = a_a^a");
        Tensor target = parse("f[f[a]]*g_a");
        target = field.transform(target);
        Tensor expected = parse("f[f[a_a^a]]*g_a");
        assertEqualsExactly(target, expected);
    }

    @Test
    public void testField25() {
        Expression field = parseExpression("a = a_a^a");
        Tensor target = parse("f[f[a],a]*g_a");
        target = field.transform(target);
        Tensor expected = parse("f[f[a_a^a],a_a^a]*g_a");
        assertEqualsExactly(target, expected);
    }

    @Test
    public void testField26() {
        Expression s = parseExpression("F_i[x_mn] = x_ik*f^k");
        Tensor t1 = parse("F_k[x_i*y_j]");// same as parse('F_k[x_i*y_j:_ij]')
        Tensor t2 = parse("F_k[x_i*y_j:_ji]");
        TAssert.assertEquals(s.transform(t1), "x_k*y_m*f^m");
        TAssert.assertEquals(s.transform(t2), "x_m*y_k*f^m");
    }

    @Test
    public void testField27() {
        Expression s = parseExpression("V_{i}[p_a, q_b] = -I*e*(p_i+q_i)");
        Tensor t = parse("V^{i}[k_a,k_a-p_a+q_a]");
        t = s.transform(t);
        TAssert.assertEquals(t, "-I*e*(2*k^i-p^i+q^i)");
    }

    @Test
    public void testField28() {
        Expression s = parseExpression("f[x,y]=x+y");
        Tensor t = parse("f[0,x]");
        t = s.transform(t);
        System.out.println(t);
        TAssert.assertEquals(t, "x");
    }

    @Test
    public void testField29() {
        Expression s = parseExpression("f[x,y]=x**y");
        Tensor t = parse("f[y,x]");
        t = s.transform(t);
        TAssert.assertEquals(t, "y**x");
    }

    @Test
    public void testField30() {
        Expression s = parseExpression("f[x,y, x+z]=x+y+z");
        Tensor t = parse("f[a,b,c]");
        t = s.transform(t);
        TAssert.assertEquals(t, "f[a,b,c]");
    }

    @Test
    public void testField31() {
        Expression s = parseExpression("f[1] = x");
        Tensor t = parse("f[-1]");
        t = s.transform(t);
        TAssert.assertEquals(t, "f[-1]");
    }
    //TODO additional tests with specified field arguments indices

    @Test
    public void testPower1() {
        Expression s = parseExpression("Sin[a - b] = c");
        Tensor t = parse("Sin[b-a]**3");
        TAssert.assertEquals(s.transform(t), "-c**3");
        t = parse("Sin[b-a]**2");
        TAssert.assertEquals(s.transform(t), "c**2");
    }

    @Test
    public void testSum1() {
        Tensor target = parse("a+b+c+d");
        target = parseExpression("c+d=-a-b").transform(target);
        Assert.assertTrue(TensorUtils.isZero(target));
    }

    @Test
    public void testSum2() {
        Tensor target = parse("(f_m + (f_ij + M_ij)*(t^i+k^i)*f_k*G^jk_m + T^a*f_am + (f_ij + V_ij)*(t^i+k^i)*f_k*G^jk_m)" +
                "*(f^m + (f_ij + M_ij)*(t^i+k^i)*f_k*G^jkm + T^a*f_a^m + (f_ij + V_ij)*(t^i+k^i)*f_k*G^jkm)");
        target = parseExpression("(f_ij + M_ij)*(t^i+k^i)*f_k*G^jk_m + (f_ij + V_ij)*(t^i+k^i)*f_k*G^jk_m = f_m").transform(target);
        TAssert.assertEquals(target, "(f_{bm}*T^{b}+2*f_{m})*(f_{a}^{m}*T^{a}+2*f^{m})");
    }

    @Test
    public void testSum2a() {
        CC.resetTensorNames(-74951565663283894L);
        Tensor target = parse("(f_m + (f_ij + M_ij)*(t^i+k^i)*f_k*G^jk_m + T^a*f_am + (f_ij + V_ij)*(t^i+k^i)*f_k*G^jk_m)" +
                "*(f^m + (f_ij + M_ij)*(t^i+k^i)*f_k*G^jkm + T^a*f_a^m + (f_ij + V_ij)*(t^i+k^i)*f_k*G^jkm)");
        Tensor old = target;
        target = parseExpression("c+d=-a-b").transform(target);
        Assert.assertTrue(old == target);
    }


    @Test
    public void testSum3() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            addSymmetry("R_mnp", IndexType.LatinLower, true, 2, 1, 0);
            Tensor target = parse("f_i + R_ijk*F^jk + R_ijk*F^kj - R_kij*F^jk");
            target = parseExpression("f_m + R_bma*F^ba - R_ljm*F^lj =  R_bam*F^ab ").transform(target);
            TAssert.assertEquals(target, "0");
        }
    }

    @Test
    public void testSum3a() {
        CC.resetTensorNames(2634486062579664417L);
        Tensor target = parse("f_i + R_ijk*F^kj + R_ijk*F^jk - R_kij*F^jk");
        target = parseExpression("f_i + R_ijk*F^kj - R_kij*F^jk = - R_ikj*F^jk ").transform(target);
        TAssert.assertEquals(target, "-F^{jk}*R_{ikj}+F^{jk}*R_{ijk}");
    }

    @Test
    public void testSum4() {
        CC.resetTensorNames(2634486062579664417L);
        Tensor target = parse("A_abc + A_bca + A_cab + A_acb + A_bac + A_cba");
        target = parseExpression("A_abc + A_bca + A_cab = F_abc").transform(target);
        TAssert.assertEquals(target, "F_{abc}+F_{bac}");
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

    @Test
    public void testProduct3a() {
        Tensor target = parse("a*b*c*n_i*n^i");
        target = parseExpression("n_i*n^i = 1").transform(target);
        target = parseExpression("b*c = 1/a").transform(target);
        Assert.assertTrue(TensorUtils.isOne(target));
    }

    @Test
    public void testProduct4() {
        Tensor target = parse("g_ab*g_cd+2*e_a^\\alpha*e_{b \\alpha}*e_c^\\beta*e_{d \\beta}");
        target = parseExpression("e_a^\\alpha*e_{b \\alpha} = -g_ab").transform(target);
        TAssert.assertEquals(target, "3*g_ab*g_cd");
    }

    @Test
    public void testProduct4a() {
        Tensor target = parse("g_ab*g_cd+4*e_a^\\alpha*e_{b \\alpha}*e_c^\\beta*e_{d \\beta}");
        target = parseExpression("2*e_a^\\alpha*e_{b \\alpha} = -g_ab").transform(target);
        TAssert.assertEquals(target, "2*g_ab*g_cd");
    }

    @Test
    public void testProduct5() {
        Tensor target = parse("k^a*k^b*p^c*p^d*e_a^\\alpha*e_{b \\alpha}*e_c^\\beta*e_{d \\beta}");
        target = parseExpression("e_a^\\alpha*e_{b \\alpha} = -g_ab").transform(target);
        target = EliminateMetricsTransformation.eliminate(target);
        TAssert.assertEquals(target, "k^a*k_a*p^b*p_b");
    }

    @Ignore
    @Test
    public void testProduct6() {
        Tensor target = parse("a[p]*b[k]");
        target = parseExpression("a[q]*b[s] = q+s").transform(target);
        TAssert.assertEquals(target, "q+s");
    }

    @Test
    public void testProduct7() {
        Tensor target = parse("F^c*(A_cb - A_bc)*K^bjp*K_japm*F^a + F^c*F^b*F_bcm");
        target = parseExpression("(A^ab - A^ba)*K_ajp*K^jcpm = F^bcm").transform(target);
        TAssert.assertEquals(target, "0");
    }

    @Test
    public void testProduct8() {
        Tensor target = parse("Sin[a-b*c]*F^c*(A_cb - A_bc)*K^bjp*K_japm*F^a + F^c*F^b*F_bcm");
        target = parseExpression("Sin[-a+b*c]*(A^ab - A^ba)*K_ajp*K^jcpm = -F^bcm").transform(target);
        TAssert.assertEquals(target, "0");
    }

    @Test
    public void testProduct9() {
        Tensor target = parse("Cos[a-b*c]*F^c*(A_cb - A_bc)*K^bjp*K_japm*F^a + F^c*F^b*F_bcm");
        target = parseExpression("Cos[-a+b*c]*(A^ab - A^ba)*K_ajp*K^jcpm = F^bcm").transform(target);
        TAssert.assertEquals(target, "0");
    }

    @Test
    public void testProduct10() {
        Tensor target = parse("Sin[a-b*c]**2*F^c*(A_cb - A_bc)*K^bjp*K_japm*F^a + F^c*F^b*F_bcm");
        target = parseExpression("Sin[-a+b*c]**2*(A^ab - A^ba)*K_ajp*K^jcpm = F^bcm").transform(target);
        TAssert.assertEquals(target, "0");
    }


    @Test
    public void testProduct11() {
        Tensor target = parse("Sin[a-b*c]**a*F^c*(A_cb - A_bc)*K^bjp*K_japm*F^a + F^c*F^b*F_bcm");
        Tensor old = target;
        target = parseExpression("Sin[-a+b*c]**a*(A^ab - A^ba)*K_ajp*K^jcpm = F^bcm").transform(target);
        TAssert.assertTrue(old == target);
    }

    @Test
    public void testProduct12() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor target = parse("Sin[a-b*c]**a*b*c*d");
            Tensor old = target;
            target = parseExpression("Sin[-a+b*c]**a*b = c").transform(target);
            TAssert.assertTrue(old == target);
        }
    }

    @Test
    public void testProduct12a() {
        Tensor target = parse("Sin[a-b*c]**a*b*c*d");
        Tensor old = target;
        target = parseExpression("Sin[-a+b*c]**a*b = c").transform(target);
        System.out.println(target);
        TAssert.assertTrue(old == target);
    }

    @Test
    public void testProduct13() {
        Tensor t = parse("p_i*p^i*(p_j+(p_i*p^i - m**2) *x_j)*(p^j+(p_i*p^i - m**2) *x^j)");
        Expression e = parseExpression("p_i*p^i = m**2");
        t = e.transform(t);
        TAssert.assertEquals(t, "m**4");
    }

    @Test
    public void testProduct15() {
        Tensor t = parse("(8*Sin[f^{i}*f_{i}]+4*((f^{m}+a^{m}*f^{a}*f_{a})*f_{m})**(-1)*Cos[f^{i}*f_{i}]**(-2)*Cos[f^{i}*f_{i}])*f^{l}*f_{l}-4*((f^{m}+a^{m}*f^{a}*f_{a})*f_{m})**(-2)*Cos[f^{i}*f_{i}]**(-2)*Sin[f^{i}*f_{i}]*f^{l}*(f_{l}+a_{l}*f_{a}*f^{a}+f_{m}*(2*a^{m}*f_{l}+d^{m}_{l}))-((f^{m}+a^{m}*f^{a}*f_{a})*f_{m})**(-2)*Cos[f^{i}*f_{i}]**(-1)*(2*d^{l}_{l}+4*a^{a}*f_{a}+2*a^{m}*f_{m}*d^{l}_{l})+2*((f^{m}+a^{m}*f^{a}*f_{a})*f_{m})**(-3)*Cos[f^{i}*f_{i}]**(-1)*(f_{l}+a_{l}*f_{a}*f^{a}+f_{m}*(2*a^{m}*f_{l}+d^{m}_{l}))*(f^{l}+a^{l}*f_{a}*f^{a}+f_{m}*(2*a^{m}*f^{l}+g^{ml}))+2*((f^{m}+a^{m}*f^{a}*f_{a})*f_{m})**(-1)*Cos[f^{i}*f_{i}]**(-2)*Sin[f^{i}*f_{i}]*d^{l}_{l}");
        Expression s = parseExpression("f_m*f^m = m**2");
        t = s.transform(t);
        t = parseExpression("a_j = 0").transform(t);
        t = EliminateMetricsTransformation.eliminate(t);
        t = s.transform(t);
        t = parseExpression("d_m^m = 4").transform(t);
        t = expand(t);
        TAssert.assertEquals(t, "4*Cos[m**2]**(-1)+8*Sin[m**2]*m**2");
    }

    @Test
    public void testProduct16() {
        Tensor t = parse("vp_A[p1_m]*v^A[p2_m]");
        Expression e = parseExpression("vp_A[p1_m]*v_B[p2_m] = vp_A[p2_m]*v_B[p1_m]");
        t = e.transform(t);
        TAssert.assertEquals(t, "vp_A[p2_m]*v^A[p1_m]");
    }

    @Test
    public void testProduct17() {
        Tensor t = parse("pv_A[p2_m]*V^A_{B m}*e^m[k2_m]*D^B_C[k1_m+p1_m]*V^C_{D n}*e^n[k1_m]*v^D[p1_m]");
        Expression e = parseExpression("pv_A[p2_m]*v_B[p1_m] = pv_A[p1_m]*v_B[p2_m]");
        t = e.transform(t);
        TAssert.assertEquals(t, "pv_A[p1_m]*V^A_{B m}*e^m[k2_m]*D^B_C[k1_m+p1_m]*V^C_{D n}*e^n[k1_m]*v^D[p2_m]");
    }


    @Test
    public void testProduct18() {
        Tensor t = parse(" x_i'*y^k' ");
        Expression e = parseExpression("x_i'*y^k' = A_i'^k'");
        t = e.transform(t);
        TAssert.assertEquals(t, "A_i'^k'");
    }

    @Test
    public void testProduct19() {
        Tensor t = parse(" x_i'*y^k' ");
        Expression e = parseExpression("x^i'*y^k' = A^i'k'");
        t = e.transform(t);
        TAssert.assertEquals(t, "x_i'*y^k'");
    }

    @Test
    public void testProduct20() {
        Tensor t = parse("e^m[k1_m]*e^n[k1_m]*G^{a'}_{b'm}*G^{b'}_{c'n}");
        Expression e = parseExpression("e^m[k1_m]*e^n[k1_m]*G^{a'}_{b'm}*G^{b'}_{c'n} = d^{a'}_{c'}*e_m[k1_m]*e^m[k1_m]");
        t = e.transform(t);
        TAssert.assertEquals(t, "e^{m}[k1_{m}]*e_{m}[k1_{m}]*d^{a'}_{c'}");
    }

    @Test
    public void testProduct21() {
        Tensor t = parse("G_{a}^{a'}_{b'}*G_b^{b'}_{c'}");
        Expression e = parseExpression("G_{a}^{a'}_{c'}*G_{b}^{c'}_{b'} = G_{b}^{a'}_{c'}*G_{a}^{c'}_{b'}");
        t = e.transform(t);
    }

    @Test
    public void testProduct22() {
        Tensor t = parse("x*(x**2 + x)");
        Expression e = parseExpression("-x = x");
        t = e.transform(t);
        TAssert.assertEquals(t, "-x*(x**2 - x)");
    }


    @Test
    public void testProduct23() {
        Tensor t = parse("K[-x]");
        Expression e = parseExpression("x = -x");
        Transformation tr = new SubstitutionTransformation(e);
        t = tr.transform(t);
        TAssert.assertEquals(t, "K[x]");
    }

    @Test
    public void testProduct24() {
        Tensor t = parse("K[-x]");
        Expression e = parseExpression("x = -x");
        t = e.transform(t);
        TAssert.assertEquals(t, "K[x]");
    }


    @Test
    public void testProduct25() {
        Tensor t = parse("(x*y + 1)*x*y");
        Expression e = parseExpression("x*y = b");
        t = e.transform(t);
        TAssert.assertEquals(t, "(b+1)*b");

        Transformation tr = new SubstitutionTransformation(e);
        t = tr.transform(t);
        TAssert.assertEquals(t, "(b+1)*b");
    }


    @Test
    public void testProduct26() {
        Tensor t = parse("(x*y + 1)*x*y");
        Expression e = parseExpression("x*y = 1");

        Transformation tr = new SubstitutionTransformation(new Expression[]{e}, false);
        t = tr.transform(t);
        TAssert.assertEquals(t, "2*x*y");
    }

    @Test
    public void testProduct27() throws Exception {
        Tensor t = parse("2*a*f_iA*k^ij*T^A_j");
        Expression subs = parseExpression("f_jB*k^jc = R_B^c");
        TAssert.assertEquals("2*a*R^{j}_{A}*T_{j}^{A}", subs.transform(t));
    }

    @Test
    public void testProduct28() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^A'_B'"), IndexType.Matrix2);


        setAntiSymmetric("f_ABC");
        setSymmetric("d_ABC");

        Tensor t = parse("Tr[T_{I}*T_{A}*T_{J}]");
        Expression subs = parseExpression("T_{A}*T_{B} = (1/2)*T^{C}*d_{ABC}+(1/2)*N**(-1)*g_{AB}+(1/2*I)*T^{C}*f_{ABC}");

        TAssert.assertEquals("((1/2)*T^{C}*d_{AIC}+(1/2*I)*T^{C}*f_{AIC}+(1/2)*N**(-1)*g_{AI})*T_{J}", subs.transform(t));
    }


    @Test
    public void testProduct29() throws Exception {
        for (int i = 0; i < 100; i++) {
            CC.reset();
            GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
            CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
            indicesInsertion.addInsertionRule(parseSimple("T^A'_B'"), IndexType.Matrix2);


            setAntiSymmetric("f_ABC");
            setSymmetric("d_ABC");

            Tensor t = parse("(1/2)*N**(-1)*Tr[T_{J}]*g_{IA}");
            Expression subs = parseExpression("T_{A}*T^{A} = (1/2)*N**(-1)*(-1+N**2)");

            TAssert.assertEquals(t, subs.transform(t));
        }
    }

    @Test
    public void testProduct30() throws Exception {
        Expression[] exprs = {
                parseExpression("p1_a*p1^a = m1"),
                parseExpression("p1_a*p2^a = 0"),
                parseExpression("p1_a*p3^a = s13"),
                parseExpression("p1_a*p4^a = s14"),
                parseExpression("p1_a*p5^a = s15"),
                parseExpression("p2_a*p2^a = m2"),
                parseExpression("p2_a*p3^a = 0"),
                parseExpression("p2_a*p4^a = s24"),
                parseExpression("p2_a*p5^a = s25"),
                parseExpression("p3_a*p3^a = m3"),
                parseExpression("p3_a*p4^a = s34"),
                parseExpression("p3_a*p5^a = 0"),
                parseExpression("p4_a*p4^a = m4"),
                parseExpression("p4_a*p5^a = s45"),
                parseExpression("p5_a*p5^a = m5"),
        };

        List<Transformation> bf = new ArrayList<>();
        for (Expression expr : exprs)
            bf.add(new BruteForceProductSubs(expr));

        TransformationCollection seqSubs = new TransformationCollection(bf);
        SubstitutionTransformation subs = new SubstitutionTransformation(exprs);

        for (int i = 0; i < TestUtils.its(10, 100); i++) {
            RandomTensor rnd = new RandomTensor();
            rnd.clearNamespace();
            rnd.addToNamespace(parse("p1_a"), parse("p2_a"), parse("p3_a"), parse("p4_a"), parse("p5_a"));

            Tensor expr = rnd.nextTensorTree(3, 4, 4, IndicesFactory.EMPTY_INDICES);

            Tensor a = subs.transform(expr);
            a = new ExpandTransformation(subs).transform(a);
            a = subs.transform(a);

            Tensor b = seqSubs.transform(expr);
            b = new ExpandTransformation(seqSubs).transform(b);
            b = seqSubs.transform(b);

            TAssert.assertEquals(expand(a), expand(b));
        }
    }

    @Test
    public void testPower13() {
        Expression s = parseExpression("d*Sin[a - b]*f_mn = k_mn");
        Tensor t = parse("d*Sin[b - a]*f_mn ");
        System.out.println(s.transform(t));
        TAssert.assertEquals(s.transform(t), "-k_mn");
    }

    @Test
    public void testPower14() {
        addSymmetry("f_mn", IndexType.LatinLower, true, 1, 0);
        Expression s = parseExpression("d*Sin[a - b]*f_mn*H^n = k_m");
        Tensor t = parse("d*Sin[b - a]*f_mn*H^m");
        TAssert.assertEquals(s.transform(t), "k_n");
    }

    @Test
    public void testScalarFunction1() {
        Expression s = parseExpression("x = ArcSin[F_ab*F^ab]");
        Tensor t = parse("(F_ab*F^ab + 1)*Sin[x]");
        TAssert.assertIndicesConsistency(s.transform(t));
    }

    @Test
    public void testScalarFunction2() {
        Tensor t = parse("Sin[f]*(Sin[f]+Sin[g]*(Sin[k]+Sin[f]))*(Sin[d]+Sin[h])");
        t = parse("Sin[f]*(Sin[g]+Sin[k])");

        Expression f = parseExpression("f = ArcSin[f_m^m+f1_a^a]");
        Expression g = parseExpression("g = ArcSin[g_m^m+g1_b^b]");
        Expression d = parseExpression("d = ArcSin[d_m^m+d1_c^c]");
        Expression h = parseExpression("h = ArcSin[h_m^m+h1_d^d]");
        Expression k = parseExpression("k = ArcSin[k_m^m+k1_e^e]");
        Expression[] es = new Expression[]{f, g, d, h, k};
        SubstitutionTransformation s = new SubstitutionTransformation(es);
        t = s.transform(t);
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void testScalarFunction3() {
        Tensor t = parse("f*g*k*(f+g*(k+f))*(d+h*(d+f)*(k*(g+k)+f))+(f+g*(k+f))*(d+h*(d+f)*(k*(g+k)+f))");
        Expression f = parseExpression("f = Sin[f]");
        Expression g = parseExpression("g = Sin[g]");
        Expression d = parseExpression("d = Sin[d]");
        Expression h = parseExpression("h = Sin[h]");
        Expression k = parseExpression("k = Sin[k]");
        Expression[] es = {f, g, d, h, k};
        t = new SubstitutionTransformation(es).transform(t);

        f = parseExpression("f = ArcSin[f_m^m+f1_a^a]");
        g = parseExpression("g = ArcSin[g_m^m+g1_b^b]");
        d = parseExpression("d = ArcSin[d_m^m+d1_c^c]");
        h = parseExpression("h = ArcSin[h_m^m+h1_d^d]");
        k = parseExpression("k = ArcSin[k_m^m+k1_e^e]");
        es = new Expression[]{f, g, d, h, k};
        IntPermutationsGenerator generator = new IntPermutationsGenerator(es.length);
        int[] permutation;
        Expression[] temp;
        while (generator.hasNext()) {
            permutation = generator.next();
            temp = Permutations.permute(es, permutation);
            for (Expression e : temp)
                t = e.transform(t);
            TAssert.assertIndicesConsistency(t);
        }
    }

    @Test
    public void testFieldDerivative1() {
        Expression s = parseExpression("f[x] = x**2");
        Tensor t = parse("f~(1)[y]");
        t = s.transform(t);
        TAssert.assertEquals(t, "2*y");
    }

    @Test
    public void testFieldDerivative2() {
        Expression s = parseExpression("f[x] = x**10");
        Tensor t = parse("f~(9)[y+z]");
        t = s.transform(t);
        TAssert.assertEquals(t, "3628800*(y+z)");
    }

    @Test
    public void testFieldDerivative3() {
        Expression s = parseExpression("f[x, y] = x**y");
        Tensor t = parse("f~(2, 2)[a, b]");
        t = s.transform(t);
        TAssert.assertEquals(t, "a**(-2+b)*b*(-1+b)*Log[a]**2+2*a**(-2+b)+2*a**(-2+b)*Log[a]*b+2*a**(-2+b)*Log[a]*(-1+b)");
    }

    @Test
    public void testFieldDerivative4() {
        Expression s = parseExpression("f_ab[x_mn, y_mn] = x_am*y_nb*x^mn");
        Tensor t = parse("f~(2, 1)^{ab}_{{mn ab} {pq}}[a_mn, b_ab]");
        t = s.transform(t);
        t = EliminateMetricsTransformation.eliminate(t);
        t = parseExpression("d_a^a = 4").transform(t);
        TAssert.assertEquals(t, "g_{mn}*g_{pq}+4*g_{mq}*g_{pn}");
    }

    @Test
    public void testFieldDerivative5() {
        Expression s = parseExpression("f~(2)[x] = x**10");
        Tensor t = parse("f~(3)[y+z]");
        t = s.transform(t);
        TAssert.assertEquals(t, "10*(y+z)**9");
    }

    @Test
    public void testFieldDerivative6() {
        //todo check what symmetries change?
        addSymmetry("x_mn", 1, 0);
        addSymmetry("y_mn", 1, 0);
        Expression s = parseExpression("f_ab[x_mn, y_mn] = x_am*y_nb*x^mn");
        Tensor t = parse("f~(2, 1)^{ab}_{{mn ab}}^{{mn}}[a_mn, b_ab]");
        t = s.transform(t);
        t = ExpandTransformation.expand(t);
        t = EliminateMetricsTransformation.eliminate(t);
        t = parseExpression("d_a^a = 4").transform(t);
        TAssert.assertEquals(t, "32");
    }


    @Test
    public void testFieldDerivatives7() {


        setSymmetric("g_ab[x_m]");
        Tensor R = parseExpression("R[x_m] = g^ab[x_m]*R_ab[x_m]");
        Expression ricci = parseExpression("R_ab[x_m] = R^n_anb[x_n]");
        Expression riemann = parseExpression("R^a_bmn[x_m] = G~(1)^a_bnm[x_m] - G~(1)^a_bmn[x_m] + G^a_gm[x_m]*G^g_bn[x_m] - G^a_gn[x_m]*G^g_bm[x_m]");
        Expression connection = parseExpression("G^a_mn[x_m] = (1/2)*g^ab[x_m]*(g~(1)_bmn[x_a] + g~(1)_bnm[x_a] - g~(1)_mnb[x_a])");
        Expression metric = parseExpression("g_mn[x_m] = g_mn + h_mn[x_m]");

        riemann = (Expression) connection.transform(riemann);
        ricci = (Expression) riemann.transform(ricci);
        R = ricci.transform(R);
        R = metric.transform(R);

        R = ExpandTransformation.expand(R);
        R = EliminateMetricsTransformation.eliminate(R);

        SumBuilder r = new SumBuilder();
        int id = parseSimple("h_mn[x_m]").getName();
        for (Tensor summand : R.get(1)) {
            if (summand instanceof Product) {
                int m = 0;
                for (Tensor multiplier : summand)
                    if (multiplier instanceof TensorField
                            && ((TensorField) multiplier).getNameDescriptor().getParent().getId() == id)
                        ++m;
                if (m <= 2)
                    r.put(summand);
            }
        }

        TAssert.assertIndicesConsistency(r.build());
    }

    @Test
    public void testSimple25() {
        //CORE-128
        CC.resetTensorNames(-4808885094055692037L);
        Tensor t = parse("(f_{c}+d_{o}^{o}*d_{k}^{k}*(d_{m}^{m}*f_{c}+f_{c}))" +
                "         *(f_{a}+d^{s}_{s}*f_{a})" +
                "         *F^{bdfghi}");
        TAssert.assertIndicesConsistency(t);
        Tensor t2 = parseExpression("d^a_a = f^a_a").transform(t);
        System.out.println(t2);
        TAssert.assertIndicesConsistency(t2);
    }

    @Ignore
    @Test
    public void testSimple25a() {
        //CORE-128
        for (int i = 0; i < 1000; ++i) {
            CC.resetTensorNames();
            parse("f_n");
            parse("g_mn");
            Tensor t = parse("(f_{c}+d_{o}^{o}_{k}^{k}*f^{p}_{p}*(d_{m}^{m}*f_{c}+f_{c}))" +
                    "         *(f_{a}+d^{s}_{s}*f_{a})" +
                    "         *F^{bdfghi}");
            TAssert.assertIndicesConsistency(t);
            Tensor t2 = parseExpression("d^a_a = f^a_a").transform(t);
            TAssert.assertIndicesConsistency(t2);
        }
    }

    @Test
    public void testSimple26() {
        CC.resetTensorNames(-4808885094055692037L);
        parse("f_n");
        parse("g_mn");
        Tensor t = parse("(f_{c}+(d_{k}^{k}*f_{p}-f_{p})*(d_{m}^{m}*f_{c}-f_{c})*(9*d_{o}^{o}*f^{p}+f^{p}))" +
                "         *(f_{a}+(d^{s}_{s}*f^{u}+f^{u})*f_{ua})" +
                "         *F^{bdfghi}_{bdfghi}");
        TAssert.assertIndicesConsistency(t);
        Tensor t2 = parseExpression("d^a_a = x").transform(t);
        TAssert.assertIndicesConsistency(t2);
        t2 = parseExpression("x = f^a_a").transform(t);
        TAssert.assertIndicesConsistency(t2);
        TAssert.assertIndicesConsistency(expand(t2));
    }

    @Test
    public void testSimple27() {
        //CORE-128 CORE-127
        CC.resetTensorNames(3795791024008453976L);
        Tensor r = parse("-68*(-260*(f_{o}*d_{r}^{r}+10*f^{q}*f_{q}*f_{o})*f^{o}*(-75*f^{p}*f_{p}*f^{d}-94*f^{d})+37*(-29*f^{d}+f^{d}*d_{p}^{p}*d_{q}^{q})*f^{l}*f_{l}*f^{s}*(-6*f_{s}+f_{s}*d_{n}^{n}))*(58*(-11*f_{a}*f_{d}+g_{ad})*f_{x}*f^{x}+(76*f^{t}*f_{t}*f_{a}+70*f_{a}*d_{u}^{u})*(10*f_{d}*f_{z}-51*d_{v}^{v}*g_{dz})*(6*f^{z}-45*f^{z}*d^{y}_{y}))*(-82*(-71*f_{b}*f_{e_{1}}+g_{be_{1}})*(65*f^{e_{1}}+f^{e_{1}}*d_{c_{1}}^{c_{1}})+2*f^{d_{1}}*f^{e_{1}}*(g_{d_{1}e_{1}}+d^{c_{1}}_{c_{1}}*g_{e_{1}d_{1}})*(f_{b}+f^{a_{1}}*f_{a_{1}}*f_{b}))*(-792*f_{e}*f^{e}*f_{c}*d_{k}^{k}+22*(60*f^{e}*f_{e}-87*d_{e}^{e}*d^{j}_{j})*f^{g}*f_{g}*(-31*f_{c}*d_{h}^{h}+f_{i}*f^{i}*f_{c}))");
        r = parseExpression("d^{g}_{g} = f_{a}*f^{a}").transform(r);
        TAssert.assertIndicesConsistency(r);
    }

    @Test
    public void testSimple28() {
        //CORE-128 CORE-127
        for (int i = 0; i < 1000; ++i) {
            CC.resetTensorNames();
            Tensor r = parse("((-d_{v}^{v}*g_{dz}+f_{d}*f_{z})*(d_{u}^{u}*f_{a}+f_{m}^{m}*f_{a})*(-d^{y}_{y}*f^{z}+f^{z})+(f_{a}*f_{d}+g_{ad})*f_{m}^{m})*((-f_{l}^{l}*f^{d}+f^{d})*(d_{r}^{r}*f_{o}+f_{f}^{f}*f_{o})*f^{o}+f_{f}^{f}*(d_{p}^{p}*d_{q}^{q}*f^{d}+f^{d})*f^{s}*(d_{n}^{n}*f_{s}+f_{s}))*((f_{b}*f_{e_{1}}+g_{be_{1}})*(d_{c_{1}}^{c_{1}}*f^{e_{1}}+f^{e_{1}})+(f_{t}^{t}*f_{b}+f_{b})*(d^{c_{1}}_{c_{1}}*g_{e_{1}d_{1}}+g_{d_{1}e_{1}})*f^{d_{1}}*f^{e_{1}})*(f_{g}^{g}*d_{k}^{k}*f_{c}+(f_{e}^{e}-d_{e}^{e}*d^{j}_{j})*f_{g}^{g}*(d_{h}^{h}*f_{c}+f_{h}^{h}*f_{c}))");
            r = parseExpression("d^{g}_{g} = f_{a}^{a}").transform(r);
            TAssert.assertIndicesConsistency(r);
        }
    }

    @Test
    public void testSimple29() {
        //CORE-128
        CC.resetTensorNames(4821775724761539743L);
        Tensor e = parse("F_abd*(f_{k}^{k}*f_{g}^{g}*f_{c}+(-f^{j}_{j}*f_{e}^{e}+f_{e}^{e})*f_{c}*f_{h}^{h}*f_{g}^{g})");
        Tensor r = parse("F_abd*(d_{k}^{k}*f_{g}^{g}*f_{c}+(-d^{j}_{j}*d_{e}^{e}+f_{e}^{e})*f_{c}*f_{h}^{h}*f_{g}^{g})");
        r = parseExpression("d^{g}_{g} = f_{a}^{a}").transform(r);
        TAssert.assertIndicesConsistency(r);
        TAssert.assertEquals(r, e);
    }

    @Test
    public void test30() {
        //linked with CORE-127
        CC.resetTensorNames(-4443309447200231241L);
        Tensor r = parse("F_b*" +
                "(f_{g}^{g}*d_{k}^{k}*f_{c}+(f_{e}^{e}-f_{e}^{e}*d^{j}_{j})*f_{g}^{g}*(d_{h}^{h}*f_{c}+f_{h}^{h}*f_{c}))");
        r = parseExpression("d^{g}_{g} = f_{a}^{a}").transform(r);
        TAssert.assertIndicesConsistency(r);
    }

    @Test
    public void test31() {
        //linked with CORE-127
        CC.resetTensorNames(-4602990689951758559L);
        Tensor r = parse("F_b*(f_{g}^{g}*d_{k}^{k}*f_{c}+(1 + d^{j}_{j})*f_{g}^{g}*f_{h}^{h}*f_{c})");
        r = parseExpression("d^{g}_{g} = f_{a}^{a}").transform(r);
        TAssert.assertIndicesConsistency(r);
    }

    @Test
    public void test32() throws Exception {
        Expression[] subs = {
                parseExpression("x  = a"),
                parseExpression("z  = b"),
                parseExpression("Z1 = 0"),
                parseExpression("f  = a"),
                parseExpression("Z2 = 0")
        };

        SubstitutionTransformation tr = new SubstitutionTransformation(subs);

        RandomTensor rnd = new RandomTensor();
        rnd.clearNamespace();
        rnd.addToNamespace(parse("x"), parse("z"), parse("Z1"), parse("f"), parse("Z2"), parse("a"), parse("b"));

        for (int i = 0; i < 10; i++) {
            Tensor expr = rnd.nextTensorTree(4, 3, 4, IndicesFactory.EMPTY_INDICES);
            Tensor ac = Transformation.Util.applyUntilUnchanged(expr, tr);
            Tensor exp = Transformation.Util.applySequentially(expr, subs);
            TAssert.assertEquals(ac, exp);
        }
    }


    static final class BruteForceProductSubs implements Transformation {
        final PrimitiveProductSubstitution ps;

        public BruteForceProductSubs(Expression expr) {
            this.ps = new PrimitiveProductSubstitution(expr.get(0), expr.get(1));
        }

        @Override
        public Tensor transform(Tensor t) {
            SubstitutionIterator it = new SubstitutionIterator(t);
            Tensor c;
            while ((c = it.next()) != null)
                if (c instanceof Product)
                    it.safeSet(ps.algorithm_subgraph_search((Product) c, it));
            return it.result();
        }
    }
}
