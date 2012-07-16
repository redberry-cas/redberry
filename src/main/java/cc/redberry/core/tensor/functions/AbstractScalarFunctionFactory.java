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
package cc.redberry.core.tensor.functions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorFactory;
import cc.redberry.core.utils.TensorUtils;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class AbstractScalarFunctionFactory implements TensorFactory {

    @Override
    public Tensor create(Tensor... tensors) {
        if (tensors.length != 1)
            throw new IllegalArgumentException();
        if (tensors[0] == null)
            throw new NullPointerException();
        if (!TensorUtils.isIndexless(tensors[0]))
            throw new IllegalArgumentException();
        return create1(tensors[0]);
    }

    protected abstract Tensor create1(Tensor tensor);
}
