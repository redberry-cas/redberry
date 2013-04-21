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

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class NameDescriptorForTensorField extends NameDescriptor {
    final int[] orders;

    NameDescriptorForTensorField(StructureOfIndices[] indexTypeStructures, int id, int[] orders) {
        super(indexTypeStructures, id);
        this.orders = orders;
    }

    public int[] getDerivativeOrders() {return orders.clone();}

    public abstract boolean isDerivative();

    public abstract NameDescriptorForTensorField getDerivative(int... orders);
}
