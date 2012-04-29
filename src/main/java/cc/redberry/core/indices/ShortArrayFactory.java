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
package cc.redberry.core.indices;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//CHECKSTYLE
final class ShortArrayFactory {
    private static final int SIZE = 128;
    private static final short[][] zerofilledShorts = new short[SIZE][];

    private ShortArrayFactory() {
    }

//    static {
//        for (int i = 0; i < zerofilledShorts.length; ++i) {
//            short[] array = new short[i];
//            //Arrays.fill(array, 0);
//            zerofilledShorts[i] = array;
//        }
//    }
//
//    static short[] getZeroFilledShortArray(final int length) {
//        if (length >= zerofilledShorts.length)
//            return new short[length];
//        return zerofilledShorts[length];
//    }
    static short[] getZeroFilledShortArray(final int length) {
        if (length >= SIZE)
            return new short[length];
        return zerofilledShorts[length] == null ? zerofilledShorts[length] = new short[length] : zerofilledShorts[length];
    }
}
