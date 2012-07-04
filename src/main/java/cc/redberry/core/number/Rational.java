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

import java.io.Serializable;
import java.math.BigInteger;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.FractionConversionException;

import static cc.redberry.core.number.NumberUtils.*;

/**
 * Representation of a rational number without any overflow. This class is a
 * wrapper of {@link BigInteger}.
 *
 * <p>This class implements mathematical operations declared in
 * {@link cc.redberry.core.number.Number} as operations with in the following
 * way. If argument is {@link Numeric}, the result will be {@code Numeric} too.
 * For example, 2 &#47 3 +1.0 will give 1.666666 and so on. If argument is {@code Rational}
 * number, the result will be {@code Rational} too.
 *
 * @see Number
 * @see Numeric
 * @see BigFraction
 *
 * @author Stanislav Poslavsky
 */
public class Rational extends Real implements Serializable {

    /**
     * A fraction representing "2 / 1".
     */
    public static final Rational TWO = new Rational(2);
    /**
     * A fraction representing "-2 / 1".
     */
    public static final Rational MINUSE_TWO = new Rational(2);
    /**
     * A fraction representing "1".
     */
    public static final Rational ONE = new Rational(1);
    /**
     * A fraction representing "0".
     */
    public static final Rational ZERO = new Rational(0);
    /**
     * A fraction representing "-1 / 1".
     */
    public static final Rational MINUS_ONE = new Rational(-1);
    /**
     * A fraction representing "4/5".
     */
    public static final Rational FOUR_FIFTHS = new Rational(4, 5);
    /**
     * A fraction representing "1/5".
     */
    public static final Rational ONE_FIFTH = new Rational(1, 5);
    /**
     * A fraction representing "1/2".
     */
    public static final Rational ONE_HALF = new Rational(1, 2);
    /**
     * A fraction representing "1/2".
     */
    public static final Rational MINUSE_ONE_HALF = new Rational(-1, 2);
    /**
     * A fraction representing "1/4".
     */
    public static final Rational ONE_QUARTER = new Rational(1, 4);
    /**
     * A fraction representing "1/3".
     */
    public static final Rational ONE_THIRD = new Rational(1, 3);
    /**
     * A fraction representing "3/5".
     */
    public static final Rational THREE_FIFTHS = new Rational(3, 5);
    /**
     * A fraction representing "3/4".
     */
    public static final Rational THREE_QUARTERS = new Rational(3, 4);
    /**
     * A fraction representing "2/5".
     */
    public static final Rational TWO_FIFTHS = new Rational(2, 5);
    /**
     * A fraction representing "2/4".
     */
    public static final Rational TWO_QUARTERS = new Rational(2, 4);
    /**
     * A fraction representing "2/3".
     */
    public static final Rational TWO_THIRDS = new Rational(2, 3);
    /**
     * Serializable version identifier.
     */
    private static final long serialVersionUID = -5630213147331578515L;
    /*
     * rational value
     */
    private final BigFraction fraction;

    public Rational(BigFraction fraction) {
        checkNotNull(fraction);
        this.fraction = fraction;
    }

    /**
     *
     * @see BigFraction#BigFraction(java.math.BigInteger)
     */
    public Rational(BigInteger num) {
        checkNotNull(num);
        fraction = new BigFraction(num);
    }

    /**
     *
     * @see BigFraction#BigFraction(java.math.BigInteger, java.math.BigInteger)
     */
    public Rational(BigInteger num, BigInteger den) {
        checkNotNull(num);
        checkNotNull(den);
        fraction = new BigFraction(num, den);
    }

    /**
     *
     * @see BigFraction#BigFraction(double)
     */
    public Rational(double value) throws MathIllegalArgumentException {
        fraction = new BigFraction(value);
    }

    /**
     *
     * @see BigFraction#BigFraction(double, double, int)
     */
    public Rational(double value, double epsilon, int maxIterations) throws FractionConversionException {
        fraction = new BigFraction(value, epsilon, maxIterations);
    }

    /**
     *
     * @see BigFraction#BigFraction(double, int)
     */
    public Rational(double value, int maxDenominator) throws FractionConversionException {
        fraction = new BigFraction(value, maxDenominator);
    }

    /**
     *
     * @see BigFraction#BigFraction(int)
     */
    public Rational(int num) {
        fraction = new BigFraction(num);
    }

    /**
     *
     * @see BigFraction#BigFraction(int, int)
     */
    public Rational(int num, int den) {
        fraction = new BigFraction(num, den);
    }

    /**
     *
     * @see BigFraction#BigFraction(long)
     */
    public Rational(long num) {
        fraction = new BigFraction(num);
    }

    /**
     *
     * @see BigFraction#BigFraction(long, long)
     */
    public Rational(long num, long den) {
        fraction = new BigFraction(num, den);
    }

    public BigFraction getBigFraction() {
        return fraction;
    }

    @Override
    public long longValue() {
        return fraction.longValue();
    }

    @Override
    public int intValue() {
        return fraction.intValue();
    }

    @Override
    public float floatValue() {
        return fraction.floatValue();
    }

    @Override
    public double doubleValue() {
        return fraction.doubleValue();
    }

    @Override
    public int hashCode() {
        return fraction.abs().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Number))
            return false;
        if (obj instanceof Rational)
            return fraction.equals(((Rational) obj).fraction);
        else
            return ((Numeric) obj).doubleValue() == doubleValue();

    }

    @Override
    public String toString() {
        return fraction.getNumerator().toString() + (fraction.getDenominator().equals(BigInteger.ONE) ? "" : ("/" + fraction.getDenominator().toString()));
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public boolean isNaN() {
        return false;
    }

    @Override
    public Real add(Real arg0) {
        checkNotNull(arg0);
        if (arg0 instanceof Numeric)
            return arg0.add(this);
        return createRational(fraction.add(((Rational) arg0).fraction));
    }

    @Override
    public Real divide(Real arg0) {
        checkNotNull(arg0);
        if (arg0 instanceof Numeric || arg0.isZero())
            return createNumeric(fraction.doubleValue() / arg0.doubleValue());
        return createRational(fraction.divide(((Rational) arg0).fraction));
    }

    @Override
    public Real multiply(double d) {
        return createNumeric(d * fraction.doubleValue());
    }

    @Override
    public Rational multiply(int arg0) {
        return arg0 == 1 ? this : arg0 == 0 ? ZERO : new Rational(fraction.multiply(arg0));
    }

    @Override
    public Real multiply(Real arg0) {
        checkNotNull(arg0);
        if (arg0 instanceof Numeric)
            return arg0.multiply(this);
        return arg0.isOne() ? this : arg0.isZero() ? ZERO : createRational(fraction.multiply(((Rational) arg0).fraction));
    }

    @Override
    public Rational negate() {
        return createRational(fraction.negate());
    }

    @Override
    public Rational reciprocal() {
        return createRational(fraction.reciprocal());
    }

    @Override
    public Real subtract(Real arg0) {
        checkNotNull(arg0);
        if (arg0 instanceof Numeric)
            return new Numeric(fraction.doubleValue() - arg0.doubleValue());
        return createRational(fraction.subtract(((Rational) arg0).fraction));
    }

    @Override
    public Rational subtract(BigFraction fraction) {
        checkNotNull(fraction);
        return createRational(this.fraction.subtract(fraction));
    }

    @Override
    public Rational subtract(long l) {
        return l == 0 ? this : createRational(fraction.subtract(l));
    }

    @Override
    public Rational subtract(int i) {
        return new Rational(fraction.subtract(i));
    }

    @Override
    public Rational subtract(BigInteger bg) {
        NumberUtils.checkNotNull(bg);
        return new Rational(fraction.subtract(bg));
    }

    public Rational reduce() {
        return new Rational(fraction.reduce());
    }

    @Override
    public Numeric pow(double exponent) {
        return new Numeric(fraction.pow(exponent));
    }

    @Override
    public Rational pow(BigInteger exponent) {
        NumberUtils.checkNotNull(exponent);
        return new Rational(fraction.pow(exponent));
    }

    @Override
    public Rational pow(long exponent) {
        return new Rational(fraction.pow(exponent));
    }

    @Override
    public Rational pow(int exponent) {
        return new Rational(fraction.pow(exponent));
    }

    public double percentageValue() {
        return fraction.percentageValue();
    }

    @Override
    public Rational multiply(BigFraction fraction) {
        NumberUtils.checkNotNull(fraction);
        return new Rational(fraction.multiply(fraction));
    }

    @Override
    public Rational multiply(long l) {
        return new Rational(fraction.multiply(l));
    }

    @Override
    public Rational multiply(BigInteger bg) {
        NumberUtils.checkNotNull(bg);
        return new Rational(fraction.multiply(bg));
    }

    public long getNumeratorAsLong() {
        return fraction.getNumeratorAsLong();
    }

    public int getNumeratorAsInt() {
        return fraction.getNumeratorAsInt();
    }

    public BigInteger getNumerator() {
        return fraction.getNumerator();
    }

    public long getDenominatorAsLong() {
        return fraction.getDenominatorAsLong();
    }

    public int getDenominatorAsInt() {
        return fraction.getDenominatorAsInt();
    }

    public BigInteger getDenominator() {
        return fraction.getDenominator();
    }

    @Override
    public Rational divide(BigFraction fraction) {
        NumberUtils.checkNotNull(fraction);
        return new Rational(fraction.divide(fraction));
    }

    @Override
    public Rational divide(long l) {
        return new Rational(fraction.divide(l));
    }

    @Override
    public Rational divide(int i) {
        return new Rational(fraction.divide(i));
    }

    @Override
    public Rational divide(BigInteger bg) {
        NumberUtils.checkNotNull(bg);
        return new Rational(fraction.divide(bg));
    }

    @Override
    public Real divide(double d) {
        return new Numeric(fraction.doubleValue() / d);
    }

    @Override
    public Rational add(BigFraction fraction) {
        NumberUtils.checkNotNull(fraction);
        return new Rational(fraction.add(fraction));
    }

    @Override
    public Rational add(long l) {
        return new Rational(fraction.add(l));
    }

    @Override
    public Rational add(BigInteger bg) {
        NumberUtils.checkNotNull(bg);
        return new Rational(fraction.add(bg));
    }

    @Override
    public Real add(double d) {
        return new Numeric(d + doubleValue());
    }

    @Override
    public Rational add(int i) {
        return new Rational(fraction.add(i));
    }

    @Override
    public Real subtract(double d) {
        return new Numeric(this.doubleValue() - d);
    }

    @Override
    public Rational abs() {
        return new Rational(fraction.abs());
    }

    @Override
    public Numeric getNumericValue() {
        return new Numeric(doubleValue());
    }

    @Override
    public int compareTo(Real o) {
        NumberUtils.checkNotNull(o);
        if (o instanceof Numeric)
            return Double.compare(doubleValue(), o.doubleValue());
        return fraction.compareTo(((Rational) o).fraction);
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public boolean isZero() {
        //Here we do not use fraction.equals() because it has low performence
        return fraction.getNumerator().equals(BigInteger.ZERO);//.equals(BigFraction.ZERO);
    }

    @Override
    public boolean isOne() {
        //Here we do not use fraction.equals() because it has low performence
        return fraction.getNumerator().equals(BigInteger.ONE) && fraction.getDenominator().equals(BigInteger.ONE);//equals(BigFraction.ONE);
    }

    /*
     * Chached instenance for comparing with it (for performance)
     */
    private static final BigInteger BI_MINUS_ONE = new BigInteger("-1");

    @Override
    public boolean isMinusOne() {
        //Here we do not use fraction.equals() because it has low performence
        return fraction.getNumerator().equals(BI_MINUS_ONE) && fraction.getDenominator().equals(BigInteger.ONE);//equals(BigFraction.ONE);
    }

    public boolean isInteger() {
        return fraction.getDenominator().compareTo(BigInteger.ONE) == 0;
    }

    @Override
    public int signum() {
        return fraction.getNumerator().signum();
    }
}
