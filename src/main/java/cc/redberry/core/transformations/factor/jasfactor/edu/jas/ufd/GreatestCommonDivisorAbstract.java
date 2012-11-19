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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.ArrayList;
import java.util.List;


/**
 * Greatest common divisor algorithms.
 *
 * @author Heinz Kredel
 */

public abstract class GreatestCommonDivisorAbstract<C extends GcdRingElem<C>> implements
        GreatestCommonDivisor<C> {


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName();
    }


    /**
     * GenPolynomial base coefficient content.
     *
     * @param P GenPolynomial.
     * @return cont(P).
     */
    public C baseContent(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P.ring.getZEROCoefficient();
        }
        C d = null;
        for (C c : P.getMap().values()) {
            if (d == null) {
                d = c;
            } else {
                d = d.gcd(c);
            }
            if (d.isONE()) {
                return d;
            }
        }
        if (d.signum() < 0) {
            d = d.negate();
        }
        return d;
    }


    /**
     * GenPolynomial base coefficient primitive part.
     *
     * @param P GenPolynomial.
     * @return pp(P).
     */
    public GenPolynomial<C> basePrimitivePart(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        C d = baseContent(P);
        if (d.isONE()) {
            return P;
        }
        GenPolynomial<C> pp = P.divide(d);
        return pp;
    }


    /**
     * Univariate GenPolynomial greatest common divisor. Uses sparse
     * pseudoRemainder for remainder.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P, S).
     */
    public abstract GenPolynomial<C> baseGcd(GenPolynomial<C> P, GenPolynomial<C> S);


    /**
     * GenPolynomial recursive content.
     *
     * @param P recursive GenPolynomial.
     * @return cont(P).
     */
    public GenPolynomial<C> recursiveContent(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P.ring.getZEROCoefficient();
        }
        GenPolynomial<C> d = null;
        for (GenPolynomial<C> c : P.getMap().values()) {
            if (d == null) {
                d = c;
            } else {
                d = gcd(d, c); // go to recursion
            }
            if (d.isONE()) {
                return d;
            }
        }
        return d.abs();
    }


    /**
     * GenPolynomial recursive primitive part.
     *
     * @param P recursive GenPolynomial.
     * @return pp(P).
     */
    public GenPolynomial<GenPolynomial<C>> recursivePrimitivePart(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomial<C> d = recursiveContent(P);
        if (d.isONE()) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> pp = PolyUtil.recursiveDivide(P, d);
        return pp;
    }


    /**
     * GenPolynomial base recursive content.
     *
     * @param P recursive GenPolynomial.
     * @return baseCont(P).
     */
    public C baseRecursiveContent(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            GenPolynomialRing<C> cf = (GenPolynomialRing<C>) P.ring.coFac;
            return cf.coFac.getZERO();
        }
        C d = null;
        for (GenPolynomial<C> c : P.getMap().values()) {
            C cc = baseContent(c);
            if (d == null) {
                d = cc;
            } else {
                d = gcd(d, cc);
            }
            if (d.isONE()) {
                return d;
            }
        }
        if (d.signum() < 0) {
            d = d.negate();
        }
        return d;
    }


    /**
     * GenPolynomial base recursive primitive part.
     *
     * @param P recursive GenPolynomial.
     * @return basePP(P).
     */
    public GenPolynomial<GenPolynomial<C>> baseRecursivePrimitivePart(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        C d = baseRecursiveContent(P);
        if (d.isONE()) {
            return P;
        }
        GenPolynomial<GenPolynomial<C>> pp = PolyUtil.baseRecursiveDivide(P, d);
        return pp;
    }


    /**
     * Univariate GenPolynomial recursive greatest common divisor. Uses
     * pseudoRemainder for remainder.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P, S).
     */
    public abstract GenPolynomial<GenPolynomial<C>> recursiveUnivariateGcd(GenPolynomial<GenPolynomial<C>> P,
                                                                           GenPolynomial<GenPolynomial<C>> S);


    /**
     * GenPolynomial content.
     *
     * @param P GenPolynomial.
     * @return cont(P).
     */
    public GenPolynomial<C> content(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            // baseContent not possible by return type
            throw new IllegalArgumentException(this.getClass().getName()
                    + " use baseContent for univariate polynomials");

        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<>(cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.recursive(rfac, P);
        GenPolynomial<C> D = recursiveContent(Pr);
        return D;
    }

    /**
     * GenPolynomial division. Indirection to GenPolynomial method.
     *
     * @param a GenPolynomial.
     * @param b coefficient.
     * @return a/b.
     */
    public GenPolynomial<C> divide(GenPolynomial<C> a, C b) {
        if (b == null || b.isZERO()) {
            throw new IllegalArgumentException("division by zero");

        }
        if (a == null || a.isZERO()) {
            return a;
        }
        return a.divide(b);
    }


    /**
     * Coefficient greatest common divisor. Indirection to coefficient method.
     *
     * @param a coefficient.
     * @param b coefficient.
     * @return gcd(a, b).
     */
    public C gcd(C a, C b) {
        if (b == null || b.isZERO()) {
            return a;
        }
        if (a == null || a.isZERO()) {
            return b;
        }
        return a.gcd(b);
    }


    /**
     * GenPolynomial greatest common divisor.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P, S).
     */
    public GenPolynomial<C> gcd(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            GenPolynomial<C> T = baseGcd(P, S);
            return T;
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac;
        if (pfac.getVars() != null && pfac.getVars().length > 0) {
            String[] v = new String[]{pfac.getVars()[pfac.nvar - 1]};
            rfac = new GenPolynomialRing<>(cfac, 1, v);
        } else {
            rfac = new GenPolynomialRing<>(cfac, 1);
        }
        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.recursive(rfac, P);
        GenPolynomial<GenPolynomial<C>> Sr = PolyUtil.recursive(rfac, S);
        GenPolynomial<GenPolynomial<C>> Dr = recursiveUnivariateGcd(Pr, Sr);
        GenPolynomial<C> D = PolyUtil.distribute(pfac, Dr);
        return D;
    }


    /**
     * GenPolynomial least common multiple.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return lcm(P, S).
     */
    public GenPolynomial<C> lcm(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomial<C> C = gcd(P, S);
        GenPolynomial<C> A = P.multiply(S);
        return PolyUtil.basePseudoDivide(A, C);
    }


    /**
     * List of GenPolynomials greatest common divisor.
     *
     * @param A non empty list of GenPolynomials.
     * @return gcd(A_i).
     */
    public GenPolynomial<C> gcd(List<GenPolynomial<C>> A) {
        if (A == null || A.isEmpty()) {
            throw new IllegalArgumentException("A may not be empty");
        }
        GenPolynomial<C> g = A.get(0);
        for (int i = 1; i < A.size(); i++) {
            GenPolynomial<C> f = A.get(i);
            g = gcd(g, f);
        }
        return g;
    }


    /**
     * Univariate GenPolynomial resultant.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return res(P, S).
     * @throws UnsupportedOperationException if there is no implementation in
     *                                       the sub-class.
     */
    @SuppressWarnings("unused")
    public GenPolynomial<C> baseResultant(GenPolynomial<C> P, GenPolynomial<C> S) {
        // can not be abstract
        throw new UnsupportedOperationException("not implmented");
    }


    /**
     * Univariate GenPolynomial recursive resultant.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return res(P, S).
     * @throws UnsupportedOperationException if there is no implementation in
     *                                       the sub-class.
     */
    @SuppressWarnings("unused")
    public GenPolynomial<GenPolynomial<C>> recursiveUnivariateResultant(GenPolynomial<GenPolynomial<C>> P,
                                                                        GenPolynomial<GenPolynomial<C>> S) {
        // can not be abstract
        throw new UnsupportedOperationException("not implmented");
    }


    /**
     * GenPolynomial recursive resultant.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return res(P, S).
     * @throws UnsupportedOperationException if there is no implementation in
     *                                       the sub-class.
     */
    public GenPolynomial<GenPolynomial<C>> recursiveResultant(GenPolynomial<GenPolynomial<C>> P,
                                                              GenPolynomial<GenPolynomial<C>> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<C>> rfac = P.ring;
        GenPolynomialRing<C> cfac = (GenPolynomialRing<C>) rfac.coFac;
        GenPolynomialRing<C> dfac = cfac.extend(rfac.getVars());
        GenPolynomial<C> Pp = PolyUtil.distribute(dfac, P);
        GenPolynomial<C> Sp = PolyUtil.distribute(dfac, S);
        GenPolynomial<C> res = resultant(Pp, Sp);
        GenPolynomial<GenPolynomial<C>> Rr = PolyUtil.recursive(rfac, res);
        return Rr;
    }


    /**
     * GenPolynomial resultant. The input polynomials are considered as
     * univariate polynomials in the main variable.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return res(P, S).
     * @throws UnsupportedOperationException if there is no implementation in
     *                                       the sub-class.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.GreatestCommonDivisorSubres#recursiveResultant
     */
    public GenPolynomial<C> resultant(GenPolynomial<C> P, GenPolynomial<C> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        // no more hacked: GreatestCommonDivisorSubres<C> ufd_sr = new GreatestCommonDivisorSubres<C>();
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return baseResultant(P, S);
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<>(cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.recursive(rfac, P);
        GenPolynomial<GenPolynomial<C>> Sr = PolyUtil.recursive(rfac, S);

        GenPolynomial<GenPolynomial<C>> Dr = recursiveUnivariateResultant(Pr, Sr);
        GenPolynomial<C> D = PolyUtil.distribute(pfac, Dr);
        return D;
    }


    /**
     * GenPolynomial co-prime list.
     *
     * @param A list of GenPolynomials.
     * @return B with gcd(b,c) = 1 for all b != c in B and for all non-constant
     *         a in A there exists b in B with b|a. B does not contain zero or
     *         constant polynomials.
     */
    public List<GenPolynomial<C>> coPrime(List<GenPolynomial<C>> A) {
        if (A == null || A.isEmpty()) {
            return A;
        }
        List<GenPolynomial<C>> B = new ArrayList<>(A.size());
        // make a coprime to rest of list
        GenPolynomial<C> a = A.get(0);
        if (!a.isZERO() && !a.isConstant()) {
            for (int i = 1; i < A.size(); i++) {
                GenPolynomial<C> b = A.get(i);
                GenPolynomial<C> g = gcd(a, b).abs();
                if (!g.isONE()) {
                    a = PolyUtil.basePseudoDivide(a, g);
                    b = PolyUtil.basePseudoDivide(b, g);
                    GenPolynomial<C> gp = gcd(a, g).abs();
                    while (!gp.isONE()) {
                        a = PolyUtil.basePseudoDivide(a, gp);
                        g = PolyUtil.basePseudoDivide(g, gp);
                        B.add(g); // gcd(a,g) == 1
                        g = gp;
                        gp = gcd(a, gp).abs();
                    }
                    if (!g.isZERO() && !g.isConstant() /*&& !B.contains(g)*/) {
                        B.add(g); // gcd(a,g) == 1
                    }
                }
                if (!b.isZERO() && !b.isConstant()) {
                    B.add(b); // gcd(a,b) == 1
                }
            }
        } else {
            B.addAll(A.subList(1, A.size()));
        }
        // make rest coprime
        B = coPrime(B);
        if (!a.isZERO() && !a.isConstant() /*&& !B.contains(a)*/) {
            a = a.abs();
            B.add(a);
        }
        return B;
    }
}
