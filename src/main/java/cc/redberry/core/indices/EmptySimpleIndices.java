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
package cc.redberry.core.indices;

import cc.redberry.core.indexmapping.IndexMapping;

/**
 * This class is an implementation of {@link Indices} interface and represents
 * <i>empty indices</i>, i.e. indices that do not contains any index. For
 * example, if we have tensor X, method {@link cc.redberry.core.tensor.Tensor#getIndices()
 * } will return {@code EmptySimpleIndices}, that means that X is scalar. This
 * class is singleton, and you can get instance throughout { @code EMPTY_SIMPLE_INDICES_INSTANCE}
 * field.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class EmptySimpleIndices extends EmptyIndices implements SimpleIndices {

    /**
     * Instance of {@code EmptySimpleIndices}
     */
    static final EmptySimpleIndices EMPTY_SIMPLE_INDICES_INSTANCE = new EmptySimpleIndices();

    EmptySimpleIndices() {
    }

    /**
     * Returns {@code EmptySimpleIndices} instance
     *
     * @return {@code EmptySimpleIndices} instance
     */
    @Override
    public SimpleIndices getInverse() {
        return this;
    }

    /**
     * Returns {@code EmptySimpleIndices} instance
     *
     * @return {@code EmptySimpleIndices} instance
     */
    @Override
    public SimpleIndices getFree() {
        return this;
    }

    /**
     * Doing nothing
     */
    @Override
    public void setSymmetries(IndicesSymmetries symmetries) {
        if (symmetries.getIndicesTypeStructure().size() != 0)
            throw new IllegalArgumentException("Symmetries dimensions are not equal to indices size.");
    }

    /**
     * Returns {@code Symmetries.EMPTY_SYMMETRIES}
     *
     * @return {@code Symmetries.EMPTY_SYMMETRIES}
     */
    @Override
    public IndicesSymmetries getSymmetries() {
        return IndicesSymmetries.EMPTY_SYMMETRIES;
    }

    /**
     * Do nothing.
     *
     * @param mapping
     * @return false
     */
    @Override
    public SimpleIndices applyIndexMapping(IndexMapping mapping) {
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj == this; // :))
    }

    /**
     * Returns {@code equals(indices)}
     *
     * @param indices indices to compare
     * @return {@code equals(indices)}
     */
    @Override
    public boolean equalsWithSymmetries(SimpleIndices indices) {
        return indices == EMPTY_SIMPLE_INDICES_INSTANCE; //There is only one instance of empty SimpleIndices
    }

    /**
     * Returns 1
     *
     * @return 1
     */
    @Override
    public int hashCode() {
        return 453679;
    }

    @Override
    public IndicesTypeStructure getIndicesTypeStructure() {
        return IndicesTypeStructure.EMPTY;
    }
}
