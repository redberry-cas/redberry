/*
 * org.redberry.concurrent: high-level Java concurrent library.
 * Copyright (c) 2010-2012.
 * Bolotin Dmitriy <bolotin.dmitriy@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 */

package cc.redberry.core.indexmapping;

public class MinusIndexMappingProvider extends IndexMappingProviderAbstract {

    public MinusIndexMappingProvider(MappingsPort opu) {
        super(opu);
    }

    @Override
    public IndexMappingBuffer take() {
        if (currentBuffer == null)
            return null;
        IndexMappingBuffer buf = currentBuffer;
        currentBuffer = null;
        buf.addSignum(true);
        return buf;
    }
}
