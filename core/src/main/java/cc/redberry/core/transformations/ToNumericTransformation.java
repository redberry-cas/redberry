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
package cc.redberry.core.transformations;

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;

/**
 * Gives the numerical value of tensor (replace all numbers with their numerical values).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class ToNumericTransformation implements Transformation {
    /**
     * Singleton instance
     */
    public static final ToNumericTransformation TO_NUMERIC = new ToNumericTransformation();

    private ToNumericTransformation() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return toNumeric(t);
    }

    /**
     * Gives the numerical value of tensor (replace all numbers with their numerical values)
     *
     * @param t tensor
     * @return the numerical value of tensor
     */
    public static Tensor toNumeric(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Complex)
                iterator.set(((Complex) c).getNumericValue());

        return iterator.result();
    }
}
