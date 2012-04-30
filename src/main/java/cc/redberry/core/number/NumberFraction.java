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

import cc.redberry.core.math.MathUtils;

public class NumberFraction extends RationalElement {
    protected final long denominator;
    protected final long gcd;

    public NumberFraction(long numerator, long denominator) {
        // Making negative only numerator. Denominator always positive
        super(denominator < 0 ? -numerator : numerator);
        if (denominator == 0)
            throw new ArithmeticException("divide by zero");
        if (denominator < 0)
            denominator = -denominator;
        this.denominator = denominator;
        mode = FracMode.FRACTION;
        gcd = MathUtils.gcd(numerator, denominator);
    }

    public long getDenominator() {
        return denominator;
    }

    public RationalElement add(RationalElement a) {
        switch (a.mode) {
            case SIMPLENUMBER:
                long numer = numerator + (a.numerator * (denominator));
                return compileFraction(numer, denominator);
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                long _gcd = MathUtils.gcd(denominator, f.denominator);
                long d1 = denominator / _gcd,
                 d2 = f.denominator / _gcd;
                return compileFraction(numerator * d2 + f.numerator * d1, _gcd * d1 * d2);
//                return compileFraction(numerator * (f.denominator) + (f.numerator * (denominator)), denominator * (f.denominator));
            default: {
            }
            throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement subtract(RationalElement a) {
        switch (a.mode) {
            case SIMPLENUMBER:
                long numer = numerator - (a.numerator * (denominator));
                return compileFraction(numer, denominator);
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                return compileFraction(numerator * (f.denominator) - (f.numerator * (denominator)), denominator * (f.denominator));
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement multiply(RationalElement a) {
        switch (a.mode) {
            case SIMPLENUMBER:
                return compileFraction(numerator * (a.numerator), denominator);
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                return compileFraction(numerator * f.numerator, denominator * f.denominator);
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement divide(RationalElement a) throws ArithmeticException {
        switch (a.mode) {
            case SIMPLENUMBER:
                return compileFraction(numerator, denominator * a.numerator);
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                return compileFraction(numerator * f.denominator, denominator * f.numerator);
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement negotiate() {
        return new NumberFraction(-numerator, denominator);
    }

    public boolean isEquals(RationalElement a) {
        switch (a.mode) {
            case SIMPLENUMBER:
                return false;
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                return numerator == (f.numerator) && denominator == (f.denominator);
            default:
                return false;
        }
    }

    @Override
    public RationalElement getReduce() {
        long numerGcd = numerator / (gcd);
        long denomGcd = denominator / (gcd);
        if (denomGcd == 1)
            return new NumberSimple(numerGcd);
        if (denomGcd == -1)
            return new NumberSimple(-numerGcd);
        return this;
    }

    @Override
    public RationalElement abs() {
        return new NumberFraction(numerator >= 0 ? numerator : -numerator, denominator);
    }

    @Override
    public RationalElement clone() {
        return new NumberFraction(numerator, denominator);
    }

    @Override
    public String toString() {
        return numerator + "/" + denominator;
    }

    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.denominator ^ (this.denominator >>> 32));
        hash = 97 * hash + super.hashCode();
        return hash;
    }

    @Override
    public boolean isNegotive() {
        return numerator < 0;
    }
}
