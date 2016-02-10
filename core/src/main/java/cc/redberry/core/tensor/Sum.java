/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.HashingStrategy;
import cc.redberry.core.utils.TensorUtils;

import java.util.Arrays;

/**
 * Representation of sum.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class Sum extends MultiTensor {

    final Tensor[] data;
    final int hash;

    Sum(final Tensor[] data, Indices indices) {
        super(indices);

        assert data.length > 1;

        this.data = data;
        TensorWrapper[] wrappers = new TensorWrapper[data.length];
        int i;
        for (i = 0; i < data.length; ++i)
            wrappers[i] = new TensorWrapper(data[i]);
        ArraysUtils.quickSort(wrappers, data);
        this.hash = hash0(data, indices);
    }

    Sum(Indices indices, Tensor[] data, int hash) {
        super(indices);
        assert data.length > 1;
        this.data = data;
        this.hash = hash;
    }

    private static int hash0(final Tensor[] data, final Indices indices) {
        if (indices.size() > 1)
            return Arrays.hashCode(data);

        //try to check whether sum is a simple polynomial
        boolean isSimplePoly = true;
        //hash codes of each term
        int[] termsHashes = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            termsHashes[i] = data[i].hashCode();
            if (!isSymbolicPoly(data[i]))
                isSimplePoly = false;
        }

        if (!isSimplePoly)
            return Arrays.hashCode(termsHashes);

        int result = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] instanceof Product)
                termsHashes[i] *= ((Product) data[i]).factor.hashWithSign();
            else if (data[i] instanceof Complex)
                termsHashes[i] *= ((Complex) data[i]).hashWithSign();
            result = 31 * result + termsHashes[i];
        }
        return result * result;
    }

    private static boolean isSymbolicPoly(final Tensor t) {
        //indices are already checked
        if (t instanceof Complex)
            return true;
        else if (t instanceof SimpleTensor)//todo if TensorField (when symmetries)?
            return t.getIndices().size() <= 1;
        else if (t instanceof Product || t instanceof Power) {
            for (Tensor m : t)
                if (!isSymbolicPoly(m))
                    return false;
            return true;
        }

        return false;
    }

    private static final class TensorWrapper implements Comparable<TensorWrapper> {
        final Tensor tensor;
        final int hashWithIndices;

        public TensorWrapper(Tensor tensor) {
            this.tensor = tensor;
            hashWithIndices = HashingStrategy.hashWithIndices(tensor);
        }

        @Override
        public int compareTo(TensorWrapper o) {
            int i = tensor.compareTo(o.tensor);
            if (i != 0)
                return i;
            return Integer.compare(hashWithIndices, o.hashWithIndices);
        }
    }

    @Override
    public Tensor[] toArray() {
        return data.clone();
    }

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
    public Tensor set(int i, Tensor tensor) {
//        return super.set(i, tensor);
        if (i >= data.length || i < 0)
            throw new IndexOutOfBoundsException();

        Tensor old = data[i];
        if (old == tensor)
            return this;
        if (TensorUtils.equalsExactly(old, tensor))
            return this;
        if (TensorUtils.isIndeterminate(tensor))
            return tensor;
        if (TensorUtils.isZero(tensor))
            return remove(i);
        Tensor[] newData = data.clone();
        newData[i] = tensor;
        if (TensorUtils.equals(old, tensor))
            return new Sum(newData, indices);
        return Tensors.sum(newData);
    }

    @Override
    public Tensor remove(int i) {
        if (i >= data.length || i < 0)
            throw new IndexOutOfBoundsException();
        if (data.length == 2)
            return data[1 - i];
        Tensor[] newData = new Tensor[data.length - 1];
        System.arraycopy(data, 0, newData, 0, i);
        if (i < data.length - 1)
            System.arraycopy(data, i + 1, newData, i, data.length - i - 1);
        return new Sum(newData, indices);
    }

    @Override
    protected Tensor remove1(final int[] positions) {
        Tensor[] newData = new Tensor[data.length - positions.length];
        int pointer = 0, counter = -1;
        for (int i = 0; i < data.length; ++i)
            if (pointer < positions.length && i == positions[pointer])
                ++pointer;
            else newData[++counter] = data[i];
        if (newData.length == 1) return newData[0];
        return new Sum(newData, indices);
    }

    @Override
    protected Complex getNeutral() {
        return Complex.ZERO;
    }

    @Override
    protected Tensor select1(int[] positions) {
        Tensor[] newData = new Tensor[positions.length];
        int i = -1;
        for (int position : positions)
            newData[++i] = data[position];
        return new Sum(newData, indices);
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
    public String toString(OutputFormat mode) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (int i = 0; ; ++i) {
            temp = get(i).toString(mode, Sum.class);
            if ((temp.charAt(0) == '-' || temp.charAt(0) == '+') && sb.length() != 0)
                sb.deleteCharAt(sb.length() - 1);
            String str = get(i).toString(mode, Sum.class);
            if (str.contains("'") && !mode.printMatrixIndices)
                return toString(mode.printMatrixIndices());
            sb.append(str);
            if (i == size() - 1)
                return sb.toString();
            sb.append('+');
        }
    }

    @Override
    protected String toString(OutputFormat mode, Class<? extends Tensor> clazz) {
        if (clazz == Power.class || clazz == Product.class)
            return "(" + toString(mode) + ")";
        else
            return toString(mode);
    }
}
