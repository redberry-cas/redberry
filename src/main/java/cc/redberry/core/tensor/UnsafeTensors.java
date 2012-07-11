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
package cc.redberry.core.tensor;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.SimpleIndices;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class UnsafeTensors {

    private UnsafeTensors() {
    }

    public static Tensor unsafeMultiplyWithoutIndicesRenaming(Tensor... factors) {
        ProductBuilder pb = new ProductBuilder();
        for (Tensor t : factors)
            pb.put(t);
        return pb.build();
    }

    public static Tensor unsafeSumWithouBuilder(Tensor... tensor) {
        if (tensor.length == 0)
            throw new IllegalArgumentException();
        if (tensor.length == 1)
            return tensor[0];
        return new Sum(tensor, IndicesFactory.createSorted(tensor[0].getIndices().getFreeIndices()));
    }

    public static TensorField unsafeSetIndicesToField(TensorField field, SimpleIndices newIndices) {
        assert CC.getNameDescriptor(field.name).getSymmetries() == newIndices.getSymmetries();
        return new TensorField(field.name, newIndices, field.args, field.argIndices);
    }

    public static SimpleTensor unsafeSetIndicesToSimpleTensor(SimpleTensor simpleTensor, SimpleIndices newIndices) {
        assert CC.getNameDescriptor(simpleTensor.name).getSymmetries() == newIndices.getSymmetries();
        return new SimpleTensor(simpleTensor.name, newIndices);
    }
    
    
}
