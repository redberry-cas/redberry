/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
 * {@link IndexSymbolConverter} for latin upper case letters.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class LatinUpperCaseConverter implements IndexSymbolConverter {
    /**
     * Index type = 1
     */
    public static final byte TYPE = 1;
    /**
     * Singleton instance
     */
    public static final LatinUpperCaseConverter INSTANCE = new LatinUpperCaseConverter();

    private LatinUpperCaseConverter() {
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        if (symbol.length() == 1) {
            char sym = symbol.charAt(0);
            if (sym >= 0x41 && sym <= 0x5A)
                return true;
        }
        return false;
    }

    @Override
    public int getCode(String symbol) {
        return (symbol.charAt(0) - 0x41);
    }

    @Override
    public String getSymbol(int code, OutputFormat mode) throws IndexConverterException {
        int number = code + 0x41;
        if (number > 0x5A)
            throw new IndexConverterException();
        return Character.toString((char) number);
    }

    /**
     * @return 1
     */
    @Override
    public byte getType() {
        return TYPE;
    }

    @Override
    public int maxNumberOfSymbols() {
        return 25;
    }
}
