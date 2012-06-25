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
package cc.redberry.core.utils;

import org.apache.commons.math3.random.BitsStreamGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ByteBackedBitArrayTest {

    @Test
    public void test3() {
        BitsStreamGenerator random = new Well19937c();
        for (int sukatvarblyad = 0; sukatvarblyad < 100; ++sukatvarblyad) {
            int length;
            boolean[] array = new boolean[length = random.nextInt(100000)];
            ByteBackedBitArray bitArray = new ByteBackedBitArray(length);

            int i, bitCount = 0;
            IntArrayList bitsPositions = new IntArrayList();
            for (i = 0; i < length; ++i)
                if (array[i] = random.nextBoolean()) {
                    bitCount++;
                    bitArray.set(i);
                    bitsPositions.add(i);
                }

            assertEquals(bitCount, bitArray.bitCount());
            assertEquals(bitCount, bitsPositions.size());

            int pointer = 0;
            for (i = 0; i < length; ++i) {
                assertTrue(array[i] == bitArray.get(i));
                if (pointer != bitCount)
                    assertTrue(bitsPositions.get(pointer) == bitArray.nextTrailingBit(i));
                else
                    assertTrue(-1 == bitArray.nextTrailingBit(i));
                if (array[i])
                    pointer++;
            }

            bitArray.setAll();
            assertEquals(length, bitArray.bitCount());
        }
    }
}