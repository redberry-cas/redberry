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

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.FullContractionsStructure;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.ProductContent;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.LongBackedBitArray;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.indices.IndicesUtils.*;
import static cc.redberry.core.tensor.FullContractionsStructure.getToTensorIndex;

/**
 * This class gives a partition of graph on subgraphs of types specified in {@link GraphType}. Such a partition
 * takes into account the links (dummy indices) of specified {@link IndexType}.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class PrimitiveSubgraphPartition {
    private final ProductContent pc;
    private final FullContractionsStructure fcs;
    private final int size;
    private final IndexType type;
    private final PrimitiveSubgraph[] partition;
    private final LongBackedBitArray used;

    /**
     * Creates partition of graph (or equivalently the product of indexed tensors)
     * specified by {@link ProductContent} taking into account edges (dummy indices) of specified {@link IndexType}.
     *
     * @param productContent {@link ProductContent} representing the graph
     * @param type           type of edges to be taken into account in partition
     */
    public PrimitiveSubgraphPartition(ProductContent productContent, IndexType type) {
        this.pc = productContent;
        this.fcs = pc.getFullContractionsStructure();
        this.size = pc.size();
        this.type = type;
        this.used = new LongBackedBitArray(size);
        this.partition = calculatePartition();
    }

    /**
     * Returns the partition of graph, i.e. an array of all its subgraphs of types specified by {@link GraphType}.
     *
     * @return the partition of graph, i.e. an array of all its subgraphs of types specified by {@link GraphType}
     */
    public PrimitiveSubgraph[] getPartition() {
        return partition.clone();
    }

    /**
     * Creates partition of graph (or equivalently the product of indexed tensors)
     * specified by {@link Product} taking into account edges (dummy indices) of specified {@link IndexType}.
     * This method returns an array of all its subgraphs of types specified by {@link GraphType}. <b>Note</b>, that
     * only indexed part of specified product (i.e. its {@link ProductContent}) will be taken into account. So the
     * positions of subgraphs elements in {@link PrimitiveSubgraph} may not be equal to the positions of tensors
     * in product (since it can e.g. have a symbolic part), but strictly corresponds to the positions of
     * tensors in its {@link ProductContent}, i.e. in the indexed part of the product.
     *
     * @param p    {@link Product} representing the graph
     * @param type type of edges to be taken into account in partition
     * @return the partition of graph, i.e. an array of all its subgraphs of types specified by {@link GraphType}
     */
    public static PrimitiveSubgraph[] calculatePartition(Product p, IndexType type) {
        return new PrimitiveSubgraphPartition(p.getContent(), type).partition;
    }

    /**
     * Creates partition of graph (or equivalently the product of indexed tensors)
     * specified by {@link ProductContent} taking into account edges (dummy indices) of specified {@link IndexType}.
     * This method returns an array of all its subgraphs of types specified by {@link GraphType}.
     *
     * @param p    {@link ProductContent} representing the graph
     * @param type type of edges to be taken into account in partition
     */
    public static PrimitiveSubgraph[] calculatePartition(ProductContent p, IndexType type) {
        return new PrimitiveSubgraphPartition(p, type).partition;
    }

    private PrimitiveSubgraph[] calculatePartition() {
        List<PrimitiveSubgraph> subgraphs = new ArrayList<>();
        for (int pivot = 0; pivot < size; ++pivot)
            if (pc.get(pivot).getIndices().size(type) != 0 && !used.get(pivot))
                subgraphs.add(calculateComponent(pivot));
        return subgraphs.toArray(new PrimitiveSubgraph[subgraphs.size()]);
    }

    private PrimitiveSubgraph calculateComponent(final int pivot) {
        ArrayDeque<Integer> positions = new ArrayDeque<>();
        positions.add(pivot);

        int[] left, right;
        left = right = getLinks(pivot);

        assert left[0] != NO_LINKS || left[1] != NO_LINKS;

        if (left[0] == BRANCHING || left[1] == BRANCHING)
            return processGraph(pivot);

        if (left[0] == left[1] && left[0] == pivot) {
            used.set(pivot);
            return new PrimitiveSubgraph(GraphType.Cycle, new int[]{pivot});
        }

        int leftPivot, rightPivot, lastLeftPivot = NOT_INITIALIZED, lastRightPivot = NOT_INITIALIZED;

        while (left != DUMMY || right != DUMMY) {

            if (left[0] == BRANCHING || left[1] == BRANCHING || right[0] == BRANCHING || right[1] == BRANCHING)
                return processGraph(pivot);

            leftPivot = left[0];
            rightPivot = right[1];

            assert leftPivot < 0 || !used.get(leftPivot);
            assert rightPivot < 0 || !used.get(rightPivot);

            //Left end detection
            if (leftPivot == NO_LINKS || leftPivot == -1)
                leftPivot = DUMMY_PIVOT;

            //Right end detection
            if (rightPivot == NO_LINKS || rightPivot == -1)
                rightPivot = DUMMY_PIVOT;

            //Odd cycle detection
            if (leftPivot >= 0 && leftPivot == lastRightPivot) {
                //Closing odd nodes number cycle
                assert rightPivot == lastLeftPivot;
                return new PrimitiveSubgraph(GraphType.Cycle, deque2array(positions));
            }

            //Adding left pivot before cycle detection (if cycle, not to add closing node twice)
            if (leftPivot >= 0)
                positions.addFirst(leftPivot);

            //Even cycle detection
            if (leftPivot >= 0 && leftPivot == rightPivot) {
                left = getLinks(leftPivot);

                // Checking next (cycle closing) node
                if (left[0] == BRANCHING || left[1] == BRANCHING)
                    return processGraph(pivot);

                return new PrimitiveSubgraph(GraphType.Cycle, deque2array(positions));
            }

            //Adding right pivot
            if (rightPivot >= 0)
                positions.addLast(rightPivot);

            //Needed in odd cycle detection
            lastLeftPivot = leftPivot;
            //Redundant (needed for assertion)
            lastRightPivot = rightPivot;

            //Next layer (breadth-first traversal)
            left = getLinks(leftPivot);
            right = getLinks(rightPivot);
        }

        return new PrimitiveSubgraph(GraphType.Line, deque2array(positions));
    }


    private int[] deque2array(ArrayDeque<Integer> deque) {
        int[] arr = new int[deque.size()];
        int i = -1;
        for (Integer ii : deque) {
            arr[++i] = ii;
            used.set(ii);
        }
        return arr;
    }

    private static final int BRANCHING = -3, NO_LINKS = -2, NOT_INITIALIZED = -4, DUMMY_PIVOT = -5;
    private static final int[] DUMMY = new int[]{DUMMY_PIVOT, DUMMY_PIVOT};

    private int[] getLinks(final int pivot) {
        if (pivot == DUMMY_PIVOT)
            return DUMMY;

        assert pivot >= 0;

        final int[] links = {NOT_INITIALIZED, NOT_INITIALIZED};
        final long[] contractions = fcs.contractions[pivot];
        Indices indices = pc.get(pivot).getIndices();
        int index, toTensorIndex;
        for (int i = contractions.length - 1; i >= 0; --i) {
            index = indices.get(i);

            if (getType(index) != type.getType())
                continue;

            toTensorIndex = getToTensorIndex(contractions[i]);
            int state = 1 - getStateInt(index);

            if (links[state] >= -1 && links[state] != toTensorIndex)
                links[state] = BRANCHING;
            if (links[state] == NOT_INITIALIZED)
                links[state] = toTensorIndex;
        }

        if (links[0] == NOT_INITIALIZED)
            links[0] = NO_LINKS;

        if (links[1] == NOT_INITIALIZED)
            links[1] = NO_LINKS;
        return links;
    }

    private PrimitiveSubgraph processGraph(int pivot) {

        IntArrayList positions = new IntArrayList();
        positions.add(pivot);

        IntArrayList stack = new IntArrayList();
        stack.push(pivot);
        used.set(pivot);

        long[] contractions;
        Indices indices;

        int currentPivot, index, toTensorIndex;
        while (!stack.isEmpty()) {

            currentPivot = stack.pop();

            indices = pc.get(currentPivot).getIndices();
            contractions = fcs.contractions[currentPivot];
            for (int i = contractions.length - 1; i >= 0; --i) {
                index = indices.get(i);
                if (getType(index) != type.getType())
                    continue;

                toTensorIndex = getToTensorIndex(contractions[i]);
                if (toTensorIndex == -1 || used.get(toTensorIndex))
                    continue;
                used.set(toTensorIndex);
                positions.add(toTensorIndex);
                stack.push(toTensorIndex);
            }
        }
        return new PrimitiveSubgraph(GraphType.Graph, positions.toArray());
    }

}
