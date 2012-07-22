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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.EmptyIterator;
import java.util.Iterator;

/**
 * This class describes the most fundamental tensor object - simple tensor, such
 * as {@code A_{mn}}, etc. The class has two main fields - indices and name.
 * <p/>
 * <p><b>Indices</b> <br> Indices of {@code SimpleTensor} are always {@link SimpleIndicesImpl}.
 * So, if you parse simple tensor {@code "A_{mnb}^{a}"}, the result tensor
 * indices will be stored in that order, in witch they were in string. If we
 * want to compare tensors e.g. {@code A_{mn}} and {@code A_{nm}}, the result
 * will be true only if tensor {@code A_{mn}} is symmetric.
 * <p/>
 * <p><b>Name</b> <br>Name is an integer number, different for different
 * tensors, and map between integer name and its string representation is
 * keeping by
 * {@link cc.redberry.core.context.NameManager}, witch, in turn, are stored in {@code Context}.
 * Integer name assignment procedure and putting it in Redberry namespace
 * happens on parsing (for examples, see {@code NameManager}).
 * {@code SimpleTensor} returns name value as its hash code. The specification
 * is so, that two tensors are different (i.e. has different names) if their
 * string names are different or their indices types structures are different.
 * So, for example tensors: <BLOCKQUOTE>
 * {@code A_{mn}} and {@code B_{mn}} - has different names; <br>{@code A_{mn}}
 * and {@code A_{\\alpha b}} - has different names; <br>{@code A_{mn}} and {@code A_{abc}}
 * - has different names; <br>{@code A_{mn}} and {@code A_{ab}} - has equals
 * names; <br>{@code A_{mn}} and {@code A^{ab}} - has equals names;
 * </BLOCKQUOTE> For more information about specification of name field see
 * {@code NameManager}.
 * <p/>
 * <p> NOTE: It is highly recommended to use {@link CC#parse(String)} to
 * construct new simple tensor object, instead of using class constructors,
 * witch can cause namespace conflicts or constructor exceptions.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Tensor
 * @see cc.redberry.core.indices.Indices
 * @see cc.redberry.core.context.NameManager
 * @see cc.redberry.core.context.Context
 */
public class SimpleTensor extends Tensor {

    protected final SimpleIndices indices;
    protected final int name;

    /**
     * Constructs {@code SimpleTensor} instance with given name and indices. In
     * fact, this is the base constructor witch is using during parsing.
     * Constructor invokes {@link cc.redberry.core.indices.Indices#testConsistent()}
     * method and throws {@code InconsistentIndicesException} when specified
     * indices are inconsistent and context regime not testing. <p> NOTE: It is
     * highly recommended to use {@link CC#parse(String)} to construct new
     * simple tensor object, instead of using class constructors, witch can
     * cause namespace conflicts or constructor exceptions.
     *
     * @param name    integer tensor name representation
     * @param indices indices of this tensor
     *
     * @throws InconsistentIndicesException when specified indices are
     *                                      inconsistent and context regime not
     *                                      testing.
     *
     * @see Indices#testConsistent()
     */
    SimpleTensor(int name, SimpleIndices indices) {
        this.name = name;
        this.indices = indices;
    }

    /**
     * Returns the name of this tensor.
     *
     * @return name of this tensor
     */
    public int getName() {
        return name;
    }

    /**
     * Returns the name of this tensor
     *
     * @return name of this tensor
     */
    @Override
    protected int hash() {
        return name;
    }

    @Override
    public SimpleIndices getIndices() {
        return indices;
    }

    @Override
    public Tensor get(int i) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString(ToStringMode mode) {
        StringBuilder sb = new StringBuilder();
        sb.append(CC.getNameDescriptor(name).getName(indices));
        sb.append(indices.toString(mode));
        return sb.toString();
    }

    @Override
    public final Iterator<Tensor> iterator() {
        return EmptyIterator.INSTANCE;
    }

    @Override
    public TensorBuilder getBuilder() {
        return new Builder(this);
    }

    @Override
    public TensorFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SimpleTensor other = (SimpleTensor) obj;
        if (this.name != other.name)
            return false;
        return this.indices.equals(other.indices);
    }

    private static final class Builder implements TensorBuilder {

        private final SimpleTensor tensor;

        public Builder(SimpleTensor tensor) {
            this.tensor = tensor;
        }

        @Override
        public Tensor build() {
            return tensor;
        }

        @Override
        public void put(Tensor tensor) {
            throw new IllegalStateException("Can not put to SimpleTensor builder!");
        }
    }

    private static final class Factory implements TensorFactory {

        private final SimpleTensor st;

        public Factory(SimpleTensor st) {
            this.st = st;
        }

        @Override
        public Tensor create(Tensor... tensors) {
            if (tensors.length != 0)
                throw new IllegalArgumentException();
            return st;
        }
    }
}
