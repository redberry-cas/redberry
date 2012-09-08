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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.random.TRandom;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProductTest {

    @Test
    public void testHashCode() {
        Assert.assertEquals(Tensors.parse("(-1)*D*S").hashCode(),
                            Tensors.parse("D*S").hashCode());
    }

    @Test
    public void testHashCode1() {
        Assert.assertEquals(Tensors.parse("(-2)*D*S").hashCode(),
                            Tensors.parse("2*D*S").hashCode());
    }

    @Test
    public void testHashCode3() {
        Assert.assertEquals(Tensors.parse("(-2)*4*D*S").hashCode(),
                            Tensors.parse("2*2*2*D*S").hashCode());
    }

    @Test
    public void testHashCode4() {
        Assert.assertFalse(Tensors.parse("(-2)*D").hashCode()
                == Tensors.parse("D").hashCode());
    }

    @Test
    public void testHashCode5() {
        Assert.assertTrue(Tensors.parse("(-1)*a*D_mn").hashCode()
                == Tensors.parse("a*D_mn").hashCode());
    }

    @Test
    public void testHashCode2() {
        Assert.assertEquals(Tensors.parse("(-1)*D").hashCode(),
                            Tensors.parse("D").hashCode());
    }

    @Test
    public void contentTest0() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Product p = (Product) Tensors.parse("a*b*c*A^ij*A_i*A_j*b_u");
            Assert.assertTrue(TensorUtils.equalsExactly(Tensors.parse("b_u"), p.getContent().getNonScalar()));
            ProductBuilder pb = new ProductBuilder();
            for (Tensor t : p.getAllScalars())
                pb.put(t);
            pb.put(Tensors.parse("b_u"));
            Assert.assertTrue(TensorUtils.equalsExactly(pb.build(), p));
        }
    }

    @Test
    public void testRebuild() {
        Tensor t = Tensors.parse("a*b*c*A^ij*A_i*A_j*b_u");
        TensorBuilder builder = t.getBuilder();
        for (Tensor c : t)
            builder.put(c);
        Assert.assertTrue(TensorUtils.equalsExactly(t, builder.build()));
    }

    @Test
    public void testBuilder() {
        Tensor t1 = Tensors.parse("p_m*p^m");
        Tensor t2 = Tensors.parse("Power[p_m*p^m,2]");
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts() {
        Tensor t1 = Tensors.parse("p_a");
        Tensor t2 = Tensors.parse("(a_i+b_a^a_i)");
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts2() {
        Tensor t1 = Tensors.parse("(p_a+d_a)");
        Tensor t2 = Tensors.parse("(a_i+b_a^a_i)");
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts3() {
        Tensor t1 = Tensors.parse("A_a^a*A_b^b*A_c^c_m^n+A_d^e*A_e^d*A_f^f_m^n");
        System.out.println(t1);
        Tensor t2 = Tensors.parse("A_a^a*A_b^b*A_c^c^m_n+A_d^e*A_e^d*A_f^f^m_n");
        TAssert.assertIndicesConsistency(Tensors.multiplyAndRenameConflictingDummies(t1, t2));
    }

    @Test
    public void testRenameConflicts4() {
        Tensor t = Tensors.parse("(1/12*gamma+1/1440*Power[gamma, 3]*g^{\\delta \\zeta }*g_{\\delta \\zeta }+1/2880*Power[gamma, 3]*d^{\\delta }_{\\delta }*d^{\\zeta }_{\\zeta })*d^{\\beta }_{\\gamma }*d^{\\alpha }_{\\sigma }*P^{\\gamma }_{\\beta }*R_{\\alpha }^{\\sigma }");
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void testRenameConflicts5() {
        Tensor t = Tensors.parse("k_a*(f_m^m+g_m^m)*(k_b*d_m^m+k_b*h_m^m*(d_m^m+f_m^m)*(k_m^m+f_m^m))");
        TAssert.assertIndicesConsistency(t);
    }

    @Test
    public void testGetRange1() {
        Product product = (Product) Tensors.parse("A*B");
        Tensor[] tensors = product.getRange(0, 1);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange2() {
        Product product = (Product) Tensors.parse("A*B*C_i*N_j*T_r*a*b");
        Tensor[] tensors = product.getRange(4, 6);
        int i = 4;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange3() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(0, 10);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange4() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(0, 1);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange5() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(0, 3);
        int i = 0;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange6() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(4, 8);
        int i = 4;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange7() {
        Product product = (Product) Tensors.parse("2*e^i*A*B*C_i*N_j*T_r*a*b*15*R^jkl*B_kly");
        Tensor[] tensors = product.getRange(1, 3);
        int i = 1;
        for (Tensor t : tensors)
            Assert.assertTrue(TensorUtils.equalsExactly(product.get(i++), t));
    }

    @Test
    public void testGetRange8() {
        Product product = (Product) Tensors.parse("2*a*b*g_mn");
        Tensor[] expected = {product.get(2)};
        Tensor[] actual = product.getRange(2, 3);
        Assert.assertTrue(expected.length == actual.length);
        for (int i = 0; i < expected.length; ++i)
            Assert.assertTrue(TensorUtils.equalsExactly(expected[i], actual[i]));
    }

    @Test
    public void testGetRange9() {
        TRandom random;
        int minProductSize, from, to;
        for (int count = 0; count < 1000; ++count) {
            CC.resetTensorNames();
            random = new TRandom(1, 100, new int[]{0, 0, 0, 0}, new int[]{2, 0, 0, 0}, true);
            minProductSize = 2 + random.nextInt(200);
            Tensor tensor = random.nextProduct(minProductSize);
            if (!(tensor instanceof Product))
                continue;
            Product p = (Product) tensor;
            to = random.nextInt(p.size());
            from = random.nextInt(to);
            assertArraysEquals(getRange(p, from, to), p.getRange(from, to));
        }
    }

    private Tensor[] getRange(Tensor tensor, int from, int to) {
        Tensor[] r = new Tensor[to - from];
        for (int i = from; i < to; ++i)
            r[i - from] = tensor.get(i);
        return r;
    }

    private static void assertArraysEquals(Tensor[] expected, Tensor[] actual) {
        Assert.assertTrue(expected.length == actual.length);
        for (int i = 0; i < expected.length; ++i)
            Assert.assertTrue(TensorUtils.equalsExactly(expected[i], actual[i]));
    }

    @Test
    public void testBuilder1() {
        Tensor x = Tensors.parse("Power[Power[pT,2] - s, 4]*Power[s, 4]");
        ProductBuilder builder = new ProductBuilder();
        builder.put(Complex.ONE);
        builder.put(x);
        Tensor t = builder.build();
        Tensor e = Tensors.parse("Power[Power[pT,2] - s, 4]*Power[s, 4]");
        Assert.assertTrue(TensorUtils.equalsExactly(t, e));
    }

    @Test
    public void toString1() {
        Tensor t = Tensors.parse("-a*b");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString2() {
        Tensor t = Tensors.parse("-a*b*g_mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString3() {
        Tensor t = Tensors.parse("-a*b*g_mn*g^mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString4() {
        Tensor t = Tensors.parse("a*b");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString5() {
        Tensor t = Tensors.parse("a*b*g_mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString6() {
        Tensor t = Tensors.parse("a*b*g_mn*g^mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }

    @Test
    public void toString7() {
        Tensor t = Tensors.parse("(2-i)*a*b*g_mn*g^mn");
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse(t.toString())));
    }
}
