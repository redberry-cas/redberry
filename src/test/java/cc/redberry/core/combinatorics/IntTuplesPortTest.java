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
package cc.redberry.core.combinatorics;

import cc.redberry.concurrent.*;
import java.util.*;
import org.apache.commons.math3.complex.*;
import org.junit.*;
import static cc.redberry.core.TAssert.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IntTuplesPortTest {

    @Test
    public void test1() {
        IntTuplesPort port = new IntTuplesPort(3, 3, 3);
        int count = 0;
        while (port.take() != null)
            ++count;
        Assert.assertEquals(count, 27);
    }

    @Test
    public void test2() {
        IntTuplesPort port = new IntTuplesPort(4, 4, 4, 4);
        int count = 0;
        while (port.take() != null)
            ++count;
        Assert.assertEquals(count, 256);
    }
}
