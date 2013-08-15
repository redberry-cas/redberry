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
package cc.redberry.physics.utils.solver;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.tensor.*;
import cc.redberry.core.tensor.iterator.FromChildToParentIterator;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.TransformationCollection;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import cc.redberry.core.transformations.factor.FactorTransformation;
import cc.redberry.core.transformations.substitutions.SubstitutionTransformation;
import cc.redberry.core.transformations.symmetrization.SymmetrizeUpperLowerIndicesTransformation;
import cc.redberry.core.utils.THashMap;
import org.junit.Test;

import java.util.Map;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class LinearSolverTest {
    @Test
    public void test1() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mapleBinDir = "/home/stas/maple13/bin";

        Expression[] eqs = {parseExpression("(a*g_mn + b*k_m*k_n)*iF^ma = d_n^a")};
        SimpleTensor[] vars = {parseSimple("iF_ab")};
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(eqs, vars, new Transformation[0]);
        Expression[] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
        assertSolution(eqs, solution);
    }

    @Test
    public void test2() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mapleBinDir = "/home/stas/maple13/bin";

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
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(equations, vars, transformations);
        Expression[] solution = ExternalSolver.solveSystemWithMaple(rd, false, mapleBinDir, temporaryDir);
        assertSolution(equations, solution, transformations);
    }

    @Test
    public void test3() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mathematicaBinDir = "/usr/local/bin";

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
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(equations, vars, transformations);
        Expression[] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
        assertSolution(equations, solution, transformations);
    }

    @Test
    public void test4() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mathematicaBinDir = "/usr/local/bin";

        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");
        Expression equation = (Expression) toInverse.transform(
                parse("F_p^mn_q^rs*iF^p_mn^a_bc=d^a_q*d_b^r*d_c^s-1/4*d^r_q*d_b^a*d_c^s"));

        Expression[] equations = {equation};

        SimpleTensor[] vars = {parseSimple("iF^pqr_ijk")};
        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(equations, vars, transformations);
        Expression[] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
        assertSolution(equations, solution, transformations);
    }

    @Test
    public void test5() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mathematicaBinDir = "/usr/local/bin";

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
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(equations, vars, transformations);
        Expression[] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
        TAssert.assertEquals(solution[0].get(1), solution[1].get(1));
        assertSolution(equations, solution, transformations);
    }


    @Test
    public void test6() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mathematicaBinDir = "/usr/local/bin";


        Expression[] equations = {
                parseExpression("x + y = 1"),
                parseExpression("x**2 - y = -1")
        };

        SimpleTensor[] vars = {parseSimple("x"), parseSimple("y")};

        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(equations, vars, transformations);
        Expression[] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
        assertSolution(equations, solution, transformations);
    }

    @Test
    public void test7() throws Exception {
        String temporaryDir = System.getProperty("java.io.tmpdir");
        String mathematicaBinDir = "/usr/local/bin";

        Expression toInverse =
                Tensors.parseExpression("F_p^mn_q^rs = "
                        + "d^s_q*d^r_p*g^mn+d^m_q*d^n_p*g^rs+(-1)*d^r_p*d^n_q*g^ms+(-1)*d^s_p*d^m_q*g^rn");
        Expression equation = (Expression) toInverse.transform(
                parse("F_p^mn_q^rs*iF^p_mn^a_bc=d^a_q*d_b^r*d_c^s - x*d^r_q*d_b^a*d_c^s"));

        Expression[] equations = {equation};

        SimpleTensor[] vars = {parseSimple("iF^pqr_ijk"), parseSimple("x")};
        Transformation[] transformations = {parseExpression("d_a^a = 4")};
        ReducedSystem rd = LinearSolver.reduceToSymbolicSystem(equations, vars, transformations);
        Expression[] solution = ExternalSolver.solveSystemWithMathematica(rd, false, mathematicaBinDir, temporaryDir);
        assertSolution(equations, solution, transformations);
    }

    private static void assertSolution(Expression[] equations, Expression[] solutions, Transformation... transformations) {
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
