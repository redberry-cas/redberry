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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.Tensor;

/**
 * This {@code enum} defines formats of string representation of expressions in Redberry.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Tensor#toString(OutputFormat)
 * @see Indices#toString(OutputFormat)
 * @see Tensor#toString()
 * @since 1.0
 */
public enum OutputFormat {

    /**
     * This format specifies expressions to be outputted in the LaTeX notation. The produces strings
     * can be simply putted in some LaTeX math environments and compiled via LaTeX compiler.
     */
    LaTeX("^", "_"),
    /**
     * This format specifies greek letters to be printed as is (if stdout supports utf-8 characters).
     * In other aspects it is similar to {@link OutputFormat#Redberry}
     */
    UTF8("^", "_"),
    /**
     * This format specifies expressions to be outputted in the Redberry input notation. Produced strings
     * can be parsed in Redberry.
     */
    Redberry("^", "_"),
    /**
     * This format specifies expressions to be outputted in the Redberry input notation. Produced strings
     * can be parsed in Redberry.
     */
    Cadabra("^", "_"),
    /**
     * This format specifies expressions to be outputted in the Wolfram Mathematica input notation.
     */
    WolframMathematica("", "-"),
    /**
     * This format specifies expressions to be outputted in the Maplesoft Maple input notation.
     */
    Maple("~", "");

    /**
     * Prefix, which specifies upper index (e.g. '^' in LaTeX)
     */
    public final String upperIndexPrefix;
    /**
     * Prefix, which specifies lower index (e.g. '_' in LaTeX)
     */
    public final String lowerIndexPrefix;

    private OutputFormat(String upperIndexPrefix, String lowerIndexPrefix) {
        this.upperIndexPrefix = upperIndexPrefix;
        this.lowerIndexPrefix = lowerIndexPrefix;
    }

    /**
     * Returns {@link #lowerIndexPrefix} if {@code intState == 0} and {@link #upperIndexPrefix} if {@code intState == 1}
     *
     * @param intState int state (0 - lower, 1 - upper)
     * @return prefix
     */
    public String getPrefixFromIntState(int intState) {
        switch (intState) {
            case 0:
                return lowerIndexPrefix;
            case 1:
                return upperIndexPrefix;
            default:
                throw new IllegalArgumentException("Not a state int");
        }
    }

    /**
     * Returns {@link #lowerIndexPrefix} if {@code intState == 0} and {@link #upperIndexPrefix} if {@code intState == 0x80000000}
     *
     * @param rawIntState int state (0 - lower, 0x80000000 - upper)
     * @return prefix
     */
    public String getPrefixFromRawIntState(int rawIntState) {
        switch (rawIntState) {
            case 0:
                return lowerIndexPrefix;
            case 0x80000000:
                return upperIndexPrefix;
            default:
                throw new IllegalArgumentException("Not a state int");
        }
    }
}
