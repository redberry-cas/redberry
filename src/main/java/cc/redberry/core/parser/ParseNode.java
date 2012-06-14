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

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseNode {

    public final TensorType tensorType;
    public ParseNode parent;
    public final ParseNode[] content;

    public ParseNode(TensorType tensorType, ParseNode... content) {
        this.tensorType = tensorType;
        this.content = content;
        for (ParseNode node : content)
            node.setParent(this);
    }

    private void setParent(ParseNode parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(tensorType).append("[");
        for (ParseNode node : content)
            builder.append(node).append(", ");
        builder.deleteCharAt(builder.length() - 1).deleteCharAt(builder.length() - 1).append("]");
        return builder.toString();
    }

    protected Tensor[] contentToTensors() {
        Tensor[] tensors = new Tensor[content.length];
        for (int i = 0; i < content.length; ++i)
            tensors[i] = content[i].toTensor();
        return tensors;
    }

    public Tensor toTensor() {
        switch (tensorType) {
            case Sum:
                return Tensors.sum(contentToTensors());
            case Pow:
                if (content.length != 2)
                    throw new IllegalStateException("Incorrect power arguments count.");
                return Tensors.pow(content[0].toTensor(), content[1].toTensor());
            case Product:
                return Tensors.multiply(contentToTensors());
        }
        throw new ParserException("Unknown tensor type: " + tensorType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParseNode other = (ParseNode) obj;
        if (this.tensorType != other.tensorType)
            return false;
        if (!Arrays.deepEquals(this.content, other.content))
            return false;
        return true;
    }
}
