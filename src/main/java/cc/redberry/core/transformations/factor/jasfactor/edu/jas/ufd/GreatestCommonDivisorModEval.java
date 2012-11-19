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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.Modular;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.ModularRingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;


/**
 * Greatest common divisor algorithms with modular evaluation algorithm for
 * recursion.
 *
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorModEval<MOD extends GcdRingElem<MOD> & Modular>
        extends GreatestCommonDivisorAbstract<MOD> {


    private final boolean debug = false;


    /**
     * Modular gcd algorithm to use.
     */
    protected final GreatestCommonDivisorAbstract<MOD> mufd
            = new GreatestCommonDivisorSimple<>();
    // = new GreatestCommonDivisorPrimitive<MOD>();
    // not okay: = new GreatestCommonDivisorSubres<MOD>();


    /**
     * Univariate GenPolynomial greatest common divisor.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<MOD> baseGcd(GenPolynomial<MOD> P, GenPolynomial<MOD> S) {
        // required as recursion base
        return mufd.baseGcd(P, S);
    }


    /**
     * Recursive univariate GenPolynomial greatest common divisor.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<GenPolynomial<MOD>> recursiveUnivariateGcd(
            GenPolynomial<GenPolynomial<MOD>> P, GenPolynomial<GenPolynomial<MOD>> S) {
        return mufd.recursiveUnivariateGcd(P, S);
    }


    /**
     * GenPolynomial greatest common divisor, modular evaluation algorithm.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<MOD> gcd(GenPolynomial<MOD> P, GenPolynomial<MOD> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        GenPolynomialRing<MOD> fac = P.ring;
        // recusion base case for univariate polynomials
        if (fac.nvar <= 1) {
            GenPolynomial<MOD> T = baseGcd(P, S);
            return T;
        }
        long e = P.degree(fac.nvar - 1);
        long f = S.degree(fac.nvar - 1);
        if (e == 0 && f == 0) {
            GenPolynomialRing<GenPolynomial<MOD>> rfac = fac.recursive(1);
            GenPolynomial<MOD> Pc = PolyUtil.<MOD>recursive(rfac, P).leadingBaseCoefficient();
            GenPolynomial<MOD> Sc = PolyUtil.<MOD>recursive(rfac, S).leadingBaseCoefficient();
            GenPolynomial<MOD> r = gcd(Pc, Sc);
            return r.extend(fac, 0, 0L);
        }
        GenPolynomial<MOD> q;
        GenPolynomial<MOD> r;
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
        if (debug) {
        }
        r = r.abs();
        q = q.abs();
        // setup factories
        ModularRingFactory<MOD> cofac = (ModularRingFactory<MOD>) P.ring.coFac;
        if (!cofac.isField()) {
        }
        GenPolynomialRing<GenPolynomial<MOD>> rfac = fac.recursive(fac.nvar - 1);
        GenPolynomialRing<MOD> mfac = new GenPolynomialRing<>(cofac, rfac);
        GenPolynomialRing<MOD> ufac = (GenPolynomialRing<MOD>) rfac.coFac;
        //GenPolynomialRing<MOD> mfac = new GenPolynomialRing<MOD>(cofac, fac.nvar - 1, fac.tord);
        //GenPolynomialRing<MOD> ufac = new GenPolynomialRing<MOD>(cofac, 1, fac.tord);
        //GenPolynomialRing<GenPolynomial<MOD>> rfac = new GenPolynomialRing<GenPolynomial<MOD>>(ufac, fac.nvar - 1, fac.tord);
        // convert polynomials
        GenPolynomial<GenPolynomial<MOD>> qr;
        GenPolynomial<GenPolynomial<MOD>> rr;
        qr = PolyUtil.recursive(rfac, q);
        rr = PolyUtil.recursive(rfac, r);

        // compute univariate contents and primitive parts
        GenPolynomial<MOD> a = recursiveContent(rr);
        GenPolynomial<MOD> b = recursiveContent(qr);
        // gcd of univariate coefficient contents
        GenPolynomial<MOD> c = gcd(a, b);
        rr = PolyUtil.recursiveDivide(rr, a);
        qr = PolyUtil.recursiveDivide(qr, b);
        if (rr.isONE()) {
            rr = rr.multiply(c);
            r = PolyUtil.distribute(fac, rr);
            return r;
        }
        if (qr.isONE()) {
            qr = qr.multiply(c);
            q = PolyUtil.distribute(fac, qr);
            return q;
        }
        // compute normalization factor
        GenPolynomial<MOD> ac = rr.leadingBaseCoefficient();
        GenPolynomial<MOD> bc = qr.leadingBaseCoefficient();
        GenPolynomial<MOD> cc = gcd(ac, bc);
        // compute degrees and degree vectors
        ExpVector rdegv = rr.degreeVector();
        ExpVector qdegv = qr.degreeVector();
        long rd0 = PolyUtil.coeffMaxDegree(rr);
        long qd0 = PolyUtil.coeffMaxDegree(qr);
        long cd0 = cc.degree(0);
        long G = (rd0 >= qd0 ? rd0 : qd0) + cd0;

        // initialize element and degree vector
        ExpVector wdegv = rdegv.subst(0, rdegv.getVal(0) + 1);
        // +1 seems to be a hack for the unlucky prime test
        MOD inc = cofac.getONE();
        long i = 0;
        long en = cofac.getIntegerModul().longValue() - 1; // just a stopper
        MOD end = cofac.fromInteger(en);
        MOD mi;
        GenPolynomial<MOD> M = null;
        GenPolynomial<MOD> mn;
        GenPolynomial<MOD> qm;
        GenPolynomial<MOD> rm;
        GenPolynomial<MOD> cm;
        GenPolynomial<GenPolynomial<MOD>> cp = null;
        if (debug) {
        }
        for (MOD d = cofac.getZERO(); d.compareTo(end) <= 0; d = d.sum(inc)) {
            if (++i >= en) {
                return mufd.gcd(P, S);
                //throw new ArithmeticException("prime list exhausted");
            }
            // map normalization factor
            MOD nf = PolyUtil.evaluateMain(cofac, cc, d);
            if (nf.isZERO()) {
                continue;
            }
            // map polynomials
            qm = PolyUtil.evaluateFirstRec(ufac, mfac, qr, d);
            if (qm.isZERO() || !qm.degreeVector().equals(qdegv)) {
                continue;
            }
            rm = PolyUtil.evaluateFirstRec(ufac, mfac, rr, d);
            if (rm.isZERO() || !rm.degreeVector().equals(rdegv)) {
                continue;
            }
            if (debug) {
            }
            // compute modular gcd in recursion
            cm = gcd(rm, qm);
            // test for constant g.c.d
            if (cm.isConstant()) {
                if (c.ring.nvar < cm.ring.nvar) {
                    c = c.extend(mfac, 0, 0);
                }
                cm = cm.abs().multiply(c);
                q = cm.extend(fac, 0, 0);
                return q;
            }
            // test for unlucky prime
            ExpVector mdegv = cm.degreeVector();
            if (wdegv.equals(mdegv)) { // TL = 0
                // prime ok, next round
                if (M != null) {
                    if (M.degree(0) > G) {
                        // continue; // why should this be required?
                    }
                }
            } else { // TL = 3
                boolean ok = false;
                if (wdegv.multipleOf(mdegv)) { // TL = 2
                    M = null; // init chinese remainder
                    ok = true; // prime ok
                }
                if (mdegv.multipleOf(wdegv)) { // TL = 1
                    continue; // skip this prime
                }
                if (!ok) {
                    M = null; // discard chinese remainder and previous work
                    continue; // prime not ok
                }
            }
            // prepare interpolation algorithm
            cm = cm.multiply(nf);
            if (M == null) {
                // initialize interpolation
                M = ufac.getONE();
                cp = rfac.getZERO();
                wdegv = wdegv.gcd(mdegv); //EVGCD(wdegv,mdegv);
            }
            // interpolate
            mi = PolyUtil.evaluateMain(cofac, M, d);
            mi = mi.inverse(); // mod p
            cp = PolyUtil.interpolate(rfac, cp, M, mi, cm, d);
            mn = ufac.getONE().multiply(d);
            mn = ufac.univariate(0).subtract(mn);
            M = M.multiply(mn);
            // test for completion
            if (M.degree(0) > G) {
                break;
            }
            //long cmn = PolyUtil.<MOD>coeffMaxDegree(cp);
            //if ( M.degree(0) > cmn ) {
            // does not work: only if cofactors are also considered?
            // break;
            //}
        }
        // remove normalization
        cp = recursivePrimitivePart(cp).abs();
        cp = cp.multiply(c);
        q = PolyUtil.distribute(fac, cp);
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
    public GenPolynomial<MOD> baseResultant(GenPolynomial<MOD> P, GenPolynomial<MOD> S) {
        // required as recursion base
        return mufd.baseResultant(P, S);
    }


    /**
     * Univariate GenPolynomial recursive resultant.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return res(P, S).
     */
    @Override
    public GenPolynomial<GenPolynomial<MOD>> recursiveUnivariateResultant(GenPolynomial<GenPolynomial<MOD>> P,
                                                                          GenPolynomial<GenPolynomial<MOD>> S) {
        // only in this class
        return recursiveResultant(P, S);
    }


    /**
     * GenPolynomial resultant, modular evaluation algorithm.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return res(P, S).
     */
    @Override
    public GenPolynomial<MOD> resultant(GenPolynomial<MOD> P, GenPolynomial<MOD> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<MOD> fac = P.ring;
        // recusion base case for univariate polynomials
        if (fac.nvar <= 1) {
            GenPolynomial<MOD> T = mufd.baseResultant(P, S);
            return T;
        }
        long e = P.degree(fac.nvar - 1);
        long f = S.degree(fac.nvar - 1);
        if (e == 0 && f == 0) {
            GenPolynomialRing<GenPolynomial<MOD>> rfac = fac.recursive(1);
            GenPolynomial<MOD> Pc = PolyUtil.<MOD>recursive(rfac, P).leadingBaseCoefficient();
            GenPolynomial<MOD> Sc = PolyUtil.<MOD>recursive(rfac, S).leadingBaseCoefficient();
            GenPolynomial<MOD> r = resultant(Pc, Sc);
            return r.extend(fac, 0, 0L);
        }
        GenPolynomial<MOD> q;
        GenPolynomial<MOD> r;
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
        if (debug) {
        }
        // setup factories
        ModularRingFactory<MOD> cofac = (ModularRingFactory<MOD>) P.ring.coFac;
        if (!cofac.isField()) {
        }
        GenPolynomialRing<GenPolynomial<MOD>> rfac = fac.recursive(fac.nvar - 1);
        GenPolynomialRing<MOD> mfac = new GenPolynomialRing<>(cofac, rfac);
        GenPolynomialRing<MOD> ufac = (GenPolynomialRing<MOD>) rfac.coFac;

        // convert polynomials
        GenPolynomial<GenPolynomial<MOD>> qr = PolyUtil.recursive(rfac, q);
        GenPolynomial<GenPolynomial<MOD>> rr = PolyUtil.recursive(rfac, r);

        // compute degrees and degree vectors
        ExpVector qdegv = qr.degreeVector();
        ExpVector rdegv = rr.degreeVector();

        long qd0 = PolyUtil.coeffMaxDegree(qr);
        long rd0 = PolyUtil.coeffMaxDegree(rr);
        qd0 = (qd0 == 0 ? 1 : qd0);
        rd0 = (rd0 == 0 ? 1 : rd0);
        long qd1 = qr.degree();
        long rd1 = rr.degree();
        qd1 = (qd1 == 0 ? 1 : qd1);
        rd1 = (rd1 == 0 ? 1 : rd1);
        long G = qd0 * rd1 + rd0 * qd1 + 1;

        // initialize element
        MOD inc = cofac.getONE();
        long i = 0;
        long en = cofac.getIntegerModul().longValue() - 1; // just a stopper
        MOD end = cofac.fromInteger(en);
        MOD mi;
        GenPolynomial<MOD> M = null;
        GenPolynomial<MOD> mn;
        GenPolynomial<MOD> qm;
        GenPolynomial<MOD> rm;
        GenPolynomial<MOD> cm;
        GenPolynomial<GenPolynomial<MOD>> cp = null;
        if (debug) {
        }
        for (MOD d = cofac.getZERO(); d.compareTo(end) <= 0; d = d.sum(inc)) {
            if (++i >= en) {
                return mufd.resultant(P, S);
                //throw new ArithmeticException("prime list exhausted");
            }
            // map polynomials
            qm = PolyUtil.evaluateFirstRec(ufac, mfac, qr, d);
            if (qm.isZERO() || !qm.degreeVector().equals(qdegv)) {
                if (debug) {
                }
                continue;
            }
            rm = PolyUtil.evaluateFirstRec(ufac, mfac, rr, d);
            if (rm.isZERO() || !rm.degreeVector().equals(rdegv)) {
                if (debug) {
                }
                continue;
            }
            // compute modular resultant in recursion
            cm = resultant(rm, qm);

            // prepare interpolation algorithm
            if (M == null) {
                // initialize interpolation
                M = ufac.getONE();
                cp = rfac.getZERO();
            }
            // interpolate
            mi = PolyUtil.evaluateMain(cofac, M, d);
            mi = mi.inverse(); // mod p
            cp = PolyUtil.interpolate(rfac, cp, M, mi, cm, d);
            mn = ufac.getONE().multiply(d);
            mn = ufac.univariate(0).subtract(mn);
            M = M.multiply(mn);
            // test for completion
            if (M.degree(0) > G) {
                if (debug) {
                }
                break;
            }
        }
        // distribute
        q = PolyUtil.distribute(fac, cp);
        return q;
    }

}
