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
package cc.redberry.core.transformations.fractions;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;

/**
 * Gives the denominator of expression.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class GetDenominatorTransformation implements TransformationToStringAble {
    /**
     * Singleton instance.
     */
    public static final GetDenominatorTransformation GET_DENOMINATOR = new GetDenominatorTransformation();

    private GetDenominatorTransformation() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return NumeratorDenominator.getNumeratorAndDenominator(t).denominator;
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "Denominator";
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }
}
