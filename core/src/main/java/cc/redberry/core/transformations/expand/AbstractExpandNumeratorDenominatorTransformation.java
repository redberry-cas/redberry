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

import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;

/**
 * Abstract class for {@link cc.redberry.core.transformations.expand.ExpandDenominatorTransformation} and
 * {@link cc.redberry.core.transformations.expand.ExpandNumeratorTransformation}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class AbstractExpandNumeratorDenominatorTransformation implements TransformationToStringAble {
    protected final Transformation[] transformations;

    protected AbstractExpandNumeratorDenominatorTransformation(Transformation[] transformations) {
        this.transformations = transformations;
    }

    protected AbstractExpandNumeratorDenominatorTransformation() {
        this(new Transformation[0]);
    }

    protected AbstractExpandNumeratorDenominatorTransformation(ExpandOptions options) {
        this(new Transformation[]{options.simplifications});
    }

    @Override
    public Tensor transform(Tensor t) {
        //expand applies only on the top level
        if (t instanceof Product || t instanceof Power)
            return expandProduct(t);
        if (t instanceof Sum) {
            SumBuilder sb = new SumBuilder(t.size());
            for (Tensor s : t)
                sb.put(transform(s));
            return sb.build();
        }
        return t;
    }

    protected abstract Tensor expandProduct(Tensor t);
}
