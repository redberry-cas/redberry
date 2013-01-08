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
package cc.redberry.core.context.defaults;

import cc.redberry.core.context.IndexConverterException;
import cc.redberry.core.context.IndexSymbolConverter;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;

/**
 * {@link IndexSymbolConverter} for letters with strokes (e.g. \\alpha' or A'').
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public final class IndexWithStrokeConverter implements IndexSymbolConverter {
    private final IndexSymbolConverter converter;
    private final byte numberOfStrokes;
    private final String strokesString;

    /**
     * Creates  {@link IndexSymbolConverter} for subscripted symbols from specified converter.
     *
     * @param converter       converter to be extended with symbols with strokes
     * @param numberOfStrokes number of strokes to be appended after symbol
     */
    public IndexWithStrokeConverter(IndexSymbolConverter converter, byte numberOfStrokes) {
        if (numberOfStrokes + converter.getType() > Byte.MAX_VALUE)
            throw new IllegalArgumentException("Too much strokes.");
        this.converter = converter;
        this.numberOfStrokes = numberOfStrokes;
        StringBuilder sb = new StringBuilder();
        while (numberOfStrokes-- > 0)
            sb.append('\'');
        strokesString = sb.toString();
    }

    private String getStrokes(String symbol) {
        return symbol.substring(symbol.length() - numberOfStrokes);
    }

    private String getBase(String symbol) {
        return symbol.substring(0, symbol.length() - numberOfStrokes);
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        if (symbol.length() <= strokesString.length())
            return false;
        if (!strokesString.equals(getStrokes(symbol)))
            return false;
        return converter.applicableToSymbol(getBase(symbol));
    }

    @Override
    public String getSymbol(int code, OutputFormat mode) throws IndexConverterException {
        return converter.getSymbol(code, mode) + strokesString;
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        return converter.getCode(getBase(symbol));
    }

    @Override
    public int maxNumberOfSymbols() {
        return converter.maxNumberOfSymbols();
    }

    @Override
    public byte getType() {
        return (byte) (IndexType.ALPHABETS_COUNT + (numberOfStrokes * converter.getType()));
    }
}
