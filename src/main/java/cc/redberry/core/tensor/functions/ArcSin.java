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
import cc.redberry.core.tensor.AbstractScalarFunction;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorBuilder;
import cc.redberry.core.tensor.Tensors;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ArcSin extends AbstractScalarFunction {

    ArcSin(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return Tensors.pow(Tensors.sum(Complex.ONE, Tensors.pow(argument, Complex.TWO)), Complex.MINUSE_ONE_HALF);
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
        return new ArcSinBuilder();
    }

    public static class ArcSinBuilder extends AbstractScalarFunctionBuilder {

        @Override
        public Tensor buid() {
            if (arg instanceof Sin)
                return arg.get(0);
            return new ArcSin(arg);
        }
    }
}
