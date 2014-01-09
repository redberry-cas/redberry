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
package cc.redberry.core.tensorgenerator;

import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.context.CC;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TensorGeneratorUtils {

    private TensorGeneratorUtils() {
    }

    public static Tensor[] allStatesCombinations(Tensor st) {

        Indices indices = st.getIndices().getFree();
        int[] indicesArray = indices.getAllIndices().copy();
        //lowering all indices
        IntArrayList metricIndices = new IntArrayList(),
                nonMetricIndices = new IntArrayList();

        for (int i = 0; i < indices.size(); ++i) {
            if (CC.isMetric(getType(indices.get(i))))
                metricIndices.add(getNameWithType(indices.get(i)));
            else
                nonMetricIndices.add(indices.get(i));
        }


        final int[] metricInds = metricIndices.toArray();
        ArrayList<Tensor> samples = new ArrayList<>(ArithmeticUtils.pow(2, metricInds.length));
        IntCombinationsGenerator gen;
        int[] temp;
        ArrayList<Tensor> combinationArray;
        for (int i = 0; i <= metricInds.length; ++i) {
            gen = new IntCombinationsGenerator(metricInds.length, i);
            combinationArray = new ArrayList<>();
            combinations:
            for (int[] combination : gen) {
                temp = new int[metricInds.length];
                Arrays.fill(temp, 0xFFFFFFFF);
                for (int j = combination.length - 1; j >= 0; --j)
                    temp[combination[j]] = createIndex(j, getType(metricInds[combination[j]]), true);//raise index
                int counter = combination.length;
                for (int j = 0; j < metricInds.length; ++j)
                    if (temp[j] == 0xFFFFFFFF)
                        temp[j] = createIndex(counter++, getType(metricInds[j]), false);//lower index
                IntArrayList _result = nonMetricIndices.clone();
                _result.addAll(temp);
                Tensor renamed = ApplyIndexMapping.applyIndexMapping(st, new Mapping(indicesArray, _result.toArray()));
                //todo bottleneck
                for (Tensor existing : combinationArray)
                    if (TensorUtils.compare1(existing, renamed) != null)
                        continue combinations;
                combinationArray.add(renamed);

            }
            samples.addAll(combinationArray);
        }
        return samples.toArray(new Tensor[samples.size()]);
    }
}
