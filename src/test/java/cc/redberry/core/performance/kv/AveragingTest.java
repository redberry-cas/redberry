package cc.redberry.core.performance.kv;

import cc.redberry.core.context.*;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.*;
import cc.redberry.core.utils.*;
import junit.framework.*;
import org.junit.Test;

public class AveragingTest {

    @Test
    public void test2() {
        Tensor t = Tensors.parse("F^\\alpha\\beta*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}+V^\\beta\\alpha*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}");
        t = Averaging.INSTANCE.transform(t);
        t = Expand.expand(t, ContractIndices.INSTANCE);
        t = ContractIndices.INSTANCE.transform(t);
        Tensor expected = Tensors.parse("1/24*V^{\\gamma }_{\\nu }+1/24*V_{\\nu }^{\\gamma }+1/24*V_{\\alpha }^{\\alpha }*d^{\\gamma }_{\\nu }+1/24*d^{\\gamma }_{\\nu }*F_{\\beta }^{\\beta }+1/24*F_{\\nu }^{\\gamma }+1/24*F^{\\gamma }_{\\nu }");
        Assert.assertTrue(TensorUtils.compare(t, expected));
    }

    @Test
    public void test3() {
        Tensor t = Tensors.parse("b_\\mu+a*n_\\mu");
        t = Averaging.INSTANCE.transform(t);
        Assert.assertTrue(TensorUtils.compare(t, Tensors.parse("b_\\mu")));
    }

    @Test
    public void test4_0() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensor t = Tensors.parse("n^\\mu*n_\\mu*n_\\alpha*n^\\alpha*n_\\nu*n^\\nu");
        Expression d = (Expression) Tensors.parse("d_\\mu^\\mu=4");
        t = Averaging.INSTANCE.transform(t);
        t = Expand.expand(t, ContractIndices.INSTANCE, d);
        t = ContractIndices.contract(t);
        t = d.transform(t);
        Assert.assertTrue(TensorUtils.isOne(t));
    }

    @Test
    public void test4() {
        Tensor t = Tensors.parse("n^\\mu*n_\\mu*n_\\alpha*n^\\alpha*n_\\nu*n^\\nu*n_\\lambda*n^\\lambda*n_\\rho*n^\\rho");
        Expression d = (Expression) Tensors.parse("d_\\mu^\\mu=4");
        t = Averaging.INSTANCE.transform(t);
        t = Expand.expand(t, ContractIndices.INSTANCE);
        t = ContractIndices.contract(t);
        t = d.transform(t);
        Assert.assertTrue(TensorUtils.isOne(t));
    }

    @Test
    public void test5() {
        Tensor ff = (Expression) Tensors.parse("FF=(-1/6)*F^{\\nu \\beta \\epsilon }_{\\zeta }*F_{\\nu \\beta }^{\\zeta }_{\\epsilon }+n^{\\mu }*F^{\\alpha }_{\\nu }^{\\epsilon }_{\\lambda }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }+(-8/3)*n^{\\mu }*F_{\\beta \\nu }^{\\epsilon }_{\\lambda }*n^{\\alpha }*n^{\\beta }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }");
        Tensors.addSymmetry("F_{\\mu\\nu\\alpha\\beta}", IndexType.GreekLower, true, new int[]{1, 0, 2, 3});
        ff = Averaging.INSTANCE.transform(ff);
        ff = Expand.expand(ff);
        ff = ContractIndices.INSTANCE.transform(ff);
        ff = ((Expression) Tensors.parse("F_{\\mu}^\\mu_\\alpha\\beta=0")).transform(ff);

        System.out.println(ff);
    }

    @Test
    public void test6() {
        Tensor t = Tensors.parse("a*n_\\mu*n_\\nu");
        t = Averaging.INSTANCE.transform(t);
        Tensor expected = Tensors.parse("1/4*a*g_\\mu\\nu");
        Assert.assertTrue(TensorUtils.compare(t, expected));
    }

    @Test
    public void test7() {
        Tensor t = Tensors.parse("a*n_\\mu*n_\\nu+g_{\\mu\\nu}*n_\\alpha*n^\\alpha+n_\\mu*n_\\nu*n_\\alpha*g^\\alpha");
        Expression d = Tensors.parseExpression("d_\\mu^\\mu =4");
        t = Averaging.INSTANCE.transform(t);
        t = Expand.expand(t, ContractIndices.INSTANCE, d);
        t = ContractIndices.contract(t);
        t = d.transform(t);
        Tensor expected = Tensors.parse("(1/4*a+1)*g_\\mu\\nu");
        Assert.assertTrue(TensorUtils.compare(t, expected));
    }
}
