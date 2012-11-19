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
 * the Free Software Foundation, either version 2 of the License, or
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
public class ArcCot extends ScalarFunction {

    ArcCot(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.multiply(Tensors.pow(
                Tensors.sum(Complex.ONE, Tensors.pow(argument, Complex.TWO)), Complex.MINUS_ONE), Complex.MINUS_ONE);
    }

    @Override
    protected String functionName() {
        return "ArcCot";
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(ArcCotFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return ArcCotFactory.FACTORY;
    }

    @Override
    protected int hash() {
        return 2311 * argument.hashCode();
    }

    public static final class ArcCotFactory extends ScalarFunctionFactory {

        public static final ArcCotFactory FACTORY = new ArcCotFactory();

        private ArcCotFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof Cot)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Tensors.parse("pi/2");
            return new ArcCot(arg);
        }
    }
}
