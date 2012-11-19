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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.LongBackedBitArray;

import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DistinctCombinationsPort implements OutputPortUnsafe<int[]> {
    private final BitArray previousMask;
    private final BitArray[] setMasks;
    private final int[] combination;
    private final BitArray temp;
    private byte state = -1;

    public DistinctCombinationsPort(int[][] sets) {
        int maxIndex = 0;
        for (int[] set : sets) {
            if(set.length == 0)
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
}
