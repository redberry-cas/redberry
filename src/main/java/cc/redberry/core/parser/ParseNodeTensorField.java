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
package cc.redberry.core.parser;

import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseNodeTensorField extends ParseNodeSimpleTensor {

    public SimpleIndices[] argumentsIndices;

    public ParseNodeTensorField(SimpleIndices indices, String name, ParseNode[] content, SimpleIndices[] argumentsIndices) {
        super(indices, name, TensorType.TensorField, content);
        this.argumentsIndices = argumentsIndices;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append('[');
        for (ParseNode node : content)
            sb.append(node.toString()).append(", ");
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append(']');
        return sb.toString();
    }

    @Override
    public Tensor toTensor() {
        return TensorsFactory.field(name, indices, argumentsIndices, contentToTensors());
    }
}
