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
package cc.redberry.core.tensor.random;

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.context.*;
import cc.redberry.core.indexgenerator.IndexGenerator;
import cc.redberry.core.indices.*;
import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.random.BitsStreamGenerator;
import org.apache.commons.math3.random.Well19937c;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public final class TRandom {

    protected final BitsStreamGenerator random;
    protected long seed;
    protected final static byte[] TYPES = {0, 1, 2, 3};
    protected static final int[] ALPHABETS_SIZES = new int[4];

    static {
        for (byte b : TYPES)
            ALPHABETS_SIZES[(int) b] = IndexType.getType(b).getSymbolConverter().maxSymbolsCount();
    }
    private final int[] minIndices, maxIndices;
    private final int diffStringNames;
    private final boolean withSymmetries;
    private NameDescriptor[] namespace;

    /**
     *
     * @param minDiffNDs     minimum number of different tensors
     * @param maxDiffNDs     maximum number of different tensors
     * @param minIndices     minimum number of indices in each tensor.
     * @param maxIndices     maximum number of indices in each tensor.
     * @param withSymmetries add symmetries to tensors
     */
    public TRandom(
            int minDiffNDs,
            int maxDiffNDs,
            int[] minIndices,
            int[] maxIndices,
            boolean withSymmetries,
            BitsStreamGenerator random) {
        this.random = random;
        this.random.setSeed(seed = random.nextLong());
        this.minIndices = minIndices;
        this.maxIndices = maxIndices;
        this.withSymmetries = withSymmetries;
        int di = 1, t;
        for (int i = 0; i < TYPES.length; ++i)
            di *= (t = maxIndices[i] - minIndices[i]) == 0 ? 1 : t;
        this.diffStringNames = (maxDiffNDs - minDiffNDs) / di;
        namespace = new NameDescriptor[minDiffNDs + (int) (0.5 * (maxDiffNDs - minDiffNDs))];//TODO add randomization
        generateDescriptors(); //TOOD add weak reference to nameManager and regenerate at CC.resetTensorNames(....)
    }

    /**
     *
     * @param minDiffNDs     minimum number of different tensors
     * @param maxDiffNDs     maximum number of different tensors
     * @param minIndices     minimum number of indices in each tensor.
     * @param maxIndices     maximum number of indices in each tensor.
     * @param withSymmetries add symmetries to tensors
     */
    public TRandom(
            int minDiffNDs,
            int maxDiffNDs,
            int[] minIndices,
            int[] maxIndices,
            boolean withSymmetries,
            long seed) {
        this.random = new Well19937c();
        this.random.setSeed(seed = random.nextLong());
        this.minIndices = minIndices;
        this.maxIndices = maxIndices;
        this.withSymmetries = withSymmetries;
        int di = 1, t;
        for (int i = 0; i < TYPES.length; ++i)
            di *= (t = maxIndices[i] - minIndices[i]) == 0 ? 1 : t;
        this.diffStringNames = (maxDiffNDs - minDiffNDs) / di;
        namespace = new NameDescriptor[minDiffNDs + (int) (0.5 * (maxDiffNDs - minDiffNDs))];//TODO add randomization
        generateDescriptors();
    }

    /**
     *
     * @param minDiffNDs     minimum number of different tensors
     * @param maxDiffNDs     maximum number of different tensors
     * @param minIndices     minimum number of indices in each tensor.
     * @param maxIndices     maximum number of indices in each tensor.
     * @param withSymmetries add symmetries to tensors
     */
    public TRandom(
            int minDiffNDs,
            int maxDiffNDs,
            int[] minIndices,
            int[] maxIndices,
            boolean withSymmetries) {
        this(minDiffNDs, maxDiffNDs, minIndices, maxIndices, withSymmetries, new Well19937c());
    }

    public final void reset() {
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
        for (int i = 0; i < namespace.length; ++i) {
            int[] typesCount = new int[IndexType.TYPES_COUNT];
            for (int j = 0; j < IndexType.TYPES_COUNT; ++j)
                typesCount[j] = minIndices[j] + nextInt(maxIndices[j] - minIndices[j]);
            IndicesTypeStructure typeStructure = new IndicesTypeStructure(TYPES, typesCount);
            NameDescriptor nameDescriptor = CC.getNameManager().mapNameDescriptor(nextName(), typeStructure);
            if (withSymmetries)
                addRandomSymmetries(nameDescriptor);
            namespace[i] = nameDescriptor;
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

    public NameDescriptor nextNameDescriptor() {
        return namespace[nextInt(namespace.length)];
    }

    private NameDescriptor nextNameDescriptor(IndicesTypeStructure typeStructure) {
        NameDescriptor nameDescriptor = CC.getNameManager().mapNameDescriptor(nextName(), typeStructure);
        if (withSymmetries)
            addRandomSymmetries(nameDescriptor);
        return nameDescriptor;
    }

    private void addRandomSymmetries(NameDescriptor descriptor) {//TODO add antisymmetries
        if (!descriptor.getSymmetries().isEmpty())
            return;
        IndicesTypeStructure typeStructure = descriptor.getIndicesTypeStructure();
        int i;
        for (byte type = 0; type < IndexType.TYPES_COUNT; ++type) {
            IndicesTypeStructure.TypeData typeData = typeStructure.getTypeData(type);
            if (typeData == null)
                continue;
            if (typeData.length == 0)//redundant
                continue;
            int cpunt = random.nextInt(4);
            for (i = 0; i < cpunt; ++i)
                descriptor.getSymmetries().addUnsafe(type, new Symmetry(nextPermutation(typeData.length), false));
        }
    }

    public SimpleTensor nextSimpleTensor() {
        NameDescriptor nd = nextNameDescriptor();
        IndicesTypeStructure indicesTypeStructure = nd.getIndicesTypeStructure();
        int[] indices = nextIndices(indicesTypeStructure);
        return Tensors.simpleTensor(nd.getId(), IndicesFactory.createSimple(nd.getSymmetries(), indices));
    }

    public Tensor nextProduct(int minProductSize, Indices indices) {
        if (minProductSize < 2)
            throw new IllegalArgumentException();//CHECKSTYLE
        indices = indices.getFreeIndices();
        IndicesTypeStructure typeStructure = new IndicesTypeStructure(IndicesFactory.createSimple(null, indices));
        List<NameDescriptor> descriptors = new ArrayList<>();
        int totalIndicesCounts[] = new int[TYPES.length];
        NameDescriptor nd;
        int i;
        for (i = 0; i < minProductSize; ++i) {
            descriptors.add(nd = nextNameDescriptor());
            for (byte b : TYPES) {
                IndicesTypeStructure.TypeData typeData = nd.getIndicesTypeStructure().getTypeData(b);
                if (typeData != null)
                    totalIndicesCounts[b] += typeData.length;
            }
        }

        //if tensors are not not enough (product.indices.size < freeIndices.size)
        for (byte b : TYPES) {
            IndicesTypeStructure.TypeData typeData = typeStructure.getTypeData(b);
            if (typeData == null)
                continue;
            while (totalIndicesCounts[b] < typeData.length) {
                descriptors.add(nd = nextNameDescriptor());
                for (byte bb : TYPES) {
                    IndicesTypeStructure.TypeData typeData1 = nd.getIndicesTypeStructure().getTypeData(bb);
                    if (typeData1 != null)
                        totalIndicesCounts[bb] += typeData1.length;
                }
            }
        }
        //fiting product.indices.size
        for (byte b : TYPES) {
            IndicesTypeStructure.TypeData typeData = typeStructure.getTypeData(b);
            if ((totalIndicesCounts[b] - (typeData == null ? 0 : typeData.length)) % 2 != 0) {
                int[] typeCount = new int[TYPES.length];
                typeCount[b] = 1;
                descriptors.add(nextNameDescriptor(new IndicesTypeStructure(TYPES, typeCount)));
                ++totalIndicesCounts[b];
            }
        }

        //Creating indices for Indices instances
        int[] _freeIndices = indices.getFreeIndices().getAllIndices().copy();
        int[][] freeIndices = new int[TYPES.length][];
        int[][] indicesSpace = new int[TYPES.length][];
        IndexGenerator indexGenerator = new IndexGenerator(_freeIndices.clone());
        for (byte b : TYPES) {
            indicesSpace[b] = new int[totalIndicesCounts[b]];
            IndicesTypeStructure.TypeData typeData = typeStructure.getTypeData(b);
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

        //Creating resulting product
        ProductBuilder pb = new ProductBuilder();
        for (NameDescriptor descriptor : descriptors) {
            IndicesTypeStructure its = descriptor.getIndicesTypeStructure();
            int[] factorIndices = new int[its.size()];
            int position = 0;
            for (byte b : TYPES) {
                IndicesTypeStructure.TypeData typeData = its.getTypeData(b);
                if (typeData == null)
                    continue;
                for (i = 0; i < typeData.length; ++i)
                    factorIndices[position++] = indicesSpace[b][--totalIndicesCounts[b]];
            }

            pb.put(Tensors.simpleTensor(descriptor.getId(), IndicesFactory.createSimple(descriptor.getSymmetries(), factorIndices)));
        }
        if (random.nextBoolean())
            pb.put(new Complex(1 + nextInt(100)));
        return pb.build();
    }

    public Tensor nextProduct(int minProductSize) {
        return nextProduct(minProductSize, IndicesFactory.createSimple(null, nextIndices(nextNameDescriptor().getIndicesTypeStructure())));
    }

    public Tensor nextSum(int sumSize, int averageProductSize, Indices indices) {//TODO introduce Poisson 
        TensorBuilder sum = new SumBuilder(sumSize);
        for (int i = 0; i < sumSize; ++i)
            sum.put(nextProduct(averageProductSize, indices));
        return sum.build();
    }

    public int[] nextIndices(IndicesTypeStructure indicesTypeStructure) {
        int[] indices = new int[indicesTypeStructure.size()];
        int[] typeInd;
        int p = 0;
        for (byte b : TYPES) {
            IndicesTypeStructure.TypeData typeData = indicesTypeStructure.getTypeData(b);
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
        if (dimension == 0)
            return new int[0];
        int[] permutation = new int[dimension];
        if (dimension == 1)
            return permutation;
        if (dimension == 2) {
            permutation[1] = 1;
            if (random.nextBoolean())
                swap(permutation, 0, 1);
            return permutation;
        }
        int i, r = nextInt(1000);
        //cycle permutation
        if (r < 100) {
            for (i = 0; i < dimension - 1; ++i)
                permutation[i] = i + 1;
            permutation[dimension - 1] = 0;
            return permutation;
        }
        for (i = 1; i < dimension; ++i)
            permutation[i] = i;
        //else composition of transpositions
        if (r < 700) {
            int p1, p2;
            final int tries = nextInt(3) + 1;
            for (i = 0; i < tries; ++i) {
                while ((p1 = nextInt(dimension)) == (p2 = nextInt(dimension)));
                swap(permutation, p1, p2);
            }
        }
        //else identity
        return permutation;
    }

    public final void shuffle(final int[] target) {
        if (target.length < 2)
            return;
        int p1, p2;
        for (int i = 0; i < target.length; ++i) {
            while ((p1 = nextInt(target.length)) == (p2 = nextInt(target.length)));
            swap(target, p1, p2);
        }
    }

    private static void swap(int[] a, int p1, int p2) {
        int c = a[p1];
        a[p1] = a[p2];
        a[p2] = c;
    }
}
