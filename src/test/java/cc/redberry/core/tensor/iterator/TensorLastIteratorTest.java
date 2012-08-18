package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TensorLastIteratorTest {

    @Test
    public void test1() {
        Tensor t = Tensors.parse("a+b+c");
        TensorLastIterator tfi = new TensorLastIterator(t);
        Tensor[] expected = new Tensor[]{t, t.get(0), t.get(1), t.get(2)};
        List<Tensor> target = new ArrayList< >();
        Tensor current;
        while ((current = tfi.next()) != null)
            target.add(current);
        TensorFirstIteratorTest.compareTwoArrays(target.toArray(expected), expected);
    }

    @Test
    public void test2() {
        Tensor t = Tensors.parse("sin[cos[a+b]]");
        TensorLastIterator tfi = new TensorLastIterator(t);
        Tensor[] expected = new Tensor[]{t.get(0).get(0).get(1), t.get(0).get(0).get(0),
                t.get(0).get(0), t.get(0), t};
        List<Tensor> target = new ArrayList< >();
        Tensor current;
        while ((current = tfi.next()) != null)
            target.add(current);
        TensorFirstIteratorTest.compareTwoArrays(target.toArray(expected), expected);
    }

    @Test
    public void test3() {
        Tensor t = Tensors.parse("sin[cos[a+b]+tan[e+l]]");
        TensorLastIterator tfi = new TensorLastIterator(t);
        Tensor[] expected = new Tensor[]{
                t.get(0).get(0).get(0).get(0), t.get(0).get(0).get(0).get(1),
                t.get(0).get(0).get(0), t.get(0).get(0),
                t.get(0).get(1).get(0).get(0), t.get(0).get(1).get(0).get(1),
                t.get(0).get(1).get(0), t.get(0).get(1),
                t.get(0), t};

        List<Tensor> target = new ArrayList< >();
        Tensor current;
        while ((current = tfi.next()) != null)
            target.add(current);
        TensorFirstIteratorTest.compareTwoArrays(target.toArray(expected), expected);
    }

    @Test
    public void test4() {
        Tensor tensor = Tensors.parse("sin[cos[a+b]+tan[e+l]]");

        TensorLastIterator tfi = new TensorLastIterator(tensor);
        while (!TensorUtils.equalsExactly(tfi.next(), Tensors.parse("a"))) ;
        tfi.set(Tensors.parse("x"));
        while (tfi.next() != null) ;

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("sin[cos[x+b]+tan[e+l]]");

        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test5() {
        Tensor tensor = Tensors.parse("(a+b)*(b+c)");
        Tensor[] expectedArray = new Tensor[]{tensor.get(0).get(0), tensor.get(0).get(1), Tensors.parse("5"),
                tensor.get(1).get(0), tensor.get(1).get(1), Tensors.parse("5"), Tensors.parse("25")};
        TensorLastIterator tfi = new TensorLastIterator(tensor);
        Tensor current;
        int i = 0;
        while ((current = tfi.next()) != null) {
            if (TensorUtils.equalsExactly(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equalsExactly(current, Tensors.parse("b")))
                tfi.set(Tensors.parse("3"));
            else if (TensorUtils.equalsExactly(current, Tensors.parse("c")))
                tfi.set(Tensors.parse("2"));
            assertTrue(TensorUtils.equalsExactly(current, expectedArray[i++]));
        }
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("25");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test6() {
        Tensor tensor = Tensors.parse("a*(a+(b+c)*3)*(a+(b+c)*2+4)*A+(B/2+D)");
        TensorLastIterator tfi = new TensorLastIterator(tensor);
        Tensor current;
        while ((current = tfi.next()) != null) {
            if (TensorUtils.equalsExactly(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equalsExactly(current, Tensors.parse("b+c"))) {
                tfi.set(Tensors.parse("4"));
            } else if (TensorUtils.equalsExactly(current, Tensors.parse("B"))) {
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
        TensorLastIterator tfi = new TensorLastIterator(tensor);
        Tensor current;
        while((current=tfi.next()) != null)
            if (TensorUtils.equalsExactly(current, Tensors.parse("a+b")))
                tfi.set(Tensors.parse("(x+y)*3"));
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("cos[sin[(x+y)*3]]");
        assertTrue(TensorUtils.equalsExactly(result, expected));
    }

    @Test
    public void test8() {
        Tensor tensor = Tensors.parse("A_{\\alpha}*B^{\\alpha}_{i}*(R^i+T^i)");
        TensorLastIterator tfi = new TensorLastIterator(tensor);
        Tensor current;
        while ((current = tfi.next()) != null) {
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
        TensorLastIterator tfi = new TensorLastIterator(tensor);
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
        TensorLastIterator tfi = new TensorLastIterator(tensor);
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
        TensorLastIterator tfi = new TensorLastIterator(tensor);
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
