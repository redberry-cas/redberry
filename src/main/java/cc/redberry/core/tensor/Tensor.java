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

import cc.redberry.core.context.Context;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;

import java.util.Iterator;

/**
 * <p>Abstract class which defines common tensor methods and properties.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.indices.Indices
 * @see cc.redberry.core.context.OutputFormat
 * @see cc.redberry.core.context.Context
 */
public abstract class Tensor
        implements Comparable<Tensor>,
                   Iterable<Tensor> {

    /**
     * <p>This method was added to the Tensor interface to obligate all tensors
     * to implement custom hash codes. This hash code should reflects only
     * tensor structure whatever the particular indices are.</p> <p>There are
     * other ways to calculate hash code for Tensor object (see
     * {@link cc.redberry.core.utils.TensorHashCalculator}).</p>
     *
     * @return hash code of this tensor
     */
    protected abstract int hash();

    /**
     * <p>Returns indices of this tensor. For more information see {@link cc.redberry.core.indices.Indices}.</p>
     *
     * @return indices of this tensor
     */
    public abstract Indices getIndices();

    /**
     * <p>Returns iterator over sub-tensors of current tensor (eg. summnads of
     * sum, multipliers of product, etc..).</p> <p>For iteration through whole
     * tensor tree use: {@link cc.redberry.core.tensor.iterator.TreeTraverseIterator}
     * or {@link cc.redberry.core.tensor.iterator.FromParentToChildIterator} and
     * {@link cc.redberry.core.tensor.iterator.FromChildToParentIterator}.</p>
     *
     * @return iterator over sub-tensors
     */
    @Override
    public Iterator<Tensor> iterator() {
        return new BasicTensorIterator(this);
    }

    /**
     * <p>Returns i-th sub-tensor of this tensor (eg. summand of sum, argument
     * of tensor field, etc...)</p>
     *
     * @param i index of sub-tensor
     *
     * @return i-th subtensor
     */
    public abstract Tensor get(int i);

    /**
     * <p>Returns count of sub-tensors of this tensor.</p>
     *
     * @return count of sub-tensors
     */
    public abstract int size();

    /**
     * <p>Returns new tensor instance with i-th sub-tensor replaced by provided
     * tensor. <p>Better tools for tensors manipulations are tree iterators.
     * See:
     * {@link cc.redberry.core.tensor.iterator.FromParentToChildIterator},
     * {@link cc.redberry.core.tensor.iterator.FromChildToParentIterator} and
     * {@link cc.redberry.core.tensor.iterator.TreeTraverseIterator}.</p> </p>
     *
     * @param i      index of sub-tensor to be replaced
     * @param tensor tensor to replace i-th sub-tensor
     *
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
     * <p>Retrieves several sub-tensors from current tensor. This function is
     * faster than sequential invocations of {@link #get(int)} method.</p>
     *
     * @param from index of first sub-tensor to be retrieved (inclusive)
     * @param to   next index after last sub-tensor to be retrieved (exclusive)
     *            
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
     * <p>Returns a string representation of a tensor. See {@link cc.redberry.core.context.OutputFormat}
     * for available modes.</p>
     *
     * @param mode printing mode (see. {@link cc.redberry.core.context.OutputFormat})
     *
     * @return a string representation of a tensor
     */
    public abstract String toString(final OutputFormat mode);

    /**
     * <p>Returns string representation of a tensor in default (see {@link cc.redberry.core.context.CC#getDefaultOutputFormat()})
     * mode. <p>Equivalent to:
     * <code>this.toString(CC.getDefaultOutputFormat())</code> </p></p>
     *
     * @return string representation of a tensor in default mode
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
     * <p>Compares tensors by their hash code.</p>
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
