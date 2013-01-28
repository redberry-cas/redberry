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
package cc.redberry.core.utils;

import java.util.Arrays;

/**
 * Immutable integer array. This class is a simple wrapper of generic array of integers.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IntArray {
    public static final IntArray EMPTY_ARRAY = new IntArray(new int[0]);
    final int[] innerArray;

    /**
     * @param innerArray generic integer array to be wrapped
     */
    public IntArray(int[] innerArray) {
        this.innerArray = innerArray;
    }

    /**
     * Returns the integer at the specified position in this {@code IntArray}.
     *
     * @param i position of the integer to return
     * @return the integer at the specified position in this {@code Indices}
     */
    public int get(int i) {
        return innerArray[i];
    }

    /**
     * Returns number of elements in this {@code IntArray}
     *
     * @return number of elements in this array
     */
    public int length() {
        return innerArray.length;
    }

    /**
     * This method returns new integer generic array, with copy of this
     * {@code IntArray} data.
     *
     * @return integer generic array, with copy of this {@code IntArray} data
     */
    public int[] copy() {
        return innerArray.clone();
    }

    /**
     * This method returns new integer generic array, with copy of this
     * {@code IntArray} data.
     *
     * @return integer generic array, with copy of this {@code IntArray} data
     */
    public int[] copy(int from, int to) {
        return Arrays.copyOfRange(innerArray, from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final IntArray other = (IntArray) obj;
        return Arrays.equals(this.innerArray, other.innerArray);
    }

    @Override
    public int hashCode() {
        return 497 + Arrays.hashCode(this.innerArray);
    }

    @Override
    public String toString() {
        return Arrays.toString(innerArray);
    }
}
