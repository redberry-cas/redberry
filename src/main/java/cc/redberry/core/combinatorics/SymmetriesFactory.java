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
import cc.redberry.core.utils.SingleIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetriesFactory {

    public static Symmetries createSymmetries(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException();
        if (dimension == 0)
            return EmptySymmetries0;
        if (dimension == 1)
            return EmptySymmetries1;
        return new SymmetriesImpl(dimension);
    }

    public static Symmetries createFullSymmetries(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException();
        if (dimension == 0)
            return EmptySymmetries0;
        if (dimension == 1)
            return EmptySymmetries1;
        return new FullSymmetries(dimension);
    }
    static DummySymmetries EmptySymmetries0 = new DummySymmetries(0) {

        @Override
        public List<Symmetry> getBaseSymmetries() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Iterator<Symmetry> iterator() {
            return EmptyIterator.INSTANCE;
        }
    };
    private static List<Symmetry> emptySymmetryContainer;

    static {
        List<Symmetry> temp = new ArrayList<>(1);
        temp.add(new Symmetry(1));
        emptySymmetryContainer = Collections.unmodifiableList(temp);
    }
    static DummySymmetries EmptySymmetries1 = new DummySymmetries(0) {

        @Override
        public List<Symmetry> getBaseSymmetries() {
            return emptySymmetryContainer;
        }

        @Override
        public Iterator<Symmetry> iterator() {
            return new SingleIterator<>(emptySymmetryContainer.get(0));
        }
    };
}
