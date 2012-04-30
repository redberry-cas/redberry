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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndexType;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class IndexConverterManager {
    public static final IndexConverterManager DEFAULT = new IndexConverterManager(IndexType.getAllConverters());
    private final IndexSymbolConverter[] converters;

    public IndexConverterManager(IndexSymbolConverter[] converters) {
        //Asserted different types
        this.converters = converters;
    }

    public String getSymbol(int code, ToStringMode mode) {
        byte typeId = (byte) ((code >>> 24) & 0x7F);
        int number = code & 0xFFFF;
        try {
            for (IndexSymbolConverter converter : converters)
                if (converter.getType() == typeId) {
                    String symbol = converter.getSymbol(number, mode);
                    return symbol.length() == 1 ? symbol : symbol + " ";
                }
            throw new RuntimeException("No appropriate converter for typeId 0x" + Integer.toHexString(typeId));
        } catch (IndexConverterException e) {
            throw new RuntimeException("Index 0x" + Integer.toHexString(code) + " conversion error");
        }
    }

    public int getCode(String symbol) {
        try {
            for (IndexSymbolConverter converter : converters)
                if (converter.applicableToSymbol(symbol))
                    return (converter.getCode(symbol) & 0xFFFF) | ((converter.getType() & 0x7F) << 24);
            throw new RuntimeException("No available converters for such symbol : " + symbol);
        } catch (IndexConverterException e) {
            throw new RuntimeException("Index " + symbol + " conversion error");
        }
    }
}
