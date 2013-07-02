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

import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.utils.ArraysUtils;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class NameDescriptorForTensorField extends NameDescriptor {
    final int[] orders;
    final String name;
    int[][] indicesPartitionMapping = null;

    NameDescriptorForTensorField(StructureOfIndices[] indexTypeStructures, int id, int[] orders, String name) {
        super(indexTypeStructures, id);
        this.orders = orders;
        this.name = name;
    }

    public int[] getDerivativeOrders() {
        return orders.clone();
    }

    public int getDerivativeOrder(int arg) {
        return orders[arg];
    }

    private void ensurePartitionInitialized() {
        if (indicesPartitionMapping != null)
            return;

        if (!isDerivative()) {
            int[][] ret = new int[structuresOfIndices.length][];
            Arrays.fill(ret, 1, ret.length, new int[0]);
            ret[0] = ArraysUtils.getSeriesFrom0(structuresOfIndices[0].size());
            indicesPartitionMapping = ret;
        }

        NameDescriptorForTensorField parent = getParent();

        StructureOfIndices[] partition = new StructureOfIndices[ArraysUtils.sum(orders) + 1];
        partition[0] = parent.getStructureOfIndices();
        int i, j;
        int totalOrder = 1;
        for (i = 0; i < structuresOfIndices.length - 1; ++i) {
            for (j = orders[i] - 1; j >= 0; --j)
                partition[totalOrder++] = parent.getArgStructureOfIndices(i);
        }

        indicesPartitionMapping = structuresOfIndices[0].getPartitionMappings(partition);

    }

    public int[][] getIndicesPartitionMapping() {
        ensurePartitionInitialized();
        return ArraysUtils.deepClone(indicesPartitionMapping);
    }

    public abstract NameDescriptorForTensorField getParent();

    public abstract boolean isDerivative();

    public abstract NameDescriptorForTensorField getDerivative(int... orders);
}
