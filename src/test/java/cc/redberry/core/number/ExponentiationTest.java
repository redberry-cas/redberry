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
 * the Free Software Foundation, either version 2 of the License, or
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
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigInteger;

public class ExponentiationTest extends TestCase {
    @Test
    public void testIntegerRoots() {
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(28), BigInteger.valueOf(3)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(22), BigInteger.valueOf(3)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(27), BigInteger.valueOf(3)),
                BigInteger.valueOf(3));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(49), BigInteger.valueOf(2)),
                BigInteger.valueOf(7));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(129140163), BigInteger.valueOf(17)),
                BigInteger.valueOf(3));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(129140162), BigInteger.valueOf(17)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(129140164), BigInteger.valueOf(17)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(19073486328125L), BigInteger.valueOf(19)),
                BigInteger.valueOf(5));
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(19073486328123L), BigInteger.valueOf(19)),
                null);
        Assert.assertEquals(Exponentiation.findIntegerRoot(BigInteger.valueOf(19073486328128L), BigInteger.valueOf(19)),
                null);
    }

    @Test
    public void testExponentiateIfPossible() {
        Assert.assertEquals(NumberParser.REAL_PARSER.parse("12/5"),
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("25/144"),
                        NumberParser.REAL_PARSER.parse("-1/2")));

        Assert.assertEquals(NumberParser.REAL_PARSER.parse("9/49"),
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("27/343"),
                        NumberParser.REAL_PARSER.parse("2/3")));

        Assert.assertEquals(null,
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("27/343"),
                        NumberParser.REAL_PARSER.parse("2/4")));

        Assert.assertEquals(NumberParser.REAL_PARSER.parse("0.28056585887484736"),
                Exponentiation.exponentiateIfPossible(NumberParser.REAL_PARSER.parse("27/343"),
                        NumberParser.REAL_PARSER.parse("0.5")));
    }

    @Test
    public void testIntegerRootOfComplex() {
        Complex base = NumberParser.COMPLEX_PARSER.parse("256/129140163+256/129140163*I");
        Assert.assertEquals(Exponentiation.findIntegerRoot(base, BigInteger.valueOf(17)), NumberParser.COMPLEX_PARSER.parse("1/3+1/3*I"));
    }
}
