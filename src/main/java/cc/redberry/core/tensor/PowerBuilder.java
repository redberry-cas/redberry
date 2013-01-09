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

import cc.redberry.core.utils.TensorUtils;

/**
 * {@link TensorBuilder} for powers. The implementation is based on {@link PowerFactory}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Power
 * @since 1.0
 */
public final class PowerBuilder implements TensorBuilder {

    private Tensor argument, power;

    public PowerBuilder() {
    }

    private PowerBuilder(Tensor argument, Tensor power) {
        this.argument = argument;
        this.power = power;
    }

    @Override
    public Tensor build() {
        if (power == null)
            throw new IllegalStateException("Power is not fully constructed.");
        return PowerFactory.power(argument, power);
    }

    @Override
    public void put(Tensor tensor) {
        if (tensor == null)
            throw new NullPointerException();
        if (!TensorUtils.isScalar(tensor))
            throw new IllegalArgumentException("Non-scalar tensor on input of Power builder.");
        if (argument == null)
            argument = tensor;
        else if (power == null)
            power = tensor;
        else
            throw new IllegalStateException("Power buider can not take more than two put() invocations.");
    }

    @Override
    public TensorBuilder clone() {
        return new PowerBuilder(argument, power);
    }
}
