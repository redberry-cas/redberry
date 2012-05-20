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
package cc.redberry.core.indices;

import cc.redberry.core.context.Context;
import cc.redberry.core.context.ToStringMode;
import cc.redberry.core.utils.IntArray;
import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * Basic abstract {@code Indices} interface implementation. Indices are stored
 * as final integer array. This class is abstract and has two main inheritors:
 * {@link SimpleIndicesImpl} and {@link SortedIndices}, due to different way of
 * indices array storing (sorted or not). This is because some methods for
 * sorted array can be written with faster algorithms. For more information see
 * links below.
 *
 * @see Indices
 * @see SortedIndices
 * @see SimpleIndicesImpl
 * @see IndicesUtils
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class AbstractIndices implements Indices {
    //indices data

    protected final int[] data;
    //lazy fields
    private WeakReference<UpperLowerIndices> upperLower = new WeakReference<>(null);

    /**
     * Construct {@code AbstractIndices} instance from specified indices array.
     *
     * @param data array of indices
     */
    AbstractIndices(int[] data) {
        this.data = data;
    }

    protected abstract UpperLowerIndices calculateUpperLower();

    abstract int[] getSortedData();

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
    public final boolean equalsIgnoreOrder(Indices indices) {
        if (indices instanceof EmptyIndices)
            return data.length == 0;
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
    public final boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return Arrays.equals(this.data, ((AbstractIndices) obj).data);
    }

    /**
     * Returns {@code Arrays.hashCode(this.data)}, where data - generic array of
     * integers, representing this indices
     *
     * @return {@code Arrays.hashCode(this.data)}, where data - generic array of
     * integers, representing this indices
     */
    @Override
    public final int hashCode() {
        return 291 + Arrays.hashCode(this.data);
    }

    /**
     * {@inheritDoc}
     *
     * @param mode {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public final String toString(ToStringMode mode) {
        if (data.length == 0)
            return "";
        boolean latex = mode == ToStringMode.LaTeX;
        StringBuilder sb = new StringBuilder();
        int stateMode = (data[0] >>> 31);
        int currentState = stateMode;
        if (stateMode == 0)
            sb.append(latex ? "_{" : "_{");
        else
            sb.append(latex ? "^{" : "^{");
        for (int i = 0; i < data.length; i++) {
            stateMode = data[i] >>> 31;
            if (currentState != stateMode) {
                if (currentState == 0)
                    sb.append(latex ? "}{}^{" : "}^{");
                if (currentState == 1)
                    sb.append(latex ? "}{}_{" : "}_{");
                currentState = stateMode;
            }
            sb.append(Context.get().getIndexConverterManager().getSymbol(data[i], mode));
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns {@code toString(Context.get().getDefaultPrintMode());}
     *
     * @return {@code toString(Context.get().getDefaultPrintMode());}
     *
     * @see AbstractIndices#toString(cc.redberry.core.context.ToStringMode)
     */
    @Override
    public final String toString() {
        return toString(Context.get().getDefaultPrintMode());
    }

    protected static class UpperLowerIndices {

        public final int[] upper;
        public final int[] lower;

        public UpperLowerIndices(int[] upper, int[] lower) {
            this.upper = upper;
            this.lower = lower;
        }
    }
}
