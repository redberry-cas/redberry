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
package cc.redberry.core.transformations.expand;

import cc.redberry.core.TAssert;
import cc.redberry.core.tensor.Tensor;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;
import static cc.redberry.core.transformations.expand.ExpandNumeratorTransformation.expandNumerator;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ExpandNumeratorTransformationTest {
    @Test
    public void test1() {
        Tensor t = parse("(a+b)/(c+d)");
        TAssert.assertTrue(t == expandNumerator(t));
    }

    @Test
    public void test2() {
        Tensor a = expandNumerator(parse("(a+b)**2/(c+d)"));
        Tensor e = parse("(a**2+2*a*b+b**2)/(c+d)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test3() {
        Tensor a = expandNumerator(parse("((a+b)**2 + x)/(c+d)"));
        Tensor e = parse("(a**2+2*a*b+b**2+x)/(c+d)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test4() {
        Tensor t = parse("((a+b)**2 + x)*(a+b)/(c+d)");
        Tensor a = expandNumerator(t);
        Tensor e = parse("(a**3+3*a**2*b+3*a*b**2+a*x+b**3+b*x)/(c+d)");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test5() {
        Tensor t = parse("(a+b)**2/x+(c+d)**2/y");
        Tensor a = expandNumerator(t);
        Tensor e = parse("(a**2+2*a*b+b**2)/x+(c**2+2*c*d+d**2)/y");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test6() {
        Tensor t = parse("f_m*(a+b)**2/x+f_m*(c+d)**2/y");
        Tensor a = expandNumerator(t);
        Tensor e = parse("(f_m*a**2+f_m*2*a*b+f_m*b**2)/x+(f_m*c**2+f_m*2*c*d+f_m*d**2)/y");
        TAssert.assertEquals(a, e);
    }

    @Test
    public void test7() {
        Tensor t = parse("((a+b)*f_mn+(c+d*(a+b))*(g_mn+l_mn))*(a+b)/a");
        Tensor a = expandNumerator(t);
        Tensor e = parse("a**(-1)*((c*b+c*a+b**2*d+a**2*d+2*d*b*a)*g_{mn}+(2*b*a+a**2+b**2)*f_{mn}+(c*b+c*a+b**2*d+a**2*d+2*d*b*a)*l_{mn})");
        TAssert.assertEquals(a, e);
    }
    //todo more tests
}
