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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.transformations.expand.ExpandDenominatorTransformation.expandDenominator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandDenominatorTransformationTest {

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

    @Test
    public void test8() {
        Tensor a = expandDenominator(parse("1/((a+b)*c)"));
        Tensor e = parse("1/(a*c + b*c)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test9() {
        Tensor a = expandDenominator(parse("(a+b)**2/(k_i*(a^i+b^i))**2"));
        Tensor e = parse("(a+b)**2/(k_i*a^i*k_j*a^j+2*k_j*a^j*k_i*b^i+k_i*b^i*k_j*b^j)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test10(){
        Tensor a = expandDenominator(parse("(a + b/(1 + c)**2)**(-2)"));
        Tensor e = parse("(a**2 + b**2/(1 + c)**4 + (2*a*b)/(1 + c)**2)**(-1)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test11(){
        Tensor a = expandDenominator(parse("(b*(c + d) + (e + f)/a)**(-2)"));
        Tensor e = parse("(b**2*c**2 + 2*b**2*c*d + b**2*d**2 + (2*b*c*e)/a + (2*b*d*e)/a + e**2/a**2 + (2*b*c*f)/a + (2*b*d*f)/a + (2*e*f)/a**2 + f**2/a**2)**(-1)");
        TAssert.assertEquals(a, e);
    }
}