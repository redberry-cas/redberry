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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.indexmapping.IndexMappings.createBijectiveProductPort;
import static cc.redberry.core.indexmapping.IndexMappings.getFirst;
import static cc.redberry.core.tensor.ApplyIndexMapping.applyIndexMapping;
import static cc.redberry.core.tensor.ApplyIndexMapping.applyIndexMappingAndRenameAllDummies;
import static cc.redberry.core.utils.TensorUtils.getAllIndicesNamesT;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class PrimitiveProductSubstitution extends PrimitiveSubstitution {
    private final Complex fromFactor;
    private final Tensor[] fromIndexless, fromData;
    private final ProductContent fromContent;

    public PrimitiveProductSubstitution(Tensor from, Tensor to) {
        super(from, to);
        Product product = (Product) from;
        this.fromFactor = product.getFactor();
        this.fromIndexless = product.getIndexless();
        this.fromContent = product.getContent();
        this.fromData = fromContent.getDataCopy();
    }

//    @Override
//    Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator) {
//        TIntHashSet forbidden = null;
//        while (currentNode instanceof Product) {
//            Product cp = (Product) currentNode;
//            IndexMappingBuffer buffer = null;
//
//            final Tensor[] currentIndexless = cp.getIndexless();
//            int[] indexlessBijection;
//            IndexlessBijectionsPort indexlessPort = new IndexlessBijectionsPort(fromIndexless, currentIndexless);
//            while ((indexlessBijection = indexlessPort.take()) != null) {
//                buffer = createBijectiveProductPort(fromIndexless, extract(currentIndexless, indexlessBijection)).take();
//                if (buffer != null)
//                    break;
//            }
//            if (buffer == null)
//                break;
//
//            boolean sign = buffer.getSignum();
//            buffer = null;
//            ProductContent currentContent = cp.getContent();
//            final Tensor[] currentData = currentContent.getDataCopy();
//            int[] dataBijection;
//            ProductsBijectionsPort dataPort = new ProductsBijectionsPort(fromContent, currentContent);
//            while ((dataBijection = dataPort.take()) != null) {
//                buffer = createBijectiveProductPort(fromData, extract(currentData, dataBijection)).take();
//                if (buffer != null)
//                    break;
//            }
//            if (buffer == null)
//                break;
//
//            buffer.addSignum(sign);
//            Tensor newTo;
//            int i;
//            if (toIsSymbolic)
//                newTo = buffer.getSignum() ? Tensors.negate(to) : to;
//            else {
//                if (forbidden == null) {
//                    //TODO review
//                    forbidden = new TIntHashSet(iterator.getForbidden());
//                    int pivot = 0;
//                    for (i = 0; i < currentIndexless.length; ++i) {
//                        if (pivot < indexlessBijection.length && i == indexlessBijection[pivot])
//                            ++pivot;
//                        else
//                            forbidden.addAll(getAllIndicesNamesT(currentIndexless[i]));
//                    }
//                    pivot = 0;
//                    for (i = 0; i < currentData.length; ++i) {
//                        if (pivot < dataBijection.length && i == dataBijection[pivot])
//                            ++pivot;
//                        else
//                            forbidden.addAll(getAllIndicesNamesT(currentData[i]));
//                    }
//
//                }
//                newTo = applyIndexMapping(to, buffer, forbidden.toArray());
//                if (newTo != to)
//                    forbidden.addAll(getAllIndicesNamesT(newTo));
//            }
//
//            Arrays.sort(indexlessBijection);
//            Arrays.sort(dataBijection);
//
//            ProductBuilder builder = new ProductBuilder();
//            builder.put(newTo);
//
//            int pivot = 0;
//            for (i = 0; i < currentIndexless.length; ++i) {
//                if (pivot < indexlessBijection.length && i == indexlessBijection[pivot])
//                    ++pivot;
//                else
//                    builder.put(currentIndexless[i]);
//            }
//            pivot = 0;
//            for (i = 0; i < currentData.length; ++i) {
//                if (pivot < dataBijection.length && i == dataBijection[pivot])
//                    ++pivot;
//                else
//                    builder.put(currentData[i]);
//            }
//
//
//            builder.put(cp.getFactor().divide(fromFactor));
//            currentNode = builder.build();
//        }
//        return currentNode;
//    }

    @Override
    Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator) {
        Product product = (Product) currentNode;
        Complex factor = product.getFactor();
        PContent content = new PContent(product.getIndexless(), product.getDataSubProduct());

        //TODO getForbidden only if necessary!!!!!!!!!!!!!!!!!
        ForbiddenContainer forbidden = new ForbiddenContainer();
        SubsResult subsResult = atomicSubstitute(content, forbidden, iterator);
        if (subsResult == null)
            return currentNode;

        List<Tensor> newTos = new ArrayList<>();
        while (true) {
            if (subsResult == null)
                break;
            factor = factor.divide(fromFactor);
            newTos.add(subsResult.newTo);
            content = subsResult.remainder;
            subsResult = atomicSubstitute(content, forbidden, iterator);
        }
        Tensor[] result = new Tensor[newTos.size() + content.indexless.length + 2];
        System.arraycopy(newTos.toArray(new Tensor[newTos.size()]), 0, result, 0, newTos.size());
        System.arraycopy(content.indexless, 0, result, newTos.size(), content.indexless.length);
        result[result.length - 2] = content.data;
        result[result.length - 1] = factor;
        return Tensors.multiply(result);
    }

    SubsResult atomicSubstitute(PContent content, ForbiddenContainer forbidden, SubstitutionIterator iterator) {
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
                mapping = Mapping.IDENTITY;
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

}
