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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsUtils {

    private PermutationsUtils() {
    }

    /**
     * Throws exception if p.length() != size.
     *
     * @param p    permutation
     * @param size size
     */
    public static void checkSizeWithException(Permutation p, int size) {
        if (p.length() != size)
            throw new IllegalArgumentException("Different size of permutation.");
    }

    /**
     * Throws exception if a != size.
     *
     * @param a
     * @param size size
     */
    public static void checkSizeWithException(int a, int size) {
        if (a != size)
            throw new IllegalArgumentException("Different size of permutation.");
    }
}
