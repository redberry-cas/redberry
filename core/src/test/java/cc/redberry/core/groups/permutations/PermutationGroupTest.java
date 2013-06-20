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
package cc.redberry.core.groups.permutations;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationGroupTest {

    @Test
    public void testMembership() {
        Permutation a, b, c, d, e, generators[];
        PermutationGroup group;

        a = new Permutation(1, 0, 2, 3, 4, 5, 6);
        b = new Permutation(1, 2, 3, 4, 5, 6, 0);
        generators = new Permutation[]{a, b};
        group = new PermutationGroup(generators);
        Assert.assertEquals(group.getOrder().longValue(), 5040L);

        b = new Permutation(1, 2, 3, 4, 5, 6, 0);
        a = new Permutation(1, 0, 2, 3, 4, 5, 6);
        c = new Permutation(1, 2, 3, 5, 6, 4, 0);
        d = new Permutation(1, 4, 3, 2, 5, 6, 0);
        generators = new Permutation[]{a, b, c, d};
        group = new PermutationGroup(generators);
        Assert.assertEquals(group.getOrder().longValue(), 5040L);

        b = new Permutation(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new Permutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new Permutation(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new Permutation(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new Permutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 18);
        generators = new Permutation[]{a, b, c, d, e};
        group = new PermutationGroup(generators);
        Assert.assertEquals(group.getOrder().longValue(), 10080L);

        b = new Permutation(1, 2, 3, 4, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        a = new Permutation(1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        c = new Permutation(1, 2, 3, 5, 6, 4, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        d = new Permutation(1, 4, 3, 2, 5, 6, 0, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
        e = new Permutation(1, 0, 2, 3, 14, 5, 6, 7, 8, 9, 10, 11, 12, 13, 4, 15, 16, 17, 18, 19);
        generators = new Permutation[]{a, b, c, d, e};
        group = new PermutationGroup(generators);
        Assert.assertEquals(group.getOrder().longValue(), 5040L * 8L);
    }
}
