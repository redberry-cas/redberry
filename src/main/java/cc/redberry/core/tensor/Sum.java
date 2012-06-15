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

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.Indices;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Sum extends MultiTensor {

    private final int hash;

    Sum(Tensor[] data, Indices indices) {
        super(data, indices);
        Arrays.sort(data);//TODO use non-stable sort
        this.hash = Arrays.hashCode(data);
    }

//    @Override
//    protected Indices calculateIndices() {
//        Indices indices = data[0].getIndices().getFreeIndices();
//
////        int p = 0;
////        boolean sorted = indices instanceof SortedIndices;
////
////        Indices current;
////        for (int i = 1; i < data.length; ++i) {
////            current = data[i].getIndices().getFreeIndices();
////            if (!current.equalsRegardlessOrder(indices))
////                throw new TensorException("Inconsistent summands: " + data[p] + " and " + data[i] + " have differrent free indices.");
////            if (!sorted && current instanceof SortedIndices) {
////                indices = current;
////                p = i;
////                sorted = true;
////            }
////        }
//        return IndicesFactory.createSorted(indices);
//    }
    @Override
    public int hash() {
        return hash;
    }

    @Override
    protected char operationSymbol() {
        return '+';
    }

    @Override
    public TensorBuilder getBuilder() {
        return new SumBuilder(data.length);
    }

    @Override
    protected String toString(ToStringMode mode, Class<? extends Tensor> clazz) {
        if (clazz == Product.class || clazz == Power.class)
            return "(" + toString(mode) + ")";
        return toString(mode);
    }
}
