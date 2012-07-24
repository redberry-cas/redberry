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

import cc.redberry.core.context.*;
import cc.redberry.core.indexmapping.IndexMappingBuffer;
import cc.redberry.core.indexmapping.IndexMappingBufferTester;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.*;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class SumBuilder implements TensorBuilder {

    private final Map<Integer, List<FactorNode>> summands;
    private Complex complex = Complex.ZERO;
    private Indices indices = null;
    private int[] sortedFreeIndices;

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
                Tensor summand = Tensors.multiply(node.builder.build(), node.factor);
                if (!TensorUtils.isZero(summand))
                    sum.add(summand);
            }

        if (sum.isEmpty())
            return complex;
        if (sum.size() == 1)
            return sum.get(0);

        return new Sum(sum.toArray(new Tensor[sum.size()]), indices);
    }

    @Override
    public void put(Tensor tensor) {
        if (TensorUtils.isZero(tensor))
            return;
        if (indices == null) {
            indices = IndicesFactory.createSorted(tensor.getIndices().getFreeIndices());
            sortedFreeIndices = indices.getAllIndices().copy();
            Arrays.sort(sortedFreeIndices);
        } else if (!indices.equalsRegardlessOrder(tensor.getIndices().getFreeIndices()))
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

        Split split = Split.split(tensor, false);

        Integer hash = TensorHashCalculator.hashWithIndices(split.factor, sortedFreeIndices);//=split.factor.hashCode();
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
            
            if (factorNodes.size() > 1) {
                System.out.println(factorNodes.size());
                for (FactorNode node : factorNodes)
                    System.out.println(node.factor);
            }
        }
    }

//    static Boolean compareFactors(final Tensor u, final Tensor v) {
//        Callable<Boolean> callable = new Callable<Boolean>() {
//
//            @Override
//            public Boolean call() throws Exception {
//                return compareFactors1(u, v);
//            }
//        };
//        RunnableFuture future = new FutureTask(callable);
//        ExecutorService service = Executors.newSingleThreadExecutor();
//        service.execute(future);
//        Boolean b = null;
//        try {
//            b = (Boolean) future.get(10, TimeUnit.SECONDS);
//        } catch (TimeoutException exception) {
//            System.out.println(u);
//            System.out.println(v);
//            throw new RuntimeException();
//        } catch (InterruptedException | ExecutionException exception) {
//            System.out.println("ПАЩЁЛ НА ХУЙ");
//        }
//        return b;
//    }
    static Boolean compareFactors(Tensor u, Tensor v) {
        IndexMappingBuffer buffer;
        if (u.getIndices().size() == 0)
            buffer = IndexMappings.createPort(u, v).take();
        else {
            int[] fromIndices = u.getIndices().getFreeIndices().getAllIndices().copy();
            for (int i = 0; i < fromIndices.length; ++i)
                fromIndices[i] = IndicesUtils.getNameWithType(fromIndices[i]);
            long start = System.currentTimeMillis();
            buffer = IndexMappings.createPort(new IndexMappingBufferTester(fromIndices, false), u, v).take();
            long stop = System.currentTimeMillis();
            if (stop - start > 3000) {
                System.out.println("time " + (stop - start));

                System.out.println(u);
                System.out.println(v);
            }
        }
        if (buffer == null)
            return null;
        assert buffer.isEmpty();
        return buffer.getSignum();
    }
}
