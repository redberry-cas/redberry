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
package cc.redberry.core.indices;

import cc.redberry.core.context.ContextSettings;
import cc.redberry.core.context.IndexSymbolConverter;
import cc.redberry.core.context.defaults.*;

/**
 * This {@code enum} is a container of the information on all available index
 * types and appropriate converters. This {@code enum} is scanning at the
 * initialization of {@link ContextSettings} and all the values are putting in
 * the Context as default indices types.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public enum IndexType {
    LatinUpper(new IndexConverterExtender(LatinSymbolUpperCaseConverter.INSTANCE)),
    LatinLower(new IndexConverterExtender(LatinSymbolDownCaseConverter.INSTANCE)),
    GreekUpper(new IndexConverterExtender(GreekLaTeXUpperCaseConverter.INSTANCE)),
    GreekLower(new IndexConverterExtender(GreekLaTeXDownCaseConverter.INSTANCE));
    /**
     * Total number of available index types
     */
    public static final int TYPES_COUNT = 4;//redundant
    private final IndexSymbolConverter converter;

    private IndexType(IndexSymbolConverter converter) {
        this.converter = converter;
    }

    /**
     * Returns the appropriate string<->integer converter for this index type.
     *
     * @return the appropriate string<->integer converter for this index type
     * @see IndexSymbolConverter
     */
    public IndexSymbolConverter getSymbolConverter() {
        return converter;
    }

    /**
     * Returns the types bits corresponding to this index type.
     *
     * @return types bits corresponding to this index type.
     */
    public byte getType() {
        return converter.getType();
    }

    public static IndexType getType(byte type) {
        for (IndexType indexType : IndexType.values())
            if (indexType.getType() == type)
                return indexType;
        throw new IllegalArgumentException("Now such type: " + type);
    }

    public static IndexSymbolConverter[] getAllConverters() {
        IndexSymbolConverter[] converters = new IndexSymbolConverter[values().length];
        int i = -1;
        for (IndexType type : values())
            converters[++i] = type.getSymbolConverter();
        return converters;
    }

    public static boolean isLatin(IndexType type) {
        return type == LatinLower || type == LatinUpper;
    }
}
