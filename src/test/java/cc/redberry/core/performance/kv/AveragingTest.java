package cc.redberry.core.performance.kv;

import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.*;
import org.junit.Test;

public class AveragingTest {

    @Test
    public void test2() {
        Tensor t = Tensors.parse("F^\\alpha\\beta*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}+V^\\beta\\alpha*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}");
        t = Averaging.INSTANCE.transform(t);
        System.out.println(t);
    }

    @Test
    public void test3() {
        Tensor t = Tensors.parse("b_\\mu+a*n_\\mu");
        t = Averaging.INSTANCE.transform(t);
        System.out.println(t);
    }

    @Test
    public void test4() {
        Tensor t = Tensors.parse("n^\\mu*n_\\mu*n_\\alpha*n^\\alpha*n_\\nu*n^\\nu*n_\\lambda*n^\\lambda*n_\\rho*n^\\rho");
        Expression d = (Expression) Tensors.parse("d_\\mu^\\mu=4");
        t = Averaging.INSTANCE.transform(t);
        t = ContractIndices.CONTRACT_INDICES.transform(t);
        t = d.transform(t);
        System.out.println(t);
    }

    @Test
    public void test5() {
        Tensor ff = (Expression) Tensors.parse("FF=(-1/6)*F^{\\nu \\beta \\epsilon }_{\\zeta }*F_{\\nu \\beta }^{\\zeta }_{\\epsilon }+n^{\\mu }*F^{\\alpha }_{\\nu }^{\\epsilon }_{\\lambda }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }+(-8/3)*n^{\\mu }*F_{\\beta \\nu }^{\\epsilon }_{\\lambda }*n^{\\alpha }*n^{\\beta }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }");
        Tensors.addSymmetry("F_{\\mu\\nu\\alpha\\beta}", IndexType.GreekLower, true, new int[]{1, 0, 2, 3});
        ff = Averaging.INSTANCE.transform(ff);
        ff = Expand.expand(ff);
        ff = ContractIndices.CONTRACT_INDICES.transform(ff);
        ff = ((Expression) Tensors.parse("F_{\\mu}^\\mu_\\alpha\\beta=0")).transform(ff);

        System.out.println(ff);
    }
}
