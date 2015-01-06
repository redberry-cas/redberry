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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserDerivative implements TokenParser {
    public static final ParserDerivative INSTANCE = new ParserDerivative();

    private ParserDerivative() {
    }

    @Override
    public ParseToken parseToken(String expression, Parser parser) {
        if (!expression.contains("][")
                || !expression.substring(0, 2).equals("D[")
                || expression.charAt(expression.length() - 1) != ']') return null;
        String[] parts = expression.split("\\]\\[");
        if (parts.length != 2)
            return null;
        String argStr = parts[1].substring(0, parts[1].length() - 1);
        if (!ParseUtils.checkBracketsConsistence(argStr)) return null;

        ParseToken arg = parser.parse(argStr);
        if (arg == null) return null;

        final char[] chars = parts[0].substring(2).toCharArray();
        int[] levels = new int[2];
        StringBuilder buffer = new StringBuilder();
        List<ParseToken> tokens = new ArrayList<>();
        tokens.add(arg);
        char c;
        for (int i = 0; i < chars.length; ++i) {
            if (levels[0] < 0 || levels[1] < 0) return null;
            c = chars[i];
            if (c == '(') ++levels[0];
            else if (c == '[') ++levels[1];
            else if (c == ')') --levels[0];
            else if (c == ']') --levels[1];
            else if (c == ',' && levels[0] == 0 && levels[1] == 0) {
                tokens.add(parser.parse(buffer.toString()));
                buffer = new StringBuilder();
            } else buffer.append(c);
        }
        if (levels[0] != 0 || levels[1] != 0) return null;

        tokens.add(parser.parse(buffer.toString()));
        return new ParseTokenDerivative(TokenType.Derivative, tokens.toArray(new ParseToken[tokens.size()]));
    }

    @Override
    public int priority() {
        return 8000;
    }
}
