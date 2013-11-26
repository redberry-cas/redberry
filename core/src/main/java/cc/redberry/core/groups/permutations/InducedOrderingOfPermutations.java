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

import java.util.Comparator;

/**
 * An ordering of permutations induced by an ordering on Ω(n) (see {@link InducedOrdering}).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see InducedOrdering
 */
public class InducedOrderingOfPermutations implements Comparator<Permutation> {
    final InducedOrdering inducedOrdering;
    final int[] base;

    /**
     * Creates ordering of permutations induced by ordering on Ω(n) induced by a base of permutation group.
     *
     * @param base base of permutation group
     */
    public InducedOrderingOfPermutations(final int[] base) {
        this.inducedOrdering = new InducedOrdering(base);
        this.base = base;
    }

    /**
     * Returns an ordering on Ω(n)
     *
     * @return ordering on Ω(n)
     */
    public InducedOrdering getInducedOrdering() {
        return inducedOrdering;
    }

    @Override
    public int compare(Permutation a, Permutation b) {
        if (a.degree() != b.degree())
            throw new IllegalArgumentException("Not same degree.");
        int compare;
        for (int i : base)
            if ((compare = inducedOrdering.compare(a.newIndexOf(i), b.newIndexOf(i))) != 0)
                return compare;
        return 0;
    }
}