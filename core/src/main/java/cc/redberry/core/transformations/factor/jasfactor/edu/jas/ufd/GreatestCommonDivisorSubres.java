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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;


/**
 * Greatest common divisor algorithms with subresultant polynomial remainder
 * sequence.
 *
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorSubres<C extends GcdRingElem<C>> extends GreatestCommonDivisorAbstract<C> {


    /**
     * Univariate GenPolynomial greatest comon divisor. Uses pseudoRemainder for
     * remainder.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<C> baseGcd(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<C> q;
        GenPolynomial<C> r;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else {
            q = P;
            r = S;
        }
        r = r.abs();
        q = q.abs();
        C a = baseContent(r);
        C b = baseContent(q);
        C c = gcd(a, b); // indirection
        r = divide(r, a); // indirection
        q = divide(q, b); // indirection
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        C g = r.ring.getONECoefficient();
        C h = r.ring.getONECoefficient();
        GenPolynomial<C> x;
        C z;
        while (!r.isZERO()) {
            long delta = q.degree(0) - r.degree(0);
            x = PolyUtil.baseDensePseudoRemainder(q, r);
            q = r;
            if (!x.isZERO()) {
                z = g.multiply(power(P.ring.coFac, h, delta));
                r = x.divide(z);
                g = q.leadingBaseCoefficient();
                z = power(P.ring.coFac, g, delta);
                h = z.divide(power(P.ring.coFac, h, delta - 1));
            } else {
                r = x;
            }
        }
        q = basePrimitivePart(q);
        return (q.multiply(c)).abs();
    }


    /**
     * Univariate GenPolynomial recursive greatest comon divisor. Uses
     * pseudoRemainder for remainder.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<GenPolynomial<C>> recursiveUnivariateGcd(GenPolynomial<GenPolynomial<C>> P,
                                                                  GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<GenPolynomial<C>> q;
        GenPolynomial<GenPolynomial<C>> r;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else {
            q = P;
            r = S;
        }
        r = r.abs();
        q = q.abs();
        GenPolynomial<C> a = recursiveContent(r);
        GenPolynomial<C> b = recursiveContent(q);

        GenPolynomial<C> c = gcd(a, b); // go to recursion
        r = PolyUtil.recursiveDivide(r, a);
        q = PolyUtil.recursiveDivide(q, b);
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        GenPolynomial<C> g = r.ring.getONECoefficient();
        GenPolynomial<C> h = r.ring.getONECoefficient();
        GenPolynomial<GenPolynomial<C>> x;
        GenPolynomial<C> z = null;
        while (!r.isZERO()) {
            long delta = q.degree(0) - r.degree(0);
            x = PolyUtil.recursiveDensePseudoRemainder(q, r);
            q = r;
            if (!x.isZERO()) {
                z = g.multiply(power(P.ring.coFac, h, delta));
                r = PolyUtil.recursiveDivide(x, z);
                g = q.leadingBaseCoefficient();
                z = power(P.ring.coFac, g, delta);
                h = PolyUtil.basePseudoDivide(z, power(P.ring.coFac, h, delta - 1));
            } else {
                r = x;
            }
        }
        q = recursivePrimitivePart(q);
        return q.abs().multiply(c); //.abs();
    }


    /**
     * Univariate GenPolynomial resultant. Uses pseudoRemainder for remainder.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return res(P, S).
     */
    @Override
    public GenPolynomial<C> baseResultant(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<C> q;
        GenPolynomial<C> r;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else {
            q = P;
            r = S;
        }
        //r = r.abs();
        //q = q.abs();
        C a = baseContent(r);
        C b = baseContent(q);
        r = divide(r, a); // indirection
        q = divide(q, b); // indirection
        RingFactory<C> cofac = P.ring.coFac;
        C g = cofac.getONE();
        C h = cofac.getONE();
        C t = power(cofac, a, e);
        t = t.multiply(power(cofac, b, f));
        long s = 1;
        GenPolynomial<C> x;
        C z;
        while (r.degree(0) > 0) {
            long delta = q.degree(0) - r.degree(0);
            if ((q.degree(0) % 2 != 0) && (r.degree(0) % 2 != 0)) {
                s = -s;
            }
            x = PolyUtil.baseDensePseudoRemainder(q, r);
            q = r;
            if (x.degree(0) > 0) {
                z = g.multiply(power(cofac, h, delta));
                r = x.divide(z);
                g = q.leadingBaseCoefficient();
                z = power(cofac, g, delta);
                h = z.divide(power(cofac, h, delta - 1));
            } else {
                r = x;
            }
        }
        z = power(cofac, r.leadingBaseCoefficient(), q.degree(0));
        h = z.divide(power(cofac, h, q.degree(0) - 1));
        z = h.multiply(t);
        if (s < 0) {
            z = z.negate();
        }
        x = P.ring.getONE().multiply(z);
        return x;
    }


    /**
     * Univariate GenPolynomial recursive resultant. Uses pseudoRemainder for
     * remainder.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return res(P, S).
     */
    @Override
    public GenPolynomial<GenPolynomial<C>> recursiveUnivariateResultant(GenPolynomial<GenPolynomial<C>> P,
                                                                        GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        if (P.ring.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " no univariate polynomial");
        }
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<GenPolynomial<C>> q;
        GenPolynomial<GenPolynomial<C>> r;
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
        } else {
            q = P;
            r = S;
        }
        r = r.abs();
        q = q.abs();
        GenPolynomial<C> a = recursiveContent(r);
        GenPolynomial<C> b = recursiveContent(q);
        r = PolyUtil.recursiveDivide(r, a);
        q = PolyUtil.recursiveDivide(q, b);
        RingFactory<GenPolynomial<C>> cofac = P.ring.coFac;
        GenPolynomial<C> g = cofac.getONE();
        GenPolynomial<C> h = cofac.getONE();
        GenPolynomial<GenPolynomial<C>> x;
        GenPolynomial<C> t;
        if (f == 0 && e == 0 && g.ring.nvar > 0) {
            // if coeffs are multivariate (and non constant)
            // otherwise it would be 1
            t = resultant(a, b);
            x = P.ring.getONE().multiply(t);
            return x;
        }
        t = power(cofac, a, e);
        t = t.multiply(power(cofac, b, f));
        long s = 1;
        GenPolynomial<C> z;
        while (r.degree(0) > 0) {
            long delta = q.degree(0) - r.degree(0);
            if ((q.degree(0) % 2 != 0) && (r.degree(0) % 2 != 0)) {
                s = -s;
            }
            x = PolyUtil.recursiveDensePseudoRemainder(q, r);
            q = r;
            if (x.degree(0) > 0) {
                z = g.multiply(power(P.ring.coFac, h, delta));
                r = PolyUtil.recursiveDivide(x, z);
                g = q.leadingBaseCoefficient();
                z = power(cofac, g, delta);
                h = PolyUtil.basePseudoDivide(z, power(cofac, h, delta - 1));
            } else {
                r = x;
            }
        }
        z = power(cofac, r.leadingBaseCoefficient(), q.degree(0));
        h = PolyUtil.basePseudoDivide(z, power(cofac, h, q.degree(0) - 1));
        z = h.multiply(t);
        if (s < 0) {
            z = z.negate();
        }
        x = P.ring.getONE().multiply(z);
        return x;
    }


    /**
     * Coefficient power.
     *
     * @param A coefficient
     * @param i exponent.
     * @return A^i.
     */
    C power(RingFactory<C> fac, C A, long i) {
        return Power.power(fac, A, i);
    }


    /**
     * Polynomial power.
     *
     * @param A polynomial.
     * @param i exponent.
     * @return A^i.
     */
    GenPolynomial<C> power(RingFactory<GenPolynomial<C>> fac, GenPolynomial<C> A, long i) {
        return Power.power(fac, A, i);
    }


}
