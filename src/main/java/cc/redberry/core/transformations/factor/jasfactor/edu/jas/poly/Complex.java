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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;


/**
 * Generic Complex class implementing the RingElem interface. Objects of this
 * class are immutable.
 *
 * @param <C> base type of RingElem (for complex polynomials).
 * @author Heinz Kredel
 */
public class Complex<C extends RingElem<C>> implements GcdRingElem<Complex<C>> {


    private static final boolean debug = false;


    /**
     * Complex class factory data structure.
     */
    public final ComplexRing<C> ring;


    /**
     * Real part of the data structure.
     */
    protected final C re;


    /**
     * Imaginary part of the data structure.
     */
    protected final C im;


    /**
     * The constructor creates a Complex object from two C objects as real and
     * imaginary part.
     *
     * @param ring factory for Complex objects.
     * @param r    real part.
     * @param i    imaginary part.
     */
    public Complex(ComplexRing<C> ring, C r, C i) {
        this.ring = ring;
        re = r;
        im = i;
    }


    /**
     * The constructor creates a Complex object from a C object as real part,
     * the imaginary part is set to 0.
     *
     * @param r real part.
     */
    public Complex(ComplexRing<C> ring, C r) {
        this(ring, r, ring.ring.getZERO());
    }


    /**
     * The constructor creates a Complex object from a long element as real
     * part, the imaginary part is set to 0.
     *
     * @param r real part.
     */
    public Complex(ComplexRing<C> ring, long r) {
        this(ring, ring.ring.fromInteger(r));
    }


    /**
     * The constructor creates a Complex object with real part 0 and imaginary
     * part 0.
     */
    public Complex(ComplexRing<C> ring) {
        this(ring, ring.ring.getZERO());
    }

    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public ComplexRing<C> factory() {
        return ring;
    }


    /**
     * Get the real part.
     *
     * @return re.
     */
    public C getRe() {
        return re;
    }


    /**
     * Get the imaginary part.
     *
     * @return im.
     */
    public C getIm() {
        return im;
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Complex<C> copy() {
        return new Complex<>(ring, re, im);
    }


    /**
     * Get the String representation.
     */
    @Override
    public String toString() {
        String s = re.toString();
        if (im.isZERO()) {
            return s;
        }
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
        return re.isZERO() && im.isZERO();
    }


    /**
     * Is Complex number one.
     *
     * @return If this is 1 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        return re.isONE() && im.isZERO();
    }


    /**
     * Is Complex imaginary one.
     *
     * @return If this is i then true is returned, else false.
     */
    public boolean isIMAG() {
        return re.isZERO() && im.isONE();
    }


    /**
     * Is Complex unit element.
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
        return norm().re.isUnit();
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object b) {
        if (!(b instanceof Complex)) {
            return false;
        }
        Complex<C> bc = null;
        try {
            bc = (Complex<C>) b;
        } catch (ClassCastException e) {
        }
        if (bc == null) {
            return false;
        }
        if (!ring.equals(bc.ring)) {
            return false;
        }
        return re.equals(bc.re) && im.equals(bc.im);
    }


    /**
     * Hash code for this Complex.
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
    public int compareTo(Complex<C> b) {
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
     * @param B a Complex<C> number.
     * @return this+B.
     */
    public Complex<C> sum(Complex<C> B) {
        return new Complex<>(ring, re.sum(B.re), im.sum(B.im));
    }


    /**
     * Complex number subtract.
     *
     * @param B a Complex<C> number.
     * @return this-B.
     */
    public Complex<C> subtract(Complex<C> B) {
        return new Complex<>(ring, re.subtract(B.re), im.subtract(B.im));
    }


    /**
     * Complex number negative.
     *
     * @return -this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#negate()
     */
    public Complex<C> negate() {
        return new Complex<>(ring, re.negate(), im.negate());
    }


    /* arithmetic operations: conjugate, absolut value 
     */

    /**
     * Complex number conjugate.
     *
     * @return the complex conjugate of this.
     */
    public Complex<C> conjugate() {
        return new Complex<>(ring, re, im.negate());
    }


    /**
     * Complex number norm.
     *
     * @return ||this||.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.StarRingElem#norm()
     */
    public Complex<C> norm() {
        // this.conjugate().multiply(this);
        C v = re.multiply(re);
        v = v.sum(im.multiply(im));
        return new Complex<>(ring, v);
    }


    /**
     * Complex number absolute value.
     *
     * @return |this|^2. <b>Note:</b> The square root is not jet implemented.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#abs()
     */
    public Complex<C> abs() {
        Complex<C> n = norm();
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
    public Complex<C> multiply(Complex<C> B) {
        return new Complex<>(ring, re.multiply(B.re).subtract(im.multiply(B.im)), re.multiply(B.im).sum(
                im.multiply(B.re)));
    }


    /**
     * Complex number inverse.
     *
     * @return S with S*this = 1, if it is defined.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#inverse()
     */
    public Complex<C> inverse() {
        C a = norm().re.inverse();
        return new Complex<>(ring, re.multiply(a), im.multiply(a.negate()));
    }


    /**
     * Complex number remainder.
     *
     * @param S is a complex number.
     * @return 0.
     */
    public Complex<C> remainder(Complex<C> S) {
        if (ring.isField()) {
            return ring.getZERO();
        }
        return quotientRemainder(S)[1];
    }


    /**
     * Complex number divide.
     *
     * @param B is a complex number, non-zero.
     * @return this/B.
     */
    public Complex<C> divide(Complex<C> B) {
        if (ring.isField()) {
            return this.multiply(B.inverse());
        }
        return quotientRemainder(B)[0];
    }


    /**
     * Complex number quotient and remainder.
     *
     * @param S Complex.
     * @return Complex[] { q, r } with q = this/S and r = rem(this,S).
     */
    @SuppressWarnings("unchecked")
    public Complex<C>[] quotientRemainder(Complex<C> S) {
        Complex<C>[] ret = new Complex[2];
        C n = S.norm().re;
        Complex<C> Sp = this.multiply(S.conjugate()); // == this*inv(S)*n
        C qr = Sp.re.divide(n);
        C rr = Sp.re.remainder(n);
        C qi = Sp.im.divide(n);
        C ri = Sp.im.remainder(n);
        C rr1 = rr;
        C ri1 = ri;
        if (rr.signum() < 0) {
            rr = rr.negate();
        }
        if (ri.signum() < 0) {
            ri = ri.negate();
        }
        C one = n.factory().fromInteger(1);
        if (rr.sum(rr).compareTo(n) > 0) { // rr > n/2
            if (rr1.signum() < 0) {
                qr = qr.subtract(one);
            } else {
                qr = qr.sum(one);
            }
        }
        if (ri.sum(ri).compareTo(n) > 0) { // ri > n/2
            if (ri1.signum() < 0) {
                qi = qi.subtract(one);
            } else {
                qi = qi.sum(one);
            }
        }
        Sp = new Complex<>(ring, qr, qi);
        Complex<C> Rp = this.subtract(Sp.multiply(S));
        ret[0] = Sp;
        ret[1] = Rp;
        return ret;
    }


    /**
     * Complex number greatest common divisor.
     *
     * @param S Complex<C>.
     * @return gcd(this, S).
     */
    public Complex<C> gcd(Complex<C> S) {
        if (S == null || S.isZERO()) {
            return this;
        }
        if (this.isZERO()) {
            return S;
        }
        if (ring.isField()) {
            return ring.getONE();
        }
        Complex<C> a = this;
        Complex<C> b = S;
        if (a.re.signum() < 0) {
            a = a.negate();
        }
        if (b.re.signum() < 0) {
            b = b.negate();
        }
        while (!b.isZERO()) {
            if (debug) {
            }
            Complex<C>[] qr = a.quotientRemainder(b);
            if (qr[0].isZERO()) {
            }
            a = b;
            b = qr[1];
        }
        if (a.re.signum() < 0) {
            a = a.negate();
        }
        return a;
    }


    /**
     * Complex extended greatest common divisor.
     *
     * @param S Complex<C>.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    @SuppressWarnings("unchecked")
    public Complex<C>[] egcd(Complex<C> S) {
        Complex<C>[] ret = new Complex[3];
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
        if (ring.isField()) {
            Complex<C> half = new Complex<>(ring, ring.ring.fromInteger(1).divide(ring.ring.fromInteger(2)));
            ret[0] = ring.getONE();
            ret[1] = this.inverse().multiply(half);
            ret[2] = S.inverse().multiply(half);
            return ret;
        }
        Complex<C>[] qr;
        Complex<C> q = this;
        Complex<C> r = S;
        Complex<C> c1 = ring.getONE();
        Complex<C> d1 = ring.getZERO();
        Complex<C> c2 = ring.getZERO();
        Complex<C> d2 = ring.getONE();
        Complex<C> x1;
        Complex<C> x2;
        while (!r.isZERO()) {
            if (debug) {
            }
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
        if (q.re.signum() < 0) {
            q = q.negate();
            c1 = c1.negate();
            c2 = c2.negate();
        }
        ret[0] = q;
        ret[1] = c1;
        ret[2] = c2;
        return ret;
    }

}
