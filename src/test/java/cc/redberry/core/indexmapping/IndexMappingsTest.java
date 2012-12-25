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
package cc.redberry.core.indexmapping;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.expand.Expand;
import cc.redberry.core.utils.TensorHashCalculator;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static cc.redberry.core.tensor.Tensors.*;

public class IndexMappingsTest {

    @Test
    public void test1() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("A_a*B_bc-A_b*B_ac");
            MappingsPort opu = IndexMappings.createPort(t, t);
            byte mask = 0;
            IndexMappingBuffer buffer;
            while ((buffer = opu.take()) != null)
                mask |= buffer.getSignum() ? 2 : 1;
            Assert.assertTrue(mask == 3);
        }
    }

    @Test
    public void test2() {
        addSymmetry("F_mn", IndexType.LatinLower, true, 1, 0);
        Tensor from = parse("F_mn*F^mn");
        Tensor to = parse("F_mn*F^nm");
        MappingsPort mp = IndexMappings.createPort(from, to);
        IndexMappingBuffer buffer;
        boolean sign = false;
        while ((buffer = mp.take()) != null)
            if (buffer.getSignum()) {
                sign = true;
                break;
            }
        Assert.assertTrue(sign);
    }

    @Test
    public void test3() {
        Tensor f = parse("a*b");
        Tensor t = parse("-a*b");
        MappingsPort mp = IndexMappings.createPort(f, t);

        IndexMappingBuffer buffer;
        boolean sign = false;
        while ((buffer = mp.take()) != null) {
            if (!sign)
                if (buffer.getSignum())
                    sign = true;
            break;
        }
        Assert.assertTrue(sign);
    }

    @Test
    public void test4() {
        Tensor f = parse("1");
        Tensor t = parse("-1");
        MappingsPort mp = IndexMappings.createPort(f, t);
        IndexMappingBuffer buffer;

        boolean sign = false;
        while ((buffer = mp.take()) != null)
            if (!sign)
                if (buffer.getSignum()) {
                    sign = true;
                    break;
                }
        Assert.assertTrue(sign);
    }

    @Test
    public void test5() {
        Tensor f = parse("A_ab^ab-d");
        Tensor t = parse("A_ba^ab+d");
        Tensors.addSymmetry("A_abmn", IndexType.LatinLower, true, 0, 1, 3, 2);
        MappingsPort mp = IndexMappings.createPort(f, t);

        IndexMappingBuffer buffer;
        boolean sign = false;
        while ((buffer = mp.take()) != null)
            if (buffer.getSignum()) {
                sign = true;
                break;
            }
        Assert.assertTrue(sign);

    }

    @Test(timeout = 2000)
    public void testScalarTensors5() {
        Tensor t1 = parse("a+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+ta+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+ta+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+ta+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t");
        Tensor t2 = parse("a+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+ta+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+ta+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+ta+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t+a+b+c+d+f+x+y+z+e+w+q+r+t");
        IndexMappingBuffer buffer = IndexMappings.createPort(t1, t2).take();
        Assert.assertTrue(buffer != null);
    }

    @Test
    public void testScalarTensors1() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t1 = parse("A_mn*B^mnpqr*A_pqr");
            Tensor t2 = parse("A_pq*B^mnpqr*A_mnr");
            Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(t1, t2);
            Assert.assertTrue(buffers.isEmpty());
        }
    }

    @Test
    public void testScalarTensors12() {
        Tensor t1 = parse("A_i*A^i");
        Tensor t2 = parse("A_i*A^i");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(t1, t2);
        Assert.assertTrue(buffers.size() >= 1);
    }

    @Test
    public void testScalarTensors2() {
        addSymmetry("B^abcde", IndexType.LatinLower, false, 2, 3, 0, 1, 4);
        Tensor t1 = parse("A_mn*B^mnpqr*A_pqr");
        Tensor t2 = parse("A_pq*B^mnpqr*A_mnr");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(t1, t2);
        Assert.assertTrue(buffers.size() == 1);
        Assert.assertTrue(buffers.iterator().next().isEmpty());
    }

    @Test
    public void testScalarTensors3() {
        Tensor t1 = parse("A_m^m*A_a^b");
        Tensor t2 = parse("A_a^n*A_n^b");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(t1, t2);
        Assert.assertTrue(buffers.isEmpty());
    }

    @Test
    public void testScalarTensors4() {
        Tensor t1 = parse("A_m^m");
        Tensor t2 = parse("A_a^n");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(t1, t2);
        Assert.assertTrue(buffers.isEmpty());
    }

    @Test
    public void testRimanSymmetries1() {
        addSymmetry("G^a_bc", IndexType.LatinLower, false, 0, 2, 1);
        addSymmetry("g_ab", IndexType.LatinLower, false, 1, 0);
        Tensor riman1 = parse("g_ax*(d_c*G^x_bd-d_d*G^x_bc+G^x_yc*G^y_bd-G^x_yd*G^y_bc)");
        Tensor riman2 = parse("g_px*(d_r*G^x_qs-d_s*G^x_qr+G^x_yr*G^y_qs-G^x_ys*G^y_qr)");

        MappingsPort mp = IndexMappings.createPort(riman1, riman2);
        IndexMappingBuffer buffera;
        while ((buffera = mp.take()) != null)
            System.out.println(buffera);

        //R_abcd -> R_pqrs
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(riman1, riman2);
        IndexMappingBuffer[] target = buffers.toArray(new IndexMappingBuffer[2]);


        IndexMappingBuffer[] expected = new IndexMappingBuffer[2];
        expected[0] = IndexMappingTestUtils.parse("+;_a->_p;_b->_q;_c->_r;_d->_s");
        expected[1] = IndexMappingTestUtils.parse("-;_a->_p;_b->_q;_c->_s;_d->_r");

        Arrays.sort(target, IndexMappingTestUtils.getComparator());
        Arrays.sort(expected, IndexMappingTestUtils.getComparator());

        Assert.assertTrue(Arrays.equals(expected, target));
    }

    @Test
    public void test6() {
        IndexMappingBuffer actual = IndexMappings.createPort(parse("A_mn*(a-b)"), parse("A_pq*(b-a)")).take();
        IndexMappingBuffer expected = IndexMappingTestUtils.parse("-;_m->_p;_n->_q");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test7() {
        IndexMappingBuffer actual = IndexMappings.createPort(parse("A_mn+B_nm"), parse("-A_pq-B_qp")).take();
        IndexMappingBuffer expected = IndexMappingTestUtils.parse("-;_m->_p;_n->_q");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test8() {
        IndexMappingBuffer actual = IndexMappings.createPort(parse("g_ax*(d_c*G^x_bd-d_d*G^x_bc)"), parse("g_px*(d_r*G^x_qs-d_s*G^x_qr)")).take();
        IndexMappingBuffer expected = IndexMappingTestUtils.parse("+;_a->_p;_b->_q;_c->_r;_d->_s");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testBuildSimpleTensor1() {
        Tensor from = parse("R^{\\alpha}_{\\beta \\mu \\nu}");
        Tensor to = parse("R^{\\alpha}_{\\mu \\beta \\nu}");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(from, to);
        Assert.assertTrue(buffers.size() == 1);
        IndexMappingBuffer target = buffers.iterator().next();
//        System.out.println(target);
        IndexMappingBufferImpl expected = IndexMappingTestUtils.parse(
                "+;^\\alpha->^\\alpha;_\\beta->_\\mu;_\\mu->_\\beta;_\\nu->_\\nu");
//        System.out.println(expected);
        Assert.assertTrue(target.equals(expected));
    }

    @Test
    public void testScalarFunctions2() {
        Tensor from = parse("g_mn*Sin[x]");
        Tensor to = parse("g_ab*Sin[x]");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(from, to);
        Assert.assertTrue(buffers.size() == 2);
    }

    @Test
    public void testDiffStates() {
        Tensor from = parse("Tensor_mn");
        Tensor to = parse("Tensor^ab");
        Set<IndexMappingBuffer> buffers = IndexMappings.getAllMappings(from, to);
        Assert.assertTrue(buffers.size() == 1);
        IndexMappingBuffer expected = IndexMappingTestUtils.parse("+;_m->^a;_n->^b");
        Assert.assertTrue(expected.equals(buffers.iterator().next()));
    }

    @Test
    public void testField1() {
        Tensor from = parse("g_mn*f[x]");
        Tensor to = parse("g_ab*f[x]");
        Assert.assertTrue(IndexMappings.createPort(from, to).take() != null);
    }

    @Test
    public void testField2() {
        Tensor from = parse("g_mn*f[x]");
        Tensor to = parse("g_ab*f[y]");
        Assert.assertTrue(IndexMappings.createPort(from, to).take() == null);
    }

    @Test
    public void scalarSign() {
        addSymmetry("R_abcd", IndexType.LatinLower, true, new int[]{0, 1, 3, 2});
        Tensor from = parse("R_{abcd}*R^abcd");
        Tensor to = parse("R_abcd*R^abdc");
        MappingsPort opu = IndexMappings.createPort(from, to);
        IndexMappingBuffer buffer;
        while ((buffer = opu.take()) != null)
            Assert.assertTrue(buffer.getSignum());
    }

    @Ignore
    @Test
    public void performanceTest() {
        Tensor from = Tensors.parse("f^{\\gamma }_{\\epsilon }*f^{\\delta }_{\\phi }*f^{\\mu_{9} }_{\\psi }*n_{\\upsilon }*n_{\\chi }*d^{\\alpha }_{\\gamma }*d^{\\nu }_{\\delta }*g^{\\upsilon \\phi }*g^{\\chi \\psi }*g^{\\mu \\epsilon }*d^{\\beta }_{\\nu_{9} }+f^{\\gamma }_{\\zeta }*f^{\\delta }_{\\alpha_1 }*f^{\\mu_{9} }_{\\gamma_1 }*n_{\\omega }*n_{\\beta_1 }*d^{\\beta }_{\\gamma }*d^{\\nu }_{\\delta }*g^{\\omega \\alpha_1 }*g^{\\beta_1 \\gamma_1 }*g^{\\mu \\zeta }*d^{\\alpha }_{\\nu_{9} }+f^{\\gamma }_{\\eta }*f^{\\delta }_{\\epsilon_1 }*f^{\\mu_{9} }_{\\eta_1 }*n_{\\delta_1 }*n_{\\zeta_1 }*d^{\\alpha }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\delta_1 \\epsilon_1 }*g^{\\zeta_1 \\eta_1 }*g^{\\mu \\eta }*d^{\\nu }_{\\nu_{9} }+f^{\\gamma }_{\\theta }*f^{\\delta }_{\\iota_1 }*f^{\\mu_{9} }_{\\lambda_1 }*n_{\\theta_1 }*n_{\\kappa_1 }*d^{\\nu }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\theta_1 \\iota_1 }*g^{\\kappa_1 \\lambda_1 }*g^{\\mu \\theta }*d^{\\alpha }_{\\nu_{9} }+f^{\\gamma }_{\\iota }*f^{\\delta }_{\\nu_1 }*f^{\\mu_{9} }_{\\pi_1 }*n_{\\mu_1 }*n_{\\xi_1 }*d^{\\nu }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\mu_1 \\nu_1 }*g^{\\xi_1 \\pi_1 }*g^{\\mu \\iota }*d^{\\beta }_{\\nu_{9} }+f^{\\gamma }_{\\kappa }*f^{\\delta }_{\\sigma_1 }*f^{\\mu_{9} }_{\\upsilon_1 }*n_{\\rho_1 }*n_{\\tau_1 }*d^{\\beta }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\rho_1 \\sigma_1 }*g^{\\tau_1 \\upsilon_1 }*g^{\\mu \\kappa }*d^{\\nu }_{\\nu_{9} }+f^{\\gamma }_{\\lambda }*f^{\\delta }_{\\chi_1 }*f^{\\mu_{9} }_{\\omega_1 }*n_{\\phi_1 }*n_{\\psi_1 }*d^{\\mu }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\phi_1 \\chi_1 }*g^{\\psi_1 \\omega_1 }*g^{\\nu \\lambda }*d^{\\beta }_{\\nu_{9} }+f^{\\gamma }_{\\xi }*f^{\\delta }_{\\beta_2 }*f^{\\mu_{9} }_{\\delta_2 }*n_{\\alpha_2 }*n_{\\gamma_2 }*d^{\\beta }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\alpha_2 \\beta_2 }*g^{\\gamma_2 \\delta_2 }*g^{\\nu \\xi }*d^{\\mu }_{\\nu_{9} }+f^{\\gamma }_{\\pi }*f^{\\delta }_{\\zeta_2 }*f^{\\mu_{9} }_{\\theta_2 }*n_{\\epsilon_2 }*n_{\\eta_2 }*d^{\\alpha }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\epsilon_2 \\zeta_2 }*g^{\\eta_2 \\theta_2 }*g^{\\nu \\pi }*d^{\\mu }_{\\nu_{9} }+f^{\\gamma }_{\\rho }*f^{\\delta }_{\\kappa_2 }*f^{\\mu_{9} }_{\\mu_2 }*n_{\\iota_2 }*n_{\\lambda_2 }*d^{\\mu }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\iota_2 \\kappa_2 }*g^{\\lambda_2 \\mu_2 }*g^{\\nu \\rho }*d^{\\alpha }_{\\nu_{9} }+f^{\\gamma }_{\\sigma }*f^{\\delta }_{\\xi_2 }*f^{\\mu_{9} }_{\\rho_2 }*n_{\\nu_2 }*n_{\\pi_2 }*d^{\\mu }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\nu_2 \\xi_2 }*g^{\\pi_2 \\rho_2 }*g^{\\alpha \\sigma }*d^{\\nu }_{\\nu_{9} }+f^{\\gamma }_{\\tau }*f^{\\delta }_{\\tau_2 }*f^{\\mu_{9} }_{\\phi_2 }*n_{\\sigma_2 }*n_{\\upsilon_2 }*d^{\\nu }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\sigma_2 \\tau_2 }*g^{\\upsilon_2 \\phi_2 }*g^{\\alpha \\tau }*d^{\\mu }_{\\nu_{9} }");
        Tensor to = Tensors.parse("f^{\\delta }_{\\epsilon }*f^{\\gamma }_{\\phi }*f^{\\mu_{9} }_{\\psi }*n_{\\upsilon }*n_{\\chi }*d^{\\alpha }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\upsilon \\phi }*g^{\\chi \\psi }*g^{\\mu \\epsilon }*d^{\\nu }_{\\nu_{9} }+f^{\\delta }_{\\zeta }*f^{\\gamma }_{\\alpha_1 }*f^{\\mu_{9} }_{\\gamma_1 }*n_{\\omega }*n_{\\beta_1 }*d^{\\beta }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\omega \\alpha_1 }*g^{\\beta_1 \\gamma_1 }*g^{\\mu \\zeta }*d^{\\nu }_{\\nu_{9} }+f^{\\delta }_{\\eta }*f^{\\gamma }_{\\epsilon_1 }*f^{\\mu_{9} }_{\\eta_1 }*n_{\\delta_1 }*n_{\\zeta_1 }*d^{\\alpha }_{\\gamma }*d^{\\nu }_{\\delta }*g^{\\delta_1 \\epsilon_1 }*g^{\\zeta_1 \\eta_1 }*g^{\\mu \\eta }*d^{\\beta }_{\\nu_{9} }+f^{\\delta }_{\\theta }*f^{\\gamma }_{\\iota_1 }*f^{\\mu_{9} }_{\\lambda_1 }*n_{\\theta_1 }*n_{\\kappa_1 }*d^{\\nu }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\theta_1 \\iota_1 }*g^{\\kappa_1 \\lambda_1 }*g^{\\mu \\theta }*d^{\\beta }_{\\nu_{9} }+f^{\\delta }_{\\iota }*f^{\\gamma }_{\\nu_1 }*f^{\\mu_{9} }_{\\pi_1 }*n_{\\mu_1 }*n_{\\xi_1 }*d^{\\nu }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\mu_1 \\nu_1 }*g^{\\xi_1 \\pi_1 }*g^{\\mu \\iota }*d^{\\alpha }_{\\nu_{9} }+f^{\\delta }_{\\kappa }*f^{\\gamma }_{\\sigma_1 }*f^{\\mu_{9} }_{\\upsilon_1 }*n_{\\rho_1 }*n_{\\tau_1 }*d^{\\beta }_{\\gamma }*d^{\\nu }_{\\delta }*g^{\\rho_1 \\sigma_1 }*g^{\\tau_1 \\upsilon_1 }*g^{\\mu \\kappa }*d^{\\alpha }_{\\nu_{9} }+f^{\\delta }_{\\lambda }*f^{\\gamma }_{\\chi_1 }*f^{\\mu_{9} }_{\\omega_1 }*n_{\\phi_1 }*n_{\\psi_1 }*d^{\\mu }_{\\gamma }*d^{\\beta }_{\\delta }*g^{\\phi_1 \\chi_1 }*g^{\\psi_1 \\omega_1 }*g^{\\nu \\lambda }*d^{\\alpha }_{\\nu_{9} }+f^{\\delta }_{\\xi }*f^{\\gamma }_{\\beta_2 }*f^{\\mu_{9} }_{\\delta_2 }*n_{\\alpha_2 }*n_{\\gamma_2 }*d^{\\beta }_{\\gamma }*d^{\\mu }_{\\delta }*g^{\\alpha_2 \\beta_2 }*g^{\\gamma_2 \\delta_2 }*g^{\\nu \\xi }*d^{\\alpha }_{\\nu_{9} }+f^{\\delta }_{\\pi }*f^{\\gamma }_{\\zeta_2 }*f^{\\mu_{9} }_{\\theta_2 }*n_{\\epsilon_2 }*n_{\\eta_2 }*d^{\\alpha }_{\\gamma }*d^{\\mu }_{\\delta }*g^{\\epsilon_2 \\zeta_2 }*g^{\\eta_2 \\theta_2 }*g^{\\nu \\pi }*d^{\\beta }_{\\nu_{9} }+f^{\\delta }_{\\rho }*f^{\\gamma }_{\\kappa_2 }*f^{\\mu_{9} }_{\\mu_2 }*n_{\\iota_2 }*n_{\\lambda_2 }*d^{\\mu }_{\\gamma }*d^{\\alpha }_{\\delta }*g^{\\iota_2 \\kappa_2 }*g^{\\lambda_2 \\mu_2 }*g^{\\nu \\rho }*d^{\\beta }_{\\nu_{9} }+f^{\\delta }_{\\sigma }*f^{\\gamma }_{\\xi_2 }*f^{\\mu_{9} }_{\\rho_2 }*n_{\\nu_2 }*n_{\\pi_2 }*d^{\\mu }_{\\gamma }*d^{\\nu }_{\\delta }*g^{\\nu_2 \\xi_2 }*g^{\\pi_2 \\rho_2 }*g^{\\alpha \\sigma }*d^{\\beta }_{\\nu_{9} }+f^{\\delta }_{\\tau }*f^{\\gamma }_{\\tau_2 }*f^{\\mu_{9} }_{\\phi_2 }*n_{\\sigma_2 }*n_{\\upsilon_2 }*d^{\\nu }_{\\gamma }*d^{\\mu }_{\\delta }*g^{\\sigma_2 \\tau_2 }*g^{\\upsilon_2 \\phi_2 }*g^{\\alpha \\tau }*d^{\\beta }_{\\nu_{9} }");
        MappingsPort opu = IndexMappings.createPort(from, to);
        IndexMappingBuffer buffer;

        buffer = opu.take();
//        while ((buffer = opu.take()) != null)
//            Assert.assertTrue(buffer.getSignum());
    }

    @Test
    public void testScalars0() {
        Tensor from = Tensors.parse(" (a+b)*(c+d)");
        Tensor to = Tensors.parse("(-a-b)*(-c-d)");
        IndexMappingBuffer buffer = IndexMappings.createPort(from, to).take();
        Assert.assertTrue(buffer != null);
    }

    @Test
    public void testScalars1() {
        Tensor from = Tensors.parse("(1/8*b+1/4*beta*b)*(1/2*b*c+beta*b*c)");
        Tensor to = Tensors.parse("(-1/8*b-1/4*beta*b)*(-1/2*b*c-1*beta*b*c)");
        IndexMappingBuffer buffer = IndexMappings.createPort(from, to).take();
        Assert.assertTrue(buffer != null);
    }

    @Test
    public void testScalars2() {
        Tensor from = Tensors.parse("P_{\\psi }^{\\psi }_{\\gamma \\delta }*P^{\\tau \\xi }_{\\kappa_1 \\lambda_1 }*n_{\\tau }*n_{\\xi }*n^{\\epsilon }*n^{\\mu }*n^{\\zeta }*n^{\\nu }*n^{\\beta }*g^{\\theta_1 \\iota_1 }");
        Tensor to = Tensors.parse("P_{\\upsilon }^{\\upsilon }_{\\kappa_1 \\lambda_1 }*P^{\\chi \\psi }_{\\gamma \\delta }*n_{\\chi }*n_{\\psi }*n^{\\epsilon }*n^{\\nu }*n^{\\zeta }*n^{\\mu }*n^{\\beta }*g^{\\theta_1 \\iota_1 }");
        System.out.println(TensorHashCalculator.hashWithIndices(from));
        System.out.println(TensorHashCalculator.hashWithIndices(to));

        IndexMappingBuffer buffer = IndexMappings.createPort(from, to).take();
        System.out.println(buffer);

        int[] fromIndices = from.getIndices().getFree().getAllIndices().copy();
        for (int i = 0; i < fromIndices.length; ++i)
            fromIndices[i] = IndicesUtils.getNameWithType(fromIndices[i]);
        buffer = IndexMappings.createPort(new IndexMappingBufferTester(fromIndices, false), from, to).take();
        System.out.println(buffer);
    }

    @Test
    public void testPerformance1() {
        Tensor from = Tensors.parse("n^i*n^m*n^e*n_g*n_d*n^b*n^z*n^n*n_k*n^t*n_l");
        Tensor to = Tensors.parse("n_l*n^n*n_g*n^z*n^t*n^b*n^m*n_k*n^i*n_d*n^e");
        int[] fromIndices = from.getIndices().getFree().getAllIndices().copy();
        for (int i = 0; i < fromIndices.length; ++i)
            fromIndices[i] = IndicesUtils.getNameWithType(fromIndices[i]);
        long start = System.currentTimeMillis();
        IndexMappingBuffer buffer = IndexMappings.createPort(new IndexMappingBufferTester(fromIndices, false), from, to).take();
        long stop = System.currentTimeMillis();
        System.out.println(buffer);
        System.out.println("time " + (stop - start));
    }

    @Test
    public void testPerformance2() {
        Tensor from = Tensors.parse("n_{\\beta }*n_{\\sigma }*((-2*Power[beta, 2]*b+2*c*Power[beta, 2]*b+4*c*Power[beta, 2]+4*c*beta+1/2*c*b+-1+-2*beta*b+-4*Power[beta, 2]+2*c*beta*b+-4*beta+-1/2*b)*g^{\\alpha \\beta }*g^{\\epsilon \\zeta }*n_{\\eta }*n_{\\theta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\zeta }_{\\theta }*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n_{\\eta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\epsilon }_{\\theta }*n^{\\alpha }*n^{\\beta }*n^{\\zeta }*n_{\\eta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\epsilon }_{\\eta }*n^{\\alpha }*n^{\\beta }*n^{\\zeta }*n_{\\theta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\zeta }_{\\eta }*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n_{\\theta }+-1/2*g^{\\alpha \\beta }*d^{\\zeta }_{\\theta }*d^{\\epsilon }_{\\eta }+-1/2*g^{\\alpha \\beta }*d^{\\epsilon }_{\\theta }*d^{\\zeta }_{\\eta }+(Power[b, 2]*Power[beta, 2]+Power[b, 2]*beta+4*Power[beta, 2]*b+-1/4*c*Power[b, 2]+-1*c+-2*c*Power[beta, 2]*b+-2*c*beta+-3/4*c*b+1+9/2*beta*b+1/4*Power[b, 2]+-1*c*Power[b, 2]*Power[beta, 2]+-1*c*Power[b, 2]*beta+4*Power[beta, 2]+4*beta+-5/2*c*beta*b+5/4*b+4*(1/2*c*beta+1/4*c))*d^{\\alpha }_{\\eta }*d^{\\beta }_{\\theta }*n^{\\epsilon }*n^{\\zeta }+(Power[b, 2]*Power[beta, 2]+Power[b, 2]*beta+4*Power[beta, 2]*b+-1/4*c*Power[b, 2]+-1*c+-2*c*Power[beta, 2]*b+-2*c*beta+-3/4*c*b+1+9/2*beta*b+1/4*Power[b, 2]+-1*c*Power[b, 2]*Power[beta, 2]+-1*c*Power[b, 2]*beta+4*Power[beta, 2]+4*beta+-5/2*c*beta*b+5/4*b+4*(1/2*c*beta+1/4*c))*d^{\\alpha }_{\\theta }*d^{\\beta }_{\\eta }*n^{\\epsilon }*n^{\\zeta }+(-54*c*Power[b, 2]*beta+-26*c*Power[b, 2]+33*Power[b, 2]*beta*Power[c, 2]+-64*c*beta*b+2*Power[b, 2]*Power[beta, 2]+5*Power[b, 2]*beta+-32*c*b+3*Power[b, 2]+-32*c*Power[beta, 2]*b+15*Power[b, 2]*Power[c, 2]+-28*c*Power[b, 2]*Power[beta, 2]+18*Power[b, 2]*Power[beta, 2]*Power[c, 2])*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }*n_{\\theta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*g^{\\epsilon \\zeta }*d^{\\beta }_{\\eta }*n^{\\alpha }*n_{\\theta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*g^{\\epsilon \\zeta }*d^{\\beta }_{\\theta }*n^{\\alpha }*n_{\\eta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*d^{\\alpha }_{\\theta }*g^{\\epsilon \\zeta }*n^{\\beta }*n_{\\eta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*d^{\\alpha }_{\\eta }*g^{\\epsilon \\zeta }*n^{\\beta }*n_{\\theta }+(75/16*b*Power[c, 2]+45/4*beta*b*Power[c, 2]+5/4*Power[beta, 2]*b+-8*c*Power[beta, 2]*b+-7*c*Power[beta, 2]+Power[beta, 2]+-12*c*beta+-6*c*b+1/4+11/4*beta*b+-21/4*c+3*beta+21/16*b+27/4*Power[beta, 2]*b*Power[c, 2]+-14*c*beta*b)*g^{\\epsilon \\zeta }*g_{\\eta \\theta }*n^{\\alpha }*n^{\\beta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\epsilon }_{\\theta }*n^{\\zeta }*n_{\\eta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\epsilon }_{\\eta }*n^{\\zeta }*n_{\\theta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\zeta }_{\\eta }*n^{\\epsilon }*n_{\\theta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\zeta }_{\\theta }*n^{\\epsilon }*n_{\\eta }+2*d^{\\zeta }_{\\theta }*d^{\\epsilon }_{\\eta }*n^{\\alpha }*n^{\\beta }+2*d^{\\epsilon }_{\\theta }*d^{\\zeta }_{\\eta }*n^{\\alpha }*n^{\\beta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\alpha }_{\\eta }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\theta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\beta }_{\\eta }*n^{\\alpha }*n^{\\epsilon }*n^{\\zeta }*n_{\\theta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\beta }_{\\theta }*n^{\\alpha }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\alpha }_{\\theta }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }+(-1*beta*b+-1/4+-2*Power[beta, 2]+-3/2*beta+-1/4*b+-1*Power[beta, 2]*b)*g^{\\alpha \\zeta }*g_{\\eta \\theta }*g^{\\beta \\epsilon }+(-1*beta*b+-1/4+-2*Power[beta, 2]+-3/2*beta+-1/4*b+-1*Power[beta, 2]*b)*g^{\\alpha \\epsilon }*g^{\\beta \\zeta }*g_{\\eta \\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\epsilon }*d^{\\beta }_{\\eta }*d^{\\zeta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\eta }*d^{\\beta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\epsilon }*d^{\\zeta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\zeta }*d^{\\epsilon }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\zeta }*d^{\\epsilon }_{\\eta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\epsilon }*d^{\\zeta }_{\\eta }*d^{\\beta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\epsilon }*d^{\\zeta }_{\\eta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\theta }*d^{\\beta }_{\\eta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\theta }*d^{\\zeta }_{\\eta }*n^{\\beta }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\epsilon }_{\\theta }*d^{\\beta }_{\\eta }*n^{\\alpha }*n^{\\zeta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\eta }*d^{\\zeta }_{\\theta }*n^{\\beta }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\epsilon }_{\\eta }*d^{\\beta }_{\\theta }*n^{\\alpha }*n^{\\zeta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\zeta }_{\\eta }*d^{\\beta }_{\\theta }*n^{\\alpha }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\eta }*d^{\\epsilon }_{\\theta }*n^{\\beta }*n^{\\zeta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\beta }_{\\eta }*d^{\\zeta }_{\\theta }*n^{\\alpha }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\theta }*d^{\\epsilon }_{\\eta }*n^{\\beta }*n^{\\zeta }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g^{\\beta \\zeta }*g_{\\eta \\theta }*n^{\\alpha }*n^{\\epsilon }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g^{\\alpha \\zeta }*g_{\\eta \\theta }*n^{\\beta }*n^{\\epsilon }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g_{\\eta \\theta }*g^{\\beta \\epsilon }*n^{\\alpha }*n^{\\zeta }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g^{\\alpha \\epsilon }*g_{\\eta \\theta }*n^{\\beta }*n^{\\zeta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\epsilon }*d^{\\beta }_{\\theta }*n^{\\zeta }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\zeta }*n^{\\epsilon }*n_{\\theta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\epsilon }*n^{\\zeta }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\epsilon }*n^{\\zeta }*n_{\\theta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\zeta }*d^{\\beta }_{\\eta }*n^{\\epsilon }*n_{\\theta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\zeta }*d^{\\beta }_{\\theta }*n^{\\epsilon }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\zeta }*n^{\\epsilon }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\epsilon }*d^{\\beta }_{\\eta }*n^{\\zeta }*n_{\\theta }+(38*c*beta*b+-15*b*Power[c, 2]+-2*Power[beta, 2]*b+16*c*Power[beta, 2]+32*c*beta+18*c*b+20*c*Power[beta, 2]*b+-5*beta*b+16*c+-18*Power[beta, 2]*b*Power[c, 2]+-3*b+-33*beta*b*Power[c, 2])*g^{\\epsilon \\zeta }*n^{\\alpha }*n^{\\beta }*n_{\\eta }*n_{\\theta }+(2*Power[beta, 2]*b+1+2*beta*b+4*Power[beta, 2]+4*beta+1/2*b)*g^{\\alpha \\zeta }*g^{\\beta \\epsilon }*n_{\\eta }*n_{\\theta }+(2*Power[beta, 2]*b+1+2*beta*b+4*Power[beta, 2]+4*beta+1/2*b)*g^{\\alpha \\epsilon }*g^{\\beta \\zeta }*n_{\\eta }*n_{\\theta }+(-1*beta*b+c+2*c*Power[beta, 2]+3*c*beta+-1*beta+c*Power[beta, 2]*b+1/4*c*b+-2*Power[beta, 2]+-1/4*b+c*beta*b+4*(-1/8*c*beta+-1/16+-1/16*c+-1/8*beta)+-1*Power[beta, 2]*b)*d^{\\alpha }_{\\theta }*g^{\\epsilon \\zeta }*d^{\\beta }_{\\eta }+(-1*beta*b+c+2*c*Power[beta, 2]+3*c*beta+-1*beta+c*Power[beta, 2]*b+1/4*c*b+-2*Power[beta, 2]+-1/4*b+c*beta*b+4*(-1/8*c*beta+-1/16+-1/16*c+-1/8*beta)+-1*Power[beta, 2]*b)*d^{\\alpha }_{\\eta }*g^{\\epsilon \\zeta }*d^{\\beta }_{\\theta }+(-1*Power[b, 2]*Power[beta, 2]+c*beta+-1*Power[b, 2]*beta+-4*Power[beta, 2]*b+1/4*c*Power[b, 2]+2*c*Power[beta, 2]*b+3/8*c*b+-1+-1/2*c+-1/4*Power[b, 2]+-17/4*beta*b+c*Power[b, 2]*Power[beta, 2]+c*Power[b, 2]*beta+-4*Power[beta, 2]+-4*beta+9/4*c*beta*b+-7/8*b+4*(-1/4*c*beta+1/8*c))*g^{\\alpha \\beta }*g_{\\eta \\theta }*n^{\\epsilon }*n^{\\zeta }+(4*Power[beta, 2]*b+-1/2*c*Power[b, 2]+-4*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+2*Power[b, 2]*beta+-2*c*Power[b, 2]*Power[beta, 2]+-2*c*Power[b, 2]*beta+b+4*beta*b+1/2*Power[b, 2]+-4*c*beta*b)*g^{\\alpha \\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }*n_{\\theta }+(beta*b+4*(1/16*c*beta+-1/32+-1/32*c+1/16*beta)+-2*c*Power[beta, 2]+-5/2*c*beta+-1*c*Power[beta, 2]*b+-1/4*c*b+3/4+-1/4*c+2*Power[beta, 2]+3/2*beta+1/4*b+-1*c*beta*b+Power[beta, 2]*b)*g^{\\alpha \\beta }*g^{\\epsilon \\zeta }*g_{\\eta \\theta }+(-3*Power[beta, 2]*b+17/2*c*Power[b, 2]+-9/4*Power[b, 2]*Power[beta, 2]+-21/4*Power[b, 2]*beta+13*c*Power[beta, 2]*b+41/4*c*b+-45/4*Power[b, 2]*beta*Power[c, 2]+-8*beta*b+-45/16*Power[b, 2]+11*c*Power[b, 2]*Power[beta, 2]+-21/4*b+-75/16*Power[b, 2]*Power[c, 2]+23*c*beta*b+39/2*c*Power[b, 2]*beta+-27/4*Power[b, 2]*Power[beta, 2]*Power[c, 2])*g_{\\eta \\theta }*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\epsilon }*d^{\\zeta }_{\\theta }*n^{\\alpha }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\zeta }*d^{\\epsilon }_{\\theta }*n^{\\alpha }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\theta }*n^{\\beta }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\epsilon }*d^{\\zeta }_{\\theta }*n^{\\beta }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\zeta }*d^{\\epsilon }_{\\eta }*n^{\\alpha }*n_{\\theta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\eta }*n^{\\beta }*n_{\\theta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\epsilon }*d^{\\zeta }_{\\eta }*n^{\\alpha }*n_{\\theta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\epsilon }*d^{\\zeta }_{\\eta }*n^{\\beta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\beta \\zeta }*n^{\\alpha }*n^{\\epsilon }*n_{\\eta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\alpha \\zeta }*n^{\\beta }*n^{\\epsilon }*n_{\\eta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\beta \\epsilon }*n^{\\alpha }*n^{\\zeta }*n_{\\eta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\alpha \\epsilon }*n^{\\beta }*n^{\\zeta }*n_{\\eta }*n_{\\theta })*(-1*c*b*d_{\\nu }^{\\nu }*n^{\\eta }*n^{\\theta }*n_{\\theta_1 }*n_{\\iota_1 }+(1/4*c*beta+-1/4+-1/8*c+1/2*beta)*d_{\\nu }^{\\nu }*g^{\\eta \\theta }*g_{\\iota_1 \\theta_1 }+(1/2*beta*b+1/4*b)*d_{\\nu }^{\\theta }*d^{\\nu }_{\\iota_1 }*n^{\\eta }*n_{\\theta_1 }+(1/2*beta*b+1/4*b)*d_{\\nu }^{\\eta }*d^{\\nu }_{\\iota_1 }*n^{\\theta }*n_{\\theta_1 }+(1/2*beta*b+1/4*b)*d_{\\nu }^{\\theta }*d^{\\nu }_{\\theta_1 }*n^{\\eta }*n_{\\iota_1 }+(1/2*beta*b+1/4*b)*g^{\\nu \\eta }*g_{\\nu \\theta_1 }*n^{\\theta }*n_{\\iota_1 }+1/2*d_{\\nu }^{\\nu }*d^{\\eta }_{\\iota_1 }*d^{\\theta }_{\\theta_1 }+1/2*d_{\\nu }^{\\nu }*d^{\\eta }_{\\theta_1 }*d^{\\theta }_{\\iota_1 }+(-1*beta+-1/2)*g^{\\nu \\eta }*d_{\\nu }^{\\theta }*g_{\\theta_1 \\iota_1 }+(beta+1/2)*d_{\\nu }^{\\eta }*d^{\\nu }_{\\theta_1 }*d^{\\theta }_{\\iota_1 }+(beta+1/2)*d_{\\nu }^{\\theta }*d^{\\nu }_{\\theta_1 }*d^{\\eta }_{\\iota_1 }+(beta+1/2)*d_{\\nu }^{\\eta }*d^{\\nu }_{\\iota_1 }*d^{\\theta }_{\\theta_1 }+(beta+1/2)*d_{\\nu }^{\\theta }*d^{\\nu }_{\\iota_1 }*d^{\\eta }_{\\theta_1 }+(1/2*c*b+c*beta*b)*g_{\\theta_1 \\iota_1 }*n_{\\nu }*n^{\\nu }*n^{\\eta }*n^{\\theta }+(1/8*c*b+-1/4*c*beta*b)*d_{\\nu }^{\\nu }*g_{\\theta_1 \\iota_1 }*n_{\\gamma }*n^{\\gamma }*n^{\\eta }*n^{\\theta }+(1/4*c*beta+1/8+1/8*c+1/4*beta)*d_{\\gamma }^{\\gamma }*g_{\\nu \\theta_1 }*d^{\\nu }_{\\iota_1 }*g^{\\eta \\theta }+(-1*c*beta+-1/2*c)*g^{\\eta \\theta }*g_{\\theta_1 \\iota_1 }*n_{\\nu }*n^{\\nu }+(1/4*c*beta+-1/8*c)*d_{\\nu }^{\\nu }*g^{\\eta \\theta }*g_{\\theta_1 \\iota_1 }*n_{\\gamma }*n^{\\gamma }+(1/2*c*b+c*beta*b)*d^{\\nu }_{\\theta_1 }*g_{\\nu \\iota_1 }*n_{\\gamma }*n^{\\gamma }*n^{\\eta }*n^{\\theta }+(-1/2*beta*b+-1/4*b)*g^{\\nu \\theta }*g_{\\theta_1 \\iota_1 }*n_{\\nu }*n^{\\eta }+(1/2*beta*b+1/4*b)*g_{\\nu \\theta_1 }*d^{\\theta }_{\\iota_1 }*n^{\\nu }*n^{\\eta }+(-1/2*beta*b+-1/4*b)*g^{\\nu \\eta }*g_{\\theta_1 \\iota_1 }*n_{\\nu }*n^{\\theta }+(1/2*beta*b+1/4*b)*d^{\\nu }_{\\iota_1 }*d^{\\theta }_{\\theta_1 }*n_{\\nu }*n^{\\eta }+(1/2*beta*b+1/4*b)*d^{\\nu }_{\\theta_1 }*d^{\\eta }_{\\iota_1 }*n_{\\nu }*n^{\\theta }+(1/2*beta*b+1/4*b)*d^{\\nu }_{\\iota_1 }*d^{\\eta }_{\\theta_1 }*n_{\\nu }*n^{\\theta }+1/4*b*d_{\\nu }^{\\nu }*d^{\\theta }_{\\theta_1 }*n^{\\eta }*n_{\\iota_1 }+1/4*b*d_{\\nu }^{\\nu }*d^{\\eta }_{\\theta_1 }*n^{\\theta }*n_{\\iota_1 }+1/4*b*d_{\\nu }^{\\nu }*d^{\\theta }_{\\iota_1 }*n^{\\eta }*n_{\\theta_1 }+1/4*b*d_{\\nu }^{\\nu }*d^{\\eta }_{\\iota_1 }*n^{\\theta }*n_{\\theta_1 }+(-1*c*b+-2*c*beta*b)*d^{\\nu }_{\\iota_1 }*n_{\\nu }*n^{\\eta }*n^{\\theta }*n_{\\theta_1 }+(-1*c*b+-2*c*beta*b)*g_{\\nu \\theta_1 }*n^{\\nu }*n^{\\eta }*n^{\\theta }*n_{\\iota_1 }+(-1*c*beta+-1/2*c)*d^{\\nu }_{\\theta_1 }*g_{\\nu \\iota_1 }*g^{\\eta \\theta }*n_{\\gamma }*n^{\\gamma }+(1/4*c*beta+-1/8*c)*d_{\\gamma }^{\\gamma }*d_{\\nu }^{\\nu }*g_{\\theta_1 \\iota_1 }*n^{\\eta }*n^{\\theta }+(-1*c*beta+1/4*beta*b+1/2*c+-1/8*b)*d_{\\nu }^{\\nu }*g_{\\iota_1 \\theta_1 }*n^{\\eta }*n^{\\theta }+(-1*beta*b+4*c*beta+2*c+-1/2*b)*d_{\\theta_1 }^{\\nu }*g_{\\nu \\iota_1 }*n^{\\eta }*n^{\\theta }+(-1/16*c*beta+1/32+1/32*c+-1/16*beta)*d_{\\gamma }^{\\gamma }*d_{\\nu }^{\\nu }*g^{\\eta \\theta }*g_{\\theta_1 \\iota_1 }+c*d_{\\nu }^{\\nu }*g^{\\eta \\theta }*n_{\\theta_1 }*n_{\\iota_1 }+(-1*c*beta+-1/2*c)*d_{\\gamma }^{\\gamma }*g_{\\nu \\theta_1 }*d^{\\nu }_{\\iota_1 }*n^{\\eta }*n^{\\theta }+(c+2*c*beta)*d^{\\nu }_{\\iota_1 }*g^{\\eta \\theta }*n_{\\nu }*n_{\\theta_1 }+(c+2*c*beta)*g_{\\nu \\theta_1 }*g^{\\eta \\theta }*n^{\\nu }*n_{\\iota_1 }+(-1*c*beta+-1+-1/2*c+-2*beta)*d_{\\theta_1 }^{\\nu }*g_{\\nu \\iota_1 }*g^{\\eta \\theta })*(-1*c*b*d^{\\sigma }_{\\alpha }*n_{\\epsilon }*n_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/4*c*beta+-1/4+-1/8*c+1/2*beta)*d^{\\sigma }_{\\alpha }*g^{\\theta_1 \\iota_1 }*g_{\\zeta \\epsilon }+1/2*d^{\\sigma }_{\\alpha }*d^{\\theta_1 }_{\\zeta }*d^{\\iota_1 }_{\\epsilon }+1/2*d^{\\sigma }_{\\alpha }*d^{\\iota_1 }_{\\zeta }*d^{\\theta_1 }_{\\epsilon }+(1/8*c*beta+1/16+1/16*c+1/8*beta)*d_{\\delta }^{\\delta }*g_{\\alpha \\zeta }*d^{\\sigma }_{\\epsilon }*g^{\\theta_1 \\iota_1 }+(1/8*c*beta+1/16+1/16*c+1/8*beta)*d_{\\delta }^{\\delta }*g_{\\alpha \\epsilon }*d^{\\sigma }_{\\zeta }*g^{\\theta_1 \\iota_1 }+(c+2*c*beta+-1/2*beta*b+-1/4*b)*g_{\\alpha \\zeta }*d_{\\epsilon }^{\\sigma }*n^{\\theta_1 }*n^{\\iota_1 }+(c+2*c*beta+-1/2*beta*b+-1/4*b)*g_{\\epsilon \\alpha }*d^{\\sigma }_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/8*c*b+-1/4*c*beta*b)*d^{\\sigma }_{\\alpha }*g_{\\epsilon \\zeta }*n_{\\delta }*n^{\\delta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*d^{\\sigma }_{\\zeta }*d^{\\theta_1 }_{\\epsilon }*n_{\\alpha }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\epsilon }*g^{\\sigma \\iota_1 }*n_{\\zeta }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\zeta }*d^{\\iota_1 }_{\\epsilon }*n^{\\sigma }*n^{\\theta_1 }+(-1/4*beta*b+-1/8*b)*g_{\\epsilon \\zeta }*g^{\\sigma \\iota_1 }*n_{\\alpha }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\epsilon }*g^{\\sigma \\theta_1 }*n_{\\zeta }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*d_{\\alpha }^{\\theta_1 }*d^{\\sigma }_{\\zeta }*n_{\\epsilon }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\epsilon }*d^{\\theta_1 }_{\\zeta }*n^{\\sigma }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*d_{\\alpha }^{\\iota_1 }*d^{\\sigma }_{\\zeta }*n_{\\epsilon }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\zeta }*g^{\\sigma \\iota_1 }*n_{\\epsilon }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*d^{\\sigma }_{\\epsilon }*d^{\\theta_1 }_{\\zeta }*n_{\\alpha }*n^{\\iota_1 }+(-1/4*beta*b+-1/8*b)*g_{\\epsilon \\zeta }*g^{\\sigma \\theta_1 }*n_{\\alpha }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\epsilon }*d^{\\iota_1 }_{\\zeta }*n^{\\sigma }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*d^{\\sigma }_{\\epsilon }*d^{\\iota_1 }_{\\zeta }*n_{\\alpha }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\zeta }*d^{\\theta_1 }_{\\epsilon }*n^{\\sigma }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*d^{\\sigma }_{\\zeta }*d^{\\iota_1 }_{\\epsilon }*n_{\\alpha }*n^{\\theta_1 }+(1/4*beta*b+1/8*b)*g_{\\alpha \\zeta }*g^{\\sigma \\theta_1 }*n_{\\epsilon }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*d_{\\alpha }^{\\theta_1 }*d^{\\sigma }_{\\epsilon }*n_{\\zeta }*n^{\\iota_1 }+(1/4*beta*b+1/8*b)*d_{\\alpha }^{\\iota_1 }*d^{\\sigma }_{\\epsilon }*n_{\\zeta }*n^{\\theta_1 }+(-1/4*beta*b+-1/8*b)*d_{\\alpha }^{\\theta_1 }*g_{\\epsilon \\zeta }*n^{\\sigma }*n^{\\iota_1 }+(-1/4*beta*b+-1/8*b)*d_{\\alpha }^{\\iota_1 }*g_{\\epsilon \\zeta }*n^{\\sigma }*n^{\\theta_1 }+(1/4*c*beta+-1/8*c)*d^{\\sigma }_{\\alpha }*g^{\\theta_1 \\iota_1 }*g_{\\epsilon \\zeta }*n_{\\delta }*n^{\\delta }+(-1/2*c*b+-1*c*beta*b)*g_{\\alpha \\epsilon }*n_{\\zeta }*n^{\\sigma }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*c*b+-1*c*beta*b)*d^{\\sigma }_{\\epsilon }*n_{\\alpha }*n_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/2*c*b+c*beta*b)*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\sigma }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*c*b+-1*c*beta*b)*g_{\\alpha \\zeta }*n_{\\epsilon }*n^{\\sigma }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*c*b+-1*c*beta*b)*d^{\\sigma }_{\\zeta }*n_{\\alpha }*n_{\\epsilon }*n^{\\theta_1 }*n^{\\iota_1 }+1/4*b*d^{\\sigma }_{\\alpha }*d^{\\theta_1 }_{\\epsilon }*n_{\\zeta }*n^{\\iota_1 }+1/4*b*d^{\\sigma }_{\\alpha }*d^{\\theta_1 }_{\\zeta }*n_{\\epsilon }*n^{\\iota_1 }+1/4*b*d^{\\sigma }_{\\alpha }*d^{\\iota_1 }_{\\zeta }*n_{\\epsilon }*n^{\\theta_1 }+1/4*b*d^{\\sigma }_{\\alpha }*d^{\\iota_1 }_{\\epsilon }*n_{\\zeta }*n^{\\theta_1 }+(1/4+1/2*beta)*g_{\\alpha \\epsilon }*g^{\\sigma \\iota_1 }*d^{\\theta_1 }_{\\zeta }+(1/4+1/2*beta)*d_{\\alpha }^{\\iota_1 }*d^{\\sigma }_{\\zeta }*d^{\\theta_1 }_{\\epsilon }+(1/4+1/2*beta)*g_{\\alpha \\zeta }*d^{\\theta_1 }_{\\epsilon }*g^{\\sigma \\iota_1 }+(1/4+1/2*beta)*g_{\\alpha \\epsilon }*d^{\\iota_1 }_{\\zeta }*g^{\\sigma \\theta_1 }+(-1/4+-1/2*beta)*d_{\\alpha }^{\\theta_1 }*g_{\\epsilon \\zeta }*g^{\\sigma \\iota_1 }+(1/4+1/2*beta)*d_{\\alpha }^{\\iota_1 }*d^{\\sigma }_{\\epsilon }*d^{\\theta_1 }_{\\zeta }+(-1/4+-1/2*beta)*d_{\\alpha }^{\\iota_1 }*g_{\\epsilon \\zeta }*g^{\\sigma \\theta_1 }+(1/4+1/2*beta)*d_{\\alpha }^{\\theta_1 }*d^{\\sigma }_{\\epsilon }*d^{\\iota_1 }_{\\zeta }+(1/4+1/2*beta)*d_{\\alpha }^{\\theta_1 }*d^{\\sigma }_{\\zeta }*d^{\\iota_1 }_{\\epsilon }+(1/4+1/2*beta)*g_{\\alpha \\zeta }*g^{\\sigma \\theta_1 }*d^{\\iota_1 }_{\\epsilon }+(1/4*c*b+1/2*c*beta*b)*g_{\\alpha \\epsilon }*d^{\\sigma }_{\\zeta }*n_{\\delta }*n^{\\delta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/4*c*b+1/2*c*beta*b)*g_{\\alpha \\zeta }*d^{\\sigma }_{\\epsilon }*n_{\\delta }*n^{\\delta }*n^{\\theta_1 }*n^{\\iota_1 }+(c*beta+1/2*c)*d^{\\sigma }_{\\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\alpha }*n_{\\epsilon }+(c*beta+1/2*c)*g_{\\alpha \\epsilon }*g^{\\theta_1 \\iota_1 }*n_{\\zeta }*n^{\\sigma }+(c*beta+1/2*c)*d^{\\sigma }_{\\epsilon }*g^{\\theta_1 \\iota_1 }*n_{\\alpha }*n_{\\zeta }+(-1*c*beta+-1/2*c)*g^{\\theta_1 \\iota_1 }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\sigma }+(c*beta+1/2*c)*g_{\\alpha \\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\epsilon }*n^{\\sigma }+(1/4*c*beta+-1/8*c)*d_{\\delta }^{\\delta }*d^{\\sigma }_{\\alpha }*g_{\\epsilon \\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*c*beta+-1/4*c)*g_{\\alpha \\epsilon }*d^{\\sigma }_{\\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\delta }*n^{\\delta }+(-1/2*c*beta+-1/4*c)*g_{\\alpha \\zeta }*d^{\\sigma }_{\\epsilon }*g^{\\theta_1 \\iota_1 }*n_{\\delta }*n^{\\delta }+(-1*c*beta+1/4*beta*b+1/2*c+-1/8*b)*d^{\\sigma }_{\\alpha }*g_{\\zeta \\epsilon }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*c*beta+-1*beta+-1/2+-1/4*c)*g_{\\epsilon \\alpha }*d^{\\sigma }_{\\zeta }*g^{\\theta_1 \\iota_1 }+(-1/2*c*beta+-1*beta+-1/2+-1/4*c)*g_{\\alpha \\zeta }*d_{\\epsilon }^{\\sigma }*g^{\\theta_1 \\iota_1 }+(-1/16*c*beta+1/32+1/32*c+-1/16*beta)*d_{\\delta }^{\\delta }*d^{\\sigma }_{\\alpha }*g^{\\theta_1 \\iota_1 }*g_{\\epsilon \\zeta }+c*d^{\\sigma }_{\\alpha }*g^{\\theta_1 \\iota_1 }*n_{\\epsilon }*n_{\\zeta }+(-1/2*c*beta+-1/4*c)*d_{\\delta }^{\\delta }*g_{\\alpha \\zeta }*d^{\\sigma }_{\\epsilon }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*c*beta+-1/4*c)*d_{\\delta }^{\\delta }*g_{\\alpha \\epsilon }*d^{\\sigma }_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 })");
        Tensor to = Tensors.parse("n_{\\alpha }*n_{\\rho }*((-2*Power[beta, 2]*b+2*c*Power[beta, 2]*b+4*c*Power[beta, 2]+4*c*beta+1/2*c*b+-1+-2*beta*b+-4*Power[beta, 2]+2*c*beta*b+-4*beta+-1/2*b)*g^{\\alpha \\beta }*g^{\\epsilon \\zeta }*n_{\\eta }*n_{\\theta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\zeta }_{\\theta }*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n_{\\eta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\epsilon }_{\\theta }*n^{\\alpha }*n^{\\beta }*n^{\\zeta }*n_{\\eta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\epsilon }_{\\eta }*n^{\\alpha }*n^{\\beta }*n^{\\zeta }*n_{\\theta }+(Power[b, 2]*Power[beta, 2]+2*Power[beta, 2]*b+5/2*Power[b, 2]*beta+6*beta*b+3/2*Power[b, 2]+9/2*b)*d^{\\zeta }_{\\eta }*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n_{\\theta }+-1/2*g^{\\alpha \\beta }*d^{\\zeta }_{\\theta }*d^{\\epsilon }_{\\eta }+-1/2*g^{\\alpha \\beta }*d^{\\epsilon }_{\\theta }*d^{\\zeta }_{\\eta }+(Power[b, 2]*Power[beta, 2]+Power[b, 2]*beta+4*Power[beta, 2]*b+-1/4*c*Power[b, 2]+-1*c+-2*c*Power[beta, 2]*b+-2*c*beta+-3/4*c*b+1+9/2*beta*b+1/4*Power[b, 2]+-1*c*Power[b, 2]*Power[beta, 2]+-1*c*Power[b, 2]*beta+4*Power[beta, 2]+4*beta+-5/2*c*beta*b+5/4*b+4*(1/2*c*beta+1/4*c))*d^{\\alpha }_{\\eta }*d^{\\beta }_{\\theta }*n^{\\epsilon }*n^{\\zeta }+(Power[b, 2]*Power[beta, 2]+Power[b, 2]*beta+4*Power[beta, 2]*b+-1/4*c*Power[b, 2]+-1*c+-2*c*Power[beta, 2]*b+-2*c*beta+-3/4*c*b+1+9/2*beta*b+1/4*Power[b, 2]+-1*c*Power[b, 2]*Power[beta, 2]+-1*c*Power[b, 2]*beta+4*Power[beta, 2]+4*beta+-5/2*c*beta*b+5/4*b+4*(1/2*c*beta+1/4*c))*d^{\\alpha }_{\\theta }*d^{\\beta }_{\\eta }*n^{\\epsilon }*n^{\\zeta }+(-54*c*Power[b, 2]*beta+-26*c*Power[b, 2]+33*Power[b, 2]*beta*Power[c, 2]+-64*c*beta*b+2*Power[b, 2]*Power[beta, 2]+5*Power[b, 2]*beta+-32*c*b+3*Power[b, 2]+-32*c*Power[beta, 2]*b+15*Power[b, 2]*Power[c, 2]+-28*c*Power[b, 2]*Power[beta, 2]+18*Power[b, 2]*Power[beta, 2]*Power[c, 2])*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }*n_{\\theta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*g^{\\epsilon \\zeta }*d^{\\beta }_{\\eta }*n^{\\alpha }*n_{\\theta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*g^{\\epsilon \\zeta }*d^{\\beta }_{\\theta }*n^{\\alpha }*n_{\\eta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*d^{\\alpha }_{\\theta }*g^{\\epsilon \\zeta }*n^{\\beta }*n_{\\eta }+(-15/16*b*Power[c, 2]+-3/4*Power[beta, 2]*b+3*c*Power[beta, 2]*b+3*c*Power[beta, 2]+-1*Power[beta, 2]+4*c*beta+3/2*c*b+-5/4+-9/4*Power[beta, 2]*b*Power[c, 2]+-3/2*beta*b+5/4*c+-3*beta*b*Power[c, 2]+-3*beta+9/2*c*beta*b+-9/16*b)*d^{\\alpha }_{\\eta }*g^{\\epsilon \\zeta }*n^{\\beta }*n_{\\theta }+(75/16*b*Power[c, 2]+45/4*beta*b*Power[c, 2]+5/4*Power[beta, 2]*b+-8*c*Power[beta, 2]*b+-7*c*Power[beta, 2]+Power[beta, 2]+-12*c*beta+-6*c*b+1/4+11/4*beta*b+-21/4*c+3*beta+21/16*b+27/4*Power[beta, 2]*b*Power[c, 2]+-14*c*beta*b)*g^{\\epsilon \\zeta }*g_{\\eta \\theta }*n^{\\alpha }*n^{\\beta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\epsilon }_{\\theta }*n^{\\zeta }*n_{\\eta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\epsilon }_{\\eta }*n^{\\zeta }*n_{\\theta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\zeta }_{\\eta }*n^{\\epsilon }*n_{\\theta }+(beta*b+1/2+2*Power[beta, 2]+2*beta+Power[beta, 2]*b)*g^{\\alpha \\beta }*d^{\\zeta }_{\\theta }*n^{\\epsilon }*n_{\\eta }+2*d^{\\zeta }_{\\theta }*d^{\\epsilon }_{\\eta }*n^{\\alpha }*n^{\\beta }+2*d^{\\epsilon }_{\\theta }*d^{\\zeta }_{\\eta }*n^{\\alpha }*n^{\\beta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\alpha }_{\\eta }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\theta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\beta }_{\\eta }*n^{\\alpha }*n^{\\epsilon }*n^{\\zeta }*n_{\\theta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\beta }_{\\theta }*n^{\\alpha }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }+(3*Power[b, 2]*beta*Power[c, 2]+-2*c*Power[b, 2]+-5*c*Power[beta, 2]*b+3/4*Power[b, 2]*Power[beta, 2]+3/2*Power[b, 2]*beta+-9/4*c*b+-4*c*Power[b, 2]*Power[beta, 2]+-6*c*Power[b, 2]*beta+2*beta*b+9/16*Power[b, 2]+-7*c*beta*b+3/4*b+15/16*Power[b, 2]*Power[c, 2]+9/4*Power[b, 2]*Power[beta, 2]*Power[c, 2]+Power[beta, 2]*b)*d^{\\alpha }_{\\theta }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }+(-1*beta*b+-1/4+-2*Power[beta, 2]+-3/2*beta+-1/4*b+-1*Power[beta, 2]*b)*g^{\\alpha \\zeta }*g_{\\eta \\theta }*g^{\\beta \\epsilon }+(-1*beta*b+-1/4+-2*Power[beta, 2]+-3/2*beta+-1/4*b+-1*Power[beta, 2]*b)*g^{\\alpha \\epsilon }*g^{\\beta \\zeta }*g_{\\eta \\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\epsilon }*d^{\\beta }_{\\eta }*d^{\\zeta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\eta }*d^{\\beta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\epsilon }*d^{\\zeta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\zeta }*d^{\\epsilon }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\zeta }*d^{\\epsilon }_{\\eta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\epsilon }*d^{\\zeta }_{\\eta }*d^{\\beta }_{\\theta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\epsilon }*d^{\\zeta }_{\\eta }+(1/2*Power[beta, 2]*b+Power[beta, 2]+1/2*beta*b+1/2*beta+1/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\theta }*d^{\\beta }_{\\eta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\theta }*d^{\\zeta }_{\\eta }*n^{\\beta }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\epsilon }_{\\theta }*d^{\\beta }_{\\eta }*n^{\\alpha }*n^{\\zeta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\eta }*d^{\\zeta }_{\\theta }*n^{\\beta }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\epsilon }_{\\eta }*d^{\\beta }_{\\theta }*n^{\\alpha }*n^{\\zeta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\zeta }_{\\eta }*d^{\\beta }_{\\theta }*n^{\\alpha }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\eta }*d^{\\epsilon }_{\\theta }*n^{\\beta }*n^{\\zeta }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\beta }_{\\eta }*d^{\\zeta }_{\\theta }*n^{\\alpha }*n^{\\epsilon }+(3/2*Power[beta, 2]*b+1/2*Power[b, 2]*Power[beta, 2]+Power[beta, 2]+3/4*Power[b, 2]*beta+b+5/4+11/4*beta*b+1/4*Power[b, 2]+3*beta)*d^{\\alpha }_{\\theta }*d^{\\epsilon }_{\\eta }*n^{\\beta }*n^{\\zeta }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g^{\\beta \\zeta }*g_{\\eta \\theta }*n^{\\alpha }*n^{\\epsilon }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g^{\\alpha \\zeta }*g_{\\eta \\theta }*n^{\\beta }*n^{\\epsilon }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g_{\\eta \\theta }*g^{\\beta \\epsilon }*n^{\\alpha }*n^{\\zeta }+(-9/4*Power[beta, 2]*b+5/32*c*Power[b, 2]+3/4*c*Power[beta, 2]*b+-7/8*Power[b, 2]*Power[beta, 2]+-1*Power[beta, 2]+-5/4*Power[b, 2]*beta+1/2*c*Power[b, 2]*beta+3/8*c*Power[b, 2]*Power[beta, 2]+5/16*c*b+-5/4+-13/32*Power[b, 2]+-15/4*beta*b+-3*beta+-21/16*b+c*beta*b)*g^{\\alpha \\epsilon }*g_{\\eta \\theta }*n^{\\beta }*n^{\\zeta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\epsilon }*d^{\\beta }_{\\theta }*n^{\\zeta }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\zeta }*n^{\\epsilon }*n_{\\theta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\epsilon }*n^{\\zeta }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\eta }*g^{\\beta \\epsilon }*n^{\\zeta }*n_{\\theta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\zeta }*d^{\\beta }_{\\eta }*n^{\\epsilon }*n_{\\theta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\zeta }*d^{\\beta }_{\\theta }*n^{\\epsilon }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*d^{\\alpha }_{\\theta }*g^{\\beta \\zeta }*n^{\\epsilon }*n_{\\eta }+(1/2*Power[beta, 2]*b+-1/16*c*Power[b, 2]+-1/2*c*Power[beta, 2]*b+1/4*Power[b, 2]*Power[beta, 2]+1/4*Power[b, 2]*beta+-1/4*c*Power[b, 2]*Power[beta, 2]+-1/4*c*Power[b, 2]*beta+-1/8*c*b+1/4*beta*b+1/16*Power[b, 2]+-1/2*c*beta*b)*g^{\\alpha \\epsilon }*d^{\\beta }_{\\eta }*n^{\\zeta }*n_{\\theta }+(38*c*beta*b+-15*b*Power[c, 2]+-2*Power[beta, 2]*b+16*c*Power[beta, 2]+32*c*beta+18*c*b+20*c*Power[beta, 2]*b+-5*beta*b+16*c+-18*Power[beta, 2]*b*Power[c, 2]+-3*b+-33*beta*b*Power[c, 2])*g^{\\epsilon \\zeta }*n^{\\alpha }*n^{\\beta }*n_{\\eta }*n_{\\theta }+(2*Power[beta, 2]*b+1+2*beta*b+4*Power[beta, 2]+4*beta+1/2*b)*g^{\\alpha \\zeta }*g^{\\beta \\epsilon }*n_{\\eta }*n_{\\theta }+(2*Power[beta, 2]*b+1+2*beta*b+4*Power[beta, 2]+4*beta+1/2*b)*g^{\\alpha \\epsilon }*g^{\\beta \\zeta }*n_{\\eta }*n_{\\theta }+(-1*beta*b+c+2*c*Power[beta, 2]+3*c*beta+-1*beta+c*Power[beta, 2]*b+1/4*c*b+-2*Power[beta, 2]+-1/4*b+c*beta*b+4*(-1/8*c*beta+-1/16+-1/16*c+-1/8*beta)+-1*Power[beta, 2]*b)*d^{\\alpha }_{\\theta }*g^{\\epsilon \\zeta }*d^{\\beta }_{\\eta }+(-1*beta*b+c+2*c*Power[beta, 2]+3*c*beta+-1*beta+c*Power[beta, 2]*b+1/4*c*b+-2*Power[beta, 2]+-1/4*b+c*beta*b+4*(-1/8*c*beta+-1/16+-1/16*c+-1/8*beta)+-1*Power[beta, 2]*b)*d^{\\alpha }_{\\eta }*g^{\\epsilon \\zeta }*d^{\\beta }_{\\theta }+(-1*Power[b, 2]*Power[beta, 2]+c*beta+-1*Power[b, 2]*beta+-4*Power[beta, 2]*b+1/4*c*Power[b, 2]+2*c*Power[beta, 2]*b+3/8*c*b+-1+-1/2*c+-1/4*Power[b, 2]+-17/4*beta*b+c*Power[b, 2]*Power[beta, 2]+c*Power[b, 2]*beta+-4*Power[beta, 2]+-4*beta+9/4*c*beta*b+-7/8*b+4*(-1/4*c*beta+1/8*c))*g^{\\alpha \\beta }*g_{\\eta \\theta }*n^{\\epsilon }*n^{\\zeta }+(4*Power[beta, 2]*b+-1/2*c*Power[b, 2]+-4*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+2*Power[b, 2]*beta+-2*c*Power[b, 2]*Power[beta, 2]+-2*c*Power[b, 2]*beta+b+4*beta*b+1/2*Power[b, 2]+-4*c*beta*b)*g^{\\alpha \\beta }*n^{\\epsilon }*n^{\\zeta }*n_{\\eta }*n_{\\theta }+(beta*b+4*(1/16*c*beta+-1/32+-1/32*c+1/16*beta)+-2*c*Power[beta, 2]+-5/2*c*beta+-1*c*Power[beta, 2]*b+-1/4*c*b+3/4+-1/4*c+2*Power[beta, 2]+3/2*beta+1/4*b+-1*c*beta*b+Power[beta, 2]*b)*g^{\\alpha \\beta }*g^{\\epsilon \\zeta }*g_{\\eta \\theta }+(-3*Power[beta, 2]*b+17/2*c*Power[b, 2]+-9/4*Power[b, 2]*Power[beta, 2]+-21/4*Power[b, 2]*beta+13*c*Power[beta, 2]*b+41/4*c*b+-45/4*Power[b, 2]*beta*Power[c, 2]+-8*beta*b+-45/16*Power[b, 2]+11*c*Power[b, 2]*Power[beta, 2]+-21/4*b+-75/16*Power[b, 2]*Power[c, 2]+23*c*beta*b+39/2*c*Power[b, 2]*beta+-27/4*Power[b, 2]*Power[beta, 2]*Power[c, 2])*g_{\\eta \\theta }*n^{\\alpha }*n^{\\beta }*n^{\\epsilon }*n^{\\zeta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\epsilon }*d^{\\zeta }_{\\theta }*n^{\\alpha }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\zeta }*d^{\\epsilon }_{\\theta }*n^{\\alpha }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\theta }*n^{\\beta }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\epsilon }*d^{\\zeta }_{\\theta }*n^{\\beta }*n_{\\eta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\zeta }*d^{\\epsilon }_{\\eta }*n^{\\alpha }*n_{\\theta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\zeta }*d^{\\epsilon }_{\\eta }*n^{\\beta }*n_{\\theta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\beta \\epsilon }*d^{\\zeta }_{\\eta }*n^{\\alpha }*n_{\\theta }+(1/2*Power[beta, 2]*b+beta*b+Power[beta, 2]+5/4+3*beta+3/8*b)*g^{\\alpha \\epsilon }*d^{\\zeta }_{\\eta }*n^{\\beta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\beta \\zeta }*n^{\\alpha }*n^{\\epsilon }*n_{\\eta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\alpha \\zeta }*n^{\\beta }*n^{\\epsilon }*n_{\\eta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\beta \\epsilon }*n^{\\alpha }*n^{\\zeta }*n_{\\eta }*n_{\\theta }+(4*Power[beta, 2]*b+-1*c*b+-1/2*c*Power[b, 2]+Power[b, 2]+-2*c*Power[beta, 2]*b+2*Power[b, 2]*Power[beta, 2]+3*Power[b, 2]*beta+-3/2*c*Power[b, 2]*beta+7*beta*b+-1*c*Power[b, 2]*Power[beta, 2]+-3*c*beta*b+5/2*b)*g^{\\alpha \\epsilon }*n^{\\beta }*n^{\\zeta }*n_{\\eta }*n_{\\theta })*(-1*c*b*d_{\\delta }^{\\delta }*n_{\\epsilon }*n_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/4*c*beta+-1/4+-1/8*c+1/2*beta)*d_{\\delta }^{\\delta }*g_{\\zeta \\epsilon }*g^{\\theta_1 \\iota_1 }+(1/2*beta*b+1/4*b)*d^{\\delta }_{\\zeta }*d_{\\delta }^{\\iota_1 }*n_{\\epsilon }*n^{\\theta_1 }+(1/2*beta*b+1/4*b)*d^{\\delta }_{\\zeta }*d_{\\delta }^{\\theta_1 }*n_{\\epsilon }*n^{\\iota_1 }+(1/2*beta*b+1/4*b)*d^{\\delta }_{\\epsilon }*d_{\\delta }^{\\iota_1 }*n_{\\zeta }*n^{\\theta_1 }+(1/2*beta*b+1/4*b)*g_{\\delta \\epsilon }*g^{\\delta \\theta_1 }*n_{\\zeta }*n^{\\iota_1 }+1/2*d_{\\delta }^{\\delta }*d^{\\iota_1 }_{\\epsilon }*d^{\\theta_1 }_{\\zeta }+1/2*d_{\\delta }^{\\delta }*d^{\\theta_1 }_{\\epsilon }*d^{\\iota_1 }_{\\zeta }+(-1*beta+-1/2)*g^{\\delta \\theta_1 }*d_{\\delta }^{\\iota_1 }*g_{\\epsilon \\zeta }+(beta+1/2)*d^{\\delta }_{\\epsilon }*d_{\\delta }^{\\theta_1 }*d^{\\iota_1 }_{\\zeta }+(beta+1/2)*d^{\\delta }_{\\epsilon }*d_{\\delta }^{\\iota_1 }*d^{\\theta_1 }_{\\zeta }+(beta+1/2)*d^{\\delta }_{\\zeta }*d_{\\delta }^{\\theta_1 }*d^{\\iota_1 }_{\\epsilon }+(beta+1/2)*d^{\\delta }_{\\zeta }*d_{\\delta }^{\\iota_1 }*d^{\\theta_1 }_{\\epsilon }+(1/2*c*b+c*beta*b)*g_{\\epsilon \\zeta }*n_{\\delta }*n^{\\delta }*n^{\\theta_1 }*n^{\\iota_1 }+(1/8*c*b+-1/4*c*beta*b)*d_{\\delta }^{\\delta }*g_{\\epsilon \\zeta }*n_{\\gamma }*n^{\\gamma }*n^{\\theta_1 }*n^{\\iota_1 }+(1/4*c*beta+1/8+1/8*c+1/4*beta)*d_{\\gamma }^{\\gamma }*g_{\\delta \\epsilon }*d^{\\delta }_{\\zeta }*g^{\\theta_1 \\iota_1 }+(-1*c*beta+-1/2*c)*g_{\\epsilon \\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\delta }*n^{\\delta }+(1/4*c*beta+-1/8*c)*d_{\\delta }^{\\delta }*g_{\\epsilon \\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\gamma }*n^{\\gamma }+(1/2*c*b+c*beta*b)*d^{\\delta }_{\\epsilon }*g_{\\delta \\zeta }*n_{\\gamma }*n^{\\gamma }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/2*beta*b+-1/4*b)*g^{\\delta \\iota_1 }*g_{\\epsilon \\zeta }*n_{\\delta }*n^{\\theta_1 }+(1/2*beta*b+1/4*b)*g_{\\delta \\epsilon }*d^{\\iota_1 }_{\\zeta }*n^{\\delta }*n^{\\theta_1 }+(-1/2*beta*b+-1/4*b)*g^{\\delta \\theta_1 }*g_{\\epsilon \\zeta }*n_{\\delta }*n^{\\iota_1 }+(1/2*beta*b+1/4*b)*d^{\\delta }_{\\zeta }*d^{\\iota_1 }_{\\epsilon }*n_{\\delta }*n^{\\theta_1 }+(1/2*beta*b+1/4*b)*d^{\\delta }_{\\epsilon }*d^{\\theta_1 }_{\\zeta }*n_{\\delta }*n^{\\iota_1 }+(1/2*beta*b+1/4*b)*d^{\\delta }_{\\zeta }*d^{\\theta_1 }_{\\epsilon }*n_{\\delta }*n^{\\iota_1 }+1/4*b*d_{\\delta }^{\\delta }*d^{\\iota_1 }_{\\epsilon }*n_{\\zeta }*n^{\\theta_1 }+1/4*b*d_{\\delta }^{\\delta }*d^{\\theta_1 }_{\\epsilon }*n_{\\zeta }*n^{\\iota_1 }+1/4*b*d_{\\delta }^{\\delta }*d^{\\iota_1 }_{\\zeta }*n_{\\epsilon }*n^{\\theta_1 }+1/4*b*d_{\\delta }^{\\delta }*d^{\\theta_1 }_{\\zeta }*n_{\\epsilon }*n^{\\iota_1 }+(-1*c*b+-2*c*beta*b)*d^{\\delta }_{\\zeta }*n_{\\delta }*n_{\\epsilon }*n^{\\theta_1 }*n^{\\iota_1 }+(-1*c*b+-2*c*beta*b)*g_{\\delta \\epsilon }*n^{\\delta }*n_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(-1*c*beta+-1/2*c)*d^{\\delta }_{\\epsilon }*g_{\\delta \\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\gamma }*n^{\\gamma }+(1/4*c*beta+-1/8*c)*d_{\\gamma }^{\\gamma }*d_{\\delta }^{\\delta }*g_{\\epsilon \\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(-1*c*beta+1/4*beta*b+1/2*c+-1/8*b)*d_{\\delta }^{\\delta }*g_{\\zeta \\epsilon }*n^{\\theta_1 }*n^{\\iota_1 }+(-1*beta*b+4*c*beta+2*c+-1/2*b)*d_{\\epsilon }^{\\delta }*g_{\\delta \\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(-1/16*c*beta+1/32+1/32*c+-1/16*beta)*d_{\\gamma }^{\\gamma }*d_{\\delta }^{\\delta }*g_{\\epsilon \\zeta }*g^{\\theta_1 \\iota_1 }+c*d_{\\delta }^{\\delta }*g^{\\theta_1 \\iota_1 }*n_{\\epsilon }*n_{\\zeta }+(-1*c*beta+-1/2*c)*d_{\\gamma }^{\\gamma }*g_{\\delta \\epsilon }*d^{\\delta }_{\\zeta }*n^{\\theta_1 }*n^{\\iota_1 }+(c+2*c*beta)*d^{\\delta }_{\\zeta }*g^{\\theta_1 \\iota_1 }*n_{\\delta }*n_{\\epsilon }+(c+2*c*beta)*g_{\\delta \\epsilon }*g^{\\theta_1 \\iota_1 }*n^{\\delta }*n_{\\zeta }+(-1*c*beta+-1+-1/2*c+-2*beta)*d_{\\epsilon }^{\\delta }*g_{\\delta \\zeta }*g^{\\theta_1 \\iota_1 })*(-1*c*b*d_{\\beta }^{\\rho }*n^{\\eta }*n^{\\theta }*n_{\\theta_1 }*n_{\\iota_1 }+(1/4*c*beta+-1/4+-1/8*c+1/2*beta)*d_{\\beta }^{\\rho }*g_{\\iota_1 \\theta_1 }*g^{\\eta \\theta }+1/2*d_{\\beta }^{\\rho }*d^{\\theta }_{\\iota_1 }*d^{\\eta }_{\\theta_1 }+1/2*d_{\\beta }^{\\rho }*d^{\\theta }_{\\theta_1 }*d^{\\eta }_{\\iota_1 }+(1/8*c*beta+1/16+1/16*c+1/8*beta)*d_{\\iota }^{\\iota }*g_{\\beta \\theta_1 }*g^{\\eta \\theta }*d^{\\rho }_{\\iota_1 }+(1/8*c*beta+1/16+1/16*c+1/8*beta)*d_{\\iota }^{\\iota }*g_{\\beta \\iota_1 }*g^{\\eta \\theta }*d^{\\rho }_{\\theta_1 }+(c+2*c*beta+-1/2*beta*b+-1/4*b)*g_{\\beta \\iota_1 }*d_{\\theta_1 }^{\\rho }*n^{\\eta }*n^{\\theta }+(c+2*c*beta+-1/2*beta*b+-1/4*b)*g_{\\theta_1 \\beta }*d^{\\rho }_{\\iota_1 }*n^{\\eta }*n^{\\theta }+(1/8*c*b+-1/4*c*beta*b)*d_{\\beta }^{\\rho }*g_{\\theta_1 \\iota_1 }*n_{\\iota }*n^{\\iota }*n^{\\eta }*n^{\\theta }+(1/4*beta*b+1/8*b)*d^{\\rho }_{\\iota_1 }*d^{\\theta }_{\\theta_1 }*n_{\\beta }*n^{\\eta }+(1/4*beta*b+1/8*b)*g_{\\beta \\theta_1 }*g^{\\rho \\theta }*n^{\\eta }*n_{\\iota_1 }+(1/4*beta*b+1/8*b)*g_{\\beta \\theta_1 }*g^{\\rho \\eta }*n^{\\theta }*n_{\\iota_1 }+(-1/4*beta*b+-1/8*b)*g^{\\rho \\theta }*g_{\\theta_1 \\iota_1 }*n_{\\beta }*n^{\\eta }+(1/4*beta*b+1/8*b)*g_{\\beta \\iota_1 }*d^{\\eta }_{\\theta_1 }*n^{\\theta }*n^{\\rho }+(1/4*beta*b+1/8*b)*g_{\\beta \\iota_1 }*g^{\\rho \\theta }*n^{\\eta }*n_{\\theta_1 }+(1/4*beta*b+1/8*b)*d_{\\beta }^{\\theta }*d^{\\rho }_{\\iota_1 }*n^{\\eta }*n_{\\theta_1 }+(-1/4*beta*b+-1/8*b)*g^{\\rho \\eta }*g_{\\theta_1 \\iota_1 }*n_{\\beta }*n^{\\theta }+(1/4*beta*b+1/8*b)*d_{\\beta }^{\\eta }*d^{\\rho }_{\\iota_1 }*n^{\\theta }*n_{\\theta_1 }+(1/4*beta*b+1/8*b)*d^{\\theta }_{\\iota_1 }*d^{\\rho }_{\\theta_1 }*n_{\\beta }*n^{\\eta }+(1/4*beta*b+1/8*b)*g_{\\beta \\theta_1 }*d^{\\theta }_{\\iota_1 }*n^{\\eta }*n^{\\rho }+(1/4*beta*b+1/8*b)*g_{\\beta \\iota_1 }*g^{\\rho \\eta }*n^{\\theta }*n_{\\theta_1 }+(1/4*beta*b+1/8*b)*d^{\\eta }_{\\theta_1 }*d^{\\rho }_{\\iota_1 }*n_{\\beta }*n^{\\theta }+(1/4*beta*b+1/8*b)*g_{\\beta \\iota_1 }*d^{\\theta }_{\\theta_1 }*n^{\\eta }*n^{\\rho }+(1/4*beta*b+1/8*b)*d_{\\beta }^{\\theta }*d^{\\rho }_{\\theta_1 }*n^{\\eta }*n_{\\iota_1 }+(1/4*beta*b+1/8*b)*d^{\\rho }_{\\theta_1 }*d^{\\eta }_{\\iota_1 }*n_{\\beta }*n^{\\theta }+(1/4*beta*b+1/8*b)*d_{\\beta }^{\\eta }*d^{\\rho }_{\\theta_1 }*n^{\\theta }*n_{\\iota_1 }+(1/4*beta*b+1/8*b)*g_{\\beta \\theta_1 }*d^{\\eta }_{\\iota_1 }*n^{\\theta }*n^{\\rho }+(-1/4*beta*b+-1/8*b)*d_{\\beta }^{\\theta }*g_{\\theta_1 \\iota_1 }*n^{\\eta }*n^{\\rho }+(-1/4*beta*b+-1/8*b)*d_{\\beta }^{\\eta }*g_{\\theta_1 \\iota_1 }*n^{\\theta }*n^{\\rho }+(1/4*c*beta+-1/8*c)*d_{\\beta }^{\\rho }*g_{\\theta_1 \\iota_1 }*g^{\\eta \\theta }*n_{\\iota }*n^{\\iota }+(-1/2*c*b+-1*c*beta*b)*d^{\\rho }_{\\theta_1 }*n_{\\beta }*n^{\\eta }*n^{\\theta }*n_{\\iota_1 }+(-1/2*c*b+-1*c*beta*b)*g_{\\beta \\theta_1 }*n^{\\eta }*n^{\\theta }*n^{\\rho }*n_{\\iota_1 }+(1/2*c*b+c*beta*b)*g_{\\theta_1 \\iota_1 }*n_{\\beta }*n^{\\eta }*n^{\\theta }*n^{\\rho }+(-1/2*c*b+-1*c*beta*b)*g_{\\beta \\iota_1 }*n^{\\eta }*n^{\\theta }*n^{\\rho }*n_{\\theta_1 }+(-1/2*c*b+-1*c*beta*b)*d^{\\rho }_{\\iota_1 }*n_{\\beta }*n^{\\eta }*n^{\\theta }*n_{\\theta_1 }+1/4*b*d_{\\beta }^{\\rho }*d^{\\eta }_{\\theta_1 }*n^{\\theta }*n_{\\iota_1 }+1/4*b*d_{\\beta }^{\\rho }*d^{\\theta }_{\\theta_1 }*n^{\\eta }*n_{\\iota_1 }+1/4*b*d_{\\beta }^{\\rho }*d^{\\theta }_{\\iota_1 }*n^{\\eta }*n_{\\theta_1 }+1/4*b*d_{\\beta }^{\\rho }*d^{\\eta }_{\\iota_1 }*n^{\\theta }*n_{\\theta_1 }+(1/4+1/2*beta)*g_{\\beta \\theta_1 }*g^{\\rho \\eta }*d^{\\theta }_{\\iota_1 }+(1/4+1/2*beta)*g_{\\beta \\theta_1 }*g^{\\rho \\theta }*d^{\\eta }_{\\iota_1 }+(1/4+1/2*beta)*d_{\\beta }^{\\eta }*d^{\\rho }_{\\iota_1 }*d^{\\theta }_{\\theta_1 }+(1/4+1/2*beta)*g_{\\beta \\iota_1 }*g^{\\rho \\eta }*d^{\\theta }_{\\theta_1 }+(-1/4+-1/2*beta)*d_{\\beta }^{\\eta }*g^{\\rho \\theta }*g_{\\theta_1 \\iota_1 }+(-1/4+-1/2*beta)*d_{\\beta }^{\\theta }*g^{\\rho \\eta }*g_{\\theta_1 \\iota_1 }+(1/4+1/2*beta)*d_{\\beta }^{\\eta }*d^{\\theta }_{\\iota_1 }*d^{\\rho }_{\\theta_1 }+(1/4+1/2*beta)*g_{\\beta \\iota_1 }*g^{\\rho \\theta }*d^{\\eta }_{\\theta_1 }+(1/4+1/2*beta)*d_{\\beta }^{\\theta }*d^{\\eta }_{\\theta_1 }*d^{\\rho }_{\\iota_1 }+(1/4+1/2*beta)*d_{\\beta }^{\\theta }*d^{\\rho }_{\\theta_1 }*d^{\\eta }_{\\iota_1 }+(1/4*c*b+1/2*c*beta*b)*g_{\\beta \\iota_1 }*d^{\\rho }_{\\theta_1 }*n_{\\iota }*n^{\\iota }*n^{\\eta }*n^{\\theta }+(1/4*c*b+1/2*c*beta*b)*g_{\\beta \\theta_1 }*d^{\\rho }_{\\iota_1 }*n_{\\iota }*n^{\\iota }*n^{\\eta }*n^{\\theta }+(c*beta+1/2*c)*g_{\\beta \\iota_1 }*g^{\\eta \\theta }*n^{\\rho }*n_{\\theta_1 }+(c*beta+1/2*c)*g^{\\eta \\theta }*d^{\\rho }_{\\iota_1 }*n_{\\beta }*n_{\\theta_1 }+(c*beta+1/2*c)*g^{\\eta \\theta }*d^{\\rho }_{\\theta_1 }*n_{\\beta }*n_{\\iota_1 }+(c*beta+1/2*c)*g_{\\beta \\theta_1 }*g^{\\eta \\theta }*n^{\\rho }*n_{\\iota_1 }+(-1*c*beta+-1/2*c)*g_{\\theta_1 \\iota_1 }*g^{\\eta \\theta }*n_{\\beta }*n^{\\rho }+(1/4*c*beta+-1/8*c)*d_{\\iota }^{\\iota }*d_{\\beta }^{\\rho }*g_{\\theta_1 \\iota_1 }*n^{\\eta }*n^{\\theta }+(-1/2*c*beta+-1/4*c)*g_{\\beta \\iota_1 }*g^{\\eta \\theta }*d^{\\rho }_{\\theta_1 }*n_{\\iota }*n^{\\iota }+(-1/2*c*beta+-1/4*c)*g_{\\beta \\theta_1 }*g^{\\eta \\theta }*d^{\\rho }_{\\iota_1 }*n_{\\iota }*n^{\\iota }+(-1*c*beta+1/4*beta*b+1/2*c+-1/8*b)*d_{\\beta }^{\\rho }*g_{\\iota_1 \\theta_1 }*n^{\\eta }*n^{\\theta }+(-1/2*c*beta+-1*beta+-1/2+-1/4*c)*g_{\\beta \\iota_1 }*g^{\\eta \\theta }*d_{\\theta_1 }^{\\rho }+(-1/2*c*beta+-1*beta+-1/2+-1/4*c)*g_{\\theta_1 \\beta }*g^{\\eta \\theta }*d^{\\rho }_{\\iota_1 }+(-1/16*c*beta+1/32+1/32*c+-1/16*beta)*d_{\\iota }^{\\iota }*d_{\\beta }^{\\rho }*g_{\\theta_1 \\iota_1 }*g^{\\eta \\theta }+c*d_{\\beta }^{\\rho }*g^{\\eta \\theta }*n_{\\theta_1 }*n_{\\iota_1 }+(-1/2*c*beta+-1/4*c)*d_{\\iota }^{\\iota }*g_{\\beta \\iota_1 }*d^{\\rho }_{\\theta_1 }*n^{\\eta }*n^{\\theta }+(-1/2*c*beta+-1/4*c)*d_{\\iota }^{\\iota }*g_{\\beta \\theta_1 }*d^{\\rho }_{\\iota_1 }*n^{\\eta }*n^{\\theta })");
        System.out.println(from.getClass());
        System.out.println(from.getIndices());
        System.out.println(to.getIndices());
        int[] fromIndices = from.getIndices().getFree().getAllIndices().copy();
        for (int i = 0; i < fromIndices.length; ++i)
            fromIndices[i] = IndicesUtils.getNameWithType(fromIndices[i]);
        long start = System.currentTimeMillis();
        IndexMappingBuffer buffer = IndexMappings.createPort(new IndexMappingBufferTester(fromIndices, false), from, to).take();
        long stop = System.currentTimeMillis();
        System.out.println(buffer);
        System.out.println("time " + (stop - start));
    }

    @Test
    public void testProduct1() {
        Tensor from = Tensors.parse("HATK^{\\alpha \\beta \\gamma }_{\\kappa_1 \\lambda_1 }*HATK^{\\mu \\nu \\theta_1 \\iota_1 }_{\\beta \\gamma }");
        Tensor to = Tensors.parse("HATK^{\\alpha \\theta_1 \\iota_1 }_{\\beta \\gamma }*HATK^{\\mu \\nu \\beta \\gamma }_{\\kappa_1 \\lambda_1 }");
        Assert.assertTrue(from.getIndices().size() - 4 == from.getIndices().getFree().size());
        Assert.assertTrue(to.getIndices().size() - 4 == to.getIndices().getFree().size());
        Assert.assertTrue(from.getIndices().getFree().size() == to.getIndices().getFree().size());
        IndexMappingBuffer buffer = IndexMappings.getFirst(from, to);
//        Assert.assertTrue(buffer != null);
    }

    @Test(timeout = 200)
    public void testPerformance3() {
        CC.resetTensorNames(-4892047359897376321L);
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});
        Tensor u = Tensors.parse("R_{\\delta }^{\\sigma }*(g^{\\gamma \\mu }*d^{\\delta }_{\\rho }*d^{\\nu }_{\\sigma }+d^{\\gamma }_{\\sigma }*g^{\\delta \\nu }*d^{\\mu }_{\\rho }+d^{\\gamma }_{\\rho }*g^{\\delta \\nu }*d^{\\mu }_{\\sigma }+g^{\\gamma \\mu }*d^{\\nu }_{\\rho }*d^{\\delta }_{\\sigma }+g^{\\gamma \\delta }*d^{\\mu }_{\\sigma }*d^{\\nu }_{\\rho }+d^{\\gamma }_{\\rho }*g^{\\delta \\mu }*d^{\\nu }_{\\sigma }+g^{\\gamma \\delta }*g^{\\mu \\nu }*g_{\\rho \\sigma }+g^{\\gamma \\nu }*d^{\\mu }_{\\rho }*d^{\\delta }_{\\sigma }+d^{\\gamma }_{\\rho }*g^{\\mu \\nu }*d^{\\delta }_{\\sigma }+g^{\\gamma \\mu }*g^{\\delta \\nu }*g_{\\rho \\sigma }+d^{\\gamma }_{\\sigma }*g^{\\delta \\mu }*d^{\\nu }_{\\rho }+g^{\\gamma \\nu }*d^{\\mu }_{\\sigma }*d^{\\delta }_{\\rho }+g^{\\gamma \\nu }*g_{\\rho \\sigma }*g^{\\delta \\mu }+d^{\\gamma }_{\\sigma }*g^{\\mu \\nu }*d^{\\delta }_{\\rho }+g^{\\gamma \\delta }*d^{\\nu }_{\\sigma }*d^{\\mu }_{\\rho })*R^{\\rho }_{\\mu \\gamma \\nu }");
        Tensor v = Tensors.parse("R_{\\mu }^{\\sigma }*(g^{\\beta \\mu }*d^{\\delta }_{\\rho }*d^{\\nu }_{\\sigma }+d^{\\beta }_{\\sigma }*g^{\\delta \\nu }*d^{\\mu }_{\\rho }+d^{\\beta }_{\\rho }*g^{\\delta \\nu }*d^{\\mu }_{\\sigma }+g^{\\beta \\mu }*d^{\\nu }_{\\rho }*d^{\\delta }_{\\sigma }+g^{\\beta \\delta }*d^{\\mu }_{\\sigma }*d^{\\nu }_{\\rho }+d^{\\beta }_{\\rho }*g^{\\delta \\mu }*d^{\\nu }_{\\sigma }+g^{\\beta \\delta }*g^{\\mu \\nu }*g_{\\rho \\sigma }+g^{\\beta \\nu }*d^{\\mu }_{\\rho }*d^{\\delta }_{\\sigma }+d^{\\beta }_{\\rho }*g^{\\mu \\nu }*d^{\\delta }_{\\sigma }+g^{\\beta \\mu }*g^{\\delta \\nu }*g_{\\rho \\sigma }+d^{\\beta }_{\\sigma }*g^{\\delta \\mu }*d^{\\nu }_{\\rho }+g^{\\beta \\nu }*d^{\\mu }_{\\sigma }*d^{\\delta }_{\\rho }+g^{\\beta \\nu }*g_{\\rho \\sigma }*g^{\\delta \\mu }+d^{\\beta }_{\\sigma }*g^{\\mu \\nu }*d^{\\delta }_{\\rho }+g^{\\beta \\delta }*d^{\\nu }_{\\sigma }*d^{\\mu }_{\\rho })*R^{\\rho }_{\\beta \\delta \\nu }");
        for (Tensor uu : u)
            System.out.println(uu);
        System.out.println("as");
        for (Tensor vv : v)
            System.out.println(vv);
        System.out.println(TensorHashCalculator.hashWithIndices(u));
        System.out.println(TensorHashCalculator.hashWithIndices(v));
        System.out.println("AS");
        System.out.println(TensorUtils.equals(u, v));

    }

    @Test
    public void testPower1() {
        Tensor from = parse("(a-b)**3"), to = parse("(b-a)**3");
        Assert.assertTrue(IndexMappings.getFirst(from, to).getSignum() == true);
    }

    @Test
    public void testPower2() {
        Tensor from = parse("(a-b)**3"), to = parse("-(b-a)**3");
        Assert.assertTrue(IndexMappings.getFirst(from, to).getSignum() == false);
    }

    @Test
    public void test12() {
        addSymmetry("R_ijk", IndexType.LatinLower, true, 0, 2, 1);
        Tensor from = parse("R_ijk*F^jk");
        Tensor to = parse("R_ijk*F^kj");
        IndexMappingBuffer mapping = IndexMappings.getFirst(from, to);
        Assert.assertTrue(mapping != null);
        Assert.assertTrue(mapping.getSignum());
    }

    @Test
    public void test13() {
        Tensor from = parse("Sin[a-b]");
        MappingsPort mapping = IndexMappings.createPort(from, from);
        IndexMappingBuffer first = mapping.take();
        Assert.assertTrue(first.isEmpty());
        Assert.assertTrue(!first.getSignum());
        Assert.assertTrue(mapping.take() == null);
    }

    @Test
    public void test14() {
        Tensor[] from = new Tensor[]{parse("f_a"), parse("f_b")};
        Tensor[] to = new Tensor[]{parse("f_a"), parse("f^a")};
        MappingsPort mapping = IndexMappings.createBijectiveProductPort(from, to);
        IndexMappingBuffer first = mapping.take();
        IndexMappingBuffer b = IndexMappingTestUtils.parse("+;_a->_a;_b->^a");
        Assert.assertEquals(first, b);
    }

    @Test
    public void test15() {
        CC.resetTensorNames(8170410325559983904L);

        Tensor from = parse("e^{d}_{f}*(4*g_{ac}*d_{d}^{f} - 4*d_{a}^{f}*g_{dc} + 4*g_{ad}*d^{f}_{c})"),
                 to = parse("e^{d}_{f}*(4*g_{ac}*d_{d}^{f} + 4*d_{a}^{f}*g_{cd} - 4*g_{ad}*d_{c}^{f})");
        Assert.assertFalse(TensorUtils.equals(from, to));
    }


    @Test
    public void test15a() {
        CC.resetTensorNames(8170410325559983904L);

        Tensor from = parse("e^{d}_{f}*(4*g_{ac}*d_{d}^{f} - 4*d_{a}^{f}*g_{dc} + 4*g_{ad}*d^{f}_{c})"),
                to = parse("e^{d}_{f}*(4*g_{ac}*d_{d}^{f} + 4*d_{a}^{f}*g_{cd} - 4*g_{ad}*d_{c}^{f})");

        System.out.println(from);
        System.out.println(to);

//        from = parse("e_{e}^{d}_{gf}*(- 4*d_{a}^{f}*g_{dc} + 4*g_{ad}*d^{f}_{c})");
//                to = parse("e_{e}^{d}_{gf}*(+ 4*d_{a}^{f}*g_{cd} - 4*g_{ad}*d_{c}^{f})");


//        Tensor t = Tensors.sum(Expand.expand(from), negate(Expand.expand(to)));
//        t = ContractIndices.contract(t);
//        System.out.println(t);


//        from = parse("4*g_{ac}*d_{d}^{f} - 4*d_{a}^{f}*g_{dc} + 4*g_{ad}*d^{f}_{c}");
//        to = parse("4*g_{ac}*d_{d}^{f} + 4*d_{a}^{f}*g_{cd} - 4*g_{ad}*d_{c}^{f}");

//        from = parse("f_mn*T^mn");
//          to = parse("f_mn*T^nm");

        IndexMappingBuffer buffer;
        MappingsPort port = IndexMappings.createPort(from, to);
        while ((buffer = port.take()) != null)
            System.out.println(buffer);


        Assert.assertFalse(TensorUtils.equals(from, to));
    }

}
