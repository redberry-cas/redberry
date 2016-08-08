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
package cc.redberry.core.context;

import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.Permutations;
import cc.redberry.core.indices.SimpleIndices;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.context.VarIndicesProvider.DerivativeArg;
import static cc.redberry.core.parser.ParserIndices.parseSimple;
import static cc.redberry.core.tensor.Tensors.parseField;

/**
 * Created by poslavsky on 08/08/16.
 */
public class VarIndicesProviderTest {
    @Test
    public void testDerivativeArg1() throws Exception {
        final SimpleIndices ii = DerivativeArg.compute(parseSimple(""), parseSimple("_ab"), parseSimple("_cd"));
        final PermutationGroup pg = ii.getSymmetries().getPermutationGroup();
        final PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{0, 2}, {1, 3}})
        );
        Assert.assertEquals(expected, pg);
    }

    @Test
    public void testDerivativeArg2() throws Exception {
        final SimpleIndices ii = DerivativeArg.compute(parseSimple(""), parseSimple("_abAB"), parseSimple("_cdCD"));
        final PermutationGroup pg = ii.getSymmetries().getPermutationGroup();
        final PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{0, 2}, {1, 3}}),
                Permutations.createPermutation(new int[][]{{4, 6}, {5, 7}})
        );
        Assert.assertEquals(expected, pg);
    }

    @Test
    public void testDerivativeArg3() throws Exception {
        final SimpleIndices a1 = parseSimple("_abAB");
        a1.getSymmetries().setSymmetric();
        final SimpleIndices ii = DerivativeArg.compute(parseSimple(""), a1, parseSimple("_cdCD"));
        final PermutationGroup pg = ii.getSymmetries().getPermutationGroup();
        final PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{0, 2}, {1, 3}}),
                Permutations.createPermutation(new int[][]{{0, 1}}),
                Permutations.createPermutation(new int[][]{{4, 6}, {5, 7}}),
                Permutations.createPermutation(new int[][]{{4, 5}})
        );
        Assert.assertEquals(expected, pg);
    }

    @Test
    public void testDerivativeArg4() throws Exception {
        final SimpleIndices ii = DerivativeArg.compute(parseSimple(""), parseSimple("_a"), parseSimple("_b"), parseSimple("_c"));
        final PermutationGroup pg = ii.getSymmetries().getPermutationGroup();
        final PermutationGroup expected = PermutationGroup.symmetricGroup(3);
        Assert.assertEquals(expected, pg);
    }

    @Test
    public void testDerivative() throws Exception {
        SimpleIndices ii = parseField("D[f_ab[x_a, y_b, z], DArg[x_p, x_q, x_r], DArg[y_m, y_n], DArg[z, 4]]").getIndices();
        final PermutationGroup expected = PermutationGroup.createPermutationGroup(
                Permutations.createPermutation(new int[][]{{2, 3}}),
                Permutations.createPermutation(new int[][]{{2, 3, 4}}),
                Permutations.createPermutation(new int[][]{{5, 6}})
        );
        Assert.assertEquals(expected, ii.getSymmetries().getPermutationGroup());
    }
}