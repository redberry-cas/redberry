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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.transformations.DifferentiateTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.ExpandAndEliminateTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseTokenDerivative extends ParseToken {
    Indices indices;

    public ParseTokenDerivative(TokenType tokenType, ParseToken... content) {
        super(tokenType, content);
        IndicesBuilder ib = new IndicesBuilder();
        ib.append(content[0].getIndices().getFree());
        for (int i = content.length - 1; i >= 1; --i)
            ib.append(content[i].getIndices().getInverted().getFree());
        indices = ib.getIndices();
    }

    @Override
    public Indices getIndices() {
        return indices;
    }

    @Override
    public Tensor toTensor() {
        SimpleTensor[] vars = new SimpleTensor[content.length - 1];
        Tensor temp = content[0].toTensor();

        TIntHashSet allowedDummies = TensorUtils.getAllIndicesNamesT(temp);
        IndicesBuilder free = new IndicesBuilder().append(temp.getIndices());
        for (int i = 1; i < content.length; ++i) {
            temp = content[i].toTensor();
            free.append(temp.getIndices().getInverted());
            allowedDummies.addAll(IndicesUtils.getIndicesNames(temp.getIndices()));
            if (!(temp instanceof SimpleTensor) && !(temp instanceof TensorField))
                throw new IllegalArgumentException("Derivative with respect to non simple argument: " + temp);
            vars[i - 1] = (SimpleTensor) temp;
        }
        allowedDummies.removeAll(IndicesUtils.getIndicesNames(free.getIndices().getFree()));
        Tensor result = new DifferentiateTransformation(
                vars, new Transformation[]{ExpandAndEliminateTransformation.EXPAND_AND_ELIMINATE}
        ).transform(content[0].toTensor());
        result = ApplyIndexMapping.optimizeDummies(result);
        TIntHashSet generated = TensorUtils.getAllDummyIndicesT(result);
        generated.removeAll(allowedDummies);

        result = ApplyIndexMapping.renameDummy(result, generated.toArray(), allowedDummies.toArray());
        return result;
    }
}
