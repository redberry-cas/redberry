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

import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class NameDescriptorForTensorFieldDerivative extends NameDescriptorForTensorField {
    final NameDescriptorForTensorFieldImpl parent;

    public NameDescriptorForTensorFieldDerivative(int id, final int[] orders, NameDescriptorForTensorFieldImpl parent) {
        super(generateStructures(parent, orders), id, orders);
        this.parent = parent;
    }

    private static StructureOfIndices[] generateStructures(NameDescriptorForTensorFieldImpl parent, final int[] orders) {
        StructureOfIndices[] structureOfIndices = parent.indexTypeStructures.clone();
        int j;
        for (int i = 0; i < orders.length; ++i) {
            for (j = 0; j < orders[i]; ++j)
                structureOfIndices[0] = structureOfIndices[0].append(structureOfIndices[i].getInverted());
        }
        return structureOfIndices;
    }

    @Override
    NameAndStructureOfIndices[] getKeys() {
        return new NameAndStructureOfIndices[0];
    }

    @Override
    public String getName(SimpleIndices indices) {
        return null;
    }

    @Override
    public boolean isDerivative() {
        return true;
    }

    @Override
    public NameDescriptorForTensorField getDerivative(int... orders) {
        if (orders.length != indexTypeStructures.length - 1)
            throw new IllegalArgumentException();

        int[] resOrder = this.orders;
        for (int i = orders.length - 1; i >= 0; --i)
            resOrder[i] += orders[i];

        return parent.getDerivative(resOrder);
    }
}
