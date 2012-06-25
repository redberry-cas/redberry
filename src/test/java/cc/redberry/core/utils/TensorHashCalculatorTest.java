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
package cc.redberry.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Tensor;
import static cc.redberry.core.tensor.Tensors.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorHashCalculatorTest {

    @Test
    public void testSomeMethod() {
        assertTrue(TensorHashCalculator.hashWithIndices(parse("T^i_j*T^j_k"))
                == TensorHashCalculator.hashWithIndices(parse("T^i_s*T^s_k")));
    }

    @Test
    public void test1() {
        addSymmetry("T_mnpq", IndexType.LatinLower, false, 1, 0, 3, 2);
        assertTrue(TensorHashCalculator.hashWithIndices(parse("T^ijpq*T_pqrs"))
                == TensorHashCalculator.hashWithIndices(parse("T^jipq*T_pqsr")));
        assertFalse(TensorHashCalculator.hashWithIndices(parse("T^ijpq*T_pqrs"))
                == TensorHashCalculator.hashWithIndices(parse("T^jpiq*T_pqsr")));
    }

    @Test
    public void test2() {
        Tensor u = parse("T_mn*(A^mi*B^jk+A^ji*B^mk)");
        Tensor v = parse("T_mn*(A^ji*B^mk+A^ji*B^mk)");
        assertTrue(u.hashCode() == v.hashCode());
        assertTrue(h(u) != h(v));
    }

    private static int h(Tensor str) {
        return TensorHashCalculator.hashWithIndices(str);
    }
}
