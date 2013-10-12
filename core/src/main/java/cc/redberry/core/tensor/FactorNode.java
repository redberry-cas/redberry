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
package cc.redberry.core.tensor;

import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexgenerator.IndexGeneratorFromData;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class FactorNode {

    final Tensor factor;
    private final TensorBuilder builder;
    int[] factorForbiddenIndices;

    FactorNode(Tensor factor, TensorBuilder builder) {
        this.factor = ApplyIndexMapping.optimizeDummies(factor);
        this.builder = builder;
        factorForbiddenIndices = TensorUtils.getAllIndicesNamesT(this.factor).toArray();
    }

    private FactorNode(Tensor factor, TensorBuilder builder, int[] factorForbiddenIndices) {
        this.factor = factor;
        this.builder = builder;
        this.factorForbiddenIndices = factorForbiddenIndices;
    }

    void put(Tensor summand, Tensor factor) {
        TIntHashSet allowed = TensorUtils.getAllDummyIndicesT(factor);
        allowed.removeAll(factorForbiddenIndices);
        IndexGenerator ig = new IndexGeneratorFromData(allowed.toArray());
        //old variant
        //IndexGenerator ig = new IndexGeneratorFromData(TensorUtils.getAllDummyIndicesT(factor).toArray());
        summand = ApplyIndexMapping.renameDummy(summand, factorForbiddenIndices, ig);
        builder.put(summand);
    }

    Tensor build() {
        return builder.build();
    }

    @Override
    public FactorNode clone() {
        //factorForbiddenIndices are immuable
        return new FactorNode(factor, builder.clone(), factorForbiddenIndices);
    }
}
