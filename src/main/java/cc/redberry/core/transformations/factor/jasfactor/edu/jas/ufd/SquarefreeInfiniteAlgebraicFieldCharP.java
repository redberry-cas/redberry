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
 * $Id: SquarefreeInfiniteAlgebraicFieldCharP.java 3290 2010-08-26 09:18:48Z
 * kredel $
 */

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.gb.GroebnerBase;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.gb.Reduction;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.gb.ReductionSeq;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.*;


/**
 * Squarefree decomposition for algebraic extensions of infinite coefficient
 * fields of characteristic p &gt; 0.
 *
 * @author Heinz Kredel
 */

public class SquarefreeInfiniteAlgebraicFieldCharP<C extends GcdRingElem<C>> extends
        SquarefreeFieldCharP<AlgebraicNumber<C>> {


    //private final boolean debug = false;


    /**
     * Squarefree engine for infinite ring of characteristic p base coefficients.
     */
    protected final SquarefreeAbstract<C> aengine;


    /**
     * Constructor.
     */
    public SquarefreeInfiniteAlgebraicFieldCharP(RingFactory<AlgebraicNumber<C>> fac) {
        super(fac);
        // isFinite() predicate now present
        if (fac.isFinite()) {
            throw new IllegalArgumentException("fac must be in-finite");
        }
        AlgebraicNumberRing<C> afac = (AlgebraicNumberRing<C>) fac;
        GenPolynomialRing<C> rfac = afac.ring;
        aengine = SquarefreeFactory.getImplementation(rfac);
    }


    /* --------- algebraic number char-th roots --------------------- */

    /**
     * Squarefree factors of a AlgebraicNumber.
     *
     * @param P AlgebraicNumber.
     * @return [p_1 -&gt; e_1,...,p_k - &gt; e_k] with P = prod_{i=1, ..., k}
     *         p_i**e_k.
     */
    @Override
    public SortedMap<AlgebraicNumber<C>, Long> squarefreeFactors(AlgebraicNumber<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        SortedMap<AlgebraicNumber<C>, Long> factors = new TreeMap<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.put(P, 1L);
            return factors;
        }
        GenPolynomial<C> an = P.val;
        AlgebraicNumberRing<C> pfac = P.ring;
        if (!an.isONE()) {
            SortedMap<GenPolynomial<C>, Long> nfac = aengine.squarefreeFactors(an);
            for (Map.Entry<GenPolynomial<C>, Long> me : nfac.entrySet()) {
                GenPolynomial<C> nfp = me.getKey();
                AlgebraicNumber<C> nf = new AlgebraicNumber<>(pfac, nfp);
                factors.put(nf, me.getValue()); //nfac.get(nfp));
            }
        }
        if (factors.size() == 0) {
            factors.put(P, 1L);
        }
        return factors;
    }


    /**
     * Characteristics root of a AlgebraicNumber.
     *
     * @param P AlgebraicNumber.
     * @return [p -&gt; k] if exists k with e=charactristic(P)*k and P = p**e,
     *         else null.
     */
    public SortedMap<AlgebraicNumber<C>, Long> rootCharacteristic(AlgebraicNumber<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        java.math.BigInteger c = P.ring.characteristic();
        if (c.signum() == 0) {
            return null;
        }
        SortedMap<AlgebraicNumber<C>, Long> root = new TreeMap<>();
        if (P.isZERO()) {
            return root;
        }
        if (P.isONE()) {
            root.put(P, 1L);
            return root;
        }
        // generate system of equations
        AlgebraicNumberRing<C> afac = P.ring;
        long deg = afac.modul.degree(0);
        int d = (int) deg;
        String[] vn = GenPolynomialRing.newVars("c", d);
        GenPolynomialRing<AlgebraicNumber<C>> pfac = new GenPolynomialRing<>(afac, d, vn);
        List<GenPolynomial<AlgebraicNumber<C>>> uv = (List<GenPolynomial<AlgebraicNumber<C>>>) pfac
                .univariateList();
        GenPolynomial<AlgebraicNumber<C>> cp = pfac.getZERO();
        GenPolynomialRing<C> apfac = afac.ring;
        long i = 0;
        for (GenPolynomial<AlgebraicNumber<C>> pa : uv) {
            GenPolynomial<C> ca = apfac.univariate(0, i++);
            GenPolynomial<AlgebraicNumber<C>> pb = pa.multiply(new AlgebraicNumber<>(afac, ca));
            cp = cp.sum(pb);
        }
        GenPolynomial<AlgebraicNumber<C>> cpp = Power
                .positivePower(cp, c);
        GenPolynomialRing<C> ppfac = new GenPolynomialRing<>(apfac.coFac, pfac);
        List<GenPolynomial<C>> gl = new ArrayList<>();
        if (deg == c.longValue() && afac.modul.length() == 2) {
            for (Monomial<AlgebraicNumber<C>> m : cpp) {
                ExpVector f = m.e;
                AlgebraicNumber<C> a = m.c;
                GenPolynomial<C> ap = a.val;
                for (Monomial<C> ma : ap) {
                    ExpVector e = ma.e;
                    C cc = ma.c;
                    C pc = P.val.coefficient(e);
                    C cc1 = ((RingFactory<C>) pc.factory()).getONE();
                    C pc1 = ((RingFactory<C>) pc.factory()).getZERO();
                    if (cc instanceof AlgebraicNumber && pc instanceof AlgebraicNumber) {
                        throw new UnsupportedOperationException(
                                "case multiple algebraic extensions not implemented");
                    } else if (cc instanceof Quotient && pc instanceof Quotient) {
                        Quotient<C> ccp = (Quotient<C>) cc;
                        Quotient<C> pcp = (Quotient<C>) pc;
                        if (pcp.isConstant()) {
                            throw new ArithmeticException("finite field not allowed here ");
                        }
                        //C dc = cc.divide(pc);
                        Quotient<C> dcp = ccp.divide(pcp);
                        if (dcp.isConstant()) { // not possible: dc.isConstant() 
                            //if ( dcp.num.isConstant() ) 
                            cc1 = cc;
                            pc1 = pc;
                        }
                        GenPolynomial<C> r = new GenPolynomial<>(ppfac, cc1, f);
                        r = r.subtract(pc1);
                        gl.add(r);
                    }
                }
            }
        } else {
            for (Monomial<AlgebraicNumber<C>> m : cpp) {
                ExpVector f = m.e;
                AlgebraicNumber<C> a = m.c;
                GenPolynomial<C> ap = a.val;
                for (Monomial<C> ma : ap) {
                    ExpVector e = ma.e;
                    C cc = ma.c;
                    C pc = P.val.coefficient(e);
                    GenPolynomial<C> r = new GenPolynomial<>(ppfac, cc, f);
                    r = r.subtract(pc);
                    gl.add(r);
                }
            }
        }
        // solve system of equations and construct result
        Reduction<C> red = new ReductionSeq<>();
        gl = red.irreducibleSet(gl);
        GroebnerBase<C> bb = new GroebnerBase<>(); //GBFactory.<C>getImplementation();
        int z = bb.commonZeroTest(gl);
        if (z < 0) { // no solution
            return null;
        }
        GenPolynomial<C> car = apfac.getZERO();
        for (GenPolynomial<C> pl : gl) {
            if (pl.length() <= 1) {
                continue;
            }
            if (pl.length() > 2) {
                throw new IllegalArgumentException("dim > 0 not implemented " + pl);
            }
            ExpVector e = pl.leadingExpVector();
            int[] v = e.dependencyOnVariables();
            if (v == null || v.length == 0) {
                continue;
            }
            int vi = v[0];
            GenPolynomial<C> ca = apfac.univariate(0, deg - 1 - vi);
            C tc = pl.trailingBaseCoefficient();
            tc = tc.negate();
            if (e.maxDeg() == c.longValue()) { // p-th root of tc ...
                //SortedMap<C, Long> br = aengine.rootCharacteristic(tc);
                SortedMap<C, Long> br = aengine.squarefreeFactors(tc);
                if (br != null && br.size() > 0) {
                    C cc = apfac.coFac.getONE();
                    for (Map.Entry<C, Long> me : br.entrySet()) {
                        C bc = me.getKey();
                        long ll = me.getValue(); //br.get(bc);
                        if (ll % c.longValue() == 0L) {
                            long fl = ll / c.longValue();
                            cc = cc.multiply(Power.<C>positivePower(bc, fl));
                        } else { // fail ?
                            cc = cc.multiply(bc);
                        }
                    }
                    tc = cc;
                }
            }
            ca = ca.multiply(tc);
            car = car.sum(ca);
        }
        AlgebraicNumber<C> rr = new AlgebraicNumber<>(afac, car);
        root.put(rr, 1L);
        return root;
    }


    /**
     * GenPolynomial char-th root main variable.
     *
     * @param P univariate GenPolynomial with AlgebraicNumber coefficients.
     * @return char-th_rootOf(P), or null, if P is no char-th root.
     */
    public GenPolynomial<AlgebraicNumber<C>> rootCharacteristic(GenPolynomial<AlgebraicNumber<C>> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<AlgebraicNumber<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // go to recursion
            GenPolynomialRing<AlgebraicNumber<C>> cfac = pfac.contract(1);
            GenPolynomialRing<GenPolynomial<AlgebraicNumber<C>>> rfac = new GenPolynomialRing<>(
                    cfac, 1);
            GenPolynomial<GenPolynomial<AlgebraicNumber<C>>> Pr = PolyUtil.recursive(
                    rfac, P);
            GenPolynomial<GenPolynomial<AlgebraicNumber<C>>> Prc = recursiveUnivariateRootCharacteristic(Pr);
            if (Prc == null) {
                return null;
            }
            GenPolynomial<AlgebraicNumber<C>> D = PolyUtil.distribute(pfac, Prc);
            return D;
        }
        RingFactory<AlgebraicNumber<C>> rf = pfac.coFac;
        if (rf.characteristic().signum() != 1) {
            // basePthRoot not possible
            throw new IllegalArgumentException(P.getClass().getName() + " only for ModInteger polynomials "
                    + rf);
        }
        long mp = rf.characteristic().longValue();
        GenPolynomial<AlgebraicNumber<C>> d = pfac.getZERO().copy();
        for (Monomial<AlgebraicNumber<C>> m : P) {
            ExpVector f = m.e;
            long fl = f.getVal(0);
            if (fl % mp != 0) {
                return null;
            }
            fl = fl / mp;
            SortedMap<AlgebraicNumber<C>, Long> sm = rootCharacteristic(m.c);
            if (sm == null) {
                return null;
            }
            AlgebraicNumber<C> r = rf.getONE();
            for (Map.Entry<AlgebraicNumber<C>, Long> me : sm.entrySet()) {
                AlgebraicNumber<C> rp = me.getKey();
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


    /**
     * GenPolynomial char-th root univariate polynomial.
     *
     * @param P GenPolynomial.
     * @return char-th_rootOf(P).
     */
    @Override
    public GenPolynomial<AlgebraicNumber<C>> baseRootCharacteristic(GenPolynomial<AlgebraicNumber<C>> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<AlgebraicNumber<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // basePthRoot not possible by return type
            throw new IllegalArgumentException(P.getClass().getName() + " only for univariate polynomials");
        }
        RingFactory<AlgebraicNumber<C>> rf = pfac.coFac;
        if (rf.characteristic().signum() != 1) {
            // basePthRoot not possible
            throw new IllegalArgumentException(P.getClass().getName() + " only for char p > 0 " + rf);
        }
        long mp = rf.characteristic().longValue();
        GenPolynomial<AlgebraicNumber<C>> d = pfac.getZERO().copy();
        for (Monomial<AlgebraicNumber<C>> m : P) {
            ExpVector f = m.e;
            long fl = f.getVal(0);
            if (fl % mp != 0) {
                return null;
            }
            fl = fl / mp;
            SortedMap<AlgebraicNumber<C>, Long> sm = rootCharacteristic(m.c);
            if (sm == null) {
                return null;
            }
            AlgebraicNumber<C> r = rf.getONE();
            for (Map.Entry<AlgebraicNumber<C>, Long> me : sm.entrySet()) {
                AlgebraicNumber<C> rp = me.getKey();
                long gl = me.getValue(); //sm.get(rp);
                AlgebraicNumber<C> re = rp;
                if (gl > 1) {
                    re = Power.positivePower(rp, gl);
                }
                r = r.multiply(re);
            }
            ExpVector e = ExpVector.create(1, 0, fl);
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
    public GenPolynomial<GenPolynomial<AlgebraicNumber<C>>> recursiveUnivariateRootCharacteristic(
            GenPolynomial<GenPolynomial<AlgebraicNumber<C>>> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<AlgebraicNumber<C>>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // basePthRoot not possible by return type
            throw new IllegalArgumentException(P.getClass().getName()
                    + " only for univariate recursive polynomials");
        }
        RingFactory<GenPolynomial<AlgebraicNumber<C>>> rf = pfac.coFac;
        if (rf.characteristic().signum() != 1) {
            // basePthRoot not possible
            throw new IllegalArgumentException(P.getClass().getName() + " only for char p > 0 " + rf);
        }
        long mp = rf.characteristic().longValue();
        GenPolynomial<GenPolynomial<AlgebraicNumber<C>>> d = pfac.getZERO().copy();
        for (Monomial<GenPolynomial<AlgebraicNumber<C>>> m : P) {
            ExpVector f = m.e;
            long fl = f.getVal(0);
            if (fl % mp != 0) {
                return null;
            }
            fl = fl / mp;
            GenPolynomial<AlgebraicNumber<C>> r = rootCharacteristic(m.c);
            if (r == null) {
                return null;
            }
            ExpVector e = ExpVector.create(1, 0, fl);
            d.doPutToMap(e, r);
        }
        return d;
    }

}
