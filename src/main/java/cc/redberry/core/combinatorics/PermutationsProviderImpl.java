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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Basic implementation of {@link PermutationsProvider}. Look there for complete
 * documentation.
 * 
 * @see PermutationsProvider
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PermutationsProviderImpl implements PermutationsProvider {
    private PermutationsProvider[] disjointProviders;
    private int[] totalPositions;
    private final Iterable<Permutation> iterable = new Iterable() {
        @Override
        public Iterator<Permutation> iterator() {
            return new TotalPermutationsIterator();
        }
    };

    /**
     * Constructs {@code PermutationsProviderImpl} from the specified array of 
     * disjoint providers.
     * 
     * @param disjointProviders disjoint providers
     */
    public PermutationsProviderImpl(PermutationsProvider... disjointProviders) {
        this(Arrays.asList(disjointProviders));
    }

    /**
     * Constructs {@code PermutationsProviderImpl} from the specified list of 
     * disjoint providers.
     * 
     * @param disjointProviders disjoint providers
     */
    public PermutationsProviderImpl(List<PermutationsProvider> disjointProviders) {
        List<PermutationsProvider> providers = new ArrayList<>();
        for (PermutationsProvider provider : disjointProviders)
            /*
            if (provider instanceof EmptyPermutationsProvider)
            continue;
            if (provider instanceof PermutationsProviderImpl) {
            providers.addAll(Arrays.asList(provider.getDisjointProviders()));
            continue;
            }*/
            //TODO consider revision after indices symmetry logic completion:
            providers.addAll(Arrays.asList(provider.getDisjointProviders()));
        this.disjointProviders = providers.toArray(new PermutationsProvider[providers.size()]);
    }

    /**
     * {@inheritDoc }
     * 
     * @return {@inheritDoc }
     */
    @Override
    public PermutationsProvider[] getDisjointProviders() {
        return disjointProviders;
    }

    /**
     * {@inheritDoc }
     * 
     * @return {@inheritDoc }
     */
    @Override
    public Iterable<Permutation> allPermutations() {
        return iterable;
    }

    /**
     * {@inheritDoc }
     * 
     * @return {@inheritDoc }
     */
    @Override
    public int[] targetPositions() {
        if (totalPositions == null) {
            int size = 0;
            for (PermutationsProvider provider : disjointProviders)
                size += provider.targetPositions().length;
            totalPositions = new int[size];
            int i = 0;
            int[] src;
            for (PermutationsProvider provider : disjointProviders) {
                src = provider.targetPositions();
                System.arraycopy(src, 0, totalPositions, i, src.length);
                i += src.length;
            }
        }
        return totalPositions;
    }

    private class TotalPermutationsIterator implements Iterator<Permutation> {
        private Iterator<Permutation>[] iterators;
        private Permutation[] permutations;
        private int[] permutation;
        private IntArrayPermutationWrapper permutationWrapper;
        private boolean finished = false;
        private int size;

        TotalPermutationsIterator() {
            size = disjointProviders.length;
            iterators = new Iterator[size];
            permutations = new Permutation[size];
            for (int i = 0; i < size; ++i) {
                iterators[i] = disjointProviders[i].allPermutations().iterator();
                permutations[i] = iterators[i].next();
            }
            int totalSize = 0;
            for (PermutationsProvider provider : disjointProviders)
                totalSize += provider.targetPositions().length;
            permutation = new int[totalSize];
            permutationWrapper = new IntArrayPermutationWrapper(permutation);
        }

        @Override
        public boolean hasNext() {
            //combinatorics[i - 1] = iterators[i - 1].next();
            return !finished;
        }

        @Override
        public Permutation next() {
            int shift = 0;
            int pSize;
            for (Permutation p : permutations) {
                pSize = p.dimension;
                for (int i = 0; i < pSize; ++i)
                    permutation[i + shift] = shift + p.newIndexOf(i);
                shift += pSize;
            }

            int indexNext = size - 1;
            for (int i = size - 1; i >= 0; --i)
                if (!iterators[i].hasNext()) {
                    iterators[i] = disjointProviders[i].allPermutations().iterator();
                    permutations[i] = iterators[i].next();
                    indexNext = i - 1;
                } else
                    break;
            if (!(finished = (indexNext == -1)))
                permutations[indexNext] = iterators[indexNext].next();
            permutationWrapper.arrayUpdated();
            return permutationWrapper;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
