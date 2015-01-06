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

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.expand.ExpandTransformation;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandAndEliminateTransformation implements Transformation {
    public static final ExpandAndEliminateTransformation EXPAND_AND_ELIMINATE = new ExpandAndEliminateTransformation();

    private ExpandAndEliminateTransformation() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return expandAndEliminate(t);
    }

    public static Tensor expandAndEliminate(Tensor t) {
        return EliminateMetricsTransformation.eliminate(ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS));
    }
}
