/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.combinatorics.IntTuplesPort;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.OutputPort;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.utils.TensorUtils.isSymbolic;

/**
 * Output port, which iteratively gives expand tensor.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class ExpandPort {

    public static Tensor expandUsingPort(Tensor t) {
        return expandUsingPort(t, true);
    }

    public static Tensor expandUsingPort(Tensor t, boolean expandSymbolic) {
        OutputPort<Tensor> port = createPort(t, expandSymbolic);
        if (port instanceof OutputPort.Singleton)
            return port.take();
        SumBuilder sb = new SumBuilder();
        Tensor n;
        while ((n = port.take()) != null)
            sb.put(n);
        return sb.build();
    }

    public static OutputPort<Tensor> createPort(Tensor tensor, boolean expandSymbolic) {
        if (tensor instanceof Product)
            return new ProductPort(tensor, expandSymbolic);
        if (tensor instanceof Sum)
            return new SumPort(tensor, expandSymbolic);
        if (ExpandUtils.isExpandablePower(tensor)
                && !TensorUtils.isNegativeNaturalNumber(tensor.get(1)))
            if (!expandSymbolic && isSymbolic(tensor.get(0)))
                return new OutputPort.Singleton<>(tensor);
            else
                return new PowerPort(tensor, expandSymbolic);
        else
            return new OutputPort.Singleton<>(tensor);
    }

    private interface ResettablePort extends OutputPort<Tensor> {
        void reset();
    }

    private static final class PowerPort implements ResettablePort {
        private final Tensor base;
        private final int power;
        private IntTuplesPort tuplesPort;
        private final int[] initialForbidden;
        private OutputPort<Tensor> currentPort;
        private final boolean expandSymbolic;

        public PowerPort(Tensor tensor, int[] initialForbidden, boolean expandSymbolic) {
            this.expandSymbolic = expandSymbolic;
            base = tensor.get(0);
            power = ((Complex) tensor.get(1)).getReal().intValue();
            int[] upperBounds = new int[power];
            Arrays.fill(upperBounds, base.size());
            tuplesPort = new IntTuplesPort(upperBounds);
            this.initialForbidden = initialForbidden;
            currentPort = nextPort();
        }

        public PowerPort(Tensor tensor, boolean expandSymbolic) {
            this(tensor, TensorUtils.getAllIndicesNamesT(tensor.get(0)).toArray(), expandSymbolic);
        }

        OutputPort<Tensor> nextPort() {
            final int[] tuple = tuplesPort.take();
            if (tuple == null)
                return null;
            TIntHashSet added = new TIntHashSet(initialForbidden);
            ProductBuilder builder = new ProductBuilder();
            builder.put(base.get(tuple[0]));
            for (int i = 1; i < tuple.length; ++i)
                builder.put(ApplyIndexMapping.renameDummy(base.get(tuple[i]), added.toArray(), added));

            return createPort(builder.build(), expandSymbolic);
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

    private static final class ProductPort implements OutputPort<Tensor> {
        private final ProductBuilder base;
        private ProductBuilder currentBuilder;
        private final ResettablePort[] sumsAndPowers;
        private final Tensor[] currentMultipliers;
        private final Tensor tensor;
        private final boolean expandSymbolic;

        public ProductPort(Tensor tensor, boolean expandSymbolic) {
            this.tensor = tensor;
            this.expandSymbolic = expandSymbolic;
            this.base = new ProductBuilder();
            List<ResettablePort> sumOrPowerPorts = new ArrayList<>();
            int theLargestSumPosition = 0, theLargestSumSize = 0, productSize = tensor.size();
            Tensor m;
            for (int i = 0; i < productSize; ++i) {
                m = tensor.get(i);
                if (m instanceof Sum && expandIfSymbolic(m)) {
                    if (m.size() > theLargestSumSize) {
                        theLargestSumPosition = sumOrPowerPorts.size();
                        theLargestSumSize = m.size();
                    }
                    sumOrPowerPorts.add(new SumPort(m, expandSymbolic));
                } else if (ExpandUtils.isExpandablePower(m)) {
                    if (TensorUtils.isNegativeNaturalNumber(m.get(1)) || !expandIfSymbolic(m.get(0))) {
//                        base.put(Tensors.reciprocal(ExpandUtils.expandPower(
//                                (Sum) m.get(0), ((Complex) m.get(1)).getReal().intValue(),
//                                TensorUtils.getAllIndicesNamesT(tensor).toArray(), new Transformation[0])));
                        base.put(m);
                        continue;
                    }
                    if (NumberUtils.pow(BigInteger.valueOf(m.get(0).size()),
                            (((Complex) m.get(1)).getReal().bigIntValue())).compareTo(BigInteger.valueOf(theLargestSumSize)) > 0) {
                        theLargestSumPosition = sumOrPowerPorts.size();
                        theLargestSumSize = m.size();
                    }
                    sumOrPowerPorts.add(new PowerPort(m, TensorUtils.getAllIndicesNamesT(tensor).toArray(), expandSymbolic));
                } else
                    base.put(m);
            }
            sumsAndPowers = sumOrPowerPorts.toArray(new ResettablePort[sumOrPowerPorts.size()]);

            if (sumsAndPowers.length <= 1) {
                currentMultipliers = new Tensor[0];
                currentBuilder = base;
            } else {
                ResettablePort temp = sumsAndPowers[theLargestSumPosition];
                sumsAndPowers[theLargestSumPosition] = sumsAndPowers[sumsAndPowers.length - 1];
                sumsAndPowers[sumsAndPowers.length - 1] = temp;
                currentMultipliers = new Tensor[sumsAndPowers.length - 2];
                for (productSize = 0; productSize < sumsAndPowers.length - 2; ++productSize)
                    currentMultipliers[productSize] = sumsAndPowers[productSize].take();
                currentBuilder = nextCombination();
            }
        }

        private boolean expandIfSymbolic(Tensor t) {
            return expandSymbolic || !isSymbolic(t);
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

    private static final class SumPort implements ResettablePort {
        private final OutputPort<Tensor>[] ports;
        private final Tensor tensor;
        private int pointer;
        private boolean expandSymbolic;

        @SuppressWarnings("unchecked")
        public SumPort(Tensor tensor, boolean expandSymbolic) {
            this.tensor = tensor;
            this.expandSymbolic = expandSymbolic;
            this.ports = new OutputPort[tensor.size()];
            reset();
        }

        @Override
        public void reset() {
            pointer = 0;
            for (int i = tensor.size() - 1; i >= 0; --i)
                ports[i] = createPort(tensor.get(i), expandSymbolic);
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
