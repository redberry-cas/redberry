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
package cc.redberry.physics.oneloopdiv;

import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensor.ApplyIndexMapping;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.TensorUtils;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Averaging implements Transformation {

    private final SimpleTensor const_n;

    public Averaging(SimpleTensor const_n) {
        this.const_n = const_n;
    }

    private static Tensor average(final int[] indices) {
        if (indices.length == 0)
            return Complex.ONE;
        if (indices.length == 2)
            return Tensors.createMetricOrKronecker(indices[0], indices[1]);
        SumBuilder sb = new SumBuilder();
        for (int i = 1; i < indices.length; ++i) {
            int[] suffix = new int[indices.length - 2];
            System.arraycopy(indices, 1, suffix, 0, i - 1);
            System.arraycopy(indices, i + 1, suffix, i - 1, indices.length - i - 1);
            sb.put(Tensors.multiply(Tensors.createMetricOrKronecker(indices[0], indices[i]), average(suffix)));
        }
        return sb.build();
    }

    @Override
    public Tensor transform(Tensor tensor) {
        if (tensor instanceof Sum || tensor instanceof Expression) {
            int i;
            Tensor tensorCurrent, tempResult;
            Tensor[] newSumElements = new Tensor[tensor.size()];
            boolean needRebuild = false;
            for (i = tensor.size() - 1; i >= 0; --i) {
                tensorCurrent = tensor.get(i);
                tempResult = transform(tensorCurrent);
                if (tensorCurrent != tempResult)
                    needRebuild = true;
                newSumElements[i] = tempResult;
            }
            if (needRebuild)
                return tensor.getFactory().create(newSumElements);
            else
                return tensor;
        }

        if (tensor instanceof Product) {
            int i;
            int count = 0;
            Tensor current, old;
            IndicesBuilder ib = new IndicesBuilder();
            List<Tensor> newProductElements = new ArrayList<>();
            for (i = tensor.size() - 1; i >= 0; --i) {
                current = tensor.get(i);
                if (isN(current)) {
                    ib.append(current);
                    ++count;
                } else {
                    if (TensorUtils.isScalar(current)) {
                        FromChildToParentIterator iterator = new FromChildToParentIterator(current);
                        Tensor temp;
                        boolean flag = false;
                        while ((temp = iterator.next()) != null) {
                            if (isN(temp)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            newProductElements.add(current);
                            continue;
                        }
                        if (!(current instanceof Power) || !TensorUtils.isInteger(current.get(1)) || ((Complex) current.get(1)).intValue() != 2)
                            throw new IllegalArgumentException();

                        Tensor[] bases = {current.get(0), current.get(0)};
                        bases[1] = ApplyIndexMapping.renameDummy(bases[1], TensorUtils.getAllIndicesNamesT(tensor).toArray());
                        flag = false;
                        for (Tensor base : bases) {
                            for (Tensor t : base) {
                                if (isN(t)) {
                                    ib.append(t);
                                    ++count;
                                    flag = true;
                                } else
                                    newProductElements.add(t);
                            }
                        }
                        if (!flag)
                            throw new IllegalArgumentException("Expand first");
                    } else
                        newProductElements.add(current);
                }
            }
            if (count == 0)
                return tensor;
            if (count % 2 != 0)
                return Complex.ZERO;
//            System.out.println(count);
            count = count / 2;
            Tensor result = average(ib.getIndices().getAllIndices().copy());
            long factor = ArithmeticUtils.pow((long) 2, count) * ArithmeticUtils.factorial(count + 1);//may be BigInteger?
            Complex number = new Complex((long) factor).reciprocal();
            result = ExpandTransformation.expand(result);
            newProductElements.add(number);
            newProductElements.add(result);
            return Tensors.multiply(newProductElements.toArray(new Tensor[newProductElements.size()]));
        }

        if (tensor instanceof Power) {
            Tensor nBase = transform(tensor.get(0));
            if (nBase == tensor.get(0))
                return tensor;
            return Tensors.pow(nBase, tensor.get(1));
        }
        return tensor;
    }

    private boolean isN(Tensor t) {
        return t instanceof SimpleTensor && ((SimpleTensor) t).getName() == const_n.getName();
    }
}
