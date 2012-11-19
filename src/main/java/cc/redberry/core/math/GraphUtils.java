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
package cc.redberry.core.math;

import cc.redberry.core.utils.ArraysUtils;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class GraphUtils {
    /**
     * Calculate connected components for the graph described by edges list.
     * Each edge defined by corresponding elements in _from and _to
     * arrays.<br/><br/> All vertex indices must belongs to [0 : vertexes-1]
     * range, instead IllegalArgumentException will be thrown. The last element
     * in resulting the resulting array is connected components count.
     *
     * @param _from array of edges begins (vertex indices)
     * @param _to array of edges ends (vertex indices)
     * @param vertexes number of vertexes in the graph
     * @return the last element in resulting array is connected components count
     * @throws IllegalArgumentException
     */
    //FIXME bad documentation
    public static int[] calculateConnectedComponents(final int[] _from, final int[] _to, final int vertexes) {
        int i;

        //Test for parameters consistence
        if (_from.length != _to.length)
            throw new IllegalArgumentException();

        //No edges case
        if (_from.length == 0) {
            int[] result = new int[vertexes + 1];
            for (i = 0; i < vertexes + 1; ++i)
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
        if (from[0] < 0 || from[from.length - 1] > vertexes)
            throw new IllegalArgumentException();

        //Creating index for fast search in from array
        int[] fromIndex = new int[vertexes];
        Arrays.fill(fromIndex, -1); //-1 in fromIndex means absance of edges for certain vertex
        int lastVertex = -1;
        for (i = 0; i < from.length; ++i)
            if (lastVertex != from[i])
                fromIndex[lastVertex = from[i]] = i;

        //Allocation resulting array
        final int[] components = new int[vertexes + 1];
        Arrays.fill(components, -1); //There will be no -1 at the end

        int currentComponent = -1;
        int m1 = 0;
        Deque<BreakfastPointer> stack = new ArrayDeque<>();
        do {
            ++currentComponent;
            components[m1] = currentComponent;
            
            if (fromIndex[m1] == -1)
                //There is no edges for curreent vertex,
                //so it is connected component by it self
                continue;

            //Pushing seed vertex to stack
            stack.push(new BreakfastPointer(m1, fromIndex[m1]));


            //Main algorithm (simple depth-first search)
            while (!stack.isEmpty()) {
                BreakfastPointer pointer = stack.peek();
                
                if (pointer.edgePointer >= from.length || from[pointer.edgePointer++] != pointer.vertex) {
                    //There are no more edges from this vertex => delete it from stack and continue
                    stack.pop();
                    continue;
                }

                // -1 because pointer.edgePointer++ was invoked
                int pointsTo = to[pointer.edgePointer - 1];
                
                if (components[pointsTo] == currentComponent)
                    //We've been here erlier, continue
                    continue;
                
                assert components[pointsTo] == -1;

                //Marking current vertex by current connected component index
                components[pointsTo] = currentComponent;
                
                if (fromIndex[pointsTo] != -1)
                    //No edges from this vertex
                    stack.push(new BreakfastPointer(pointsTo, fromIndex[pointsTo]));
            }
        } while ((m1 = firstM1(components)) != vertexes);

        //writing components count
        components[vertexes] = currentComponent + 1;
        return components;
    }

    /**
     * First "-1" in the array
     */
    private static int firstM1(int[] arr) {
        for (int i = 0; i < arr.length; ++i)
            if (arr[i] == -1)
                return i;
        return -1;
    }
    
    private static final class BreakfastPointer {
        final int vertex;
        int edgePointer;
        
        public BreakfastPointer(int node, int edgePointer) {
            this.vertex = node;
            this.edgePointer = edgePointer;
        }
    }

    //TODO comment
    public static int componentSize(final int vertexPosition, final int[] components) {
        if (vertexPosition > components.length - 1)
            throw new IndexOutOfBoundsException();
        int componentCount = components[vertexPosition];
        int count = 0;
        for (int i = 0; i < components.length; ++i)
            if (components[i] == componentCount)
                ++count;
        return count;
    }
}
