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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.context.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.*;
import org.junit.*;
import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandBracketsTest {

    @Test
    public void test1() {
        Tensor t = parse("(a+b)*c+a*c");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("2*a*c+b*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test2() {
        Tensor t = parse("(a+b)*c-a*c");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("b*c");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test3() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*p_i");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("b*c*p_i");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test4() {
        Tensor t = parse("(a*p_i+b*p_i)*c-a*c*k_i");
        Tensor actual = ExpandBrackets.expandBrackets(t);
        Tensor expected = Tensors.parse("(a*c+c*b)*p_i-a*c*k_i");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test5() {
        Tensor actual = parse("c*(a*(c+n)+b)");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("c*a*c+c*a*n+c*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }

    @Test
    public void test6() {
        Tensor actual = parse("a*(c+b)");
        actual = ExpandBrackets.expandBrackets(actual);
        Tensor expected = parse("a*c+a*b");
        Assert.assertTrue(TensorUtils.equals(actual, expected));
    }
}