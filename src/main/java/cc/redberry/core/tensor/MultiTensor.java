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

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.Indices;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class MultiTensor extends Tensor {

    protected final Indices indices;

    MultiTensor(Indices indices) {
        this.indices = indices;
    }

   
    @Override
    public Indices getIndices() {
        return indices;
    }

    protected abstract char operationSymbol();

    //protected abstract Indices calculateIndices();
    //protected abstract int calculateHash();

    
    //TODO implement without builder?
    public final Tensor remove(int position) {
        int size = size();
        if (position >= size || position < 0)
            throw new IndexOutOfBoundsException();
        TensorBuilder builder = getBuilder();
        for (int i = 0; i < size; ++i)
            if (i == position)
                continue;
            else
                builder.put(get(i));
        return builder.build();
    }

    @Override
    public String toString(ToStringMode mode) {
        char operation = operationSymbol();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; ++i) {
            sb.append(get(i).toString(mode, this.getClass()));
            if (i == size() - 1)
                return sb.toString();
            sb.append(operation);
        }
    }
}
