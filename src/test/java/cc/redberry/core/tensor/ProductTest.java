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

import cc.redberry.core.utils.*;
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
        Product p = (Product) Tensors.parse("a*b*c*A^ij*A_i*A_j*b_u");
        Assert.assertTrue(TensorUtils.equals(Tensors.parse("b_u"), p.getContent().getNonScalar()));
        ProductBuilder pb = new ProductBuilder();
        for (Tensor t : p.getAllScalars())
            pb.put(t);
        pb.put(Tensors.parse("b_u"));
        Assert.assertTrue(TensorUtils.equals(pb.build(), p));
    }

    @Test
    public void testRebuild() {
        Tensor t = Tensors.parse("a*b*c*A^ij*A_i*A_j*b_u");
        TensorBuilder builder = t.getBuilder();
        for (Tensor c : t)
            builder.put(c);
        Assert.assertTrue(TensorUtils.equals(t, builder.build()));
    }

    @Test
    public void testBuilder() {
        Tensor t1 = Tensors.parse("p_m*p^m");
        Tensor t2 = Tensors.parse("Power[p_m*p^m,2]");
        System.out.println(UnsafeTensors.unsafeMultiplyWithoutIndicesRenaming(t1, t2));
    }

    @Test
    public void testRenameConflicts() {
        Tensor t1 = Tensors.parse("p_a");
        Tensor t2 = Tensors.parse("(a_i+b_a^a_i)");
        System.out.println(Tensors.multiply(t1, t2));
    }

    @Test
    public void testRenameConflicts2() {
        Tensor t1 = Tensors.parse("(p_a+d_a)");
        Tensor t2 = Tensors.parse("(a_i+b_a^a_i)");
        System.out.println(Tensors.multiply(t1, t2));
    }

    @Test
    public void testRenameConflicts3() {
        Tensor t1 = Tensors.parse("A_a^a*A_b^b*A_c^c_m^n+A_d^e*A_e^d*A_f^f_m^n");
        Tensor t2 = Tensors.parse("A_a^a*A_b^b*A_c^c^m_n+A_d^e*A_e^d*A_f^f^m_n");
        System.out.println(Tensors.multiply(t1, t2));
    }

    @Test
    public void testRenameConflicts4() {
        Tensor t1 = Tensors.parse("(A_a^a+B_a^a)");
        Tensor t2 = Tensors.parse("(A_a^a+B_a^a)");
        System.out.println(Tensors.multiply(t1, t2));
    }

    @Test
    public void testRenameConflicts5() {
        Tensor t = Tensors.parse("(A_mn*B^mn_ab+C_ab)*C^dc");
        System.out.println(t);
    }
}