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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;


/**
 * Greatest common divisor algorithms with monic polynomial remainder sequence.
 * If C is a field, then the monic PRS (on coefficients) is computed otherwise
 * no simplifications in the reduction are made.
 *
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorSimple<C extends GcdRingElem<C>> extends GreatestCommonDivisorAbstract<C> {


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
        boolean field = P.ring.coFac.isField();
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
        C c;
        if (field) {
            r = r.monic();
            q = q.monic();
            c = P.ring.getONECoefficient();
        } else {
            r = r.abs();
            q = q.abs();
            C a = baseContent(r);
            C b = baseContent(q);
            c = gcd(a, b); // indirection
            r = divide(r, a); // indirection
            q = divide(q, b); // indirection
        }
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        GenPolynomial<C> x;
        while (!r.isZERO()) {
            x = PolyUtil.baseSparsePseudoRemainder(q, r);
            q = r;
            if (field) {
                r = x.monic();
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
        boolean field = P.leadingBaseCoefficient().ring.coFac.isField();
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
        if (field) {
            r = PolyUtil.monic(r);
            q = PolyUtil.monic(q);
        } else {
            r = r.abs();
            q = q.abs();
        }
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
        GenPolynomial<GenPolynomial<C>> x;
        while (!r.isZERO()) {
            x = PolyUtil.recursivePseudoRemainder(q, r);
            q = r;
            if (field) {
                r = PolyUtil.monic(x);
            } else {
                r = x;
            }
        }
        q = recursivePrimitivePart(q);
        q = q.abs().multiply(c);
        return q;
    }


    /**
     * Univariate GenPolynomial resultant.
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
        if (P.ring.nvar > 1 || P.ring.nvar == 0) {
            throw new IllegalArgumentException("no univariate polynomial");
        }
        long e = P.degree(0);
        long f = S.degree(0);
        if (f == 0 && e == 0) {
            return P.ring.getONE();
        }
        if (e == 0) {
            return Power.power(P.ring, P, f);
        }
        if (f == 0) {
            return Power.power(S.ring, S, e);
        }
        GenPolynomial<C> q;
        GenPolynomial<C> r;
        int s = 0; // sign is +, 1 for sign is -
        if (e < f) {
            r = P;
            q = S;
            long t = e;
            e = f;
            f = t;
            if ((e % 2 != 0) && (f % 2 != 0)) { // odd(e) && odd(f)
                s = 1;
            }
        } else {
            q = P;
            r = S;
        }
        RingFactory<C> cofac = P.ring.coFac;
        boolean field = cofac.isField();
        C c = cofac.getONE();
        GenPolynomial<C> x;
        long g;
        do {
            if (field) {
                x = q.remainder(r);
            } else {
                x = PolyUtil.baseSparsePseudoRemainder(q, r);
            }
            if (x.isZERO()) {
                return x;
            }
            e = q.degree(0);
            f = r.degree(0);
            if ((e % 2 != 0) && (f % 2 != 0)) { // odd(e) && odd(f)
                s = 1 - s;
            }
            g = x.degree(0);
            C c2 = r.leadingBaseCoefficient();
            for (int i = 0; i < (e - g); i++) {
                c = c.multiply(c2);
            }
            q = r;
            r = x;
        } while (g != 0);
        C c2 = r.leadingBaseCoefficient();
        for (int i = 0; i < f; i++) {
            c = c.multiply(c2);
        }
        if (s == 1) {
            c = c.negate();
        }
        x = P.ring.getONE().multiply(c);
        return x;
    }


    /**
     * Univariate GenPolynomial recursive resultant.
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
        if (P.ring.nvar > 1 || P.ring.nvar == 0) {
            throw new IllegalArgumentException("no recursive univariate polynomial");
        }
        long e = P.degree(0);
        long f = S.degree(0);
        if (f == 0 && e == 0) {
            // if coeffs are multivariate (and non constant)
            // otherwise it would be 1
            GenPolynomial<C> t = resultant(P.leadingBaseCoefficient(), S.leadingBaseCoefficient());
            return P.ring.getONE().multiply(t);
        }
        if (e == 0) {
            return Power.power(P.ring, P, f);
        }
        if (f == 0) {
            return Power.power(S.ring, S, e);
        }
        GenPolynomial<GenPolynomial<C>> q;
        GenPolynomial<GenPolynomial<C>> r;
        int s = 0; // sign is +, 1 for sign is -
        if (f > e) {
            r = P;
            q = S;
            long g = f;
            f = e;
            e = g;
            if ((e % 2 != 0) && (f % 2 != 0)) { // odd(e) && odd(f)
                s = 1;
            }
        } else {
            q = P;
            r = S;
        }
        GenPolynomial<GenPolynomial<C>> x;
        RingFactory<GenPolynomial<C>> cofac = P.ring.coFac;
        GenPolynomial<C> c = cofac.getONE();
        long g;
        do {
            x = PolyUtil.recursiveSparsePseudoRemainder(q, r);
            //x = PolyUtil.<C>recursiveDensePseudoRemainder(q,r);
            if (x.isZERO()) {
                return x;
            }
            //no: x = recursivePrimitivePart(x);
            e = q.degree(0);
            f = r.degree(0);
            if ((e % 2 != 0) && (f % 2 != 0)) { // odd(e) && odd(f)
                s = 1 - s;
            }
            g = x.degree(0);
            GenPolynomial<C> c2 = r.leadingBaseCoefficient();
            for (int i = 0; i < (e - g); i++) {
                c = c.multiply(c2);
            }
            q = r;
            r = x;
        } while (g != 0);
        GenPolynomial<C> c2 = r.leadingBaseCoefficient();
        for (int i = 0; i < f; i++) {
            c = c.multiply(c2);
        }
        if (s == 1) {
            c = c.negate();
        }
        x = P.ring.getONE().multiply(c);
        return x;
    }

}
