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
package cc.redberry.core.number.parser;

import cc.redberry.core.number.ComplexElement;
import cc.redberry.core.parser.BracketsError;

public abstract class OperatorParser implements ElementParser {
    private char operatorSymbol;
    private char operatorInverseSymbol;

    public OperatorParser(char operatorSymbol, char operatorInverseSymbol) {
        this.operatorSymbol = operatorSymbol;
        this.operatorInverseSymbol = operatorInverseSymbol;
    }

    @Override
    public boolean canParse(String expression) {
        char[] expressionChars = expression.toCharArray();
        int level = 0;
        for (char c : expressionChars) {
            if (c == '(' || c == '[')
                level++;
            if (c == ')' || c == ']')
                level--;
            if (level < 0)
                throw new BracketsError(expression);
            if (c == operatorSymbol && level == 0)
                return true;
            if (c == operatorInverseSymbol && level == 0)
                return true;
        }
        return false;
    }

    protected enum Mode {
        Direct, Inverse
    };

    public ComplexElement parse(String expression) {
        char[] expressionChars = expression.toCharArray();
        StringBuffer buffer = new StringBuffer();
        ComplexElement temp = null;
        int level = 0;
        Mode inverseMode = Mode.Direct;
//        int i = 0;
//        if (expressionChars[0] == operatorSymbol)
//            i = 1;
//        if (expressionChars[0] == operatorInverseSymbol) {
//            i = 1;
//            inverseMode = Mode.Inverse;
//        }
//        for (; i < expressionChars.length; ++i) {
//            char c = expressionChars[i];
        for (char c : expressionChars) {
            if (c == '(')
                level++;
            if (c == ')')
                level--;
            if (level < 0)
                throw new BracketsError();
            if (c == operatorSymbol && level == 0) {
                String toParse = buffer.toString();
                if (!toParse.isEmpty())
                    if (temp == null)
                        temp = Parser.parse(toParse);
                    else
                        temp = modeCompiler(temp, Parser.parse(toParse), inverseMode);
                buffer = new StringBuffer();
                inverseMode = Mode.Direct;
            } else if (c == operatorInverseSymbol && level == 0) {
                String toParse = buffer.toString();
                if (!toParse.isEmpty()) {
                    if (temp == null)
                        temp = OperatorParser.this.getNeutralElement();
                    temp = modeCompiler(temp, Parser.parse(toParse), inverseMode);
                }
                buffer = new StringBuffer();
                inverseMode = Mode.Inverse;
            } else
                buffer.append(c);
        }
        if (temp == null)
            temp = OperatorParser.this.getNeutralElement();
        temp = modeCompiler(temp, Parser.parse(buffer.toString()), inverseMode);
        return temp;
    }

    protected abstract ComplexElement modeCompiler(ComplexElement first, ComplexElement second, Mode mode);

    protected abstract ComplexElement getNeutralElement();
}
