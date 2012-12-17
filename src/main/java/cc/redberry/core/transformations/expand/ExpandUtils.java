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
 * the Free Software Foundation, either version 2 of the License, or
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
package cc.redberry.core.transformations.expand;

import cc.redberry.concurrent.OutputPort;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ExpandUtils {

    /**
     * This is a safe concurrent port, which expands out product of two sums.
     */
    public static final class ExpandPairPort implements OutputPort<Tensor> {

        private final Tensor sum1, sum2;
        private final Tensor[] factors;
        private final AtomicLong atomicLong = new AtomicLong();

        /**
         * Creates port from two sums.
         *
         * @param s1 first sum
         * @param s2 second sum
         */
        public ExpandPairPort(Sum s1, Sum s2) {
            sum1 = s1;
            sum2 = s2;
            this.factors = new Tensor[0];
        }

        /**
         * Creates port from two sums.
         *
         * @param s1      first sum
         * @param s2      second sum
         * @param factors if specified, then each resulting term will be multiplied on this factors
         */
        public ExpandPairPort(Sum s1, Sum s2, Tensor[] factors) {
            this.sum1 = s1;
            this.sum2 = s2;
            this.factors = factors;
        }

        /**
         * Consequently returns terms of the resulting expanded expression,
         * calculating the next term on invocation.
         *
         * @return next term in the resulting expanded expression
         */
        @Override
        public Tensor take() {
            long index = atomicLong.getAndIncrement();
            if (index >= sum1.size() * sum2.size())
                return null;
            int i1 = (int) (index / sum2.size());
            int i2 = (int) (index % sum2.size());
            if (factors.length == 0)
                return Tensors.multiply(sum1.get(i1), sum2.get(i2));
            else
                return Tensors.multiply(ArraysUtils.addAll(factors, sum1.get(i1), sum2.get(i2)));
        }
    }

    /**
     * Expands out the product of two sums.
     *
     * @param s1              first sum
     * @param s2              second sum
     * @param transformations additional transformations to be
     *                        consequently applied on each term
     *                        in the resulting expression.
     * @param factors         if specified, then each resulting term will be multiplied on this factor
     * @return the resulting expanded tensor
     */
    public static Tensor expandPairOfSums(Sum s1, Sum s2, Tensor[] factors, Transformation[] transformations) {
        ExpandPairPort epp = new ExpandPairPort(s1, s2, factors);
        TensorBuilder sum = new SumBuilder();
        Tensor t;
        while ((t = epp.take()) != null)
            sum.put(apply(transformations, t));
        return sum.build();
    }

    /**
     * Expands out the product of two sums.
     *
     * @param s1              first sum
     * @param s2              second sum
     * @param transformations additional transformations to be
     *                        consequently applied on each term
     *                        in the resulting expression.
     * @return the resulting expanded tensor
     */
    public static Tensor expandPairOfSums(Sum s1, Sum s2, Transformation[] transformations) {
        return expandPairOfSums(s1, s2, new Tensor[0], transformations);
    }

    public static Tensor expandProductOfSums(Product product, Transformation[] transformations) {
        Tensor indexless = product.getIndexlessSubProduct(),
                data = product.getDataSubProduct();
        boolean expandIndexless = false, expandData = false, containsIndexlessSumNeededExpand = false;
        if (indexless instanceof Sum && sumContainsIndexed(indexless)) {
            //data is not 1 at this point
            containsIndexlessSumNeededExpand = true;
            expandIndexless = true;
            expandData = true;
        }
        if (indexless instanceof Product)
            for (Tensor t : indexless)
                if (t instanceof Sum)
                    if (ExpandUtils.sumContainsIndexed(t)) {
                        //even if data is 1 it will be recreated
                        containsIndexlessSumNeededExpand = true;
                        expandData = true;
                        expandIndexless = true;
                        break;
                    } else
                        expandIndexless = true;
        if (!expandData) {
            if (data instanceof Sum)
                expandData = true;
            if (data instanceof Product)
                for (Tensor t : data)
                    if (t instanceof Sum) {
                        expandData = true;
                        break;
                    }
        }

        if (!expandData && !expandIndexless)
            return product;

        if (!expandData)
            return Tensors.multiply(expandProductOfSums1(indexless, transformations, false), data);

        if (!expandIndexless) {
            Tensor newData = expandProductOfSums1(data, transformations, true);
            if (newData instanceof Sum)
                return FastTensors.multiplySumElementsOnScalarFactorAndExpandScalars((Sum) newData, indexless);
            else
                return expandIndexlessSubproduct.transform(Tensors.multiply(indexless, newData));
        }

        if (!containsIndexlessSumNeededExpand) {
            indexless = expandProductOfSums1(indexless, transformations, false);
            data = expandProductOfSums1(data, transformations, true);
        } else {
            List<Tensor> dataList;
            if (data instanceof Product)
                dataList = new ArrayList<>(Arrays.asList(data.toArray()));
            else {
                dataList = new ArrayList<>();
                dataList.add(data);
            }
            if (indexless instanceof Sum) {
                dataList.add(indexless);
                indexless = Complex.ONE;
                data = expandProductOfSums1(dataList, transformations, true);
            } else {
                assert indexless instanceof Product;
                List<Tensor> indexlessList = new ArrayList<>(indexless.size());
                expandIndexless = false;
                for (Tensor in : indexless)
                    if (sumContainsIndexed(in))
                        dataList.add(in);
                    else {
                        if (in instanceof Sum)
                            expandIndexless = true;
                        indexlessList.add(in);
                    }
                if (expandIndexless)
                    indexless = expandProductOfSums1(indexlessList, transformations, false);
                else
                    indexless = Tensors.multiply(indexlessList.toArray(new Tensor[indexlessList.size()]));
                data = expandProductOfSums1(dataList, transformations, true);
            }
        }

        if (data instanceof Sum)
            return FastTensors.multiplySumElementsOnScalarFactorAndExpandScalars((Sum) data, indexless);
        return Tensors.multiply(indexless, data);
    }

//    public static Tensor expandProductOfIndexedSums(Iterable<Tensor> tensor, Transformation[] transformations) {
//        return expandProductOfSums1(tensor,
//                                            ArraysUtils.addAll(new Transformation[]{expandIndexlessSubproduct}, transformations));
//    }
    public static Tensor apply(Transformation[] transformations, Tensor tensor) {
        for (Transformation tr : transformations)
            tensor = tr.transform(tensor);
        return tensor;
    }

    public static Tensor expandProductOfSums1(Iterable<Tensor> tensor, Transformation[] transformations, boolean indexed) {
        Transformation[] transformations1 =
                indexed
                ? ArraysUtils.addAll(new Transformation[]{expandIndexlessSubproduct}, transformations)
                : transformations;
        int capacity = 10;
        boolean isTensor;
        if (isTensor = (tensor instanceof Tensor)) {
            if (!(tensor instanceof Product))
                return (Tensor) tensor;
            capacity = ((Tensor) tensor).size();
        }

        List<Tensor> ns = new ArrayList<>(capacity); //non sums
        List<Sum> sums = new ArrayList<>(capacity);
        for (Tensor t : tensor)
            if (t instanceof Sum)
                sums.add((Sum) t);
            else
                ns.add(t);
        if (sums.isEmpty()) {
            if (isTensor)
                return (Tensor) tensor;
            return Tensors.multiply(ns.toArray(new Tensor[ns.size()]));
        }
        if (sums.size() == 1)
            if (indexed)
                return apply(transformations,
                             FastTensors.multiplySumElementsOnFactorAndExpand(sums.get(0), Tensors.multiply(ns.toArray(new Tensor[ns.size()]))));
            else
                return apply(transformations,
                             FastTensors.multiplySumElementsOnFactor(sums.get(0), Tensors.multiply(ns.toArray(new Tensor[ns.size()]))));

        Tensor base = sums.get(0);
        for (int i = 1, size = sums.size();; ++i)
            if (i == size - 1) {
                if (base == null)
                    if (indexed)
                        return apply(transformations,
                                     FastTensors.multiplySumElementsOnFactorAndExpand(sums.get(i), Tensors.multiply(ns.toArray(new Tensor[ns.size()]))));
                    else
                        return apply(transformations,
                                     FastTensors.multiplySumElementsOnFactor(sums.get(i), Tensors.multiply(ns.toArray(new Tensor[ns.size()]))));

                return expandPairOfSums((Sum) base, sums.get(i), ns.toArray(new Tensor[ns.size()]), transformations1);
            } else {
                if (base == null) {
                    base = sums.get(i);
                    continue;
                }

                base = expandPairOfSums((Sum) base, sums.get(i), transformations1);
                if (!(base instanceof Sum)) {
                    ns.add(base);
                    base = null;
                }
            }
    }

    public static boolean isExpandablePower(Tensor t) {
        return t instanceof Power && t.get(0) instanceof Sum && TensorUtils.isInteger(t.get(1));
    }

    public static boolean sumContainsIndexed(Tensor t) {
        if (!(t instanceof Sum))
            return false;
        for (Tensor s : t)
            if (s.getIndices().size() != 0)
                return true;
        return false;
    }

    public static Tensor expandSymbolicPower(Sum argument, int power, Transformation[] transformations) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
        for (i = power - 1; i >= 1; --i)
            temp = expandPairOfSums((Sum) temp,
                                    argument, transformations);
        return temp;
    }

    public static Tensor expandPower(Sum argument, int power, int[] forbiddenIndices, Transformation[] transformations) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
        TIntHashSet forbidden = new TIntHashSet(forbiddenIndices);
        TIntHashSet argIndices = TensorUtils.getAllIndicesNamesT(argument);
        forbidden.ensureCapacity(argIndices.size() * power);
        forbidden.addAll(argIndices);
        for (i = power - 1; i >= 1; --i)
            temp = expandPairOfSums((Sum) temp,
                                    (Sum) ApplyIndexMapping.renameDummy(argument, forbidden.toArray(), forbidden),
                                    transformations);

        return temp;
    }
    public static final Transformation expandIndexlessSubproduct = new Transformation() {

        @Override
        public Tensor transform(Tensor t) {
            if (!(t instanceof Product))
                return t;
            Product p = (Product) t;
            Tensor indexless = p.getIndexlessSubProduct();
            
            boolean needExpand = false;
            if (indexless instanceof Product)
                for (Tensor i : indexless)
                    if (i instanceof Sum) {
                        needExpand = true;
                        break;
                    }
            if (needExpand)
                return Tensors.multiply(expandProductOfSums1((Product) indexless, new Transformation[0], false), p.getDataSubProduct());
            return t;
        }
    };
}
