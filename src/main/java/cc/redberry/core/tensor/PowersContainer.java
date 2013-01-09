/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.utils.EmptyIterator;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.function.TObjectFunction;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PowersContainer implements Iterable<Tensor> {
    private boolean sign;
    private final TIntObjectHashMap<ArrayList<PowerNode>> powers;

    public PowersContainer() {
        this.powers = new TIntObjectHashMap<>();
    }

    public PowersContainer(int initialCapacity) {
        this.powers = new TIntObjectHashMap<>(initialCapacity);
    }

    private PowersContainer(TIntObjectHashMap<ArrayList<PowerNode>> powers, boolean sign) {
        this.powers = powers;
        this.sign = sign;
    }

    public boolean isSign() {
        return sign;
    }

    public boolean isEmpty() {
        return powers.isEmpty();
    }

    public int size(){
        return powers.size();
    }

    public void put(Tensor tensor) {
        Tensor base, exponent;
        if (tensor instanceof Power) {
            //case x^y
            base = tensor.get(0);
            exponent = tensor.get(1);
        } else {
            //case x^1 (= x)
            base = tensor;
            exponent = Complex.ONE;
        }

        int hash = base.hashCode();
        ArrayList<PowerNode> nodes = powers.get(hash);
        if (nodes == null)
            powers.put(hash, nodes = new ArrayList<>());
        for (PowerNode node : nodes) {
            Boolean compare = TensorUtils.compare1(node.base, base);
            if (compare == null)
                continue;

            if (compare == false) {
                //case base == node.base
                node.putExponent(exponent);
                return;
            }

            //at this point we know, that base == - power.base
            if (TensorUtils.isInteger(exponent)) {
                //exponent = 2*n
                //e.g. node.base = (a-b) node.exponent == x
                //          base = (b-a)      exponent == 2
                //then (a-b)**x * (b-a)**2 -> (a-b)**(x+2)
                node.putExponent(exponent);
                if (TensorUtils.isIntegerOdd(exponent))
                    //exponent = 2*n + 1
                    //e.g. node.base = (a-b) node.exponent == x
                    //          base = (b-a)      exponent == 3
                    //then (a-b)**x * (b-a)**3 -> -(a-b)**(x+3)
                    sign = !sign;
                return;
            }

            //at this point we know, that exponent is not integer
            if (node.exponent == null || node.exponent.summands.isEmpty()) {
                //node.exponent is number
                Complex exponent1 = node.exponent == null ? Complex.ONE : node.exponent.complex;
                if (exponent1.isInteger()) {
                    //node.exponent = 2*n
                    //e.g. node.base = (a-b) node.exponent == 2
                    //          base = (b-a)      exponent == x
                    //then (a-b)**2 * (b-a)**x -> (b-a)**(x+2)
                    node.base = base;
                    node.putExponent(exponent);
                    if (NumberUtils.isIntegerOdd(exponent1))
                        //node.exponent = 2*n+1
                        //e.g. node.base = (a-b) node.exponent == 3
                        //          base = (b-a)      exponent == x
                        //then (a-b)**3 * (b-a)**x -> -(b-a)**(x+3)
                        sign = !sign;
                    return;
                }
            }
        }
        //no similar powers were found
        nodes.add(new PowerNode(base, exponent));
    }

    public void putNew(Tensor base, Tensor exponent) {
        ArrayList<PowerNode> newNodes = new ArrayList<>();
        ArrayList<PowerNode> nodes = powers.putIfAbsent(base.hashCode(), newNodes);
        if (nodes != null) {
            nodes.add(new PowerNode(base, exponent));
        } else {
            newNodes.add(new PowerNode(base, exponent));
        }
    }

    @Override
    public Iterator<Tensor> iterator() {
        return new It();
    }

    private class It implements Iterator<Tensor> {
        private final Iterator<ArrayList<PowerNode>> lists = powers.valueCollection().iterator();
        @SuppressWarnings("unchecked")
        private Iterator<PowerNode> nodes = EmptyIterator.INSTANCE;

        @Override
        public boolean hasNext() {
            if (nodes.hasNext())
                return true;
            else if (!lists.hasNext())
                return false;
            nodes = lists.next().iterator();
            return hasNext();
        }

        @Override
        public Tensor next() {
            return nodes.next().build();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public PowersContainer clone() {
        TIntObjectHashMap<ArrayList<PowerNode>> newPowers = new TIntObjectHashMap<>(powers);
        newPowers.transformValues(copyPowers);
        return new PowersContainer(newPowers, sign);
    }

    private static final TObjectFunction<ArrayList<PowerNode>, ArrayList<PowerNode>> copyPowers
            = new TObjectFunction<ArrayList<PowerNode>, ArrayList<PowerNode>>() {
        @Override
        public ArrayList<PowerNode> execute(ArrayList<PowerNode> value) {
            ArrayList<PowerNode> newList = new ArrayList<>(value.size());
            for (PowerNode node : value)
                newList.add(node.clone());
            return newList;
        }
    };

    static final class PowerNode {
        private Tensor base;
        private SumBuilder exponent;

        PowerNode(Tensor base, Tensor exponent) {
            this.base = base;
            if (exponent == Complex.ONE)
                this.exponent = null;
            else {
                this.exponent = new SumBuilder();
                this.exponent.put(exponent);
            }
        }


        private PowerNode(SumBuilder exponent, Tensor base) {
            this.exponent = exponent;
            this.base = base;
        }

        void putExponent(Tensor exp) {
            if (exponent == null) {
                exponent = new SumBuilder();
                exponent.put(Complex.ONE);
            }
            exponent.put(exp);
        }

        Tensor build() {
            if (exponent == null)
                return base;
            return Tensors.pow(base, exponent.build());
        }

        public PowerNode clone() {
            return new PowerNode(exponent == null ? null : (SumBuilder) exponent.clone(), base);
        }
    }
}