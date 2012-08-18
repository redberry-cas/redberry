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

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.*;
import junit.framework.*;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
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
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test2() {
        Tensor tensor = Tensors.parse("A_ij*A_kl");
        Tensor expected = Tensors.parse("A_kl*A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test3() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        Tensor expected = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test4() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*A_ij");
        Tensor expected = Tensors.parse("A_mn*A_kl*A_ij+A_nl*A_km*A_ij");
        assertTrue(TensorUtils.equalsExactly(tensor, expected));
    }

    @Test
    public void test5() {
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*B_ij+B_ijk*C_lmn");
            Tensor expected = Tensors.parse("C_lmn*B_ijk+A_mn*A_kl*A_ij+A_nl*A_km*B_ij");
            assertTrue(TensorUtils.equalsExactly(tensor, expected));
        }
    }

    @Test
    public void testParity1() {
        Tensor tensor = Tensors.parse("A_ij*A_kl*A_mn+A_km*A_nl*B_ij+B_ijk*C_lmn");
        Tensor expected = Tensors.parse("A_mn*A_kt*A_ij+A_nt*A_km*B_ij+C_tmn*B_ijk");
        assertFalse(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testParity2() {
        Tensor tensor = Tensors.parse("A_ij^m*B_mlk+C_ijlkmn*T^mn");
        Tensor expected = Tensors.parse("A_ij^u*B_ulk+C_ijlknp*T^np");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testParity3() {
        Tensor tensor = Tensors.parse("A_ij^m*B_mlk");
        Tensor expected = Tensors.parse("A_ij^u*B_ulk");
        assertTrue(TensorUtils.equals(tensor, expected));
    }

    @Test
    public void testIsZeroDueToSymmetry1() {
        Tensors.addSymmetry("A_mn", IndexType.LatinLower, true, 1, 0);
        Tensors.addSymmetry("S_mn", IndexType.LatinLower, false, 1, 0);
        Tensor t = Tensors.parse("A_mn*S^mn");
        Assert.assertTrue(TensorUtils.isZeroDueToSymmetry(t));
    }

    @Test
    public void testIsZeroDueToSymmetry2() {
        Tensors.addSymmetry("A_mn", IndexType.LatinLower, true, 1, 0);
        Tensors.addSymmetry("S_mn", IndexType.LatinLower, false, 1, 0);
        Tensors.addSymmetry("F_mnab", IndexType.LatinLower, false, 1, 0, 2, 3);
        Tensors.addSymmetry("F_mnab", IndexType.LatinLower, true, 0, 1, 3, 2);
        Tensor t = Tensors.parse("A_mn*S^mn+F_mn^mn");
        Assert.assertTrue(TensorUtils.isZeroDueToSymmetry(t));
    }

    @Test
    public void testIsZeroDueToSymmetry3() {
        Tensors.addSymmetry("F_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, 1, 0, 2, 3);
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Tensor t = Tensors.parse("-23/60*R^{\\gamma \\mu }*F_{\\gamma \\mu }^{\\rho_5 }_{\\rho_5 }");
        Assert.assertTrue(TensorUtils.isZeroDueToSymmetry(t));
    }

    @Test
    public void testIsZeroDueToSymmetry4() {
        Tensors.addSymmetry("F_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, 1, 0, 2, 3);
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Tensor t = Tensors.parse("1/15*R_{\\delta }^{\\rho }*R_{\\rho }^{\\delta }+-23/60*R^{\\gamma \\mu }*F_{\\gamma \\mu }^{\\rho_5 }_{\\rho_5 }+1/12*F^{\\alpha }_{\\nu }^{\\rho_5 }_{\\zeta }*F_{\\alpha }^{\\nu \\zeta }_{\\rho_5 }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/30*Power[R, 2]+1/6*R*W^{\\beta }_{\\beta }");
        t = RemoveDueToSymmetry.INSANCE.transform(t);
        Tensor e = Tensors.parse("1/15*R_{\\delta }^{\\rho }*R_{\\rho }^{\\delta }+1/12*F^{\\alpha }_{\\nu }^{\\rho_5 }_{\\zeta }*F_{\\alpha }^{\\nu \\zeta }_{\\rho_5 }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/30*Power[R, 2]+1/6*R*W^{\\beta }_{\\beta }");
        Assert.assertTrue(TensorUtils.equals(t, e));
    }
}
