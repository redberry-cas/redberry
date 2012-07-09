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
import cc.redberry.core.number.Rational;
import cc.redberry.core.utils.TensorUtils;
import java.math.BigInteger;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PowerBuilder implements TensorBuilder {

    private Tensor argument, power;

    public PowerBuilder() {
    }

    @Override
    public Tensor build() {
        if (power == null)
            throw new IllegalStateException("Power is not fully constructed.");
        //TODO improve Complex^Complex
        if (argument instanceof Complex && power instanceof Complex) {
            Complex a = (Complex) argument;
            Complex p = (Complex) power;
            if (a.isInfinite())
                if (p.isZero())
                    return Complex.ComplexNaN;
                else
                    return a;
            if (a.isOne())
                if (p.isInfinite())
                    return p.multiply(a);
                else
                    return a;
            if (p.isOne())
                return a;
            if (a.isZero()) {
                if (p.getReal().signum() <= 0)
                    return Complex.ComplexNaN;
                return a;
            }
            if (p.isZero())
                return Complex.ONE;
            if (a.isNumeric() || p.isNumeric())
                return a.powNumeric(p);
            if (p.isReal()) {
                Rational pp = (Rational) p.getReal();
                if (pp.isInteger()) {
                    boolean sign = pp.getNumerator().compareTo(BigInteger.ZERO) > 0;
                    BigInteger exponent = pp.getNumerator().abs();

                    Complex result = Complex.ONE, base = a;
                    while (exponent.signum() > 0) {
                        if (exponent.testBit(0))
                            result = result.multiply(base);
                        base = base.multiply(base);
                        exponent = exponent.shiftRight(1);
                    }
                    if (sign)
                        return result;
                    else
                        return result.reciprocal();
                }
            }
        }
        if (TensorUtils.isOne(power))
            return argument;
        if (TensorUtils.isZero(power) || TensorUtils.isOne(argument))
            return Complex.ONE;
        if (TensorUtils.isZero(argument))
            return Complex.ZERO;
        if (argument instanceof Product) {
            Tensor[] scalars = ((Product) argument).getAllScalars();
            if (scalars.length > 1) {
                TensorBuilder pb = argument.getBuilder();//creating product builder             
                for (Tensor t : scalars)
                    pb.put(Tensors.pow(t, power));//TODO refactor for performance
                return pb.build();
            }
        }
        if (argument instanceof Power)
            return Tensors.pow(argument.get(0), Tensors.multiplyUnsafe(argument.get(1), power));
        return new Power(argument, power);
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
}
