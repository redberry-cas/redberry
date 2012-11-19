/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algeba System (JAS).
 *
 * JAS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * JAS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JAS. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * $Id$
 */

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith;


/**
 * Combinatoric algorithms. Similar to ALDES/SAC2 SACCOMB module.
 *
 * @author Heinz Kredel
 */
public class Combinatoric {

    /**
     * Factorial.
     *
     * @param n integer.
     * @return n!, with 0! = 1.
     */
    public static BigInteger factorial(long n) {
        if (n <= 1) {
            return BigInteger.ONE;
        }
        BigInteger f = BigInteger.ONE;
        if (n >= Integer.MAX_VALUE) {
            throw new UnsupportedOperationException(n + " >= Integer.MAX_VALUE = " + Integer.MAX_VALUE);
        }
        for (int i = 2; i <= n; i++) {
            f = f.multiply(new BigInteger(i));
        }
        return f;
    }

}
