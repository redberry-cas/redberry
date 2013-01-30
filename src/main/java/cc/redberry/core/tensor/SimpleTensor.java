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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.utils.EmptyIterator;

import java.util.Iterator;

/**
 * Implementation of simple tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class SimpleTensor extends Tensor {

    protected final SimpleIndices indices;
    protected final int name;

    SimpleTensor(int name, SimpleIndices indices) {
        this.name = name;
        this.indices = indices;
    }

    /**
     * Returns the name (unique identifier) of this tensor.
     *
     * @return name of this tensor
     * @see cc.redberry.core.context.NameDescriptor
     */
    public int getName() {
        return name;
    }

    /**
     * Returns the name (unique identifier) of this tensor
     *
     * @return name of this tensor
     * @see cc.redberry.core.context.NameDescriptor
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
    public String toString(OutputFormat mode) {
        StringBuilder sb = new StringBuilder();
        sb.append(CC.getNameDescriptor(name).getName(indices));
        sb.append(indices.toString(mode));
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<Tensor> iterator() {
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

    public String getStringName() {
        return CC.current().getNameDescriptor(name).getName(indices);
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

        @Override
        public TensorBuilder clone() {
            return this;
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
