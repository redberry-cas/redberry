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
package cc.redberry.core.indexgenerator;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.Indices;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.procedure.TByteObjectProcedure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cc.redberry.core.indices.IndicesUtils.*;

/**
 * Generates distinct indices of particular types, which does not contain in
 * specified sets of indices (engaged data).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IndexGenerator {
    protected TByteObjectHashMap<IntGenerator> generators = new TByteObjectHashMap<>();

    /**
     * Creates with generator without engaged data.
     */
    public IndexGenerator() {
    }

    /**
     * Creates with generator with specified engaged data.
     *
     * @param indices forbidden indices
     */
    public IndexGenerator(Indices indices) {
        this(indices.getAllIndices().copy());
    }

    protected IndexGenerator(TByteObjectHashMap<IntGenerator> generators) {
        this.generators = generators;
    }

    /**
     * Creates with generator with specified engaged data.
     *
     * @param indices forbidden indices
     */
    public IndexGenerator(final int[] indices) {
        if (indices.length == 0)
            return;
        for (int i = 0; i < indices.length; ++i)
            indices[i] = getNameWithType(indices[i]);
        Arrays.sort(indices);
        byte type = getType(indices[0]);
        indices[0] = getNameWithoutType(indices[0]);
        int prevIndex = 0;
        for (int i = 1; i < indices.length; ++i) {
            if (getType(indices[i]) != type) {
                generators.put(type, new IntGenerator(Arrays.copyOfRange(indices, prevIndex, i)));
                prevIndex = i;
                type = getType(indices[i]);
            }
            indices[i] = getNameWithoutType(indices[i]);
        }
        generators.put(type, new IntGenerator(Arrays.copyOfRange(indices, prevIndex, indices.length)));
    }

    /**
     * Returns true if index contains in engaged data or already was generated.
     *
     * @param index index
     * @return true if index contains in engaged data or already was generated
     */
    public boolean contains(int index) {
        byte type = getType(index);
        IntGenerator intGen;
        if ((intGen = generators.get(type)) == null)
            return false;
        return intGen.contains(getNameWithoutType(index));
    }

    /**
     * Merges from specified generator.
     *
     * @param other index generator
     */
    public void mergeFrom(IndexGenerator other) {
        other.generators.forEachEntry(
                new TByteObjectProcedure<IntGenerator>() {
                    @Override
                    public boolean execute(byte a, IntGenerator b) {
                        IntGenerator thisGenerator = generators.get(a);
                        if (thisGenerator == null)
                            generators.put(a, b.clone());
                        else
                            thisGenerator.mergeFrom(b);
                        return true;
                    }
                }
        );
        /*for (Map.Entry<Byte, IntGenerator> entry : other.generators.entrySet()) {
            IntGenerator thisGenerator = generators.get(entry.getKey());
            if (thisGenerator == null)
                generators.put(entry.getKey(), entry.getValue().clone());
            else
                thisGenerator.mergeFrom(entry.getValue());
        }*/
    }

    /*public void add(int index) {
        byte type = getType(index);
        IntGenerator intGen;
        if ((intGen = generators.get(type)) == null) {
            generators.put(type, new IntGenerator(new int[]{getNameWithoutType(index)}));
            return;
        }
        intGen.add(getNameWithoutType(index));
    }*/

    /**
     * Generates new index of a particular type.
     *
     * @param type index type
     * @return new index of a particular type
     */
    public int generate(IndexType type) {
        return generate(type.getType());
    }

    /**
     * Generates new index of a particular type.
     *
     * @param type index type
     * @return new index of a particular type
     */
    public int generate(byte type) {
        IntGenerator ig = generators.get(type);
        if (ig == null)
            generators.put(type, ig = new IntGenerator());
        return setType(type, ig.getNext());
    }


    @Override
    public IndexGenerator clone() {
        final TByteObjectHashMap<IntGenerator> newMap = new TByteObjectHashMap<>(generators.size());
        generators.forEachEntry(
                new TByteObjectProcedure<IntGenerator>() {
                    @Override
                    public boolean execute(byte a, IntGenerator b) {
                        newMap.put(a, b.clone());
                        return true;
                    }
                }
        );
        /*for (Map.Entry<Byte, IntGenerator> entry : generators.entrySet())
            newMap.put(entry.getKey(), entry.getValue().clone());      */
        return new IndexGenerator(newMap);
    }
}
