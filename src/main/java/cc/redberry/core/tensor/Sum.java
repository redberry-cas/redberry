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
import cc.redberry.core.indices.SortedIndices;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Sum extends MultiTensor {

    public Sum(Tensor... data) {
        super(data);
        Arrays.sort(data);//TODO use non-stable sort
    }

    @Override
    protected Indices calculateIndices() {

        int p = 0;
        Indices indices = data[0].getIndices().getFreeIndices();
        boolean sorted = indices instanceof SortedIndices;

        Indices current;
        for (int i = 1; i < data.length; ++i) {
            current = data[i].getIndices().getFreeIndices();
            if (!current.equalsIgnoreOrder(indices))
                throw new TensorException("Inconsistent summands: " + data[p] + " and " + data[i] + " have differrent free indices.");
            if (!sorted && current instanceof SortedIndices) {
                indices = current;
                p = i;
                sorted = true;
            }
        }
        return IndicesFactory.createSorted(indices);
    }

    @Override
    protected char operationSymbol() {
        return '+';
    }

    @Override
    protected int calculateHash() {
        return Arrays.hashCode(data);
    }

    @Override
    public TensorBuilder getBuilder() {
        return new SumBuilder(data.length);
    }
    
    
}
