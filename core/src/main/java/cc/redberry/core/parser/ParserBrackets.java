/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

/**
 * Parses expressions of form in brackets.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserBrackets implements TokenParser {
    public static final ParserBrackets INSTANCE = new ParserBrackets();

    private ParserBrackets() {
    }

    private final static int parserID = Integer.MAX_VALUE;

    @Override
    public ParseToken parseToken(String expression, Parser parser) {
        if (expression.charAt(0) == '(')
            if (expression.charAt(expression.length() - 1) != ')')
                checkWithException(expression);
            else {
                int level = 0;
                for (int i = 0; i < expression.length(); ++i) {
                    char c = expression.charAt(i);
                    if (c == '(')
                        level++;
                    if (level < 1)
                        return null;
                    if (c == ')')
                        level--;
                }
                if (level != 0)
                    throw new BracketsError();
                return parser.parse(expression.substring(1, expression.length() - 1));
            }
        return null;
    }

    @Override
    public int priority() {
        return parserID;
    }

    private void checkWithException(String expression) {
        int level = 0;
        for (int i = 0; i < expression.length(); ++i) {
            char c = expression.charAt(i);
            if (c == '(')
                level++;
            if (c == ')')
                level--;
        }
        if (level != 0)
            throw new BracketsError(expression);
    }
}
