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
package cc.redberry.core.indices;

import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.parser.ParserIndices;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by poslavsky on 06/02/16.
 */
public class IndicesSymmetriesTest {
    @Test
    public void test1() throws Exception {
        SimpleIndices indices = ParserIndices.parseSimple("_abcdefpq");
        indices.getSymmetries().addSymmetries(Permutations.createPermutation(1, 0, 3, 2));
        Assert.assertArrayEquals(new short[]{0, 0, 1, 1, 4, 5, 6, 7}, indices.getPositionsInOrbits());
    }
}