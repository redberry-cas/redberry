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
package cc.redberry.core.groups.permutations;

import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArray;

import java.util.Arrays;

/**
 * Int-array-list-based Schreier vector
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SchreierVector {
    int[] data;

    public SchreierVector(int[] data) {
        this.data = data;
    }

    public SchreierVector(int initialCapacity) {
        if (initialCapacity < Permutations.DEFAULT_IDENTITY_LENGTH)
            initialCapacity = Permutations.DEFAULT_IDENTITY_LENGTH;
        data = new int[initialCapacity];
        Arrays.fill(data, -2);
    }

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            data = Arrays.copyOf(data, newCapacity);
            Arrays.fill(data, oldCapacity, data.length, -2);
        }
    }

    public void set(int position, int num) {
        if (position >= data.length)
            ensureCapacity(position);
        data[position] = num;
    }

    public int get(int i) {
        if (i >= data.length)
            return -2;
        return data[i];
    }

    public int length() {
        return data.length;
    }

    public void reset() {
        Arrays.fill(data, -2);
    }

    @Override
    public SchreierVector clone() {
        return new SchreierVector(data.clone());
    }
}
