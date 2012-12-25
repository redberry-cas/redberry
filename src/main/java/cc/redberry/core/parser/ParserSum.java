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

import cc.redberry.core.number.Complex;

import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserSum extends ParserOperator {

    public static final ParserSum INSTANCE = new ParserSum();

    private ParserSum() {
        super('+', '-');
    }

    @Override
    protected ParseNode compile(List<ParseNode> nodes) {
        return new ParseNode(TensorType.Sum, nodes.toArray(new ParseNode[nodes.size()]));
    }

    @Override
    protected ParseNode inverseOperation(ParseNode node) {
        ParseNode[] content;
        if (node.tensorType == TensorType.Product) {
            content = new ParseNode[1 + node.content.length];
            content[0] = new ParseNodeNumber(Complex.MINUS_ONE);
            System.arraycopy(node.content, 0, content, 1, node.content.length);
        } else
            content = new ParseNode[]{
                new ParseNodeNumber(Complex.MINUS_ONE),
                node
            };
        return new ParseNode(TensorType.Product, content);
    }

    @Override
    public int priority() {
        return 1000;
    }
}
