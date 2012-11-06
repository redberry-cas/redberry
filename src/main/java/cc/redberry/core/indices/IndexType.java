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
import cc.redberry.core.context.defaults.IndexConverterExtender;
import cc.redberry.core.context.defaults.IndexWithStrokeWrapper;
import cc.redberry.core.context.defaults.LatinSymbolDownCaseConverter;

import static cc.redberry.core.context.defaults.IndexConverterExtender.*;

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
    LatinLower(LatinLowerEx),
    LatinUpper(LatinUpperEx),
    GreekLower(GreekLowerEx),
    GreekUpper(GreekUpperEx),
    LatinLower1(new IndexWithStrokeWrapper(LatinLowerEx, (byte) 1)),
    LatinUpper1(new IndexWithStrokeWrapper(LatinUpperEx, (byte) 1)),
    GreekLower1(new IndexWithStrokeWrapper(GreekLowerEx, (byte) 1)),
    GreekUpper1(new IndexWithStrokeWrapper(GreekUpperEx, (byte) 1));

    private static IndexSymbolConverter LatinLowerExtender
            = new IndexConverterExtender(LatinSymbolDownCaseConverter.INSTANCE);


    /**
     * Total number of available index types
     */
    public static final byte TYPES_COUNT = 8;//redundant

    /**
     * Total number of alphabets is 4: latin lower,
     * latin upper, greek lower, greek upper
     */
    public static final byte ALPHABETS_COUNT = 4;//redundant

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

    public static byte[] getBytes() {
        byte[] bytes = new byte[TYPES_COUNT];
        for (byte i = 0; i < TYPES_COUNT; ++i)
            bytes[i] = i;
        return bytes;
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
}
