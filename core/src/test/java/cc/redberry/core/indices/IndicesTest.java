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
package cc.redberry.core.indices;

import cc.redberry.core.combinatorics.IntPermutationsGenerator;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.ContextManager;
import cc.redberry.core.context.ContextSettings;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.utils.IntArrayList;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.tensor.Tensors.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @since 1.0
 */
public class IndicesTest {

    public IndicesTest() {
    }

    @Test
    public void testGetSymmetries() {
        SimpleTensor t = (SimpleTensor) parse("g_mn");
        addSymmetry(t, IndexType.LatinLower, false, 1, 0);
        assertTrue(CC.getNameDescriptor(t.getName()).getSymmetries() == t.getIndices().getSymmetries());
    }

    @Test
    public void testGetUpper1() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices upper = ParserIndices.parseSimple("^a");
        assertTrue(indices.getFree().getUpper().equals(upper.getAllIndices()));
    }

    @Test
    public void testGetLower() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices upper = ParserIndices.parseSimple("_m");
        assertTrue(indices.getFree().getLower().equals(upper.getAllIndices()));
    }

    @Test
    public void testGetFreeIndices() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices free = ParserIndices.parseSimple("_m^a");
        assertTrue(indices.getFree().equalsRegardlessOrder(free));

        Indices indices1 = parse("g_mn^abn_b").getIndices(); //ordered indices
        assertTrue(indices1.getFree().equalsRegardlessOrder(free));
    }

    @Test
    public void testGetInverseIndices1() {
        Indices indices = parse("g_mn*T^ab*D^n_b").getIndices(); //sorted indices
        Indices inverse = indices.getInverted();
        Indices expected = ParserIndices.parseSimple("^mn_abn^b");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        expected = IndicesFactory.create(expected);
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));

        SimpleIndices indices1 = (SimpleIndices) parse("g_mn^abn_b").getIndices(); //ordered indices
        assertTrue(indices1.getInverted().equalsRegardlessOrder(expected));
        assertTrue(indices1.getSymmetries() == indices1.getInverted().getSymmetries());
    }

    @Test
    public void testGetInverseIndices2() {
        Indices indices = parse("g_mn*T_ab").getIndices(); //sorted indices
        Indices inverse = indices.getInverted();
        Indices expected = ParserIndices.parseSimple("^abmn");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));
    }

    @Test
    public void testGetInverseIndices3() {
        Indices indices = parse("g^mn*T^ab").getIndices(); //sorted indices
        Indices inverse = indices.getInverted();
        Indices expected = ParserIndices.parseSimple("_abmn");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));
    }

    @Test
    public void testGetInverseIndices4() {
        Indices indices = parse("g_n*T^a*D_bzx").getIndices(); //sorted indices
        Indices inverse = indices.getInverted();
        Indices expected = ParserIndices.parseSimple("^n_a^bzx");

        assertTrue(expected.equalsRegardlessOrder(inverse));
        expected = IndicesFactory.create(expected);
        assertTrue(inverse.getLower().equals(expected.getLower()));
        assertTrue(inverse.getUpper().equals(expected.getUpper()));
    }

    @Test(expected = InconsistentIndicesException.class)
    public void testTestConsistent1() {
        //_dd
        Indices indices = ParserIndices.parseSimple("^abcio_sdd");
        indices.testConsistentWithException();
    }

    @Test(expected = InconsistentIndicesException.class)
    public void testTestConsistent2() {
        //_dd^d
        Indices indices1 = ParserIndices.parseSimple("^abcio_sdd^d");
        indices1.testConsistentWithException();
    }
//    @Test
//    public void applyEmptyIndexMapping() {
//        IndexMappingImpl im = new IndexMappingImpl();
//        Indices indices = CC.parse("G_MN").getIndices();
//        Indices copy = indices.clone();
//        indices.applyIndexMapping(im);
//        assertTrue(indices.equals(copy));
//    }

    @Test
    public void testGetByTypeSimpleIndices() {
        RandomTensor randomTensor = new RandomTensor(100,
                1000,
                new int[]{0, 0, 0, 0},
                new int[]{10, 10, 10, 10},
                false, true, new Well19937c());
        StructureOfIndices typeStructure;
        Indices indices;
        SimpleIndicesBuilder builder;
        for (int i = 0; i < 1000; ++i) {
            builder = new SimpleIndicesBuilder();
            typeStructure = randomTensor.nextNameDescriptor().getStructureOfIndices();
            indices = IndicesFactory.createSimple(null, randomTensor.nextIndices(typeStructure));
            int typeCount;
            for (int k = 0; k < IndexType.TYPES_COUNT; ++k) {
                typeCount = typeStructure.typeCount((byte) k);
                Assert.assertEquals(typeCount, indices.size(IndexType.getType((byte) k)));
                if (typeCount == 0)
                    continue;
                for (int p = 0; p < typeCount; ++p) {
                    int index = indices.get(IndexType.getType((byte) k), p);
                    Assert.assertEquals(IndicesUtils.getType(index), (byte) k);
                    builder.append(IndicesFactory.createSimple(null, index));
                }
            }
            Assert.assertEquals(indices, builder.getIndices());
        }
    }

    @Test
    public void testGetByTypeSortedIndices() {
        RandomTensor randomTensor = new RandomTensor(100,
                1000,
                new int[]{0, 0, 0, 0},
                new int[]{10, 10, 10, 10},
                false, true, new Well19937c());
        StructureOfIndices typeStructure;
        Indices indices;
        IndicesBuilder builder;
        for (int i = 0; i < 1000; ++i) {
            builder = new IndicesBuilder();
            typeStructure = randomTensor.nextNameDescriptor().getStructureOfIndices();
            indices = IndicesFactory.create(randomTensor.nextIndices(typeStructure));
            int typeCount;
            for (int k = 0; k < IndexType.TYPES_COUNT; ++k) {
                typeCount = typeStructure.typeCount((byte) k);
                Assert.assertEquals(typeCount, indices.size(IndexType.getType((byte) k)));
                if (typeCount == 0)
                    continue;
                for (int p = 0; p < typeCount; ++p) {
                    int index = indices.get(IndexType.getType((byte) k), p);
                    Assert.assertEquals(IndicesUtils.getType(index), (byte) k);
                    builder.append(IndicesFactory.createSimple(null, index));
                }
            }
            Assert.assertEquals(indices, builder.getIndices());
        }
    }

    @Test
    public void testGetOfTypeSimpleIndices() {
        RandomTensor randomTensor = new RandomTensor(100,
                1000,
                new int[]{0, 0, 0, 0},
                new int[]{10, 10, 10, 10},
                false, true, new Well19937c());
        StructureOfIndices typeStructure;
        Indices indices;
        for (int i = 0; i < 1000; ++i) {
            typeStructure = randomTensor.nextNameDescriptor().getStructureOfIndices();
            indices = IndicesFactory.createSimple(null, randomTensor.nextIndices(typeStructure));
            IndexType indexType;
            int sizeOfType;
            for (byte type = 0; type < IndexType.TYPES_COUNT; ++type) {
                indexType = IndexType.getType(type);
                Indices ofType = indices.getOfType(indexType);
                IntArrayList sb = new IntArrayList();
                sizeOfType = indices.size(indexType);
                for (int k = 0; k < sizeOfType; ++k)
                    sb.add(indices.get(indexType, k));
                Assert.assertEquals(ofType, IndicesFactory.createSimple(null, sb.toArray()));
            }
        }
    }

    @Test
    public void testGetOfTypeSortedIndices() {
        RandomTensor randomTensor = new RandomTensor(100,
                1000,
                new int[]{0, 0, 0, 0},
                new int[]{10, 10, 10, 10},
                false, true, new Well19937c());
        StructureOfIndices typeStructure;
        Indices indices;
        for (int i = 0; i < 1000; ++i) {
            typeStructure = randomTensor.nextNameDescriptor().getStructureOfIndices();
            indices = IndicesFactory.create(randomTensor.nextIndices(typeStructure));
            IndexType indexType;
            int sizeOfType;
            for (byte type = 0; type < IndexType.TYPES_COUNT; ++type) {
                indexType = IndexType.getType(type);
                Indices ofType = indices.getOfType(indexType);
                IntArrayList sb = new IntArrayList();
                sizeOfType = indices.size(indexType);
                for (int k = 0; k < sizeOfType; ++k)
                    sb.add(indices.get(indexType, k));
                Assert.assertEquals(ofType, IndicesFactory.create(sb.toArray()));
            }
        }
    }

    @Test
    public void testDiffIds1() {
        SimpleTensor r = parseSimple("R_abcd");
        addSymmetry(r, IndexType.LatinLower, false, 2, 3, 0, 1);
        addSymmetry(r, IndexType.LatinLower, true, 1, 0, 2, 3);
        short[] diffIds = r.getIndices().getPositionsInOrbits();
        short[] expected = new short[4];
        Assert.assertTrue(Arrays.equals(diffIds, expected));
    }

    @Test
    public void testDiffIds2() {
        Permutation[] symmetries = new Permutation[]{
                new PermutationOneLine(false, new int[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
                new PermutationOneLine(false, new int[]{0, 2, 1, 3, 4, 5, 6, 7, 8, 9, 10, 11}),
                new PermutationOneLine(false, new int[]{0, 1, 3, 2, 4, 5, 6, 7, 8, 9, 10, 11}),
                new PermutationOneLine(false, new int[]{0, 1, 2, 4, 3, 5, 6, 7, 8, 9, 10, 11}),
                new PermutationOneLine(false, new int[]{0, 1, 2, 3, 5, 4, 6, 7, 8, 9, 10, 11}),
                new PermutationOneLine(false, new int[]{0, 1, 2, 3, 4, 6, 7, 11, 10, 9, 8, 5}),
                new PermutationOneLine(false, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 10, 9})
        };
        IntPermutationsGenerator gen = new IntPermutationsGenerator(symmetries.length);
        int[] p;
        while (gen.hasNext()) {
            CC.resetTensorNames();
            SimpleTensor r = parseSimple("R_abcdefghijkl");
            p = gen.next();
            for (int i = 0; i < p.length; ++i)
                r.getIndices().getSymmetries().add(symmetries[p[i]]);

            short[] diffIds = r.getIndices().getPositionsInOrbits();
            short[] expected = {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0};
            Assert.assertTrue(Arrays.equals(diffIds, expected));
        }
    }

    @Test
    public void testToString1() {
        Indices indices = ParserIndices.parseSimple("_{\\alpha \\beta a}^{\\gamma cd \\Gamma}");
        Assert.assertEquals(indices.toString(), "_{a}^{cd}_{\\alpha\\beta}^{\\gamma\\Gamma}");
        Assert.assertEquals(indices.toString(OutputFormat.WolframMathematica), "-a,c,d,-\\[Alpha],-\\[Beta],\\[Gamma],\\[CapitalGamma]");
        Assert.assertEquals(indices.toString(OutputFormat.Maple), "a,~c,~d,alpha,beta,~gamma,~Gamma");
        Assert.assertEquals(indices.toString(OutputFormat.Cadabra), "_{a c d \\alpha \\beta \\gamma \\Gamma}");
    }

    @Test
    public void testToString2() {
        ContextSettings settings = new ContextSettings(OutputFormat.Redberry, "d");
        settings.addMetricIndexType(IndexType.LatinLower);
        ContextManager.initializeNew(settings);

        Indices indices = ParserIndices.parseSimple("_{a}^bc_A^B_CD^EF");
        Assert.assertEquals(indices.toString(OutputFormat.Cadabra), "_{a b c}_{A}^{B}_{C D}^{E F}");
    }

    @Test
    public void testDummiesOfSorted() {
        RandomTensor randomTensor = new RandomTensor(100,
                1000,
                new int[]{0, 0, 0, 0},
                new int[]{10, 10, 10, 10},
                false, true, new Well19937c());
        StructureOfIndices typeStructure;
        Indices indices;
        for (int i = 0; i < 1000; ++i) {
            typeStructure = randomTensor.nextNameDescriptor().getStructureOfIndices();
            indices = IndicesFactory.create(randomTensor.nextIndices(typeStructure));
            TIntHashSet dummies = new TIntHashSet(IndicesUtils.getIndicesNames(indices));
            dummies.removeAll(IndicesUtils.getIndicesNames(indices.getFree()));
            int[] _dummies = dummies.toArray();
            Arrays.sort(_dummies);
            Assert.assertArrayEquals(_dummies, indices.getNamesOfDummies());
        }
    }

    @Test
    public void testDummiesOfSimple() {
        RandomTensor randomTensor = new RandomTensor(100,
                1000,
                new int[]{0, 0, 0, 0},
                new int[]{10, 10, 10, 10},
                false, true, new Well19937c());
        StructureOfIndices typeStructure;
        Indices indices;
        for (int i = 0; i < 1000; ++i) {
            typeStructure = randomTensor.nextNameDescriptor().getStructureOfIndices();
            indices = IndicesFactory.createSimple(null, randomTensor.nextIndices(typeStructure));
            TIntHashSet dummies = new TIntHashSet(IndicesUtils.getIndicesNames(indices));
            dummies.removeAll(IndicesUtils.getIndicesNames(indices.getFree()));
            int[] _dummies = dummies.toArray();
            Arrays.sort(_dummies);
            int[] __dummies = indices.getNamesOfDummies();
            Arrays.sort(__dummies);
            Assert.assertArrayEquals(_dummies, __dummies);
        }
    }
}
