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
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.TensorHashCalculator;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Sum extends MultiTensor {

    private final Tensor[] data;
    private final int hash;

    Sum(final Tensor[] data, Indices indices) {
        super(indices);

        assert data.length > 1;

        this.data = data;
        TensorWrapper[] wrappers = new TensorWrapper[data.length];
        int i;
        for (i = 0; i < data.length; ++i)
            wrappers[i] = new TensorWrapper(data[i]);
        ArraysUtils.quickSort(wrappers, data);
        this.hash = Arrays.hashCode(data);
    }

    private static final class TensorWrapper implements Comparable<TensorWrapper> {

        final Tensor tensor;
        final int hashWithIndices;

        public TensorWrapper(Tensor tensor) {
            this.tensor = tensor;
            hashWithIndices = TensorHashCalculator.hashWithIndices(tensor);
        }

        @Override
        public int compareTo(TensorWrapper o) {
            int i = tensor.compareTo(o.tensor);
            if (i != 0)
                return i;
            return Integer.compare(hashWithIndices, o.hashWithIndices);
        }
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
    public Tensor get(int i) {
        return data[i];
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public Tensor[] getRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

    @Override
    public int hash() {
        return hash;
    }

    @Override
    public TensorBuilder getBuilder() {
        return new SumBuilder(data.length);
    }

    @Override
    public TensorFactory getFactory() {
        return SumFactory.FACTORY;
    }

    @Override
    public String toString(ToStringMode mode) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (int i = 0;; ++i) {
            temp = get(i).toString(mode, Sum.class);
            if ((temp.charAt(0) == '-' || temp.charAt(0) == '+') && sb.length() != 0)
                sb.deleteCharAt(sb.length() - 1);
            sb.append(get(i).toString(mode, Sum.class));
            if (i == size() - 1)
                return sb.toString();
            sb.append('+');
        }
    }

    @Override
    protected String toString(ToStringMode mode, Class<? extends Tensor> clazz) {
        if (clazz == Power.class || clazz == Product.class)
            return "(" + toString(mode) + ")";
        else
            return toString(mode);
    }
}
