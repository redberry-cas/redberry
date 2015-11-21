/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.groups.permutations.Permutations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper of {@link cc.redberry.core.groups.permutations.PermutationGroup} that holds symmetries of indices. This
 * class aggregates symmetries added via methods {@code add(...)} and calculate permutation group generated by these
 * symmetries on the invocation of methods {@code getPermutationGroup()}, {@code getPositionsInOrbits()} etc. After
 * permutation group was calculated the modification of symmetries is forbidden: all methods {@code add(...)} will
 * throw {@code IllegalStateException}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IndicesSymmetries {
    /**
     * Structure of indices
     */
    private final StructureOfIndices structureOfIndices;
    /**
     * Aggregated generators
     */
    private final List<Permutation> generators;
    /**
     * Permutation group
     */
    private PermutationGroup permutationGroup;

    /**
     * Crates symmetries with empty generating set
     *
     * @param structureOfIndices structure of indices
     */
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

    /**
     * Creates symmetries with empty generating set.
     *
     * @param structureOfIndices structure of indices for which this symmetries can be assigned
     * @return symmetries with empty generating set for indices with specified structure
     */
    public static IndicesSymmetries create(StructureOfIndices structureOfIndices) {
        if (structureOfIndices.size() == 0)
            return getEmpty();
        return new IndicesSymmetries(structureOfIndices);
    }

    /**
     * Creates symmetries of indices with specified structure and with specified permutation group.
     *
     * @param structureOfIndices structure of indices for which this symmetries can be assigned
     * @param group              permutation group
     * @return symmetries with specified permutation group for indices with specified structure
     * @throws java.lang.IllegalArgumentException if {@code group.degree() != structureOfIndices.size()}
     */
    public static IndicesSymmetries create(StructureOfIndices structureOfIndices, PermutationGroup group) {
        if (group.degree() != structureOfIndices.size())
            throw new IllegalArgumentException("Degree of permutation group not equal to indices size.");
        if (structureOfIndices.size() == 0)
            return getEmpty();
        return new IndicesSymmetries(structureOfIndices, group.generators(), group);
    }

    /**
     * Creates symmetries of indices with specified structure and with specified generating set.
     *
     * @param structureOfIndices structure of indices for which this symmetries can be assigned
     * @param generators         generating set
     * @return symmetries with specified generating set for indices with specified structure
     * @throws java.lang.IllegalArgumentException if some generator in {@code generators} have degree different from
     *                                            size of {@code structureOfIndices}
     */
    public static IndicesSymmetries create(StructureOfIndices structureOfIndices,
                                           List<Permutation> generators) {
        for (Permutation p : generators)
            if (p.degree() > structureOfIndices.size())
                throw new IllegalArgumentException("Permutation degree not equal to indices size.");
        if (structureOfIndices.size() == 0)
            return getEmpty();
        return new IndicesSymmetries(structureOfIndices, new ArrayList<>(generators), null);
    }

    /**
     * Returns underlying structure of indices.
     *
     * @return structure of indices
     */
    public StructureOfIndices getStructureOfIndices() {
        return structureOfIndices;
    }

    /**
     * Returns generating set (unmodifiable) of this symmetries.
     *
     * @return generating set (unmodifiable) of this symmetries
     */
    public List<Permutation> getGenerators() {
        return Collections.unmodifiableList(generators);
    }

    /**
     * Returns a permutation group that represents symmetries of this indices. The invocation of this method makes this
     * instance unmodifiable, i.e. invocation of method {@code add(...)} after invocation of this method will cause exception.
     *
     * @return permutation group that represents symmetries of this indices
     */
    public PermutationGroup getPermutationGroup() {
        if (permutationGroup == null) {
            if (generators.isEmpty())
                permutationGroup = PermutationGroup.trivialGroup();
            else
                permutationGroup = PermutationGroup.createPermutationGroup(generators);
        }
        return permutationGroup;
    }

    private short[] positionsInOrbits = null;

    /**
     * Returns positions of indices in the array of orbits of underlying permutation group. Specifically, this method
     * returns {@link cc.redberry.core.groups.permutations.PermutationGroup#getPositionsInOrbits()} converted to
     * {@code short[]}. This method returns same reference each time; so, if one will use the returned array, do not
     * forget to clone it, The invocation of this method makes this instance unmodifiable, i.e. invocation of method
     * {@code add(...)} after invocation of this method will cause exception.
     *
     * @return positions of indices in the array of orbits of underlying permutation group
     * @see cc.redberry.core.groups.permutations.PermutationGroup#getPositionsInOrbits()
     */
    public short[] getPositionsInOrbits() {
        if (positionsInOrbits == null) {
            final int[] positionsInOrbitsInt = getPermutationGroup().getPositionsInOrbits();
            positionsInOrbits = new short[structureOfIndices.size()];
            int i = 0;
            for (; i < positionsInOrbitsInt.length; ++i) {
                assert positionsInOrbitsInt[i] < Short.MAX_VALUE;
                positionsInOrbits[i] = (short) positionsInOrbitsInt[i];
            }
            for (; i < structureOfIndices.size(); ++i)
                positionsInOrbits[i] = (short) i;
        }
        return positionsInOrbits;
    }

    /**
     * Returns {@code true} if all generators are identity (and, consequently, the underlying permutation group is trivial)
     * and false otherwise.
     *
     * @return {@code true} if all generators are identity
     */
    public final boolean isTrivial() {
        for (Permutation p : generators)
            if (!p.isIdentity())
                return false;
        return true;
    }

    /**
     * Returns {@code true} if one can add new symmetries to this, or {@code false}, if this symmetries is already in
     * use in some tensors; in latter case, the invocation of method {@code add(...)} will throw exception.
     *
     * @return {@code true} if one can add new symmetries to this, or {@code false}, if this symmetries is already in
     * use in some tensors; in latter case, the invocation of method {@code add(...)} will throw exception.
     */
    public boolean availableForModification() {
        return permutationGroup == null;
    }

    /**
     * Adds specified permutation written in one-line notation to this symmetries.
     *
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addSymmetry(int... permutation) {
        add(false, permutation);
    }

    /**
     * Adds specified antisymmetry written in one-line notation to this symmetries.
     *
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addAntiSymmetry(int... permutation) {
        add(true, permutation);
    }

    /**
     * Adds specified symmetry written in one-line notation to this symmetries.
     *
     * @param sign        {@code true} for antisymmetry, {@code false} for symmetry
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException when inputting antisymmetry with odd order
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void add(boolean sign, int[] permutation) {
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
        addSymmetry(type, Permutations.createPermutation(sign, permutation));
    }

    /**
     * Adds specified permutation written in one-line notation to symmetries of indices of specified type.
     *
     * @param type        type of indices
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if length of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public void addSymmetry(IndexType type, int... permutation) {
        add(type, false, permutation);
    }

    /**
     * Adds specified antisymmetry written in one-line notation to symmetries of indices of specified type.
     *
     * @param type        type of indices
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException if order of specified permutation is odd
     * @throws java.lang.IllegalArgumentException if length of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public void addAntiSymmetry(IndexType type, int... permutation) {
        add(type, true, permutation);
    }

    /**
     * Adds specified symmetry written in one-line notation to symmetries of indices of specified type.
     *
     * @param type        type of indices
     * @param sign        {@code true} for antisymmetry, {@code false} for symmetry
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException when inputting antisymmetry with odd order
     * @throws java.lang.IllegalArgumentException if length of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public void add(IndexType type, boolean sign, int... permutation) {
        addSymmetry(type.getType(), Permutations.createPermutation(sign, permutation));
    }

    /**
     * Adds specified symmetry written in one-line notation to symmetries of indices of specified type.
     *
     * @param type        type of indices
     * @param sign        {@code true} for antisymmetry, {@code false} for symmetry
     * @param permutation permutation written in one-line notation
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if specified permutation is inconsistent with one-line notation
     * @throws java.lang.IllegalArgumentException when inputting antisymmetry with odd order
     * @throws java.lang.IllegalArgumentException if length of specified permutation differs from the size of indices
     *                                            of specified type
     */
    public void add(byte type, boolean sign, int... permutation) {
        addSymmetry(type, Permutations.createPermutation(sign, permutation));
    }

    /**
     * Adds specified symmetry to symmetries of indices of specified type.
     *
     * @param type     type of indices
     * @param symmetry symmetry
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addSymmetry(byte type, Permutation symmetry) {
        if (symmetry.isIdentity())
            return;
        if (permutationGroup != null)
            throw new IllegalStateException("Permutation group is already in use.");

        StructureOfIndices.TypeData data = structureOfIndices.getTypeData(type);
        if (data == null)
            throw new IllegalArgumentException("No such type: " + IndexType.getType(type));
        if (data.length < symmetry.degree())
            throw new IllegalArgumentException("Wrong symmetry length.");
        int[] s = new int[structureOfIndices.size()];
        int i = 0;
        for (; i < data.from; ++i)
            s[i] = i;
        for (int j = 0; j < data.length; ++j, ++i)
            s[i] = symmetry.newIndexOf(j) + data.from;
        for (; i < structureOfIndices.size(); ++i)
            s[i] = i;
        generators.add(Permutations.createPermutation(symmetry.antisymmetry(), s));
    }


    /**
     * Adds specified symmetry to this.
     *
     * @param symmetry symmetry
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addSymmetry(Permutation symmetry) {
        if (permutationGroup != null)
            throw new IllegalStateException("Permutation group is already in use.");
        generators.add(symmetry);
    }

    /**
     * Adds specified symmetries to this.
     *
     * @param symmetries symmetries
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addSymmetries(Permutation... symmetries) {
        if (permutationGroup != null)
            throw new IllegalStateException("Permutation group is already in use.");
        for (Permutation symmetry : symmetries)
            generators.add(symmetry);
    }

    /**
     * Adds specified symmetries to this.
     *
     * @param symmetries symmetries
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addSymmetries(Collection<Permutation> symmetries) {
        if (permutationGroup != null)
            throw new IllegalStateException("Permutation group is already in use.");
        for (Permutation symmetry : symmetries)
            generators.add(symmetry);
    }

    /**
     * Adds specified symmetries to this.
     *
     * @param symmetry symmetry
     * @throws java.lang.IllegalStateException    if this instance of symmetries is already in use (permutation group
     *                                            calculated)
     * @throws java.lang.IllegalArgumentException if this structure of indices is inconsistent with specified
     *                                            permutation (permutation mixes indices of different types)
     */
    public void addAll(Collection<Permutation> symmetry) {
        if (permutationGroup != null)
            throw new IllegalArgumentException();
        generators.addAll(symmetry);
    }

    /**
     * Makes this {@code symmetries} totally symmetric in all its indices (for each type). The invocation of this
     * method makes this instance unmodifiable, i.e. invocation of method {@code add(...)} after invocation of this
     * method will cause exception.
     *
     * @throws java.lang.IllegalStateException if this instance of symmetries is already in use (permutation group
     *                                         calculated)
     */
    public void setSymmetric() {
        if (permutationGroup != null)
            throw new IllegalStateException("Permutation group is already in use.");
        PermutationGroup sym = null;
        int[] counts = structureOfIndices.getTypesCounts();
        for (int c : counts) {
            if (c != 0)
                if (sym == null)
                    sym = PermutationGroup.symmetricGroup(c);
                else
                    sym = sym.directProduct(PermutationGroup.symmetricGroup(c));

        }

        permutationGroup = sym;
        generators.clear();
        generators.addAll(sym.generators());
    }

    /**
     * Makes this {@code symmetries} totally antisymmetric in all its indices (for each type). The invocation of this
     * method makes this instance unmodifiable, i.e. invocation of method {@code add(...)} after invocation of this
     * method will cause exception.
     *
     * @throws java.lang.IllegalStateException if this instance of symmetries is already in use (permutation group
     *                                         calculated)
     */
    public void setAntiSymmetric() {
        if (permutationGroup != null)
            throw new IllegalStateException("Permutation group is already in use.");
        PermutationGroup sym = null;
        int[] counts = structureOfIndices.getTypesCounts();
        for (int c : counts) {
            if (c != 0) {
                if (sym == null)
                    sym = PermutationGroup.antisymmetricGroup(c);
                else
                    sym = sym.directProduct(PermutationGroup.antisymmetricGroup(c));
            }

        }

        permutationGroup = sym;
        generators.clear();
        generators.addAll(sym.generators());
    }

    @Override
    public IndicesSymmetries clone() {
        if (structureOfIndices.size() == 0)
            return getEmpty();
        return new IndicesSymmetries(structureOfIndices, new ArrayList<>(generators), permutationGroup);
    }

    /**
     * For indices with zero length
     */
    private static IndicesSymmetries EMPTY_INDICES_SYMMETRIES;

    /**
     * For indices with zero length
     */
    public static IndicesSymmetries getEmpty() {
        if (EMPTY_INDICES_SYMMETRIES == null)
            EMPTY_INDICES_SYMMETRIES = new IndicesSymmetries(StructureOfIndices.getEmpty(), Collections.EMPTY_LIST, null);
        return EMPTY_INDICES_SYMMETRIES;
    }

    @Override
    public String toString() {
        return generators.toString();
    }
}
