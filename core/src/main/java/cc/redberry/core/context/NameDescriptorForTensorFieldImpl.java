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

import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class NameDescriptorForTensorFieldImpl extends NameDescriptorForTensorField {
    final HashMap<DerivativeDescriptor, NameDescriptorForTensorFieldDerivative> derivatives = new HashMap<>();
    final NameAndStructureOfIndices[] keys;

    public NameDescriptorForTensorFieldImpl(String name, StructureOfIndices[] indexTypeStructures, int id, boolean isDiracDelta) {
        super(indexTypeStructures, id, new int[indexTypeStructures.length - 1], name, isDiracDelta);
        this.keys = new NameAndStructureOfIndices[]{new NameAndStructureOfIndices(name, indexTypeStructures)};
    }

    @Override
    NameAndStructureOfIndices[] getKeys() {
        return keys;
    }

    @Override
    public String getName(SimpleIndices indices, OutputFormat format) {
        return name;
    }

    @Override
    public boolean isDerivative() {
        return false;
    }

    @Override
    public NameDescriptorForTensorField getParent() {
        return this;
    }

    @Override
    public NameDescriptorForTensorField getDerivative(int... orders) {
        if (orders.length != structuresOfIndices.length - 1)
            throw new IllegalArgumentException();

        boolean b = true;
        for (int o : orders) {
            if (o < 0)
                throw new IllegalArgumentException("Negative derivative order.");

            if (o != 0)
                b = false;
        }

        if (b) return this;

        final DerivativeDescriptor derivativeDescriptor = new DerivativeDescriptor(orders);
        NameDescriptorForTensorFieldDerivative nd = derivatives.get(derivativeDescriptor);
        if (nd == null)
            synchronized (this) {
                nd = derivatives.get(derivativeDescriptor);
                if (nd == null)
                    derivatives.put(derivativeDescriptor,
                            nd = nameManager.createDescriptorForFieldDerivative(this, orders));
            }
        return nd;
    }

    private static final class DerivativeDescriptor {
        final int[] orders;

        private DerivativeDescriptor(int[] orders) {
            this.orders = orders;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DerivativeDescriptor that = (DerivativeDescriptor) o;

            return Arrays.equals(orders, that.orders);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(orders);
        }
    }
}
