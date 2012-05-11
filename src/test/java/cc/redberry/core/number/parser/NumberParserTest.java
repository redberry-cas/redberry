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
package cc.redberry.core.number.parser;

import cc.redberry.core.number.*;
import org.apache.commons.math3.fraction.*;
import org.junit.*;

/**
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class NumberParserTest {
    @Test
    public void test1() {
        Real r1 = NumberParser.REAL_PARSER.parse("2/3");
        Real r2 = NumberParser.REAL_PARSER.parse("2/3-2/3");
        Assert.assertTrue(r2.isZero());
        Assert.assertTrue(r1.multiply(r2).isZero());
        Assert.assertTrue(r1.divide(r2).isInfinite());
    }

    @Test(expected = NumberFormatException.class)
    public void test2() {
        NumberParser.REAL_PARSER.parse("1+a");
    }

    @Test(expected = NumberFormatException.class)
    public void test3() {
        NumberParser.REAL_PARSER.parse("2/5+7/(3-(2+1/(4+o-9))*5/4)");
    }

    @Test(expected = NumberFormatException.class)
    public void test4() {
        NumberParser.REAL_PARSER.parse("2/5+7/(3-(2+1/(4^-9))*5/4)");
    }

    @Test
    public void test5() {
        Real r = NumberParser.REAL_PARSER.parse("2/5+7/(3-(2+1/(4-9))*5/4)");
        Real exp = new Rational(146, 15);
        Assert.assertTrue(r.equals(exp));
    }

    @Test
    public void test6() {
        Real r = NumberParser.REAL_PARSER.parse("2/5+7/(3-(2+1/(4-9))*5/4)");
        Real exp = new Rational(146, 15);
        Assert.assertTrue(r.equals(exp));
    }

    @Test
    public void test7() {
        Real r = NumberParser.REAL_PARSER.parse("2/5+7/(3-(2+1/(4-9))*5/4)+1/0");
        Assert.assertTrue(r.isInfinite());
    }

    @Test
    public void test8() {
        Real r = NumberParser.REAL_PARSER.parse("1/(2/5+7/(3-(2+1/(4-9))*5/4)+1/0)");
        Assert.assertTrue(r.isZero());
    }

    @Test
    public void test9() {
        Real r = NumberParser.REAL_PARSER.parse("(1-3/3*1.0-2.2+22/10)/(2/5+7/(3-(2+1/(4-9))*5/4)+1/0)");
        Assert.assertTrue(r.isZero());
    }

    @Test
    public void test10() {
        Real r = NumberParser.REAL_PARSER.parse("1-3/3*1.0-2.2+22/10");
        Assert.assertTrue(r.isZero());
    }

    @Test
    public void test11() {
        Real r = NumberParser.REAL_PARSER.parse("1/(1-3/3*1.0-2.2+22/10)/(2/5+7/(3-(2+1/(4-9))*5/4)+1/0)");
        Assert.assertTrue(r.isNaN());
    }
    
    @Test
    public void test12() {
        Real r = NumberParser.REAL_PARSER.parse("1+0.0");
        Assert.assertTrue(r.isNumeric());
    }
    
    @Test
    public void test13() {
        Real r = NumberParser.REAL_PARSER.parse("2*1.0");
        Assert.assertTrue(r.isNumeric());
    }
    
    
    @Test
    public void test14() {
        Real r = NumberParser.REAL_PARSER.parse("2/1.0");
        Assert.assertTrue(r.isNumeric());
    }
}
