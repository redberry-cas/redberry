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
package cc.redberry.core.transformations.powerexpand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.SimpleTensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.tensor.Tensors.parseSimple;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PowerExpandIntoChainTransformationTest {
    @Test
    public void test1() throws Exception {
        PowerExpandIntoChainTransformation pe = PowerExpandIntoChainTransformation.POWER_EXPAND_TRANSFORMATION;
        TAssert.assertEquals(pe.transform(parse("(a_m*b^m*c)**2")), "c**2*a_{m}*a_{a}*b^{m}*b^{a}");

        TAssert.assertEquals(pe.transform(parse("(a_m*a^m*c)**2")), "c**2*a_{m}*a_{a}*a^{m}*a^{a}");
    }

    @Test
    public void test2() {
        SimpleTensor[] vars = new SimpleTensor[]{parseSimple("A_m")};
        PowerExpandIntoChainTransformation pe = new PowerExpandIntoChainTransformation(vars);
        TAssert.assertEquals(pe.transform(parse("(A_m*A^m)**2")), "A_{m}*A_{a}*A^{m}*A^{a}");
    }

    @Test
    public void test3() {
        SimpleTensor[] vars = new SimpleTensor[]{parseSimple("A_m")};
        PowerExpandIntoChainTransformation pe = new PowerExpandIntoChainTransformation(vars);
        TAssert.assertEquals(pe.transform(parse("(A_m*A^m*c)**2")), "c**2*A_{m}*A_{a}*A^{m}*A^{a}");
    }
}
