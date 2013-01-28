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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.fractions.NumeratorDenominator;

import static cc.redberry.core.tensor.Tensors.reciprocal;

/**
 * Expands out all products and integer powers in any part of expression.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ExpandAllTransformation extends AbstractExpandTransformation {
    /**
     * The default instance.
     */
    public static final ExpandAllTransformation EXPAND_ALL = new ExpandAllTransformation();

    private ExpandAllTransformation() {
        super(new Transformation[0], TraverseGuide.ALL);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand.
     *
     * @param transformations transformations to be applied after each step of expand
     */
    public ExpandAllTransformation(Transformation[] transformations) {
        super(transformations, TraverseGuide.ALL);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand and leaves unexpanded parts of expression specified by
     * {@code traverseGuide}.
     *
     * @param transformations transformations to be applied after each step of expand
     * @param traverseGuide   traverse guide
     */
    public ExpandAllTransformation(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    /**
     * Expands out all products and integer powers in any part of expression.
     *
     * @param tensor tensor to be transformed
     * @return result
     */
    public static Tensor expandAll(Tensor tensor) {
        return EXPAND_ALL.transform(tensor);
    }

    /**
     * Expands out all products and integer powers in any part of expression and applies specified transformations
     * after each step of expand.
     *
     * @param tensor tensor to be transformed
     * @return result
     */
    public static Tensor expandAll(Tensor tensor, Transformation... transformations) {
        return new ExpandAllTransformation(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        NumeratorDenominator numDen = NumeratorDenominator.getNumeratorAndDenominator(product, NumeratorDenominator.integerDenominatorIndicator);
        Tensor denominator = numDen.denominator;

//        assert !isPositiveIntegerPower(denominator);
        if (denominator instanceof Product)
            denominator = ExpandUtils.expandProductOfSums((Product) numDen.denominator, transformations);
        boolean denExpanded = denominator != numDen.denominator;
        denominator = reciprocal(denominator);

        Tensor numerator = numDen.numerator;
        Tensor res = Tensors.multiply(denominator, numerator), temp = res;
        if (res instanceof Product)
            res = ExpandUtils.expandProductOfSums((Product) temp, transformations);
        if (denExpanded || res != temp)
            return res;
        return product;
    }
}
