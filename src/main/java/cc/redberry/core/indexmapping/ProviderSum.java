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
package cc.redberry.core.indexmapping;

import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.tensor.Tensor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class ProviderSum implements IndexMappingProvider {

    static final IndexMappingProviderFactory FACTORY = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            if (from.size() != to.size())
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            for (int i = 0; i < from.size(); ++i)
                if (from.get(i).hashCode() != to.get(i).hashCode())
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
            return new ProviderSum(opu, from, to);
        }
    };
    private final IndexMappingProvider mainProvider;
    private final Tester[] testers;
    private final SignumHolder holder;

    private ProviderSum(IndexMappingProvider opu, Tensor from, Tensor to) {
        int begin = 0;

        //Search for main source
        int i;
        final int size = from.size();
        int mainStretchCoord = -1;
        int mainStretchIndex = -1;
        int mainStretchLength = Integer.MAX_VALUE;
        List<Tester> testersList = new ArrayList<>();
        for (i = 1; i <= size; ++i)
            if (i == size || from.get(i).hashCode() != from.get(i - 1).hashCode()) {
                //if (i - 1 != begin) {
                //Here is main stretches iteration code
                testersList.add(i - begin == 1
                                ? new SinglePairTester(from.get(begin), to.get(begin))
                                : new StretchPairTester(from.getRange(begin, i), to.getRange(begin, i)));
                if (mainStretchLength > i - begin) {
                    mainStretchCoord = begin;
                    mainStretchLength = i - begin;
                    mainStretchIndex = testersList.size() - 1;
                }
                //}

                begin = i;
            }

        holder = new SignumHolder(opu);
        if (mainStretchLength == 1) {
            this.mainProvider = IndexMappings.createPort(holder, from.get(mainStretchCoord),
                                                         to.get(mainStretchCoord));
            testersList.remove(mainStretchIndex);
        } else {
            final Tensor[] preFrom = from.getRange(mainStretchCoord,
                                                   mainStretchCoord + mainStretchLength);
            final Tensor[] preTo = to.getRange(mainStretchCoord,
                                               mainStretchCoord + mainStretchLength);

            this.mainProvider = new StretchPairSource(holder, preFrom, preTo);

            testersList.set(mainStretchIndex, (StretchPairSource) this.mainProvider);
        }
        this.testers = testersList.toArray(new Tester[testersList.size()]);
    }

    @Override
    public boolean tick() {
        return mainProvider.tick();
    }

    @Override
    public IndexMappingBuffer take() {
        OUTER:
        while (true) {
            final IndexMappingBuffer buffer = mainProvider.take();
            if (buffer == null)
                return null;
            buffer.removeContracted();
            if (holder.signum)
                buffer.addSignum(true);
            final IndexMappingBufferTester tester = IndexMappingBufferTester.create(buffer);
            for (Tester t : testers)
                if (!t.test(tester))
                    continue OUTER;
            if (holder.signum)
                buffer.addSignum(true);
            return buffer;
        }
    }

    //TODO review signum holder pattern
    private static class SignumHolder implements IndexMappingProvider {

        private final IndexMappingProvider provider;
        boolean signum;

        public SignumHolder(IndexMappingProvider provider) {
            this.provider = provider;
        }

        @Override
        public boolean tick() {
            return provider.tick();
        }

        @Override
        public IndexMappingBuffer take() {
            IndexMappingBuffer buffer = provider.take();
            if (buffer == null)
                return null;
            signum = buffer.getSignum();
            return buffer;
        }
    }

    private interface Tester {

        boolean test(IndexMappingBufferTester tester);
    }

    private static class StretchPairSource extends IndexMappingProviderAbstract
            implements Tester {

        private final Tensor[] from, to;
        private final IntPermutationsGenerator permutationGenerator;
        private MappingsPort currentSource = null;
        private int[] currentPermutation;

        public StretchPairSource(final MappingsPort opu,
                                 final Tensor[] from, final Tensor[] to) {
            super(opu);
            this.from = from;
            this.to = to;
            this.permutationGenerator = new IntPermutationsGenerator(from.length);
        }

        @Override
        public IndexMappingBuffer take() {
            if (currentBuffer == null)
                return null;
            IndexMappingBuffer buf;
            while (true) {
                if (currentSource != null && (buf = currentSource.take()) != null)
                    return buf;
                if (!permutationGenerator.hasNext()) {
                    currentBuffer = null;
                    return null;
                }
                currentPermutation = permutationGenerator.next();
                currentSource = IndexMappings.createPort(currentBuffer.clone(), from[0], to[currentPermutation[0]]);
            }
        }

        @Override
        protected void _tick() {
            permutationGenerator.reset();
        }

        @Override
        public boolean test(IndexMappingBufferTester tester) {
            for (int i = 1; i < from.length; ++i)
                if (!IndexMappingBufferTester.test(tester, from[i], to[currentPermutation[i]]))
                    return false;
            return true;
        }
    }

    private static class StretchPairTester implements Tester {

        private final Tensor[] from, to;
//        private final PriorityPermutationGenerator permutationGenerator;
        private final int length;

        public StretchPairTester(final Tensor[] from, final Tensor[] to) {
            this.from = from;
            this.to = to;
            this.length = from.length;

//            this.permutationGenerator = new PriorityPermutationGenerator(from.length);
        }

        @Override
        public boolean test(final IndexMappingBufferTester tester) {
            //TODO discuss algorithm with Dima
            boolean[] bijection = new boolean[length];
            int i, j;
            OUT:
            for (i = 0; i < length; ++i) {
                for (j = 0; j < length; ++j)
                    if (!bijection[j] && IndexMappingBufferTester.test(tester, from[j], to[i])) {
                        bijection[j] = true;
                        continue OUT;
                    }
                return false;
            }
            return true;
//            int[] permutation;
//            final PriorityPermutationGenerator generator = permutationGenerator;
//            generator.reset();
//            int i;
//            OUTER:
//            while ((permutation = generator.next()) != null)
//                for (i = 0; i < from.length; ++i) {
//                    if (!IndexMappingBufferTester.test(tester, from[i], to[permutation[i]]))
//                        continue OUTER;
//                    generator.nice();
//                    return true;
//                }
//            return false;
        }
    }

    private static class SinglePairTester implements Tester {

        private final Tensor from, to;

        public SinglePairTester(final Tensor from, final Tensor to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean test(final IndexMappingBufferTester tester) {
            return IndexMappingBufferTester.test(tester, from, to);
        }
    }
//    private static boolean test(final Tensor from, final Tensor to, final IndexMappingBufferTester tester) {
//        tester.reset();
//        final IndexMappingProvider provider =
//                IndexMappings.createPort(IndexMappingProvider.Util.singleton(tester), from, to, tester.allowDiffStates());
//        provider.tick();
//        return provider.take() != null;
//    }
}
