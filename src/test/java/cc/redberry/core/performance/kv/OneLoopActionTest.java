/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
package cc.redberry.core.performance.kv;

import cc.redberry.core.context.*;
import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.*;
import cc.redberry.core.transformations.*;
import cc.redberry.core.utils.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
//@Ignore
public class OneLoopActionTest {

    @Ignore
    @Test
    public void testDummyMatrices() {
        Tensors.addSymmetry("f_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Expression KINV = (Expression) Tensors.parse("KINV^\\mu_\\nu=f^\\mu_\\nu");
        Expression K = (Expression) Tensors.parse("K^\\mu_\\nu^\\alpha_\\beta=g^{\\mu\\alpha}*g_{\\nu\\beta}");
        Expression S = (Expression) Tensors.parse("S^\\rho^\\mu_\\nu=0");
        Expression W = (Expression) Tensors.parse("W^\\mu_\\nu=0");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, OneLoopUtils.antiDeSitterBackround());

        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }

    @Ignore
    @Test
    public void testVectorField0() {
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+\\gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=P^{\\alpha}_{\\beta}+(\\lambda/2)*R^\\alpha_\\beta");

        Expression lambda = Tensors.parseExpression("\\lambda=0");//gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=0");//gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null);
        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);
        Tensor expected = Tensors.parse("7/60*Power[R, 2]+-4/15*R^{\\mu \\nu }*R_{\\mu \\nu }+1/2*P^{\\gamma }_{\\alpha }*P^{\\alpha }_{\\gamma }+1/6*P*R");
        Assert.assertTrue(TensorUtils.compare(A, expected));
    }

    @Ignore
    @Test
    public void testVectorField0AntiDeSitter() {
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+\\gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=P^{\\alpha}_{\\beta}+(\\lambda/2)*R^\\alpha_\\beta");

        Expression lambda = Tensors.parseExpression("\\lambda=0");//gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=0");//gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, OneLoopUtils.antiDeSitterBackround());
        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);
        Tensor expected = Tensors.parse("-2/3*P*LAMBDA+4/5*Power[LAMBDA, 2]+1/2*P^{\\gamma }_{\\alpha }*P^{\\alpha }_{\\gamma }");
        Assert.assertTrue(TensorUtils.compare(A, expected));
    }

//    @Ignore
    @Test
    public void testVectorField() {
        CC.resetTensorNames(7400654047858175284L);
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+\\gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=P^{\\alpha}_{\\beta}+(\\lambda/2)*R^\\alpha_\\beta");

        Expression lambda = Tensors.parseExpression("\\lambda=gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null);

        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);

        for (Expression e : input.getKnQuantities())
            System.out.println(e);
        System.out.println(input.getHatF());

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);

        for (Tensor s : A)
            if (s instanceof Product && s.getIndices().size() != 0) {
                Product p = (Product) s;
                System.out.println(p.getDataSubProduct() + ":  " + p.getIndexlessSubProduct());
            }


        Tensor expected =
                Tensors.parse("(1/24*Power[gamma,2]+1/4*gamma+1/2)*P_\\mu\\nu*P^\\mu\\nu"
                + "+1/48*Power[gamma,2]*Power[P,2]"
                + "+(1/12*Power[gamma,2]+1/3*gamma)*R_\\mu\\nu*P^\\mu\\nu"
                + "+(1/24*Power[gamma,2]+1/12*gamma+1/6)*R*P"
                + "+(1/24*Power[gamma,2]+1/12*gamma-4/15)*R_\\mu\\nu*R^\\mu\\nu"
                + "+(1/48*Power[gamma,2]+1/12*gamma+7/60)*Power[R,2]");
    }

    @Ignore
    @Test
    public void test12() {

        Tensor expected =
                Tensors.parse("(1/24*Power[gamma,2]+1/4*gamma+1/2)*P_\\mu\\nu*P^\\mu\\nu"
                + "+1/48*Power[gamma,2]*Power[P,2]"
                + "+(1/12*Power[gamma,2]+1/3*gamma)*R_\\mu\\nu*P^\\mu\\nu"
                + "+(1/24*Power[gamma,2]+1/12*gamma+1/6)*R*P"
                + "+(1/24*Power[gamma,2]+1/12*gamma-4/15)*R_\\mu\\nu*R^\\mu\\nu"
                + "+(1/48*Power[gamma,2]+1/12*gamma+7/60)*Power[R,2]");
        expected = Tensors.parseExpression("gamma=0").transform(expected);

        Transformation[] deSitter = ArraysUtils.addAll(new Transformation[]{Tensors.parseExpression("R=R_\\mu^\\mu")}, OneLoopUtils.antiDeSitterBackround());
        for (Transformation t : deSitter)
            expected = t.transform(expected);
        expected = Expand.expand(expected, ContractIndices.INSTANCE);
        expected = Tensors.parseExpression("d_\\mu^\\mu=4").transform(expected);
        expected = ContractIndices.INSTANCE.transform(expected);
        expected = Tensors.parseExpression("d_\\mu^\\mu=4").transform(expected);
        System.out.println(expected);
    }

    @Ignore
    @Test
    public void testSpin3() {
        Expression KINV = (Expression) Tensors.parse("KINV^{\\alpha\\beta}_{\\mu\\nu} = P^{\\alpha\\beta}_{\\mu\\nu}-1/4*c*g_{\\mu\\nu}*g^{\\alpha\\beta}+"
                + "(1/4)*b*(n_{\\mu}*n^{\\alpha}*d^{\\beta}_{\\nu}+n_{\\mu}*n^{\\beta}*d^{\\alpha}_{\\nu}+n_{\\nu}*n^{\\alpha}*d^{\\beta}_{\\mu}+n_{\\nu}*n^{\\beta}*d^{\\alpha}_{\\mu})+"
                + "c*(n_{\\mu}*n_{\\nu}*g^{\\alpha\\beta}+n^{\\alpha}*n^{\\beta}*g_{\\mu\\nu})-c*b*n_{\\mu}*n_{\\nu}*n^{\\alpha}*n^{\\beta}");
        Expression K =
                (Expression) Tensors.parse("K^{\\mu\\nu}^{\\alpha\\beta}_{\\gamma\\delta} = g^{\\mu\\nu}*P^{\\alpha\\beta}_{\\gamma\\delta}+"
                + "(1+2*beta)*((1/4)*(d^{\\mu}_{\\gamma}*g^{\\alpha \\nu}*d^{\\beta}_{\\delta} + d^{\\mu}_{\\delta}*g^{\\alpha \\nu}*d^{\\beta}_{\\gamma}+d^{\\mu}_{\\gamma}*g^{\\beta \\nu}*d^{\\alpha}_{\\delta}+ d^{\\mu}_{\\delta}*g^{\\beta \\nu}*d^{\\alpha}_{\\gamma})+"
                + "(1/4)*(d^{\\nu}_{\\gamma}*g^{\\alpha \\mu}*d^{\\beta}_{\\delta} + d^{\\nu}_{\\delta}*g^{\\alpha \\mu}*d^{\\beta}_{\\gamma}+d^{\\nu}_{\\gamma}*g^{\\beta \\mu}*d^{\\alpha}_{\\delta}+ d^{\\nu}_{\\delta}*g^{\\beta \\mu}*d^{\\alpha}_{\\gamma}) -"
                + "(1/4)*(g_{\\gamma\\delta}*g^{\\mu \\alpha}*g^{\\nu \\beta}+g_{\\gamma\\delta}*g^{\\mu \\beta}*g^{\\nu \\alpha})-"
                + "(1/4)*(g^{\\alpha\\beta}*d^{\\mu}_{\\gamma}*d^{\\nu}_{\\delta}+g^{\\alpha\\beta}*d^{\\mu}_{\\delta}*d^{\\nu}_{\\gamma})+(1/8)*g^{\\mu\\nu}*g_{\\gamma\\delta}*g^{\\alpha\\beta})");
        Expression P =
                (Expression) Tensors.parse("P^{\\alpha\\beta}_{\\mu\\nu} = (1/2)*(d^{\\alpha}_{\\mu}*d^{\\beta}_{\\nu}+d^{\\alpha}_{\\nu}*d^{\\beta}_{\\mu})-"
                + "(1/4)*g_{\\mu\\nu}*g^{\\alpha\\beta}");
        KINV = (Expression) P.transform(KINV);
        K = (Expression) P.transform(K);

        Expression consts[] = {Tensors.parseExpression("a=1"),
                               Tensors.parseExpression("b=0"),
                               Tensors.parseExpression("d=1"),
                               Tensors.parseExpression("beta=1"),
                               Tensors.parseExpression("c=0")};
        for (Expression cons : consts) {
            KINV = (Expression) cons.transform(KINV);
            K = (Expression) cons.transform(K);
        }

        Expression S = (Expression) Tensors.parse("S^\\rho^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression W = (Expression) Tensors.parse("W^{\\alpha\\beta}_{\\mu\\nu}=0");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, OneLoopUtils.antiDeSitterBackround());

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }
}