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
import cc.redberry.core.combinatorics.Symmetry;

/**
 * This class provides static factory methods to create {@link Symmetries} objects.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Symmetries
 */
public final class SymmetriesFactory {

    /**
     * Empty symmetries with zero dimension.
     */
    private final static Symmetries EmptySymmetries0 = new EmptySymmetries(0);
    /**
     * Empty symmetries with dimension 1.
     */
    private final static Symmetries EmptySymmetries1 = new EmptySymmetries(1);

    private SymmetriesFactory() {
    }

    /**
     * Creates empty {@link Symmetries} object (i.e. contained only identity symmetry)
     * with specified dimension.
     *
     * @param dimension dimension of symmetries
     * @return {@code Symmetries} object
     */
    public static Symmetries createSymmetries(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException();
        if (dimension == 0)
            return EmptySymmetries0;
        if (dimension == 1)
            return EmptySymmetries1;
        return new SymmetriesImpl(dimension);
    }

    /**
     * Creates {@link Symmetries} object with specified dimension, which contains one transposition and one cycle.
     * Other words, it represents a full symmetric group with specified dimension, so, for example, all
     * permutations of specified dimension can be obtained via its
     * {@link cc.redberry.core.combinatorics.symmetries.Symmetries#iterator()}  method.
     *
     * @param dimension dimension
     * @return {@code Symmetries} object with one transposition and one cycle
     */
    public static Symmetries createFullSymmetries(int dimension) {
        if (dimension < 0)
            throw new IllegalArgumentException();
        if (dimension == 0)
            return EmptySymmetries0;
        if (dimension == 1)
            return EmptySymmetries1;
        return new FullSymmetries(dimension);
    }

    /**
     * Returns a symmetry object, which represents a Cartesian product of two symmetric groups
     * with dimensions {@code upperCount} and {@code lowerCount} respectively. This object contains symmetries
     * of dimension {@code upperCount + lowerCount} and contains one transposition and one cycle of the first
     * block of {@code upperCount} elements, and one transposition and one cycle of the second
     * block of {@code lowerCount} elements
     *
     * @param upperCount dimension of the first subgroup
     * @param lowerCount dimension of the second subgroup
     * @return {@code Symmetries} object which represents a Cartesian product of two symmetric groups
     */
    public static Symmetries createFullSymmetries(int upperCount, int lowerCount) {
        if (upperCount < 0 || upperCount < 0)
            throw new IllegalArgumentException();
        if (upperCount + lowerCount <= 1)
            return SymmetriesFactory.createSymmetries(upperCount + lowerCount);

        SymmetriesImpl symmetries = new SymmetriesImpl(upperCount + lowerCount);

        //TODO refactor
        //transposition
        int i;
        if (upperCount > 1) {
            int[] upperTransposition = Combinatorics.createIdentity(upperCount + lowerCount);
            upperTransposition[0] = 1;
            upperTransposition[1] = 0;
            Symmetry upperTranspositionSymmetry = new Symmetry(upperTransposition, false);
            symmetries.addUnsafe(upperTranspositionSymmetry);
        }

        if (lowerCount > 1) {
            int[] lowerTransposition = Combinatorics.createIdentity(upperCount + lowerCount);
            lowerTransposition[upperCount] = 1 + upperCount;
            lowerTransposition[upperCount + 1] = upperCount;
            Symmetry lowerTranspositionSymmetry = new Symmetry(lowerTransposition, false);
            symmetries.addUnsafe(lowerTranspositionSymmetry);
        }

        //cycle        
        if (upperCount > 2) {
            int[] upperCycle = new int[upperCount + lowerCount];
            upperCycle[0] = upperCount - 1;
            for (i = 1; i < upperCount; ++i)
                upperCycle[i] = i - 1;
            for (; i < upperCount + lowerCount; ++i)
                upperCycle[i] = i;
            Symmetry upperCycleSymmetry = new Symmetry(upperCycle, false);
            symmetries.addUnsafe(upperCycleSymmetry);
        }
        if (lowerCount > 2) {
            int[] lowerCycle = new int[upperCount + lowerCount];
            for (i = 0; i < upperCount; ++i)
                lowerCycle[i] = i;
            lowerCycle[upperCount] = upperCount + lowerCount - 1;
            ++i;
            for (; i < upperCount + lowerCount; ++i)
                lowerCycle[i] = i - 1;
            Symmetry lowerCycleSymmetry = new Symmetry(lowerCycle, false);
            symmetries.addUnsafe(lowerCycleSymmetry);
        }

        return symmetries;
    }
}
