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
import java.util.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductFactory implements TensorFactory {

    public static final ProductFactory FACTORY = new ProductFactory();

    private ProductFactory() {
    }

    @Override
    public Tensor create(final Tensor... tensors) {
        if (tensors.length == 0)
            return Complex.ONE;
        else if (tensors.length == 1)
            return tensors[0];

        Complex complex = Complex.ONE;

        IndexlessWrapper indexlessContainer = new IndexlessWrapper();
        DataWrapper dataContainer = new DataWrapper();
        int i;
        Tensor current;
        Product p;
        for (i = tensors.length - 1; i >= 0; --i) {
            current = tensors[i];
            if (current instanceof Complex)
                complex = complex.multiply((Complex) current);
            else if (current instanceof Product) {
                p = (Product) tensors[i];
                indexlessContainer.add(p.indexlessData);
                dataContainer.add(p.data, p.contentReference.get(), p.indices);
                complex = complex.multiply(p.factor);
            } else if (current.getIndices().size() == 0)
                indexlessContainer.add(current);
            else
                dataContainer.add(current);
            if (complex.isNaN())
                return complex;
        }

        if (complex.isZero() || complex.isNaN() || complex.isInfinite())
            return complex;

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
            Map<Tensor, TensorBuilder> powers = new HashMap<>(indexlessContainer.list.size());
            List<Tensor> indexlessArray = new ArrayList<>();
            Tensor tensor;
            for (i = indexlessContainer.list.size() - 1; i >= 0; --i) {
                tensor = indexlessContainer.list.get(i);
                if (TensorUtils.isSymbol(tensor)) {
                    TensorBuilder sb = powers.get(tensor);
                    if (sb == null) {
                        sb = new SumBuilder();
                        powers.put(tensor, sb);
                    }
                    sb.put(Complex.ONE);
                } else if (tensor instanceof Power) {
                    Tensor argument = tensor.get(0);
                    if (TensorUtils.isSymbolOrNumber(argument)) {
                        TensorBuilder sb = powers.get(argument);
                        if (sb == null) {
                            sb = new SumBuilder();
                            powers.put(argument, sb);
                        }
                        sb.put(tensor.get(1));
                    } else
                        indexlessArray.add(tensor);
                } else
                    indexlessArray.add(tensor);
            }

            for (Map.Entry<Tensor, TensorBuilder> entry : powers.entrySet()) {
                Tensor t = Tensors.pow(entry.getKey(), entry.getValue().build());

                assert !(t instanceof Product);

                if (t instanceof Complex)
                    complex = complex.multiply((Complex) t);
                else
                    indexlessArray.add(t);

            }
            //complex may change
            if (complex.isZero() || complex.isNaN() || complex.isInfinite())
                return complex;

            indexless = indexlessArray.toArray(new Tensor[indexlessArray.size()]);
            Arrays.sort(indexless);
        }

        //Constructing result
        if (data.length == 0 && indexless.length == 0)
            return complex;
        if (complex.isOne()) {
            if (data.length == 1 && indexless.length == 0)
                return data[0];
            if (data.length == 0 && indexless.length == 1)
                return indexless[0];
        }
        return new Product(complex, indexless, data, content, indices);
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
