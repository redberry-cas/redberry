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
package cc.redberry.core.utils;

import cc.redberry.core.groups.permutations.Permutation;
import cc.redberry.core.groups.permutations.PermutationGroup;
import cc.redberry.core.groups.permutations.PermutationOneLine;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FinSymmTest {

    @Ignore
    @Test
    public void testName() throws Exception {
        SimpleTensor t = Tensors.parseSimple("T_zxcvbnmasdfghjkq");
        Permutation[] generators = {
                new PermutationOneLine(16, new int[][]{{1, 12, 6, 5, 14}, {2, 7, 9, 8, 4}, {3, 11, 15, 13, 10}}),
                new PermutationOneLine(16, new int[][]{{1, 4}, {2, 15}, {3, 11}, {6, 14}, {7, 10}, {9, 12}}),
                new PermutationOneLine(16, new int[][]{{0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 9}, {10, 11}, {12, 13}, {14, 15}})};
        for (Permutation generator : generators)
            t.getIndices().getSymmetries().add(generator);

        SimpleIndices indices = ParserIndices.parseSimple("_zxcvbnmasdfghjkq");
        PermutationGroup found = FinSymm.find(indices, t);

        PermutationGroup expected = t.getIndices().getSymmetries().getPermutationGroup();
        System.out.println(found.equals(expected));
        System.out.println(expected.order());
        System.out.println(found.order());
    }
}
