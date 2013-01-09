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

package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SubstitutionIteratorTest {
    @Test
    public void test0() {
        CC.resetTensorNames(1423);
        Tensor tensor = Tensors.parse("A_mk*G^amn_a*(S^k_g*(D^g+Q^gz_z)+N^k_ez^ez)*E");
        SubstitutionIterator si = new SubstitutionIterator(tensor);
        System.out.println(IndicesFactory.createSorted(TensorUtils.getAllIndicesNamesT(tensor).toArray()));
        Tensor current;
        while ((current = si.next()) != null) {
            if (current.equals(Tensors.parse("E")))
                si.set(Tensors.parse("H^l_l"));
            System.out.println(current + " : " + IndicesFactory.createSorted(si.getForbidden()).toString() + "\n");
            int k = 0;
        }

        System.out.println(si.result());
    }

    @Test
    public void test1() {
        CC.resetTensorNames(1423);
        Tensor tensor = Tensors.parse("a*b");
        SubstitutionIterator si = new SubstitutionIterator(tensor);
        System.out.println(IndicesFactory.createSorted(TensorUtils.getAllIndicesNamesT(tensor).toArray()));
        Tensor current;
        while ((current = si.next()) != null) {
            System.out.println(current + " : " + IndicesFactory.createSorted(si.getForbidden()).toString() + "\n");
            if (current.equals(Tensors.parse("a*b")))
                si.set(Tensors.parse("H^l_l"));
            System.out.println(current + " : " + IndicesFactory.createSorted(si.getForbidden()).toString() + "\n");
            int k = 0;
        }

        System.out.println(si.result());
    }
}
