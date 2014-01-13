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

import cc.redberry.core.groups.permutations.*;
import cc.redberry.core.indexmapping.IndexMappings;
import cc.redberry.core.indexmapping.Mapping;
import cc.redberry.core.indices.SimpleIndices;
import cc.redberry.core.tensor.Tensor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FinSymm {
    public static PermutationGroup find(SimpleIndices indices, final Tensor tensor) {
        int degree = indices.size();
        List<BSGSElement> sym = AlgorithmsBase.createSymmetricGroupBSGS(degree);
        final int[] indicesArray = indices.toArray();
        ArrayList<BSGSCandidateElement> res = new ArrayList<>();
        Indicator<Permutation> test = new Indicator<Permutation>() {
            @Override
            public boolean is(Permutation object) {
                return IndexMappings.testMapping(new Mapping(indicesArray,
                        object.permute(indicesArray)), tensor, tensor);
            }
        };
        AlgorithmsBacktrack.subgroupSearch(sym, res, BacktrackSearchTestFunction.TRUE, test);
        return new PermutationGroup(AlgorithmsBase.asBSGSList(res), true);

    }
}
