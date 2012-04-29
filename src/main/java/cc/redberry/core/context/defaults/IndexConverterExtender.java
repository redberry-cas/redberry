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
import cc.redberry.core.context.ToStringMode;

public final class IndexConverterExtender implements IndexSymbolConverter {
    private final IndexSymbolConverter innerConverter;

    public IndexConverterExtender(IndexSymbolConverter innerConverter) {
        this.innerConverter = innerConverter;
    }

    @Override
    public boolean applicableToSymbol(String symbol) {
        int _position = symbol.lastIndexOf('_');
        if (_position == -1)
            return innerConverter.applicableToSymbol(symbol);
        try {
            if (Integer.parseInt(symbol.substring(_position + 1)) > 9)
                return false;
            return innerConverter.applicableToSymbol(symbol.substring(0, _position));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getCode(String symbol) throws IndexConverterException {
        int _position = symbol.lastIndexOf('_');
        if (_position == -1)
            return innerConverter.getCode(symbol);
        int num = Integer.parseInt(symbol.substring(_position + 1));
        return (num) * (1 + innerConverter.maxSymbolsCount()) + innerConverter.getCode(symbol.substring(0, _position));
    }

    @Override
    public String getSymbol(int code, ToStringMode mode) throws IndexConverterException {
        int num = code / (innerConverter.maxSymbolsCount() + 1);
        if (num == 0)
            return innerConverter.getSymbol(code, mode);
        else
            return innerConverter.getSymbol(code % (innerConverter.maxSymbolsCount() + 1), mode) + "_" + (num < 9 ? num : "{" + num + "}");
    }

    @Override
    public byte getType() {
        return innerConverter.getType();
    }

    @Override
    public int maxSymbolsCount() {
        return 10 * (innerConverter.maxSymbolsCount() + 1) - 1;
    }
}
