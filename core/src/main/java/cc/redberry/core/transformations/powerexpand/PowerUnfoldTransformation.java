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
package cc.redberry.core.transformations.powerexpand;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.Creator;
import cc.redberry.core.transformations.substitutions.SubstitutionIterator;
import cc.redberry.core.utils.Indicator;

import static cc.redberry.core.transformations.powerexpand.PowerExpandUtils.*;

/**
 * Expands all powers of products and powers with respect to specified variables and unwraps powers of
 * indexed arguments into products (e.g. (A_m*A^m)**2 -> A_m*A^m*A_a*A^a).
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1.5
 */
public final class PowerUnfoldTransformation implements TransformationToStringAble {
    public static final PowerUnfoldTransformation POWER_UNFOLD_TRANSFORMATION =
            new PowerUnfoldTransformation();
    private final Indicator<Tensor> toExpandIndicator;

    private PowerUnfoldTransformation() {
        this(Indicator.TRUE_INDICATOR);
    }

    /**
     * Creates transformation that expands all powers of products and powers and
     * unwraps powers of indexed arguments into products (e.g. (A_m*A^m)**2 -> A_m*A^m*A_a*A^a).
     *
     * @param toExpandIndicator applies only to powers that match this indicator
     */
    public PowerUnfoldTransformation(final Indicator<Tensor> toExpandIndicator) {
        this.toExpandIndicator = toExpandIndicator;
    }

    /**
     * Creates transformation that expands all powers of products and powers with respect to specified variables and
     * unwraps powers of indexed arguments into products (e.g. (A_m*A^m)**2 -> A_m*A^m*A_a*A^a).
     *
     * @param vars patterns
     */
    @Creator(vararg = true, hasArgs = true)
    public PowerUnfoldTransformation(final SimpleTensor... vars) {
        this(Indicator.Utils.iterativeIndicator(varsToIndicator(vars)));
    }

    @Override
    public Tensor transform(Tensor t) {
        SubstitutionIterator iterator = new SubstitutionIterator(t);
        Tensor c;

        while ((c = iterator.next()) != null)
            if (powerUnfoldApplicable(c, toExpandIndicator))
                iterator.set(Tensors.multiply(powerExpandIntoChainToArray1(c, iterator.getForbidden(), toExpandIndicator)));

        return iterator.result();
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        return "PowerUnfold";
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }
}
