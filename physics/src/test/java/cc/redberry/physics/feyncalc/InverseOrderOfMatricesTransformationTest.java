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
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.indices.IndexType.Matrix1;
import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;
import static cc.redberry.physics.feyncalc.InverseOrderOfMatricesTransformation.inverseOrderOfMatrices;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class InverseOrderOfMatricesTransformationTest {
    @Test
    public void test1() {
        GeneralIndicesInsertion gii = new GeneralIndicesInsertion();
        gii.addInsertionRule(parseSimple("A^a'_b'"), Matrix1);
        gii.addInsertionRule(parseSimple("B^a'_b'"), Matrix1);
        gii.addInsertionRule(parseSimple("C^a'_b'"), Matrix1);
        gii.addInsertionRule(parseSimple("cv_b'"), Matrix1);
        gii.addInsertionRule(parseSimple("v^b'"), Matrix1);
        CC.current().getParseManager().defaultParserPreprocessors.add(gii);

        Tensor t, exp;
        t = parse("A*B*C");
        exp = parse("C*B*A");
        TAssert.assertEquals(inverseOrderOfMatrices(t, Matrix1), exp);

        t = parse("cv*A*B*C");
        exp = parse("cv*C*B*A");
        TAssert.assertEquals(inverseOrderOfMatrices(t, Matrix1), exp);

        t = parse("A*B*C*v");
        exp = parse("C*B*A*v");
        TAssert.assertEquals(inverseOrderOfMatrices(t, Matrix1), exp);

        t = parse("cv*A*B*C*v");
        exp = parse("cv*C*B*A*v");
        TAssert.assertEquals(inverseOrderOfMatrices(t, Matrix1), exp);
    }
}
