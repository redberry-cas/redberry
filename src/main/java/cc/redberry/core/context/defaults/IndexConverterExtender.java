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

/**
 * {@link IndexSymbolConverter} for subscripted letters (e.g. \\alpha_{2} or A_{4}).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class IndexConverterExtender implements IndexSymbolConverter {
    /**
     * {@link IndexSymbolConverter} for subscripted latin lower case letters.
     */
    public static final IndexConverterExtender LatinLowerEx
            = new IndexConverterExtender(LatinLowerCaseConverter.INSTANCE);
    /**
     * {@link IndexSymbolConverter} for subscripted latin upper case letters.
     */
    public static final IndexConverterExtender LatinUpperEx
            = new IndexConverterExtender(LatinUpperCaseConverter.INSTANCE);
    /**
     * {@link IndexSymbolConverter} for subscripted Greek lower case letters.
     */
    public static final IndexConverterExtender GreekLowerEx
            = new IndexConverterExtender(GreekLaTeXLowerCaseConverter.INSTANCE);
    /**
     * {@link IndexSymbolConverter} for subscripted Greek upper case letters.
     */
    public static final IndexConverterExtender GreekUpperEx
            = new IndexConverterExtender(GreekLaTeXUpperCaseConverter.INSTANCE);

    private final IndexSymbolConverter innerConverter;

    /**
     * Creates  {@link IndexSymbolConverter} for subscripted symbols from specified converter.
     *
     * @param innerConverter converter to be extended with subscripted symbols
     */
    public IndexConverterExtender(IndexSymbolConverter innerConverter) {
        this.innerConverter = innerConverter;
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        if (!symbol.contains("_"))
            return innerConverter.applicableToSymbol(symbol);
        String[] split = symbol.split("_");
        if (split.length != 2 || split[1].length() == 0)
            return false;
        if (split[1].charAt(0) == '{') {
            if (split[1].length() < 3)
                return false;
            split[1] = split[1].substring(1, split[1].length() - 1);
        }
        try {
            Integer.parseInt(split[1]);
            return innerConverter.applicableToSymbol(split[0]);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        if (!symbol.contains("_"))
            return innerConverter.getCode(symbol);
        String[] split = symbol.split("_");
        if (split.length != 2 || split[1].length() == 0)
            throw new IndexConverterException();
        if (split[1].charAt(0) == '{') {
            if (split[1].length() < 3)
                throw new IndexConverterException();
            split[1] = split[1].substring(1, split[1].length() - 1);
        }
        int num;
        try {
            num = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            throw new IndexConverterException();
        }
        return (num) * (1 + innerConverter.maxNumberOfSymbols()) + innerConverter.getCode(split[0]);
    }

    @Override
    public String getSymbol(int code, OutputFormat mode) throws IndexConverterException {
        int num = code / (innerConverter.maxNumberOfSymbols() + 1);
        if (num == 0)
            return innerConverter.getSymbol(code, mode);
        else
            return innerConverter.getSymbol(code % (innerConverter.maxNumberOfSymbols() + 1), mode) + "_" + ("{" + num + "}");
    }

    @Override
    public byte getType() {
        return innerConverter.getType();
    }

    @Override
    public int maxNumberOfSymbols() {
        return 10 * (innerConverter.maxNumberOfSymbols() + 1) - 1;
    }
}
