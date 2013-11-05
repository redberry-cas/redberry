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
package cc.redberry.groovy

import cc.redberry.core.combinatorics.Combinatorics
import cc.redberry.core.groups.permutations.Permutation
import cc.redberry.core.utils.IntArrayList
import org.junit.Test

import static cc.redberry.core.combinatorics.Combinatorics.createIdentity

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class MyTest {
    public static int[] orbit(int[][] permutations, int point) {
        IntArrayList orbit = new IntArrayList();
        orbit.add(point);

        for (int i = 0; i < orbit.size(); ++i) {
            for (int[] permutation : permutations) {
                int n = new Permutation(permutation).newIndexOf(orbit.get(i));
                if (!orbit.contains(n))
                    orbit.add(n);
            }
        }
        int[] r = orbit.toArray();
        Arrays.sort(r);
        return r;
    }

    @Test
    public void testOrbit() {
        int[][] p = [[0, 1, 3, 2, 4, 5], [2, 0, 3, 1, 5, 4]]
        System.out.println(Arrays.toString(orbit(p, 0)));
        System.out.println(Arrays.toString(orbit(p, 4)));
    }

    public static orbit_stabilizer(Permutation[] permutations, int point) {
        def del = [[point, permutations[0].getIdentity()]]
        def Y = []

        for (int i = 0; i < del.size(); ++i) {
            def c = del[i]
            for (p in permutations) {

                if (!del.collect({ it[0] }).contains(p.newIndexOf(del[i][0])))
                    del << [p.newIndexOf(del[i][0]), del[i][1].composition(p)]
                else
                    Y <<
            }
        }
        return del;
    }

    @Test
    public void os() {
        Permutation[] p = [new Permutation([0, 1, 3, 2, 4, 5] as int[]), new Permutation([2, 0, 3, 1, 5, 4] as int[])]
        println orbit_stabilizer(p, 0)
        println orbit_stabilizer(p, 4)
    }
}
