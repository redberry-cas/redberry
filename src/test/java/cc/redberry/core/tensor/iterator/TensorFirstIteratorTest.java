package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TensorFirstIteratorTest {

    public static boolean compareTwoArrays(Tensor[] target, Tensor[] expeted) {
        if (target.length != expeted.length)
            return false;
        for (int i = 0; i < target.length; ++i) {
            if (!TensorUtils.equals(target[i], expeted[i]))
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
        while(!TensorUtils.equals(tfi.next(), Tensors.parse("a")));
        tfi.set(Tensors.parse("x"));
        while(tfi.next()!= null);

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("sin[cos[x+b]+tan[e+l]]");

        assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void test5() {
        Tensor tensor = Tensors.parse("a*(a+b)*(a+b+4)");

        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while((current = tfi.next()) != null){
            if (TensorUtils.equals(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equals(current, Tensors.parse("b")))
                tfi.set(Tensors.parse("3"));
        }

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("90");

        assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void test6() {
        Tensor tensor = Tensors.parse("a*(a+(b+c)*3)*(a+(b+c)*2+4)*A+(B/2+D)");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        while((current = tfi.next()) != null){
            if (TensorUtils.equals(current, Tensors.parse("a")))
                tfi.set(Tensors.parse("2"));
            else if (TensorUtils.equals(current, Tensors.parse("b+c"))){
                tfi.set(Tensors.parse("4"));
            }
            else if (TensorUtils.equals(current, Tensors.parse("B"))){
                tfi.set(Tensors.parse("1/5"));
            }
        }

        Tensor result = tfi.result();
        Tensor expected = Tensors.parse("392*A+1/10+D");
        assertTrue(TensorUtils.equals(result, expected));
    }

    @Test
    public void test7() {
        Tensor tensor = Tensors.parse("cos[sin[a+b]]");
        TensorFirstIterator tfi = new TensorFirstIterator(tensor);
        Tensor current;
        System.out.println(tfi.next());
        System.out.println(tfi.next());
        tfi.set(Tensors.parse("(x+y)*3"));
        System.out.println(tfi.next());//TODO return NULL!!! why?
        Tensor result = tfi.result();
        System.out.println(result);
        //Tensor expected = Tensors.parse("392*A+1/10+D");
        //assertTrue(TensorUtils.equals(result, expected));
    }
}
