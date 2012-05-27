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

import cc.redberry.core.indices.EmptyIndices;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserTest {

    @Test
    public void test1() {
        ParseNode node = Parser.DEFAULT.parse("2*a_\\mu-b_\\mu/(c*x)*x[x,y]");
        System.out.println(node);
    }

    @Test
    public void test2() {
        ParseNode node = Parser.DEFAULT.parse("f[a_\\mu] - f[b_\\mu/ (c * g) * g[x, y]]");
        ParseNode expected = new ParseNode(TensorType.Sum,
                                           new ParseNodeTensorField(EmptyIndices.INSTANCE, "f", new ParseNode[]{new ParseNodeSimpleTensor(ParserIndices.parseSimple("_\\mu"), "a")}, new SimpleIndices[]{EmptyIndices.INSTANCE}),
                                           new ParseNode(TensorType.Product,
                                                         new ParseNodeNumber(Complex.MINUSE_ONE),
                                                         new ParseNodeTensorField(EmptyIndices.INSTANCE, "f",
                                                                                  new ParseNode[]{new ParseNode(TensorType.Product,
                                                                                                                new ParseNodeSimpleTensor(ParserIndices.parseSimple("_\\mu"), "b"),
                                                                                                                new ParseNode(TensorType.Pow, new ParseNode(TensorType.Product, new ParseNodeSimpleTensor(EmptyIndices.INSTANCE, "c"), new ParseNodeSimpleTensor(EmptyIndices.INSTANCE, "g")),
                                                                                                                              new ParseNodeNumber(Complex.MINUSE_ONE)),
                                                                                                                new ParseNodeTensorField(EmptyIndices.INSTANCE, "g",
                                                                                                                                         new ParseNode[]{new ParseNodeSimpleTensor(EmptyIndices.INSTANCE, "x"),
                                                                                                                                                         new ParseNodeSimpleTensor(EmptyIndices.INSTANCE, "y")},
                                                                                                                                         new SimpleIndices[]{EmptyIndices.INSTANCE, EmptyIndices.INSTANCE}))},
                                                                                  new SimpleIndices[]{EmptyIndices.INSTANCE})));
        Assert.assertEquals(expected, node);
    }

    @Test
    public void test3() {
        ParseNode node = Parser.DEFAULT.parse("f[b_\\mu/(c*g)*g[x,y]]");
        System.out.println(node);
        System.out.println(node.getClass());
    }

    @Test
    public void test4() {
        ParseNode node = Parser.DEFAULT.parse("a-b");
        ParseNode expected = new ParseNode(TensorType.Sum,
                                           new ParseNodeSimpleTensor(EmptyIndices.INSTANCE, "a"),
                                           new ParseNode(TensorType.Product,
                                                         new ParseNodeNumber(Complex.MINUSE_ONE),
                                                         new ParseNodeSimpleTensor(EmptyIndices.INSTANCE, "b")));
        Assert.assertEquals(expected, node);
    }

    @Test
    public void testReallySimpleTensor() {
        ParseNode node = Parser.DEFAULT.parse("S^k*(c_k*Power[a,1]/a-b_k)");
        Tensor t = node.toTensor();
        System.out.println(((Product)t).getScalars()[0]);
    }
}
