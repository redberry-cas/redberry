/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
import cc.redberry.core.indices.SimpleIndices;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Stanislav Poslavsky
 */
public final class TensorField extends Tensor {
    /**
     * Resulting indices of field
     */
    final SimpleIndices indices;
    /**
     * Head (e.g. for f[x] -- "f")
     */
    final SimpleTensor head;
    /**
     * Arguments
     */
    final Tensor[] args;
    /**
     * Indices of arguments
     */
    final SimpleIndices[] argIndices;

    TensorField(SimpleIndices indices, SimpleTensor head, Tensor[] args, SimpleIndices[] argIndices) {
        this.indices = indices;
        this.head = head;
        this.args = args;
        this.argIndices = argIndices;
    }

    /**
     * Returns head of this function
     *
     * @return head of this function
     */
    public SimpleTensor getHead() {
        return head;
    }

    /**
     * Return arguments array
     *
     * @return arguments array
     */
    public Tensor[] getArguments() {
        return args.clone();
    }

    /**
     * Returns array of arguments indices
     *
     * @return array of arguments indices
     */
    public SimpleIndices[] getArgIndices() {
        return argIndices.clone();
    }

    /**
     * Returns indices of i-th argument
     *
     * @param i position
     * @return indices of i-th argument
     */
    public SimpleIndices getArgIndices(int i) {
        return argIndices[i];
    }

    @Override
    public SimpleIndices getIndices() {return indices;}

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
    protected int hash() {return head.hash() + 17 * Arrays.hashCode(args);}

    @Override
    public String toString(OutputFormat mode) {
        //TODO add argIndices toString(REDBERRY)

        StringBuilder sb = new StringBuilder();
        sb.append(head.toString0(mode));

        if (mode.is(OutputFormat.Maple) || mode.is(OutputFormat.C))
            sb.append('(');
        else sb.append('[');

        for (Tensor t : args) {
            sb.append(t.toString(mode));
            sb.append(',');
        }
        sb.deleteCharAt(sb.length() - 1);

        if (mode.is(OutputFormat.Maple) || mode.is(OutputFormat.C))
            sb.append(')');
        else sb.append(']');

        return sb.toString();
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
        private final Tensor[] data;
        private int pointer = 0;

        Builder(TensorField field) {
            this.field = field;
            this.data = new Tensor[field.size()];
        }

        Builder(TensorField field, Tensor[] data, int pointer) {
            this.field = field;
            this.data = data;
            this.pointer = pointer;
        }

        @Override
        public Tensor build() {
            if (pointer != data.length)
                throw new IllegalStateException("Tensor field not fully constructed.");
            else {
                if (!changed)
                    return field;
                //recompute indices
                SimpleIndices newIndices = field.head.getVarDescriptor().computeIndices(field.indices, data);
                if (!newIndices.getFree().equalsRegardlessOrder(field.indices.getFree()))
                    throw new IllegalArgumentException("Indices of function changed by the builder");
                return new TensorField(newIndices, field.head, data, field.argIndices);
            }
        }

        private boolean changed = false;

        @Override
        public void put(Tensor tensor) {
            if (pointer == data.length)
                throw new IllegalStateException("No more arguments in field.");
            if (tensor == null)
                throw new NullPointerException();
            if (data[pointer] != tensor)
                changed = true;
            data[pointer++] = tensor;
        }

        @Override
        public TensorBuilder clone() {
            return new Builder(field, data.clone(), pointer);
        }
    }

    private static final class Factory implements TensorFactory {
        private final TensorField field;

        Factory(TensorField field) {
            this.field = field;
        }

        @Override
        public Tensor create(Tensor... data) {
            if (data.length != field.size())
                throw new IllegalArgumentException("Wrong arguments count.");
            boolean changed = false;
            for (int i = data.length - 1; i >= 0; --i) {
                if (data[i] != field.args[i]) {
                    changed = true;
                    break;
                }
            }
            if (!changed)
                return field;
            else {
                SimpleIndices newIndices = field.head.getVarDescriptor().computeIndices(field.indices, data);
                if (!newIndices.getFree().equalsRegardlessOrder(field.indices.getFree()))
                    throw new IllegalArgumentException("Indices of function changed by the builder");
                return new TensorField(newIndices, field.head, data, field.argIndices);
            }
        }
    }
}
