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
package cc.redberry.core.indexgenerator;

import cc.redberry.core.indices.IndexType;
import gnu.trove.map.hash.TByteObjectHashMap;

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.*;

/**
 * Returns distinct indices only from the allowed set of indices and produces exception if no more allowed indices exist.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexGeneratorFromData implements IndexGenerator {
    private final TByteObjectHashMap<IntProvider> generators = new TByteObjectHashMap<>();

    public IndexGeneratorFromData(int[] allowedIndices) {
        if (allowedIndices.length > 0) {
            Arrays.sort(allowedIndices);
            byte type = getType(allowedIndices[0]);
            allowedIndices[0] = getNameWithoutType(allowedIndices[0]);
            int prevIndex = 0;
            for (int i = 1; i < allowedIndices.length; ++i) {
                if (getType(allowedIndices[i]) != type) {
                    generators.put(type, new IntProvider(Arrays.copyOfRange(allowedIndices, prevIndex, i)));
                    prevIndex = i;
                    type = getType(allowedIndices[i]);
                }
                allowedIndices[i] = getNameWithoutType(allowedIndices[i]);
            }
            generators.put(type, new IntProvider(Arrays.copyOfRange(allowedIndices, prevIndex, allowedIndices.length)));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if no more allowed indices exist
     */
    @Override
    public int generate(byte type) {
        IntProvider ig = generators.get(type);
        if (ig == null)
            throw new IndexOutOfBoundsException("No allowed indices with specified type: " + IndexType.getType(type));
        return setType(type, ig.getNext());
    }

    private static final class IntProvider {
        final int[] data;
        int pointer;

        private IntProvider(int[] data) {
            this.data = data;
            this.pointer = 0;
        }

        int getNext() {
            return data[pointer++];
        }
    }
}
