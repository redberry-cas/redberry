/*
 * Redberry: symbolic tensor computations.
 *
 * Copyright (c) 2010-2015:
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

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * @author Stanislav Poslavsky
 */
public class RationalTest {
    @Test
    public void testHashCode() throws Exception {
        Assert.assertEquals(new Rational(2, 3).hashCode(),
                -new Rational(-2, 3).hashCode());
        Assert.assertEquals(new Rational(-2, -3).hashCode(),
                -new Rational(-2, 3).hashCode());
        Assert.assertEquals(new Rational(new BigInteger("1231231231876239486"),
                        new BigInteger("123242342342342342331231231876239486")).hashCode(),
                -new Rational(new BigInteger("1231231231876239486"),
                        new BigInteger("-123242342342342342331231231876239486")).hashCode());
    }

    @Test
    public void testHashCode1() throws Exception {
        Assert.assertEquals(0, Rational.ZERO.hashCode());
        Assert.assertEquals(1, Rational.ONE.hashCode());
        Assert.assertEquals(-1, Rational.MINUS_ONE.hashCode());
    }
}