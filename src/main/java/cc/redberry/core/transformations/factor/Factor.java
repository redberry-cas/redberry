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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.Rational;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.ScalarFunction;
import cc.redberry.core.tensor.iterator.TensorFirstIterator;
import cc.redberry.core.tensor.iterator.TensorLastIterator;
import cc.redberry.core.tensor.iterator.TreeIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.fractions.Together;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.math.BigInteger;
import java.util.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Factor implements Transformation {
    public static final Factor FACTOR = new Factor();

    private Factor() {
    }

    @Override
    public Tensor transform(Tensor t) {
        return factor(t);
    }

    public static Tensor factor(Tensor tensor) {
        TensorFirstIterator iterator = new TensorFirstIterator(tensor);
        TreeIterator iterator1;
        Tensor c, t;
        Complex e;
        out:
        while ((c = iterator.next()) != null) {
            if (!(c instanceof Sum))
                continue;
            iterator1 = new TensorLastIterator(c);
            boolean needTogether = false;
            while ((t = iterator1.next()) != null) {
                if (t.getIndices().size() != 0 || t instanceof ScalarFunction)
                    continue out;
                if (t instanceof Power) {
                    if (!(t.get(1) instanceof Complex))
                        continue out;
                    e = (Complex) t.get(1);
                    if (!e.isReal() || e.isNumeric())
                        continue out;
                    if (e.getReal().signum() < 0)
                        needTogether = true;
                }

                if (t instanceof Sum && !needTogether)
                    iterator1.set(factorOut(t));
            }

            c = iterator1.result();
            iterator1 = new TensorFirstIterator(c);
            while ((c = iterator1.next()) != null) {
                if (!(c instanceof Sum))
                    continue;

                if (needTogether) {
                    c = Together.together(c);
                    if (c instanceof Product) {
                        for (int i = c.size() - 1; i >= 0; --i) {
                            if (c.get(i) instanceof Sum)
                                c = c.set(i, JasFactor.factor(c.get(i)));
                        }
                        iterator1.set(c);
                    }
                } else {
                    iterator1.set(JasFactor.factor(c));
                }
            }
            iterator.set(iterator1.result());
        }
        return iterator.result();
    }

    static Tensor factorOut(Tensor tensor) {
        int minSize = Integer.MAX_VALUE, minSizePosition = -1, i, j;
        Tensor temp;
        SumBuilder nonProductsSB = new SumBuilder();
        for (i = tensor.size() - 1; i >= 0; --i) {
            temp = tensor.get(i);

            if (temp instanceof Complex)
                return tensor;
            if (!(temp instanceof Product)) {
                nonProductsSB.put(temp);
                if (tensor instanceof Sum)
                    tensor = ((Sum) tensor).remove(i);
                else
                    tensor = Complex.ZERO;
            }
            if (temp.size() < minSize) {
                minSize = temp.size();
                minSizePosition = i;
            }
        }

        Tensor nonProducts = JasFactor.factor(nonProductsSB.build());
        if (tensor == Complex.ZERO)
            return nonProducts;

        tensor = Tensors.sum(tensor, nonProducts);

        ArrayList<FactorNode> baseFactors = new ArrayList<>(minSize);
        temp = tensor.get(minSizePosition);
        FactorNode baseNode, tempNode;
        for (Tensor t : temp) {
            baseFactors.add(baseNode = createNode(t));
            baseNode.minExponent = new Int();
        }

        FactorNode[][] resultingNodes = new FactorNode[tensor.size()][];

        BigInteger baseExponent, tempExponent;
        TIntObjectHashMap<ArrayList<FactorNode>> map;
        ArrayList<FactorNode> tempList;
        Boolean sign = null;
        for (i = tensor.size() - 1; i >= 0; --i) {
            if (baseFactors.isEmpty())
                return tensor;

            temp = tensor.get(i);
            if (!(temp instanceof Product)) {
                resultingNodes[i] = new FactorNode[1];
                tempNode = createNode(temp);
                resultingNodes[i][0] = tempNode;
                for (j = baseFactors.size() - 1; j >= 0; --j) {
                    baseNode = baseFactors.get(j);
                    if (!TensorUtils.equals(baseNode.tensor, tempNode.tensor)) {
                        baseFactors.remove(j);
                        continue;
                    }
                    baseExponent = baseNode.exponent;
                    tempExponent = tempNode.exponent;

                    if (baseExponent.signum() != tempExponent.signum()) {
                        baseFactors.remove(j);
                        continue;
                    }

                    tempNode.minExponent = baseNode.minExponent;
                    if (baseExponent.signum() > 0 && baseExponent.compareTo(tempExponent) > 0)
                        baseNode.exponent = tempExponent;
                    else if (baseExponent.signum() < 0 && baseExponent.compareTo(tempExponent) < 0)
                        baseNode.exponent = tempExponent;
                }
            } else {
                map = new TIntObjectHashMap<>(temp.size());
                resultingNodes[i] = new FactorNode[temp.size()];
                j = 0;

                for (Tensor t : temp) {
                    tempNode = createNode(t);
                    resultingNodes[i][j++] = tempNode;
                    tempList = map.get(tempNode.tensor.hashCode());
                    if (tempList != null) {
                        tempList.add(tempNode);
                        continue;
                    }
                    tempList = new ArrayList<>();
                    tempList.add(tempNode);
                    map.put(tempNode.tensor.hashCode(), tempList);
                }

                for (j = baseFactors.size() - 1; j >= 0; --j) {
                    baseNode = baseFactors.get(j);
                    tempList = map.get(baseNode.tensor.hashCode());
                    if (tempList == null) {
                        baseFactors.remove(j);
                        continue;
                    }
                    for (FactorNode nn : tempList) {
                        sign = TensorUtils.compare1(baseNode.tensor, nn.tensor);
                        if (sign == null)
                            continue;

                        baseExponent = baseNode.exponent;
                        tempExponent = nn.exponent;

                        if (baseExponent.signum() != tempExponent.signum()) {
                            baseFactors.remove(j);
                            continue;
                        }

                        if (sign)
                            nn.diffSigns = true;

                        nn.minExponent = baseNode.minExponent;

                        if (baseExponent.signum() > 0 && baseExponent.compareTo(tempExponent) > 0)
                            baseNode.exponent = tempExponent;
                        else if (baseExponent.signum() < 0 && baseExponent.compareTo(tempExponent) < 0)
                            baseNode.exponent = tempExponent;
                        break;
                    }

                    if (sign == null) {
                        baseFactors.remove(j);
                        continue;
                    }
                }
            }
        }

        if (baseFactors.isEmpty())
            return tensor;
        ProductBuilder pb = new ProductBuilder(baseFactors.size(), baseFactors.size());
        for (FactorNode node : baseFactors) {
            pb.put(node.toTensor());
            node.minExponent.value = node.exponent;
        }

        SumBuilder sb = new SumBuilder(tensor.size());
        for (FactorNode[] nodes : resultingNodes)
            sb.put(nodesToProduct(nodes));

        return Tensors.multiply(pb.build(), sb.build());
    }


    private static FactorNode createNode(Tensor tensor) {
        if (tensor instanceof Power && TensorUtils.isInteger(tensor.get(1)))
            return new FactorNode(tensor.get(0),
                    ((Rational) ((Complex) tensor.get(1)).getReal()).getNumerator());
        return new FactorNode(tensor, BigInteger.ONE);
    }

    private static Tensor nodesToProduct(FactorNode[] nodes) {
        Tensor[] tensors = new Tensor[nodes.length];
        for (int i = nodes.length - 1; i >= 0; --i)
            tensors[i] = nodes[i].toTensor();
        return Tensors.multiply(tensors);
    }

    private static class FactorNode {
        final Tensor tensor;
        BigInteger exponent;
        Int minExponent;
        boolean diffSigns = false;

        private FactorNode(Tensor tensor, BigInteger exponent) {
            this.tensor = tensor;
            this.exponent = exponent;
        }

        Tensor toTensor() {
            BigInteger exponent = this.exponent;
            if (minExponent != null && minExponent.value != null)
                exponent = exponent.subtract(minExponent.value);
            if (diffSigns && minExponent.value.testBit(0))
                return Tensors.negate(Tensors.pow(tensor, new Complex(exponent)));
            return Tensors.pow(tensor, new Complex(exponent));
        }

        @Override
        public boolean equals(Object o) {
            return TensorUtils.equals(((FactorNode) o).tensor, tensor);
        }

        @Override
        public int hashCode() {
            return tensor.hashCode();
        }

        @Override
        public String toString() {
            return tensor + " -> " + exponent;
        }
    }

    private static final class Int {
        BigInteger value;
    }
}
