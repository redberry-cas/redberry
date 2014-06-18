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
import cc.redberry.core.context.CC;
import cc.redberry.core.number.Complex;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.parse;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class FastTensorsTest {
    @Test
    public void test1() {
        Tensor t = parse("(1+b)/a - 1/a - b/a");
        Tensor actual = FastTensors.multiplySumElementsOnFactor((Sum) t, parse("a"));
        Tensor expected = Complex.ZERO;
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test2() {
        Tensor t = parse("I*(p2*e5+1)+(-I)*p2*e5-I");
        Tensor actual = FastTensors.multiplySumElementsOnFactor((Sum) t, Complex.IMAGINARY_UNIT);
        Tensor expected = Complex.ZERO;
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test3() {
        Tensor t = parse("(x-y)**2*f + (-x**2 + 2*x*y - y**2)*f");
        Tensor actual = FastTensors.multiplySumElementsOnFactorAndExpand((Sum) t, parse("1/(x**2 - 2*x*y + y**2)"));
        Tensor expected = Complex.ZERO;
        TAssert.assertEquals(actual, expected);
    }

    @Test
    public void test4() {
        for (int i = 0; i < 30; ++i) {
            CC.resetTensorNames();
            Tensor sum = parse("b+(-b+a)*f_{m}*f^{m}+a");
            Tensor factor = parse("f_a*f^a");
            Tensor actual = FastTensors.multiplySumElementsOnFactorAndExpand((Sum) sum, factor);
            Tensor expected = parse("(a + b)*f_a*f^a + (- b + a)*f_{m}*f^{m}*f_a*f^a");
            TAssert.assertEquals(actual, expected);
        }
    }

    @Test
    public void test5() {
        for (int i = 0; i < 30; ++i) {
            CC.resetTensorNames();
            Tensor sum = parse("(a + b)*f_a*f^a + (a - b)*f_{m}*f^{m}*f_a*f^a");
            Tensor factor = parse("(b + a)");
            Tensor actual = FastTensors.multiplySumElementsOnFactorAndExpand((Sum) sum, factor);
            Tensor expected = parse("(a**2 + b**2+ 2*a*b )*f_a*f^a + (a**2 - b**2)*f_{m}*f^{m}*f_a*f^a");
            TAssert.assertEquals(actual, expected);
        }
    }

    @Test
    public void test6() {
        for (int i = 0; i < 30; ++i) {
            CC.resetTensorNames();
            Tensor sum = parse("b+(-b+a)*f_{m}*f^{m}+a");
            Tensor factor = parse("f_a*f^a");
            Tensor actual = FastTensors.multiplySumElementsOnFactorAndExpand((Sum) sum, factor);
            Tensor expected = parse("(a + b)*f_a*f^a + (- b + a)*f_{m}*f^{m}*f_a*f^a");
            TAssert.assertEquals(actual, expected);
            sum = actual;
            factor = parse("(b + a)");
            actual = FastTensors.multiplySumElementsOnFactorAndExpand((Sum) sum, factor);
            expected = parse("(a**2 + b**2+ 2*a*b )*f_a*f^a + (a**2 - b**2)*f_{m}*f^{m}*f_a*f^a");
            TAssert.assertEquals(actual, expected);
        }
    }

}
