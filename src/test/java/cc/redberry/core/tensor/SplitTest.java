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
 * the Free Software Foundation, either version 2 of the License, or
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

import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SplitTest {

    @Test
    public void testSplitScalars1() {
        Tensor t = Tensors.parse("2*a");
        Split split = Split.splitScalars(t);
        Assert.assertTrue(TensorUtils.equals(split.factor, t));
        Assert.assertTrue(TensorUtils.equals(split.summand, Complex.ONE));
    }

    @Test
    public void testSplitScalars2() {
        Tensor t = Tensors.parse("2*a*g_mn");
        Split split = Split.splitScalars(t);
        Assert.assertTrue(TensorUtils.equals(split.factor, Tensors.parse("g_mn")));
        Assert.assertTrue(TensorUtils.equals(split.summand, Tensors.parse("2*a")));
    }

    @Test
    public void testSplitScalars3() {
        Tensor t = Tensors.parse("g^ab*g^cd*g_mn*F_ab*K_cd");
        Split split = Split.splitScalars(t);
        Assert.assertTrue(TensorUtils.equals(split.factor, Tensors.parse("g_mn")));
        Assert.assertTrue(TensorUtils.equals(split.summand, Tensors.parse("g^ab*g^cd*F_ab*K_cd")));
    }

    @Test
    public void testSplitScalars4() {
        Split s1 = Split.splitScalars(Tensors.parse("c1*k_{b}*k^{c}"));
        Split s2 = Split.splitScalars(Tensors.parse("(c0-c0*a**(-1))*k_{i}*k^{i}*k_{b}*k^{c}"));
        Assert.assertEquals(s1.factor.hashCode(), s2.factor.hashCode());
    }
}
