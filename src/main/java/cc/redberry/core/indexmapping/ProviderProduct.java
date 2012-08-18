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
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ProductContent;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.stretces.PrecalculatedStretches;
import cc.redberry.core.utils.stretces.Stretch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class ProviderProduct implements IndexMappingProvider {

    static final IndexMappingProviderFactory FACTORY = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            Product pfrom = (Product) from,
                    pto = (Product) to;
            if (pfrom.sizeWithoutFactor() != pto.sizeWithoutFactor())
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            Boolean booluon = compareFactors(pfrom.getFactor(), pto.getFactor());
            if (booluon == null)
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            if (pfrom.getFactor().equals(pto.getFactor()))
                for (int i = 0; i < pfrom.sizeWithoutFactor(); ++i)
                    if (pfrom.getWithoutFactor(i).hashCode() != pto.getWithoutFactor(i).hashCode())
                        return IndexMappingProvider.Util.EMPTY_PROVIDER;
            ProductContent fromContent = pfrom.getContent(), toContent = pto.getContent();
            if (!fromContent.getContractionStructure().equals(toContent.getContractionStructure()))
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            Tensor[] fromScalars = pfrom.getAllScalarsWithoutFactor(), toScalars = pto.getAllScalarsWithoutFactor();
            if (fromScalars.length != toScalars.length)
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            if (fromScalars.length != 1 && !testScalars(fromScalars, toScalars))
                return IndexMappingProvider.Util.EMPTY_PROVIDER;

//            Temporary, until scalars mappings does not work
            if (booluon)
                return new MinusIndexMappingProviderWrapper(new ProviderProduct(opu, pfrom, pto));
            return new ProviderProduct(opu, pfrom, pto);

//            True variant
//            return new ProviderProduct(opu, fromC.getNonScalarContent(), toC.getNonScalarContent(), allowDiffStates);
        }
    };

    private static Boolean compareFactors(Complex c1, Complex c2) {
        if (c1.equals(c2))
            return Boolean.FALSE;
        if (c1.equals(c2.negate()))
            return Boolean.TRUE;
        return null;
    }

    private static boolean testScalars(Tensor[] from, Tensor[] to) {
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
                if (!mappingExists(from[stretch.from], to[stretch.from]))
                    return false;
        OUTER:
        for (Stretch stretch : precalculatedStretches)
            if (stretch.length > 1) {
                SEMIOUTER:
                for (int[] permutation : new IntPermutationsGenerator(stretch.length)) {
                    for (i = 0; i < stretch.length; ++i)
                        if (!mappingExists(from[stretch.from + i], to[stretch.from + permutation[i]]))
                            continue SEMIOUTER; // This permutation is bad
                    continue OUTER; //Good permutation has been found
                }
                return false; //No good combinatorics found
            }
        return true;
    }

    private static boolean mappingExists(Tensor from, Tensor to) {
        final IndexMappingProvider pp = IndexMappings.createPort(
                IndexMappingProvider.Util.singleton(new IndexMappingBufferImpl()),
                from, to);
        pp.tick();
        return pp.take() != null;
    }
//    private final ProductContent from, to;
//    private PermutationsProvider permutationsProvider;
    private final DummyIndexMappingProvider dummyProvider;
    private final MappingsPort op;

    private ProviderProduct(final MappingsPort opu,
                            final Product from, final Product to) {
        this.dummyProvider = new DummyIndexMappingProvider(opu);
//        this.from = from;
//        this.to = to;
        int begin = 0;
        int i;
        ProductContent fromContent = from.getContent(),
                toContent = to.getContent();

//        List<PermutationsProvider> disjointProviders = new ArrayList<>();
        List<Pair> stretches = new ArrayList<>();
        //non permutable
        List<IndexMappingProvider> providers = new ArrayList<>();

        IndexMappingProvider lastOutput = dummyProvider;

        Tensor[] indexlessFrom = from.getIndexless(), indexlessTo = to.getIndexless();

        for (i = 1; i <= indexlessFrom.length; ++i)
            if (i == indexlessFrom.length || indexlessFrom[i].hashCode() != indexlessFrom[i - 1].hashCode()) {
                if (i - 1 != begin)
                    providers.add(lastOutput =
                            new PermutatorProvider(lastOutput, Arrays.copyOfRange(indexlessFrom, begin, i),
                                                   Arrays.copyOfRange(indexlessTo, begin, i)));
                begin = i;
            }

        begin = 0;
        for (i = 1; i <= indexlessFrom.length; ++i)
            if (i == indexlessFrom.length || indexlessFrom[i].hashCode() != indexlessFrom[i - 1].hashCode()) {
                if (i - 1 == begin)
                    providers.add(lastOutput =
                            IndexMappings.createPort(lastOutput,
                                                     indexlessFrom[begin],
                                                     indexlessTo[begin]));
                begin = i;
            }

        begin = 0;
        for (i = 1; i <= fromContent.size(); ++i)
            if (i == fromContent.size() || !fromContent.getContractionStructure().get(i).equals(fromContent.getContractionStructure().get(i - 1))) {
                if (i - 1 != begin)
                    stretches.add(new Pair(fromContent.getRange(begin, i), toContent.getRange(begin, i)));
                else
                    providers.add(lastOutput =
                            IndexMappings.createPort(lastOutput,
                                                     fromContent.get(begin),
                                                     toContent.get(begin)));
                begin = i;
            }

        Collections.sort(stretches);

//        if (!npFrom.isEmpty())
//            lastOutput = new SimpleProductMappingsPort(dummyProvider,
//                                                   npFrom.toArray(new Tensor[npFrom.size()]),
//                                                   npTo.toArray(new Tensor[npTo.size()]), allowDiffStates);

//        if (stretches.isEmpty())
//            this.op = lastOutput;
//        else
        for (Pair p : stretches)
            providers.add(lastOutput = new PermutatorProvider(lastOutput,
                                                              p.from, p.to));

        this.op = new SimpleProductMappingsPort(providers.toArray(new IndexMappingProvider[providers.size()]));
    }

    @Override
    public boolean tick() {
        return dummyProvider.tick();
    }

    @Override
    public IndexMappingBuffer take() {
        IndexMappingBuffer buffer = op.take();
        if (buffer == null)
            return null;
        buffer.removeContracted();
        return buffer;
    }

    protected static class Pair implements Comparable<Pair> {

        public final Tensor[] from, to;

        public Pair(final Tensor[] from, final Tensor[] to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int compareTo(Pair o) {
            return Integer.compare(from.length, o.from.length);
        }
    }
}
