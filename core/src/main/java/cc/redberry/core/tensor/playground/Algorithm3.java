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

import java.util.Arrays;

import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
import static cc.redberry.core.indices.IndicesUtils.getStateInt;
import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class Algorithm3 extends IAlgorithm {
    public static final Algorithm3 ALGORITHM_3 = new Algorithm3("algorithm3");

    public Algorithm3(String name) {
        super(name);
    }

    @Override
    ProductData calc0(Tensor t) {
        return algorithm3(t);
    }

    static final int REFINEMENT_LEVEL = 2;

    static ProductData algorithm3(final Tensor tensor) {
        if (tensor instanceof Product)
            return algorithm3(((Product) tensor).getContent().getDataCopy(), tensor.getIndices());
        throw new RuntimeException();
    }

    static ProductData algorithm3(final Tensor[] data, final Indices tIndices) {
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

        final short[] stretchIndices = calculateStretchIndices(data);


        //Allocating array for results, one contraction for each tensor
        final long[][] contractions = new long[data.length][];
        final long[] freeContraction = new long[freeIndices.size()];


        calculateInfo(data, freeIndices, info, indices, contractions);
        assert Arrays.equals(indices[0], indices[1]);

        int i;
        final int[] hashCodes = new int[data.length];
        for (i = 0; i < data.length; ++i)
            hashCodes[i] = data[i].hashCode();
        reCalculateContractions(differentIndicesCount, info, freeContraction, contractions);
        refine(hashCodes, contractions, data);

        //Calculating connected components
        int[] components = GraphUtils.calculateConnectedComponents(
                positionsFromInfo(upperInfo), positionsFromInfo(lowerInfo), data.length + 1);

        //<- do additional sort
        final int[] sortedIndices = IndicesUtils.getIndicesNames(freeIndices);
        Arrays.sort(sortedIndices);
        final Wrapper[] wrappers = new Wrapper[data.length];
        for (i = 0; i < data.length; ++i)
            wrappers[i] = new Wrapper(data[i].hashCode(), hashCodes[i], components[i + 1], data[i], sortedIndices);

        ArraysUtils.quickSort(wrappers, data);

        for (i = 0; i < data.length; ++i)
            hashCodes[i] = wrappers[i].graphHash;

        calculateInfo(data, freeIndices, info, indices, contractions);
        reCalculateContractions(differentIndicesCount, info, freeContraction, contractions);
        components = GraphUtils.calculateConnectedComponents(
                positionsFromInfo(upperInfo), positionsFromInfo(lowerInfo), data.length + 1);

        GraphStructure structureOfContractions = new GraphStructure(freeContraction, contractions, components, 1); //todo <- wrong components
        ContentData content = new ContentData(null, structureOfContractions, data, stretchIndices, hashCodes);

        int hashCode = Arrays.hashCode(hashCodes);
        return new ProductData(data, tIndices, content, hashCode);
    }

    static void calculateInfo(final Tensor[] data,
                              final Indices freeIndices,
                              final long[][] info,
                              final int[][] indices,
                              final long[][] contractions) {
        final int[] pointer = new int[2];
        int tensorIndex, state, index, i;
        //Processing free indices = creating contractions for dummy tensor
        for (i = 0; i < freeIndices.size(); ++i) {
            index = freeIndices.get(i);
            state = 1 - getStateInt(index);
            //Important:
            info[state][pointer[state]] = dummyInfo;
            indices[state][pointer[state]++] = getNameWithType(index);
        }

        for (tensorIndex = 0; tensorIndex < data.length; ++tensorIndex) {
            //Main algorithm
            Indices tInds = data[tensorIndex].getIndices();
            short[] diffIds = tInds.getPositionsInOrbits();
            for (i = 0; i < tInds.size(); ++i) {
                index = tInds.get(i);
                state = getStateInt(index);
                info[state][pointer[state]] = info(tensorIndex, diffIds[i], i);
                indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
            }

            //Result allocation
            if (contractions[tensorIndex] == null)
                contractions[tensorIndex] = new long[tInds.size()];
        }

        //Here we can use unstable sorting algorithm (all indices are different)
        ArraysUtils.quickSort(indices[0], info[0]);
        ArraysUtils.quickSort(indices[1], info[1]);
    }

    static void reCalculateContractions(final int differentIndicesCount,
                                        final long[][] info,
                                        final long[] freeContraction,
                                        final long[][] contractions) {
        int fromPosition, freePointer = 0, fromIPosition;
        for (int i = 0; i < differentIndicesCount; ++i) {
            //Contractions from lower to upper
            fromPosition = tPosition(info[0][i]); //From tensor index
            fromIPosition = iPosition(info[0][i]);

            long contraction = contraction(info[0][i], info[1][i]);
            if (fromPosition == -1)
                freeContraction[freePointer++] = contraction;
            else
                contractions[fromPosition][fromIPosition] = contraction;


            //Contractions from upper to lower
            fromPosition = tPosition(info[1][i]); //From tensor index
            fromIPosition = iPosition(info[1][i]);

            contraction = contraction(info[1][i], info[0][i]);
            if (fromPosition == -1)
                freeContraction[freePointer++] = contraction;
            else
                contractions[fromPosition][fromIPosition] = contraction;
        }
    }

    static void refine(final int[] hashCodes, final long[][] contractions, final Tensor[] data) {
        final int[] temp = new int[data.length];
        final int[] newHashCodes = new int[data.length];
        for (int i = 0; i < data.length; ++i) {
            Arrays.fill(temp, 0);
            newHashCodes[i] += refine(temp, REFINEMENT_LEVEL, data, i, contractions, hashCodes, true);
        }
        System.arraycopy(newHashCodes, 0, hashCodes, 0, data.length);
    }

    static int refine(final int[] temp, final int level,
                      final Tensor[] data, final int i,
                      final long[][] contractions,
                      final int[] hashCodes, boolean sum) {
        if (level == 0)
            return 0;
        final int jLevel = JenkinWang32shift(level);
        int vHash = 137;
        boolean freeOnly = true;
        for (long contraction : contractions[i]) {
            int toPosition = toPosition(contraction);
            short diffId = data[i].getIndices().getPositionsInOrbits()[fromIPosition(contraction)];
            if (toPosition == -1)
                vHash += JenkinWang32shift(53 * (diffId + 1) + jLevel);
            else {
                freeOnly = false;
                temp[toPosition] += JenkinWang32shift(level
                        + 17 * hashCodes[i]
                        + 91 * hashCodes[toPosition]
                        + 3671 * (diffId + 1)
                        + 2797 * (toDiffId(contraction) + 1));
                refine(temp, level - 1, data, toPosition, contractions, hashCodes, false);
            }
        }
        if (!sum)
            return 0;
        if (!freeOnly)
            for (int j = 0; j < contractions.length; ++j)
                if (i != j)
                    vHash += JenkinWang32shift(temp[j]);
        return vHash - JenkinWang32shift(temp[i]);
    }

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

    /**
     * Function to pack data to intermediate 64-bit record.
     *
     * @param tPosition index of tensor in the data array (before second
     *                  sorting)
     * @param diffId    id of index in tensor indices list (could be !=0 only
     *                  for simple tensors)
     * @param iPosition index of Index in Indices of tensor ( only 16 bits
     *                  used !!!!!!!!! )
     * @return packed record (long)
     */

    private static long info(final int tPosition, final short diffId, final int iPosition) {
        return (((long) iPosition) << 48) | (((long) tPosition) << 16) | (0xFFFFL & diffId);
    }

    private static final long dummyInfo = info(-1, (short) 0, -1);

    private static int tPosition(final long info) {
        return (int) (0xFFFFFFFFL & (info >> 16));
    }

    private static int iPosition(final long info) {
        return (int) (0xFFFFL & (info >> 48));
    }

    private static short diffId(final long info) {
        return (short) (0xFFFFL & info);
    }

    private static int[] positionsFromInfo(final long[] info) {
        final int[] result = new int[info.length];
        for (int i = 0; i < info.length; ++i)
            result[i] = tPosition(info[i]) + 1;
        return result;
    }

    private static long contraction(final int fromIPosition, final int toIDiffId, final int toTPosition) {
        return (((long) toTPosition) << 32) | (0xFFFF0000L & (toIDiffId << 16)) | (0xFFFFL & fromIPosition);
    }

    private static long contraction(long lInfo, long uInfo) {
        return (0xFFFFFFFFFFFF0000L & (uInfo << 16)) | (0xFFFFL & (lInfo));
    }

    private static long hashedContraction(short fromDiffId, short toStretch, short toDiffId) {
        return (0x0000FFFF00000000L & (((long) fromDiffId) << 32)) | (0xFFFF0000L & (toStretch << 16)) | (0xFFFFL & toDiffId);
    }

    static int toPosition(final long contraction) {
        return (int) (contraction >> 32);
    }

    static short toDiffId(final long contraction) {
        return (short) (0xFFFF & (contraction >> 16));
    }

    static int fromIPosition(final long contraction) {
        return (int) (0xFFFFL & contraction);
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
        final int tensorHash;
        final int graphHash;
        final int indicesHash;
        final int graphComponent;

        private Wrapper(int tensorHash, int graphHash, int graphComponent, Tensor t, int[] indices) {
            this.tensorHash = tensorHash;
            this.graphHash = graphHash;
            this.graphComponent = graphComponent;
            this.indicesHash = hc(t, indices);
        }

        @Override
        public int compareTo(final Wrapper o) {
            int c = Integer.compare(tensorHash, o.tensorHash);
            if (c == 0)
                c = Integer.compare(graphHash, o.graphHash);
            if (c == 0)
                c = Integer.compare(indicesHash, o.indicesHash);
            if (c == 0)
                c = Integer.compare(graphComponent, o.graphComponent);
            return c;
        }
    }
}
