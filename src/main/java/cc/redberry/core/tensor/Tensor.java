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

import cc.redberry.core.context.Context;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;

import java.util.Iterator;

/**
 * Abstract class which defines common tensor properties and methods. All tensors are immutable.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.indices.Indices
 * @see cc.redberry.core.context.OutputFormat
 * @see cc.redberry.core.context.Context
 * @since 1.0
 */
public abstract class Tensor
        implements Comparable<Tensor>,
        Iterable<Tensor> {

    /**
     * Hash code of this tensor.
     *
     * @return hash code of this tensor
     */
    protected abstract int hash();

    /**
     * Returns indices of this tensor.
     *
     * @return indices of this tensor
     */
    public abstract Indices getIndices();

    /**
     * Return read-only iterator over tensor elements.
     *
     * @return read-only iterator over tensor elements
     */
    @Override
    public Iterator<Tensor> iterator() {
        return new BasicTensorIterator(this);
    }

    /**
     * Returns element at i-th position.
     *
     * @param i position
     * @return element at i-th position
     * @throws IndexOutOfBoundsException if {@code i < 0} or {@code i >= size()}
     */
    public abstract Tensor get(int i);

    /**
     * Returns the number of elements in this tensor.
     *
     * @return the number of elements in this tensor
     */
    public abstract int size();

    /**
     * Returns new tensor instance with i-th sub-tensor replaced by provided
     * tensor.
     *
     * @param i      index of sub-tensor to be replaced
     * @param tensor tensor to replace i-th sub-tensor
     * @return new instance of tensor
     */
    public Tensor set(int i, Tensor tensor) {
        int size = size();
        if (i >= size || i < 0)
            throw new IndexOutOfBoundsException();
        if (tensor == null)
            throw new NullPointerException();
        TensorBuilder builder = getBuilder();
        for (int j = 0; j < size; ++j)
            if (j == i)
                builder.put(tensor);
            else
                builder.put(get(j));
        return builder.build();
    }

    /**
     * Retrieves several sub-tensors from current tensor. This function is
     * faster than sequential invocations of {@link #get(int)} method.
     *
     * @param from index of first sub-tensor to be retrieved (inclusive)
     * @param to   next index after last sub-tensor to be retrieved (exclusive)
     * @return array with retrieved tensors
     */
    public Tensor[] getRange(int from, final int to) {
        int size = size();
        if (from < 0 || from > to || to >= size)
            throw new IndexOutOfBoundsException();
        Tensor[] range = new Tensor[from - to];
        for (size = 0; from < to; ++size, ++from)
            range[size] = get(from);
        return range;
    }

    public Tensor[] toArray() {
        return getRange(0, size());
    }

    /**
     * Returns a string representation of a tensor according to the specified
     * {@link cc.redberry.core.context.OutputFormat}.
     *
     * @param outputFormat output format
     * @return a string representation of a tensor
     */
    public abstract String toString(OutputFormat outputFormat);

    /**
     * Returns a string representation of a tensor according to the default
     * {@link cc.redberry.core.context.OutputFormat} defined in
     * {@link cc.redberry.core.context.CC#getDefaultOutputFormat()}.
     *
     * @return a string representation of a tensor
     */
    @Override
    public final String toString() {
        return toString(Context.get().getDefaultOutputFormat());
    }

    /**
     * For internal use.
     */
    protected String toString(OutputFormat mode, Class<? extends Tensor> clazz) {
        return toString(mode);
    }

    /**
     * Compares tensors by their hash codes.
     *
     * @param t tensor
     * @return 0 if hashes are equals, 1 if hash code of this greater the hash code of specified
     * tensor and -1 otherwise
     */
    @Override
    public final int compareTo(Tensor t) {
        return Integer.compare(hash(), t.hash());
    }

    @Override
    public final int hashCode() {
        return hash();
    }

    /**
     * Creates a builder for this tensor. See {@link TensorBuilder} for more
     * information.
     *
     * @return builder for this tensor
     */
    public abstract TensorBuilder getBuilder();

    /**
     * Returns a factory for this tensor. See {@link TensorFactory} for more
     * information.
     *
     * @return builder for this tensor
     */
    public abstract TensorFactory getFactory();
}
