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

import cc.redberry.core.context.OutputFormat;
import junit.framework.Assert;
import org.junit.Test;

public class PowerFactoryTest {

    @Test
    public void rationalValuesTest() {
        Assert.assertEquals(Tensors.parse("1/4"),
                            Tensors.parse("Power[1/2,2]"));

        Assert.assertEquals(Tensors.parse("1/3"),
                            Tensors.parse("Power[1/9,1/2]"));

        Assert.assertEquals(Tensors.parse("3"),
                            Tensors.parse("Power[1/9,-1/2]"));

        Assert.assertEquals(Tensors.parse("27"),
                            Tensors.parse("Power[1/9,-3/2]"));
    }

    @Test
    public void rationalValuesNegativeTest() {
        Assert.assertEquals("(1/2)**(1/2)",
                            Tensors.parse("Power[1/2,1/2]").toString(OutputFormat.Redberry));

        Assert.assertEquals("(1/2)**(1/3)",
                            Tensors.parse("Power[1/2,1/3]").toString(OutputFormat.Redberry));
    }

    @Test
    public void testPower1() {
        Tensor expected = Tensors.parse("3+I");
        Tensor actual = Tensors.parse("(28+I*96)**(1/4)");
        Assert.assertEquals(expected, actual);
    }
}
