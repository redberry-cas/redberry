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
package cc.redberry.core.transformations;

import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappingBufferRecord;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ApplyIndexMapping implements Transformation {

    private final int[] from, to, forbidden;

    public ApplyIndexMapping(int[] from, int[] to, int[] forbidden) {
        this.from = from.clone();
        this.to = to.clone();
        this.forbidden = forbidden;
    }

    @Override
    public Tensor transform(Tensor t) {
        checkConsistent(t, from);
        return applyIndexMapping(t, from, to, forbidden);
    }

    private static void checkConsistent(Tensor tensor, final int[] from) {
        int[] freeIndices = tensor.getIndices().getFreeIndices().getAllIndices().copy();
        Arrays.sort(freeIndices);
        int[] _from = from.clone();
        Arrays.sort(_from);
        if (!Arrays.equals(freeIndices, _from))
            throw new IllegalArgumentException("From indices are not equal to free indices of tensor.");
    }

    public static Tensor applyIndexMapping(Tensor tensor, int[] from, int[] to, int[] forbidden) {
        checkConsistent(tensor, from);
        return applyIndexMapping1(tensor, from.clone(), to.clone(), forbidden);
    }

    public static Tensor applyIndexMapping(Tensor tensor, IndexMappingBuffer buffer) {
        return applyIndexMapping(tensor, buffer, new int[0]);
    }

    public static Tensor applyIndexMapping(Tensor tensor, IndexMappingBuffer buffer, int[] forbidden) {
        buffer.removeContracted();
        Map<Integer, IndexMappingBufferRecord> map = buffer.getMap();
        int[] from = new int[map.size()], to = new int[map.size()];
        int count = 0;
        IndexMappingBufferRecord record;
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
            from[count] = entry.getKey();
            record = entry.getValue();
            to[count++] = record.getIndexName() ^ (record.isContracted() ? 0x80000000 : 0);
        }

        final int[] freeIndices = tensor.getIndices().getFreeIndices().getAllIndices().copy();
        for (int i = 0; i < freeIndices.length; ++i)
            freeIndices[i] = IndicesUtils.getNameWithType(freeIndices[i]);
        Arrays.sort(freeIndices);
        int[] _from = from.clone();
        Arrays.sort(_from);
        if (!Arrays.equals(freeIndices, _from))
            throw new IllegalArgumentException("From indices are not equal to free indices of tensor.");

        return applyIndexMapping1(tensor, from, to, forbidden);
    }

    private static Tensor applyIndexMapping1(Tensor tensor, int[] from, int[] to, int[] forbidden) {
        int i, rawState;
        for (i = from.length - 1; i >= 0; --i) {
            rawState = IndicesUtils.getRawStateInt(from[i]);
            from[i] ^= rawState;
            to[i] ^= rawState;
        }
        ArraysUtils.quickSort(from, to);
        Set<Integer> allIndices = TensorUtils.getAllIndices(tensor);

        //extracting contracted only
        Indices indices = tensor.getIndices().getFreeIndices();
        for (i = indices.size() - 1; i >= 0; --i)
            allIndices.remove(IndicesUtils.getNameWithType(indices.get(i)));


        int[] allForbidden = new int[to.length + forbidden.length];
        System.arraycopy(to, 0, allForbidden, 0, to.length);
        System.arraycopy(forbidden, 0, allForbidden, to.length, forbidden.length);
        for (i = allForbidden.length - 1; i >= 0; --i)
            allForbidden[i] = IndicesUtils.getNameWithType(allForbidden[i]);

        IntArrayList fromL = new IntArrayList(from.length), toL = new IntArrayList(to.length);
        fromL.addAll(from);
        toL.addAll(to);

        IndexGenerator generator = new IndexGenerator(allForbidden);//also sorts allForbidden array
        for (Integer index : allIndices)
            if (Arrays.binarySearch(allForbidden, index) >= 0 && Arrays.binarySearch(from, index) < 0) {
                fromL.add(index);
                toL.add(generator.generate(IndicesUtils.getType(index)));
            }

        int[] _from = fromL.toArray(), _to = toL.toArray();
        ArraysUtils.quickSort(_from, _to);

        return applyIndexMapping(tensor, new IndexMapper(_from, _to));
    }

    private static Tensor applyIndexMapping(Tensor tensor, IndexMapper mapper) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        TraverseState state;
        SimpleIndices oldIndices, newIndices;
        SimpleTensor simpleTensor;
        while ((state = iterator.next()) != null) {
            if (state == TraverseState.Leaving)
                continue;
            if (!(iterator.current() instanceof SimpleTensor))
                continue;
            simpleTensor = (SimpleTensor) iterator.current();
            oldIndices = simpleTensor.getIndices();
            newIndices = oldIndices.applyIndexMapping(mapper);
            if (oldIndices != newIndices)
                iterator.set(Tensors.simpleTensor(simpleTensor.getName(), newIndices));
        }
        return iterator.result();
    }

    private final static class IndexMapper implements IndexMapping {

        private final int[] from, to;

        public IndexMapper(int[] from, int[] to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int map(int index) {
            int position = Arrays.binarySearch(from, IndicesUtils.getNameWithType(index));
            if (position < 0)
                return index;
            return IndicesUtils.getRawStateInt(index) ^ to[position];
        }
    }
}
