/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2014:
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
 * ModInteger class with GcdRingElem interface. Objects of this class are
 * immutable. The SAC2 static methods are also provided.
 *
 * @author Heinz Kredel
 * @see java.math.BigInteger
 */

public final class ModInteger implements GcdRingElem<ModInteger>, Modular {


    /**
     * ModIntegerRing reference.
     */
    public final ModIntegerRing ring;


    /**
     * Value part of the element data structure.
     */
    public final java.math.BigInteger val;


    /**
     * The constructor creates a ModInteger object from a ModIntegerRing and a
     * value part.
     *
     * @param m ModIntegerRing.
     * @param a math.BigInteger.
     */
    public ModInteger(ModIntegerRing m, java.math.BigInteger a) {
        ring = m;
        val = a.mod(ring.modul);
    }


    /**
     * The constructor creates a ModInteger object from a ModIntegerRing and a
     * long value part.
     *
     * @param m ModIntegerRing.
     * @param a long.
     */
    public ModInteger(ModIntegerRing m, long a) {
        this(m, new java.math.BigInteger(String.valueOf(a)));
    }


    /**
     * The constructor creates a ModInteger object from a ModIntegerRing and a
     * String value part.
     *
     * @param m ModIntegerRing.
     * @param s String.
     */
    public ModInteger(ModIntegerRing m, String s) {
        this(m, new java.math.BigInteger(s.trim()));
    }


    /**
     * The constructor creates a 0 ModInteger object from a given
     * ModIntegerRing.
     *
     * @param m ModIntegerRing.
     */
    public ModInteger(ModIntegerRing m) {
        this(m, java.math.BigInteger.ZERO);
    }


    /**
     * Get the value part.
     *
     * @return val.
     */
    public java.math.BigInteger getVal() {
        return val;
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public ModIntegerRing factory() {
        return ring;
    }

    /**
     * Return a symmetric BigInteger from this Element.
     *
     * @return a symmetric BigInteger of this.
     */
    public BigInteger getSymmetricInteger() {
        java.math.BigInteger v = val;
        if (val.add(val).compareTo(ring.modul) > 0) {
            // val > m/2 as 2*val > m, make symmetric to 0
            v = val.subtract(ring.modul);
        }
        return new BigInteger(v);
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public ModInteger copy() {
        return new ModInteger(ring, val);
    }


    /**
     * Is ModInteger number zero.
     *
     * @return If this is 0 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isZERO()
     */
    public boolean isZERO() {
        return val.equals(java.math.BigInteger.ZERO);
    }


    /**
     * Is ModInteger number one.
     *
     * @return If this is 1 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        return val.equals(java.math.BigInteger.ONE);
    }


    /**
     * Is ModInteger number a unit.
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
        java.math.BigInteger g = ring.modul.gcd(val).abs();
        return (g.equals(java.math.BigInteger.ONE));
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return val.toString();
    }


    /**
     * ModInteger comparison.
     *
     * @param b ModInteger.
     * @return sign(this-b).
     */
    //JAVA6only: @Override
    public int compareTo(ModInteger b) {
        java.math.BigInteger v = b.val;
        if (ring != b.ring) {
            v = v.mod(ring.modul);
        }
        return val.compareTo(v);
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof ModInteger)) {
            return false;
        }
        return (0 == compareTo((ModInteger) b));
    }


    /**
     * Hash code for this ModInteger.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        //return 37 * val.hashCode();
        return val.hashCode();
    }


    /**
     * ModInteger absolute value.
     *
     * @return the absolute value of this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#abs()
     */
    public ModInteger abs() {
        return new ModInteger(ring, val.abs());
    }


    /**
     * ModInteger negative.
     *
     * @return -this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#negate()
     */
    public ModInteger negate() {
        return new ModInteger(ring, val.negate());
    }

    /**
     * ModInteger signum.
     *
     * @return signum(this).
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#signum()
     */
    public int signum() {
        return val.signum();
    }


    /**
     * ModInteger subtraction.
     *
     * @param S ModInteger.
     * @return this-S.
     */
    public ModInteger subtract(ModInteger S) {
        return new ModInteger(ring, val.subtract(S.val));
    }


    /**
     * ModInteger divide.
     *
     * @param S ModInteger.
     * @return this/S.
     */
    public ModInteger divide(ModInteger S) {
        try {
            return multiply(S.inverse());
        } catch (NotInvertibleException e) {
            try {
                if (val.remainder(S.val).equals(java.math.BigInteger.ZERO)) {
                    return new ModInteger(ring, val.divide(S.val));
                }
                throw new NotInvertibleException(e);
            } catch (ArithmeticException a) {
                throw new NotInvertibleException(a);
            }
        }
    }

    /**
     * ModInteger inverse.
     *
     * @return S with S=1/this if defined.
     * @throws NotInvertibleException if the element is not invertible.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#inverse()
     */
    public ModInteger inverse() /*throws NotInvertibleException*/ {
        try {
            return new ModInteger(ring, val.modInverse(ring.modul));
        } catch (ArithmeticException e) {
            java.math.BigInteger g = val.gcd(ring.modul);
            java.math.BigInteger f = ring.modul.divide(g);
            throw new ModularNotInvertibleException(e, new BigInteger(ring.modul), new BigInteger(g),
                    new BigInteger(f));
        }
    }


    /**
     * ModInteger remainder.
     *
     * @param S ModInteger.
     * @return remainder(this, S).
     */
    public ModInteger remainder(ModInteger S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        if (S.isONE()) {
            return ring.getZERO();
        }
        if (S.isUnit()) {
            return ring.getZERO();
        }
        return new ModInteger(ring, val.remainder(S.val));
    }


    /**
     * ModInteger multiply.
     *
     * @param S ModInteger.
     * @return this*S.
     */
    public ModInteger multiply(ModInteger S) {
        return new ModInteger(ring, val.multiply(S.val));
    }


    /**
     * ModInteger summation.
     *
     * @param S ModInteger.
     * @return this+S.
     */
    public ModInteger sum(ModInteger S) {
        return new ModInteger(ring, val.add(S.val));
    }


    /**
     * ModInteger greatest common divisor.
     *
     * @param S ModInteger.
     * @return gcd(this, S).
     */
    public ModInteger gcd(ModInteger S) {
        if (S.isZERO()) {
            return this;
        }
        if (isZERO()) {
            return S;
        }
        if (isUnit() || S.isUnit()) {
            return ring.getONE();
        }
        return new ModInteger(ring, val.gcd(S.val));
    }

    /**
     * ModInteger extended greatest common divisor.
     *
     * @param S ModInteger.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    public ModInteger[] egcd(ModInteger S) {
        ModInteger[] ret = new ModInteger[3];
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
        if (this.isUnit() || S.isUnit()) {
            ret[0] = ring.getONE();
            if (this.isUnit() && S.isUnit()) {
                //ModInteger half = ring.fromInteger(2).inverse();
                //ret[1] = this.inverse().multiply(half);
                //ret[2] = S.inverse().multiply(half);
                // (1-1*this)/S
                ret[1] = ring.getONE();
                ModInteger x = ret[0].subtract(ret[1].multiply(this));
                ret[2] = x.divide(S);
                return ret;
            }
            if (this.isUnit()) {
                // oder inverse(S-1)?
                ret[1] = this.inverse();
                ret[2] = ring.getZERO();
                return ret;
            }
            // if ( S.isUnit() ) {
            // oder inverse(this-1)?
            ret[1] = ring.getZERO();
            ret[2] = S.inverse();
            return ret;
            //}
        }
        java.math.BigInteger[] qr;
        java.math.BigInteger q = this.val;
        java.math.BigInteger r = S.val;
        java.math.BigInteger c1 = BigInteger.ONE.val;
        java.math.BigInteger d1 = BigInteger.ZERO.val;
        java.math.BigInteger c2 = BigInteger.ZERO.val;
        java.math.BigInteger d2 = BigInteger.ONE.val;
        java.math.BigInteger x1;
        java.math.BigInteger x2;
        while (!r.equals(java.math.BigInteger.ZERO)) {
            qr = q.divideAndRemainder(r);
            q = qr[0];
            x1 = c1.subtract(q.multiply(d1));
            x2 = c2.subtract(q.multiply(d2));
            c1 = d1;
            c2 = d2;
            d1 = x1;
            d2 = x2;
            q = r;
            r = qr[1];
        }
        ret[0] = new ModInteger(ring, q);
        ret[1] = new ModInteger(ring, c1);
        ret[2] = new ModInteger(ring, c2);
        return ret;
    }

}
