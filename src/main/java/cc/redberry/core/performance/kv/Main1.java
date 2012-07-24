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
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.ContractIndices;
import cc.redberry.core.transformations.Expand;
import cc.redberry.core.transformations.Transformation;

/**
 *
 * @author stas
 */
public class Main1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("POIHALLLI");
        Tensor t = Tensors.parse("HATK^{\\mu \\epsilon \\zeta }_{\\eta \\theta }*HATK^{\\alpha \\gamma \\delta }_{\\epsilon \\zeta }*HATK^{\\nu \\eta \\theta }_{\\kappa_1 \\lambda_1 }*HATK^{\\beta \\theta_1 \\iota_1 }_{\\gamma \\delta }");

        Expression P =
                (Expression) Tensors.parse("P^{\\alpha\\beta}_{\\mu\\nu} = (1/2)*(d^{\\alpha}_{\\mu}*d^{\\beta}_{\\nu}+d^{\\alpha}_{\\nu}*d^{\\beta}_{\\mu})-"
                + "(1/4)*g_{\\mu\\nu}*g^{\\alpha\\beta}");
        Expression e1 = (Expression) Tensors.parse("HATK^{\\alpha \\beta \\gamma \\delta }_{\\epsilon \\zeta } = 2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*n_{\\zeta }*n^{\\beta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n^{\\alpha }*n_{\\zeta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\zeta }*n_{\\epsilon }*n^{\\beta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n^{\\alpha }*n_{\\epsilon }+2*(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n^{\\beta }*n^{\\alpha }+-1/4*c*g^{\\gamma \\delta }*g^{\\alpha \\beta }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*P^{\\gamma \\delta }_{\\zeta }^{\\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*P^{\\gamma \\delta }_{\\epsilon }^{\\alpha }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*P^{\\gamma \\delta \\alpha }_{\\zeta }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*P^{\\gamma \\delta \\beta \\alpha }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*P^{\\gamma \\delta }_{\\epsilon }^{\\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*P^{\\gamma \\delta }_{\\zeta }^{\\alpha }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*P^{\\gamma \\delta \\beta }_{\\epsilon }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*P^{\\gamma \\delta \\alpha \\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*P^{\\gamma \\delta \\alpha }_{\\epsilon }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*P^{\\gamma \\delta \\beta }_{\\zeta }+(1/8+1/4*beta)*c*g^{\\gamma \\delta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\eta }*n^{\\eta }+-1/4*(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\gamma \\delta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }+-1*b*c*g^{\\alpha \\beta }*n_{\\eta }*n_{\\theta }*n^{\\delta }*n^{\\gamma }*P^{\\eta \\theta }_{\\epsilon \\zeta }+(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n^{\\gamma }*n^{\\delta }+c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*n_{\\eta }*n_{\\theta }*P^{\\eta \\theta }_{\\epsilon \\zeta }+c*g^{\\alpha \\beta }*n^{\\gamma }*n^{\\delta }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\eta }*n^{\\eta }+(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\eta }*n^{\\eta }+1/4*b*g^{\\alpha \\beta }*n_{\\theta }*n^{\\gamma }*P^{\\delta \\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\theta }*n^{\\delta }*P^{\\gamma \\theta }_{\\epsilon \\zeta }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n^{\\delta }*n^{\\gamma }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n^{\\delta }*n^{\\gamma }+1/4*b*g^{\\alpha \\beta }*n_{\\eta }*n^{\\delta }*P^{\\eta \\gamma }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\eta }*n^{\\gamma }*P^{\\eta \\delta }_{\\epsilon \\zeta }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\epsilon }*n^{\\delta }*n^{\\gamma }*n_{\\zeta }*n^{\\alpha }+-2*b*(-1/4+-1/2*beta)*c*g_{\\epsilon \\zeta }*n^{\\delta }*n^{\\gamma }*n^{\\alpha }*n^{\\beta }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\zeta }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\epsilon }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\zeta }*n^{\\delta }*n^{\\gamma }*n_{\\epsilon }*n^{\\alpha }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n^{\\gamma }*n^{\\delta }+-1*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*d^{\\alpha }_{\\zeta }+-1*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }+-1*b*(-1/4+-1/2*beta)*c*d^{\\beta }_{\\zeta }*d^{\\alpha }_{\\epsilon }*n_{\\theta }*n^{\\theta }*n^{\\delta }*n^{\\gamma }+-1*b*(-1/4+-1/2*beta)*c*d^{\\beta }_{\\epsilon }*d^{\\alpha }_{\\zeta }*n_{\\theta }*n^{\\theta }*n^{\\delta }*n^{\\gamma }+-1/2*(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*g^{\\alpha \\beta }+-1*b*(1/8+1/4*beta)*c*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\theta }*n^{\\theta }*n^{\\delta }*n^{\\gamma }+(b*(1/8+1/4*beta)+2*(-1/4+-1/2*beta)*c)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n^{\\delta }*n^{\\gamma }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*g^{\\gamma \\delta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*g^{\\gamma \\delta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*P^{\\gamma \\delta \\theta }_{\\theta }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*P^{\\gamma \\delta \\theta }_{\\theta }+(1/8+1/4*beta)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*P^{\\gamma \\delta \\theta }_{\\theta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\gamma }*n^{\\delta }*n_{\\epsilon }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\alpha \\delta }*n^{\\gamma }*n^{\\beta }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\delta }*n^{\\gamma }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\delta }*n^{\\gamma }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*g^{\\alpha \\delta }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n^{\\delta }*n^{\\beta }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\gamma }*n^{\\delta }*n^{\\alpha }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\alpha \\gamma }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\delta }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\delta }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*g^{\\alpha \\delta }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n^{\\delta }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*g^{\\alpha \\gamma }*n^{\\delta }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\delta }*n^{\\gamma }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\delta }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n^{\\delta }*n^{\\alpha }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\delta }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*g^{\\alpha \\gamma }*n^{\\delta }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\gamma }*n^{\\delta }*n_{\\zeta }+g^{\\alpha \\beta }*P^{\\eta \\theta }_{\\epsilon \\zeta }*P^{\\gamma \\delta }_{\\eta \\theta }");
        Expression e2 = (Expression) Tensors.parse("HATK^{\\beta \\gamma \\delta }_{\\epsilon \\zeta } = -1/4*(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\alpha \\beta }*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }+(b*(-1/4+-1/2*beta)+4*(1/4+1/2*beta)*c)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }+c*g^{\\alpha \\beta }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\theta }*n^{\\gamma }*P^{\\delta \\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\theta }*n^{\\delta }*P^{\\gamma \\theta }_{\\epsilon \\zeta }+c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*n_{\\alpha }*n_{\\eta }*n_{\\theta }*P^{\\eta \\theta }_{\\epsilon \\zeta }+-1*b*(1/8+1/4*beta)*c*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n_{\\theta }*n^{\\theta }*n^{\\gamma }*n^{\\delta }+2*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*n_{\\alpha }*n_{\\zeta }*n^{\\beta }+2*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*n_{\\alpha }*n_{\\epsilon }*n^{\\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta \\alpha }_{\\zeta }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\alpha \\beta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\alpha }_{\\epsilon }+g^{\\alpha \\beta }*n_{\\alpha }*P^{\\eta \\theta }_{\\epsilon \\zeta }*P^{\\gamma \\delta }_{\\eta \\theta }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }+-1/4*(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }+-1/2*(-1/4+-1/2*beta)*c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta }_{\\zeta }^{\\beta }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta }_{\\epsilon }^{\\beta }+(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\beta }_{\\epsilon }+(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta \\beta }_{\\zeta }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\delta }*n^{\\alpha }*n_{\\alpha }*n^{\\gamma }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\delta }*n^{\\alpha }*n_{\\alpha }*n^{\\gamma }+1/2*b*(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*g^{\\beta \\gamma }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*d_{\\epsilon }^{\\delta }*n^{\\alpha }*n_{\\alpha }*n^{\\gamma }+1/2*b*(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }+(1/4+1/2*beta)*d^{\\beta }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta }_{\\epsilon }^{\\alpha }+(-1/4+-1/2*beta)*g_{\\epsilon \\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\beta \\alpha }+(1/4+1/2*beta)*d^{\\beta }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta }_{\\zeta }^{\\alpha }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\epsilon }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n_{\\zeta }+-2*b*(-1/4+-1/2*beta)*c*g_{\\epsilon \\zeta }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n^{\\beta }+-2*b*(1/4+1/2*beta)*c*d^{\\beta }_{\\zeta }*n^{\\alpha }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n_{\\epsilon }+-1*b*c*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\eta }*n_{\\theta }*n^{\\gamma }*n^{\\delta }*P^{\\eta \\theta }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\eta }*n^{\\delta }*P^{\\eta \\gamma }_{\\epsilon \\zeta }+1/4*b*g^{\\alpha \\beta }*n_{\\alpha }*n_{\\eta }*n^{\\gamma }*P^{\\eta \\delta }_{\\epsilon \\zeta }+(b*(1/8+1/4*beta)+2*(-1/4+-1/2*beta)*c)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\zeta }+-2*b*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\gamma }*n^{\\beta }*n_{\\epsilon }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\theta }_{\\theta }+(-1/4+-1/2*beta)*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*P^{\\gamma \\delta \\theta }_{\\theta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n^{\\alpha }*n_{\\alpha }*n_{\\zeta }+2*(1/4+1/2*beta)*c*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n^{\\alpha }*n_{\\alpha }*n_{\\epsilon }+2*(-1/4+-1/2*beta)*c*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n^{\\alpha }*n_{\\alpha }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\gamma }*n_{\\alpha }*n^{\\delta }*n_{\\epsilon }+1/2*b*(-1/4+-1/2*beta)*g^{\\alpha \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\gamma }*n_{\\alpha }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\gamma }*n_{\\alpha }*n^{\\delta }*n^{\\beta }+1/2*b*(-1/4+-1/2*beta)*g^{\\alpha \\gamma }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\delta }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*d_{\\zeta }^{\\delta }*n_{\\alpha }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*d_{\\epsilon }^{\\delta }*n_{\\alpha }*n^{\\gamma }*n^{\\beta }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\gamma }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\delta }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\delta }*n_{\\alpha }*n^{\\gamma }*n_{\\zeta }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\zeta }*g^{\\beta \\delta }*n_{\\alpha }*n^{\\gamma }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*g^{\\alpha \\gamma }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\delta }*n_{\\epsilon }+1/2*b*(1/4+1/2*beta)*d^{\\alpha }_{\\epsilon }*g^{\\beta \\gamma }*n_{\\alpha }*n^{\\delta }*n_{\\zeta }+-1*b*(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n_{\\theta }*n^{\\theta }*n^{\\gamma }*n^{\\delta }+-1*b*(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n_{\\theta }*n^{\\theta }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\epsilon }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\theta }_{\\theta }*d^{\\alpha }_{\\zeta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }+(1/8+1/4*beta)*c*d^{\\theta }_{\\theta }*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n^{\\gamma }*n^{\\delta }+(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }*n_{\\eta }*n^{\\eta }+(-1/4+-1/2*beta)*c*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }*n_{\\eta }*n^{\\eta }+-1*(1/4+1/2*beta)*c*d^{\\alpha }_{\\zeta }*g^{\\gamma \\delta }*d^{\\beta }_{\\epsilon }*n_{\\alpha }+-1*(1/4+1/2*beta)*c*d^{\\alpha }_{\\epsilon }*g^{\\gamma \\delta }*d^{\\beta }_{\\zeta }*n_{\\alpha }+(1/8+1/4*beta)*c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*g_{\\epsilon \\zeta }*n_{\\alpha }*n_{\\eta }*n^{\\eta }+-1/4*c*g^{\\alpha \\beta }*g^{\\gamma \\delta }*n_{\\alpha }*P_{\\theta }^{\\theta }_{\\epsilon \\zeta }+(1/8+1/4*beta)*g^{\\alpha \\beta }*g_{\\epsilon \\zeta }*n_{\\alpha }*P^{\\gamma \\delta \\theta }_{\\theta }");
        e1 = (Expression) P.transform(e1);
        e2 = (Expression) P.transform(e2);
        t = e1.transform(t);
        t = e2.transform(t);
        Expression kronecker = (Expression) Tensors.parse("d_\\mu^\\mu=4");
        System.out.println("POIHALLLI");

        t = Expand.expand(t, new Transformation[]{ContractIndices.CONTRACT_INDICES, new SqrSubs(Tensors.parseSimple("n_\\mu")), kronecker}, 1);
        System.out.println(t);
    }
}
