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
package cc.redberry.core.tensor.functions;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.ComplexUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import cc.redberry.core.tensor.TensorFactory;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Tan extends ScalarFunction {

    Tan(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.pow(new Cos(argument), Complex.MINUS_TWO);
    }

    @Override
    public String functionName() {
        return "Tan";
    }

    @Override
    protected int hash() {
        return 17 * argument.hashCode();
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(TanFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return TanFactory.FACTORY;
    }

    public static class TanFactory extends ScalarFunctionFactory {

        public static final TanFactory FACTORY = new TanFactory();

        private TanFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof ArcTan)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Complex.ZERO;
            if (TensorUtils.isNumeric(arg))
                return ComplexUtils.tan((Complex) arg);
            return new Tan(arg);
        }
    }
}
