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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.*;
import cc.redberry.core.indexgenerator.*;
import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.*;
import cc.redberry.core.utils.*;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimpleSubstitution implements Transformation {

    static final SubstitutionProvider SIMPLE_SUBSTITUTION_PROVIDER = new SubstitutionProvider() {

        @Override
        public SimpleSubstitution createSubstitution(Tensor from, Tensor to, boolean allowDiffStates) {
            return new SimpleSubstitution(from, to);
        }
    };
    private final Tensor from, to;
    private final boolean allowDiffStates;
    private final Set<Integer> toDummyIndices;
    SubstitutionIterator iterator;

    public SimpleSubstitution(Tensor from, Tensor to) {
        this.from = from;
        this.to = to;
        this.allowDiffStates = CC.withMetric();

        toDummyIndices = TensorUtils.getAllIndicesNames(to);
        int free[] = IndicesUtils.getIndicesNames(to.getIndices().getFreeIndices());
        for (int i : free)
            toDummyIndices.remove(i);

    }

    private Tensor applyIndexMapping(IndexMappingBuffer buffer) {
//        Set<Integer> forbidden;
//        if (toDummyIndices.isEmpty())
//            forbidden = Collections.EMPTY_SET;
//        else
//            forbidden = iterator.forbiddenIndices();
//
//        Map<Integer, IndexMappingBufferRecord> map = buffer.getMap();
//
//        IntArrayList from = new IntArrayList(map.size()), to = new IntArrayList(map.size());
//        IndexMappingBufferRecord record;
//        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
//            from.add(entry.getKey());
//            record = entry.getValue();
//            to.add(record.getIndexName() ^ (record.isContracted() ? 0x80000000 : 0));
//        }
//
//        if (!toDummyIndices.isEmpty()) {
//            int[] _forbidden = new int[forbidden.size()];
//            int count = -1;
//            for (Integer f : forbidden)
//                _forbidden[++count] = f;
//            IndexGenerator generator = new IndexGenerator(_forbidden);
//            for (Integer dummy : toDummyIndices)
//                if (forbidden.contains(dummy)) {
//                    from.add(dummy);
//                    to.add(count = generator.generate(IndicesUtils.getType(dummy)));
//                    forbidden.add(count);
//                }
//        }
//        int[] _from = from.toArray(),_to = to.toArray();
//        ArraysUtils.quickSort(_from, _to);
//        return ApplyIndexMapping.applyIndexMapping(this.to, new ApplyIndexMapping.IndexMapper(_from, _to));
        int[] forbidden = new int[iterator.forbiddenIndices().size()];
        int c = -1;
        for (Integer f : iterator.forbiddenIndices())
            forbidden[++c] = f;
        Tensor n = ApplyIndexMapping.applyIndexMapping(to, buffer, forbidden);
        iterator.forbiddenIndices().addAll(TensorUtils.getAllIndicesNames(n));
        return n;

    }

    @Override
    public Tensor transform(Tensor tensor) {
        iterator = new SubstitutionIterator(tensor);
        Tensor current;
        while ((current = iterator.next()) != null) {
            IndexMappingBuffer buffer =
                    IndexMappings.getFirst(from, current, allowDiffStates);
            if (buffer == null)
                continue;
            Tensor n = applyIndexMapping(buffer);
            iterator.set(n);
        }
        return iterator.result();
    }
}
