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

package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

public class NewSubstitutionIteratorTest {
    @Test
    public void test0() {
        CC.resetTensorNames(1423);
        NewSubstitutionIterator si = new NewSubstitutionIterator(Tensors.parse("A_mk*G^mn*(S^k_g*(D^g+Q^gz_z)+N^k_ez^ez)*E"));

        Tensor tensor;
        while ((tensor = si.next()) != null) {
            if (tensor.equals(Tensors.parse("E")))
                si.set(Tensors.parse("H^l_l"));
            System.out.println(tensor + " : " + IndicesFactory.createSimple(null, si.getForbidden()).toString() + "\n");
        }

        System.out.println(si.result());
    }
}
