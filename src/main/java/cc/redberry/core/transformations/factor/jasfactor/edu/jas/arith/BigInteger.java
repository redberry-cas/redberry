/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * BigInteger class to make java.math.BigInteger available with RingElem
 * respectively the GcdRingElem interface. Objects of this class are immutable.
 * The SAC2 static methods are also provided.
 *
 * @author Heinz Kredel
 * @see java.math.BigInteger
 */

public final class BigInteger implements GcdRingElem<BigInteger>, RingFactory<BigInteger>,
        Iterable<BigInteger> {


    /**
     * The data structure.
     */
    public final java.math.BigInteger val;


    private final static Random random = new Random();


    /**
     * The constant 0.
     */
    public final static BigInteger ZERO = new BigInteger(java.math.BigInteger.ZERO);


    /**
     * The constant 1.
     */
    public final static BigInteger ONE = new BigInteger(java.math.BigInteger.ONE);


    /**
     * Constructor for BigInteger from math.BigInteger.
     *
     * @param a java.math.BigInteger.
     */
    public BigInteger(java.math.BigInteger a) {
        val = a;
    }


    /**
     * Constructor for BigInteger from long.
     *
     * @param a long.
     */
    public BigInteger(long a) {
        val = new java.math.BigInteger(String.valueOf(a));
    }


    /**
     * Constructor for BigInteger from String.
     *
     * @param s String.
     */
    public BigInteger(String s) {
        val = new java.math.BigInteger(s.trim());
    }


    /**
     * Constructor for BigInteger without parameters.
     */
    public BigInteger() {
        val = java.math.BigInteger.ZERO;
    }


    /**
     * Get the value.
     *
     * @return val java.math.BigInteger.
     */
    public java.math.BigInteger getVal() {
        return val;
    }


    /**
     * Get the value as long.
     *
     * @return val as long.
     */
    public long longValue() {
        return val.longValue();
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public BigInteger factory() {
        return this;
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<BigInteger> generators() {
        List<BigInteger> g = new ArrayList<>(1);
        g.add(getONE());
        return g;
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return false;
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public BigInteger copy() {
        return new BigInteger(val);
    }


    /**
     * Copy BigInteger element c.
     *
     * @param c BigInteger.
     * @return a copy of c.
     */
    public BigInteger copy(BigInteger c) {
        return new BigInteger(c.val);
    }


    /**
     * Get the zero element.
     *
     * @return 0.
     */
    public BigInteger getZERO() {
        return ZERO;
    }


    /**
     * Get the one element.
     *
     * @return 1.
     */
    public BigInteger getONE() {
        return ONE;
    }


    /**
     * Query if this ring is commutative.
     *
     * @return true.
     */
    public boolean isCommutative() {
        return true;
    }


    /**
     * Query if this ring is associative.
     *
     * @return true.
     */
    public boolean isAssociative() {
        return true;
    }


    /**
     * Query if this ring is a field.
     *
     * @return false.
     */
    public boolean isField() {
        return false;
    }


    /**
     * Characteristic of this ring.
     *
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return java.math.BigInteger.ZERO;
    }


    /**
     * Get a BigInteger element from a math.BigInteger.
     *
     * @param a math.BigInteger.
     * @return a as BigInteger.
     */
    public BigInteger fromInteger(java.math.BigInteger a) {
        return new BigInteger(a);
    }


    /**
     * Get a BigInteger element from a math.BigInteger.
     *
     * @param a math.BigInteger.
     * @return a as BigInteger.
     */
    public static BigInteger valueOf(java.math.BigInteger a) {
        return new BigInteger(a);
    }


    /**
     * Get a BigInteger element from long.
     *
     * @param a long.
     * @return a as BigInteger.
     */
    public BigInteger fromInteger(long a) {
        return new BigInteger(a);
    }


    /**
     * Get a BigInteger element from long.
     *
     * @param a long.
     * @return a as BigInteger.
     */
    public static BigInteger valueOf(long a) {
        return new BigInteger(a);
    }


    /**
     * Is BigInteger number zero.
     *
     * @return If this is 0 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isZERO()
     */
    public boolean isZERO() {
        return val.equals(java.math.BigInteger.ZERO);
    }


    /**
     * Is BigInteger number one.
     *
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        return val.equals(java.math.BigInteger.ONE);
    }


    /**
     * Is BigInteger number unit.
     *
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isUnit()
     */
    public boolean isUnit() {
        return (this.isONE() || this.negate().isONE());
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
     * Compare to BigInteger b.
     *
     * @param b BigInteger.
     * @return 0 if this == b, 1 if this > b, -1 if this < b.
     */
    //JAVA6only: @Override
    public int compareTo(BigInteger b) {
        return val.compareTo(b.val);
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof BigInteger)) {
            return false;
        }
        BigInteger bi = (BigInteger) b;
        return val.equals(bi.val);
    }


    /**
     * Hash code for this BigInteger.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return val.hashCode();
    }


    /**
     * Absolute value of this.
     *
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#abs()
     */
    public BigInteger abs() {
        return new BigInteger(val.abs());
    }


    /* Negative value of this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#negate()
     */
    public BigInteger negate() {
        return new BigInteger(val.negate());
    }


    /**
     * signum.
     *
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#signum()
     */
    public int signum() {
        return val.signum();
    }

    /**
     * BigInteger subtract.
     *
     * @param S BigInteger.
     * @return this-S.
     */
    public BigInteger subtract(BigInteger S) {
        return new BigInteger(val.subtract(S.val));
    }

    /**
     * BigInteger divide.
     *
     * @param S BigInteger.
     * @return this/S.
     */
    public BigInteger divide(BigInteger S) {
        return new BigInteger(val.divide(S.val));
    }


    /**
     * Integer inverse. R is a non-zero integer. S=1/R if defined else 0.
     *
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#inverse()
     */
    public BigInteger inverse() {
        if (this.isONE() || this.negate().isONE()) {
            return this;
        }
        return ZERO;
    }


    /**
     * BigInteger remainder.
     *
     * @param S BigInteger.
     * @return this - (this/S)*S.
     */
    public BigInteger remainder(BigInteger S) {
        return new BigInteger(val.remainder(S.val));
    }


    /**
     * BigInteger compute quotient and remainder. Throws an exception, if S ==
     * 0.
     *
     * @param S BigInteger.
     * @return BigInteger[] { q, r } with this = q S + r and 0 &le; r &lt; |S|.
     */
    //@Override
    public BigInteger[] quotientRemainder(BigInteger S) {
        BigInteger[] qr = new BigInteger[2];
        java.math.BigInteger[] C = val.divideAndRemainder(S.val);
        qr[0] = new BigInteger(C[0]);
        qr[1] = new BigInteger(C[1]);
        return qr;
    }


    /**
     * BigInteger greatest common divisor.
     *
     * @param S BigInteger.
     * @return gcd(this, S).
     */
    public BigInteger gcd(BigInteger S) {
        return new BigInteger(val.gcd(S.val));
    }


    /**
     * BigInteger extended greatest common divisor.
     *
     * @param S BigInteger.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    public BigInteger[] egcd(BigInteger S) {
        BigInteger[] ret = new BigInteger[3];
        ret[0] = null;
        ret[1] = null;
        ret[2] = null;
        if (S == null || S.isZERO()) {
            ret[0] = this;
            return ret;
        }
        if (this.isZERO()) {
            ret[0] = S;
            return ret;
        }
        BigInteger[] qr;
        BigInteger q = this;
        BigInteger r = S;
        BigInteger c1 = ONE;
        BigInteger d1 = ZERO;
        BigInteger c2 = ZERO;
        BigInteger d2 = ONE;
        BigInteger x1;
        BigInteger x2;
        while (!r.isZERO()) {
            qr = q.quotientRemainder(r);
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
        if (q.signum() < 0) {
            q = q.negate();
            c1 = c1.negate();
            c2 = c2.negate();
        }
        ret[0] = q;
        ret[1] = c1;
        ret[2] = c2;
        return ret;
    }


    /**
     * BigInteger random.
     *
     * @param n such that 0 &le; r &le; (2<sup>n</sup>-1).
     * @return r, a random BigInteger.
     */
    public BigInteger random(int n) {
        return random(n, random);
    }


    /**
     * BigInteger random.
     *
     * @param n   such that 0 &le; r &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return r, a random BigInteger.
     */
    public BigInteger random(int n, Random rnd) {
        java.math.BigInteger r = new java.math.BigInteger(n, rnd);
        if (rnd.nextBoolean()) {
            r = r.negate();
        }
        return new BigInteger(r);
    }

    /**
     * BigInteger multiply.
     *
     * @param S BigInteger.
     * @return this*S.
     */
    public BigInteger multiply(BigInteger S) {
        return new BigInteger(val.multiply(S.val));
    }


    /**
     * BigInteger summation.
     *
     * @param S BigInteger.
     * @return this+S.
     */
    public BigInteger sum(BigInteger S) {
        return new BigInteger(val.add(S.val));
    }

    private boolean nonNegative = true;


    /**
     * Set the iteration algorithm to all elements.
     */
    public void setAllIterator() {
        nonNegative = false;
    }


    /**
     * Set the iteration algorithm to non-negative elements.
     */
    public void setNonNegativeIterator() {
        nonNegative = true;
    }


    /**
     * Get a BigInteger iterator.
     *
     * @return a iterator over all integers.
     */
    public Iterator<BigInteger> iterator() {
        return new BigIntegerIterator(nonNegative);
    }

}


/**
 * Big integer iterator.
 *
 * @author Heinz Kredel
 */
class BigIntegerIterator implements Iterator<BigInteger> {


    /**
     * data structure.
     */
    java.math.BigInteger curr;


    final boolean nonNegative;

    /**
     * BigInteger iterator constructor.
     *
     * @param nn true for an iterator over non-negative longs, false for all
     *           elements iterator.
     */
    public BigIntegerIterator(boolean nn) {
        curr = java.math.BigInteger.ZERO;
        nonNegative = nn;
    }


    /**
     * Test for availability of a next element.
     *
     * @return true if the iteration has more elements, else false.
     */
    public boolean hasNext() {
        return true;
    }


    /**
     * Get next integer.
     *
     * @return next integer.
     */
    public synchronized BigInteger next() {
        BigInteger i = new BigInteger(curr);
        if (nonNegative) {
            curr = curr.add(java.math.BigInteger.ONE);
        } else if (curr.signum() > 0 && !nonNegative) {
            curr = curr.negate();
        } else {
            curr = curr.negate().add(java.math.BigInteger.ONE);
        }
        return i;
    }


    /**
     * Remove an element if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove elements");
    }
}
