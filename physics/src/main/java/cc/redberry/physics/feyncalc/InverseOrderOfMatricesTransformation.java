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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.context.CC;
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indexmapping.IndexMapping;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.utils.ArraysUtils;

import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class InverseOrderOfMatricesTransformation implements Transformation {
    private final IndexType type;

    public InverseOrderOfMatricesTransformation(IndexType type) {
        assertType(type);
        this.type = type;
    }

    @Override
    public Tensor transform(Tensor t) {
        return inverseOrderOfMatrices1(t, type);
    }

    public static void assertType(IndexType type) {
        if (CC.isMetric(type.getType()))
            throw new IllegalArgumentException("Type should be non-metric.");
    }

    public static Tensor inverseOrderOfMatrices(Tensor t, IndexType type) {
        assertType(type);
        return inverseOrderOfMatrices1(t, type);
    }

    private static Tensor inverseOrderOfMatrices1(Tensor t, IndexType type) {
        FromChildToParentIterator iterator = new FromChildToParentIterator(t);
        Tensor c;
        while ((c = iterator.next()) != null)
            if (c instanceof Product)
                iterator.set(inverseOrderInProduct((Product) c, type));
        return iterator.result();
    }

    private static Tensor inverseOrderInProduct(Product product, IndexType type) {
        ProductContent pc = product.getContent();
        PrimitiveSubgraph[] subgraphs = PrimitiveSubgraphPartition.calculatePartition(pc, type);
        Tensor[] data = pc.getDataCopy();
        boolean somethingDone = false;
        for (PrimitiveSubgraph ps : subgraphs) {
            if (ps.getGraphType() == GraphType.Graph)
                throw new IllegalArgumentException("Not a product of matrices.");
            if (ps.getGraphType() != GraphType.Line)
                continue;
            int[] partition = ps.getPartition();

            Tensor left = null, right = null;
            Indices leftIndices = null, rightIndices = null;
            Indices leftSubIndices = null, rightSubIndices = null;
            int leftUpperCount, leftLowerCount, rightUpperCount, rightLowerCount, luCount = -1;
            boolean leftSkip = false, rightSkip = false, leftMatrix = false, rightMatrix = false;

            for (int leftPointer = 0, rightPointer = partition.length - 1;
                 leftPointer < rightPointer; ++leftPointer, --rightPointer) {

                if (!leftSkip) {
                    left = data[partition[leftPointer]];

                    leftIndices = left.getIndices();
                    leftSubIndices = leftIndices.getOfType(type);

                    leftUpperCount = leftSubIndices.getUpper().length();
                    leftLowerCount = leftSubIndices.getLower().length();

                    if (leftUpperCount != leftLowerCount) {
                        if (leftLowerCount != 0 && leftUpperCount != 0)
                            throw new IllegalArgumentException("Not a product of matrices.");
                        leftMatrix = false;
                    } else {
                        if (luCount == -1)
                            luCount = leftUpperCount;
                        else if (luCount != leftUpperCount)
                            throw new IllegalArgumentException("Not a product of matrices.");
                        leftMatrix = true;
                    }
                }

                if (!rightSkip) {
                    right = data[partition[rightPointer]];

                    rightIndices = right.getIndices();
                    rightSubIndices = rightIndices.getOfType(type);

                    rightUpperCount = rightSubIndices.getUpper().length();
                    rightLowerCount = rightSubIndices.getLower().length();

                    if (rightUpperCount != rightLowerCount) {
                        if (rightUpperCount != 0 && rightLowerCount != 0)
                            throw new IllegalArgumentException("Not a product of matrices.");
                        rightMatrix = false;
                    } else {
                        if (luCount == -1)
                            luCount = rightUpperCount;
                        else if (luCount != rightUpperCount)
                            throw new IllegalArgumentException("Not a product of matrices.");
                        rightMatrix = true;
                    }
                }

                //dump
                leftSkip = rightSkip = false;

                if (!leftMatrix && !rightMatrix)
                    continue;
                else if (leftMatrix && !rightMatrix) {
                    leftSkip = true;
                    --leftPointer;
                } else if (!leftMatrix && rightMatrix) {
                    rightSkip = true;
                    ++rightPointer;
                } else {
                    somethingDone = true;
                    left = setIndices(left, leftIndices, renameOfType(leftIndices, leftSubIndices, rightSubIndices));
                    right = setIndices(right, rightIndices, renameOfType(rightIndices, rightSubIndices, leftSubIndices));
                    data[partition[leftPointer]] = right;
                    data[partition[rightPointer]] = left;
                }
            }
        }
        if (!somethingDone)
            return product;
        return Tensors.multiply(product.getIndexlessSubProduct(), Tensors.multiply(data));
    }

    private static Tensor setIndices(Tensor t, Indices from, Indices to) {
        return ApplyIndexMapping.applyIndexMapping(t, from.getAllIndices().copy(), to.getAllIndices().copy(), new int[0]);
    }

    private static final Indices renameOfType(Indices indices, Indices fromSubIndices, Indices toSubIndices) {
        return indices.applyIndexMapping(new Mapper(fromSubIndices, toSubIndices));
    }

    private static final class Mapper implements IndexMapping {
        private final int[] from, to;

        private Mapper(Indices fromSubIndices, Indices toSubIndices) {
            from = fromSubIndices.getAllIndices().copy();
            to = toSubIndices.getAllIndices().copy();
            ArraysUtils.quickSort(from, to);
        }

        @Override
        public int map(int from) {
            int position;
            if ((position = Arrays.binarySearch(this.from, from)) >= 0)
                return to[position];
            return from;
        }
    }


}
