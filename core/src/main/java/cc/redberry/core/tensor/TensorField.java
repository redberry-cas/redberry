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
package cc.redberry.core.tensor;

import cc.redberry.core.context.NameDescriptorForTensorField;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.SimpleIndicesBuilder;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Representation of tensor field.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class TensorField extends SimpleTensor {

    protected Tensor[] args;
    protected SimpleIndices[] argIndices;

    TensorField(int name, SimpleIndices indices, Tensor[] args, SimpleIndices[] argIndices) {
        super(name, indices);
        this.args = args;
        this.argIndices = argIndices;
    }

    TensorField(TensorField field, Tensor[] args) {
        super(field.name, field.indices);
        this.args = args;
        this.argIndices = field.argIndices;
    }

    public Tensor[] getArguments() {
        return args.clone();
    }

    public SimpleIndices[] getArgIndices() {
        return argIndices.clone();
    }

    public SimpleIndices getArgIndices(int i) {
        return argIndices[i];
    }

    public boolean isDerivative() {
        return ((NameDescriptorForTensorField) getNameDescriptor()).isDerivative();
    }

    @Override
    public Tensor get(int i) {
        return args[i];
    }

    @Override
    public int size() {
        return args.length;
    }

    @Override
    public Iterator<Tensor> iterator() {
        return new BasicTensorIterator(this);
    }

    @Override
    public Tensor[] getRange(int from, int to) {
        return Arrays.copyOfRange(args, from, to);
    }

    @Override
    public NameDescriptorForTensorField getNameDescriptor() {
        return (NameDescriptorForTensorField) super.getNameDescriptor();
    }

    /**
     * Calculates and returns partition of derivative indices. The resulting array organized as follows:
     * {@code result[0][0]} --- contains pure field indices ({@code result[0].length == 1}), {@code result[i][j]} ---
     * are indices formed by j-th derivative with respect to i-th field argument
     *
     * @return partition of derivative indices
     */
    public SimpleIndices[][] calcIndicesPartition() {

        NameDescriptorForTensorField fieldDescriptor = getNameDescriptor();
        if (!fieldDescriptor.isDerivative()) {
            SimpleIndices[][] ret = new SimpleIndices[args.length + 1][];
            Arrays.fill(ret, 1, ret.length, new SimpleIndices[0]);
            ret[0] = new SimpleIndices[]{indices};
            return ret;
        }

        int[] orders = fieldDescriptor.getDerivativeOrders();

        int[][] _mapping = fieldDescriptor.getIndicesPartitionMapping();

        SimpleIndices[][] iPartition = new SimpleIndices[args.length + 1][];

        SimpleIndicesBuilder ib;
        int totalOrder = 0;
        int i, j, k, l, m, _map[];
        for (i = 0; i <= args.length; ++i) {
            l = i == 0 ? 1 : orders[i - 1];
            iPartition[i] = new SimpleIndices[l];
            for (j = 0; j < l; ++j) {
                _map = _mapping[totalOrder++];
                ib = new SimpleIndicesBuilder(_map.length);
                for (k = 0, m = _map.length; k < m; ++k)
                    ib.append(indices.get(_map[k]));
                iPartition[i][j] = ib.getIndices();
            }
        }
        return iPartition;
    }

    @Override
    public String toString(OutputFormat mode) {
        //TODO add argIndices toString(REDBERRY)

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (Tensor t : args) {
            sb.append(t.toString(mode));
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(']');
        return super.toString(mode) + sb.toString();
    }

    @Override
    public TensorBuilder getBuilder() {
        return new Builder(this);
    }

    @Override
    public TensorFactory getFactory() {
        return new Factory(this);
    }

    private static final class Builder implements TensorBuilder {

        private final TensorField field;
        private int pointer = 0;
        private final Tensor[] data;

        public Builder(TensorField field) {
            this.field = field;
            this.data = new Tensor[field.size()];
        }

        Builder(TensorField field, Tensor[] data, int pointer) {
            this.field = field;
            this.data = data;
        }

        @Override
        public Tensor build() {
            if (pointer != data.length)
                throw new IllegalStateException("Tensor field not fully constructed.");
            return new TensorField(field, data);
        }

        @Override
        public void put(Tensor tensor) {
            if (pointer == data.length)
                throw new IllegalStateException("No more arguments in field.");
            if (tensor == null)
                throw new NullPointerException();
            if (!tensor.getIndices().getFree().equalsRegardlessOrder(field.getArgIndices(pointer)))
                throw new IllegalArgumentException("Free indices of puted tensor differs from field argument binding indices!");
            data[pointer++] = tensor;
        }

        @Override
        public TensorBuilder clone() {
            return new Builder(field, data.clone(), pointer);
        }
    }

    private static final class Factory implements TensorFactory {

        private final TensorField field;

        public Factory(TensorField field) {
            this.field = field;
        }

        @Override
        public Tensor create(Tensor... tensors) {
            if (tensors.length != field.size())
                throw new IllegalArgumentException("Wrong arguments count.");
            for (int i = tensors.length - 1; i >= 0; --i) {
                if (tensors[i] == null)
                    throw new NullPointerException();
                if (!tensors[i].getIndices().getFree().equalsRegardlessOrder(field.getArgIndices(i)))
                    throw new IllegalArgumentException("Free indices of puted tensor differs from field argument binding indices!");
            }
            return new TensorField(field, tensors);
        }
    }
}
