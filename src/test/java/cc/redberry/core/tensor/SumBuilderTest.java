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

import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumBuilderTest {

    @Test
    public void test1() {
        SumBuilder isb = new SumBuilder();
        isb.put(parse("a"));
        isb.put(parse("2*a"));
        isb.put(parse("-3*a"));
        isb.put(parse("a*b"));
        isb.put(parse("7*a*b"));
        isb.put(parse("Sin[c]"));
        isb.put(parse("d"));
        isb.put(parse("Sin[-c]"));

        Tensor expected = Tensors.parse("8*a*b+d");
        Assert.assertTrue(TensorUtils.equalsExactly(expected, isb.build()));
    }

    @Test
    public void test2() {
        SumBuilder isb = new SumBuilder();
        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(TensorUtils.equalsExactly(expected, isb.build()));
    }

    @Test
    public void test3() {
        SumBuilder isb = new SumBuilder();
        isb.put(parse("a"));
        isb.put(parse("2*a"));
        isb.put(parse("-3*a"));
        isb.put(parse("0"));
        isb.put(parse("-Power[d,2]"));
        isb.put(parse("Sin[c]"));
        isb.put(parse("Power[d,2]"));
        isb.put(parse("Sin[-c]"));
        isb.put(parse("(1/2)*Cos[-c]"));
        isb.put(parse("(1/2)*Cos[-c]"));

        Tensor expected = Tensors.parse("Cos[c]");
        Assert.assertTrue(IndexMappings.mappingExists(expected, isb.build()));
    }

    @Test
    public void test5() {
        SumBuilder isb = new SumBuilder();
        isb.put(parse("a_mn"));
        isb.put(parse("2*a_mn"));
        isb.put(parse("-3*a_mn"));


        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(IndexMappings.mappingExists(expected, isb.build()));
    }

    @Test
    public void test6() {
        Tensor t = Tensors.parse("0+a_m^m+2*a_m^m-3*a_m^m+3-3+Sin[x]");
        Tensor expected = Tensors.parse("Sin[x]");
        Assert.assertTrue(IndexMappings.mappingExists(expected, t));
    }

    @Test
    public void test7() {
        Tensor t = Tensors.parse("0*(a_m^m+2*a_m^m-3*a_m^m)+3-3+Sin[x]+Sin[-x]");
        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(IndexMappings.mappingExists(expected, t));
    }

    @Test
    public void test8() {
        Tensor t = Tensors.parse("1/(0*(a_m^m+2*a_m^m-3*a_m^m))+3-3+Sin[x]");
        Tensor expected = Tensors.parse("NaN+I*NaN");
        Assert.assertTrue(IndexMappings.mappingExists(expected, t));
    }

    @Test
    public void test9() {
        Tensors.addSymmetry("F_{ab}", IndexType.LatinLower, true, new int[]{1, 0});
        Tensor e = Tensors.parse("F_{mn}*F^{mn}+F_{mn}*F^{nm}");
        Tensor expected = Tensors.parse("0");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test10() {
        Tensor e = Tensors.parse("2*f_m+a*f_m");
        Tensor expected = Tensors.parse("(2+a)*f_m");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test11() {
        Tensor e = Tensors.parse("0+2*f_m+a*f_m-a*b/b*f_m-f_m");
        Tensor expected = Tensors.parse("f_m");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }

    @Test
    public void test12() {
        Tensor e = Tensors.parse("2*(A_M+A_M)+A_M");
        Tensor expected = Tensors.parse("5*A__M");
        Assert.assertTrue(IndexMappings.mappingExists(expected, e));
    }
}