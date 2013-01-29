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
package cc.redberry.core.parser.preprocessor;

import cc.redberry.core.TAssert;
import cc.redberry.core.context.CC;
import cc.redberry.core.indices.IndexType;
import cc.redberry.core.parser.ParseToken;
import org.junit.Test;

import static cc.redberry.core.indices.IndexType.LatinLower;
import static cc.redberry.core.indices.IndexType.LatinUpper;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ChangeIndicesTypesAndTensorNamesTest {
    @Test
    public void test1() {
        String str = "f_mn * (f^ma + k^ma)";
        ParseToken token = CC.current().getParseManager().getParser().parse(str);
        TypesAndNamesTransformer transformer = TypesAndNamesTransformer.Utils.changeType(LatinLower, LatinUpper);
        token = new ChangeIndicesTypesAndTensorNames(transformer).transform(token);
        TAssert.assertEquals(token.toTensor(), "f_{MN}*(k^{MA}+f^{MA})");
    }

    @Test
    public void test2() {
        String str = "f_mn * (f^ma + k^ma)";
        ParseToken token = CC.current().getParseManager().getParser().parse(str);
        TypesAndNamesTransformer transformer = new TypesAndNamesTransformer() {
            @Override
            public IndexType newType(IndexType oldType) {
                return oldType == LatinLower ? LatinUpper : oldType;
            }

            @Override
            public String newName(String oldName) {
                return oldName.equals("f") ? "k" : oldName;
            }
        };
        token = new ChangeIndicesTypesAndTensorNames(transformer).transform(token);
        TAssert.assertEquals(token.toTensor(), "2*k_{MN}*k^{MA}");
    }
}
