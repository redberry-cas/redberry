package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
        while(!TensorUtils.equals(tfi.next(), Tensors.parse("a")));
        tfi.set(Tensors.parse("x"));
        while(tfi.next()!= null);

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("sin[cos[x+b]+tan[e+l]]");

        assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void test5() {
        Tensor tensor = Tensors.parse("(a+b)*(b+c)");

        TensorLastIterator tfi = new TensorLastIterator(tensor);
        Tensor current;
        List<Tensor> tensorList = new ArrayList<>();
        while((current = tfi.next()) != null){
            if (TensorUtils.equals(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equals(current, Tensors.parse("b")))
                tfi.set(Tensors.parse("3"));
             else if (TensorUtils.equals(current, Tensors.parse("c")))
                tfi.set(Tensors.parse("1"));
            System.out.println(current);
        }
/*      a
        b
        5
        b -- Why? expected 3???
        c
        4
        20*/   //TODO WTF???
        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("20");

        assertTrue(TensorUtils.equals(result, expected));
    }

}
