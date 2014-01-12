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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

import java.util.Objects;

/**
 * AST node for simple tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParseTokenSimpleTensor extends ParseToken {
    /**
     * Indices of simple tensor.
     */
    public SimpleIndices indices;
    /**
     * String name of simple tensor.
     */
    public String name;

    protected ParseTokenSimpleTensor(SimpleIndices indices, String name, TokenType type, ParseToken[] content) {
        super(type, content);
        this.indices = indices;
        this.name = name;
    }

    /**
     * @param indices indices of simple tensor
     * @param name    string name of simple tensor
     */
    public ParseTokenSimpleTensor(SimpleIndices indices, String name) {
        super(TokenType.SimpleTensor);
        this.indices = indices;
        this.name = name;
    }

    /**
     * @return {@link NameAndStructureOfIndices}
     */
    public NameAndStructureOfIndices getIndicesTypeStructureAndName() {
        return new NameAndStructureOfIndices(name, new StructureOfIndices[]{new StructureOfIndices(indices)});
    }

    @Override
    public SimpleIndices getIndices() {
        return indices;
    }

    @Override
    public String toString() {
        return name + indices.toString();
    }

    @Override
    public Tensor toTensor() {
        return Tensors.simpleTensor(name, indices);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj))
            return false;
        final ParseTokenSimpleTensor other = (ParseTokenSimpleTensor) obj;
        if (!Objects.equals(this.indices, other.indices))
            return false;
        return Objects.equals(this.name, other.name);
    }

    public boolean isKroneckerOrMetric() {
        return name.equals(CC.getNameManager().getKroneckerName()) ||
                name.equals(CC.getNameManager().getMetricName());
    }

    public boolean isKronecker() {
        return name.equals(CC.getNameManager().getKroneckerName());
    }

    @Override
    public String toString(OutputFormat mode) {
        //Initializing StringBuilder
        StringBuilder sb = new StringBuilder();

        //Adding tensor name
        if (mode == OutputFormat.Maple && isKroneckerOrMetric()) {
            if (isKronecker())
                sb.append("KroneckerDelta");
            else
                sb.append("g_");
        } else
            sb.append(name);

        //If there are no indices return builder content
        if (indices.size() == 0)
            return sb.toString();

        //Writing indices
        boolean external = mode == OutputFormat.WolframMathematica || mode == OutputFormat.Maple;
        if (external)
            sb.append("[");
        sb.append(indices.toString(mode));
        if (external)
            sb.append("]");

        return sb.toString();
    }
}
