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
package cc.redberry.core.tensor;

import cc.redberry.core.combinatorics.Symmetry;
import cc.redberry.core.combinatorics.symmetries.Symmetries;
import cc.redberry.core.combinatorics.symmetries.SymmetriesFactory;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.parser.ParserIndices;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

import static cc.redberry.core.TAssert.assertEqualsSymmetries;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorFieldTest {

    @Test
    public void testIterator() {
        Tensor t = parse("f[a,b,c]");
        int i = 0;
        for (Tensor c : t)
            ++i;
        Assert.assertEquals(i, 3);
    }

    @Test
    public void testDerivativeSymmetries1() {
        SimpleTensor d;
        Symmetry[] _expected;

        d = Tensors.parseSimple("f~(2)_{mn ab}[x_a]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{0, 1, 3, 2}, false)};
        assertEqualsSymmetries(
                d, create(_expected));

        d = Tensors.parseSimple("f~(3)_{mn abc}[x_a]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{0, 1, 3, 2, 4}, false),
                new Symmetry(new int[]{0, 1, 4, 3, 2}, false)};
        assertEqualsSymmetries(
                d, create(_expected));

        d = Tensors.parseSimple("f~(3)_{mn ab cd ef}[x_ab]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{0, 1, 4, 5, 2, 3, 6, 7}, false),
                new Symmetry(new int[]{0, 1, 6, 7, 2, 3, 4, 5}, false)};
        assertEqualsSymmetries(d, create(_expected));
    }

    @Test
    public void testDerivativeSymmetries2() {
        SimpleTensor d;
        Symmetry[] _expected;

        addSymmetry(parseSimple("f_mn[x_a]"), 1, 0);

        d = Tensors.parseSimple("f~(2)_{mn ab}[x_a]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{1, 0, 2, 3}, false),
                new Symmetry(new int[]{0, 1, 3, 2}, false)};
        assertEqualsSymmetries(
                d, create(_expected));

        d = Tensors.parseSimple("f~(3)_{mn abc}[x_a]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{1, 0, 2, 3, 4}, false),
                new Symmetry(new int[]{0, 1, 3, 2, 4}, false),
                new Symmetry(new int[]{0, 1, 4, 3, 2}, false)};
        assertEqualsSymmetries(
                d, create(_expected));


        addAntiSymmetry(parseSimple("f_mn[x_ab]"), 1, 0);
        d = Tensors.parseSimple("f~(3)_{mn ab cd ef}[x_ab]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{1, 0, 2, 3, 4, 5, 6, 7}, true),
                new Symmetry(new int[]{0, 1, 4, 5, 2, 3, 6, 7}, false),
                new Symmetry(new int[]{0, 1, 6, 7, 2, 3, 4, 5}, false)};
        assertEqualsSymmetries(d, create(_expected));
    }

    @Test
    public void testDerivativeSymmetries3() {
        SimpleTensor d;
        Symmetry[] _expected;


        d = Tensors.parseSimple("f~(1, 1)_{mn ab}[x_a, y_b]");
        _expected = new Symmetry[]{
                new Symmetry(new int[]{0, 1, 3, 2}, false)};
        assertEqualsSymmetries(
                d, create(_expected));

        d = Tensors.parseSimple("f~(1, 1)_{mn abc de}[x_abc, y_ab]");

        _expected = new Symmetry[]{
                new Symmetry(new int[]{0, 1, 4, 5, 6, 2, 3}, false)};
        assertEqualsSymmetries(
                d, create(_expected));
    }

    private static Symmetries create(Symmetry... symmetries) {
        Symmetries expected = SymmetriesFactory.createSymmetries(symmetries[0].dimension());
        for (Symmetry sym : symmetries)
            expected.addUnsafe(sym);
        return expected;
    }


    @Test
    public void testPartition1() {
        TensorField f = (TensorField) parse("f~(2,3,2)_{mn {ab cd} {x y z} {AB}}[x_ab,f_c, x_A]");
        SimpleIndices[][] iP = f.calcIndicesPartition();
        SimpleIndices[][] asserted = new SimpleIndices[][]{{ParserIndices.parseSimple("_mn")},
                {ParserIndices.parseSimple("_ab"), ParserIndices.parseSimple("_cd")},
                {ParserIndices.parseSimple("_x"), ParserIndices.parseSimple("_y"), ParserIndices.parseSimple("_z")},
                {ParserIndices.parseSimple("_A"), ParserIndices.parseSimple("_B")}};

        Assert.assertTrue(Arrays.deepEquals(iP, asserted));
    }
}
