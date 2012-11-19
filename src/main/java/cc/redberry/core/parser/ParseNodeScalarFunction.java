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
package cc.redberry.core.parser;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

import java.util.Objects;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ParseNodeScalarFunction extends ParseNode {

    public String function;

    public ParseNodeScalarFunction(String function, ParseNode[] content) {
        super(TensorType.ScalarFunction, content);
        if (content.length != 1)
            throw new IllegalArgumentException();
        this.function = function;
    }

    @Override
    public Indices getIndices() {
        return IndicesFactory.EMPTY_INDICES;
    }

    @Override
    public String toString() {
        return function + "[" + content[0] + "]";
    }

    @Override
    public Tensor toTensor() {
        if (content.length != 1)
            throw new IllegalArgumentException("Wrong scalar function node.");
        Tensor arg = content[0].toTensor();
        switch (function.toLowerCase()) {
            case "sin":
                return Tensors.sin(arg);
            case "cos":
                return Tensors.cos(arg);
            case "tan":
                return Tensors.tan(arg);
            case "cot":
                return Tensors.cot(arg);
            case "arcsin":
                return Tensors.arcsin(arg);
            case "arccos":
                return Tensors.arccos(arg);
            case "arctan":
                return Tensors.arctan(arg);
            case "arccot":
                return Tensors.arccot(arg);
            case "log":
                return Tensors.log(arg);
            case "exp":
                return Tensors.exp(arg);
        }
        throw new IllegalStateException("Unknown scalar function \"" + function + "\".");
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        final ParseNodeScalarFunction other = (ParseNodeScalarFunction) obj;
        return Objects.equals(this.function, other.function);
    }
}
