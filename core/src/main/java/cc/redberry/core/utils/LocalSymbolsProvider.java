/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.utils;

import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Sequentially generates variables of the form "prefix0","prefix1","prefix2","prefix3" etc., so that each
 * symbol does not  appear in the specified tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see TensorUtils#generateReplacementsOfScalars(cc.redberry.core.tensor.Tensor, cc.redberry.concurrent.OutputPortUnsafe)
 */
public class LocalSymbolsProvider implements OutputPortUnsafe<SimpleTensor> {
    private final String prefix;
    private final TIntHashSet forbiddenNames;
    private long counter = 0;

    /**
     * Creates provider with given prefix.
     *
     * @param forbidden forbidden content
     * @param prefix    string prefix
     */
    public LocalSymbolsProvider(Tensor forbidden, String prefix) {
        this.forbiddenNames = TensorUtils.getSimpleTensorsNames(forbidden);
        this.prefix = prefix;
    }

    @Override
    public SimpleTensor take() {
        SimpleTensor st;
        do {
            st = Tensors.simpleTensor(prefix + (counter++), IndicesFactory.EMPTY_SIMPLE_INDICES);
        } while (forbiddenNames.contains(st.getName()));
        return st;
    }
}
