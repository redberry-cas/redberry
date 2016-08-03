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
package cc.redberry.core.context2;

import cc.redberry.core.indices.*;

/**
 * @author Stanislav Poslavsky
 */
public interface VarIndicesProvider {
    SimpleIndices compute(SimpleIndices self, Indices... indices);

    IndicesSymmetries unique();

    /**
     * Holds a unique
     */
    final class SymmetriesHolder implements VarIndicesProvider {
        public final IndicesSymmetries symmetries;

        public SymmetriesHolder(IndicesSymmetries symmetries) {
            this.symmetries = symmetries;
        }

        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {
            return UnsafeIndicesFactory.createOfTensor(symmetries, self);
        }

        @Override
        public IndicesSymmetries unique() {
            return symmetries;
        }
    }

    final class IndicesPropagator implements VarIndicesProvider {
        public static final IndicesPropagator IndicesPropagator = new IndicesPropagator();

        private IndicesPropagator() {
        }

        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {
            if (self.size() != 0 || indices.length != 1)
                throw new IllegalStateException();

            SimpleIndices argIndices = IndicesFactory.createSimple(null, indices[0]);
            return UnsafeIndicesFactory.createOfTensor(IndicesSymmetries.create(argIndices.getStructureOfIndices()), argIndices);
        }

        @Override
        public IndicesSymmetries unique() {
            return null;
        }
    }
}
