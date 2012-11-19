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
 * the Free Software Foundation, either version 2 of the License, or
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

import java.util.Iterator;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IntCombinationsGenerator implements IntCombinatoricGenerator {
    final int[] combination;
    private final int n, k;
    private boolean onFirst = true;

    public IntCombinationsGenerator(int n, int k) {
        if (n < k)
            throw new IllegalArgumentException(" n < k ");
        this.n = n;
        this.k = k;
        this.combination = new int[k];
        reset();
    }

    @Override
    public boolean hasNext() {
        return onFirst || !isLast();
    }

    @Override
    public void reset() {
        onFirst = true;
        for (int i = 0; i < k; ++i)
            combination[i] = i;
    }

    private boolean isLast() {
        for (int i = 0; i < k; ++i)
            if (combination[i] != i + n - k)
                return false;
        return true;
    }

    @Override
    public int[] next() {
        if (onFirst)
            onFirst = false;
        else {
            int i;
            for (i = k - 1; i >= 0; --i)
                if (combination[i] != i + n - k)
                    break;
            int m = ++combination[i++];
            for (; i < k; ++i)
                combination[i] = ++m;
        }
        return combination;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<int[]> iterator() {
        return this;
    }

    public int getK() {
        return k;
    }

    public int getN() {
        return n;
    }

    @Override
    public int[] getReference() {
        return combination;
    }
}
