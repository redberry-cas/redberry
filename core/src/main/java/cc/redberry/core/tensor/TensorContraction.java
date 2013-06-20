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
package cc.redberry.core.tensor;

import cc.redberry.core.utils.HashFunctions;

import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class TensorContraction implements Comparable<TensorContraction> {
    
    public final short tensorId; //aka stretch Id
    public final long[] indexContractions;
    private int hash = -1;

    public TensorContraction(final short tensorId, final long[] indexContractions) {
        this.tensorId = tensorId;
        this.indexContractions = indexContractions;
    }

    public void sortContractions() {
        Arrays.sort(indexContractions);
    }

    public boolean containsFreeIndex() {
        for (long contraction : indexContractions)
            if (getToTensorId(contraction) == -1)
                return true;
        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        if (hashCode() != obj.hashCode())
            return false;
        final TensorContraction other = (TensorContraction) obj;
        if (tensorId != other.tensorId)
            return false;
        return Arrays.equals(indexContractions, other.indexContractions);
    }

    @Override//TODO improve hashCode(), may be precalculate with indices diff ids
    public int hashCode() {
        if (hash == -1) {
            long hash = 1L;
            for (long l : indexContractions)
                hash ^= HashFunctions.JenkinWang64shift(l);
            this.hash = HashFunctions.Wang64to32shift(hash);
        }
        return hash;

    }

    @Override
    public int compareTo(final TensorContraction o) {
        int val;
        if ((val = Integer.compare(tensorId, o.tensorId)) != 0)
            return val;
        if ((val = Integer.compare(indexContractions.length, o.indexContractions.length)) != 0)
            return val;
        for (int i = 0; i < indexContractions.length; ++i)
            if ((val = Long.compare(indexContractions[i], o.indexContractions[i])) != 0)
                return val;
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (indexContractions.length == 0)
            return builder.append(tensorId).append("x").toString();
        builder.append(tensorId).append("x{");
        for (long l : indexContractions) {
            builder.append("^").append(getFromIndexId(l)).append("->").append(getToTensorId(l)).append("^").append(getToIndexId(l));
            builder.append(":");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

    public static short getFromIndexId(final long contraction) {
        return (short) ((contraction >> 32) & 0xFFFF);
    }

    public static short getToIndexId(final long contraction) {
        return (short) (contraction & 0xFFFF);
    }

    public static short getToTensorId(final long contraction) {
        return (short) ((contraction >> 16) & 0xFFFF);
    }
}
