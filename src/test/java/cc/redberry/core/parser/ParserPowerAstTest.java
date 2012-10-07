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

package cc.redberry.core.parser;

import cc.redberry.core.tensor.Power;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

public class ParserPowerAstTest {
    @Test
    public void test0() {
        Tensor tensor = Tensors.parse("a**b");
        Assert.assertEquals(tensor.getClass(), Power.class);
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, "Power[a,b]"));
    }

    @Test
    public void test1() {
        Assert.assertTrue(TensorUtils.equalsExactly(Tensors.parse("a**b**c"), "Power[a,Power[b,c]]"));
        Assert.assertTrue(TensorUtils.equalsExactly(Tensors.parse("a**(b**c)"), "Power[a,Power[b,c]]"));
        Assert.assertTrue(TensorUtils.equalsExactly(Tensors.parse("(a**b)**c"), "Power[a,b*c]"));
    }

    @Test
    public void testProduct0() {
        Tensor tensor = Tensors.parse("a**b*c");
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, "Power[a,b]*c"));
    }

    @Test
    public void testProduct1() {
        Tensor tensor = Tensors.parse("c*a**b");
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, "Power[a,b]*c"));
    }

    @Test
    public void testSum0() {
        Tensor tensor = Tensors.parse("a**b+c");
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, "Power[a,b]+c"));
    }

    @Test
    public void testSum1() {
        Tensor tensor = Tensors.parse("a**(b+c)");
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, "Power[a,b+c]"));
    }

    @Test
    public void testSum2() {
        Tensor tensor = Tensors.parse("c+a**b");
        Assert.assertTrue(TensorUtils.equalsExactly(tensor, "Power[a,b]+c"));
    }
}
