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
package cc.redberry.core.indices;

import java.lang.ref.WeakReference;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
final class SimpleIndicesIsolated extends AbstractSimpleIndices {

    SimpleIndicesIsolated(int[] data, IndicesSymmetries symmetries) {
        super(data, symmetries);
    }

    SimpleIndicesIsolated(boolean notResort, int[] data, IndicesSymmetries symmetries) {
        super(notResort, data, symmetries);
    }

    SimpleIndicesIsolated(boolean notSort, int[] data, IndicesSymmetries symmetries, UpperLowerIndices ul) {
        super(notSort, data, symmetries);
        this.upperLower = new WeakReference<>(ul);
    }

    @Override
    protected SimpleIndices create(int[] data, IndicesSymmetries symmetries) {
        return new SimpleIndicesIsolated(true, data, symmetries == null ? null : symmetries.clone());
    }

    @Override
    public IndicesSymmetries getSymmetries() {
        if (symmetries == null)
            symmetries = IndicesSymmetries.create(StructureOfIndices.create(this));
        return symmetries;
    }

    @Override
    public void setSymmetries(IndicesSymmetries symmetries) {
        if (!symmetries.getStructureOfIndices().isStructureOf(this))
            throw new IllegalArgumentException("Illegal symmetries instance.");
        this.symmetries = symmetries;
    }
}
