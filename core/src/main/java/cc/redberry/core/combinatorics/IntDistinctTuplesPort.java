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
package cc.redberry.core.combinatorics;

import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.LongBackedBitArray;

import java.util.Arrays;

/**
 * This class represents an iterator (implemented in the output port pattern) over
 * all distinct N-tuples, which can be chosen from {@code N} sets of integers. More formally,
 * for {@code N} integer arrays: <i>array</i><sub>1</sub>,
 * <i>array</i><sub>2</sub>,...,<i>array</i><sub>N</sub>, this class allows to
 * iterate over all possible integer arrays of the form [i<sub>1</sub>, i<sub>2</sub>,...,i<sub>N</sub>],
 * where all numbers numbers i<sub>j</sub> are different and i<sub>1</sub> is chosen
 * from <i>array</i><sub>1</sub>, i<sub>2</sub> is chosen from <i>array</i><sub>2</sub> and so on.</p>
 *
 * <p>Consider the example:
 * <code><pre>
 * int[] a1 = {1, 2, 3};
 * int[] a2 = {2, 3};
 * DistinctCombinationsPort dcp = new DistinctCombinationsPort(a1, a2);
 * int[] tuple;
 * while ((tuple = dcp.take()) != null)
 *      System.out.println(Arrays.toString(tuple));
 * </pre></code>
 * This code will produce the following sequence:
 * <code><pre>
 * [1, 2]
 * [1, 3]
 * [2, 3]
 * [3, 2]
 * </pre></code>
 * </p>
 *
 * <p>This class is implemented via output port pattern and the calculation of the next
 * tuple occurs only on the invocation of {@link #take()}.</p>
 *
 * <p><b>Note:</b> method {@link #take()} returns the same reference on each invocation.
 * So, if it is needed not only to obtain the information from {@link #take()}, but also save the result,
 * it is necessary to clone the returned array.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class IntDistinctTuplesPort implements IntCombinatorialPort {
    private final BitArray previousMask;
    private final BitArray[] setMasks;
    private final int[] combination;
    private final BitArray temp;
    private byte state = -1;

    public IntDistinctTuplesPort(int[]... sets) {
        int maxIndex = 0;
        for (int[] set : sets) {
            if (set.length == 0)
                continue;
            Arrays.sort(set);
            if (maxIndex < set[set.length - 1])
                maxIndex = set[set.length - 1];
        }
        ++maxIndex;
        previousMask = new LongBackedBitArray(maxIndex);
        temp = new LongBackedBitArray(maxIndex);

        setMasks = new BitArray[sets.length];
        for (int i = 0; i < sets.length; ++i) {
            setMasks[i] = new LongBackedBitArray(maxIndex);
            for (int j : sets[i])
                setMasks[i].set(j);
        }
        combination = new int[sets.length];
        previousMask.setAll();
        init();
    }

    private void init() {
        int i = 0, nextBit;
        while (i < setMasks.length) {

            temp.loadValueFrom(setMasks[i]);
            temp.and(previousMask);

            nextBit = temp.nextTrailingBit(combination[i]);
            if (nextBit != -1) {
                combination[i] = nextBit;
                previousMask.clear(nextBit);
            } else {
                if (i == 0) {
                    state = 1;
                    return;
                }
                combination[i] = 0;
                previousMask.set(combination[--i]);
                ++combination[i];
                continue;
            }
            ++i;
        }
    }

    /**
     * Calculates and returns the next tuple, or {@code null} if no more distinct tuples exist.
     *
     * @return the next tuple, or {@code null} if no more distinct tuples exist
     */
    @Override
    public int[] take() {
        if (state == 1)
            return null;

        if (state == -1) {
            state = 0;
            return combination;
        }
        previousMask.set(combination[setMasks.length - 1]++);

        int i = setMasks.length - 1, nextBit;
        while (i < setMasks.length) {

            temp.loadValueFrom(setMasks[i]);
            temp.and(previousMask);

            nextBit = temp.nextTrailingBit(combination[i]);
            if (nextBit != -1) {
                combination[i] = nextBit;
                previousMask.clear(nextBit);
            } else {
                if (i == 0) {
                    state = 1;
                    return null;
                }
                combination[i] = 0;
                previousMask.set(combination[--i]);
                ++combination[i];
                continue;
            }
            ++i;
        }

        return combination;
    }

    @Override
    public void reset() {
        state = -1;
        Arrays.fill(combination, 0);
        previousMask.setAll();
        init();
    }

    @Override
    public int[] getReference() {
        return combination;
    }
}
