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
 * This class implements {@link PermutationsProvider} and represents 
 * <i>single</i> disjoint provider. In fact, it is a simple wrapper of 
 * {@link PermutationsGenerator}. 
 * 
 * @see PermutationsProvider
 * @see PermutationsGenerator
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimplePermutationProvider implements PermutationsProvider {
    private int[] targetPositions;

    /**
     * Constructs {@code SimplePermutationProvider} with target positions 
     * specified from {@code from} (inclusive) to {@code to} (exclusive).
     * 
     * @param from first target positions
     * @param to after last target position 
     */
    public SimplePermutationProvider(int from, int to) {
        if (from >= to)
            throw new IllegalArgumentException();
        targetPositions = new int[to - from];
        for (int i = from; i < to; ++i)
            targetPositions[i - from] = i;
    }

    /**
     * Returns array with single {@code this} element.
     * 
     * @return array with single {@code this} element
     */
    @Override
    public PermutationsProvider[] getDisjointProviders() {
        return new PermutationsProvider[]{this};
    }

    /**
     * Returns new {@code Iterable<Permutation>} witch returns new 
     * {@code PermutationsGenerator(targetPositions.length)} as its iterator.
     * 
     * @return new {@code Iterable<Permutation>} witch returns new 
     * {@code PermutationsGenerator(targetPositions.length)} as its iterator
     */
    @Override
    public Iterable<Permutation> allPermutations() {
        return new Iterable<Permutation>() {
            @Override
            public Iterator<Permutation> iterator() {
                return new PermutationsGenerator(targetPositions.length);
            }
        };
    }

    /**
     * {@inheritDoc }
     * 
     * @return {@inheritDoc }
     */
    @Override
    public int[] targetPositions() {
        return targetPositions;
    }
}
