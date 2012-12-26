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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;

import java.util.Arrays;

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

    public abstract Tensor remove(int position);

    public Tensor select(int[] positions) {
        if (positions.length == 0)
            return getNeutral();
        if (positions.length == 1)
            return get(positions[0]);

        final int[] p = positions.clone();
        Arrays.sort(p);
        for (int i = 1; i < p.length; ++i)
            if (p[i - 1] == p[i])
                throw new IllegalArgumentException("Several similar positions.");

        if (positions.length == size())
            return this;

        return select1(p);
    }

    protected abstract Complex getNeutral();

    protected abstract Tensor select1(int[] positions);
}
