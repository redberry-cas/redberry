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
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ArcCos extends ScalarFunction {

    ArcCos(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.multiply(Tensors.pow(Tensors.sum(Complex.ONE, Tensors.pow(argument, Complex.TWO)), Complex.MINUSE_ONE_HALF), Complex.MINUSE_ONE);
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
            return new ArcCos(arg);
        }
    }
}
