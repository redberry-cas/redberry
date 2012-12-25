/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2012:
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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.transformations.expand.ExpandDenominator.expandDenominator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandDenominatorTest {
    @Test
    public void test1() {
        Tensor t = parse("(a+b)/(c+d)");
        TAssert.assertTrue(t == expandDenominator(t));
    }

    @Test
    public void test2() {
        Tensor a = parse("(a+b)**2/(c+d)");
        Tensor e = expandDenominator(a);
        TAssert.assertTrue(a == e);
    }

    @Test
    public void test3() {
        Tensor a = expandDenominator(parse("(a+b)**2/(c+d)**2"));
        Tensor e = parse("(a+b)**2/(c**2+2*c*d+d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test4() {
        Tensor a = expandDenominator(parse("(x+(a+b)**2)/(c+d)**2"));
        Tensor e = parse("(x+(a+b)**2)/(c**2+2*c*d+d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test5() {
        Tensor a = expandDenominator(parse("f*(x+(a+b)**2)/(c+d)**2"));
        Tensor e = parse("f*(x+(a+b)**2)/(c**2+2*c*d+d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test6() {
        Tensor a = expandDenominator(parse("f*(x+(a+b)**2)/((c+d)**2*k)"));
        Tensor e = parse("f*(x+(a+b)**2)/(k*c**2+2*k*c*d+k*d**2)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test7() {
        Tensor a = expandDenominator(parse("f*(x+(a+b)**2)/((c+d)**2*k*i)"));
        Tensor e = parse("f*(x+(a+b)**2)/(k*c**2*i+2*k*c*d*i+k*d**2*i)");
        TAssert.assertEquals(a, e);
    }
    //todo more tests
}
