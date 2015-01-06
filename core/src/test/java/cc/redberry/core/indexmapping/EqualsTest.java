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
package cc.redberry.core.indexmapping;

import cc.redberry.core.AbstractRedberryTestClass;
import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.groups.permutations.*;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.indices.IndicesUtils;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.indices.StructureOfIndices;
import cc.redberry.core.parser.ParseToken;
import cc.redberry.core.parser.ParseTokenSimpleTensor;
import cc.redberry.core.parser.TokenType;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.random.RandomTensor;
import cc.redberry.core.utils.TensorUtils;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class EqualsTest
        extends AbstractRedberryTestClass {

    private static ParseToken rewriteTensor(Tensor tensor, RandomGenerator rg) {
        String sTensor = tensor.toString(OutputFormat.Redberry);
        int[] dummy = TensorUtils.getAllDummyIndicesT(tensor).toArray();
        int[] permutedDummy = dummy.clone();
        Permutations.shuffle(permutedDummy, rg);
        for (int i = 0; i < dummy.length; ++i)
            sTensor = sTensor.replace(IndicesUtils.toString(dummy[i]), IndicesUtils.toString(permutedDummy[i]));
        ParseToken token = CC.current().getParseManager().getParser().parse(sTensor);
        permute(token, rg);
        return token;
    }

    private static void permute(ParseToken token, RandomGenerator rg) {
        if (token.tokenType == TokenType.Product || token.tokenType == TokenType.Sum)
            Permutations.shuffle(token.content, rg);
        if (token instanceof ParseTokenSimpleTensor) {
            ParseTokenSimpleTensor sToken = (ParseTokenSimpleTensor) token;
            SimpleIndices indices = sToken.indices;

            PermutationGroup pg = CC.getNameManager().mapNameDescriptor(((ParseTokenSimpleTensor) token).name,
                    ((ParseTokenSimpleTensor) token).indices.getStructureOfIndices()).getSymmetries().getPermutationGroup();
            if (pg.isTrivial())
                return;

            Permutation s = RandomPermutation.random(pg.randomSource());
            //todo add antysymmetry!!!

            int[] ind = indices.toArray();
            sToken.indices = IndicesFactory.createSimple(null, s.permute(ind));
        }

        for (ParseToken subToken : token.content)
            permute(subToken, rg);
    }

    @Test
    public void test1() {
        RandomGenerator rnd = CC.getRandomGenerator();
        RandomTensor randomTensor = new RandomTensor(2, 5, new int[]{2, 0, 0, 0}, new int[]{5, 0, 0, 0}, true, true, rnd);

        for (int i = 0; i < 50; ++i) {
            SimpleIndices ind = IndicesFactory.createSimple(null,
                    randomTensor.nextIndices(StructureOfIndices.create((byte) 0, 1 + rnd.nextInt(10))));
            Tensor a = randomTensor.nextProduct(2 + rnd.nextInt(10), ind);
            Tensor b = rewriteTensor(a, rnd).toTensor();
            TAssert.assertEquals(a, b);
        }
    }

    @Test
    public void test2_longTest() {
        testNonTrivialGroups(500, 20, 0, 1);
    }

    @Test
    public void test2() {
        testNonTrivialGroups(50, 15, 0, 1);
    }

    @Test
    public void test3() {
        testNonTrivialGroups(50, 20, 4, 2);
    }


    private static void testNonTrivialGroups(int numberOfTests,
                                             int pSize, int sSize, int treeDepth) {
        RandomGenerator rnd = CC.getRandomGenerator();
        RandomTensor randomTensor = new RandomTensor();
        randomTensor.clearNamespace();

        Tensors.parseSimple("R_abcd").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(1, 0, 2, 3));
        Tensors.parseSimple("R_abcd").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(0, 1, 3, 2));
        Tensors.parseSimple("R_abcd").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(2, 3, 0, 1));

        Tensors.parseSimple("A_abcde").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(new int[][]{{0, 1, 2, 3, 4}}));

        Tensors.parseSimple("B_abcde").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(new int[][]{{1, 3}, {2, 4}}));
        Tensors.parseSimple("B_abcde").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(new int[][]{{0, 1, 2, 4, 3}}));

        Tensors.parseSimple("C_abcde").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(new int[][]{{1, 2, 3, 4}}));
        Tensors.parseSimple("C_abcde").getIndices().getSymmetries().addSymmetry(
                Permutations.createPermutation(new int[][]{{0, 1, 2, 4, 3}}));

        randomTensor.addToNamespace(Tensors.parse("R_abcd", "A_abcde", "B_abcde", "C_abcde"));

        for (int i = 0; i < numberOfTests; ++i) {
            SimpleIndices ind = IndicesFactory.createSimple(null,
                    randomTensor.nextIndices(StructureOfIndices.create((byte) 0, 1 + rnd.nextInt(6))));

            Tensor a = randomTensor.nextTensorTree(RandomTensor.TensorType.Product, treeDepth,
                    2 + (pSize == 0 ? 0 : rnd.nextInt(pSize)),
                    2 + (sSize == 0 ? 0 : rnd.nextInt(sSize)), ind);
            Tensor b = rewriteTensor(a, rnd).toTensor();

            //System.out.println(a);
            //System.out.println(b);
            //System.out.println();
            TAssert.assertEquals(a, b);
        }
    }
}
