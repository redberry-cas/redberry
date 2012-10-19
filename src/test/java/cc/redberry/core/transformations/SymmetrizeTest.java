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
package cc.redberry.core.transformations;

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertEquals;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SymmetrizeTest {

    @Test
    public void testEmpty() {
        Symmetrize symmetrize = new Symmetrize(
                ParserIndices.parse("_abmn"),
                new Symmetry[0], true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        System.out.println(symmetrize.transform(t));
        assertEquals(symmetrize.transform(t), "g_mn*g_ab");
    }

    @Test
    public void testIdentity() {
        Symmetries symmetries = SymmetriesFactory.createSymmetries(4);
        symmetries.add(new Symmetry(new int[]{1, 0, 2, 3}, false));
        Symmetrize symmetrize = new Symmetrize(
                ParserIndices.parse("_abmn"),
                symmetries.getBasisSymmetries().toArray(new Symmetry[0]),
                true);
        Tensor t = Tensors.parse("g_mn*g_ab");
        assertEquals(symmetrize.transform(t), t);
    }

    @Test
    public void testAll1() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(4);
        Symmetrize symmetrize = new Symmetrize(
                ParserIndices.parse("_abmn"),
                symmetries.getBasisSymmetries().toArray(new Symmetry[0]),
                false);
        Tensor t = Tensors.parse("g_mn*g_ab");
        System.out.println(symmetrize.transform(t));
        assertEquals(symmetrize.transform(t), "g_mn*g_ab+g_am*g_bn+g_an*g_bm");
    }

    @Test
    public void testAll2() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(2);
        Symmetrize symmetrize = new Symmetrize(
                ParserIndices.parse("_ab"),
                symmetries.getBasisSymmetries().toArray(new Symmetry[0]),
                true);
        Tensor t = Tensors.parse("A_a*B_b");
        assertEquals(symmetrize.transform(t), "(1/2)*(A_a*B_b+A_b*B_a)");
    }

    @Test
    public void testAll3() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(2);
        Symmetrize symmetrize = new Symmetrize(
                ParserIndices.parse("_a^c"),
                symmetries.getBasisSymmetries().toArray(new Symmetry[0]),
                true);
        Tensor t = Tensors.parse("A_a*A^c");
        assertEquals(symmetrize.transform(t), "A_a*A^c");
    }

    @Test
    public void testAll4() {
        Symmetries symmetries = SymmetriesFactory.createFullSymmetries(2);
        Symmetrize symmetrize = new Symmetrize(
                ParserIndices.parse("_a^c"),
                symmetries.getBasisSymmetries().toArray(new Symmetry[0]),
                true);
        Tensor t = Tensors.parse("A_a*B^c");
        assertEquals(symmetrize.transform(t), "(1/2)*(A_a*B^c+B_a*A^c)");
    }
}
