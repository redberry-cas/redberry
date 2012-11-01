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
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.transformations.ExpandUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Expand implements Transformation {
    public static final Expand Expand = new Expand();

    private final Transformation[] transformations;
    private final TraverseGuide traverseGuide;

    private Expand() {
        this(new Transformation[0], TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
    }

    public Expand(Transformation[] transformations) {
        this(transformations, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
    }

    public Expand(Transformation[] transformations, TraverseGuide traverseGuide) {
        this.transformations = transformations;
        this.traverseGuide = traverseGuide;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return expand(tensor, traverseGuide, transformations);
    }

    public static Tensor expand(Tensor tensor) {
        return expand(tensor, new Transformation[0]);
    }

    public static Tensor expand(Tensor tensor, Transformation... transformations) {
        return expand(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS, transformations);
    }

    public static Tensor expand(Tensor tensor, TraverseGuide traverseGuide, Transformation... transformations) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.unsafeSet(expandProductOfSums((Product) current, transformations));
            else if (isPositiveIntegerPower(current)) {
                Sum sum = (Sum) current.get(0);
                int exponent = ((Complex) current.get(1)).getReal().intValue();
                if (TensorUtils.isSymbolic(sum))
                    iterator.unsafeSet(expandSymbolicPower(sum, exponent, transformations));
                else
                    iterator.set(expandPower(sum, exponent, iterator.getForbidden(), transformations));
            }

        }
        return iterator.result();
    }
}
