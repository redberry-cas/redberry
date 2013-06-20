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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.Monomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Squarefree decomposition for finite coefficient fields of characteristic p.
 *
 * @author Heinz Kredel
 */

public class SquarefreeFiniteFieldCharP<C extends GcdRingElem<C>> extends SquarefreeFieldCharP<C> {


    //private final boolean debug = false;


    /**
     * Constructor.
     */
    public SquarefreeFiniteFieldCharP(RingFactory<C> fac) {
        super(fac);
        // isFinite() predicate now present
        if (!fac.isFinite()) {
            throw new IllegalArgumentException("fac must be finite");
        }
    }


    /* --------- char-th roots --------------------- */

    /**
     * Characteristics root of a coefficient. <b>Note:</b> not needed at the
     * moment.
     *
     * @param p coefficient.
     * @return [p -&gt; k] if exists k with e=k*charactristic(c) and c = p**e,
     *         else null.
     */
    public SortedMap<C, Long> rootCharacteristic(C p) {
        if (p == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " p == null");
        }
        // already checked in constructor:
        //java.math.BigInteger c = p.factory().characteristic();
        //if ( c.signum() == 0 ) {
        //    return null;
        //}
        SortedMap<C, Long> root = new TreeMap<>();
        if (p.isZERO()) {
            return root;
        }
        // true for finite fields:
        root.put(p, 1L);
        return root;
    }


    /**
     * Characteristics root of a coefficient.
     *
     * @param c coefficient.
     * @return r with r**p == c, if such an r exists, else null.
     */
    public C coeffRootCharacteristic(C c) {
        if (c == null || c.isZERO()) {
            return c;
        }
        C r = c;
        if (aCoFac == null && qCoFac == null) {
            // case ModInteger: c**p == c
            return r;
        }
        if (aCoFac != null) {
            // case AlgebraicNumber<ModInteger>: r = c**(p**(d-1)), r**p == c
            long d = aCoFac.totalExtensionDegree();
            if (d <= 1) {
                return r;
            }
            BigInteger p = new BigInteger(aCoFac.characteristic());
            BigInteger q = Power.positivePower(p, d - 1);
            r = Power.positivePower(r, q.getVal());
            return r;
        }
        if (qCoFac != null) {
            throw new UnsupportedOperationException("case QuotientRing not yet implemented");
        }
        return r;
    }


    /**
     * Characteristics root of a polynomial. <b>Note:</b> call only in
     * recursion.
     *
     * @param P polynomial.
     * @return [p -&gt; k] if exists k with e=k*charactristic(P) and P = p**e,
     *         else null.
     */
    public SortedMap<GenPolynomial<C>, Long> rootCharacteristic(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        java.math.BigInteger c = P.ring.characteristic();
        if (c.signum() == 0) {
            return null;
        }
        SortedMap<GenPolynomial<C>, Long> root = new TreeMap<>();
        if (P.isZERO()) {
            return root;
        }
        if (P.isONE()) {
            root.put(P, 1L);
            return root;
        }
        SortedMap<GenPolynomial<C>, Long> sf = squarefreeFactors(P);
        // better: test if sf.size() == 1 // not ok
        Long k = null;
        for (Map.Entry<GenPolynomial<C>, Long> me : sf.entrySet()) {
            GenPolynomial<C> p = me.getKey();
            if (p.isConstant()) {
                continue;
            }
            Long e = me.getValue(); //sf.get(p);
            java.math.BigInteger E = new java.math.BigInteger(e.toString());
            java.math.BigInteger r = E.remainder(c);
            if (!r.equals(java.math.BigInteger.ZERO)) {
                return null;
            }
            if (k == null) {
                k = e;
            } else if (k.compareTo(e) >= 0) {
                k = e;
            }
        }
        // now c divides all exponents
        Long cl = c.longValue();
        GenPolynomial<C> rp = P.ring.getONE();
        for (Map.Entry<GenPolynomial<C>, Long> me : sf.entrySet()) {
            GenPolynomial<C> q = me.getKey();
            Long e = me.getValue(); // sf.get(q);
            if (q.isConstant()) { // ensure p-th root
                C qc = q.leadingBaseCoefficient();
                if (e > 1L) {
                    qc = Power.positivePower(qc, e);
                    //e = 1L;
                }
                C qr = coeffRootCharacteristic(qc);
                q = P.ring.getONE().multiply(qr);
                root.put(q, 1L);
                continue;
            }
            if (e > k) {
                long ep = e / cl;
                q = Power.positivePower(q, ep);
            }
            rp = rp.multiply(q);
        }
        if (k != null) {
            k = k / cl;
            root.put(rp, k);
        }
        return root;
    }


    /**
     * GenPolynomial char-th root univariate polynomial. Base coefficient type
     * must be finite field, that is ModInteger or
     * AlgebraicNumber&lt;ModInteger&gt; etc.
     *
     * @param P GenPolynomial.
     * @return char-th_rootOf(P), or null if no char-th root.
     */
    @Override
    public GenPolynomial<C> baseRootCharacteristic(GenPolynomial<C> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar > 1) {
            // basePthRoot not possible by return type
            throw new IllegalArgumentException(P.getClass().getName() + " only for univariate polynomials");
        }
        RingFactory<C> rf = pfac.coFac;
        if (rf.characteristic().signum() != 1) {
            // basePthRoot not possible
            throw new IllegalArgumentException(P.getClass().getName() + " only for char p > 0 " + rf);
        }
        long mp = rf.characteristic().longValue();
        GenPolynomial<C> d = pfac.getZERO().copy();
        for (Monomial<C> m : P) {
            ExpVector f = m.e;
            long fl = f.getVal(0);
            if (fl % mp != 0) {
                return null;
            }
            fl = fl / mp;
            ExpVector e = ExpVector.create(1, 0, fl);
            // for m.c exists a char-th root, since finite field
            C r = coeffRootCharacteristic(m.c);
            d.doPutToMap(e, r);
        }
        return d;
    }


    /**
     * GenPolynomial char-th root univariate polynomial with polynomial
     * coefficients.
     *
     * @param P recursive univariate GenPolynomial.
     * @return char-th_rootOf(P), or null if P is no char-th root.
     */
    @Override
    public GenPolynomial<GenPolynomial<C>> recursiveUnivariateRootCharacteristic(
            GenPolynomial<GenPolynomial<C>> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // basePthRoot not possible by return type
            throw new IllegalArgumentException(P.getClass().getName() + " only for univariate polynomials");
        }
        RingFactory<GenPolynomial<C>> rf = pfac.coFac;
        if (rf.characteristic().signum() != 1) {
            // basePthRoot not possible
            throw new IllegalArgumentException(P.getClass().getName() + " only for char p > 0 " + rf);
        }
        long mp = rf.characteristic().longValue();
        GenPolynomial<GenPolynomial<C>> d = pfac.getZERO().copy();
        for (Monomial<GenPolynomial<C>> m : P) {
            ExpVector f = m.e;
            long fl = f.getVal(0);
            if (fl % mp != 0) {
                return null;
            }
            fl = fl / mp;
            SortedMap<GenPolynomial<C>, Long> sm = rootCharacteristic(m.c);
            if (sm == null) {
                return null;
            }
            GenPolynomial<C> r = rf.getONE();
            for (Map.Entry<GenPolynomial<C>, Long> me : sm.entrySet()) {
                GenPolynomial<C> rp = me.getKey();
                long gl = me.getValue(); //sm.get(rp);
                if (gl > 1) {
                    rp = Power.positivePower(rp, gl);
                }
                r = r.multiply(rp);
            }
            ExpVector e = ExpVector.create(1, 0, fl);
            d.doPutToMap(e, r);
        }
        return d;
    }

}
