/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.MathUtils;
import org.apache.commons.math3.random.RandomGenerator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

/**
 * This class provides factory and utility methods for combinatorics infrastructure.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class Combinatorics {

    private Combinatorics() {
    }

    /**
     * <p>Returns an {@link IntCombinatorialGenerator} object, which allows to iterate over
     * all possible unique combinations with permutations (i.e. {0,1} and {1,0} both appears for {@code k=2}) of
     * {@code k} numbers, which can be chosen from the set of {@code n} numbers, numbered in the order
     * 0,1,2,...,{@code n}. The total number of such combinations will be {@code n!/(n-k)!}.</p>
     * <p/>
     * <p>For example, for {@code k=2} and {@code n=3}, this method will produce an iterator over
     * the following arrays: [0,1], [1,0], [0,2], [2,0], [1,2], [2,1].</p>
     *
     * @param n number of elements in the set
     * @param k sample size
     * @return an iterator over all combinations (with permutations) to choose k numbers from n numbers.
     * @see IntCombinatorialGenerator
     */
    public static IntCombinatorialGenerator createIntGenerator(int n, int k) {
        if (n < k)
            throw new IllegalArgumentException();
        if (n == k)
            return new IntPermutationsGenerator(n);
        else
            return new IntCombinationPermutationGenerator(n, k);
    }
}
