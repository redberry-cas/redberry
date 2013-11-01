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
package cc.redberry.core.parser;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

import java.util.Arrays;

/**
 * Abstract syntax tree. The implementation is a simple linked tree.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParseToken {

    /**
     * Node type.
     */
    public final TokenType tokenType;
    /**
     * Parent node.
     */
    public ParseToken parent;
    /**
     * Child nodes.
     */
    public final ParseToken[] content;

    /**
     * @param tokenType node type
     * @param content   child nodes
     */
    public ParseToken(TokenType tokenType, ParseToken... content) {
        this.tokenType = tokenType;
        this.content = content;
        for (ParseToken node : content)
            node.setParent(this);
    }

    private void setParent(ParseToken parent) {
        this.parent = parent;
    }

    /**
     * Returns {@link Indices} of the corresponding mathematical expression.
     *
     * @return {@link Indices} of the corresponding mathematical expression
     */
    public Indices getIndices() {
        switch (tokenType) {
            case Product:
                IndicesBuilder builder = new IndicesBuilder();
                for (ParseToken node : content)
                    builder.append(node.getIndices());
                return builder.getIndices();
            case Sum:
                return IndicesFactory.create(content[0].getIndices());
            case Power:
                return IndicesFactory.EMPTY_INDICES;
        }
        throw new ParserException("Unknown tensor type: " + tokenType);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(tokenType).append("[");
        for (ParseToken node : content)
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

    /**
     * Converts this AST to tensor.
     *
     * @return resulting tensor
     */
    public Tensor toTensor() {
        switch (tokenType) {
            case Sum:
                return Tensors.sum(contentToTensors());
            case Power:
                assert content.length == 2;
                return Tensors.pow(content[0].toTensor(), content[1].toTensor());
            case Product:
                return Tensors.multiplyAndRenameConflictingDummies(contentToTensors());
        }
        throw new ParserException("Unknown tensor type: " + tokenType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParseToken other = (ParseToken) obj;
        if (this.tokenType != other.tokenType)
            return false;
        return Arrays.deepEquals(this.content, other.content);
    }
}
