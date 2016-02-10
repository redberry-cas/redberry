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
import cc.redberry.core.tensor.ProductContent;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.HashingStrategy;

import java.util.Arrays;

import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * This class represents the structure of product contraction.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class GraphStructure {
    public final static GraphStructure EMPTY_FULL_CONTRACTIONS_STRUCTURE = new GraphStructure(new long[0], new long[0][], new int[0], 0);
    public final long[] freeContractions;
    public final long[][] contractions;
    public final int[] components;
    public final int componentCount;

    GraphStructure(long[] freeContractions, long[][] contractions, int[] components, int componentCount) {
        this.freeContractions = freeContractions;
        this.contractions = contractions;
        this.components = components;
        this.componentCount = componentCount;
    }

    public GraphStructure(final Tensor[] data, final int differentIndicesCount, final Indices freeIndices) {
        //Names (names with type, see IndicesUtils.getNameWithType() ) of all indices in this multiplication
        //It will be used as index name -> index index [0,1,2,3...] mapping
        final int[] upperIndices = new int[differentIndicesCount], lowerIndices = new int[differentIndicesCount];
        //This is storage for intermediate information about indices, used in the algorithm (see below)
        //Structure:
        //
        final long[] upperInfo = new long[differentIndicesCount], lowerInfo = new long[differentIndicesCount];

        //This is for generalization of algorithm
        //indices[0] == lowerIndices
        //indices[1] == upperIndices
        final int[][] indices = new int[][]{lowerIndices, upperIndices};

        //This is for generalization of algorithm too
        //info[0] == lowerInfo
        //info[1] == upperInfo
        final long[][] info = new long[][]{lowerInfo, upperInfo};

        //Pointers for lower and upper indices, used in algorithm
        //pointer[0] - pointer to lower
        //pointer[1] - pointer to upper
        final int[] pointer = new int[2];

        //Allocating array for results, one contraction for each tensor
        contractions = new long[data.length][];
        //There is one dummy tensor with index -1, it represents fake
        //tensor contracting with whole Product to leave no contracted indices.
        //So, all "contractions" with this dummy "contraction" looks like a scalar
        //product.
        freeContractions = new long[freeIndices.size()];

        int state, index, i;

        //Processing free indices = creating contractions for dummy tensor
        for (i = 0; i < freeIndices.size(); ++i) {
            index = freeIndices.get(i);
            //Inverse state (because it is state of index at (??) dummy tensor,
            //contracted with this free index)
            state = 1 - IndicesUtils.getStateInt(index);

            //Important:
            info[state][pointer[state]] = dummyTensorInfo;
            indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
        }

        int tensorIndex;
        for (tensorIndex = 0; tensorIndex < data.length; ++tensorIndex) {
            //Main algorithm
            Indices tInds = data[tensorIndex].getIndices();
            short[] diffIds = tInds.getPositionsInOrbits();

            //FUTURE move to other place
            if (tInds.size() >= 0x10000)
                throw new RuntimeException("Too many indices!!! max count = 2^16");

            for (i = 0; i < tInds.size(); ++i) {
                index = tInds.get(i);
                state = IndicesUtils.getStateInt(index);
                info[state][pointer[state]] = packToLong(tensorIndex, diffIds[i], i);
                indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
            }

            //Result allocation
            contractions[tensorIndex] = new long[tInds.size()];
        }

        //Here we can use unstable sorting algorithm (all indices are different)
        ArraysUtils.quickSort(indices[0], info[0]);
        ArraysUtils.quickSort(indices[1], info[1]);

        //Calculating connected components
        int[] infoTensorIndicesFrom = infoToTensorIndices(lowerInfo);
        int[] infoTensorIndicesTo = infoToTensorIndices(upperInfo);

        int shift = 0, last = 0;
        for (i = 0; i < infoTensorIndicesFrom.length; ++i)
            if (infoTensorIndicesFrom[i] == -1
                    || infoTensorIndicesTo[i] == -1) {
                System.arraycopy(infoTensorIndicesFrom, last, infoTensorIndicesFrom, last - shift, i - last);
                System.arraycopy(infoTensorIndicesTo, last, infoTensorIndicesTo, last - shift, i - last);
                last = i + 1;
                ++shift;
            }
        System.arraycopy(infoTensorIndicesFrom, last, infoTensorIndicesFrom, last - shift, i - last);
        System.arraycopy(infoTensorIndicesTo, last, infoTensorIndicesTo, last - shift, i - last);
        infoTensorIndicesFrom = Arrays.copyOf(infoTensorIndicesFrom, infoTensorIndicesFrom.length - shift);
        infoTensorIndicesTo = Arrays.copyOf(infoTensorIndicesTo, infoTensorIndicesTo.length - shift);

        int[] components = GraphUtils.calculateConnectedComponents(infoTensorIndicesFrom, infoTensorIndicesTo, data.length);
        componentCount = components[components.length - 1];
        this.components = Arrays.copyOfRange(components, 0, components.length - 1);
        //<-- Here we have mature info arrays

        assert Arrays.equals(indices[0], indices[1]);

        int freePointer = 0;
        int indexIndex;
        for (i = 0; i < differentIndicesCount; ++i) {
            //Contractions from lower to upper
            tensorIndex = (int) (0xFFFFFFFFL & (info[0][i] >> 16)); //From tensor index
            indexIndex = (int) (0xFFFFL & (info[0][i] >> 48));
            long contraction = (0xFFFFFFFFFFFF0000L & (info[1][i] << 16))
                    | (0xFFFFL & (info[0][i]));
            if (tensorIndex == -1)
                freeContractions[freePointer++] = contraction;
            else
                contractions[tensorIndex][indexIndex] = contraction;

            //Contractions from upper to lower
            tensorIndex = (int) (0xFFFFFFFFL & (info[1][i] >> 16)); //From tensor index
            indexIndex = (int) (0xFFFFL & (info[1][i] >> 48));
            contraction = (0xFFFFFFFFFFFF0000L & (info[0][i] << 16))
                    | (0xFFFFL & (info[1][i]));
            if (tensorIndex == -1)
                freeContractions[freePointer++] = contraction;
            else
                contractions[tensorIndex][indexIndex] = contraction;
        }
    }

    public int graphHashCode(final Tensor[] data) {
        int hash = 0;
        final int[] temp = new int[contractions.length];
        for (int i = 0; i < contractions.length; ++i) {
            Arrays.fill(temp, 0);
            int tHash = 137;
            for (long contraction : contractions[i]) {
                int to = getToTensorIndex(contraction);
                if (to == -1)
                    tHash += 53 * (data[i].getIndices().getPositionsInOrbits()[getFromIndexId(contraction)] + 1);
                else
                    temp[to] += JenkinWang32shift(17 * data[i].hashCode() + 91 * data[to].hashCode()
                            + 3671 * (data[i].getIndices().getPositionsInOrbits()[getFromIndexId(contraction)] + 1)
                            + 2797 * (data[to].getIndices().getPositionsInOrbits()[getToIndexId(contraction)] + 1));
            }
            //todo: if all indices of -ith are free -> no need to iterate
            for (int j = 0; j < contractions.length; ++j)
                if (i != j)
                    tHash += JenkinWang32shift(temp[j]);
            hash += 17 * tHash - JenkinWang32shift(temp[i]);
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contractions.length; i++) {
            for (long l : contractions[i]) {
                sb.append(i).append("_").append(getFromIndexId(l)).append(" -> ").append(getToTensorIndex(l)).append("_").append(getToIndexId(l));
                sb.append("\n");
            }

        }
        return sb.toString();
    }

    //    public int graphHashCodeWithIndices(final ProductContent pc, final int[] sortedIndices) {
//        final Tensor[] data = pc.getDataReference();
//        if (data.length == 1)
//            return HashingStrategy._hashWithIndices(data[0], sortedIndices);
//        final int[] hashOfIndices = new int[data.length];
//        int hash = 0;
//        for (int i = 0; i < data.length; i++)
//            hashOfIndices[i] = JenkinWang32shift(pc.getStretchId(i)) * HashingStrategy._hashWithIndices(data[i], sortedIndices);
//
//        final int[] temp = new int[contractions.length];
//        for (int i = 0; i < contractions.length; ++i) {
//            Arrays.fill(temp, 0);
//            int tHash = 137;
//            for (long contraction : contractions[i]) {
//                final int to = getToTensorIndex(contraction);
//                final short fromIndexId = getFromIndexId(contraction);
//                if (to == -1)
//                    tHash += 17 * hashOfIndices[i] + 53 * (data[i].getIndices().getPositionsInOrbits()[fromIndexId] + 1);
//                else {
//                    temp[to] += JenkinWang32shift(17 * hashOfIndices[i]
//                                    + 91 * hashOfIndices[to]
//                                    + 3671 * (data[i].getIndices().getPositionsInOrbits()[fromIndexId] + 1)
//                                    + 2797 * (data[to].getIndices().getPositionsInOrbits()[getToIndexId(contraction)] + 1)
//                    );
//                }
//            }
//            //todo: if all indices of -ith are free -> no need to iterate
//            for (int j = 0; j < contractions.length; ++j)
//                if (i != j)
//                    tHash += JenkinWang32shift(temp[j]);
//            hash += 17 * tHash - JenkinWang32shift(temp[i]);
//        }
//        return hash;
//    }
//
//    private int indicesHash(Tensor[] data, int i, int[] sortedIndices) {
//        int tHash = 0;
//        for (long contraction : contractions[i]) {
//            final int to = getToTensorIndex(contraction);
//            if (to != -1)
//                continue;
//            final short fromIndexId = getFromIndexId(contraction);
//            int ind = Arrays.binarySearch(sortedIndices, data[i].getIndices().get(fromIndexId));
//            if (ind < 0)
//                continue;
//            tHash += TensorHashCalculator._hashWithIndices(data[i], sortedIndices) + 97 * ind + 193 * sortedIndices[ind];
//        }
////        System.out.println(tHash);
//        return tHash;
//    }


    public static int getToTensorIndex(final long contraction) {
        return (int) (contraction >> 32);
    }

    public static short getToIndexId(final long contraction) {
        return (short) (0xFFFF & (contraction >> 16));
    }

    public static short getFromIndexId(final long contraction) {
        return (short) (0xFFFF & contraction);
    }

    /**
     * Function to pack data to intermediate 64-bit record.
     *
     * @param tensorIndex index of tensor in the data array (before second
     *                    sorting)
     * @param id          id of index in tensor indices list (could be !=0 only
     *                    for simple tensors)
     * @param indexIndex  index of Index in Indices of tensor ( only 16 bits
     *                    used !!!!!!!!! )
     * @return packed record (long)
     */
    private static long packToLong(final int tensorIndex, final short id, final int indexIndex) {
        return (((long) tensorIndex) << 16) | (0xFFFFL & id) | (((long) indexIndex) << 48);
    }

    //0xFFFFFFFF00000000L == packToLong(-1, (short) 0, -1);
    private static final long dummyTensorInfo = 0xFFFFFFFFFFFF0000L;

    private static int[] infoToTensorIndices(final long[] info) {
        final int[] result = new int[info.length];
        for (int i = 0; i < info.length; ++i)
            result[i] = ((int) (0xFFFFFFFFL & (info[i] >> 16)));
        return result;
    }
}
