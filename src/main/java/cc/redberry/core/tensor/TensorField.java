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
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorField extends SimpleTensor {

    private Tensor[] args;
    private SimpleIndices[] argIndices;

    public TensorField(int name, SimpleIndices indices, Tensor[] args) {
        super(name, indices);
        this.args = args;
        argIndices = new SimpleIndices[args.length];
        int i = 0;
        for (Tensor t : args)
            argIndices[i++] = IndicesFactory.createSimple(null, t.getIndices().getFreeIndices());
    }

    public TensorField(int name, SimpleIndices indices, Tensor[] args, SimpleIndices[] argIndices) {
        super(name, indices);
        this.args = args;
        this.argIndices = argIndices;
    }

    public SimpleIndices[] getArgIndices() {
        return argIndices;
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
}
