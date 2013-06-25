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

import java.util.Iterator;

/**
 * The implementation of {@code Iterator<Tensor>} based on {@code Tensor}
 * {@link cc.redberry.core.tensor.Tensor#get(int)} and {@link cc.redberry.core.tensor.Tensor#size()}  methods.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class BasicTensorIterator implements Iterator<Tensor> {
    private final Tensor tensor;
    private int position = -1;
    private final int size;

    /**
     * Creates iterator over tensor elements.
     *
     * @param tensor tensor
     */
    public BasicTensorIterator(Tensor tensor) {
        this.tensor = tensor;
        this.size = tensor.size();
    }

    @Override
    public boolean hasNext() {
        return position < size - 1;
    }

    @Override
    public Tensor next() {
        return tensor.get(++position);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}