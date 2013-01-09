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
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SimpleIndicesIsolated extends SimpleIndicesAbstract {

    SimpleIndicesIsolated(int[] data, IndicesSymmetries symmetries) {
        super(data, symmetries);
    }

    SimpleIndicesIsolated(boolean notResort, int[] data, IndicesSymmetries symmetries) {
        super(notResort, data, symmetries);
    }

    @Override
    protected SimpleIndices create(int[] data, IndicesSymmetries symmetries) {
        return new SimpleIndicesIsolated(true, data, symmetries == null ? null : symmetries.clone());
    }

    @Override
    public IndicesSymmetries getSymmetries() {
        if (symmetries == null)
            symmetries = new IndicesSymmetries(new StructureOfIndices(this));
        return symmetries;
    }

    @Override
    public void setSymmetries(IndicesSymmetries symmetries) {
        if (!symmetries.getStructureOfIndices().isStructureOf(this))
            throw new IllegalArgumentException("Illegal symmetries instance.");
        this.symmetries = symmetries;
    }
}
