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
package cc.redberry.core.tensor;

import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesBuilderSorted;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.math.GraphUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.ArraysUtils;
import java.util.Arrays;

/**
 * This class represents set of multipliers and contraction structure between
 * them.<br/><br/> <h3>Contraction structure</h3> .... <br/><br/> <h3>Used terms
 * explanation</h3>
 * <pre>
 *Tensor:       a_i^j  a_j^k  a_m^i  b_k^q  b_n^m  v     c_p^t          []_t^np
 *Hashes:       233    233    233    462    462    75    98             -
 *TensorIndex:  0      1      2      3      4      5     6              -1
 *StratchIndex: 0      0      0      1      1      2     3              -1
 *
 * [] - is a dummy tensor
 * </pre>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class ProductContent {

    private final static ProductContent EMPTY_CONTENT = new ProductContent(new Tensor[0]);
    
    private final int differentIndicesCount;
    private final Indices freeIndices;
    
    private short[] stretchIndex = null;
    private int[] stretchHashReflection = null;
    
    private ContractionStructure contractionStructure = null;
    private Tensor nonScalar = null;
    private Tensor[] scalars = null;
    private final Tensor[] data;

    private ProductContent(final Indices indices, Complex factor, final Tensor... data) {
        this.data = data;
        this.freeIndices = indices.getFreeIndices();
        this.differentIndicesCount = (indices.size() + freeIndices.size()) / 2;
        if (freeIndices.size() == 0)
            nonScalar = null;
    }

    private ProductContent(final Tensor[] data) {
        this(new IndicesBuilderSorted().append(data).getIndices(), Complex.ONE, data);
    }

    private static ProductContent createNonScalar(final Tensor[] data) {
        if (data.length == 0)
            return EMPTY_CONTENT;
        ProductContent nonScalar = new ProductContent(data);
        nonScalar.scalars = new Tensor[0];
        nonScalar.nonScalar = new Product(data);
        return nonScalar;
    }

    private static ProductContent createScalar(final Tensor[] data) {
        assert data.length != 0;
        ProductContent scalar = new ProductContent(data);
        scalar.scalarContents = new ProductContent[]{scalar};
        scalar.nonScalarContent = EMPTY_CONTENT;
        return scalar;
    }

    //Here is main algorithm of ProductContent creation from tensors array
    //АДЪ
    private void ensureInitialized() {
        //Lazy initialization
        if (contractionStructure != null)
            return;
        //Names (names with type, see IndicesUtils.getNameWithType() ) of all indices in this multiplication
        //It will be used as index name -> index index [0,1,2,3...] mapping
        final int[] upperIndices = new int[differentIndicesCount], lowerIndices = new int[differentIndicesCount];
        //This is sorage for intermediate information about indices, used in the algorithm (see below)
        //Structure:
        //
        final long[] upperInfo = new long[differentIndicesCount], lowerInfo = new long[differentIndicesCount];

        //This is for generalization of algorithm
        //indices[0] == lowerIndices
        //indices[1] == lowerIndices
        final int[][] indices = new int[][]{lowerIndices, upperIndices};

        //This is for generalization of algorithm too
        //info[0] == lowerInfo
        //info[1] == lowerInfo
        final long[][] info = new long[][]{lowerInfo, upperInfo};

        //Pointers for lower and upper indices, used in algorithm
        //pointer[0] - pointer to lower
        //pointer[1] - pointer to upper
        final int[] pointer = new int[2];
        final short[] stretchIndices = getStretchIndex(); //for preformance

        //Allocating array for results, one contraction for each tensor
        final TensorContraction[] contractions = new TensorContraction[data.length];
        //There is one dummy tensor with index -1, it represents fake
        //tensor contracting with whole Product to leave no contracting indices.
        //So, all "conractions" with this dummy "contraction" looks like a scalar
        //product. (sorry for English)
        final TensorContraction freeContraction = new TensorContraction((short) -1, new long[freeIndices.size()]);

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
            short[] diffIds = tInds.getDiffIds();
            for (i = 0; i < tInds.size(); ++i) {
                index = tInds.get(i);
                state = IndicesUtils.getStateInt(index);
                info[state][pointer[state]] = packToLong(tensorIndex, stretchIndices[tensorIndex], diffIds[i]);
                indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
            }

            //Result allocation
            contractions[tensorIndex] = new TensorContraction(stretchIndices[tensorIndex], new long[tInds.size()]);
        }

        //Here we can use unstable sorting algorithm (all indices are different)
        ArraysUtils.quickSort(indices[0], info[0]);
        ArraysUtils.quickSort(indices[1], info[1]);

        //<-- Here we have mature info arrays

        //Processing scalar and non scalar parts
        if (nonScalar == null || scalars == null) {

            //Creating input graph components
            int[] components = GraphUtils.calculateConnectedComponents(
                    infoToTensorIndices(upperInfo), infoToTensorIndices(lowerInfo), data.length + 1);

            //the number of components
            final int componentCount = components[components.length - 1];
            int[] componentSizes = new int[componentCount];

            //finding each component size
            for (i = 1; i < components.length - 1; ++i)
                ++componentSizes[components[i]];

            //allocating resulting datas 0 - is non scalar data
            Tensor[][] datas = new Tensor[componentCount][];
            for (i = 0; i < componentCount; ++i)
                datas[i] = new Tensor[componentSizes[i]];

            //from here we shall use components sizes as pointers
            Arrays.fill(componentSizes, 0);

            //writing data
            for (i = 1; i < data.length + 1; ++i)
                datas[components[i]][componentSizes[components[i]]++] = data[i - 1];

            nonScalarContent = createNonScalar(datas[0]);
            scalarContents = new ProductContent[componentCount - 1];
            for (i = 1; i < componentCount; ++i)
                scalarContents[i - 1] = createScalar(datas[i]);
            Arrays.sort(scalarContents, ArraysUtils.HASH_COMPARATOR); //TODO change to quick sort
        }

        //assert Arrays.equals(indices[0], indices[1]);
        assert Arrays.equals(indices[0], indices[1]);

        final int[] pointers = new int[data.length];
        int freePointer = 0;
        for (i = 0; i < differentIndicesCount; ++i) {
            //Contractions from lower to upper
            tensorIndex = (int) (info[0][i] >> 32);
            long contraction = (0x0000FFFF00000000L & (info[0][i] << 32))
                    | (0xFFFFFFFFL & (info[1][i]));
            if (tensorIndex == -1)
                freeContraction.indexContractions[freePointer++] = contraction;
            else
                contractions[tensorIndex].indexContractions[pointers[tensorIndex]++] = contraction;

            //Contractions from upper to lower
            tensorIndex = (int) (info[1][i] >> 32);
            contraction = (0x0000FFFF00000000L & (info[1][i] << 32))
                    | (0xFFFFFFFFL & (info[0][i]));
            if (tensorIndex == -1)
                freeContraction.indexContractions[freePointer++] = contraction;
            else
                contractions[tensorIndex].indexContractions[pointers[tensorIndex]++] = contraction;
        }

        //Sorting per-index contractions in each TensorContraction
        for (TensorContraction contraction : contractions)
            contraction.sortContractions();
        freeContraction.sortContractions();

        //Here we can use unstable sort algorithm
        ArraysUtils.quickSort(contractions, data);
        contractionStructure = new ContractionStructure(freeContraction, contractions);
    }

    public ProductContent getNonScalarContent() {
        if (nonScalarContent == null)
            ensureInitialized();
        return nonScalarContent;
    }

    public ProductContent[] getScalarContents() {
        if (scalarContents == null)
            ensureInitialized();
        return scalarContents;
    }

    public ContractionStructure getContractionStructure() {
        ensureInitialized();
        return contractionStructure;
    }

    public FullContractionsStructure getFullContractionStructure() {
        ensureInitialized();
        return new FullContractionsStructure(data, differentIndicesCount, freeIndices);
    }

    public final short[] getStretchIndex() {
        if (stretchIndex == null) {
            stretchIndex = new short[data.length];
            //stretchIndex[0] = 0;
            if (data.length <= 1)
                return stretchIndex;
            short index = 0;
            int i = 1;
            int oldHash = data[i - 1].hashCode();
            for (; i < data.length; ++i)
                if (oldHash == data[i].hashCode())
                    stretchIndex[i] = index;
                else {
                    stretchIndex[i] = ++index;
                    oldHash = data[i].hashCode();
                }
        }
        return stretchIndex;
    }

    public short getStretchIndexByHash(final int hashCode) {
        if (stretchHashReflection == null) {
            final short[] si = getStretchIndex();
            stretchHashReflection = new int[si[si.length - 1] + 1];
            //TODO performance (!!!)
            for (int i = 0; i < si.length; ++i)
                stretchHashReflection[si[i]] = data[i].hashCode();
        }
        int index = Arrays.binarySearch(stretchHashReflection, hashCode);
        if (index < 0)
            return -1;
        return (short) index;
    }

    /**
     * Function to pack data to intermediate 64-bit record.
     *
     * @param tensorIndex  index of tensor in the data array (before second
     *                     sorting)
     * @param stretchIndex stretch index of this tensor (sequence number of
     *                     tensors hash in array)
     * @param id           id of index in tensor indices list (could be !=0 only
     *                     for simple tensors)
     *
     * @return packed record (long)
     */
    private static long packToLong(final int tensorIndex, final short stretchIndex, final short id) {
        return (((long) tensorIndex) << 32) | (0xFFFF0000L & (stretchIndex << 16)) | (0xFFFFL & id);
    }

    private static int[] infoToTensorIndices(final long[] info) {
        final int[] result = new int[info.length];
        for (int i = 0; i < info.length; ++i)
            result[i] = ((int) (info[i] >> 32)) + 1;
        return result;
    }

    /**
     * Returns tensor index from packed data via packToLong() method
     *
     * @param info
     *
     * @return
     */
    private static int tensorIndex(long info) {
        return (int) (info >>> 32);
    }
    //-65536 == packToLong(-1, (short) -1, (short) 0);
    private static final long dummyTensorInfo = -65536;

    @Override
    public String toString() {
        return new Product(data).toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        ensureInitialized();
        for (int i = 0; i < data.length; ++i)
            hash ^= data[i].hashCode() * contractionStructure.get(i).hashCode();
        return hash;
    }
}
