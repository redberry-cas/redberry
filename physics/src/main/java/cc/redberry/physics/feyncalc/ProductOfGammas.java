/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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

import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilder;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.Indicator;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.OutputPort;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
final class ProductOfGammas {
    final int offset;
    final int length;
    final ProductContent pc;
    final IntArrayList gPositions;
    final IntArrayList g5Positions;
    final GraphType graphType;

    public ProductOfGammas(int offset, ProductContent pc, IntArrayList gPositions, IntArrayList g5Positions, GraphType graphType) {
        this.offset = offset;
        this.pc = pc;
        this.gPositions = gPositions;
        this.g5Positions = g5Positions;
        this.graphType = graphType;
        this.length = gPositions.size();
    }

    public Tensor[] toArray() {
        Tensor[] array = new Tensor[gPositions.size()];
        for (int i = 0; i < gPositions.size(); i++)
            array[i] = pc.get(gPositions.get(i));
        return array;
    }

    public Tensor toProduct() {
        ProductBuilder pb = new ProductBuilder();
        for (int i = 0; i < gPositions.size(); i++)
            pb.put(pc.get(gPositions.get(i)));
        return pb.build();
    }

    public List<Tensor> toList() {
        List<Tensor> array = new ArrayList<>(gPositions.size());
        for (int i = 0; i < gPositions.size(); i++)
            array.add(pc.get(gPositions.get(i)));
        return array;
    }

    public Indices getIndices() {
        IndicesBuilder indices = new IndicesBuilder();
        for (int i = 0; i < length; i++)
            indices.append(pc.get(gPositions.get(i)));
        return indices.getIndices();
    }

    public static final class It implements OutputPort<ProductOfGammas> {
        final Product product;
        final ProductContent content;
        final IndexType matrixType;
        final PrimitiveSubgraph[] partition;
        final int gamma, gamma5;
        final Indicator<GraphType> filter;
        static Indicator<GraphType> defaultFilter = new Indicator<GraphType>() {
            @Override
            public boolean is(GraphType object) {
                return object != GraphType.Graph;
            }
        };

        public It(int gamma, int gamma5, Product product, IndexType matrixType, Indicator<GraphType> filter) {
            this.gamma = gamma;
            this.gamma5 = gamma5;
            this.product = product;
            this.content = product.getContent();
            this.matrixType = matrixType;
            this.partition
                    = PrimitiveSubgraphPartition.calculatePartition(this.content, matrixType);
            this.filter = filter;
        }

        int iPartition = 0, iSubgraphPosition = 0;

        //cache
        final IntArrayList gPositions = new IntArrayList(), g5Positions = new IntArrayList();

        @Override
        public ProductOfGammas take() {
            if (iPartition == partition.length)
                return null;

            for (; iPartition < partition.length; ++iPartition)
                if (filter.is(partition[iPartition].getGraphType()))
                    break;

            if (iPartition == partition.length)
                return null;

            PrimitiveSubgraph currentSubgraph = partition[iPartition];

            gPositions.clear();
            g5Positions.clear();

            for (; ; ++iSubgraphPosition) {
                if (iSubgraphPosition == currentSubgraph.size()) {
                    ++iPartition;
                    iSubgraphPosition = 0;
                    break;
                }
                int p = currentSubgraph.getPosition(iSubgraphPosition);
                if (isGamma(content.get(p)))
                    gPositions.add(p);
                else if (isGamma5(content.get(p))) {
                    g5Positions.add(gPositions.size());
                    gPositions.add(p);
                } else if (!gPositions.isEmpty() || !g5Positions.isEmpty())
                    break;
            }

            if (gPositions.isEmpty() && g5Positions.isEmpty())
                return take();

            //if cycle and one gamma5 -> move it to right
            if (currentSubgraph.getGraphType() == GraphType.Cycle
                    && g5Positions.size() == 1
                    && gPositions.size() == currentSubgraph.size()
                    && g5Positions.first() != currentSubgraph.size() - 1) {
                int g5 = g5Positions.first(),
                        size = currentSubgraph.size();
                g5Positions.set(0, size - 1);
                for (int i = 0; i <= g5; ++i)
                    gPositions.set(size - g5 - 1 + i, currentSubgraph.getPosition(i));
                for (int i = g5 + 1; i < size; ++i)
                    gPositions.set(i - g5 - 1, currentSubgraph.getPosition(i));
            }
            GraphType graphType = GraphType.Line;
            if (currentSubgraph.getGraphType() == GraphType.Cycle
                    && gPositions.size() == currentSubgraph.size())
                graphType = GraphType.Cycle;

            if (!filter.is(graphType))
                return take();

            return new ProductOfGammas(product.sizeOfIndexlessPart(), content, gPositions, g5Positions, graphType);
        }

        private boolean isGamma(Tensor t) {
            return t.hashCode() == gamma && t.getClass().equals(SimpleTensor.class);
        }

        private boolean isGamma5(Tensor t) {
            return t.hashCode() == gamma5 && t.getClass().equals(SimpleTensor.class);
        }
    }
}
