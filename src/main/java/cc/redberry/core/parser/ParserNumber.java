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

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.parser.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParserNumber implements NodeParser {
    public static final ParserNumber INSTANCE = new ParserNumber();

    private ParserNumber() {
    }

    @Override
    public ParseNode parseNode(String expression, cc.redberry.core.parser.Parser parser) {
        Complex value;
        try {
            value = (Complex) NumberParser.COMPLEX_PARSER.parse(expression);
        } catch (NumberFormatException e) {
            return null;
        }
        return new ParseNodeNumber(value);
    }

    @Override
    public int priority() {
        return 9999;
    }
}
