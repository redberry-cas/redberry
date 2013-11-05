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
package cc.redberry.core.number;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

/**
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 */
public class NumberUtilsTest {

    @Test
    public void test1() {
        BigInteger nine = new BigInteger("9");
        Assert.assertEquals(BigInteger.valueOf(3), NumberUtils.sqrt(nine));
    }

//    @Test
//    public void root() {

//        for (int i = 0; i < 1000; ++i) {
//            NumberUtils.factorial(1000);
//        }
//
//        long start = System.currentTimeMillis();
//        NumberUtils.factorial(100000);
//        System.out.println(System.currentTimeMillis() - start);
//        start = System.currentTimeMillis();
//        NumberUtils.factorial(100000);
//        System.out.println(System.currentTimeMillis() - start);
//        start = System.currentTimeMillis();
//        NumberUtils.factorial(100000);
//        System.out.println(System.currentTimeMillis() - start);
//
//        System.out.println(NumberUtils.factorial(1000));
//    }
}
