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
package cc.redberry.core.transformations;

import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ContractIndices implements Transformation {

    public static final ContractIndices CONTRACT_INDICES = new ContractIndices();

    private ContractIndices() {
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return transform(tensor, DummyMetricsChain.INSTANCE);
    }

    //TODO possibly refactor using iteration guide
    private Tensor transform(Tensor tensor, MetricsChain chain) {
        if (tensor instanceof SimpleTensor) {
            tensor = chain.apply((SimpleTensor) tensor);

            //this is only in case of tensor field
            TensorBuilder builder = tensor.getBuilder();
            int size = tensor.size();
            for (int i = 0; i < size; ++i)
                builder.put(transform(tensor.get(i)));
            return builder.build();
        } else if (tensor instanceof ScalarFunction) {
            TensorBuilder builder = tensor.getBuilder();
            for (int i = tensor.size() - 1; i >= 0; --i)
                builder.put(transform(tensor.get(i)));
            return builder.build();
        } else if (tensor instanceof Product) {
            Product p = (Product) tensor;

            ProductContent content = p.getContent();
            //no tensors with indices in product
            if (content.size() == 0)
                return tensor;

            MetricsChainImpl tempContainer = new MetricsChainImpl(chain);
            List<Tensor> nonMetrics = new ArrayList<>();
            Tensor current, temp;
            int i;
            boolean applied = false;
            for (i = content.size() - 1; i >= 0; --i) {
                current = content.get(i);
                if (Tensors.isKroneckerOrMetric(current))
                    applied = applied | (tempContainer.add(new MetricWrapper(current)));
                else
                    nonMetrics.add(current);
            }
            for (i = nonMetrics.size() - 1; i >= 0; --i) {
                temp = nonMetrics.get(i);
                current = transform(temp, tempContainer);
                if (current != temp) {
                    applied = true;
                    nonMetrics.set(i, current);
                }
            }
            if (!applied)
                return tensor;

            ProductBuilder builder = new ProductBuilder();
            builder.put(p.getIndexlessSubProduct());
            for (Tensor nonMetric : nonMetrics)
                builder.put(transform(nonMetric, tempContainer));
            for (MetricWrapper mk : tempContainer.container)
                builder.put(mk.tensorMK);
            return builder.build();
        } else if (tensor instanceof Sum) {

            Tensor[] data = new Tensor[tensor.size()];
            boolean applied = false;

            Tensor oldTensor, newTensor;
            for (int i = tensor.size() - 1; i >= 0; --i) {
                oldTensor = tensor.get(i);
                if (i == 0)
                    newTensor = transform(oldTensor, chain);
                else
                    newTensor = transform(oldTensor, chain.clone());
                data[i] = newTensor;
                if (oldTensor != newTensor)
                    applied = true;

            }
            if (!applied)
                return tensor;
            return Tensors.sum(data);
        } else
            return tensor;
    }

    private static interface MetricsChain {

        boolean mergeWith(MetricWrapper mk);

        boolean add(MetricWrapper mK);

        SimpleTensor apply(SimpleTensor t);

        MetricsChain clone();

        boolean equals(MetricsChain container);
    }

    private static final class MetricsChainImpl implements MetricsChain {

        final MetricsChain parent;
        List<MetricWrapper> container;

        MetricsChainImpl(MetricsChain parent) {
            container = new ArrayList<>();
            this.parent = parent;
        }

        private MetricsChainImpl(List<MetricWrapper> container, MetricsChain parent) {
            this.container = container;
            this.parent = parent;
        }

        @Override
        public boolean mergeWith(MetricWrapper mk) {
            boolean b = false;
            ListIterator<MetricWrapper> it = container.listIterator();
            while (it.hasNext()) {
                MetricWrapper _mk = it.next();
                if (mk.apply(_mk)) {
                    it.remove();
                    b = true;
                }
            }
            return parent.mergeWith(mk) || b;
        }

        @Override
        public boolean add(MetricWrapper mK) {
            //merging from this to mk
//        boolean b = mergeWith(mK);
//        container.add(mK);
//        return b;
            return mergeWith(mK) | !container.add(mK);
        }

        @Override
        public SimpleTensor apply(SimpleTensor t) {
            ListIterator<MetricWrapper> iterator = container.listIterator();
            MetricWrapper current;
            SimpleTensor newVal, oldVal = t;
            while (iterator.hasNext()) {
                current = iterator.next();
                if ((newVal = current.apply(oldVal)) != oldVal) {
                    iterator.remove();
                    oldVal = newVal;
                }
            }
            newVal = parent.apply(oldVal);
            return newVal;
        }

        @Override
        public MetricsChainImpl clone() {
            List<MetricWrapper> newList = new ArrayList<>();
            for (MetricWrapper mk : container)
                newList.add(mk.clone());
            return new MetricsChainImpl(newList, parent.clone());
        }

        @Override
        public boolean equals(MetricsChain gC) {
            if (gC instanceof DummyMetricsChain)
                return false;
            MetricsChainImpl gc = (MetricsChainImpl) gC;
            if (container.size() != gc.container.size())
                return false;
            Collections.sort(container);
            Collections.sort(gc.container);
            for (int i = 0; i < container.size(); ++i)
                if (!container.get(i).equals(gc.container.get(i)))
                    return false;
            return parent.equals(gc.parent);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (MetricWrapper mk : container)
                sb.append(mk.toString()).append(";");
            return sb.toString();
        }
    }

    private static final class DummyMetricsChain implements MetricsChain {

        static final DummyMetricsChain INSTANCE = new DummyMetricsChain();

        private DummyMetricsChain() {
        }

        @Override
        public boolean mergeWith(MetricWrapper mk) {
            return false;
        }

        @Override
        public boolean add(MetricWrapper mK) {
            throw new IllegalStateException();
        }

        @Override
        public SimpleTensor apply(SimpleTensor t) {
            return t;
        }

        @Override
        public MetricsChain clone() {
            return INSTANCE;
        }

        @Override
        public boolean equals(MetricsChain gc) {
            return gc instanceof DummyMetricsChain;
        }

        @Override
        public String toString() {
            return "RootMetricKroneckerContainer";
        }
    }

    private static final class MetricWrapper implements Comparable<MetricWrapper> {

        final int[] indices = new int[2];
        Tensor tensorMK;

        MetricWrapper(Tensor tensorMK) {
            this.indices[0] = tensorMK.getIndices().get(0);
            this.indices[1] = tensorMK.getIndices().get(1);
            Arrays.sort(this.indices);
            this.tensorMK = tensorMK;
        }

        private MetricWrapper(int index1, int index2, Tensor tensorMK) {
            indices[0] = index1;
            indices[1] = index2;
            this.tensorMK = tensorMK;
        }

        @Override
        public int compareTo(MetricWrapper o) {
            int res;
            if ((res = Integer.compare(indices[0], o.indices[0])) != 0)
                return res;
            return Integer.compare(indices[1], o.indices[1]);
        }

        SimpleTensor apply(SimpleTensor t) {
            IM im = new IM();
            SimpleIndices oldIndices = t.getIndices();
            OUTER:
            for (int i = 0; i < oldIndices.size(); ++i)
                for (int j = 0; j < 2; ++j)
                    if ((oldIndices.get(i) ^ indices[j])
                            == 0x80000000) {
                        im.add(oldIndices.get(i),
                               indices[1 - j]);
                        break OUTER;
                    }
            SimpleIndices newIndices = oldIndices.applyIndexMapping(im);
            if (oldIndices == newIndices)
                return t;
            if (Tensors.isKroneckerOrMetric(t))
                return Tensors.createMetricOrKronecker(newIndices.get(0), newIndices.get(1));
            return Tensors.simpleTensor(t.getName(), newIndices);
        }

        boolean apply(MetricWrapper mK) {
            for (int i = 0; i < 2; ++i)
                for (int j = 0; j < 2; ++j)
                    if ((indices[i] ^ mK.indices[j]) == 0x80000000) {
                        tensorMK = Tensors.createMetricOrKronecker(indices[1 - i], mK.indices[1 - j]);
                        indices[i] = mK.indices[1 - j];
                        Arrays.sort(this.indices);
                        return true;
                    }
            return false;
        }

        @Override
        public MetricWrapper clone() {
            Tensor t = Tensors.createMetricOrKronecker(indices[0], indices[1]);
            return new MetricWrapper(indices[0], indices[1], t);
        }

        @Override
        public String toString() {
            return tensorMK.toString();
        }

        private class IM implements IndexMapping {

            Map<Integer, Integer> map = new HashMap<>();

            IM() {
            }

            public void add(int from, int to) {
                map.put(from, to);
            }

            @Override
            public int map(int from) {
                Integer to = map.get(from);
                if (to != null)
                    return to.intValue();
                return from;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final MetricWrapper other = (MetricWrapper) obj;
            if (!Arrays.equals(this.indices, other.indices))
                return false;
            return true;
        }
    }
}
