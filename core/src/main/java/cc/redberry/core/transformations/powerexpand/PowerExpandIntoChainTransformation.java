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
package cc.redberry.core.transformations.powerexpand;

import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.Indicator;

import static cc.redberry.core.transformations.powerexpand.PowerExpandUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PowerExpandIntoChainTransformation implements Transformation {
    public static final PowerExpandIntoChainTransformation POWER_EXPAND_TRANSFORMATION = new PowerExpandIntoChainTransformation();
    private final Indicator<Tensor> toExpandIndicator;

    private PowerExpandIntoChainTransformation() {
        this(Indicator.TRUE_INDICATOR);
    }

    public PowerExpandIntoChainTransformation(final Indicator<Tensor> toExpandIndicator) {
        this.toExpandIndicator = toExpandIndicator;
    }

    public PowerExpandIntoChainTransformation(final SimpleTensor[] vars) {
        this(Indicator.Utils.iterativeIndicator(varsToIndicator(vars)));
    }

    @Override
    public Tensor transform(Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor c;

        while ((c = iterator.next()) != null)
            if (powerExpandApplicable(c, toExpandIndicator))
                iterator.set(Tensors.multiply(powerExpandIntoChainToArray1(c, iterator.getForbidden(), toExpandIndicator)));


        return iterator.result();
    }
}
