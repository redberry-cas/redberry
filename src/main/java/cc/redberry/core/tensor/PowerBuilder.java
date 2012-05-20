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

import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.*;
import java.math.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PowerBuilder implements TensorBuilder {

    private Tensor argument, power;
    private int state = 0;

    public PowerBuilder() {
    }

    @Override
    public Tensor buid() {
        if (state != 2)
            throw new IllegalStateException("Power is not fully constructed.");
        //TODO Complex^Complex incuding Infinity, NaN, Zero and so so so!!!
        if (TensorUtils.isOne(power))
            return argument;
        if (TensorUtils.isZero(power) || TensorUtils.isOne(argument))
            return Complex.ONE;
        if (TensorUtils.isZero(argument))
            return Complex.ZERO;
        if (argument instanceof Product) {
            TensorBuilder pb = argument.getBuilder();
            for (Tensor t : argument)
                pb.put(TensorsFactory.buildPower(t, power));
            return pb.buid();
        }
        if (argument instanceof Power)
            return TensorsFactory.buildPower(argument.get(0), TensorsFactory.buidProduct(argument.get(1), power));
        return new Power(argument, power);
    }

    @Override
    public void put(Tensor tensor) {
        if (tensor == null)
            throw new NullPointerException();
        if (TensorUtils.isScalar(tensor))
            throw new IllegalArgumentException("Non-scalar tensor on input of Power builder.");
        switch (state) {
            case 0:
                argument = tensor;
                ++state;
                return;
            case 1:
                power = tensor;
                ++state;
                return;
            default:
                throw new IllegalStateException("Power buider can not take more than two put() invocations.");
        }
    }
}
