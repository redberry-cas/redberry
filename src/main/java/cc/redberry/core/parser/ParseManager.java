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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ParseManager {

    private final Parser parser = Parser.DEFAULT;
    private final List<Transformation> tensorPreprocessors = new ArrayList<>();
    private final List<ParseNodeTransformer> nodesPreprocessors = new ArrayList<>();

    public List<ParseNodeTransformer> getNodesPreprocessors() {
        return nodesPreprocessors;
    }

    public List<Transformation> getTensorPreprocessors() {
        return tensorPreprocessors;
    }

    public Tensor parse(String expression) {
        ParseNode node = parser.parse(expression);
        for (ParseNodeTransformer tr : nodesPreprocessors)
            node = tr.transform(node);
        Tensor t = node.toTensor();
        for (Transformation tr : tensorPreprocessors)
            t = tr.transform(t);
        return t;
    }
}
