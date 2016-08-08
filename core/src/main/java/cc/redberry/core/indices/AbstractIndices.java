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
package cc.redberry.core.indices;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.Context;
import cc.redberry.core.context.IndexConverterManager;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.utils.IntArray;
import cc.redberry.core.utils.IntArrayList;

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

    final int[] data;
    //FUTURE investigate performance
    WeakReference<UpperLowerIndices> upperLower = new WeakReference<>(null);

    AbstractIndices(int[] data) {
        this.data = data;
    }

    abstract UpperLowerIndices calculateUpperLower();

    abstract int[] getSortedData();

    UpperLowerIndices getUpperLowerIndices() {
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
    public final IntArray getAllIndices() {
        return new IntArray(data);
    }

    @Override
    public final boolean equalsRegardlessOrder(Indices indices) {
        if (this == indices)
            return true;
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

    /**
     * Returns whether there are contracted indices
     *
     * @return whether there are contracted indices
     */
    @Override
    public boolean hasContracted() {
        return size() > 1 && size() != getFree().size();
    }

    //    @Override
//    public boolean containsSubIndices(Indices subIndices) {
//        int pointer = 0, index;
//        for (int s = 0; s < subIndices.size(); ++s) {
//            index = subIndices.get(s);
//            while (get(pointer) != index)
//                pointer++;
//            if (pointer == size())
//                return false;
//            ++pointer;
//        }
//        return true;
//    }

    @Override
    public final int hashCode() {
        return 291 + Arrays.hashCode(this.data);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof AbstractIndices))
            return false;
        return Arrays.equals(this.data, ((AbstractIndices) obj).data);
    }

    @Override
    public final String toString(OutputFormat format) {
        if (data.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        int currentState;

        if (format.is(WolframMathematica) || format.is(Maple)) {
            for (int i = 0; ; i++) {
                currentState = data[i] >>> 31;
                if (currentState == 1) sb.append(format.upperIndexPrefix);
                else sb.append(format.lowerIndexPrefix);
                sb.append(IndexConverterManager.DEFAULT.getSymbol(data[i], format));
                if (i == data.length - 1)
                    break;
                sb.append(",");
            }
        } else if (format.is(Cadabra)) {
            IntArrayList nonMetricIndices = new IntArrayList();
            IntArrayList metricIndices = new IntArrayList(data.length);
            for (int i = 0; i < data.length; ++i)
                if (CC.isMetric(IndicesUtils.getType(data[i])))
                    metricIndices.add(data[i]);
                else
                    nonMetricIndices.add(data[i]);

            if (!metricIndices.isEmpty()) {
                sb.append("_{");
                for (int i = 0, size = metricIndices.size() - 1; ; ++i) {
                    sb.append(IndexConverterManager.DEFAULT.getSymbol(metricIndices.get(i), format));
                    if (i == size)
                        break;
                    sb.append(' ');
                }
                sb.append('}');
            }

            if (!nonMetricIndices.isEmpty()) {
                currentState = (nonMetricIndices.get(0) >>> 31);
                sb.append(format.lowerIndexPrefix).append('{');
                int lastState = currentState;
                for (int i = 0, size = nonMetricIndices.size() - 1; ; ++i) {
                    currentState = nonMetricIndices.get(i) >>> 31;
                    if (lastState != currentState) {
                        sb.append('}').append(format.getPrefixFromIntState(currentState)).append('{');
                        lastState = currentState;
                    }
                    sb.append(IndexConverterManager.DEFAULT.getSymbol(nonMetricIndices.get(i), format));

                    if (i == size)
                        break;
                    if (currentState == nonMetricIndices.get(i + 1) >>> 31)
                        sb.append(' ');
                }
                sb.append('}');
            }
        } else {
            String latexBrackets = format.is(LaTeX) ? "{}" : "";

            int totalToPrint = 0;
            int lastState = -1;
            for (int i = 0; i < data.length; i++) {
                if (!CC.isMetric(IndicesUtils.getType(data[i])) && !format.printMatrixIndices)
                    continue;
                currentState = data[i] >>> 31;
                if (lastState != currentState) {
                    if (totalToPrint != 0)
                        sb.append('}');
                    sb.append(latexBrackets).append(format.getPrefixFromIntState(currentState)).append('{');
                    lastState = currentState;
                }
                sb.append(IndexConverterManager.DEFAULT.getSymbol(data[i], format));
                ++totalToPrint;
            }
            sb.append('}');
            if (totalToPrint == 0)
                return "";
        }

        return sb.toString();
    }

    @Override
    public final String toString() {
        return toString(CC.current().getDefaultOutputFormat());
    }

    final static class UpperLowerIndices {

        final int[] upper;
        final int[] lower;

        UpperLowerIndices(int[] upper, int[] lower) {
            this.upper = upper;
            this.lower = lower;
        }
    }
}
