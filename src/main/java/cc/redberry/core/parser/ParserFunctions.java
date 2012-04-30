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
public class ParserFunctions implements NodeParser {
    public static final ParserFunctions INSTANCE = new ParserFunctions();
    private static final String[] functions = {"Sin", "Cos", "Tan","Log","Exp",""};

    private ParserFunctions() {
    }

    @Override
    public int priority() {
        return 9987;
    }

    @Override
    public ParseNode parseNode(String expression, Parser parser) {
        if (!expression.contains("[") || expression.lastIndexOf(']') != expression.length() - 1)
            return null;
        String function = null;
        for (int i = 0; i < functions.length; ++i) {
            function = functions[i];
            if (expression.length() - 2 < function.length())
                continue;
            if (expression.substring(0, function.length()).equals(function))
                break;
            function = null;
        }
        if (function == null)
            return null;
        if (expression.charAt(function.length()) != '[')
            return null;

        int level = 0;
        char c;
        for (int i = function.length() + 1; i < expression.length() - 2; ++i) {
            c = expression.charAt(i);
            if (c == '[')
                level++;
            if (c == ']')
                level--;
            if (level < 0)
                return null;
        }
        String argument = expression.substring(function.length() + 1, expression.length() - 1);
        return new ParseNodeScalarFunction(function, new ParseNode[]{parser.parse(argument)});
    }
}
