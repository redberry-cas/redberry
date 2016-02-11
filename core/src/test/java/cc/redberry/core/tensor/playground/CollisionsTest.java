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

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.*;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.test.TestUtils;
import cc.redberry.core.utils.TensorUtils;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.indices.IndexType.Matrix1;
import static cc.redberry.core.indices.IndicesFactory.createAlphabetical;
import static cc.redberry.core.indices.IndicesUtils.inverseIndexState;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.tensor.playground.Algorithm0.ALGORITHM_0;
import static cc.redberry.core.tensor.playground.Algorithm0.algorithm0;
import static cc.redberry.core.tensor.playground.Algorithm1.ALGORITHM_1;
import static cc.redberry.core.tensor.playground.Algorithm1.algorithm1;
import static cc.redberry.core.tensor.playground.Algorithm2.ALGORITHM_2;
import static cc.redberry.core.tensor.playground.Algorithm2.algorithm2;
import static cc.redberry.core.tensor.playground.Algorithm3.ALGORITHM_3;
import static cc.redberry.core.tensor.playground.Algorithm3.algorithm3;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@Ignore
public class CollisionsTest {
    @Before
    public void setUp() throws Exception {
        CC.reset();
    }

    @Test
    public void test1() throws Exception {
        List<Tensor> tensors = generateListOfSameTensors(defaultRandomSource(), 10, 4, 2);
        Assert.assertEquals(Product.class, sum(tensors).getClass());
    }

    @Test
    public void test2() throws Exception {
        for (int i = 0; i < TestUtils.its(10, 100); i++) {
            CC.reset();
            setSymmetric("T_ab", "T_abc", "T_abcd");
            List<Tensor> tts = generateListOfDiffTensors(defaultRandomSource(), 100, 7, 0);
            Assert.assertTrue(tts.size() > 1);
            Assert.assertEquals(tts.size(), sum(tts).size());
        }
    }

    @Test
    public void testAlgorithmDefault_SameHash0() throws Exception {
        setSymmetric("T_ab", "T_abc", "T_abcd");
        for (int k = 0; k < 100; k++) {
            List<Tensor> tensors = generateListOfSameTensors(defaultRandomSource(), 100, 6, 0);
            int hash = algorithmDefault_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithmDefault_shuffle(tensors.get(i)).hash);
        }
    }

    @Test
    public void testAlgorithm0_SameHash0() throws Exception {
        setSymmetric("T_ab", "T_abc", "T_abcd");
        for (int k = 0; k < 100; k++) {
            List<Tensor> tensors = generateListOfSameTensors(defaultRandomSource(), 100, 6, 0);
            int hash = algorithm0_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithm0_shuffle(tensors.get(i)).hash);
        }
    }

    @Test
    public void testAlgorithm1_SameHash0() throws Exception {
        setSymmetric("T_ab", "T_abc", "T_abcd");
        for (int k = 0; k < 1000; k++) {
            List<Tensor> tensors = generateListOfSameTensors(defaultRandomSource(), 100, 6, 0);
            int hash = algorithm1_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithm1_shuffle(tensors.get(i)).hash);
        }
    }


    @Test
    public void testAlgorithm2_SameHash0() throws Exception {
        setSymmetric("T_ab", "T_abc", "T_abcd");
        for (int k = 0; k < 100; k++) {
            List<Tensor> tensors = generateListOfSameTensors(defaultRandomSource(), 100, 6, 0);
            int hash = algorithm2_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithm2_shuffle(tensors.get(i)).hash);
        }
    }


    @Test
    public void testAlgorithm3_SameHash0() throws Exception {
        CC.reset();
        testSameHash3(defaultRandomSource(), 0);
        testSameHash3(defaultRandomSource(), 5);

        CC.reset();
        setSymmetric("T_ab", "T_abc", "T_abcd");
        testSameHash3(defaultRandomSource(), 0);
        testSameHash3(defaultRandomSource(), 5);

        CC.reset();
        testSameHash3(randomSource3(), 0);
        testSameHash3(randomSource3(), 5);

        CC.reset();
        setSymmetric("T_ab", "T_abc", "T_abcd");
        testSameHash3(randomSource3(), 0);
        testSameHash3(randomSource3(), 5);

        CC.reset();
        setUpMatrices();
        testSameHash3(matrixRandomSource(true), 0);
        testSameHash3(matrixRandomSource(true), 6);

        CC.reset();
        setUpMatrices();
        testSameHash3(matrixRandomSource(false), 0);
        testSameHash3(matrixRandomSource(false), 6);
    }

    static void testSameHash3(RandomSource source, int iSize) {
        for (int k = 0; k < 1000; k++) {
            List<Tensor> tensors = generateListOfSameTensors(source, 1000, 16, iSize);
            int hash = algorithm3_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithm3_shuffle(tensors.get(i)).hash);
        }
        System.out.println("ok");
    }

    @Test
    public void testAlgorithm2_HashCollisions() throws Exception {
        CC.resetTensorNames(123);
        setSymmetric("T_ab", "T_abc", "T_abcd");
        List<Tensor> tts = generateListOfDiffTensors(randomSource3(), 100, 7, 1);

        TIntHashSet hashSet_default = new TIntHashSet();
        TIntHashSet hashSet_alg0 = new TIntHashSet();
        TIntHashSet hashSet_alg1 = new TIntHashSet();
        TIntHashSet hashSet_alg2 = new TIntHashSet();
        TIntHashSet hashSet_alg3 = new TIntHashSet();

        System.out.println("Calculating hash codes:");
        for (Tensor tt : tts) {
            hashSet_alg0.add(algorithm0(tt).hash);
            hashSet_alg1.add(algorithm1(tt).hash);
            hashSet_alg2.add(algorithm2(tt).hash);
            hashSet_alg3.add(algorithm3(tt).hash);
            hashSet_default.add(tt.hashCode());
        }

        System.out.println("Diff: " + tts.size()
                + "\nAlg0 diff hash: " + hashSet_alg0.size()
                + "\nAlg1 diff hash: " + hashSet_alg1.size()
                + "\nAlg2 diff hash: " + hashSet_alg2.size()
                + "\nAlg3 diff hash: " + hashSet_alg3.size());

        Assert.assertTrue(tts.size() > 1);
        Assert.assertEquals(tts.size(), sum(tts).size());
        assertTrue(tts.size() >= hashSet_alg0.size());
        assertEquals(hashSet_default.size(), hashSet_alg0.size());
    }

    @Test
    public void testAlgorithm2_HashCollisions_Traces() throws Exception {
        CC.resetTensorNames(123);

        List<Tensor> tts = generateListOfDiffTensors(matrixRandomSource(true), 50, 8, 0);

        TIntHashSet hashSet_default = new TIntHashSet();
        TIntHashSet hashSet_alg0 = new TIntHashSet();
        TIntHashSet hashSet_alg1 = new TIntHashSet();
        TIntHashSet hashSet_alg2 = new TIntHashSet();
        TIntHashSet hashSet_alg3 = new TIntHashSet();

        System.out.println("Calculating hash codes:");
        for (Tensor tt : tts) {
            hashSet_alg0.add(algorithm0(tt).hash);
            hashSet_alg1.add(algorithm1(tt).hash);
            hashSet_alg2.add(algorithm2(tt).hash);
            hashSet_alg3.add(algorithm3(tt).hash);
            hashSet_default.add(tt.hashCode());
        }

        System.out.println("Diff: " + tts.size()
                + "\nAlg0 diff hash: " + hashSet_alg0.size()
                + "\nAlg1 diff hash: " + hashSet_alg1.size()
                + "\nAlg2 diff hash: " + hashSet_alg2.size()
                + "\nAlg3 diff hash: " + hashSet_alg3.size());

        Assert.assertTrue(tts.size() > 1);
        Assert.assertEquals(tts.size(), sum(tts).size());
        assertTrue(tts.size() >= hashSet_alg0.size());
        assertEquals(hashSet_default.size(), hashSet_alg0.size());

        //Diff: 41  hash: 1
    }


    @Test
    public void testPerformance1() throws Exception {
        setUpMatrices();
        setSymmetric("T_ab", "T_abc", "T_abcd");
        IAlgorithm[] algorithms = {
                ALGORITHM_DEFAULT,
                ALGORITHM_0,
                ALGORITHM_1,
                ALGORITHM_2,
                ALGORITHM_3
        };
        TIntHashSet[] hashes = new TIntHashSet[algorithms.length];
        TIntLongHashMap[] pSizeStat = new TIntLongHashMap[algorithms.length];
        TIntLongHashMap[] iSizeStat = new TIntLongHashMap[algorithms.length];
        for (int i = 0; i < algorithms.length; i++) {
            hashes[i] = new TIntHashSet();
            pSizeStat[i] = new TIntLongHashMap();
            iSizeStat[i] = new TIntLongHashMap();
        }


        RandomSource source = randomSource3();
        for (int i = 0; i < 1000; i++) {
            Tensor product = source.randomProduct(8, 0);
            for (int j = 0; j < 10; j++)
                for (IAlgorithm algorithm : algorithms)
                    algorithm.calc(product);
        }

        for (int pSize = 3; pSize < 25; ++pSize) {
            for (IAlgorithm algorithm : algorithms)
                algorithm.restart();

            for (int i = 0; i < 1000; i++) {
                Tensor product = source.randomProduct(pSize, pSize % 2);
                for (int j = 0; j < 100; j++)
                    for (int k = 0; k < algorithms.length; k++) {
                        hashes[k].add(algorithms[k].calc(product).hash);
                        iSizeStat[k].adjustOrPutValue(product.getIndices().getNamesOfDummies().length,
                                algorithms[k].timingMillis(),
                                algorithms[k].timingMillis());
                    }
            }

            for (int k = 0; k < algorithms.length; k++)
                pSizeStat[k].put(pSize, algorithms[k].timingMillis());
        }

        System.out.println("\npSize:\n");
        for (int k = 0; k < algorithms.length; k++) {
            TIntLongIterator it = pSizeStat[k].iterator();
            System.out.println();
            System.out.print(algorithms[k].name + "={");
            while (it.hasNext()) {
                it.advance();
                System.out.print("{" + it.key() + ", " + it.value() + "}");
                if (!it.hasNext())
                    break;
                System.out.print(",");
            }
            System.out.print("};");
        }

        System.out.println("\niSize:\n");
        for (int k = 0; k < algorithms.length; k++) {
            TIntLongIterator it = iSizeStat[k].iterator();
            System.out.println();
            System.out.print("i" + algorithms[k].name + "={");
            while (it.hasNext()) {
                it.advance();
                System.out.print("{" + it.key() + ", " + it.value() + "}");
                if (!it.hasNext())
                    break;
                System.out.print(",");
            }
            System.out.print("};");
        }

        System.out.println("\n");
        for (int k = 0; k < algorithms.length; k++) {
            System.out.println(algorithms[k].name + "   diff hash codes: " + hashes[k].size());
        }
    }

    static List<Tensor> generateListOfSameTensors(RandomSource randomSource, int size, int pSize, int freeIndices) {
        Tensor product = ((Product) randomSource.randomProduct(pSize, freeIndices)).getDataSubProduct();
        List<Tensor> tensors = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            product = ApplyIndexMapping.renameDummy(product, product.getIndices().getNamesOfDummies());
            Tensor[] data = product.toArray();
            shuffle(data);
            tensors.add(multiply(data));
        }
        return tensors;
    }

    static List<Tensor> generateListOfDiffTensors(RandomSource randomSource, int size, int pSize, int freeIndices) {
        List<Tensor> tensors = new ArrayList<>();
        out:
        for (int i = 0; i < size; i++) {
            System.out.println(i);
            Tensor candidate = ((Product) randomSource.randomProduct(pSize, freeIndices)).getDataSubProduct();
            if (!tensors.isEmpty() && candidate.size() != pSize)
                continue;
            for (int j = 0; j < tensors.size(); j++)
                if (TensorUtils.equals(tensors.get(j), candidate))
                    continue out;

            tensors.add(candidate);
        }
        return tensors;
    }

    static void shuffleUnsame(Tensor[] array, Indices allIndices) {
        int[] namesOfDummies = IndicesUtils.getSortedDistinctIndicesNames(allIndices.getOfType(LatinLower));
        int[] perm = namesOfDummies.clone();
        Permutations.shuffle(perm);
        Mapping mapping = new Mapping(namesOfDummies, perm);
        for (int i = 0; i < array.length; i++)
            array[i] = mapping.transform(array[i]);
    }


    static void shuffle(Tensor[] array) {
        Permutations.shuffle(array, CC.getRandomGenerator());
        for (int i = 0; i < array.length; i++)
            array[i] = shuffle(array[i]);
    }

    static Tensor shuffle(Tensor tensor) {
        if (!(tensor instanceof SimpleTensor))
            return tensor;
        SimpleTensor st = (SimpleTensor) tensor;
        SimpleIndices indices = st.getIndices();
        Permutation p = indices.getSymmetries().getPermutationGroup().randomElement();
        return simpleTensor(st.getName(), IndicesFactory.createSimple(null, p.permute(indices.toArray())));
    }

    static RandomSource randomSource3() {
        RandomTensor generator = new RandomTensor();
        generator.clearNamespace();

        generator.addToNamespace(parse("T_abc"));
        return defaultRandomSource(generator);
    }

    static RandomSource defaultRandomSource() {
        RandomTensor generator = new RandomTensor();
        generator.clearNamespace();

        generator.addToNamespace(parse("g_ab"));
        generator.addToNamespace(parse("T_a"));
        generator.addToNamespace(parse("T_ab"));
        generator.addToNamespace(parse("T_abc"));
        generator.addToNamespace(parse("T_abcd"));
        return defaultRandomSource(generator);
    }

    static RandomSource defaultRandomSource(final RandomTensor rnd) {
        return new RandomSource() {
            @Override
            public Tensor randomProduct(int pSize, int freeIndices) {
                return rnd.nextProduct(pSize, createAlphabetical(IndexType.LatinLower, freeIndices));
            }
        };
    }

    static RandomSource matrixRandomSource(final boolean doTrace) {
        return new RandomSource() {
            int a = 0;

            @Override
            public Tensor randomProduct(int pSize, int indices) {
                StringBuilder sb = new StringBuilder();
                if (doTrace)
                    sb.append("Tr[");
                for (int i = 0; ; i++) {
                    sb.append("G").append(IndicesUtils.toString(i));
                    if (i == pSize - 1)
                        break;
                    sb.append("*");
                }
                if (doTrace)
                    sb.append("]");
                Tensor line = parse(sb.toString());

                TIntHashSet done = new TIntHashSet();
                while (line.getIndices().getOfType(LatinLower).getFree().size() != indices) {
                    int from = CC.getRandomGenerator().nextInt(pSize);
                    int to = CC.getRandomGenerator().nextInt(pSize);
                    if (from != to && !done.contains(from) && !done.contains(to)) {
                        line = new Mapping(
                                new int[]{from},
                                new int[]{inverseIndexState(to)})
                                .transform(line);
                        done.add(from); done.add(to);
                    }
                }
                int[] free = line.getIndices().getOfType(LatinLower).getFree().toArray();
                Permutations.shuffle(free);
                return new Mapping(free,
                        createAlphabetical(LatinLower, free.length).toArray()).transform(line);
            }
        };
    }

    interface RandomSource {
        Tensor randomProduct(int pSize, int indices);
    }

    static void setUpMatrices() {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), Matrix1);
    }

    static final IAlgorithm ALGORITHM_DEFAULT = new IAlgorithm("   default") {
        @Override
        ProductData calc0(Tensor tensor) {
            if (tensor instanceof Product) {
                Product p = (Product) ((Product) tensor).getDataSubProduct();
                p.calculateContent();
                return new ProductData(null, null, null, p.hashCode());
            }
            throw new RuntimeException();
        }
    };

    static ProductData algorithmDefault_shuffle(final Tensor tensor) {
        if (tensor instanceof Product) {
            Tensor[] dataCopy = ((Product) tensor).getContent().getDataCopy();
            Product p = (Product) Tensors.multiply(dataCopy);
            return new ProductData(p.getContent().getDataCopy(), p.getIndices(), null, p.hashCode());
        }
        throw new RuntimeException();
    }

    static ProductData algorithm0_shuffle(final Tensor tensor) {
        if (tensor instanceof Product) {
            Tensor[] dataCopy = ((Product) tensor).getContent().getDataCopy();
            Permutations.shuffle(dataCopy, CC.getRandomGenerator());
            return algorithm0(dataCopy, tensor.getIndices());
        }
        throw new RuntimeException();
    }

    static ProductData algorithm1_shuffle(final Tensor tensor) {
        if (tensor instanceof Product) {
            Tensor[] dataCopy = ((Product) tensor).getContent().getDataCopy();
            Permutations.shuffle(dataCopy, CC.getRandomGenerator());
            return algorithm1(dataCopy, tensor.getIndices());
        }
        throw new RuntimeException();
    }

    static ProductData algorithm2_shuffle(final Tensor tensor) {
        if (tensor instanceof Product) {
            Tensor[] dataCopy = ((Product) tensor).getContent().getDataCopy();
            Permutations.shuffle(dataCopy, CC.getRandomGenerator());
            return algorithm2(dataCopy, tensor.getIndices());
        }
        throw new RuntimeException();
    }

    static ProductData algorithm3_shuffle(final Tensor tensor) {
        if (tensor instanceof Product) {
            Tensor[] dataCopy = ((Product) tensor).getContent().getDataCopy();
            Permutations.shuffle(dataCopy, CC.getRandomGenerator());
            return algorithm3(dataCopy, tensor.getIndices());
        }
        throw new RuntimeException();
    }
}