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

    @Test
    public void test5() {
        Tensor actual = Tensors.parse("-43/960*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+31751/2880*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)-161/960*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3311/1920*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3833/5760*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-281/60*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-59/12*R**2*la**2*(1+la)**(-1)+34979/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)-7651/1440*R**2*la**4*(1+la)**(-1)+1627/2880*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+(7/45*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-107/30*la+1631/720*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-6841/160*la**4*(1+la)**(-1)*(1+la)**(-1)-4619/5760*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+101/96*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3211/360*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+1729/80*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-18517/960*la**5*(1+la)**(-1)*(1+la)**(-1)-3697/2880*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-179/720*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3109/5760*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+953/1440*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2533/720*la**6*(1+la)**(-1)*(1+la)**(-1)+79/30*la**5*(1+la)**(-1)-2551/2880*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+127/30*la*(1+la)**(-1)+7/6+10387/1152*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-25/48*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-5477/2880*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2825/72*la**3*(1+la)**(-1)*(1+la)**(-1)+881/36*la**2*(1+la)**(-1)-95/9*la**2*(1+la)**(-1)*(1+la)**(-1)+6197/180*la**3*(1+la)**(-1)-301/480*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-23/30*la**4-299/60*la**3-541/60*la**2+1067/1440*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4003/240*la**4*(1+la)**(-1)-803/1440*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+281/1440*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+155/8*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-571/240*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1))*R^{\\mu \\nu }*R_{\\mu \\nu }-3223/360*R**2*la**3*(1+la)**(-1)-667/360*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-1109/288*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-91/60*R**2*la**5*(1+la)**(-1)-1/30*R**2*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+9/20*R**2*la**4-7349/11520*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+157/60*R**2*la**2+181/120*R**2*la**3+103/320*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-13/10*R**2*la*(1+la)**(-1)+859/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+7/12*R**2-20419/11520*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3181/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-533/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-15/64*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+601/72*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)+13/10*R**2*la+25/96*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-4955/2304*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+919/480*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)-139/960*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+17/480*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4/3*R**2*la**2*(1+la)**(-1)*(1+la)**(-1)");
        actual = Expand.expand(Together.together(actual));
        Tensor expected = Tensors.parse("55/4*(la+1)**(-6)*R**2*la**4+61/6*(la+1)**(-6)*R**2*la**5+67/12*(la+1)**(-6)*R**2*la**6+7/12*(la+1)**(-6)*R**2+1/3*(la+1)**(-6)*R**2*la**8+7/2*la*(la+1)**(-6)*R**2+2*(la+1)**(-6)*R**2*la**7+(la+1)**(-6)*(2/3*la**8+14/3*la**7+91/3*la**5+91/6*la**6+112/3*la**3+133/6*la**2+245/6*la**4+23/3*la+7/6)*R_{\\mu \\nu }*R^{\\mu \\nu}+109/12*(la+1)**(-6)*R**2*la**2+41/3*(la+1)**(-6)*R**2*la**3");
        TAssert.assertEquals(actual, expected);
    }
}