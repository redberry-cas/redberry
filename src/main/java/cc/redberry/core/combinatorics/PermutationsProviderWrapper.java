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
 * This class wrapping {@link PermutationsProvider} to iterate directly over 
 * target positions, but not over their positions in {@code targetPositions} array.
 * 
 * <p>Example:
 *  <blockquote><pre>
 *       SimplePermutationProvider a = new SimplePermutationProvider(1, 4);
 *       SimplePermutationProvider b = new SimplePermutationProvider(5, 7);
 *       PermutationsProvider provider = new PermutationsProviderImpl(a, b);
 *       PermutationsProviderWrapper providerWrapper = new PermutationsProviderWrapper(7, provider);
 *       for(Permutation p: providerWrapper)
 *            System.out.println(p);
 *  </blockquote></pre>
 * <p>The result will be
 * <blockquote><pre>
 *  [0, 1, 2, 3, 4, 5, 6]
 *  [0, 1, 2, 3, 4, 6, 5]
 *  [0, 1, 3, 2, 4, 5, 6]
 *  [0, 1, 3, 2, 4, 6, 5]
 *  [0, 2, 1, 3, 4, 5, 6]
 *  [0, 2, 1, 3, 4, 6, 5]
 *  [0, 2, 3, 1, 4, 5, 6]
 *  [0, 2, 3, 1, 4, 6, 5]
 *  [0, 3, 1, 2, 4, 5, 6]
 *  [0, 3, 1, 2, 4, 6, 5]
 *  [0, 3, 2, 1, 4, 5, 6]
 *  [0, 3, 2, 1, 4, 6, 5]
 *  </blockquote></pre>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsProviderWrapper implements Iterable<Permutation> {
    private final int size;
    private final PermutationsProvider provider;

    /**
     * Constructs {@code PermutationsProviderWrapper}, witch iterates over 
     * array with specified {@code size}, using specified 
     * {@code PermutationsProvider}
     * 
     * @param size size of the array, witch is permuting by specified provider
     * @param provider combinatorics provider
     */
    public PermutationsProviderWrapper(int size, PermutationsProvider provider) {
        if (size < 0)
            throw new IllegalArgumentException();
        if (provider == null)
            throw new NullPointerException();
        this.size = size;
        this.provider = provider;
        for (int i : provider.targetPositions())
            if (i >= size)
                throw new IllegalArgumentException();
    }

    /**
     * Returns {@link PermutationsProvider#targetPositions() }.
     * 
     * @return {@link PermutationsProvider#targetPositions() }
     */
    public int[] targetPositions() {
        return provider.targetPositions();
    }

    /**
     * Returns iterator over array of target positions.
     * 
     * @return iterator over array of target positions.
     */
    @Override
    public Iterator<Permutation> iterator() {
        return new PermutationsIterator(provider.allPermutations().iterator());
    }

    private class PermutationsIterator implements Iterator<Permutation> {
        private Iterator<Permutation> iterator;

        PermutationsIterator(Iterator<Permutation> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        private Permutation convert(Permutation p) {
            int[] array = new int[size];
            for (int i = 0; i < size; ++i)
                array[i] = i;
            for (int i = 0; i < p.dimension; ++i)
                array[provider.targetPositions()[i]] = provider.targetPositions()[p.permutation[i]];
            return new Permutation(array, true);
        }

        @Override
        public Permutation next() {
            return convert(iterator.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
