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

import java.math.BigInteger;

/**
 * Implementation of {@link Permutation} based on cycle notation.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Permutation
 * @see PermutationOneLine
 */
public class PermutationCycles implements Permutation {

    @Override
    public int[] imageOf(int[] set) {
        return new int[0];
    }

    @Override
    public int newIndexOf(int i) {
        return 0;
    }

    @Override
    public int newIndexOfUnderInverse(int i) {
        return 0;
    }

    @Override
    public boolean antisymmetry() {
        return false;
    }

    @Override
    public PermutationCycles composition(Permutation other) {
        return null;
    }

    @Override
    public PermutationCycles compositionWithInverse(Permutation other) {
        return null;
    }

    @Override
    public PermutationCycles inverse() {
        return null;
    }

    @Override
    public boolean isIdentity() {
        return false;
    }

    @Override
    public PermutationCycles getIdentity() {
        return null;
    }

    @Override
    public int[] permute(int[] array) {
        return new int[0];
    }

    @Override
    public BigInteger order() {
        return null;
    }

    @Override
    public boolean orderIsOdd() {
        return false;
    }

    @Override
    public int degree() {
        return 0;
    }

    @Override
    public PermutationCycles pow(int exponent) {
        return null;
    }

    @Override
    public int compareTo(Permutation o) {
        return 0;
    }

    @Override
    public int parity() {
        return 0;
    }

    @Override
    public Permutation extendBefore(int newDegree) {
        return null;
    }

    @Override
    public Permutation extendAfter(int newDegree) {
        return null;
    }
}
