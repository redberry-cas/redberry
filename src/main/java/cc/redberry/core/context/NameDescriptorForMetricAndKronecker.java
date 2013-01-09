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
package cc.redberry.core.context;

import cc.redberry.core.indices.IndicesTypeStructure;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
final class NameDescriptorForMetricAndKronecker extends NameDescriptor {

    //first name for kronecker, second for metric
    //same instance as in NameManager
    private final String[] names;

    public NameDescriptorForMetricAndKronecker(String[] names, byte type, int id) {
        super(createIndicesTypeStructures(type), id);
        this.names = names;
    }

    private static IndicesTypeStructure[] createIndicesTypeStructures(byte type) {
        IndicesTypeStructure[] structures = new IndicesTypeStructure[1];
        if (!CC.isMetric(type))
            structures[0] = new IndicesTypeStructure(type, 2, true, false);
        else
            structures[0] = new IndicesTypeStructure(type, 2);
        return structures;
    }

    /**
     * first for kronecker, second for metric
     *
     * @return
     */
    @Override
    IndicesTypeStructureAndName[] getKeys() {
        return new IndicesTypeStructureAndName[]{new IndicesTypeStructureAndName(names[0], indexTypeStructures),
                new IndicesTypeStructureAndName(names[1], indexTypeStructures)};
    }

    @Override
    public String getName(SimpleIndices indices) {
        return (IndicesUtils.getRawStateInt(indices.get(0)) ^ IndicesUtils.getRawStateInt(indices.get(1))) == 0x80000000 ? names[0] : names[1];
    }

    @Override
    public String toString() {
        return names[0] + ":" + Arrays.toString(indexTypeStructures);
    }
}
