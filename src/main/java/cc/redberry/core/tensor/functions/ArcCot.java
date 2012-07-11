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
public class ArcCot extends AbstractScalarFunction {

    ArcCot(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return UnsafeTensors.unsafeMultiplyWithoutIndicesRenaming(Tensors.pow(
                Tensors.sum(Complex.ONE, Tensors.pow(argument, Complex.TWO)), Complex.MINUSE_ONE), Complex.MINUSE_ONE);
    }

    @Override
    protected String functionName() {
        return "ArcCot";
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ArcCotBuilder();
    }

    @Override
    protected int hash() {
        return 2311 * argument.hashCode();
    }

    public static class ArcCotBuilder extends AbstractScalarFunctionBuilder {

        @Override
        public Tensor build() {
            if (arg instanceof Cot)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Tensors.parse("pi/2");
            return new ArcCot(arg);
        }
    }
}
