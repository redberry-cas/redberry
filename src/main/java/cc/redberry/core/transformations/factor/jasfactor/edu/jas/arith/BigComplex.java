/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2012:
 *    Heinz Kredel   <kredel@rz.uni-mannheim.de>
 *
 * This file is part of Java Algeba System (JAS).
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * BigComplex class based on BigRational implementing the RingElem respectively
 * the StarRingElem interface. Objects of this class are immutable. The SAC2
 * static methods are also provided.
 *
 * @author Heinz Kredel
 */
public final class BigComplex implements GcdRingElem<BigComplex>,
        RingFactory<BigComplex> {


    /**
     * Real part of the data structure.
     */
    public final BigRational re;


    /**
     * Imaginary part of the data structure.
     */
    public final BigRational im;


    private final static Random random = new Random();


    /**
     * The constructor creates a BigComplex object from two BigRational objects
     * real and imaginary part.
     *
     * @param r real part.
     * @param i imaginary part.
     */
    public BigComplex(BigRational r, BigRational i) {
        re = r;
        im = i;
    }


    /**
     * The constructor creates a BigComplex object from a BigRational object as
     * real part, the imaginary part is set to 0.
     *
     * @param r real part.
     */
    public BigComplex(BigRational r) {
        this(r, BigRational.ZERO);
    }

    /**
     * The constructor creates a BigComplex object with real part 0 and
     * imaginary part 0.
     */
    public BigComplex() {
        this(BigRational.ZERO);
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public BigComplex factory() {
        return this;
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<BigComplex> generators() {
        List<BigComplex> g = new ArrayList<>(2);
        g.add(getONE());
        g.add(getIMAG());
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
    public BigComplex copy() {
        return new BigComplex(re, im);
    }


    /**
     * Copy BigComplex element c.
     *
     * @param c BigComplex.
     * @return a copy of c.
     */
    public BigComplex copy(BigComplex c) {
        return new BigComplex(c.re, c.im);
    }


    /**
     * Get the zero element.
     *
     * @return 0 as BigComplex.
     */
    public BigComplex getZERO() {
        return ZERO;
    }


    /**
     * Get the one element.
     *
     * @return 1 as BigComplex.
     */
    public BigComplex getONE() {
        return ONE;
    }


    /**
     * Get the i element.
     *
     * @return i as BigComplex.
     */
    public BigComplex getIMAG() {
        return I;
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
     * @return true.
     */
    public boolean isField() {
        return true;
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
     * Get a BigComplex element from a BigInteger.
     *
     * @param a BigInteger.
     * @return a BigComplex.
     */
    public BigComplex fromInteger(BigInteger a) {
        return new BigComplex(new BigRational(a));
    }


    /**
     * Get a BigComplex element from a long.
     *
     * @param a long.
     * @return a BigComplex.
     */
    public BigComplex fromInteger(long a) {
        return new BigComplex(new BigRational(a));
    }


    /**
     * The constant 0.
     */
    public static final BigComplex ZERO = new BigComplex();


    /**
     * The constant 1.
     */
    public static final BigComplex ONE = new BigComplex(BigRational.ONE);


    /**
     * The constant i.
     */
    public static final BigComplex I = new BigComplex(BigRational.ZERO, BigRational.ONE);


    /**
     * Get the real part.
     *
     * @return re.
     */
    public BigRational getRe() {
        return re;
    }


    /**
     * Get the imaginary part.
     *
     * @return im.
     */
    public BigRational getIm() {
        return im;
    }


    /**
     * Get the String representation.
     */
    @Override
    public String toString() {
        String s = "" + re;
        int i = im.compareTo(BigRational.ZERO);
        if (i == 0)
            return s;
        s += "i" + im;
        return s;
    }


    /**
     * Is Complex number zero.
     *
     * @return If this is 0 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isZERO()
     */
    public boolean isZERO() {
        return re.equals(BigRational.ZERO) && im.equals(BigRational.ZERO);
    }

    /**
     * Is Complex number one.
     *
     * @return If this is 1 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        return re.equals(BigRational.ONE) && im.equals(BigRational.ZERO);
    }


    /**
     * Is Complex unit element.
     *
     * @return If this is a unit then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isUnit()
     */
    public boolean isUnit() {
        return (!isZERO());
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof BigComplex)) {
            return false;
        }
        BigComplex bc = (BigComplex) b;
        return re.equals(bc.re) && im.equals(bc.im);
    }


    /**
     * Hash code for this BigComplex.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 37 * re.hashCode() + im.hashCode();
    }


    /**
     * Since complex numbers are unordered, we use lexicographical order of re
     * and im.
     *
     * @return 0 if this is equal to b; 1 if re > b.re, or re == b.re and im >
     *         b.im; -1 if re < b.re, or re == b.re and im < b.im
     */
    //JAVA6only: @Override
    public int compareTo(BigComplex b) {
        int s = re.compareTo(b.re);
        if (s != 0) {
            return s;
        }
        return im.compareTo(b.im);
    }


    /**
     * Since complex numbers are unordered, we use lexicographical order of re
     * and im.
     *
     * @return 0 if this is equal to 0; 1 if re > 0, or re == 0 and im > 0; -1
     *         if re < 0, or re == 0 and im < 0
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#signum()
     */
    public int signum() {
        int s = re.signum();
        if (s != 0) {
            return s;
        }
        return im.signum();
    }


    /* arithmetic operations: +, -, -
     */

    /**
     * Complex number summation.
     *
     * @param B a BigComplex number.
     * @return this+B.
     */
    public BigComplex sum(BigComplex B) {
        return new BigComplex(re.sum(B.re), im.sum(B.im));
    }


    /**
     * Complex number subtract.
     *
     * @param B a BigComplex number.
     * @return this-B.
     */
    public BigComplex subtract(BigComplex B) {
        return new BigComplex(re.subtract(B.re), im.subtract(B.im));
    }


    /**
     * Complex number negative.
     *
     * @return -this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#negate()
     */
    public BigComplex negate() {
        return new BigComplex(re.negate(), im.negate());
    }


    /* arithmetic operations: conjugate, absolut value 
     */

    /**
     * Complex number conjugate.
     *
     * @return the complex conjugate of this.
     */
    public BigComplex conjugate() {
        return new BigComplex(re, im.negate());
    }


    /**
     * Complex number norm.
     *
     * @return ||this||.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.StarRingElem#norm()
     */
    public BigComplex norm() {
        // this.conjugate().multiply(this);
        BigRational v = re.multiply(re);
        v = v.sum(im.multiply(im));
        return new BigComplex(v);
    }


    /**
     * Complex number absolute value.
     *
     * @return |this|^2. Note: The square root is not jet implemented.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#abs()
     */
    public BigComplex abs() {
        BigComplex n = norm();
        // n = n.sqrt();
        return n;
    }

    /* arithmetic operations: *, inverse, / 
     */


    /**
     * Complex number product.
     *
     * @param B is a complex number.
     * @return this*B.
     */
    public BigComplex multiply(BigComplex B) {
        return new BigComplex(re.multiply(B.re).subtract(im.multiply(B.im)), re.multiply(B.im).sum(
                im.multiply(B.re)));
    }


    /**
     * Complex number inverse.
     *
     * @return S with S*this = 1.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#inverse()
     */
    public BigComplex inverse() {
        BigRational a = norm().re.inverse();
        return new BigComplex(re.multiply(a), im.multiply(a.negate()));
    }


    /**
     * Complex number inverse.
     *
     * @param S is a complex number.
     * @return 0.
     */
    public BigComplex remainder(BigComplex S) {
        if (S.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        return ZERO;
    }

    /**
     * Complex number divide.
     *
     * @param B is a complex number, non-zero.
     * @return this/B.
     */
    public BigComplex divide(BigComplex B) {
        return this.multiply(B.inverse());
    }


    /**
     * Complex number, random. Random rational numbers A and B are generated
     * using random(n). Then R is the complex number with real part A and
     * imaginary part B.
     *
     * @param n such that 0 &le; A, B &le; (2<sup>n</sup>-1).
     * @return R.
     */
    public BigComplex random(int n) {
        return random(n, random);
    }


    /**
     * Complex number, random. Random rational numbers A and B are generated
     * using random(n). Then R is the complex number with real part A and
     * imaginary part B.
     *
     * @param n   such that 0 &le; A, B &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return R.
     */
    public BigComplex random(int n, Random rnd) {
        BigRational r = BigRational.ONE.random(n, rnd);
        BigRational i = BigRational.ONE.random(n, rnd);
        return new BigComplex(r, i);
    }

    /**
     * Complex number greatest common divisor.
     *
     * @param S BigComplex.
     * @return gcd(this, S).
     */
    public BigComplex gcd(BigComplex S) {
        if (S == null || S.isZERO()) {
            return this;
        }
        if (this.isZERO()) {
            return S;
        }
        return ONE;
    }


    /**
     * BigComplex extended greatest common divisor.
     *
     * @param S BigComplex.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    public BigComplex[] egcd(BigComplex S) {
        BigComplex[] ret = new BigComplex[3];
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
        BigComplex half = new BigComplex(new BigRational(1, 2));
        ret[0] = ONE;
        ret[1] = this.inverse().multiply(half);
        ret[2] = S.inverse().multiply(half);
        return ret;
    }

}
