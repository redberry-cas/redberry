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

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserFunctions implements TokenParser {

    public static final ParserFunctions INSTANCE = new ParserFunctions();
    private static final String[] functions = {"Sin", "Cos", "Tan", "Log", "Exp", "Cot", "ArcSin", "ArcCos", "ArcTan", "ArcCot"};

    private ParserFunctions() {
    }

    @Override
    public int priority() {
        return 9987;
    }

    @Override
    public ParseToken parseNode(String expression, Parser parser) {
        if (!expression.contains("[") || expression.lastIndexOf(']') != expression.length() - 1)
            return null;
        String temp = null, function = null;
        for (int i = 0; i < functions.length; ++i) {
            temp = functions[i];
            if (expression.length() - 2 < temp.length())
                continue;
            if (expression.substring(0, temp.length()).equals(temp)) {
                function = temp;
                break;
            }
            temp = null;
        }
        if (function == null)
            return null;
        if (expression.charAt(function.length()) != '[')
            return null;

        int level = 0;
        char c;
        for (int i = temp.length() + 1; i < expression.length() - 2; ++i) {
            c = expression.charAt(i);
            if (c == '[')
                level++;
            if (c == ']')
                level--;
            if (level < 0)
                return null;
            if (c == ',' && level == 0)//case for Sin[x,y]
                throw new ParserException("Sin, Cos, Tan and others scalar functions take only one argument.");
        }
        String argument = expression.substring(temp.length() + 1, expression.length() - 1);
        return new ParseTokenScalarFunction(temp, new ParseToken[]{parser.parse(argument)});
    }
}
