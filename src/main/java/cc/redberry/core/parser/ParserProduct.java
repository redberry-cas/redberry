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
 * Parser of mathematical product.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserProduct extends ParserOperator {
    /**
     * Singleton instance.
     */
    public static final ParserProduct INSTANCE = new ParserProduct();

    private ParserProduct() {
        super('*', '/');
    }

    @Override
    protected ParseToken compile(List<ParseToken> nodes) {
        return new ParseToken(TokenType.Product, nodes.toArray(new ParseToken[nodes.size()]));
    }

    @Override
    protected ParseToken inverseOperation(ParseToken node) {
        return new ParseToken(TokenType.Power, node, new ParseTokenNumber(Complex.MINUS_ONE));
    }

    @Override
    protected boolean testOperator(char[] expressionChars, int position) {
        return !((position + 1 < expressionChars.length && expressionChars[position + 1] == '*') ||
                (position - 1 >= 0 && expressionChars[position - 1] == '*'));
    }

    @Override
    public int priority() {
        return 999;
    }
}
