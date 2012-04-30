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



public class BracketParser implements ElementParser {
    public static final BracketParser INSTANCE = new BracketParser();

    private BracketParser() {
    }
    public boolean canParse(String expression) {
        if (expression.charAt(0) == '(' && expression.charAt(expression.length() - 1) == ')') {
            char[] expressionChars = expression.toCharArray();
            int level = 0;
            for (char c : expressionChars) {
                if (c == '(')
                    level++;
                if (level < 1)
                    return false;
                if (c == ')')
                    level--;
            }
            return true;
        } else
            return false;
    }

    public ComplexElement parse(String expression) {
        return Parser.parse(expression.substring(1, expression.length() - 1));
    }
}
