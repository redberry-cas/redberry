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
package cc.redberry.core.indexmapping;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.utils.IntArrayList;

import java.util.Iterator;

import static cc.redberry.core.indices.IndicesUtils.*;

/**
 * {@link SimpleTensor}-specific mapping provider.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
final class ProviderSimpleTensor extends IndexMappingProviderAbstractFT<SimpleTensor> {

    public static final IndexMappingProviderFactory FACTORY_SIMPLETENSOR = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            if (((SimpleTensor) from).getName() != ((SimpleTensor) to).getName())
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            if (from.getIndices().size() == 0)
                return new DummyIndexMappingProvider(opu);
            return new ProviderSimpleTensor(opu, (SimpleTensor) from, (SimpleTensor) to);
        }
    };
    public static final IndexMappingProviderFactory FACTORY_TENSORFIELD = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            if (((TensorField) from).getName() != ((TensorField) to).getName())
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            for (int i = 0; i < from.size(); ++i) {
                if (!IndexMappings.positiveMappingExists(from.get(i), to.get(i)))
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
            }
            return new ProviderSimpleTensor(opu, (SimpleTensor) from, (SimpleTensor) to);
        }
    };
    private Iterator<Permutation> searchForPermutations;

    private ProviderSimpleTensor(OutputPortUnsafe<IndexMappingBuffer> opu, SimpleTensor from, SimpleTensor to) {
        super(opu, from, to);
    }

    @Override
    public IndexMappingBuffer take() {
        if (currentBuffer == null)
            return null;

        SimpleIndices fromIndices = from.getIndices();
        SimpleIndices toIndices = to.getIndices();
        int size = fromIndices.size();
        if (size == 0) {
            IndexMappingBuffer r = currentBuffer;
            currentBuffer = null;
            return r;
        }

        if (searchForPermutations != null) {
            Permutation permutation;
            out:
            while (searchForPermutations.hasNext()) {
                permutation = searchForPermutations.next();
                IndexMappingBuffer tempBuffer = currentBuffer.clone();
                for (int i = 0; i < size; ++i)
                    if (!tempBuffer.tryMap(fromIndices.get(i), toIndices.get(permutation.newIndexOf(i))))
                        continue out;
                tempBuffer.addSign(permutation.antisymmetry());
                return tempBuffer;
            }
            searchForPermutations = null;
            return currentBuffer = null;
        }

        if (fromIndices.size() == 1 || fromIndices.getSymmetries().isTrivial()) {
            IndexMappingBuffer tempBuffer = currentBuffer;
            for (int i = 0; i < size; ++i)
                if (!tempBuffer.tryMap(fromIndices.get(i), toIndices.get(i)))
                    return currentBuffer = null;
            currentBuffer = null;
            return tempBuffer;
        }

        //try to find partial mapping
        IntArrayList permMappingFrom = null, permMappingTo = null;
        outer:
        for (int mapFrom = 0; mapFrom < size; ++mapFrom) {
            int fromIndex = fromIndices.get(mapFrom);
            IndexMappingBufferRecord bRec = currentBuffer.getMap().get(getNameWithType(fromIndex));
            //no such index in mapping yet
            if (bRec == null)
                continue;
            //index contained in mapping have same state
            if (getRawStateInt(fromIndex) == bRec.getFromRawState())
                return currentBuffer = null;
            //toIndex that we'll find in toIndices
            int toIndex = inverseIndexState(setRawState(bRec.getToRawState(), bRec.getIndexName()));
            for (int mapTo = 0; mapTo < size; ++mapTo) {
                if (toIndices.get(mapTo) == toIndex) {
                    if (permMappingFrom == null) {
                        permMappingFrom = new IntArrayList();
                        permMappingTo = new IntArrayList();
                    }
                    permMappingFrom.add(mapFrom);
                    permMappingTo.add(mapTo);
                    continue outer;
                }
            }
            //no index found in toIndices
            return currentBuffer = null;
        }
        if (permMappingFrom == null)
            searchForPermutations = fromIndices.getSymmetries().getPermutationGroup().iterator();
        else
            searchForPermutations = new OutputPortUnsafe.PortIterator<>(
                    fromIndices.getSymmetries().getPermutationGroup().mapping(
                            permMappingFrom.toArray(), permMappingTo.toArray()));
        return take();
    }

    @Override
    protected void _tick() {
        searchForPermutations = null;
        currentBuffer = null;
    }
}
