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
package cc.redberry.core.indices;

import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.tensor.SimpleTensor;

/**
 * Additional specification for indices of simple tensors (see {@link SimpleTensor}).
 * {@code SimpleIndices} preserves the relative ordering of indices with the same type.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Indices
 * @see IndicesFactory
 * @see SimpleIndicesBuilder
 * @since 1.0
 */
public interface SimpleIndices extends Indices {

    /**
     * Returns {@link IndicesSymmetries} of this {@code Indices}.
     *
     * @return symmetries of this indices
     */
    IndicesSymmetries getSymmetries();

//    /**
//     * Returns {@link IndicesSymmetries} of specified "subindices", i.e. {@code IndicesSymmetries} which contain
//     * permutations that fix all indices (pointwise) in {@code this} expect specified indices.
//     *
//     * @param subIndices indices in this
//     * @return symmetries of specified indices in this
//     * @throws java.lang.IllegalArgumentException if specified {@code subindices} are not a real "subindices" of this.
//     *                                            To be precise: the exception thrown if for any <i>i</i> {@code this} does not
//     *                                            contain {@code subindices.get(i)}; the exception also thrown if for
//     *                                            some <i>i, j, i < j</i> and <i>m, n, m > n</i>
//     *                                            {@code subindices.get(i) == this.get(m) && subindices.get(j) == this.get(n)}
//     */
//    IndicesSymmetries getSymmetriesOf(SimpleIndices subIndices);

    /**
     * Sets indices symmetries to the specified
     *
     * @param symmetries {@link IndicesSymmetries}
     */
    void setSymmetries(IndicesSymmetries symmetries);

    @Override
    SimpleIndices getUpper();

    @Override
    SimpleIndices getLower();

    @Override
    SimpleIndices getInverted();

    @Override
    SimpleIndices getFree();

    @Override
    SimpleIndices getOfType(IndexType type);

    @Override
    SimpleIndices applyIndexMapping(IndexMapping im);

    /**
     * Compares simple indices taking into account possible permutations according to the symmetries.
     *
     * @param indices indices to compare with this
     * @return {@code true} if specified indices can be obtained via permutations (specified by symmetries)
     * of this indices.
     */
    boolean equalsWithSymmetries(SimpleIndices indices);

    /**
     * Returns the structure of this indices.
     *
     * @return structure of this indices
     */
    StructureOfIndices getStructureOfIndices();
}
