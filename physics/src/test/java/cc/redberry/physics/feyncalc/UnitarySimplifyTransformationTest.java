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
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.preprocessor.GeneralIndicesInsertion;
import org.junit.Test;

import static cc.redberry.core.indices.IndexType.Matrix2;
import static cc.redberry.core.tensor.Tensors.*;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class UnitarySimplifyTransformationTest {
    @Test
    public void test1() {
        GeneralIndicesInsertion indicesInsertion = new GeneralIndicesInsertion();
        CC.current().getParseManager().defaultParserPreprocessors.add(indicesInsertion);
        indicesInsertion.addInsertionRule(parseSimple("T^a'_b'A"), IndexType.Matrix1);

        setAntiSymmetric("f_ABC");
        setSymmetric("d_ABC");

        UnitarySimplifyTransformation tr = new UnitarySimplifyTransformation(parseSimple("T_A"),
                parseSimple("f_ABC"), parseSimple("d_ABC"), parseSimple("n"));

        System.out.println(tr.transform(parse("T_A*T^A")));

    }
}
