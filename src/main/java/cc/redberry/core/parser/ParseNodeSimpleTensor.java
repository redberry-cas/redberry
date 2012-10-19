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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import java.util.Objects;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ParseNodeSimpleTensor extends ParseNode {

    public SimpleIndices indices;
    public String name;

    protected ParseNodeSimpleTensor(SimpleIndices indices, String name, TensorType type, ParseNode[] content) {
        super(type, content);
        this.indices = indices;
        this.name = name;
    }

    public ParseNodeSimpleTensor(SimpleIndices indices, String name) {
        super(TensorType.SimpleTensor);
        this.indices = indices;
        this.name = name;
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
        final ParseNodeSimpleTensor other = (ParseNodeSimpleTensor) obj;
        if (!Objects.equals(this.indices, other.indices))
            return false;
        return Objects.equals(this.name, other.name);
    }
}
