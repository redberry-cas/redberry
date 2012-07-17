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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpressionBuilder implements TensorBuilder {

    private Tensor left, right;
    private Indices indices;

    public ExpressionBuilder() {
    }

    @Override
    public Expression build() {
        return new Expression(indices, left, right);
    }

    @Override
    public void put(Tensor tensor) {
        if (tensor == null)
            throw new NullPointerException();
        else if (left == null) {
            left = tensor;
            indices = IndicesFactory.createSorted(left.getIndices().getFreeIndices());
        } else if (right == null) {
            right = tensor;
            if (!indices.equalsRegardlessOrder(right.getIndices().getFreeIndices()))
                throw new TensorException("Inconsistent indices in expression.", tensor);
        } else
            throw new TensorException("Expression have only two parts.");
    }
}
