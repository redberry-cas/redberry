package cc.redberry.core.number;

import java.math.BigInteger;

/**
 * This class contains methods for complex numbers exponentiation.
 */
public final class Exponentiation {
    public static Real exponentiate(Real base, Real power) {
        if (base.isNumeric() || power.isNumeric()) { // Bease or power are numeric
            return new Numeric(Math.pow(base.getNumericValue().doubleValue(), power.getNumericValue().doubleValue()));
        }

        //Power and Base are rational

        if (power.isInteger())
            return new Rational(((Rational) base).getBigFraction().pow(((Rational) power).getNumerator())); //Using BigFraction pow method.

        //Power is not integer


        return null;
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
}
