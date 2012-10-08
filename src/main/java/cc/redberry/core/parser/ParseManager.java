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

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ParseManager {

    private final Parser parser;

    public ParseManager(Parser parser) {
        this.parser = parser;
    }

    public Tensor parse(String expression) {
        return parser.parse(expression).toTensor();
    }

    public Tensor parse(String expression, Transformation[] tensorPreprocessors, ParseNodeTransformer[] nodesPreprocessors) {
        ParseNode node = parser.parse(expression);
        for (ParseNodeTransformer tr : nodesPreprocessors)
            node = tr.transform(node);
        Tensor t = node.toTensor();
        for (Transformation tr : tensorPreprocessors)
            t = tr.transform(t);
        return t;
    }

    public Tensor parse(String expression, ParseNodeTransformer... nodesPreprocessors) {
        return parse(expression, new Transformation[0], nodesPreprocessors);
    }
}
