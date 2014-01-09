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

import java.util.Iterator;

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
    private Iterator<Permutation> symmetryIterator;

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

        if (symmetryIterator != null) {
            OUT:
            while (symmetryIterator.hasNext()) {
                Permutation s = symmetryIterator.next();
                IndexMappingBuffer tempBuffer = currentBuffer.clone();
                for (int i = 0; i < size; ++i)
                    if (!tempBuffer.tryMap(fromIndices.get(s.newIndexOf(i)), toIndices.get(i)))
                        continue OUT;
                tempBuffer.addSign(s.antisymmetry());
                return tempBuffer;
            }
            symmetryIterator = null;
            currentBuffer = null;
            return null;
        }
        if (fromIndices.getSymmetries().isTrivial()) {
            IndexMappingBuffer tempBuffer = currentBuffer;
            for (int i = 0; i < size; ++i)
                if (!tempBuffer.tryMap(fromIndices.get(i), toIndices.get(i))) {
                    currentBuffer = null;
                    return null;
                }
            currentBuffer = null;
            return tempBuffer;
        }
        symmetryIterator = fromIndices.getSymmetries().iterator();
        return take();
    }

    @Override
    protected void _tick() {
        symmetryIterator = null;
        currentBuffer = null;
    }
}
