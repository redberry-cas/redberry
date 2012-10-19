package cc.redberry.core.transformations;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.SimpleTensor;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.core.transformations.ContractIndices.contract;
import static cc.redberry.core.transformations.Differentiate.differentiate;
import static cc.redberry.core.transformations.Expand.expand;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class DifferentiateTest {
    @Test
    public void test1() {
        Tensor t = parse("x*Sin[y**3]");
        SimpleTensor var = parseSimple("y");
        TAssert.assertEquals(differentiate(t, var, 1), "3*y**2*Cos[y**3]*x");
        TAssert.assertEquals(differentiate(t, var, 2), "6*y*Cos[y**3]*x-9*y**4*Sin[y**3]*x");
        TAssert.assertEquals(differentiate(t, var, 3), "-27*y**6*Cos[y**3]*x-54*y**3*Sin[y**3]*x+6*Cos[y**3]*x");
        TAssert.assertEquals(differentiate(t, var, 4), "-180*y**2*Sin[y**3]*x-324*y**5*Cos[y**3]*x+81*y**8*Sin[y**3]*x");

    }

    @Test
    public void test2() {
        Tensor t = parse("Sin[f^mn*(x_mn+x_nm)]");
        SimpleTensor var = parseSimple("x_ij");
        t = differentiate(t, var);
        t = contract(expand(t));
        TAssert.assertEquals(t, "Cos[f^mn*(x_mn+x_nm)]*f^ij+Cos[f^mn*(x_mn+x_nm)]*f^ji");
        var = parseSimple("f_i^i");
        t = differentiate(t, var);
        t = contract(expand(t));
        TAssert.assertEquals(t, "-2*Sin[(x_{nm}+x_{mn})*f^{mn}]*x^{m}_{m}*f^{ji}-2*Sin[(x_{nm}+x_{mn})*f^{mn}]*x^{m}_{m}*f^{ij}+2*Cos[(x_{nm}+x_{mn})*f^{mn}]*g^{ij}");
    }
}
