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
package cc.redberry.core.indices;

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.utils.IntArray;

/**
 * This interface states common tensor indices functionality. For specification
 * and more information see methods summary and implementations. Objects of this type
 * are considered to be immutable. <p>For individual index structure (bit masks in the integer
 * representation) see {@link IndicesUtils}.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see IndicesBuilder
 * @see IndexMapping
 * @see IndicesUtils
 */
public interface Indices {

    /**
     * Return immutable {@link IntArray} of upper case indices.
     *
     * @return {@code IntArray} of upper case indices
     * @see IntArray
     */
    IntArray getUpper();

    /**
     * Return immutable {@link IntArray} of lower case indices.
     *
     * @return {@code IntArray} of lower case indices
     * @see IntArray
     */
    IntArray getLower();

    /**
     * Return immutable {@link IntArray} of all indices.
     *
     * @return {@code IntArray} of all indices
     */
    IntArray getAllIndices();

    /**
     * Returns the number of indices
     *
     * @return number of indices
     */
    int size();


    /**
     * Returns the number of indices of specified type.
     *
     * @param type type of indices
     * @return number of indices of specified type
     */
    int size(IndexType type);

    /**
     * Returns the index at the specified position in this
     * <code>Indices</code>.
     *
     * @param position position of the index
     * @return the index at the specified position in this
     *         <code>Indices</code>
     * @throws IndexOutOfBoundsException - if the index is out of range (index <
     *                                   0 || index >= size())
     */
    int get(int position);

    /**
     * Returns the index of the specified type at the
     * specified position in this <code>Indices</code>.
     *
     * @param type     IndexType
     * @param position position of the index to return
     * @return the index of the specified type at the
     *         specified position in this <code>Indices</code>
     * @throws IndexOutOfBoundsException - if the index is out of range
     */
    int get(IndexType type, int position);

//    /**
//     * Returns sorted array of dummy indices names.
//     *
//     * @return sorted array of dummy indices names
//     */
//    int[] getDummyNames();

    /**
     * Returns new instance of {@code Indices}, which contains only free (non contracted) indices from
     * this {@code Indices} instance. The returned {@code Indices} object keeps the relative order
     * of indices same as in the this {@code Indices}.
     *
     * @return only free indices
     */
    Indices getFree();

    /**
     * Returns new instance of {@code Indices}, which contains inverted indices, i.e.
     * all upper indices becomes lower, and all lower become upper. The terms of ordering
     * in the result are same as in the initial {@code Indices} object.
     *
     * @return indices with inverted states
     */
    Indices getInverted();

    /**
     * Returns indices of the specified type, which are contained
     * in this {@code Indices} object.
     *
     * @param type the type of indices
     * @return indices of the specified type, which are contained
     *         in this indices object.
     */
    Indices getOfType(IndexType type);

    /**
     * Returns {@code true} if this {@code Indices} object contains exactly same
     * indices as specified one, without taking into account the relative ordering of indices.
     *
     * @param indices indices
     * @return {@code true} if this {@code Indices} object contains exactly same
     *         indices as specified one, with no respect to the relative ordering of indices.
     */
    boolean equalsRegardlessOrder(Indices indices);

    /**
     * Returns always {@code true} in public API methods.
     */
    void testConsistentWithException();

    /**
     * This method applies specified {@link IndexMapping} to this {@code Indices} object and
     * returns the resulting {@code Indices} object.
     *
     * @param mapping specified {@code IndexMapping}
     * @return resulting {@code Indices} object
     * @see IndexMapping
     */
    Indices applyIndexMapping(IndexMapping mapping);

    /**
     * String representation of {@code Indices} object in specified output format.
     *
     * @param outputFormat output format
     * @return string representation of {@code Indices} object in specified output format.
     * @see cc.redberry.core.context.OutputFormat
     */
    String toString(OutputFormat outputFormat);

    @Override
    boolean equals(Object other);

    /**
     * Returns the array of indices <b>id</b>s with respect to symmetries. First
     * of all, for SortedIndices, it always returns the array with length equal
     * to #size() and filled by zeros. For SimpleIndices we assume, that each
     * index in Indices data array have some <b>id</b>, which is {@code short}.
     * If there is no any symmetries, index <b>id</b> is equal to the index
     * position in Indices data array. If there is a symmetry, which transpose
     * two indices, then their <b>id</b>s are equal, otherwise their <b>id</b>s
     * are not equals. For example, if we consider the following symmetries: [2,
     * 1, 0, 3, 4, 5, 6, 7] and [0, 1, 2, 3, 7, 4, 6, 5], the resulting diffIds
     * array will be the following: [0, 0, 1, 2, 3, 3, 4, 3]
     *
     * @return the array of indices <b>id</b>s with respect to symmetries
     */
    short[] getDiffIds();
}
