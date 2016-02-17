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
package cc.redberry.core.tensor.playground;

/**
 * Representation of hashed graph of product.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
final class GraphStructureHashed {
    /**
     * Singleton for empty structure
     */
    public static final GraphStructureHashed EMPTY_INSTANCE =
            new GraphStructureHashed(new short[0], new long[0], new long[0][0]);

    final short[] stretchIndices;
    final long[] freeContraction;
    final long[][] contractions;

    public GraphStructureHashed(short[] stretchIndices, long[] freeContraction, long[][] contractions) {
        this.stretchIndices = stretchIndices;
        this.freeContraction = freeContraction;
        this.contractions = contractions;
    }
}
