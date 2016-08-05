/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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

import cc.redberry.core.context.VarDescriptor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.utils.OutputPort;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stanislav Poslavsky
 */
final class ProviderTensorField extends IndexMappingProviderAbstract {
    final SimpleProductMappingsPort headAndArgsProvider;
    final DummyIndexMappingProvider dummyProvider;

    public static final IndexMappingProviderFactory FACTORY_TENSORFIELD = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            TensorField fromF = (TensorField) from, toF = (TensorField) to;
            if (fromF.getHead().getName() != toF.getHead().getName() || fromF.size() != toF.size())
                return IndexMappingProvider.Util.EMPTY_PROVIDER;
            VarDescriptor headDescriptor = fromF.getHead().getVarDescriptor();
            boolean propagateArguments = false;
            for (int i = 0; i < from.size(); ++i) {
                if (headDescriptor.propagatesIndices(i))
                    propagateArguments = true;
                else if (!IndexMappings.positiveMappingExists(from.get(i), to.get(i)))
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
            }
            if (!propagateArguments)
                return ProviderSimpleTensor.FACTORY_SIMPLETENSOR.create(opu, fromF.getHead(), toF.getHead());
            return new ProviderTensorField(opu, fromF, toF);
        }
    };

    private ProviderTensorField(OutputPort<IndexMappingBuffer> opu, TensorField from, TensorField to) {
        super(opu);
        IndexMappingProvider pr;
        List<IndexMappingProvider> headAndArgsChain = new ArrayList<>();
        headAndArgsChain.add(pr = IndexMappings.createPort(dummyProvider = new DummyIndexMappingProvider(opu), from.getHead(), to.getHead()));
        VarDescriptor headDescriptor = from.getHead().getVarDescriptor();
        for (int i = 0; i < from.size(); ++i)
            if (headDescriptor.propagatesIndices(i))
                headAndArgsChain.add(pr = IndexMappings.createPort(pr, from.get(i), to.get(i)));
        headAndArgsProvider = new SimpleProductMappingsPort(headAndArgsChain.toArray(new IndexMappingProvider[headAndArgsChain.size()]));
    }

    @Override
    public boolean tick() {
        return dummyProvider.tick();
    }

    @Override
    public IndexMappingBuffer take() {
        IndexMappingBuffer buffer = headAndArgsProvider.take();
        if (buffer == null)
            return null;
        buffer.removeContracted();
        return buffer;
    }
}
