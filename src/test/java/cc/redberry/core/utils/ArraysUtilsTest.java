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

import org.apache.commons.math3.stat.descriptive.*;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ArraysUtilsTest {

    public ArraysUtilsTest() {
    }

    @Test
    public void testShort1() {
        short[] target = {2, 1, 0};
        assertArrayEquals(new int[]{2, 1, 0}, ArraysUtils.quickSortP(target));
    }

    @Test
    public void testShort2() {
        short[] target = {2};
        assertArrayEquals(new int[]{0}, ArraysUtils.quickSortP(target));
    }

    @Test
    public void testShort3() {
        short[] target = new short[0];
        assertArrayEquals(new int[0], ArraysUtils.quickSortP(target));
    }
}