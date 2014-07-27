/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.tensor.random;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.NameDescriptor;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexgenerator.IndexGeneratorImpl;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.IntArrayList;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well1024a;
import org.apache.commons.math3.random.Well19937c;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public final class RandomTensor {

    protected final RandomGenerator random;
    protected long seed;
    protected final static byte TYPES_COUNT = 4;
    protected final static byte[] TYPES = {0, 1, 2, 3};
    protected static final int[] ALPHABETS_SIZES = new int[4];

    static {
        for (byte b : TYPES)
            ALPHABETS_SIZES[(int) b] = IndexType.getType(b).getSymbolConverter().maxNumberOfSymbols();
    }

    private final int[] minIndices, maxIndices;
    private final int diffStringNames;
    private final boolean withSymmetries;
    private final List<NameDescriptor> namespace;
    private final int initialNamespaceSize;

    private final boolean generateNewDescriptors;

    public static enum TensorType {
        Product, Sum
    }

    /**
     * Creates random with default values. Same as {@code new RandomTensor(2, 5, new int[]{0, 0, 0, 0}, new int[]{4, 0,
     * 0, 0}, true)}
     */
    public RandomTensor() {
        this(true);
    }

    public RandomTensor(boolean generateNewDescriptors) {
        this(2, 5, new int[]{0, 0, 0, 0}, new int[]{4, 4, 4, 4}, true, generateNewDescriptors);
    }

    /**
     * @param minDiffNDs             min number of different tensors
     * @param maxDiffNDs             max number of different tensors
     * @param minIndices             min number of indices in each tensor.
     * @param maxIndices             max number of indices in each tensor.
     * @param withSymmetries         add symmetries to tensors
     * @param generateNewDescriptors if false then only specified tensors will be used
     * @param random                 random generator
     */
    public RandomTensor(
            int minDiffNDs,
            int maxDiffNDs,
            int[] minIndices,
            int[] maxIndices,
            boolean withSymmetries,
            boolean generateNewDescriptors,
            RandomGenerator random) {
        this.generateNewDescriptors = generateNewDescriptors;
        this.random = random;
        this.random.setSeed(seed = random.nextLong());
        this.minIndices = minIndices;
        this.maxIndices = maxIndices;
        this.withSymmetries = withSymmetries;
        int di = 1, t;
        for (int i = 0; i < TYPES.length; ++i)
            di *= (t = maxIndices[i] - minIndices[i]) == 0 ? 1 : t;
        this.diffStringNames = (maxDiffNDs - minDiffNDs) / di;
//        namespace = new NameDescriptor[minDiffNDs + (int) (0.5 * (maxDiffNDs - minDiffNDs))];//TODO add randomization
        //initial namespace size
        initialNamespaceSize = minDiffNDs + (int) (0.5 * (maxDiffNDs - minDiffNDs));
        namespace = new ArrayList<>(initialNamespaceSize);//TODO add randomization
        generateDescriptors(); //TOOD add weak reference to nameManager and regenerate at CC.resetTensorNames(....)
    }

    /**
     * @param minDiffNDs             minimum number of different tensors
     * @param maxDiffNDs             maximum number of different tensors
     * @param minIndices             minimum number of indices in each tensor.
     * @param maxIndices             maximum number of indices in each tensor.
     * @param withSymmetries         add symmetries to tensors
     * @param generateNewDescriptors if false then only specified tensors will be used
     * @param seed                   random seed
     */
    public RandomTensor(
            int minDiffNDs,
            int maxDiffNDs,
            int[] minIndices,
            int[] maxIndices,
            boolean withSymmetries,
            boolean generateNewDescriptors,
            long seed) {
        this(minDiffNDs, maxDiffNDs, minIndices, maxIndices, withSymmetries, generateNewDescriptors, new Well1024a(seed));
    }

    /**
     * @param minDiffNDs             minimum number of different tensors
     * @param maxDiffNDs             maximum number of different tensors
     * @param minIndices             minimum number of indices in each tensor.
     * @param maxIndices             maximum number of indices in each tensor.
     * @param withSymmetries         add symmetries to tensors
     * @param generateNewDescriptors if false then only specified tensors will be used
     */
    public RandomTensor(
            int minDiffNDs,
            int maxDiffNDs,
            int[] minIndices,
            int[] maxIndices,
            boolean withSymmetries,
            boolean generateNewDescriptors) {
        this(minDiffNDs, maxDiffNDs, minIndices, maxIndices, withSymmetries, generateNewDescriptors, new Well19937c());
    }

    public RandomGenerator getRandom() {
        return random;
    }

    public void clearNamespace() {
        namespace.clear();
    }

    public void reset() {
        random.setSeed(seed = random.nextLong());
        generateDescriptors();
    }

    public void reset(long seed) {
        random.setSeed(this.seed = seed);
        generateDescriptors();
    }

    public final int nextInt(int n) {
        if (n == 0)
            return 0;
        return random.nextInt(n);
    }

    public long getSeed() {
        return seed;
    }

    private void generateDescriptors() {
        if (!generateNewDescriptors)
            return;

        for (int i = 0; i < initialNamespaceSize; ++i) {
            int[] typesCount = new int[TYPES_COUNT];
            for (int j = 0; j < TYPES_COUNT; ++j)
                typesCount[j] = minIndices[j] + nextInt(maxIndices[j] - minIndices[j]);
            StructureOfIndices typeStructure = new StructureOfIndices(TYPES, typesCount);
            NameDescriptor nameDescriptor = CC.getNameManager().mapNameDescriptor(nextName(), typeStructure);
            if (withSymmetries)
                addRandomSymmetries(nameDescriptor);
            namespace.add(nameDescriptor);
        }
    }

    private String nextName() {
        int i = diffStringNames < 10 ? nextInt(10) : nextInt(diffStringNames);
        int second = i / ALPHABETS_SIZES[1];
        int first = i - second * ALPHABETS_SIZES[1];
        if (second == 0)
            return new String(new char[]{(char) (0x41 + first)});
        else
            return new String(new char[]{(char) (0x40 + second), (char) (0x41 + first)});
    }

    public void addToNamespace(Tensor... tensors) {
        //todo check if contains
        for (SimpleTensor st : TensorUtils.getAllDiffSimpleTensors(tensors))
            namespace.add(st.getNameDescriptor());
    }

    public int getInitialNamespaceSize() {
        return initialNamespaceSize;
    }


    public int getNamespaceSize() {
        return namespace.size();
    }

    public NameDescriptor nextNameDescriptor() {
        return namespace.get(nextInt(namespace.size()));
    }

    private NameDescriptor nextNameDescriptor(StructureOfIndices typeStructure) {
        //search
        IntArrayList positions = new IntArrayList();
        for (int i = namespace.size() - 1; i >= 0; --i)
            if (namespace.get(i).getStructureOfIndices().equals(typeStructure))
                positions.add(i);
        if (!positions.isEmpty())
            return namespace.get(positions.get(random.nextInt(positions.size())));

        if (!generateNewDescriptors)
            throw new IllegalArgumentException("No descriptor for such structure.");

        //create new nameDescriptor
        NameDescriptor nameDescriptor = CC.getNameManager().mapNameDescriptor(nextName(), typeStructure);
        if (withSymmetries)
            addRandomSymmetries(nameDescriptor);
        if (namespace.indexOf(nameDescriptor) == -1)
            namespace.add(nameDescriptor);
        return nameDescriptor;
    }

    private void addRandomSymmetries(NameDescriptor descriptor) {//TODO add antisymmetries
        if (!descriptor.getSymmetries().isTrivial()) //todo <= review this moment
            return;
        StructureOfIndices typeStructure = descriptor.getStructureOfIndices();
        int i;
        for (byte type = 0; type < TYPES_COUNT; ++type) {
            StructureOfIndices.TypeData typeData = typeStructure.getTypeData(type);
            if (typeData == null)
                continue;
            if (typeData.length == 0)//redundant
                continue;
            int count = random.nextInt(4);
            for (i = 0; i < count; ++i)
                descriptor.getSymmetries().addSymmetry(type, Permutations.createPermutation(false, nextPermutation(typeData.length)));
        }
    }

    public SimpleTensor nextSimpleTensor(SimpleIndices indices) {
        NameDescriptor nd = nextNameDescriptor(indices.getStructureOfIndices());
        StructureOfIndices structureOfIndices = nd.getStructureOfIndices();
        int[] _indices = nextIndices(structureOfIndices);
        return Tensors.simpleTensor(nd.getId(), IndicesFactory.createSimple(nd.getSymmetries(), _indices));
    }

    public SimpleTensor nextSimpleTensor() {
        NameDescriptor nd = nextNameDescriptor();
        StructureOfIndices structureOfIndices = nd.getStructureOfIndices();
        int[] indices = nextIndices(structureOfIndices);
        return Tensors.simpleTensor(nd.getId(), IndicesFactory.createSimple(nd.getSymmetries(), indices));
    }

    public Tensor nextProduct(int minProductSize, Indices indices) {
        if (minProductSize < 2)
            throw new IllegalArgumentException();
        return nextProductTree(1, new Parameters(0, 0, minProductSize, minProductSize), indices);
    }

    public Tensor nextProduct(int minProductSize) {
        return nextProduct(minProductSize, IndicesFactory.createSimple(null, nextIndices(nextNameDescriptor().getStructureOfIndices())));
    }

    public Tensor nextSum(Parameters parameters, Indices indices) {
        return nextSumTree(2, parameters, indices);
    }

    public Tensor nextSum(int sumSize, int productSize, Indices indices) {//TODO introduce Poisson
        return nextSumTree(2, new Parameters(sumSize, sumSize, productSize, productSize), indices);
    }

    public Tensor nextTensorTree(int depth, Parameters parameters, Indices indices) {
        return nextTensorTree(random.nextBoolean() ? TensorType.Product : TensorType.Sum,
                depth, parameters, indices);
    }

    public Tensor nextTensorTree(TensorType head, int depth, Parameters parameters, Indices indices) {
        if (head == null)
            nextTensorTree(depth, parameters, indices);

        indices = indices.getFree();
        if (depth == 0)
            return nextSimpleTensor(IndicesFactory.createSimple(null, indices));
        if (head == TensorType.Product)
            return nextProductTree(depth, parameters, indices);
        if (head == TensorType.Sum)
            return nextSumTree(depth, parameters, indices);
        throw new RuntimeException();
    }


    protected Tensor nextTensorTree(TensorType head, int depth, Parameters parameters) {
        return nextTensorTree(head, depth, parameters,
                IndicesFactory.createSimple(null, nextIndices(nextNameDescriptor().getStructureOfIndices())));
    }


    public Tensor nextProductTree(int depth, Parameters parameters, Indices indices) {
        int productSize = getRandomValue(parameters.minProductSize, parameters.maxProductSize);
        indices = indices.getFree();
        StructureOfIndices typeStructure = new StructureOfIndices(IndicesFactory.createSimple(null, indices));
        List<Tensor> descriptors = new ArrayList<>();
        int totalIndicesCounts[] = new int[TYPES.length];
        Tensor nd;
        int i;
        for (i = 0; i < productSize; ++i) {
            descriptors.add(nd = nextTensorTree(TensorType.Sum, depth - 1, parameters));
            for (byte b : TYPES) {
                StructureOfIndices.TypeData typeData = IndicesFactory.createSimple(null, nd.getIndices().getFree()).getStructureOfIndices().getTypeData(b);
                if (typeData != null)
                    totalIndicesCounts[b] += typeData.length;
            }
        }

        //if tensors are not not enough (product.indices.size < freeIndices.size)
        for (byte b : TYPES) {
            StructureOfIndices.TypeData typeData = typeStructure.getTypeData(b);
            if (typeData == null)
                continue;
            while (totalIndicesCounts[b] < typeData.length) {
                descriptors.add(nd = nextTensorTree(TensorType.Sum, depth - 1, parameters));
                for (byte bb : TYPES) {
                    StructureOfIndices.TypeData typeData1 = IndicesFactory.createSimple(null, nd.getIndices().getFree()).getStructureOfIndices().getTypeData(bb);
                    if (typeData1 != null)
                        totalIndicesCounts[bb] += typeData1.length;
                }
            }
        }
        //fitting product.indices.size
        for (byte b : TYPES) {
            StructureOfIndices.TypeData typeData = typeStructure.getTypeData(b);
            if ((totalIndicesCounts[b] - (typeData == null ? 0 : typeData.length)) % 2 != 0) {
                int[] typeCount = new int[TYPES.length];
                typeCount[b] = 1;
                descriptors.add(nextTensorTree(TensorType.Sum, depth - 1, parameters, IndicesFactory.createSimple(null, nextIndices(new StructureOfIndices(TYPES, typeCount)))));
                ++totalIndicesCounts[b];
            }
        }

        //Creating indices for Indices instances
        int[] _freeIndices = indices.getFree().getAllIndices().copy();
        int[][] freeIndices = new int[TYPES.length][];
        int[][] indicesSpace = new int[TYPES.length][];
        IndexGeneratorImpl indexGenerator = new IndexGeneratorImpl(_freeIndices.clone());
        for (byte b : TYPES) {
            indicesSpace[b] = new int[totalIndicesCounts[b]];
            StructureOfIndices.TypeData typeData = typeStructure.getTypeData(b);
            if (typeData == null)
                freeIndices[b] = new int[0];
            else {
                freeIndices[b] = new int[typeData.length];
                System.arraycopy(_freeIndices, typeData.from, freeIndices[b], 0, typeData.length);
            }
            int diff = (totalIndicesCounts[b] - freeIndices[b].length) / 2;
            for (i = 0; i < diff; ++i)
                indicesSpace[b][i] = indexGenerator.generate(b);
            for (i = 0; i < diff; ++i)
                indicesSpace[b][i + diff] = IndicesUtils.inverseIndexState(indicesSpace[b][i]);
            System.arraycopy(freeIndices[b], 0, indicesSpace[b], diff * 2, freeIndices[b].length);
            shuffle(indicesSpace[b]);
        }

        TIntHashSet forbidden = new TIntHashSet();
        for (int[] sp : indicesSpace) {
            forbidden.ensureCapacity(sp.length);
            forbidden.addAll(IndicesUtils.getIndicesNames(sp));
        }

        //Creating resulting product
        ProductBuilder pb = new ProductBuilder(10, productSize);
        for (Tensor descriptor : descriptors) {
            StructureOfIndices its = IndicesFactory.createSimple(null, descriptor.getIndices().getFree()).getStructureOfIndices();
            int[] factorIndices = new int[its.size()];
            int position = 0;
            for (byte b : TYPES) {
                StructureOfIndices.TypeData typeData = its.getTypeData(b);
                if (typeData == null)
                    continue;
                for (i = 0; i < typeData.length; ++i)
                    factorIndices[position++] = indicesSpace[b][--totalIndicesCounts[b]];
            }

            descriptor = ApplyIndexMapping.applyIndexMapping(descriptor,
                    new Mapping(descriptor.getIndices().getFree().getAllIndices().copy(), factorIndices), forbidden.toArray());
            descriptor = ApplyIndexMapping.renameDummy(descriptor, forbidden.toArray());
            forbidden.addAll(TensorUtils.getAllIndicesNamesT(descriptor));

            pb.put(descriptor);
        }


        if (random.nextBoolean()) {
            Complex factor = new Complex(1 + nextInt(100));
            factor = random.nextBoolean() ? factor : factor.negate();
            pb.put(factor);
        }
        return pb.build();
    }


    public Tensor nextSumTree(int depth, Parameters parameters, Indices indices) {
        int sumSize = getRandomValue(parameters.minSumSize, parameters.maxSumSize);
        TensorBuilder sum = new SumBuilder();
        for (int i = 0; i < sumSize; ++i)
            sum.put(nextTensorTree(TensorType.Product, depth - 1, parameters, indices));
        return sum.build();
    }

    public int[] nextIndices(StructureOfIndices structureOfIndices) {
        int[] indices = new int[structureOfIndices.size()];
        int[] typeInd;
        int p = 0;
        for (byte b : TYPES) {
            StructureOfIndices.TypeData typeData = structureOfIndices.getTypeData(b);
            if (typeData == null)
                continue;
            typeInd = new int[typeData.length];
            int i;
            int contracted = (contracted = nextInt(indices.length / 2)) == 0 ? 1 : contracted;
            for (i = 0; i < typeInd.length / contracted; ++i)
                typeInd[i] = IndicesUtils.setType(b, i);
            if (i - contracted < 0)
                contracted = i;
            for (; i < typeInd.length; ++i)
                typeInd[i] = IndicesUtils.createIndex(i - contracted, b, true);
            shuffle(typeInd);
            System.arraycopy(typeInd, 0, indices, p, typeInd.length);
            p += typeInd.length;
        }
        return indices;
    }

    public int[] nextPermutation(final int dimension) {
        return Permutations.randomPermutation(dimension, random);
    }

    public final void shuffle(final int[] target) {
        if (target.length < 2)
            return;
        int p1, p2;
        for (int i = 0; i < target.length; ++i) {
            while ((p1 = nextInt(target.length)) == (p2 = nextInt(target.length))) ;
            swap(target, p1, p2);
        }
    }

    private static void swap(int[] a, int p1, int p2) {
        int c = a[p1];
        a[p1] = a[p2];
        a[p2] = c;
    }

    private int getRandomValue(int min, int max) {
        if (min == max)
            return min;
        return min + random.nextInt(max - min);
    }

    public static class Parameters {
        final int minSumSize, maxSumSize, minProductSize, maxProductSize;

        public Parameters(int minSumSize, int maxSumSize, int minProductSize, int maxProductSize) {
            this.minSumSize = minSumSize;
            this.maxSumSize = maxSumSize;
            this.minProductSize = minProductSize;
            this.maxProductSize = maxProductSize;
        }
    }

    public Tensor nextTensorTree(int depth, int productSize, int sumSize, Indices indices) {
        return nextTensorTree(depth, new Parameters(sumSize, sumSize, productSize, productSize), indices);
    }

    public Tensor nextTensorTree(TensorType head, int depth, int productSize, int sumSize, Indices indices) {
        return nextTensorTree(head, depth, new Parameters(sumSize, sumSize, productSize, productSize), indices);
    }

    public Tensor nextSumTree(int depth, int productSize, int sumSize, Indices indices) {
        return nextSumTree(depth, new Parameters(sumSize, sumSize, productSize, productSize), indices);
    }

    public Tensor nextProductTree(int depth, int productSize, int sumSize, Indices indices) {
        return nextProductTree(depth, new Parameters(sumSize, sumSize, productSize, productSize), indices);
    }
}
