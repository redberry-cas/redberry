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
 * the Free Software Foundation, either version 2 of the License, or
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static cc.redberry.core.indices.IndicesUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexGenerator {
    //TODO change to TByteMap
    protected Map<Byte, IntGenerator> generators = new HashMap<>();

    public IndexGenerator() {
    }

    public IndexGenerator(Indices indices) {
        this(indices.getAllIndices().copy());
    }

    protected IndexGenerator(Map<Byte, IntGenerator> generators) {
        this.generators = generators;
    }

    public IndexGenerator(final int[] indexArray) {
        if (indexArray.length == 0)
            return;
        for (int i = 0; i < indexArray.length; ++i)
            indexArray[i] = getNameWithType(indexArray[i]);
        Arrays.sort(indexArray);
        byte type = getType(indexArray[0]);
        indexArray[0] = getNameWithoutType(indexArray[0]);
        int prevIndex = 0;
        for (int i = 1; i < indexArray.length; ++i) {
            if (getType(indexArray[i]) != type) {
                generators.put(type, new IntGenerator(Arrays.copyOfRange(indexArray, prevIndex, i)));
                prevIndex = i;
                type = getType(indexArray[i]);
            }
            indexArray[i] = getNameWithoutType(indexArray[i]);
        }
        generators.put(type, new IntGenerator(Arrays.copyOfRange(indexArray, prevIndex, indexArray.length)));
    }

    public boolean contains(int index) {
        byte type = getType(index);
        IntGenerator intGen;
        if ((intGen = generators.get(type)) == null)
            return false;
        return intGen.contains(getNameWithoutType(index));
    }

    public void mergeFrom(IndexGenerator other) {
        for (Map.Entry<Byte, IntGenerator> entry : other.generators.entrySet()) {
            IntGenerator thisGenerator = generators.get(entry.getKey());
            if (thisGenerator == null)
                generators.put(entry.getKey(), entry.getValue().clone());
            else
                thisGenerator.mergeFrom(entry.getValue());
        }
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
    public int generate(IndexType type) {
        return generate(type.getType());
    }

    public int generate(byte type) {
        IntGenerator ig = generators.get(type);
        if (ig == null)
            generators.put(type, ig = new IntGenerator());
        return setType(type, ig.getNext());
    }


    @Override
    public IndexGenerator clone() {
        Map<Byte, IntGenerator> newMap = new HashMap<>(generators.size());
        for (Map.Entry<Byte, IntGenerator> entry : generators.entrySet())
            newMap.put(entry.getKey(), entry.getValue().clone());
        return new IndexGenerator(newMap);
    }
}
