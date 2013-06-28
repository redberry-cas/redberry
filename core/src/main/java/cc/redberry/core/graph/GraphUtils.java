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

import cc.redberry.core.utils.ArraysUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * This class implements useful graph algorithms.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class GraphUtils {
    /**
     * Calculates connected components of the graph from its list of edges.
     * The list of edges is provided as two arrays ({@code _from}, which contains 'from' vertices
     * indices, and array {@code _to}, which contains vertices indices connected with corresponding
     * 'from' vertices). Vertices are indexed from zero to {@code vertexes - 1}, where
     * {@code vertexes} is the total number of vertices of the graph. All indices
     * in arrays {@code _from} and {@code _to} should be in the range [ 0 .. (vertices -1) ].
     *
     * <p>The resulting array have length equal to the total number of vertices plus one. Two vertices A and B
     * belongs to the same connected component if the resulting array have equal values at positions
     * A and B. The last element in the resulting array represents the total number of connected components.
     *
     * @param _from    array of 'from' vertices
     * @param _to      array of 'to' vertices
     * @param vertices total number of vertices in graph
     * @return array of connected components and the total number of connected components at the last position
     * @throws IllegalArgumentException if {@code _from.length() != _to.length}
     * @throws IllegalArgumentException if any element of {@code _from} or {@code _to} equal or
     *                                  greater then {@code vertices}
     * @since 1.0
     */
    public static int[] calculateConnectedComponents(final int[] _from, final int[] _to, final int vertices) {

        //Test for parameters consistence
        if (_from.length != _to.length)
            throw new IllegalArgumentException();

        //No edges case
        if (_from.length == 0) {
            int[] result = new int[vertices + 1];
            for (int i = 0; i < vertices + 1; ++i)
                result[i] = i;
            return result;
        }

        //Creating sorted union
        final int[] from = new int[_from.length << 1];
        final int[] to = new int[_from.length << 1];
        System.arraycopy(_from, 0, from, 0, _from.length);
        System.arraycopy(_to, 0, to, 0, _from.length);
        System.arraycopy(_from, 0, to, _from.length, _from.length);
        System.arraycopy(_to, 0, from, _from.length, _from.length);

        //Sorting to easy indexing by from 
        ArraysUtils.quickSort(from, to);

        //Test for parameters consistence
        if (from[0] < 0 || from[from.length - 1] > vertices)
            throw new IllegalArgumentException();

        //Creating index for fast search in from array
        int[] fromIndex = new int[vertices];
        Arrays.fill(fromIndex, -1); //-1 in fromIndex means absence of edges for certain vertex
        int lastVertex = -1;
        for (int i = 0; i < from.length; ++i)
            if (lastVertex != from[i])
                fromIndex[lastVertex = from[i]] = i;

        //Allocation resulting array
        final int[] components = new int[vertices + 1];
        Arrays.fill(components, -1); //There will be no -1 at the end

        int currentComponent = -1;
        int m1 = 0;
        Deque<BreadthFirstPointer> stack = new ArrayDeque<>();
        do {
            ++currentComponent;
            components[m1] = currentComponent;

            if (fromIndex[m1] == -1)
                //There is no edges for curreent vertex,
                //so it is connected component by it self
                continue;

            //Pushing seed vertex to stack
            stack.push(new BreadthFirstPointer(m1, fromIndex[m1]));


            //Main algorithm (simple depth-first search)
            while (!stack.isEmpty()) {
                BreadthFirstPointer pointer = stack.peek();

                if (pointer.edgePointer >= from.length || from[pointer.edgePointer++] != pointer.vertex) {
                    //There are no more edges from this vertex => delete it from stack and continue
                    stack.pop();
                    continue;
                }

                // -1 because pointer.edgePointer++ was invoked
                int pointsTo = to[pointer.edgePointer - 1];

                if (components[pointsTo] == currentComponent)
                    //We've been here earlier, continue
                    continue;

                assert components[pointsTo] == -1;

                //Marking current vertex by current connected component index
                components[pointsTo] = currentComponent;

                if (fromIndex[pointsTo] != -1)
                    //No edges from this vertex
                    stack.push(new BreadthFirstPointer(pointsTo, fromIndex[pointsTo]));
            }
        } while ((m1 = firstM1(components)) != vertices);

        //writing components count
        components[vertices] = currentComponent + 1;
        return components;
    }

    /**
     * Finds first "-1" in the array
     */
    private static int firstM1(int[] arr) {
        for (int i = 0; i < arr.length; ++i)
            if (arr[i] == -1)
                return i;
        return -1;
    }

    private static final class BreadthFirstPointer {
        final int vertex;
        int edgePointer;

        public BreadthFirstPointer(int node, int edgePointer) {
            this.vertex = node;
            this.edgePointer = edgePointer;
        }
    }

    /**
     * Returns the number of vertices belonging to the connected component containing specified {@code vertex}.
     *
     * @param vertex     vertex of the graph
     * @param components the array, produced by {@link #calculateConnectedComponents(int[], int[], int)}
     * @return number of vertices belonging to the same connected component as specified {@code vertex}
     * @since 1.0
     */
    public static int componentSize(final int vertex, final int[] components) {
        if (vertex > components.length - 1)
            throw new IndexOutOfBoundsException();
        int componentCount = components[vertex];
        int count = 0;
        for (int i = 0; i < components.length; ++i)
            if (components[i] == componentCount)
                ++count;
        return count;
    }
}
