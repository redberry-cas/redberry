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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PermutationGroupImpl implements PermutationGroup {
    final BSGS _BSGS;
    final int length;

    PermutationGroupImpl(BSGS BSGS) {
        _BSGS = BSGS;
        length = BSGS.BSGSList.get(0).stabilizerGenerators.get(0).length();
    }

    public BSGS getBSGS() {
        return _BSGS;
    }

    @Override
    public List<Permutation> generators() {
        return _BSGS.BSGSList.get(0).stabilizerGenerators;
    }

    @Override
    public boolean isMember(Permutation permutation) {
        return _BSGS.isMember(permutation);
    }

    @Override
    public BigInteger order() {
        return _BSGS.order();
    }

    @Override
    public int dimension() {
        return length;
    }

    @Override
    public int[] orbit(int point) {
        return new int[0];
    }

    @Override
    public int[] orbit(int[] point) {
        return new int[0];
    }

    @Override
    public int[][] orbits() {
        return new int[0][];
    }

    @Override
    public boolean isTransitive() {
        return false;
    }

    @Override
    public PermutationGroup setwiseStabilizer(int[] set) {
        return null;
    }

    @Override
    public PermutationGroup pointwiseStabilizer(int[] set) {
        return null;
    }

    @Override
    public PermutationGroup setwiseMapping(int[] a, int[] b) {
        return null;
    }

    @Override
    public PermutationGroup pointwiseMapping(int[] a, int[] b) {
        return null;
    }

    @Override
    public boolean isSubgroup(PermutationGroup group) {
        return false;
    }

    @Override
    public Permutation[] rightCosetRepresentatives(PermutationGroup subgroup) {
        return new Permutation[0];
    }

    @Override
    public Iterator<Permutation> iterator() {
        return _BSGS.iterator();
    }
}
