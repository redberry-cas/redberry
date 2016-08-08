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
     * Whether i-th argument propagates indices to outer environment. For example, Expand[x_a*(y_b + z_b)] propagates
     * indices as is, while for abstract f_ab[x_a] indices of argument are irrelevant
     *
     * @param i index of argument
     */
    boolean propagatesIndices(int i);

    /**
     * Returns whether any of arguments should propagate its indices outside of the field scope (like e.g. Expand[x_a])
     *
     * @return whether any of arguments should propagate its indices outside of the field scope (like e.g. Expand[x_a])
     */
    boolean propagatesIndices();

    /**
     * Returns indices of head (indices of arguments are irrelevant)
     */
    VarIndicesProvider SelfIndices = new VarIndicesProvider() {
        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {return self;}

        @Override
        public boolean propagatesIndices(int i) {return false;}

        @Override
        public boolean propagatesIndices() {return false;}
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

        @Override
        public boolean propagatesIndices(int i) {return i == 0;}

        @Override
        public boolean propagatesIndices() {return true;}
    };

    /**
     * Joins indices of the first argument
     */
    VarIndicesProvider JoinFirst = new VarIndicesProvider() {
        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {
            if (indices[0].size() == 0)
                return self;
            SimpleIndices argIndices = indices[0] instanceof SimpleIndices ? (SimpleIndices) indices[0] : IndicesFactory.createSimple(null, indices[0]);
            return new SimpleIndicesBuilder().append(self).append(argIndices).getIndices();
        }

        @Override
        public boolean propagatesIndices(int i) {return i == 0;}

        @Override
        public boolean propagatesIndices() {return true;}
    };
    /**
     * Joins indices of all arguments
     */
    VarIndicesProvider AllArgs = new VarIndicesProvider() {
        @Override
        public SimpleIndices compute(SimpleIndices self, Indices... indices) {
            SimpleIndicesBuilder ib = new SimpleIndicesBuilder().append(self);
            for (Indices ii : indices)
                if (ii instanceof SimpleIndices)
                    ib.append((SimpleIndices) ii);
                else
                    ib.append(IndicesFactory.createSimple(null, ii));
            return ib.getIndices();
        }

        @Override
        public boolean propagatesIndices(int i) {
            return true;
        }

        @Override
        public boolean propagatesIndices() {
            return true;
        }
    };
}
