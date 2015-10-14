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
package cc.redberry.core.transformations.reverse;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationToStringAble;
import cc.redberry.core.transformations.options.Creator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ReverseTransformation implements TransformationToStringAble {
    private final SingleReverse[] reverse;

    @Creator(vararg = true)
    public ReverseTransformation(IndexType... types) {
        reverse = new SingleReverse[types.length];
        for (int i = 0; i < types.length; i++)
            reverse[i] = new SingleReverse(types[i]);
    }

    @Override
    public Tensor transform(Tensor t) {
        return Transformation.Util.applySequentially(t, reverse);
    }

    @Override
    public String toString(OutputFormat outputFormat) {
        StringBuilder sb = new StringBuilder().append("Reverse[");
        for (int i = 0; ; ++i) {
            sb.append(reverse[i].type);
            if (i == reverse.length - 1)
                return sb.append("]").toString();
            sb.append(",");
        }
    }

    @Override
    public String toString() {
        return toString(CC.getDefaultOutputFormat());
    }

}
