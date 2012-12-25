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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;


/**
 * Greatest common divisor algorithms with modular computation and chinese
 * remainder algorithm.
 *
 * @author Heinz Kredel
 */

public class GreatestCommonDivisorModular<MOD extends GcdRingElem<MOD> & Modular> extends
        GreatestCommonDivisorAbstract<BigInteger> {


    private final boolean debug = false; //logger.isInfoEnabled();


    /*
     * Modular gcd algorithm to use.
     */
    protected final GreatestCommonDivisorAbstract<MOD> mufd;


    /*
     * Integer gcd algorithm for fall back.
     */
    protected final GreatestCommonDivisorAbstract<BigInteger> iufd = new GreatestCommonDivisorSubres<>();


    /**
     * Constructor to set recursive algorithm. Use modular evaluation GCD
     * algorithm.
     */
    public GreatestCommonDivisorModular() {
        this(false);
    }


    /**
     * Constructor to set recursive algorithm.
     *
     * @param simple , true if the simple PRS should be used.
     */
    public GreatestCommonDivisorModular(boolean simple) {
        if (simple) {
            mufd = new GreatestCommonDivisorSimple<>();
        } else {
            mufd = new GreatestCommonDivisorModEval<>();
        }
    }


    /**
     * Univariate GenPolynomial greatest comon divisor. Delegate to subresultant
     * baseGcd, should not be needed.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<BigInteger> baseGcd(GenPolynomial<BigInteger> P, GenPolynomial<BigInteger> S) {
        return iufd.baseGcd(P, S);
    }


    /**
     * Univariate GenPolynomial recursive greatest comon divisor. Delegate to
     * subresultant recursiveGcd, should not be needed.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<GenPolynomial<BigInteger>> recursiveUnivariateGcd(
            GenPolynomial<GenPolynomial<BigInteger>> P, GenPolynomial<GenPolynomial<BigInteger>> S) {
        return iufd.recursiveUnivariateGcd(P, S);
    }


    /**
     * GenPolynomial greatest comon divisor, modular algorithm.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return gcd(P, S).
     */
    @Override
    public GenPolynomial<BigInteger> gcd(GenPolynomial<BigInteger> P, GenPolynomial<BigInteger> S) {
        if (S == null || S.isZERO()) {
            return P;
        }
        if (P == null || P.isZERO()) {
            return S;
        }
        GenPolynomialRing<BigInteger> fac = P.ring;
        // special case for univariate polynomials
        if (fac.nvar <= 1) {
            GenPolynomial<BigInteger> T = baseGcd(P, S);
            return T;
        }
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<BigInteger> q;
        GenPolynomial<BigInteger> r;
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
        // compute contents and primitive parts
        BigInteger a = baseContent(r);
        BigInteger b = baseContent(q);
        // gcd of coefficient contents
        BigInteger c = gcd(a, b); // indirection
        r = divide(r, a); // indirection
        q = divide(q, b); // indirection
        if (r.isONE()) {
            return r.multiply(c);
        }
        if (q.isONE()) {
            return q.multiply(c);
        }
        // compute normalization factor
        BigInteger ac = r.leadingBaseCoefficient();
        BigInteger bc = q.leadingBaseCoefficient();
        BigInteger cc = gcd(ac, bc); // indirection
        // compute norms
        BigInteger an = r.maxNorm();
        BigInteger bn = q.maxNorm();
        BigInteger n = (an.compareTo(bn) < 0 ? bn : an);
        n = n.multiply(cc).multiply(n.fromInteger(2));
        // compute degree vectors
        ExpVector rdegv = r.degreeVector();
        ExpVector qdegv = q.degreeVector();
        //compute factor coefficient bounds
        BigInteger af = an.multiply(PolyUtil.factorBound(rdegv));
        BigInteger bf = bn.multiply(PolyUtil.factorBound(qdegv));
        BigInteger cf = (af.compareTo(bf) < 0 ? bf : af);
        cf = cf.multiply(cc.multiply(cc.fromInteger(8)));
        //initialize prime list and degree vector
        PrimeList primes = new PrimeList();
        int pn = 10; //primes.size();
        ExpVector wdegv = rdegv.subst(0, rdegv.getVal(0) + 1);
        // +1 seems to be a hack for the unlucky prime test
        ModularRingFactory<MOD> cofac;
        ModularRingFactory<MOD> cofacM = null;
        GenPolynomial<MOD> qm;
        GenPolynomial<MOD> rm;
        GenPolynomialRing<MOD> mfac;
        GenPolynomialRing<MOD> rfac = null;
        int i = 0;
        BigInteger M = null;
        BigInteger cfe = null;
        GenPolynomial<MOD> cp = null;
        GenPolynomial<MOD> cm = null;
        GenPolynomial<BigInteger> cpi = null;
        if (debug) {
        }
        for (java.math.BigInteger p : primes) {
            if (p.longValue() == 2L) { // skip 2
                continue;
            }
            if (++i >= pn) {
                return iufd.gcd(P, S);
                //throw new ArithmeticException("prime list exhausted");
            }
            // initialize coefficient factory and map normalization factor
            if (ModLongRing.MAX_LONG.compareTo(p) > 0) {
                cofac = (ModularRingFactory) new ModLongRing(p, true);
            } else {
                cofac = (ModularRingFactory) new ModIntegerRing(p, true);
            }
            MOD nf = cofac.fromInteger(cc.getVal());
            if (nf.isZERO()) {
                continue;
            }
            // initialize polynomial factory and map polynomials
            mfac = new GenPolynomialRing<>(cofac, fac.nvar, fac.tord, fac.getVars());
            qm = PolyUtil.fromIntegerCoefficients(mfac, q);
            if (qm.isZERO() || !qm.degreeVector().equals(qdegv)) {
                continue;
            }
            rm = PolyUtil.fromIntegerCoefficients(mfac, r);
            if (rm.isZERO() || !rm.degreeVector().equals(rdegv)) {
                continue;
            }
            if (debug) {
            }
            // compute modular gcd
            cm = mufd.gcd(rm, qm);
            // test for constant g.c.d
            if (cm.isConstant()) {
                return fac.getONE().multiply(c);
                //return cm.abs().multiply( c );
            }
            // test for unlucky prime
            ExpVector mdegv = cm.degreeVector();
            if (wdegv.equals(mdegv)) { // TL = 0
                // prime ok, next round
                if (M != null) {
                    if (M.compareTo(cfe) > 0) {
                        // continue; // why should this be required?
                    }
                }
            } else { // TL = 3
                boolean ok = false;
                if (wdegv.multipleOf(mdegv)) { // TL = 2 // EVMT(wdegv,mdegv)
                    M = null; // init chinese remainder
                    ok = true; // prime ok
                }
                if (mdegv.multipleOf(wdegv)) { // TL = 1 // EVMT(mdegv,wdegv)
                    continue; // skip this prime
                }
                if (!ok) {
                    M = null; // discard chinese remainder and previous work
                    continue; // prime not ok
                }
            }
            //--wdegv = mdegv;
            // prepare chinese remainder algorithm
            cm = cm.multiply(nf);
            if (M == null) {
                // initialize chinese remainder algorithm
                M = new BigInteger(p);
                cofacM = cofac;
                rfac = mfac;
                cp = cm;
                wdegv = wdegv.gcd(mdegv); //EVGCD(wdegv,mdegv);
                cfe = cf;
                for (int k = 0; k < wdegv.length(); k++) {
                    cfe = cfe.multiply(new BigInteger(wdegv.getVal(k) + 1));
                }
            } else {
                // apply chinese remainder algorithm
                BigInteger Mp = M;
                MOD mi = cofac.fromInteger(Mp.getVal());
                mi = mi.inverse(); // mod p
                M = M.multiply(new BigInteger(p));
                if (ModLongRing.MAX_LONG.compareTo(M.getVal()) > 0) {
                    cofacM = (ModularRingFactory) new ModLongRing(M.getVal());
                } else {
                    cofacM = (ModularRingFactory) new ModIntegerRing(M.getVal());
                }
                rfac = new GenPolynomialRing<>(cofacM, fac);
                if (!cofac.getClass().equals(cofacM.getClass())) {
                    cofac = (ModularRingFactory) new ModIntegerRing(p);
                    mfac = new GenPolynomialRing<>(cofac, fac);
                    GenPolynomial<BigInteger> mm = PolyUtil.integerFromModularCoefficients(fac, cm);
                    cm = PolyUtil.fromIntegerCoefficients(mfac, mm);
                    mi = cofac.fromInteger(Mp.getVal());
                    mi = mi.inverse(); // mod p
                }
                if (!cp.ring.coFac.getClass().equals(cofacM.getClass())) {
                    ModularRingFactory cop = (ModularRingFactory) cp.ring.coFac;
                    cofac = (ModularRingFactory) new ModIntegerRing(cop.getIntegerModul().getVal());
                    mfac = new GenPolynomialRing<>(cofac, fac);
                    GenPolynomial<BigInteger> mm = PolyUtil.integerFromModularCoefficients(fac, cp);
                    cp = PolyUtil.fromIntegerCoefficients(mfac, mm);
                }
                cp = PolyUtil.chineseRemainder(rfac, cp, mi, cm);
            }
            // test for completion
            if (n.compareTo(M) <= 0) {
                break;
            }
            // must use integer.sumNorm
            cpi = PolyUtil.integerFromModularCoefficients(fac, cp);
            BigInteger cmn = cpi.sumNorm();
            cmn = cmn.multiply(cmn.fromInteger(4));
            //if ( cmn.compareTo( M ) <= 0 ) {
            // does not work: only if cofactors are also considered?
            // break;
            //}
            if (i % 2 != 0 && !cp.isZERO()) {
                // check if done on every second prime
                GenPolynomial<BigInteger> x;
                x = PolyUtil.integerFromModularCoefficients(fac, cp);
                x = basePrimitivePart(x);
                if (!PolyUtil.<BigInteger>baseSparsePseudoRemainder(q, x).isZERO()) {
                    continue;
                }
                if (!PolyUtil.<BigInteger>baseSparsePseudoRemainder(r, x).isZERO()) {
                    continue;
                }
                break;
            }
        }
        if (debug) {
        }
        // remove normalization
        q = PolyUtil.integerFromModularCoefficients(fac, cp);
        q = basePrimitivePart(q);
        return q.abs().multiply(c);
    }


    /**
     * Univariate GenPolynomial resultant.
     *
     * @param P univariate GenPolynomial.
     * @param S univariate GenPolynomial.
     * @return res(P, S).
     */
    @Override
    public GenPolynomial<BigInteger> baseResultant(GenPolynomial<BigInteger> P, GenPolynomial<BigInteger> S) {
        // not a special case here
        return resultant(P, S);
    }


    /**
     * Univariate GenPolynomial recursive resultant.
     *
     * @param P univariate recursive GenPolynomial.
     * @param S univariate recursive GenPolynomial.
     * @return res(P, S).
     */
    public GenPolynomial<GenPolynomial<BigInteger>> recursiveUnivariateResultant(GenPolynomial<GenPolynomial<BigInteger>> P,
                                                                                 GenPolynomial<GenPolynomial<BigInteger>> S) {
        // only in this class
        return recursiveResultant(P, S);
    }


    /**
     * GenPolynomial resultant, modular algorithm.
     *
     * @param P GenPolynomial.
     * @param S GenPolynomial.
     * @return res(P, S).
     */
    @Override
    public GenPolynomial<BigInteger> resultant(GenPolynomial<BigInteger> P, GenPolynomial<BigInteger> S) {
        if (S == null || S.isZERO()) {
            return S;
        }
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<BigInteger> fac = P.ring;
        // no special case for univariate polynomials in this class !
        //if (fac.nvar <= 1) {
        //    GenPolynomial<BigInteger> T = iufd.baseResultant(P, S);
        //    return T;
        //}
        long e = P.degree(0);
        long f = S.degree(0);
        GenPolynomial<BigInteger> q;
        GenPolynomial<BigInteger> r;
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
        // compute norms
        BigInteger an = r.maxNorm();
        BigInteger bn = q.maxNorm();
        an = Power.power(fac.coFac, an, f);
        bn = Power.power(fac.coFac, bn, e);
        BigInteger cn = Combinatoric.factorial(e + f);
        BigInteger n = cn.multiply(an).multiply(bn);

        // compute degree vectors
        ExpVector rdegv = r.leadingExpVector(); //degreeVector();
        ExpVector qdegv = q.leadingExpVector(); //degreeVector();

        //initialize prime list and degree vector
        PrimeList primes = new PrimeList();
        int pn = 30; //primes.size();
        ModularRingFactory<MOD> cofac;
        ModularRingFactory<MOD> cofacM = null;
        GenPolynomial<MOD> qm;
        GenPolynomial<MOD> rm;
        GenPolynomialRing<MOD> mfac;
        GenPolynomialRing<MOD> rfac = null;
        int i = 0;
        BigInteger M = null;
        GenPolynomial<MOD> cp = null;
        GenPolynomial<MOD> cm = null;
        GenPolynomial<BigInteger> cpi = null;
        for (java.math.BigInteger p : primes) {
            if (p.longValue() == 2L) { // skip 2
                continue;
            }
            if (++i >= pn) {
                return iufd.resultant(P, S);
                //throw new ArithmeticException("prime list exhausted");
            }
            // initialize coefficient factory and map normalization factor
            if (ModLongRing.MAX_LONG.compareTo(p) > 0) {
                cofac = (ModularRingFactory) new ModLongRing(p, true);
            } else {
                cofac = (ModularRingFactory) new ModIntegerRing(p, true);
            }
            // initialize polynomial factory and map polynomials
            mfac = new GenPolynomialRing<>(cofac, fac);
            qm = PolyUtil.fromIntegerCoefficients(mfac, q);
            if (qm.isZERO() || !qm.leadingExpVector().equals(qdegv)) { //degreeVector()
                continue;
            }
            rm = PolyUtil.fromIntegerCoefficients(mfac, r);
            if (rm.isZERO() || !rm.leadingExpVector().equals(rdegv)) { //degreeVector()
                continue;
            }

            // compute modular resultant
            cm = mufd.resultant(qm, rm);

            // prepare chinese remainder algorithm
            if (M == null) {
                // initialize chinese remainder algorithm
                M = new BigInteger(p);
                cofacM = cofac;
                //rfac = mfac;
                cp = cm;
            } else {
                // apply chinese remainder algorithm
                BigInteger Mp = M;
                MOD mi = cofac.fromInteger(Mp.getVal());
                mi = mi.inverse(); // mod p
                M = M.multiply(new BigInteger(p));
                if (ModLongRing.MAX_LONG.compareTo(M.getVal()) > 0) {
                    cofacM = (ModularRingFactory) new ModLongRing(M.getVal());
                } else {
                    cofacM = (ModularRingFactory) new ModIntegerRing(M.getVal());
                }
                rfac = new GenPolynomialRing<>(cofacM, fac);
                if (!cofac.getClass().equals(cofacM.getClass())) {
                    cofac = (ModularRingFactory) new ModIntegerRing(p);
                    mfac = new GenPolynomialRing<>(cofac, fac);
                    GenPolynomial<BigInteger> mm = PolyUtil.integerFromModularCoefficients(fac, cm);
                    cm = PolyUtil.fromIntegerCoefficients(mfac, mm);
                    mi = cofac.fromInteger(Mp.getVal());
                    mi = mi.inverse(); // mod p
                }
                if (!cp.ring.coFac.getClass().equals(cofacM.getClass())) {
                    ModularRingFactory cop = (ModularRingFactory) cp.ring.coFac;
                    cofac = (ModularRingFactory) new ModIntegerRing(cop.getIntegerModul().getVal());
                    mfac = new GenPolynomialRing<>(cofac, fac);
                    GenPolynomial<BigInteger> mm = PolyUtil.integerFromModularCoefficients(fac, cp);
                    cp = PolyUtil.fromIntegerCoefficients(mfac, mm);
                }
                cp = PolyUtil.chineseRemainder(rfac, cp, mi, cm);
            }
            // test for completion
            if (n.compareTo(M) <= 0) {
                break;
            }
        }
        // convert to integer polynomial
        q = PolyUtil.integerFromModularCoefficients(fac, cp);
        return q;
    }

}
