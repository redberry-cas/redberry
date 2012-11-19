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
package cc.redberry.core.indexmapping;

import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.ArraysUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexMappingBufferTester implements IndexMappingBuffer {

    private IndexMappingBufferImpl innerBuffer;
    private final int[] from, to;
    private final boolean signum;

    public IndexMappingBufferTester(int[] from, boolean signum) {
        from = IndicesUtils.getIndicesNames(from);
        Arrays.sort(from);
        this.from = this.to = from;
        this.signum = signum;
        innerBuffer = new IndexMappingBufferImpl();
    }

    public IndexMappingBufferTester(int[] from, int[] to, boolean signum) {
        if (from.length != to.length)
            throw new IllegalArgumentException();
        from = IndicesUtils.getIndicesNames(from);
        to = IndicesUtils.getIndicesNames(to);
        this.from = from;
        this.to = to;
        this.signum = signum;
        //Here we can use unstable sort method
        ArraysUtils.quickSort(this.from, this.to);
        innerBuffer = new IndexMappingBufferImpl();
    }

    private IndexMappingBufferTester(IndexMappingBufferImpl buffer) {
        Map<Integer, IndexMappingBufferRecord> map = buffer.map;
        final int size = map.size();
        from = new int[size];
        to = new int[size];
        signum = buffer.getSignum();
        int i = 0;
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : map.entrySet()) {
            from[i] = entry.getKey();
            to[i++] = entry.getValue().getIndexName();
        }
        ArraysUtils.quickSort(this.from, this.to);
        innerBuffer = new IndexMappingBufferImpl();
    }

    private IndexMappingBufferTester(IndexMappingBufferTester buffer) {
        int innerBufferSize = buffer.innerBuffer.map.size();
        from = new int[buffer.from.length + innerBufferSize];
        to = new int[buffer.from.length + innerBufferSize];
        signum = buffer.getSignum();
        System.arraycopy(buffer.from, 0, from, 0, buffer.from.length);
        System.arraycopy(buffer.to, 0, to, 0, buffer.to.length);
        int i = buffer.from.length;
        for (Map.Entry<Integer, IndexMappingBufferRecord> entry : buffer.innerBuffer.map.entrySet()) {
            from[i] = entry.getKey();
            to[i++] = entry.getValue().getIndexName();
        }
        ArraysUtils.quickSort(this.from, this.to);
        innerBuffer = new IndexMappingBufferImpl();
    }

    private IndexMappingBufferTester(IndexMappingBufferImpl innerBuffer, int[] from, int[] to, boolean signum) {
        this.innerBuffer = innerBuffer;
        this.from = from;
        this.to = to;
        this.signum = signum;
    }

    public static IndexMappingBufferTester create(IndexMappingBuffer buffer) {
        if (buffer instanceof IndexMappingBufferTester)
            return new IndexMappingBufferTester((IndexMappingBufferTester) buffer);
        if (buffer instanceof IndexMappingBufferImpl)
            return new IndexMappingBufferTester((IndexMappingBufferImpl) buffer);
        throw new RuntimeException("Unknown IndexMappingBufferType");
    }

    public static boolean test(IndexMappingBufferTester tester, Tensor from, Tensor to) {
        tester.reset();
        final IndexMappingProvider provider =
                IndexMappings.createPort(IndexMappingProvider.Util.singleton(tester), from, to);
        provider.tick();
        IndexMappingBuffer buffer;
        while ((buffer = provider.take()) != null)
            if (!buffer.getSignum())
                return true;
        return false;
    }

    @Override
    public boolean tryMap(int from, int to) {
        int fromName = IndicesUtils.getNameWithType(from),
                toName = IndicesUtils.getNameWithType(to);
        int position;
        if ((position = Arrays.binarySearch(this.from, fromName)) < 0)
            return innerBuffer.tryMap(from, to);
        return this.to[position] == toName;
    }

    @Override
    public void addSignum(boolean signum) {
        innerBuffer.addSignum(signum);
    }

    @Override
    public boolean getSignum() {
        return signum ^ innerBuffer.signum;
    }

    @Override
    public IndexMappingBufferTester clone() {
        return new IndexMappingBufferTester(innerBuffer.clone(), from, to, signum);
    }

    @Override
    public void removeContracted() {
        innerBuffer.removeContracted();
    }

    @Override
    public boolean isEmpty() {
        return innerBuffer.isEmpty();
    }

    @Override
    public Map<Integer, IndexMappingBufferRecord> getMap() {
        return innerBuffer.getMap();
    }

    public void reset() {
        innerBuffer = new IndexMappingBufferImpl();
    }

    @Override
    public String toString() {
        return "inner: " + innerBuffer.toString();
    }
}
