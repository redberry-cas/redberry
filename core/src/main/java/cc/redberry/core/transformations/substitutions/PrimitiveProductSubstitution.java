/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.indexmapping.IndexMappings.createBijectiveProductPort;
import static cc.redberry.core.indexmapping.IndexMappings.getFirst;
import static cc.redberry.core.tensor.ApplyIndexMapping.applyIndexMapping;
import static cc.redberry.core.tensor.ApplyIndexMapping.applyIndexMappingAndRenameAllDummies;
import static cc.redberry.core.tensor.StructureOfContractions.getToTensorIndex;
import static cc.redberry.core.utils.TensorUtils.getAllIndicesNamesT;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class PrimitiveProductSubstitution extends PrimitiveSubstitution {
    private final Complex fromFactor;
    private final Tensor[] fromIndexless, fromData;
    private final Tensor indexlessSubProductReciprocal;
    private final ProductContent fromContent;
    private final boolean zeroRhs;
    final boolean simpleContractions;
    final boolean simpleCombinations;

    public PrimitiveProductSubstitution(Tensor from, Tensor to) {
        super(from, to);
        Product product = (Product) from;
        this.fromFactor = product.getFactor();
        this.fromIndexless = product.getIndexless();
        this.indexlessSubProductReciprocal = Tensors.reciprocal(product.getIndexlessSubProduct());
        this.fromContent = product.getContent();
        this.fromData = fromContent.getDataCopy();
        this.simpleContractions = fromContent.size() == 2 && product.getIndices().size() > product.getIndices().getFree().size();
        this.simpleCombinations = fromContent.size() == 2;
        this.zeroRhs = TensorUtils.isZeroOrIndeterminate(to);
    }

    @Override
    Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator) {
        Product product = (Product) currentNode;
        //early termination
        if (product.sizeWithoutFactor() < ((Product) from).sizeWithoutFactor())
            return currentNode;
        if (product.sizeOfDataPart() < ((Product) from).sizeOfDataPart())
            return currentNode;
        ProductContent content = product.getContent();
        if (fromContent.size() > 0 &&
                (content.first().hashCode() > fromContent.first().hashCode()
                        || content.last().hashCode() < fromContent.last().hashCode()))
            return currentNode;


        //complete subgraph search
        return algorithm_subgraph_search(product, iterator);
    }

    /* COMPLETE SUBGRAPH SEARCH */

    Tensor algorithm_subgraph_search(final Product product, final SubstitutionIterator iterator) {
        Complex factor = product.getFactor();
        PContent content = new PContent(product.getIndexless(), product.getDataSubProduct());
        ForbiddenContainer forbidden = new ForbiddenContainer();
        SubsResult subsResult = atomic_substitute(content, forbidden, iterator);
        if (subsResult == null)
            return product;

        List<Tensor> newTos = new ArrayList<>();
        while (true) {
            if (subsResult == null)
                break;
            factor = factor.divide(fromFactor);
            newTos.add(subsResult.newTo);
            content = subsResult.remainder;
            subsResult = atomic_substitute(content, forbidden, iterator);
        }
        Tensor[] result = new Tensor[newTos.size() + content.indexless.length + 2];
        System.arraycopy(newTos.toArray(new Tensor[newTos.size()]), 0, result, 0, newTos.size());
        System.arraycopy(content.indexless, 0, result, newTos.size(), content.indexless.length);
        result[result.length - 2] = content.data;
        result[result.length - 1] = factor;
        return Tensors.multiply(result);
    }

    private SubsResult atomic_substitute(final PContent content, final ForbiddenContainer forbidden,
                                         final SubstitutionIterator iterator) {
        Mapping mapping = null;
        int[] indexlessBijection, dataBijection;

        IndexlessBijectionsPort indexlessPort
                = new IndexlessBijectionsPort(fromIndexless, content.indexless);

        while ((indexlessBijection = indexlessPort.take()) != null) {
            mapping = createBijectiveProductPort(fromIndexless, extract(content.indexless, indexlessBijection)).take();
            if (mapping != null)
                break;
        }

        if (mapping == null)
            return null;

        boolean sign = mapping.getSign();
        mapping = null;

        Tensor[] currentData;
        if (content.data instanceof Product) {
            ProductContent currentContent = ((Product) content.data).getContent();
            currentData = currentContent.getDataCopy();
            ProductsBijectionsPort dataPort = new ProductsBijectionsPort(fromContent, currentContent);
            while ((dataBijection = dataPort.take()) != null) {
                mapping = createBijectiveProductPort(fromData, extract(currentData, dataBijection)).take();
                if (mapping != null)
                    break;
            }
        } else {
            if (TensorUtils.isOne(content.data)) {
                if (fromContent.size() != 0)
                    return null;
                dataBijection = new int[0];
                currentData = new Tensor[0];
                mapping = Mapping.IDENTITY_MAPPING;
            } else {
                if (fromContent.size() != 1)
                    return null;
                dataBijection = new int[1];
                currentData = new Tensor[]{content.data};
                mapping = getFirst(fromContent.get(0), content.data);
            }
        }

        if (mapping == null)
            return null;

        mapping = mapping.addSign(sign);
        Arrays.sort(indexlessBijection);
        Arrays.sort(dataBijection);

        Tensor[] indexlessRemainder = new Tensor[content.indexless.length - fromIndexless.length];
        ProductBuilder dataRemainder = new ProductBuilder(0,
                (content.data instanceof Product)
                        ? content.data.size()
                        : 1 - fromContent.size());
        int pivot = 0;
        int i, j = 0;
        for (i = 0; i < content.indexless.length; ++i) {
            if (pivot < indexlessBijection.length && i == indexlessBijection[pivot])
                ++pivot;
            else
                indexlessRemainder[j++] = content.indexless[i];
        }
        pivot = 0;
        for (i = 0; i < currentData.length; ++i) {
            if (pivot < dataBijection.length && i == dataBijection[pivot])
                ++pivot;
            else
                dataRemainder.put(currentData[i]);
        }
        Tensor dataRemainderT = dataRemainder.build();

        PContent remainder = new PContent(indexlessRemainder, dataRemainderT);

        Tensor newTo;
        if (toIsSymbolic)
            newTo = mapping.getSign() ? Tensors.negate(to) : to;
        else {
            if (possiblyAddsDummies) {
                if (forbidden.forbidden == null)
                    forbidden.forbidden = new TIntHashSet(iterator.getForbidden());

                TIntHashSet remainderIndices = new TIntHashSet(forbidden.forbidden);
                remainderIndices.addAll(getAllIndicesNamesT(indexlessRemainder));
                remainderIndices.addAll(getAllIndicesNamesT(dataRemainderT));
                newTo = applyIndexMapping(to, mapping, remainderIndices.toArray());
                forbidden.forbidden.addAll(getAllIndicesNamesT(newTo));
            } else {
                TIntHashSet allowed = new TIntHashSet();
                for (int index : indexlessBijection)
                    allowed.addAll(TensorUtils.getAllDummyIndicesT(content.indexless[index]));
                IndicesBuilder ib = new IndicesBuilder();
                for (int index : dataBijection) {
                    allowed.addAll(TensorUtils.getAllDummyIndicesT(currentData[index]));
                    ib.append(currentData[index]);
                }
                allowed.addAll(ib.getIndices().getNamesOfDummies());
                allowed.removeAll(IndicesUtils.getIndicesNames(mapping.getToData()));
                newTo = applyIndexMappingAndRenameAllDummies(to, mapping, allowed.toArray());
            }
        }
        return new SubsResult(newTo, remainder);
    }

    private static final class ForbiddenContainer {
        TIntHashSet forbidden = null;
    }

    private static Tensor[] extract(final Tensor[] source, final int[] positions) {
        Tensor[] r = new Tensor[positions.length];
        for (int i = 0; i < positions.length; ++i)
            r[i] = source[positions[i]];
        return r;
    }

    private static final class SubsResult {
        final Tensor newTo;
        final PContent remainder;

        private SubsResult(Tensor newTo, PContent remainder) {
            this.newTo = newTo;
            this.remainder = remainder;
        }
    }

    private static final class PContent {
        final Tensor[] indexless;
        final Tensor data;

        private PContent(Tensor[] indexless, Tensor data) {
            this.indexless = indexless;
            this.data = data;
        }
    }


    /* FAST BRUTE FORCE OPTIMIZATIONS */

    public static ResultContained algorithm_with_simple_contractions(final Product cProduct,
                                                                     final PrimitiveProductSubstitution[] subs,
                                                                     final SubstitutionIterator iterator) {
        final ProductContent content = cProduct.getContent();
        final StructureOfContractions st = content.getStructureOfContractions();

        int first;
        final int cSize = content.size();
        final int[] mask = new int[cSize];
        Arrays.fill(mask, 0);
        final int[] rPowers = new int[subs.length];
        Arrays.fill(rPowers, 0);

        final ArrayList<Tensor> newTo = new ArrayList<>();
        final IntArrayList toRemove = new IntArrayList();
        final int offset = cProduct.sizeOfIndexlessPart();

        boolean supposeIndicesAreAdded = false;
        for (int iSub = 0; iSub < subs.length; iSub++) {
            assert subs[iSub].simpleContractions;

            final int firstHash = subs[iSub].fromContent.get(0).hashCode(),
                    secondHash = subs[iSub].fromContent.get(1).hashCode();

            for (first = 0; first < cSize; ++first)
                if (content.get(first).hashCode() == firstHash)
                    break;
            if (first == cSize)
                continue;

            out:
            for (; first < cSize && content.get(first).hashCode() == firstHash; ++first) {
                if (mask[first] != 0)
                    continue;
                for (long contraction : st.contractions[first]) {
                    int second = getToTensorIndex(contraction);
                    if (second == -1 || mask[second] != 0 || first == second
                            || content.get(second).hashCode() != secondHash)
                        continue;
                    Mapping mapping = IndexMappings.createBijectiveProductPort(subs[iSub].fromData, new Tensor[]{content.get(first), content.get(second)}).take();
                    if (mapping == null && firstHash == secondHash)
                        mapping = IndexMappings.createBijectiveProductPort(subs[iSub].fromData, new Tensor[]{content.get(second), content.get(first)}).take();

                    if (mapping == null)
                        continue;

                    if (subs[iSub].zeroRhs)
                        return new ResultContained(subs[iSub].to);

                    supposeIndicesAreAdded |= subs[iSub].possiblyAddsDummies;
                    final TIntHashSet dummies = TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(content.get(first));
                    dummies.addAll(TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(content.get(second)));
                    dummies.addAll(new IndicesBuilder().append(content.get(first)).append(content.get(second)).getIndices().getNamesOfDummies());

                    newTo.add(subs[iSub].applyIndexMappingToTo(dummies.toArray(), subs[iSub].to, mapping, iterator));
                    ++rPowers[iSub];
                    mask[first] = mask[second] = -1;
                    toRemove.ensureCapacity(2);
                    toRemove.add(offset + first);
                    toRemove.add(offset + second);
                    continue out;
                }
            }

        }
        return new ResultContained(__build__(subs, cProduct, rPowers, toRemove, newTo), supposeIndicesAreAdded);
    }

    public static ResultContained algorithm_with_simple_combinations(final Product cProduct,
                                                                     final PrimitiveProductSubstitution[] subs,
                                                                     final SubstitutionIterator iterator) {
        final ProductContent content = cProduct.getContent();
        final int cSize = content.size();
        final int offset = cProduct.sizeOfIndexlessPart();
        final int[] mask = new int[cSize];
        Arrays.fill(mask, 0);
        final int[] rPowers = new int[subs.length];
        Arrays.fill(rPowers, 0);
        final ArrayList<Tensor> newTo = new ArrayList<>();
        final IntArrayList toRemove = new IntArrayList();
        boolean possiblyAddsDummies = false;

        for (int iSub = 0; iSub < subs.length; ++iSub) {
            assert subs[iSub].simpleCombinations;

            final int firstHash = subs[iSub].fromContent.get(0).hashCode(),
                    secondHash = subs[iSub].fromContent.get(1).hashCode();

            assert firstHash <= secondHash;

            if (content.get(0).hashCode() > firstHash
                    || content.get(cSize - 1).hashCode() < secondHash)
                continue;

            int firstBegin, firstEnd, secondBegin, secondEnd;
            for (firstBegin = 0; firstBegin < cSize; ++firstBegin)
                if (content.get(firstBegin).hashCode() == firstHash)
                    break;
            if (firstBegin == cSize)
                continue;

            for (firstEnd = firstBegin + 1; firstEnd < cSize; ++firstEnd)
                if (content.get(firstEnd).hashCode() != firstHash)
                    break;

            if (secondHash == firstHash) {
                secondBegin = firstBegin;
                secondEnd = firstEnd;
            } else {
                for (secondBegin = firstEnd; secondBegin < cSize; ++secondBegin)
                    if (content.get(secondBegin).hashCode() == secondHash)
                        break;
                if (secondBegin == cSize)
                    continue;

                for (secondEnd = secondBegin + 1; secondEnd < cSize; ++secondEnd)
                    if (content.get(secondEnd).hashCode() != secondHash)
                        break;
            }

            out:
            for (int i = firstBegin; i < firstEnd; ++i) {
                if (mask[i] != 0)
                    continue;
                for (int j = secondBegin; j < secondEnd; ++j) {
                    if (i == j || mask[j] != 0)
                        continue;
                    Mapping mapping = IndexMappings.createBijectiveProductPort(subs[iSub].fromData, new Tensor[]{content.get(i), content.get(j)}).take();
                    if (mapping == null && firstHash == secondHash)
                        mapping = IndexMappings.createBijectiveProductPort(subs[iSub].fromData, new Tensor[]{content.get(j), content.get(i)}).take();

                    if (mapping == null)
                        continue;

                    if (subs[iSub].zeroRhs)
                        return new ResultContained(subs[iSub].to);

                    possiblyAddsDummies |= subs[iSub].possiblyAddsDummies;
                    final TIntHashSet dummies = TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(content.get(i));
                    dummies.addAll(TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(content.get(j)));
                    dummies.addAll(new IndicesBuilder().append(content.get(i)).append(content.get(j)).getIndices().getNamesOfDummies());

                    newTo.add(subs[iSub].applyIndexMappingToTo(dummies.toArray(), subs[iSub].to, mapping, iterator));
                    ++rPowers[iSub];

                    mask[i] = mask[j] = -1;
                    toRemove.ensureCapacity(2);
                    toRemove.add(offset + i);
                    toRemove.add(offset + j);
                    continue out;
                }
            }
        }
        return new ResultContained(__build__(subs, cProduct, rPowers, toRemove, newTo), possiblyAddsDummies);
    }

    static class ResultContained {
        final Tensor result;
        final boolean possiblyAddsDummies;

        public ResultContained(Tensor result) {
            this(result, false);
        }

        public ResultContained(Tensor result, boolean possiblyAddsDummies) {
            this.result = result;
            this.possiblyAddsDummies = possiblyAddsDummies;
        }
    }

    private static Tensor __build__(final PrimitiveProductSubstitution[] subs,
                                    final Product cProduct,
                                    final int[] rPowers,
                                    final IntArrayList toRemove,
                                    final ArrayList<Tensor> newTo) {

        if (newTo.isEmpty())
            return cProduct;

        newTo.ensureCapacity(2);
        boolean possiblyAddsDummies = false;
        for (int i = 0; i < rPowers.length; ++i) {
            possiblyAddsDummies |= subs[i].possiblyAddsDummies;
            if (rPowers[i] != 0)
                newTo.add(Tensors.pow(subs[i].indexlessSubProductReciprocal, rPowers[i]));
        }
        newTo.add(cProduct.remove(toRemove.toArray()));
        final Tensor result = Tensors.multiplyAndRenameConflictingDummies(newTo);
        if (possiblyAddsDummies)
            //dummies will be renamed later automatically
            return result;
        else
            //manual dummies renaming
            return ApplyIndexMapping.renameDummy(result,
                    TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(result).toArray(),
                    TensorUtils.getAllDummyIndicesIncludingScalarFunctionsT(cProduct).toArray());
    }
}
