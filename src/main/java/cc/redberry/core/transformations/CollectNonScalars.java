/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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

import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.SumBuilderSplitingScalars;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.iterator.TensorLastIterator;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class CollectNonScalars implements Transformation {

    public static final CollectNonScalars CollectNonScalars = new CollectNonScalars();

    private CollectNonScalars() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return collectNonScalars(t);
    }

    public static Tensor collectNonScalars(Tensor t) {
        TensorLastIterator iterator = new TensorLastIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Sum) {
                //TODO add check whether we need to do this transformation
                SumBuilderSplitingScalars sbss = new SumBuilderSplitingScalars(c.size());
                for (Tensor tt : c)
                    sbss.put(tt);
                iterator.set(sbss.build());
            }
        return iterator.result();
    }
}
