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
public final class Cos extends ScalarFunction {

    Cos(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.multiply(Complex.MINUS_ONE, new Sin(argument));
    }

    @Override
    protected int hash() {
        return 11 * argument.hashCode();
    }

    @Override
    public String functionName() {
        return "Cos";
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(CosFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return CosFactory.FACTORY;
    }

    public static final class CosFactory extends ScalarFunctionFactory {

        public static final CosFactory FACTORY = new CosFactory();

        private CosFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof ArcCos)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Complex.ONE;
            return new Cos(arg);
        }
    }
}
