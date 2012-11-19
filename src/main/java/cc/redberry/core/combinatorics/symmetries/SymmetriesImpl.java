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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.combinatorics.symmetries;

import cc.redberry.core.combinatorics.InconsistentGeneratorsException;
import cc.redberry.core.combinatorics.PermutationsSpanIterator;
import cc.redberry.core.combinatorics.Symmetry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class SymmetriesImpl extends AbstractSymmetries {

    SymmetriesImpl(int dimension) {
        super(dimension, new ArrayList<Symmetry>());
        this.basis.add(new Symmetry(dimension));
    }

    SymmetriesImpl(int dimension, List<Symmetry> basis) {
        super(dimension, basis);
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public boolean add(Symmetry symmetry)
            throws InconsistentGeneratorsException {
        if (symmetry.dimension() != dimension)
            throw new IllegalArgumentException();
        PermutationsSpanIterator<Symmetry> it = new PermutationsSpanIterator<>(basis);
        //BOTTLENECK review
        while (it.hasNext()) {
            Symmetry s = it.next();
            if (s.equals(symmetry))
                return false;
        }
        basis.add(symmetry);
        //BOTTLENECK
        //checking consistense
        it = new PermutationsSpanIterator<>(basis);
        while (it.hasNext())
            it.next();
        return true;
    }

    @Override
    public boolean addUnsafe(Symmetry symmetry) {
        basis.add(symmetry);
        return true;
    }

    /**
     * Returns iterator over basis symmetries (which are contains in this set)
     * and all possible symmetries, which can be obtained by composing the basis
     * symmetries, i.e. it works as {@link PermutationsSpanIterator}.
     *
     * @return iterator over basis symmetries (which are contains in this set)
     *         and all possible symmetries, which can be obtained by composing
     *         the basis symmetries, i.e. it works as
     *         {@code PermutationsSpanIterator}
     *
     * @see PermutationsSpanIterator
     */
    @Override
    public Iterator<Symmetry> iterator() {
        return new PermutationsSpanIterator<>(basis);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public List<Symmetry> getBasisSymmetries() {
        return Collections.unmodifiableList(basis);
    }

    @Override
    public SymmetriesImpl clone() {
        return new SymmetriesImpl(dimension, new ArrayList<>(basis));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Symmetry s : basis)
            sb.append(s.toString()).append("\n");
        return sb.toString();
    }
}
