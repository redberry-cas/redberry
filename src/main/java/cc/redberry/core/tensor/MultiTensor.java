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
     * Removes tensors at the specified positions and returns the result.
     *
     * @param positions position in tensor
     * @return result of removing
     */
    public Tensor remove(int[] positions) {
        int[] p = MathUtils.getSortedDistinct(positions);
        Tensor temp = this;
        for (int i = p.length - 1; i >= 0; --i) {
            if (temp instanceof MultiTensor) {
                temp = ((MultiTensor) temp).remove(p[i]);
            } else temp = getNeutral();
        }
        return temp;
    }

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

        final int[] p = MathUtils.getSortedDistinct(positions);
        if (p.length == size())
            return this;
        return select1(p);
    }


    protected abstract Complex getNeutral();

    protected abstract Tensor select1(int[] positions);
}
