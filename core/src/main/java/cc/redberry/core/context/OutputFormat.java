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
public final class OutputFormat {
    /**
     * This format specifies expressions to be outputted in the LaTeX notation. The produces strings
     * can be simply putted in some LaTeX math environments and compiled via LaTeX compiler.
     */
    public static final OutputFormat LaTeX = new OutputFormat(0, "^", "_"),
    /**
     * This format specifies greek letters to be printed as is (if stdout supports utf-8 characters).
     * In other aspects it is similar to {@link OutputFormat#Redberry}
     */
    UTF8 = new OutputFormat(1, "^", "_"),
    /**
     * This format specifies expressions to be outputted in the Redberry input notation. Produced strings
     * can be parsed in Redberry.
     */
    Redberry = new OutputFormat(2, "^", "_"),
    /**
     * This format specifies expressions to be outputted in the Redberry input notation. Produced strings
     * can be parsed in Redberry.
     */
    Cadabra = new OutputFormat(3, "^", "_"),
    /**
     * This format specifies expressions to be outputted in the Wolfram Mathematica input notation.
     */
    WolframMathematica = new OutputFormat(4, "", "-"),
    /**
     * This format specifies expressions to be outputted in the Maplesoft Maple input notation.
     */
    Maple = new OutputFormat(5, "~", ""),
    /**
     * This format will not print explicit indices of matrices. E.g. if A and B are matrices, that it will
     * produce A*B instead of A^i'_j'*B^j'_k'.
     */
    SimpleRedberry = new OutputFormat(6, "^", "_", false);
    /**
     * Unique identifier.
     */
    public final int id;
    /**
     * Prefix, which specifies upper index (e.g. '^' in LaTeX)
     */
    public final String upperIndexPrefix;
    /**
     * Prefix, which specifies lower index (e.g. '_' in LaTeX)
     */
    public final String lowerIndexPrefix;
    /**
     * Specifies whether print matrix indices or not.
     */
    public final boolean printMatrixIndices;

    private OutputFormat(OutputFormat format, boolean printMatrixIndices) {
        this(format.id, format.upperIndexPrefix, format.lowerIndexPrefix, printMatrixIndices);
    }

    private OutputFormat(int id, String upperIndexPrefix, String lowerIndexPrefix) {
        this(id, upperIndexPrefix, lowerIndexPrefix, true);
    }

    private OutputFormat(int id, String upperIndexPrefix, String lowerIndexPrefix, boolean printMatrixIndices) {
        this.id = id;
        this.upperIndexPrefix = upperIndexPrefix;
        this.lowerIndexPrefix = lowerIndexPrefix;
        this.printMatrixIndices = printMatrixIndices;
    }

    /**
     * Returns output format that will not print matrix indices.
     *
     * @return output format that will not print matrix indices
     */
    public OutputFormat doNotPrintMatrixIndices() {
        return printMatrixIndices ? new OutputFormat(this, false) : this;
    }

    /**
     * Returns output format that will always print matrix indices.
     *
     * @return output format that will always print matrix indices
     */
    public OutputFormat printMatrixIndices() {
        return printMatrixIndices ? this : new OutputFormat(this, true);
    }

    /**
     * Returns whether this and oth defines same format.
     *
     * @param oth other format
     * @return whether this and oth defines same format
     */
    public boolean is(OutputFormat oth) {
        return id == oth.id;
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
