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
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.fractions.NumeratorDenominator;

/**
 * Expands out products and powers that appear as numerators in expressions.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ExpandNumeratorTransformation extends AbstractExpandTransformation {
    /**
     * The default instance.
     */
    public static final ExpandNumeratorTransformation EXPAND_NUMERATOR = new ExpandNumeratorTransformation();

    private ExpandNumeratorTransformation() {
        super();
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand.
     *
     * @param transformations transformations to be applied after each step of expand
     */
    public ExpandNumeratorTransformation(Transformation[] transformations) {
        super(transformations);
    }

    /**
     * Creates expand transformation with specified additional transformations to
     * be applied after each step of expand and leaves unexpanded parts of expression specified by
     * {@code traverseGuide}.
     *
     * @param transformations transformations to be applied after each step of expand
     * @param traverseGuide   traverse guide
     */
    public ExpandNumeratorTransformation(Transformation[] transformations, TraverseGuide traverseGuide) {
        super(transformations, traverseGuide);
    }

    /**
     * Expands out products and powers that appear as numerators of tensor.
     *
     * @param tensor tensor to be transformed
     * @return result
     */
    public static Tensor expandNumerator(Tensor tensor) {
        return EXPAND_NUMERATOR.transform(tensor);
    }

    /**
     * Expands out products and powers that appear as numerators of tensor and applies specified transformations
     * after each step of expand.
     *
     * @param tensor tensor to be transformed
     * @return result
     */
    public static Tensor expandNumerator(Tensor tensor, Transformation... transformations) {
        return new ExpandNumeratorTransformation(transformations).transform(tensor);
    }

    @Override
    protected Tensor expandProduct(Product product, Transformation[] transformations) {
        NumeratorDenominator numDen = NumeratorDenominator.getNumeratorAndDenominator(product, NumeratorDenominator.integerDenominatorIndicator);
        Tensor numerator = numDen.numerator;
        if (numerator instanceof Product)
            numerator = ExpandUtils.expandProductOfSums((Product) numDen.numerator, transformations);
        if (numDen.numerator == numerator)
            return product;
        return Tensors.multiply(numerator, Tensors.reciprocal(numDen.denominator));
    }
}
