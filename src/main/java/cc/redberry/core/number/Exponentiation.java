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

import java.math.BigInteger;

/**
 * This class contains methods for complex numbers exponentiation.
 */
public final class Exponentiation {
    public static Real exponentiateIfPossible(Real base, Real power) {
        if (base.isZero())
            if (power.isInfinite())
                return Numeric.NaN;
            else
                return Rational.ZERO;

        if (base.isNumeric() || power.isNumeric()) { // Bease or power are numeric
            return new Numeric(Math.pow(base.getNumericValue().doubleValue(), power.getNumericValue().doubleValue()));
        }

        //TODO some more ONE, ZERO etc. tests

        //<-- Power and Base are rational

        if (power.isInteger())
            return new Rational(((Rational) base).getBigFraction().pow(((Rational) power).getNumerator())); //Using BigFraction pow method.

        //<-- Power is not integer

        BigInteger powerNum = ((Rational) power).getNumerator();
        BigInteger powerDen = ((Rational) power).getDenominator();

        BigInteger baseNum = ((Rational) base).getNumerator();
        BigInteger baseDen = ((Rational) base).getDenominator();

        baseNum = findIntegerRoot(baseNum, powerDen);
        baseDen = findIntegerRoot(baseDen, powerDen);

        if (baseNum == null || baseDen == null) //Result is irrational
            return null;

        return exponentiateIfPossible(new Rational(baseNum, baseDen), new Rational(powerNum));
    }

    private static BigInteger BI_MINUS_ONE = BigInteger.ONE.negate();

    static BigInteger findIntegerRoot(BigInteger base, BigInteger power) {
        BigInteger maxBits = BigInteger.valueOf(base.bitLength() + 1); // base < 2 ^ (maxBits + 1)
        // => base ^ ( 1 / power ) < 2 ^ ( (maxBits + 1) / power )

        BigInteger[] divResult = maxBits.divideAndRemainder(power);
        if (divResult[1].signum() == 0) // i.e. divResult[1] == 0
            maxBits = divResult[0];
        else
            maxBits = divResult[0].add(BigInteger.ONE);

        if (maxBits.bitLength() > 31)
            throw new RuntimeException("Too many bits...");

        int targetBitsNumber = maxBits.intValue();
        int resultLengthM1 = targetBitsNumber / 8 + 1; //resultLength minus one
        byte[] result = new byte[resultLengthM1];
        resultLengthM1--;

        int bitNumber = targetBitsNumber;

        int cValue;
        BigInteger testValue;

        while ((--bitNumber) >= 0) {
            //setting bit
            result[resultLengthM1 - (bitNumber >> 3)] |= 1 << (bitNumber & 0x7);

            //Testing
            testValue = new BigInteger(result);
            cValue = testValue.pow(power.intValue()).compareTo(base); // TODO it is not working [ power.intValue() ] !!!!!!!!!!!!!!!!!!
            if (cValue == 0)
                return testValue;
            if (cValue > 0)
                result[resultLengthM1 - (bitNumber >> 3)] &= ~(1 << (bitNumber & 0x7));
        }

        return null;
    }

    public static Complex exponentiateIfPossible(Complex base, Complex power) {
        //Partially copied from PowerFactory

        if (base.isInfinite())
            if (power.isZero())
                return Complex.ComplexNaN;
            else
                return base;

        if (base.isOne())
            if (power.isInfinite())
                return power.multiply(base);
            else
                return base;

        if (power.isOne())
            return base;

        if (base.isZero()) {
            if (power.getReal().signum() <= 0)
                return Complex.ComplexNaN;
            return base;
        }

        if (power.isZero())
            return Complex.ONE;

        if (base.isNumeric() || power.isNumeric())
            return base.powNumeric(power);

        if (power.isReal()) {
            Rational pp = (Rational) power.getReal();

            if (base.isReal()) {
                Real value = exponentiateIfPossible(base.getReal(), pp);
                if (value == null)
                    return null;
                return new Complex(value);
            }

            if (pp.isInteger()) {

                boolean sign = pp.getNumerator().signum() > 0;
                BigInteger exponent = pp.getNumerator().abs();

                Complex result = Complex.ONE;

                //base ~ k2
                while (exponent.signum() != 0) {
                    if (exponent.testBit(0))
                        result = result.multiply(base);
                    base = base.multiply(base);
                    exponent = exponent.shiftRight(1);
                }

                if (sign)
                    return result;
                else
                    return result.reciprocal();
            }
        }

        return null;
    }
}
