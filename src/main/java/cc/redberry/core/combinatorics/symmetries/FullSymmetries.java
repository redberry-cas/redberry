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
package cc.redberry.core.combinatorics.symmetries;

import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.PermutationsGenerator;
import cc.redberry.core.combinatorics.Symmetry;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class FullSymmetries extends DummySymmetries {

    FullSymmetries(int dimension) {
        super(dimension, Collections.unmodifiableList(
                Arrays.asList(
                new Symmetry(dimension),
                new Symmetry(Combinatorics.createTransposition(dimension), false),
                new Symmetry(Combinatorics.createCycle(dimension), false))));
    }

    @Override
    public List<Symmetry> getBasisSymmetries() {
        return basis;
    }

    @Override
    public Iterator<Symmetry> iterator() {
        return new PermutationsGenerator<>(dimension);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
