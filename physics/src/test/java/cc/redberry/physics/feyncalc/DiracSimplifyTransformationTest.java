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
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * Created by poslavsky on 07/08/15.
 */
public class DiracSimplifyTransformationTest {
    @Test
    public void test1() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);


        DiracSimplifyTransformation ds = new DiracSimplifyTransformation(parseSimple("G_a"), parseSimple("G5"));

        Tensor t;
        t = parse("G_a*G_b*G^a");
        TAssert.assertEquals("-2*G_b", ds.transform(t));

        t = parse("G5*G_a*G_b");
        TAssert.assertEquals("G_a*G_b*G5", ds.transform(t));

        t = parse("G5*G_a*G_b*G_c*G_d*p^a*p^d*G^c*G5*G^b");
        TAssert.assertEquals("-4*p^{d}*p_{d}", ds.transform(t));
    }

    @Test
    public void test2() throws Exception {
        CC.setDefaultOutputFormat(OutputFormat.SimpleRedberry);
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("G^a'_b'a"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("G5^a'_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cu_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("u^b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("cv_b'"), IndexType.Matrix1);
        indicesInsertion.addInsertionRule(parseSimple("v^b'"), IndexType.Matrix1);

        DiracSimplifyTransformation ds = new DiracSimplifyTransformation(parseSimple("G_a"), parseSimple("G5"));

        Tensor t;
        t = parse("cu*G_a*G_b*G^a*u");
        TAssert.assertEquals("-2*cu*G_b*u", ds.transform(t));
    }
}