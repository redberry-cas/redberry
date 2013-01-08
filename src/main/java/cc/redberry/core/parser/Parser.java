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

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Parser {

    public static final Parser DEFAULT =
            new Parser(ParserBrackets.INSTANCE,
                    ParserSum.INSTANCE,
                    ParserProduct.INSTANCE,
                    ParserSimpleTensor.INSTANCE,
                    ParserTensorField.INSTANCE,
                    ParserPower.INSTANCE,
                    ParserNumber.INSTANCE,
                    ParserFunctions.INSTANCE,
                    ParserExpression.INSTANCE,
                    ParserPowerAst.INSTANCE);
    private final TokenParser[] tokenParsers;

    public Parser(TokenParser... tokenParsers) {
        this.tokenParsers = tokenParsers;
        Arrays.sort(tokenParsers, NodeParserComparator.INSTANCE);
    }

    public ParseToken parse(String expression) {
        if (expression.isEmpty())
            throw new IllegalArgumentException("Empty expression.");
        for (TokenParser tokenParser : tokenParsers) {
            ParseToken node = tokenParser.parseNode(expression.trim(), this);
            if (node != null)
                return node;
        }
        throw new ParserException("No appropriate parser for expression: \"" + expression + "\"");
    }
}
