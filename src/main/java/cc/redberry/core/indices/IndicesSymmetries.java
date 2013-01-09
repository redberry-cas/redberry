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
package cc.redberry.core.indices;

import cc.redberry.core.combinatorics.InconsistentGeneratorsException;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Representation of permutational symmetries of indices of simple tensors.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesSymmetries implements Iterable<Symmetry> {

    private final StructureOfIndices structureOfIndices;
    private final Symmetries symmetries;
    private short[] diffIds = null;

    IndicesSymmetries(StructureOfIndices structureOfIndices) {
        this.structureOfIndices = structureOfIndices;
        this.symmetries = SymmetriesFactory.createSymmetries(structureOfIndices.size());
    }

    private IndicesSymmetries(StructureOfIndices structureOfIndices, Symmetries symmetries, short[] diffIds) {
        this.structureOfIndices = structureOfIndices;
        this.symmetries = symmetries;
        this.diffIds = diffIds;
    }

    IndicesSymmetries(StructureOfIndices structureOfIndices, Symmetries symmetries) {
        this.structureOfIndices = structureOfIndices;
        this.symmetries = symmetries;
    }

    /**
     * Returns the type structure of indices, for which this symmetries defined.
     *
     * @return the type structure of indices, for which this symmetries defined
     */
    public StructureOfIndices getStructureOfIndices() {
        return structureOfIndices;
    }

    /**
     * Returns the reference on the internal representation of symmetries.
     *
     * @return the reference on the internal representation of symmetries
     */
    public Symmetries getInnerSymmetries() {
        return symmetries;
    }

    /**
     * Returns the basis.
     *
     * @return the basis
     * @see cc.redberry.core.combinatorics.symmetries.Symmetries#getBasisSymmetries()
     */
    public List<Symmetry> getBasis() {
        return symmetries.getBasisSymmetries();
    }

    @Override
    public Iterator<Symmetry> iterator() {
        return symmetries.iterator();
    }

    /**
     * See {@link cc.redberry.core.indices.Indices#getDiffIds()}
     *
     * @return {@code diffIds} for indices as specified in {@link cc.redberry.core.indices.Indices#getDiffIds()}
     */
    public short[] getDiffIds() {
        //TODO synchronize
        if (diffIds == null) {
            List<Symmetry> list = symmetries.getBasisSymmetries();
            diffIds = new short[symmetries.dimension()];
            Arrays.fill(diffIds, (short) -1);
            short number = 0;
            IntArrayList removed = new IntArrayList(2);
            int i0, i1;
            for (Symmetry symmetry : list)
                for (i0 = diffIds.length - 1; i0 >= 0; --i0)
                    if ((i1 = symmetry.newIndexOf(i0)) != i0)
                        if (diffIds[i0] == -1 && diffIds[i1] == -1)
                            diffIds[i0] = diffIds[i1] = number++;
                        else if (diffIds[i0] == -1)
                            diffIds[i0] = diffIds[i1];
                        else if (diffIds[i1] == -1)
                            diffIds[i1] = diffIds[i0];
                        else if (diffIds[i1] != diffIds[i0]) {
                            int n = diffIds[i1];
                            for (int k = 0; k < diffIds.length; ++k)
                                if (diffIds[k] == n)
                                    diffIds[k] = diffIds[i0];
                            removed.add(n);
                        }
            for (i1 = 0; i1 < diffIds.length; ++i1)
                if (diffIds[i1] == -1)
                    diffIds[i1] = number++;

            removed.sort();
            for (i0 = diffIds.length - 1; i0 >= 0; --i0) {
                diffIds[i0] += ArraysUtils.binarySearch(removed, diffIds[i0]) + 1;
            }
        }
        return diffIds;
    }

    /**
     * Adds permutational symmetry.
     *
     * @param permutation permutation
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if there are more then one type of indices in corresponding indices
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size()}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean addSymmetry(int... permutation) {
        return add(false, permutation);
    }

    /**
     * Adds permutational antisymmetry.
     *
     * @param permutation permutation
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if there are more then one type of indices in corresponding indices
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size()}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean addAntiSymmetry(int... permutation) {
        return add(true, permutation);
    }

    /**
     * Adds permutational symmetry for a particular type of indices.
     *
     * @param permutation permutation
     * @param type        type of indices
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size(type)}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean addSymmetry(IndexType type, int... permutation) {
        return add(type, false, permutation);
    }

    /**
     * Adds permutational antisymmetry for a particular type of indices.
     *
     * @param permutation permutation
     * @param type        type of indices
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size(type)}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean addAntiSymmetry(IndexType type, int... permutation) {
        return add(type, true, permutation);
    }

    /**
     * Adds permutational (anti)symmetry for a particular type of indices.
     *
     * @param permutation permutation
     * @param sign        sign of symmetry ({@code true} means '-', {@code false} means '+')
     * @param type        type of indices
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size(type)}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean add(IndexType type, boolean sign, int... permutation) {
        return add(type.getType(), new Symmetry(permutation, sign));
    }

    /**
     * Adds permutational (anti)symmetry for a particular type of indices.
     *
     * @param permutation permutation
     * @param sign        sign of symmetry ({@code true} means '-', {@code false} means '+')
     * @param type        type of indices
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size(type)}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean add(byte type, boolean sign, int... permutation) {
        return add(type, new Symmetry(permutation, sign));
    }

    /**
     * Adds permutational (anti)symmetry.
     *
     * @param permutation permutation
     * @param sign        sign of symmetry ({@code true} means '-', {@code false} means '+')
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if there are more then one type of indices in corresponding indices
     * @throws IllegalArgumentException if {@code permutation.length() != indices.size(type)}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean add(boolean sign, int... permutation) {
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
        return add(type, new Symmetry(permutation, sign));
    }

    /**
     * Adds permutational (anti)symmetry.
     *
     * @param symmetry symmetry
     * @return {@code true} if it is a new symmetry of indices and {@code false} if it follows from
     *         already defined symmetries.
     * @throws IllegalArgumentException if {@code symmetry.dimension() != indices.size(type)}
     * @throws InconsistentGeneratorsException
     *                                  if the specified symmetry is
     *                                  inconsistent with already defined
     */
    public boolean add(byte type, Symmetry symmetry) {
        StructureOfIndices.TypeData data = structureOfIndices.getTypeData(type);
        if (data == null)
            throw new IllegalArgumentException("No such type: " + IndexType.getType(type));
        if (data.length != symmetry.dimension())
            throw new IllegalArgumentException("Wrong symmetry length.");
        int[] s = new int[structureOfIndices.size()];
        int i = 0;
        for (; i < data.from; ++i)
            s[i] = i;
        for (int j = 0; j < data.length; ++j, ++i)
            s[i] = symmetry.newIndexOf(j) + data.from;
        for (; i < structureOfIndices.size(); ++i)
            s[i] = i;
        try {
            if (symmetries.add(new Symmetry(s, symmetry.isAntiSymmetry()))) {
                diffIds = null;
                return true;
            }
            return false;
        } catch (InconsistentGeneratorsException exception) {
            throw new InconsistentGeneratorsException("Adding inconsistent symmetry to tensor indices symmetries.");
        }
    }

    /**
     * Adds permutational (anti)symmetry for a particular type of indices without any checks.
     *
     * @param symmetry symmetry
     * @return {@code true}
     */
    public boolean addUnsafe(byte type, Symmetry symmetry) {
        StructureOfIndices.TypeData data = structureOfIndices.getTypeData(type);
        if (data == null)
            throw new IllegalArgumentException("No such type: " + IndexType.getType(type));
        if (data.length != symmetry.dimension())
            throw new IllegalArgumentException("Wrong symmetry length.");
        int[] s = new int[structureOfIndices.size()];
        int i = 0;
        for (; i < data.from; ++i)
            s[i] = i;
        for (int j = 0; j < data.length; ++j, ++i)
            s[i] = symmetry.newIndexOf(j) + data.from;
        for (; i < structureOfIndices.size(); ++i)
            s[i] = i;
        symmetries.addUnsafe(new Symmetry(s, symmetry.isAntiSymmetry()));
        return true;
    }

    /**
     * Adds permutational (anti)symmetry of indices without any checks.
     *
     * @param symmetry symmetry
     * @return {@code true}
     */
    public boolean addUnsafe(Symmetry symmetry) {
        return this.symmetries.addUnsafe(symmetry);
    }

    /**
     * Returns <tt>true</tt> if and only if this set contains only identity
     * symmetry and <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if and only if this set contains only identity
     *         symmetry and <tt>false</tt> otherwise
     */
    public final boolean isEmpty() {
        return symmetries.isEmpty();
    }

    @Override
    public IndicesSymmetries clone() {
        return new IndicesSymmetries(structureOfIndices, symmetries.clone(), diffIds);
    }

    @Override
    public String toString() {
        return symmetries.toString();
    }

    //    @Override
//    public boolean equals(Object obj) {
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        final IndicesSymmetries other = (IndicesSymmetries) obj;
//        if (!this.indicesTypeStructure.equals(other.indicesTypeStructure))
//            return false;
//        if (!Objects.equals(this.symmetries, other.symmetries))
//            return false;
//        if (!Arrays.equals(this.diffIds, other.diffIds))
//            return false;
//        return true;
//    }
    @Override
    public int hashCode() {
        return 301 + Objects.hashCode(this.symmetries);
    }

    /*
     * private static void checkConsistent(IndicesTypeStructure
     * indicesTypeStructure, SymmetriesImpl symmetries) { List<Symmetry> list =
     * symmetries.getBasisSymmetries(); for (Symmetry s : list)
     * checkConsistent(indicesTypeStructure, s); }
     *
     * private static void checkConsistent(IndicesTypeStructure
     * indicesTypeStructure, Symmetry symmetry) { for (int i = 0; i <
     * symmetry.dimension(); ++i) if (indicesTypeStructure.data[i] !=
     * indicesTypeStructure.data[symmetry.newIndexOf(i)]) throw new
     * IllegalArgumentException("Inconsistent symmetry: permutes indices with
     * different types."); }
     */

    /**
     * Creates container of indices symmetries for a specified structure of indices.
     *
     * @param structureOfIndices structure of indices
     * @return indices symmetries for a specified structure of indices
     */
    public static IndicesSymmetries create(StructureOfIndices structureOfIndices) {
        if (structureOfIndices.size() == 0)
            return EMPTY_SYMMETRIES;
        return new IndicesSymmetries(structureOfIndices);
    }

    static final IndicesSymmetries EMPTY_SYMMETRIES =
            new IndicesSymmetries(new StructureOfIndices(EmptySimpleIndices.EMPTY_SIMPLE_INDICES_INSTANCE),
                    SymmetriesFactory.createSymmetries(0), new short[0]) {

                @Override
                public IndicesSymmetries clone() {
                    return this;
                }
            };
}
