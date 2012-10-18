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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumBijectionPortTest {

    @Test
    public void test1() {
        Tensor from = Tensors.parse("a+b");
        Tensor to = Tensors.parse("a+b+c");
        System.out.println(from);
        System.out.println(to);
        SumBijectionPort port = new SumBijectionPort(from, to);
        BijectionContainer bc;
        while ((bc = port.take()) != null)
            System.out.println(bc);
    }

    @Test
    public void test2() {
        Tensors.addSymmetry("b_nm", IndexType.LatinLower, true, 1, 0);
        Tensor from = Tensors.parse("a_mn+b_mn");
        Tensor to = Tensors.parse("a_mn-b_nm+c_mn");
        System.out.println(from);
        System.out.println(to);
        SumBijectionPort port = new SumBijectionPort(from, to);
        BijectionContainer bc;
        while ((bc = port.take()) != null)
            System.out.println(bc);
    }

    @Test
    public void test3() {
        Tensor from = Tensors.parse("a_mn+a_nm+x_mn+x_nm");
        Tensor to = Tensors.parse("a_mn+a_nm+c_mn+x_mn+x_nm");
        System.out.println(from);
        System.out.println(to);
        SumBijectionPort port = new SumBijectionPort(from, to);
        BijectionContainer bc;
        while ((bc = port.take()) != null)
            System.out.println(bc);
    }

    @Test
    public void test4() {
        Tensor u = Tensors.parse("f_{cd}+V_{cd}");
        Tensor v = Tensors.parse("c + d");
        Assert.assertTrue(new SumBijectionPort(v, u).take() == null);
    }
}
