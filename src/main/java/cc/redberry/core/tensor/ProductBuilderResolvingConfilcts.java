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

import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.transformations.ApplyIndexMapping;
import cc.redberry.core.utils.TensorUtils;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductBuilderResolvingConfilcts implements TensorBuilder {

    private final ProductBuilder builder;

    public ProductBuilderResolvingConfilcts(int initialCapacityIndexless, int initialCapacityData) {
        builder = new ProductBuilder(initialCapacityIndexless, initialCapacityData);
    }

    public ProductBuilderResolvingConfilcts() {
        builder = new ProductBuilder();
    }

    @Override
    public Tensor build() {
        Tensor t = builder.build();
        if (!(t instanceof Product))
            return t;
        //postprocessing product
        Product p = (Product) t;
        //all product indices
        Set<Integer> totalIndices = TensorUtils.getAllIndices(p);
        int i, j;
        int[] forbidden;
        Tensor current;

        //processing indexless data
        for (i = 0; i < p.indexlessData.length; ++i) {
            current = p.indexlessData[i];
            if (current instanceof Sum || current instanceof Power) {
                forbidden = new int[totalIndices.size()];
                j = -1;
                for (Integer index : totalIndices)
                    forbidden[++j] = index;
                p.indexlessData[i] = ApplyIndexMapping.renameDummyFromClonedSource(current, forbidden);
                if (current != p.indexlessData[i])//adding generated indices to totalIndices only if renames were performed
                    totalIndices.addAll(TensorUtils.getAllIndices(p.indexlessData[i]));
            }
        }
        Set<Integer> free;
        for (i = 0; i < p.data.length; ++i) {
            current = p.data[i];
            if (current instanceof Sum || current instanceof Power) {
                free = new HashSet<>(current.getIndices().size());
                for (j = current.getIndices().size() - 1; j >= 0; --j)
                    free.add(IndicesUtils.getNameWithType(current.getIndices().get(j)));
                totalIndices.removeAll(free);
                forbidden = new int[totalIndices.size()];
                j = -1;
                for (Integer index : totalIndices)
                    forbidden[++j] = index;
                p.data[i] = ApplyIndexMapping.renameDummyFromClonedSource(current, forbidden);
                if (current != p.data[i])//adding generated indices to totalIndices only if renames were performed                    
                    totalIndices.addAll(TensorUtils.getAllIndices(p.data[i]));
                else//otherwise adding removed free indices
                    totalIndices.addAll(free);
            }
        }
        return p;
    }

    @Override
    public void put(Tensor tensor) {
        builder.put(tensor);
    }
}
