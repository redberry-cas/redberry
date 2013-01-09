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

import cc.redberry.core.context.OutputFormat;
import cc.redberry.core.indices.Indices;
import cc.redberry.core.indices.IndicesFactory;
import cc.redberry.core.tensor.*;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.util.FastMath;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Implementation of complex numbers.
 *
 * @author Stanislav Poslavsky
 */
public final class Complex extends Tensor
        implements Number<Complex>,
        Serializable {

    public static final Complex ComplexNaN =
            new Complex(Numeric.NaN, Numeric.NaN);

    public static final Complex REAL_POSITIVE_INFINITY =
            new Complex(Numeric.POSITIVE_INFINITY, Numeric.ZERO);

    public static final Complex REAL_NEGATIVE_INFINITY =
            new Complex(Numeric.NEGATIVE_INFINITY, Numeric.ZERO);

    public static final Complex IMAGINARY_POSITIVE_INFINITY =
            new Complex(Numeric.ZERO, Numeric.POSITIVE_INFINITY);

    public static final Complex IMAGINARY_NEGATIVE_INFINITY =
            new Complex(Numeric.ZERO, Numeric.NEGATIVE_INFINITY);

    public static final Complex COMPLEX_NEGATIVE_INFINITY =
            new Complex(Numeric.NEGATIVE_INFINITY, Numeric.NEGATIVE_INFINITY);

    public static final Complex COMPLEX_POSITIVE_INFINITY =
            new Complex(Numeric.POSITIVE_INFINITY, Numeric.POSITIVE_INFINITY);

    public static final Complex COMPLEX_INFINITY = COMPLEX_POSITIVE_INFINITY;

    public static final Complex ZERO =
            new Complex(Rational.ZERO, Rational.ZERO);

    public static final Complex ONE =
            new Complex(Rational.ONE, Rational.ZERO);

    public static final Complex ONE_HALF =
            new Complex(Rational.ONE_HALF, Rational.ZERO);

    public static final Complex TWO =
            new Complex(Rational.TWO, Rational.ZERO);

    public static final Complex FOUR =
            new Complex(Rational.FOUR, Rational.ZERO);

    public static final Complex MINUS_ONE =
            new Complex(Rational.MINUS_ONE, Rational.ZERO);

    public static final Complex MINUS_ONE_HALF =
            new Complex(Rational.MINUSE_ONE_HALF, Rational.ZERO);

    public static final Complex MINUS_TWO =
            new Complex(Rational.MINUS_TWO, Rational.ZERO);

    public static final Complex IMAGE_ONE =
            new Complex(Rational.ZERO, Rational.ONE);

    private final Real real;
    private final Real imaginary;

    public Complex(Real real, Real imaginary) {
        if (real instanceof Numeric || imaginary instanceof Numeric) {
            this.real = real.getNumericValue();
            this.imaginary = imaginary.getNumericValue();
        } else {
            this.real = real;
            this.imaginary = imaginary;
        }
    }

    public Complex(Real real) {
        if (real instanceof Numeric) {
            this.real = real.getNumericValue();
            this.imaginary = Numeric.ZERO;
        } else {
            this.real = real;
            this.imaginary = Rational.ZERO;
        }
    }

    public Complex(org.apache.commons.math3.complex.Complex complex) {
        this(complex.getReal(), complex.getImaginary());
    }

    public Complex(int real, int imaginary) {
        this(new Rational(real), new Rational(imaginary));
    }

    public Complex(int real) {
        this(new Rational(real), Rational.ZERO);
    }

    public Complex(double real, double imaginary) {
        this(new Numeric(real), new Numeric(imaginary));
    }

    public Complex(int real, double imaginary) {
        this(new Numeric(real), new Numeric(imaginary));
    }

    public Complex(double real, int imaginary) {
        this(new Numeric(real), new Numeric(imaginary));
    }

    public Complex(double real) {
        this(new Numeric(real), Numeric.ZERO);
    }

    public Complex(long real) {
        this(new Rational(real), Rational.ZERO);
    }

    public Complex(long real, long imaginary) {
        this(new Rational(real), new Rational(imaginary));
    }

    public Complex(BigInteger real, BigInteger imaginary) {
        this(new Rational(real), new Rational(imaginary));
    }

    public Complex(BigInteger real) {
        this(new Rational(real), Rational.ZERO);
    }

    @Override
    public Tensor get(int i) {
        throw new IndexOutOfBoundsException();
    }

    @Override
    public Indices getIndices() {
        return IndicesFactory.EMPTY_INDICES;
    }

    @Override
    protected int hash() {
        return 47 * (329 + real.hashCode()) + this.imaginary.hashCode();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public String toString(OutputFormat mode) {
        if (real.isZero())
            if (imaginary.isZero())
                return "0";
            else if (imaginary.isOne())
                return "I";
            else if (imaginary.isMinusOne())
                return "-I";
            else
                return imaginary.toString() + "*I";
        int is = imaginary.signum();
        if (is == 0)
            return real.toString();
        Real abs = imaginary.abs();
        if (is < 0)
            if (abs.isOne())
                return real.toString() + "-I";
            else
                return real.toString() + "-I*" + imaginary.abs();
        if (abs.isOne())
            return real.toString() + "+I";
        else
            return real.toString() + "+I*" + imaginary.abs();

    }

    @Override
    protected String toString(OutputFormat mode, Class<? extends Tensor> clazz) {
        if (clazz == Product.class || clazz == Power.class) {
            if (!imaginary.isZero() || real.signum() < 0 || !real.isInteger())
                return "(" + toString(mode) + ")";
        }
        return toString(mode);
    }

    @Override
    public TensorBuilder getBuilder() {
        return new ComplexBuilder(this);
    }

    @Override
    public TensorFactory getFactory() {
        return null;
    }

    private static class ComplexBuilder implements TensorBuilder {

        private final Complex complex;

        public ComplexBuilder(Complex complex) {
            this.complex = complex;
        }

        @Override
        public Tensor build() {
            return complex;
        }

        @Override
        public void put(Tensor tensor) {
            throw new IllegalStateException("Can not put to Complex tensor builder!");
        }

        @Override
        public TensorBuilder clone() {
            return this;
        }
    }

    public Real getImaginary() {
        return imaginary;
    }

    public Real getReal() {
        return real;
    }

    public boolean isReal() {
        return imaginary.isZero();
    }

    public boolean isImaginary() {
        return real.isZero();
    }

    public Complex getImaginaryAsComplex() {
        if (imaginary.isOne())
            return ONE;
        if (imaginary.isZero())
            return ZERO;
        return new Complex(Rational.ZERO, imaginary);
    }

    public Complex getRealAsComplex() {
        if (real.isOne())
            return ONE;
        if (real.isZero())
            return ZERO;
        return new Complex(real);
    }

    @Override
    public boolean isNaN() {
        return real.isNaN() || imaginary.isNaN();
    }

    @Override
    public boolean isNumeric() {
        return real.isNumeric();
    }

    @Override
    public boolean isInfinite() {
        return real.isInfinite() || imaginary.isInfinite();
    }

    @Override
    public boolean isZero() {
        return real.isZero() && imaginary.isZero();
    }

    @Override
    public boolean isOne() {
        return real.isOne() && imaginary.isZero();
    }

    @Override
    public boolean isMinusOne() {
        return real.isMinusOne() && imaginary.isZero();
    }

    public boolean isOneOrMinusOne() {
        return imaginary.isZero() && (real.isOne() || real.isMinusOne());
    }

    /**
     * Returns double value of the real part.
     *
     * @return double value of the real part
     */
    @Override
    public double doubleValue() {
        return real.doubleValue();
    }

    /**
     * Returns float value of the real part.
     *
     * @return float value of the real part
     */
    @Override
    public float floatValue() {
        return real.floatValue();
    }

    /**
     * Returns int value of the real part.
     *
     * @return int value of the real part
     */
    @Override
    public int intValue() {
        return real.intValue();
    }

    /**
     * Returns long value of the real part.
     *
     * @return long value of the real part
     */
    @Override
    public long longValue() {
        return real.longValue();
    }

    /**
     * Return the conjugate of this complex number. The conjugate of {@code a + bi} is {@code a - bi}. <br/> {@link
     * #ComplexNaN} is returned if either the real or imaginary part of this Complex number equals {@code Double.NaN}.
     * <br/> If the imaginary part is infinite, and the real part is not {@code NaN}, the returned value has infinite
     * imaginary part of the opposite sign, e.g. the conjugate of {@code 1 + POSITIVE_INFINITY i} is {@code 1 -
     * NEGATIVE_INFINITY i}.
     *
     * @return the conjugate of this Complex object.
     */
    public Complex conjugate() {
        return new Complex(real, imaginary.negate());
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this + addend)}. Uses the definitional formula
     * <pre>
     * <code>
     *   (a + bi) + (c + di) = (a+c) + (b+d)i
     * </code>
     * </pre> <br/> If either {@code this} or {@code addend} has a {@code NaN}
     * value in either part, {@link #ComplexNaN} is returned; otherwise {@code Infinite} and {@code NaN} values are
     * returned in the parts of the result according to the rules for {@link java.lang.Double} arithmetic.
     *
     * @param a value to be added to this {@code Complex}.
     * @return {@code this + addend}.
     * @throws NullArgumentException if {@code addend} is {@code null}.
     */
    @Override
    public Complex add(Complex a) {
        NumberUtils.checkNotNull(a);
        return a.isZero() ? a.isNumeric() ? this.getNumericValue() : this : new Complex(real.add(a.real), imaginary.add(a.imaginary));
    }

    /**
     * Returns a {@code Complex} whose value is {@code (this / divisor)}. Implements the definitional formula
     * <pre>
     * <code>
     *    a + bi          ac + bd + (bc - ad)i
     *    ----------- = -------------------------
     *    c + di         c<sup>2</sup> + d<sup>2</sup>
     * </code>
     * </pre> but uses <a href="http://doi.acm.org/10.1145/1039813.1039814">
     * prescaling of operands</a> to limit the effects of overflows and underflows in the computation. <br/> {@code
     * Infinite} and {@code NaN} values are handled according to the following rules, applied in the order presented:
     * <ul> <li>If either {@code this} or {@code divisor} has a {@code NaN} value in either part, {@link #ComplexNaN} is
     * returned. </li> <li>If {@code divisor} equals {@link #ZERO}, {@link #ComplexNaN} is returned. </li> <li>If {@code
     * this} and {@code divisor} are both infinite, {@link #ComplexNaN} is returned. </li> <li>If {@code this} is finite
     * (i.e., has no {@code Infinite} or {@code NaN} parts) and {@code divisor} is infinite (one or both parts
     * infinite), {@link #ZERO} is returned. </li> <li>If {@code this} is infinite and {@code divisor} is finite, {@code
     * NaN} values are returned in the parts of the result if the {@link java.lang.Double} rules applied to the
     * definitional formula force {@code NaN} results. </li> </ul>
     *
     * @param divisor Value by which this {@code Complex} is to be divided.
     * @return {@code this / divisor}.
     * @throws NullArgumentException if {@code divisor} is {@code null}.
     */
    @Override
    public Complex divide(Complex divisor) {
        NumberUtils.checkNotNull(divisor);
        if (divisor.isOne())
            return divisor.isNumeric() ? this.getNumericValue() : this;
        if (divisor.isNaN() || isNaN())
            return ComplexNaN;


        final Real c = divisor.real;
        final Real d = divisor.imaginary;

        //        Real denominator = (c.multiply(c)).add(d.multiply(d));
        //        return new Complex(((real.multiply(c)).add(imaginary.multiply(d))).divide(denominator),
        //                           ((imaginary.multiply(c)).subtract(real.multiply(d))).divide(denominator));
        if (c.abs().compareTo(d.abs()) < 0) {
            Real q = c.divide(d);
            Real denominator = c.multiply(q).add(d);
            return new Complex((real.multiply(q).add(imaginary)).divide(denominator),
                    (imaginary.multiply(q).subtract(real)).divide(denominator));
        } else {
            Real q = d.divide(c);
            Real denominator = d.multiply(q).add(c);
            return new Complex(((imaginary.multiply(q)).add(real)).divide(denominator),
                    (imaginary.subtract(real.multiply(q))).divide(denominator));
        }
    }

    @Override
    public Field<Complex> getField() {
        return ComplexField.getInstance();
    }

    @Override
    public Complex multiply(int n) {
        return n == 1 ? this : new Complex(real.multiply(n), imaginary.multiply(n));
    }

    /**
     * Returns a {@code Complex} whose value is {@code this * factor}. Implements preliminary checks for {@code NaN} and
     * infinity followed by the definitional formula:
     * <pre>
     * <code>
     *   (a + bi)(c + di) = (ac - bd) + (ad + bc)i
     * </code>
     * </pre> Returns {@link #ComplexNaN} if either {@code this} or {@code factor} has
     * one or more {@code NaN} parts. <br/> Returns {@link #COMPLEX_INFINITY} if neither {@code this} nor {@code factor}
     * has one or more {@code NaN} parts and if either {@code this} or {@code factor} has one or more infinite parts
     * (same result is returned regardless of the sign of the components). <br/> Returns finite values in components of
     * the result per the definitional formula in all remaining cases.
     *
     * @param factor value to be multiplied by this {@code Complex}.
     * @return {@code this * factor}.
     * @throws NullArgumentException if {@code factor} is {@code null}.
     */
    @Override
    public Complex multiply(Complex factor) {
        NumberUtils.checkNotNull(factor);
        if (factor.isNaN())
            return ComplexNaN;
        return new Complex(real.multiply(factor.real).subtract(imaginary.multiply(factor.imaginary)),
                real.multiply(factor.imaginary).add(imaginary.multiply(factor.real)));
    }

    @Override
    public Complex negate() {
        return new Complex(real.negate(), imaginary.negate());
    }

    @Override
    public Complex reciprocal() {
        if (isNaN())
            return ComplexNaN;
        //        if (FastMath.abs(real) < FastMath.abs(imaginary)) {
        //            double q = real / imaginary;
        //            double scale = 1. / (real * q + imaginary);
        //            return createComplex(scale * q, -scale);
        //        } else {
        //            double q = imaginary / real;
        //            double scale = 1. / (imaginary * q + real);
        //            return createComplex(scale, -scale * q);
        //        }
        if (real.abs().compareTo(imaginary.abs()) < 0) {
            Real q = real.divide(imaginary);
            Real scale = (real.multiply(q).add(imaginary)).reciprocal();
            return new Complex(scale.multiply(q), scale.negate());
        } else {
            Real q = imaginary.divide(real);
            Real scale = (imaginary.multiply(q).add(real)).reciprocal();
            return new Complex(scale, scale.multiply(q).negate());
        }
    }

    @Override
    public Complex subtract(Complex a) {
        NumberUtils.checkNotNull(a);
        return a.isZero() ? a.isNumeric() ? this.getNumericValue() : this : new Complex(real.subtract(a.real), imaginary.subtract(a.imaginary));
    }

    @Override
    public Complex getNumericValue() {
        return isNumeric() ? this : new Complex(real.getNumericValue(), imaginary.getNumericValue());
    }

    @Override
    public Complex abs() {
        if (isZero() || isOne() || isInfinite() || isNaN())
            return this;
        Real abs2 = real.multiply(real).add(imaginary.multiply(imaginary));
        if (isNumeric())
            return new Complex(abs2.pow(0.5));
        Rational abs2r = (Rational) abs2;
        BigInteger num = abs2r.getNumerator();
        BigInteger den = abs2r.getDenominator();

        BigInteger nR = NumberUtils.sqrt(num);
        if (!NumberUtils.isSqrt(num, nR))
            throw new IllegalStateException();

        BigInteger dR = NumberUtils.sqrt(den);
        if (!NumberUtils.isSqrt(den, dR))
            throw new IllegalStateException();

        return new Complex(new Rational(nR, dR));
    }

    public double absNumeric() {
        if (isNaN())
            return Double.NaN;

        if (isInfinite())
            return Double.POSITIVE_INFINITY;

        final double real = this.real.doubleValue();
        final double imaginary = this.imaginary.doubleValue();

        return absNumeric(real, imaginary);
    }

    public static double absNumeric(final double real, final double imaginary) {
        if (FastMath.abs(real) < FastMath.abs(imaginary)) {
            if (imaginary == 0.0) {
                return FastMath.abs(real);
            }
            double q = real / imaginary;
            return FastMath.abs(imaginary) * FastMath.sqrt(1 + q * q);
        } else {
            if (real == 0.0) {
                return FastMath.abs(imaginary);
            }
            double q = imaginary / real;
            return FastMath.abs(real) * FastMath.sqrt(1 + q * q);
        }
    }


    @Override
    public Complex add(BigFraction fraction) {
        return fraction.compareTo(BigFraction.ZERO) == 0 ? this : new Complex(real.add(fraction), imaginary);
    }

    @Override
    public Complex add(double d) {
        return d == 0.0 ? this.getNumericValue() : new Complex(real.add(d), imaginary);
    }

    @Override
    public Complex add(long d) {
        return d == 0 ? this : new Complex(real.add(d), imaginary);
    }

    @Override
    public Complex add(int d) {
        return d == 0 ? this : new Complex(real.add(d), imaginary);
    }

    @Override
    public Complex add(BigInteger bg) {
        NumberUtils.checkNotNull(bg);
        return bg.equals(BigInteger.ZERO) ? this : new Complex(real.add(bg), imaginary);
    }

    @Override
    public Complex subtract(BigFraction fraction) {
        return fraction.compareTo(BigFraction.ZERO) == 0 ? this : new Complex(real.subtract(fraction), imaginary);
    }

    @Override
    public Complex subtract(double d) {
        return d == 0.0 ? this.getNumericValue() : new Complex(real.subtract(d), imaginary);
    }

    @Override
    public Complex subtract(long l) {
        return l == 0 ? this : new Complex(real.subtract(l), imaginary);
    }

    @Override
    public Complex subtract(int i) {
        return i == 0 ? this : new Complex(real.subtract(i), imaginary);
    }

    @Override
    public Complex subtract(BigInteger bg) {
        NumberUtils.checkNotNull(bg);
        return bg.equals(BigInteger.ZERO) ? this : new Complex(real.subtract(bg), imaginary);
    }

    @Override
    public Complex multiply(BigFraction fraction) {
        return fraction.compareTo(BigFraction.ONE) == 0 ? this : new Complex(real.multiply(fraction), imaginary.multiply(fraction));
    }

    @Override
    public Complex multiply(double d) {
        return d == 1.0 ? this.getNumericValue() : Double.isNaN(d) ? ComplexNaN : new Complex(real.multiply(d), imaginary.multiply(d));
    }

    @Override
    public Complex multiply(BigInteger bg) {
        return bg.compareTo(BigInteger.ONE) == 0 ? this : new Complex(real.multiply(bg), imaginary.multiply(bg));
    }

    @Override
    public Complex multiply(long d) {
        return d == 1 ? this : new Complex(real.multiply(d), imaginary.multiply(d));
    }

    @Override
    public Complex divide(BigFraction fraction) {
        return fraction.compareTo(BigFraction.ONE) == 0 ? this : new Complex(real.divide(fraction), imaginary.divide(fraction));
    }

    @Override
    public Complex divide(long l) {
        return l == 1 ? this : new Complex(real.divide(l), imaginary.divide(l));
    }

    @Override
    public Complex divide(int i) {
        return i == 1 ? this : new Complex(real.divide(i), imaginary.divide(i));
    }

    @Override
    public Complex divide(BigInteger bg) {
        return bg.compareTo(BigInteger.ONE) == 0 ? this : new Complex(real.divide(bg), imaginary.divide(bg));
    }

    @Override
    public Complex divide(double d) {
        return d == 1.0 ? this : new Complex(real.divide(d), imaginary.divide(d));
    }

    public Complex add(Real d) {
        NumberUtils.checkNotNull(d);
        return d.isZero() ? (d.isNumeric() ? this.getNumericValue() : this) : new Complex(real.add(d), imaginary.add(d));
    }

    public Complex subtract(Real d) {
        NumberUtils.checkNotNull(d);
        return d.isZero() ? (d.isNumeric() ? this.getNumericValue() : this) : new Complex(real.subtract(d), imaginary.add(d));
    }

    public Complex multiply(Real d) {
        NumberUtils.checkNotNull(d);
        return d.isOne() ? (d.isNumeric() ? this.getNumericValue() : this) : new Complex(real.multiply(d), imaginary.multiply(d));
    }

    public Complex divide(Real d) {
        NumberUtils.checkNotNull(d);
        return d.isOne() ? (d.isNumeric() ? this.getNumericValue() : this) : new Complex(real.divide(d), imaginary.divide(d));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Complex other = (Complex) obj;
        return real.equals(other.real) && imaginary.equals(other.imaginary);
    }

    /**
     * Returns na
     *
     * @return
     */
    public Complex logNumeric() {
        if (isNaN()) {
            return ComplexNaN;
        }

        final double real = this.real.doubleValue();
        final double imaginary = this.imaginary.doubleValue();

        return new Complex(FastMath.log(absNumeric(real, imaginary)),
                FastMath.atan2(imaginary, real));
    }


    @Override
    public Complex pow(double exponent) {
        return this.logNumeric().multiply(exponent).expNumeric();
    }

    @Override
    public Complex pow(BigInteger exponent) {
        if (exponent.compareTo(BigInteger.ZERO) < 0)
            return reciprocal().pow(exponent.negate());

        Complex result = Complex.ONE;
        Complex k2p = this;
        while (!BigInteger.ZERO.equals(exponent)) {
            if (exponent.testBit(0))
                result = result.multiply(k2p);

            k2p = k2p.multiply(k2p);
            exponent = exponent.shiftRight(1);
        }

        return result;
    }

    @Override
    public Complex pow(long exponent) {
        if (exponent < 0)
            return reciprocal().pow(-exponent);

        Complex result = Complex.ONE;
        Complex k2p = this;
        while (exponent != 0) {
            if ((exponent & 0x1) != 0)
                result = result.multiply(k2p);

            k2p = k2p.multiply(k2p);
            exponent = exponent >> 1;
        }

        return result;
    }

    @Override
    public Complex pow(int exponent) {
        if (exponent < 0)
            return reciprocal().pow(-exponent);

        Complex result = Complex.ONE;
        Complex k2p = this;
        while (exponent != 0) {
            if ((exponent & 0x1) != 0)
                result = result.multiply(k2p);

            k2p = k2p.multiply(k2p);
            exponent = exponent >> 1;
        }

        return result;
    }

    public Complex expNumeric() {
        if (isNaN()) {
            return ComplexNaN;
        }

        final double real = this.real.doubleValue();
        final double imaginary = this.imaginary.doubleValue();

        double expReal = FastMath.exp(real);
        return new Complex(expReal * FastMath.cos(imaginary),
                expReal * FastMath.sin(imaginary));
    }


    public Complex powNumeric(Complex exponent) {
        return this.logNumeric().multiply(exponent).expNumeric();
    }

    @Override
    public boolean isInteger() {
        return imaginary.isZero() && real.isInteger();
    }

    @Override
    public boolean isNatural() {
        return imaginary.isZero() && real.isNatural();
    }

    public boolean isNegativeInteger() {
        return imaginary.isZero() && real.isInteger() && real.signum() < 0;
    }
}
