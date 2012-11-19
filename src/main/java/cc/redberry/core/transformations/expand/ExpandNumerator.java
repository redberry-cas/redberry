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
 * the Free Software Foundation, either version 2 of the License, or
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
import cc.redberry.core.transformations.fractions.NumeratorDenominator;
import cc.redberry.core.transformations.Transformation;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandNumerator extends ExpandAbstract {
    public static final ExpandNumerator EXPAND_NUMERATOR = new ExpandNumerator();

    private ExpandNumerator() {
        super();
    }

    public ExpandNumerator(Transformation[] transformations) {
        super(transformations);
    }

    public ExpandNumerator(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    public static Tensor expandNumerator(Tensor tensor) {
        return EXPAND_NUMERATOR.transform(tensor);
    }

    public static Tensor expandNumerator(Tensor tensor, Transformation... transformations) {
        return new ExpandNumerator(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        NumeratorDenominator numDen = NumeratorDenominator.getNumeratorAndDenominator(product, NumeratorDenominator.integerDenominatorIndicator);
        Tensor numerator = numDen.numerator;
        if (numerator instanceof Product)
            numerator = ExpandUtils.expandProductOfSums((Product) numDen.numerator, transformations);
        if (numDen.numerator == numerator)
            return product;
        return Tensors.multiply(numerator, numDen.denominator);
    }
}
