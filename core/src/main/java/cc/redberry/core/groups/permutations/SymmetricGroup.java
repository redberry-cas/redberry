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

import cc.redberry.core.number.NumberUtils;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import static cc.redberry.core.groups.permutations.PermutationsUtils.checkSizeWithException;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SymmetricGroup implements PermutationGroup {
    final int length;
    final int[] orbit;

    public SymmetricGroup(final int length) {
        this.length = length;
        orbit = new int[length];
        for (int i = 0; i < length; ++i)
            orbit[i] = i;
    }

    @Override
    public BSGS getBSGS() {
        return null;
    }

    @Override
    public List<Permutation> generators() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isMember(Permutation permutation) {
        checkSizeWithException(permutation, length);
        return permutation.isSign();
    }

    @Override
    public BigInteger order() {
        return NumberUtils.factorial(length);
    }

    @Override
    public int degree() {
        return length;
    }

    @Override
    public int[] orbit(int point) {
        return orbit;
    }

    @Override
    public int[] orbit(int[] point) {
        return orbit;
    }

    @Override
    public int[][] orbits() {
        return new int[][]{orbit};
    }

    @Override
    public boolean isTransitive() {
        return true;
    }

    @Override
    public PermutationGroup setwiseStabilizer(int[] set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermutationGroup pointwiseStabilizer(int[] set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermutationGroup setwiseMapping(int[] a, int[] b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PermutationGroup pointwiseMapping(int[] a, int[] b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSubgroup(PermutationGroup group) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Permutation[] rightCosetRepresentatives(PermutationGroup subgroup) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Permutation> iterator() {
        throw new UnsupportedOperationException();
    }
}
