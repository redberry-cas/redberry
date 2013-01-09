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

import java.util.Arrays;

/**
 * Parser for powers of form a**b.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserPowerAst implements TokenParser {
    /**
     * Singleton instance.
     */
    public static final ParserPowerAst INSTANCE = new ParserPowerAst();

    private ParserPowerAst() {
    }

    @Override
    public ParseToken parseToken(String expression, Parser parser) {
        char[] expressionChars = expression.toCharArray();
        int level = 0;
        char c;
        String argString = null, powerString = null;
        for (int i = 0; i < expressionChars.length; ++i) {
            c = expressionChars[i];

            if (c == '(' || c == '[')
                level++;
            if (c == ')' || c == ']')
                level--;
            if (level < 0)
                throw new BracketsError();

            if (c == '*' && level == 0)
                if (i + 1 < expressionChars.length && expressionChars[i + 1] == '*') {
                    argString = new String(Arrays.copyOfRange(expressionChars, 0, i));
                    powerString = new String(Arrays.copyOfRange(expressionChars, i + 2, expressionChars.length));
                    break;
                }
        }
        if (argString == null)
            return null;

        ParseToken arg = parser.parse(argString);
        if (arg == null)
            return null;

        ParseToken power = parser.parse(powerString);
        if (power == null)
            return null;
        return new ParseToken(TokenType.Power, arg, power);
    }

    @Override
    public int priority() {
        return 990;
    }
}
