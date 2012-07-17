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

import cc.redberry.core.indexmapping.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.*;
import java.util.*;
import java.util.List;
import static cc.redberry.core.tensor.FullContractionsStructure.*;
import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContractIndices implements Transformation {

    public static final ContractIndices CONTRACT_INDICES = new ContractIndices();

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

            ProductContent content = p.getContent();
            FullContractionsStructure fcs = content.getFullContractionsStructure();
            IntArrayList fromList = new IntArrayList(), toList = new IntArrayList();
            int fromTensorIndex, j1, j2, toTensorIndex = -1, k;
            Tensor temp, toTensor;
            long[] contractions, contractions1;

            HashSet<Tensor> positions = new HashSet<>();

            for (fromTensorIndex = content.size() - 1; fromTensorIndex >= 0; --fromTensorIndex) {
                temp = content.get(fromTensorIndex);
                if (positions.contains(temp))
                    continue;

                if (!isKroneckerOrMetric(temp)) {
                    positions.add(temp);
                    continue;
                }

                contractions = fcs.contractions[fromTensorIndex];
                assert contractions.length == 2;

                //here two cases are available : a) contraction: g_mn * X^mn... b) all g_mn indices are free
                if ((j1 = getToTensorIndex(contractions[0])) == (j2 = getToTensorIndex(contractions[1]))) {
                    if (j1 != -1) {// when g_mn is contracting
                        fromList.add(IndicesUtils.inverseIndexState(temp.getIndices().get(0)));
                        toList.add(temp.getIndices().get(1));
                    } else
                        positions.add(temp);// when g_mn has only free indices

                    toTensor = content.get(j1);
                    if (isKroneckerOrMetric(toTensor))
                        positions.add(toTensor);
                    continue;
                }


                //
                //skiping case when e.g. product: g^mn*g_na*g^ai
                //                        cursor:       ^ 
                if (j1 != -1 && j2 != -1) {
                    if (isKroneckerOrMetric(content.get(j1)) && isKroneckerOrMetric(content.get(j2)))
                        continue;

                    if (!isKroneckerOrMetric(content.get(j1)) && !isKroneckerOrMetric(content.get(j1))) {
                        fromList.add(temp.getIndices().get(0));
                        toList.add(IndicesUtils.inverseIndexState(temp.getIndices().get(0)));
                        continue;
                    }
                }

                for (j1 = 0; j1 < 2; ++j1) {
                    toTensorIndex = getToTensorIndex(contractions[j1]);
                    if (toTensorIndex != -1 && isKroneckerOrMetric(content.get(toTensorIndex)))
                        break;
                }

                if (j1 == 2)
                    j1 = 1;
//                fromIndex = IndicesUtils.inverseIndexState(temp.getIndices().get(1 - j1));
                toList.add(temp.getIndices().get(1 - j1));
                j2 = j1;
                WHILE:
                while (true) {
                    toTensor = content.get(toTensorIndex);
                    if (!isKroneckerOrMetric(toTensor)) {
                        fromList.add(content.get(fromTensorIndex).getIndices().get(1 - j2));
                        break;
                    }
                    k = toTensorIndex;
                    contractions1 = fcs.contractions[toTensorIndex];
                    for (j2 = 0; j2 < 2; ++j2) {
                        j1 = getToTensorIndex(contractions1[j2]);
                        if (j1 == -1) {
                            positions.add(toTensor);
                            fromList.add(toTensor.getIndices().get(1 - j2));
                            break WHILE;
                        }
                        if (j1 != fromTensorIndex) {
                            k = toTensorIndex;
                            toTensorIndex = j1;
                        }
                    }
                    fromTensorIndex = k;
                }

            }

            int[] from = fromList.toArray(), to = toList.toArray();
            ArraysUtils.quickSort(from, to);
            IndexMapper mapper = new IndexMapper(from, to);

            ProductBuilder builder = new ProductBuilder();
            builder.put(p.getIndexlessSubProduct());
            for (Tensor position : positions)
                builder.put(applyIndexMapping(position, mapper));
            Tensor re = builder.build();
            iterator.set(re);
        }
        return iterator.result();
    }

    private final static class IndexMapper implements IndexMapping {

        private final int[] from, to;

        public IndexMapper(int[] from, int[] to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int map(int index) {
            int position = Arrays.binarySearch(from, index);
            if (position < 0)
                return index;
            return to[position];
        }
    }

    private static Tensor applyIndexMapping(Tensor tensor, IndexMapper mapper) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        TraverseState state;
        SimpleIndices oldIndices, newIndices;
        SimpleTensor simpleTensor;
        while ((state = iterator.next()) != null) {
            if (state == TraverseState.Leaving)
                continue;
            if (!(iterator.current() instanceof SimpleTensor))
                continue;
            simpleTensor = (SimpleTensor) iterator.current();
            oldIndices = simpleTensor.getIndices();
            newIndices = oldIndices.applyIndexMapping(mapper);
            if (oldIndices != newIndices)
                if (simpleTensor instanceof TensorField)
                    iterator.set(Tensors.setIndicesToField((TensorField) simpleTensor, newIndices));
                else if (Tensors.isKroneckerOrMetric(simpleTensor))
                    iterator.set(Tensors.createMetricOrKronecker(newIndices.get(0), newIndices.get(1)));
                else
                    iterator.set(Tensors.setIndicesToSimpleTensor(simpleTensor, newIndices));
        }
        return iterator.result();
    }
}
