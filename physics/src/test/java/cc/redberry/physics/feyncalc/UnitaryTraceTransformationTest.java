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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.transformations.EliminateFromSymmetriesTransformation;
import cc.redberry.core.transformations.EliminateMetricsTransformation;
import cc.redberry.core.transformations.Transformation;
import cc.redberry.core.transformations.expand.ExpandTransformation;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class UnitaryTraceTransformationTest {

    @Test
    public void test1() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.Matrix1);

        Tensor t = parse("Tr[T_a*T_b]");
        t = unitaryTrace(t);
        System.out.println(t);
        TAssert.assertEquals(t, parse("g_ab/2"));
    }

    @Test
    public void test2() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.Matrix1);

        setSymmetric(parseSimple("d_abd"));
        setAntiSymmetric("f_abc");
        Tensor t = parse("Tr[T_a*T_b*T_c]");
        t = unitaryTrace(t);
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(t);
        Tensor expected = parse("d_abc/4+I/4*f_abc");
        TAssert.assertEquals(t, expected);
    }


    @Test
    public void test3() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.Matrix1);

        setSymmetric(parseSimple("d_abd"));
        addAntiSymmetry(parseSimple("f_abc"), 1, 0, 2);
        addSymmetry("f_abc", 2, 0, 1);
        Tensor t = parse("Tr[T_a*T_b*T_c*T_d]");
        Tensor t1 = parse("Tr[T_b*T_a*T_c*T_d]");

        t = unitaryTrace(t);
        t1 = unitaryTrace(t1);
        System.out.println(subtract(t, t1));

        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(t);
        Tensor expected = parse("-(I/8)*f_adx*d_bc^x + (I/8)*d_adx*f_bc^x+1/8*d_ade*d_bc^e - 1/8*d_bde*d_ac^e+1/8*d_cde*d_ab^e + 1/(4*N)*g_ad*g_bc - 1/(4*N)*g_ac*g_bd + 1/(4*N)*g_ab*g_cd");
        System.out.println(t);
        System.out.println(expected);
//        TAssert.assertEquals(t, expected);
    }

    @Test
    public void test4() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("M^A'_B'\\alpha"), IndexType.Matrix2);

        setSymmetric("e_\\alpha\\beta\\gamma");
        setAntiSymmetric("r_\\alpha\\beta\\gamma");

        Transformation trace = new UnitaryTraceTransformation(
                parseSimple("M_\\alpha^A'_B'"),
                parseSimple("e_\\alpha\\beta\\gamma"),
                parseSimple("r_\\alpha\\beta\\gamma"),
                parseSimple("D"));

        Tensor t = parse("Tr[M_\\alpha*M_\\beta*M_\\gamma]");
        t = trace.transform(t);
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(t);

        Tensor expected = parse("r_\\alpha\\beta\\gamma/4+I/4*e_\\alpha\\beta\\gamma");
        TAssert.assertEquals(t, expected);
    }

    @Test
    public void test5() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.Matrix1);

        setSymmetric(parseSimple("d_abd"));
        setAntiSymmetric("f_abc");

        Transformation trace = new UnitaryTraceTransformation(
                parseSimple("T_a"),
                parseSimple("f_abc"),
                parseSimple("d_abc"),
                parse("3"));

        Tensor t = parse("g^ab*g^cr*Tr[T_a*T_b*T_c + f_abc]*Tr[T_p*T_q*T_r - 1/12*d_pqr]");
        t = trace.transform(t);
        t = ExpandTransformation.expand(t, EliminateMetricsTransformation.ELIMINATE_METRICS);
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(t);
        TAssert.assertEquals(t, "0");
    }

    @Ignore
    @Test
    public void test6() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.Matrix1);

        setSymmetric("d_abd");
        setAntiSymmetric("f_abc");

        Transformation trace = new UnitaryTraceTransformation(
                parseSimple("T_a"),
                parseSimple("f_abc"),
                parseSimple("d_abc"),
                parse("N"));

        Tensor t = parse("Tr[T_a*T_b*T_c*T^a]");
        TAssert.assertEquals(trace.transform(t), "(N/4-1/4/N)*g_bc");
        t = parse("Tr[T_a*T_b*T^a*T_c]");
        TAssert.assertEquals(trace.transform(t), "-1/4/N*g_bc");
        t = parse("Tr[T_a*T_b*T^a*T_c]");
        TAssert.assertEquals(trace.transform(t), "-1/4/N*g_bc");
        t = parse("Tr[T_a*T_b*T_c*T^a*T^b]");
        TAssert.assertEquals(trace.transform(t), "0");
        t = parse("Tr[T_a*T_b*T_c*T_d*T^a*T^c]");
        TAssert.assertEquals(trace.transform(t), "g_bd/8/N**2");
        t = parse("Tr[T_a*T_b*T_c*T_d*T^c*T^a]");
        TAssert.assertEquals(trace.transform(t), "g_bd/8/N**2-g_bd/8");
        t = parse("Tr[T_a*T_b*T_c*T_d*T^a*T^b*T^c]");
//        System.out.println(trace.transform(t));
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(trace.transform(t));
        System.out.println(t);
        TAssert.assertEquals(trace.transform(t), "0");

    }

    @Ignore
    @Test
    public void test6a() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'a"), IndexType.Matrix1);

        setSymmetric("d_abd");
        setAntiSymmetric("f_abc");
        setAntiSymmetric("e_abc");

        Transformation trace = new UnitaryTraceTransformation(
                parseSimple("T_a"),
                parseSimple("f_abc"),
                parseSimple("d_abc"),
                parse("N"));
        Tensor t;
//        t = parse("Tr[T_c*T_d*T_b*T^b*T^c]");
//        TAssert.assertEquals(trace.transform(t), "0");
//
//        t = parse("Tr[T_f*T_c*T_d*T^a*T^b*T^c]");
//        System.out.println(trace.transform(t));
//        TAssert.assertEquals(trace.transform(t), "0");

        t = parse("Tr[T_a*T_b*T_c*T_d*T^a*T^b*T^c]");
        t = EliminateFromSymmetriesTransformation.ELIMINATE_FROM_SYMMETRIES.transform(trace.transform(t));
        t = parseExpression("f_abc = I*e_abc").transform(t);
        t = new LeviCivitaSimplifyTransformation(parseSimple("e_abc"), false).transform(t);
        t = new UnitarySimplifyTransformation(
                parseSimple("T_a"),
                parseSimple("f_abc"),
                parseSimple("d_abc"),
                parse("N")).transform(t);
        System.out.println(t);
        TAssert.assertEquals(trace.transform(t), "0");
    }

    static Tensor unitaryTrace(Tensor t) {
        return new UnitaryTraceTransformation(parseSimple("T_a^a'_b'"), parseSimple("f_abc"), parseSimple("d_abc"), parseSimple("N")).transform(t);
    }
}
