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
        Expression F = Tensors.parseExpression("F_\\mu\\nu^\\alpha_\\beta=0");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F, OneLoopUtils.antiDeSitterBackround());

        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }

    @Test
    public void testVectorField0() {
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+\\gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=P^{\\alpha}_{\\beta}+(\\lambda/2)*R^\\alpha_\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=R_\\mu\\nu\\alpha\\beta");

        Expression lambda = Tensors.parseExpression("\\lambda=0");//gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=0");//gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);
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
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=R_\\mu\\nu\\alpha\\beta");

        Expression lambda = Tensors.parseExpression("\\lambda=0");//gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=0");//gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F, OneLoopUtils.antiDeSitterBackround());
        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);
        Tensor expected = Tensors.parse("-2/3*P*LAMBDA+4/5*Power[LAMBDA, 2]+1/2*P^{\\gamma }_{\\alpha }*P^{\\alpha }_{\\gamma }");
        Assert.assertTrue(TensorUtils.compare(A, expected));
    }

    @Test
    public void testVectorField() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+\\gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=g^{\\mu\\nu}*d_{\\alpha}^{\\beta}-\\lambda/2*(g^{\\mu\\beta}*d_\\alpha^\\nu+g^{\\nu\\beta}*d_\\alpha^\\mu)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=P^{\\alpha}_{\\beta}+(\\lambda/2)*R^\\alpha_\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=R_\\mu\\nu\\alpha\\beta");


        Expression lambda = Tensors.parseExpression("\\lambda=gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);

        Tensor expected = Expand.expand(Tensors.parse("-5/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+47/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+1789/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+929/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-19/120*gamma*Power[gamma+1, -1]*Power[R, 2]+167/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+-5/72*R*Power[gamma+1, -1]*P*Power[gamma, 4]+-337/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+7/60*Power[R, 2]+-1/24*R*Power[gamma+1, -1]*P*Power[gamma, 2]+1/12*gamma*R*P+-109/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-37/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+1453/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+-1409/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/72*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 6]+(6851/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+11/20*Power[gamma+1, -1]*Power[gamma, 5]+-39/80*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-199/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-107/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+1333/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-23/120*Power[gamma, 4]+-49/60*Power[gamma, 2]+-67/120*Power[gamma, 3]+1259/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-133/144*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+11/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+31/64*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-41/60*gamma+29/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+3869/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+23/30*gamma*Power[gamma+1, -1]+811/360*Power[gamma+1, -1]*Power[gamma, 3]+329/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-4/15+-6631/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+97/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+1277/720*Power[gamma+1, -1]*Power[gamma, 2]+-2489/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1319/720*Power[gamma+1, -1]*Power[gamma, 4]+-2627/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/120*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-3253/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-965/576*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-9/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+17/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 10]+-1511/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-341/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+737/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-11/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3])*R^{\\mu \\nu }*R_{\\mu \\nu }+1/48*Power[P, 2]*Power[gamma, 2]+(-5/36*Power[gamma+1, -1]*Power[gamma, 4]+-7/12*Power[gamma+1, -1]*Power[gamma, 2]+-37/72*Power[gamma+1, -1]*Power[gamma, 3]+1/6*Power[gamma, 2]+1/18*Power[gamma, 3]+1/6*gamma+1/6*gamma*Power[gamma+1, -1]+73/72*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/9*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+2/3*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+11/24*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6])*P^{\\mu \\alpha }*R_{\\mu \\alpha }+1/6*R*P+-1391/1440*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+319/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-203/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/18*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+29/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+49/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+-271/480*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+29/120*gamma*Power[R, 2]+1/12*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+19/288*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+-1/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+1/20*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+9/80*Power[R, 2]*Power[gamma, 4]+17/40*Power[R, 2]*Power[gamma, 2]+2761/11520*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/12*R*P*Power[gamma, 2]+-37/120*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/36*R*P*Power[gamma, 3]+-497/1152*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+4669/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+83/240*Power[R, 2]*Power[gamma, 3]+-43/40*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+53/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+-403/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+(1/24*Power[gamma, 2]+1/4*gamma+1/2)*P^{\\alpha }_{\\rho_5 }*P^{\\rho_5 }_{\\alpha }+1/480*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+-13/144*R*Power[gamma+1, -1]*P*Power[gamma, 3]+-19/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 10]"));
        //ACTION = 4669/5760*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-497/1152*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1409/2880*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+53/720*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1391/1440*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]+1/480*Power[R, 2]*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/36*P*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+29/120*Power[R, 2]*gamma+-19/120*Power[R, 2]*gamma*Power[gamma+1, -1]+829/5760*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+9/80*Power[R, 2]*Power[gamma, 4]+17/40*Power[R, 2]*Power[gamma, 2]+-271/480*Power[R, 2]*Power[gamma, 2]*Power[gamma+1, -1]+47/180*Power[R, 2]*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+83/240*Power[R, 2]*Power[gamma, 3]+49/720*Power[R, 2]*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/18*P*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+-37/120*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]+929/5760*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-43/40*Power[R, 2]*Power[gamma, 3]*Power[gamma+1, -1]+-37/240*Power[R, 2]*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/12*P*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+(1/4*gamma+1/2+1/24*Power[gamma, 2])*P^{\\alpha }_{\\rho_5 }*P^{\\rho_5 }_{\\alpha }+1439/5760*Power[R, 2]*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+7/60*Power[R, 2]+-203/3840*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/72*P*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+1/48*Power[gamma, 2]*Power[P, 2]+-13/144*P*Power[gamma, 3]*Power[gamma+1, -1]*R+1/6*P*R+-403/5760*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1453/1920*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+(329/960*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1427/720*Power[gamma, 4]*Power[gamma+1, -1]+127/720*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-125/576*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-127/240*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]+31/64*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-169/576*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+179/192*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-287/720*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1289/576*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1*(101/240*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/30*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+15/8*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-2/15*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-73/240*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-113/80*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+3/20*Power[gamma, 4]*Power[gamma+1, -1]+11/20*Power[gamma, 3]*Power[gamma+1, -1]+23/60*Power[gamma, 2]*Power[gamma+1, -1]+1/5*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/40*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-9/20*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/15*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+187/240*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-173/80*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-3/40*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+13/6*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+7/240*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-7/40*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/30*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-4/5*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1])+17/240*Power[gamma, 10]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1427/2880*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/20*Power[gamma, 5]*Power[gamma+1, -1]+-4/15*gamma+11/15+107/1440*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-3121/960*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-817/360*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/60*gamma*Power[gamma+1, -1]+241/90*Power[gamma, 3]*Power[gamma+1, -1]+29/320*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1129/5760*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+383/2880*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-23/120*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-17/30*Power[gamma, 2]+-23/120*Power[gamma, 4]+-67/120*Power[gamma, 3]+-1*(-77/192*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-43/48*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+-47/96*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]+-29/192*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+49/64*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+5/16*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/12*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/3*Power[gamma, 2]*Power[gamma+1, -1]+-1/8*Power[gamma, 3]*Power[gamma+1, -1]+5/12*gamma+1+-3/4*gamma*Power[gamma+1, -1]+-5/24*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/24*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/4*Power[gamma, 2]+-1/4*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/24*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/16*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-13/96*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/32*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/12*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1])+353/2880*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+625/1152*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+349/288*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+97/320*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/8*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+137/1920*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/6*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1313/720*Power[gamma, 2]*Power[gamma+1, -1])*R_{\\delta \\zeta }*R^{\\zeta \\delta }+1789/5760*Power[R, 2]*Power[gamma, 7]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/144*P*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+167/3840*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/12*P*Power[gamma, 2]*R+1/36*P*Power[gamma, 3]*R+-337/5760*Power[R, 2]*Power[gamma, 9]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-109/5760*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+319/1440*Power[R, 2]*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/144*P*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+1/12*P*gamma*R+(1/36*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/36*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/36*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/36*Power[gamma, 6]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/36*Power[gamma, 4]*Power[gamma+1, -1]+-7/12*Power[gamma, 2]*Power[gamma+1, -1]+-37/72*Power[gamma, 3]*Power[gamma+1, -1]+73/72*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/6*gamma+1/6*gamma*Power[gamma+1, -1]+1/6*Power[gamma, 2]+1/18*Power[gamma, 3]+1/9*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]+2/3*Power[gamma, 2]*Power[gamma+1, -1]*Power[gamma+1, -1]+11/24*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1])*P_{\\sigma }^{\\alpha }*R_{\\alpha }^{\\sigma }+-19/1440*Power[R, 2]*Power[gamma, 10]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-5/72*P*Power[gamma, 4]*Power[gamma+1, -1]*R+29/1920*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-1/24*P*Power[gamma, 2]*Power[gamma+1, -1]*R+19/288*Power[R, 2]*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/36*P*Power[gamma, 3]*Power[gamma+1, -1]*Power[gamma+1, -1]*R+2761/11520*Power[R, 2]*Power[gamma, 4]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+1/20*Power[R, 2]*Power[gamma, 5]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]+-37/384*Power[R, 2]*Power[gamma, 8]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]
        Assert.assertTrue(TensorUtils.compare(Expand.expand(A), expected));


        //simplified result
        //Tensor expected =
        //        Tensors.parse("(1/24*Power[gamma,2]+1/4*gamma+1/2)*P_\\mu\\nu*P^\\mu\\nu"
        //        + "+1/48*Power[gamma,2]*Power[P,2]"
        //        + "+(1/12*Power[gamma,2]+1/3*gamma)*R_\\mu\\nu*P^\\mu\\nu"
        //        + "+(1/24*Power[gamma,2]+1/12*gamma+1/6)*R*P"
        //        + "+(1/24*Power[gamma,2]+1/12*gamma-4/15)*R_\\mu\\nu*R^\\mu\\nu"
        //        + "+(1/48*Power[gamma,2]+1/12*gamma+7/60)*Power[R,2]");
    }

    @Test
    public void testSquaredVectorField() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+(2*\\gamma+Power[\\gamma,2])*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu\\gamma\\delta}_\\alpha^{\\beta}="
                + "d_\\alpha^\\beta*1/3*(g^{\\mu\\nu}*g^{\\gamma\\delta}+ g^{\\mu\\gamma}*g^{\\nu\\delta}+ g^{\\mu\\delta}*g^{\\nu\\gamma})"
                + "+1/12*(-2*\\lambda+Power[\\lambda,2])*("
                + "g^{\\mu\\nu}*d_\\alpha^\\gamma*g^{\\beta\\delta}"
                + "+g^{\\mu\\nu}*d_\\alpha^\\delta*g^{\\beta\\gamma}"
                + "+g^{\\mu\\gamma}*d_\\alpha^\\nu*g^{\\beta\\delta}"
                + "+g^{\\mu\\gamma}*d_\\alpha^\\delta*g^{\\beta\\nu}"
                + "+g^{\\mu\\delta}*d_\\alpha^\\nu*g^{\\beta\\gamma}"
                + "+g^{\\mu\\delta}*d_\\alpha^\\gamma*g^{\\beta\\nu}"
                + "+g^{\\nu\\gamma}*d_\\alpha^\\mu*g^{\\beta\\delta}"
                + "+g^{\\nu\\gamma}*d_\\alpha^\\delta*g^{\\beta\\mu}"
                + "+g^{\\nu\\delta}*d_\\alpha^\\mu*g^{\\beta\\gamma}"
                + "+g^{\\nu\\delta}*d_\\alpha^\\gamma*g^{\\beta\\mu}"
                + "+g^{\\gamma\\delta}*d_\\alpha^\\mu*g^{\\beta\\nu}"
                + "+g^{\\gamma\\delta}*d_\\alpha^\\nu*g^{\\beta\\mu})");
        Expression S = Tensors.parseExpression("S^\\mu\\nu\\rho\\alpha\\beta=0");
        //W^{\\mu \\nu }_{\\alpha }^{\\beta } = d^{\\nu }_{\\alpha }*R^{\\beta \\mu }+d^{\\mu }_{\\alpha }*R^{\\beta \\nu }+g^{\\mu \\beta }*R_{\\alpha }^{\\nu }+2*P_{\\alpha }^{\\beta }*g^{\\mu \\nu }+-2/3*d_{\\alpha }^{\\beta }*R^{\\mu \\nu }
        Expression W = Tensors.parseExpression("W^{\\mu\\nu}_\\alpha^\\beta="
                + "2*P_{\\alpha}^{\\beta}*g^{\\mu\\nu}-2/3*R^\\mu\\nu*d_\\alpha^\\beta"
                + "-\\lambda/2*P_\\alpha^\\mu*g^\\nu\\beta"
                + "-\\lambda/2*P_\\alpha^\\nu*g^\\mu\\beta"
                + "-\\lambda/2*P^\\beta\\mu*d^\\nu_\\alpha"
                + "-\\lambda/2*P^\\beta\\nu*d^\\mu_\\alpha"
                + "+1/6*(\\lambda-2*Power[\\lambda,2])*("
                + "R_\\alpha^\\mu*g^\\nu\\beta"
                + "+R_\\alpha^\\nu*g^\\mu\\beta"
                + "+R^\\beta\\mu*d^\\nu_\\alpha"
                + "+R^\\beta\\nu*d^\\mu_\\alpha)"
                + "+1/6*(2*\\lambda-Power[\\lambda,2])*"
                + "(R_\\alpha^\\mu\\beta\\nu+R_\\alpha^\\nu\\beta\\mu)"
                + "+1/2*(2*\\lambda-Power[\\lambda,2])*g^\\mu\\nu*R_\\alpha^\\beta");
        Expression N = Tensors.parseExpression("N^\\rho\\alpha\\beta=0");
        Expression M = Tensors.parseExpression("M_\\alpha^\\beta = "
                + "P_\\alpha\\mu*P^\\mu\\beta-1/2*R_\\mu\\nu\\gamma\\alpha*R^\\mu\\nu\\gamma\\beta"
                + "+\\lambda/2*P_\\alpha\\mu*R^\\mu\\beta"
                + "+\\lambda/2*P_\\mu\\nu*R^\\mu_\\alpha^\\nu\\beta"
                + "+1/6*(\\lambda-2*Power[\\lambda,2])*R_\\alpha\\mu*R^\\mu\\beta"
                + "+1/12*(4*\\lambda+7*Power[\\lambda,2])*R_\\mu\\alpha\\nu^\\beta*R^\\mu\\nu"
                + "+1/4*(2*\\lambda-Power[\\lambda,2])*R_\\alpha\\mu\\nu\\gamma*R^\\gamma\\mu\\nu\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=R_\\mu\\nu\\alpha\\beta");


        Expression lambda = Tensors.parseExpression("\\lambda=0");//gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=0");//gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));
        M = (Expression) gamma.transform(lambda.transform(M));

        System.out.println(K);
        System.out.println(S);
        System.out.println(W);
        System.out.println(M);

        System.out.println(KINV);
        System.out.println(K);
        System.out.println(S);
        System.out.println(W);
        System.out.println(M);
        OneLoopInput input = new OneLoopInput(4, KINV, K, S, W, N, M, F);
        System.out.println(input.getL());
        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);
        for (Expression e : input.getKnQuantities())
            System.out.println(e);


        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);
        System.out.println(action.DELTA_1());
        System.out.println(action.DELTA_2());
        System.out.println(action.DELTA_3());
        System.out.println(action.DELTA_4());

        TensorBuilder sb = SumBuilderFactory.defaultSumBuilder();
        for (Tensor s : A)
            if (s instanceof Product && s.getIndices().size() != 0) {
                Product p = (Product) s;
                System.out.println(p.getDataSubProduct() + ":  " + p.getIndexlessSubProduct());
            } else
                sb.put(s);
        System.out.println(sb.build());
    }

    @Test
    public void testLambdaGaugeGravityGhosts0() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=d_\\alpha^\\beta*g^\\mu\\nu-1/2*beta*(d_\\alpha^\\mu*g^\\nu\\beta+d_\\alpha^\\nu*g^\\mu\\beta)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=(1+beta/2)*R^\\alpha_\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=R_\\mu\\nu\\alpha\\beta");


        Expression beta = Tensors.parseExpression("beta=0");
        Expression lambda = Tensors.parseExpression("gamma=beta/(1-beta)");
        KINV = (Expression) beta.transform(lambda.transform(KINV));
        K = (Expression) beta.transform(lambda.transform(K));
        S = (Expression) beta.transform(lambda.transform(S));
        W = (Expression) beta.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION();

        Tensor expected = Expand.expand(Tensors.parse("ACTION = 7/30*R^{\\mu \\nu }*R_{\\mu \\nu }+17/60*Power[R, 2]"));
        Assert.assertTrue(TensorUtils.compare(Expand.expand(A), expected));
    }

    @Test
    public void testLambdaGaugeGravityGhosts() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta+gamma*n_\\alpha*n^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu}_\\alpha^{\\beta}=d_\\alpha^\\beta*g^\\mu\\nu-1/2*beta*(d_\\alpha^\\mu*g^\\nu\\beta+d_\\alpha^\\nu*g^\\mu\\beta)");
        Expression S = Tensors.parseExpression("S^\\rho^\\mu_\\nu=0");
        Expression W = Tensors.parseExpression("W^{\\alpha}_{\\beta}=(1+beta/2)*R^\\alpha_\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=R_\\mu\\nu\\alpha\\beta");


        Expression beta = Tensors.parseExpression("beta=gamma/(1+gamma)");
        KINV = (Expression) beta.transform(KINV);
        K = (Expression) beta.transform(K);
        S = (Expression) beta.transform(S);
        W = (Expression) beta.transform(W);

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);

        //TODO simplify result
        //non simplified result
        Tensor expected = Expand.expand(Tensors.parse("17/60*Power[R, 2]+1789/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-203/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+-337/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+61/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-109/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-497/480*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+-497/1152*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-97/160*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+-19/120*Power[1+gamma, -1]*Power[R, 2]*gamma+2441/11520*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+(17/320*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-67/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+71/60*gamma+7/120*Power[gamma, 4]+161/120*Power[gamma, 2]+233/360*Power[gamma, 3]+-1*(29/20*gamma+1/4*Power[gamma, 4]+23/20*Power[gamma, 3]+39/20*Power[gamma, 2]+83/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-43/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+3/5*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+667/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-121/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+99/160*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-7/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+7/48*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-13/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+-181/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-33/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-29/20*Power[1+gamma, -1]*gamma+-7/10*Power[1+gamma, -1]*Power[gamma, 5]+103/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-117/40*Power[1+gamma, -1]*Power[gamma, 2]+8/5+-173/40*Power[1+gamma, -1]*Power[gamma, 3]+-37/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-341/120*Power[1+gamma, -1]*Power[gamma, 4]+293/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+281/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-29/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+11/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+37/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-13/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-14/15*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-139/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+317/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3])+481/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1009/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+899/288*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+13/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-1231/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-31/60*Power[1+gamma, -1]*gamma+-3/20*Power[1+gamma, -1]*Power[gamma, 5]+35/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-4661/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+1/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+11/6+127/90*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+3509/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+3919/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+1877/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1/24*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+49/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+-827/720*Power[1+gamma, -1]*Power[gamma, 4]+-931/360*Power[1+gamma, -1]*Power[gamma, 3]+-1559/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+59/144*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+1441/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+5/64*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-1249/720*Power[1+gamma, -1]*Power[gamma, 2]+-1/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+731/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5])*R^{\\gamma }_{\\mu }*R^{\\mu }_{\\gamma }+1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+13/40*Power[R, 2]*gamma+-839/720*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+9/80*Power[R, 2]*Power[gamma, 4]+127/240*Power[R, 2]*Power[gamma, 2]+29/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+269/720*Power[R, 2]*Power[gamma, 3]+49/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+-37/120*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+3/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-403/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+167/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+5149/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+53/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+283/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-19/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 10]+-37/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+-1409/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+11/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+4679/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+319/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]"));
        Assert.assertTrue(TensorUtils.compare(Expand.expand(A), expected));

        //simplified result
        //Tensor expected = Tensors.parse("(7/30+2/3*gamma+1/6*gamma^2)*R_\\mu\\nu*R^\\mu\\nu + (17/60+1/6*gamma+5/60*gamma**2)*R**2");
    }

    @Test
    public void testMinimalSecondOrderOperator() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha^\\beta=d_\\alpha^\\beta*g^{\\mu\\nu}");
        Expression S = Tensors.parseExpression("S^\\mu\\alpha\\beta=0");
        Expression W = Tensors.parseExpression("W_\\alpha^\\beta=W_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);
        A = RemoveDueToSymmetry.INSANCE.transform(A);

        //this is the exact K.V. result with corrections that 1/12*F_..*F^.. and oth are not under tr operation and that tr of 1 is 4
        Tensor expected = Tensors.parse("1/30*Power[R, 2]+1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/15*R_{\\delta \\nu }*R^{\\delta \\nu }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/6*R*W^{\\beta }_{\\beta }");
        Assert.assertTrue(TensorUtils.compare(A, expected));

    }

    @Test
    public void testMinimalSecondOrderOperatorBarvinskyVilkovisky() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);

        //Phys. Rep. 119 ( 1985) 1-74 
        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha^\\beta=d_\\alpha^\\beta*g^{\\mu\\nu}");
        Expression S = Tensors.parseExpression("S^\\mu\\alpha\\beta=0");
        //here P^... from BV equal to W^...
        Expression W = Tensors.parseExpression("W_\\alpha^\\beta=W_\\alpha^\\beta-1/6*R*d_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);
        A = RemoveDueToSymmetry.INSANCE.transform(A);
        System.out.println(A);
        //this is the exact Barvinsky and Vilkovisky
        Tensor expected = Tensors.parse("1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/2*W^{\\rho_5 }_{\\alpha }*W^{\\alpha }_{\\rho_5 }+-1/45*Power[R, 2]+1/15*R^{\\mu \\nu }*R_{\\mu \\nu }");
        Assert.assertTrue(TensorUtils.compare(A, expected));
    }

    @Test
    public void testMinimalFourthOrderOperator() {
        CC.setDefaultPrintMode(ToStringMode.REDBERRY_SOUT);
        Tensors.addSymmetry("P_\\mu\\nu", IndexType.GreekLower, false, 1, 0);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^{\\mu\\nu\\gamma\\delta}_\\alpha^{\\beta}="
                + "d_\\alpha^\\beta*1/3*(g^{\\mu\\nu}*g^{\\gamma\\delta}+ g^{\\mu\\gamma}*g^{\\nu\\delta}+ g^{\\mu\\delta}*g^{\\nu\\gamma})");
        Expression S = Tensors.parseExpression("S^\\mu\\nu\\rho\\alpha\\beta=0");
        Expression W = Tensors.parseExpression("W^{\\mu\\nu}_\\alpha^\\beta=0*W^{\\mu\\nu}_\\alpha^\\beta");
        Expression N = Tensors.parseExpression("N^\\rho\\alpha\\beta=0*N^\\rho\\alpha\\beta");
        Expression M = Tensors.parseExpression("M_\\alpha^\\beta = 0*M_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(4, KINV, K, S, W, N, M, F);
        System.out.println(input.getL());
        for (Expression[] exps : input.getHatQuantities())
            for (Expression e : exps)
                System.out.println(e);
        for (Expression e : input.getKnQuantities())
            System.out.println(e);


        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
        Tensor A = action.ACTION().get(1);

        A = RemoveDueToSymmetry.INSANCE.transform(A);
        System.out.println(A);
        //this is the exact K.V. result with corrections that 1/12*F_..*F^.. is not under tr operation and that tr of 1 is 4
        Tensor expected = Tensors.parse("1/30*Power[R, 2]+1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/15*R_{\\delta \\nu }*R^{\\delta \\nu }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/6*R*W^{\\beta }_{\\beta }");
        Assert.assertTrue(TensorUtils.compare(A, expected));

        
        TensorBuilder sb = SumBuilderFactory.defaultSumBuilder();
        for (Tensor s : A)
            if (s instanceof Product && s.getIndices().size() != 0) {
                Product p = (Product) s;
                System.out.println(p.getDataSubProduct() + ":  " + p.getIndexlessSubProduct());
            } else
                sb.put(s);
        System.out.println(sb.build());
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
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=0");
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F, OneLoopUtils.antiDeSitterBackround());

        OneLoopAction action = OneLoopAction.calculateOneLoopAction(input);
    }
}