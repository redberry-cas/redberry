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
package cc.redberry.physics.feyncalc;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.Tensor;
import org.junit.Before;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * Created by poslavsky on 07/08/15.
 */
public class SpinorsSimplifyTransformationTest {
    @Before
    public void setUp() throws Exception {
        CC.reset();
    }

    @Test
    public void test1() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cu_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("u^b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cv_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^b'"), IndexType.Matrix1);

        SpinorsSimplifyTransformation sp = new SpinorsSimplifyTransformation(parseSimple("G_a"),
                parseSimple("u"), parseSimple("v"),
                parseSimple("cu"), parseSimple("cv"),
                parseSimple("p_a"), parseSimple("m"));

        Tensor t;
        t = parse("cu*G_a*p^a");
        TAssert.assertEquals("m*cu", sp.transform(t));

        t = parse("cu*G_b*G_a*p^a");
        TAssert.assertEquals("2*cu*p_{b}-m*cu*G_{b}", sp.transform(t));

        t = parse("2*t_s*cu*G_a*p^a");
        TAssert.assertEquals("2*t_s*m*cu", sp.transform(t));

        t = parse("G_a*p^a*u");
        TAssert.assertEquals("m*u", sp.transform(t));

        t = parse("G_b*G_a*p^a*u");
        TAssert.assertEquals("G_b*m*u", sp.transform(t));

        t = parse("G_a*G_b*p^a*u");
        TAssert.assertEquals("2*p_b*u-G_b*m*u", sp.transform(t));

        t = parse("G_a*G_b*p^a*v");
        TAssert.assertEquals("2*p_b*v+G_b*m*v", sp.transform(t));

        t = parse("k^b*G_a*G_b*p^a*u");
        TAssert.assertEquals("2*k^b*p_b*u-k^b*G_b*m*u", sp.transform(t));

        t = parse("p^a*G_a*G_b*G_c*u");
        TAssert.assertEquals("m*G_b*G_c*u+2*G_c*u*p_b-2*G_b*u*p_c", sp.transform(t));

        t = parse("p^a*G_a*G_b*G_c*v");
        TAssert.assertEquals("-m*G_b*G_c*v+2*G_c*v*p_b-2*G_b*v*p_c", sp.transform(t));

        t = parse("cu*p^a*G_a*G_b*G_c*v");
        TAssert.assertEquals("m*cu*G_b*G_c*v", sp.transform(t));
    }


    @Test
    public void test1_a() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cu_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("u^b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cv_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^b'"), IndexType.Matrix1);

        SpinorsSimplifyTransformation sp = new SpinorsSimplifyTransformation(parseSimple("G_a"),
                null, parseSimple("v"),
                null, null,
                parseSimple("p_a"), parseSimple("m"));

        Tensor t;
        t = parse("cu*p^a*G_a*G_b*v");
        TAssert.assertEquals("2*cu*v*p_{b}+m*cu*G_{b}*v", sp.transform(t));
    }

    @Test
    public void test2() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cu_b'[p_a]"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("u^b'[p_a]"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cv_b'[p_a]"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^b'[p_a]"), IndexType.Matrix1);

        SpinorsSimplifyTransformation sp = new SpinorsSimplifyTransformation(parseSimple("G_a"),
                parseSimple("u[p_a]"), parseSimple("v[p_a]"),
                parseSimple("cu[p_a]"), parseSimple("cv[p_a]"),
                parseSimple("p_a"), parseSimple("m"));

        Tensor t;
        t = parse("cu[p_a]*G_a*p^a");
        TAssert.assertEquals("m*cu[p_a]", sp.transform(t));

        t = parse("cu[k_a]*G_a*p^a");
        TAssert.assertTrue(t == sp.transform(t));

        t = parse("cu[p_a]*G_a*p^a*G_b*p^b*u[p_a]");
        TAssert.assertEquals("m**2*cu[p_{a}]*u[p_{a}]", sp.transform(t));

        t = parse("cu[p1_a]*G_a*p^a*G_b*p^b*u[p1_a]");
        TAssert.assertTrue(t == sp.transform(t));

        t = parse("2*p_i*p_j*Tr[G_p*G^q]*cu[p_a]*G_c*G_a*p^a*G_b*p^b*G^c*u[p_a]");
        TAssert.assertEquals("2*p_i*p_j*Tr[G_p*G^q]*4*m**2*cu[p_{a}]*u[p_{a}]", sp.transform(t));

        t = parse("2*p_i*p_j*Tr[G^i*G^j]*cu[p_a]*G_c*G_a*p^a*G_b*p^b*G^c*u[p_a]");
        TAssert.assertEquals("2*m**2*4*4*m**2*cu[p_{a}]*u[p_{a}]", sp.transform(t));
    }

    @Test
    public void test3() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("T^A'_B'"), IndexType.Matrix2);

        indicesInsertion.addInsertionRule(parseSimple("cu_{a'A'}[p1_{m}[charm]]"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cu_{a'A'}[p1_{m}[charm]]"), IndexType.Matrix2);

        indicesInsertion.addInsertionRule(parseSimple("v^b'B'[p2_a[charm]]"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^b'B'[p2_a[charm]]"), IndexType.Matrix2);


        SpinorsSimplifyTransformation sp2 = new SpinorsSimplifyTransformation(parseSimple("G_a"),
                null, null, parseSimple("cu[p1_a[charm]]"), null,
                parseSimple("p1_a[charm]"), parseSimple("mc"));

        Tensor t = parse("p1^{a}[charm]*p1^{e}[charm]*v^{b'A'}[p2_{m}[charm]]*G_{a}^{e'}_{b'}*G_{b}^{a'}_{e'}*cu_{a'A'}[p1_{m}[charm]]*k2^{g}*e^{b}_{kge}*k1^{k}");
        sp2.transform(t);
        TAssert.assertTrue(true);
    }

    @Test
    public void test4() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cu_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("u^b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cv_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^b'"), IndexType.Matrix1);

        SpinorsSimplifyTransformation sp = new SpinorsSimplifyTransformation(parseSimple("G_a"),
                parseSimple("u"), parseSimple("v"),
                parseSimple("cu"), parseSimple("cv"),
                parseSimple("p_a"), parseSimple("m"));

        Tensor t;
        t = parse("cu*v");
        TAssert.assertEquals("0", sp.transform(t));

        t = parse("cu*G_a*p^a*v");
        TAssert.assertEquals("0", sp.transform(t));

        t = parse("cu*G_a*p^a*G_b*p^b*v");
        TAssert.assertEquals("0", sp.transform(t));
    }


}