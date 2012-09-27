package cc.redberry.core.transformations.substitutions;

import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

public class NewSubstitutionIteratorTest {
    @Test
    public void test0() {
        CC.resetTensorNames(1423);
        NewSubstitutionIterator si = new NewSubstitutionIterator(Tensors.parse("A_mk*G^mn*(S^k_g*(D^g+Q^gz_z)+N^k_ez^ez)*E"));

        Tensor tensor;
        while ((tensor = si.next()) != null) {
            if (tensor.equals(Tensors.parse("E")))
                si.set(Tensors.parse("H^l_l"));
            System.out.println(tensor + " : " + IndicesFactory.createSimple(null, si.getForbidden()).toString() + "\n");
        }

        System.out.println(si.result());
    }
}
