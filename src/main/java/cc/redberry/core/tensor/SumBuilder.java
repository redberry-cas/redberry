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

import cc.redberry.core.context.CC;
import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappingBufferTester;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SumBuilder implements TensorBuilder {

    private final Map<Integer, List<FactorNode>> summands;
    private Complex complex = Complex.ZERO;
    private Indices indices = null;

    public SumBuilder() {
        this(7);
    }

    public SumBuilder(int initialCapacity) {
        summands = new HashMap<>(initialCapacity);
    }

    @Override
    public Tensor build() {
        if (complex.isNaN() || complex.isInfinite())
            return complex;

        List<Tensor> sum = new ArrayList<>();
        if (!complex.isZero())
            sum.add(complex);

        for (Map.Entry<Integer, List<FactorNode>> entry : summands.entrySet())
            for (FactorNode node : entry.getValue()) {
                Tensor summand = multiply(node.builder.build(), node.factor);//for performance
                if (!TensorUtils.isZero(summand))
                    sum.add(summand);
            }

        if (sum.isEmpty())
            return complex;
        if (sum.size() == 1)
            return sum.get(0);

        return new Sum(sum.toArray(new Tensor[sum.size()]), indices);
    }

    //TODO check performance
    static Tensor multiply(Tensor summand, Tensor factor) {
        if (TensorUtils.isZero(summand))
            return Complex.ZERO;
        else if (TensorUtils.isOne(summand))
            return factor;
        else if (factor.getIndices().size() == 0)
            if (factor instanceof Product) {
                Product p = (Product) factor;
                return new Product(checkOneOrMinuseOne((Complex) summand),
                                   p.indexlessData, p.data, ProductContent.EMPTY_INSTANCE, p.indices);
            } else
                return new Product(checkOneOrMinuseOne((Complex) summand),
                                   new Tensor[]{factor}, new Tensor[0], null, IndicesFactory.EMPTY_INDICES);
        else if (factor instanceof Product) {
            Product p = (Product) factor;
            if (summand instanceof Product) {
                Product s = (Product) summand;
                return new Product(s.factor, s.indexlessData, p.data, p.contentReference.get(), p.indices);
            } else if (summand instanceof Complex)
                return new Product(checkOneOrMinuseOne((Complex) summand),
                                   new Tensor[0], p.data, p.contentReference.get(), p.indices);
            else
                return new Product(Complex.ONE, new Tensor[]{summand}, p.data, p.contentReference.get(), p.indices);
        } else if (summand instanceof Product) {
            Product s = (Product) summand;
            return new Product(s.factor, s.indexlessData, new Tensor[]{factor}, null, factor.getIndices());
        } else if (summand instanceof Complex)
            return new Product(checkOneOrMinuseOne((Complex) summand),
                               new Tensor[0], new Tensor[]{factor}, null, factor.getIndices());
        else
            return new Product(Complex.ONE, new Tensor[]{summand}, new Tensor[]{factor}, null, factor.getIndices());
    }

    static Complex checkOneOrMinuseOne(Complex c) {
        if (c.isOne())
            return Complex.ONE;
        if (c.isMinusOne())
            return Complex.MINUSE_ONE;
        return c;
    }

    @Override
    public void put(Tensor tensor) {
        if (TensorUtils.isZero(tensor))
            return;
        if (indices == null)
            indices = IndicesFactory.createSorted(tensor.getIndices().getFreeIndices());
        else if (!indices.equalsRegardlessOrder(tensor.getIndices().getFreeIndices()))
            throw new TensorException("Inconsinstent indices in sum.", tensor);
        if (tensor instanceof Sum) {
            for (Tensor s : tensor)
                put(s);
            return;
        }
        if (tensor instanceof Complex) {
            complex = complex.add((Complex) tensor);
            return;
        }

        Split split = split(tensor);

        Integer hash = split.factor.hashCode();
        List<FactorNode> factorNodes = summands.get(hash);
        if (factorNodes == null) {
            List<FactorNode> fns = new ArrayList<>();
            fns.add(new FactorNode(split.factor, split.getBuilder()));
            summands.put(hash, fns);
        } else {
            Boolean b = null;
            for (FactorNode node : factorNodes)
                if ((b = compareFactors(split.factor, node.factor)) != null) {
                    if (b)
                        node.builder.put(Tensors.negate(split.summand));
                    else
                        node.builder.put(split.summand);
                    break;
                }
            if (b == null)
                factorNodes.add(new FactorNode(split.factor, split.getBuilder()));
        }
    }

     static Boolean compareFactors(Tensor u, Tensor v) {
        IndexMappingBuffer buffer;
        if (u.getIndices().size() == 0)
            buffer = IndexMappings.createPort(u, v).take();
        else {
            int[] fromIndices = u.getIndices().getFreeIndices().getAllIndices().copy();
            for (int i = 0; i < fromIndices.length; ++i)
                fromIndices[i] = IndicesUtils.getNameWithType(fromIndices[i]);
            buffer = IndexMappings.createPort(new IndexMappingBufferTester(fromIndices, false, CC.withMetric()), u, v).take();
        }
        if (buffer == null)
            return null;
        assert buffer.isEmpty();
        return buffer.getSignum();
    }

    static Split split(Tensor tensor) {
        if (tensor.getIndices().size() == 0) {//case 2*a*b*c
            Complex complex;
            Tensor factor;
            if (tensor instanceof Product) {
                Product product = (Product) tensor;
                complex = product.factor;
                if (complex == Complex.ONE)//case a*b
                    factor = tensor;
                else if (product.size() == 2)//case 2*a
                    factor = product.get(1);
                else//case 2*a*b => factor = a*b
                    factor = new Product(Complex.ONE, product.indexlessData, product.data, product.contentReference.get(), product.indices);
            } else {
                complex = Complex.ONE;
                factor = tensor;
            }
            return new SplitNumbers(factor, complex);
        } else {//case 2*a*g_mn*g_cd 
            Tensor summand;
            Tensor factor;
            if (tensor instanceof Product) {
                Product product = (Product) tensor;
                if (product.indexlessData.length == 0)
                    summand = product.factor;
                else if (product.factor == Complex.ONE && product.indexlessData.length == 1)
                    summand = product.indexlessData[0];
                else
                    summand = new Product(product.factor, product.indexlessData, new Tensor[0], ProductContent.EMPTY_INSTANCE, IndicesFactory.EMPTY_INDICES);

                if (product.data.length == 1)
                    factor = product.data[0];
                else
                    factor = new Product(Complex.ONE, new Tensor[0], product.data, product.contentReference.get(), product.indices);
            } else {
                summand = Complex.ONE;
                factor = tensor;
            }
            return new SplitIndexless(factor, summand);
        }
    }


    private static class SplitNumbers extends Split {

        public SplitNumbers(Tensor factor, Tensor summand) {
            super(factor, summand);
        }

        @Override
        TensorBuilder getBuilder() {
            TensorBuilder builder = new ComplexSumBuilder();
            builder.put(summand);
            return builder;
        }
    }

    private static class ComplexSumBuilder implements TensorBuilder {

        Complex complex = Complex.ZERO;

        public ComplexSumBuilder() {
        }

        @Override
        public Tensor build() {
            return complex;
        }

        @Override
        public void put(Tensor tensor) {
            complex = complex.add((Complex) tensor);
        }
    }

    private static class SplitIndexless extends Split {

        public SplitIndexless(Tensor factor, Tensor summand) {
            super(factor, summand);
        }

        @Override
        TensorBuilder getBuilder() {
            TensorBuilder builder = new SumBuilder();
            builder.put(summand);
            return builder;
        }
    }
}
