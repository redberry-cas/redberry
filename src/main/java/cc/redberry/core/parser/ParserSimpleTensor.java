/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

import cc.redberry.core.indices.SimpleIndices;

/**
 * Parser for simple tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParserSimpleTensor implements TokenParser {
    /**
     * Singleton instance.
     */
    public static final ParserSimpleTensor INSTANCE = new ParserSimpleTensor();

    private ParserSimpleTensor() {
    }

    @Override
    public ParseTokenSimpleTensor parseToken(String expression, Parser parser) {
        expression = expression.replaceAll("\\{[\\s]*\\}", "");
        int indicesBegin = expression.indexOf('_'), i = expression.indexOf('^');
        if (indicesBegin < 0 && i >= 0)
            indicesBegin = i;
        if (indicesBegin >= 0 && i >= 0)
            indicesBegin = Math.min(indicesBegin, i);
        if (indicesBegin < 0)
            indicesBegin = expression.length();

        String name = expression.substring(0, indicesBegin);
        if (name.isEmpty())
            throw new ParserException("Simple tensor with empty name.");

        SimpleIndices indices = ParserIndices.parseSimple(expression.substring(indicesBegin));
        return new ParseTokenSimpleTensor(indices, name);
    }

    @Override
    public int priority() {
        return 0;
    }
}
