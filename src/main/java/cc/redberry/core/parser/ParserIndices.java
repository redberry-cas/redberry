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
 * the Free Software Foundation, either version 2 of the License, or
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

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.IntArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ParserIndices {
    public static SimpleIndices parseSimple(String expression) {
        return IndicesFactory.createSimple(null, parse(expression));
    }

    public static int[] parse(String expression) {
        if (expression.isEmpty())
            return new int[0];

        IntArrayList indices = new IntArrayList();
        int level = 0;
        int state = 0;
        final char[] expressionChars = expression.toCharArray();
        char c;
        int beginIndex = 0, endIndex = 0;
        for (; endIndex < expressionChars.length; ++endIndex) {
            c = expressionChars[endIndex];
            if (c == '{')
                ++level;
            else if (c == '}')
                --level;
            else if (c == '_' && level == 0) {
                if (endIndex != 0)
                    parse(expression.substring(beginIndex + 1, endIndex), indices, state);
                state = 0;
                beginIndex = endIndex;
            } else if (c == '^') {
                if (level != 0)
                    throw new BracketsError();
                if (endIndex != 0)
                    parse(expression.substring(beginIndex + 1, endIndex), indices, state);
                state = 0x80000000;
                beginIndex = endIndex;
            }
        }
        if (level != 0)
            throw new BracketsError();
        if (beginIndex != endIndex)
            parse(expression.substring(beginIndex + 1, endIndex), indices, state);
        return indices.toArray();
    }

    public static final Pattern pattern = Pattern.compile("((?>(?>[a-zA-Z])|(?>\\\\[a-zA-A]*))(?>_(?>(?>[0-9])|(?>[\\{][0-9\\s]*[\\}])))?[']*)");

    static void parse(String expression, IntArrayList indices, int state) {
        Matcher matcher = pattern.matcher(expression);
        String singleIndex;
        while (matcher.find()) {
            singleIndex = matcher.group();
            indices.add(CC.getIndexConverterManager().getCode(singleIndex) | state);
        }
        String remainder = matcher.replaceAll("");
        remainder = remainder.replaceAll("[\\{\\}\\s]*", "");
        if (remainder.length() != 0)
            throw new ParserException();
    }
}
