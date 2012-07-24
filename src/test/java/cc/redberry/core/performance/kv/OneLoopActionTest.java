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

import cc.redberry.core.indices.*;
import cc.redberry.core.tensor.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
@Ignore
public class OneLoopActionTest {

    @Test
    public void testDummyMatrices() {
        Tensors.addSymmetry("f_\\mu\\nu", IndexType.GreekLower, false, 1, 0);
        Expression KINV = (Expression) Tensors.parse("KINV^\\mu_\\nu=f^\\mu_\\nu");
        Expression K = (Expression) Tensors.parse("K^\\mu_\\nu^\\alpha_\\beta=g^{\\mu\\alpha}*g_{\\nu\\beta}");
        Expression S = (Expression) Tensors.parse("S^\\rho^\\mu_\\nu=0");
        Expression W = (Expression) Tensors.parse("W^\\mu_\\nu=0");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null);

        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }

    @Test
    public void test1() {
        Expression KINV = (Expression) Tensors.parse("KINV^{\\alpha\\beta}_{\\mu\\nu} = P^{\\alpha\\beta}_{\\mu\\nu}-1/4*c*g_{\\mu\\nu}*g^{\\alpha\\beta}+"
                + "(1/4)*b*(n_{\\mu}*n^{\\alpha}*d^{\\beta}_{\\nu}+n_{\\mu}*n^{\\beta}*d^{\\alpha}_{\\nu}+n_{\\nu}*n^{\\alpha}*d^{\\beta}_{\\mu}+n_{\\nu}*n^{\\beta}*d^{\\alpha}_{\\mu})+"
                + "c*(n_{\\mu}*n_{\\nu}*g^{\\alpha\\beta}+n^{\\alpha}*n^{\\beta}*g_{\\mu\\nu})-c*b*n_{\\mu}*n_{\\nu}*n^{\\alpha}*n^{\\beta}");
        Expression K =
                (Expression) Tensors.parse("K^{\\mu\\nu}^{\\alpha\\beta}_{\\gamma\\delta} = g^{\\mu\\nu}*P^{\\alpha\\beta}_{\\gamma\\delta}+"
                + "(1+2*beta)*((1/4)*(d^{\\mu}_{\\gamma}*g^{\\alpha \\nu}*d^{\\beta}_{\\delta} + d^{\\mu}_{\\delta}*g^{\\alpha \\nu}*d^{\\beta}_{\\gamma}+d^{\\mu}_{\\gamma}*g^{\\beta \\nu}*d^{\\alpha}_{\\delta}+ d^{\\mu}_{\\delta}*g^{\\beta \\nu}*d^{\\alpha}_{\\gamma})+"
                + "(1/4)*(d^{\\nu}_{\\gamma}*g^{\\alpha \\mu}*d^{\\beta}_{\\delta} + d^{\\nu}_{\\delta}*g^{\\alpha \\mu}*d^{\\beta}_{\\gamma}+d^{\\nu}_{\\gamma}*g^{\\beta \\mu}*d^{\\alpha}_{\\delta}+ d^{\\nu}_{\\delta}*g^{\\beta \\mu}*d^{\\alpha}_{\\gamma}) -"
                + "(1/4)*(g_{\\gamma\\delta}*g^{\\mu \\alpha}*g^{\\nu \\beta}+g_{\\gamma\\delta}*g^{\\mu \\beta}*g^{\\nu \\alpha})-"
                + "(1/4)*(g^{\\alpha\\beta}*d^{\\mu}_{\\gamma}*d^{\\nu}_{\\delta}+g^{\\alpha\\beta}*d^{\\mu}_{\\delta}*d^{\\nu}_{\\gamma})+(1/8)*g^{\\mu\\nu}*g_{\\gamma\\delta}*g^{\\alpha\\beta})");
        Expression S = (Expression) Tensors.parse("S^\\rho^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression W = (Expression) Tensors.parse("W^{\\alpha\\beta}_{\\mu\\nu}=0");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null);

        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }

    @Test
    public void testVectorField() {

        Expression KINV =
                (Expression) Tensors.parse("KINV_\\alpha^\\beta=d_\\alpha^\\beta");//+\\lambda/(1-\\lambda)*n_\\alpha*n^\\beta");
        Expression K =
                (Expression) Tensors.parse("K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}");//-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)");
        Expression S =
                (Expression) Tensors.parse("S^\\rho^\\mu_\\nu=0");
        Expression W =
                (Expression) Tensors.parse("W^{\\alpha}_{\\beta}=P^{\\alpha}_{\\beta}");//+(\\lambda/2)*R^\\alpha_\\beta");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null);

        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);
        for (Expression e : input.getNablaS())
            System.out.println(e);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }
}