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

package cc.redberry.physics.oneloopdiv;

import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.*;
import java.util.Arrays;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class SqrSubs implements Transformation {

    private int name;

    public SqrSubs(SimpleTensor st) {
        if (st.getIndices().size() != 1)
            throw new IllegalArgumentException();
        name = st.getName();
    }

    @Override
    public Tensor transform(Tensor tensor) {
        if (!(tensor instanceof Product))
            return tensor;
        Product product = (Product) tensor;

        ProductContent content = product.getContent();
        StructureOfContractionsHashed cs = content.getStructureOfContractionsHashed();
        short si = content.getStretchIndexByHash(name);
        if (si == -1)
            return tensor;

        TensorContraction contraction = new TensorContraction(si, new long[]{((long) si) << 16});
        short[] sIndices = content.getStretchIds(); //For preformance.
        int index = Arrays.binarySearch(sIndices, si);
        while (index >= 0 && sIndices[index--] == si);
        ++index;
        IntArrayList list = new IntArrayList();
        do {
            Tensor t = content.get(index);
            if (!(t instanceof SimpleTensor))
                continue;
            SimpleTensor st = (SimpleTensor) t;
            if (st.getName() != name)
                continue;
            int indexName;
            if (cs.get(index).equals(contraction)
                    && ((indexName = st.getIndices().get(0)) & 0x80000000) == 0)
                list.add(indexName);
        } while (index < sIndices.length - 1 && sIndices[++index] == si);
        int[] indices = list.toArray();

        if (indices.length == 0)
            return tensor;

        Arrays.sort(indices);
        IntArrayList toRemvoe = new IntArrayList();
        Tensor current;
        int size = content.size();
        for (index = 0; index < size; ++index) {
            current = content.get(index);
            if (!(current instanceof SimpleTensor))
                continue;
            SimpleTensor st = (SimpleTensor) current;
            if (st.getName() != name)
                continue;
            if (Arrays.binarySearch(indices, IndicesUtils.getNameWithType(st.getIndices().get(0))) >= 0)
                toRemvoe.add(index);
        }
        if (toRemvoe.size() == 0)
            return tensor;

        Tensor indexless = product.getIndexlessSubProduct();
        ProductBuilder pb = new ProductBuilder();
        pb.put(indexless);

        for (int i = size - 1; i >= 0; --i)
            if (ArraysUtils.binarySearch(toRemvoe, i) < 0)//toRemove is sorted                
                pb.put(content.get(i));
        return pb.build();
    }
}
