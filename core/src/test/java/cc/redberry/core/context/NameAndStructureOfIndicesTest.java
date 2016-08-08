/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.context;

import cc.redberry.core.indices.StructureOfIndices;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by poslavsky on 02/08/16.
 */
public class NameAndStructureOfIndicesTest {
    @Test
    public void test1() throws Exception {
        int hash = Integer.MIN_VALUE;
        for (char c = 'A'; c < 'z'; ++c) {
            final int x = new NameAndStructureOfIndices(Character.toString(c), StructureOfIndices.getEmpty()).hashCode();
            Assert.assertTrue(x > hash);
            hash = x;
        }
    }
}