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
        Assert.assertFalse(Tensors.parse("(-2)*D").hashCode() ==
                Tensors.parse("D").hashCode());
    }

    @Test
    public void testHashCode2() {
        Assert.assertEquals(Tensors.parse("(-1)*D").hashCode(),
                Tensors.parse("D").hashCode());
    }

    @Test
    public void testHashCode5() {
        System.out.println(0x00080000 >> 19);
    }

    @Test
    public void contentTest0() {
        Product p = (Product) Tensors.parse("a*b*c*A^ij*A_i*A_j");
        System.out.println("NS:" + p.getNonScalar());
        for (Tensor t : p.getScalars())
            System.out.println("S:" + t);
    }
}