/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.indices;

import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.PermutationOneLine;

import java.util.*;

/**
 * Representation of permutational symmetries of indices of simple tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IndicesSymmetries
        implements Iterable<Permutation> {

    static final IndicesSymmetries EMPTY_INDICES_SYMMETRIES
            = new IndicesSymmetries(StructureOfIndices.EMPTY, Collections.EMPTY_LIST, null);

    private final StructureOfIndices structureOfIndices;
    private final List<Permutation> generators;
    private PermutationGroup permutationGroup;

    private IndicesSymmetries(StructureOfIndices structureOfIndices) {
        this.structureOfIndices = structureOfIndices;
        this.generators = new ArrayList<>();
    }

    /*
     * For clone()
     */
    private IndicesSymmetries(StructureOfIndices structureOfIndices,
                              List<Permutation> generators, PermutationGroup permutationGroup) {
        this.structureOfIndices = structureOfIndices;
        this.generators = generators;
        this.permutationGroup = permutationGroup;
    }

    public PermutationGroup getPermutationGroup() {
        if (permutationGroup == null) {
            if (generators.isEmpty())
                permutationGroup = PermutationGroup.trivialGroup(structureOfIndices.size());
            else
                permutationGroup = new PermutationGroup(generators);
        }
        return permutationGroup;
    }

    public static IndicesSymmetries create(StructureOfIndices structureOfIndices) {
        if (structureOfIndices.size() == 0)
            return EMPTY_INDICES_SYMMETRIES;
        return new IndicesSymmetries(structureOfIndices);
    }

    public static IndicesSymmetries create(StructureOfIndices structureOfIndices, PermutationGroup group) {
        if (structureOfIndices.size() == 0)
            return EMPTY_INDICES_SYMMETRIES;
        return new IndicesSymmetries(structureOfIndices, group.generators(), group);
    }

    public static IndicesSymmetries create(StructureOfIndices structureOfIndices,
                                           List<Permutation> generators) {
        if (structureOfIndices.size() == 0)
            return EMPTY_INDICES_SYMMETRIES;
        return new IndicesSymmetries(structureOfIndices, new ArrayList<>(generators), null);
    }

    public StructureOfIndices getStructureOfIndices() {
        return structureOfIndices;
    }

    public List<Permutation> getGenerators() {
        return Collections.unmodifiableList(generators);
    }

    @Override
    public Iterator<Permutation> iterator() {
        return getPermutationGroup().iterator();
    }

    private short[] positionsInOrbits = null;

    public short[] getDiffIds() {
        if (positionsInOrbits == null) {
            final int[] positionsInOrbitsInt = getPermutationGroup().getPositionsInOrbits();
            positionsInOrbits = new short[positionsInOrbitsInt.length];
            for (int i = 0; i < positionsInOrbitsInt.length; ++i) {
                assert positionsInOrbitsInt[i] < Short.MAX_VALUE;
                positionsInOrbits[i] = (short) positionsInOrbitsInt[i];
            }
        }
        return positionsInOrbits;
    }

    public boolean addSymmetry(int... permutation) {
        return add(false, permutation);
    }

    public boolean addAntiSymmetry(int... permutation) {
        return add(true, permutation);
    }

    public boolean addSymmetry(IndexType type, int... permutation) {
        return add(type, false, permutation);
    }

    public boolean addAntiSymmetry(IndexType type, int... permutation) {
        return add(type, true, permutation);
    }

    public boolean add(IndexType type, boolean sign, int... permutation) {
        return add(type.getType(), new PermutationOneLine(sign, permutation));
    }

    public boolean add(byte type, boolean sign, int... permutation) {
        return add(type, new PermutationOneLine(sign, permutation));
    }

    public boolean add(boolean sign, int[] permutation) {
        byte type = -1;
        StructureOfIndices.TypeData typeData;
        for (int i = 0; i < IndexType.TYPES_COUNT; ++i) {
            typeData = structureOfIndices.getTypeData((byte) i);
            if (typeData.length != 0) {
                if (type != -1)
                    throw new IllegalArgumentException();
                if (typeData.length != permutation.length)
                    throw new IllegalArgumentException();
                type = (byte) i;
            }
        }
        return add(type, new PermutationOneLine(sign, permutation));
    }


    public boolean add(byte type, Permutation symmetry) {
        StructureOfIndices.TypeData data = structureOfIndices.getTypeData(type);
        if (data == null)
            throw new IllegalArgumentException("No such type: " + IndexType.getType(type));
        if (data.length != symmetry.degree())
            throw new IllegalArgumentException("Wrong symmetry length.");
        int[] s = new int[structureOfIndices.size()];
        int i = 0;
        for (; i < data.from; ++i)
            s[i] = i;
        for (int j = 0; j < data.length; ++j, ++i)
            s[i] = symmetry.newIndexOf(j) + data.from;
        for (; i < structureOfIndices.size(); ++i)
            s[i] = i;
        return generators.add(new PermutationOneLine(symmetry.antisymmetry(), s));
    }


    public boolean add(Permutation symmetry) {
        return generators.add(symmetry);
    }

    public boolean addAll(Collection<Permutation> symmetry) {
        return generators.addAll(symmetry);
    }

    public final boolean isTrivial() {
        for (Permutation p : generators)
            if (!p.isIdentity())
                return false;
        return true;
    }

    @Override
    public IndicesSymmetries clone() {
        if (structureOfIndices.size() == 0)
            return EMPTY_INDICES_SYMMETRIES;
        return new IndicesSymmetries(structureOfIndices, new ArrayList<>(generators), permutationGroup);
    }

    @Override
    public String toString() {
        return generators.toString();
    }
}
