/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2016:
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
package cc.redberry.core.transformations.factor;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import cc.redberry.core.tensor.Tensors;
import org.junit.Test;

import static cc.redberry.core.transformations.expand.ExpandTransformation.expand;
import static cc.redberry.core.transformations.factor.FactorOutNumber.FACTOR_OUT_NUMBER;

/**
 * Created by poslavsky on 03/03/16.
 */
public class FactorOutNumberTest {
    @Test
    public void test1() throws Exception {
        Tensor t = Tensors.parse("2*a - 2*I*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*a - 2*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*a + 2*I*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*I*a - 2*I*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*I*a - 2*I*b + 3*a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*I*a - 2*I*b + a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*I*a - 22*I*b + 122*a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("1222*I*a - 1222*I*b + 122*a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("1223*I*a - 1225*I*b + 127*a");
        TAssert.assertTrue(t == FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("1223*I/19*a - 1225*I/17*b + 127/17*a");
        TAssert.assertTrue(t == FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("12*31*I*a - 12*37*b + 12*39*v");
        TAssert.assertEquals("12*(31*I*a - 37*b + 39*v)", FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("12*31*I*a - 12*I*37*b + 12*I*39*v");
        TAssert.assertEquals("12*I*(31*a - 37*b + 39*v)", FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("2*(12*a+12*b) + 24*c");
        TAssert.assertEquals("24*(a+b+c)", FACTOR_OUT_NUMBER.transform(t));
    }


    @Test
    public void test2() throws Exception {
        Tensor t = Tensors.parse("2/13*a - 2/13*I*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2/13*a - 2/13*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2*a/13 + 2*I*b/13");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2/13*I*a - 2/13*I*b");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2/13*I*a - 2/13*I*b + 3/13*a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2/13*I*a - 2/13*I*b + a/13");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("2/13*I*a - 22/13*I*b + 122/13*a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("1222/13*I*a - 1222/13*I*b + 122/13*a");
        TAssert.assertEquals(t, expand(FACTOR_OUT_NUMBER.transform(t)));

        t = Tensors.parse("1223/13*I*a - 1225/13*I*b + 127/13*a");
        TAssert.assertTrue(t == FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("1223*I/19*a - 1225*I/17*b + 127/17*a");
        TAssert.assertTrue(t == FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("12*31*I/13*a - 12*37/13*b + 12*40/13*v");
        TAssert.assertEquals("12/13*(31*I*a - 37*b + 40*v)", FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("12*31/13*I*a/13 - 12*I*37*b/13 + 12*I*41*v/13");
        TAssert.assertEquals("12*I*(31/13*a - 37*b + 41*v)/13", FACTOR_OUT_NUMBER.transform(t));

        t = Tensors.parse("2*(12*a+12*b/13) + 24*c");
        TAssert.assertEquals("24*(a+b/13+c)", FACTOR_OUT_NUMBER.transform(t));
    }

    @Test
    public void test3() throws Exception {
        Tensor t = Tensors.parse("-I*a - I*b");
        TAssert.assertEquals("-I*(a+b)", FACTOR_OUT_NUMBER.transform(t));
    }
}