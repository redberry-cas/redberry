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

import java.util.Collection;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class DummySymmetries implements Symmetries {

    protected final int dimension;

    DummySymmetries(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public boolean add(Symmetry symmetry) throws InconsistentGeneratorsException {
        if (symmetry.dimension() != dimension || symmetry.isAntiSymmetry())
            throw new IllegalArgumentException();
        return false;
    }

    @Override
    public boolean add(boolean sign, int... symmetry) throws InconsistentGeneratorsException {
        if (symmetry.length != dimension || sign)
            throw new IllegalArgumentException();
        return false;
    }

    @Override
    public boolean addAll(Symmetry... symmetries) throws InconsistentGeneratorsException {
        for (Symmetry symmetry : symmetries)
            if (symmetry.dimension() != dimension || symmetry.isAntiSymmetry())
                throw new IllegalArgumentException();
        return false;
    }

    @Override
    public boolean addAll(Collection<Symmetry> symmetries) throws InconsistentGeneratorsException {
        for (Symmetry symmetry : symmetries)
            if (symmetry.dimension() != dimension || symmetry.isAntiSymmetry())
                throw new IllegalArgumentException();
        return false;
    }

    @Override
    public boolean addAllUnsafe(Symmetry... symmetries) {
        return addAll(symmetries);
    }

    @Override
    public boolean addAllUnsafe(Collection<Symmetry> symmetries) {
        return addAll(symmetries);
    }

    @Override
    public boolean addAllUnsafe(Symmetries symmetries) {
        if (symmetries.dimension() != dimension)
            throw new IllegalArgumentException();
        return false;
    }

    @Override
    public boolean addUnsafe(Symmetry symmetry) {
        return add(symmetry);
    }

    @Override
    public Symmetries clone() {
        return this;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}