package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.utils.TensorUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

}
