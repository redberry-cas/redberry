package cc.redberry.core.tensor.iterator;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

public class TensorFirstIteratorTest {

    @Test
    public void test1(){
        Tensor t = Tensors.parse("a+b+c");
        TensorFirstIterator tfi = new  TensorFirstIterator(t);
        System.out.println(tfi.next());
    }
}
