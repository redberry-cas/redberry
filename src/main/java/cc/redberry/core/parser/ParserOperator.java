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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class ParserOperator implements TokenParser {

    private char operatorSymbol;
    private char operatorInverseSymbol;

    protected ParserOperator(char operatorSymbol, char operatorInverseSymbol) {
        this.operatorSymbol = operatorSymbol;
        this.operatorInverseSymbol = operatorInverseSymbol;
    }

    protected final boolean canParse(String expression) {
        char[] expressionChars = expression.toCharArray();
        int level = 0;

        char c;
        for (int i = 0; i < expressionChars.length; ++i) {
            c = expressionChars[i];

            if (c == '(' || c == '[')
                level++;
            if (c == ')' || c == ']')
                level--;
            if (level < 0)
                throw new BracketsError();
            if (c == operatorSymbol && level == 0 && testOperator(expressionChars, i))
                return true;
            if (c == operatorInverseSymbol && level == 0)
                return true;
        }
        return false;
    }

    private enum Mode {
        Direct, Inverse
    }

    @Override
    public ParseToken parseNode(String expression, Parser parser) {
        if (!canParse(expression))
            return null;
        expression = expression.replace("--", "+");
        expression = expression.replace("++", "+");
        expression = expression.replace("+-", "-");
        expression = expression.replace("-+", "-");
        char[] expressionChars = expression.toCharArray();
        StringBuffer buffer = new StringBuffer();
        List<ParseToken> nodes = new ArrayList<>();
        int level = 0, indicesLevel = 0;
        Mode mode = Mode.Direct;

        char c;
        for (int i = 0; i < expressionChars.length; ++i) {
            c = expressionChars[i];

            if (c == '(' || c == '[')
                level++;
            if (c == '{')
                indicesLevel++;
            if (c == '}')
                indicesLevel--;
            if (c == ')' || c == ']')
                level--;
            if (level < 0)
                throw new BracketsError();
            if (c == ' ' && indicesLevel == 0)
                continue;
            if (c == operatorSymbol && level == 0 && testOperator(expressionChars, i)) {
                String toParse = buffer.toString();
                if (!toParse.isEmpty())
                    modeParser(toParse, mode, parser, nodes);
                buffer = new StringBuffer();
                mode = Mode.Direct;
            } else if (c == operatorInverseSymbol && level == 0) {
                String toParse = buffer.toString();
                if (!toParse.isEmpty())
                    modeParser(toParse, mode, parser, nodes);
                buffer = new StringBuffer();
                mode = Mode.Inverse;
            } else
                buffer.append(c);
        }
        modeParser(buffer.toString(), mode, parser, nodes);
        return compile(nodes);
    }

    private void modeParser(String expression, Mode mode, Parser parser, List<ParseToken> nodes) {
        if (mode == Mode.Direct) {
            nodes.add(parser.parse(expression));
            return;
        }
        if (mode == Mode.Inverse)
            nodes.add(inverseOperation(parser.parse(expression)));
        else
            throw new ParserException("unrepoted operator parser mode");
    }

    protected boolean testOperator(char[] expressionChars, int position) {
        return true;
    }

    protected abstract ParseToken compile(List<ParseToken> nodes);

    protected abstract ParseToken inverseOperation(ParseToken tensor);
}
