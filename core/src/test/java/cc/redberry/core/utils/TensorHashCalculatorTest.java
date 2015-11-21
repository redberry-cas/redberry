/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import org.junit.Assert;
import org.junit.Test;

import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.tensor.Tensors.*;
import static cc.redberry.core.utils.TensorHashCalculator.hashWithIndices;
import static org.junit.Assert.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorHashCalculatorTest {

    @Test
    public void testSomeMethod() {
        assertTrue(hashWithIndices(parse("T^i_j*T^j_k"))
                == hashWithIndices(parse("T^i_s*T^s_k")));
    }

    @Test
    public void test1() {
        addSymmetry("T_mnpq", LatinLower, false, 1, 0, 3, 2);
        assertTrue(hashWithIndices(parse("T^ijpq*T_pqrs"))
                == hashWithIndices(parse("T^jipq*T_pqsr")));
        assertFalse(hashWithIndices(parse("T^ijpq*T_pqrs"))
                == hashWithIndices(parse("T^jpiq*T_pqsr")));
    }

    @Test
    public void test2() {
        SimpleTensor t = parseSimple("T_abcd");
        addSymmetry(t, LatinLower, false, new int[]{1, 0, 2, 3});
        Tensor a = parse("T_{bdca}");
        Tensor b = parse("T_{cdab}");
        Assert.assertTrue(hashWithIndices(a) != hashWithIndices(b));
    }

    @Test
    public void test3() throws Exception {
        Tensor a = parse("(A_abc - A_bac)");
        Tensor b = parse("(A_bac - A_abc)");
        assertEquals(TensorHashCalculator.hashWithIndices(a), TensorHashCalculator.hashWithIndices(b));
    }


    //    @Test
//    public void test2() {
//        assertTrue(parse("(A^mi*B^jk+A^ji*B^mk)").hashCode() == parse("(A^ji*B^mk+A^ji*B^mk)").hashCode());
//
//        Tensor u = parse("T_mn*(A^mi*B^jk+A^ji*B^mk)");
//        Tensor v = parse("T_mn*(A^ji*B^mk+A^ji*B^mk)");
//
//        System.out.println(v);
//        assertTrue(u.hashCode() == v.hashCode());
//        assertTrue(h(u) != h(v));
//    }

    private static int h(Tensor str) {
        return hashWithIndices(str);
    }
}
