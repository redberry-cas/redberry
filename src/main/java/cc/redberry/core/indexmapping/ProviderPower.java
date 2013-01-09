/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.tensor.Tensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
class ProviderPower implements IndexMappingProviderFactory {

    static final ProviderPower INSTANCE = new ProviderPower();

    private ProviderPower() {
    }

    @Override
    public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
        IndexMappingBuffer exponentMapping = IndexMappings.getFirst(from.get(1), to.get(1));   //todo try get first positive mapping
        if (exponentMapping == null || exponentMapping.getSign())
            return IndexMappingProvider.Util.EMPTY_PROVIDER;

        //todo two signs are possible
        IndexMappingBuffer baseMapping = IndexMappings.getFirst(from.get(0), to.get(0));
        if (baseMapping == null)
            return IndexMappingProvider.Util.EMPTY_PROVIDER;

        if (baseMapping.getSign() == false)
            return new DummyIndexMappingProvider(opu);
        if (!(from.get(1) instanceof Complex))
            return IndexMappingProvider.Util.EMPTY_PROVIDER;

        assert to.get(1) instanceof Complex;
        Complex exponent = (Complex) from.get(1);
        if (NumberUtils.isIntegerEven(exponent))
            return new DummyIndexMappingProvider(opu);
        if (NumberUtils.isIntegerOdd(exponent))
            return new MinusIndexMappingProvider(opu);

        return IndexMappingProvider.Util.EMPTY_PROVIDER;
    }
}
