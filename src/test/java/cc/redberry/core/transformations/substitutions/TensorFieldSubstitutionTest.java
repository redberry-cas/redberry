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
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertParity;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.tensor.Tensors.parse;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorFieldSubstitutionTest {

    public TensorFieldSubstitutionTest() {
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
    public void test1() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^d+w_c^d]");
        target = t.transform(target);
        System.out.println(target);
        assertParity(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void test12() {
        Tensor from = parse("f_a[x_a]");
        Tensor to = parse("x_a");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a[x^a]");
        target = t.transform(target);
        System.out.println(target);
        assertParity(target, "x_a");
    }

    @Test
    public void testSubs1() {
        TensorField from = (TensorField) parse("f[x]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        System.out.println(target);
        assertTrue(TensorUtils.equals(target, parse("g+y")));
    }

    @Test
    public void testSubs2() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("g_a+y_a")));
    }

    @Test
    public void testSubs3() {
        TensorField from = (TensorField) parse("f_m[x_i,y_j]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p,k_k]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("g_a+k_a")));
    }

    @Test
    public void testSubs5() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g^p]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        System.out.println(target);
        target = contract(target);
        System.out.println(target);
        assertTrue(TensorUtils.equals(target, parse("g_a+y_a")));
    }

    @Test
    public void testSubs6() {
        TensorField from = (TensorField) parse("f_m[x_i,y^k]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f^a[X^i,Y_j]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        System.out.println(target);
        target = contract(target);
        assertTrue(TensorUtils.equals(target, parse("X^a+Y^a")));
    }

    @Test
    public void testSubs7() {
        TensorField from = (TensorField) parse("f_m[x_i,y^kpq]");
        Tensor to = parse("x_m+y^i_i_m");
        Tensor target = parse("f^a[X^i,Y_jzx]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        target = contract(target);
        assertTrue(TensorUtils.equals(target, parse("X^a+Y^i_i^a")));
    }

    @Test
    public void testSubs8() {
        Tensor target = parse("V_mni[p_m-k_m,k_m]*D^mnab[k_a]*V_abj[k_m,p_m-k_m]");
        target = substitute(target, "D_mnab[k_p]=(g_ma*g_nb+g_mb*g_an-g_mn*g_ab)*1/(k_p*k^p)");
        target = substitute(target, "V_mni[k_p,q_p]=k_m*g_ni-q_m*g_ni");


        target = expand(target);
        target = contract(target);

        Tensor expected = parse(target.toString(ToStringMode.REDBERRY_SOUT));
        assertTrue(TensorUtils.equals(target, expected));
    }

    @Test
    public void testSubs9() {
        TensorField from = (TensorField) parse("f[x]");
        Tensor to = parse("x+y");
        Tensor target = parse("f[g]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("g+y")));
    }

    @Test
    public void testSubs10() {
        TensorField from = (TensorField) parse("f_m[x_i]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("g_a+y_a")));
    }

    @Test
    public void testSubs11() {
        TensorField from = (TensorField) parse("f_m[x_i,y_j]");
        Tensor to = parse("x_m+y_m");
        Tensor target = parse("f_a[g_p,k_k]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("g_a+k_a")));
    }

//    @Test
//    public void testSubs12() {
//        TensorField from = (TensorField) parse("f_m[x_i]");
//        Tensor to = parse("x_m+y_m");
//        Tensor target = parse("f_a[g^p]");
//        Transformation transformation = Substitutions.getTransformation(from, to);
//        target = transformation.transform(target);
//        assertTrue(TensorUtils.equals(target, parse("f_{a}[g^{p}]")));
//    }
    @Test
    public void testSubs13() {
        TensorField from = (TensorField) parse("f_ab[x_mn]");
        Tensor to = parse("x_ab");
        Tensor target = parse("f_mn[z_i*y_j]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("z_{m}*y_{n}")));
    }

    @Test
    public void testSubs14() {
        TensorField from = (TensorField) parse("f_ab[x_mn]");
        Tensor to = parse("x_ab");
        Tensor target = parse("f_mn[y_j*z_i]");
        Transformation transformation = Substitutions.getTransformation(from, to);
        target = transformation.transform(target);
        assertTrue(TensorUtils.equals(target, parse("z_{m}*y_{n}")));
    }

    @Test
    public void subs16_derivative() {
        Tensor target = parse("L[x^n]");
        target = substitute(target,
                            "L[x_m]=D[f_m,x_m]");
    }

    @Test
    public void rimanTensorSubstitution_diffStates2() {

        //Riman without diff states
        Tensor target = parse("Rf[g_mn]");
        target = substitute(target,
                            "Rf[g_ab]=g^ab*Rf_ab[g_mn]");
        target = substitute(target,
                            "Rf_{mn}[g^mn]=Rf^{a}_{man}[g_pq]");
        target = substitute(target,
                            "Rf^a_bmn[g^pq]=p_m*Gf^a_bn[g_ab]+p_n*Gf^a_bm[g_ab]+Gf^a_gm[g_ab]*Gf^g_bn[g_ab]-Gf^a_gn[g_ab]*Gf^g_bm[g_ab]");
        target = substitute(target,
                            "Gf^a_mn[r^mn]=(1/2)*r^ag*(p_m*r_gn+p_n*r_gm-p_g*r_mn)");

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
    public void safiuasiofu() {
        Tensor target = parse("Gf^a_gm[g_ab]*Gf^g_bn[g_ab]");
        target = substitute(target,
                            "Gf^a_mn[r^mn]=(1/2)*r^ag*p_m*r_gn");

    }

    @Test
    public void rimanTensorSubstitution_diffStates3() {

        //Riman without diff states
        Tensor target = parse("g^{ab}*p_{b}*Gf^{c}_{ac}[g_{ab}]");
        substitute(target,
                   "Gf^a_mn[r^mn]=(1/2)*r^ag*(p_m*r_gn+p_n*r_gm-p_g*r_mn)");

        target = contract(target);

    }

    @Test
    public void test2() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^a+w_c^a:_c^a]");
        target = t.transform(target);
        assertParity(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void test3() {
        Tensor from = parse("f_a^b[x_m^n:_m^n]");
        Tensor to = parse("x_a^b+y_a^b");
        Transformation t = Substitutions.getTransformation(from, to);
        Tensor target = parse("f_a^a[z_c^a+w_c^a:_c^a]");
        target = t.transform(target);
        assertParity(target, "z_{m}^{m}+w_{m}^{m}+y_{m}^{m}");
    }

    @Test
    public void testFieldDepth1() {
        
    }
    //TODO additional tests with specified field arguments indices
}