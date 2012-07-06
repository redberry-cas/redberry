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
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductBuilder implements TensorBuilder {

    private Complex complex = Complex.ONE;
    private final List<Tensor> elements;
    private final List<Tensor> indexlessElements;
    //Only SimpleTensor and Complex can be putted in this map
    //Both SimpleTensor and Complex have hashCode() and equals()
    private final Map<Tensor, SumBuilder> powers = new HashMap<>();

    public ProductBuilder(int initialCapacityIndexless, int initialCapacityData) {
        elements = new ArrayList<>(initialCapacityData);
        indexlessElements = new ArrayList<>(initialCapacityIndexless);
    }

    public ProductBuilder() {
        this(4, 3);
    }

    @Override
    public Tensor build() {
        if (complex.isZero() || complex.isInfinite() || complex.isNaN())
            return complex;

        ArrayList<Tensor> indexlessData = new ArrayList<>(powers.size() + indexlessElements.size());

        Complex complex = this.complex;
        for (Map.Entry<Tensor, SumBuilderConcurrent> entry : powers.entrySet()) {
            Tensor t = Tensors.pow(entry.getKey(), entry.getValue().build());

            assert !(t instanceof Product);

            if (t instanceof Complex)
                complex = complex.multiply((Complex) t);
            else
                indexlessData.add(t);

        }

        //may be redundant...
        if (complex.isZero() || complex.isInfinite() || complex.isNaN())
            return complex;

        indexlessData.addAll(indexlessElements);

        //Only complex factor
        if (indexlessData.isEmpty() && elements.isEmpty())
            return complex;

        // 1 * (something)
        if (complex.isOne()) {
            if (indexlessData.size() == 1 && elements.isEmpty())
                return indexlessData.get(0);
            if (indexlessData.isEmpty() && elements.size() == 1)
                return elements.get(0);
        }

        //Calculating product indices
        IndicesBuilder ibs = new IndicesBuilder();
        Indices indices;
        for (Tensor t : elements)
            ibs.append(t);
        try {
            indices = ibs.getIndices();
        } catch (InconsistentIndicesException exception) {
            throw new InconsistentIndicesException(exception.getIndex());
        }

        return new Product(indices, complex,
                           indexlessData.toArray(new Tensor[indexlessData.size()]),
                           elements.toArray(new Tensor[elements.size()]));
    }

    @Override
    public void put(Tensor tensor) {
        //FUTURE calculate indices
        if (tensor instanceof Product) {
            for (Tensor t : tensor)
                put(t);
            return;
        }
        if (tensor instanceof Complex) {
            complex = complex.multiply((Complex) tensor);
            return;
        }
        if (complex.isZero())
            return;

        if (TensorUtils.isSymbol(tensor)) {
            SumBuilderConcurrent sb = powers.get(tensor);
            if (sb == null) {
                sb = new SumBuilderConcurrent();
                powers.put(tensor, sb);
            }
            sb.put(Complex.ONE);
            return;
        }
        if (tensor instanceof Power) {
            Tensor argument = tensor.get(0);
            if (TensorUtils.isSymbolOrNumber(argument)) {
                SumBuilderConcurrent sb = powers.get(argument);
                if (sb == null) {
                    sb = new SumBuilderConcurrent();
                    powers.put(argument, sb);
                }
                sb.put(tensor.get(1));
                return;
            }
        }
        if (tensor.getIndices().size() == 0)
            indexlessElements.add(tensor);
        else
            elements.add(tensor);
    }
}
