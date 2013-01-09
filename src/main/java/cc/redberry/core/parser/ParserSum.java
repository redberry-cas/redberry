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
package cc.redberry.core.parser;

import cc.redberry.core.number.Complex;

import java.util.List;

/**
 * Parser for mathematical sums.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserSum extends ParserOperator {
    /**
     * Singleton instance.
     */
    public static final ParserSum INSTANCE = new ParserSum();

    private ParserSum() {
        super('+', '-');
    }

    @Override
    protected ParseToken compile(List<ParseToken> nodes) {
        return new ParseToken(TokenType.Sum, nodes.toArray(new ParseToken[nodes.size()]));
    }

    @Override
    protected ParseToken inverseOperation(ParseToken node) {
        ParseToken[] content;
        if (node.tokenType == TokenType.Product) {
            content = new ParseToken[1 + node.content.length];
            content[0] = new ParseTokenNumber(Complex.MINUS_ONE);
            System.arraycopy(node.content, 0, content, 1, node.content.length);
        } else
            content = new ParseToken[]{
                    new ParseTokenNumber(Complex.MINUS_ONE),
                    node
            };
        return new ParseToken(TokenType.Product, content);
    }

    @Override
    public int priority() {
        return 1000;
    }
}
