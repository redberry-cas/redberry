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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.InconsistentIndicesException;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilderSorted;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Product extends MultiTensor {

    public Product(Tensor... data) {
        super(data);
        //TODO add sorting
    }

    @Override
    protected Indices calculateIndices() {
        IndicesBuilderSorted ibs = new IndicesBuilderSorted();
        for (Tensor t : data)
            ibs.append(t);
        try {
            return ibs.getIndices();
        } catch (InconsistentIndicesException exception) {
            throw new InconsistentIndicesException(exception.getIndex(), this);
        }
    }

    @Override
    protected char operationSymbol() {
        return '*';
    }

    public Tensor[] getScalars() {
        throw new UnsupportedOperationException();
    }

    public Tensor getNonScalar() {
        throw new UnsupportedOperationException();
    }

    public ContractionStructure getContractionStructure() {
        throw new UnsupportedOperationException();
    }

    public FullContractionsStructure getFullContractionStructure() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int calculateHash() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
