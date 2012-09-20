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

import cc.redberry.core.number.*;
import cc.redberry.core.utils.*;
import org.junit.*;
import static cc.redberry.core.TAssert.*;

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
        Tensor t = Tensors.parse("g^ab*g^cd*g_mn*F_zxab^m*K_cd");
        Split split = Split.splitScalars(t);
        System.out.println(split);
//        Assert.assertTrue(TensorUtils.equals(split.factor, Tensors.parse("g_mn")));
//        Assert.assertTrue(TensorUtils.equals(split.summand, Tensors.parse("2*a")));
    }
}