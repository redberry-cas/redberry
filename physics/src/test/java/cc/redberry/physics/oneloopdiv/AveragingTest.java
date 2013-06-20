/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2013:
 *   Stanislav Poslavsky   <stvlpos@mail.ru>
 *   Bolotin Dmitriy       <bolotin.dmitriy@gmail.com>
 *
 * This file is part of Redberry.
 *
 * Redberry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Redberry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Redberry. If not, see <http://www.gnu.org/licenses/>.
 */

package cc.redberry.physics.oneloopdiv;

import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.EliminateFromSymmetriesTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.utils.TensorUtils;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class AveragingTest {

    @Test
    public void test2() {

        Tensor t = Tensors.parse("F^\\alpha\\beta*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}+V^\\beta\\alpha*n_{\\nu}*n_{\\alpha}*n_{\\beta}*n^{\\gamma}");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS);
        t = EliminateMetricsTransformation.ELIMINATE_METRICS.transform(t);
        Tensor expected = Tensors.parse("1/24*V^{\\gamma }_{\\nu }+1/24*V_{\\nu }^{\\gamma }+1/24*V_{\\alpha }^{\\alpha }*d^{\\gamma }_{\\nu }+1/24*d^{\\gamma }_{\\nu }*F_{\\beta }^{\\beta }+1/24*F_{\\nu }^{\\gamma }+1/24*F^{\\gamma }_{\\nu }");
        Assert.assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void test3() {
        Tensor t = Tensors.parse("b_\\mu+a*n_\\mu");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse("b_\\mu")));
    }

    @Test
    public void test4_0() {
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
        for (int i = 0; i < 100; ++i) {
            CC.resetTensorNames();
            Tensor t = Tensors.parse("n^\\mu*n_\\mu*n_\\alpha*n^\\alpha*n_\\nu*n^\\nu");
            Expression d = (Expression) Tensors.parse("d_\\mu^\\mu=4");
            t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
            t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS, d);
            t = EliminateMetricsTransformation.eliminate(t);
            t = d.transform(t);
            if (!TensorUtils.isOne(t))
                System.out.println(t);
            Assert.assertTrue(TensorUtils.isOne(t));
        }
    }

    @Test
    public void test4() {
        Tensor t = Tensors.parse("n^\\mu*n_\\mu*n_\\alpha*n^\\alpha*n_\\nu*n^\\nu*n_\\lambda*n^\\lambda*n_\\rho*n^\\rho");
        Expression d = (Expression) Tensors.parse("d_\\mu^\\mu=4");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS);
        t = EliminateMetricsTransformation.eliminate(t);
        t = d.transform(t);
        Assert.assertTrue(TensorUtils.isOne(t));
    }

    @Test
    public void test5() {
        Tensor ff = (Expression) Tensors.parse("FF=(-1/6)*F^{\\nu \\beta \\epsilon }_{\\zeta }*F_{\\nu \\beta }^{\\zeta }_{\\epsilon }+n^{\\mu }*F^{\\alpha }_{\\nu }^{\\epsilon }_{\\lambda }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }+(-8/3)*n^{\\mu }*F_{\\beta \\nu }^{\\epsilon }_{\\lambda }*n^{\\alpha }*n^{\\beta }*n^{\\nu }*F_{\\alpha \\mu }^{\\lambda }_{\\epsilon }");
        Tensors.addSymmetry("F_{\\mu\\nu\\alpha\\beta}", IndexType.GreekLower, true, new int[]{1, 0, 2, 3});
        ff = new Averaging(Tensors.parseSimple("n_\\mu")).transform(ff);
        ff = ExpandTransformation.expand(ff);
        ff = EliminateMetricsTransformation.ELIMINATE_METRICS.transform(ff);
        ff = ((Expression) Tensors.parse("F_{\\mu}^\\mu_\\alpha\\beta=0")).transform(ff);

        System.out.println(ff);
    }

    @Test
    public void test6() {
        Tensor t = Tensors.parse("a*n_\\mu*n_\\nu");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        Tensor expected = Tensors.parse("1/4*a*g_\\mu\\nu");
        Assert.assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void test7() {
        Tensor t = Tensors.parse("a*n_\\mu*n_\\nu+g_{\\mu\\nu}*n_\\alpha*n^\\alpha+n_\\mu*n_\\nu*n_\\alpha*g^\\alpha");
        Expression d = Tensors.parseExpression("d_\\mu^\\mu =4");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS, d);
        t = EliminateMetricsTransformation.eliminate(t);
        t = d.transform(t);
        Tensor expected = Tensors.parse("(1/4*a+1)*g_\\mu\\nu");
        Assert.assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void test8() {
        Tensor t = Tensors.parse("1");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        Tensor expected = Tensors.parse("1");
        Assert.assertTrue(TensorUtils.equals(t, expected));
    }

    @Test
    public void test9() {
        Tensor t = Tensors.parse("n_\\mu*n_\\nu*n^\\alpha*n^\\beta");
        t = new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse("1/24*(d^{\\alpha }_{\\nu }*d^{\\beta }_{\\mu }+d^{\\alpha }_{\\mu }*d^{\\beta }_{\\nu }+g^{\\alpha \\beta }*g_{\\mu \\nu })")));
    }

    @Test(timeout = 2000)
    public void test10() {
        Tensor t = Tensors.parse("n_\\mu*n_\\nu*n^\\alpha*n^\\beta*n^\\gamma*n^\\lambda*n^\\sigma*n^\\rho*n^\\theta*n^\\zeta");
        new Averaging(Tensors.parseSimple("n_\\mu")).transform(t);
    }

    @Ignore
    @Test
    public void test11() {
        Tensor t = Tensors.parse("(n_{\\sigma}*n^{\\alpha}*R_{\\alpha}^{\\sigma})**2");
        t = new Averaging(Tensors.parseSimple("n_\\alpha")).transform(t);
        t = EliminateMetricsTransformation.eliminate(t);
        System.out.println(t);
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse("(1/16)*R_{\\sigma}^{\\sigma}**2")));
    }

    @Ignore
    @Test
    public void test12() {
        Tensor t = Tensors.parse("2+(n_{\\sigma}*n^{\\alpha}*R_{\\alpha}^{\\sigma})**2");
        t = new Averaging(Tensors.parseSimple("n_\\alpha")).transform(t);
        t = EliminateMetricsTransformation.eliminate(t);
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse("2+(1/16)*R_{\\sigma}^{\\sigma}**2")));
    }

    @Ignore
    @Test
    public void test13() {
        Tensor t = Tensors.parse("n_\\mu*n^\\mu+(n_{\\sigma}*n^{\\alpha}*R_{\\alpha}^{\\sigma})**2");
        t = new Averaging(Tensors.parseSimple("n_\\alpha")).transform(t);
        t = EliminateMetricsTransformation.eliminate(t);
        t = Tensors.parseExpression("d_\\mu^\\mu = 4").transform(t);
        Assert.assertTrue(TensorUtils.equals(t, Tensors.parse("1+(1/16)*R_{\\sigma}^{\\sigma}**2")));
    }

    @Ignore
    @Test
    public void test14() {
        Tensor t = Tensors.parse("(128/5)*(R^{\\sigma}_{\\alpha\\beta\\gamma}*n^{\\gamma}*n_{\\sigma}*n^{\\alpha}*n^{\\beta})**2-(1/6)*R**2+(24/5)*(R_{\\gamma}^{\\sigma}*n_{\\sigma}*n^{\\gamma})**2-(1/3)*R_{\\mu\\nu}*R^{\\mu\\nu}");
        t = new Averaging(Tensors.parseSimple("n_\\alpha")).transform(t);
        t = ExpandTransformation.expand(t);
        t = EliminateMetricsTransformation.eliminate(t);
        t = Tensors.parseExpression("d_\\mu^\\mu = 4").transform(t);
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(t);
    }

    @Ignore
    @Test
    public void test15() {
        Tensor t = Tensors.parse("4*(-(4/5)*(n_{\\rho}*n^{\\beta}*R_{\\beta}^{\\rho})**2+(32/5)*(R^{\\sigma}_{\\alpha\\beta\\gamma}*n^{\\alpha}*n^{\\beta}*n_{\\sigma}*n^{\\gamma})**2)");
        System.out.println(ExpandTransformation.expand(t));
    }

}
