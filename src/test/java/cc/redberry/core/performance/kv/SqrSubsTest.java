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
import cc.redberry.core.tensor.iterator.*;
import cc.redberry.core.transformations.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class SqrSubsTest {


    @Test
    public void test1() {
        SimpleTensor n = (SimpleTensor) Tensors.parse("n_{a}");
        Transformation tr = new Transformer(TraverseState.Leaving, new Transformation[]{new SqrSubs(n)});
        Tensor t = Tensors.parse("n_m*n^m*a*n_a*n^a*n_i*n^j*b");
        System.out.println(tr.transform(t));
    }

    @Test
    public void test2() {
        SimpleTensor n = (SimpleTensor) Tensors.parse("n_{a}");
        Transformation tr = new Transformer(TraverseState.Leaving, new Transformation[]{new SqrSubs(n)});
        Tensor t = Tensors.parse("n_m*n^m*n_a*n^a+2");
        System.out.println(tr.transform(t));
    }
}