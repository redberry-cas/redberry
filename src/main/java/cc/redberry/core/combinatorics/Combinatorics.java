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

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Combinatorics {

    private Combinatorics() {
    }

    public static IntCombinatoricGenerator createIntGenerator(int n, int k) {
        if (n < k)
            throw new IllegalArgumentException();
        if (n == k)
            return new IntPermutationsGenerator(n);
        else
            return new IntCombinationPermutationGenerator(n, k);
    }

    public static boolean isIdentity(final int[] permutation) {
        for (int i = 0; i < permutation.length; ++i)
            if (permutation[i] != i)
                return false;
        return true;

    }

    public static boolean isIdentity(Permutation permutation) {
        return isIdentity(permutation.permutation);
    }

    public static boolean isIdentity(Symmetry symmetry) {
        return !symmetry.isAntiSymmetry() && isIdentity(symmetry.permutation);
    }

    public static int[] createIdentity(final int dimension) {
        int[] perm = new int[dimension];
        for (int i = 0; i < dimension; ++i)
            perm[i] = i;
        return perm;
    }
}
