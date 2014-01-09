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
package cc.redberry.core.groups.permutations;


import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.IntComparator;

import java.util.Arrays;

/**
 * An ordering of points &Omega;(n) induced by a base B: if b<sub>i</sub>, b<sub>j</sub> &isin; B then
 * b<sub>i</sub> <b>&lt;</b> b<sub>j</sub> if and only if i < j, and b <b>&lt;</b> a for any b &isin; B and a ∉ B .
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.6
 */
public class InducedOrdering implements IntComparator {
    private final int[] positions;
    private final int degree;

    /**
     * Construct an ordering induced by specified base
     *
     * @param base base permutation group
     */
    public InducedOrdering(final int[] base, final int degree) {
        this.positions = new int[degree + 2];
        this.degree = degree;

        Arrays.fill(positions, -1);
        for (int i = 0; i < base.length; ++i)
            this.positions[1 + base[i]] = i;
        int next = base.length;
        for (int i = 1; i < degree + 1; ++i)
            if (positions[i] == -1)
                positions[i] = next++;

        positions[0] = Integer.MIN_VALUE;
        positions[degree + 1] = Integer.MAX_VALUE;
    }

    /**
     * Returns a position of specified point in base or {@code Integer.MAX_VALUE} if specified point is not a base point.
     *
     * @param a some point
     * @return position of specified point in base or {@code Integer.MAX_VALUE} if specified point is not a base point
     */
    public int positionOf(int a) {
        return positions[a + 1];
    }

    @Override
    public int compare(int a, int b) {
        return Integer.compare(positions[a + 1], positions[b + 1]);
    }

    /**
     * Returns the <b>&lt;</b>-greatest point under this ordering.
     *
     * @param a point
     * @param b point
     * @return <b>&lt;</b>-greatest point under this ordering
     */
    public int max(int a, int b) {
        return compare(a, b) >= 0 ? a : b;
    }

    /**
     * Returns the <b>&lt;</b>-least point under this ordering.
     *
     * @param a point
     * @param b point
     * @return <b>&lt;</b>-least point under this ordering
     */
    public int min(int a, int b) {
        return compare(a, b) >= 0 ? b : a;
    }

    /**
     * Returns the max element representative under this ordering, i.e. the element that larger then all points
     *
     * @return max element representative under this ordering, i.e. the element that larger then all points
     */
    public int maxElement() {
        return degree;
    }

    /**
     * Returns the min element representative under this ordering, i.e. the element that less then any point. Returns
     * -1 in this implementation.
     *
     * @return max element representative under this ordering, i.e. the element that larger then all points
     */
    public int minElement() {
        return -1;
    }

    /**
     * Returns the <b>&lt;</b>-least point under this ordering in specified array.
     *
     * @param array array
     * @return <b>&lt;</b>-least point under this ordering
     */
    public int min(final int[] array) {
        if (array.length == 0)
            throw new IllegalArgumentException();
        int min = array[0];
        for (int i = 1; i < array.length; ++i)
            min = min(min, array[i]);
        return min;
    }

    /**
     * Returns the <b>&lt;</b>-least point under this ordering in specified array.
     *
     * @param array array
     * @return <b>&lt;</b>-least point under this ordering
     */
    public int min(final IntArrayList array) {
        if (array.size() == 0)
            throw new IllegalArgumentException();
        int min = array.get(array.size() - 1);
        for (int i = array.size() - 2; i >= 0; --i)
            min = min(min, array.get(i));
        return min;
    }

    /**
     * Returns the <b>&lt;</b>-greatest point under this ordering in specified array.
     *
     * @param array array
     * @return <b>&lt;</b>-greatest point under this ordering
     */
    public int max(final int[] array) {
        if (array.length == 0)
            throw new IllegalArgumentException();
        int max = array[0];
        for (int i = 1; i < array.length; ++i)
            max = max(max, array[i]);
        return max;
    }

    /**
     * Returns the <b>&lt;</b>-greatest point under this ordering in specified array.
     *
     * @param array array
     * @return <b>&lt;</b>-greatest point under this ordering
     */
    public int max(final IntArrayList array) {
        if (array.size() == 0)
            throw new IllegalArgumentException();
        int max = array.get(array.size() - 1);
        for (int i = array.size() - 2; i >= 0; --i)
            max = max(max, array.get(i));
        return max;
    }
}