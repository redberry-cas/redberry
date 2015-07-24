/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
import cc.redberry.core.utils.ArraysUtils;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
abstract class GreekLettersConverter implements IndexSymbolConverter {
    private final Container latexConverter, utfConverter;
    private final String[] symbols, utf;

    GreekLettersConverter(String[] symbols, String[] utf) {
        this.symbols = symbols;
        this.utf = utf;
        int[] codes = new int[symbols.length];
        for (int i = 0; i < symbols.length; ++i)
            codes[i] = i;
        this.latexConverter = new Container(symbols, codes);
        this.utfConverter = new Container(utf, codes);
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        return latexConverter.applicableToSymbol(symbol)
                || utfConverter.applicableToSymbol(symbol);
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        int code;
        if ((code = latexConverter.getCode(symbol)) < 0 && (code = utfConverter.getCode(symbol)) < 0)
            throw new IndexConverterException("Unknown index: " + symbol);
        return code;
    }

    @Override
    public int maxNumberOfSymbols() {
        return symbols.length - 1;
    }

    @Override
    public String getSymbol(int code, OutputFormat mode) throws IndexConverterException {
        String symbol;
        try {
            if (mode.is(OutputFormat.UTF8))
                return utf[code];
            symbol = symbols[code];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IndexConverterException();
        }
        if (mode.is(OutputFormat.WolframMathematica))
            symbol = "\\[" + Character.toUpperCase(symbol.charAt(1)) + symbol.substring(2) + "]";
        if (mode.is(OutputFormat.Maple))
            symbol = symbol.substring(1);
        return symbol;
    }

    private static final class Container {
        private final String[] sortedSymbols;
        private final int[] coSortedCodes;

        public Container(String[] symbols, int[] codes) {
            this.sortedSymbols = symbols.clone();
            this.coSortedCodes = codes.clone();
            ArraysUtils.quickSort(this.sortedSymbols, this.coSortedCodes);
        }

        public boolean applicableToSymbol(String symbol) {
            return Arrays.binarySearch(sortedSymbols, symbol) >= 0;
        }

        public int getCode(String symbol) throws IndexConverterException {
            int codePosition = Arrays.binarySearch(sortedSymbols, symbol);
            if (codePosition < 0)
                return -1;
            return coSortedCodes[codePosition];
        }
    }
}
