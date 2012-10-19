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

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.combinatorics.IntCombinationPermutationGenerator;
import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappingBufferTester;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.MappingsPort;
import cc.redberry.core.tensor.Tensor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SumBijectionPort implements OutputPortUnsafe<BijectionContainer> {

    private List<Mapper> mappers;
    private int[] bijection;
    private boolean finished = false;
    private MapperSource source;

    public SumBijectionPort(Tensor from, Tensor to) {
        if (from.size() > to.size()) {
            finished = true;
            return;
        }
        mappers = new ArrayList<>();
        int i, j = 0, fromBegin = 0, toBegin, fromSize = from.size(), toSize = to.size();
        int mainStretchFromCoord = -1, mainStretchFromPointer = -1, mainStretchFromLength = -1,
                mainStretchToLength = Integer.MAX_VALUE, maintStretchIndex = -1;
        int hash = from.get(0).hashCode();
        for (i = 1; i <= fromSize; ++i) {
            if (i == fromSize || from.get(i).hashCode() != hash) {
                for (; j < toSize; ++j)
                    if (to.get(j).hashCode() >= hash)
                        break;
                if (j == toSize || to.get(j).hashCode() > hash) {
                    finished = true;
                    break;
                }
                toBegin = j;
                for (; j < toSize; ++j)
                    if (to.get(j).hashCode() != hash)
                        break;

                if (j - toBegin < i - fromBegin) {
                    finished = true;
                    break;
                }
                if (j - toBegin == 1)
                    mappers.add(new SinglePairMapper(from.get(fromBegin), to.get(toBegin), toBegin));
                else
                    mappers.add(new StretchPairMapper(from.getRange(fromBegin, i), to.getRange(toBegin, j), toBegin));
                if (j - toBegin < mainStretchToLength) {
                    mainStretchToLength = j - toBegin;
                    mainStretchFromLength = i - fromBegin;
                    mainStretchFromCoord = fromBegin;
                    mainStretchFromPointer = toBegin;
                    maintStretchIndex = mappers.size() - 1;
                }
                fromBegin = i;
            }
            if (i != fromSize)
                hash = from.get(i).hashCode();
        }
        if (finished)
            return;
        if (mainStretchToLength == 1)
            source = new SinglePairSource(from.get(mainStretchFromCoord), to.get(mainStretchFromPointer), mainStretchFromPointer);
        else
            source = new StretchPairSource(
                    from.getRange(mainStretchFromCoord, mainStretchFromCoord + mainStretchFromLength),
                    to.getRange(mainStretchFromPointer, mainStretchFromPointer + mainStretchToLength),
                    mainStretchFromPointer);
        mappers.set(maintStretchIndex, source);
        bijection = new int[from.size()];
    }

    @Override
    public BijectionContainer take() {
        if (finished)
            return null;
        List<int[]> bijections = new ArrayList<>();
        IndexMappingBuffer buffer;
        int i, b[];
        final int mappersSize = mappers.size();
        OUT:
        while (true) {
            buffer = source.take();
            if (buffer == null) {
                finished = true;
                return null;
            }
            for (i = 0; i < mappersSize; ++i) {
                b = mappers.get(i).nextMapping(buffer);
                if (b == null) {
                    for (; i >= 0; --i)
                        mappers.get(i).reset();
                    bijections.clear();
                    continue OUT;
                }
                bijections.add(b);
            }
            return new BijectionContainer(buffer, fill(bijection, bijections));
        }
    }

    private static int[] fill(int[] r, List<int[]> list) {
        int size = list.size(), begin = 0;
        int[] temp;
        for (int i = 0; i < size; ++i) {
            temp = list.get(i);
            System.arraycopy(temp, 0, r, begin, temp.length);
            begin += temp.length;
        }
        return r;
    }

    private static interface Mapper {

        int[] nextMapping(IndexMappingBuffer buffer);

        void reset();
    }

    private static abstract class AbstaractMapper implements Mapper {

        @Override
        public int[] nextMapping(IndexMappingBuffer buffer) {
            if (buffer == null)
                return null;
            return _nextMapping(buffer);
        }

        abstract int[] _nextMapping(IndexMappingBuffer buffer);
    }

    private static abstract class AbstractStretchMapper extends AbstaractMapper {

        final Tensor[] from, to;
        final int fromPointer;
        IntCombinationPermutationGenerator permutationGenerator;
        int[] currentPermutation;

        public AbstractStretchMapper(Tensor[] from, Tensor[] to, int fromPointer) {
            this.from = from;
            this.to = to;
            this.fromPointer = fromPointer;
            this.permutationGenerator = new IntCombinationPermutationGenerator(to.length, from.length);
        }

        public boolean test(IndexMappingBuffer buffer) {
            IndexMappingBufferTester tester = IndexMappingBufferTester.create(buffer);
            for (int i = 1; i < from.length; ++i)
                if (!IndexMappingBufferTester.test(tester, from[i], to[currentPermutation[i]]))
                    return false;
            return true;
        }
    }

    private static interface MapperSource extends Mapper, MappingsPort {
    }

    private static final class SinglePairSource extends AbstaractMapper
            implements MapperSource {

        private final MappingsPort mappingsPort;
        private final int[] fromPointer;

        public SinglePairSource(Tensor from, Tensor to, int fromPointer) {
            this.mappingsPort = IndexMappings.createPort(from, to);
            this.fromPointer = new int[]{fromPointer};
        }

        @Override
        public int[] _nextMapping(IndexMappingBuffer buffer) {
            return fromPointer;
        }

        @Override
        public IndexMappingBuffer take() {
            return mappingsPort.take();
        }

        @Override
        public void reset() {
        }
    }

    private static final class SinglePairMapper extends AbstaractMapper {

        final Tensor from, to;
        final int[] fromPointer;

        public SinglePairMapper(Tensor from, Tensor to, int fromPointer) {
            this.from = from;
            this.to = to;
            this.fromPointer = new int[]{fromPointer};
        }

        @Override
        public int[] _nextMapping(IndexMappingBuffer buffer) {
            if (!IndexMappingBufferTester.test(IndexMappingBufferTester.create(buffer), from, to))
                return null;
            return fromPointer;
        }

        @Override
        public void reset() {
        }
    }

    private static final class StretchPairSource extends AbstractStretchMapper
            implements MapperSource {

        private MappingsPort currentSource;

        public StretchPairSource(Tensor[] from, Tensor[] to, int fromPointer) {
            super(from, to, fromPointer);
        }

        @Override
        public int[] _nextMapping(IndexMappingBuffer buffer) {
            if (buffer == null)
                return null;
            if (!test(buffer))
                return null;
            int[] mapping = new int[from.length];
            for (int i = 0; i < from.length; ++i)
                mapping[i] = fromPointer + currentPermutation[i];
            return mapping;
        }

        @Override
        public IndexMappingBuffer take() {
            IndexMappingBuffer buf;
            while (true) {
                if (currentSource != null && (buf = currentSource.take()) != null)
                    return buf;
                if (!permutationGenerator.hasNext())
                    return null;
                currentPermutation = permutationGenerator.next();
                currentSource = IndexMappings.createPort(from[0], to[currentPermutation[0]]);
            }
        }

        @Override
        public void reset() {
        }
    }

    private static final class StretchPairMapper extends AbstractStretchMapper {

        public StretchPairMapper(Tensor[] from, Tensor[] to, int fromPointer) {
            super(from, to, fromPointer);
            currentPermutation = permutationGenerator.next();
        }

        @Override
        public void reset() {
            permutationGenerator = new IntCombinationPermutationGenerator(to.length, from.length);
            currentPermutation = permutationGenerator.next();
        }

        @Override
        public int[] _nextMapping(IndexMappingBuffer buffer) {
            while (true) {
                if (currentPermutation == null)
                    return null;
                if (!test(buffer))
                    currentPermutation = permutationGenerator.next();
                else {
                    int[] bijection = new int[from.length];
                    for (int i = 0; i < from.length; ++i)
                        bijection[i] = fromPointer + currentPermutation[i];
                    return bijection;
                }
            }
        }
    }
}
