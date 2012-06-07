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
import cc.redberry.core.tensor.SimpleTensor;

/**
 * This interface states additional functionality of the indices of the
 * {@link SimpleTensor}. In contrast to {@link SortedIndices}, here we have
 * additional methods, which are responsible for the {@link SymmetriesImpl}
 * manipulations.
 *
 * @see Indices
 * @see OrderedIndices
 * @see EmptyIndices
 * @see SymmetriesImpl
 * @see SortedIndices
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public interface SimpleIndices extends Indices {

    /**
     * This method returns {@link SymmetriesImpl} of this {@code Indices}.
     *
     * @return {@code SymmetriesImpl} of this {@code Indices}
     */
    IndicesSymmetries getSymmetries();

    /**
     * This method allows to set {@code SymmetriesImpl} of this {@code Indices}.
     *
     * @param symmetries {@code SymmetriesImpl} to be set as {@code SymmetriesImpl} of
     * this {@code Indices}
     */
    void setSymmetries(IndicesSymmetries symmetries);

    @Override
    SimpleIndices getInverseIndices();

    @Override
    SimpleIndices getFreeIndices();

    @Override
    SimpleIndices applyIndexMapping(IndexMapping im);

    /**
     * Returns result of indices comparing, using their symmetries lists.
     *
     * @param indices indices to compare with this
     *
     * @return
     * <
     * code>true</code> if indices are equals and
     * <code>false</code> if not.
     */
    boolean equalsWithSymmetries(SimpleIndices indices);

    IndicesTypeStructure getIndicesTypeStructure();
}
