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

/**
 * Interface, which states common functionality of string-integer converter for indices
 * of the same particular type.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public interface IndexSymbolConverter {
    /**
     * Returns true if this converter can convert index string to integer
     * representation
     *
     * @param symbol index string representation
     * @return true, if this converter can convert index string to integer
     *         representation
     */
    boolean applicableToSymbol(String symbol);

    /**
     * Returns string representation from specified integer representation of single
     * index in the specified {@code outputFormat}.
     *
     * @param code         index integer representation
     * @param outputFormat output format
     * @return string representation of specified integer index, according to
     *         the specified output format
     * @throws IndexConverterException if code does not corresponds to this
     *                                 converter
     * @see OutputFormat
     */
    String getSymbol(int code, OutputFormat outputFormat) throws IndexConverterException;

    /**
     * Returns integer representation from specified string representation of single
     * index.
     *
     * @param symbol index string representation
     * @return index integer representation
     * @throws IndexConverterException if converter does not applicable to
     *                                 specified symbol
     */
    int getCode(String symbol) throws IndexConverterException;

    /**
     * Returns the number of symbols, supported by this converter. For example
     * {@link cc.redberry.core.context.defaults.LatinLowerCaseConverter} supports 26 symbols (Latin alphabet
     * size).
     *
     * @return the number of symbols, supported by this converter
     */
    int maxNumberOfSymbols();

    /**
     * Returns the type of indices, which this converter processes. The type value is
     * unique for each converter.
     *
     * @return type of indices, which this converter process
     */
    byte getType();
}
