/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

/**
 * AST node for tensor field.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParseTokenTensorField extends ParseTokenSimpleTensor {
    /**
     * Indices of arguments.
     */
    public SimpleIndices[] argumentsIndices;

    /**
     * @param indices          indices of field
     * @param name             string name of field
     * @param content          child nodes
     * @param argumentsIndices indices of arguments.
     */
    public ParseTokenTensorField(SimpleIndices indices,
                                 String name,
                                 ParseToken[] content,
                                 SimpleIndices[] argumentsIndices) {
        super(indices, name, TokenType.TensorField, content);
        this.argumentsIndices = argumentsIndices;
    }

    @Override
    public NameAndStructureOfIndices getIndicesTypeStructureAndName() {
        StructureOfIndices[] typeStructures = new StructureOfIndices[1 + argumentsIndices.length];
        typeStructures[0] = StructureOfIndices.create(indices);
        for (int i = 0; i < argumentsIndices.length; ++i) {
            if (argumentsIndices[i] == null)
                argumentsIndices[i] = IndicesFactory.createSimple(null, content[i].getIndices().getFree());
            typeStructures[i + 1] = StructureOfIndices.create(argumentsIndices[i]);
        }
        return new NameAndStructureOfIndices(name, typeStructures);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append('[');
        for (ParseToken node : content)
            sb.append(node.toString()).append(", ");
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append(']');
        return sb.toString();
    }

    @Override
    public Tensor toTensor() {
        Tensor[] arguments = contentToTensors();
        for (int i = 0; i < arguments.length; ++i)
            if (argumentsIndices[i] == null)
                argumentsIndices[i] = IndicesFactory.createSimple(null, arguments[i].getIndices().getFree());

        int i;
        if ((i = name.indexOf('~')) >= 0) {
            String ordersDescriptor = name.substring(i + 1);
            String fieldName = name.substring(0, i);
            ordersDescriptor = ordersDescriptor.replace(" ", "");
            if (ordersDescriptor.length() == 0)
                throw new ParserException("Error in derivative orders in \"" + name + "\"");

            if (ordersDescriptor.charAt(0) == '(') {
                if (ordersDescriptor.charAt(ordersDescriptor.length() - 1) != ')')
                    throw new ParserException("Unbalanced brackets in derivative orders in \"" + name + "\"");

                ordersDescriptor = ordersDescriptor.substring(1, ordersDescriptor.length() - 1);
            }

            String[] ordersStr = ordersDescriptor.split(",");
            if (ordersStr.length != arguments.length)
                throw new ParserException("Number of arguments does not match number of derivative orders in \"" + name + "\"");
            int[] orders = new int[ordersStr.length];
            for (i = orders.length - 1; i >= 0; --i)
                try {
                    orders[i] = Integer.parseInt(ordersStr[i], 10);
                } catch (NumberFormatException nfe) {
                    throw new ParserException("Illegal order of derivative: \"" + ordersStr[i] + "\" in \"" + name + "\"");
                }

            return Tensors.fieldDerivative(fieldName, indices, argumentsIndices, contentToTensors(), orders);
        } else
            return Tensors.field(name, indices, argumentsIndices, contentToTensors());
    }
}
