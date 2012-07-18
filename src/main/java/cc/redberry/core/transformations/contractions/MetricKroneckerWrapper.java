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
package cc.redberry.core.transformations.contractions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import cc.redberry.core.context.CC;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class MetricKroneckerWrapper implements Comparable<MetricKroneckerWrapper> {

    final int[] indices = new int[2];
    Tensor tensorMK;

    MetricKroneckerWrapper(Tensor tensorMK) {
        this.indices[0] = tensorMK.getIndices().get(0);
        this.indices[1] = tensorMK.getIndices().get(1);
        Arrays.sort(this.indices);
        this.tensorMK = tensorMK;
    }

    private MetricKroneckerWrapper(int index1, int index2, Tensor tensorMK) {
        indices[0] = index1;
        indices[1] = index2;
        this.tensorMK = tensorMK;
    }

    @Override
    public int compareTo(MetricKroneckerWrapper o) {
        int res;
        if ((res = Integer.compare(indices[0], o.indices[0])) != 0)
            return res;
        return Integer.compare(indices[1], o.indices[1]);
    }

    SimpleTensor apply(SimpleTensor t) {
        IM im = new IM();
        SimpleIndices oldIndices = t.getIndices();
        OUTER:
        for (int i = 0; i < oldIndices.size(); ++i)
            for (int j = 0; j < 2; ++j)
                if ((oldIndices.get(i) ^ indices[j])
                        == 0x80000000) {
                    im.add(oldIndices.get(i),
                           indices[1 - j]);
                    break OUTER;
                }
        SimpleIndices newIndices = oldIndices.applyIndexMapping(im);
        if (oldIndices == newIndices)
            return t;
        return Tensors.simpleTensor(t.getName(), newIndices);
    }

    boolean apply(MetricKroneckerWrapper mK) {
        for (int i = 0; i < 2; ++i)
            for (int j = 0; j < 2; ++j)
                if ((indices[i] ^ mK.indices[j]) == 0x80000000) {
                    tensorMK = Tensors.createMetricOrKronecker(indices[1 - i], mK.indices[1 - j]);
                    indices[i] = mK.indices[1 - j];
                    Arrays.sort(this.indices);
                    return true;
                }
        return false;
    }

    @Override
    public MetricKroneckerWrapper clone() {
        Tensor t = Tensors.createMetricOrKronecker(indices[0], indices[1]);
        return new MetricKroneckerWrapper(indices[0], indices[1], t);
    }

    @Override
    public String toString() {
        return tensorMK.toString();
    }

    private class IM implements IndexMapping {

        Map<Integer, Integer> map = new HashMap<>();

        IM() {
        }

        public void add(int from, int to) {
            map.put(from, to);
        }

        @Override
        public int map(int from) {
            Integer to = map.get(from);
            if (to != null)
                return to.intValue();
            return from;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MetricKroneckerWrapper other = (MetricKroneckerWrapper) obj;
        if (!Arrays.equals(this.indices, other.indices))
            return false;
        return true;
    }
}
