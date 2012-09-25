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
package cc.redberry.core.combinatorics;

import cc.redberry.concurrent.OutputPortUnsafe;
import java.util.Arrays;

/**
 * Generates combinations of integers {a_1,a_2,...,a_N}, where
 * <code>0&#60;=a_i&#60;=upperBounds[i]</code>. <br>For example, for
 * <code>uppersBounds = {2, 3, 2}</code> all combinations are
 * <code>
 * <br>[0, 0, 0]
 * <br>[0, 0, 1]
 * <br>[0, 1, 0]
 * <br>[0, 1, 1]
 * <br>[0, 2, 0]
 * <br>[0, 2, 1]
 * <br>[1, 0, 0]
 * <br>[1, 0, 1]
 * <br>[1, 1, 0]
 * <br>[1, 1, 1]
 * <br>[1, 2, 0]
 * <br>[1, 2, 1]
 * </code>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IntTuplesPort implements OutputPortUnsafe<int[]> {

    private final int[] upperBounds;
    private int[] current;

    public IntTuplesPort(final int... upperBounds) {
        checkWithException(upperBounds);
        this.upperBounds = upperBounds;
        this.current = new int[upperBounds.length];
        this.current[upperBounds.length - 1] = -1;
    }

    private static void checkWithException(int[] upperBounds) {
        for (int i : upperBounds)
            if (i < 0)
                throw new IllegalArgumentException("Upper bound cannot be negative.");
    }

    @Override
    public int[] take() {
        int pointer = upperBounds.length - 1;
        boolean next = false;
        ++current[pointer];
        if (current[pointer] == upperBounds[pointer]) {
            current[pointer] = 0;
            next = true;
        }
        while (--pointer >= 0 && next) {
            next = false;
            ++current[pointer];
            if (current[pointer] == upperBounds[pointer]) {
                current[pointer] = 0;
                next = true;
            }
        }
        if (next)
            return null;
        return current;
    }

    public void reset() {
        Arrays.fill(current, 0);
        current[upperBounds.length - 1] = -1;
    }
}
