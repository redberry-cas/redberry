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
package cc.redberry.core.performance.kv;

import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndicesFactoryUtil {

    public static Indices createIndices(Tensor[] used_expressions, IndicesTypeStructure indicesStructure) {
        IndexGenerator generator = new IndexGenerator();

        Set<Integer> expIndices = TensorUtils.getAllIndicesNames(used_expressions);
        for (Integer i : expIndices)
            generator.add(i);

        int[] i_array = new int[indicesStructure.size()];
        byte type;
        int i = 0;
        for (IndexType indexType : IndexType.values()) {
            IndicesTypeStructure.TypeData typeData = indicesStructure.getTypeData(indexType.getType());
            for (i = typeData.from; i < typeData.from + typeData.length; ++i)
                i_array[i] = (generator.generate((byte) (indexType.getType() & 0x7F))) | (indexType.getType() << 24);
        }
        return IndicesFactory.createSimple(null, i_array);
    }

    public static Indices createIndices(Tensor[] used_expressions, SimpleIndices sample) {
        return createIndices(used_expressions, new IndicesTypeStructure(sample));
    }

    public static Indices createIndices(Tensor[] used_expressions, String sample) {
        return createIndices(used_expressions, ParserIndices.parseSimple(sample));
    }

    public static Indices doubleAndDumpIndices(Indices indices) {
        int length = indices.size();
        int[] i_array = indices.getAllIndices().copy();
        int[] res = new int[length * 2];
        System.arraycopy(i_array, 0, res, 0, length);
        for (int i = 0; i < length; ++i)
            res[i + length] = 0x80000000 ^ i_array[i];
        return IndicesFactory.createSimple(null, res);
    }

    public static Indices doubleAndDumpIndices(String indices) {
        return doubleAndDumpIndices(ParserIndices.parseSimple(indices));
    }
}
