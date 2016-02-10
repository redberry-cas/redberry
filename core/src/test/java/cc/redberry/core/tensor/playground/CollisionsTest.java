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
import gnu.trove.set.hash.TIntHashSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.indices.IndexType.*;
import static cc.redberry.core.indices.IndicesFactory.createAlphabetical;
import static cc.redberry.core.indices.IndicesUtils.inverseIndexState;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.tensor.playground.Algorithm0.algorithm0;
import static cc.redberry.core.tensor.playground.Algorithm1.algorithm1;

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
    public void testAlgorithm1_0() throws Exception {
        CC.resetTensorNames(~90012345678L);
        CC.setParserAllowsSameVariance(true);
        Product t = (Product) parse("f_abc*f_apq*f_bpr*f_cqr");

        System.out.println("Original:");
        System.out.println(t.getContent().getStructureOfContractions());
        System.out.println("\n\nAlgorithm1:");
        ProductData pd = algorithm1_shuffle(t);
        System.out.println(pd.content.structureOfContractions);
    }

    @Test
    public void testAlgorithm1_SameHash0() throws Exception {
        setSymmetric("T_ab", "T_abc", "T_abcd");
        for (int k = 0; k < 100; k++) {
            List<Tensor> tensors = generateListOfSameTensors(defaultRandomSource(), 100, 6, 0);
            int hash = algorithm1_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithm1_shuffle(tensors.get(i)).hash);
        }
    }
    @Test
    public void testAlgorithm1_SameHash1() throws Exception {
        setSymmetric("T_ab", "T_abc", "T_abcd");
        for (int k = 0; k < 100; k++) {
            List<Tensor> tensors = generateListOfSameTensors(matrixRandomSource(true), 100, 6, 0);
            int hash = algorithm1_shuffle(tensors.get(0)).hash;
            for (int i = 1; i < tensors.size(); i++)
                Assert.assertEquals(hash, algorithm1_shuffle(tensors.get(i)).hash);
        }
    }
    @Test
    public void testAlgorithm0_HashCollisions0() throws Exception {
        setSymmetric("T_ab", "T_abc");
        List<Tensor> tts = generateListOfDiffTensors(defaultRandomSource(), 5000, 6, 0);
        Assert.assertTrue(tts.size() > 1);
        Assert.assertEquals(tts.size(), sum(tts).size());

        TIntHashSet hashSet_default = new TIntHashSet();
        TIntHashSet hashSet_alg0 = new TIntHashSet();
        for (Tensor tt : tts) {
            hashSet_alg0.add(algorithm0(tt).hash);
            hashSet_default.add(tt.hashCode());
        }

        assertTrue(tts.size() >= hashSet_alg0.size());
        assertEquals(hashSet_default.size(), hashSet_alg0.size());

        System.out.println("Diff: " + tts.size() + "  hash: " + hashSet_alg0.size());
    }


    @Test
    public void testAlgorithm0_HashCollisions0_Traces() throws Exception {
        CC.resetTensorNames(123);
        setUpMatrices();
        List<Tensor> tts = generateListOfDiffTensors(matrixRandomSource(true), 50, 9, 1);
        Assert.assertTrue(tts.size() > 1);
        Assert.assertEquals(tts.size(), sum(tts).size());

        TIntHashSet hashSet_default = new TIntHashSet();
        TIntHashSet hashSet_alg0 = new TIntHashSet();
        for (Tensor tt : tts) {
            hashSet_alg0.add(algorithm0(tt).hash);
            hashSet_default.add(tt.hashCode());
        }

        assertTrue(tts.size() >= hashSet_alg0.size());
        assertEquals(hashSet_default.size(), hashSet_alg0.size());

        System.out.println("Diff: " + tts.size() + "  hash: " + hashSet_alg0.size());
        //Diff: 41  hash: 1
    }

    @Test
    public void testAlgorithm1_HashCollisions0_Traces() throws Exception {
        CC.resetTensorNames(123);
        setUpMatrices();
        List<Tensor> tts = generateListOfDiffTensors(matrixRandomSource(true), 50, 9, 1);
        Assert.assertTrue(tts.size() > 1);
        Assert.assertEquals(tts.size(), sum(tts).size());

        TIntHashSet hashSet_default = new TIntHashSet();
        TIntHashSet hashSet_alg1 = new TIntHashSet();
        for (Tensor tt : tts) {
            hashSet_alg1.add(algorithm1(tt).hash);
            hashSet_default.add(tt.hashCode());
        }

        assertTrue(tts.size() >= hashSet_alg1.size());
//        assertEquals(hashSet_default.size(), hashSet_alg1.size());

        System.out.println("Diff: " + tts.size() + "  hash: " + hashSet_alg1.size());
    }

    @Test
    public void test3() throws Exception {
        RandomSource def = defaultRandomSource();
        for (int i = 0; i < 100; ++i) {
            Tensor t = def.randomProduct(10, 4);
            System.out.println(algorithm0_shuffle(t).hash == algorithm1_shuffle(t).hash);
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

                System.out.println(a++);
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
}