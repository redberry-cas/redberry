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
 * Created by poslavsky on 04/08/15.
 */
public class SimplifyGamma5TransformationTest {
    @Test
    public void test1() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);

        SimplifyGamma5Transformation tr = new SimplifyGamma5Transformation(
                parseSimple("G_a"), parseSimple("G5"));

        Tensor t;
        t = parse("G_a*G5");
        assertTrue(t == tr.transform(t));

        t = parse("G5*G_a");
        assertEquals("-G_a*G5", tr.transform(t));

        t = parse("G5*G_a*G5");
        assertEquals("-G_a", tr.transform(t));

        t = parse("G5*G5*G5");
        assertEquals("G5", tr.transform(t));

        t = parse("G5*G_a*G_b*G5");
        assertEquals("G_a*G_b", tr.transform(t));

        t = parse("G5*G_a*G5*G_b*G5");
        assertEquals("-G_a*G_b*G5", tr.transform(t));

        t = parse("G5*G5*G_a*G5*G_b*G5");
        assertEquals("-G_a*G_b", tr.transform(t));

        t = parse("G_a*G5*G_b*G5*G5*G5");
        assertEquals("-G_a*G_b", tr.transform(t));
    }

    @Test
    public void test2() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("A^a'_b'"), IndexType.Matrix1);

        SimplifyGamma5Transformation tr = new SimplifyGamma5Transformation(
                parseSimple("G_a"), parseSimple("G5"));

        Tensor t;
        t = parse("G5*A*G_a");
        assertEquals("G5*A*G_a", tr.transform(t));

        t = parse("G5*G_a*G5*A*G5");
        assertEquals("-G_a*A*G5", tr.transform(t));

        t = parse("G5*G5*G5*A*G5*G_d*G_c");
        assertEquals("G5*A*G_d*G_c*G5", tr.transform(t));

        t = parse("Tr[G5*G_a*G_b*G5]");
        assertEquals("Tr[G_a*G_b]", tr.transform(t));

        t = parse("2*k^a*p^c*q^d*G5*G_a*G5*A*G5*G_d*G_c");
        assertEquals("-2*k^a*p^c*q^d*G_a*A*G_d*G_c*G5", tr.transform(t));
    }
}