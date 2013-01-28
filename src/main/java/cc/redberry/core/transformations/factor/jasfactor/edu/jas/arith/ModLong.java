/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2013:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algebra System (JAS).
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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.NotInvertibleException;


/**
 * ModLong class with RingElem interface. Objects of this class are immutable.
 *
 * @author Heinz Kredel
 * @see ModInteger
 */

public final class ModLong implements GcdRingElem<ModLong>, Modular {


    /**
     * ModLongRing reference.
     */
    public final ModLongRing ring;


    /**
     * Value part of the element data structure.
     */
    public final long val;


    /**
     * The constructor creates a ModLong object from a ModLongRing and a value
     * part.
     *
     * @param m ModLongRing.
     * @param a math.BigInteger.
     */
    public ModLong(ModLongRing m, java.math.BigInteger a) {
        this(m, a.mod(new java.math.BigInteger("" + m.modul)).longValue());
    }


    /**
     * The constructor creates a ModLong object from a ModLongRing and a long
     * value part.
     *
     * @param m ModLongRing.
     * @param a long.
     */
    public ModLong(ModLongRing m, long a) {
        ring = m;
        long v = a % ring.modul;
        val = (v >= 0L ? v : v + ring.modul);
    }

    /**
     * Get the value part.
     *
     * @return val.
     */
    public long getVal() {
        return val;
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public ModLongRing factory() {
        return ring;
    }


    /**
     * Return a symmetric BigInteger from this Element.
     *
     * @return a symmetric BigInteger of this.
     */
    public BigInteger getSymmetricInteger() {
        long v = val;
        if ((val + val) > ring.modul) {
            // val > m/2 as 2*val > m, make symmetric to 0
            v = val - ring.modul;
        }
        return new BigInteger(v);
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public ModLong copy() {
        return new ModLong(ring, val);
    }


    /**
     * Is ModLong number zero.
     *
     * @return If this is 0 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isZERO()
     */
    public boolean isZERO() {
        return val == 0L;
    }


    /**
     * Is ModLong number one.
     *
     * @return If this is 1 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        return val == 1L;
    }


    /**
     * Is ModLong number a unit.
     *
     * @return If this is a unit then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isUnit()
     */
    public boolean isUnit() {
        if (isZERO()) {
            return false;
        }
        if (ring.isField()) {
            return true;
        }
        long g = gcd(ring.modul, val);
        return (g == 1L || g == -1L);
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "" + val;
    }


    /**
     * ModLong comparison.
     *
     * @param b ModLong.
     * @return sign(this-b).
     */
    //JAVA6only: @Override 
    public int compareTo(ModLong b) {
        long v = b.val;
        if (ring != b.ring) {
            v = v % ring.modul;
        }
        if (val > v) {
            return 1;
        }
        return (val < v ? -1 : 0);
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof ModLong)) {
            return false;
        }
        return (0 == compareTo((ModLong) b));
    }


    /**
     * Hash code for this ModLong.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) val;
    }


    /**
     * ModLong absolute value.
     *
     * @return the absolute value of this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#abs()
     */
    public ModLong abs() {
        return new ModLong(ring, (val < 0 ? -val : val));
    }


    /**
     * ModLong negative.
     *
     * @return -this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#negate()
     */
    public ModLong negate() {
        return new ModLong(ring, -val);
    }


    /**
     * ModLong signum.
     *
     * @return signum(this).
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#signum()
     */
    public int signum() {
        if (val > 0L) {
            return 1;
        }
        return (val < 0L ? -1 : 0);
    }


    /**
     * ModLong subtraction.
     *
     * @param S ModLong.
     * @return this-S.
     */
    public ModLong subtract(ModLong S) {
        return new ModLong(ring, val - S.val);
    }


    /**
     * ModLong divide.
     *
     * @param S ModLong.
     * @return this/S.
     */
    public ModLong divide(ModLong S) {
        try {
            return multiply(S.inverse());
        } catch (NotInvertibleException e) {
            try {
                if ((val % S.val) == 0L) {
                    return new ModLong(ring, val / S.val);
                }
                throw new NotInvertibleException(e.getCause());
            } catch (ArithmeticException a) {
                throw new NotInvertibleException(a.getCause());
            }
        }
    }


    /**
     * ModLong inverse.
     *
     * @return S with S=1/this if defined.
     * @throws NotInvertibleException if the element is not invertible.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#inverse()
     */
    public ModLong inverse() /*throws NotInvertibleException*/ {
        try {
            return new ModLong(ring, modInverse(val, ring.modul));
        } catch (ArithmeticException e) {
            long g = gcd(val, ring.modul);
            long f = ring.modul / g;
            throw new ModularNotInvertibleException(e, new BigInteger(ring.modul), new BigInteger(g),
                    new BigInteger(f));
        }
    }


    /**
     * ModLong remainder.
     *
     * @param S ModLong.
     * @return remainder(this, S).
     */
    public ModLong remainder(ModLong S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        if (S.isONE()) {
            return ring.getZERO();
        }
        if (S.isUnit()) {
            return ring.getZERO();
        }
        return new ModLong(ring, val % S.val);
    }


    /**
     * ModLong multiply.
     *
     * @param S ModLong.
     * @return this*S.
     */
    public ModLong multiply(ModLong S) {
        return new ModLong(ring, val * S.val);
    }


    /**
     * ModLong summation.
     *
     * @param S ModLong.
     * @return this+S.
     */
    public ModLong sum(ModLong S) {
        return new ModLong(ring, val + S.val);
    }


    /**
     * ModInteger greatest common divisor.
     *
     * @param S ModInteger.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    public ModLong gcd(ModLong S) {
        if (S.isZERO()) {
            return this;
        }
        if (isZERO()) {
            return S;
        }
        if (isUnit() || S.isUnit()) {
            return ring.getONE();
        }
        return new ModLong(ring, gcd(val, S.val));
    }


    /**
     * ModInteger extended greatest common divisor.
     *
     * @param S ModInteger.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    public ModLong[] egcd(ModLong S) {
        ModLong[] ret = new ModLong[3];
        ret[0] = null;
        ret[1] = null;
        ret[2] = null;
        if (S == null || S.isZERO()) {
            ret[0] = this;
            return ret;
        }
        if (isZERO()) {
            ret[0] = S;
            return ret;
        }
        if (isUnit() || S.isUnit()) {
            ret[0] = ring.getONE();
            if (isUnit() && S.isUnit()) {
                //ModLong half = (new ModLong(ring, 2L)).inverse();
                //ret[1] = this.inverse().multiply(half);
                //ret[2] = S.inverse().multiply(half);
                // (1-1*this)/S
                ret[1] = ring.getONE();
                ModLong x = ret[0].subtract(ret[1].multiply(this));
                ret[2] = x.divide(S);
                return ret;
            }
            if (isUnit()) {
                // oder inverse(S-1)?
                ret[1] = this.inverse();
                ret[2] = ring.getZERO();
                return ret;
            }
            // if ( s.isUnit() ) {
            // oder inverse(this-1)?
            ret[1] = ring.getZERO();
            ret[2] = S.inverse();
            return ret;
            //}
        }
        long q = this.val;
        long r = S.val;
        long c1 = 1L; // BigInteger.ONE.val;
        long d1 = 0L; // BigInteger.ZERO.val;
        long c2 = 0L; // BigInteger.ZERO.val;
        long d2 = 1L; // BigInteger.ONE.val;
        long x1;
        long x2;
        while (r != 0L) {
            //qr = q.divideAndRemainder(r);
            long a = q / r;
            long b = q % r;
            q = a;
            x1 = c1 - q * d1;
            x2 = c2 - q * d2;
            c1 = d1;
            c2 = d2;
            d1 = x1;
            d2 = x2;
            q = r;
            r = b;
        }
        ret[0] = new ModLong(ring, q);
        ret[1] = new ModLong(ring, c1);
        ret[2] = new ModLong(ring, c2);
        return ret;
    }


    /**
     * Long greatest common divisor.
     *
     * @param T long.
     * @param S long.
     * @return gcd(T, S).
     */
    public long gcd(long T, long S) {
        if (S == 0L) {
            return T;
        }
        if (T == 0L) {
            return S;
        }
        long a = T;
        long b = S;
        while (b != 0L) {
            long r = a % b;
            a = b;
            b = r;
        }
        return a;
    }


    /**
     * Long half extended greatest common divisor.
     *
     * @param T long.
     * @param S long.
     * @return [ gcd(T,S), a ] with a*T + b*S = gcd(T,S).
     */
    public long[] hegcd(long T, long S) {
        long[] ret = new long[2];
        if (S == 0L) {
            ret[0] = T;
            ret[1] = 1L;
            return ret;
        }
        if (T == 0L) {
            ret[0] = S;
            ret[1] = 0L;
            return ret;
        }
        long a = T;
        long b = S;
        long a1 = 1L;
        long b1 = 0L;
        while (b != 0L) {
            long q = a / b;
            long r = a % b;
            a = b;
            b = r;
            long r1 = a1 - q * b1;
            a1 = b1;
            b1 = r1;
        }
        if (a1 < 0L) {
            a1 += S;
        }
        ret[0] = a;
        ret[1] = a1;
        return ret;
    }


    /**
     * Long modular inverse.
     *
     * @param T long.
     * @param m long.
     * @return a with with a*T = 1 mod m.
     */
    public long modInverse(long T, long m) {
        if (T == 0L) {
            throw new NotInvertibleException("zero is not invertible");
        }
        long[] hegcd = hegcd(T, m);
        long a = hegcd[0];
        if (!(a == 1L || a == -1L)) { // gcd != 1
            throw new ModularNotInvertibleException("element not invertible, gcd != 1", new BigInteger(m),
                    new BigInteger(a), new BigInteger(m / a));
        }
        long b = hegcd[1];
        if (b == 0L) { // when m divides this, e.g. m.isUnit()
            throw new NotInvertibleException("element not invertible, divisible by modul");
        }
        if (b < 0L) {
            b += m;
        }
        return b;
    }

}
