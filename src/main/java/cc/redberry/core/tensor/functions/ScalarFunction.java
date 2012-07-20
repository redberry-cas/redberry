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

import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorException;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class ScalarFunction extends Tensor {

    protected final Tensor argument;

    protected ScalarFunction(Tensor argument) {
        if (!TensorUtils.isScalar(argument))
            throw new TensorException("Non scalar argument " + argument + " in scalar function");
        this.argument = argument;
    }

    @Override
    public final Indices getIndices() {
        return IndicesFactory.EMPTY_INDICES;
    }

    protected abstract String functionName();

    public abstract Tensor derivative();

    @Override
    public final Tensor get(int i) {
        if (i != 0)
            throw new IndexOutOfBoundsException();
        return argument;
    }

    @Override
    public final int size() {
        return 1;
    }

    @Override
    public final String toString(ToStringMode mode) {
        String stringSymbol = functionName();
        switch (mode) {
            case UTF8:
                return stringSymbol + "(" + argument.toString(ToStringMode.UTF8) + ")";
            case LaTeX:
                return "\\" + stringSymbol.toLowerCase() + "(" + argument.toString(ToStringMode.UTF8) + ")";
            case REDBERRY:
                return Character.toString(Character.toUpperCase(stringSymbol.charAt(0))) + stringSymbol.substring(1, stringSymbol.length()) + "[" + argument.toString(ToStringMode.REDBERRY) + "]";
            default:
                return stringSymbol + "(" + argument.toString(ToStringMode.UTF8) + ")";
        }
    }
}
