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

/**
 * Abstract class which postulates common tensor functionalities. This class
 * specifies common abstract methods, which inherent to every
 * <code>Tensor</code> implementation.
 * <p/>
 * <
 * p/> <h4><a name="Indices">Indices</a></h4> The most fundamental tensor
 * attribute is its indices. Interface
 * {@link cc.redberry.core.indices.Indices} provides common indices
 * functionalities. There are some
 * <code>Indices</code> implementations, and different implementations used in
 * different inheritors of
 * <code>Tensor</code>. Specified abstract method for getting Indices: <strong><code>Indices getIndices(),</code></strong>
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
 * @see MultiTensor
 * @see Observer
 * @see java.util.Iterator
 * @see TensorIterator
 * @see cc.redberry.core.context.ToStringMode
 * @see cc.redberry.core.context.Context
 */
public abstract class Tensor
        implements Comparable<Tensor>,
        Iterable<Tensor> {

    /**
     * This method was make abstract to bind all inheritors have their hash code
     * function.
     * <code>Object</code> method <strong><code>hashCode()</code> </strong>
     * simply returns the result of this method. Always try to make unique hash
     * function when implementing this method. For examples see implementation
     * in existing inheritors.
     *
     * @return hash code of this tensor
     * @see MultiTensor#hash()
     * @see SimpleTensor#hash()
     */
    protected abstract int hash();

    /**
     * Returns
     * <code>Indices</code> of this
     * <code>Tensor</code>
     *
     * @return indices of this tensor
     * @see cc.redberry.core.indices.Indices
     */
    public abstract Indices getIndices();

    /**
     * This method returns
     * <code>TensorIterator</code> instance, which allows to iterate and set the
     * tensor components of this during iterating. Such specification allows to
     * separate tensor, transformation and comparison essences. Using
     * <code>TensorIterator</code>, it is possible to go through tensor
     * components of this, so we do not need to specify any code for
     * transformations or comparisons algorithms in
     * <code>Tensor</code> class.
     *
     * @return iterator
     */
    @Override
    public Iterator<Tensor> iterator() {
        return new BasicTensorIterator(this);
    }

    public abstract Tensor get(int i);

    public abstract int size();

    public final Tensor set(int position, Tensor tensor) {
        int size = size();
        if (position >= size || position < 0)
            throw new IndexOutOfBoundsException();
        if (tensor == null)
            throw new NullPointerException();
        TensorBuilder builder = getBuilder();
        for (int i = 0; i < size; ++i)
            if (i == position)
                builder.put(tensor);
            else
                builder.put(get(i));
        return builder.build();
    }

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
     * Returns a string representation of tensor. Parameter
     * {@link cc.redberry.core.context.ToStringMode} mode specifies
     * representation mode. By default, we provides two print modes: <strong><code>ToStringMode.LaTeX</code></strong>
     * and <strong><code>ToStringMode.UTF8</code></strong>. For example, in
     * first case indices of tensor printing as their char representation, and
     * in second as LaTeX code. Indices string representation specified in
     * {@link cc.redberry.core.indices.Indices#toString(cc.redberry.core.context.ToStringMode)
     * }.
     * <p/>
     * <p>Example code: <BR><code> Expression expr = Context.get().parser("A_mn/B");</code> <BR><code> System.out.println(expr.toString(ToStringMode.LaTeX));</code>
     * <p>The result will be: <BR>
     * <code>\frac{A_{m n}}{B}</code> <p><code> System.out.println(expr.toString(ToStringMode.UTF8))</code>
     * <p>The result will be: <BR>
     * <code>A_mn/B</code>
     *
     * @param mode symbols printing mode (e.g.
     *             <code>UTF8</code> or
     *             <code>LaTeX</code>)
     * @return a string representation of tensor
     * @see cc.redberry.core.context.ToStringMode
     * @see cc.redberry.core.indices.Indices#toString(cc.redberry.core.context.ToStringMode)
     */
    public abstract String toString(final ToStringMode mode);

    /**
     * @return {@code toString(CC.getDefaultPrintMode())}
     */
    @Override
    public final String toString() {
        return toString(Context.get().getDefaultPrintMode());
    }

    protected String toString(ToStringMode mode, Class<? extends Tensor> clazz) {
        return toString(mode);
    }

    /**
     * Comparing by hash code method.
     *
     * @param t tensor to compare
     * @return < code>(hash() < t.hash() ? -1 : (hash() == t.hash() ? 0 : 1))</code>
     */
    @Override
    public final int compareTo(Tensor t) {
        int hash = hash(), thash = t.hash();
        return hash < thash ? -1 : (hash == thash ? 0 : 1);
    }

    @Override
    public final int hashCode() {
        return hash();
    }

    public TensorBuilder getBuilder() {
        return new DefaultBuilder(getFactory(), size());
    }

    public abstract TensorFactory getFactory();
}
