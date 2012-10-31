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

import cc.redberry.concurrent.OutputPort;
import cc.redberry.concurrent.OutputPortUnsafe;
import cc.redberry.core.combinatorics.IntTuplesPort;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.tensor.iterator.TraverseGuide;
import cc.redberry.core.tensor.iterator.TraverseState;
import cc.redberry.core.tensor.iterator.TreeTraverseIterator;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.TIntSet;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Expand implements Transformation {
    public static final Expand ExpandAll = new Expand();

    private final Indicator<Tensor> indicator;

    public Expand(Indicator<Tensor> indicator) {
        this.indicator = indicator;
    }

    public Expand() {
        this.indicator = Indicator.TRUE_INDICATOR;
    }

    @Override
    public Tensor transform(Tensor tensor) {
        return expand(tensor, indicator, new Transformation[0]);
    }

    public static Tensor expand(Tensor tensor) {
        return expand(tensor, Indicator.TRUE_INDICATOR, new Transformation[0]);
    }

    public static Tensor expand(Tensor tensor, Transformation... transformations) {
        return expand(tensor, Indicator.TRUE_INDICATOR, transformations);
    }

    private static final Indicator<Tensor> productIndicator = Indicator.Utils.classIndicator(Product.class);

    public static Tensor expand(Tensor tensor, Indicator<Tensor> indicator, Transformation[] transformations) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor, TraverseGuide.EXCEPT_FUNCTIONS_AND_FIELDS);
        TraverseState state;
        Tensor current;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;
            current = iterator.current();
            if (current instanceof Product && indicator.is(current))
                iterator.set(expandProductOfSums(current, indicator, transformations));
            else if (isExpandablePower(current) && indicator.is(current) && !iterator.checkLevel(productIndicator, 1)) {
                Sum sum = (Sum) current.get(0);
                iterator.set(expandPower(sum,
                        ((Complex) current.get(1)).getReal().intValue(),
                        TensorUtils.getAllIndicesNamesT(sum).toArray(),
                        transformations));
            }
        }
        return iterator.result();
    }

    private static boolean isExpandablePower(Tensor t) {
        return t instanceof Power && t.get(0) instanceof Sum && TensorUtils.isNaturalNumber(t.get(1));
    }

    private static Tensor expandProductOfSums(Tensor current, Indicator<Tensor> indicator, Transformation[] transformations) {

        // g_mn  {_m->^a, _n->_b}  => g^a_b  <=> d^a_b |           Tensors.isMetric(g_ab )  d_a^b

        // a*b | a_m*b_v | (a+b*f) | (a_i+(c+2)*b_i) 

        //1: a*b | (a+b*f)  => result1 = a**2*b+b**2*a*f    Tensors.multiplyAndExpand(nonSum, Sum)
        //2: a_m*b_v | (a_i+(c+2)*b_i)   => result2 = a_m*b_v*a_i+(c+2)*a_m*b_v*b_i
        //3: result1 * result2                              Tensors.multiplyAndExpand(scalarSum, nonScalarSum)
        // (a_m^m**2*b+b**2*a*a_m^m) * (a_m^m*a_m*b_v*a_i+a_m^m**2*B_avi+(c+2)*a_m*b_v*b_i) => (.....)*a_m*b_v*a_i +(....)*B_avi + ( .....  )*a_m*b_v*b_i 

        ArrayList<Tensor> indexlessNonSums = new ArrayList<>();
        ArrayList<Tensor> nonSums = new ArrayList<>();

        Sum indexlessSum = null;
        Sum sum = null;

        int i;
        Tensor t, temp;
        TIntSet initialForbiddenIndices = null;
        boolean expand = false;
        for (i = current.size() - 1; i >= 0; --i) {
            t = current.get(i);
            if (isExpandablePower(t)) {
                if (initialForbiddenIndices == null)
                    initialForbiddenIndices = TensorUtils.getAllIndicesNamesT(current);
                t = expandPower((Sum) t.get(0),
                        ((Complex) t.get(1)).getReal().intValue(),
                        initialForbiddenIndices.toArray(),
                        transformations);
                initialForbiddenIndices.addAll(TensorUtils.getAllIndicesNamesT(t));
                expand = true;
            }
            if (t.getIndices().size() == 0)
                if (t instanceof Sum)
                    if (indexlessSum == null)
                        indexlessSum = (Sum) t;
                    else {
                        temp = expandPairOfSums((Sum) t, indexlessSum, transformations);
                        expand = true;
                        if (temp instanceof Sum)
                            indexlessSum = (Sum) temp;
                        else {
                            indexlessNonSums.add(temp);
                            indexlessSum = null;
                        }
                    }
                else
                    indexlessNonSums.add(t);
            else if (t instanceof Sum)
                if (sum == null)
                    sum = (Sum) t;
                else {
                    temp = expandPairOfSums((Sum) t, sum, transformations);
                    temp = expand(temp, indicator, transformations);
                    expand = true;
                    if (temp instanceof Sum)
                        sum = (Sum) temp;
                    else {
                        nonSums.add(temp);
                        sum = null;
                    }
                }
            else
                nonSums.add(t);
        }

        if (!expand && sum == null && (indexlessSum == null || indexlessNonSums.isEmpty()))
            return current;


        Tensor indexless = multiply(indexlessNonSums.toArray(new Tensor[indexlessNonSums.size()]));
        if (indexlessSum != null)
            indexless = multiplySumElementsOnFactor(indexlessSum, indexless);

        Tensor main = multiply(nonSums.toArray(new Tensor[nonSums.size()]));
        if (sum != null)
            main = multiplySumElementsOnFactor(sum, main);

        if (main instanceof Sum)
            main = multiplySumElementsOnFactorAndExpandScalars((Sum) main, indexless);
        else
            main = multiply(indexless, main);

        return main;
    }

    private static Tensor expandPower(Sum argument, int power, int[] initialForbidden, Transformation[] transformations) {
        //TODO improve algorithm using Newton formula!!!
        int i;
        Tensor temp = argument;
//        Set<Integer> initialDummy = TensorUtils.getAllIndicesNames(argument);
//        int[] initialForbidden = new int[initialDummy.size()];
//        i = -1;
//        for (Integer index : initialDummy)
//            initialForbidden[++i] = index;
        IndexMapper mapper = new IndexMapper(initialForbidden);//(a_m^m+b_m^m)^30  
        for (i = power - 1; i >= 1; --i) {
            temp = expandPairOfSums((Sum) temp, (Sum) renameDummy(argument, mapper), transformations);
            mapper.reset();
        }
        return temp;
    }

    private static Tensor renameDummy(Tensor tensor, IndexMapper mapper) {
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        SimpleIndices oldIndices, newIndices;
        SimpleTensor simpleTensor;
        while ((state = iterator.next()) != null) {
            if (state != TraverseState.Leaving)
                continue;

            if (!(iterator.current() instanceof SimpleTensor))
                continue;
            simpleTensor = (SimpleTensor) iterator.current();
            oldIndices = simpleTensor.getIndices();
            newIndices = oldIndices.applyIndexMapping(mapper);
            if (oldIndices != newIndices)
                if (simpleTensor instanceof TensorField)
                    iterator.set(Tensors.setIndicesToField((TensorField) simpleTensor, newIndices));
                else
                    iterator.set(Tensors.setIndicesToSimpleTensor(simpleTensor, newIndices));
        }
        return iterator.result();
    }

    private static final class IndexMapper implements IndexMapping {

        private final IndexGenerator generator;
        private Map<Integer, Integer> map;

        public IndexMapper(int[] initialUsed) {
            generator = new IndexGenerator(initialUsed);
            map = new HashMap<>(initialUsed.length);
        }

        @Override
        public int map(int from) {
            int fromName = IndicesUtils.getNameWithType(from);
            Integer to = map.get(fromName);
            if (to == null)
                map.put(fromName, to = generator.generate(IndicesUtils.getType(from)));
            return IndicesUtils.getRawStateInt(from) ^ to;
        }

        void reset() {
            map = new HashMap<>();
        }
    }

    public static final class ExpandPairPort implements OutputPort<Tensor> {

        private final Tensor sum1, sum2;
        private final AtomicLong atomicLong = new AtomicLong();

        public ExpandPairPort(Sum s1, Sum s2) {
            sum1 = s1;
            sum2 = s2;
        }

        @Override
        public Tensor take() {
            long index = atomicLong.getAndIncrement();
            if (index >= sum1.size() * sum2.size())
                return null;
            int i1 = (int) (index / sum2.size());
            int i2 = (int) (index % sum2.size());
            return Tensors.multiply(sum1.get(i1), sum2.get(i2));
        }
    }

    public static Tensor expandPairOfSums(Sum s1, Sum s2) {
        return expandPairOfSums(s1, s2, new Transformation[0]);
    }

    public static Tensor expandPairOfSums(Sum s1, Sum s2, Transformation[] transformations) {
        ExpandPairPort epp = new ExpandPairPort(s1, s2);
        TensorBuilder sum = new SumBuilder();
        Tensor t;
        while ((t = epp.take()) != null) {
            for (Transformation transformation : transformations)
                t = transformation.transform(t);
            sum.put(t);
        }
        return sum.build();
    }

    //TODO review and add Indicator
    private static boolean canBeExpanded(Tensor tensor) {
        TensorLastIterator iterator = new TensorLastIterator(tensor);
        Tensor c;
        int sumsCount = 0;
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Product))
                continue;
            for (Tensor m : c) {
                if (m instanceof Sum) {
                    ++sumsCount;
                    if (!TensorUtils.isIndexless(m))
                        return true;
                }
                if (sumsCount >= 2)
                    return true;
            }
        }
        return false;
    }

    /*
     * Ports infrastructure.
     */
    public static Tensor expandUsingPort(Tensor t) {
        return expandUsingPort(t, new Transformation[0]);
    }

    public static Tensor expandUsingPort(Tensor t, Transformation[] transformations) {
        if (t instanceof Expression)
            return Tensors.expression(expandUsingPort(t.get(0)), expandUsingPort(t.get(1)));
        SumBuilder sb = new SumBuilder();
        OutputPortUnsafe<Tensor> opu = Expand.createPort(t);
        Tensor c;
        while ((c = opu.take()) != null) {
            if (transformations.length != 0) {
                for (Transformation tr : transformations)
                    c = tr.transform(c);
                while (canBeExpanded(c))
                    c = expandUsingPort(c, transformations);
            }
            sb.put(c);
        }
        return sb.build();
    }

    public static OutputPortUnsafe<Tensor> createPort(Tensor tensor) {
        if (tensor instanceof Product)
            return new ProductPort(tensor);
        if (tensor instanceof Sum)
            return new SumPort(tensor);
        if (isExpandablePower(tensor))
            return new PowerPort(tensor);
        else
            return new OutputPortUnsafe.Singleton<>(tensor);
    }

    private static interface ResetablePort extends OutputPortUnsafe<Tensor> {

        void reset();
    }

    private static final class PowerPort implements ResetablePort {

        private final Tensor base;
        private final int power;
        private IntTuplesPort tuplesPort;
        private final int[] initialForbidden;
        private OutputPortUnsafe<Tensor> currentPort;

        public PowerPort(Tensor tensor, int[] initialForbidden) {
            base = tensor.get(0);
            power = ((Complex) tensor.get(1)).getReal().intValue();
            int[] upperBounds = new int[power];
            Arrays.fill(upperBounds, base.size());
            tuplesPort = new IntTuplesPort(upperBounds);
            this.initialForbidden = initialForbidden;
            currentPort = nextPort();
        }

        public PowerPort(Tensor tensor) {
            this(tensor, TensorUtils.getAllIndicesNamesT(tensor.get(0)).toArray());
        }

        OutputPortUnsafe<Tensor> nextPort() {
            final int[] tuple = tuplesPort.take();
            if (tuple == null)
                return null;
            IndexMapper mapper = new IndexMapper(initialForbidden);//(a_m^m+b_m^m)^30  
            ProductBuilder builder = new ProductBuilder();
            builder.put(base.get(tuple[0]));
            for (int i = 1; i < tuple.length; ++i) {
                builder.put(renameDummy(base.get(tuple[i]), mapper));
                mapper.reset();
            }
            return createPort(builder.build());
        }

        @Override
        public Tensor take() {
            if (currentPort == null)
                return null;
            Tensor t = currentPort.take();
            if (t == null) {
                currentPort = nextPort();
                return take();
            }
            return t;
        }

        @Override
        public void reset() {
            tuplesPort.reset();
            currentPort = nextPort();
        }
    }

    private static final class ProductPort implements OutputPortUnsafe<Tensor> {

        private final ProductBuilder base;
        private ProductBuilder currentBuilder;
        private final ResetablePort[] sumsAndPowers;
        private final Tensor[] currentMultipliers;
        private final Tensor tensor;

        public ProductPort(Tensor tensor) {
            this.tensor = tensor;
            this.base = new ProductBuilder();
            List<ResetablePort> sumOrPowerPorts = new ArrayList<>();
            int theLargestSumPosition = 0, theLargestSumSize = 0, productSize = tensor.size();
            Tensor m;
            for (int i = 0; i < productSize; ++i) {
                m = tensor.get(i);
                if (m instanceof Sum) {
                    if (m.size() > theLargestSumSize) {
                        theLargestSumPosition = sumOrPowerPorts.size();
                        theLargestSumSize = m.size();
                    }
                    sumOrPowerPorts.add(new SumPort(m));
                } else if (isExpandablePower(m)) {
                    if (BigInteger.valueOf(m.get(0).size()).pow(((Complex) m.get(1)).getReal().intValue()).compareTo(BigInteger.valueOf(theLargestSumSize)) > 0) {
                        theLargestSumPosition = sumOrPowerPorts.size();
                        theLargestSumSize = m.size();
                    }
                    sumOrPowerPorts.add(new PowerPort(m, TensorUtils.getAllIndicesNamesT(tensor).toArray()));
                } else
                    base.put(m);
            }
            sumsAndPowers = sumOrPowerPorts.toArray(new ResetablePort[sumOrPowerPorts.size()]);

            if (sumsAndPowers.length <= 1) {
                currentMultipliers = new Tensor[0];
                currentBuilder = base;
            } else {
                ResetablePort temp = sumsAndPowers[theLargestSumPosition];
                sumsAndPowers[theLargestSumPosition] = sumsAndPowers[sumsAndPowers.length - 1];
                sumsAndPowers[sumsAndPowers.length - 1] = temp;
                currentMultipliers = new Tensor[sumsAndPowers.length - 2];
                for (productSize = 0; productSize < sumsAndPowers.length - 2; ++productSize)
                    currentMultipliers[productSize] = sumsAndPowers[productSize].take();
                currentBuilder = nextCombination();
            }
        }

        private ProductBuilder nextCombination() {
            if (sumsAndPowers.length == 1)
                return null;
            int pointer = sumsAndPowers.length - 2;
            ProductBuilder temp = base.clone();
            boolean next = false;
            Tensor c;
            c = sumsAndPowers[pointer].take();
            if (c == null) {
                sumsAndPowers[pointer].reset();
                c = sumsAndPowers[pointer].take();
                next = true;
            }
            temp.put(c);
            while (--pointer >= 0) {
                if (next) {
                    next = false;
                    c = sumsAndPowers[pointer].take();
                    if (c == null) {
                        sumsAndPowers[pointer].reset();
                        c = sumsAndPowers[pointer].take();
                        next = true;
                    }
                    currentMultipliers[pointer] = c;
                }
                temp.put(currentMultipliers[pointer]);
            }
            if (next)
                return null;
            return temp;
        }

        @Override
        public Tensor take() {
            if (currentBuilder == null)
                return null;
            if (sumsAndPowers.length == 0) {
                currentBuilder = null;
                return tensor;
            }
            Tensor t = sumsAndPowers[sumsAndPowers.length - 1].take();
            if (t == null) {
                currentBuilder = nextCombination();
                sumsAndPowers[sumsAndPowers.length - 1].reset();
                return take();
            }
            ProductBuilder temp = currentBuilder.clone();
            temp.put(t);
            return temp.build();
        }
    }

    private static final class SumPort implements ResetablePort {

        private final OutputPortUnsafe<Tensor>[] ports;
        private final Tensor tensor;
        private int pointer;

        public SumPort(Tensor tensor) {
            this.tensor = tensor;
            //noinspection unchecked
            this.ports = new OutputPortUnsafe[tensor.size()];
            reset();
        }

        @Override
        public void reset() {
            pointer = 0;
            for (int i = tensor.size() - 1; i >= 0; --i)
                ports[i] = createPort(tensor.get(i));
        }

        @Override
        public Tensor take() {
            Tensor t = null;
            while (pointer < tensor.size()) {
                t = ports[pointer].take();
                if (t == null)
                    ++pointer;
                else
                    return t;
            }
            return t;
        }
    }
}
