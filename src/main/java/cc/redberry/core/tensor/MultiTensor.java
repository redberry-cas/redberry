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
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class MultiTensor extends Tensor {

    protected final Tensor[] data;
    protected final Indices indices;

    MultiTensor(Tensor[] data, Indices indices) {
        assert data.length > 1;
        this.data = data;
        this.indices = indices;
    }

    @Override
    public Tensor get(int i) {
        return data[i];
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public Indices getIndices() {
        return indices;
    }

    protected abstract char operationSymbol();

    //protected abstract Indices calculateIndices();
    //protected abstract int calculateHash();

    public Tensor[] getRange(int from, int to) {
        return Arrays.copyOfRange(data, from, to);
    }

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
        return builder.buid();
    }

    @Override
    public String toString(ToStringMode mode) {
        if (data.length == 0)
            return "";
        char operation = operationSymbol();
        StringBuilder sb = new StringBuilder();
        for (int i = 0;; ++i) {
            sb.append(data[i].toString(mode, this.getClass()));
            if (i == data.length - 1)
                return sb.toString();
            sb.append(operation);
        }
    }
}
