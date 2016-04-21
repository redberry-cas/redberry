/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.graph.GraphType;
import cc.redberry.core.graph.GraphUtils;
import cc.redberry.core.graph.PrimitiveSubgraph;
import cc.redberry.core.graph.PrimitiveSubgraphPartition;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.number.NumberUtils;
import cc.redberry.core.transformations.fractions.NumeratorDenominator;
import cc.redberry.core.utils.*;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static cc.redberry.core.indices.IndicesUtils.getNameWithType;
import static cc.redberry.core.indices.IndicesUtils.getStateInt;
import static cc.redberry.core.tensor.StructureOfContractions.*;
import static cc.redberry.core.utils.HashFunctions.JenkinWang32shift;

/**
 * Representation of product of mathematical expressions.
 * <p/>
 * <p>The implementation keeps numerical factor, indexless multipliers, and multipliers
 * with non empty free indices separately. If there is no numeric factor in product, it is equal to 1.
 * Bot indexless and indexed data are sorted according to {@link Tensor#compareTo(Tensor)} method.
 * Indexed data is additionally sorted according to a special comparator based on {@link StructureOfContractions}
 * of this product.
 * </p>
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
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
    final SoftReferenceWrapper<ProductContent> contentReference;
    /**
     * Hash code of this product.
     */
    int hash;
    /**
     * Hash code of this product sensitive to particular free indices configuration (to within equality).
     */
    int iHash;

    Product(Indices indices, Complex factor, Tensor[] indexless, Tensor[] data) {
        super(indices);
        this.factor = getDefaultReference(factor);
        this.indexlessData = indexless;
        this.data = data;

        Arrays.sort(data);
        Arrays.sort(indexless);

        this.contentReference = new SoftReferenceWrapper<>();
        calculateContent();
        calculateHash();
    }

    Product(Complex factor, Tensor[] indexlessData, Tensor[] data, ProductContent content, Indices indices) {
        super(indices);
        this.factor = getDefaultReference(factor);
        this.indexlessData = indexlessData;
        this.data = data;
        this.contentReference = new SoftReferenceWrapper<>();
        if (content == null)
            calculateContent();
        else
            this.contentReference.resetReferent(content);
        calculateHash();
    }

    //very unsafe
    Product(Indices indices, Complex factor, Tensor[] indexlessData, Tensor[] data, SoftReferenceWrapper<ProductContent> contentReference, int hash, int iHash) {
        super(indices);
        this.factor = factor;
        this.indexlessData = indexlessData;
        this.data = data;
        this.contentReference = contentReference;
        this.hash = hash;
        this.iHash = iHash;
    }

    //very unsafe
    Product(Indices indices, Complex factor, Tensor[] indexlessData, Tensor[] data, SoftReferenceWrapper<ProductContent> contentReference) {
        super(indices);
        this.factor = factor;
        this.indexlessData = indexlessData;
        this.data = data;
        this.contentReference = contentReference;
        calculateHash();
    }

    private static Complex getDefaultReference(Complex factor) {
        return factor.isOne() ? Complex.ONE : factor.isMinusOne() ? Complex.MINUS_ONE : factor;
    }

    @Override
    public Indices getIndices() {
        return indices;
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

        int indexlessMaxPos = indexlessData.length;
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

    /**
     * Returns the size of product without factor. I.e. if factor is one, then {@code sizeWithoutFactor() == size()},
     * otherwise {@code sizeWithoutFactor() == size() - 1}.
     *
     * @return size of product without factor
     */
    public int sizeWithoutFactor() {
        return data.length + indexlessData.length;
    }

    /**
     * Returns the size of indexless part (including numerical factor).
     *
     * @return size of indexless part (including numerical factor)
     */
    public int sizeOfIndexlessPart() {
        return indexlessData.length + (factor == Complex.ONE ? 0 : 1);
    }

    /**
     * Returns the size of data part (i.e. including only those terms that have nonempty indices).
     *
     * @return size of data part (i.e. including only those terms that have nonempty indices)
     */
    public int sizeOfDataPart() {
        return data.length;
    }

    @Override
    public Tensor set(int i, Tensor tensor) {
        if (i >= size() || i < 0)
            throw new IndexOutOfBoundsException();
        Tensor old = get(i);
        if (old == tensor)
            return this;
        if (TensorUtils.equalsExactly(old, tensor))
            return this;
        if (tensor instanceof Complex)
            return setComplex(i, (Complex) tensor);

        int size = size(), j;
        if (TensorUtils.passOutDummies(tensor)) {
            TIntHashSet forbidden = new TIntHashSet();
            for (j = 0; j < size; ++j)
                if (j != i)
                    TensorUtils.appendAllIndicesNamesT(get(j), forbidden);
            tensor = ApplyIndexMapping.renameDummy(tensor, forbidden.toArray());
        }

        Boolean compare = TensorUtils.compare1(old, tensor);
        if (compare == null)
            return super.set(i, tensor);

        Complex newFactor = factor;
        if (compare) {
            tensor = Tensors.negate(tensor);
            newFactor = factor.negate();
            newFactor = getDefaultReference(newFactor);
        }

        if (factor != Complex.ONE) {
            assert i != 0;
            --i;
        }

        if (i < indexlessData.length) {
            Tensor[] newIndexless = indexlessData.clone();
            newIndexless[i] = tensor;
            return new Product(indices, newFactor, newIndexless, data, contentReference);
        } else {
            Tensor[] newData = data.clone();
            newData[i - indexlessData.length] = tensor;
            return new Product(new IndicesBuilder().append(newData).getIndices(),
                    newFactor, indexlessData, newData);
        }
    }

    @Override
    public Tensor remove(int position) {
        return setComplex(position, Complex.ONE);
    }

    private Tensor setComplex(int i, Complex complex) {
        if (NumberUtils.isZeroOrIndeterminate(complex))
            return complex;


        if (factor != Complex.ONE) {
            if (i == 0) {
                if (complex.isOne()) {
                    if (data.length == 1 && indexlessData.length == 0)
                        return data[0];
                    if (data.length == 0 && indexlessData.length == 1)
                        return indexlessData[0];
                }
                complex = getDefaultReference(complex);
                return new Product(indices, complex, indexlessData, data, contentReference);
            }
            complex = complex.multiply(factor);
            complex = getDefaultReference(complex);
            --i;
        }

        if (complex.isOne()) {
            if (data.length == 2 && indexlessData.length == 0)
                return data[1 - i];
            if (data.length == 0 && indexlessData.length == 2)
                return indexlessData[1 - i];
            if (data.length == 1 && indexlessData.length == 1)
                return i == 0 ? data[0] : indexlessData[0];
        }
        if ((data.length == 1 && indexlessData.length == 0) || (data.length == 0 && indexlessData.length == 1))
            return complex;


        if (i < indexlessData.length) {
            Tensor[] newIndexless = ArraysUtils.remove(indexlessData, i);
            return new Product(indices, complex, newIndexless, data, contentReference);
        } else {
            Tensor[] newData = ArraysUtils.remove(data, i - indexlessData.length);
            return new Product(new IndicesBuilder().append(newData).getIndices(),
                    complex, indexlessData, newData);
        }
    }

    @Override
    protected Tensor remove1(int[] positions) {
        Complex newFactor = factor;
        if (factor != Complex.ONE) {
            if (positions[0] == 0) {
                newFactor = Complex.ONE;
                positions = Arrays.copyOfRange(positions, 1, positions.length);
            }
            for (int i = positions.length - 1; i >= 0; --i)
                --positions[i];
        }

        int dataFrom = Arrays.binarySearch(positions, indexlessData.length - 1);
        if (dataFrom < 0) dataFrom = ~dataFrom - 1;

        final int[] indexlessPositions = Arrays.copyOfRange(positions, 0, dataFrom + 1);
        final int[] dataPositions = Arrays.copyOfRange(positions, dataFrom + 1, positions.length);
        for (int i = 0; i < dataPositions.length; ++i)
            dataPositions[i] -= indexlessData.length;

        Tensor[] newIndexless = ArraysUtils.remove(indexlessData, indexlessPositions);
        Tensor[] newData = ArraysUtils.remove(data, dataPositions);

        return createProduct(new IndicesBuilder().append(newData).getIndices(),
                newFactor, newIndexless, newData);
    }

    @Override
    protected Complex getNeutral() {
        return Complex.ONE;
    }

    @Override
    protected Tensor select1(int[] positions) {
        int add = factor == Complex.ONE ? 0 : 1;
        Complex newFactor = Complex.ONE;
        List<Tensor> newIndexless = new ArrayList<>(), newData = new ArrayList<>();
        for (int position : positions) {
            position -= add;
            if (position == -1)
                newFactor = factor;
            else if (position < indexlessData.length)
                newIndexless.add(indexlessData[position]);
            else
                newData.add(data[position - indexlessData.length]);
        }
        return new Product(new IndicesBuilder().append(newData).getIndices(), newFactor,
                newIndexless.toArray(new Tensor[newIndexless.size()]),
                newData.toArray(new Tensor[newData.size()]));
    }

    private static Tensor createProduct(Indices indices, Complex factor, Tensor[] indexless, Tensor[] data) {
        if (indexless.length == 0 && data.length == 0)
            return factor;
        if (factor == Complex.ONE) {
            if (indexless.length == 0 && data.length == 1)
                return data[0];
            if (indexless.length == 1 && data.length == 0)
                return indexless[0];
        }
        return new Product(indices, factor, indexless, data);
    }


    /**
     * Returns element at i-th position excluding numerical factor of this product from numbering. So, if
     * factor is one, then this method returns {@code get(i)}, otherwise is returns {@code get(i-1)}.
     *
     * @param i position
     * @return element at i-th position excluding numerical factor of this product from numbering
     */
    public Tensor getWithoutFactor(int i) {
        return i < indexlessData.length ? indexlessData[i] : data[i - indexlessData.length];
    }

    //     public Tensor[] getRangeWithoutFactor(int from,int to) {
    //         if(to < indexlessData.length)
    //             return Arrays.copyOfRange(data, to)
    //         return  null;
    ////        return i < indexlessData.length ? indexlessData[i] : data[i - indexlessData.length];
    //    }

    /**
     * Returns numerical factor of this product.
     *
     * @return numerical factor of this product
     */
    public Complex getFactor() {
        return factor;
    }

    private void calculateHash() {
        int result;
        if (factor == Complex.ONE || factor == Complex.MINUS_ONE)
            result = 0;
        else
            result = factor.hashCode();

        for (Tensor t : indexlessData)
            result = result * 31 + t.hashCode();
        for (Tensor t : data)
            result = result * 17 + t.hashCode();
        if (factor == Complex.MINUS_ONE && size() == 2) {
            hash = result;
            iHash = HashingStrategy.iHash(get(1));
            return;
        }
        hash = result - 79 * getContent().graphHash();
        iHash = result - 79 * getContent().iGraphHash();
    }

    /**
     * Returns the product content, i.e. the information about the corresponding graph.
     *
     * @return product content
     */
    public ProductContent getContent() {
        ProductContent content = contentReference.getReference().get();
        if (content == null)
            content = calculateContent();
        return content;
    }

    /**
     * Returns a copy of indexless data.
     *
     * @return a copy of indexless data
     */
    public Tensor[] getIndexless() {
        return indexlessData.clone();
    }

    /**
     * Returns i-th element of indexless data.
     *
     * @return i-th element of indexless data
     */
    public Tensor getIndexless(int i) {
        return indexlessData[i];
    }

    /**
     * Returns length of {@link #getIndexless()} array
     *
     * @return length of {@link #getIndexless()} array
     */
    public int indexlessLength() {
        return indexlessData.length;
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ProductBuilder(indexlessData.length, data.length);
    }

    /**
     * Returns all scalar factors in this product. For example, if this is 2*a*f_mn*G^mn*f_a*T^ab, the
     * result will be [2,a,f_mn*G^mn].
     *
     * @return all scalar factors in this product
     */
    public Tensor[] getAllScalars() {
        Tensor[] scalars = getContent().getScalars();
        if (factor == Complex.ONE) {
            Tensor[] allScalars = new Tensor[indexlessData.length + scalars.length];
            System.arraycopy(indexlessData, 0, allScalars, 0, indexlessData.length);
            System.arraycopy(scalars, 0, allScalars, indexlessData.length, scalars.length);
            return allScalars;
        } else {
            Tensor[] allScalars = new Tensor[1 + indexlessData.length + scalars.length];
            allScalars[0] = factor;
            System.arraycopy(indexlessData, 0, allScalars, 1, indexlessData.length);
            System.arraycopy(scalars, 0, allScalars, indexlessData.length + 1, scalars.length);
            return allScalars;
        }
    }

    /**
     * Returns all scalar factors in this product excluding the numerical factor. For example, if
     * this is 2*a*f_mn*G^mn*f_a*T^ab, the result will be [a,f_mn*G^mn].
     *
     * @return all scalar factors in this product excluding the numerical factor
     */
    public Tensor[] getAllScalarsWithoutFactor() {
        Tensor[] scalras = getContent().getScalars();
        Tensor[] allScalars = new Tensor[indexlessData.length + scalras.length];
        System.arraycopy(indexlessData, 0, allScalars, 0, indexlessData.length);
        System.arraycopy(scalras, 0, allScalars, indexlessData.length, scalras.length);
        return allScalars;
    }

    /**
     * Returns indexless data as a product of tensors.
     *
     * @return indexless data as a product of tensors
     */
    public Tensor getIndexlessSubProduct() {
        if (indexlessData.length == 0)
            return factor;
        else if (factor == Complex.ONE && indexlessData.length == 1)
            return indexlessData[0];
        else
            return new Product(factor, indexlessData, new Tensor[0], ProductContent.EMPTY_INSTANCE, IndicesFactory.EMPTY_INDICES);
    }

    /**
     * Returns this product but without numeric factor.
     *
     * @return this product but without numeric factor
     */
    public Tensor getSubProductWithoutFactor() {
        if (factor == Complex.ONE)
            return this;
        return new Product(indices, Complex.ONE, indexlessData, data, contentReference);
    }

    /**
     * Returns the product of indexed (with non empty free indices) multipliers of this tensor.
     *
     * @return product of indexed (with non empty free indices) multipliers of this tensor
     */
    public Tensor getDataSubProduct() {
        if (data.length == 0)
            return Complex.ONE;
        if (data.length == 1)
            return data[0];
        return new Product(indices, Complex.ONE, new Tensor[0], data, contentReference);
    }

    @Override
    protected int hash() {
        return hash;
    }

    /**
     * Returns hash code sensitive to free indices configuration (to within equality relation)
     *
     * @return hash code sensitive to free indices configuration
     */
    public int iHashCode() {
        return iHash;
    }

    @Override
    public TensorFactory getFactory() {
        return ProductFactory.FACTORY;
    }

    @Override
    public String toString(OutputFormat format) {
        if (format.is(OutputFormat.C))
            return toCppString();
        StringBuilder sb = new StringBuilder();
        char operatorChar = format == OutputFormat.LaTeX ? ' ' : '*';

        if (factor.isReal() && factor.getReal().signum() < 0) {
            sb.append('-');
            Complex f = factor.abs();
            if (!f.isOne())
                sb.append(((Tensor) f).toString(format, Product.class)).append(operatorChar);
        } else if (factor != Complex.ONE)
            sb.append(((Tensor) factor).toString(format, Product.class)).append(operatorChar);

        int i = 0, size = factor == Complex.ONE ? size() : size() - 1;

        for (; i < indexlessData.length; ++i) {
            sb.append(indexlessData[i].toString(format, Product.class));
            if (i == size - 1)
                return sb.toString();
            sb.append(operatorChar);
        }

//        removeLastOperatorChar(sb, operatorChar);
        EnumSet<IndexType> matrixTypes;
        if (format.printMatrixIndices || (matrixTypes = IndicesUtils.nonMetricTypes(indices)).isEmpty())
            return printData(sb, format, operatorChar);
        return printMatrices(sb, format, operatorChar, matrixTypes);
    }

    private String toCppString() {
        final NumeratorDenominator nd = NumeratorDenominator.getNumeratorAndDenominator(this);
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(toCppString(nd.getNumerator())).append(")");
        if (!TensorUtils.isOne(nd.denominator))
            sb.append("/(").append(toCppString(nd.getDenominator())).append(")");
        return sb.toString();
    }

    private static String toCppString(Tensor t) {
        if (t instanceof Product)
            return ((Product) t).toCppString0();
        return t.toString(OutputFormat.C);
    }

    private String toCppString0() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; i++) {
            sb.append(get(i).toString(OutputFormat.C, Product.class));
            if (i == size() - 1)
                break;
            sb.append("*");
        }
        return sb.toString();
    }

    private String printData(StringBuilder sb, OutputFormat format, char operatorChar) {
//        if (sb.length() != 0)
//            sb.append(operatorChar);
        for (int i = 0; ; ++i) {
            sb.append(data[i].toString(format, Product.class));
            if (i == data.length - 1)
                break;
            sb.append(operatorChar);
        }
        removeLastOperatorChar(sb, operatorChar);
        return sb.toString();
    }

    private String printMatrices(StringBuilder sb, OutputFormat format, char operatorChar, EnumSet<IndexType> matrixTypes) {
//        if (sb.length() != 0)
//            sb.append(operatorChar);
        sb.append(new MatricesPrinter(format, operatorChar, matrixTypes).sb.toString());
        removeLastOperatorChar(sb, operatorChar);
        return sb.toString();
    }

    static void removeLastOperatorChar(StringBuilder sb, char operatorChar) {
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == operatorChar)
            sb.deleteCharAt(sb.length() - 1);
    }

    private final class MatricesPrinter {
        final OutputFormat format;
        final char operatorChar;
        final EnumSet<IndexType> matrixTypes;
        final BitArray matrixPrint = new BitArray(data.length);
        final BitArray graphPrint = new BitArray(data.length);
        final StringBuilder sb = new StringBuilder();

        private MatricesPrinter(OutputFormat format, char operatorChar, EnumSet<IndexType> matrixTypes) {
            this.format = format;
            this.operatorChar = operatorChar;
            this.matrixTypes = matrixTypes;
            printData();
        }

        void fillGraphPrint(int[] partition) {
            for (int i : partition) {
                graphPrint.set(i);
                matrixPrint.set(i);
            }
        }

        void printData() {
            ArrayList<SubgraphContainer> subgraphs = new ArrayList<>();
            for (IndexType type : matrixTypes) {
                PrimitiveSubgraph[] sgs = PrimitiveSubgraphPartition.calculatePartition(getContent(), type);
                out0:
                for (PrimitiveSubgraph sg : sgs) {
                    int[] partition = sg.getPartition();
                    TIntHashSet points = new TIntHashSet(partition);
                    boolean newSg = true, newGraph = false;
                    IntArrayList matched = new IntArrayList();
                    for (int i = subgraphs.size() - 1; i >= 0; --i) {
                        SubgraphContainer container = subgraphs.get(i);
                        if (points.equals(container.points)) {
                            //completely same
                            if (container.graphType != sg.getGraphType()
                                    || !Arrays.equals(container.partition, partition)) {
                                fillGraphPrint(subgraphs.get(i).partition);
                                subgraphs.remove(i);
                                continue out0;
                            }
                            container.types.add(type);
                            newSg = false;
                            matched.add(i);
                        } else if (includes(partition, container.partition)
                                && sg.getGraphType() == container.graphType) {
                            //can safely remove smaller partition
                            if (partition.length < container.partition.length)
                                continue out0;
                            else
                                subgraphs.remove(i);
                        } else if (intersects(points, container.points)) {
                            //just intersects
                            fillGraphPrint(subgraphs.get(i).partition);
                            subgraphs.remove(i);
                            newSg = false;
                            newGraph = true;
                            for (int j = 0; j < matched.size(); ++j) {
                                fillGraphPrint(subgraphs.get(matched.get(j)).partition);
                                subgraphs.remove(matched.get(j));
                            }
                        }
                    }
                    if (newSg)
                        subgraphs.add(new SubgraphContainer(sg.getGraphType(), partition, points, type));
                    else if (newGraph) fillGraphPrint(partition);
                }
            }

            for (int i = 0; i < subgraphs.size(); ++i) {
                SubgraphContainer subgraph = subgraphs.get(i);
                int ppLength = sb.length();
                if (subgraph.graphType == GraphType.Cycle)
                    printTrace(subgraph);
                else if (subgraph.graphType == GraphType.Line)
                    printProductOfMatrices(subgraph);
                else {
                    for (int j = 0; j < subgraph.partition.length; ++j) {
                        matrixPrint.set(subgraph.partition[j]);
                        graphPrint.set(subgraph.partition[j]);
                    }
                    continue;
                }
                if (i == subgraphs.size() - 1)
                    break;
                if (sb.length() != ppLength)
                    sb.append(operatorChar);
            }

            removeLastOperatorChar();
            //printing graph structures
            if (!graphPrint.isEmpty()) {
                if (sb.length() != 0)
                    sb.append(operatorChar);
                OutputFormat printMatrixIndices = format.printMatrixIndices();
                for (int i = 0; i < data.length; ++i)
                    if (graphPrint.get(i)) {
                        sb.append(data[i].toString(printMatrixIndices, Product.class));
                        sb.append(operatorChar);
                    }
                removeLastOperatorChar();
            }
            //if nothing more to print
            if (matrixPrint.isFull())
                return;

            if (sb.length() != 0)
                sb.append(operatorChar);
            for (int i = 0; i < data.length; ++i)
                if (!matrixPrint.get(i)) {
                    sb.append(data[i].toString(format, Product.class));
                    sb.append(operatorChar);
                }
            removeLastOperatorChar();
        }

        void removeLastOperatorChar() {
            Product.removeLastOperatorChar(sb, operatorChar);
        }

        void printTrace(SubgraphContainer subgraph) {
            if (subgraph.partition.length == 1 && Tensors.isKronecker(data[subgraph.partition[0]])) {
                int position = subgraph.partition[0];
                matrixPrint.set(position);
                sb.append(data[position].toString(format.printMatrixIndices(), Product.class));
            } else {
                sb.append("Tr[");
                printProductOfMatrices(subgraph);
                if (subgraph.types.size() > 1) {
                    sb.append(", ");
                    for (int i = 0; ; ++i) {
                        sb.append(subgraph.types.get(i));
                        if (i == subgraph.types.size() - 1)
                            break;
                        sb.append(", ");
                    }
                }
                sb.append("]");
            }
        }

        void printProductOfMatrices(SubgraphContainer subgraph) {
            for (int i = 0; ; ++i) {
                int position = subgraph.partition[i];
                matrixPrint.set(position);
                String str = data[position].toString(format, Product.class);
                sb.append(str);
                if (i == subgraph.partition.length - 1)
                    return;
                if (!str.isEmpty())
                    sb.append(operatorChar);
            }
        }
    }

    static boolean intersects(TIntHashSet a, TIntHashSet b) {
        a = new TIntHashSet(a);
        a.retainAll(b);
        return a.size() != 0;
    }

    private static class SubgraphContainer {
        private final List<IndexType> types = new ArrayList<>();
        private final GraphType graphType;
        private final int[] partition;
        private final TIntHashSet points;

        private SubgraphContainer(GraphType graphType, int[] partition, TIntHashSet points, IndexType type) {
            this.graphType = graphType;
            this.partition = partition;
            this.points = points;
            this.types.add(type);
        }
    }

    //l and s are distinct
    static boolean includes(int[] l, int[] s) {
        if (s.length > l.length)
            return includes(s, l);

        int p = 0;
        for (int v : s) {
            while (p < l.length && l[p] != v)
                ++p;
            if (p == l.length)
                return false;
        }
        return true;
    }

    @Override
    protected String toString(OutputFormat mode, Class<? extends Tensor> clazz) {
        if (clazz == Power.class)
            return "(" + toString(mode) + ")";
        else
            return toString(mode);
    }


    //================== Calculating content ==========================//

    private static final int REFINEMENT_LEVEL = 2;

    private ProductContent calculateContent() {
        if (data.length == 0) {
            contentReference.resetReferent(ProductContent.EMPTY_INSTANCE);
            return ProductContent.EMPTY_INSTANCE;
        }

        //<- Important! Data assumed to be sorted.
        //Arrays.sort(data);

        final Indices freeIndices = this.indices.getFree();
        if (freeIndices.size() == this.indices.size()) {
            //no any contractions
            return calculateContentWithNoContractions();
        }

        final int differentIndicesCount = (this.indices.size() + freeIndices.size()) / 2;

        final int[]
                upperIndices = new int[differentIndicesCount],
                lowerIndices = new int[differentIndicesCount];

        final long[]
                upperInfo = new long[differentIndicesCount],
                lowerInfo = new long[differentIndicesCount];

        final int[][] indices = new int[][]{lowerIndices, upperIndices};
        final long[][] info = new long[][]{lowerInfo, upperInfo};

        //Allocating array for results, one contraction for each tensor
        final long[][] contractions = new long[data.length][];

        calculateInfo(data, freeIndices, info, indices, contractions);
        assert Arrays.equals(indices[0], indices[1]);
        reCalculateContractions(differentIndicesCount, info, contractions);

        final int[] sortedIndices = IndicesUtils.getIndicesNames(freeIndices); //<- indices are sorted
        Arrays.sort(sortedIndices);
        assert !(freeIndices instanceof SimpleIndices);

        if (data.length == 1) {
            Tensor[] scalars;
            Tensor nonScalar;
            final int[] hashCodes = new int[1], iHashCodes = new int[1];
            if (data[0] instanceof SimpleTensor) {
                SimpleTensor st = (SimpleTensor) data[0];
                hashCodes[0] = HashingStrategy.iGraphHashWithoutIndices(st);
                iHashCodes[0] = HashingStrategy.iGraphHash(st, sortedIndices);
            } else {
                hashCodes[0] = data[0].hashCode();
                iHashCodes[0] = HashingStrategy.iHash(data[0], sortedIndices);
            }
            if (freeIndices.size() == 0) {
                scalars = new Tensor[]{data[0]};
                nonScalar = null;
            } else {
                scalars = new Tensor[0];
                nonScalar = data[0];
            }

            final ProductContent pc = new ProductContent(new StructureOfContractions(contractions, new int[1], 1), data, hashCodes, iHashCodes, nonScalar, scalars);
            contentReference.resetReferent(pc);
            return pc;
        }

        int i;
        final int[] hashCodes = new int[data.length];
        int[] iHashCodes;
        if (sortedIndices.length == 0) {
            for (i = 0; i < data.length; ++i)
                hashCodes[i] = data[i].hashCode();
            refine(hashCodes, contractions, data);
            iHashCodes = hashCodes;
        } else {
            iHashCodes = new int[data.length];
            for (i = 0; i < data.length; ++i) {
                hashCodes[i] = data[i].hashCode();
                iHashCodes[i] = HashingStrategy.iHash(data[i], sortedIndices);
            }
            iRefine(hashCodes, iHashCodes, contractions, data);
        }

        //calculating connected components
        final int[] components = GraphUtils.calculateConnectedComponents(
                positionsFromInfo(upperInfo), positionsFromInfo(lowerInfo), data.length + 1);
        //the number of components
        final int componentCount = components[components.length - 1];

        final Wrapper[] wrappers = new Wrapper[data.length];
        for (i = 0; i < data.length; ++i)
            wrappers[i] = new Wrapper(data[i].hashCode(), hashCodes[i], iHashCodes[i], components[i + 1]);

        ArraysUtils.quickSort(wrappers, data);

        for (i = 0; i < data.length; ++i) {
            hashCodes[i] = wrappers[i].graphHash;
            iHashCodes[i] = wrappers[i].iGraphHash;
            components[i + 1] = wrappers[i].component;
        }

        calculateInfo(data, freeIndices, info, indices, contractions);
        reCalculateContractions(differentIndicesCount, info, contractions);

        //<- All graph/hash stuff is done.


        final int[] componentSizes = new int[componentCount];
        //finding each component size
        for (i = 1; i < components.length - 1; ++i)
            ++componentSizes[components[i]];

        //allocating resulting datas 0 - is non scalar data
        final Tensor[][] sData = new Tensor[componentCount][];
        for (i = 0; i < componentCount; ++i)
            sData[i] = new Tensor[componentSizes[i]];

        //from here we shall use components sizes as pointers
        Arrays.fill(componentSizes, 0);
        for (i = 1; i < data.length + 1; ++i)
            sData[components[i]][componentSizes[components[i]]++] = data[i - 1];

        Tensor nonScalar = null;
        if (componentCount == 1) //There are no scalar subproducts in this product
            nonScalar = new Product(this.indices, Complex.ONE, new Tensor[0], data, this.contentReference, 0, 0);
        else if (sData[0].length > 0)
            nonScalar = Tensors.multiply(sData[0]);

        final Tensor[] scalars = new Tensor[componentCount - 1];
        if (nonScalar == null && componentCount == 2 && factor == Complex.ONE && indexlessData.length == 0)
            scalars[0] = this;
        else {
            for (i = 1; i < componentCount; ++i)
                scalars[i - 1] = Tensors.multiply(sData[i]);
            Arrays.sort(scalars);
        }

        ProductContent pc = new ProductContent(new StructureOfContractions(contractions), data, hashCodes, iHashCodes, nonScalar, scalars);
        contentReference.resetReferent(pc);

        if (componentCount == 1 && nonScalar instanceof Product)
            ((Product) nonScalar).calculateHash();

        return pc;
    }

    //when all indices are free
    private ProductContent calculateContentWithNoContractions() {
        if (data.length == 1) {
            final ProductContent pc = new ProductContent(getFreeStructure(indices.size()),
                    data, new int[]{data[0].hashCode()}, new int[]{HashingStrategy.iHash(data[0])},
                    data[0], new Tensor[0]);
            this.contentReference.resetReferent(pc);
            return pc;
        }

        final int[] hashCodes = new int[data.length], iHashCodes = new int[data.length];
        final int[] sortedIndices = IndicesUtils.getIndicesNames(this.indices);
        Arrays.sort(sortedIndices);

        int i;
        for (i = 0; i < data.length; ++i) {
            hashCodes[i] = data[i].hashCode();
            iHashCodes[i] = HashingStrategy.iHash(data[i], sortedIndices);
        }

        final int[] components = new int[data.length];
        for (i = 0; i < data.length; ++i)
            components[i] = i;

        final Wrapper[] wrappers = new Wrapper[data.length];
        for (i = 0; i < data.length; ++i)
            wrappers[i] = new Wrapper(data[i].hashCode(), hashCodes[i], iHashCodes[i], components[i]);

        ArraysUtils.quickSort(wrappers, data);

        for (i = 0; i < data.length; ++i) {
            hashCodes[i] = wrappers[i].graphHash;
            iHashCodes[i] = wrappers[i].iGraphHash;
            components[i] = wrappers[i].component;
        }
        final long[][] contractions = new long[data.length][];
        for (i = 0; i < data.length; ++i)
            contractions[i] = getFreeContractions(data[i].getIndices().size());

        final Product nonScalar = new Product(this.indices, Complex.ONE, new Tensor[0], data, this.contentReference, 0, 0);
        ProductContent pc = new ProductContent(new StructureOfContractions(contractions, components, data.length), data, hashCodes, iHashCodes, nonScalar, new Tensor[0]);
        this.contentReference.resetReferent(pc);

        nonScalar.calculateHash();
        return pc;
    }

    private static final long[][] freeContractionsCache;
    private static final StructureOfContractions[] freeStructuresCache;
    private static final int[] singleComponent;

    static {
        final int cacheSize = 64;
        freeContractionsCache = new long[cacheSize][];
        freeStructuresCache = new StructureOfContractions[cacheSize];
        singleComponent = new int[1];
        for (int i = 0; i < freeContractionsCache.length; ++i) {
            freeContractionsCache[i] = getFreeContractions0(i);
            freeStructuresCache[i] = new StructureOfContractions(new long[][]{freeContractionsCache[i]}, singleComponent, 1);
        }
    }

    private static StructureOfContractions getFreeStructure(final int sizeOfIndices) {
        if (sizeOfIndices >= freeContractionsCache.length)
            return new StructureOfContractions(new long[][]{getFreeContractions(sizeOfIndices)}, singleComponent, 1);
        else
            return freeStructuresCache[sizeOfIndices];
    }

    private static long[] getFreeContractions(final int sizeOfIndices) {
        if (sizeOfIndices >= freeContractionsCache.length)
            return getFreeContractions0(sizeOfIndices);
        else
            return freeContractionsCache[sizeOfIndices];
    }

    private static long[] getFreeContractions0(final int sizeOfIndices) {
        final long[] contractions = new long[sizeOfIndices];
        for (int i = 0; i < sizeOfIndices; ++i)
            contractions[i] = freeContraction(i);
        return contractions;
    }

    private static long freeContraction(final int fromIPosition) {
        return 0xFFFFFFFFFFFF0000L | (0xFFFFL & fromIPosition);
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

            contractions[tensorIndex] = new long[tInds.size()];
        }

        //Here we can use unstable sorting algorithm (all indices are different)
        ArraysUtils.quickSort(indices[0], info[0]);
        ArraysUtils.quickSort(indices[1], info[1]);
    }

    static void reCalculateContractions(final int differentIndicesCount,
                                        final long[][] info,
                                        final long[][] contractions) {
        int fromPosition, fromIPosition;
        for (int i = 0; i < differentIndicesCount; ++i) {
            //Contractions from lower to upper
            fromPosition = tPosition(info[0][i]); //From tensor index
            fromIPosition = iPosition(info[0][i]);

            long contraction = contraction(info[0][i], info[1][i]);
            if (fromPosition != -1)
                contractions[fromPosition][fromIPosition] = contraction;

            //Contractions from upper to lower
            fromPosition = tPosition(info[1][i]); //From tensor index
            fromIPosition = iPosition(info[1][i]);

            contraction = contraction(info[1][i], info[0][i]);
            if (fromPosition != -1)
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

    private static int refine(final int[] temp,
                              final int level,
                              final Tensor[] data,
                              final int i,
                              final long[][] contractions,
                              final int[] hashCodes,
                              final boolean doSum) {
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
                        + 2797 * (toIDiffId(contraction) + 1));
                refine(temp, level - 1, data, toPosition, contractions, hashCodes, false);
            }
        }
        if (!doSum)
            return 0;
        if (!freeOnly) {
            for (int j = 0; j < contractions.length; ++j)
                if (i != j)
                    vHash += JenkinWang32shift(temp[j]);
        }
        return vHash - JenkinWang32shift(temp[i]);
    }

    static void iRefine(final int[] hashCodes, final int[] iHashCodes,
                        final long[][] contractions, final Tensor[] data) {
        final int[] temp = new int[data.length], iTemp = new int[data.length];
        final int[] newHashCodes = new int[data.length], newIHashCodes = new int[data.length];
        for (int i = 0; i < data.length; ++i) {
            Arrays.fill(temp, 0);
            Arrays.fill(iTemp, 0);
            final int[] r = iRefine(temp, iTemp, REFINEMENT_LEVEL, data, i, contractions, hashCodes, iHashCodes, true);
            newHashCodes[i] += r[0];
            newIHashCodes[i] += r[1];
        }
        System.arraycopy(newHashCodes, 0, hashCodes, 0, data.length);
        System.arraycopy(newIHashCodes, 0, iHashCodes, 0, data.length);
    }

    private final static int[] zero = {0, 0};

    private static int[] iRefine(final int[] temp,
                                 final int[] iTemp,
                                 final int level,
                                 final Tensor[] data,
                                 final int i,
                                 final long[][] contractions,
                                 final int[] hashCodes,
                                 final int[] iHashCodes,
                                 final boolean doSum) {
        if (level == 0)
            return zero;
        final int jLevel = JenkinWang32shift(level);
        int vHash = 137, iVHash = 139;
        boolean freeOnly = true;
        for (long contraction : contractions[i]) {
            int toPosition = toPosition(contraction);
            short diffId = data[i].getIndices().getPositionsInOrbits()[fromIPosition(contraction)];
            int toAdd;
            if (toPosition == -1) {
                toAdd = JenkinWang32shift(53 * (diffId + 1) + jLevel);
                vHash += toAdd;
                iVHash += toAdd + iHashCodes[i];
            } else {
                freeOnly = false;
                toAdd = JenkinWang32shift(level
                        + 17 * hashCodes[i]
                        + 91 * hashCodes[toPosition]
                        + 3671 * (diffId + 1)
                        + 2797 * (toIDiffId(contraction) + 1));
                temp[toPosition] += toAdd;
                iTemp[toPosition] += toAdd + iHashCodes[i] + 19 * iHashCodes[toPosition];
                iRefine(temp, iTemp, level - 1, data, toPosition, contractions, hashCodes, iHashCodes, false);
            }
        }
        if (!doSum)
            return zero;
        if (!freeOnly) {
            for (int j = 0; j < contractions.length; ++j)
                if (i != j) {
                    vHash += JenkinWang32shift(temp[j]);
                    iVHash += JenkinWang32shift(iTemp[j]);
                }
        }
        return new int[]{vHash - JenkinWang32shift(temp[i]), iVHash - JenkinWang32shift(iTemp[i])};
    }

    static void iRefineIHashCodesOnly(final int[] hashCodes, final int[] iHashCodes,
                                      final long[][] contractions, final Tensor[] data) {
        final int[] iTemp = new int[data.length];
        final int[] newIHashCodes = new int[data.length];
        for (int i = 0; i < data.length; ++i) {
            Arrays.fill(iTemp, 0);
            newIHashCodes[i] += iRefineIHashCodesOnly(iTemp, REFINEMENT_LEVEL, data, i, contractions, hashCodes, iHashCodes, true);
        }
        System.arraycopy(newIHashCodes, 0, iHashCodes, 0, data.length);
    }

    private static int iRefineIHashCodesOnly(final int[] iTemp,
                                             final int level,
                                             final Tensor[] data,
                                             final int i,
                                             final long[][] contractions,
                                             final int[] hashCodes,
                                             final int[] iHashCodes,
                                             final boolean doSum) {
        if (level == 0)
            return 0;
        final int jLevel = JenkinWang32shift(level);
        int iVHash = 139;
        boolean freeOnly = true;
        for (long contraction : contractions[i]) {
            int toPosition = toPosition(contraction);
            short diffId = data[i].getIndices().getPositionsInOrbits()[fromIPosition(contraction)];
            int toAdd;
            if (toPosition == -1) {
                toAdd = JenkinWang32shift(53 * (diffId + 1) + jLevel);
                iVHash += toAdd + iHashCodes[i];
            } else {
                freeOnly = false;
                toAdd = JenkinWang32shift(level
                        + 17 * hashCodes[i]
                        + 91 * hashCodes[toPosition]
                        + 3671 * (diffId + 1)
                        + 2797 * (toIDiffId(contraction) + 1));
                iTemp[toPosition] += toAdd + iHashCodes[i] + 19 * iHashCodes[toPosition];
                iRefineIHashCodesOnly(iTemp, level - 1, data, toPosition, contractions, hashCodes, iHashCodes, false);
            }
        }
        if (!doSum)
            return 0;
        if (!freeOnly) {
            for (int j = 0; j < contractions.length; ++j)
                if (i != j) {
                    iVHash += JenkinWang32shift(iTemp[j]);
                }
        }
        return iVHash - JenkinWang32shift(iTemp[i]);
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

    private static long contraction(long lInfo, long uInfo) {
        return (0xFFFFFFFFFFFF0000L & (uInfo << 16)) | (0xFFFFL & (lInfo));
    }

    private static final long dummyInfo = info(-1, (short) 0, -1);

    private static int tPosition(final long info) {
        return (int) (0xFFFFFFFFL & (info >> 16));
    }

    private static int iPosition(final long info) {
        return (int) (0xFFFFL & (info >> 48));
    }

    private static int[] positionsFromInfo(final long[] info) {
        final int[] result = new int[info.length];
        for (int i = 0; i < info.length; ++i)
            result[i] = tPosition(info[i]) + 1;
        return result;
    }

    private static class Wrapper implements Comparable<Wrapper> {
        final int tensorHash;
        final int graphHash;
        final int iGraphHash;
        final int component;

        private Wrapper(int tensorHash, int graphHash, int iGraphHash, int component) {
            this.tensorHash = tensorHash;
            this.graphHash = graphHash;
            this.component = component;
            this.iGraphHash = iGraphHash;
        }

        @Override
        public int compareTo(final Wrapper o) {
            int c = Integer.compare(tensorHash, o.tensorHash);
            if (c == 0)
                c = Integer.compare(graphHash, o.graphHash);
            if (c == 0)
                c = Integer.compare(iGraphHash, o.iGraphHash);
            if (c == 0)
                c = Integer.compare(component, o.component);
            return c;
        }
    }

//    public ProductContent calculateContent() {
//        if (data.length == 0) {
//            contentReference.resetReferent(ProductContent.EMPTY_INSTANCE);
//            return ProductContent.EMPTY_INSTANCE;
//        }
//        final Indices freeIndices = indices.getFree();
//        final int differentIndicesCount = (getIndices().size() + freeIndices.size()) / 2;
//
//        //Names (names with type, see IndicesUtils.getNameWithType() ) of all indices in this multiplication
//        //It will be used as index name -> index index [0,1,2,3...] mapping
//        final int[] upperIndices = new int[differentIndicesCount], lowerIndices = new int[differentIndicesCount];
//        //This is sorage for intermediate information about indices, used in the algorithm (see below)
//        //Structure:
//        //
//        final long[] upperInfo = new long[differentIndicesCount], lowerInfo = new long[differentIndicesCount];
//
//        //This is for generalization of algorithm
//        //indices[0] == lowerIndices
//        //indices[1] == lowerIndices
//        final int[][] indices = new int[][]{lowerIndices, upperIndices};
//
//        //This is for generalization of algorithm too
//        //info[0] == lowerInfo
//        //info[1] == lowerInfo
//        final long[][] info = new long[][]{lowerInfo, upperInfo};
//
//        //Pointers for lower and upper indices, used in algorithm
//        //pointer[0] - pointer to lower
//        //pointer[1] - pointer to upper
//        final int[] pointer = new int[2];
//        final short[] stretchIndices = calculateStretchIndices(); //for performance
//
//        //Allocating array for results, one contraction for each tensor
//        final TensorContraction[] contractions = new TensorContraction[data.length];
//        //There is one dummy tensor with index -1, it represents fake
//        //tensor contracting with whole Product to leave no contracting indices.
//        //So, all "conractions" with this dummy "contraction" looks like a scalar
//        //product. (sorry for English)
//        final TensorContraction freeContraction = new TensorContraction((short) -1, new long[freeIndices.size()]);
//
//        int state, index, i;
//
//        //Processing free indices = creating contractions for dummy tensor
//        for (i = 0; i < freeIndices.size(); ++i) {
//            index = freeIndices.get(i);
//            //Inverse state (because it is state of index at (??) dummy tensor,
//            //contracted with this free index)
//            state = 1 - IndicesUtils.getStateInt(index);
//            //Important:
//            info[state][pointer[state]] = dummyTensorInfo;
//            indices[state][pointer[state]++] = IndicesUtils.getNameWithType(index);
//        }
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
//            contractions[tensorIndex] = new TensorContraction(stretchIndices[tensorIndex], new long[tInds.size()]);
//        }
//
//        //Here we can use unstable sorting algorithm (all indices are different)
//        ArraysUtils.quickSort(indices[0], info[0]);
//        ArraysUtils.quickSort(indices[1], info[1]);
//
//        //<-- Here we have mature info arrays
//
//        //Processing scalar and non scalar parts
//
//        //Creating input graph components
//        int[] components = GraphUtils.calculateConnectedComponents(
//                infoToTensorIndices(upperInfo), infoToTensorIndices(lowerInfo), data.length + 1);
//
//        //the number of components
//        final int componentCount = components[components.length - 1]; //Last element of this array contains components count
//        //(this is specification of GraphUtils.calculateConnectedComponents method)
//        int[] componentSizes = new int[componentCount];
//
//        //patch for jvm bug (7u4 ~ 7u14)
//        //commented in v1.1.5
//        //Arrays.fill(componentSizes, 0);
//
//        //finding each component size
//        for (i = 1; i < components.length - 1; ++i)
//            ++componentSizes[components[i]];
//
//        //allocating resulting datas 0 - is non scalar data
//        Tensor[][] datas = new Tensor[componentCount][];
//        for (i = 0; i < componentCount; ++i)
//            datas[i] = new Tensor[componentSizes[i]];
//
//        //from here we shall use components sizes as pointers
//        Arrays.fill(componentSizes, 0);
//
//        //writing data
//        for (i = 1; i < data.length + 1; ++i)
//            datas[components[i]][componentSizes[components[i]]++] = data[i - 1];
//
//        Tensor nonScalar = null;
//        if (componentCount == 1) //There are no scalar subproducts in this product
//            if (data.length == 1)
//                nonScalar = data[0];
//            else
//                nonScalar = new Product(this.indices, Complex.ONE, new Tensor[0], data, this.contentReference, 0);
////                nonScalar = new Product(Complex.ONE, new Tensor[0], data, ProductContent.EMPTY_INSTANCE, this.indices);
//        else if (datas[0].length > 0)
//            nonScalar = Tensors.multiply(datas[0]);
//
//        Tensor[] scalars = new Tensor[componentCount - 1];
//
//        if (nonScalar == null && componentCount == 2 && factor == Complex.ONE && indexlessData.length == 0)
//            scalars[0] = this;
//        else {
//            for (i = 1; i < componentCount; ++i)
//                scalars[i - 1] = Tensors.multiply(datas[i]);
//            Arrays.sort(scalars); //TODO use nonstable sort
//        }
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
//                freeContraction.indexContractions[freePointer++] = contraction;
//            else
//                contractions[tensorIndex].indexContractions[pointers[tensorIndex]++] = contraction;
//
//            //Contractions from upper to lower
//            tensorIndex = (int) (info[1][i] >> 32);
//            contraction = (0x0000FFFF00000000L & (info[1][i] << 32))
//                    | (0xFFFFFFFFL & (info[0][i]));
//            if (tensorIndex == -1)
//                freeContraction.indexContractions[freePointer++] = contraction;
//            else
//                contractions[tensorIndex].indexContractions[pointers[tensorIndex]++] = contraction;
//        }
//
//        //Sorting per-index contractions in each TensorContraction
//        for (TensorContraction contraction : contractions)
//            contraction.sortContractions();
//        freeContraction.sortContractions();
//
//        int[] inds = IndicesUtils.getIndicesNames(this.indices.getFree());
//        Arrays.sort(inds);
//        ScaffoldWrapper[] wrappers = new ScaffoldWrapper[contractions.length];
//        for (i = 0; i < contractions.length; ++i)
//            wrappers[i] = new ScaffoldWrapper(inds, components[i + 1], data[i], contractions[i]);
//
//        ArraysUtils.quickSort(wrappers, data);
//
//        for (i = 0; i < contractions.length; ++i)
//            contractions[i] = wrappers[i].tc;
//
//        //Here we can use unstable sort algorithm
//        //ArraysUtils.quickSort(contractions, 0, contractions.length, data);
//
//        //Form resulting content
//
//        //TODO should be lazy field in ProductContent
//        StructureOfContractions structureOfContractions = null;//new StructureOfContractions(data, differentIndicesCount, freeIndices);
//        ProductContent content = new ProductContent(structureOfContractions, scalars, nonScalar, data, null);
//        contentReference.resetReferent(content);
//
//        if (componentCount == 1 && nonScalar instanceof Product) {
//            ((Product) nonScalar).hash = ((Product) nonScalar).calculateHash(); //TODO !!!discuss with Dima!!!
//        }
//        return content;
//    }
//
//    private short[] calculateStretchIndices() {
//        short[] stretchIndex = new short[data.length];
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
}
