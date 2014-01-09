/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
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

import cc.redberry.core.TAssert;
import cc.redberry.core.combinatorics.IntCombinationsGenerator;
import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.apache.commons.math3.util.ArithmeticUtils;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertEquals;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetrizeTransformationTest {

    @Test
    public void testEmpty() {
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_abmn"), SymmetriesFactory.createSymmetries(4), true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        System.out.println(symmetrizeTransformation.transform(t));
        assertEquals(symmetrizeTransformation.transform(t), "g_mn*g_ab");
    }

    @Test
    public void testIdentity() {
        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
        symmetries.add(new Symmetry(false, new int[]{1, 0, 2, 3}));
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_abmn"), symmetries, true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        System.out.println(symmetrizeTransformation.transform(t));
        assertEquals(symmetrizeTransformation.transform(t), t);
    }

    @Test
    public void testAll1() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(4);
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_abmn"), symmetries, true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        assertEquals(symmetrizeTransformation.transform(t), "(1/3)*g_mn*g_ab+(1/3)*g_am*g_bn+(1/3)*g_an*g_bm");
    }

    @Test
    public void testAll2() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(2);
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_ab"), symmetries, true);
        Tensor t = Tensors.parse("A_a*B_b");
        assertEquals(symmetrizeTransformation.transform(t), "(1/2)*A_a*B_b+(1/2)*A_b*B_a");
    }

    @Test
    public void testAll3() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(2);
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_a^c"), symmetries, true);
        Tensor t = Tensors.parse("A_a*A^c");
        assertEquals(symmetrizeTransformation.transform(t), "A_a*A^c");
    }

    @Test
    public void testAll4() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(2);
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_a^c"), symmetries, true);
        Tensor t = Tensors.parse("A_a*B^c");
        assertEquals(symmetrizeTransformation.transform(t), "(1/2)*A_a*B^c+(1/2)*B_a*A^c");
    }

    @Test
    public void testAll5() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(3);
        SymmetrizeTransformation symmetrizeTransformation = new SymmetrizeTransformation(
                ParserIndices.parse("_abc"), symmetries, true);
        Tensor t = Tensors.parse("T_abc");
        System.out.println(t = symmetrizeTransformation.transform(t));
        System.out.println(Tensors.parseExpression("T_abc = A_a*B_b*C_c").transform(t));
    }

    @Ignore
    @Test
    public void testAllGroups() {
        SimpleTensor tensor = Tensors.parseSimple("T_abcdef");
        int[] indices = tensor.getIndices().getAllIndices().copy();

        int dim = indices.length, order = (int) ArithmeticUtils.factorial(indices.length);
        System.out.println(ArithmeticUtils.pow(2, order));

        Symmetries symmetries = SymmetriesFactory.createFullAntiSymmetries(dim);
        Symmetry[] all = new Symmetry[order];
        int p = -1;
        for (Symmetry s : symmetries)
            all[++p] = s;


        int counter = 0;
        IntCombinationsGenerator combinations;
        Symmetries temp;
        SymmetrizeTransformation symmetrize;
        Tensor symmetrization;
        for (p = 0; p <= 12; ++p) {
            System.out.println("\n\n\nSS " + p + "\n\n\n");
            combinations = new IntCombinationsGenerator(order, p);
            for (int[] combination : combinations) {
                counter++;
                temp = SymmetriesFactory.createSymmetries(dim);
                for (int position : combination)
                    temp.add(all[position]);
                symmetrize = new SymmetrizeTransformation(indices, temp, true);
                symmetrization = symmetrize.transform(tensor);
                if (symmetrization.size() > 6)
                    continue;
                TAssert.assertEqualsSymmetries(temp, TensorUtils.findIndicesSymmetries(indices, symmetrization));
                System.out.println(counter);
            }
        }


    }
}
