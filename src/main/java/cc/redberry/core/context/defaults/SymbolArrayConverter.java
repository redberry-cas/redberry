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
 * the Free Software Foundation, either version 2 of the License, or
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

public abstract class SymbolArrayConverter implements IndexSymbolConverter {

    private final String[] symbols;
    private final String[] utf;

    public SymbolArrayConverter(String[] symbols, String[] utf) {
        this.symbols = symbols;
        this.utf = utf;
        if (symbols.length != utf.length)
            throw new RuntimeException();
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        for (String s : symbols)
            if (s.equals(symbol))
                return true;
        return false;
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        for (int i = 0; i < symbols.length; ++i)
            if (symbols[i].equals(symbol))
                return i;
        throw new IndexConverterException();
    }

    @Override
    public String getSymbol(int code, OutputFormat mode) throws IndexConverterException {
        try {
            switch (mode) {
                default:
                case Redberry:
                case LaTeX:
                    return symbols[code];
                case UTF8:
                    return utf[code];
                case RedberryConsole:
                    return "\\" + symbols[code];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IndexConverterException();
        }
    }

    @Override
    public int maxSymbolsCount() {
        return symbols.length - 1;
    }
}
