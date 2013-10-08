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
package cc.redberry.core.tensor;

import cc.redberry.core.context.OutputFormat;
import junit.framework.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ToStringTest {
    @Test
    public void test1() {
        Tensor t = parse("T_{\\mu\\nu}");
        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[-\\[Mu],-\\[Nu]]");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[mu,nu]");
    }

    @Test
    public void test2() {
        Tensor t = parse("T_{\\mu\\nu} * F_a^b * a**2");
        System.out.println(t.toString(OutputFormat.WolframMathematica));
        System.out.println(t.toString(OutputFormat.Maple));
//        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[-\\[Mu],-\\[Nu]]");
//        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[mu,nu]");
    }
}
