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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SumBuilder implements TensorBuilder {

    private final List<Tensor> summands;
    private Indices freeIndices = null;
    private Complex complex = Complex.ZERO;

    public SumBuilder() {
        summands = new ArrayList<>(7);
    }

    public SumBuilder(int initialCapacity) {
        summands = new ArrayList<>(initialCapacity);
    }

    @Override
    public Tensor buid() {
        if (!complex.isZero() && summands.size() == 1)
            return summands.get(0);

        if (summands.isEmpty())
            return complex;

        if (complex.isZero()) {
            return new Sum(summands.toArray(new Tensor[summands.size()]), freeIndices);
        }
        Tensor[] ss = new Tensor[summands.size() + 1];
        ss[0] = complex;
        System.arraycopy(summands.toArray(new Tensor[summands.size()]), 0, ss, 1, summands.size());
        return new Sum(ss, freeIndices);
    }

    @Override
    public void put(Tensor tensor) {
        if (freeIndices == null)//TODO check indices for sorted
            freeIndices = IndicesFactory.createSorted(tensor.getIndices().getFreeIndices());
        else if (!freeIndices.equalsRegardlessOrder(tensor.getIndices().getFreeIndices()))
            throw new IllegalArgumentException("Inconsistent indices in added summand");
        if (tensor instanceof Complex) {
            complex = complex.add((Complex) tensor);
            return;
        }
        summands.add(tensor);
    }
}
