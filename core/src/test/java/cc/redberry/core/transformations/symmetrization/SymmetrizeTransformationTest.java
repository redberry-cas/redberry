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
package cc.redberry.core.transformations.symmetrization;

import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.TAssert.assertEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetrizeTransformationTest {

    @Test
    public void testEmpty() {
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parseSimple("_abmn"), true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        assertEquals(symmetrizeTransformation.transform(t), "g_mn*g_ab");
    }

    @Test
    public void testIdentity() {
        SimpleIndices indices = ParserIndices.parseSimple("_abmn");
        indices.getSymmetries().add(new PermutationOneLine(1, 0, 2, 3));
        SymmetrizeTransformation symmetrizeTransformation =
                new SymmetrizeTransformation(indices, true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        assertEquals(symmetrizeTransformation.transform(t), t);
    }

    @Test
    public void testAll1() {
        SimpleIndices indices = ParserIndices.parseSimple("_abmn");
        indices.getSymmetries().setSymmetric();
        SymmetrizeTransformation symmetrizeTransformation =
                new SymmetrizeTransformation(indices, true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        assertEquals(symmetrizeTransformation.transform(t), "(1/3)*g_mn*g_ab+(1/3)*g_am*g_bn+(1/3)*g_an*g_bm");
    }

    @Test
    public void testAll2() {
        SimpleIndices indices = ParserIndices.parseSimple("_ab");
        indices.getSymmetries().setSymmetric();
        SymmetrizeTransformation symmetrizeTransformation =
                new SymmetrizeTransformation(indices, true);
        Tensor t = Tensors.parse("A_a*B_b");
        assertEquals(symmetrizeTransformation.transform(t), "(1/2)*A_a*B_b+(1/2)*A_b*B_a");
    }

    @Test
    public void testAll3() {
        SimpleIndices indices = ParserIndices.parseSimple("_a^c");
        indices.getSymmetries().setSymmetric();
        SymmetrizeTransformation symmetrizeTransformation =
                new SymmetrizeTransformation(indices, true);
        Tensor t = Tensors.parse("A_a*A^c");
        assertEquals(symmetrizeTransformation.transform(t), "A_a*A^c");
    }

    @Test
    public void testAll4() {
        SimpleIndices indices = ParserIndices.parseSimple("_a^c");
        indices.getSymmetries().setSymmetric();
        SymmetrizeTransformation symmetrizeTransformation =
                new SymmetrizeTransformation(indices, true);
        Tensor t = Tensors.parse("A_a*B^c");
        assertEquals(symmetrizeTransformation.transform(t), "(1/2)*A_a*B^c+(1/2)*B_a*A^c");
    }

    @Test
    public void testAll5() {
        SimpleIndices indices = ParserIndices.parseSimple("_abc");
        indices.getSymmetries().setSymmetric();
        SymmetrizeTransformation symmetrizeTransformation =
                new SymmetrizeTransformation(indices, true);
        Tensor t = Tensors.parse("T_abc");
        System.out.println(t = symmetrizeTransformation.transform(t));
        System.out.println(Tensors.parseExpression("T_abc = A_a*B_b*C_c").transform(t));
    }
}
