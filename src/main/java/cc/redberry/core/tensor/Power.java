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
package cc.redberry.core.tensor;

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;

/**
 * Representation of mathematical power <i>A^B</i>.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see PowerBuilder
 * @see PowerFactory
 */
public final class Power extends Tensor {

    private final Tensor argument, power;

    Power(Tensor a, Tensor power) {
        this.argument = a;
        this.power = power;
    }

    @Override
    public Tensor get(int i) {
        switch (i) {
            case 0:
                return argument;
            case 1:
                return power;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public Indices getIndices() {
        return IndicesFactory.EMPTY_INDICES;
    }

    @Override
    protected int hash() {
        return 37 * argument.hash() + power.hash();
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public String toString(OutputFormat mode) {
        if (mode == OutputFormat.WolframMathematica)
            return "Power[" + argument.toString(mode) + ", " + power.toString(mode) + "]";

        if (mode == OutputFormat.LaTeX)
            if (TensorUtils.isRealNegativeNumber(power))
                return "\\frac{1}{" + argument.toString(mode, Power.class) +
                        (TensorUtils.isMinusOne(power) ?
                                "" : "^" + ((Complex) power).abs().toString(mode)) + "}";
            else
                return argument.toString(mode, Power.class) + "^{" + power.toString(mode) + "}";
        return argument.toString(mode, Power.class) + "**" + power.toString(mode, Power.class);

//        return "Power[" + argument.toString(mode) + ", " + power.toString(mode) + "]";
    }

    @Override
    public TensorBuilder getBuilder() {
        return new PowerBuilder();
    }

    @Override
    public TensorFactory getFactory() {
        return PowerFactory.FACTORY;
    }
}
