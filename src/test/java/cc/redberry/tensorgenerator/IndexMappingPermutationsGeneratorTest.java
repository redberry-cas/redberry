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
package cc.redberry.tensorgenerator;

import org.junit.Test;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.*;
import static org.junit.Assert.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class IndexMappingPermutationsGeneratorTest {
    public IndexMappingPermutationsGeneratorTest() {
    }

    @Test
    public void test1() {
        for (Tensor t : IndexMappingPermutationsGenerator.getAllPermutations(Tensors.parse("g_mn*g_ab")))
            System.out.println(t);
    }

    @Test
    public void test2() {
        for (Tensor t : IndexMappingPermutationsGenerator.getAllPermutations(Tensors.parse("g_mn*g^ab")))
            System.out.println(t);
    }

    @Test
    public void test3() {
        int c = 0;        
        Tensor erpziv = Tensors.parse("g^pq*g^rs*g_mn*g_ab");
        System.out.println(erpziv);
        System.out.println(erpziv.getIndices());
        System.out.println(TensorUtils.getIndicesSymmetries(erpziv.getIndices().getAllIndices().copy(), erpziv));
        for (Tensor t : IndexMappingPermutationsGenerator.getAllPermutations(Tensors.parse("g_mn*g_ab*g^pq*g^rs"))) {
            c++;
            System.out.println(t);
        }
        assertTrue(c== 9);
    }
}
