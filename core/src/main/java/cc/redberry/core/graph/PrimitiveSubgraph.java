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
package cc.redberry.core.graph;

import java.util.Arrays;

/**
 * Sub-graph of graph. This class is simply holds {@link GraphType} of sub-graph
 * and indices of its elements in the whole graph.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.1
 */
public final class PrimitiveSubgraph {
    private final GraphType graphType;
    private final int[] partition;

    /**
     * @param graphType type of sub-graph
     * @param partition positions of sub-graph elements in the whole graph
     */
    public PrimitiveSubgraph(GraphType graphType, int[] partition) {
        this.graphType = graphType;
        this.partition = partition;
    }

    /**
     * Returns sub-graph type
     *
     * @return sub-graph type
     */
    public GraphType getGraphType() {
        return graphType;
    }

    /**
     * Returns positions of sub-graph elements in the whole graph.
     *
     * @return positions of sub-graph elements in the whole graph
     */
    public int[] getPartition() {
        return partition.clone();
    }

    @Override
    public String toString() {
        return graphType + ": " + Arrays.toString(partition);
    }
}
