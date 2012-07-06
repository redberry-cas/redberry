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

import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.number.Complex;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class Split {

    final Tensor factor;
    final Tensor summand;

    public Split(Tensor factor, Tensor summand) {
        this.factor = factor;
        this.summand = summand;
    }

    abstract TensorBuilder getBuilder();

    static Split split(final Tensor tensor, final boolean concurrent) {
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
            return new SplitNumbers(factor, complex, concurrent);
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
            return new SplitIndexless(factor, summand, concurrent);
        }
    }

    private static class SplitNumbers extends Split {

        private final boolean concurrent;

        public SplitNumbers(Tensor factor, Tensor summand, boolean concurrent) {
            super(factor, summand);
            this.concurrent = concurrent;
        }

        @Override
        TensorBuilder getBuilder() {
            TensorBuilder builder;
            if (concurrent)
                builder = new ComplexSumBuilderConcurrent();
            else
                builder = new ComplexSumBuilder();
            builder.put(summand);
            return builder;
        }
    }

    private static final class SplitIndexless extends Split {

        private final boolean concurrent;

        public SplitIndexless(Tensor factor, Tensor summand, boolean concurrent) {
            super(factor, summand);
            this.concurrent = concurrent;
        }

        @Override
        TensorBuilder getBuilder() {
            TensorBuilder builder;
            if (concurrent)
                builder = new SumBuilderConcurrent();
            else
                builder = new SumBuilder();
            builder.put(summand);
            return builder;
        }
    }

    private static final class ComplexSumBuilder implements TensorBuilder {

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

    private static final class ComplexSumBuilderConcurrent implements TensorBuilder {

        AtomicReference< Complex> atomicComplex = new AtomicReference<>(Complex.ZERO);

        public ComplexSumBuilderConcurrent() {
        }

        @Override
        public Tensor build() {
            return atomicComplex.get();
        }

        @Override
        public void put(Tensor tensor) {
            Complex oldVal, newVal, toAdd = (Complex) tensor;
            do {
                oldVal = atomicComplex.get();
                newVal = oldVal.add(toAdd);
            } while (!atomicComplex.compareAndSet(oldVal, newVal));

        }
    }
}