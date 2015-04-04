/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FeynCalcUtilsTest {
    @Test
    public void testSetMandelstam1() {
        Tensor[][] input = new Tensor[][]{
                {parse("k1_i"), parse("m1")}, {parse("k2_i"), parse("m2")}, {parse("k3_i"), parse("m3")}, {parse("k4_i"), parse("m4")}
        };
        Expression[] ma = FeynCalcUtils.setMandelstam(input);
        // (k1,k1) = m1^2
        // (k2,k2) = m2^2
        // (k3,k3) = m3^2
        // (k4,k4) = m4^2
        // 2(k1, k2) = s - k1^2 - k2^2
        // 2(k3, k4) = s - k3^2 - k4^2
        // -2(k1, k3) = t - k1^2 - k3^2
        // -2(k2, k4) = t - k2^2 - k4^2
        // -2(k1, k4) = u - k1^2 - k4^2
        // -2(k2, k3) = u - k2^2 - k3^2

        for(Expression e: ma)
            System.out.println(e);
    }

    @Test
    public void test2() throws Exception {

        Tensor[][] input = new Tensor[][]{
                {parse("k1_i"), parse("m1")}, {parse("k2_i"), parse("m2")}, {parse("k3_i"), parse("m3")},
                {parse("k4_i"), parse("m4")}, {parse("k5_i"), parse("m5")}
        };
        Expression[] ma = FeynCalcUtils.setMandelstam5(input);

        // k1_{a}*k2^{a} = (1/2)*(-m1**2+s-m2**2)
        // k1_{a}*k3^{a} = (1/2)*(m1**2+m3**2-t1)
        // k1_{a}*k4^{a} = (1/2)*(m1**2-t2+m4**2)
        // k1_{a}*k5^{a} = (1/2)*(-m1**2+t2+s-m4**2-m2**2-m3**2+t1)
        // k2_{a}*k3^{a} = (1/2)*(-u1+m2**2+m3**2)
        // k2_{a}*k4^{a} = (1/2)*(m4**2+m2**2-u2)
        // k5^{a}*k2_{a} = (1/2)*(u1-m1**2+s-m4**2-m2**2-m3**2+u2)
        // k4^{a}*k3_{a} = (1/2)*(-u1-t2+m5**2+2*m1**2-s+m4**2+m3**2-t1-u2+2*m2**2)
        // k5^{a}*k3_{a} = (1/2)*(-m1**2+t2-m5**2+s-m4**2-m2**2-m3**2+u2)
        // k5^{a}*k4_{a} = (1/2)*(u1-m1**2-m5**2+s-m4**2-m2**2-m3**2+t1)

        for(Expression e: ma)
            System.out.println(e);

    }
}
