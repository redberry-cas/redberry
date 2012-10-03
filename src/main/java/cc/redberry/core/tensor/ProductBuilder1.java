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

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.HashMap;

import static cc.redberry.core.tensor.Tensors.pow;
import static cc.redberry.core.utils.TensorUtils.isScalar;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProductBuilder1 implements TensorBuilder {
    private Complex complex = Complex.ONE;
    private final HashMap<Integer, ArrayList<PowerNode>> scalarPowers;
    private final ArrayList<Component> connectedComponents;

    public ProductBuilder1(int initialScalarsCapacity, int initialDataCapacity) {
        connectedComponents = new ArrayList<>(initialDataCapacity);
        scalarPowers = new HashMap<>(initialScalarsCapacity);
    }

    public ProductBuilder1() {
        this(4, 3);
    }

    @Override
    public void put(Tensor tensor) {
        if (tensor instanceof Complex) {
            complex = complex.multiply((Complex) tensor);
            return;
        }
        if (complexIsZero()) {
            return;
        }
        if (tensor instanceof Product) {
            Product p = ((Product) tensor);
            complex = complex.multiply(p.factor);
            if (complexIsZero())
                return;

            ProductContent pc = p.getContent();

            for (Tensor t : pc.getScalars())
                putScalar(t);
            Tensor nonScalar = pc.getNonScalar();
            if (nonScalar == null)
                return;
            else if (nonScalar instanceof Product)
                for (Tensor t : nonScalar)
                    putNonScalar(t);
            else
                putNonScalar(nonScalar);
            return;
        }
        if (isScalar(tensor)) {
            putScalar(tensor);
            return;
        } else
            putNonScalar(tensor);

    }

    private boolean complexIsZero() {
        return complex.isZero() || complex.isNaN() || complex.isInfinite(); //TODO check condition
    }

    private void putScalar(Tensor tensor) {
        Tensor base, exponent;
        if (tensor instanceof Power) {
            base = tensor.get(0);
            exponent = tensor.get(1);
        } else {
            base = tensor;
            exponent = Complex.ONE;
        }

        ArrayList<PowerNode> nodes = scalarPowers.get(base.hash());
        if (nodes == null) {
            nodes = new ArrayList<>(2);
            nodes.add(new PowerNode(base, exponent));
            scalarPowers.put(base.hash(), nodes);
        } else {
            Boolean compare = null;
            for (PowerNode node : nodes) {
                compare = TensorUtils.compare1(base, node.base);
                if (compare == null)
                    continue;
                else if (!compare.booleanValue()) {
                    node.power.put(exponent);
                    return;
                } else {
                    if (TensorUtils.isIntegerOdd(exponent)) {
                        node.power.put(exponent);
                        complex = complex.negate();
                        return;
                    }
                    if (TensorUtils.isIntegerEven(exponent)) {
                        node.power.put(exponent);
                        return;
                    }
                    compare = null;
                }
            }
            if (compare == null) {
                SumBuilder exponentBuilder = new SumBuilder();
                exponentBuilder.put(exponent);
                nodes.add(new PowerNode(base, exponent));
            }
        }
    }

    private void putNonScalar(Tensor tensor) {
        Indices freeIndices = tensor.getIndices().getFree();
        int matchedPosition = -1;
        int freeIndex = 0;
        for (int i = freeIndices.size() - 1; i >= 0; --i) {
            freeIndex = freeIndices.get(i);
            for (matchedPosition = connectedComponents.size() - 1; matchedPosition >= 0; --matchedPosition) {
                if (connectedComponents.get(matchedPosition).freeIndices.contains(IndicesUtils.inverseIndexState(freeIndex)))
                    break;
            }
        }
        if (matchedPosition == -1) {
            connectedComponents.add(new Component(tensor, new TIntHashSet(freeIndices.getAllIndices().copy())));
            return;
        }

        Component component = connectedComponents.get(matchedPosition);
        component.put(tensor);
        for (int i = freeIndices.size() - 1; i >= 0; --i) {
            freeIndex = freeIndices.get(i);
            if (!component.freeIndices.remove(IndicesUtils.inverseIndexState(freeIndex)))
                component.freeIndices.add(freeIndex);
        }
        if (component.freeIndices.isEmpty()) {
            connectedComponents.remove(matchedPosition);
            putScalar(new Product(component.indicesBuilder.getIndices(), Complex.ONE, new Tensor[0], component.toArray()));
            return;
        }
    }

    @Override
    public Tensor build() {
        if (complexIsZero())
            return complex;

        ArrayList<Tensor> indexLessDataList = new ArrayList<>();
        ArrayList<Tensor> dataList = new ArrayList<>();
        IndicesBuilder indicesBuilder = new IndicesBuilder();

        Tensor temp;
        for (ArrayList<PowerNode> powerNodes : scalarPowers.values()) {
            for (PowerNode node : powerNodes) {
                temp = pow(node.base, node.power.build());
                if (temp instanceof Complex) {
                    complex = complex.multiply((Complex) temp);
                    if (complexIsZero())
                        return complex;
                }
                if (temp.getIndices().size() == 0)
                    indexLessDataList.add(temp);
                else {
                    dataList.add(temp);
                    indicesBuilder.append(temp);
                }
            }
        }
        for (Component component : connectedComponents) {
            indicesBuilder.append(component.indicesBuilder);
            for (Tensor t : component.elements) {
                dataList.add(t);
            }
        }

        if (indexLessDataList.isEmpty() && dataList.isEmpty())
            return complex;

        if (complex.isOne()) {
            if (indexLessDataList.size() == 1 && dataList.isEmpty())
                return indexLessDataList.get(0);
            if (indexLessDataList.isEmpty() && dataList.size() == 1)
                return dataList.get(0);
        }

        return new Product(indicesBuilder.getIndices(), complex,
                indexLessDataList.toArray(new Tensor[indexLessDataList.size()]),
                dataList.toArray(new Tensor[dataList.size()]));
    }

    @Override
    public TensorBuilder clone() {
        return null;
    }

    private static final class Component {
        final ArrayList<Tensor> elements;
        final TIntHashSet freeIndices;
        final IndicesBuilder indicesBuilder;//may be put only dummy indices?

        private Component(Tensor element, TIntHashSet freeIndices) {
            elements = new ArrayList<>();
            elements.add(element);
            this.freeIndices = freeIndices;
            indicesBuilder = new IndicesBuilder();
            indicesBuilder.append(element);
        }

        void put(Tensor element) {
            elements.add(element);
            indicesBuilder.append(element);
        }

        Tensor[] toArray() {
            return elements.toArray(new Tensor[elements.size()]);
        }
    }

    private static final class PowerNode {
        private final Tensor base;
        private final SumBuilder power;

        private PowerNode(Tensor base, Tensor exponent) {
            this.base = base;
            this.power = new SumBuilder();
            this.power.put(exponent);
        }
    }
}
