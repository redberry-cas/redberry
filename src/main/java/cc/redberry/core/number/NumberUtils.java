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

import org.apache.commons.math3.fraction.BigFraction;

import java.math.BigInteger;

/**
 * @author Stanislav Poslavsky
 */
public class NumberUtils {

    private NumberUtils() {
    }

    public static boolean isZeroOrIndeterminate(Complex complex) {
        return complex.isZero() || complex.isInfinite() || complex.isNaN();
    }

    public static boolean isIndeterminate(Complex complex) {
        return complex.isInfinite() || complex.isNaN();
    }

    /**
     * Checks that an object is not null.
     *
     * @param o Object to be checked.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    static void checkNotNull(Object o)
            throws NullPointerException {
        if (o == null)
            throw new NullPointerException();
    }

    public static Numeric createNumeric(double d) {
        //FUTURE investigate performance
        if (d == 0)
            return Numeric.ZERO;
        else if (d == 1)
            return Numeric.ONE;
        else if (d == Double.POSITIVE_INFINITY)
            return Numeric.POSITIVE_INFINITY;
        else if (d == Double.NEGATIVE_INFINITY)
            return Numeric.NEGATIVE_INFINITY;
        else if (d != d)// d is NaN
            return Numeric.NaN;
        else
            return new Numeric(d);
    }

    public static Rational createRational(BigFraction fraction) {
        //FUTURE investigate performance
        if (fraction.getNumerator().equals(BigInteger.ZERO))
            return Rational.ZERO;
        if (BigFraction.ONE.equals(fraction))
            return Rational.ONE;
        return new Rational(fraction);
    }

    private final static BigInteger TWO = new BigInteger("2");

    /**
     * Computes the integer square root of a number.
     *
     * @param n The number.
     * @return The integer square root, i.e. the largest number whose square
     *         doesn't exceed n.
     */
    public static BigInteger sqrt(BigInteger n) {
        if (n.signum() >= 0) {
            final int bitLength = n.bitLength();
            BigInteger root = BigInteger.ONE.shiftLeft(bitLength / 2);

            while (!isSqrtXXX(n, root))
                root = root.add(n.divide(root)).divide(TWO);
            return root;
        } else
            throw new ArithmeticException("square root of negative number");
    }

    private static boolean isSqrtXXX(BigInteger n, BigInteger root) {
        final BigInteger lowerBound = root.pow(2);
        final BigInteger upperBound = root.add(BigInteger.ONE).pow(2);
        return lowerBound.compareTo(n) <= 0
                && n.compareTo(upperBound) < 0;
    }

    public static boolean isSqrt(BigInteger n, BigInteger root) {
        return n.compareTo(root.pow(2)) == 0;
    }

    public static boolean isIntegerOdd(Complex complex) {
        if (complex.isInteger())
            return complex.getReal().abs().intValue() % 2 == 1;
        return false;
    }

    public static boolean isIntegerEven(Complex complex) {
        if (complex.isInteger())
            return complex.getReal().intValue() % 2 == 0;
        return false;
    }
}
