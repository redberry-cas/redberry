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
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.HashFunctions;

import java.util.Arrays;

import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Algorithm1 {
//    static ProductData algorithm1(final Tensor tensor) {
//        if (tensor instanceof Product)
//            return algorithm1(((Product) tensor).getContent().getDataCopy(), tensor.getIndices());
//        throw new RuntimeException();
//    }
//
//    static ProductData algorithm1(final Tensor[] data, final Indices tIndices) {
//        final Indices freeIndices = tIndices.getFree();
//        final int differentIndicesCount = (tIndices.size() + freeIndices.size()) / 2;
//
//        final int[]
//                upperIndices = new int[differentIndicesCount],
//                lowerIndices = new int[differentIndicesCount];
//
//        final long[]
//                upperInfo = new long[differentIndicesCount],
//                lowerInfo = new long[differentIndicesCount];
//
//        final int[][] indices = new int[][]{lowerIndices, upperIndices};
//        final long[][] info = new long[][]{lowerInfo, upperInfo};
//
//        final int[] pointer = new int[2];
//        final short[] stretchIndices = calculateStretchIndices(data);
//
//
//        int state, index, i;
//        //Processing free indices = creating contractions for dummy tensor
//        for (i = 0; i < freeIndices.size(); ++i) {
//            index = freeIndices.get(i);
//            state = 1 - IndicesUtils.getStateInt(index);
//            //Important:
//            info[state][pointer[state]] = dummyTensorInfo;
//            indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
//        }
//
//        //Allocating array for results, one contraction for each tensor
//        final long[][] contractions = new long[data.length][];
//        final long[] freeContraction = new long[freeIndices.size()];
//
//        int tensorIndex;
//        for (tensorIndex = 0; tensorIndex < data.length; ++tensorIndex) {
//            //Main algorithm
//            Indices tInds = data[tensorIndex].getIndices();
//            short[] diffIds = tInds.getPositionsInOrbits();
//            for (i = 0; i < tInds.size(); ++i) {
//                index = tInds.get(i);
//                state = IndicesUtils.getStateInt(index);
//                info[state][pointer[state]] = packToLong(tensorIndex, stretchIndices[tensorIndex], diffIds[i]);
//                indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
//            }
//
//            //Result allocation
//            contractions[tensorIndex] = new long[tInds.size()];
//        }
//
//        //Here we can use unstable sorting algorithm (all indices are different)
//        ArraysUtils.quickSort(indices[0], info[0]);
//        ArraysUtils.quickSort(indices[1], info[1]);
//
//        //<-- Here we have mature info arrays
//
//        //Creating input graph components
//        final int[] components = GraphUtils.calculateConnectedComponents(
//                infoToTensorIndices(upperInfo), infoToTensorIndices(lowerInfo), data.length + 1);
//
//        //assert Arrays.equals(indices[0], indices[1]);
//        assert Arrays.equals(indices[0], indices[1]);
//
//        final int[] pointers = new int[data.length];
//        int freePointer = 0;
//        for (i = 0; i < differentIndicesCount; ++i) {
//            //Contractions from lower to upper
//            tensorIndex = (int) (info[0][i] >> 32);
//            long contraction = (0x0000FFFF00000000L & (info[0][i] << 32))
//                    | (0xFFFFFFFFL & (info[1][i]));
//            if (tensorIndex == -1)
//                freeContraction[freePointer++] = contraction;
//            else
//                contractions[tensorIndex][pointers[tensorIndex]++] = contraction;
//
//            //Contractions from upper to lower
//            tensorIndex = (int) (info[1][i] >> 32);
//            contraction = (0x0000FFFF00000000L & (info[1][i] << 32))
//                    | (0xFFFFFFFFL & (info[0][i]));
//            if (tensorIndex == -1)
//                freeContraction[freePointer++] = contraction;
//            else
//                contractions[tensorIndex][pointers[tensorIndex]++] = contraction;
//        }
//
//        //Sorting per-index contractions in each TensorContraction
//        for (long[] contraction : contractions)
//            Arrays.sort(contraction);
//        Arrays.sort(freeContraction);
//
//        GraphStructure structureOfContractions = new GraphStructure(data, differentIndicesCount, freeIndices);
//
//        //<- do additional sort
//        final int[] sortedIndices = IndicesUtils.getIndicesNames(freeIndices);
//        Arrays.sort(sortedIndices);
//        ScaffoldWrapper[] wrappers = new ScaffoldWrapper[contractions.length];
//        for (i = 0; i < contractions.length; ++i)
//            wrappers[i] = new ScaffoldWrapper(sortedIndices, components[i + 1], data[i], stretchIndices[i], contractions[i]);
//
//        ArraysUtils.quickSort(wrappers, data);
//
//        for (i = 0; i < contractions.length; ++i)
//            contractions[i] = wrappers[i].contractions;
//
//        GraphStructureHashed structureOfContractionsHashed = new GraphStructureHashed(stretchIndices, freeContraction, contractions);
//
//        ContentData content = new ContentData(structureOfContractionsHashed, structureOfContractions, data, stretchIndices);
//        int hashCode = 0;
//        for (i = 0; i < data.length; i++)
//            hashCode = hashCode * 17 + JenkinWang32shift(data[i].hashCode() + 91 * hashCode(contractions[i]));
//        hashCode -= 19 * hashCode(freeContraction);
//        return new ProductData(data, tIndices, content, hashCode);
//    }
//
//    static int hashCode(long[] indexContractions) {
//        long hash = 1L;
//        for (long l : indexContractions)
//            hash ^= HashFunctions.JenkinWang64shift(l);
//        return HashFunctions.Wang64to32shift(hash);
//
//    }
//
//    static final long dummyTensorInfo = -65536;
//
//    private static short[] calculateStretchIndices(final Tensor[] data) {
//        final short[] stretchIndex = new short[data.length];
//        //stretchIndex[0] = 0;
//        short index = 0;
//        int oldHash = data[0].hashCode();
//        for (int i = 1; i < data.length; ++i)
//            if (oldHash == data[i].hashCode())
//                stretchIndex[i] = index;
//            else {
//                stretchIndex[i] = ++index;
//                oldHash = data[i].hashCode();
//            }
//
//        return stretchIndex;
//    }
//
//    private static long packToLong(final int tensorIndex, final short stretchIndex, final short id) {
//        return (((long) tensorIndex) << 32) | (0xFFFF0000L & (stretchIndex << 16)) | (0xFFFFL & id);
//    }
//
//    private static int[] infoToTensorIndices(final long[] info) {
//        final int[] result = new int[info.length];
//        for (int i = 0; i < info.length; ++i)
//            result[i] = ((int) (info[i] >> 32)) + 1;
//        return result;
//    }
//
//
//    private static int hc(Tensor t, int[] inds) {
//        Indices ind = t.getIndices().getFree();
//        int h = 31;
//        int ii;
//        for (int i = ind.size() - 1; i >= 0; --i) {
//            ii = IndicesUtils.getNameWithType(ind.get(i));
//            if ((ii = Arrays.binarySearch(inds, ii)) >= 0)
//                h ^= JenkinWang32shift(ii);
//        }
//        return h;
//    }
//
//    private static class ScaffoldWrapper implements Comparable<ScaffoldWrapper> {
//        final int[] indices;
//        final int component;
//        final Tensor t;
//        final long[] contractions;
//        final int hashWithIndices;
//        final short stretchId;
//
//        private ScaffoldWrapper(int[] indices, int component, Tensor t, short stretchId, long[] contractions) {
//            this.indices = indices;
//            this.t = t;
//            this.contractions = contractions;
//            this.component = component;
//            this.stretchId = stretchId;
//            hashWithIndices = hc(t, indices);
//        }
//
//        @Override
//        public int compareTo(ScaffoldWrapper o) {
//            int r = compareContractions(o);
//            if (r != 0)
//                return r;
//            if ((r = Integer.compare(hashWithIndices, o.hashWithIndices)) != 0)
//                return r;
//            return Integer.compare(component, o.component);
//        }
//
//        int compareContractions(ScaffoldWrapper o) {
//            int val;
//            if ((val = Integer.compare(stretchId, o.stretchId)) != 0)
//                return val;
//            if ((val = Integer.compare(contractions.length, o.contractions.length)) != 0)
//                return val;
//            for (int i = 0; i < contractions.length; ++i)
//                if ((val = Long.compare(contractions[i], o.contractions[i])) != 0)
//                    return val;
//            return 0;
//        }
//    }
}
