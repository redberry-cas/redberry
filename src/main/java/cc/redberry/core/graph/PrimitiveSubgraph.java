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
package cc.redberry.core.graph;

import java.util.Arrays;

/**
 * Subgraph of some graph. This class is simply holds {@link GraphType} of subgraph and
 * positions of its elements in the whole graph.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PrimitiveSubgraph {
    private final GraphType graphType;
    private final int[] partition;

    /**
     * @param graphType type of subgraph
     * @param partition positions of subgraph elements in the whole graph
     */
    public PrimitiveSubgraph(GraphType graphType, int[] partition) {
        this.graphType = graphType;
        this.partition = partition;
    }

    /**
     * Returns subgraph type
     *
     * @return subgraph type
     */
    public GraphType getGraphType() {
        return graphType;
    }

    /**
     * Returns positions of subgraph elements in the whole graph.
     *
     * @return positions of subgraph elements in the whole graph
     */
    public int[] getPartition() {
        return partition.clone();
    }

    @Override
    public String toString() {
        return graphType + ": " + Arrays.toString(partition);
    }
}
