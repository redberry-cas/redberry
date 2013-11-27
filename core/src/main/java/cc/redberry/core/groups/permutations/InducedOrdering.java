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


import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntComparator;

import java.util.Arrays;

/**
 * An ordering of points Ω(n) induced by a base B: if b<sub>i</sub>, b<sub>j</sub> ∈ B then
 * b<sub>i</sub> ≺ b<sub>j</sub> if and only if i < j, and b ≺ a for any b ∈ B and a ∉ B .
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class InducedOrdering implements IntComparator {
    private final int[] sortedBase;
    private final int[] positions;
    private final int[] base;


    /**
     * Construct an ordering induced by specified base
     *
     * @param base base permutation group
     */
    public InducedOrdering(final int[] base) {
        this.base = base;
        this.sortedBase = base.clone();
        this.positions = new int[base.length];
        for (int i = 1; i < base.length; ++i)
            positions[i] = i;
        ArraysUtils.quickSort(this.sortedBase, positions);
    }

    /**
     * Return a reference to sorted array of base points
     *
     * @return a reference to sorted array of base points
     */
    public int[] getSortedBaseReference() {
        return sortedBase;
    }

    /**
     * Returns a position of specified point in base or {@code Integer.MAX_VALUE} if specified point is not a base point.
     * Method complexity is log(k), where k is length of base.
     *
     * @param a some point
     * @return position of specified point in base or {@code Integer.MAX_VALUE} if specified point is not a base point
     */
    public int positionOf(int a) {
        int p = Arrays.binarySearch(sortedBase, a);
        if (p < 0)
            return Integer.MAX_VALUE;
        return positions[p];
    }

    @Override
    public int compare(int a, int b) {
        if (a == b) return 0;
        if (a < 0) return a < b ? -1 : 1;
        if (b < 0) return b < a ? 1 : -1;

        int pa = positionOf(a), pb = positionOf(b);
        if (pa == Integer.MAX_VALUE && pb == Integer.MAX_VALUE)//not base points
            return 0;
        return Integer.compare(pa, pb);
    }

    /**
     * Returns the ≺-greatest point under this ordering.
     *
     * @param a point
     * @param b point
     * @return ≺-greatest point under this ordering
     */
    public int max(int a, int b) {
        return compare(a, b) >= 0 ? a : b;
    }

    /**
     * Returns the ≺-least point under this ordering.
     *
     * @param a point
     * @param b point
     * @return ≺-greatest point under this ordering
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
        int pivot = 0;
        for (int i = 0; i < sortedBase.length; ++i) {
            if (pivot != sortedBase[i])
                return pivot;
            ++pivot;
        }
        return base[base.length - 1];
    }

    /**
     * Returns the min element representative under this ordering, i.e. the element that less then any point. Returns
     * -1 in this implementation.
     *
     * @return max element representative under this ordering, i.e. the element that larger then all points
     */
    public int minElement() {
        int pivot = 0;
        for (int i = 0; i < sortedBase.length; ++i) {
            if (pivot != sortedBase[i])
                return pivot;
            ++pivot;
        }
        return base[base.length - 1];
    }
}