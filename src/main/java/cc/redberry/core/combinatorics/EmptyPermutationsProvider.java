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

import cc.redberry.core.utils.EmptyIterator;

import java.util.Iterator;

/**
 * This class represents <i>empty</i> {@link PermutationsProvider}. It is 
 * singleton.
 * 
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class EmptyPermutationsProvider implements PermutationsProvider {
    private static final Iterable<Permutation> EMPTY_ITERABLE = new Iterable<Permutation>() {
        @Override
        public Iterator<Permutation> iterator() {
            return EmptyIterator.INSTANCE;
        }
    };
    /**
     * Singleton instance of this class.
     */
    public static final EmptyPermutationsProvider INSTANCE = new EmptyPermutationsProvider();

    private EmptyPermutationsProvider() {
    }

    /**
     * Returns zero length array.
     * 
     * @return zero length array
     */
    @Override
    public PermutationsProvider[] getDisjointProviders() {
        return new PermutationsProvider[0];
    }

    /**
     * Returns {@link EmptyIterator} instance.
     * 
     * @return {@link EmptyIterator} instance
     */
    @Override
    public Iterable<Permutation> allPermutations() {
        return EMPTY_ITERABLE;
    }

    /**
     * Returns zero length array.
     * 
     * @return zero length array
     */
    @Override
    public int[] targetPositions() {
        return new int[0];
    }
}
