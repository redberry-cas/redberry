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
 * This class represents an iterator over over all possible unique
 * combinations (with permutations, i.e. {0,1} and {1,0} both will appear in the iteration) of
 * {@code k} numbers, which can be chosen from the set of {@code n} numbers, numbered in the order
 * 0,1,2,...,{@code n}. The total number of such combinations will be {@code n!/(n-k)!}.</p>
 * <p/>
 * <p>For example, for {@code k=2} and {@code n=3}, it will iterate over
 * the following arrays: [0,1], [1,0], [0,2], [2,0], [1,2], [2,1].</p>
 * <p/>
 * <p>The iterator is implemented such that each next combination will be calculated only on
 * the invocation of method {@link #next()}.</p>
 * <br></br><b>Note:</b> method {@link #next()} returns the same reference on each invocation.
 * So, if it is needed not only to obtain the information from {@link #next()}, but also save the result,
 * it is necessary to clone the returned array.</p>
 * <p/>
 * <p>Inner implementation of this class is simply uses the combination of {@link IntCombinationsGenerator}
 * and {@link IntPermutationsGenerator}.</p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see IntCombinationsGenerator
 * @see IntPermutationsGenerator
 * @since 1.0
 */
public final class IntCombinationPermutationGenerator
        extends IntCombinatorialGenerator
        implements IntCombinatorialPort {
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
    public int[] take() {
        return hasNext() ? next() : null;
    }

    @Override
    public boolean hasNext() {
        return combinationsGenerator.hasNext() || permutationsGenerator.hasNext();
    }

    /**
     * Calculates and returns the next combination.
     *
     * @return the next combination
     */
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

    /**
     * @throws UnsupportedOperationException always
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
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
