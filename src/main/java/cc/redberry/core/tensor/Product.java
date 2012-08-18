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
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.math.GraphUtils;
import cc.redberry.core.number.Complex;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.HashFunctions;
import java.lang.ref.SoftReference;
import java.util.Arrays;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class Product extends MultiTensor {

    /**
     * Numerical factor.
     */
    final Complex factor;
    /**
     * Elements with zero size of indices (without indices).
     */
    final Tensor[] indexlessData;
    /**
     * Elements with indices.
     */
    final Tensor[] data;
    /**
     * Reference to cached ProductContent object.
     */
    SoftReference<ProductContent> contentReference;
    /**
     * Hash code of this product.
     */
    final int hash;

    Product(Indices indices, Complex factor, Tensor[] indexless, Tensor[] data) {
        super(indices);
        this.factor = factor.isOne() ? Complex.ONE : factor.isMinusOne() ? Complex.MINUSE_ONE : factor;
        this.indexlessData = indexless;
        this.data = data;

        Arrays.sort(data);
        Arrays.sort(indexless);

        this.contentReference = new SoftReference<>(calculateContent());
        this.hash = calculateHash();
    }

    Product(Complex factor, Tensor[] indexlessData, Tensor[] data, ProductContent content, Indices indices) {
        super(indices);
        this.factor = factor.isOne() ? Complex.ONE : factor.isMinusOne() ? Complex.MINUSE_ONE : factor;
        this.indexlessData = indexlessData;
        this.data = data;
        this.contentReference = new SoftReference<>(content == null ? calculateContent() : content);
        this.hash = calculateHash();
    }

    @Override
    public Indices getIndices() {
        return indices;
    }

    @Override
    protected char operationSymbol() {
        return '*';
    }

    @Override
    public Tensor get(int i) {
        if (factor != Complex.ONE)
            --i;
        if (i == -1)
            return factor;
        if (i < indexlessData.length)
            return indexlessData[i];
        else
            return data[i - indexlessData.length];
        //return data[i + ((hash & 0x00080000) >> 19)]; // ;)
    }

    @Override
    public Tensor[] getRange(int from, int to) {//TODO optimize and comment
        if (from < 0 || to > size())
            throw new ArrayIndexOutOfBoundsException();
        if (from > to)
            throw new IllegalArgumentException();

        int indexlessMaxPos = indexlessData.length, dataMaxPos = indexlessMaxPos + data.length;
        Tensor[] result = new Tensor[to - from];
        if (to == from)
            return result;
        int n = 0;  //offset for result if factor isn't 1
        if (factor != Complex.ONE) {
            if (from == 0) {
                result[0] = factor;
                ++n;
            } else
                --from;
            --to;
        }
        if (to < indexlessMaxPos)
            System.arraycopy(indexlessData, from, result, n, to - from);
        else if (from < indexlessMaxPos) {
            System.arraycopy(indexlessData, from, result, n, indexlessMaxPos - from);
            System.arraycopy(data, 0, result, indexlessMaxPos - from + n, to - indexlessMaxPos);
        } else
            System.arraycopy(data, from - indexlessMaxPos, result, n, to - from);

        return result;
    }

    @Override
    public int size() {
        int size = data.length + indexlessData.length;
        if (factor == Complex.ONE)
            return size;
        return size + 1;
        //return data.length - ((hash & 0x00080000) >> 19); // ;)
    }

    public int sizeWithoutFactor() {
        return data.length + indexlessData.length;
    }

    public Tensor getWithoutFactor(int i) {
        return i < indexlessData.length ? indexlessData[i] : data[i - indexlessData.length];
    }

    //     public Tensor[] getRangeWithoutFactor(int from,int to) {
    //         if(to < indexlessData.length)
    //             return Arrays.copyOfRange(data, to)
    //         return  null;
    ////        return i < indexlessData.length ? indexlessData[i] : data[i - indexlessData.length];
    //    }
    public Complex getFactor() {
        return factor;
    }

    private int calculateHash() {
        int result;
        if (factor == Complex.ONE || factor == Complex.MINUSE_ONE)
            result = 0;
        else
            result = factor.hashCode();

        for (Tensor t : indexlessData)
            result = result * 31 + t.hashCode();
        for (Tensor t : data)
            result = result * 17 + t.hashCode();
        if (factor == Complex.MINUSE_ONE && size() == 2)
            return result;
        return result - 79 * getContent().getContractionStructure().hashCode();
    }

    public ProductContent getContent() {
        ProductContent content = contentReference.get();
        if (content == null)
            contentReference = new SoftReference<>(content = calculateContent());
        return content;
    }

    public Tensor[] getIndexless() {
        return indexlessData.clone();
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ProductBuilder(indexlessData.length, data.length);
    }

    public Tensor[] getAllScalars() {
        Tensor[] scalras = getContent().getScalars();
        if (factor == Complex.ONE) {
            Tensor[] allScalars = new Tensor[indexlessData.length + scalras.length];
            System.arraycopy(indexlessData, 0, allScalars, 0, indexlessData.length);
            System.arraycopy(scalras, 0, allScalars, indexlessData.length, scalras.length);
            return allScalars;
        } else {
            Tensor[] allScalars = new Tensor[1 + indexlessData.length + scalras.length];
            allScalars[0] = factor;
            System.arraycopy(indexlessData, 0, allScalars, 1, indexlessData.length);
            System.arraycopy(scalras, 0, allScalars, indexlessData.length + 1, scalras.length);
            return allScalars;
        }
    }

    public Tensor[] getAllScalarsWithoutFactor() {
        Tensor[] scalras = getContent().getScalars();
        Tensor[] allScalars = new Tensor[indexlessData.length + scalras.length];
        System.arraycopy(indexlessData, 0, allScalars, 0, indexlessData.length);
        System.arraycopy(scalras, 0, allScalars, indexlessData.length, scalras.length);
        return allScalars;
    }

    public Tensor getIndexlessSubProduct() {
        if (indexlessData.length == 0)
            return factor;
        else if (factor == Complex.ONE && indexlessData.length == 1)
            return indexlessData[0];
        else
            return new Product(factor, indexlessData, new Tensor[0], ProductContent.EMPTY_INSTANCE, IndicesFactory.EMPTY_INDICES);
    }

    public Tensor getDataSubProduct() {
        if (data.length == 0)
            return Complex.ONE;
        if (data.length == 1)
            return data[0];
        return new Product(Complex.ONE, new Tensor[0], data, contentReference.get(), indices);
    }

    private ProductContent calculateContent() {
        if (data.length == 0)
            return ProductContent.EMPTY_INSTANCE;
        final Indices freeIndices = indices.getFree();
        final int differentIndicesCount = (getIndices().size() + freeIndices.size()) / 2;

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
        final short[] stretchIndices = calculateStretchIndices(); //for preformance

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

        //Creating input graph components
        int[] components = GraphUtils.calculateConnectedComponents(
                infoToTensorIndices(upperInfo), infoToTensorIndices(lowerInfo), data.length + 1);

        //the number of components
        final int componentCount = components[components.length - 1]; //Last element of this array contains components count 
        //(this is specification of GraphUtils.calculateConnectedComponents method)
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

        Tensor nonScalar = null;
        if (componentCount == 1) //There are no scalar subproducts in this product
            nonScalar = this;
        else if (datas[0].length > 0)
            nonScalar = Tensors.multiply(datas[0]);

        Tensor[] scalars = new Tensor[componentCount - 1];

        if (nonScalar == null && componentCount == 2 && factor == Complex.ONE && indexlessData.length == 0)
            scalars[0] = this;
        else {
            for (i = 1; i < componentCount; ++i)
                scalars[i - 1] = Tensors.multiply(datas[i]);
            Arrays.sort(scalars); //TODO use nonstable sort
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

        int[] inds = IndicesUtils.getIndicesNames(this.indices.getFree());
        Arrays.sort(inds);
        ScaffoldWrapper[] wrappers = new ScaffoldWrapper[contractions.length];
        for (i = 0; i < contractions.length; ++i)
            wrappers[i] = new ScaffoldWrapper(inds, data[i], contractions[i]);

        ArraysUtils.quickSort(wrappers, data);

        for (i = 0; i < contractions.length; ++i)
            contractions[i] = wrappers[i].tc;

        //Here we can use unstable sort algorithm
        //ArraysUtils.quickSort(contractions, 0, contractions.length, data);

        //Form resulting content
        ContractionStructure contractionStructure = new ContractionStructure(freeContraction, contractions);

        //TODO should be lazy field in ProductContent
        FullContractionsStructure fullContractionsStructure = new FullContractionsStructure(data, differentIndicesCount, freeIndices);
        return new ProductContent(contractionStructure, fullContractionsStructure, scalars, nonScalar, stretchIndices, data);
    }

    private short[] calculateStretchIndices() {
        short[] stretchIndex = new short[data.length];
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

    @Override
    protected int hash() {
        return hash;
    }

    @Override
    public TensorFactory getFactory() {
        return null;
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
    //-65536 == packToLong(-1, (short) -1, (short) 0);
    private static final long dummyTensorInfo = -65536;
    //        private static class ProductContent {
    //
    //        final ContractionStructure contractionStructure;
    //        final FullContractionsStructure fullContractionsStructure;
    //        final Tensor[] scalars;
    //        final Tensor nonScalar;
    //        final short[] stretchIndices;
    //
    //        public ProductContent(ContractionStructure contractionStructure,
    //                              FullContractionsStructure fullContractionsStructure,
    //                              Tensor[] scalars, Tensor nonScalar,
    //                              short[] stretchIndices) {
    //            this.contractionStructure = contractionStructure;
    //            this.fullContractionsStructure = fullContractionsStructure;
    //            this.scalars = scalars;
    //            this.nonScalar = nonScalar;
    //            this.stretchIndices = stretchIndices;
    //        }
    //    }

    private static int hc(Tensor t, int[] inds) {
        Indices ind = t.getIndices().getFree();
        int h = 31;
        int ii;
        for (int i = ind.size() - 1; i >= 0; --i) {
            ii = IndicesUtils.getNameWithType(ind.get(i));
            if ((ii = Arrays.binarySearch(inds, ii)) >= 0)
                h ^= HashFunctions.JenkinWang32shift(ii);
        }
        return h;
    }

    private static class ScaffoldWrapper implements Comparable<ScaffoldWrapper> {

        final int[] inds;
        final Tensor t;
        final TensorContraction tc;
        final int hashWithIndices;

        private ScaffoldWrapper(int[] inds, Tensor t, TensorContraction tc) {
            this.inds = inds;
            this.t = t;
            this.tc = tc;
            hashWithIndices = hc(t, inds);
        }

        @Override
        public int compareTo(ScaffoldWrapper o) {
            int r = tc.compareTo(o.tc);
            if (r != 0)
                return r;
            return Integer.compare(hashWithIndices, o.hashWithIndices);
        }
    }
}
