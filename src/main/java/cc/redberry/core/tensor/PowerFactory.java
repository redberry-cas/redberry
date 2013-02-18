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

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Exponentiation;
import cc.redberry.core.utils.TensorUtils;

/**
 * {@link TensorFactory} for powers. It performs basic simplifications and reduces the
 * resulting power to the standard form.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Power
 * @since 1.0
 */
public final class PowerFactory implements TensorFactory {

    public static final PowerFactory FACTORY = new PowerFactory();

    private PowerFactory() {
    }

    @Override
    public Tensor create(Tensor... tensors) {
        checkWithException(tensors);
        return power(tensors[0], tensors[1]);
    }

    static Tensor power(Tensor argument, Tensor power) {
        //TODO improve Complex^Complex
        if (argument instanceof Complex && power instanceof Complex) {

            Complex a = (Complex) argument;
            Complex p = (Complex) power;
            Complex result = Exponentiation.exponentiateIfPossible(a, p);

            if (result != null)
                return result;
        }
        if (TensorUtils.isOne(power))
            return argument;
        if (TensorUtils.isZero(power) || TensorUtils.isOne(argument))
            return Complex.ONE;
        if (TensorUtils.isZero(argument))
            return Complex.ZERO;
        if (argument instanceof Product) {
            if (TensorUtils.isInteger(power)
                    //case (2*x)**(y)           //todo replace with isPositiveNumerical(argument.get(0))
                    || (argument.size() == 2 && TensorUtils.isRealPositiveNumber(argument.get(0)))) {
                Tensor[] scalars = ((Product) argument).getAllScalars();
                if (scalars.length > 1) {
                    TensorBuilder pb = argument.getBuilder();//creating product builder
                    for (Tensor t : scalars)
                        pb.put(Tensors.pow(t, power));//TODO refactor for performance
                    return pb.build();
                }
            }
        }
        if (argument instanceof Power)
            return Tensors.pow(argument.get(0), Tensors.multiply(argument.get(1), power));
        return new Power(argument, power);
    }

    private static void checkWithException(Tensor[] tensors) {
        if (tensors.length != 2)
            throw new IllegalArgumentException("Wrong number of arguments.");
        if (!TensorUtils.isScalar(tensors))
            throw new IllegalArgumentException("Non scalar power parametres.");
        for (Tensor t : tensors)
            if (t == null)
                throw new NullPointerException();
    }
}
