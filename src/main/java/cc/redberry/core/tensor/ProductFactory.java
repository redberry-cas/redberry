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

import cc.redberry.core.indices.InconsistentIndicesException;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.number.NumberUtils.isZeroOrIndeterminate;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductFactory implements TensorFactory {

    public static final ProductFactory FACTORY = new ProductFactory();

    private ProductFactory() {
    }

    @Override
    public Tensor create(final Tensor... tensors) {
//        return new DefaultFactory(new ProductBuilder()).create(tensors);
        if (tensors.length == 0)
            return Complex.ONE;
        else if (tensors.length == 1)
            return tensors[0];

        Complex factor = Complex.ONE;

        IndexlessWrapper indexlessContainer = new IndexlessWrapper();
        DataWrapper dataContainer = new DataWrapper();
        int i;
        Tensor current;
        Product p;
        for (i = tensors.length - 1; i >= 0; --i) {
            current = tensors[i];
            if (current instanceof Complex)
                factor = factor.multiply((Complex) current);
            else if (current instanceof Product) {
                p = (Product) tensors[i];
                indexlessContainer.add(p.indexlessData);
                dataContainer.add(p.data, p.contentReference.getReferent(), p.indices);
                factor = factor.multiply(p.factor);
            } else if (current.getIndices().size() == 0)
                indexlessContainer.add(current);
            else
                dataContainer.add(current);
            if (factor.isNaN())
                return factor;
        }

        if (isZeroOrIndeterminate(factor))
            return factor;

        //Processing data with indices
        ProductContent content;
        Indices indices;
        Tensor[] data = dataContainer.list.toArray(new Tensor[dataContainer.list.size()]);
        if (dataContainer.count == 1) {
            content = dataContainer.content;
            indices = dataContainer.indices;
            if (indices == null) {
                assert dataContainer.list.size() == 1;
                indices = IndicesFactory.createSorted(dataContainer.list.get(0).getIndices());
            }
        } else {
            content = null;
            Arrays.sort(data);
            IndicesBuilder builder = new IndicesBuilder();
            for (i = dataContainer.list.size() - 1; i >= 0; --i)
                builder.append(dataContainer.list.get(i));
            try {
                indices = builder.getIndices();
            } catch (InconsistentIndicesException exception) {
                throw new InconsistentIndicesException(exception.getIndex());
            }
        }

        //Processing indexless data
        Tensor[] indexless;
        if (indexlessContainer.count == 0)
            indexless = new Tensor[0];
        else if (indexlessContainer.count == 1)
            indexless = indexlessContainer.list.toArray(new Tensor[indexlessContainer.list.size()]);
        else {
            PowersContainer powersContainer = new PowersContainer(indexlessContainer.list.size());
            List<Tensor> indexlessArray = new ArrayList<>();
            Tensor tensor;
            for (i = indexlessContainer.list.size() - 1; i >= 0; --i) {
                tensor = indexlessContainer.list.get(i);
                if (TensorUtils.isSymbolic(tensor)) {
                    powersContainer.putSymbolic(tensor);
                } else
                    indexlessArray.add(tensor);
            }

            for (Tensor t : powersContainer) {
                assert !(t instanceof Product);

                if (t instanceof Complex) {
                    factor = factor.multiply((Complex) t);
                    if (isZeroOrIndeterminate(factor))
                        return factor;
                } else
                    indexlessArray.add(t);
            }

            if (powersContainer.isSign())
                factor = factor.negate();

            indexless = indexlessArray.toArray(new Tensor[indexlessArray.size()]);
            Arrays.sort(indexless);
        }

        //Constructing result
        if (data.length == 0 && indexless.length == 0)
            return factor;
        if (factor.isOne()) {
            if (data.length == 1 && indexless.length == 0)
                return data[0];
            if (data.length == 0 && indexless.length == 1)
                return indexless[0];
        }
        if (factor.isMinusOne()) {
            Sum s = null;
            if (indexless.length == 1 && data.length == 0 && indexless[0] instanceof Sum)
                //case (-1)*(a+b) -> -a-b
                s = ((Sum) indexless[0]);
            if (indexless.length == 0 && data.length == 1 && data[0] instanceof Sum)
                //case (-1)*(a_i+b_i) -> -a_i-b_i
                s = ((Sum) data[0]);
            if (s != null) {
                Tensor sumData[] = s.data.clone();
                for (i = sumData.length - 1; i >= 0; --i)
                    sumData[i] = Tensors.negate(sumData[i]);
                return new Sum(s.indices, sumData, s.hashCode());
            }
        }
        return new Product(factor, indexless, data, content, indices);
    }

    private static class ListWrapper {

        final ArrayList<Tensor> list = new ArrayList<>();
        int count = 0;

        void add(Tensor t) {
            list.add(t);
            ++count;
        }
    }

    private static final class IndexlessWrapper extends ListWrapper {

        void add(Tensor[] t) {
            if (t.length != 0) {
                list.addAll(Arrays.asList(t));
                ++count;
            }
        }
    }

    private static final class DataWrapper extends ListWrapper {

        private ProductContent content;
        private Indices indices;

        void add(Tensor[] t, ProductContent content, Indices indices) {
            if (t.length != 0) {
                list.addAll(Arrays.asList(t));
                this.content = content;
                this.indices = indices;
                ++count;
            }
        }
    }
}
