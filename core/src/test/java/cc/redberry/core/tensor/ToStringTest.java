/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
        Tensor t = parse("T_{\\mu_{1} \\nu_{2}} ");
        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[-Subscript[\\[Mu], 1],-Subscript[\\[Nu], 2]]");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[mu1,nu2]");
    }

    @Test
    public void test3() {
        Tensor t = parse("T_{\\mu_{1} \\nu_{2}}^abc_d");
        Assert.assertEquals(t.toString(OutputFormat.Cadabra), "T_{a b c d \\mu_{1} \\nu_{2}}");
    }

    @Test
    public void test4() {
        Tensor t = parse("T^{\\mu\\nu}");
        Assert.assertEquals(t.toString(OutputFormat.WolframMathematica), "T[\\[Mu],\\[Nu]]");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "T[~mu,~nu]");
        Assert.assertEquals(t.toString(OutputFormat.Redberry), "T^{\\mu\\nu}");
    }

    @Test
    public void test5() {
        Tensor t = parse("g_mn");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "g_[m,n]");
        t = parse("d_m^n");
        Assert.assertEquals(t.toString(OutputFormat.Maple), "KroneckerDelta[m,~n]");
    }

    @Test
    public void test6() {
        TensorField t = (TensorField) parse("f~(1)[x]");
        Assert.assertEquals("Derivative[1][f][x]", t.toString(OutputFormat.WolframMathematica));
        t = (TensorField) parse("f~(1,2,0)[x,y,2]");
        Assert.assertEquals("Derivative[1,2,0][f][x,y,2]", t.toString(OutputFormat.WolframMathematica));
    }

    @Test
    public void test7() {
        TensorField t = (TensorField) parse("f~(1)[x]");
        Assert.assertEquals("D[1](f)(x)", t.toString(OutputFormat.Maple));
        t = (TensorField) parse("f~(1,2,0)[x,y,2]");
        Assert.assertEquals("D[1,2,2](f)(x,y,2)", t.toString(OutputFormat.Maple));
    }


}
