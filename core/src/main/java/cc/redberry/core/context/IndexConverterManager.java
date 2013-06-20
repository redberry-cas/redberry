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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndexType;
import gnu.trove.set.hash.TByteHashSet;

/**
 * This class is responsible for the mapping between strings and
 * internal integer Redberry ids of single tensor index.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IndexConverterManager {
    /**
     * The default index converter manager defined for all types of indices defined in enum {@link IndexType}.
     */
    public static final IndexConverterManager DEFAULT = new IndexConverterManager(IndexType.getAllConverters());
    private final IndexSymbolConverter[] converters;

    /**
     * Creates index converted manager for specified index converters.
     *
     * @param converters index converters
     * @throws IllegalArgumentException if several converters have the same index type
     */
    public IndexConverterManager(IndexSymbolConverter[] converters) {
        TByteHashSet types = new TByteHashSet(converters.length);
        for (IndexSymbolConverter converter : converters) {
            if (types.contains(converter.getType()))
                throw new IllegalArgumentException("Several converters for same type.");
            types.add(converter.getType());
        }
        this.converters = converters;
    }

    /**
     * Returns string representation from specified integer representation of single
     * index in the specified {@code outputFormat}.
     *
     * @param code         integer representation of single index
     * @param outputFormat output format to be used to produce string representation
     * @return string representation of specified integer index
     * @throws IllegalArgumentException if rule for specified code
     */
    public String getSymbol(int code, OutputFormat outputFormat) {
        byte typeId = (byte) ((code >>> 24) & 0x7F);
        int number = code & 0xFFFF;
        try {
            for (IndexSymbolConverter converter : converters)
                if (converter.getType() == typeId) {
                    return converter.getSymbol(number, outputFormat);//symbol.length() == 1 ? symbol : symbol + " ";
                }
            throw new IllegalArgumentException("No appropriate converter for typeId 0x" + Integer.toHexString(typeId));
        } catch (IndexConverterException e) {
            throw new IllegalArgumentException("Index 0x" + Integer.toHexString(code) + " conversion error");
        }
    }

    /**
     * Returns integer representation from specified string representation of single
     * index.
     *
     * @param index string representation of single index
     * @return integer representation of specified index
     * @throws IllegalArgumentException if rule for specified string
     */
    public int getCode(String index) {
        try {
            for (IndexSymbolConverter converter : converters)
                if (converter.applicableToSymbol(index))
                    return (converter.getCode(index) & 0xFFFF) | ((converter.getType() & 0x7F) << 24);
            throw new IllegalArgumentException("No available converters for such symbol : " + index);
        } catch (IndexConverterException e) {
            throw new IllegalArgumentException("No available converters for such symbol : " + index);
        }
    }
}
