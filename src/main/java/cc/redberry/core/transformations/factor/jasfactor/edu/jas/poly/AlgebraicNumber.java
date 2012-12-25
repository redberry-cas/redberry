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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.NotInvertibleException;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;


/**
 * Algebraic number class based on GenPolynomial with RingElem interface.
 * Objects of this class are immutable.
 *
 * @author Heinz Kredel
 */

public class AlgebraicNumber<C extends RingElem<C>> implements GcdRingElem<AlgebraicNumber<C>> {


    /**
     * Ring part of the data structure.
     */
    public final AlgebraicNumberRing<C> ring;


    /**
     * Value part of the element data structure.
     */
    public final GenPolynomial<C> val;


    /**
     * Flag to remember if this algebraic number is a unit. -1 is unknown, 1 is
     * unit, 0 not a unit.
     */
    protected int isunit = -1; // initially unknown


    /**
     * The constructor creates a AlgebraicNumber object from AlgebraicNumberRing
     * modul and a GenPolynomial value.
     *
     * @param r ring AlgebraicNumberRing<C>.
     * @param a value GenPolynomial<C>.
     */
    public AlgebraicNumber(AlgebraicNumberRing<C> r, GenPolynomial<C> a) {
        ring = r; // assert r != 0
        val = a.remainder(ring.modul); //.monic() no go
        if (val.isZERO()) {
            isunit = 0;
        }
        if (ring.isField()) {
            isunit = 1;
        }
    }


    /**
     * The constructor creates a AlgebraicNumber object from a GenPolynomial
     * object module.
     *
     * @param r ring AlgebraicNumberRing<C>.
     */
    public AlgebraicNumber(AlgebraicNumberRing<C> r) {
        this(r, r.ring.getZERO());
    }


    /**
     * Get the value part.
     *
     * @return val.
     */
    public GenPolynomial<C> getVal() {
        return val;
    }


    /**
     * Get the corresponding element factory.
     *
     * @return factory for this Element.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Element#factory()
     */
    public AlgebraicNumberRing<C> factory() {
        return ring;
    }


    /**
     * Clone this.
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public AlgebraicNumber<C> copy() {
        return new AlgebraicNumber<>(ring, val);
    }


    /**
     * Is AlgebraicNumber zero.
     *
     * @return If this is 0 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isZERO()
     */
    public boolean isZERO() {
        return val.equals(ring.ring.getZERO());
    }


    /**
     * Is AlgebraicNumber one.
     *
     * @return If this is 1 then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isONE()
     */
    public boolean isONE() {
        return val.equals(ring.ring.getONE());
    }


    /**
     * Is AlgebraicNumber unit.
     *
     * @return If this is a unit then true is returned, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#isUnit()
     */
    public boolean isUnit() {
        if (isunit > 0) {
            return true;
        }
        if (isunit == 0) {
            return false;
        }
        // not jet known
        if (val.isZERO()) {
            isunit = 0;
            return false;
        }
        if (ring.isField()) {
            isunit = 1;
            return true;
        }
        boolean u = val.gcd(ring.modul).isUnit();
        if (u) {
            isunit = 1;
        } else {
            isunit = 0;
        }
        return (u);
    }


    /**
     * Get the String representation as RingElem.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return val.toString(ring.ring.vars);
    }


    /**
     * AlgebraicNumber comparison.
     *
     * @param b AlgebraicNumber.
     * @return sign(this-b).
     */
    //JAVA6only: @Override
    public int compareTo(AlgebraicNumber<C> b) {
        int s = 0;
        if (ring.modul != b.ring.modul) { // avoid compareTo if possible
            s = ring.modul.compareTo(b.ring.modul);
        }
        if (s != 0) {
            return s;
        }
        return val.compareTo(b.val);
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    // not jet working
    public boolean equals(Object b) {
        if (!(b instanceof AlgebraicNumber)) {
            return false;
        }
        AlgebraicNumber<C> a = null;
        try {
            a = (AlgebraicNumber<C>) b;
        } catch (ClassCastException e) {
        }
        if (a == null) {
            return false;
        }
        if (!ring.equals(a.ring)) {
            return false;
        }
        return (0 == compareTo(a));
    }


    /**
     * Hash code for this AlgebraicNumber.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 37 * val.hashCode() + ring.hashCode();
    }


    /**
     * AlgebraicNumber absolute value.
     *
     * @return the absolute value of this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#abs()
     */
    public AlgebraicNumber<C> abs() {
        return new AlgebraicNumber<>(ring, val.abs());
    }


    /**
     * AlgebraicNumber summation.
     *
     * @param S AlgebraicNumber.
     * @return this+S.
     */
    public AlgebraicNumber<C> sum(AlgebraicNumber<C> S) {
        return new AlgebraicNumber<>(ring, val.sum(S.val));
    }


    /**
     * AlgebraicNumber summation.
     *
     * @param c coefficient.
     * @return this+c.
     */
    public AlgebraicNumber<C> sum(GenPolynomial<C> c) {
        return new AlgebraicNumber<>(ring, val.sum(c));
    }


    /**
     * AlgebraicNumber summation.
     *
     * @param c polynomial.
     * @return this+c.
     */
    public AlgebraicNumber<C> sum(C c) {
        return new AlgebraicNumber<>(ring, val.sum(c));
    }


    /**
     * AlgebraicNumber negate.
     *
     * @return -this.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#negate()
     */
    public AlgebraicNumber<C> negate() {
        return new AlgebraicNumber<>(ring, val.negate());
    }


    /**
     * AlgebraicNumber signum.
     *
     * @return signum(this).
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#signum()
     */
    public int signum() {
        return val.signum();
    }


    /**
     * AlgebraicNumber subtraction.
     *
     * @param S AlgebraicNumber.
     * @return this-S.
     */
    public AlgebraicNumber<C> subtract(AlgebraicNumber<C> S) {
        return new AlgebraicNumber<>(ring, val.subtract(S.val));
    }


    /**
     * AlgebraicNumber division.
     *
     * @param S AlgebraicNumber.
     * @return this/S.
     */
    public AlgebraicNumber<C> divide(AlgebraicNumber<C> S) {
        return multiply(S.inverse());
    }


    /**
     * AlgebraicNumber inverse.
     *
     * @return S with S = 1/this if defined.
     * @throws NotInvertibleException if the element is not invertible.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem#inverse()
     */
    public AlgebraicNumber<C> inverse() {
        try {
            return new AlgebraicNumber<>(ring, val.modInverse(ring.modul));
        } catch (AlgebraicNotInvertibleException e) {
            throw e;
        } catch (NotInvertibleException e) {
            throw new AlgebraicNotInvertibleException(e + ", val = " + val + ", modul = " + ring.modul + ", gcd = "
                    + val.gcd(ring.modul), e);
        }
    }


    /**
     * AlgebraicNumber remainder.
     *
     * @param S AlgebraicNumber.
     * @return this - (this/S)*S.
     */
    public AlgebraicNumber<C> remainder(AlgebraicNumber<C> S) {
        if (S == null || S.isZERO()) {
            throw new ArithmeticException("division by zero");
        }
        if (S.isONE()) {
            return ring.getZERO();
        }
        if (S.isUnit()) {
            return ring.getZERO();
        }
        GenPolynomial<C> x = val.remainder(S.val);
        return new AlgebraicNumber<>(ring, x);
    }


    /**
     * AlgebraicNumber multiplication.
     *
     * @param S AlgebraicNumber.
     * @return this*S.
     */
    public AlgebraicNumber<C> multiply(AlgebraicNumber<C> S) {
        GenPolynomial<C> x = val.multiply(S.val);
        return new AlgebraicNumber<>(ring, x);
    }


    /**
     * AlgebraicNumber multiplication.
     *
     * @param c coefficient.
     * @return this*c.
     */
    public AlgebraicNumber<C> multiply(C c) {
        GenPolynomial<C> x = val.multiply(c);
        return new AlgebraicNumber<>(ring, x);
    }


    /**
     * AlgebraicNumber multiplication.
     *
     * @param c polynomial.
     * @return this*c.
     */
    public AlgebraicNumber<C> multiply(GenPolynomial<C> c) {
        GenPolynomial<C> x = val.multiply(c);
        return new AlgebraicNumber<>(ring, x);
    }


    /**
     * AlgebraicNumber monic.
     *
     * @return this with monic value part.
     */
    public AlgebraicNumber<C> monic() {
        return new AlgebraicNumber<>(ring, val.monic());
    }


    /**
     * AlgebraicNumber greatest common divisor.
     *
     * @param S AlgebraicNumber.
     * @return gcd(this, S).
     */
    public AlgebraicNumber<C> gcd(AlgebraicNumber<C> S) {
        if (S.isZERO()) {
            return this;
        }
        if (isZERO()) {
            return S;
        }
        if (isUnit() || S.isUnit()) {
            return ring.getONE();
        }
        return new AlgebraicNumber<>(ring, val.gcd(S.val));
    }


    /**
     * AlgebraicNumber extended greatest common divisor.
     *
     * @param S AlgebraicNumber.
     * @return [ gcd(this,S), a, b ] with a*this + b*S = gcd(this,S).
     */
    @SuppressWarnings("unchecked")
    public AlgebraicNumber<C>[] egcd(AlgebraicNumber<C> S) {
        AlgebraicNumber<C>[] ret = new AlgebraicNumber[3];
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
                AlgebraicNumber<C> half = ring.fromInteger(2).inverse();
                ret[1] = this.inverse().multiply(half);
                ret[2] = S.inverse().multiply(half);
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
        GenPolynomial<C>[] qr;
        GenPolynomial<C> q = this.val;
        GenPolynomial<C> r = S.val;
        GenPolynomial<C> c1 = ring.ring.getONE();
        GenPolynomial<C> d1 = ring.ring.getZERO();
        GenPolynomial<C> c2 = ring.ring.getZERO();
        GenPolynomial<C> d2 = ring.ring.getONE();
        GenPolynomial<C> x1;
        GenPolynomial<C> x2;
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
        ret[0] = new AlgebraicNumber<>(ring, q);
        ret[1] = new AlgebraicNumber<>(ring, c1);
        ret[2] = new AlgebraicNumber<>(ring, c2);
        return ret;
    }

}
