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
public class LongBackedBitArrayTest {

    @Test
    public void test1() {
        LongBackedBitArray array = new LongBackedBitArray(4);
        array.set(0);
        array.set(2);
        assertEquals(array.toString(), "1010");
        assertEquals(array.nextTrailingBit(0), 0);
        assertEquals(array.nextTrailingBit(1), 2);
        assertEquals(array.nextTrailingBit(2), 2);
        assertEquals(array.nextTrailingBit(3), -1);
        assertEquals(array.bitCount(), 2);
        array.xor(array);
        assertEquals(array.toString(), "0000");
    }

    @Test
    public void test2() {
        LongBackedBitArray array = new LongBackedBitArray(99);
        array.set(0);
        array.set(2);
        array.set(64);
        array.set(65);
        array.set(80);
        array.set(98);
        assertEquals(array.bitCount(), 6);
        assertEquals(array.nextTrailingBit(0), 0);
        assertEquals(array.nextTrailingBit(1), 2);
        assertEquals(array.nextTrailingBit(2), 2);
        assertEquals(array.nextTrailingBit(3), 64);
        assertEquals(array.nextTrailingBit(64), 64);
        assertEquals(array.nextTrailingBit(65), 65);
        assertEquals(array.nextTrailingBit(66), 80);
        assertEquals(array.nextTrailingBit(80), 80);
        assertEquals(array.nextTrailingBit(90), 98);
        assertEquals(array.nextTrailingBit(98), 98);
        assertEquals(array.bitCount(), 6);
        array.xor(array);
        assertEquals(array.bitCount(), 0);
    }

    @Test
    public void test3() {
        BitsStreamGenerator random = new Well19937c();
        for (int sukatvarblyad = 0; sukatvarblyad < 10; ++sukatvarblyad) {
            int length;
            boolean[] array = new boolean[length = random.nextInt(100000)];
            LongBackedBitArray bitArray = new LongBackedBitArray(length);

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
