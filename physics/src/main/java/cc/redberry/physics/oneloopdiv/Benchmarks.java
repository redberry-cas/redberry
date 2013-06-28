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
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.factor.FactorTransformation;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class contains several performance benchmarks of one-loop divergences
 * calculation. Here is the summary:
 * <p/>
 * <p/>
 * <pre>
 * Machine:
 *  Processor family: Intel(R) Core(TM) i5 CPU M 430  @ 2.27GHz.
 *  -Xmx value : 3g.
 *  Max memory used: 1.2g.
 *  Java version: 1.7.0_03 HotSpot 64-bit server VM
 *
 * Benchmark results:
 *  Minimal second order : 2 s.
 *  Minimal fourth order : 2 s.
 *  Vector field : 19 s.
 *  Gravity ghosts : 19 s.
 *  Squared vector field : 313 s.
 *  Lambda gauge gravity : 612 s.
 *  Spin 3 ghosts : 920 s.
 * </pre>
 * <pre>
 * Machine:
 *  Processor family: AMD Phenom(tm) II X6 1100T Processor
 *  -Xmx value : 3g
 *  Max memory used: 1.2g
 *  Java version: 1.7.0_04 HotSpot 64-bit server VM
 *
 * Benchmark results:
 *  Minimal second order : 1 s.
 *  Minimal fourth order : 1 s.
 *  Vector field : 14 s.
 *  Gravity ghosts : 14 s.
 *  Squared vector field : 219 s.
 *  Lambda gauge gravity : 521 s.
 *  Spin 3 ghosts : 627 s.
 * </pre>
 *
 * @author Stanislav Poslavsky
 */
public final class Benchmarks {

    private Benchmarks() {
    }

    private static final OutputStream dummyOutputStream = new OutputStream() {

        @Override
        public void write(int b) throws IOException {
        }
    };
    private static final PrintStream defaultOutputStream = System.out;

    private static void println(String str) {
        defaultOutputStream.println(str);
    }

    public static void main(String[] args) {
        //Processor family: Intel(R) Core(TM) i5 CPU M 430  @ 2.27GHz.
        //-Xmx value : 3g.
        //Max memory used: 1.2g.
        //Java version: 1.7.0_03 HotSpot 64-bit server VM
        //Benchmark results:
        //
        //Minimal second order : 2 s.
        //Minimal fourth order : 2 s.
        //Vector field : 19 s.
        //Gravity ghosts : 19 s.
        //Squared vector field : 313 s.
        //Lambda gauge gravity : 612 s.
        //Spin 3 ghosts : 920 s.

        //Processor family: AMD Phenom(tm) II X6 1100T Processor
        //-Xmx value : 3g
        //Max memory used: 1.2g
        //Java version: 1.7.0_04 HotSpot 64-bit server VM
        //Benchmark results:
        //
        //Minimal second order : 1 s.
        //Minimal fourth order : 1 s.
        //Vector field : 14 s.
        //Gravity ghosts : 14 s.
        //Squared vector field : 219 s.
        //Lambda gauge gravity : 521 s.
        //Spin 3 ghosts : 627 s.

        //suppressing output
        System.setOut(new PrintStream(dummyOutputStream));
        //burning JVM
        burnJVM();
        Timer timer = new Timer();
        timer.start();
        testMinimalSecondOrderOperator();
        println("Minimal second order : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
        testMinimalFourthOrderOperator();
        println("Minimal fourth order : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
        testVectorField();
        println("Vector field : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
        testGravityGhosts();
        println("Gravity ghosts : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
        testSquaredVectorField();
        println("Squared vector field : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
        testLambdaGaugeGravity();
        println("Lambda gauge gravity : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
        testSpin3Ghosts();
        println("Spin 3 ghosts : " + timer.elapsedTimeInSeconds() + " s.");
        timer.restart();
    }

    /**
     * Warm up the JVM.
     */
    public static void burnJVM() {
        testVectorField();
        for (int i = 0; i < 10; ++i)
            testMinimalFourthOrderOperator();
        println("JVM warmed up.");
    }

    static class Timer {

        private long start, stop;

        public Timer() {
        }

        void start() {
            start = System.currentTimeMillis();
        }

        long elapsedTime() {
            return System.currentTimeMillis() - start;
        }

        long elapsedTimeInSeconds() {
            return (System.currentTimeMillis() - start) / 1000;
        }

        void restart() {
            start();
        }
    }

    /**
     * This method calculates one-loop counterterms of the vector field in the
     * non-minimal gauge.
     */
    public static void testVectorField() {
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
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

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the squared vector field
     * in the non-minimal gauge.
     */
    public static void testSquaredVectorField() {
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
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


        Expression lambda = Tensors.parseExpression("\\lambda=gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("\\gamma=gamma");
        KINV = (Expression) gamma.transform(lambda.transform(KINV));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));
        M = (Expression) gamma.transform(lambda.transform(M));

        OneLoopInput input = new OneLoopInput(4, KINV, K, S, W, N, M, F);
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates ghosts contribution to the one-loop counterterms
     * of the gravitational field in the non-minimal gauge. The gauge fixing
     * term in LaTeX notation:
     * <pre>
     *  &nbsp;&nbsp;&nbsp;&nbsp; S_{gf} = -1/2 \int d^4 x \sqrt{-g} g_{\mu\nu} \chi^\mu \chi^\nu,
     *  where
     *  &nbsp;&nbsp;&nbsp;&nbsp; \chi^\mu = 1/\sqrt{1+\lambda} (g^{\mu\alpha} \nabla^\beta h_{\alpha\beta}-1/2 g^{\alpha\beta} \nabla^\mu h_{\alpha\beta})
     * </pre>
     */
    public static void testGravityGhosts() {
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
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

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates the main contribution to the one-loop counterterms
     * of the gravitational field in the non-minimal gauge. The gauge fixing
     * term in LaTeX notation:
     * <pre>
     *  &nbsp;&nbsp;&nbsp;&nbsp; S_{gf} = -1/2 \int d^4 x \sqrt{-g} g_{\mu\nu} \chi^\mu \chi^\nu,
     *  where
     *  &nbsp;&nbsp;&nbsp;&nbsp; \chi^\mu = 1/\sqrt{1+\lambda} (g^{\mu\alpha} \nabla^\beta h_{\alpha\beta}-1/2 g^{\alpha\beta} \nabla^\mu h_{\alpha\beta})
     * </pre>
     */
    public static void testLambdaGaugeGravity() {
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
        Tensors.addSymmetry("R_\\mu\\nu", 1, 0);
        Tensors.addAntiSymmetry("R_\\mu\\nu\\alpha\\beta", 1, 0, 2, 3);
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", 2, 3, 0, 1);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha\\beta^\\gamma\\delta = "
                + "(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2+"
                + "la/2*("
                + "d_\\alpha^\\gamma*n_\\beta*n^\\delta"
                + "+d_\\alpha^\\delta*n_\\beta*n^\\gamma"
                + "+d_\\beta^\\gamma*n_\\alpha*n^\\delta"
                + "+d_\\beta^\\delta*n_\\alpha*n^\\gamma)"
                + "-la*g^\\gamma\\delta*n_\\alpha*n_\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha\\beta^\\gamma\\delta = "
                + "g^\\mu\\nu*(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2"
                + "-la/(4*(1+la))*("
                + "d_\\alpha^\\gamma*d_\\beta^\\mu*g^\\delta\\nu"
                + "+d_\\alpha^\\gamma*d_\\beta^\\nu*g^\\delta\\mu"
                + "+d_\\alpha^\\delta*d_\\beta^\\mu*g^\\gamma\\nu"
                + "+d_\\alpha^\\delta*d_\\beta^\\nu*g^\\gamma\\mu"
                + "+d_\\beta^\\gamma*d_\\alpha^\\mu*g^\\delta\\nu"
                + "+d_\\beta^\\gamma*d_\\alpha^\\nu*g^\\delta\\mu"
                + "+d_\\beta^\\delta*d_\\alpha^\\mu*g^\\gamma\\nu"
                + "+d_\\beta^\\delta*d_\\alpha^\\nu*g^\\gamma\\mu)"
                + "+la/(2*(1+la))*g^\\gamma\\delta*(d_\\alpha^\\mu*d_\\beta^\\nu+d_\\alpha^\\nu*d_\\beta^\\mu)");
        Expression S = Tensors.parseExpression("S^\\rho_{\\alpha\\beta}^{\\gamma\\delta}=0");
        Expression W = Tensors.parseExpression("W_{\\alpha\\beta}^{\\gamma\\delta}=P_\\alpha\\beta^\\gamma\\delta"
                + "-la/(2*(1+la))*(R_\\alpha^\\gamma_\\beta^\\delta+R_\\alpha^\\delta_\\beta^\\gamma)"
                + "+la/(4*(1+la))*("
                + "d_\\alpha^\\gamma*R_\\beta^\\delta"
                + "+d_\\alpha^\\delta*R_\\beta^\\gamma"
                + "+d_\\beta^\\gamma*R_\\alpha^\\delta"
                + "+d_\\beta^\\delta*R_\\alpha^\\gamma)");
        Expression P = Tensors.parseExpression("P_\\gamma\\delta^\\mu\\nu = "
                + "R_\\gamma^\\mu_\\delta^\\nu+R_\\gamma^\\nu_\\delta^\\mu"
                + "+1/2*("
                + "d_\\gamma^\\mu*R_\\delta^\\nu"
                + "+d_\\gamma^\\nu*R_\\delta^\\mu"
                + "+d_\\delta^\\mu*R_\\gamma^\\nu"
                + "+d_\\delta^\\nu*R_\\gamma^\\mu)"
                + "-g^\\mu\\nu*R_\\gamma\\delta"
                + "-R^\\mu\\nu*g_\\gamma\\delta"
                + "+(-d_\\gamma^\\mu*d_\\delta^\\nu-d_\\gamma^\\nu*d_\\delta^\\mu+g^\\mu\\nu*g_\\gamma\\delta)*R/2");
        W = (Expression) P.transform(W);
        Expression F = Tensors.parseExpression("F_\\mu\\nu^\\lambda\\delta_\\rho\\tau = "
                + "R^\\lambda_\\rho\\mu\\nu*d^\\delta_\\tau+R^\\delta_\\tau\\mu\\nu*d^\\lambda_\\rho");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the second order minimal
     * operator.
     */
    public static void testMinimalSecondOrderOperator() {
        //TIME = 6.1 s
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);

        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha^\\beta=d_\\alpha^\\beta*g^{\\mu\\nu}");
        Expression S = Tensors.parseExpression("S^\\mu\\alpha\\beta=0");
        Expression W = Tensors.parseExpression("W_\\alpha^\\beta=W_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the second order minimal
     * operator in Barvinsky and Vilkovisky notation (Phys. Rep. 119 ( 1985)
     * 1-74 ).
     */
    public static void testMinimalSecondOrderOperatorBarvinskyVilkovisky() {
        //TIME = 4.5 s
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);

        //Phys. Rep. 119 ( 1985) 1-74 
        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha^\\beta=d_\\alpha^\\beta*g^{\\mu\\nu}");
        Expression S = Tensors.parseExpression("S^\\mu\\alpha\\beta=0");
        //here P^... from BV equal to W^...
        Expression W = Tensors.parseExpression("W_\\alpha^\\beta=W_\\alpha^\\beta-1/6*R*d_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the fourth order minimal
     * operator.
     */
    public static void testMinimalFourthOrderOperator() {
        //TIME = 6.2 s
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
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
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates ghosts contribution to the one-loop counterterms
     * of the theory with spin = 3.
     */
    public static void testSpin3Ghosts() {
        //TIME = 990 s
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
        Expression KINV = Tensors.parseExpression(
                "KINV^{\\alpha\\beta}_{\\mu\\nu} = P^{\\alpha\\beta}_{\\mu\\nu}-1/4*c*g_{\\mu\\nu}*g^{\\alpha\\beta}+"
                        + "(1/4)*b*(n_{\\mu}*n^{\\alpha}*d^{\\beta}_{\\nu}+n_{\\mu}*n^{\\beta}*d^{\\alpha}_{\\nu}+n_{\\nu}*n^{\\alpha}*d^{\\beta}_{\\mu}+n_{\\nu}*n^{\\beta}*d^{\\alpha}_{\\mu})+"
                        + "c*(n_{\\mu}*n_{\\nu}*g^{\\alpha\\beta}+n^{\\alpha}*n^{\\beta}*g_{\\mu\\nu})"
                        + "-c*b*n_{\\mu}*n_{\\nu}*n^{\\alpha}*n^{\\beta}");
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

        Expression consts[] = {
                Tensors.parseExpression("c=(1+2*beta)/(5+6*beta)"),
                Tensors.parseExpression("b=-(1+2*beta)/(1+beta)")
        };
        for (Expression cons : consts) {
            KINV = (Expression) cons.transform(KINV);
            K = (Expression) cons.transform(K);
        }

        Expression S = (Expression) Tensors.parse("S^\\rho^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression W = (Expression) Tensors.parse("W^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta\\gamma\\delta=0");

        Transformation[] ds = OneLoopUtils.antiDeSitterBackground();
        Transformation[] tr = new Transformation[ds.length + 1];
        System.arraycopy(ds, 0, tr, 0, ds.length);
        tr[tr.length - 1] = FactorTransformation.FACTOR;
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F, tr);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates the main contribution to the one-loop counterterms
     * of the gravitational field in general the non-minimal gauge. The gauge
     * fixing term in LaTeX notation:
     * <pre>
     *  &nbsp;&nbsp;&nbsp;&nbsp; S_{gf} = -1/2 \int d^4 x \sqrt{-g} g_{\mu\nu} \chi^\mu \chi^\nu,
     *  where
     *  &nbsp;&nbsp;&nbsp;&nbsp; \chi^\mu = 1/\sqrt{1+\lambda} (g^{\mu\alpha} \nabla^\beta h_{\alpha\beta}-(1+\beta)/2 g^{\alpha\beta} \nabla^\mu h_{\alpha\beta})
     * </pre>
     */
    public static void testNonMinimalGaugeGravity() {
        //FIXME works more than hour
        CC.setDefaultOutputFormat(OutputFormat.RedberryConsole);
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});


        Expression KINV = Tensors.parseExpression("KINV_\\alpha\\beta^\\gamma\\delta = "
                + "(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2-"
                + "la/2*("
                + "d_\\alpha^\\gamma*n_\\beta*n^\\delta"
                + "+d_\\alpha^\\delta*n_\\beta*n^\\gamma"
                + "+d_\\beta^\\gamma*n_\\alpha*n^\\delta"
                + "+d_\\beta^\\delta*n_\\alpha*n^\\gamma)"
                + "-ga*(g_\\alpha\\beta*n^\\gamma*n^\\delta+g^\\gamma\\delta*n_\\alpha*n_\\beta)"
                + "-1/2*g_\\alpha\\beta*g^\\gamma\\delta"
                + "+2*ga*(ga*la-2*ga+2*la)*n_\\alpha*n_\\beta*n^\\gamma*n^\\delta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha\\beta^\\gamma\\delta = "
                + "g^\\mu\\nu*(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\beta^\\gamma*d_\\alpha^\\delta)/2"
                + "-la/(4*(1+la))*("
                + "d_\\alpha^\\gamma*d_\\beta^\\mu*g^\\delta\\nu"
                + "+d_\\alpha^\\gamma*d_\\beta^\\nu*g^\\delta\\mu"
                + "+d_\\alpha^\\delta*d_\\beta^\\mu*g^\\gamma\\nu"
                + "+d_\\alpha^\\delta*d_\\beta^\\nu*g^\\gamma\\mu"
                + "+d_\\beta^\\gamma*d_\\alpha^\\mu*g^\\delta\\nu"
                + "+d_\\beta^\\gamma*d_\\alpha^\\nu*g^\\delta\\mu"
                + "+d_\\beta^\\delta*d_\\alpha^\\mu*g^\\gamma\\nu"
                + "+d_\\beta^\\delta*d_\\alpha^\\nu*g^\\gamma\\mu)"
                + "+(la-be)/(2*(1+la))*(g^\\gamma\\delta*(d_\\alpha^\\mu*d_\\beta^\\nu+d_\\alpha^\\nu*d_\\beta^\\mu)+g_\\alpha\\beta*(g^\\gamma\\mu*g^\\delta\\nu+g^\\gamma\\nu*g^\\delta\\mu))"
                + "+g^\\mu\\nu*g_\\alpha\\beta*g^\\gamma\\delta*(-1+(1+be)**2/(2*(1+la)))");
        K = (Expression) Tensors.parseExpression("be = ga/(1+ga)").transform(K);
        Expression S = Tensors.parseExpression("S^\\rho_{\\alpha\\beta}^{\\gamma\\delta}=0");
        Expression W = Tensors.parseExpression("W_{\\alpha\\beta}^{\\gamma\\delta}=P_\\alpha\\beta^\\gamma\\delta"
                + "-la/(2*(1+la))*(R_\\alpha^\\gamma_\\beta^\\delta+R_\\alpha^\\delta_\\beta^\\gamma)"
                + "+la/(4*(1+la))*("
                + "d_\\alpha^\\gamma*R_\\beta^\\delta"
                + "+d_\\alpha^\\delta*R_\\beta^\\gamma"
                + "+d_\\beta^\\gamma*R_\\alpha^\\delta"
                + "+d_\\beta^\\delta*R_\\alpha^\\gamma)");
        Expression P = Tensors.parseExpression("P_\\alpha\\beta^\\mu\\nu ="
                + "1/4*(d_\\alpha^\\gamma*d_\\beta^\\delta+d_\\alpha^\\delta*d_\\beta^\\gamma-g_\\alpha\\beta*g^\\gamma\\delta)"
                + "*(R_\\gamma^\\mu_\\delta^\\nu+R_\\gamma^\\nu_\\delta^\\mu-g^\\mu\\nu*R_\\gamma\\delta-g_\\gamma\\delta*R^\\mu\\nu"
                + "+1/2*(d^\\mu_\\gamma*R^\\nu_\\delta+d^\\nu_\\gamma*R_\\delta^\\mu+d^\\mu_\\delta*R^\\nu_\\gamma+d^\\nu_\\delta*R^\\mu_\\gamma)"
                + "-1/2*(d^\\mu_\\gamma*d^\\nu_\\delta+d^\\nu_\\gamma*d^\\mu_\\delta)*(R-2*LA)+1/2*g_\\gamma\\delta*g^\\mu\\nu*R)");
        P = (Expression) ExpandTransformation.expand(P,
                EliminateMetricsTransformation.ELIMINATE_METRICS,
                Tensors.parseExpression("R_{\\mu \\nu}^{\\mu}_{\\alpha} = R_{\\nu\\alpha}"),
                Tensors.parseExpression("R_{\\mu\\nu}^{\\alpha}_{\\alpha}=0"),
                Tensors.parseExpression("R_{\\mu}^{\\mu}= R"));
        W = (Expression) P.transform(W);
        Expression F = Tensors.parseExpression("F_\\mu\\nu^\\lambda\\delta_\\rho\\tau = "
                + "R^\\lambda_\\rho\\mu\\nu*d^\\delta_\\tau+R^\\delta_\\tau\\mu\\nu*d^\\lambda_\\rho");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }
}
