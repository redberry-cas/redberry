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

package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TensorFirstIteratorTest {

    public static boolean compareTwoArrays(Tensor[] target, Tensor[] expeted) {
        if (target.length != expeted.length)
            return false;
        for (int i = 0; i < target.length; ++i) {
            if (!TensorUtils.equalsExactly(target[i], expeted[i]))
                return false;
        }
        return true;
    }

    @Test
    public void test1() {
        Tensor t = Tensors.parse("a+b+c");
        TensorFirstIterator tfi = new TensorFirstIterator(t);
        Tensor[] expected = new Tensor[]{t, t.get(0), t.get(1), t.get(2)};
        List<Tensor> target = new ArrayList< >();
        Tensor current;
        while ((current = tfi.next()) != null)
            target.add(current);
        compareTwoArrays(target.toArray(expected), expected);
    }

    @Test
    public void test2() {
        Tensor t = Tensors.parse("sin[cos[a+b]]");
        TensorFirstIterator tfi = new TensorFirstIterator(t);
        Tensor[] expected = new Tensor[]{t, t.get(0),
                t.get(0).get(0), t.get(0).get(0).get(0),
                t.get(0).get(0).get(0), t.get(0).get(0).get(1)};
        List<Tensor> target = new ArrayList< >();
        Tensor current;
        while ((current = tfi.next()) != null)
            target.add(current);
        compareTwoArrays(target.toArray(expected), expected);
    }

    @Test
    public void test3() {
        Tensor t = Tensors.parse("sin[cos[a+b]+tan[e+l]]");
        TensorFirstIterator tfi = new TensorFirstIterator(t);
        Tensor[] expected = new Tensor[]{t, t.get(0),
                t.get(0).get(0), t.get(0).get(0).get(0),
                t.get(0).get(0).get(0).get(0), t.get(0).get(0).get(0).get(1),
                t.get(0).get(1), t.get(0).get(1).get(0),
                t.get(0).get(1).get(0).get(0), t.get(0).get(1).get(0).get(1)};
        List<Tensor> target = new ArrayList< >();
        Tensor current;
        while ((current = tfi.next()) != null)
            target.add(current);
        compareTwoArrays(target.toArray(expected), expected);
    }


    @Test
    public void test4() {
        Tensor tensor = Tensors.parse("sin[cos[a+b]+tan[e+l]]");

        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        while(!TensorUtils.equalsExactly(tfi.next(), Tensors.parse("a")));
        tfi.set(Tensors.parse("x"));
        while(tfi.next()!= null);

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("sin[cos[x+b]+tan[e+l]]");

        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test5() {
        Tensor tensor = Tensors.parse("a*(a+b)*(a+b+4)");

        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while((current = tfi.next()) != null){
            if (TensorUtils.equalsExactly(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equalsExactly(current, Tensors.parse("b")))
                tfi.set(Tensors.parse("3"));
        }

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("90");

        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test6() {
        Tensor tensor = Tensors.parse("a*(a+(b+c)*3)*(a+(b+c)*2+4)*A+(B/2+D)");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while((current = tfi.next()) != null){
            if (TensorUtils.equalsExactly(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equalsExactly(current, Tensors.parse("b+c"))){
                tfi.set(Tensors.parse("4"));
            }
            else if (TensorUtils.equalsExactly(current, Tensors.parse("B"))){
                tfi.set(Tensors.parse("1/5"));
            }
        }

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("392*A+1/10+D");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test7() {
        Tensor tensor = Tensors.parse("cos[sin[a+b]]");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        tfi.next();
        tfi.next();
        tfi.set(Tensors.parse("(x+y)*3"));
        assertTrue(tfi.next() == null);
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("cos[(x+y)*3]");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test8() {
        Tensor tensor = Tensors.parse("A_{\\alpha}*B^{\\alpha}_{i}*(R^i+T^i)");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while ((current = tfi.next()) != null){
            if (TensorUtils.equalsExactly(current, Tensors.parse("R^i+T^i")))
                tfi.set(Tensors.parse("W^j*3"));
        }
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("A_\\alpha*B^\\alpha_i*W^j*3");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test9() {
        Tensor tensor = Tensors.parse("A_{\\alpha}*B^{\\alpha}*(a+b)/(a+b*(W+U_i*U^i))*3");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while ((current = tfi.next()) != null) {
            if (TensorUtils.equalsExactly(current, Tensors.parse("a+b*(W+U_i*U^i)")))
                tfi.set(Tensors.parse("(a+b)*3"));
        }
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("Power[a+b, -1]*(a+b)*B^{\\alpha }*A_{\\alpha }");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test10() {
        Tensor tensor = Tensors.parse("A_{\\alpha}*B^{\\alpha}*(a+b)/(a+b*(W+U_i*U^i))*3*(K/(e+f)+(e+f)/K)");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while ((current = tfi.next()) != null) {
            if (TensorUtils.equalsExactly(current, Tensors.parse("a+b*(W+U_i*U^i)")))
                tfi.set(Tensors.parse("10"));
            if (TensorUtils.equalsExactly(current, Tensors.parse("e+f")))
                tfi.set(Tensors.parse("K*2"));
        }
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("(a+b)*B^{\\alpha }*A_{\\alpha }*3/4");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test11() {
        Tensor tensor = Tensors.parse("A_{\\alpha}^\\beta*B^{\\alpha}_ijk*T^ijf_\\beta+A_{\\alpha}^\\beta*U_{k\\beta}^{f\\alpha}*10");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while ((current = tfi.next()) != null) {
            if (TensorUtils.equalsExactly(current, Tensors.parse("B^{\\alpha}_ijk")))
                tfi.set(Tensors.parse("U_{k\\beta}^{f\\alpha}"));
            if (TensorUtils.equalsExactly(current, Tensors.parse("T^ijf_\\beta")))
                tfi.set(Tensors.parse("2"));
        }
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("A_{\\alpha}^\\beta*U_{k\\beta}^{f\\alpha}*12");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }
}
