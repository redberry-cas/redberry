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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.number.Complex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SumBuilderSplitingScalars extends AbstractSumBuilder {

    SumBuilderSplitingScalars(Map<Integer, List<FactorNode>> summands, Complex complex, Indices indices, int[] sortedFreeIndices) {
        super(summands, complex, indices, sortedFreeIndices);
    }

    public SumBuilderSplitingScalars(int initialCapacity) {
        super(initialCapacity);
    }

    public SumBuilderSplitingScalars() {
    }

    @Override
    protected Split split(Tensor tensor) {
        return Split.splitScalars(tensor);
    }

    @Override
    public TensorBuilder clone() {
        Map<Integer, List<FactorNode>> summands = new HashMap<>(this.summands);
        for (Map.Entry<Integer, List<FactorNode>> entry : summands.entrySet()) {
            List<FactorNode> fns = entry.getValue();
            for (int i = fns.size() - 1; i >= 0; --i)
                fns.set(i, fns.get(i).clone());
        }
        return new SumBuilderSplitingScalars(summands, complex, indices, sortedFreeIndices.clone());
    }
}
