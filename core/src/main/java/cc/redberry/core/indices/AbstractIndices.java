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
package cc.redberry.core.indices;

import cc.redberry.core.context.Context;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.utils.IntArray;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import static cc.redberry.core.context.OutputFormat.*;

/**
 * Basic abstract {@link Indices} implementation. Indices are stored as final
 * integer array.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
abstract class AbstractIndices implements Indices {

    protected final int[] data;
    //FUTURE investigate performance
    private WeakReference<UpperLowerIndices> upperLower = new WeakReference<>(null);

    AbstractIndices(int[] data) {
        this.data = data;
    }

    protected abstract UpperLowerIndices calculateUpperLower();

    abstract int[] getSortedData();

    protected UpperLowerIndices getUpperLowerIndices() {
        WeakReference<UpperLowerIndices> wul = upperLower;
        UpperLowerIndices ul = wul.get();
        if (ul == null) {
            ul = calculateUpperLower();
            upperLower = new WeakReference<>(ul);
        }
        return ul;
    }

    @Override
    public final int[] toArray() {
        return data.clone();
    }

    @Override
    public final IntArray getUpper() {
        WeakReference<UpperLowerIndices> wul = upperLower;
        UpperLowerIndices ul = wul.get();
        if (ul == null) {
            ul = calculateUpperLower();
            upperLower = new WeakReference<>(ul);
        }
        return new IntArray(ul.upper);
    }

    @Override
    public final IntArray getLower() {
        WeakReference<UpperLowerIndices> wul = upperLower;
        UpperLowerIndices ul = wul.get();
        if (ul == null) {
            ul = calculateUpperLower();
            upperLower = new WeakReference<>(ul);
        }
        return new IntArray(ul.lower);
    }

    @Override
    public final IntArray getAllIndices() {
        return new IntArray(data);
    }

    @Override
    public final boolean equalsRegardlessOrder(Indices indices) {
        if (this == indices)
            return true;
        if (indices instanceof EmptyIndices)
            return data.length == 0;
//        if (data.length == 0)
//            return indices.size() == 0;
        return Arrays.equals(getSortedData(), ((AbstractIndices) indices).getSortedData());
    }

    @Override
    public final int size() {
        return data.length;
    }

    @Override
    public final int get(int position) {
        return data[position];
    }

    @Override
    public final int hashCode() {
        return 291 + Arrays.hashCode(this.data);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return Arrays.equals(this.data, ((AbstractIndices) obj).data);
    }

    @Override
    public final String toString(OutputFormat format) {
        if (data.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int currentState;

        if (format == WolframMathematica || format == Maple) {
            for (int i = 0; ; i++) {
                currentState = data[i] >>> 31;
                if (currentState == 1) sb.append(format.upperIndexPrefix);
                else sb.append(format.lowerIndexPrefix);
                sb.append(Context.get().getIndexConverterManager().getSymbol(data[i], format));
                if (i == data.length - 1)
                    break;
                sb.append(",");
            }
        } else {
            String latexBrackets = format == LaTeX ? "{}" : "";

            currentState = (data[0] >>> 31);
            if (currentState == 0)
                sb.append(format.lowerIndexPrefix).append("{");
            else
                sb.append(format.upperIndexPrefix).append("{");

            int lastState = currentState;
            for (int i = 0; i < data.length; i++) {
                currentState = data[i] >>> 31;
                if (lastState != currentState) {
                    if (lastState == 0)
                        sb.append("}").append(latexBrackets).append(format.upperIndexPrefix).append("{");
                    if (lastState == 1)
                        sb.append("}").append(latexBrackets).append(format.lowerIndexPrefix).append("{");
                    lastState = currentState;
                }
                sb.append(Context.get().getIndexConverterManager().getSymbol(data[i], format));
            }
            sb.append("}");
        }

        return sb.toString();
    }

    private static String beginingSeparator(OutputFormat format) {
        switch (format) {
            case WolframMathematica:
            case Maple:
                return "-";
            default:
                return "_";
        }
    }

    @Override
    public final String toString() {
        return toString(Context.get().getDefaultOutputFormat());
    }

    protected static class UpperLowerIndices {

        final int[] upper;
        final int[] lower;

        UpperLowerIndices(int[] upper, int[] lower) {
            this.upper = upper;
            this.lower = lower;
        }
    }
}
