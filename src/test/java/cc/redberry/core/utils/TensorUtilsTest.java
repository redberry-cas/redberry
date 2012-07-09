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
package cc.redberry.core.utils;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorUtilsTest {
    @Test
    public void test1() {
        Tensor tensor = Tensors.parse("A_ij");
        Tensor expected = Tensors.parse("A_ij");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void test2() {
        Tensor tensor = Tensors.parse("A_ij*A_kl");
        Tensor expected = Tensors.parse("A_kl*A_ij");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void test3() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        Tensor expected = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void test4() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        Tensor expected = Tensors.parse("A_mn*A_kl*A_ij+A_nl*A_km*A_ij");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void test5() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*B_ij+B_ijk*C_lmn");
        Tensor expected = Tensors.parse("C_lmn*B_ijk+A_mn*A_kl*A_ij+A_nl*A_km*B_ij");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testParity1() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*B_ij+B_ijk*C_lmn");
        Tensor expected = Tensors.parse("C_tmn*B_ijk+A_mn*A_kt*A_ij+A_nt*A_km*B_ij");
        assertTrue(TensorUtils.testParity(tensor, expected));
    }

    @Test
    public void testParity2() {
        Tensor tensor =   Tensors.parse("A_ij^m*B_mlk+C_ijlkmn*T^mn");
        Tensor expected = Tensors.parse("A_ij^u*B_ulk+C_ijlknp*T^np");
        assertTrue(TensorUtils.testParity(tensor, expected));//TODO no mapping has been found, WTF???
    }
}
