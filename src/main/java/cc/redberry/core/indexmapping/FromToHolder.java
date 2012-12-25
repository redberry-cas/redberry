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
package cc.redberry.core.indexmapping;

import cc.redberry.core.utils.ArraysUtils;

public final class FromToHolder {
    final int[] from, to;
    final boolean signum;

    FromToHolder(int[] from, int[] to, boolean signum) {
        ArraysUtils.quickSort(from, to);
        this.from = from;
        this.to = to;
        this.signum = signum;
    }

    public FromToHolder mergeWith(FromToHolder other) {
        final int[] aFrom = this.from, aTo = this.to,
                bFrom = other.from, bTo = other.to;

        int bPointer = 0, aPointer = 0;
        int counter = 0;
        while (aPointer < aFrom.length && bPointer < bFrom.length)
            if (aFrom[aPointer] == bFrom[bPointer]) {
                if (aTo[aPointer] != bTo[bPointer])
                    throw new IllegalArgumentException();
                aPointer++;
                bPointer++;
                counter++;
            } else if (aFrom[aPointer] < bFrom[bPointer]) {
                aPointer++;
                counter++;
            } else if (aFrom[aPointer] > bFrom[bPointer]) {
                counter++;
                bPointer++;
            }
        counter += (aFrom.length - aPointer) + (bFrom.length - bPointer); //Assert aPoiner==a.length || bPointer==b.length
        final int[] resultFrom = new int[counter],
                resultTo = new int[counter];
        counter = 0;
        aPointer = 0;
        bPointer = 0;
        while (aPointer < aFrom.length && bPointer < bFrom.length)
            if (aFrom[aPointer] == bFrom[bPointer]) {
                resultTo[counter] = bTo[bPointer];
                resultFrom[counter++] = bFrom[bPointer];
                aPointer++;
                bPointer++;
            } else if (aFrom[aPointer] < bFrom[bPointer]) {
                resultTo[counter] = aTo[aPointer];
                resultFrom[counter++] = aFrom[aPointer++];
            } else if (aFrom[aPointer] > bFrom[bPointer]) {
                resultTo[counter] = bTo[bPointer];
                resultFrom[counter++] = bFrom[bPointer++];
            }
        if (aPointer == aFrom.length) {
            System.arraycopy(bTo, bPointer, resultTo, counter, bTo.length - bPointer);
            System.arraycopy(bFrom, bPointer, resultFrom, counter, bFrom.length - bPointer);
        } else {
            System.arraycopy(aTo, aPointer, resultTo, counter, aTo.length - aPointer);
            System.arraycopy(aFrom, aPointer, resultFrom, counter, aFrom.length - aPointer);
        }
        return new FromToHolder(resultFrom, resultTo, signum ^ other.signum);
    }
}
