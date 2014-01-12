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

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;

import java.util.Objects;

/**
 * AST node for {@link Complex}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ParseTokenNumber extends ParseToken {
    /**
     * Complex number.
     */
    public Complex value;

    public ParseTokenNumber(Complex value) {
        super(TokenType.Number);
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public Tensor toTensor() {
        return value;
    }

    @Override
    public Indices getIndices() {
        return IndicesFactory.EMPTY_INDICES;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ParseTokenNumber other = (ParseTokenNumber) obj;
        return Objects.equals(this.value, other.value);
    }

    public String toString(OutputFormat mode) {
        return "(" + value.toString(mode) + ")";
    }
}
