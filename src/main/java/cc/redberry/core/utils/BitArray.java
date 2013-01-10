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
package cc.redberry.core.utils;

/**
 * Mutable array of bits with fixed size.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see ByteBackedBitArray
 * @see LongBackedBitArray
 * @since 1.0
 */
public interface BitArray {
    /**
     * Logical {@code and} operation.
     *
     * @param bitArray bit array
     * @throws IllegalArgumentException if size of this not equals to size of specified array.
     */
    void and(BitArray bitArray);

    /**
     * Number of nonzero bits in this array.
     *
     * @return number of nonzero bits in this array
     */
    int bitCount();

    /**
     * Sets i-th bit to zero.
     *
     * @param i position in array
     */
    void clear(int i);

    /**
     * Sets all bits in this array to zero.
     */
    void clearAll();

    /**
     * Returns a deep clone of this.
     *
     * @return a deep clone of this
     */
    BitArray clone();

    /**
     * Returns bit at i-th position.
     *
     * @param i position
     * @return value of bit
     */
    boolean get(int i);

    /**
     * Returns an array with positions of the nonzero bits.
     *
     * @return an array with positions of the nonzero bits
     */
    int[] getBits();

    /**
     * Returns {@code true} if there are at least two nonzero bits at same positions in
     * this and specified array.
     *
     * @param bitArray bit array
     * @return {@code true} if there are at least two nonzero bits at the same positions in
     *         this array and specified array
     * @throws IllegalArgumentException if size of this not equal to the size pf specified array
     */
    boolean intersects(BitArray bitArray);

    /**
     * This will set all bits in this array to bits from specified array.
     *
     * @param bitArray bit array
     * @throws IllegalArgumentException if size of this not equals to size of specified array
     */
    void loadValueFrom(BitArray bitArray);

    /**
     * Logical or operation.
     *
     * @param bitArray bit array
     * @throws IllegalArgumentException if size of this not equals to size of specified array
     */
    void or(BitArray bitArray);

    /**
     * Sets i-th bit in this to 1.
     *
     * @param i position of bit
     */
    void set(int i);

    /**
     * Sets i-th bit in this to specified value.
     *
     * @param i     position of bit
     * @param value bit
     */

    void set(int i, boolean value);

    /**
     * Sets all bits in this to 1.
     */
    void setAll();

    /**
     * Size of array.
     *
     * @return size of array
     */
    int size();

    /**
     * Logical xor operation.
     *
     * @param bitArray bit array
     * @throws IllegalArgumentException if size of this not equals to size of specified array
     */
    void xor(BitArray bitArray);

    //todo comment
    int nextTrailingBit(int position);
}
