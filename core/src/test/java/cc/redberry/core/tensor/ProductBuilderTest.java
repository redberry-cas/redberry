/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2014:
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
package cc.redberry.core.tensor;

import cc.redberry.core.TAssert;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ProductBuilderTest {
    @Test
    public void testRationalPowers1() {
        Tensor a = parse("a/(-a)**(1/2)");
        TAssert.assertEquals(a, "-(-a)**(1/2)");

        a = parse("(a-b)/(b-a)**(1/2)");
        TAssert.assertEquals(a, "-(b-a)**(1/2)");
    }


    @Test
    public void testPower1() {
        Tensor t = parse("(a+b)**(3/2) - (a+b)*(a+b)**(1/2)");
        TAssert.assertEquals(t, "0");
    }

}
