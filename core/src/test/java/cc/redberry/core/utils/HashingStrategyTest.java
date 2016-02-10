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
package cc.redberry.core.utils;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.indices.IndexType.Matrix1;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.utils.HashingStrategy.hashWithIndices;
import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class HashingStrategyTest {

    @Test
    public void testSomeMethod() {
        assertTrue(hashWithIndices(parse("T^i_j*T^j_k"))
                == hashWithIndices(parse("T^i_s*T^s_k")));
    }

    @Test
    public void test1() {
        addSymmetry("T_mnpq", LatinLower, false, 1, 0, 3, 2);
        assertTrue(hashWithIndices(parse("T^ijpq*T_pqrs"))
                == hashWithIndices(parse("T^jipq*T_pqsr")));
        assertFalse(hashWithIndices(parse("T^ijpq*T_pqrs"))
                == hashWithIndices(parse("T^jpiq*T_pqsr")));
    }

    @Test
    public void test2() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, new int[]{1, 0, 2, 3});
        Tensor a = parse("T_{bdca}");
        Tensor b = parse("T_{cdab}");
        Assert.assertTrue(hashWithIndices(a) != hashWithIndices(b));
    }

    @Test
    public void test3() throws Exception {
        Tensor a = parse("(A_abc - A_bac)");
        Tensor b = parse("(A_bac - A_abc)");
        assertEquals(HashingStrategy.hashWithIndices(a), HashingStrategy.hashWithIndices(b));
    }

    @Test
    public void test4() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);


        Tensor a = parse("Tr[G_{a}*G_{b}*G_{c}]");
        Tensor b = parse("Tr[G_{b}*G_{a}*G_{c}]");

        ProductContent pc = ((Product) a).getContent();
        System.out.println(Arrays.toString(pc.getStretchIds()));
        StructureOfContractionsHashed st = pc.getStructureOfContractionsHashed();


        System.out.println(a.toString(OutputFormat.SimpleRedberry));
        System.out.println(b.toString(OutputFormat.SimpleRedberry));


        System.out.println(hashWithIndices(a, a.getIndices().getFree()));
        System.out.println(hashWithIndices(b, a.getIndices().getFree()));

        System.out.println(TensorUtils.equals(a, b));
    }

    @Test
    public void test5() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("Y^a'_b'a"), Matrix1);


        Tensor a = parse("Tr[G_{a}*G_{b}*G^{a}*G^{b}]");
        Tensor b = parse("Tr[G_{a}*G^{a}*G_{b}*G^{b}]");




        System.out.println(a.hashCode());
        System.out.println(b.hashCode());

        System.out.println(a.toString(OutputFormat.SimpleRedberry));
        System.out.println(b.toString(OutputFormat.SimpleRedberry));


        System.out.println(hashWithIndices(a, a.getIndices().getFree()));
        System.out.println(hashWithIndices(b, a.getIndices().getFree()));

        System.out.println(TensorUtils.equals(a, b));
    }

    @Test
    public void test6() throws Exception {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("Y^a'_b'a"), Matrix1);

        Tensor a = parse("Tr[G_{o}*G_{m}*G_{n}*G_{p}*G_{r}*G_{f}*G_{h}*G_{c}]");
        Tensor b = parse("Tr[G_{o}*G_{m}*G_{f}*G_{r}*G_{p}*G_{n}*G_{h}*G_{c}]");

        System.out.println(a.toString(OutputFormat.SimpleRedberry));
        System.out.println(b.toString(OutputFormat.SimpleRedberry));

        System.out.println(a.getIndices().getFree());
        System.out.println(b.getIndices().getFree());

        System.out.println(TensorUtils.compare1(a,b));

        System.out.println(hashWithIndices(a, a.getIndices().getFree()));
        System.out.println(hashWithIndices(b, a.getIndices().getFree()));


    }

    @Test
    public void test7() {
        Tensor a = parse("(A_abc - A_bac)*T^c");
        Tensor b = parse("(A_bac - A_abc)*T^c");
        TAssert.assertEquals(a.hashCode(), b.hashCode());
        TAssert.assertEquals(
                HashingStrategy.hashWithIndices(a, a.getIndices().getFree()),
                HashingStrategy.hashWithIndices(b, a.getIndices().getFree()));
    }
//
//    @Test
//    public void test8() {
//        Tensor a = parse("A_abc");
//        Tensor b = parse("A_bac");
//        TAssert.assertEquals(a.hashCode(), b.hashCode());
//        TAssert.assertEquals(
//                TensorHashCalculator.hashWithIndices(a, a.getIndices().getFree()),
//                TensorHashCalculator.hashWithIndices(b, a.getIndices().getFree()));
//    }
    //    @Test
//    public void test2() {
//        assertTrue(parse("(A^mi*B^jk+A^ji*B^mk)").hashCode() == parse("(A^ji*B^mk+A^ji*B^mk)").hashCode());
//
//        Tensor u = parse("T_mn*(A^mi*B^jk+A^ji*B^mk)");
//        Tensor v = parse("T_mn*(A^ji*B^mk+A^ji*B^mk)");
//
//        System.out.println(v);
//        assertTrue(u.hashCode() == v.hashCode());
//        assertTrue(h(u) != h(v));
//    }
}
