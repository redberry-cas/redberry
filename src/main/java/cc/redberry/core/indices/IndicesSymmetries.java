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
package cc.redberry.core.indices;

import cc.redberry.core.combinatorics.InconsistentGeneratorsException;
import cc.redberry.core.combinatorics.Symmetries;
import cc.redberry.core.combinatorics.Symmetry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesSymmetries implements Iterable<Symmetry> {
    private final IndicesTypeStructure indicesTypeStructure;
    private final Symmetries symmetries;
    private short[] diffIds = null;

    IndicesSymmetries(IndicesTypeStructure indicesTypeStructure) {
        this.indicesTypeStructure = indicesTypeStructure;
        this.symmetries = new Symmetries(indicesTypeStructure.size());
    }

    IndicesSymmetries(IndicesTypeStructure indicesTypeStructure, Symmetries symmetries, short[] diffIds) {
        this.indicesTypeStructure = indicesTypeStructure;
        this.symmetries = symmetries;
        this.diffIds = diffIds;
    }

    IndicesSymmetries(IndicesTypeStructure indicesTypeStructure, Symmetries symmetries) {
        this.indicesTypeStructure = indicesTypeStructure;
        this.symmetries = symmetries;
    }

    public Symmetries getReference() {
        return symmetries;
    }

    @Override
    public Iterator<Symmetry> iterator() {
        return symmetries.iterator();
    }

    public short[] getDiffIds() {
        //TODO synchronize
        if (diffIds == null) {
            List<Symmetry> list = symmetries.getBaseSymmetries();
            diffIds = new short[symmetries.dimension()];
            Arrays.fill(diffIds, (short) -1);
            short number = 0;
            int i0, i1;
            for (Symmetry symmetry : list)
                for (i0 = 0; i0 < symmetry.dimension(); ++i0)
                    if ((i1 = symmetry.newIndexOf(i0)) != i0)
                        if (diffIds[i0] == -1 && diffIds[i1] == -1)
                            diffIds[i0] = diffIds[i1] = number++;
                        else if (diffIds[i0] == -1)
                            diffIds[i0] = diffIds[i1];
                        else
                            diffIds[i1] = diffIds[i0];
            for (i1 = 0; i1 < diffIds.length; ++i1)
                if (diffIds[i1] == -1)
                    diffIds[i1] = number++;
        }
        return diffIds;
    }

    public boolean add(byte type, Symmetry symmetry) {
        IndicesTypeStructure.TypeData data = indicesTypeStructure.getTypeDatas(type);
        if (data == null)
            throw new IllegalArgumentException("No such type: "+ IndexType.getType(type));
        if (data.length != symmetry.dimension())
            throw new IllegalArgumentException("Wrong symmetry length.");
        int[] s = new int[indicesTypeStructure.size()];
        int i = 0;
        for (; i < data.from; ++i)
            s[i] = i;
        for (int j = 0; j < data.length; ++j, ++i)
            s[i] = symmetry.newIndexOf(j) + data.from;
        for (; i < indicesTypeStructure.size(); ++i)
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
    
    public boolean addUnsafe(byte type, Symmetry symmetry) {
        IndicesTypeStructure.TypeData data = indicesTypeStructure.getTypeDatas(type);
        if (data == null)
            throw new IllegalArgumentException("No such type: "+ IndexType.getType(type));
        if (data.length != symmetry.dimension())
            throw new IllegalArgumentException("Wrong symmetry length.");
        int[] s = new int[indicesTypeStructure.size()];
        int i = 0;
        for (; i < data.from; ++i)
            s[i] = i;
        for (int j = 0; j < data.length; ++j, ++i)
            s[i] = symmetry.newIndexOf(j) + data.from;
        for (; i < indicesTypeStructure.size(); ++i)
            s[i] = i;
        symmetries.addUnsafe(new Symmetry(s, symmetry.isAntiSymmetry()));
            return true;
    }

    public boolean addUnsafe(Symmetry symmetry) {
        return this.symmetries.addUnsafe(symmetry);
    }

    public boolean addAllUnsafe(Collection<Symmetry> symmetries) {
        return this.symmetries.addAllUnsafe(symmetries);
    }

    public boolean addAllUnsafe(Symmetry... symmetries) {
        return this.symmetries.addAllUnsafe(symmetries);
    }

    public boolean addAllUnsafe(IndicesSymmetries indicesSymmetries) {
        if (!indicesTypeStructure.equals(indicesSymmetries.indicesTypeStructure))
            throw new IllegalArgumentException("Not compatible symmetries");
        return this.symmetries.addAllUnsafe(indicesSymmetries.symmetries);
    }

    public final boolean isEmpty() {
        return symmetries.isEmpty();
    }

    @Override
    public IndicesSymmetries clone() {
        return new IndicesSymmetries(indicesTypeStructure, symmetries.clone(), diffIds);
    }

    @Override
    public String toString() {
        return symmetries.toString();
    }

    private static void checkConsistent(IndicesTypeStructure indicesTypeStructure, Symmetries symmetries) {
        List<Symmetry> list = symmetries.getBaseSymmetries();
        for (Symmetry s : list)
            checkConsistent(indicesTypeStructure, s);
    }

    private static void checkConsistent(IndicesTypeStructure indicesTypeStructure, Symmetry symmetry) {
        for (int i = 0; i < symmetry.dimension(); ++i)
            if (indicesTypeStructure.data[i] != indicesTypeStructure.data[symmetry.newIndexOf(i)])
                throw new IllegalArgumentException("Inconsistent symmetry: permutes indices with different types.");
    }

    public static IndicesSymmetries create(IndicesTypeStructure indicesTypeStructure) {
        if (indicesTypeStructure.size() == 0)
            return EMPTY_SYMMETRIES;
        return new IndicesSymmetries(indicesTypeStructure);
    }
    public static final IndicesSymmetries EMPTY_SYMMETRIES =
            new IndicesSymmetries(new IndicesTypeStructure(EmptyIndices.INSTANCE),
            Symmetries.EMPTY_SYMMETRIES, new short[0]) {
                @Override
                public IndicesSymmetries clone() {
                    return this;
                }
            };
}
