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

import cc.redberry.core.context.NameAndStructureOfIndices;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;

import java.util.Objects;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseTokenSimpleTensor extends ParseToken {

    public SimpleIndices indices;
    public String name;

    protected ParseTokenSimpleTensor(SimpleIndices indices, String name, TokenType type, ParseToken[] content) {
        super(type, content);
        this.indices = indices;
        this.name = name;
    }

    public ParseTokenSimpleTensor(SimpleIndices indices, String name) {
        super(TokenType.SimpleTensor);
        this.indices = indices;
        this.name = name;
    }

    public NameAndStructureOfIndices getIndicesTypeStructureAndName() {
        return new NameAndStructureOfIndices(name, new StructureOfIndices[]{new StructureOfIndices(indices)});
    }

    @Override
    public Indices getIndices() {
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
}
