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
import cc.redberry.core.indices.EmptyIndices;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.utils.TensorUtils;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Power extends Tensor {

    private final Tensor[] data;

    public Power(Tensor a, Tensor power) {
        if (!TensorUtils.isScalar(a, power))
            throw new TensorException("Non scalar power: Power[" + a + ", " + power + "]");
        data = new Tensor[2];
        data[0] = a;
        data[1] = power;
    }

    @Override
    public Tensor get(int i) {
        return data[i];
    }

    @Override
    public Indices getIndices() {
        return EmptyIndices.INSTANCE;
    }

    @Override
    protected int hash() {
        return 37 * data[0].hash() + data[1].hash();
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public String toString(ToStringMode mode) {
        return "Power[" + data[0].toString(mode) + ", " + data[1].toString(mode) + "]";
    }
}
