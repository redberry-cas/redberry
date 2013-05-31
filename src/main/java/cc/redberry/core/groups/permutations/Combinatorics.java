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

import cc.redberry.core.utils.BitArray;
import cc.redberry.core.utils.LongBackedBitArray;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Combinatorics {

    /**
     * Tests whether the specified array satisfies the one-line notation for permutations
     *
     * @param permutation array to be tested
     * @return {@code true} if specified array satisfies the one-line
     *         notation for permutations and {@code false} if not
     */
    public static boolean testPermutationCorrectness(int[] permutation) {
        int length = permutation.length;
        BitArray checked = new LongBackedBitArray(length);
        for (int i = 0; i < length; ++i) {
            if (permutation[i] >= length || permutation[i] < 0)
                return false;
            if (checked.get(permutation[i]))
                return false;
            checked.set(permutation[i]);
        }
        return checked.isFull();
    }

    private static final Permutation[] cachedIdentities = new Permutation[64];

    private static Permutation createIdentity(int length) {
        int[] array = new int[length];
        for (int i = 0; i < length; ++i)
            array[i] = i;
        return new Permutation(true, false, array);
    }

    public static Permutation getIdentity(int length) {
        if (cachedIdentities.length >= length)
            return createIdentity(length);
        if (cachedIdentities[length] == null)
            synchronized (cachedIdentities) {
                if (cachedIdentities[length] == null)
                    cachedIdentities[length] = createIdentity(length);
            }
        return cachedIdentities[length];
    }

}
