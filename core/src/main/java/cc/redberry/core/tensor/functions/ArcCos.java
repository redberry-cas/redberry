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
public class ArcCos extends ScalarFunction {

    ArcCos(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.multiply(Tensors.pow(Tensors.sum(Complex.ONE, Tensors.pow(argument, Complex.TWO)), Complex.MINUS_ONE_HALF), Complex.MINUS_ONE);
    }

    @Override
    protected String functionName() {
        return "ArcCos";
    }

    @Override
    protected int hash() {
        return 92841 * argument.hashCode();
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(ArcCosFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return ArcCosFactory.FACTORY;
    }

    public static final class ArcCosFactory extends ScalarFunctionFactory {

        public final static ArcCosFactory FACTORY = new ArcCosFactory();

        private ArcCosFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof Cos)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Tensors.parse("pi/2");
            if (TensorUtils.isNumeric(arg))
                return ComplexUtils.arccos((Complex) arg);
            return new ArcCos(arg);
        }
    }
}
