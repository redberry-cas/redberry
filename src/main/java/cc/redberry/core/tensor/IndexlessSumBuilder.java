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

import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.MappingsPort;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
class IndexlessSumBuilder implements TensorBuilder {

    private final Map<Integer, List<FactorNode>> map = new HashMap<>();
    private Complex number = Complex.ZERO;

    IndexlessSumBuilder() {
    }

    @Override
    public Tensor build() {
        List<Tensor> sum = new ArrayList<>();
        if (!number.isZero())
            sum.add(number);

        for (Map.Entry<Integer, List<FactorNode>> entry : map.entrySet())
            for (FactorNode node : entry.getValue()) {
                Tensor summand = Tensors.multiply(node.complex, node.factor);
                if (!TensorUtils.isZero(summand))
                    sum.add(summand);//TODO improve, using nor Tensors.multiply, but special method
            }

        return new Sum(sum.toArray(new Tensor[sum.size()]), IndicesFactory.createSorted(new int[0]));
    }

    @Override
    public void put(Tensor tensor) {
        if (tensor instanceof Sum) {
            for (Tensor t : tensor)
                put(t);
            return;
        }
        if (tensor instanceof Complex) {
            number = number.add((Complex) tensor);
            return;
        }
        if (number.isNaN())//CHECKSTYLE
            return;

        Complex complex;
        Tensor factor;
        if (tensor instanceof Product) {
            Product product = (Product) tensor;
            complex = product.getFactor();
            if (complex == Complex.ONE)
                factor = tensor;
            else if (product.size() == 2)
                factor = product.get(1);
            else
                factor = new Product(Complex.ONE, product.indexlessData, product.data, product.contentReference.get(), product.indices);
        } else {
            complex = Complex.ONE;
            factor = tensor;
        }

        Integer hash = factor.hashCode();
        List<FactorNode> factorNodes = map.get(hash);
        if (factorNodes == null) {
            List<FactorNode> fns = new ArrayList<>();
            fns.add(new FactorNode(complex, factor));
            map.put(hash, fns);
        } else {
            Boolean b = null;
            for (FactorNode node : factorNodes)
                if ((b = compareFactors(factor, node.factor)) != null) {
                    if (b)
                        node.complex = node.complex.subtract(complex);
                    else
                        node.complex = node.complex.add(complex);
                    break;
                }
            if (b == null)
                factorNodes.add(new FactorNode(complex, factor));
        }
    }

    private Boolean compareFactors(Tensor u, Tensor v) {
        MappingsPort mp = IndexMappings.createPort(u, v);
        IndexMappingBuffer buffer = mp.take();
        if (buffer == null)
            return null;
        return buffer.getSignum();
    }

    private static class FactorNode {

        Complex complex;
        final Tensor factor;

        public FactorNode(Complex complex, Tensor factor) {
            this.complex = complex;
            this.factor = factor;
        }
    }
}
