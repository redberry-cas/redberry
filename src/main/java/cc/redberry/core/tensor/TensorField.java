/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */
package cc.redberry.core.tensor;

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.SimpleIndices;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TensorField extends SimpleTensor {

    private Tensor[] args;
    private SimpleIndices[] argIndices;

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

    public SimpleIndices[] getArgIndices() {
        return argIndices.clone();
    }

    public SimpleIndices getArgIndices(int i) {
        return argIndices[i];
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
    public String toString(ToStringMode mode) {
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

    private static class Builder implements TensorBuilder {

        private final TensorField field;
        private int pointer = 0;
        private final Tensor[] data;

        public Builder(TensorField field) {
            this.field = field;
            this.data = new Tensor[field.size()];
        }

        @Override
        public Tensor buid() {
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
            if (!tensor.getIndices().getFreeIndices().equalsRegardlessOrder(field.getArgIndices(pointer)))
                throw new IllegalArgumentException("Free indices of puted tensor differs from field argument binding indices!");
            data[pointer++] = tensor;
        }
    }
}
