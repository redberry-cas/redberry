/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
package cc.redberry.core.tensor;

import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.utils.TensorUtils;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class FactorNode {

    final Tensor factor;
    private final TensorBuilder builder;
    int[] factorForbiddenIndices;

    FactorNode(Tensor factor, TensorBuilder builder) {
        this.factor = factor;
        this.builder = builder;
        Set<Integer> factorIndices = TensorUtils.getAllIndicesNames(factor);
        factorForbiddenIndices = new int[factorIndices.size()];
        int i = -1;
        for (Integer ii : factorIndices)
            factorForbiddenIndices[++i] = ii;
    }

    void put(Tensor t) {
        t = ApplyIndexMapping.renameDummy(t, factorForbiddenIndices);//TODO improve performance!!!!!!!
        builder.put(t);
    }

    Tensor build() {
        return builder.build();
    }
}