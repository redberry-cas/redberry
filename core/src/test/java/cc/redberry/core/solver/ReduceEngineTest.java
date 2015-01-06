/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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
package cc.redberry.core.solver;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.number.Complex;
import cc.redberry.core.parser.ParserIndices;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.tensorgenerator.GeneratedTensor;
import cc.redberry.core.tensorgenerator.TensorGenerator;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.factor.FactorTransformation;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.transformations.symmetrization.SymmetrizeUpperLowerIndicesTransformation;
import cc.redberry.core.utils.THashMap;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertFalse;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ReduceEngineTest {
    public final String mapleBinDir;
    public final String temporaryDir;
    public final String mathematicaBinDir;

    public ReduceEngineTest() {
        String mapleBinDir;
        mapleBinDir = "/home/stas/maple13/bin";
        if (!new File(mapleBinDir + "/maple").exists()) {
            mapleBinDir = System.getenv("MAPLE");
            if (mapleBinDir == null)
                mapleBinDir = System.getProperty("redberry.maple");
            if (mapleBinDir != null) {
                //check maple
                File maple = new File(mapleBinDir + "/maple");
                if (!maple.exists())
                    mapleBinDir = null;
                else {
                    //todo check licence
                }
            }
        }

        if (mapleBinDir != null)
            System.out.println("MAPLE directory: " + mapleBinDir);
        this.mapleBinDir = mapleBinDir;

        String mathematicaBinDir = "/usr/local/bin";
        File mathematicaScriptExecutor = new File(mathematicaBinDir + "/MathematicaScript");
        if (!mathematicaScriptExecutor.exists())
            mathematicaBinDir = null;
        else
            System.out.println("Mathematica script executor:" + mathematicaScriptExecutor.getAbsolutePath());

        this.mathematicaBinDir = mathematicaBinDir;
        temporaryDir = System.getProperty("java.io.tmpdir");
    }

    @Before
    public void beforeMethod() {
        Assume.assumeTrue(mapleBinDir != null || mathematicaBinDir != null);
    }

    @Test
    public void test1() throws Exception {
        Expression[] eqs = {parseExpression("(a*g_mn + b*k_m*k_n)*iF^ma = d_n^a")};
        SimpleTensor[] vars = {parseSimple("iF_ab")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(eqs, vars, new Transformation[0]);
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
            assertSolution(eqs, solution);
        }
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            System.out.println(Arrays.toString(solution));
            assertSolution(eqs, solution);
        }
    }

    @Test
    public void test2() throws Exception {

        Expression[] equations = {
                Tensors.expression(parse("(d_p^a*d_q^b*d_r^c+"
                        + "6*(-1/2+2*b**2)*g_pq*g^ab*d_r^c+"
                        + "3*(-1+2)*n_p*n^a*d_q^b*d_r^c+"
                        + "6*(1/2+2*b)*(n_p*n_q*g^ab*d_r^c+n^a*n^b*g_pq*d_r^c)+"
                        + "6*(-1/4+2*b**2)*n_p*g_qr*n^a*g^bc)*iK^pqr_ijk"),

                        SymmetrizeUpperLowerIndicesTransformation.symmetrizeUpperLowerIndices(parse("d_i^a*d_j^b*d_k^c"), true))
        };

        SimpleTensor[] vars = {parseSimple("iK^pqr_ijk")};
        Transformation[] transformations = {parseExpression("n_a*n^a = 1"), parseExpression("d_a^a = 4")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
    }

    @Test
    public void test4() throws Exception {
        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");
        Expression equation = (Expression) toInverse.transform(
                parse("F_p^mn_q^rs*iF^p_mn^a_bc=d^a_q*d_b^r*d_c^s-1/4*d^r_q*d_b^a*d_c^s"));

        Expression[] equations = {equation};

        SimpleTensor[] vars = {parseSimple("iF^pqr_ijk")};
        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
    }

    @Test
    public void test5() throws Exception {

        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");

        Expression[] equations = {
                parseExpression("F_p^mn_q^rs*iF^p_mn^a_bc + F_p^mn_q^rs*iiF^p_mn^a_bc = 2*d^a_q*d_b^r*d_c^s - 1/2*d^r_q*d_b^a*d_c^s"),
                parseExpression("iF^p_mn^a_bc = iiF^p_mn^a_bc")
        };

        for (int i = 0; i < equations.length; ++i)
            equations[i] = (Expression) toInverse.transform(equations[i]);

        SimpleTensor[] vars = {parseSimple("iF^pqr_ijk"), parseSimple("iiF^pqr_ijk")};
        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            TAssert.assertEquals(solution[0][0].get(1), solution[0][1].get(1));
            assertSolution(equations, solution, transformations);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
            TAssert.assertEquals(solution[0][0].get(1), solution[0][1].get(1));
            assertSolution(equations, solution, transformations);
        }
    }

    @Test
    public void test6() throws Exception {
        Expression[] equations = {
                parseExpression("x + y = 1"),
                parseExpression("x**2 - y = -1")
        };

        SimpleTensor[] vars = {parseSimple("x"), parseSimple("y")};

        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
    }

    @Test
    public void test7() throws Exception {
        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");
        Expression equation = (Expression) toInverse.transform(
                parse("F_p^mn_q^rs*iF^p_mn^a_bc=d^a_q*d_b^r*d_c^s - x*d^r_q*d_b^a*d_c^s"));

        Expression[] equations = {equation};

        SimpleTensor[] vars = {parseSimple("iF^pqr_ijk"), parseSimple("x")};
        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
    }


    @Test
    public void test4a() throws Exception {
        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");
        Expression equation = (Expression) toInverse.transform(
                parse("F_p^mn_q^rs*iF^p_mn^a_bc=d^a_q*d_b^r*d_c^s-2/4*d^r_q*d_b^a*d_c^s"));

        Expression[] equations = {equation};

        SimpleTensor[] vars = {parseSimple("iF^pqr_ijk")};
        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
            TAssert.assertTrue(solution.length == 0);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
            TAssert.assertTrue(solution.length == 0);
        }
    }

    @Test
    public void test8() throws Exception {
        Expression[] equations = {
                parseExpression("x + y = 1"),
        };

        SimpleTensor[] vars = {parseSimple("x"), parseSimple("y")};

        Transformation[] transformations = {};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, true, mathematicaBinDir, temporaryDir);
            assertFalse(solution[0][0].get(1) instanceof Complex);
            assertFalse(solution[0][1].get(1) instanceof Complex);
            assertSolution(equations, solution, transformations);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, true, mapleBinDir, temporaryDir);
            assertFalse(solution[0][0].get(1) instanceof Complex);
            assertFalse(solution[0][1].get(1) instanceof Complex);
            assertSolution(equations, solution, transformations);
        }
    }

    @Test
    public void test9() throws Exception {
        Expression[] equations = {
                parseExpression("F_mn + F_nm + A_mn = - A_nm"),
                parseExpression("1/2*F_mn + F_nm + A_mn + 1/2*A_nm = 0")
        };

        SimpleTensor[] vars = {parseSimple("F_mn")};

        Transformation[] transformations = {};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, vars, transformations);
        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, true, mathematicaBinDir, temporaryDir);
            assertEquals(solution[0][0], "F_{mn} = -A_{nm}");
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, true, mapleBinDir, temporaryDir);
            assertEquals(solution[0][0], "F_{mn} = -A_{nm}");
        }
    }

    @Test
    public void test10() throws Exception {
        GeneratedTensor genTensor = TensorGenerator.generateStructure(ParserIndices.parseSimple("_abc"), new Tensor[]{parse("g_mn"), parse("k_m")}, true, true, true);

        Expression[] equations = new Expression[]{
                parseExpression("F_mnp*F^mnp = 1"), expression(parse("F_abc"), genTensor.generatedTensor)
        };
        Transformation[] transformations = {parseExpression("d_m^m = dim"), parseExpression("k_m*k^m = 1")};
        ReducedSystem rd = ReduceEngine.reduceToSymbolicSystem(equations, genTensor.coefficients, transformations);

        if (mathematicaBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMathematica(rd, true, mathematicaBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
        if (mapleBinDir != null) {
            Expression[][] solution = ExternalSolver.solveSystemWithMaple(rd, true, mapleBinDir, temporaryDir);
            assertSolution(equations, solution, transformations);
        }
    }

    private static void assertSolution(Expression[] equations, Expression[][] allsolutions, Transformation... transformations) {
        for (Expression[] solutions : allsolutions)
            for (Tensor equation : equations) {
                for (Expression solution : solutions)
                    equation = solution.transform(equation);
                equation = ExpandTransformation.expand(equation, EliminateMetricsTransformation.ELIMINATE_METRICS);
                equation = EliminateMetricsTransformation.eliminate(equation);
                equation = new TransformationCollection(transformations).transform(equation);
                equation = replaceScalars(equation);
                equation = FactorTransformation.factor(equation);
                TAssert.assertTrue(((Expression) equation).isIdentity());
            }
    }


    private static Tensor replaceScalars(Tensor tensor) {
            /*in order to process equations with Maple we must to replace all tensors
        with indices (they are found only in scalar combinations) with some symbols*/

        //scalar tensor <-> symbol
        THashMap<Tensor, Tensor> tensorSubstitutions = new THashMap<>();
        //all symbols will have names scalar1,scalar2, etc.

        //processing equations
        int i;
        //iterating over the whole equation
        FromChildToParentIterator iterator = new FromChildToParentIterator(tensor);
        Tensor t;
        while ((t = iterator.next()) != null) {
            if (t instanceof SimpleTensor && t.getIndices().size() != 0 && t.getIndices().getFree().size() == 0) {
                if (!tensorSubstitutions.containsKey(t)) {
                    //map does not contains rule for current scalar (e.g. k_{i}*k^{i})
                    //adding new rule for the scalar, e.g. k_{i}*k^{i} = scalar2
                    tensorSubstitutions.put(t, CC.generateNewSymbol());
                }
            }
            if (t instanceof Product) {
                //scalars content
                Tensor[] scalars = ((Product) t).getContent().getScalars();
                for (Tensor scalar : scalars) {
                    if (!tensorSubstitutions.containsKey(scalar)) {
                        //map does not contains rule for current scalar (e.g. k_{i}*k^{i})
                        //adding new rule for the scalar, e.g. k_{i}*k^{i} = scalar2
                        tensorSubstitutions.put(scalar, CC.generateNewSymbol());
                    }
                }
            }
        }


        final Expression[] scalarSubs = new Expression[tensorSubstitutions.size()];
        i = -1;
        for (Map.Entry<Tensor, Tensor> entry : tensorSubstitutions.entrySet())
            scalarSubs[++i] = Tensors.expression(entry.getKey(), entry.getValue());
        SubstitutionTransformation fullSub = new SubstitutionTransformation(scalarSubs, true);
        return fullSub.transform(tensor);
    }
}
