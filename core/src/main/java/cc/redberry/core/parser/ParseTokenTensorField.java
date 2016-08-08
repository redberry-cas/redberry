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
package cc.redberry.core.parser;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.VarDescriptor;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.Tensors;

/**
 * AST node for tensor field.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParseTokenTensorField extends ParseToken {
    /**
     * Head of this function
     */
    public ParseTokenSimpleTensor head;
    /**
     * Indices of arguments (may be null).
     */
    public SimpleIndices[] argumentsIndices;

    /**
     * @param head    head of this function
     * @param content child nodes
     */
    public ParseTokenTensorField(ParseTokenSimpleTensor head,
                                 ParseToken[] content) {
        this(head, content, TensorField.nullArray(content.length).clone());
    }

    /**
     * @param head             head of this function
     * @param content          child nodes
     * @param argumentsIndices indices of arguments.
     */
    public ParseTokenTensorField(ParseTokenSimpleTensor head,
                                 ParseToken[] content,
                                 SimpleIndices[] argumentsIndices) {
        super(TokenType.TensorField, content);
        this.head = head;
        this.argumentsIndices = argumentsIndices;
        computeResultingIndices();
    }

    public SimpleIndices computeResultingIndices() {
        final VarDescriptor headDescriptor = CC.getNameManager().getVarDescriptor(head.getIndicesTypeStructureAndName());
        if (headDescriptor == null)
            return head.getIndices();
        final SimpleIndices[] ai = new SimpleIndices[content.length];
        for (int i = 0; i < content.length; ++i)
            if (argumentsIndices[i] != null)
                ai[i] = argumentsIndices[i];
            else
                ai[i] = IndicesFactory.createSimple(null, content[i].getIndices());
        return headDescriptor.computeIndices(head.indices, ai);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head.toString()).append('[');
        for (ParseToken node : content)
            sb.append(node.toString()).append(", ");
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(sb.length() - 1).append(']');
        return sb.toString();
    }

    @Override
    public SimpleIndices getIndices() {
        return computeResultingIndices();
    }

    @Override
    public Tensor toTensor() {
//        int i;
//        if ((i = name.indexOf('~')) >= 0) {
//            String ordersDescriptor = name.substring(i + 1);
//            String fieldName = name.substring(0, i);
//            ordersDescriptor = ordersDescriptor.replace(" ", "");
//            if (ordersDescriptor.length() == 0)
//                throw new ParserException("Error in derivative orders in \"" + name + "\"");
//
//            if (ordersDescriptor.charAt(0) == '(') {
//                if (ordersDescriptor.charAt(ordersDescriptor.length() - 1) != ')')
//                    throw new ParserException("Unbalanced brackets in derivative orders in \"" + name + "\"");
//
//                ordersDescriptor = ordersDescriptor.substring(1, ordersDescriptor.length() - 1);
//            }
//
//            String[] ordersStr = ordersDescriptor.split(",");
//            if (ordersStr.length != arguments.length)
//                throw new ParserException("Number of arguments does not match number of derivative orders in \"" + name + "\"");
//            int[] orders = new int[ordersStr.length];
//            for (i = orders.length - 1; i >= 0; --i)
//                try {
//                    orders[i] = Integer.parseInt(ordersStr[i], 10);
//                } catch (NumberFormatException nfe) {
//                    throw new ParserException("Illegal order of derivative: \"" + ordersStr[i] + "\" in \"" + name + "\"");
//                }
//
//            return Tensors.fieldDerivative(fieldName, indices, argumentsIndices, contentToTensors(), orders);
//        } else
//            return Tensors.field(name, indices, argumentsIndices, contentToTensors());
        for (SimpleIndices ai : argumentsIndices)
            if (ai != null)
                return Tensors.field(head.toTensor(), contentToTensors(), argumentsIndices);
        return Tensors.field(head.toTensor(), contentToTensors());
    }
}
