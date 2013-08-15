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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.math.MathUtils;
import cc.redberry.core.number.Complex;

/**
 * Parent class for sums and products.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
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

    /**
     * Removes tensor at the specified position and returns the result.
     *
     * @param position position in tensor
     * @return result of removing
     */
    public abstract Tensor remove(int position);

    /**
     * Removes the first occurrence of the specified tensor (reference) from this list, if it is present.
     * If the tensor does not contain the element, it is unchanged.
     * More formally, removes the element with the lowest index i such that t == get(i) (if such an element exists).
     *
     * @param tensor specified reference
     * @return result of removing
     */
    public Tensor remove(Tensor tensor) {
        for (int l = 0, size = size(); l < size; ++l)
            if (get(l) == tensor) return remove(l);
        return tensor;
    }

    /**
     * Removes tensors at the specified positions and returns the result.
     *
     * @param positions position in tensor
     * @return result of removing
     * @throws IndexOutOfBoundsException
     */
    public Tensor remove(int[] positions) {
        if(positions.length == 0)
            return this;
        int size = size();
        for (int i : positions)
            if (i >= size || i < 0)
                throw new IndexOutOfBoundsException();

        int[] p = MathUtils.getSortedDistinct(positions.clone());
        if (p.length == size)
            return getNeutral();
        return remove1(p);
    }

    protected abstract Tensor remove1(int[] positions);

    /**
     * Selects tensors at the specified positions and puts it together.
     *
     * @param positions positions in tensor
     * @return result subtensor
     */
    public Tensor select(int[] positions) {
        if (positions.length == 0)
            return getNeutral();
        if (positions.length == 1)
            return get(positions[0]);

        final int[] p = MathUtils.getSortedDistinct(positions.clone());
        if (p.length == size())
            return this;
        return select1(p);
    }


    protected abstract Complex getNeutral();

    protected abstract Tensor select1(int[] positions);
}
