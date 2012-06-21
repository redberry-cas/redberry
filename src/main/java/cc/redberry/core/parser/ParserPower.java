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
public class ParserPower implements NodeParser {

    public static final ParserPower INSTANCE = new ParserPower();
    private static final String power = "Power";
    private static final int minLength = power.length() + 2;

    private ParserPower() {
    }

    @Override
    public int priority() {
        return 9986;
    }

    @Override
    public ParseNode parseNode(String expression, Parser parser) {
        if (expression.length() <= minLength)
            return null;
        if (!(power + '[').equals(expression.substring(0, power.length() + 1))
                || expression.charAt(expression.length() - 1) != ']')
            return null;
        int level = 0;
        for (char c : expression.toCharArray()) {
            if (c == '[')
                ++level;
            if (level < 1)
                return null;
            if (c == ']')
                --level;
        }
        String[] parts = expression.substring(power.length() + 1, expression.length() - 1).split(",");
        if (parts.length == 1)
            return null;
        ParseNode arg = parser.parse(parts[0]);
        ParseNode power = parser.parse(parts[1]);
        return new ParseNode(TensorType.Power, new ParseNode[]{arg, power});
    }
}
