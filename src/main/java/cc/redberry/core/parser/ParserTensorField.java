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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.SimpleIndices;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserTensorField implements NodeParser {
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
                throw new BracketsError();
        }
        return true;
    }

    @Override
    public ParseNode parseNode(String expression, Parser parser) {
        if (!expression.contains("["))
            return null;
        if (!canParse(expression))
            return null;

        String tensorPart = expression.substring(0, expression.indexOf('['));
        ParseNodeSimpleTensor simpleTensorNode = ParserSimpleTensor.INSTANCE.parseNode(tensorPart, parser);

        String argString = expression.substring(expression.indexOf("[") + 1, expression.length() - 1);

        List<ParseNode> arguments = new ArrayList<>();
        List<Indices> indices = new ArrayList<>();

        int beginIndex = 0, level = 0;
        char[] argsChars = argString.toCharArray();
        ParseNode a;
        SimpleIndices aIndices;
        for (int i = 0; i < argsChars.length; ++i) {
            char c = argsChars[i];

            if ((c == ',' && level == 0) || i == argsChars.length - 1) {
                String argument = argString.substring(beginIndex, i == argsChars.length - 1 ? i + 1 : i);
                String[] split = argument.split(":");
                if (split.length == 1) {
                    a = parser.parse(argument);
                    aIndices = null;//CHECKSTYLE IndicesFactory.createIsolated(a.getIndices().getFreeIndices());
                } else {
                    if (split.length != 2)
                        throw new ParserException(expression);
                    a = parser.parse(split[0]);
                    aIndices = ParserIndices.parseSimple(split[1]);
                    //TODO add assertion on indices compatability
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
        return new ParseNodeTensorField(
                simpleTensorNode.indices,
                simpleTensorNode.name,
                arguments.toArray(new ParseNode[arguments.size()]),
                indices.toArray(new SimpleIndices[indices.size()]));
    }
}
