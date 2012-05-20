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

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class TensorFieldBuilder implements TensorBuilder {

    private final TensorField field;
    private int pointer = 0;
    private final Tensor[] data;

    public TensorFieldBuilder(TensorField field) {
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
        if (!tensor.getIndices().getFreeIndices().equalsIgnoreOrder(field.getArgIndices(pointer)))
            throw new IllegalArgumentException("Free indices of puted tensor differs from field argument binding indices!");
        data[pointer++] = tensor;
    }
}
