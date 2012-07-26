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
package cc.redberry.core.performance.kv;

import cc.redberry.core.tensor.*;
import cc.redberry.core.utils.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class NaiveSubstitutionTest {

    @Test
    public void testSomeMethod() {
        Tensor t = Tensors.parse("1/2*(x+y)+1/3*(x+z)");
        Tensor from = Tensors.parse("x"),to = Tensors.parse("u+v");
        t = new NaiveSubstitution(from, to).transform(t);
        
        Expression e = Tensors.parseExpression("x = u+v");
        System.out.println(e.transform(t));
        System.out.println(t);
        Tensor expected = Tensors.parse("1/2*(u+v+y)+1/3*(u+v+z)");
        Assert.assertTrue(TensorUtils.equals(t, expected));
    }

}