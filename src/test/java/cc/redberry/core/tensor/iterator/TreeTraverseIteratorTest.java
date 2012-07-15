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

import cc.redberry.core.number.Complex;
import cc.redberry.core.tensor.Sum;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.tensor.functions.Sin;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class TreeTraverseIteratorTest {

    @Test
    public void test1() {
        Tensor t = parse("a+b+Sin[x]");
        Tensor[] expectedSequence = {t,//a+b+Sin[x]
                t.get(0),//a
                t.get(0),//a
                t.get(1),//b
                t.get(1),//b
                t.get(2),//Sin[x]
                t.get(2),//Sin[x]
                t//a+b+Sin[x]
        };
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (parent.getClass() == Sin.class)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        List<Tensor> list = new ArrayList<>();
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        Tensor[] actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }

    /*    @Test
    public void test1x() {
        Tensor t = parse("a+b+Sin[x]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        while (iterator.next() != null)
            System.out.println(iterator.current());
        //Tensor[] actual = list.toArray(new Tensor[0]);
        //Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }*/


    @Test
    public void test2() {
        Tensor t = parse("Cos[a+b+Sin[x]]");
        Tensor[] expectedSequence = {t,//Cos[a+b+Sin[x]]
                t.get(0),//a+b+Sin[x]
                t.get(0).get(0),//a
                t.get(0).get(0),//a
                t.get(0).get(1),//b
                t.get(0).get(1),//b
                t.get(0).get(2),//Sin[x]
                t.get(0).get(2),//Sin[x]
                t.get(0),//a+b+Sin[x]
                t//Cos[a+b+Sin[x]]
        };
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (parent.getClass() == Sin.class)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        List<Tensor> list = new ArrayList<>();
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        Tensor[] actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }

    @Test
    public void test3() {
        Tensor t = parse("Cos[Sin[x+y]]");
        Tensor[] expectedSequence = {t,//Cos[Sin[x+y]]
                t.get(0),//Sin[x+y]
                t.get(0).get(0),//x+y
                t.get(0).get(0).get(0),//x
                t.get(0).get(0).get(0),//x
                t.get(0).get(0).get(1),//y
                t.get(0).get(0).get(1),//y
                t.get(0).get(0),//x+y
                t.get(0),//Sin[x+y]
                t//Cos[Sin[x+y]]
        };
        List<Tensor> list = new ArrayList<>();
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        while (iterator.next() != null)
            list.add(iterator.current());
        Tensor[] actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }

    @Test
    public void test4() {
        Tensor t = parse("Cos[Sin[x+y]]");
        Tensor[] expectedSequence = {t,//Cos[Sin[x+y]]
                t.get(0),//Sin[x+y]
                t.get(0),//Sin[x+y]
                t//Cos[Sin[x+y]]
        };
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (parent.getClass() == Sin.class)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        List<Tensor> list = new ArrayList<>();
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        Tensor[] actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));

        //equivalent guide
        guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (tensor.getClass() == Sum.class)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        list.clear();
        iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));

        //equivalent guide
        guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (tensor.getClass() == Sum.class && parent.getClass() == Sin.class && indexInParent == 0)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        list.clear();
        iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }

    @Test
    public void test5() {
        Tensor t = parse("Cos[x]");
        Tensor[] expectedSequence = {};
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (indexInParent == 0)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        List<Tensor> list = new ArrayList<>();
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        Tensor[] actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));

        expectedSequence = new Tensor[]{t,//Cos[x]
                t//Cos[x]
        };

        guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (indexInParent == 0 && tensor.size() == 0)
                    return TraversePermission.DontShow;
                return TraversePermission.Enter;
            }
        };
        list.clear();
        iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }

    @Test
    public void test6() {
        Tensor t = parse("Cos[Sin[x+y]]");
        Tensor[] expectedSequence = {t,//Cos[Sin[x+y]]
                t.get(0),//Sin[x+y]
                t.get(0).get(0),//x+y
                t.get(0).get(0),//x+y
                t.get(0),//Sin[x+y]
                t//Cos[Sin[x+y]]
        };
        TraverseGuide guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (parent.getClass() == Sin.class)
                    return TraversePermission.ShowButNotEnter;
                return TraversePermission.Enter;
            }
        };
        List<Tensor> list = new ArrayList<>();
        TreeTraverseIterator iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next()
                != null)
            list.add(iterator.current());
        Tensor[] actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));


        //equivalent guide
        guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (tensor.getClass() == Sum.class)
                    return TraversePermission.ShowButNotEnter;
                return TraversePermission.Enter;
            }
        };
        list.clear();
        iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));

        //equivalent guide
        guide = new TraverseGuide() {

            @Override
            public TraversePermission getPermission(Tensor parent, int indexInParent, Tensor tensor) {
                if (tensor.getClass() == Sum.class && parent.getClass() == Sin.class && indexInParent == 0)
                    return TraversePermission.ShowButNotEnter;
                return TraversePermission.Enter;
            }
        };
        list.clear();
        iterator = new TreeTraverseIterator(t, guide);
        while (iterator.next() != null)
            list.add(iterator.current());
        actual = list.toArray(new Tensor[0]);
        Assert.assertTrue(TensorUtils.equals(expectedSequence, actual));
    }

    @Test
    public void test7() {
        Tensor t = Tensors.parse("(x*y+(a-2*b)*Sin[x]-Cos[x+Sin[x]/Power[a,2]])*(a*a*Power[a,Sin[x-Cos[x]+1]] -32*a*a*2)/63");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        while (iterator.next() != null)
            if (TensorUtils.equals(iterator.current(), Tensors.parse("x")))
                iterator.set(Complex.ZERO);
        Tensor actual = iterator.result();
        Tensor expected = Tensors.parse("Power[a,2]");
        Assert.assertTrue(TensorUtils.equals(expected, actual));
    }

    @Test
    public void test8() {
        /*
         * Substituting s=M^2 and pT = M in
         *
         * 9*M^20 + 18*M^18*(2*pT^2 - 3*s) + pT^4*(pT^2 - s)^4*s^4 +
         * 3*M^16*(18*pT^4 - 70*pT^2*s + 51*s^2) + 6*M^14*(6*pT^6 - 54*pT^4*s +
         * 98*pT^2*s^2 - 45*s^3) + 2*M^2*s^3*(pT^3 - pT*s)^2*(-3*pT^6 +
         * 10*pT^4*s - 11*pT^2*s^2 + 3*s^3) - 2*M^10*s*(51*pT^8 - 377*pT^6*s +
         * 753*pT^4*s^2 - 561*pT^2*s^3 + 135*s^4) + M^12*(9*pT^8 - 252*pT^6*s +
         * 920*pT^4*s^2 - 1008*pT^2*s^3 + 324*s^4) + 2*M^6*s^2*(42*pT^10 -
         * 227*pT^8*s + 456*pT^6*s^2 - 425*pT^4*s^3 + 180*pT^2*s^4 - 27*s^5) +
         * M^8*s*(-18*pT^10 + 344*pT^8*s - 1142*pT^6*s^2 + 1476*pT^4*s^3 -
         * 810*pT^2*s^4 + 153*s^5) + M^4*s^2*(9*pT^12 - 86*pT^10*s +
         * 269*pT^8*s^2 - 374*pT^6*s^3 + 263*pT^4*s^4 - 84*pT^2*s^5 + 9*s^6)
         *
         * Result: 16*M^20
         */
        Tensor t = Tensors.parse("9*Power[M, 20] + 18*Power[M, 18]*(-3*s + 2*Power[pT, 2]) + 3*Power[M, 16]*(-70*s*Power[pT, 2] + 18*Power[pT, 4] + 51*Power[s, 2]) + 6*Power[M, 14]*(-54*s*Power[pT, 4] + 6*Power[pT, 6] + 98*Power[pT, 2]*Power[s, 2] - 45*Power[s, 3]) + Power[pT, 4]*Power[Power[pT,2] - s, 4]*Power[s, 4] - 2*s*Power[M, 10]*(-377*s*Power[pT, 6] + 51*Power[pT, 8] + 753*Power[pT, 4]*Power[s, 2] - 561*Power[pT, 2]*Power[s, 3] + 135*Power[s, 4]) + Power[M, 12]*(-252*s*Power[pT, 6] + 9*Power[pT, 8] + 920*Power[pT, 4]*Power[s, 2] - 1008*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + 2*Power[M, 6]*Power[s, 2]*(-227*s*Power[pT, 8] + 42*Power[pT, 10] + 456*Power[pT, 6]*Power[s, 2] - 425*Power[pT, 4]*Power[s, 3] + 180*Power[pT, 2]*Power[s, 4] - 27*Power[s, 5]) + s*Power[M, 8]*(344*s*Power[pT, 8] - 18*Power[pT, 10] - 1142*Power[pT, 6]*Power[s, 2] + 1476*Power[pT, 4]*Power[s, 3] - 810*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + Power[M, 4]*Power[s, 2]*(-86*s*Power[pT, 10] + 9*Power[pT, 12] + 269*Power[pT, 8]*Power[s, 2] - 374*Power[pT, 6]*Power[s, 3] + 263*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) + 2*Power[M, 2]*Power[s, 3]*(10*s*Power[pT, 4] - 3*Power[pT, 6] - 11*Power[pT, 2]*Power[s, 2] + 3*Power[s, 3])*Power[Power[pT,3] - pT*s, 2]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        Tensor M = Tensors.parse("M");

        while (iterator.next() != null)
            if (TensorUtils.equals(iterator.current(), Tensors.parse("pT")))
                iterator.set(M);
            else if (TensorUtils.equals(iterator.current(), Tensors.parse("s")))
                iterator.set(Tensors.pow(M, Complex.TWO));
        Tensor actual = iterator.result();
        Tensor expected = Tensors.parse("16*Power[M,20]");
        Assert.assertTrue(TensorUtils.equals(expected, actual));
    }

    @Test
    public void test9() {
        /*
         * Substituting s=M^2 in
         *
         * 9*M^20 + 18*M^18*(2*pT^2 - 3*s) + pT^4*(pT^2 - s)^4*s^4 +
         * 3*M^16*(18*pT^4 - 70*pT^2*s + 51*s^2) + 6*M^14*(6*pT^6 - 54*pT^4*s +
         * 98*pT^2*s^2 - 45*s^3) + 2*M^2*s^3*(pT^3 - pT*s)^2*(-3*pT^6 +
         * 10*pT^4*s - 11*pT^2*s^2 + 3*s^3) - 2*M^10*s*(51*pT^8 - 377*pT^6*s +
         * 753*pT^4*s^2 - 561*pT^2*s^3 + 135*s^4) + M^12*(9*pT^8 - 252*pT^6*s +
         * 920*pT^4*s^2 - 1008*pT^2*s^3 + 324*s^4) + 2*M^6*s^2*(42*pT^10 -
         * 227*pT^8*s + 456*pT^6*s^2 - 425*pT^4*s^3 + 180*pT^2*s^4 - 27*s^5) +
         * M^8*s*(-18*pT^10 + 344*pT^8*s - 1142*pT^6*s^2 + 1476*pT^4*s^3 -
         * 810*pT^2*s^4 + 153*s^5) + M^4*s^2*(9*pT^12 - 86*pT^10*s +
         * 269*pT^8*s^2 - 374*pT^6*s^3 + 263*pT^4*s^4 - 84*pT^2*s^5 + 9*s^6)
         *
         *
         */
        Tensor t = Tensors.parse("9*Power[M, 20] + 18*Power[M, 18]*(-3*s + 2*Power[pT, 2]) + 3*Power[M, 16]*(-70*s*Power[pT, 2] + 18*Power[pT, 4] + 51*Power[s, 2]) + 6*Power[M, 14]*(-54*s*Power[pT, 4] + 6*Power[pT, 6] + 98*Power[pT, 2]*Power[s, 2] - 45*Power[s, 3]) + Power[pT, 4]*Power[Power[pT,2] - s, 4]*Power[s, 4] - 2*s*Power[M, 10]*(-377*s*Power[pT, 6] + 51*Power[pT, 8] + 753*Power[pT, 4]*Power[s, 2] - 561*Power[pT, 2]*Power[s, 3] + 135*Power[s, 4]) + Power[M, 12]*(-252*s*Power[pT, 6] + 9*Power[pT, 8] + 920*Power[pT, 4]*Power[s, 2] - 1008*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + 2*Power[M, 6]*Power[s, 2]*(-227*s*Power[pT, 8] + 42*Power[pT, 10] + 456*Power[pT, 6]*Power[s, 2] - 425*Power[pT, 4]*Power[s, 3] + 180*Power[pT, 2]*Power[s, 4] - 27*Power[s, 5]) + s*Power[M, 8]*(344*s*Power[pT, 8] - 18*Power[pT, 10] - 1142*Power[pT, 6]*Power[s, 2] + 1476*Power[pT, 4]*Power[s, 3] - 810*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + Power[M, 4]*Power[s, 2]*(-86*s*Power[pT, 10] + 9*Power[pT, 12] + 269*Power[pT, 8]*Power[s, 2] - 374*Power[pT, 6]*Power[s, 3] + 263*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) + 2*Power[M, 2]*Power[s, 3]*(10*s*Power[pT, 4] - 3*Power[pT, 6] - 11*Power[pT, 2]*Power[s, 2] + 3*Power[s, 3])*Power[Power[pT,3] - pT*s, 2]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        Tensor M = Tensors.parse("M");

        while (iterator.next() != null)
            if (TensorUtils.equals(iterator.current(), Tensors.parse("s")))
                iterator.set(Tensors.pow(M, Complex.TWO));
        Tensor actual = iterator.result();

        Tensor expected = Tensors.parse("9*Power[M, 20] + 18*Power[M, 18]*(-3*Power[M, 2] + 2*Power[pT, 2]) + 3*Power[M, 16]*(51*Power[M, 4] - 70*Power[M, 2]*Power[pT, 2] + 18*Power[pT, 4]) + 6*Power[M, 14]*(-45*Power[M, 6] + 98*Power[M, 4]*Power[pT, 2] - 54*Power[M, 2]*Power[pT, 4] + 6*Power[pT, 6]) + Power[M, 12]*(324*Power[M, 8] - 1008*Power[M, 6]*Power[pT, 2] + 920*Power[M, 4]*Power[pT, 4] - 252*Power[M, 2]*Power[pT, 6] + 9*Power[pT, 8]) - 2*Power[M, 12]*(135*Power[M, 8] - 561*Power[M, 6]*Power[pT, 2] + 753*Power[M, 4]*Power[pT, 4] - 377*Power[M, 2]*Power[pT, 6] + 51*Power[pT, 8]) + Power[M, 10]*(153*Power[M, 10] - 810*Power[M, 8]*Power[pT, 2] + 1476*Power[M, 6]*Power[pT, 4] - 1142*Power[M, 4]*Power[pT, 6] + 344*Power[M, 2]*Power[pT, 8] - 18*Power[pT, 10]) + 2*Power[M, 10]*(-27*Power[M, 10] + 180*Power[M, 8]*Power[pT, 2] - 425*Power[M, 6]*Power[pT, 4] + 456*Power[M, 4]*Power[pT, 6] - 227*Power[M, 2]*Power[pT, 8] + 42*Power[pT, 10]) + Power[M, 8]*(9*Power[M, 12] - 84*Power[M, 10]*Power[pT, 2] + 263*Power[M, 8]*Power[pT, 4] - 374*Power[M, 6]*Power[pT, 6] + 269*Power[M, 4]*Power[pT, 8] - 86*Power[M, 2]*Power[pT, 10] + 9*Power[pT, 12]) + Power[M, 8]*Power[pT, 4]*Power[-Power[M, 2] + Power[pT, 2], 4] + 2*Power[M, 8]*(3*Power[M, 6] - 11*Power[M, 4]*Power[pT, 2] + 10*Power[M, 2]*Power[pT, 4] - 3*Power[pT, 6])*Power[-(pT*Power[M, 2]) + Power[pT, 3], 2]");
        Assert.assertTrue(TensorUtils.equals(expected, actual));
    }

    @Test
    public void test10() {
        /*
         * Substituting pT=M in
         *
         * 9*M^20 + 18*M^18*(2*pT^2 - 3*s) + pT^4*(pT^2 - s)^4*s^4 +
         * 3*M^16*(18*pT^4 - 70*pT^2*s + 51*s^2) + 6*M^14*(6*pT^6 - 54*pT^4*s +
         * 98*pT^2*s^2 - 45*s^3) + 2*M^2*s^3*(pT^3 - pT*s)^2*(-3*pT^6 +
         * 10*pT^4*s - 11*pT^2*s^2 + 3*s^3) - 2*M^10*s*(51*pT^8 - 377*pT^6*s +
         * 753*pT^4*s^2 - 561*pT^2*s^3 + 135*s^4) + M^12*(9*pT^8 - 252*pT^6*s +
         * 920*pT^4*s^2 - 1008*pT^2*s^3 + 324*s^4) + 2*M^6*s^2*(42*pT^10 -
         * 227*pT^8*s + 456*pT^6*s^2 - 425*pT^4*s^3 + 180*pT^2*s^4 - 27*s^5) +
         * M^8*s*(-18*pT^10 + 344*pT^8*s - 1142*pT^6*s^2 + 1476*pT^4*s^3 -
         * 810*pT^2*s^4 + 153*s^5) + M^4*s^2*(9*pT^12 - 86*pT^10*s +
         * 269*pT^8*s^2 - 374*pT^6*s^3 + 263*pT^4*s^4 - 84*pT^2*s^5 + 9*s^6)
         *
         *
         */
        Tensor t = Tensors.parse("9*Power[M, 20] + 18*Power[M, 18]*(-3*s + 2*Power[pT, 2]) + 3*Power[M, 16]*(-70*s*Power[pT, 2] + 18*Power[pT, 4] + 51*Power[s, 2]) + 6*Power[M, 14]*(-54*s*Power[pT, 4] + 6*Power[pT, 6] + 98*Power[pT, 2]*Power[s, 2] - 45*Power[s, 3]) + Power[pT, 4]*Power[Power[pT,2] - s, 4]*Power[s, 4] - 2*s*Power[M, 10]*(-377*s*Power[pT, 6] + 51*Power[pT, 8] + 753*Power[pT, 4]*Power[s, 2] - 561*Power[pT, 2]*Power[s, 3] + 135*Power[s, 4]) + Power[M, 12]*(-252*s*Power[pT, 6] + 9*Power[pT, 8] + 920*Power[pT, 4]*Power[s, 2] - 1008*Power[pT, 2]*Power[s, 3] + 324*Power[s, 4]) + 2*Power[M, 6]*Power[s, 2]*(-227*s*Power[pT, 8] + 42*Power[pT, 10] + 456*Power[pT, 6]*Power[s, 2] - 425*Power[pT, 4]*Power[s, 3] + 180*Power[pT, 2]*Power[s, 4] - 27*Power[s, 5]) + s*Power[M, 8]*(344*s*Power[pT, 8] - 18*Power[pT, 10] - 1142*Power[pT, 6]*Power[s, 2] + 1476*Power[pT, 4]*Power[s, 3] - 810*Power[pT, 2]*Power[s, 4] + 153*Power[s, 5]) + Power[M, 4]*Power[s, 2]*(-86*s*Power[pT, 10] + 9*Power[pT, 12] + 269*Power[pT, 8]*Power[s, 2] - 374*Power[pT, 6]*Power[s, 3] + 263*Power[pT, 4]*Power[s, 4] - 84*Power[pT, 2]*Power[s, 5] + 9*Power[s, 6]) + 2*Power[M, 2]*Power[s, 3]*(10*s*Power[pT, 4] - 3*Power[pT, 6] - 11*Power[pT, 2]*Power[s, 2] + 3*Power[s, 3])*Power[Power[pT,3] - pT*s, 2]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(t);
        Tensor M = Tensors.parse("M");

        while (iterator.next() != null)
            if (TensorUtils.equals(iterator.current(), Tensors.parse("pT")))
                iterator.set(M);
        Tensor actual = iterator.result();

        Tensor expected = Tensors.parse("18*(-3*s + 2*Power[M, 2])*Power[M, 18] + 9*Power[M, 20] + 3*Power[M, 16]*(-70*s*Power[M, 2] + 18*Power[M, 4] + 51*Power[s, 2]) + 6*Power[M, 14]*(-54*s*Power[M, 4] + 6*Power[M, 6] + 98*Power[M, 2]*Power[s, 2] - 45*Power[s, 3]) - 2*s*Power[M, 10]*(-377*s*Power[M, 6] + 51*Power[M, 8] + 753*Power[M, 4]*Power[s, 2] - 561*Power[M, 2]*Power[s, 3] + 135*Power[s, 4]) + Power[M, 12]*(-252*s*Power[M, 6] + 9*Power[M, 8] + 920*Power[M, 4]*Power[s, 2] - 1008*Power[M, 2]*Power[s, 3] + 324*Power[s, 4]) + 2*Power[M, 6]*Power[s, 2]*(-227*s*Power[M, 8] + 42*Power[M, 10] + 456*Power[M, 6]*Power[s, 2] - 425*Power[M, 4]*Power[s, 3] + 180*Power[M, 2]*Power[s, 4] - 27*Power[s, 5]) + s*Power[M, 8]*(344*s*Power[M, 8] - 18*Power[M, 10] - 1142*Power[M, 6]*Power[s, 2] + 1476*Power[M, 4]*Power[s, 3] - 810*Power[M, 2]*Power[s, 4] + 153*Power[s, 5]) + Power[M, 4]*Power[s, 2]*(-86*s*Power[M, 10] + 9*Power[M, 12] + 269*Power[M, 8]*Power[s, 2] - 374*Power[M, 6]*Power[s, 3] + 263*Power[M, 4]*Power[s, 4] - 84*Power[M, 2]*Power[s, 5] + 9*Power[s, 6]) + Power[M, 4]*Power[s, 4]*Power[-s + Power[M, 2], 4] + 2*Power[M, 2]*Power[s, 3]*(10*s*Power[M, 4] - 3*Power[M, 6] - 11*Power[M, 2]*Power[s, 2] + 3*Power[s, 3])*Power[-(M*s) + Power[M, 3], 2]");
        Assert.assertTrue(TensorUtils.equals(expected, actual));
    }

    @Test
    public void test11() {
        Tensor tensor = Tensors.parse("a");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        while (iterator.next() != null)
            if (TensorUtils.equals(iterator.current(), "a"))
                iterator.set(Tensors.parse("b"));
        Assert.assertTrue(TensorUtils.equals(iterator.result(), "b"));
    }

    @Test
    public void testDepth1() {
        Tensor tensor = Tensors.parse("a+b+d*g*(m+f)");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        while (iterator.next() != null)
            if (TensorUtils.equals(iterator.current(), "m"))
                Assert.assertEquals(3, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "a"))
                Assert.assertEquals(1, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "d"))
                Assert.assertEquals(2, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "m+f"))
                Assert.assertEquals(2, iterator.depth());
        Assert.assertTrue(iterator.depth() == -1);
    }

    @Test
    public void testDepth2() {
        Tensor tensor = parse("Cos[a+b+Sin[x]]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        while (iterator.next() != null) {
            Assert.assertTrue(iterator.depth() >= 0);
            if (TensorUtils.equals(iterator.current(), "Cos[a+b+Sin[x]]"))
                Assert.assertEquals(0, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "a+b+Sin[x]"))
                Assert.assertEquals(1, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "a"))
                Assert.assertEquals(2, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "b"))
                Assert.assertEquals(2, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "Sin[x]"))
                Assert.assertEquals(2, iterator.depth());
            else if (TensorUtils.equals(iterator.current(), "x"))
                Assert.assertEquals(3, iterator.depth());
        }
        Assert.assertTrue(iterator.depth() == -1);
    }

    @Test
    public void testLevelUp() {
        Tensor tensor = parse("Sin[1/4*x*y]");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);

        TraverseState state;
        while ((state = iterator.next()) != null)
            if (TensorUtils.equals(iterator.current(), "y"))
                iterator.set(Tensors.parse("4"));
    }

    @Test
    public void testSet1() {
        Tensor tensor = parse("x*(a-b+c)");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        while ((state = iterator.next()) != null)
            if (state == TraverseState.Leaving) {
                if (TensorUtils.equals(parse("b"), iterator.current()))
                    iterator.set(parse("d"));
                if (TensorUtils.equals(parse("x*(a-d+c)"), iterator.current()))
                    iterator.set(parse("a"));

            }
        //no double set exception 
        Assert.assertTrue(TensorUtils.equals(iterator.result(), parse("a")));
    }

    @Test
    public void testSet2() {
        Tensor tensor = parse("d+x*(a-b+c)");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        while ((state = iterator.next()) != null)
            if (state == TraverseState.Leaving) {
                if (TensorUtils.equals(parse("b"), iterator.current()))
                    iterator.set(parse("d"));
                if (TensorUtils.equals(parse("x*(a-d+c)"), iterator.current()))
                    iterator.set(parse("a"));

            }
        //no double set exception 
        Assert.assertTrue(TensorUtils.equals(iterator.result(), parse("d+a")));
    }

    @Test
    public void testSet3() {
        Tensor tensor = parse("d*(a+b)+x*(a-b+c)");
        TreeTraverseIterator iterator = new TreeTraverseIterator(tensor);
        TraverseState state;
        while ((state = iterator.next()) != null)
            if (state == TraverseState.Leaving) {
                if (TensorUtils.equals(parse("d"), iterator.current()))
                    iterator.set(parse("1"));
                if (TensorUtils.equals(parse("x"), iterator.current()))
                    iterator.set(parse("1"));

            }
        //no double set exception 
        Assert.assertTrue(TensorUtils.equals(iterator.result(), parse("2*a+c")));
    }
}
