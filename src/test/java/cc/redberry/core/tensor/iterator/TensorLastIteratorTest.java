package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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


}
