/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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

        OneLoopUtils.setUpRiemannSymmetries();
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
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b+c*n_a*n^b");
        Expression K = Tensors.parseExpression("K^{lm}_a^{b}=g^{lm}*d_{a}^{b}-k/2*(g^{lb}*d_a^m+g^{mb}*d_a^l)");
        Expression S = Tensors.parseExpression("S^p^l_m=0");
        Expression W = Tensors.parseExpression("W^{a}_{b}=P^{a}_{b}+(k/2)*R^a_b");
        Expression F = Tensors.parseExpression("F_lmab=R_lmab");


        Expression lambda = Tensors.parseExpression("k=gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("c=gamma");
        iK = (Expression) gamma.transform(lambda.transform(iK));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the squared vector field
     * in the non-minimal gauge.
     */
    public static void testSquaredVectorField() {
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b+(2*c+Power[c,2])*n_a*n^b");
        Expression K = Tensors.parseExpression("K^{lmcd}_a^{b}="
                + "d_a^b*1/3*(g^{lm}*g^{cd}+ g^{lc}*g^{md}+ g^{ld}*g^{mc})"
                + "+1/12*(-2*k+Power[k,2])*("
                + "g^{lm}*d_a^c*g^{bd}"
                + "+g^{lm}*d_a^d*g^{bc}"
                + "+g^{lc}*d_a^m*g^{bd}"
                + "+g^{lc}*d_a^d*g^{bm}"
                + "+g^{ld}*d_a^m*g^{bc}"
                + "+g^{ld}*d_a^c*g^{bm}"
                + "+g^{mc}*d_a^l*g^{bd}"
                + "+g^{mc}*d_a^d*g^{bl}"
                + "+g^{md}*d_a^l*g^{bc}"
                + "+g^{md}*d_a^c*g^{bl}"
                + "+g^{cd}*d_a^l*g^{bm}"
                + "+g^{cd}*d_a^m*g^{bl})");
        Expression S = Tensors.parseExpression("S^lmpab=0");
        //W^{l m }_{a }^{b } = d^{m }_{a }*R^{b l }+d^{l }_{a }*R^{b m }+g^{l b }*R_{a }^{m }+2*P_{a }^{b }*g^{l m }+-2/3*d_{a }^{b }*R^{l m }
        Expression W = Tensors.parseExpression("W^{lm}_a^b="
                + "2*P_{a}^{b}*g^{lm}-2/3*R^lm*d_a^b"
                + "-k/2*P_a^l*g^mb"
                + "-k/2*P_a^m*g^lb"
                + "-k/2*P^bl*d^m_a"
                + "-k/2*P^bm*d^l_a"
                + "+1/6*(k-2*Power[k,2])*("
                + "R_a^l*g^mb"
                + "+R_a^m*g^lb"
                + "+R^bl*d^m_a"
                + "+R^bm*d^l_a)"
                + "+1/6*(2*k-Power[k,2])*"
                + "(R_a^lbm+R_a^mbl)"
                + "+1/2*(2*k-Power[k,2])*g^lm*R_a^b");
        Expression N = Tensors.parseExpression("N^pab=0");
        Expression M = Tensors.parseExpression("M_a^b = "
                + "P_al*P^lb-1/2*R_lmca*R^lmcb"
                + "+k/2*P_al*R^lb"
                + "+k/2*P_lm*R^l_a^mb"
                + "+1/6*(k-2*Power[k,2])*R_al*R^lb"
                + "+1/12*(4*k+7*Power[k,2])*R_lam^b*R^lm"
                + "+1/4*(2*k-Power[k,2])*R_almc*R^clmb");
        Expression F = Tensors.parseExpression("F_lmab=R_lmab");


        Expression lambda = Tensors.parseExpression("k=gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("c=gamma");
        iK = (Expression) gamma.transform(lambda.transform(iK));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));
        M = (Expression) gamma.transform(lambda.transform(M));

        OneLoopInput input = new OneLoopInput(4, iK, K, S, W, N, M, F);
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
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b+gamma*n_a*n^b");
        Expression K = Tensors.parseExpression("K^{lm}_a^{b}=d_a^b*g^lm-1/2*beta*(d_a^l*g^mb+d_a^m*g^lb)");
        Expression S = Tensors.parseExpression("S^p^l_m=0");
        Expression W = Tensors.parseExpression("W^{a}_{b}=(1+beta/2)*R^a_b");
        Expression F = Tensors.parseExpression("F_lmab=R_lmab");


        Expression beta = Tensors.parseExpression("beta=gamma/(1+gamma)");
        iK = (Expression) beta.transform(iK);
        K = (Expression) beta.transform(K);
        S = (Expression) beta.transform(S);
        W = (Expression) beta.transform(W);

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

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
        Tensors.addSymmetry("R_lm", 1, 0);
        Tensors.addAntiSymmetry("R_lmab", 1, 0, 2, 3);
        Tensors.addSymmetry("R_lmab", 2, 3, 0, 1);

        Expression iK = Tensors.parseExpression("iK_ab^cd = "
                + "(d_a^c*d_b^d+d_b^c*d_a^d)/2+"
                + "la/2*("
                + "d_a^c*n_b*n^d"
                + "+d_a^d*n_b*n^c"
                + "+d_b^c*n_a*n^d"
                + "+d_b^d*n_a*n^c)"
                + "-la*g^cd*n_a*n_b");
        Expression K = Tensors.parseExpression("K^lm_ab^cd = "
                + "g^lm*(d_a^c*d_b^d+d_b^c*d_a^d)/2"
                + "-la/(4*(1+la))*("
                + "d_a^c*d_b^l*g^dm"
                + "+d_a^c*d_b^m*g^dl"
                + "+d_a^d*d_b^l*g^cm"
                + "+d_a^d*d_b^m*g^cl"
                + "+d_b^c*d_a^l*g^dm"
                + "+d_b^c*d_a^m*g^dl"
                + "+d_b^d*d_a^l*g^cm"
                + "+d_b^d*d_a^m*g^cl)"
                + "+la/(2*(1+la))*g^cd*(d_a^l*d_b^m+d_a^m*d_b^l)");
        Expression S = Tensors.parseExpression("S^p_{ab}^{cd}=0");
        Expression W = Tensors.parseExpression("W_{ab}^{cd}=P_ab^cd"
                + "-la/(2*(1+la))*(R_a^c_b^d+R_a^d_b^c)"
                + "+la/(4*(1+la))*("
                + "d_a^c*R_b^d"
                + "+d_a^d*R_b^c"
                + "+d_b^c*R_a^d"
                + "+d_b^d*R_a^c)");
        Expression P = Tensors.parseExpression("P_cd^lm = "
                + "R_c^l_d^m+R_c^m_d^l"
                + "+1/2*("
                + "d_c^l*R_d^m"
                + "+d_c^m*R_d^l"
                + "+d_d^l*R_c^m"
                + "+d_d^m*R_c^l)"
                + "-g^lm*R_cd"
                + "-R^lm*g_cd"
                + "+(-d_c^l*d_d^m-d_c^m*d_d^l+g^lm*g_cd)*R/2");
        W = (Expression) P.transform(W);
        Expression F = Tensors.parseExpression("F_lm^kd_pr = "
                + "R^k_plm*d^d_r+R^d_rlm*d^k_p");

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the second order minimal
     * operator.
     */
    public static void testMinimalSecondOrderOperator() {
        //TIME = 6.1 s

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b");
        Expression K = Tensors.parseExpression("K^lm_a^b=d_a^b*g^{lm}");
        Expression S = Tensors.parseExpression("S^lab=0");
        Expression W = Tensors.parseExpression("W_a^b=W_a^b");
        Expression F = Tensors.parseExpression("F_lmab=F_lmab");

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the second order minimal
     * operator in Barvinsky and Vilkovisky notation (Phys. Rep. 119 ( 1985)
     * 1-74 ).
     */
    public static void testMinimalSecondOrderOperatorBarvinskyVilkovisky() {
        //TIME = 4.5 s

        //Phys. Rep. 119 ( 1985) 1-74 
        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b");
        Expression K = Tensors.parseExpression("K^lm_a^b=d_a^b*g^{lm}");
        Expression S = Tensors.parseExpression("S^lab=0");
        //here P^... from BV equal to W^...
        Expression W = Tensors.parseExpression("W_a^b=W_a^b-1/6*R*d_a^b");
        Expression F = Tensors.parseExpression("F_lmab=F_lmab");

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates one-loop counterterms of the fourth order minimal
     * operator.
     */
    public static void testMinimalFourthOrderOperator() {
        //TIME = 6.2 s
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b");
        Expression K = Tensors.parseExpression("K^{lmcd}_a^{b}="
                + "d_a^b*1/3*(g^{lm}*g^{cd}+ g^{lc}*g^{md}+ g^{ld}*g^{mc})");
        Expression S = Tensors.parseExpression("S^lmpab=0");
        Expression W = Tensors.parseExpression("W^{lm}_a^b=0*W^{lm}_a^b");
        Expression N = Tensors.parseExpression("N^pab=0*N^pab");
        Expression M = Tensors.parseExpression("M_a^b = 0*M_a^b");
        Expression F = Tensors.parseExpression("F_lmab=F_lmab");

        OneLoopInput input = new OneLoopInput(4, iK, K, S, W, N, M, F);
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }

    /**
     * This method calculates ghosts contribution to the one-loop counterterms
     * of the theory with spin = 3.
     */
    public static void testSpin3Ghosts() {
        //TIME = 990 s
        Expression iK = Tensors.parseExpression(
                "iK^{ab}_{lm} = P^{ab}_{lm}-1/4*c*g_{lm}*g^{ab}+"
                        + "(1/4)*b*(n_{l}*n^{a}*d^{b}_{m}+n_{l}*n^{b}*d^{a}_{m}+n_{m}*n^{a}*d^{b}_{l}+n_{m}*n^{b}*d^{a}_{l})+"
                        + "c*(n_{l}*n_{m}*g^{ab}+n^{a}*n^{b}*g_{lm})"
                        + "-c*b*n_{l}*n_{m}*n^{a}*n^{b}");
        Expression K = Tensors.parseExpression(
                "K^{lm}^{ab}_{cd} = g^{lm}*P^{ab}_{cd}+"
                        + "(1+2*beta)*((1/4)*(d^{l}_{c}*g^{a m}*d^{b}_{d} + d^{l}_{d}*g^{a m}*d^{b}_{c}+d^{l}_{c}*g^{b m}*d^{a}_{d}+ d^{l}_{d}*g^{b m}*d^{a}_{c})+"
                        + "(1/4)*(d^{m}_{c}*g^{a l}*d^{b}_{d} + d^{m}_{d}*g^{a l}*d^{b}_{c}+d^{m}_{c}*g^{b l}*d^{a}_{d}+ d^{m}_{d}*g^{b l}*d^{a}_{c}) -"
                        + "(1/4)*(g_{cd}*g^{l a}*g^{m b}+g_{cd}*g^{l b}*g^{m a})-"
                        + "(1/4)*(g^{ab}*d^{l}_{c}*d^{m}_{d}+g^{ab}*d^{l}_{d}*d^{m}_{c})+(1/8)*g^{lm}*g_{cd}*g^{ab})");
        Expression P = Tensors.parseExpression(
                "P^{ab}_{lm} = (1/2)*(d^{a}_{l}*d^{b}_{m}+d^{a}_{m}*d^{b}_{l})-(1/4)*g_{lm}*g^{ab}");
        iK = (Expression) P.transform(iK);
        K = (Expression) P.transform(K);

        Expression consts[] = {
                Tensors.parseExpression("c=(1+2*beta)/(5+6*beta)"),
                Tensors.parseExpression("b=-(1+2*beta)/(1+beta)")
        };
        for (Expression cons : consts) {
            iK = (Expression) cons.transform(iK);
            K = (Expression) cons.transform(K);
        }

        Expression S = (Expression) Tensors.parse("S^p^{ab}_{lm}=0");
        Expression W = (Expression) Tensors.parse("W^{ab}_{lm}=0");
        Expression F = Tensors.parseExpression("F_lmabcd=0");

        Transformation[] ds = OneLoopUtils.antiDeSitterBackground();
        Transformation[] tr = new Transformation[ds.length + 1];
        System.arraycopy(ds, 0, tr, 0, ds.length);
        tr[tr.length - 1] = FactorTransformation.FACTOR;
        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F, tr);

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
        Tensors.addSymmetry("R_lm", IndexType.LatinLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_lmab", IndexType.LatinLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_lmab", IndexType.LatinLower, false, new int[]{2, 3, 0, 1});


        Expression iK = Tensors.parseExpression("iK_ab^cd = "
                + "(d_a^c*d_b^d+d_b^c*d_a^d)/2-"
                + "la/2*("
                + "d_a^c*n_b*n^d"
                + "+d_a^d*n_b*n^c"
                + "+d_b^c*n_a*n^d"
                + "+d_b^d*n_a*n^c)"
                + "-ga*(g_ab*n^c*n^d+g^cd*n_a*n_b)"
                + "-1/2*g_ab*g^cd"
                + "+2*ga*(ga*la-2*ga+2*la)*n_a*n_b*n^c*n^d");
        Expression K = Tensors.parseExpression("K^lm_ab^cd = "
                + "g^lm*(d_a^c*d_b^d+d_b^c*d_a^d)/2"
                + "-la/(4*(1+la))*("
                + "d_a^c*d_b^l*g^dm"
                + "+d_a^c*d_b^m*g^dl"
                + "+d_a^d*d_b^l*g^cm"
                + "+d_a^d*d_b^m*g^cl"
                + "+d_b^c*d_a^l*g^dm"
                + "+d_b^c*d_a^m*g^dl"
                + "+d_b^d*d_a^l*g^cm"
                + "+d_b^d*d_a^m*g^cl)"
                + "+(la-be)/(2*(1+la))*(g^cd*(d_a^l*d_b^m+d_a^m*d_b^l)+g_ab*(g^cl*g^dm+g^cm*g^dl))"
                + "+g^lm*g_ab*g^cd*(-1+(1+be)**2/(2*(1+la)))");
        K = (Expression) Tensors.parseExpression("be = ga/(1+ga)").transform(K);
        Expression S = Tensors.parseExpression("S^p_{ab}^{cd}=0");
        Expression W = Tensors.parseExpression("W_{ab}^{cd}=P_ab^cd"
                + "-la/(2*(1+la))*(R_a^c_b^d+R_a^d_b^c)"
                + "+la/(4*(1+la))*("
                + "d_a^c*R_b^d"
                + "+d_a^d*R_b^c"
                + "+d_b^c*R_a^d"
                + "+d_b^d*R_a^c)");
        Expression P = Tensors.parseExpression("P_ab^lm ="
                + "1/4*(d_a^c*d_b^d+d_a^d*d_b^c-g_ab*g^cd)"
                + "*(R_c^l_d^m+R_c^m_d^l-g^lm*R_cd-g_cd*R^lm"
                + "+1/2*(d^l_c*R^m_d+d^m_c*R_d^l+d^l_d*R^m_c+d^m_d*R^l_c)"
                + "-1/2*(d^l_c*d^m_d+d^m_c*d^l_d)*(R-2*LA)+1/2*g_cd*g^lm*R)");
        P = (Expression) ExpandTransformation.expand(P,
                EliminateMetricsTransformation.ELIMINATE_METRICS,
                Tensors.parseExpression("R_{l m}^{l}_{a} = R_{ma}"),
                Tensors.parseExpression("R_{lm}^{a}_{a}=0"),
                Tensors.parseExpression("R_{l}^{l}= R"));
        W = (Expression) P.transform(W);
        Expression F = Tensors.parseExpression("F_lm^kd_pr = "
                + "R^k_plm*d^d_r+R^d_rlm*d^k_p");

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
    }
}
