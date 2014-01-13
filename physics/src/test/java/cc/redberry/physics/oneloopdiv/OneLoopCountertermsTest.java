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
        OneLoopUtils.setUpRiemannSymmetries();
        Tensors.addSymmetry("F_lm", IndexType.LatinLower, true, new int[]{1, 0});

        String FR_ =
                "FR= Power[L,2]*Power[(L-1),2]*(-2*n^a)*g^{bc}*g^{lm}*n_q*((1/60)*R^q_{blc}*F_{am}"
                        + "+(1/20)*R^q_{alc}*F_{mb}+(1/15)*R^q_{cla}*F_{mb}"
                        + "+(1/60)*R^q_{lmc}*F_{ab})";
        Tensor FR = Tensors.parse(FR_);

        FR = Tensors.parseExpression("L = 2").transform(FR);
        FR = ExpandTransformation.expand(FR);
        FR = new Averaging(Tensors.parseSimple("n_l")).transform(FR);
        FR = ExpandTransformation.expand(FR);
        FR = EliminateMetricsTransformation.eliminate(FR);
        Expression[] riemansSubstitutions = new Expression[]{
                Tensors.parseExpression("R_{l m}^{l}_{a} = R_{ma}"),
                Tensors.parseExpression("R_{lm}^{a}_{a}=0"),
                Tensors.parseExpression("F_{l}^{l}^{a}_{b}=0"),
                Tensors.parseExpression("R_{lmab}*R^{lamb}=(1/2)*R_{lmab}*R^{lmab}"),
                Tensors.parseExpression("R_{lmab}*R^{lmab}=4*R_{lm}*R^{lm}-R*R"),
                Tensors.parseExpression("R_{l}^{l}= R"),
                Tensors.parseExpression("P_{l}^{l}= P")
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
        OneLoopUtils.setUpRiemannSymmetries();
        //TIME = 6.1 s
        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b");
        Expression K = Tensors.parseExpression("K^lm_a^b=d_a^b*g^{lm}");
        Expression S = Tensors.parseExpression("S^lab=0");
        Expression W = Tensors.parseExpression("W_a^b=W_a^b");
        Expression F = Tensors.parseExpression("F_lmab=F_lmab");

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);

        Tensor A = action.getCounterterms().get(1);
        A = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(A);

        //this is the exact K.V. result with corrections that 1/12*F_..*F^.. and oth are not under tr operation and that tr of 1 is 4
        Tensor expected = Tensors.parse("1/30*Power[R, 2]+1/12*F_{m b }^{e }_{p_5 }*F^{m b p_5 }_{e }+1/15*R_{d m }*R^{d m }+1/2*W^{a }_{p_5 }*W^{p_5 }_{a }+1/6*R*W^{b }_{b }");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testMinimalSecondOrderOperatorBarvinskyVilkovisky() {
        OneLoopUtils.setUpRiemannSymmetries();
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
        Tensor A = action.getCounterterms().get(1);
        A = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(A);
        //this is the exact Barvinsky and Vilkovisky
        Tensor expected = Tensors.parse("1/12*F_{m b }^{e }_{p_5 }*F^{m b p_5 }_{e }+1/2*W^{p_5 }_{a }*W^{a }_{p_5 }+-1/45*Power[R, 2]+1/15*R^{l m }*R_{l m }");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testMinimalFourthOrderOperator() {
        //TIME = 6.2 s
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
        OneLoopUtils.setUpRiemannSymmetries();

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
        Tensor A = action.getCounterterms().get(1);

        A = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(A);
        Tensor expected = Tensors.parse("44/135*R**2-32/135*R_lm*R^lm+2/3*F_lmab*F^lmba");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testVectorField0() {
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
        OneLoopUtils.setUpRiemannSymmetries();

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b+c*n_a*n^b");
        Expression K = Tensors.parseExpression("K^{lm}_a^{b}=g^{lm}*d_{a}^{b}-k/2*(g^{lb}*d_a^m+g^{mb}*d_a^l)");
        Expression S = Tensors.parseExpression("S^p^l_m=0");
        Expression W = Tensors.parseExpression("W^{a}_{b}=P^{a}_{b}+(k/2)*R^a_b");
        Expression F = Tensors.parseExpression("F_lmab=R_lmab");

        Expression lambda = Tensors.parseExpression("k=0");//gamma/(1+gamma)");
        Expression gamma = Tensors.parseExpression("c=0");//gamma");
        iK = (Expression) gamma.transform(lambda.transform(iK));
        K = (Expression) gamma.transform(lambda.transform(K));
        S = (Expression) gamma.transform(lambda.transform(S));
        W = (Expression) gamma.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);
        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);
        A = Tensors.parseExpression("P^l_l = P").transform(A);
        System.out.println(A);
        Tensor expected = Tensors.parse("7/60*Power[R, 2]-4/15*R^{l m }*R_{l m }+1/2*P^{c }_{a }*P^{a }_{c }+1/6*P*R");
        TAssert.assertEquals(A, expected);
    }

    @Test
    public void testGravityGhosts0() {
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
        OneLoopUtils.setUpRiemannSymmetries();

        Expression iK = Tensors.parseExpression("iK_a^b=d_a^b+gamma*n_a*n^b");
        Expression K = Tensors.parseExpression("K^{lm}_a^{b}=d_a^b*g^lm-1/2*beta*(d_a^l*g^mb+d_a^m*g^lb)");
        Expression S = Tensors.parseExpression("S^p^l_m=0");
        Expression W = Tensors.parseExpression("W^{a}_{b}=(1+beta/2)*R^a_b");
        Expression F = Tensors.parseExpression("F_lmab=R_lmab");


        Expression beta = Tensors.parseExpression("beta=0");
        Expression lambda = Tensors.parseExpression("gamma=beta/(1-beta)");
        iK = (Expression) beta.transform(lambda.transform(iK));
        K = (Expression) beta.transform(lambda.transform(K));
        S = (Expression) beta.transform(lambda.transform(S));
        W = (Expression) beta.transform(lambda.transform(W));

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);

        Tensor expected = ExpandTransformation.expand(Tensors.parse("7/30*R^{l m }*R_{l m }+17/60*Power[R, 2]"));
        TAssert.assertEquals(ExpandTransformation.expand(A), expected);
    }

    /*
    * ****************************
    * ******* Medium tests *******
    * ****************************
    */

    @Test
    public void testVectorField() {
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
        OneLoopUtils.setUpRiemannSymmetries();

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
        Tensor A = action.getCounterterms().get(1);

        Tensor actual = Tensors.parseExpression("P^l_l = P").transform(A);

        Tensor expected = ExpandTransformation.expand(Tensors.parse("-5/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+47/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+1789/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+929/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-19/120*gamma*Power[gamma+1, -1]*Power[R, 2]+167/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+-5/72*R*Power[gamma+1, -1]*P*Power[gamma, 4]+-337/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+7/60*Power[R, 2]+-1/24*R*Power[gamma+1, -1]*P*Power[gamma, 2]+1/12*gamma*R*P+-109/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-37/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+1453/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+-1409/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/72*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 6]+(6851/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+11/20*Power[gamma+1, -1]*Power[gamma, 5]+-39/80*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-199/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-107/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+1333/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-23/120*Power[gamma, 4]+-49/60*Power[gamma, 2]+-67/120*Power[gamma, 3]+1259/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-133/144*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+11/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+31/64*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-41/60*gamma+29/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+3869/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+23/30*gamma*Power[gamma+1, -1]+811/360*Power[gamma+1, -1]*Power[gamma, 3]+329/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-4/15+-6631/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+97/320*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+1277/720*Power[gamma+1, -1]*Power[gamma, 2]+-2489/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1319/720*Power[gamma+1, -1]*Power[gamma, 4]+-2627/960*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/120*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+-3253/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-965/576*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6]+-9/40*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 9]+17/240*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 10]+-1511/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 8]+-341/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 7]+737/2880*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-11/180*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3])*R^{l m }*R_{l m }+1/48*Power[P, 2]*Power[gamma, 2]+(-5/36*Power[gamma+1, -1]*Power[gamma, 4]+-7/12*Power[gamma+1, -1]*Power[gamma, 2]+-37/72*Power[gamma+1, -1]*Power[gamma, 3]+1/6*Power[gamma, 2]+1/18*Power[gamma, 3]+1/6*gamma+1/6*gamma*Power[gamma+1, -1]+73/72*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/9*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+2/3*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 2]+11/24*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 3]+1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 4]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 5]+-1/36*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma, 6])*P^{l a }*R_{l a }+1/6*R*P+-1391/1440*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+319/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+-203/3840*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/18*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 5]+29/1920*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+49/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 9]+-271/480*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+29/120*gamma*Power[R, 2]+1/12*R*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+19/288*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+-1/144*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 3]+1/20*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+9/80*Power[R, 2]*Power[gamma, 4]+17/40*Power[R, 2]*Power[gamma, 2]+2761/11520*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+1/12*R*P*Power[gamma, 2]+-37/120*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 5]+1/36*R*P*Power[gamma, 3]+-497/1152*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 6]+4669/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 4]+83/240*Power[R, 2]*Power[gamma, 3]+-43/40*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 3]+53/720*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-1/36*R*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*P*Power[gamma, 4]+-403/5760*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 8]+(1/24*Power[gamma, 2]+1/4*gamma+1/2)*P^{a }_{p_5 }*P^{p_5 }_{a }+1/480*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 2]+-13/144*R*Power[gamma+1, -1]*P*Power[gamma, 3]+-19/1440*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[gamma+1, -1]*Power[R, 2]*Power[gamma, 10]"));
        TAssert.assertEquals(ExpandTransformation.expand(actual), expected);


        //simplified result
        actual = FactorTransformation.factor(A, false);
        actual = Tensors.parseExpression("P^l_l = P").transform(actual);
        expected =
                Tensors.parse("1/24*(gamma**2+6*gamma+12)*P_lm*P^lm"
                        + "+1/48*gamma**2*P**2"
                        + "+gamma/12*(gamma+4)*R_lm*P^lm"
                        + "+1/24*(gamma**2+2*gamma+4)*R*P"
                        + "+1/120*(-32+10*gamma+5*gamma**2)*R_lm*R^lm"
                        + "+1/240*(28+20*gamma+5*gamma**2)*R**2");
        TAssert.assertEquals(expected, actual);
    }

    @Test
    public void testGravityGhosts() {
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
        OneLoopUtils.setUpRiemannSymmetries();

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
        Tensor A = action.getCounterterms().get(1);

        //non simplified result
        Tensor expected = ExpandTransformation.expand(Tensors.parse("17/60*Power[R, 2]+1789/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-203/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+-337/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+61/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-109/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-497/480*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+-497/1152*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+1439/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+829/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-97/160*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+-19/120*Power[1+gamma, -1]*Power[R, 2]*gamma+2441/11520*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+(17/320*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-67/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+71/60*gamma+7/120*Power[gamma, 4]+161/120*Power[gamma, 2]+233/360*Power[gamma, 3]+-1*(29/20*gamma+1/4*Power[gamma, 4]+23/20*Power[gamma, 3]+39/20*Power[gamma, 2]+83/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-43/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+3/5*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+667/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-121/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+99/160*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-7/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+7/48*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-13/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+-181/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+-1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-33/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-29/20*Power[1+gamma, -1]*gamma+-7/10*Power[1+gamma, -1]*Power[gamma, 5]+103/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-117/40*Power[1+gamma, -1]*Power[gamma, 2]+8/5+-173/40*Power[1+gamma, -1]*Power[gamma, 3]+-37/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-341/120*Power[1+gamma, -1]*Power[gamma, 4]+293/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+281/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-29/120*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+11/60*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+37/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-13/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-14/15*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+-1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-139/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+317/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3])+481/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1009/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+899/288*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5]+13/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+9/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+-1231/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+-31/60*Power[1+gamma, -1]*gamma+-3/20*Power[1+gamma, -1]*Power[gamma, 5]+35/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-4661/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+1/80*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 10]+11/6+127/90*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 3]+3509/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 4]+3919/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+1877/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+-1/24*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+49/960*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 9]+-827/720*Power[1+gamma, -1]*Power[gamma, 4]+-931/360*Power[1+gamma, -1]*Power[gamma, 3]+-1559/576*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 6]+59/144*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 2]+1/30*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+1441/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+5/64*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 8]+-1249/720*Power[1+gamma, -1]*Power[gamma, 2]+-1/40*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 7]+731/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[gamma, 5])*R^{c }_{l }*R^{l }_{c }+1/480*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 2]+13/40*Power[R, 2]*gamma+-839/720*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+9/80*Power[R, 2]*Power[gamma, 4]+127/240*Power[R, 2]*Power[gamma, 2]+29/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+269/720*Power[R, 2]*Power[gamma, 3]+49/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 9]+-37/120*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+3/32*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 3]+-403/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+-37/384*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+167/3840*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+5149/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 4]+53/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+283/1920*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]+-19/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 10]+-37/240*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 8]+-1409/2880*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 7]+11/720*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+4679/5760*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 5]+319/1440*Power[1+gamma, -1]*Power[1+gamma, -1]*Power[R, 2]*Power[gamma, 6]"));
        TAssert.assertEquals(ExpandTransformation.expand(A), expected);

        //simplified result
        expected = Tensors.parse("(1/30)*(20*gamma+5*gamma**2+7)*R_{lm}*R^{lm}+(1/60)*(10*gamma+5*gamma**2+17)*R**2");
        TAssert.assertEquals(FactorTransformation.factor(A, false), expected);
    }

    /*
    * **************************************
    * ******* High complexity tests ********
    * **************************************
    */

    @Test
    public void testSquaredVectorField() {
        Tensors.addSymmetry("P_lm", IndexType.LatinLower, false, 1, 0);
        OneLoopUtils.setUpRiemannSymmetries();

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

        //not simplified result
        Tensor A = action.getCounterterms().get(1);
        Tensor actual = Tensors.parseExpression("P^l_l = P").transform(A);
        actual = ExpandTransformation.expand(TogetherTransformation.together(ExpandTransformation.expand(TogetherTransformation.together(actual))));
        Tensor expected = Tensors.parse("(8723/120)*(1+gamma)**(-12)*R**2*gamma**10+(2093/120)*(1+gamma)**(-12)*R**2*gamma**2+(689/30)*(1+gamma)**(-12)*R**2*gamma**11+(289/12)*(1+gamma)**(-12)*R*gamma**2*P+(7/30)*(1+gamma)**(-12)*R**2+(1859/5)*(1+gamma)**(-12)*R**2*gamma**7+(1859/12)*(1+gamma)**(-12)*R**2*gamma**4+33*(1+gamma)**(-12)*P**2*gamma**7+33*(1+gamma)**(-12)*P**2*gamma**9+286*(1+gamma)**(-12)*R**2*gamma**8+((1+gamma)**(-12)+(1/12)*(1+gamma)**(-12)*gamma**14+(3/2)*(1+gamma)**(-12)*gamma**13+(25/2)*(1+gamma)**(-12)*gamma**12+(25/2)*(1+gamma)**(-12)*gamma+(190/3)*(1+gamma)**(-12)*gamma**11+254*(1+gamma)**(-12)*gamma**3+(5445/4)*(1+gamma)**(-12)*gamma**6+(865/12)*(1+gamma)**(-12)*gamma**2+(869/4)*(1+gamma)**(-12)*gamma**10+968*(1+gamma)**(-12)*gamma**8+(1067/2)*(1+gamma)**(-12)*gamma**9+(6347/6)*(1+gamma)**(-12)*gamma**5+(1221/2)*(1+gamma)**(-12)*gamma**4+1320*(1+gamma)**(-12)*gamma**7)*P^{l}_{p_{5}}*P^{p_{5}}_{l}+(377/6)*(1+gamma)**(-12)*R**2*gamma**3+(100/3)*(1+gamma)**(-12)*R*gamma**11*P+(299/60)*(1+gamma)**(-12)*R**2*gamma**12+(1199/12)*(1+gamma)**(-12)*R*gamma**10*P+(14729/40)*(1+gamma)**(-12)*R**2*gamma**6+(1/12)*(1+gamma)**(-12)*R*gamma**14*P+(55/6)*(1+gamma)**(-12)*P**2*gamma**5+(55/6)*(1+gamma)**(-12)*P**2*gamma**11+(2189/6)*(1+gamma)**(-12)*R*gamma**5*P+(47/6)*(1+gamma)**(-12)*R*gamma**12*P+(-(9647/30)*(1+gamma)**(-12)*gamma**5-(1987/60)*(1+gamma)**(-12)*gamma**2+(2563/60)*(1+gamma)**(-12)*gamma**10+(1/12)*(1+gamma)**(-12)*gamma**14+(7/6)*(1+gamma)**(-12)*gamma**13-55*(1+gamma)**(-12)*gamma**8+(187/6)*(1+gamma)**(-12)*gamma**9+(209/30)*(1+gamma)**(-12)*gamma**12-(187/30)*(1+gamma)**(-12)*gamma-(316/3)*(1+gamma)**(-12)*gamma**3+(344/15)*(1+gamma)**(-12)*gamma**11-(8/15)*(1+gamma)**(-12)-(1012/5)*(1+gamma)**(-12)*gamma**7-(6391/20)*(1+gamma)**(-12)*gamma**6-(1331/6)*(1+gamma)**(-12)*gamma**4)*R_{lm}*R^{lm}+(25/6)*(1+gamma)**(-12)*R*gamma*P+(7/6)*(1+gamma)**(-12)*R*gamma**13*P+(256/3)*(1+gamma)**(-12)*R*gamma**3*P+(1331/6)*(1+gamma)**(-12)*R*gamma**9*P+(77/2)*(1+gamma)**(-12)*P**2*gamma**8+((1/6)*(1+gamma)**(-12)*gamma**14+(8/3)*(1+gamma)**(-12)*gamma**13+19*(1+gamma)**(-12)*gamma**12+46*(1+gamma)**(-12)*gamma**3+(49/6)*(1+gamma)**(-12)*gamma**2+(2/3)*(1+gamma)**(-12)*gamma+(242/3)*(1+gamma)**(-12)*gamma**11+462*(1+gamma)**(-12)*gamma**9+(473/3)*(1+gamma)**(-12)*gamma**4+682*(1+gamma)**(-12)*gamma**8+748*(1+gamma)**(-12)*gamma**7+(1100/3)*(1+gamma)**(-12)*gamma**5+(1221/2)*(1+gamma)**(-12)*gamma**6+(1375/6)*(1+gamma)**(-12)*gamma**10)*P^{p_{5}}_{a}*R_{p_{5}}^{a}+(1/24)*(1+gamma)**(-12)*R**2*gamma**14+(2/3)*(1+gamma)**(-12)*R**2*gamma**13+(4147/15)*(1+gamma)**(-12)*R**2*gamma**5+(165/8)*(1+gamma)**(-12)*P**2*gamma**6+(165/8)*(1+gamma)**(-12)*P**2*gamma**10+(1925/4)*(1+gamma)**(-12)*R*gamma**6*P+(1243/6)*(1+gamma)**(-12)*R*gamma**4*P+374*(1+gamma)**(-12)*R*gamma**8*P+(1/2)*(1+gamma)**(-12)*P**2*gamma**3+(1/2)*(1+gamma)**(-12)*P**2*gamma**13+(1/24)*(1+gamma)**(-12)*P**2*gamma**2+(1/24)*(1+gamma)**(-12)*P**2*gamma**14+(1001/6)*(1+gamma)**(-12)*R**2*gamma**9+(1/3)*(1+gamma)**(-12)*R*P+(89/30)*(1+gamma)**(-12)*R**2*gamma+(11/4)*(1+gamma)**(-12)*P**2*gamma**4+(11/4)*(1+gamma)**(-12)*P**2*gamma**12+484*(1+gamma)**(-12)*R*gamma**7*P");

        TAssert.assertEquals(actual, expected);

        //simplified result
        actual = FactorTransformation.factor(A, false);
        actual = Tensors.parseExpression("P^l_l = P").transform(actual);

        expected =
                Tensors.parse("1/12*(gamma**2+6*gamma+12)*P_lm*P^lm"
                        + "+1/24*gamma**2*P**2"
                        + "+gamma/6*(gamma+4)*R_lm*P^lm"
                        + "+1/12*(gamma**2+2*gamma+4)*R*P"
                        + "+1/60*(-32+10*gamma+5*gamma**2)*R_lm*R^lm"
                        + "+1/120*(28+20*gamma+5*gamma**2)*R**2");
        TAssert.assertEquals(actual, expected);
    }


    @Test
    public void testLambdaGaugeGravity() {
        OneLoopUtils.setUpRiemannSymmetries();

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
        Tensor A = action.getCounterterms().get(1);

        A = ExpandTransformation.expand(TogetherTransformation.together(ExpandTransformation.expand(A)));
        //TODO simplify result
        //non simplified result
        Tensor expected = Tensors.parse("((7/6)*(1+la)**(-6)+(23/3)*la*(1+la)**(-6)+(2/3)*la**8*(1+la)**(-6)+(14/3)*la**7*(1+la)**(-6)+(91/3)*la**5*(1+la)**(-6)+(91/6)*la**6*(1+la)**(-6)+(112/3)*la**3*(1+la)**(-6)+(133/6)*la**2*(1+la)**(-6)+(245/6)*la**4*(1+la)**(-6))*R_{lm}*R^{lm}+(1/3)*la**8*R**2*(1+la)**(-6)+2*la**7*R**2*(1+la)**(-6)+(7/2)*R**2*la*(1+la)**(-6)+(109/12)*la**2*R**2*(1+la)**(-6)+(7/12)*R**2*(1+la)**(-6)+(41/3)*la**3*R**2*(1+la)**(-6)+(55/4)*la**4*R**2*(1+la)**(-6)+(61/6)*la**5*R**2*(1+la)**(-6)+(67/12)*la**6*R**2*(1+la)**(-6)");
        Assert.assertTrue(TensorUtils.equals(A, expected));

        //simplified result
        //Tensor expected = Tensors.parse("1/6*(4*la**2+4*la+7)*R_lm*R^lm+1/12*(4*la**2+7)*R**2");
    }

    @Test
    public void testSpin3Ghosts() {
        OneLoopUtils.setUpRiemannSymmetries();
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
        //        for (Expression cons : consts) {
        //            iK = (Expression) cons.transform(iK);
        //            K = (Expression) cons.transform(K);
        //        }
        Expression S = (Expression) Tensors.parse("S^p^{ab}_{lm}=0");
        Expression W = (Expression) Tensors.parse("W^{ab}_{lm}=0");
        Expression F = Tensors.parseExpression("F_lmabcd=0");

        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F, OneLoopUtils.antiDeSitterBackground());

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
        OneLoopUtils.setUpRiemannSymmetries();
        //FIXME works more than hour
        Tensors.addSymmetry("R_lm", IndexType.LatinLower, false, new int[]{1, 0});
        Tensors.addSymmetry("R_lmab", IndexType.LatinLower, true, new int[]{0, 1, 3, 2});
        Tensors.addSymmetry("R_lmab", IndexType.LatinLower, false, new int[]{2, 3, 0, 1});


        Expression iK = Tensors.parseExpression("iK_ab^lm = "
                + "(d_a^l*d_b^m+d_b^l*d_a^m)/2"
                + "-la/2*("
                + "d_a^l*n_b*n^m"
                + "+d_a^m*n_b*n^l"
                + "+d_b^l*n_a*n^m"
                + "+d_b^m*n_a*n^l)"
                + "-ga*(g_ab*n^l*n^m+g^lm*n_a*n_b)"
                + "-1/2*g_ab*g^lm"
                + "+2*ga*(ga*la-2*ga+2*la)*n_a*n_b*n^l*n^m");
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
                + "+(la-be)/(2*(1+la))*("
                + "g^cd*(d_a^l*d_b^m+d_a^m*d_b^l)"
                + "+g_ab*(g^cl*g^dm+g^cm*g^dl))"
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
        //        Expression F = Tensors.parseExpression("F_lm^kd_pr = "
        //                + "R^k_plm*d^d_r+R^d_rlm*d^k_p");

        //todo together symbolic
        OneLoopInput input = new OneLoopInput(2, iK, K, S, W, null, null, F);

        OneLoopCounterterms action = OneLoopCounterterms.calculateOneLoopCounterterms(input);
        Tensor A = action.getCounterterms().get(1);


        //TODO simplify result
        //non simplified result
        //        Tensor expected = Tensors.parse("-43/960*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+31751/2880*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)-161/960*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3311/1920*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3833/5760*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-281/60*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-59/12*R**2*la**2*(1+la)**(-1)+34979/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)-7651/1440*R**2*la**4*(1+la)**(-1)+1627/2880*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+(7/45*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-107/30*la+1631/720*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-6841/160*la**4*(1+la)**(-1)*(1+la)**(-1)-4619/5760*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+101/96*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+3211/360*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+1729/80*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-18517/960*la**5*(1+la)**(-1)*(1+la)**(-1)-3697/2880*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-179/720*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3109/5760*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+953/1440*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2533/720*la**6*(1+la)**(-1)*(1+la)**(-1)+79/30*la**5*(1+la)**(-1)-2551/2880*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+127/30*la*(1+la)**(-1)+7/6+10387/1152*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-25/48*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-5477/2880*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-2825/72*la**3*(1+la)**(-1)*(1+la)**(-1)+881/36*la**2*(1+la)**(-1)-95/9*la**2*(1+la)**(-1)*(1+la)**(-1)+6197/180*la**3*(1+la)**(-1)-301/480*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-23/30*la**4-299/60*la**3-541/60*la**2+1067/1440*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4003/240*la**4*(1+la)**(-1)-803/1440*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+281/1440*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+155/8*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-571/240*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1))*R^{l m }*R_{l m }-3223/360*R**2*la**3*(1+la)**(-1)-667/360*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-1109/288*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-91/60*R**2*la**5*(1+la)**(-1)-1/30*R**2*la**10*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+9/20*R**2*la**4-7349/11520*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+157/60*R**2*la**2+181/120*R**2*la**3+103/320*R**2*la**4*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-13/10*R**2*la*(1+la)**(-1)+859/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+7/12*R**2-20419/11520*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-3181/5760*R**2*la**5*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-533/480*R**2*la**7*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-15/64*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+601/72*R**2*la**3*(1+la)**(-1)*(1+la)**(-1)+13/10*R**2*la+25/96*R**2*la**8*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)-4955/2304*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+919/480*R**2*la**6*(1+la)**(-1)*(1+la)**(-1)-139/960*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+17/480*R**2*la**9*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)*(1+la)**(-1)+4/3*R**2*la**2*(1+la)**(-1)*(1+la)**(-1)");
        //        Assert.assertTrue(TensorUtils.equals(A, expected));

        //simplified result
        //Tensor expected = Tensors.parse("1/12*(c1*R_lm*R^lm+c2*R**2+c3*R*LA+c4*LA**2)");
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
            group = group.replace("al", "\\a");
            group = group.replace("be", "\\b");
            group = group.replace("gm", "\\c");
            group = group.replace("de", "\\d");
            group = group.replace("s", "\\q");
            group = group.replace("ro", "\\p");
            group = group.replace("mu", "\\l");
            group = group.replace("nu", "\\m");
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
    //        Tensors.addSymmetry("R_lm", IndexType.LatinLower, false, new int[]{1, 0});
    //        Tensors.addSymmetry("R_lmab", IndexType.LatinLower, true, new int[]{0, 1, 3, 2});
    //        Tensors.addSymmetry("R_lmab", IndexType.LatinLower, false, new int[]{2, 3, 0, 1});
    //        Expression[] riemansSubstitutions = new Expression[]{
    //                Tensors.parseExpression("R_{l m}^{l}_{a} = R_{ma}"),
    //                Tensors.parseExpression("R_{lm}^{a}_{a}=0"),
    //                Tensors.parseExpression("F_{l}^{l}^{a}_{b}=0"),
    //                Tensors.parseExpression("R_{lmab}*R^{lamb}=(1/2)*R_{lmab}*R^{lmab}"),
    //                Tensors.parseExpression("R_{lmab}*R^{lmab}=4*R_{lm}*R^{lm}-R*R"),
    //                Tensors.parseExpression("R_{l}^{l}= R"),
    //                Tensors.parseExpression("P_{l}^{l}= P")
    //        };
    //        Expression kronecker = (Expression) Tensors.parse("d_{l}^{l}=4");
    //        Transformation n2 = new SqrSubs(Tensors.parseSimple("n_l")), n2Transformer = new Transformer(TraverseState.Leaving, new Transformation[]{n2});
    //        Transformation[] common = new Transformation[]{ContractIndices.ContractIndices, n2Transformer, kronecker};
    //        Transformation[] all = ArraysUtils.addAll(common, riemansSubstitutions);
    //
    //        Tensor t;
    ////        t = Tensors.parse("-64*(n^a*g^bc+n^b*g^ac+n^c*g^ab)*n^d*n_q*n_p*g^lm*((-1/30)*R^p_{cmb}*R^q_{adl}-(1/180)*R^p_{lcm}*R^q_{abd}+(1/180)*R^p_{lcd}*R^q_{abm})");
    ////        t = Tensors.parse("-64*n^d*n_q*n_p*("
    ////                + "n^b*g^ac*g^lm*(-1/30)*R^p_{cmb}*R^q_{adl}"
    ////                + "+n^c*g^ab*g^lm*(-1/180)*R^p_{lcm}*R^q_{abd}"
    ////                + "+n^b*g^ac*g^lm*(1/180)*R^p_{lcd}*R^q_{abm})");
    ////        t = Tensors.parse("4**2*3*12*1/3*(n^a*g^bc+n^b*g^ac+n^c*g^ab)*n^d*n_q*n_p/3*g^lm*(-1/10*R^p_lcm*R^q_adb+1/15*R^p_dam*R^q_blc+1/60*R^p_bdm*R^q_cla)");
    //        Expression Kn1 = Tensors.parseExpression("Kn^a=n^a");
    //        Expression Kn2 = Tensors.parseExpression("Kn^ab=1/3*(2*n^a*n^b+g^ab)");
    //        Expression Kn3 = Tensors.parseExpression("Kn^abc=1/3*(n^a*g^bc+n^b*g^ac+n^c*g^ab)");
    //        Tensor delta = Tensors.parseExpression(
    //                "DELTA^{lma}="
    //                        + "-(1/6)*L*(L-1)*(L-2)*Kn^{lma}"
    //                        + "+Power[L,2]*(L-1)*(1/3)*("
    //                        + "Kn^{l m }*Kn^{a }+"
    //                        + "Kn^{a m }*Kn^{l }+"
    //                        + "Kn^{l a }*Kn^{m })"
    //                        + "-Power[L,3]*Kn^{l }*Kn^{m }*Kn^{a }");
    //        delta = Tensors.parseExpression("L=4").transform(delta);
    //        delta = Kn1.transform(delta);
    //        delta = Kn2.transform(delta);
    //        delta = Kn3.transform(delta);
    //        delta = Expand.expand(delta, all);
    //        for (Transformation tr : all)
    //            delta = tr.transform(delta);
    //        t = Tensors.parse(
    //                //                "Power[L,2]*(L-1)"
    //                "DELTA^abc"
    //                        + "*n^d*n_q*n_p"
    //                        + "*Kn^lm"
    //                        + "*("
    //                        + "-1/10*R^p_lcm*R^q_adb"
    //                        + "+1/15*R^p_dam*R^q_blc"
    //                        + "+1/60*R^p_bdm*R^q_cla)");
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
    //        temp = new Averaging(Tensors.parseSimple("n_l")).transform(temp);
    //        temp = Expand.expand(temp, all);
    //        for (Transformation tr : all)
    //            temp = tr.transform(temp);
    //        temp = Expand.expand(temp, all);
    //
    //        Assert.assertTrue(TensorUtils.equals(temp, Tensors.parse("-19/360*R^lm*R_lm-1/80*R**2")));
    //    }
}
