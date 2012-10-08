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
package cc.redberry.core.transformations;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.TensorUtils;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class RemoveDueToSymmetry implements Transformation {

    public static final RemoveDueToSymmetry INSANCE = new RemoveDueToSymmetry();

    private RemoveDueToSymmetry() {
    }

    @Override
    public Tensor transform(Tensor t) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        TraverseState state;
        Tensor c;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            c = iterator.current();
            if (TensorUtils.isZeroDueToSymmetry(c))
                iterator.set(Complex.ZERO);
        }
        return iterator.result();
    }
}
