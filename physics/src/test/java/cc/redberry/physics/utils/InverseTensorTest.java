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

package cc.redberry.physics.utils;

import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.ExpressionFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.factor.FactorTransformation;
import cc.redberry.core.transformations.symmetrization.SymmetrizeUpperLowerIndicesTransformation;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class InverseTensorTest {

    public final String mapleBinDir;
    public final String temporaryDir;

    public InverseTensorTest() {
        String mapleBinDir;
        mapleBinDir = System.getenv("MAPLE");
        if (mapleBinDir == null) {
            System.out.println("No MAPLE enviroment variable specified.");
            mapleBinDir = System.getProperty("redberry.maple");
        }
        if (mapleBinDir == null)
            System.out.println("No maple directory specified.");
        else
            System.out.println("MAPLE directory: " + mapleBinDir);

        this.mapleBinDir = mapleBinDir;
        temporaryDir = System.getProperty("java.io.tmpdir");
    }

    @Before
    public void beforeMethod() {
        Assume.assumeTrue(mapleBinDir != null);
    }

    @Test
    public void testVectorField1() {
        Transformation[] transformations = new Transformation[]{Tensors.parseExpression("k_a*k^a=1")};
        Expression toInverse = Tensors.parseExpression("D_mn = k_m*k_n-(1/a)*k_i*k^i*g_mn");
        Expression equation = Tensors.parseExpression("D_ab*K^ac=d_b^c");
        Tensor[] samples = {Tensors.parse("g_mn"), Tensors.parse("g^mn"), Tensors.parse("d_m^n"), Tensors.parse("k_m"), Tensors.parse("k^b")};
        Tensor expected = Tensors.parse("K^ac=-a*g^ac+a**2/(a-1)*k^a*k^c");
        try {
            Tensor actual = InverseTensor.findInverseWithMaple(toInverse, equation, samples, false, transformations, mapleBinDir, temporaryDir);
            Assert.assertTrue(TensorUtils.equals(expected, actual));
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }

    @Test
    public void testVectorField1a() {
        Transformation[] transformations = new Transformation[]{Tensors.parseExpression("k_a*k^a=1")};
        Expression toInverse = Tensors.parseExpression("D_mn = k_m*k_n-(1/c1)*k_i*k^i*g_mn");
        Expression equation = Tensors.parseExpression("D_ab*K^ac=d_b^c");
        Tensor[] samples = {Tensors.parse("g_mn"), Tensors.parse("g^mn"), Tensors.parse("d_m^n"), Tensors.parse("k_m"), Tensors.parse("k^b")};
        Tensor expected = Tensors.parse("K^ac=-c1*g^ac+c1**2/(c1-1)*k^a*k^c");
        try {
            Tensor actual = InverseTensor.findInverseWithMaple(toInverse, equation, samples, false, transformations, mapleBinDir, temporaryDir);
            Assert.assertTrue(TensorUtils.equals(expected, actual));
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }

    @Test
    public void testVectorField2() {
        Expression toInverse = Tensors.parseExpression("D_mn = k_m*k_n-(1/a)*k_i*k^i*g_mn");
        Expression equation = Tensors.parseExpression("D_ab*K^ac=d_b^c");
        Tensor[] samples = {Tensors.parse("g_mn"), Tensors.parse("g^mn"), Tensors.parse("d_m^n"), Tensors.parse("k_m"), Tensors.parse("k^b")};
        try {
            Tensor expected = Tensors.parse("K^ac=-a*g^ac*(k_i*k^i)**(-1)+a**2/(a-1)*k^a*k^c*(k_i*k^i)**(-2)");
            Tensor actual = InverseTensor.findInverseWithMaple(toInverse, equation, samples, false, new Transformation[0], mapleBinDir, temporaryDir);
            Assert.assertTrue(TensorUtils.equals(expected, actual));
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }

    @Test
    public void example1() {
        Expression toInverse = Tensors.parseExpression("D_mn = k_m*k_n-(1/a)*k_i*k^i*g_mn");
        Expression equation = Tensors.parseExpression("D_ab*K^ac=d_b^c");
        Tensor[] samples = {Tensors.parse("g_mn"), Tensors.parse("g^mn"), Tensors.parse("d_m^n"), Tensors.parse("k_m"), Tensors.parse("k^b")};
        InverseTensor inverseTensor = new InverseTensor(toInverse, equation, samples);
        System.out.println(inverseTensor.getGeneralInverseForm());
        System.out.println(Arrays.toString(inverseTensor.getEquations()));

    }

    @Test
    public void test2() {
        Transformation[] transformations = new Transformation[]{Tensors.parseExpression("n_a*n^a=1"), Tensors.parseExpression("d_a^a=4")};

        Tensor toInv = Tensors.parse("d_p^a*d_q^b*d_r^c+"
                + "6*(-1/2+l*b**2)*g_pq*g^ab*d_r^c+"
                + "3*(-1+l)*n_p*n^a*d_q^b*d_r^c+"
                + "6*(1/2+l*b)*(n_p*n_q*g^ab*d_r^c+n^a*n^b*g_pq*d_r^c)+"
                + "6*(-1/4+l*b**2)*n_p*g_qr*n^a*g^bc");
        Expression toInverse = ExpressionFactory.FACTORY.create(Tensors.parseSimple("K^abc_pqr"),
                SymmetrizeUpperLowerIndicesTransformation.symmetrizeUpperLowerIndices(toInv, true));

        Tensor eqRhs = Tensors.parse("d_i^a*d_j^b*d_k^c");
        Expression equation = ExpressionFactory.FACTORY.create(Tensors.parse("K^abc_pqr*KINV^pqr_ijk"),
                ExpandTransformation.expand(SymmetrizeUpperLowerIndicesTransformation.symmetrizeUpperLowerIndices(eqRhs, true)));

        Tensor[] samples = {Tensors.parse("g_mn"), Tensors.parse("g^mn"), Tensors.parse("d_m^n"), Tensors.parse("n_m"), Tensors.parse("n^b")};

        Tensor expected = Tensors.parse("KINV^{pqr}_{ijk} = -1/4*(1+b)**(-1)*(3*l-24*b**2-36*b-14+12*l*b**2+12*l*b)**(-1)*l**(-1)*(-14*l+32*b**3+80*b**2+64*b+16+24*l**2*b**3+36*l**2*b**2+18*l**2*b+3*l**2-32*l*b**3-72*l*b**2-56*l*b)*(g_{jk}*n_{i}*n^{p}*n^{q}*n^{r}+g_{ij}*n_{k}*n^{p}*n^{q}*n^{r}+g_{ik}*n_{j}*n^{p}*n^{q}*n^{r})-1/12*(g_{ij}*g^{pr}*d_{k}^{q}+g_{ik}*d_{j}^{q}*g^{pr}+g_{ij}*d_{k}^{p}*g^{qr}+d_{i}^{p}*g^{qr}*g_{jk}+g_{ik}*g^{pq}*d_{j}^{r}+d_{i}^{r}*g_{jk}*g^{pq}+g_{ik}*d_{j}^{p}*g^{qr}+d_{i}^{q}*g_{jk}*g^{pr}+g_{ij}*d_{k}^{r}*g^{pq})-1/6*l**(-1)*(-1+l)*(d_{i}^{q}*d_{k}^{p}*n_{j}*n^{r}+d_{j}^{p}*d_{k}^{r}*n_{i}*n^{q}+d_{i}^{q}*d_{j}^{p}*n_{k}*n^{r}+d_{i}^{p}*d_{k}^{q}*n_{j}*n^{r}+d_{k}^{r}*d_{j}^{q}*n_{i}*n^{p}+d_{k}^{p}*d_{j}^{q}*n_{i}*n^{r}+d_{i}^{p}*d_{j}^{q}*n_{k}*n^{r}+d_{j}^{p}*d_{k}^{q}*n_{i}*n^{r}+d_{k}^{p}*d_{j}^{r}*n_{i}*n^{q}+d_{i}^{q}*d_{j}^{r}*n_{k}*n^{p}+d_{i}^{p}*d_{j}^{r}*n_{k}*n^{q}+d_{k}^{q}*d_{j}^{r}*n_{i}*n^{p}+d_{i}^{r}*d_{k}^{p}*n_{j}*n^{q}+d_{i}^{r}*d_{k}^{q}*n_{j}*n^{p}+d_{i}^{r}*d_{j}^{p}*n_{k}*n^{q}+d_{i}^{q}*d_{k}^{r}*n_{j}*n^{p}+d_{i}^{p}*d_{k}^{r}*n_{j}*n^{q}+d_{i}^{r}*d_{j}^{q}*n_{k}*n^{p})-1/4*l**(-1)*(3*l-24*b**3-60*b**2-50*b-14+12*l*b**3+24*l*b**2+15*l*b)**(-1)*(-14*l+32*b**3+80*b**2+64*b+16+24*l**2*b**3+36*l**2*b**2+18*l**2*b+3*l**2-32*l*b**3-72*l*b**2-56*l*b)*(g^{pq}*n_{i}*n_{j}*n_{k}*n^{r}+g^{qr}*n_{i}*n_{j}*n_{k}*n^{p}+g^{pr}*n_{i}*n_{j}*n_{k}*n^{q})+1/6*(d_{i}^{q}*d_{k}^{p}*d_{j}^{r}+d_{i}^{p}*d_{k}^{q}*d_{j}^{r}+d_{i}^{r}*d_{k}^{p}*d_{j}^{q}+d_{i}^{r}*d_{j}^{p}*d_{k}^{q}+d_{i}^{q}*d_{j}^{p}*d_{k}^{r}+d_{i}^{p}*d_{k}^{r}*d_{j}^{q})+1/12*(1+b)**(-1)*(2*b+1)*(g_{ij}*d_{k}^{q}*n^{p}*n^{r}+g_{ik}*d_{j}^{p}*n^{q}*n^{r}+g_{ik}*d_{j}^{q}*n^{p}*n^{r}+g_{ik}*d_{j}^{r}*n^{p}*n^{q}+d_{i}^{r}*g_{jk}*n^{p}*n^{q}+g_{ij}*d_{k}^{r}*n^{p}*n^{q}+g_{ij}*d_{k}^{p}*n^{q}*n^{r}+d_{i}^{q}*g_{jk}*n^{p}*n^{r}+d_{i}^{p}*g_{jk}*n^{q}*n^{r})+1/12*(1+b)**(-1)*(2*b+1)*(g^{pq}*d_{j}^{r}*n_{i}*n_{k}+d_{k}^{p}*g^{qr}*n_{i}*n_{j}+d_{i}^{p}*g^{qr}*n_{j}*n_{k}+d_{i}^{r}*g^{pq}*n_{j}*n_{k}+d_{j}^{p}*g^{qr}*n_{i}*n_{k}+d_{i}^{q}*g^{pr}*n_{j}*n_{k}+d_{k}^{r}*g^{pq}*n_{i}*n_{j}+g^{pr}*d_{k}^{q}*n_{i}*n_{j}+d_{j}^{q}*g^{pr}*n_{i}*n_{k})-1/12*(1+b)**(-2)*l**(-1)*(-3*l+8*b**2+16*b+6+4*l*b**2-4*l*b)*(d_{i}^{r}*n_{j}*n_{k}*n^{p}*n^{q}+d_{k}^{r}*n_{i}*n_{j}*n^{p}*n^{q}+d_{k}^{p}*n_{i}*n_{j}*n^{q}*n^{r}+d_{i}^{q}*n_{j}*n_{k}*n^{p}*n^{r}+d_{i}^{p}*n_{j}*n_{k}*n^{q}*n^{r}+d_{k}^{q}*n_{i}*n_{j}*n^{p}*n^{r}+d_{j}^{p}*n_{i}*n_{k}*n^{q}*n^{r}+d_{j}^{q}*n_{i}*n_{k}*n^{p}*n^{r}+d_{j}^{r}*n_{i}*n_{k}*n^{p}*n^{q})+1/12*(3*l-24*b**2-36*b-14+12*l*b**2+12*l*b)**(-1)*l**(-1)*(-14*l+32*b**2+48*b+18+12*l**2*b**2+12*l**2*b+3*l**2-24*l*b**2-36*l*b)*(g^{qr}*g_{jk}*n_{i}*n^{p}+g_{ij}*g^{qr}*n_{k}*n^{p}+g_{ik}*g^{qr}*n_{j}*n^{p}+g_{jk}*g^{pr}*n_{i}*n^{q}+g_{ij}*g^{pr}*n_{k}*n^{q}+g_{ik}*g^{pr}*n_{j}*n^{q}+g_{jk}*g^{pq}*n_{i}*n^{r}+g_{ij}*g^{pq}*n_{k}*n^{r}+g_{ik}*g^{pq}*n_{j}*n^{r})+3/4*(2*b+1+b**2)**(-1)*(3*l-24*b**2-36*b-14+12*l*b**2+12*l*b)**(-1)*(12*l-64*b**4-224*b**3-256*b**2-120*b-l**2-20+32*l**2*b**2+80*l**2*b**4+96*l**2*b**3-16*l*b**3-64*l*b**4+80*l*b**2+60*l*b)*l**(-1)*n_{i}*n_{j}*n_{k}*n^{p}*n^{q}*n^{r}");
        expected = FactorTransformation.factor(expected);
        try {
            Tensor actual = InverseTensor.findInverseWithMaple(toInverse, equation, samples, true, transformations, mapleBinDir, temporaryDir);
            Assert.assertTrue(TensorUtils.equals(expected, actual));
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }

    @Test
    public void test3() {
        Transformation[] transformations = new Transformation[]{Tensors.parseExpression("d_a^a=4")};

        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");
        Expression equation = Tensors.parseExpression("F_p^mn_q^rs*iF^p_mn^a_bc=d^a_q*d_b^r*d_c^s-1/4*d^r_q*d_b^a*d_c^s");

        Tensor[] samples = {Tensors.parse("g_mn"), Tensors.parse("g^mn"), Tensors.parse("d_m^n")};
        try {
            Tensor actual = InverseTensor.findInverseWithMaple(
                    toInverse,
                    equation,
                    samples,
                    false,
                    false,
                    transformations,
                    mapleBinDir,
                    temporaryDir);
            System.out.println(actual);
            //TODO check answer
            Assert.assertTrue(actual != null);
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }

    @Test
    public void testDirac1() {
        Expression t = Tensors.parseExpression("K^i_j = p_\\mu*G^{\\mu i}_j - m* d^i_j");
        Expression eq = Tensors.parseExpression("K^i_j * D^j_k = d^i_k");
        Tensor[] samples = new Tensor[]{
                Tensors.parse("d^i_j"),
                Tensors.parse("p_\\mu*G^{\\mu i}_j")};
        Transformation[] tr = new Transformation[]{Tensors.parseExpression("p_\\mu*p_\\nu*G^{\\mu i}_j*G^{\\nu j}_k = p_\\mu*p^\\mu*d^i_k")};
        try {
            Tensor r = InverseTensor.findInverseWithMaple(t, eq, samples, false, tr, mapleBinDir, temporaryDir);
            Tensor expected = Tensors.parse("D^{j}_{k} = -m*(m**2-p_{\\mu }*p^{\\mu })**(-1)*d^{j}_{k}-(m**2-p_{\\mu }*p^{\\mu })**(-1)*G^{j}_{k}^{\\mu }*p_{\\mu }");
            Assert.assertTrue(TensorUtils.equals(r, expected));
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }

    @Test
    public void testDirac2() {
        Expression t = Tensors.parseExpression("K^i_j = p_\\mu*G^{\\mu i}_j - m* d^i_j");
        Expression eq = Tensors.parseExpression("K^i_j * D^j_k = I * d^i_k");
        Tensor[] samples = new Tensor[]{
                Tensors.parse("d^i_j"),
                Tensors.parse("p_\\mu*G^{\\mu i}_j")};
        Transformation[] tr = new Transformation[]{Tensors.parseExpression("p_\\mu*p_\\nu*G^{\\mu i}_j*G^{\\nu j}_k = p_\\mu*p^\\mu*d^i_k")};
        try {
            Tensor r = InverseTensor.findInverseWithMaple(t, eq, samples, false, tr, mapleBinDir, temporaryDir);
            Tensor expected = Tensors.parse("D^{j}_{k} = -I*m*(m**2-p_{\\mu }*p^{\\mu })**(-1)*d^{j}_{k}-I*(m**2-p_{\\mu }*p^{\\mu })**(-1)*G^{j}_{k}^{\\mu }*p_{\\mu }");
            Assert.assertTrue(TensorUtils.equals(r, expected));
        } catch (IOException | InterruptedException e) {
            // do nothing since MAPLE failed
        }
    }
}