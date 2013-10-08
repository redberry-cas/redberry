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
abstract class SymbolArrayConverter implements IndexSymbolConverter {

    private final String[] sortedSymbols;
    private final int[] coSortedCodes;

    private final String[] symbols;
    private final String[] utf;

    public SymbolArrayConverter(String[] symbols, String[] utf) {
        this.symbols = symbols;
        this.utf = utf;
        if (symbols.length != utf.length)
            throw new RuntimeException();

        this.sortedSymbols = symbols.clone();
        this.coSortedCodes = new int[symbols.length];
        for (int i = 0; i < coSortedCodes.length; ++i)
            coSortedCodes[i] = i;
        ArraysUtils.quickSort(this.sortedSymbols, this.coSortedCodes);
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        return Arrays.binarySearch(sortedSymbols, symbol) >= 0;
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        int codePosition = Arrays.binarySearch(sortedSymbols, symbol);
        if (codePosition < 0)
            throw new IndexConverterException();
        return coSortedCodes[codePosition];
    }

    @Override
    public int maxNumberOfSymbols() {
        return sortedSymbols.length - 1;
    }
}
