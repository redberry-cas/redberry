/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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
package cc.redberry.core.number;

import cc.redberry.core.tensor.Tensors;
import org.junit.Assert;
import org.junit.Test;

public class ComplexUtilsTest {
    private static final double DELTA = 1E-10;

    @Test
    public void test() {
        atomicTest((Complex) Tensors.parse("1+2*I"), true);
        atomicTest((Complex) Tensors.parse("7*I"), false);
    }

    private static void atomicTest(Complex input, boolean exp) {
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arcsin(ComplexUtils.sin(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.sin(ComplexUtils.arcsin(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arccos(ComplexUtils.cos(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.cos(ComplexUtils.arccos(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arctan(ComplexUtils.tan(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.tan(ComplexUtils.arctan(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.arccot(ComplexUtils.cot(input))).absNumeric(), DELTA);
        Assert.assertEquals(0.0, input.subtract(ComplexUtils.cot(ComplexUtils.arccot(input))).absNumeric(), DELTA);

        Assert.assertEquals(0.0, input.subtract(ComplexUtils.exp(ComplexUtils.log(input))).absNumeric(), DELTA);
        if (exp)
            Assert.assertEquals(0.0, input.subtract(ComplexUtils.log(ComplexUtils.exp(input))).absNumeric(), DELTA);
    }
}
