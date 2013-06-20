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
package cc.redberry.core.indexgenerator;

import java.util.Arrays;

/**
 * Generates distinct integers, which does not contain in
 * specified engaged data.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IntGenerator {
    private static final int[] EMPTY_ARRAY = {-1};
    private int[] engagedData;
    private int counter, match;

    /**
     * Constructs generator with no engaged data
     */
    public IntGenerator() {
        this(EMPTY_ARRAY);
    }

    private IntGenerator(int[] engagedData, int counter, int match) {
        this.engagedData = engagedData;
        this.counter = counter;
        this.match = match;
    }

    /**
     * Constructs generator with specified engaged data
     *
     * @param engagedData engaged data
     */
    public IntGenerator(int[] engagedData) {
        this.engagedData = engagedData;
        counter = -1;
        match = 0;
        Arrays.sort(this.engagedData);
        int shift = 0;
        int i = 0;
        while (i + shift + 1 < engagedData.length)
            if (engagedData[i + shift] == engagedData[i + shift + 1])
                ++shift;
            else {
                engagedData[i] = engagedData[i + shift];
                ++i;
            }
        engagedData[i] = engagedData[i + shift];
        while (++i < engagedData.length)
            engagedData[i] = Integer.MAX_VALUE;
    }

    /**
     * Merge data from another generator
     *
     * @param intGenerator integers generator
     */
    public void mergeFrom(IntGenerator intGenerator) {
        if (intGenerator.engagedData != engagedData)
            throw new IllegalArgumentException();
        if (intGenerator.counter > this.counter) {
            this.counter = intGenerator.counter;
            this.match = intGenerator.match;
        }
    }

    /*private void ensureCapacity(int minCapacity) {
        int oldCapacity = engagedData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            engagedData = Arrays.copyOf(engagedData, newCapacity);
            //todo potentially breaks generator: if engagedData == EMPTY_ARRAY, it possibly can be filled
            Arrays.fill(engagedData, oldCapacity, newCapacity, Integer.MAX_VALUE);
        }
    }

    public void add(int index) {
        if (index <= counter)
            return;
        int pointer = Arrays.binarySearch(engagedData, match, engagedData.length, index);
        if (pointer >= 0)
            return;
        pointer = ~pointer; //-pointer-1
        if (engagedData[engagedData.length - 1] != Integer.MAX_VALUE)
            ensureCapacity(engagedData.length + 1);
        System.arraycopy(engagedData, pointer, engagedData, pointer + 1, engagedData.length - pointer - 1);
        engagedData[pointer] = index;
    }*/

    /**
     * Returns new integer.
     *
     * @return new integer
     */
    public int getNext() {
        counter++;
        while (match < engagedData.length && engagedData[match] == counter) {
            match++;
            counter++;
        }
        return counter;
    }

    /**
     * Returns true if index contains in engaged data or already was generated.
     *
     * @param index integer
     * @return true if index contains in engaged data or already was generated
     */
    public boolean contains(int index) {
        if (counter >= index)
            return true;
        return Arrays.binarySearch(engagedData, match, engagedData.length, index) >= 0;
    }

    @Override
    public IntGenerator clone() {
        //OLD VARIANT [NEW NEW VARIANT]:
        //engagedData not cloning due to absence ability to adding indices,
        //so it never can be changed
        return new IntGenerator(engagedData, counter, match);
        //NEW VARIANT:
        //return new IntGenerator(engagedData.clone(), counter, match);
    }
}
