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
package cc.redberry.core.tensor;

import cc.redberry.core.number.Complex;
import cc.redberry.core.number.parser.NumberParser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static cc.redberry.core.tensor.Tensors.pow;
//import static сс.redberry.core.TAssert.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class PowerBuilderTest {

    private static Complex parseComplex(String expression) {
        return NumberParser.COMPLEX_PARSER.parse(expression);
    }

    @Test
    public void test1() {
        Complex a = parseComplex("2");
        Complex p = parseComplex("2");
        Complex expected = parseComplex("4");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test2() {
        Complex a = parseComplex("3");
        Complex p = parseComplex("12");
        Complex expected = parseComplex("531441");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test3() {
        Complex a = parseComplex("-3");
        Complex p = parseComplex("12");
        Complex expected = parseComplex("531441");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test4() {
        Complex a = parseComplex("-3");
        Complex p = parseComplex("11");
        Complex expected = parseComplex("-531441/3");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test5() {
        Complex a = parseComplex("1/3");
        Complex p = parseComplex("12");
        Complex expected = parseComplex("1/531441");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Ignore
    @Test
    public void test6() {
        Complex a = parseComplex("1/0");
        Complex p = parseComplex("12");
        Complex expected = parseComplex("1/0");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test7() {
        Complex a;
        Complex p = parseComplex("0");
        Complex expected = parseComplex("0/0");
        a = Complex.COMPLEX_POSITIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.COMPLEX_NEGATIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.REAL_POSITIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.REAL_NEGATIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.IMAGINARY_NEGATIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.IMAGINARY_POSITIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test8() {
        Complex a = parseComplex("1");
        Complex p = parseComplex("12");
        Complex expected = a;
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test9() {
        Complex a = parseComplex("1");
        Complex p = parseComplex("0");
        Complex expected = a;
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test10() {
        Complex a = parseComplex("1+3*i");
        Complex p = parseComplex("0");
        Complex expected = Complex.ONE;
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test11() {
        Complex a = parseComplex("1+3*i");
        Complex p = parseComplex("1");
        Complex expected = a;
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test12() {
        Complex a = parseComplex("2+3*i");
        Complex p = parseComplex("4");
        Complex expected = parseComplex("-119-120*i");
        Assert.assertEquals(expected, pow(a, p));
    }

    @Ignore
    @Test
    public void test13() {
        Complex a;
        Complex p = parseComplex("43");
        Complex expected = Complex.COMPLEX_INFINITY;
        a = Complex.COMPLEX_POSITIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.COMPLEX_NEGATIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.REAL_POSITIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.REAL_NEGATIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.IMAGINARY_NEGATIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
        a = Complex.IMAGINARY_POSITIVE_INFINITY;
        Assert.assertEquals(expected, pow(a, p));
    }

    @Test
    public void test14() {
        Tensor t = Tensors.parse("1/0*a");
        Assert.assertEquals(Complex.ComplexNaN, t);
    }
}