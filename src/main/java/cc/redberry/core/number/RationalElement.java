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

public abstract class RationalElement implements FieldElement<RationalElement> {
    protected final long numerator;
    public static final RationalElement ONE = new NumberSimple(1);
    public static final RationalElement ZERO = new NumberSimple(0);
    public static final RationalElement MINUS_ONE = new NumberSimple(-1);

    @Override
    public RationalElementField getField() {
        return RationalElementField.getInstance();
    }

    protected RationalElement(long value) {
        this.numerator = value;
    }

    protected enum FracMode {
        FRACTION,
        SIMPLENUMBER;
    };
    protected FracMode mode;

    public long getNumerator() {
        return numerator;
    }

    public RationalElement compileFraction(long numer, long denom) throws ArithmeticException {
        if (denom == 0)
            throw new ArithmeticException("divide by zero");
        if (denom == 1)
            return new NumberSimple(numer);
        if (denom == -1)
            return new NumberSimple(-numer);
        long gcd = MathUtils.gcd(numer, denom);
        long numerGcd = numer / (gcd);
        long denomGcd = denom / (gcd);
        if (denomGcd == 1)
            return new NumberSimple(numerGcd);
        if (denomGcd == -1)
            return new NumberSimple(-(numerGcd));
        return new NumberFraction(numerGcd, denomGcd);
    }

    public abstract RationalElement getReduce();

    boolean positive() {
        return numerator >= 0;
    }

    public abstract RationalElement abs();

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().isInstance(this.getClass()))
            return false;
        return isEquals((RationalElement) obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (int) (this.numerator ^ (this.numerator >>> 32));
        return hash;
    }

    @Override
    public abstract RationalElement clone();

    public abstract boolean isNegotive();

    public boolean isZero() {
        return numerator == 0;
    }

    public boolean isOne() {
        return equals(ONE);
    }

    public boolean isMinusOne() {
        return equals(MINUS_ONE);
    }

    public RationalElement pow(RationalElement power) {
        if (power instanceof NumberFraction)
            return this;
        RationalElement r = NumberSimple.ONE;
        long val = power.numerator;
        if (val == 0)
            return r;
        for (int i = 0; i < val; ++i)
            r = r.multiply(this);
        return r;
    }
}
