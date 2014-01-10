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

import cc.redberry.core.TAssert;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.SimpleTensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SimpleIndicesBuilderTest {


    @Test
    public void test1() {
        SimpleIndices indices1 = ParserIndices.parseSimple("_ab");
        indices1.getSymmetries().add(IndexType.LatinLower, true, 1, 0);
        SimpleIndices indices2 = ParserIndices.parseSimple("_cd");
        indices2.getSymmetries().add(IndexType.LatinLower, false, 1, 0);
        SimpleIndicesBuilder builder = new SimpleIndicesBuilder();
        builder.append(indices1);
        builder.append(indices2);
        SimpleIndices indices = builder.getIndices();

        SimpleIndices expected = ParserIndices.parseSimple("_{abcd}");
        expected.getSymmetries().add(IndexType.LatinLower, false, 0, 1, 3, 2);
        expected.getSymmetries().add(IndexType.LatinLower, true, 1, 0, 2, 3);

        assertEquals(expected, indices);
        TAssert.assertEquals(indices.getSymmetries().getPermutationGroup(),
                expected.getSymmetries().getPermutationGroup());
    }

    @Test
    public void test2() {
        SimpleIndices indices1 = ParserIndices.parseSimple("_Aab");
        indices1.getSymmetries().add(IndexType.LatinLower, true, 1, 0);
        SimpleIndices indices2 = ParserIndices.parseSimple("^Bcd");
        indices2.getSymmetries().add(IndexType.LatinLower, false, 1, 0);
        SimpleIndicesBuilder builder = new SimpleIndicesBuilder();
        builder.append(indices1);
        builder.append(indices2);
        SimpleIndices indices = builder.getIndices();
        SimpleIndices expected = ParserIndices.parseSimple("_ab^cd_A^B");
        expected.getSymmetries().add(IndexType.LatinLower, false, 0, 1, 3, 2);
        expected.getSymmetries().add(IndexType.LatinLower, true, 1, 0, 2, 3);

        assertEquals(expected, indices);
        TAssert.assertEquals(indices.getSymmetries().getPermutationGroup(),
                expected.getSymmetries().getPermutationGroup());
    }

    @Test
    public void test3() {
        SimpleIndicesBuilder ibs = new SimpleIndicesBuilder();
        SimpleTensor t1 = parseSimple("T_mn");
        t1.getIndices().getSymmetries().add(IndexType.LatinLower, false, new int[]{1, 0});
        SimpleTensor t2 = parseSimple("T_ab");
        t2.getIndices().getSymmetries().add(IndexType.LatinLower, false, new int[]{1, 0});
        ibs.append(t1.getIndices()).append(t2.getIndices());

        Indices expectedIndices = ParserIndices.parseSimple("_{mnab}");
        assertTrue(ibs.getIndices().equals(expectedIndices));

        //Expected
        Permutation s1 = new PermutationOneLine(false, new int[]{0, 1, 2, 3});
        Permutation s2 = new PermutationOneLine(false, new int[]{1, 0, 2, 3});
        Permutation s3 = new PermutationOneLine(false, new int[]{0, 1, 3, 2});
        Permutation[] expected = {s1, s2, s3};

        TAssert.assertEquals(ibs.getIndices().getSymmetries().getPermutationGroup(), new PermutationGroup(expected));
    }

    @Test
    public void test4() {
        SimpleIndicesBuilder ibs = new SimpleIndicesBuilder();

        ibs.appendWithoutSymmetries(parse("A^p*B_q").getIndices());

        SimpleTensor t1 = parseSimple("T_mn");
        t1.getIndices().getSymmetries().add(IndexType.LatinLower, false, new int[]{1, 0});
        SimpleTensor t2 = parseSimple("T_ab");
        t2.getIndices().getSymmetries().add(IndexType.LatinLower, false, new int[]{1, 0});
        ibs.append(t1.getIndices()).append(t2.getIndices());

        ibs.appendWithoutSymmetries(parse("A^q*B_p").getIndices());

        Indices expectedIndices = ParserIndices.parseSimple("^p_{qmnab}^q_p");
        assertTrue(ibs.getIndices().equals(expectedIndices));

        //Expected
        Permutation s1 = new PermutationOneLine(false, new int[]{0, 1, 2, 3, 4, 5, 6, 7});
        Permutation s2 = new PermutationOneLine(false, new int[]{0, 1, 3, 2, 4, 5, 6, 7});
        Permutation s3 = new PermutationOneLine(false, new int[]{0, 1, 2, 3, 5, 4, 6, 7});
        Permutation[] expected = {s1, s2, s3};

        TAssert.assertEquals(ibs.getIndices().getSymmetries().getPermutationGroup(), new PermutationGroup(expected));
    }

    @Test
    public void test5() {
        SimpleIndicesBuilder ibs = new SimpleIndicesBuilder();

        ibs.appendWithoutSymmetries(parse("a*b").getIndices()).appendWithoutSymmetries(parse("A^{\\mu}*B_{\\nu}").getIndices());

        SimpleTensor t1 = parseSimple("T_mn");
        t1.getIndices().getSymmetries().add(IndexType.LatinLower, false, new int[]{1, 0});
        SimpleTensor t2 = parseSimple("T_ab");
        t1.getIndices().getSymmetries().add(IndexType.LatinLower, false, new int[]{1, 0});
        ibs.append(t1.getIndices()).append(parseSimple("a").getIndices()).append(t2.getIndices());

        ibs.appendWithoutSymmetries(parse("A^{\\nu}*B_{\\mu}").getIndices());

        Indices expectedIndices = ParserIndices.parseSimple("_{mnab}^{\\mu}_{\\nu}^{\\nu}_{\\mu}");
        System.out.println(ibs);
        assertTrue(ibs.getIndices().equals(expectedIndices));

        //Expected
        Permutation s1 = new PermutationOneLine(false, new int[]{0, 1, 2, 3, 4, 5, 6, 7});
        Permutation s2 = new PermutationOneLine(false, new int[]{1, 0, 2, 3, 4, 5, 6, 7});
        Permutation s3 = new PermutationOneLine(false, new int[]{0, 1, 3, 2, 4, 5, 6, 7});
        Permutation[] expected = {s1, s2, s3};

        TAssert.assertEquals(ibs.getIndices().getSymmetries().getPermutationGroup(), new PermutationGroup(expected));
    }
}
