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
package cc.redberry.core.number;

import cc.redberry.core.number.parser.NumberParser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class ComplexTest {
    @Test
    public void test1() {
        Complex c = new Complex(new Rational(4), new Rational(3));
        Assert.assertEquals(5, c.abs().intValue());
    }

    @Test
    public void test2() {
        Complex c = NumberParser.COMPLEX_PARSER.parse("2+I*3/2");
        Assert.assertEquals(2.5, c.abs().doubleValue(), Double.MIN_VALUE);
    }

    @Test
    public void testIntegerExponentiation() {
        Complex c = NumberParser.COMPLEX_PARSER.parse("1+I");
        Assert.assertEquals(c.pow(10), NumberParser.COMPLEX_PARSER.parse("I*32"));
        c = NumberParser.COMPLEX_PARSER.parse("5+I");
        Assert.assertEquals(c.pow(100), NumberParser.COMPLEX_PARSER.
                parse("35285997703156887662757093411637173142881213037477773358335032217829376+43564079327764355710590239114714227097865139047852601182929616371712000*I"));
    }

    @Test
    public void testNumericComplexExponentiation() {
        Complex c = NumberParser.COMPLEX_PARSER.parse("5+I");
        Complex e = c.powNumeric(c);
        Complex expected = NumberParser.COMPLEX_PARSER.parse("-2447.6068984138622390537015124004469474415099289143+" +
                "1419.5557138599609517808549217505859917260231093976*I");
        Assert.assertEquals(0.0, e.subtract(expected).absNumeric(), 1E-10);
    }

    @Test
    public void testNegativeIntegerExponentiation() {
        Complex c = NumberParser.COMPLEX_PARSER.parse("5+I");
        Complex e = c.pow(-1);
        Complex expected = NumberParser.COMPLEX_PARSER.parse("5/26-I/26");
        Assert.assertEquals(e, expected);
    }


    @Test
    public void testNumericDoubleExponentiation() {
        Complex c = NumberParser.COMPLEX_PARSER.parse("1+I");
        Complex e = c.pow(1.56);
        Complex expected = NumberParser.COMPLEX_PARSER.parse("0.581657+" +
                "1.61562*I");
        Assert.assertEquals(0.0, e.subtract(expected).absNumeric(), 1E-5);
    }
}
