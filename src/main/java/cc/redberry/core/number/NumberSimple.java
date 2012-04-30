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

public class NumberSimple extends RationalElement {
    public NumberSimple(long value) {
        super(value);
        mode = FracMode.SIMPLENUMBER;
    }

    public RationalElement add(RationalElement a) {
        switch (a.mode) {
            case FRACTION:
                return a.add(this);
            case SIMPLENUMBER:
                NumberSimple t = (NumberSimple) a;
                return new NumberSimple(numerator + (t.numerator));
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement subtract(RationalElement a) {
        switch (a.mode) {
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                long numer = numerator * (f.denominator) - (f.numerator);
                return compileFraction(numer, f.denominator);
            case SIMPLENUMBER:
                return new NumberSimple(numerator - (a.numerator));
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement multiply(RationalElement a) {
        switch (a.mode) {
            case FRACTION:
                return a.multiply(this);
            case SIMPLENUMBER:
                return new NumberSimple(numerator * (a.numerator));
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement divide(RationalElement a) throws ArithmeticException {
        switch (a.mode) {
            case FRACTION:
                NumberFraction f = (NumberFraction) a;
                return compileFraction(numerator * (f.denominator), f.numerator);
            case SIMPLENUMBER:
                return compileFraction(numerator, a.numerator);
            default:
                throw new RuntimeException("frac mode aAAAA");
        }
    }

    public RationalElement negotiate() {
        return new NumberSimple(-numerator);
    }

    public boolean isEquals(RationalElement a) {
        if (!(a instanceof NumberSimple))
            return false;
        NumberSimple t = (NumberSimple) a;
        return numerator == (t.numerator);
    }

    @Override
    public RationalElement abs() {
        return new NumberSimple(numerator >= 0 ? numerator : -numerator);
    }

    @Override
    public RationalElement getReduce() {
        return this;
    }

    @Override
    public RationalElement clone() {
        return new NumberSimple(numerator);
    }

    @Override
    public String toString() {
        return String.valueOf(numerator);
    }

    @Override
    public boolean isNegotive() {
        return numerator < 0;
    }
}
