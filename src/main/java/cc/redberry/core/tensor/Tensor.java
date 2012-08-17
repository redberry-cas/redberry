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

import cc.redberry.core.context.Context;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.Indices;

import java.util.Iterator;

//TODO rewrite or remove before 1.0

/**
 * <p>Abstract class which defines common tensor methods and properties.</p>
 * <p/>
 * <h4>Indices</h4>
 * <p>The most fundamental tensor attribute is its indices. Interface
 * {@link cc.redberry.core.indices.Indices} provides common indices methods and properties.
 * There are several <code>Indices</code> implementations, and different implementations used
 * in different inheritors of <code>Tensor</code>. Specified abstract method for getting
 * Indices: <strong><code>Indices getIndices(),</code></strong>
 * which returns indices of this
 * <code>Tensor</code>.
 * <p/>
 * <
 * p/> <h4><a name="Comparable">Comparable</a></h4>
 * {@link Comparable} implementation needs to sort arrays or collections of
 * tensors by their hash code. This functionality provides fast algorithms of
 * comparing (e.g. equality algorithms), indices mapping and pattern recognition
 * of tensors. Also sorting needs to provide fast algorithms of some
 * transformations. Method <strong><code>compareTo(tensor)</code></strong>
 * compares tensors by their hash code, which specified by method <strong><code>int hashCode()</code></strong>,
 * using abstract protected method <strong><code>int hash()</code></strong>,
 * which must be implemented in every inheritor of this class. This abstraction
 * allows different hash algorithms for different tensor types. For example, the
 * most simple and fundamental class {@link SimpleTensor} returns {@link SimpleTensor#name}
 * as it's hash code.
 * <p/>
 * <
 * p/> <h4><a name="Iterable">Iterable</a></h4>
 * {@link Iterable} implementation provided by abstract method <strong><code>TensorIterator iterator()
 * </code></strong>. This method returns {@link TensorIterator} instance, which
 * allows to iterate and set the tensor components of this during iterating.
 * Such specification allows to separate tensor, transformation and comparison
 * essences. Using
 * <code>TensorIterator</code>, it is possible to go through tensor components
 * of this, so we do not need to specify any code for transformations or
 * comparisons algorithms in
 * <code>Tensor</code> class.
 * <p/>
 * <
 * p/> <h4><a name="Observer">Observer</a></h4>
 * {@link Observer} implementation allows have some 'lazy initialized ' fields
 * in tensor inheritors. Every tensor has field {@link #parent} - link on its
 * parent tensor, i.e. tensor whose component it is. Thus, every tensor knows,
 * which part it is. Method <strong><code>void update()</code></strong> can be
 * overridden in inheritors. Inheritor, which have lazy fields, must override
 * method as follow: first it must dump lazy fields of current tensor, and then
 * invoke super.update(), which basically calls update() method of parent
 * tensor. For example look {@link MultiTensor#update() }. This method must be
 * called every time, when tensor, has changed.
 * <p/>
 * <
 * p/> <h4><a name="Other">Other</a></h4> This class specifies abstract method <strong><code>clone()</code></strong>,
 * due to mutability of tensors objects. Copy, returned by this method, must
 * have
 * {@code Context.get().getRootParentTensor()} as it parent tensor. Also this
 * class specifies <strong><code>toStrin(mode)</code></strong> method for
 * <code>String</code> output. Value mode is of type {@link cc.redberry.core.context.ToStringMode}
 * and it is specifying printing mode. For example it can be utf8 or LaTeX
 * format. Every inheritor must implement this method to describe possibilities
 * of it own printing. Method <strong><code>toString()</code></strong> returning <strong><code>toString({@link cc.redberry.core.context.Context#getDefaultPrintMode()})</code></strong>.
 * <p/>
 * <
 * p/> <p>To implement this class, the programmer needs only to extend this
 * class and provide implementations for the
 * {@link #hash()},
 * {@link #toString(cc.redberry.core.context.ToStringMode)},
 * {@link #clone() } and
 * {@link #iterator() } methods.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see cc.redberry.core.indices.Indices
 * @see cc.redberry.core.context.ToStringMode
 * @see cc.redberry.core.context.Context
 */
public abstract class Tensor
        implements Comparable<Tensor>,
        Iterable<Tensor> {

    /**
     * <p>This method was added to the Tensor interface to obligate all tensors to
     * implement custom hash codes. This hash code should reflects only tensor structure
     * whatever the particular indices are.</p>
     * <p>There are other ways to calculate hash code for Tensor object (see
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
     * <p>Returns iterator over sub-tensors of current tensor (eg. summnads of sum, multipliers of
     * product, etc..).</p>
     * <p>For iteration through whole tensor tree use: {@link cc.redberry.core.tensor.iterator.TreeTraverseIterator}
     * or {@link cc.redberry.core.tensor.iterator.TensorFirstIterator} and
     * {@link cc.redberry.core.tensor.iterator.TensorLastIterator}.</p>
     *
     * @return iterator over sub-tensors
     */
    @Override
    public Iterator<Tensor> iterator() {
        return new BasicTensorIterator(this);
    }

    /**
     * <p>Returns i-th sub-tensor of this tensor (eg. summand of sum, argument of tensor field, etc...)</p>
     *
     * @param i index of sub-tensor
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
     * <p>Returns new tensor instance with i-th sub-tensor replaced by provided tensor.
     * <p>Better tools for tensors manipulations are tree iterators. See:
     * {@link cc.redberry.core.tensor.iterator.TensorFirstIterator},
     * {@link cc.redberry.core.tensor.iterator.TensorLastIterator} and
     * {@link cc.redberry.core.tensor.iterator.TreeTraverseIterator}.</p>
     * </p>
     *
     * @param i      index of sub-tensor to be replaced
     * @param tensor tensor to replace i-th sub-tensor
     * @return new instance of tensor
     */
    public final Tensor set(int i, Tensor tensor) {
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
     * <p>Retrieves several sub-tensors from current tensor. This function is faster
     * than sequential invocations of {@link #get(int)} method.</p>
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

    /**
     * <p>Returns a string representation of a tensor. See {@link cc.redberry.core.context.ToStringMode}
     * for available modes.</p>
     *
     * @param mode printing mode (see. {@link cc.redberry.core.context.ToStringMode})
     * @return a string representation of a tensor
     */
    public abstract String toString(final ToStringMode mode);

    /**
     * <p>Returns string representation of a tensor in default (see {@link cc.redberry.core.context.CC#getDefaultPrintMode()}) mode.
     * <p>Equivalent to: <code>this.toString(CC.getDefaultPrintMode())</code>
     * </p></p>
     *
     * @return string representation of a tensor in default mode
     */
    @Override
    public final String toString() {
        return toString(Context.get().getDefaultPrintMode());
    }

    /**
     * For internal use.
     */
    protected String toString(ToStringMode mode, Class<? extends Tensor> clazz) {
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
     * Creates a builder for this tensor. See {@link TensorBuilder} for more information.
     *
     * @return builder for this tensor
     */
    public abstract TensorBuilder getBuilder();

    /**
     * Returns a factory for this tensor. See {@link TensorFactory} for more information.
     *
     * @return builder for this tensor
     */
    public abstract TensorFactory getFactory();
}
