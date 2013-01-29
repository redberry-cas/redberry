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
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.parser.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ChangeIndicesTypesAndTensorNames implements ParseTokenTransformer {
    private final TypesAndNamesTransformer transformer;

    public ChangeIndicesTypesAndTensorNames(TypesAndNamesTransformer transformer) {
        this.transformer = transformer;
    }

    @Override
    public ParseToken transform(ParseToken node) {

        TokenType type = node.tokenType;
        switch (type) {
            case SimpleTensor:
                ParseTokenSimpleTensor st = (ParseTokenSimpleTensor) node;
                return new ParseTokenSimpleTensor(transformIndices(st.getIndices()), transformer.newName(st.name));
            case TensorField:
                ParseTokenTensorField tf = (ParseTokenTensorField) node;
                SimpleIndices[] newArgsIndices = new SimpleIndices[tf.argumentsIndices.length];
                for (int i = newArgsIndices.length - 1; i >= 0; --i)
                    newArgsIndices[i] = transformIndices(tf.argumentsIndices[i]);

                return new ParseTokenTensorField(transformIndices(tf.getIndices()),
                        transformer.newName(tf.name), transformContent(tf.content), newArgsIndices);
            case Number:
                return node;
            case ScalarFunction:
                return new ParseTokenScalarFunction(((ParseTokenScalarFunction) node).function, transformContent(node.content));
            default:
                return new ParseToken(node.tokenType, transformContent(node.content));
        }
    }

    private ParseToken[] transformContent(ParseToken[] content) {
        ParseToken[] newContent = new ParseToken[content.length];
        for (int i = content.length - 1; i >= 0; --i)
            newContent[i] = transform(content[i]);
        return newContent;
    }

    private SimpleIndices transformIndices(SimpleIndices old) {
        int[] newIndices = new int[old.size()];
        for (int i = old.size() - 1; i >= 0; --i)
            newIndices[i] = IndicesUtils.setType(transformer.newType(IndicesUtils.getTypeEnum(old.get(i))), old.get(i));
        return IndicesFactory.createSimple(null, newIndices);
    }
}
