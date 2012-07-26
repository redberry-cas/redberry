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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.combinatorics.DistinctCombinationsPort;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.IntArrayList;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class IndexlessBijectionsPort implements OutputPortUnsafe<int[]> {

    private boolean finished = false;
    private final DistinctCombinationsPort combinationsPort;

    public IndexlessBijectionsPort(final Tensor[] from, final Tensor[] to) {
        if (from.length > to.length) {
            finished = true;
            combinationsPort = null;
            return;
        }

        IntArrayList[] hashReflections = new IntArrayList[from.length];
        int i, j, hash;
        for (i = 0; i < from.length; ++i) {
            hashReflections[i] = new IntArrayList();
            hash = from[i].hashCode();

            for (j = 0; j < to.length; ++j)
                if (to[j].hashCode() >= hash)
                    break;
            if (to[j].hashCode() > hash) {
                finished = true;
                break;
            }
            for (; j < to.length; ++j) {
                if (to[j].hashCode() != hash)
                    break;
                hashReflections[i].add(j);
            }
        }
        int[][] r = new int[from.length][];
        for (i = 0; i < from.length; ++i)
            r[i] = hashReflections[i].toArray();
        combinationsPort = new DistinctCombinationsPort(r);
    }

    @Override
    public int[] take() {
        if (finished)
            return null;
        return combinationsPort.take();
    }
}
