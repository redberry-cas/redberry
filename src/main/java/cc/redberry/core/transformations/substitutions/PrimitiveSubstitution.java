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
package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.TensorUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class PrimitiveSubstitution {
    final Tensor from, to;
    final boolean toIsSymbolic;

    PrimitiveSubstitution(Tensor from, Tensor to) {
        this.from = from;
        this.to = to;
        this.toIsSymbolic = TensorUtils.isSymbolic(to);
    }

    Tensor newTo(Tensor current, SubstitutionIterator iterator) {
        if (current.getClass() != from.getClass())
            return current;
        return newTo_(current, iterator);
    }

    abstract Tensor newTo_(Tensor currentNode, SubstitutionIterator iterator);
}
