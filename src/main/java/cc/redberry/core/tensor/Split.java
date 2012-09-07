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

import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public abstract class Split {

    public final Tensor factor;
    public final Tensor summand;

    public Split(Tensor factor, Tensor summand) {
        this.factor = factor;
        this.summand = summand;
    }

    public abstract TensorBuilder getBuilder();

    public static Split splitScalars(final Tensor tensor) {
        if (tensor.getIndices().getFree().size() == 0)//case 2*a*b*c            
            return new SplitNumbers(tensor, Complex.ONE);
        else {//case 2*a*g_mn*g_cd 
            Tensor summand;
            Tensor factor;
            if (tensor instanceof Product) {
                Product product = (Product) tensor;
                ProductContent content = product.getContent();
                factor = content.getNonScalar();
                Tensor[] scalars = content.getScalars();
                int dataLength = factor instanceof Product
                                 ? product.data.length - ((Product) factor).data.length
                                 : product.data.length == 0
                                   ? 0
                                   : (product.data.length - 1);
                if (factor == null)
                    factor = Complex.ONE;
                if (dataLength == 0)
                    if (product.indexlessData.length == 0)
                        summand = product.factor;
                    else if (product.indexlessData.length == 1 && product.factor == Complex.ONE)
                        summand = product.indexlessData[0];
                    else
                        summand = new Product(product.factor, product.indexlessData, new Tensor[0], ProductContent.EMPTY_INSTANCE, IndicesFactory.EMPTY_INDICES);
                else if (dataLength == 1 && product.indexlessData.length == 0 && product.factor == Complex.ONE)
                    summand = scalars[0];
                else {
                    Tensor[] data = new Tensor[dataLength];
                    IndicesBuilder ib = new IndicesBuilder();
                    dataLength = -1;
                    for (Tensor t : scalars)
                        if (t instanceof Product)
                            for (Tensor d : t) {
                                data[++dataLength] = d;
                                ib.append(d);
                            }
                        else {
                            data[++dataLength] = t;
                            ib.append(t);
                        }
                    assert dataLength == data.length - 1;
                    Arrays.sort(data);
                    summand = new Product(product.factor, product.indexlessData, data, null, ib.getIndices());
                }
            } else {
                summand = Complex.ONE;
                factor = tensor;
            }
            return new SplitIndexless(factor, summand);
        }
    }

    public static Split splitIndexless(final Tensor tensor) {
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

    @Override
    public String toString() {
        return summand + " * " + factor;
    }

    private static class SplitNumbers extends Split {

        public SplitNumbers(Tensor factor, Tensor summand) {
            super(factor, summand);
        }

        @Override
        public TensorBuilder getBuilder() {
            TensorBuilder builder = new ComplexSumBuilder();
            builder.put(summand);
            return builder;
        }
    }

    private static final class SplitIndexless extends Split {

        public SplitIndexless(Tensor factor, Tensor summand) {
            super(factor, summand);
        }

        @Override
        public TensorBuilder getBuilder() {
            TensorBuilder builder = new SumBuilder();
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

    @Deprecated
    private static final class ComplexSumBuilderConcurrent implements TensorBuilder {

        final AtomicReference< Complex> atomicComplex = new AtomicReference<>(Complex.ZERO);

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