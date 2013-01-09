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
package cc.redberry.core.indices;

/**
 * This class represents ordered indices and stores indices without sorting. It
 * is the most fundamental {@code Indices} implementation. It is using to
 * represent {@link SimpleTensor} indices, and in contract with, for example,
 * production, witch indices array can be sorted, it stores indices array in
 * order, in witch they were passed in constructor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see SimpleIndices
 * @see EmptyIndices
 * @see AbstractIndices
 * @see SortedIndices
 */
final class SimpleIndicesOfTensor extends SimpleIndicesAbstract {

    SimpleIndicesOfTensor(int[] data, IndicesSymmetries symmetries) {
        super(data, symmetries);
    }

    public SimpleIndicesOfTensor(boolean notResort, int[] data, IndicesSymmetries symmetries) {
        super(notResort, data, symmetries);
    }

    /**
     * This method allows to set {@code Symmetries} of this {@code Indices}.
     *
     * @param symmetries {@code Symmetries} to be set as {@code Symmetries} of
     * this {@code Indices}
     */
    @Override
    public void setSymmetries(IndicesSymmetries symmetries) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IndicesSymmetries getSymmetries() {
        return symmetries;
    }

    @Override
    protected SimpleIndices create(int[] data, IndicesSymmetries symmetries) {
        return new SimpleIndicesOfTensor(true, data, symmetries);
    }
}
