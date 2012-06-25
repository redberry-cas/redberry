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
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.stretces.PrecalculatedStretches;
import cc.redberry.core.utils.stretces.Stretch;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class ProviderProduct implements IndexMappingProvider {

    static final IndexMappingProviderFactory FACTORY = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to, boolean allowDiffStates) {
            Product pfrom = (Product) from,
                    pto = (Product) to;
            if (pfrom.size() != pto.size())
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            for (int i = 0; i < pfrom.size(); ++i)
                if (pfrom.get(i).hashCode() != pto.get(i).hashCode())
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
            if (!pfrom.getContractionStructure().equals(pto.getContractionStructure()))
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            if (!testScalars(pfrom.getScalars(), pto.getScalars(), allowDiffStates))
                return IndexMappingProvider.Util.EMPTY_PROVIDER;

            //Temporary, until scalars mappings does not work
            return new ProviderProduct(opu, pfrom, pto, allowDiffStates);

            //True variant
            //return new ProviderProduct(opu, fromC.getNonScalarContent(), toC.getNonScalarContent(), allowDiffStates);
        }
    };

    private static boolean testScalars(Tensor[] from, Tensor[] to, boolean allowDiffStates) {
        if (from.length != to.length)
            return false;
        int i;
        int[] hashes = new int[from.length];
        for (i = 0; i < from.length; ++i)
            if ((hashes[i] = from[i].hashCode()) != to[i].hashCode())
                return false;
        PrecalculatedStretches precalculatedStretches = new PrecalculatedStretches(hashes);
        for (Stretch stretch : precalculatedStretches)
            if (stretch.length == 1)
                if (!mappingExists(from[stretch.from], to[stretch.from], allowDiffStates))
                    return false;
        OUTER:
        for (Stretch stretch : precalculatedStretches)
            if (stretch.length > 1) {
                SEMIOUTER:
                for (int[] permutation : new IntPermutationsGenerator(stretch.length)) {
                    for (i = 0; i < stretch.length; ++i)
                        if (!mappingExists(from[stretch.from + i], to[stretch.from + permutation[i]], allowDiffStates))
                            continue SEMIOUTER; // This permutation is bad
                    continue OUTER; //Good permutation has been found
                }
                return false; //No good combinatorics found
            }
        return true;
    }

    private static boolean mappingExists(Tensor from, Tensor to, boolean allowDiffStates) {
        final IndexMappingProvider pp = IndexMappings.createPort(
                IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl(allowDiffStates)),
                from, to, allowDiffStates);
        pp.tick();
        return pp.take() != null;
    }
    //private final ProductContent from, to;
    //private PermutationsProvider permutationsProvider;
    private final DummyIndexMappingProvider dummyProvider;
    private final MappingsPort op;

    private ProviderProduct(final MappingsPort opu,
                            final Product from, final Product to, boolean allowDiffStates) {
        this.dummyProvider = new DummyIndexMappingProvider(opu);
        //this.from = from;
        //this.to = to;
        int begin = 0;
        int i;
        //List<PermutationsProvider> disjointProviders = new ArrayList<>();
        List<Pair> stretches = new ArrayList<>();
        //non permutable
        List<Tensor> npFrom = new ArrayList<>(), npTo = new ArrayList<>();
        for (i = 1; i <= from.size(); ++i)
            if (i == from.size() || !from.getContractionStructure().get(i).equals(from.getContractionStructure().get(i - 1))) {
                if (i - 1 != begin)
                    stretches.add(new Pair(from.getRange(begin, i), to.getRange(begin, i)));
                else {
                    npFrom.add(from.get(i - 1));
                    npTo.add(to.get(i - 1));
                }
                begin = i;
            }
        //TODO sort stretches by length
        MappingsPort lastOutput = dummyProvider;
        if (!npFrom.isEmpty())
            lastOutput = new SimpleProductProvider(dummyProvider,
                                                   npFrom.toArray(new Tensor[npFrom.size()]),
                                                   npTo.toArray(new Tensor[npTo.size()]), allowDiffStates);
        if (stretches.isEmpty())
            this.op = lastOutput;
        else {
            PermutatorProvider[] pProviders = new PermutatorProvider[stretches.size()];
            i = 0;
            for (Pair p : stretches)
                lastOutput = pProviders[i++] = new PermutatorProvider(lastOutput, p.from, p.to, allowDiffStates);
            this.op = new SimpleProductProvider(pProviders);
        }
    }

    @Override
    public boolean tick() {
        return dummyProvider.tick();
    }

    @Override
    public IndexMappingBuffer take() {
        return op.take();
    }

    protected static class Pair {

        public final Tensor[] from, to;

        public Pair(final Tensor[] from, final Tensor[] to) {
            this.from = from;
            this.to = to;
        }
    }
}
