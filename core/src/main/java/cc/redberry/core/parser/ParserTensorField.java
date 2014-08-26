/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for tensor fields.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserTensorField implements TokenParser {
    /**
     * Singleton instance.
     */
    public static final ParserTensorField INSTANCE = new ParserTensorField();

    private ParserTensorField() {
    }

    @Override
    public int priority() {
        return 7000;
    }

    public boolean canParse(String expression) {
        char[] expressionChars = expression.toCharArray();
        int level = 0;
        boolean inBrackets = false;
        for (char c : expressionChars) {
            if ((c == '+' || c == '*' || c == '-' || c == '/') && !inBrackets)
                return false;
            if (c == '[') {
                inBrackets = true;
                level++;
            }
            if (c == ']')
                level--;
            if (level == 0)
                inBrackets = false;
            if (level < 0)
                throw new BracketsError(expression);
        }
        return true;
    }

    @Override
    public ParseToken parseToken(String expression, Parser parser) {
        if (!expression.contains("["))
            return null;
        if (!canParse(expression))
            return null;

        String tensorPart = expression.substring(0, expression.indexOf('['));
        ParseTokenSimpleTensor simpleTensorNode = ParserSimpleTensor.INSTANCE.parseToken(tensorPart, parser);

        String argString = expression.substring(expression.indexOf("[") + 1, expression.length() - 1);

        List<ParseToken> arguments = new ArrayList<>();
        List<Indices> indices = new ArrayList<>();

        int beginIndex = 0, level = 0;
        char[] argsChars = argString.toCharArray();
        ParseToken a;
        SimpleIndices aIndices;
        for (int i = 0; i < argsChars.length; ++i) {
            char c = argsChars[i];

            if ((c == ',' && level == 0) || i == argsChars.length - 1) {
                String argument = argString.substring(beginIndex, i == argsChars.length - 1 ? i + 1 : i);
                String[] split = argument.split(":");
                if (split.length == 1) {
                    a = parser.parse(argument);
                    aIndices = null;
                } else {
                    if (split.length != 2)
                        throw new ParserException(expression);
                    a = parser.parse(split[0]);
                    aIndices = ParserIndices.parseSimple(split[1]);
                }
                arguments.add(a);
                indices.add(aIndices);
                beginIndex = i + 1;
            }
            if (c == '[')
                ++level;
            if (c == ']')
                --level;
        }

        //todo fix CORE-106
        //Sqrt[x]
        if (simpleTensorNode.name.toLowerCase().equals("sqrt")
                && simpleTensorNode.indices.size() == 0
                && arguments.size() == 1
                && arguments.get(0).getIndices().getFree().size() == 0)
            return new ParseToken(TokenType.Power, new ParseToken[]{arguments.get(0), new ParseTokenNumber(Complex.ONE_HALF)});

        return new ParseTokenTensorField(
                simpleTensorNode.indices,
                simpleTensorNode.name,
                arguments.toArray(new ParseToken[arguments.size()]),
                indices.toArray(new SimpleIndices[indices.size()]));
    }
}
