/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.transformations.powerexpand;

import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.Indicator;

import static cc.redberry.core.transformations.powerexpand.PowerExpandUtils.*;

/**
 * Expands all powers of products and powers with respect to specified variables.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.5
 */
public final class PowerExpandTransformation implements Transformation {
    public static final PowerExpandTransformation POWER_EXPAND_TRANSFORMATION = new PowerExpandTransformation();
    private final Indicator<Tensor> toExpandIndicator;

    private PowerExpandTransformation() {
        this(Indicator.TRUE_INDICATOR);
    }

    /**
     * Creates PowerExpand transformation that expands all powers of products expanding powers matching specified
     * indicator.
     *
     * @param toExpandIndicator pattern
     */
    public PowerExpandTransformation(final Indicator<Tensor> toExpandIndicator) {
        this.toExpandIndicator = toExpandIndicator;
    }

    /**
     * Creates PowerExpand transformation that expands all powers of products expands only with respect to the
     * specified simple tensors.
     *
     * @param vars simple tensors
     */
    public PowerExpandTransformation(final SimpleTensor... vars) {
        this(varsToIndicator(vars));
    }

    @Override
    public Tensor transform(Tensor t) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;

        while ((c = iterator.next()) != null)
            if (powerExpandApplicable(c, toExpandIndicator))
                iterator.set(Tensors.multiply(powerExpandToArray1(c, toExpandIndicator)));

        return iterator.result();
    }
}
