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
package cc.redberry.core.tensor.functions;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.ComplexUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import cc.redberry.core.tensor.TensorFactory;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ArcSin extends ScalarFunction {

    ArcSin(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.pow(Tensors.sum(Complex.ONE, Tensors.pow(argument, Complex.TWO)), Complex.MINUS_ONE_HALF);
    }

    @Override
    protected String functionName() {
        return "ArcSin";
    }

    @Override
    protected int hash() {
        return 92837 * argument.hashCode();
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(ArcSinFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return new ArcSinFactory();
    }

    public static final class ArcSinFactory extends ScalarFunctionFactory {

        public static final ArcSinFactory FACTORY = new ArcSinFactory();

        private ArcSinFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof Sin)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Complex.ZERO;
            if (TensorUtils.isNumeric(arg))
                return ComplexUtils.arcsin((Complex) arg);
            return new ArcSin(arg);
        }
    }
}
