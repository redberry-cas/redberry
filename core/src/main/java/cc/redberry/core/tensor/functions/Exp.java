/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 * @deprecated should be replace with power
 */
@Deprecated
public final class Exp extends ScalarFunction {

    Exp(Tensor argument) {
        super(argument);
    }

    @Override
    public Tensor derivative() {
        return this;
    }

    @Override
    protected int hash() {
        return 3 * argument.hashCode();
    }

    @Override
    public String functionName() {
        return "Exp";
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ScalarFunctionBuilder(ExpFactory.FACTORY);
    }

    @Override
    public TensorFactory getFactory() {
        return ExpFactory.FACTORY;
    }

    public static final class ExpFactory extends ScalarFunctionFactory {

        public static final ExpFactory FACTORY = new ExpFactory();

        private ExpFactory() {
        }

        @Override
        public Tensor create1(Tensor arg) {
            if (arg instanceof Log)
                return arg.get(0);
            if (TensorUtils.isZero(arg))
                return Complex.ONE;
            return new Exp(arg);
        }
    }
}
