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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.fractions.NumeratorDenominator;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.options.Options;

/**
 * Expands out products and powers that appear as numerators in expressions.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ExpandNumeratorTransformation extends AbstractExpandNumeratorDenominatorTransformation {
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

    @Creator
    public ExpandNumeratorTransformation(@Options ExpandOptions options) {
        super(options);
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
    protected Tensor expandProduct(Tensor product) {
        NumeratorDenominator numDen = NumeratorDenominator.getNumeratorAndDenominator(product, NumeratorDenominator.integerDenominatorIndicator);
        Tensor numerator = ExpandTransformation.expand(numDen.numerator, transformations);
        if (numDen.numerator == numerator)
            return product;
        return Tensors.multiply(numerator, Tensors.reciprocal(numDen.denominator));
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "ExpandNumerator";
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }
}
