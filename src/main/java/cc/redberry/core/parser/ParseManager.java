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

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ParseManager {

    /**
     * Default AST transformers to be applied before {@link cc.redberry.core.parser.ParseToken#toTensor()} conversion.
     */
    public List<ParseTokenTransformer> defaultParserPreprocessors = new ArrayList<>();

    /**
     * Default transformations to be applied after {@link cc.redberry.core.parser.ParseToken#toTensor()} conversion.
     */
    public List<Transformation> defaultTensorPreprocessors = new ArrayList<>();

    private final Parser parser;

    /**
     * @param parser parser
     */
    public ParseManager(Parser parser) {
        this.parser = parser;
    }

    /**
     * @param expression          string expression
     * @param tensorPreprocessors transformation
     * @param nodesPreprocessors  AST transformers
     * @return tensor
     */
    public Tensor parse(String expression, Transformation[] tensorPreprocessors, ParseTokenTransformer[] nodesPreprocessors) {
        ParseToken node = parser.parse(expression);
        for (ParseTokenTransformer tr : nodesPreprocessors)
            node = tr.transform(node);
        Tensor t = node.toTensor();
        for (Transformation tr : tensorPreprocessors)
            t = tr.transform(t);
        return t;
    }

    /**
     * @param expression         string expression
     * @param nodesPreprocessors AST transformers
     * @return tensor
     */
    public Tensor parse(String expression, ParseTokenTransformer... nodesPreprocessors) {
        return parse(expression, new Transformation[0], nodesPreprocessors);
    }

    /**
     * @param expression string expression
     * @return tensor
     */
    public Tensor parse(String expression) {
        return parse(expression,
                defaultTensorPreprocessors.toArray(new Transformation[defaultTensorPreprocessors.size()]),
                defaultParserPreprocessors.toArray(new ParseTokenTransformer[defaultParserPreprocessors.size()]));
    }
}
