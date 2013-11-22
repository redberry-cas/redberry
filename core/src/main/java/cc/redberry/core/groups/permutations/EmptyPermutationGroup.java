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

import cc.redberry.core.utils.SingleIterator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class EmptyPermutationGroup implements PermutationGroup {
    final int length;
    final Permutation identity;

    EmptyPermutationGroup(int length) {
        this.length = length;
        this.identity = Permutations.getIdentityOneLine(length);
    }

    @Override
    public BaseAndStrongGeneratingSet getBSGS() {
        return BaseAndStrongGeneratingSet.EMPTY;
    }

    @Override
    public List<Permutation> generators() {
        return Collections.singletonList(identity);
    }

    @Override
    public boolean isMember(Permutation permutation) {
        return permutation.isIdentity();
    }

    @Override
    public BigInteger order() {
        return BigInteger.ONE;
    }

    @Override
    public int degree() {
        return length;
    }

    @Override
    public int[] orbit(int point) {
        return new int[]{point};
    }

    @Override
    public int[] orbit(int[] point) {
        return point.clone();
    }

    @Override
    public int[][] orbits() {
        int[][] orbits = new int[length][];
        for (int i = 0; i < length; ++i)
            orbits[i] = new int[]{i};
        return orbits;
    }

    @Override
    public boolean isTransitive() {
        return false;
    }

    @Override
    public PermutationGroup setwiseStabilizer(int[] set) {
        return this;
    }

    @Override
    public PermutationGroup pointwiseStabilizer(int[] set) {
        return this;
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
        if (group.degree() != length)
            throw new IllegalArgumentException("Different lengths.");
        return group.order().equals(BigInteger.ONE);
    }

    @Override
    public Permutation[] rightCosetRepresentatives(PermutationGroup subgroup) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Permutation> iterator() {
        return new SingleIterator<Permutation>(identity);
    }
}
