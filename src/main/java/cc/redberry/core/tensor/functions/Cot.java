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
public final class Cot extends ScalarFunction {

    Cot(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.pow(new Sin(argument), Complex.MINUS_TWO);
    }

    @Override
    public String functionName() {
        return "Cot";
    }

    @Override
    protected int hash() {
        return 19 * argument.hashCode();
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(CotFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return CotFactory.FACTORY;
    }

    public static final class CotFactory extends ScalarFunctionFactory {

        public static final CotFactory FACTORY = new CotFactory();

        private CotFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof ArcCot)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Complex.COMPLEX_POSITIVE_INFINITY;//FUTURE complex refactoring
            if (TensorUtils.isNumeric(arg))
                return ComplexUtils.cot((Complex) arg);
            return new Cot(arg);
        }
    }
}
