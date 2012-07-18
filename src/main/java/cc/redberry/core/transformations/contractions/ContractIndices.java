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

import java.util.ArrayList;
import java.util.List;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.transformations.Transformation;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ContractIndices implements Transformation {

    public static final ContractIndices CONTRACT_INDICES = new ContractIndices();

    private ContractIndices() {
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return transform(tensor, RootMetricKroneckerContainer.INSTANCE);
    }

    //TODO possibly refactor using iteration guide
    private Tensor transform(Tensor tensor, MetricKroneckerContainer container) {
        if (tensor.getClass() == SimpleTensor.class)
            return container.apply((SimpleTensor) tensor);
        if (tensor instanceof TensorField) {
            tensor = container.apply((SimpleTensor) tensor);
            TensorBuilder builder = tensor.getBuilder();
            int size = tensor.size();
            for (int i = 0; i < size; ++i)
                builder.put(transform(tensor.get(i)));
            return builder.build();
        }
        if (tensor instanceof ScalarFunction) {
            TensorBuilder builder = tensor.getBuilder();
            for (int i = tensor.size() - 1; i >= 0; --i)
                builder.put(transform(tensor.get(i)));
            return builder.build();
        }
        if (tensor instanceof Product) {
            MetricKroneckerContainerImpl tempContainer = new MetricKroneckerContainerImpl(container);
            List<Tensor> nonMetrics = new ArrayList<>();
            Tensor current;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                current = tensor.get(i);
                if (Tensors.isKroneckerOrMetric(current))
                    tempContainer.add(new MetricKroneckerWrapper(current));
                else
                    nonMetrics.add(current);
            }
            ProductBuilder builder = new ProductBuilder();
            for (Tensor nonMetric : nonMetrics)
                builder.put(transform(nonMetric, tempContainer));
            for (MetricKroneckerWrapper mk : tempContainer.container)
                builder.put(mk.tensorMK);
            return builder.build();
        }
        if (tensor instanceof Sum) {
            List<MetricKroneckerContainer> containers = new ArrayList<>();
            TensorBuilder builder = SumBuilderFactory.defaultSumBuilder();
            for (int i = tensor.size() - 1; i >= 0; --i)
                if (i == 0) {
                    builder.put(transform(tensor.get(i), container));
                    containers.add(container);
                } else
                    builder.put(transform(tensor.get(i), container.clone()));
//            //FIXME temporary check
//            for (int i = 1; i < containers.size(); ++i)
//                if (!containers.get(0).equals(containers.get(i)))
//                    throw new RuntimeException();
            return builder.build();
        }
        return tensor;
    }
}
