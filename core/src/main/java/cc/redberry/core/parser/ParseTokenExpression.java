/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.context.Context;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseTokenExpression extends ParseToken {

    public ParseTokenExpression(boolean preprocessing, ParseToken lhs, ParseToken rhs) {
        super(preprocessing ? TokenType.PreprocessingExpression : TokenType.Expression, lhs, rhs);
    }

    @Override
    public Indices getIndices() {
        return content[0].getIndices().getFree();
    }

    @Override
    public Tensor toTensor() {
        Expression expression = Tensors.expression(content[0].toTensor(), content[1].toTensor());
        if (tokenType == TokenType.PreprocessingExpression)
            Context.get().getParseManager().defaultTensorPreprocessors.add(expression);
        return expression;
    }
}
