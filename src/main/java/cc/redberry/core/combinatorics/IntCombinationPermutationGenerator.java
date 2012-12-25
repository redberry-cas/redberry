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

import java.util.Iterator;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IntCombinationPermutationGenerator implements IntCombinatoricGenerator {
    private final int[] permutation, combination;
    final int[] combinationPermutation;
    private final IntPermutationsGenerator permutationsGenerator;
    private final IntCombinationsGenerator combinationsGenerator;
    private final int k;

    public IntCombinationPermutationGenerator(int n, int k) {
        this.k = k;
        this.combinationsGenerator = new IntCombinationsGenerator(n, k);
        this.combination = this.combinationsGenerator.combination;
        this.permutationsGenerator = new IntPermutationsGenerator(k);
        this.permutation = this.permutationsGenerator.permutation;
        this.combinationPermutation = new int[k];
        combinationsGenerator.next();
        System.arraycopy(combination, 0, combinationPermutation, 0, k);
    }

    @Override
    public boolean hasNext() {
        return combinationsGenerator.hasNext() || permutationsGenerator.hasNext();
    }

    @Override
    public int[] next() {
        if (!permutationsGenerator.hasNext()) {
            permutationsGenerator.reset();
            combinationsGenerator.next();
        }
        permutationsGenerator.next();
        for (int i = 0; i < k; ++i)
            combinationPermutation[i] = combination[permutation[i]];
        return combinationPermutation;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<int[]> iterator() {
        return this;
    }

    @Override
    public void reset() {
        permutationsGenerator.reset();
        combinationsGenerator.reset();
        combinationsGenerator.next();
    }

    @Override
    public int[] getReference() {
        return combinationPermutation;
    }
}
