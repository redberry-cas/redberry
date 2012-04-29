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
package cc.redberry.core.context.defaults;

import java.util.ArrayList;
import java.util.List;
import cc.redberry.core.parser.ParserBrackets;
import cc.redberry.core.parser.ParserDerivative;
import cc.redberry.core.parser.ParserProduct;
import cc.redberry.core.parser.ParserSimpleTensor;
import cc.redberry.core.parser.ParserSum;
import cc.redberry.core.parser.ParserTensorField;
import cc.redberry.core.parser.ParserTensorNumber;
import cc.redberry.core.parser.TensorParser;

public class DefaultTensorParsers {
    private DefaultTensorParsers() {
    }
    private static final List<TensorParser> parsers = new ArrayList<>();

    static {
        parsers.add(ParserSimpleTensor.INSTANCE);
        parsers.add(ParserTensorField.INSTANCE);
        parsers.add(ParserBrackets.INSTANCE);
        parsers.add(ParserProduct.INSTANCE);
        parsers.add(ParserSum.INSTANCE);
        parsers.add(ParserTensorNumber.INSTANCE);
        parsers.add(ParserDerivative.INSTANCE);
        //parsers.add(SinParser.INSTANCE);
    }

    public static List<TensorParser> getDefaultParsers() {
        return parsers;
    }
}
