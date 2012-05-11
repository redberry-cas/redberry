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

/**
 *
 * @author Stanislav Poslavsky
 */
public abstract class OperatorToken<T extends cc.redberry.core.number.Number<T>> implements TokenParser<T> {
    private final char operationSymbol, operationInverseSymbol;

    public OperatorToken(char operationSymbol, char operationInverseSymbol) {
        this.operationSymbol = operationSymbol;
        this.operationInverseSymbol = operationInverseSymbol;
    }

    private boolean canParse(String expression) {
        char[] expressionChars = expression.toCharArray();
        int level = 0;
        for (char c : expressionChars) {
            if (c == '(' || c == '[')
                level++;
            if (c == ')' || c == ']')
                level--;
            if (level < 0)
                throw new BracketsError(expression);
            if (c == operationSymbol && level == 0)
                return true;
            if (c == operationInverseSymbol && level == 0)
                return true;
        }
        return false;
    }

    @Override
    public T parse(String expression, NumberParser<T> parser) {
        if (!canParse(expression))
            return null;
        char[] expressionChars = expression.toCharArray();
        StringBuffer buffer = new StringBuffer();
        T temp = null;
        int level = 0;
        boolean mode = false;//true - inverse
        for (char c : expressionChars) {
            if (c == '(')
                level++;
            if (c == ')')
                level--;
            if (level < 0)
                throw new BracketsError();
            if (c == operationSymbol && level == 0) {
                String toParse = buffer.toString();
                if (!toParse.isEmpty())
                    if (temp == null)
                        temp = parser.parse(toParse);
                    else
                        temp = operation(temp, parser.parse(toParse), mode);
                buffer = new StringBuffer();
                mode = false;
            } else if (c == operationInverseSymbol && level == 0) {
                String toParse = buffer.toString();
                if (!toParse.isEmpty()) {
                    if (temp == null)
                        temp = neutral();
                    temp = operation(temp, parser.parse(toParse), mode);
                }
                buffer = new StringBuffer();
                mode = true;
            } else
                buffer.append(c);
        }
        if (temp == null)
            temp = neutral();
        temp = operation(temp, parser.parse(buffer.toString()), mode);
        return temp;
    }

    protected abstract T neutral();

    protected abstract T operation(T c1, T c2, boolean mode);
}
