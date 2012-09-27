package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

public class NewSubstitutionIteratorTest {
    @Test
    public void test0() {
        NewSubstitutionIterator si = new NewSubstitutionIterator(Tensors.parse("A*b*c+k*d*(v+f*a+j+y)"));

        Tensor tensor;
        while ((tensor = si.next()) != null)
            System.out.println(tensor + "\n");

    }
}
