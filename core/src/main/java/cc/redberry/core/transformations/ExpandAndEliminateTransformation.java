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
package cc.redberry.core.transformations;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.ArraysUtils;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandAndEliminateTransformation implements TransformationToStringAble {
    public static final ExpandAndEliminateTransformation EXPAND_AND_ELIMINATE = new ExpandAndEliminateTransformation();

    private final Transformation[] transformations;

    private ExpandAndEliminateTransformation() {
        this.transformations = new Transformation[]{EliminateMetricsTransformation.ELIMINATE_METRICS};
    }

    public ExpandAndEliminateTransformation(Transformation... transformations) {
        this.transformations = ArraysUtils.addAll(new Transformation[]{EliminateMetricsTransformation.ELIMINATE_METRICS}, transformations);
    }

    @Override
    public Tensor transform(Tensor t) {
        return Transformation.Util.applySequentially(ExpandTransformation.expand(t, transformations), transformations);
    }

    public static Tensor expandAndEliminate(Tensor t) {
        return EXPAND_AND_ELIMINATE.transform(t);
    }


    public static Tensor expandAndEliminate(Tensor t, Transformation... transformations) {
        return new ExpandAndEliminateTransformation(transformations).transform(t);
    }

    @Override
    public String toString(OutputFormat f) {
        return "ExpandAndEliminate";
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }
}
