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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.TensorField;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraversePermission;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.TensorUtils;

import static cc.redberry.core.tensor.Tensors.reciprocal;
import static cc.redberry.core.utils.TensorUtils.isNegativeIntegerPower;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
abstract class AbstractExpandTransformation implements TransformationToStringAble {
    public static TraverseGuide DefaultExpandTraverseGuide = new TraverseGuide() {
        @Override
        public TraversePermission getPermission(Tensor tensor, Tensor parent, int indexInParent) {
            if (tensor instanceof ScalarFunction)
                return TraversePermission.DontShow;
            if (tensor instanceof TensorField)
                return TraversePermission.DontShow;
            if (isNegativeIntegerPower(tensor))
                return TraversePermission.DontShow;
            return TraversePermission.Enter;
        }
    };

    protected final Transformation[] transformations;
    protected final TraverseGuide traverseGuide;

    protected AbstractExpandTransformation() {
        this(new Transformation[0], DefaultExpandTraverseGuide);
    }

    protected AbstractExpandTransformation(Transformation[] transformations) {
        this(transformations, DefaultExpandTraverseGuide);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand and leaves unexpanded parts of expression specified by
     * {@code traverseGuide}.
     *
     * @param transformations transformations to be applied after each step of expand
     * @param transformations to be applied after each step of expand
     * @param traverseGuide   traverse guide
     */
    protected AbstractExpandTransformation(Transformation[] transformations, TraverseGuide traverseGuide) {
        this.transformations = transformations;
        this.traverseGuide = traverseGuide;
    }

    protected AbstractExpandTransformation(ExpandOptions options) {
        this.transformations = new Transformation[]{options.simplifications};
        this.traverseGuide = options.traverseGuide;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        SubstitutionIterator iterator = new SubstitutionIterator(tensor, traverseGuide);
        Tensor current;
        while ((current = iterator.next()) != null) {
            if (current instanceof Product)
                iterator.unsafeSet(expandProduct((Product) current, transformations));
            else if (ExpandUtils.isExpandablePower(current)) {
                Sum sum = (Sum) current.get(0);
                int exponent = ((Complex) current.get(1)).intValue();
                if (exponent == -1)
                    continue;
                boolean symbolic = TensorUtils.isSymbolic(sum),
                        reciprocal = exponent < 0;
                exponent = Math.abs(exponent);
                Tensor temp;
                if (symbolic)
                    temp = ExpandUtils.expandSymbolicPower(sum, exponent, transformations);
                else
                    temp = ExpandUtils.expandPower(sum, exponent, iterator.getForbidden(), transformations);
                if (reciprocal)
                    temp = reciprocal(temp);
                if (symbolic)
                    iterator.unsafeSet(temp);
                else
                    iterator.set(temp);
            }
        }
        return iterator.result();
    }

    protected abstract Tensor expandProduct(Product product, Transformation[] transformations);
}
