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

import cc.redberry.core.tensor.Tensor;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class ContentData {
    static final ContentData EMPTY_INSTANCE =
            new ContentData(GraphStructureHashed.EMPTY_INSTANCE,
                    GraphStructure.EMPTY_FULL_CONTRACTIONS_STRUCTURE,
                    new Tensor[0], new short[0], new int[0]);

    final GraphStructureHashed structureOfContractionsHashed;
    final GraphStructure structureOfContractions;
    final Tensor[] data;
    final short[] stretchIndices;
    final int[] hashCodes;

    ContentData(GraphStructureHashed structureOfContractionsHashed,
                GraphStructure structureOfContractions,
                Tensor[] data, short[] stretchIds, int[] hashCodes) {
        this.structureOfContractionsHashed = structureOfContractionsHashed;
        this.structureOfContractions = structureOfContractions;
        this.data = data;
        this.stretchIndices = stretchIds;
        this.hashCodes = hashCodes;
    }
}
