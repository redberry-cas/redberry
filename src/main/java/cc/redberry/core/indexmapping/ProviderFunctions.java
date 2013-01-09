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

import cc.redberry.core.tensor.Tensor;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProviderFunctions {

    static final IndexMappingProviderFactory ODD_FACTORY = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            MappingsPort mp = IndexMappings.createPort(from.get(0), to.get(0));
            IndexMappingBuffer buffer;

            byte state = 0;
            while ((buffer = mp.take()) != null) {
                state |= (byte) (buffer.getSign() ? 0x10 : 0x01);
                if (state == 0x11)
                    break;
            }
            switch (state) {
                case 0x00:
                    return IndexMappingProvider.Util.EMPTY_PROVIDER;
                case 0x01:
                    return new DummyIndexMappingProvider(opu);
                case 0x10:
                    return new MinusIndexMappingProvider(opu);
                case 0x11:
                    return new PlusMinusIndexMappingProvider(opu);
                default:
                    throw new RuntimeException("Ups");
            }
        }
    };
    static final IndexMappingProviderFactory EVEN_FACTORY = new IndexMappingProviderFactory() {

        @Override
        public IndexMappingProvider create(IndexMappingProvider opu, Tensor from, Tensor to) {
            if (IndexMappings.createPort(from.get(0), to.get(0)).take() != null)
                return new DummyIndexMappingProvider(opu);
            return IndexMappingProvider.Util.EMPTY_PROVIDER;
        }
    };
}
