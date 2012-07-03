/*
 * Redberry: symbolic current computations.
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
package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.functions.Sin;
import cc.redberry.core.utils.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TensorTreeIteratorTest {

    @Test
    public void test1() {
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (parent.getClass() == Sin.class)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        Tensor t = Tensors.parse("a+b+Sin[x]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        TraverseState state;
        while ((state = iterator.next()) != null)
            System.out.println(state + " : " + iterator.current());
    }

    @Test
    public void test2() {
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (parent.getClass() == Sin.class)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        Tensor t = Tensors.parse("a+3*b+(-b-n+(x*n)/a)*3");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        TraverseState state;
        Tensor ret = Tensors.parse("Sin[x]");
        while ((state = iterator.next()) != null) {
            if (TensorUtils.equals(iterator.current(), Tensors.parse("x")))
                iterator.set(Tensors.parse("a"));
            System.out.println(state + " : " + iterator.current());
        }
        System.out.println(iterator.result());
    }
//    @Test
//    public void testSequence0WithDepthCheck() {
//        Tensor tensor = Tensors.parse("a+b+d*g*(m+f)");
//        String[] assertedSequence = {"a",
//                                     "b",
//                                     "d",
//                                     "g",
//                                     "m",
//                                     "f",
//                                     "m+f",
//                                     "d*g*(m+f)",
//                                     "a+b+d*g*(m+f)"};
//        int[] depths = {1, 1, 2, 2, 3, 3, 2, 1, 0};
//        int i = -1;
//        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
//        while (iterator.hasNext()) {
//            Tensor t = iterator.next();
//            assertEquals(t, assertedSequence[++i]);
//            assertEquals(iterator.depth(), depths[i]);
//        }
//    }
}