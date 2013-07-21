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

import cc.redberry.core.TAssert;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.tensor.Expression;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import cc.redberry.core.transformations.EliminateFromSymmetriesTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.factor.FactorTransformation;
import cc.redberry.core.transformations.fractions.TogetherTransformation;
import cc.redberry.core.utils.ArraysUtils;
import cc.redberry.core.utils.TensorUtils;
import junit.framework.Assert;
import org.junit.*;
import org.junit.rules.TestName;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class OneLoopCountertermsTest {
    private static enum TestComplexity {Easy, Medium, High, Extremal}

    private static final TestComplexity DEFAULT_COMPLEXITY = TestComplexity.Medium;

    private static final String[]
            EasyComplexityMethods,
            MediumComplexityMethods,
            HighComplexityMethods,
            ExtremalComplexityMethods;

    private static final String[]
            notHigherThenEasy,
            notHigherThenMedium,
            notHigherThenHigh,
            notHigherThenExtremal;
    private static final String[] allTests;

    static {
        EasyComplexityMethods
                = new String[]{
                "testMinimalSecondOrderOperator", "testMinimalSecondOrderOperatorBarvinskyVilkovisky",
                "testMinimalFourthOrderOperator", "testVectorField0", "testGravityGhosts0"};
        MediumComplexityMethods
                = new String[]{"testVectorField", "testGravityGhosts"};
        HighComplexityMethods
                = new String[]{"testSquaredVectorField", "testLambdaGaugeGravity", "testSpin3Ghosts"};
        ExtremalComplexityMethods
                = new String[]{"testNonMinimalGaugeGravity"};
        Arrays.sort(EasyComplexityMethods);
        Arrays.sort(MediumComplexityMethods);
        Arrays.sort(HighComplexityMethods);
        Arrays.sort(ExtremalComplexityMethods);
        notHigherThenEasy = EasyComplexityMethods;
        notHigherThenMedium = ArraysUtils.addAll(EasyComplexityMethods, MediumComplexityMethods);
        notHigherThenHigh = ArraysUtils.addAll(notHigherThenMedium, HighComplexityMethods);
        notHigherThenExtremal = ArraysUtils.addAll(notHigherThenHigh, ExtremalComplexityMethods);
        Arrays.sort(notHigherThenEasy);
        Arrays.sort(notHigherThenMedium);
        Arrays.sort(notHigherThenHigh);
        Arrays.sort(notHigherThenExtremal);
        allTests = notHigherThenExtremal;
    }

    private final TestComplexity complexity;

    public OneLoopCountertermsTest() {
        String complexity = System.getProperty("physics.testcomplexity");
        if (complexity == null) {
            this.complexity = DEFAULT_COMPLEXITY;
            return;
        }
        switch (complexity.toLowerCase()) {
            case "extremal":
                this.complexity = TestComplexity.Extremal;
                return;
            case "high":
                this.complexity = TestComplexity.High;
                return;
            case "medium":
                this.complexity = TestComplexity.Medium;
                return;
            case "easy":
                this.complexity = DEFAULT_COMPLEXITY;
            default:
                throw new RuntimeException("Unknown property value: -Dphysics.testcomplexity=" + complexity);
        }
    }

    private final boolean needToTest(String currentMethod) {
        if (!containsString(allTests, currentMethod))
            return true;
        switch (complexity) {
            case Extremal:
                return containsString(notHigherThenExtremal, currentMethod);
            case High:
                return containsString(notHigherThenHigh, currentMethod);
            case Medium:
                return containsString(notHigherThenMedium, currentMethod);
            case Easy:
            default:
                return containsString(notHigherThenEasy, currentMethod);
        }
    }

    private static boolean containsString(String[] strings, String string) {
        return Arrays.binarySearch(strings, string) >= 0;
    }

    @Rule
    public TestName name = new TestName();

    @Before
    public void beforeMethod() {
        Assume.assumeTrue(needToTest(name.getMethodName()));
    }


    @Test
    public void testMin_1() {
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("F_\\mu\\nu", IndexType.GreekLower, true, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});

        String FR_ =
                "FR= Power[L,2]*Power[(L-1),2]*(-2*n^\\alpha)*g^{\\beta\\gamma}*g^{\\mu\\nu}*n_\\sigma*((1/60)*R^\\sigma_{\\beta\\mu\\gamma}*F_{\\alpha\\nu}"
                        + "+(1/20)*R^\\sigma_{\\alpha\\mu\\gamma}*F_{\\nu\\beta}+(1/15)*R^\\sigma_{\\gamma\\mu\\alpha}*F_{\\nu\\beta}"
                        + "+(1/60)*R^\\sigma_{\\mu\\nu\\gamma}*F_{\\alpha\\beta})";
        Tensor FR = Tensors.parse(FR_);

        FR = Tensors.parseExpression("L = 2").transform(FR);
        FR = ExpandTransformation.expand(FR);
        FR = new Averaging(Tensors.parseSimple("n_\\mu")).transform(FR);
        FR = ExpandTransformation.expand(FR);
        FR = EliminateMetricsTransformation.eliminate(FR);
        Expression[] riemansSubstitutions = new Expression[]{
                Tensors.parseExpression("R_{\\mu \\nu}^{\\mu}_{\\alpha} = R_{\\nu\\alpha}"),
                Tensors.parseExpression("R_{\\mu\\nu}^{\\alpha}_{\\alpha}=0"),
                Tensors.parseExpression("F_{\\mu}^{\\mu}^{\\alpha}_{\\beta}=0"),
                Tensors.parseExpression("R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\alpha\\nu\\beta}=(1/2)*R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\nu\\alpha\\beta}"),
                Tensors.parseExpression("R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\nu\\alpha\\beta}=4*R_{\\mu\\nu}*R^{\\mu\\nu}-R*R"),
                Tensors.parseExpression("R_{\\mu}^{\\mu}= R"),
                Tensors.parseExpression("P_{\\mu}^{\\mu}= P")
        };
        for (Expression expression : riemansSubstitutions)
            FR = expression.transform(FR);
        System.out.println(FR);

    }

    /*
    * ***************************
    * ******** Easy tests *******
    * ***************************
    */

    @Test
    public void testMinimalSecondOrderOperator() {
        //TIME = 6.1 s
        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha^\\beta=d_\\alpha^\\beta*g^{\\mu\\nu}");
        Expression S = Tensors.parseExpression("S^\\mu\\alpha\\beta=0");
        Expression W = Tensors.parseExpression("W_\\alpha^\\beta=W_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);

        Tensor A = action.getCounterterms().get(1);
        A = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(A);

        //this is the exact K.V. result with corrections that 1/12*F_..*F^.. and oth are not under tr operation and that tr of 1 is 4
        Tensor expected = Tensors.parse("1/30*Power[R, 2]+1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/15*R_{\\delta \\nu }*R^{\\delta \\nu }+1/2*W^{\\alpha }_{\\rho_5 }*W^{\\rho_5 }_{\\alpha }+1/6*R*W^{\\beta }_{\\beta }");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testMinimalSecondOrderOperatorBarvinskyVilkovisky() {
        //TIME = 4.5 s
        //Phys. Rep. 119 ( 1985) 1-74
        Expression KINV = Tensors.parseExpression("KINV_\\alpha^\\beta=d_\\alpha^\\beta");
        Expression K = Tensors.parseExpression("K^\\mu\\nu_\\alpha^\\beta=d_\\alpha^\\beta*g^{\\mu\\nu}");
        Expression S = Tensors.parseExpression("S^\\mu\\alpha\\beta=0");
        //here P^... from BV equal to W^...
        Expression W = Tensors.parseExpression("W_\\alpha^\\beta=W_\\alpha^\\beta-1/6*R*d_\\alpha^\\beta");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta=F_\\mu\\nu\\alpha\\beta");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);
        A = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(A);
        //this is the exact Barvinsky and Vilkovisky
        Tensor expected = Tensors.parse("1/12*F_{\\nu \\beta }^{\\epsilon }_{\\rho_5 }*F^{\\nu \\beta \\rho_5 }_{\\epsilon }+1/2*W^{\\rho_5 }_{\\alpha }*W^{\\alpha }_{\\rho_5 }+-1/45*Power[R, 2]+1/15*R^{\\mu \\nu }*R_{\\mu \\nu }");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testMinimalFourthOrderOperator() {
        //TIME = 6.2 s
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
        Tensor A = action.getCounterterms().get(1);

        A = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(A);
        Tensor expected = Tensors.parse("44/135*R**2-32/135*R_\\mu\\nu*R^\\mu\\nu+2/3*F_\\mu\\nu\\alpha\\beta*F^\\mu\\nu\\beta\\alpha");
        TAssert.assertEquals(A, expected);
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
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);
        A = Tensors.parseExpression("P^\\mu_\\mu = P").transform(A);
        Tensor expected = Tensors.parse("7/60*Power[R, 2]-4/15*R^{\\mu \\nu }*R_{\\mu \\nu }+1/2*P^{\\gamma }_{\\alpha }*P^{\\alpha }_{\\gamma }+1/6*P*R");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testGravityGhosts0() {
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

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);

        Tensor expected = ExpandTransformation.expand(Tensors.parse("7/30*R^{\\mu \\nu }*R_{\\mu \\nu }+17/60*Power[R, 2]"));
        TAssert.assertEquals(ExpandTransformation.expand(A), expected);
    }

    /*
    * ****************************
    * ******* Medium tests *******
    * ****************************
    */

    @Test
    public void testVectorField() {
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
        Tensor A = action.getCounterterms().get(1);

        Tensor actual = Tensors.parseExpression("P^\\mu_\\mu = P").transform(A);

        Tensor expected = ExpandTransformation.expand(Tensors.parse("-5/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+47/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+1789/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+929/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-19/120*gamma*Power[gamma+1, -1]*Power[R, 2]+167/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+-5/72*R*Power[gamma+1, -1]*P*Power[gamma, 4]+-337/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+7/60*Power[R, 2]+-1/24*R*Power[gamma+1, -1]*P*Power[gamma, 2]+1/12*gamma*R*P+-109/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-37/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+1453/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+-1409/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/72*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 6]+(6851/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+11/20*Power[gamma+1, -1]*Power[gamma, 5]+-39/80*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-199/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-107/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+1333/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-23/120*Power[gamma, 4]+-49/60*Power[gamma, 2]+-67/120*Power[gamma, 3]+1259/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-133/144*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+11/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+31/64*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-41/60*gamma+29/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+3869/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+23/30*gamma*Power[gamma+1, -1]+811/360*Power[gamma+1, -1]*Power[gamma, 3]+329/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-4/15+-6631/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+97/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+1277/720*Power[gamma+1, -1]*Power[gamma, 2]+-2489/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1319/720*Power[gamma+1, -1]*Power[gamma, 4]+-2627/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/120*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-3253/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-965/576*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-9/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+17/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 10]+-1511/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-341/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+737/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-11/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3])*R^{\\mu \\nu }*R_{\\mu \\nu }+1/48*Power[P, 2]*Power[gamma, 2]+(-5/36*Power[gamma+1, -1]*Power[gamma, 4]+-7/12*Power[gamma+1, -1]*Power[gamma, 2]+-37/72*Power[gamma+1, -1]*Power[gamma, 3]+1/6*Power[gamma, 2]+1/18*Power[gamma, 3]+1/6*gamma+1/6*gamma*Power[gamma+1, -1]+73/72*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/9*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+2/3*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+11/24*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6])*P^{\\mu \\alpha }*R_{\\mu \\alpha }+1/6*R*P+-1391/1440*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+319/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-203/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/18*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+29/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+49/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+-271/480*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+29/120*gamma*Power[R, 2]+1/12*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+19/288*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+-1/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+1/20*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+9/80*Power[R, 2]*Power[gamma, 4]+17/40*Power[R, 2]*Power[gamma, 2]+2761/11520*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/12*R*P*Power[gamma, 2]+-37/120*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/36*R*P*Power[gamma, 3]+-497/1152*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+4669/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+83/240*Power[R, 2]*Power[gamma, 3]+-43/40*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+53/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+-403/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+(1/24*Power[gamma, 2]+1/4*gamma+1/2)*P^{\\alpha }_{\\rho_5 }*P^{\\rho_5 }_{\\alpha }+1/480*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+-13/144*R*Power[gamma+1, -1]*P*Power[gamma, 3]+-19/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 10]"));
        TAssert.assertEquals(ExpandTransformation.expand(actual), expected);


        //simplified result
        actual = FactorTransformation.factor(A);
        actual = Tensors.parseExpression("P^\\mu_\\mu = P").transform(actual);
        expected =
                Tensors.parse("1/24*(gamma**2+6*gamma+12)*P_\\mu\\nu*P^\\mu\\nu"
                        + "+1/48*gamma**2*P**2"
                        + "+gamma/12*(gamma+4)*R_\\mu\\nu*P^\\mu\\nu"
                        + "+1/24*(gamma**2+2*gamma+4)*R*P"
                        + "+1/120*(-32+10*gamma+5*gamma**2)*R_\\mu\\nu*R^\\mu\\nu"
                        + "+1/240*(28+20*gamma+5*gamma**2)*R**2");
        TAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGravityGhosts() {
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
        Tensor A = action.getCounterterms().get(1);

        //non simplified result
        Tensor expected = ExpandTransformation.expand(Tensors.parse("17/60*Power[R, 2]+1789/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-203/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+-337/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+61/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-109/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-497/480*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+-497/1152*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-97/160*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+-19/120*Power[1+gamma, -1]*Power[R, 2]*gamma+2441/11520*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+(17/320*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-67/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+71/60*gamma+7/120*Power[gamma, 4]+161/120*Power[gamma, 2]+233/360*Power[gamma, 3]+-1*(29/20*gamma+1/4*Power[gamma, 4]+23/20*Power[gamma, 3]+39/20*Power[gamma, 2]+83/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-43/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+3/5*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+667/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-121/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+99/160*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-7/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+7/48*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-13/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+-181/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-33/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-29/20*Power[1+gamma, -1]*gamma+-7/10*Power[1+gamma, -1]*Power[gamma, 5]+103/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-117/40*Power[1+gamma, -1]*Power[gamma, 2]+8/5+-173/40*Power[1+gamma, -1]*Power[gamma, 3]+-37/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-341/120*Power[1+gamma, -1]*Power[gamma, 4]+293/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+281/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-29/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+11/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+37/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-13/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-14/15*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-139/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+317/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3])+481/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1009/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+899/288*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+13/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-1231/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-31/60*Power[1+gamma, -1]*gamma+-3/20*Power[1+gamma, -1]*Power[gamma, 5]+35/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-4661/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+1/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+11/6+127/90*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+3509/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+3919/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+1877/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1/24*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+49/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+-827/720*Power[1+gamma, -1]*Power[gamma, 4]+-931/360*Power[1+gamma, -1]*Power[gamma, 3]+-1559/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+59/144*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+1441/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+5/64*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-1249/720*Power[1+gamma, -1]*Power[gamma, 2]+-1/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+731/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5])*R^{\\gamma }_{\\mu }*R^{\\mu }_{\\gamma }+1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+13/40*Power[R, 2]*gamma+-839/720*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+9/80*Power[R, 2]*Power[gamma, 4]+127/240*Power[R, 2]*Power[gamma, 2]+29/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+269/720*Power[R, 2]*Power[gamma, 3]+49/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+-37/120*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+3/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-403/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+167/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+5149/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+53/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+283/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-19/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 10]+-37/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+-1409/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+11/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+4679/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+319/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]"));
        TAssert.assertEquals(ExpandTransformation.expand(A), expected);

        //simplified result
        expected = Tensors.parse("(1/30)*(20*gamma+5*gamma**2+7)*R_{\\mu\\nu}*R^{\\mu\\nu}+(1/60)*(10*gamma+5*gamma**2+17)*R**2");
        TAssert.assertEquals(FactorTransformation.factor(A), expected);
    }

    /*
    * **************************************
    * ******* High complexity tests ********
    * **************************************
    */

    @Test
    public void testSquaredVectorField() {
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

        //not simplified result
        Tensor A = action.getCounterterms().get(1);
        Tensor actual = Tensors.parseExpression("P^\\mu_\\mu = P").transform(A);
        actual = ExpandTransformation.expand(TogetherTransformation.together(ExpandTransformation.expand(TogetherTransformation.together(actual))));
        Tensor expected = Tensors.parse("(8723/120)*(1+gamma)**(-12)*R**2*gamma**10+(2093/120)*(1+gamma)**(-12)*R**2*gamma**2+(689/30)*(1+gamma)**(-12)*R**2*gamma**11+(289/12)*(1+gamma)**(-12)*R*gamma**2*P+(7/30)*(1+gamma)**(-12)*R**2+(1859/5)*(1+gamma)**(-12)*R**2*gamma**7+(1859/12)*(1+gamma)**(-12)*R**2*gamma**4+33*(1+gamma)**(-12)*P**2*gamma**7+33*(1+gamma)**(-12)*P**2*gamma**9+286*(1+gamma)**(-12)*R**2*gamma**8+((1+gamma)**(-12)+(1/12)*(1+gamma)**(-12)*gamma**14+(3/2)*(1+gamma)**(-12)*gamma**13+(25/2)*(1+gamma)**(-12)*gamma**12+(25/2)*(1+gamma)**(-12)*gamma+(190/3)*(1+gamma)**(-12)*gamma**11+254*(1+gamma)**(-12)*gamma**3+(5445/4)*(1+gamma)**(-12)*gamma**6+(865/12)*(1+gamma)**(-12)*gamma**2+(869/4)*(1+gamma)**(-12)*gamma**10+968*(1+gamma)**(-12)*gamma**8+(1067/2)*(1+gamma)**(-12)*gamma**9+(6347/6)*(1+gamma)**(-12)*gamma**5+(1221/2)*(1+gamma)**(-12)*gamma**4+1320*(1+gamma)**(-12)*gamma**7)*P^{\\mu}_{\\rho_{5}}*P^{\\rho_{5}}_{\\mu}+(377/6)*(1+gamma)**(-12)*R**2*gamma**3+(100/3)*(1+gamma)**(-12)*R*gamma**11*P+(299/60)*(1+gamma)**(-12)*R**2*gamma**12+(1199/12)*(1+gamma)**(-12)*R*gamma**10*P+(14729/40)*(1+gamma)**(-12)*R**2*gamma**6+(1/12)*(1+gamma)**(-12)*R*gamma**14*P+(55/6)*(1+gamma)**(-12)*P**2*gamma**5+(55/6)*(1+gamma)**(-12)*P**2*gamma**11+(2189/6)*(1+gamma)**(-12)*R*gamma**5*P+(47/6)*(1+gamma)**(-12)*R*gamma**12*P+(-(9647/30)*(1+gamma)**(-12)*gamma**5-(1987/60)*(1+gamma)**(-12)*gamma**2+(2563/60)*(1+gamma)**(-12)*gamma**10+(1/12)*(1+gamma)**(-12)*gamma**14+(7/6)*(1+gamma)**(-12)*gamma**13-55*(1+gamma)**(-12)*gamma**8+(187/6)*(1+gamma)**(-12)*gamma**9+(209/30)*(1+gamma)**(-12)*gamma**12-(187/30)*(1+gamma)**(-12)*gamma-(316/3)*(1+gamma)**(-12)*gamma**3+(344/15)*(1+gamma)**(-12)*gamma**11-(8/15)*(1+gamma)**(-12)-(1012/5)*(1+gamma)**(-12)*gamma**7-(6391/20)*(1+gamma)**(-12)*gamma**6-(1331/6)*(1+gamma)**(-12)*gamma**4)*R_{\\mu\\nu}*R^{\\mu\\nu}+(25/6)*(1+gamma)**(-12)*R*gamma*P+(7/6)*(1+gamma)**(-12)*R*gamma**13*P+(256/3)*(1+gamma)**(-12)*R*gamma**3*P+(1331/6)*(1+gamma)**(-12)*R*gamma**9*P+(77/2)*(1+gamma)**(-12)*P**2*gamma**8+((1/6)*(1+gamma)**(-12)*gamma**14+(8/3)*(1+gamma)**(-12)*gamma**13+19*(1+gamma)**(-12)*gamma**12+46*(1+gamma)**(-12)*gamma**3+(49/6)*(1+gamma)**(-12)*gamma**2+(2/3)*(1+gamma)**(-12)*gamma+(242/3)*(1+gamma)**(-12)*gamma**11+462*(1+gamma)**(-12)*gamma**9+(473/3)*(1+gamma)**(-12)*gamma**4+682*(1+gamma)**(-12)*gamma**8+748*(1+gamma)**(-12)*gamma**7+(1100/3)*(1+gamma)**(-12)*gamma**5+(1221/2)*(1+gamma)**(-12)*gamma**6+(1375/6)*(1+gamma)**(-12)*gamma**10)*P^{\\rho_{5}}_{\\alpha}*R_{\\rho_{5}}^{\\alpha}+(1/24)*(1+gamma)**(-12)*R**2*gamma**14+(2/3)*(1+gamma)**(-12)*R**2*gamma**13+(4147/15)*(1+gamma)**(-12)*R**2*gamma**5+(165/8)*(1+gamma)**(-12)*P**2*gamma**6+(165/8)*(1+gamma)**(-12)*P**2*gamma**10+(1925/4)*(1+gamma)**(-12)*R*gamma**6*P+(1243/6)*(1+gamma)**(-12)*R*gamma**4*P+374*(1+gamma)**(-12)*R*gamma**8*P+(1/2)*(1+gamma)**(-12)*P**2*gamma**3+(1/2)*(1+gamma)**(-12)*P**2*gamma**13+(1/24)*(1+gamma)**(-12)*P**2*gamma**2+(1/24)*(1+gamma)**(-12)*P**2*gamma**14+(1001/6)*(1+gamma)**(-12)*R**2*gamma**9+(1/3)*(1+gamma)**(-12)*R*P+(89/30)*(1+gamma)**(-12)*R**2*gamma+(11/4)*(1+gamma)**(-12)*P**2*gamma**4+(11/4)*(1+gamma)**(-12)*P**2*gamma**12+484*(1+gamma)**(-12)*R*gamma**7*P");

        TAssert.assertEquals(actual, expected);

        //simplified result
        actual = FactorTransformation.factor(A);
        actual = Tensors.parseExpression("P^\\mu_\\mu = P").transform(actual);

        expected =
                Tensors.parse("1/12*(gamma**2+6*gamma+12)*P_\\mu\\nu*P^\\mu\\nu"
                        + "+1/24*gamma**2*P**2"
                        + "+gamma/6*(gamma+4)*R_\\mu\\nu*P^\\mu\\nu"
                        + "+1/12*(gamma**2+2*gamma+4)*R*P"
                        + "+1/60*(-32+10*gamma+5*gamma**2)*R_\\mu\\nu*R^\\mu\\nu"
                        + "+1/120*(28+20*gamma+5*gamma**2)*R**2");
        TAssert.assertEquals(actual, expected);
    }


    @Test
    public void testLambdaGaugeGravity() {

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
        Tensor A = action.getCounterterms().get(1);

        A = ExpandTransformation.expand(TogetherTransformation.together(ExpandTransformation.expand(A)));
        //TODO simplify result
        //non simplified result
        Tensor expected = Tensors.parse("((7/6)*(1+la)**(-6)+(23/3)*la*(1+la)**(-6)+(2/3)*la**8*(1+la)**(-6)+(14/3)*la**7*(1+la)**(-6)+(91/3)*la**5*(1+la)**(-6)+(91/6)*la**6*(1+la)**(-6)+(112/3)*la**3*(1+la)**(-6)+(133/6)*la**2*(1+la)**(-6)+(245/6)*la**4*(1+la)**(-6))*R_{\\mu\\nu}*R^{\\mu\\nu}+(1/3)*la**8*R**2*(1+la)**(-6)+2*la**7*R**2*(1+la)**(-6)+(7/2)*R**2*la*(1+la)**(-6)+(109/12)*la**2*R**2*(1+la)**(-6)+(7/12)*R**2*(1+la)**(-6)+(41/3)*la**3*R**2*(1+la)**(-6)+(55/4)*la**4*R**2*(1+la)**(-6)+(61/6)*la**5*R**2*(1+la)**(-6)+(67/12)*la**6*R**2*(1+la)**(-6)");
        Assert.assertTrue(TensorUtils.equals(A, expected));

        //simplified result
        //Tensor expected = Tensors.parse("1/6*(4*la**2+4*la+7)*R_\\mu\\nu*R^\\mu\\nu+1/12*(4*la**2+7)*R**2");
    }

    @Test
    public void testSpin3Ghosts() {
        //TIME = 990 s
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
        //        for (Expression cons : consts) {
        //            KINV = (Expression) cons.transform(KINV);
        //            K = (Expression) cons.transform(K);
        //        }
        Expression S = (Expression) Tensors.parse("S^\\rho^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression W = (Expression) Tensors.parse("W^{\\alpha\\beta}_{\\mu\\nu}=0");
        Expression F = Tensors.parseExpression("F_\\mu\\nu\\alpha\\beta\\gamma\\delta=0");

        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F, OneLoopUtils.antiDeSitterBackground());

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);
        A = ExpandTransformation.expand(TogetherTransformation.together(A));
        Tensor expected = Tensors.parse("55585/96*beta**3*b**3*LAMBDA**2-169/1440*beta**3*b**6*LAMBDA**2+1553833/2880*beta**2*b**3*LAMBDA**2-185763/640*beta**2*c**3*b**4*LAMBDA**2+154689/2560*beta**2*c**4*b**4*LAMBDA**2-17/180*beta*c*b**6*LAMBDA**2+2297/128*beta**4*c**4*b**5*LAMBDA**2-371/45*beta*c*b**5*LAMBDA**2+1441129/5760*beta*c**2*b**4*LAMBDA**2-20099/128*beta*c**3*b**4*LAMBDA**2-659/80*beta**4*c**3*b**6*LAMBDA**2+1837/30*beta**6*c**2*b**4*LAMBDA**2+60169/240*beta**5*c**2*b**4*LAMBDA**2+97939/640*beta**3*b**4*LAMBDA**2+709/36*beta**6*b**4*LAMBDA**2+220697/360*beta*b*LAMBDA**2-6973/120*c*beta**3*b**5*LAMBDA**2+40247/90*beta**2*LAMBDA**2+65/256*beta*c**4*b**6*LAMBDA**2-47/768*c**3*b**6*LAMBDA**2+103/72*beta**6*c**2*b**6*LAMBDA**2-221813/180*c*beta**3*b**2*LAMBDA**2-2055559/5760*c*beta**2*b**4*LAMBDA**2+9487/45*beta**4*LAMBDA**2-22117/72*beta*c*b*LAMBDA**2-10841/90*c*beta**6*b**3*LAMBDA**2+24097/92160*b**5*LAMBDA**2-239/11520*beta*b**6*LAMBDA**2+35123/720*LAMBDA**2-23071/640*beta**2*c**3*b**5*LAMBDA**2-45563/720*c*b*LAMBDA**2+2909/5760*beta*c**2*b**6*LAMBDA**2-1627/30*c*beta**6*b**4*LAMBDA**2+28622/45*beta**4*b*LAMBDA**2-1829/40*beta**5*c**3*b**5*LAMBDA**2-227/180*c*beta**3*b**6*LAMBDA**2+170959/480*beta*c**2*b**2*LAMBDA**2+10313/80*b*LAMBDA**2+178177/320*beta**3*c**2*b**4*LAMBDA**2-229/2880*beta**4*b**6*LAMBDA**2-1688857/46080*c*b**4*LAMBDA**2+2999503/2880*beta**2*c**2*b**3*LAMBDA**2-10987/11520*c*b**5*LAMBDA**2-2271/20*beta**5*c**3*b**4*LAMBDA**2-71477/384*beta*c**3*b**3*LAMBDA**2+69137/46080*c**2*b**5*LAMBDA**2-22625/128*beta*c*b**4*LAMBDA**2+95291/1440*beta**5*c**2*b**5*LAMBDA**2-2687/960*beta**2*c**3*b**6*LAMBDA**2+277117/360*beta**4*b**2*LAMBDA**2+2144/45*beta**6*LAMBDA**2+9/4*beta**6*c**4*b**4*LAMBDA**2+9/4*beta**6*c**4*b**5*LAMBDA**2-829/11520*beta**2*b**6*LAMBDA**2+35987/1280*beta**4*c**4*b**4*LAMBDA**2+56911/960*beta*b**4*LAMBDA**2-193/30*beta**3*c**3*b**6*LAMBDA**2-107/46080*b**6*LAMBDA**2-88805/576*c*b**2*LAMBDA**2-43871/90*c*beta**3*b*LAMBDA**2-6233/80*beta**4*c**3*b**5*LAMBDA**2-103/160*beta*c**3*b**6*LAMBDA**2+105127/960*beta**4*c**2*b**5*LAMBDA**2-83/180*c*beta**6*b**6*LAMBDA**2-2504/15*c*beta**5*b*LAMBDA**2-104795/3072*c**3*b**4*LAMBDA**2-1077641/2880*c*beta**4*b**4*LAMBDA**2+33/16*beta**5*c**4*b**6*LAMBDA**2-199/10*beta**6*c**3*b**3*LAMBDA**2+6049/360*beta**6*c**2*b**5*LAMBDA**2+48407/640*c**2*b**2*LAMBDA**2+9/16*beta**6*c**4*b**6*LAMBDA**2-186*beta**4*c**3*b**3*LAMBDA**2+1627247/30720*c**2*b**4*LAMBDA**2+2014/45*beta**6*c**2*b**2*LAMBDA**2-447/40*beta**6*c**3*b**5*LAMBDA**2-23369/360*c*beta**4*b**5*LAMBDA**2+2567/1024*beta*c**4*b**5*LAMBDA**2+10941/640*beta**3*c**4*b**5*LAMBDA**2+751939/5760*b**2*LAMBDA**2-13/9*c*beta**5*b**6*LAMBDA**2+4597/128*beta*c**4*b**4*LAMBDA**2+102257/7680*beta*c**2*b**5*LAMBDA**2-1339/720*c*beta**4*b**6*LAMBDA**2-3557/90*c*beta**5*b**5*LAMBDA**2+1781/360*beta**5*c**2*b**6*LAMBDA**2+240619/3840*b**3*LAMBDA**2-20714/45*c*beta**5*b**2*LAMBDA**2-21166/45*c*beta**5*b**3*LAMBDA**2-145183/480*beta**3*c**3*b**4*LAMBDA**2+16381/1152*beta**3*b**5*LAMBDA**2-1369/2880*c*beta**2*b**6*LAMBDA**2+1956/5*beta**5*b*LAMBDA**2-1249/128*beta*c**3*b**5*LAMBDA**2+88553/5760*beta**4*b**5*LAMBDA**2+8017/90*beta**6*c**2*b**3*LAMBDA**2-261/10*beta**6*c**3*b**4*LAMBDA**2-5476/45*c*beta**6*b**2*LAMBDA**2+28987/30*beta**3*b*LAMBDA**2-143707/480*beta**3*c**3*b**3*LAMBDA**2+3847/720*beta**3*c**2*b**6*LAMBDA**2-621281/1440*c*beta**3*b**4*LAMBDA**2+5726/45*beta**6*b**2*LAMBDA**2-167/30*beta**5*c**3*b**6*LAMBDA**2+3986/15*beta**5*b**3*LAMBDA**2+1851/160*beta**5*c**4*b**4*LAMBDA**2+129737/360*beta**4*c**2*b**2*LAMBDA**2+100487/46080*beta*b**5*LAMBDA**2-20919/64*beta**2*c**3*b**3*LAMBDA**2-13442/45*c*beta**4*b*LAMBDA**2-1467893/11520*c*b**3*LAMBDA**2-31/20*beta**6*c**3*b**6*LAMBDA**2+6599/128*beta**3*c**4*b**4*LAMBDA**2-71069/320*beta**4*c**3*b**4*LAMBDA**2+16547/45*beta**5*c**2*b**3*LAMBDA**2-232/5*c*beta**6*b*LAMBDA**2-5659/80*beta**3*c**3*b**5*LAMBDA**2+79/32*beta**3*c**4*b**6*LAMBDA**2-1331/15*beta**5*c**3*b**3*LAMBDA**2+112063/1440*beta**5*b**4*LAMBDA**2-149279/180*c*beta**4*b**2*LAMBDA**2+33335/4096*c**4*b**4*LAMBDA**2-35/32*c**3*b**5*LAMBDA**2+151133/12288*b**4*LAMBDA**2+279/256*beta**2*c**4*b**6*LAMBDA**2-3435461/5760*beta*c*b**3*LAMBDA**2-1621111/1440*c*beta**3*b**3*LAMBDA**2+96849/160*beta*b**2*LAMBDA**2+1/720*beta**5*b**6*LAMBDA**2+938203/7680*beta**2*b**4*LAMBDA**2+6008/45*beta**5*LAMBDA**2+1434631/11520*c**2*b**3*LAMBDA**2-89/11520*c*b**6*LAMBDA**2-236093/180*c*beta**2*b**2*LAMBDA**2+3323219/5760*beta*c**2*b**3*LAMBDA**2+379547/360*beta**3*b**2*LAMBDA**2-737/72*c*beta**6*b**5*LAMBDA**2+3171/320*beta**5*c**4*b**5*LAMBDA**2+1079/23040*c**2*b**6*LAMBDA**2+126683/180*beta**4*c**2*b**3*LAMBDA**2+1816/15*beta**6*b*LAMBDA**2+280889/2880*beta**3*c**2*b**5*LAMBDA**2-171649/5760*c*beta**2*b**5*LAMBDA**2+10177/1440*beta**4*c**2*b**6*LAMBDA**2+29107/3840*beta**2*b**5*LAMBDA**2-525161/720*beta*c*b**2*LAMBDA**2+16556/45*beta**3*LAMBDA**2+20506/45*beta**5*b**2*LAMBDA**2-633377/576*c*beta**2*b**3*LAMBDA**2+70903/1440*beta**2*c**2*b**5*LAMBDA**2+199/64*beta**4*c**4*b**6*LAMBDA**2+6247/90*beta**6*b**3*LAMBDA**2+313015/2304*beta**4*b**4*LAMBDA**2+488123/480*beta**3*c**2*b**3*LAMBDA**2+13/720*beta**6*b**6*LAMBDA**2-75797/90*c*beta**4*b**3*LAMBDA**2+64913/60*beta**2*b*LAMBDA**2+69409/120*beta**3*c**2*b**2*LAMBDA**2+25/1024*c**4*b**6*LAMBDA**2+22489/90*beta*LAMBDA**2-2201/4*c*beta**2*b*LAMBDA**2+8797/960*beta**5*b**5*LAMBDA**2+1667909/5760*beta*b**3*LAMBDA**2+12997/5760*beta**2*c**2*b**6*LAMBDA**2+283/120*beta**6*b**5*LAMBDA**2+11559/1280*beta**2*c**4*b**5*LAMBDA**2+8228/45*beta**5*c**2*b**2*LAMBDA**2+228367/360*beta**2*c**2*b**2*LAMBDA**2-10415/256*c**3*b**3*LAMBDA**2+1870429/3840*beta**2*c**2*b**4*LAMBDA**2+585/2048*c**4*b**5*LAMBDA**2+390509/360*beta**2*b**2*LAMBDA**2-6427/30*c*beta**5*b**4*LAMBDA**2+16621/36*beta**4*b**3*LAMBDA**2+2624573/5760*beta**4*c**2*b**4*LAMBDA**2");
        Assert.assertTrue(TensorUtils.equals(A, expected));
    }

    /*
    * *****************************************
    * ******* Extremal complexity tests *******
    * *****************************************
    */

    @Test
    public void testNonMinimalGaugeGravity() {
        //FIXME works more than hour
        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});


        Expression KINV = Tensors.parseExpression("KINV_\\alpha\\beta^\\mu\\nu = "
                + "(d_\\alpha^\\mu*d_\\beta^\\nu+d_\\beta^\\mu*d_\\alpha^\\nu)/2"
                + "-la/2*("
                + "d_\\alpha^\\mu*n_\\beta*n^\\nu"
                + "+d_\\alpha^\\nu*n_\\beta*n^\\mu"
                + "+d_\\beta^\\mu*n_\\alpha*n^\\nu"
                + "+d_\\beta^\\nu*n_\\alpha*n^\\mu)"
                + "-ga*(g_\\alpha\\beta*n^\\mu*n^\\nu+g^\\mu\\nu*n_\\alpha*n_\\beta)"
                + "-1/2*g_\\alpha\\beta*g^\\mu\\nu"
                + "+2*ga*(ga*la-2*ga+2*la)*n_\\alpha*n_\\beta*n^\\mu*n^\\nu");
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
                + "+(la-be)/(2*(1+la))*("
                + "g^\\gamma\\delta*(d_\\alpha^\\mu*d_\\beta^\\nu+d_\\alpha^\\nu*d_\\beta^\\mu)"
                + "+g_\\alpha\\beta*(g^\\gamma\\mu*g^\\delta\\nu+g^\\gamma\\nu*g^\\delta\\mu))"
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
        //        Expression F = Tensors.parseExpression("F_\\mu\\nu^\\lambda\\delta_\\rho\\tau = "
        //                + "R^\\lambda_\\rho\\mu\\nu*d^\\delta_\\tau+R^\\delta_\\tau\\mu\\nu*d^\\lambda_\\rho");

        //todo together symbolic
        OneLoopInput input = new OneLoopInput(2, KINV, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);


        //TODO simplify result
        //non simplified result
        //        Tensor expected = Tensors.parse("-43/960*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+31751/2880*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)-161/960*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3311/1920*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3833/5760*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-281/60*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-59/12*R**2*la**2*(1+la)**(-1)+34979/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)-7651/1440*R**2*la**4*(1+la)**(-1)+1627/2880*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+(7/45*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-107/30*la+1631/720*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-6841/160*la**4*(1+la)**(-1)*(1+la)**(-1)-4619/5760*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+101/96*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3211/360*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+1729/80*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-18517/960*la**5*(1+la)**(-1)*(1+la)**(-1)-3697/2880*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-179/720*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3109/5760*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+953/1440*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2533/720*la**6*(1+la)**(-1)*(1+la)**(-1)+79/30*la**5*(1+la)**(-1)-2551/2880*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+127/30*la*(1+la)**(-1)+7/6+10387/1152*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-25/48*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-5477/2880*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2825/72*la**3*(1+la)**(-1)*(1+la)**(-1)+881/36*la**2*(1+la)**(-1)-95/9*la**2*(1+la)**(-1)*(1+la)**(-1)+6197/180*la**3*(1+la)**(-1)-301/480*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-23/30*la**4-299/60*la**3-541/60*la**2+1067/1440*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4003/240*la**4*(1+la)**(-1)-803/1440*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+281/1440*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+155/8*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-571/240*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1))*R^{\\mu \\nu }*R_{\\mu \\nu }-3223/360*R**2*la**3*(1+la)**(-1)-667/360*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-1109/288*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-91/60*R**2*la**5*(1+la)**(-1)-1/30*R**2*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+9/20*R**2*la**4-7349/11520*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+157/60*R**2*la**2+181/120*R**2*la**3+103/320*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-13/10*R**2*la*(1+la)**(-1)+859/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+7/12*R**2-20419/11520*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3181/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-533/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-15/64*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+601/72*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)+13/10*R**2*la+25/96*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-4955/2304*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+919/480*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)-139/960*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+17/480*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4/3*R**2*la**2*(1+la)**(-1)*(1+la)**(-1)");
        //        Assert.assertTrue(TensorUtils.equals(A, expected));

        //simplified result
        //Tensor expected = Tensors.parse("1/12*(c1*R_\\mu\\nu*R^\\mu\\nu+c2*R**2+c3*R*LA+c4*LA**2)");
        //where
        //Expression c1 = Tensors.parseExpression("c1 = la**2*(2*ga**4+8*ga**3+12*ga**2+8*ga+8)+la*(-8*ga**4-16*ga**3-4*ga**2+8*ga+8)+(8*ga**4-8*ga**2+14)");
        //Expression c2 = Tensors.parseExpression("c2 = la**2*(ga**4+4*ga**3+6*ga**2+4*ga+4)+la*(-4*ga**4-8*ga**3-6*ga**2-4*ga)+(4*ga**4+4*ga**2+7)");
        //Expression c3 = Tensors.parseExpression("c3 = la**2*(-12*ga**4-48*ga**3-72*ga**2-48*ga-48)+la*(48*ga**4+72*ga**3-8*ga**2-40*ga-56)+(-48*ga**4+48*ga**3+40*ga**2+8*ga-104)");
        //Expression c4 = Tensors.parseExpression("c4 = la**2*(23*ga**4+96*ga**3+144*ga**2+96*ga+96)+la*(-96*ga**4-96*ga**3+144*ga**2+192*ga+192)+(96*ga**4-192*ga**3-144*ga**2+96*ga+240)");
        //expected = c1.transform(expected);
        //expected = c2.transform(expected);
        //expected = c3.transform(expected);
        //expected = c4.transform(expected);
    }

    /*
    * ************************
    * ******* Asorted *******
    * ************************
    */


    private static String reduce2Redberry(String exrpession) {
        exrpession = exrpession.replace('&', '*');
        Pattern pattern = Pattern.compile("(R|n|hk|d)\\(([a-zA-Z0-9,]*)\\)");
        Matcher matcher = pattern.matcher(exrpession);
        StringBuffer sb = new StringBuffer();
        String group, tensorName, indices;

        while (matcher.find()) {
            group = matcher.group();
            tensorName = matcher.group(1);
            indices = matcher.group(2);
            if (tensorName.equals("R")) {
                String[] indicesArray = indices.split(",");
                assert indicesArray.length == 2 || indicesArray.length == 4;
                if (indicesArray.length == 4) {
                    indices = "^{" + indicesArray[0] + "}_{";
                    for (int i = 1; i < 4; ++i)
                        indices += indicesArray[i] + " ";
                    indices += "}";
                } else
                    indices = "_{" + indices.replace(',', ' ') + "}";
            } else if (tensorName.equals("n"))
                indices = "_{" + indices.replace(',', ' ') + "}";
            else
                indices = "^{" + indices.replace(',', ' ') + "}";
            group = tensorName + indices;
            group = group.replace("al", "\\\\alpha");
            group = group.replace("be", "\\\\beta");
            group = group.replace("gm", "\\\\gamma");
            group = group.replace("de", "\\\\delta");
            group = group.replace("s", "\\\\sigma");
            group = group.replace("ro", "\\\\rho");
            group = group.replace("mu", "\\\\mu");
            group = group.replace("nu", "\\\\nu");
            group = group.replace("j1", "");
            group = group.replace("j2", "");
            group = group.replace("j3", "");
            group = group.replace("j4", "");
            group = group.replace("j5", "");
            group = group.replace("j6", "");
            group = group.replace("j7", "");
            group = group.replace("j8", "");

            matcher.appendReplacement(sb, group);
        }
        matcher.appendTail(sb);
        String result = sb.toString();
        Tensors.parse(result);
        return result;
    }

    @Ignore
    @Test
    public void reduce2redberryRR() {
        String exrpession = "L**2/10 *(R(s,al,be,gm)*R(ro,mu,nu,de) *n(s)*n(ro))*hk(de,j1,j2)*d(mu,nu,al,be,j2,j3)*hk(gm,j3,j1) + L**2*(L-1)**2*(L-2)*n(s)*n(ro) *(2/45*R(ro,al,de,nu)*R(s,be,mu,gm)-1/120*R(ro,de,al,nu)*R(s,be,mu,gm)) *hk(be,gm,de,j1,j2)*d(al,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)*n(ro)*n(s) *(-1/10*R(s,mu,gm,nu)*R(ro,al,de,be)+1/15*R(s,de,al,nu)*R(ro,be,mu,gm) +1/60*R(s,be,de,nu)*R(ro,gm,mu,al)) *hk(de,j1,j2)*d(al,be,gm,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)**2*n(s)*n(ro) *(-1/20*R(ro,mu,be,nu)*R(s,de,al,gm)+1/180*R(ro,al,nu,be)*R(s,gm,de,mu) -7/360*R(ro,mu,gm,nu)*R(s,al,de,be)-1/240*R(ro,de,be,nu)*R(s,gm,al,mu) -1/120*R(ro,be,gm,nu)*R(s,al,de,mu)-1/30*R(ro,de,be,nu)*R(s,al,gm,mu)) *hk(gm,de,j1,j2)*d(al,be,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)*(L-2)*n(s)*n(ro) *(-1/30*R(s,gm,nu,be)*R(ro,al,de,mu)-1/180*R(s,mu,gm,nu)*R(ro,al,be,de) +1/180*R(s,mu,gm,de)*R(ro,al,be,nu)) *hk(de,j1,j2)*d(mu,nu,j2,j3)*hk(al,be,gm,j3,j1) + L**2*(L-1)**2*(L-2)*n(s)*n(ro) *(1/45*R(ro,mu,gm,nu)*R(s,al,be,de)-1/80*R(ro,be,nu,gm)*R(s,mu,al,de) +1/90*R(ro,be,nu,gm)*R(s,de,al,mu)) *hk(mu,nu,j1,j2)*d(de,j2,j3)*hk(al,be,gm,j3,j1) + L**2*(L-1)*n(s)*n(ro) *(7/120*R(ro,be,gm,nu)*R(s,mu,al,de)-3/40*R(ro,be,gm,de)*R(s,mu,al,nu) +1/120*R(ro,de,gm,nu)*R(s,al,be,mu)) *hk(mu,nu,j1,j2)*d(al,be,gm,j2,j3)*hk(de,j3,j1) + L**2*(L-1)*(L-2)*n(s)*n(ro) *(-1/24*R(ro,mu,gm,nu)*R(s,al,be,de)-1/180*R(ro,nu,gm,de)*R(s,al,be,mu) -1/360*R(ro,de,gm,nu)*R(s,al,be,mu)) *hk(al,be,gm,j1,j2)*d(mu,nu,j2,j3)*hk(de,j3,j1) - L**2*(L-1)*(L-2)*(L-3)*(n(s)*n(ro) *R(s,al,be,gm)*R(ro,mu,nu,de)) *hk(de,j1,j2)*d(gm,j2,j3)*hk(mu,nu,al,be,j3,j1) /120 - L**2*(L-1)**2*(L-2)*(L-3)*(n(s)*n(ro) *R(ro,gm,be,mu)*R(s,al,de,nu)) *hk(al,be,gm,de,j1,j2)*hk(mu,nu,j2,j1) /80 + L**2*n(ro) *(-1/8*R(be,gm)*R(ro,nu,al,mu)+3/20*R(be,gm)*R(ro,mu,al,nu) +3/40*R(al,mu)*R(ro,be,gm,nu)+1/40*R(s,be,gm,mu)*R(ro,nu,al,s) -3/20*R(s,al,be,mu)*R(ro,gm,nu,s)+1/10*R(s,al,be,nu)*R(ro,gm,mu,s)) *hk(mu,j1,j2)*d(al,be,gm,j2,j3)*hk(nu,j3,j1) + L**2*(L-1)*n(ro) *(1/20*R(al,nu)*R(ro,gm,be,mu) +1/20*R(al,gm)*R(ro,mu,be,nu)+1/10*R(al,be)*R(ro,mu,gm,nu) +1/20*R(s,al,nu,gm)*R(ro,s,be,mu)-1/60*R(s,mu,al,nu)*R(ro,be,s,gm) +1/10*R(s,al,be,gm)*R(ro,mu,s,nu)-1/12*R(s,al,be,nu)*R(ro,mu,s,gm)) *hk(gm,j1,j2)*d(al,be,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)**2*n(ro) *(1/60*R(al,mu)*R(ro,be,nu,gm)-1/20*R(al,mu)*R(ro,gm,nu,be) +1/120*R(al,be)*R(ro,mu,nu,gm)+3/40*R(al,gm)*R(ro,nu,be,mu) +1/20*R(s,gm,mu,al)*R(ro,nu,s,be)+1/120*R(s,al,mu,gm)*R(ro,be,nu,s) -1/40*R(s,al,mu,gm)*R(ro,s,nu,be)+1/40*R(s,al,mu,be)*R(ro,s,nu,gm) -1/20*R(s,al,mu,be)*R(ro,gm,nu,s)-1/40*R(s,mu,be,nu)*R(ro,gm,s,al)) *hk(al,be,j1,j2)*d(gm,j2,j3)*hk(mu,nu,j3,j1) + L**2*(L-1)*n(ro) *(1/20*R(s,mu,nu,be)*R(ro,gm,s,al)-7/60*R(s,be,mu,al)*R(ro,gm,nu,s) +1/20*R(s,be,mu,al)*R(ro,s,nu,gm)+1/10*R(s,mu,be,gm)*R(ro,nu,al,s) +1/60*R(s,be,mu,gm)*R(ro,al,nu,s)+7/120*R(al,be)*R(ro,nu,gm,mu) +11/60*R(be,mu)*R(ro,nu,al,gm)) *hk(al,be,j1,j2)*d(mu,nu,j2,j3)*hk(gm,j3,j1) + L**2*(L-1)*(L-2)*n(ro) *(7/240*R(al,be)*R(ro,gm,mu,nu)+7/240*R(al,nu)*R(ro,be,gm,mu) -1/60*R(al,mu)*R(ro,be,gm,nu)-1/24*R(s,al,be,nu)*R(ro,s,gm,mu) +1/15*R(s,al,be,nu)*R(ro,mu,gm,s)+1/40*R(s,al,be,mu)*R(ro,s,gm,nu) +1/40*R(be,gm)*R(ro,nu,mu,al)+1/48*R(s,be,gm,mu)*R(ro,nu,al,s)) *hk(al,be,gm,j1,j2)*d(mu,j2,j3)*hk(nu,j3,j1) + L**2*(L-1)**2*(L-2) *n(ro)*(-7/240*R(ro,be,gm,nu)*R(mu,al)+1/240*R(ro,mu,al,nu)*R(be,gm) -1/40*R(ro,nu,gm,s)*R(s,al,mu,be)) *hk(mu,nu,j1,j2)*hk(al,be,gm,j2,j1) + L*(L-1)*(L-2)*(L-3) *(1/180*R(mu,nu)*R(al,be)+7/720*R(s,al,be,ro)*R(ro,mu,nu,s)) *hk(mu,nu,al,be,j1,j1)";
        System.out.println(reduce2Redberry(exrpession));
    }

    @Ignore
    @Test
    public void reduce2redberryFF() {
        String exrpession = "- L**2*(L-1)**2*(R(mu,al,j2,j3)*R(nu,be,j4,j1)) &hk(mu,nu,j1,j2)&hk(al,be,j3,j4)/24 +L**2*(R(be,nu,j2,j3)*R(al,mu,j5,j1) - 5*R(be,mu,j2,j3)*R(al,nu,j5,j1)) &hk(mu,j1,j2)&d(al,be,j3,j4)&hk(nu,j4,j5)/24 - L**2*(L-1) *(1/48*R(be,nu,j2,j3)*R(al,mu,j5,j1)+1/48*R(be,mu,j2,j3)*R(al,nu,j5,j1)) &hk(mu,j1,j2)&d(nu,j3,j4)&hk(al,be,j4,j5)";
        System.out.println(reduce2Redberry(exrpession));
    }

    //    @Ignore
    //    @Test
    //    public void performanceTest() {
    //        Tensors.addSymmetry("R_\\mu\\nu", IndexType.GreekLower, false, new int[]{1, 0});
    //        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, true, new int[]{0, 1, 3, 2});
    //        Tensors.addSymmetry("R_\\mu\\nu\\alpha\\beta", IndexType.GreekLower, false, new int[]{2, 3, 0, 1});
    //        Expression[] riemansSubstitutions = new Expression[]{
    //                Tensors.parseExpression("R_{\\mu \\nu}^{\\mu}_{\\alpha} = R_{\\nu\\alpha}"),
    //                Tensors.parseExpression("R_{\\mu\\nu}^{\\alpha}_{\\alpha}=0"),
    //                Tensors.parseExpression("F_{\\mu}^{\\mu}^{\\alpha}_{\\beta}=0"),
    //                Tensors.parseExpression("R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\alpha\\nu\\beta}=(1/2)*R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\nu\\alpha\\beta}"),
    //                Tensors.parseExpression("R_{\\mu\\nu\\alpha\\beta}*R^{\\mu\\nu\\alpha\\beta}=4*R_{\\mu\\nu}*R^{\\mu\\nu}-R*R"),
    //                Tensors.parseExpression("R_{\\mu}^{\\mu}= R"),
    //                Tensors.parseExpression("P_{\\mu}^{\\mu}= P")
    //        };
    //        Expression kronecker = (Expression) Tensors.parse("d_{\\mu}^{\\mu}=4");
    //        Transformation n2 = new SqrSubs(Tensors.parseSimple("n_\\mu")), n2Transformer = new Transformer(TraverseState.Leaving, new Transformation[]{n2});
    //        Transformation[] common = new Transformation[]{ContractIndices.ContractIndices, n2Transformer, kronecker};
    //        Transformation[] all = ArraysUtils.addAll(common, riemansSubstitutions);
    //
    //        Tensor t;
    ////        t = Tensors.parse("-64*(n^\\alpha*g^\\beta\\gamma+n^\\beta*g^\\alpha\\gamma+n^\\gamma*g^\\alpha\\beta)*n^\\delta*n_\\sigma*n_\\rho*g^\\mu\\nu*((-1/30)*R^\\rho_{\\gamma\\nu\\beta}*R^\\sigma_{\\alpha\\delta\\mu}-(1/180)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}+(1/180)*R^\\rho_{\\mu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\nu})");
    ////        t = Tensors.parse("-64*n^\\delta*n_\\sigma*n_\\rho*("
    ////                + "n^\\beta*g^\\alpha\\gamma*g^\\mu\\nu*(-1/30)*R^\\rho_{\\gamma\\nu\\beta}*R^\\sigma_{\\alpha\\delta\\mu}"
    ////                + "+n^\\gamma*g^\\alpha\\beta*g^\\mu\\nu*(-1/180)*R^\\rho_{\\mu\\gamma\\nu}*R^\\sigma_{\\alpha\\beta\\delta}"
    ////                + "+n^\\beta*g^\\alpha\\gamma*g^\\mu\\nu*(1/180)*R^\\rho_{\\mu\\gamma\\delta}*R^\\sigma_{\\alpha\\beta\\nu})");
    ////        t = Tensors.parse("4**2*3*12*1/3*(n^\\alpha*g^\\beta\\gamma+n^\\beta*g^\\alpha\\gamma+n^\\gamma*g^\\alpha\\beta)*n^\\delta*n_\\sigma*n_\\rho/3*g^\\mu\\nu*(-1/10*R^\\rho_\\mu\\gamma\\nu*R^\\sigma_\\alpha\\delta\\beta+1/15*R^\\rho_\\delta\\alpha\\nu*R^\\sigma_\\beta\\mu\\gamma+1/60*R^\\rho_\\beta\\delta\\nu*R^\\sigma_\\gamma\\mu\\alpha)");
    //        Expression Kn1 = Tensors.parseExpression("Kn^\\alpha=n^\\alpha");
    //        Expression Kn2 = Tensors.parseExpression("Kn^\\alpha\\beta=1/3*(2*n^\\alpha*n^\\beta+g^\\alpha\\beta)");
    //        Expression Kn3 = Tensors.parseExpression("Kn^\\alpha\\beta\\gamma=1/3*(n^\\alpha*g^\\beta\\gamma+n^\\beta*g^\\alpha\\gamma+n^\\gamma*g^\\alpha\\beta)");
    //        Tensor delta = Tensors.parseExpression(
    //                "DELTA^{\\mu\\nu\\alpha}="
    //                        + "-(1/6)*L*(L-1)*(L-2)*Kn^{\\mu\\nu\\alpha}"
    //                        + "+Power[L,2]*(L-1)*(1/3)*("
    //                        + "Kn^{\\mu \\nu }*Kn^{\\alpha }+"
    //                        + "Kn^{\\alpha \\nu }*Kn^{\\mu }+"
    //                        + "Kn^{\\mu \\alpha }*Kn^{\\nu })"
    //                        + "-Power[L,3]*Kn^{\\mu }*Kn^{\\nu }*Kn^{\\alpha }");
    //        delta = Tensors.parseExpression("L=4").transform(delta);
    //        delta = Kn1.transform(delta);
    //        delta = Kn2.transform(delta);
    //        delta = Kn3.transform(delta);
    //        delta = Expand.expand(delta, all);
    //        for (Transformation tr : all)
    //            delta = tr.transform(delta);
    //        t = Tensors.parse(
    //                //                "Power[L,2]*(L-1)"
    //                "DELTA^\\alpha\\beta\\gamma"
    //                        + "*n^\\delta*n_\\sigma*n_\\rho"
    //                        + "*Kn^\\mu\\nu"
    //                        + "*("
    //                        + "-1/10*R^\\rho_\\mu\\gamma\\nu*R^\\sigma_\\alpha\\delta\\beta"
    //                        + "+1/15*R^\\rho_\\delta\\alpha\\nu*R^\\sigma_\\beta\\mu\\gamma"
    //                        + "+1/60*R^\\rho_\\beta\\delta\\nu*R^\\sigma_\\gamma\\mu\\alpha)");
    //        t = Tensors.parseExpression("L=4").transform(t);
    //        t = ((Expression) delta).transform(t);
    //        t = Kn2.transform(t);
    //
    //        Tensor temp = t;
    //
    //        temp = Expand.expand(temp, all);
    //        for (Transformation tr : all)
    //            temp = tr.transform(temp);
    //
    //        temp = Expand.expand(temp, all);
    //        for (Transformation tr : all)
    //            temp = tr.transform(temp);
    //
    //        temp = new Averaging(Tensors.parseSimple("n_\\mu")).transform(temp);
    //        temp = Expand.expand(temp, all);
    //        for (Transformation tr : all)
    //            temp = tr.transform(temp);
    //        temp = Expand.expand(temp, all);
    //
    //        Assert.assertTrue(TensorUtils.equals(temp, Tensors.parse("-19/360*R^\\mu\\nu*R_\\mu\\nu-1/80*R**2")));
    //    }
}