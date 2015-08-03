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


import cc.redberry.core.context.CC;
import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.TAssert.assertEquals;
import static cc.redberry.core.TAssert.assertTrue;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * Created by poslavsky on 03/08/15.
 */
public class DiracOrderTransformationTest {
    @Test
    public void test1() throws Exception {
//        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);

        DiracOrderTransformation order = new DiracOrderTransformation(parseSimple("G_a"));

        Tensor t;
        t = parse("G_a*G_b");
        assertTrue(t == order.transform(t));

        t = parse("G_a*G^a");
        assertTrue(t == order.transform(t));

        t = parse("G_b*G_a");
        assertEquals("2*g_{ba}-G_{a}*G_{b}", order.transform(t));

        t = parse("G_b*G^a");
        assertEquals("2*d_b^a-G^{a}*G_{b}", order.transform(t));

        t = parse("G^b*G_a");
        assertEquals("2*g^b_a-G_{a}*G^{b}", order.transform(t));

        order = new DiracOrderTransformation(parseSimple("G_a"));

        t = parse("G^b*G_a");
        assertEquals("2*g^b_a-G_{a}*G^{b}", order.transform(t));

        t = parse("G_c*G_b*G_a");
        assertEquals("-G_{a}*G_{b}*G_{c}+2*g_{bc}*G_{a}-2*g_{ac}*G_{b}+2*g_{ab}*G_{c}",
                order.transform(t));
    }

    @Test
    public void test2() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);

        DiracOrderTransformation order = new DiracOrderTransformation(parseSimple("G_a"));
        Tensor t;
        t = parse("G_d*G_c*G_b*G_a");
        assertEquals("G_{a}*G_{b}*G_{c}*G_{d}-2*G_{c}*G_{d}*g_{ab}+2*G_{b}*G_{d}*g_{ac}-2*G_{b}*G_{c}*g_{ad}-2*G_{a}*G_{d}*g_{bc}+4*g_{ad}*g_{bc}+2*G_{a}*G_{c}*g_{bd}-4*g_{ac}*g_{bd}-2*G_{a}*G_{b}*g_{cd}+4*g_{ab}*g_{cd}",
                order.transform(t));
    }

    @Test
    public void test3() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);

        DiracOrderTransformation order = new DiracOrderTransformation(parseSimple("G_a"));
        Tensor t;
        t = parse("G_e*G_d*G_c*G_b*G_a");
        assertEquals("G_{a}*G_{b}*G_{c}*G_{d}*G_{e}-2*G_{c}*G_{d}*G_{e}*g_{ab}+2*G_{b}*G_{d}*G_{e}*g_{ac}-2*G_{b}*G_{c}*G_{e}*g_{ad}+2*G_{b}*G_{c}*G_{d}*g_{ae}-2*G_{a}*G_{d}*G_{e}*g_{bc}+2*G_{a}*G_{c}*G_{e}*g_{bd}-2*G_{a}*G_{c}*G_{d}*g_{be}-2*G_{a}*G_{b}*G_{e}*g_{cd}+2*G_{a}*G_{b}*G_{d}*g_{ce}-2*G_{a}*G_{b}*G_{c}*g_{de}+4*g_{be}*g_{cd}*G_{a}-4*g_{bd}*g_{ce}*G_{a}+4*g_{bc}*g_{de}*G_{a}-4*g_{ae}*g_{cd}*G_{b}+4*g_{ad}*g_{ce}*G_{b}-4*g_{ac}*g_{de}*G_{b}+4*g_{ae}*g_{bd}*G_{c}-4*g_{ad}*g_{be}*G_{c}+4*g_{ab}*g_{de}*G_{c}-4*g_{ae}*g_{bc}*G_{d}+4*g_{ac}*g_{be}*G_{d}-4*g_{ab}*g_{ce}*G_{d}+4*g_{ad}*g_{bc}*G_{e}-4*g_{ac}*g_{bd}*G_{e}+4*g_{ab}*g_{cd}*G_{e}",
                order.transform(t));
    }

    @Test
    public void test4() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);

        DiracOrderTransformation order = new DiracOrderTransformation(parseSimple("G_a"));
        Tensor t;
        t = parse("k^a*k^b*G_b*G_a");
        System.out.println(order.transform(t));
//        assertEquals("G_{a}*G_{b}*G_{c}*G_{d}*G_{e}-2*G_{c}*G_{d}*G_{e}*g_{ab}+2*G_{b}*G_{d}*G_{e}*g_{ac}-2*G_{b}*G_{c}*G_{e}*g_{ad}+2*G_{b}*G_{c}*G_{d}*g_{ae}-2*G_{a}*G_{d}*G_{e}*g_{bc}+2*G_{a}*G_{c}*G_{e}*g_{bd}-2*G_{a}*G_{c}*G_{d}*g_{be}-2*G_{a}*G_{b}*G_{e}*g_{cd}+2*G_{a}*G_{b}*G_{d}*g_{ce}-2*G_{a}*G_{b}*G_{c}*g_{de}+4*g_{be}*g_{cd}*G_{a}-4*g_{bd}*g_{ce}*G_{a}+4*g_{bc}*g_{de}*G_{a}-4*g_{ae}*g_{cd}*G_{b}+4*g_{ad}*g_{ce}*G_{b}-4*g_{ac}*g_{de}*G_{b}+4*g_{ae}*g_{bd}*G_{c}-4*g_{ad}*g_{be}*G_{c}+4*g_{ab}*g_{de}*G_{c}-4*g_{ae}*g_{bc}*G_{d}+4*g_{ac}*g_{be}*G_{d}-4*g_{ab}*g_{ce}*G_{d}+4*g_{ad}*g_{bc}*G_{e}-4*g_{ac}*g_{bd}*G_{e}+4*g_{ab}*g_{cd}*G_{e}",
//                EliminateMetricsTransformation.eliminate(order.transform(t)));
    }
}