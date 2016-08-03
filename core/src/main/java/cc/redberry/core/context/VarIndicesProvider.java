/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.context;

import cc.redberry.core.indices.*;

/**
 * Computes indices and symmetries of function
 *
 * @author Stanislav Poslavsky
 */
public interface VarIndicesProvider {
    /**
     * Computes indices of function
     *
     * @param self    head indices (e.g. for function {@code f_ab[x_a, y_a] -- "_ab"})
     * @param indices indices of arguments (e.g. for function {@code f_ab[x_a, y_a] -- {"_a", "_a"}})
     * @return resulting indices of function
     */
    SimpleIndices compute(SimpleIndices self, Indices... indices);

    /**
     * Returns indices of head (indices of arguments are irrelevant)
     */
    VarIndicesProvider SelfIndices = new VarIndicesProvider() {
        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {
            return self;
        }
    };

    /**
     * Returns indices of first arg (e.g. Expand[x_a] has indices "_a")
     */
    VarIndicesProvider FirstArg = new VarIndicesProvider() {
        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {
            if (self.size() != 0 || indices.length != 1)
                throw new IllegalArgumentException();
            SimpleIndices argIndices = IndicesFactory.createSimple(null, indices[0]);
            return UnsafeIndicesFactory.createOfTensor(IndicesSymmetries.create(argIndices.getStructureOfIndices()), argIndices);
        }
    };
}
