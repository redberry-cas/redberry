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
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.fraction.BigFraction;

import static cc.redberry.core.number.NumberUtils.checkNotNull;
import static cc.redberry.core.number.NumberUtils.createNumeric;

/**
 * The {@code Numeric} class extends class
 * {@link cc.redberry.core.number.Number} and gives numeric representation of
 * Redberry numbers. It is simply wraps a value of the primitive type
 * {@code double} in an object (like java {@link Double}).
 * <p/>
 * <p>This class implements all mathematical operations declared in
 * {@link cc.redberry.core.number.Number} as operations with doubles. So all
 * methods return numeric numbers. As for example, 1.0 + 2 &#47 3 will give
 * 1.666666 and so on. All mathematical methods works with its arguments using
 * {@link Number#doubleValue() } method.
 *
 * @author Dmitry Bolotin
 * @author Stanislav Poslavsky
 * @see Number
 * @see Double
 * @see RationalNumber
 */
public final class Numeric extends Real implements Serializable {

    /**
     * A constant holding 0 value.
     */
    public static final Numeric ZERO = new Numeric(0);
    /**
     * A constant holding 1 value.
     */
    public static final Numeric ONE = new Numeric(1);
    /**
     * A constant holding -1 value.
     */
    public static final Numeric MINUS_ONE = new Numeric(-1);

    /**
     * A constant holding the positive infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0x7ff0000000000000L)}.
     *
     * @see Double#POSITIVE_INFINITY
     */
    public static final Numeric POSITIVE_INFINITY = new Numeric(Double.POSITIVE_INFINITY);
    /**
     * A constant holding the negative infinity of type
     * {@code double}. It is equal to the value returned by
     * {@code Double.longBitsToDouble(0xfff0000000000000L)}.
     *
     * @see Double#NEGATIVE_INFINITY
     */
    public static final Numeric NEGATIVE_INFINITY = new Numeric(Double.NEGATIVE_INFINITY);
    /**
     * A constant holding a Not-a-Number (NaN) value of type
     * {@code double}. It is equivalent to the value returned by
     * {@code Double.longBitsToDouble(0x7ff8000000000000L)}.
     *
     * @see Double#NaN
     */
    public static final Numeric NaN = new Numeric(Double.NaN);
    /**
     * A constant holding the largest positive finite value of type
     * {@code double}, (2-2<sup>-52</sup>)&middot;2<sup>1023</sup>. It is equal
     * to the hexadecimal floating-point literal
     * {@code 0x1.fffffffffffffP+1023} and also equal to
     * {@code Double.longBitsToDouble(0x7fefffffffffffffL)}.
     *
     * @see Double#MAX_VALUE
     */
    public static final Numeric MAX_VALUE = new Numeric(Double.MAX_VALUE);
    /**
     * A constant holding the smallest positive normal value of type
     * {@code double}, 2<sup>-1022</sup>. It is equal to the hexadecimal
     * floating-point literal {@code 0x1.0p-1022} and also equal to {@code Double.longBitsToDouble(0x0010000000000000L)}.
     *
     * @see Double#MIN_NORMAL
     */
    public static final Numeric MIN_NORMAL = new Numeric(Double.MIN_NORMAL); // 2.2250738585072014E-308
    /**
     * A constant holding the smallest positive nonzero value of type
     * {@code double}, 2<sup>-1074</sup>. It is equal to the hexadecimal
     * floating-point literal
     * {@code 0x0.0000000000001P-1022} and also equal to
     * {@code Double.longBitsToDouble(0x1L)}.
     *
     * @see Double#MIN_VALUE
     */
    public static final Numeric MIN_VALUE = new Numeric(Double.MIN_VALUE); // 4.9e-324
    /*
     * numeric value
     */
    private final double value;

    public Numeric(final double value) {
        this.value = value;
    }

    public Numeric(final int value) {
        this.value = value;
    }

    public Numeric(final float value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Numeric} instance, which {@code double} variable
     * is taken from {@code value.doubleValue()}. So, this constructor makes <br>{@code
     * this.value = value.doubleValue();
     * }
     *
     * @param value the value, which double representation to be represented by
     *              the {@code Double}.
     * @throws NullArgumentException if value is {@code null}.
     */
    public Numeric(final Number value) {
        checkNotNull(value);
        this.value = value.doubleValue();
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public Numeric add(final Real n) {
        checkNotNull(n);
        return add(n.doubleValue());
    }

    @Override
    public Numeric divide(final Real n) {
        checkNotNull(n);
        return divide(n.doubleValue());
    }

    @Override
    public Numeric multiply(final int n) {
        return n == 1 ? this : createNumeric(value * n);
    }

    @Override
    public Numeric multiply(final Real n) {
        checkNotNull(n);
        return multiply(n.doubleValue());
    }

    @Override
    public Numeric subtract(final Real n) {
        checkNotNull(n);
        return subtract(n.doubleValue());
    }

    @Override
    public Numeric negate() {
        return createNumeric(-value);
    }

    @Override
    public Numeric reciprocal() {
        return createNumeric(1 / value);
    }

    /**
     * @return similar to {@link Double}
     * @see Double#hashCode()
     */
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(value * value);
        return (int) (bits ^ (bits >>> 32));
    }

    /**
     * @return {@code Double.isInfinite(value)}
     * @see Double#isInfinite(double)
     */
    @Override
    public boolean isInfinite() {
        return Double.isInfinite(value);
    }

    /**
     * @return {@code Double.isNaN(value)}
     * @see Double#isNaN(double)
     */
    @Override
    public boolean isNaN() {
        return Double.isNaN(value);
    }

    @Override
    public Numeric abs() {
        return value >= 0 ? this : negate();
    }

    @Override
    public Numeric add(double d) {
        return d == 0 ? this : createNumeric(value + d);
    }

    @Override
    public Numeric add(int i) {
        return add((double) i);
    }

    @Override
    public Numeric add(BigFraction fraction) {
        checkNotNull(fraction);
        //FUTURE fraction.doubleValue() is very unefficient operation
        return add(fraction.doubleValue());
    }

    @Override
    public Numeric add(long l) {
        return add((double) l);
    }

    @Override
    public Numeric add(BigInteger bg) {
        checkNotNull(bg);
        //FUTURE fraction.doubleValue() is very unefficient operation
        return add(bg.doubleValue());
    }

    @Override
    public Numeric divide(double d) {
        return d == 1 ? this : createNumeric(value / d);
    }

    @Override
    public Numeric divide(BigFraction fraction) {
        checkNotNull(fraction);
        //FUTURE fraction.doubleValue() is very unefficient operation
        return divide(fraction.doubleValue());
    }

    @Override
    public Numeric divide(long l) {
        return divide((double) l);
    }

    @Override
    public Numeric divide(int i) {
        return divide((double) i);
    }

    @Override
    public Numeric divide(BigInteger bg) {
        checkNotNull(bg);
        //FUTURE fraction.doubleValue() is very unefficient operation
        return divide(bg.doubleValue());

    }

    @Override
    public Numeric multiply(double d) {
        return d == 1.0 ? this : createNumeric(d * value);
    }

    @Override
    public Numeric multiply(BigFraction fraction) {
        checkNotNull(fraction);
        //FUTURE fraction.doubleValue() is very unefficient operation
        return multiply(fraction.doubleValue());
    }

    @Override
    public Numeric multiply(long l) {
        return multiply((double) l);
    }

    @Override
    public Numeric multiply(BigInteger bg) {
        checkNotNull(bg);
        //FUTURE bg.doubleValue() is very unefficient operation
        return multiply(bg.doubleValue());
    }

    @Override
    public Numeric subtract(BigFraction fraction) {
        return add(fraction.negate());
    }

    @Override
    public Numeric subtract(long l) {
        return add((double) (-l));
    }

    @Override
    public Numeric subtract(int i) {
        return add((double) (-i));
    }

    @Override
    public Numeric subtract(BigInteger bg) {
        return add(bg.negate());
    }

    @Override
    public Numeric subtract(double d) {
        return add(-d);
    }

    @Override
    public Numeric pow(double exponent) {
        return createNumeric(StrictMath.pow(value, exponent));
    }

    @Override
    public Numeric pow(BigInteger exponent) {
        checkNotNull(exponent);
        //FUTURE bg.doubleValue() is very unefficient operation
        return pow(exponent.doubleValue());
    }

    @Override
    public Numeric pow(long exponent) {
        return pow((double) exponent);
    }

    @Override
    public Numeric pow(int exponent) {
        return pow((double) exponent);
    }

    /**
     * @see Double#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Number))
            return false;
        return Double.doubleToLongBits(value) == Double.doubleToLongBits(((Number) obj).doubleValue());
    }

    /**
     * @see Double#toString()
     */
    @Override
    public String toString() {
        return Double.toString(value);
    }

    /**
     * @return this
     */
    @Override
    public Numeric getNumericValue() {
        return this;
    }

    @Override
    public boolean isZero() {
        return value == 0.0;
    }

    /**
     * @see Double#compare(double, double)
     */
    @Override
    public int compareTo(Real o) {
        checkNotNull(o);
        return Double.compare(value, o.doubleValue());
    }

    @Override
    public boolean isNumeric() {
        return true;
    }

    @Override
    public boolean isOne() {
        return value == 1.0;
    }

    @Override
    int signum() {
        return value > 0 ? 1 : value == 0 ? 0 : -1;
    }
}
