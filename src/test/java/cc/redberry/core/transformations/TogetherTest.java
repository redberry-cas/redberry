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
package cc.redberry.core.transformations;

import cc.redberry.core.*;
import cc.redberry.core.context.*;
import cc.redberry.core.tensor.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TogetherTest {

    @Test
    public void testTogetherProduct1() {
        Tensor actual = Tensors.parse("(a+b)*(a+b)");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("(a+b)**2");
        TAssert.assertEqualsExactly(actual, expected);
    }

    @Test
    public void testTogetherProduct2() {
        Tensor actual = Tensors.parse("(a_m^m+b_n^n)*(a_m^m+b_a^a)");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("(a_i^i+b_i^i)**2");
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void testTogetherProduct3() {
        Tensor actual = Tensors.parse("k_i*k^i*k_j*k^j");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("(k_i*k^i)**2");
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test1() {
        Tensor actual = Tensors.parse("1/a+1/b");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("(a+b)/(a*b)");
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test2() {
        Tensor actual = Tensors.parse("1/a**2+1/a");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("(a+1)/(a**2)");
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test3() {
        Tensor actual = Tensors.parse("1/12*P*R*gamma+19/288*Power[gamma, 3]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+49/720*Power[gamma, 9]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-1/144*P*R*Power[gamma, 3]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+47/180*Power[gamma, 3]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+2761/11520*Power[gamma, 4]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1/18*P*R*Power[gamma, 5]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+4669/5760*Power[gamma, 4]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+7/60*Power[R, 2]-37/240*Power[gamma, 8]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-19/120*gamma*Power[R, 2]*Power[(1+gamma), (-1)]-1/36*P*R*Power[gamma, 4]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-1391/1440*Power[gamma, 4]*Power[R, 2]*Power[(1+gamma), (-1)]-13/144*P*R*Power[gamma, 3]*Power[(1+gamma), (-1)]+1/48*Power[P, 2]*Power[gamma, 2]+1/12*P*R*Power[gamma, 4]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1/480*Power[gamma, 2]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+929/5760*Power[gamma, 6]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+53/720*Power[gamma, 7]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-497/1152*Power[gamma, 6]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1/6*P*R-271/480*Power[gamma, 2]*Power[R, 2]*Power[(1+gamma), (-1)]-37/384*Power[gamma, 8]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1439/5760*Power[gamma, 8]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-37/120*Power[gamma, 5]*Power[R, 2]*Power[(1+gamma), (-1)]-5/144*P*R*Power[gamma, 5]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1/20*Power[gamma, 5]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-337/5760*Power[gamma, 9]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-43/40*Power[gamma, 3]*Power[R, 2]*Power[(1+gamma), (-1)]-5/72*P*R*Power[gamma, 4]*Power[(1+gamma), (-1)]-1/24*P*R*Power[gamma, 2]*Power[(1+gamma), (-1)]-109/5760*Power[gamma, 6]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-203/3840*Power[gamma, 5]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-1409/2880*Power[gamma, 7]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+167/3840*Power[gamma, 4]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+29/1920*Power[gamma, 5]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+29/120*gamma*Power[R, 2]-403/5760*Power[gamma, 7]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+829/5760*Power[gamma, 6]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1/36*P*R*Power[gamma, 3]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1453/1920*Power[gamma, 5]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-1/72*P*R*Power[gamma, 6]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1789/5760*Power[gamma, 7]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]-19/1440*Power[gamma, 10]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+1/12*P*R*Power[gamma, 2]+1/36*P*R*Power[gamma, 3]+319/1440*Power[gamma, 6]*Power[R, 2]*Power[(1+gamma), (-1)]*Power[(1+gamma), (-1)]+9/80*Power[gamma, 4]*Power[R, 2]+17/40*Power[gamma, 2]*Power[R, 2]+83/240*Power[gamma, 3]*Power[R, 2]");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("(gamma+1)**(-6)*(4669/5760*R**2*gamma**4*(gamma+1)**4+17/40*R**2*gamma**2*(gamma+1)**6-1391/1440*R**2*gamma**4*(gamma+1)**5+29/1920*R**2*gamma**5*(gamma+1)+167/3840*R**2*gamma**4*(gamma+1)**2+29/120*gamma*R**2*(gamma+1)**6+319/1440*R**2*gamma**6*(gamma+1)**4+19/288*R**2*gamma**3*(gamma+1)**4-497/1152*R**2*gamma**6*(gamma+1)**2-1/72*gamma**6*(gamma+1)**3*P*R+1/12*gamma*(gamma+1)**6*P*R-271/480*R**2*gamma**2*(gamma+1)**5+1/48*gamma**2*P**2*(gamma+1)**6-37/120*R**2*gamma**5*(gamma+1)**5-37/384*R**2*gamma**8-19/1440*R**2*gamma**10+1439/5760*R**2*gamma**8*(gamma+1)-37/240*R**2*gamma**8*(gamma+1)**2+7/60*R**2*(gamma+1)**6-43/40*R**2*gamma**3*(gamma+1)**5-13/144*gamma**3*(gamma+1)**5*P*R-109/5760*R**2*gamma**6-5/144*gamma**5*(gamma+1)**3*P*R+47/180*R**2*gamma**3*(gamma+1)**3-337/5760*R**2*gamma**9-403/5760*R**2*gamma**7-203/3840*R**2*gamma**5*(gamma+1)**2+1/12*gamma**2*(gamma+1)**6*P*R+1789/5760*R**2*gamma**7*(gamma+1)+1/12*gamma**4*(gamma+1)**4*P*R+1/18*gamma**5*(gamma+1)**4*P*R+1/6*(gamma+1)**6*P*R-1/24*gamma**2*(gamma+1)**5*P*R-5/72*gamma**4*(gamma+1)**5*P*R+53/720*R**2*gamma**7*(gamma+1)**3-1409/2880*R**2*gamma**7*(gamma+1)**2+1/36*gamma**3*(gamma+1)**4*P*R+1/36*gamma**3*(gamma+1)**6*P*R-1/36*gamma**4*(gamma+1)**3*P*R+1/20*R**2*gamma**5*(gamma+1)**3+2761/11520*R**2*gamma**4*(gamma+1)**3-1/144*gamma**3*(gamma+1)**3*P*R+83/240*R**2*gamma**3*(gamma+1)**6+929/5760*R**2*gamma**6*(gamma+1)**3+49/720*R**2*gamma**9*(gamma+1)+9/80*R**2*gamma**4*(gamma+1)**6+829/5760*R**2*gamma**6*(gamma+1)-19/120*gamma*R**2*(gamma+1)**5+1453/1920*R**2*gamma**5*(gamma+1)**4+1/480*R**2*gamma**2*(gamma+1)**4)");
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test4() {
        Tensor actual = Tensors.parse("(a*b+b*c+c*a)/(1/a+1/b+1/c)");
        actual = Together.together(actual);
        Tensor expected = Tensors.parse("a*b*c");
        TAssert.assertEquals(actual, expected);

    }
}