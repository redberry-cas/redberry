/*
 * Copyright (C) 2012 stas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.redberry.core.performance.kv;

import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensors;

/**
 *
 * @author stas
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {


        Expression KINV = Tensors.parseExpression(
                "KINV^{\\alpha\\beta}_{\\mu\\nu} = P^{\\alpha\\beta}_{\\mu\\nu}-1/4*c*g_{\\mu\\nu}*g^{\\alpha\\beta}+"
                + "(1/4)*b*(n_{\\mu}*n^{\\alpha}*d^{\\beta}_{\\nu}+n_{\\mu}*n^{\\beta}*d^{\\alpha}_{\\nu}+n_{\\nu}*n^{\\alpha}*d^{\\beta}_{\\mu}+n_{\\nu}*n^{\\beta}*d^{\\alpha}_{\\mu})+"
                + "c*(n_{\\mu}*n_{\\nu}*g^{\\alpha\\beta}+n^{\\alpha}*n^{\\beta}*g_{\\mu\\nu})-c*b*n_{\\mu}*n_{\\nu}*n^{\\alpha}*n^{\\beta}");
        Expression K = Tensors.parseExpression(
                "K^{\\mu\\nu}^{\\alpha\\beta}_{\\gamma\\delta} = g^{\\mu\\nu}*P^{\\alpha\\beta}_{\\gamma\\delta}+"
                + "(1+2*beta)*((1/4)*(d^{\\mu}_{\\gamma}*g^{\\alpha \\nu}*d^{\\beta}_{\\delta} + d^{\\mu}_{\\delta}*g^{\\alpha \\nu}*d^{\\beta}_{\\gamma}+d^{\\mu}_{\\gamma}*g^{\\beta \\nu}*d^{\\alpha}_{\\delta}+ d^{\\mu}_{\\delta}*g^{\\beta \\nu}*d^{\\alpha}_{\\gamma})+"
                + "(1/4)*(d^{\\nu}_{\\gamma}*g^{\\alpha \\mu}*d^{\\beta}_{\\delta} + d^{\\nu}_{\\delta}*g^{\\alpha \\mu}*d^{\\beta}_{\\gamma}+d^{\\nu}_{\\gamma}*g^{\\beta \\mu}*d^{\\alpha}_{\\delta}+ d^{\\nu}_{\\delta}*g^{\\beta \\mu}*d^{\\alpha}_{\\gamma}) -"
                + "(1/4)*(g_{\\gamma\\delta}*g^{\\mu \\alpha}*g^{\\nu \\beta}+g_{\\gamma\\delta}*g^{\\mu \\beta}*g^{\\nu \\alpha})-"
                + "(1/4)*(g^{\\alpha\\beta}*d^{\\mu}_{\\gamma}*d^{\\nu}_{\\delta}+g^{\\alpha\\beta}*d^{\\mu}_{\\delta}*d^{\\nu}_{\\gamma})+(1/8)*g^{\\mu\\nu}*g_{\\gamma\\delta}*g^{\\alpha\\beta})");
        Expression P = Tensors.parseExpression(
                "P^{\\alpha\\beta}_{\\mu\\nu} = (1/2)*(d^{\\alpha}_{\\mu}*d^{\\beta}_{\\nu}+d^{\\alpha}_{\\nu}*d^{\\beta}_{\\mu})-(1/4)*g_{\\mu\\nu}*g^{\\alpha\\beta}");
        KINV = (Expression) P.transform(KINV);
        K = (Expression) P.transform(K);

//        Expression consts[] = {Tensors.parseExpression("a=1"),
//                               Tensors.parseExpression("b=0"),
//                               Tensors.parseExpression("d=1"),
//                               Tensors.parseExpression("beta=1"),
//                               Tensors.parseExpression("c=0")};
//        for (Expression cons : consts) {
//            KINV = (Expression) cons.transform(KINV);
//            K = (Expression) cons.transform(K);
//        }

        Expression S = (Expression) Tensors.parse("S^\\rho^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression W = (Expression) Tensors.parse("W^{\\alpha\\beta}_{\\mu\\nu}=0");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, OneLoopUtils.antiDeSitterBackround());

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        System.out.println(action.ACTION());
    }
}
