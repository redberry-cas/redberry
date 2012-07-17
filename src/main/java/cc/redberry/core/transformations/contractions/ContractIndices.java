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
package cc.redberry.core.transformations.contractions;

import cc.redberry.core.context.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.*;
import static cc.redberry.core.tensor.FullContractionsStructure.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContractIndices implements Transformation {

    @Override
    public Tensor transform(Tensor t) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        TraverseState state;
        Tensor current;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            current = iterator.current();
            if (!(current instanceof Product))
                continue;
            Product p = (Product) current;

            ProductContent productContent = p.getContent();
            FullContractionsStructure fcs = productContent.getFullContractionsStructure();
            IntArrayList from = new IntArrayList(), to = new IntArrayList();
            int fromTensorIndex, j, toTensorIndex = -1, fromIndex, toIndex;
            Tensor temp;
            long[] contractions;
            for (fromTensorIndex = productContent.size() - 1; fromTensorIndex >= 0; --fromTensorIndex) {
                temp = productContent.get(fromTensorIndex);
                if (Tensors.isKroneckerOrMetric(temp)) {

                    contractions = fcs.contractions[fromTensorIndex];

                    assert contractions.length == 2;

                    for (j = 0; j < 2; ++j) {
                        toTensorIndex = getToTensorIndex(contractions[j]);
                        if (toTensorIndex == -1 || Tensors.isKroneckerOrMetric(temp = productContent.get(toTensorIndex)))
                            continue;
                        else
                            break;
                    }
                    if (toTensorIndex == -1)
                        continue;

                    fromIndex = IndicesUtils.inverseIndexState(temp.getIndices().get(j));
                    toIndex = temp.getIndices().get(1 - j);

                    temp = productContent.get(toTensorIndex);
                    while (toTensorIndex != -1
                            && Tensors.isKroneckerOrMetric(temp)){
                        toIndex = temp.getIndices().get(1 - j);
                        temp = productContent.get(toTensorIndex);
                    }



                    from.add(fromIndex);
                    to.add(toIndex);

                }

            }

        }
        return iterator.result();
    }
}
