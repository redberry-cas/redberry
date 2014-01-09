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

import cc.redberry.core.combinatorics.Combinatorics;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.IntArrayList;

import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class NameDescriptorForTensorFieldDerivative extends NameDescriptorForTensorField {
    final NameDescriptorForTensorFieldImpl parent;

    NameDescriptorForTensorFieldDerivative(int id, final int[] orders, NameDescriptorForTensorFieldImpl parent) {
        super(generateStructures(parent, orders), id, orders, generateName(orders, parent));
        this.parent = parent;
        initializeSymmetries();
    }

    @Override
    public NameDescriptorForTensorField getParent() {
        return parent;
    }

    @Override
    NameAndStructureOfIndices[] getKeys() {
        return new NameAndStructureOfIndices[0];
    }

    @Override
    public String getName(SimpleIndices indices) {
        return name;
    }

    @Override
    public boolean isDerivative() {
        return true;
    }

    @Override
    public NameDescriptorForTensorField getDerivative(int... orders) {
        if (orders.length != structuresOfIndices.length - 1)
            throw new IllegalArgumentException();

        int[] resOrder = this.orders.clone();
        for (int i = orders.length - 1; i >= 0; --i)
            resOrder[i] += orders[i];

        return parent.getDerivative(resOrder);
    }

    private void initializeSymmetries() {
        StructureOfIndices baseStructure = structuresOfIndices[0];

        StructureOfIndices[] partition = new StructureOfIndices[1 + ArraysUtils.sum(orders)];
        partition[0] = parent.structuresOfIndices[0];
        int i, j, k = 0;
        for (i = 0; i < orders.length; ++i)
            for (j = 0; j < orders[i]; ++j)
                partition[++k] = structuresOfIndices[i + 1].getInverted();
        int[][] mapping = baseStructure.getPartitionMappings(partition);

        //adding field symmetries
        List<Symmetry> fieldSymmetries = parent.symmetries.getInnerSymmetries().getBasisSymmetries();
        for (k = 1; k < fieldSymmetries.size(); ++k)
            symmetries.addUnsafe(fieldSymmetries.get(k).convert(mapping[0], baseStructure.size()));


        //adding block symmetries
        IntArrayList aggregator = new IntArrayList();
        j = 1;
        int a, b, cycle[];
        for (i = 0; i < orders.length; ++i) {
            if (orders[i] >= 2) {
                //adding symmetries for indices from each slot
                cycle = Combinatorics.createBlockCycle(structuresOfIndices[i + 1].size(), 2);
                aggregator.addAll(mapping[j]);
                aggregator.addAll(mapping[j + 1]);
                symmetries.addUnsafe(
                        new Symmetry(false, Combinatorics.convertPermutation(cycle, aggregator.toArray(), baseStructure.size())));

                if (orders[i] >= 3) {
                    for (k = 2; k < orders[i]; ++k)
                        aggregator.addAll(mapping[j + k]);

                    cycle = Combinatorics.createBlockCycle(structuresOfIndices[i + 1].size(), orders[i]);
                    symmetries.addUnsafe(
                            new Symmetry(false, Combinatorics.convertPermutation(cycle, aggregator.toArray(), baseStructure.size())));
                }
                aggregator.clear();
            }
            j += orders[i];
        }
    }

    private static String generateName(final int[] orders, NameDescriptorForTensorFieldImpl parent) {
        StringBuilder sb = new StringBuilder();
        sb.append(parent.name);
        sb.append('~');
        if (orders.length != 1)
            sb.append('(');
        for (int i = 0; ; ++i) {
            sb.append(orders[i]);
            if (i == orders.length - 1)
                break;
            sb.append(',');
        }
        if (orders.length != 1)
            sb.append(')');
        return sb.toString();
    }

    private static StructureOfIndices[] generateStructures(NameDescriptorForTensorFieldImpl parent, final int[] orders) {
        StructureOfIndices[] structureOfIndices = parent.structuresOfIndices.clone();
        int j;
        for (int i = 0; i < orders.length; ++i) {
            for (j = 0; j < orders[i]; ++j)
                structureOfIndices[0] = structureOfIndices[0].append(structureOfIndices[i + 1].getInverted());
        }
        return structureOfIndices;
    }
}
