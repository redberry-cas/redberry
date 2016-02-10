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
package cc.redberry.core.tensor.playground;

import cc.redberry.core.graph.GraphUtils;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.tensor.Product;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.HashFunctions;

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
import static cc.redberry.core.indices.IndicesUtils.getStateInt;
import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Algorithm0 {
    static ProductData algorithm0(final Tensor tensor) {
        if (tensor instanceof Product)
            return algorithm0(((Product) tensor).getContent().getDataCopy(), tensor.getIndices());
        throw new RuntimeException();
    }

    static ProductData algorithm0(final Tensor[] data, final Indices tIndices) {
        //<- Important!
        Arrays.sort(data);

        final Indices freeIndices = tIndices.getFree();
        final int differentIndicesCount = (tIndices.size() + freeIndices.size()) / 2;

        final int[]
                upperIndices = new int[differentIndicesCount],
                lowerIndices = new int[differentIndicesCount];

        final long[]
                upperInfo = new long[differentIndicesCount],
                lowerInfo = new long[differentIndicesCount];

        final int[][] indices = new int[][]{lowerIndices, upperIndices};
        final long[][] info = new long[][]{lowerInfo, upperInfo};

        final int[] pointer = new int[2];
        final short[] stretchIndices = calculateStretchIndices(data);


        int state, index, i;
        //Processing free indices = creating contractions for dummy tensor
        for (i = 0; i < freeIndices.size(); ++i) {
            index = freeIndices.get(i);
            state = 1 - getStateInt(index);
            //Important:
            info[state][pointer[state]] = dummyTensorInfo;
            indices[state][pointer[state]++] = getNameWithType(index);
        }

        //Allocating array for results, one contraction for each tensor
        final long[][] contractions = new long[data.length][];
        final long[] freeContraction = new long[freeIndices.size()];

        int tensorIndex;
        for (tensorIndex = 0; tensorIndex < data.length; ++tensorIndex) {
            //Main algorithm
            Indices tInds = data[tensorIndex].getIndices();
            short[] diffIds = tInds.getPositionsInOrbits();
            for (i = 0; i < tInds.size(); ++i) {
                index = tInds.get(i);
                state = getStateInt(index);
                info[state][pointer[state]] = packToLong(tensorIndex, stretchIndices[tensorIndex], diffIds[i]);
                indices[state][pointer[state]++] = getNameWithType(index);
            }

            //Result allocation
            contractions[tensorIndex] = new long[tInds.size()];
        }

        //Here we can use unstable sorting algorithm (all indices are different)
        ArraysUtils.quickSort(indices[0], info[0]);
        ArraysUtils.quickSort(indices[1], info[1]);

        //<-- Here we have mature info arrays

        //Creating input graph components
        final int[] components = GraphUtils.calculateConnectedComponents(
                infoToTensorIndices(upperInfo), infoToTensorIndices(lowerInfo), data.length + 1);

        //assert Arrays.equals(indices[0], indices[1]);
        assert Arrays.equals(indices[0], indices[1]);

        final int[] pointers = new int[data.length];
        int freePointer = 0;
        for (i = 0; i < differentIndicesCount; ++i) {
            //Contractions from lower to upper
            tensorIndex = (int) (info[0][i] >> 32);
            long contraction = (0x0000FFFF00000000L & (info[0][i] << 32)) | (0xFFFFFFFFL & (info[1][i]));
            if (tensorIndex == -1)
                freeContraction[freePointer++] = contraction;
            else
                contractions[tensorIndex][pointers[tensorIndex]++] = contraction;

            //Contractions from upper to lower
            tensorIndex = (int) (info[1][i] >> 32);
            contraction = (0x0000FFFF00000000L & (info[1][i] << 32)) | (0xFFFFFFFFL & (info[0][i]));
            if (tensorIndex == -1)
                freeContraction[freePointer++] = contraction;
            else
                contractions[tensorIndex][pointers[tensorIndex]++] = contraction;
        }

        //Sorting per-index contractions in each TensorContraction
        for (long[] contraction : contractions)
            Arrays.sort(contraction);
        Arrays.sort(freeContraction);

        final int[] hashCodes = new int[data.length];
        for (i = 0; i < data.length; ++i)
            hashCodes[i] = data[i].hashCode() + 91 * contractionsHashCode(contractions[i]);

        //<- do additional sort
        final int[] sortedIndices = IndicesUtils.getIndicesNames(freeIndices);
        Arrays.sort(sortedIndices);
        Wrapper[] wrappers = new Wrapper[data.length];
        for (i = 0; i < data.length; ++i)
            wrappers[i] = new Wrapper(hashCodes[i], components[i + 1], data[i], sortedIndices, contractions[i]);

        ArraysUtils.quickSort(wrappers, data);

        for (i = 0; i < data.length; ++i) {
            contractions[i] = wrappers[i].contractions;
            hashCodes[i] = wrappers[i].graphHash;
        }

        GraphStructureHashed structureOfContractionsHashed = new GraphStructureHashed(stretchIndices, freeContraction, contractions);
        GraphStructure structureOfContractions = new GraphStructure(data, differentIndicesCount, freeIndices);

        ContentData content = new ContentData(structureOfContractionsHashed, structureOfContractions, data, stretchIndices, hashCodes);

        int hashCode = Arrays.hashCode(hashCodes);
        hashCode = 7 * hashCode + contractionsHashCode(freeContraction);
        hashCode = 7 * hashCode + contractionsHashCode(contractions);
        return new ProductData(data, tIndices, content, hashCode);
    }

    static int contractionsHashCode(long[][] contractions) {
        int result = 1;
        for (long[] element : contractions)
            result = 31 * result + Arrays.hashCode(element);
        return result;
    }

    static int contractionsHashCode(long[] indexContractions) {
        long hash = 1L;
        for (long l : indexContractions)
            hash ^= 7 * hash + HashFunctions.JenkinWang64shift(l);
        return HashFunctions.Wang64to32shift(hash);
    }

    static final long dummyTensorInfo = -65536;

    private static short[] calculateStretchIndices(final Tensor[] data) {
        final short[] stretchIndex = new short[data.length];
        //stretchIndex[0] = 0;
        short index = 0;
        int oldHash = data[0].hashCode();
        for (int i = 1; i < data.length; ++i)
            if (oldHash == data[i].hashCode())
                stretchIndex[i] = index;
            else {
                stretchIndex[i] = ++index;
                oldHash = data[i].hashCode();
            }

        return stretchIndex;
    }

    private static long packToLong(final int tensorIndex, final short stretchIndex, final short id) {
        return (((long) tensorIndex) << 32) | (0xFFFF0000L & (stretchIndex << 16)) | (0xFFFFL & id);
    }

    private static int[] infoToTensorIndices(final long[] info) {
        final int[] result = new int[info.length];
        for (int i = 0; i < info.length; ++i)
            result[i] = ((int) (info[i] >> 32)) + 1;
        return result;
    }


    private static int hc(Tensor t, int[] inds) {
        Indices ind = t.getIndices().getFree();
        int h = 31;
        int ii;
        for (int i = ind.size() - 1; i >= 0; --i) {
            ii = getNameWithType(ind.get(i));
            if ((ii = Arrays.binarySearch(inds, ii)) >= 0)
                h ^= JenkinWang32shift(ii);
        }
        return h;
    }

    private static class Wrapper implements Comparable<Wrapper> {
        final int graphHash;
        final int indicesHash;
        final int graphComponent;
        final long[] contractions;

        private Wrapper(int graphHash, int graphComponent, Tensor t, int[] indices, long[] contractions) {
            this.graphHash = graphHash;
            this.graphComponent = graphComponent;
            this.indicesHash = hc(t, indices);
            this.contractions = contractions;
        }

        @Override
        public int compareTo(final Wrapper o) {
            int c = Integer.compare(graphHash, o.graphHash);
            if (c == 0)
                c = Integer.compare(indicesHash, o.indicesHash);
            if (c == 0)
                c = Integer.compare(graphComponent, o.graphComponent);
            return c;
        }
    }
}
