/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */
package cc.redberry.core.indexmapping;

import cc.redberry.concurrent.*;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.Arrays;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.addSymmetry;
import static cc.redberry.core.tensor.Tensors.parse;

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
        Tensor t1 = parse("A_mn*B^mnpqr*A_pqr");
        Tensor t2 = parse("A_pq*B^mnpqr*A_mnr");
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(t1, t2, true);
        Assert.assertTrue(buffers.isEmpty());
    }

    @Test
    public void testScalarTensors12() {
        Tensor t1 = parse("A_i*A^i");
        Tensor t2 = parse("A_i*A^i");
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(t1, t2, true);
        Assert.assertTrue(buffers.size() >= 1);
    }

    @Test
    public void testScalarTensors2() {
        addSymmetry("B^abcde", IndexType.LatinLower, false, 2, 3, 0, 1, 4);
        Tensor t1 = parse("A_mn*B^mnpqr*A_pqr");
        Tensor t2 = parse("A_pq*B^mnpqr*A_mnr");
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(t1, t2, true);
        Assert.assertTrue(buffers.size() == 1);
        Assert.assertTrue(buffers.iterator().next().isEmpty());
    }

    @Test
    public void testScalarTensors3() {
        Tensor t1 = parse("A_m^m*A_a^b");
        Tensor t2 = parse("A_a^n*A_n^b");
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(t1, t2, true);
        Assert.assertTrue(buffers.isEmpty());
    }

    @Test
    public void testScalarTensors4() {
        Tensor t1 = parse("A_m^m");
        Tensor t2 = parse("A_a^n");
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(t1, t2, true);
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
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(riman1, riman2, false);
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
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(from, to, false);
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
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(from, to, false);
        Assert.assertTrue(buffers.size() == 2);
    }

    @Test
    public void testDiffStates() {
        Tensor from = parse("Tensor_mn");
        Tensor to = parse("Tensor^ab");
        Set<IndexMappingBuffer> buffers = IndexMappings.createAllMappings(from, to, true);
        Assert.assertTrue(buffers.size() == 1);
        IndexMappingBuffer expected = IndexMappingTestUtils.parse("+;_m->^a;_n->^b");
        Assert.assertTrue(expected.equals(buffers.iterator().next()));
    }

    @Test
    public void testField1() {
        Tensor from = parse("g_mn*f[x]");
        Tensor to = parse("g_ab*f[x]");
        Assert.assertTrue(IndexMappings.createPort(from, to, true).take() != null);
    }

    @Test
    public void testField2() {
        Tensor from = parse("g_mn*f[x]");
        Tensor to = parse("g_ab*f[y]");
        Assert.assertTrue(IndexMappings.createPort(from, to, true).take() == null);
    }

    @Test
    public void scalarSign() {
        addSymmetry("R_abcd", IndexType.LatinLower, true, new int[]{0, 1, 3, 2});
        Tensor from = parse("R_{abcd}*R^abcd");
        Tensor to = parse("R_abcd*R^abdc");
        OutputPortUnsafe<IndexMappingBuffer> opu = IndexMappings.createPort(from, to);
        IndexMappingBuffer buffer;
        while ((buffer = opu.take()) != null)
            Assert.assertTrue(buffer.getSignum());
    }
}
