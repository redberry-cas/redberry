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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.KsubSet;

import java.util.*;


/**
 * Integer coefficients factorization algorithms. This class implements
 * factorization methods for polynomials over integers.
 * @author Heinz Kredel
 */

/**
 * @param <MOD>
 * @author kredel
 */
public class FactorInteger<MOD extends GcdRingElem<MOD> & Modular> extends FactorAbstract<BigInteger> {


    private final boolean debug = false;


    /**
     * Factorization engine for modular base coefficients.
     */
    protected final FactorAbstract<MOD> mfactor;


    /**
     * Gcd engine for modular base coefficients.
     */
    protected final GreatestCommonDivisorAbstract<MOD> mengine;


    /**
     * No argument constructor.
     */
    public FactorInteger() {
        this(BigInteger.ONE);
    }


    /**
     * Constructor.
     *
     * @param cfac coefficient ring factory.
     */
    public FactorInteger(RingFactory<BigInteger> cfac) {
        super(cfac);
        ModularRingFactory<MOD> mcofac = (ModularRingFactory<MOD>) new ModLongRing(13, true); // hack
        mfactor = FactorFactory.getImplementation(mcofac); //new FactorModular(mcofac);
        mengine = GCDFactory.getImplementation(mcofac);
        //mengine = GCDFactory.getProxy(mcofac);
    }


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     *
     * @param P squarefree and primitive! GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<GenPolynomial<BigInteger>> baseFactorsSquarefree(GenPolynomial<BigInteger> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<BigInteger>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<BigInteger> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        if (!engine.baseContent(P).isONE()) {
            throw new IllegalArgumentException(this.getClass().getName() + " P not primitive");
        }
        if (P.degree(0) <= 1L) { // linear is irreducible
            factors.add(P);
            return factors;
        }
        // compute norm
        BigInteger an = P.maxNorm();
        BigInteger ac = P.leadingBaseCoefficient();
        //compute factor coefficient bounds
        ExpVector degv = P.degreeVector();
        int degi = (int) P.degree(0);
        BigInteger M = an.multiply(PolyUtil.factorBound(degv));
        M = M.multiply(ac.abs().multiply(ac.fromInteger(8)));
        //M = M.multiply(M); // test

        //initialize prime list and degree vector
        PrimeList primes = new PrimeList(PrimeList.Range.small);
        int pn = 30; //primes.size();
        ModularRingFactory<MOD> cofac;
        GenPolynomial<MOD> am = null;
        GenPolynomialRing<MOD> mfac = null;
        final int TT = 5; // 7
        List<GenPolynomial<MOD>>[] modfac = new List[TT];
        List<GenPolynomial<BigInteger>>[] intfac = new List[TT];
        List<GenPolynomial<MOD>> mlist = null;
        List<GenPolynomial<BigInteger>> ilist = null;
        int i = 0;
        if (debug) {
        }
        Iterator<java.math.BigInteger> pit = primes.iterator();
        pit.next(); // skip p = 2
        pit.next(); // skip p = 3
        MOD nf = null;
        for (int k = 0; k < TT; k++) {
            if (k == TT - 1) { // -2
                primes = new PrimeList(PrimeList.Range.medium);
                pit = primes.iterator();
            }
            if (k == TT + 1) { // -1
                primes = new PrimeList(PrimeList.Range.large);
                pit = primes.iterator();
            }
            while (pit.hasNext()) {
                java.math.BigInteger p = pit.next();
                if (++i >= pn) {
                    throw new ArithmeticException("prime list exhausted");
                }
                if (ModLongRing.MAX_LONG.compareTo(p) > 0) {
                    cofac = (ModularRingFactory) new ModLongRing(p, true);
                } else {
                    cofac = (ModularRingFactory) new ModIntegerRing(p, true);
                }
                nf = cofac.fromInteger(ac.getVal());
                if (nf.isZERO()) {
                    continue;
                }
                // initialize polynomial factory and map polynomial
                mfac = new GenPolynomialRing<>(cofac, pfac);
                am = PolyUtil.fromIntegerCoefficients(mfac, P);
                if (!am.degreeVector().equals(degv)) { // allways true
                    continue;
                }
                GenPolynomial<MOD> ap = PolyUtil.baseDeriviative(am);
                if (ap.isZERO()) {
                    continue;
                }
                GenPolynomial<MOD> g = mengine.baseGcd(am, ap);
                if (g.isONE()) {
                    break;
                }
            }
            // now am is squarefree mod p, make monic and factor mod p
            if (!nf.isONE()) {
                am = am.divide(nf); // make monic
            }
            mlist = mfactor.baseFactorsSquarefree(am);
            if (mlist.size() <= 1) {
                factors.add(P);
                return factors;
            }
            if (!nf.isONE()) {
                GenPolynomial<MOD> mp = mfac.getONE(); //mlist.get(0);
                mp = mp.multiply(nf);
                mlist.add(0, mp); // set(0,mp);
            }
            modfac[k] = mlist;
        }

        // search shortest factor list
        int min = Integer.MAX_VALUE;
        BitSet AD = null;
        for (int k = 0; k < TT; k++) {
            List<ExpVector> ev = PolyUtil.leadingExpVector(modfac[k]);
            BitSet D = factorDegrees(ev, degi);
            if (AD == null) {
                AD = D;
            } else {
                AD.and(D);
            }
            int s = modfac[k].size();
            if (s < min) {
                min = s;
                mlist = modfac[k];
            }
        }
        if (mlist.size() <= 1) {
            factors.add(P);
            return factors;
        }
        if (AD.cardinality() <= 2) { // only one possible factor
            factors.add(P);
            return factors;
        }

        boolean allLists = false; //true; //false;
        if (allLists) {
            // try each factor list
            for (int k = 0; k < TT; k++) {
                mlist = modfac[k];
                if (debug) {
                }
                if (P.leadingBaseCoefficient().isONE()) { // monic case
                    factors = searchFactorsMonic(P, M, mlist, AD); // does now work in all cases
                    if (factors.size() == 1) {
                        factors = searchFactorsNonMonic(P, M, mlist, AD);
                    }
                } else {
                    factors = searchFactorsNonMonic(P, M, mlist, AD);
                }
                intfac[k] = factors;
            }
        } else {
            // try only shortest factor list
            if (debug) {
            }
            if (P.leadingBaseCoefficient().isONE()) {
                long t = System.currentTimeMillis();
                try {
                    mlist = PolyUtil.monic(mlist);
                    factors = searchFactorsMonic(P, M, mlist, AD); // does now work in all cases
                    if (debug) {
                        t = System.currentTimeMillis();
                        List<GenPolynomial<BigInteger>> fnm = searchFactorsNonMonic(P, M, mlist, AD);
                        t = System.currentTimeMillis() - t;
                    }
                } catch (RuntimeException e) {
                    factors = searchFactorsNonMonic(P, M, mlist, AD);
                }
            } else {
                long t = System.currentTimeMillis();
                factors = searchFactorsNonMonic(P, M, mlist, AD);
            }
            return normalizeFactorization(factors);
        }

        // search longest factor list
        int max = 0;
        for (int k = 0; k < TT; k++) {
            int s = intfac[k].size();
            if (s > max) {
                max = s;
                ilist = intfac[k];
            }
        }
        factors = normalizeFactorization(ilist);
        return factors;
    }


    /**
     * BitSet for factor degree list.
     *
     * @param E exponent vector list.
     * @return b_0, ..., b_k} a BitSet of possible factor degrees.
     */
    public BitSet factorDegrees(List<ExpVector> E, int deg) {
        BitSet D = new BitSet(deg + 1);
        D.set(0); // constant factor
        for (ExpVector e : E) {
            int i = (int) e.getVal(0);
            BitSet s = new BitSet(deg + 1);
            for (int k = 0; k < deg + 1 - i; k++) { // shift by i places
                s.set(i + k, D.get(k));
            }
            D.or(s);
        }
        return D;
    }


    /**
     * Sum of all degrees.
     *
     * @param L univariate polynomial list.
     * @return sum deg(p) for p in L.
     */
    public static <C extends RingElem<C>> long degreeSum(List<GenPolynomial<C>> L) {
        long s = 0L;
        for (GenPolynomial<C> p : L) {
            ExpVector e = p.leadingExpVector();
            long d = e.getVal(0);
            s += d;
        }
        return s;
    }


    /**
     * Factor search with modular Hensel lifting algorithm. Let p =
     * f_i.ring.coFac.modul() i = 0, ..., n-1 and assume C == prod_{0,...,n-1}
     * f_i mod p with ggt(f_i,f_j) == 1 mod p for i != j
     *
     * @param C GenPolynomial.
     * @param M bound on the coefficients of g_i as factors of C.
     * @param F = [f_0,...,f_{n-1}] List&lt;GenPolynomial&gt;.
     * @param D bit set of possible factor degrees.
     * @return [g_0, ..., g_{n-1}] = lift(C,F), with C = prod_{0,...,n-1} g_i mod
     *         p**e. <b>Note:</b> does not work in all cases.
     */
    List<GenPolynomial<BigInteger>> searchFactorsMonic(GenPolynomial<BigInteger> C, BigInteger M,
                                                       List<GenPolynomial<MOD>> F, BitSet D) {
        if (C == null || C.isZERO() || F == null || F.size() == 0) {
            throw new IllegalArgumentException("C must be nonzero and F must be nonempty");
        }
        GenPolynomialRing<BigInteger> pfac = C.ring;
        if (pfac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        List<GenPolynomial<BigInteger>> factors = new ArrayList<>(F.size());
        List<GenPolynomial<MOD>> mlist = F;
        List<GenPolynomial<MOD>> lift;

        //MOD nf = null;
        GenPolynomial<MOD> ct = mlist.get(0);
        if (ct.isConstant()) {
            //nf = ct.leadingBaseCoefficient();
            mlist.remove(ct);
            if (mlist.size() <= 1) {
                factors.add(C);
                return factors;
            }
        } else {
            //nf = ct.ring.coFac.getONE();
        }
        ModularRingFactory<MOD> mcfac = (ModularRingFactory<MOD>) ct.ring.coFac;
        BigInteger m = mcfac.getIntegerModul();
        long k = 1;
        BigInteger pi = m;
        while (pi.compareTo(M) < 0) {
            k++;
            pi = pi.multiply(m);
        }
        GenPolynomial<BigInteger> PP = C, P = C;
        // lift via Hensel
        try {
            lift = HenselUtil.liftHenselMonic(PP, mlist, k);
        } catch (NoLiftingException e) {
            throw new RuntimeException(e);
        }
        GenPolynomialRing<MOD> mpfac = lift.get(0).ring;

        // combine trial factors
        int dl = (lift.size() + 1) / 2;
        GenPolynomial<BigInteger> u = PP;
        long deg = (u.degree(0) + 1L) / 2L;
        //BigInteger ldcf = u.leadingBaseCoefficient();
        for (int j = 1; j <= dl; j++) {
            KsubSet<GenPolynomial<MOD>> ps = new KsubSet<>(lift, j);
            for (List<GenPolynomial<MOD>> flist : ps) {
                if (!D.get((int) FactorInteger.<MOD>degreeSum(flist))) {
                    continue;
                }
                GenPolynomial<MOD> mtrial = Power.multiply(mpfac, flist);
                //GenPolynomial<MOD> mtrial = mpfac.getONE();
                //for (int kk = 0; kk < flist.size(); kk++) {
                //    GenPolynomial<MOD> fk = flist.get(kk);
                //    mtrial = mtrial.multiply(fk);
                //}
                if (mtrial.degree(0) > deg) { // this test is sometimes wrong
                    //continue;
                }
                GenPolynomial<BigInteger> trial = PolyUtil.integerFromModularCoefficients(pfac, mtrial);
                //trial = engine.basePrimitivePart( trial.multiply(ldcf) );
                trial = engine.basePrimitivePart(trial);
                if (PolyUtil.<BigInteger>baseSparsePseudoRemainder(u, trial).isZERO()) {
                    //trial = engine.basePrimitivePart(trial);
                    factors.add(trial);
                    u = PolyUtil.basePseudoDivide(u, trial); //u.divide( trial );
                    //if (lift.removeAll(flist)) {
                    lift = removeOnce(lift, flist);
                    dl = (lift.size() + 1) / 2;
                    j = 0; // since j++
                    break;
                }
            }
        }
        if (!u.isONE() && !u.equals(P)) {
            factors.add(u);
        }
        if (factors.size() == 0) {
            factors.add(PP);
        }
        return normalizeFactorization(factors);
    }


    /**
     * Factor search with modular Hensel lifting algorithm. Let p =
     * f_i.ring.coFac.modul() i = 0, ..., n-1 and assume C == prod_{0,...,n-1}
     * f_i mod p with ggt(f_i,f_j) == 1 mod p for i != j
     *
     * @param C GenPolynomial.
     * @param M bound on the coefficients of g_i as factors of C.
     * @param F = [f_0,...,f_{n-1}] List&lt;GenPolynomial&gt;.
     * @param D bit set of possible factor degrees.
     * @return [g_0, ..., g_{n-1}] = lift(C,F), with C = prod_{0,...,n-1} g_i mod
     *         p**e.
     */
    List<GenPolynomial<BigInteger>> searchFactorsNonMonic(GenPolynomial<BigInteger> C, BigInteger M,
                                                          List<GenPolynomial<MOD>> F, BitSet D) {
        if (C == null || C.isZERO() || F == null || F.size() == 0) {
            throw new IllegalArgumentException("C must be nonzero and F must be nonempty");
        }
        GenPolynomialRing<BigInteger> pfac = C.ring;
        if (pfac.nvar != 1) { // todo assert
            throw new IllegalArgumentException("polynomial ring not univariate");
        }
        List<GenPolynomial<BigInteger>> factors = new ArrayList<>(F.size());
        List<GenPolynomial<MOD>> mlist = F;

        MOD nf;
        GenPolynomial<MOD> ct = mlist.get(0);
        if (ct.isConstant()) {
            nf = ct.leadingBaseCoefficient();
            mlist.remove(ct);
            if (mlist.size() <= 1) {
                factors.add(C);
                return factors;
            }
        } else {
            nf = ct.ring.coFac.getONE();
        }
        GenPolynomialRing<MOD> mfac = ct.ring;
        GenPolynomial<MOD> Pm = PolyUtil.fromIntegerCoefficients(mfac, C);
        GenPolynomial<BigInteger> PP = C, P = C;

        // combine trial factors
        int dl = (mlist.size() + 1) / 2;
        GenPolynomial<BigInteger> u = PP;
        long deg = (u.degree(0) + 1L) / 2L;
        GenPolynomial<MOD> um = Pm;
        //BigInteger ldcf = u.leadingBaseCoefficient();
        HenselApprox<MOD> ilist;
        for (int j = 1; j <= dl; j++) {
            KsubSet<GenPolynomial<MOD>> ps = new KsubSet<>(mlist, j);
            for (List<GenPolynomial<MOD>> flist : ps) {
                if (!D.get((int) FactorInteger.<MOD>degreeSum(flist))) {
                    continue;
                }
                GenPolynomial<MOD> trial = mfac.getONE().multiply(nf);
                for (GenPolynomial<MOD> fk : flist) {
                    trial = trial.multiply(fk);
                }
                if (trial.degree(0) > deg) { // this test is sometimes wrong
                    //continue;
                }
                GenPolynomial<MOD> cofactor = um.divide(trial);

                // lift via Hensel
                try {
                    // ilist = HenselUtil.liftHenselQuadraticFac(PP, M, trial, cofactor);
                    ilist = HenselUtil.liftHenselQuadratic(PP, M, trial, cofactor);
                    //ilist = HenselUtil.<MOD> liftHensel(PP, M, trial, cofactor);
                } catch (NoLiftingException e) {
                    // no liftable factors
                    continue;
                }
                GenPolynomial<BigInteger> itrial = ilist.A;
                GenPolynomial<BigInteger> icofactor = ilist.B;

                itrial = engine.basePrimitivePart(itrial);
                if (PolyUtil.<BigInteger>baseSparsePseudoRemainder(u, itrial).isZERO()) {
                    //itrial = engine.basePrimitivePart(itrial);
                    factors.add(itrial);
                    //u = PolyUtil.<BigInteger> basePseudoDivide(u, itrial); //u.divide( trial );
                    u = icofactor;
                    PP = u; // fixed finally on 2009-05-03
                    um = cofactor;
                    //if (mlist.removeAll(flist)) {
                    mlist = removeOnce(mlist, flist);
                    dl = (mlist.size() + 1) / 2;
                    j = 0; // since j++
                    break;
                }
            }
        }
        if (!u.isONE() && !u.equals(P)) {
            factors.add(u);
        }
        if (factors.size() == 0) {
            factors.add(PP);
        }
        return normalizeFactorization(factors);
    }


    /**
     * GenPolynomial factorization of a multivariate squarefree polynomial,
     * using Hensel lifting if possible.
     *
     * @param P squarefree and primitive! (respectively monic) multivariate
     *          GenPolynomial over the integers.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,r} p_i.
     */
    @Override
    public List<GenPolynomial<BigInteger>> factorsSquarefree(GenPolynomial<BigInteger> P) {
        GenPolynomialRing<BigInteger> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return baseFactorsSquarefree(P);
        }
        List<GenPolynomial<BigInteger>> topt = new ArrayList<>(1);
        topt.add(P);
        OptimizedPolynomialList<BigInteger> opt = TermOrderOptimization.optimizeTermOrder(pfac,
                topt);
        P = opt.list.get(0);
        List<Integer> iperm = TermOrderOptimization.inversePermutation(opt.perm);

        ExpVector degv = P.degreeVector();
        int[] donv = degv.dependencyOnVariables();
        List<GenPolynomial<BigInteger>> facs = null;
        if (degv.length() == donv.length) { // all variables appear, hack for Hensel, TODO check
            try {
                facs = factorsSquarefreeHensel(P);
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } else { // not all variables appear, remove unused variables, hack for Hensel, TODO check
            GenPolynomial<BigInteger> pu = PolyUtil.removeUnusedUpperVariables(P);
            try {
                facs = factorsSquarefreeHensel(pu);
                List<GenPolynomial<BigInteger>> fs = new ArrayList<>(facs.size());
                GenPolynomialRing<BigInteger> pf = P.ring;
                GenPolynomialRing<BigInteger> pfu = pu.ring;
                for (GenPolynomial<BigInteger> p : facs) {
                    GenPolynomial<BigInteger> pel = p.extendLower(pfu, 0, 0L);
                    GenPolynomial<BigInteger> pe = pel.extend(pf, 0, 0L);
                    fs.add(pe);
                }
                facs = fs;
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        if (facs == null) {
            facs = super.factorsSquarefree(P);
        }
        List<GenPolynomial<BigInteger>> iopt = TermOrderOptimization.permutation(iperm, pfac,
                facs);
        facs = normalizeFactorization(iopt);
        return facs;
    }


    /**
     * GenPolynomial factorization of a multivariate squarefree polynomial,
     * using Hensel lifting.
     *
     * @param P squarefree and primitive! (respectively monic) multivariate
     *          GenPolynomial over the integers.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,r} p_i.
     */
    public List<GenPolynomial<BigInteger>> factorsSquarefreeHensel(GenPolynomial<BigInteger> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<BigInteger> pfac = P.ring;
        if (pfac.nvar == 1) {
            return baseFactorsSquarefree(P);
        }
        List<GenPolynomial<BigInteger>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.degreeVector().totalDeg() <= 1L) {
            factors.add(P);
            return factors;
        }
        GenPolynomial<BigInteger> pd = P;
        // ldcf(pd)
        BigInteger ac = pd.leadingBaseCoefficient();

        // factor leading coefficient as polynomial in the lowest! variable
        GenPolynomialRing<GenPolynomial<BigInteger>> rnfac = pfac.recursive(pfac.nvar - 1);
        GenPolynomial<GenPolynomial<BigInteger>> pr = PolyUtil.recursive(rnfac, pd);
        GenPolynomial<GenPolynomial<BigInteger>> prr = PolyUtil.switchVariables(pr);

        GenPolynomial<BigInteger> prrc = engine.recursiveContent(prr); // can have content wrt this variable
        List<GenPolynomial<BigInteger>> cfactors = null;
        if (!prrc.isONE()) {
            prr = PolyUtil.recursiveDivide(prr, prrc);
            GenPolynomial<BigInteger> prrcu = prrc.extendLower(pfac, 0, 0L); // since switched vars
            pd = PolyUtil.basePseudoDivide(pd, prrcu);
            cfactors = factorsSquarefree(prrc);
            List<GenPolynomial<BigInteger>> cff = new ArrayList<>(cfactors.size());
            for (GenPolynomial<BigInteger> fs : cfactors) {
                GenPolynomial<BigInteger> fsp = fs.extendLower(pfac, 0, 0L); // since switched vars
                cff.add(fsp);
            }
            cfactors = cff;
        }
        GenPolynomial<BigInteger> lprr = prr.leadingBaseCoefficient();
        boolean isMonic = false; // multivariate monic
        if (lprr.isConstant()) { // isONE ?
            isMonic = true;
        }
        SortedMap<GenPolynomial<BigInteger>, Long> lfactors = factors(lprr);
        List<GenPolynomial<BigInteger>> lfacs = new ArrayList<>(lfactors.keySet());

        // search evaluation point and evaluate
        GenPolynomialRing<BigInteger> cpfac;
        GenPolynomial<BigInteger> pe;
        GenPolynomial<BigInteger> pep;
        GenPolynomialRing<BigInteger> ccpfac;
        List<GenPolynomial<BigInteger>> ce;
        List<GenPolynomial<BigInteger>> cep;
        List<BigInteger> cei;
        List<BigInteger> dei;
        BigInteger pec;
        BigInteger pecw;
        BigInteger ped;

        List<GenPolynomial<BigInteger>> ufactors;
        List<TrialParts> tParts = new ArrayList<>();
        List<GenPolynomial<BigInteger>> lf;
        GenPolynomial<BigInteger> lpx;
        List<GenPolynomial<BigInteger>> ln = null;
        List<GenPolynomial<BigInteger>> un = null;
        GenPolynomial<BigInteger> pes = null;

        List<BigInteger> V;
        long evStart = 0L; //3L * 5L;
        List<Long> Evs = new ArrayList<>(pfac.nvar + 1); // Evs(0), Evs(1) unused
        for (int j = 0; j <= pfac.nvar; j++) {
            Evs.add(evStart);
        }
        final int trials = 4;
        int countSeparate = 0;
        final int COUNT_MAX = 50;
        double ran = 1.001; // higher values not good
        boolean isPrimitive;
        boolean notLucky = true;
        while (notLucky) { // for Wang's test
            if (Math.abs(evStart) > 371L) {
                throw new RuntimeException("no lucky evaluation point found after " + Math.abs(evStart)
                        + " iterations");
            }
            if (Math.abs(evStart) % 100L <= 3L) {
                ran = ran * (Math.PI - 2.14);
            }
            notLucky = false;
            V = new ArrayList<>();
            cpfac = pfac;
            pe = pd;
            ccpfac = lprr.ring;
            ce = lfacs;
            cep = null;
            cei = null;
            pec = null;
            ped = null;
            long vi = 0L;
            for (int j = pfac.nvar; j > 1; j--) {
                // evaluation up to univariate case
                long degp = pe.degree(cpfac.nvar - 2);
                cpfac = cpfac.contract(1);
                ccpfac = ccpfac.contract(1);
                //vi = evStart; // + j;//0L; //(long)(pfac.nvar-j); // 1L; 0 not so good for small p
                vi = Evs.get(j); //evStart + j;//0L; //(long)(pfac.nvar-j); // 1L; 0 not so good for small p
                BigInteger Vi;

                // search evaluation point
                boolean doIt = true;
                Vi = null;
                pep = null;
                while (doIt) {
                    Vi = new BigInteger(vi);
                    pep = PolyUtil.evaluateMain(cpfac, pe, Vi);
                    // check lucky evaluation point 
                    if (degp == pep.degree(cpfac.nvar - 1)) {
                        // check squarefree
                        if (sengine.isSquarefree(pep)) { // cpfac.nvar == 1 && ?? no, must test on each variable
                            //if ( isNearlySquarefree(pep) ) {
                            doIt = false; //break;
                        }
                    }
                    if (vi > 0L) {
                        vi = -vi;
                    } else {
                        vi = 1L - vi;
                    }
                }
                //if ( !isMonic ) {
                if (ccpfac.nvar >= 1) {
                    cep = PolyUtil.evaluateMain(ccpfac, ce, Vi);
                } else {
                    cei = PolyUtil.evaluateMain(ccpfac.coFac, ce, Vi);
                }
                //}
                int jj = (int) Math.round(ran + 0.52 * Math.random()); // j, random increment
                //jj = 1; // ...4 test   
                if (vi > 0L) {
                    Evs.set(j, vi + jj); // record last tested value plus increment
                    evStart = vi + jj;
                } else {
                    Evs.set(j, vi - jj); // record last tested value minus increment
                    evStart = vi - jj;
                }
                //evStart = vi+1L;
                V.add(Vi);
                pe = pep;
                ce = cep;
            }
            pecw = engine.baseContent(pe); // original Wang
            isPrimitive = pecw.isONE();
            ped = ccpfac.coFac.getONE();
            pec = pe.ring.coFac.getONE();
            if (!isMonic) {
                if (countSeparate > COUNT_MAX) {
                    pec = pe.ring.coFac.getONE(); // hack is sometimes better
                } else {
                    pec = pecw;
                }
                //pec = pecw;
                if (lfacs.get(0).isConstant()) {
                    ped = cei.remove(0);
                    //lfacs.remove(0); // later
                }
                // test Wang's condition
                dei = new ArrayList<>();
                dei.add(pec.multiply(ped).abs()); // .abs()
                int i = 1;
                for (BigInteger ci : cei) {
                    if (ci.isZERO()) {
                        notLucky = true;
                        break;
                    }
                    BigInteger q = ci.abs();
                    for (int ii = i - 1; ii >= 0; ii--) {
                        BigInteger r = dei.get(ii);
                        while (!r.isONE()) {
                            r = r.gcd(q);
                            q = q.divide(r);
                        }
                    }
                    dei.add(q);
                    if (q.isONE()) {
                        if (!testSeparate(cei, pecw)) {
                            countSeparate++;
                            if (countSeparate > COUNT_MAX) {
                            }
                        }
                        notLucky = true;
                        break;
                    }
                    i++;
                }
            }
            if (notLucky) {
                continue;
            }
            //pe = pe.abs();
            //ufactors = baseFactorsRadical(pe); //baseFactorsSquarefree(pe); wrong since not primitive
            ufactors = baseFactorsSquarefree(pe.divide(pecw)); //wrong if not primitive
            if (!pecw.isONE()) {
                ufactors.add(0, cpfac.getONE().multiply(pecw));
            }
            if (ufactors.size() <= 1) {
                factors.add(pd); // P
                if (cfactors != null) {
                    cfactors.addAll(factors);
                    factors = cfactors;
                }
                return factors;
            }

            // determine leading coefficient polynomials for factors
            lf = new ArrayList<>();
            lpx = lprr.ring.getONE();
            for (GenPolynomial<BigInteger> unused : ufactors) {
                lf.add(lprr.ring.getONE());
            }
            if (!isMonic || !pecw.isONE()) {
                if (lfacs.size() > 0 && lfacs.get(0).isConstant()) {
                    GenPolynomial<BigInteger> unused = lfacs.remove(0);
                    //BigInteger xxi = xx.leadingBaseCoefficient();
                }
                for (int i = ufactors.size() - 1; i >= 0; i--) {
                    GenPolynomial<BigInteger> pp = ufactors.get(i);
                    BigInteger ppl = pp.leadingBaseCoefficient();
                    ppl = ppl.multiply(pec); // content
                    GenPolynomial<BigInteger> lfp = lf.get(i);
                    int ii = 0;
                    for (BigInteger ci : cei) {
                        if (ci.abs().isONE()) {
                            throw new RuntimeException("something is wrong, ci is a unit");
                            //notLucky = true;
                        }
                        while (ppl.remainder(ci).isZERO() && lfacs.size() > ii) {
                            ppl = ppl.divide(ci);
                            lfp = lfp.multiply(lfacs.get(ii));
                        }
                        ii++;
                    }
                    lfp = lfp.multiply(ppl);
                    lf.set(i, lfp);
                }
                // adjust if pec != 1
                pec = pecw;
                lpx = Power.multiply(lprr.ring, lf); // test only, not used
                if (!lprr.degreeVector().equals(lpx.degreeVector())) {
                    notLucky = true;
                    continue;
                }
                if (!pec.isONE()) { // content, was always false by hack
                    // evaluate factors of ldcf
                    List<GenPolynomial<BigInteger>> lfe = lf;
                    List<BigInteger> lfei = null;
                    ccpfac = lprr.ring;
                    for (int j = lprr.ring.nvar; j > 0; j--) {
                        ccpfac = ccpfac.contract(1);
                        BigInteger Vi = V.get(lprr.ring.nvar - j);
                        if (ccpfac.nvar >= 1) {
                            lfe = PolyUtil.evaluateMain(ccpfac, lfe, Vi);
                        } else {
                            lfei = PolyUtil.evaluateMain(ccpfac.coFac, lfe, Vi);
                        }
                    }

                    ln = new ArrayList<>(lf.size());
                    un = new ArrayList<>(lf.size());
                    for (int jj = 0; jj < lf.size(); jj++) {
                        GenPolynomial<BigInteger> up = ufactors.get(jj);
                        BigInteger ui = up.leadingBaseCoefficient();
                        BigInteger li = lfei.get(jj);
                        BigInteger di = ui.gcd(li).abs();
                        BigInteger udi = ui.divide(di);
                        BigInteger ldi = li.divide(di);
                        GenPolynomial<BigInteger> lp = lf.get(jj);
                        GenPolynomial<BigInteger> lpd = lp.multiply(udi);
                        GenPolynomial<BigInteger> upd = up.multiply(ldi);
                        if (pec.isONE()) {
                            ln.add(lp);
                            un.add(up);
                        } else {
                            ln.add(lpd);
                            un.add(upd);
                            BigInteger pec1 = pec.divide(ldi);
                            pec = pec1;
                        }
                    }
                    if (!lf.equals(ln) || !un.equals(ufactors)) {
                        //lf = ln;
                        //ufactors = un;
                        // adjust pe
                    }
                    if (!pec.isONE()) { // still not 1
                        ln = new ArrayList<>(lf.size());
                        un = new ArrayList<>(lf.size());
                        pes = pe;
                        for (int jj = 0; jj < lf.size(); jj++) {
                            GenPolynomial<BigInteger> up = ufactors.get(jj);
                            GenPolynomial<BigInteger> lp = lf.get(jj);
                            if (!up.isConstant()) {
                                up = up.multiply(pec);
                            }
                            lp = lp.multiply(pec);
                            if (jj != 0) {
                                pes = pes.multiply(pec);
                            }
                            un.add(up);
                            ln.add(lp);
                        }
                        if (pes.equals(Power.<GenPolynomial<BigInteger>>multiply(pe.ring, un))) {
                            //ystem.out.println("*ln  = " + ln + ", *lf = " + lf);
                            isPrimitive = false;
                            //pe = pes;
                            //lf = ln;
                            //ufactors = un;
                        } else {
                        }
                    }
                }
                if (notLucky) {
                    continue;
                }
                lpx = Power.multiply(lprr.ring, lf);
                if (!lprr.abs().equals(lpx.abs())) { // not correctly distributed
                    if (!lprr.degreeVector().equals(lpx.degreeVector())) {
                        notLucky = true;
                    }
                }
            } // end determine leading coefficients for factors

            if (!notLucky) {
                TrialParts tp = null;
                if (isPrimitive) {
                    tp = new TrialParts(V, pe, ufactors, cei, lf);
                } else {
                    tp = new TrialParts(V, pes, un, cei, ln);
                }
                if (tp.univPoly != null) {
                    if (tp.ldcfEval.size() != 0) {
                        tParts.add(tp);
                    }
                }
                if (tParts.size() < trials) {
                    notLucky = true;
                }
            }
        } // end notLucky loop

        // search TrialParts with shortest factorization of univariate polynomial
        int min = Integer.MAX_VALUE;
        TrialParts tpmin = null;
        for (TrialParts tp : tParts) {
            if (tp.univFactors.size() < min) {
                min = tp.univFactors.size();
                tpmin = tp;
            }
        }
        for (TrialParts tp : tParts) {
            if (tp.univFactors.size() == min) {
                if (!tp.univFactors.get(0).isConstant()) {
                    tpmin = tp;
                    break;
                }
            }
        }
        // set to (first) shortest 
        V = tpmin.evalPoints;
        pe = tpmin.univPoly;
        ufactors = tpmin.univFactors;
        cei = tpmin.ldcfEval; // unused
        lf = tpmin.ldcfFactors;

        GenPolynomialRing<BigInteger> ufac = pe.ring;

        //initialize prime list
        PrimeList primes = new PrimeList(PrimeList.Range.medium); // PrimeList.Range.medium);
        Iterator<java.math.BigInteger> primeIter = primes.iterator();
        int pn = 50; //primes.size();
        BigInteger ae = pe.leadingBaseCoefficient();
        GenPolynomial<MOD> Pm = null;
        ModularRingFactory<MOD> cofac = null;
        GenPolynomialRing<MOD> mufac = null;

        // search lucky prime
        for (int i = 0; i < 11; i++) { // prime meta loop
            //for ( int i = 0; i < 1; i++ ) { // meta loop
            java.math.BigInteger p = null; //new java.math.BigInteger("19"); //primes.next();
            // 2 small, 5 medium and 4 large size primes
            if (i == 0) { // medium size
                primes = new PrimeList(PrimeList.Range.medium);
                primeIter = primes.iterator();
            }
            if (i == 5) { // small size
                primes = new PrimeList(PrimeList.Range.small);
                primeIter = primes.iterator();
                primeIter.next(); // 2
                primeIter.next(); // 3
                primeIter.next(); // 5
                primeIter.next(); // 7
            }
            if (i == 7) { // large size
                primes = new PrimeList(PrimeList.Range.large);
                primeIter = primes.iterator();
            }
            int pi = 0;
            while (pi < pn && primeIter.hasNext()) {
                p = primeIter.next();
                // initialize coefficient factory and map normalization factor and polynomials
                ModularRingFactory<MOD> cf;
                if (ModLongRing.MAX_LONG.compareTo(p) > 0) {
                    cf = (ModularRingFactory) new ModLongRing(p, true);
                } else {
                    cf = (ModularRingFactory) new ModIntegerRing(p, true);
                }
                MOD nf = cf.fromInteger(ae.getVal());
                if (nf.isZERO()) {
                    continue;
                }
                mufac = new GenPolynomialRing<>(cf, ufac);
                Pm = PolyUtil.fromIntegerCoefficients(mufac, pe);
                if (!mfactor.isSquarefree(Pm)) {
                    continue;
                }
                cofac = cf;
                break;
            }
            if (cofac != null) {
                break;
            }
        } // end prime meta loop
        if (cofac == null) { // no lucky prime found
            throw new RuntimeException("giving up on Hensel preparation, no lucky prime found");
        }

        // coefficient bound
        BigInteger an = pd.maxNorm();
        BigInteger mn = an.multiply(ac.abs()).multiply(new BigInteger(2L));
        long k = Power.logarithm(cofac.getIntegerModul(), mn) + 1L;

        BigInteger q = Power.positivePower(cofac.getIntegerModul(), k);
        ModularRingFactory<MOD> muqfac;
        if (ModLongRing.MAX_LONG.compareTo(q.getVal()) > 0) {
            muqfac = (ModularRingFactory) new ModLongRing(q.getVal());
        } else {
            muqfac = (ModularRingFactory) new ModIntegerRing(q.getVal());
        }
        GenPolynomialRing<MOD> mucpfac = new GenPolynomialRing<>(muqfac, ufac);

        List<GenPolynomial<MOD>> muqfactors = PolyUtil.fromIntegerCoefficients(mucpfac, ufactors);

        // convert C from Z[...] to Z_q[...]
        GenPolynomialRing<MOD> qcfac = new GenPolynomialRing<>(muqfac, pd.ring);
        GenPolynomial<MOD> pq = PolyUtil.fromIntegerCoefficients(qcfac, pd);

        List<MOD> Vm = new ArrayList<>(V.size());
        for (BigInteger v : V) {
            MOD vm = muqfac.fromInteger(v.getVal());
            Vm.add(vm);
        }

        // Hensel lifting of factors
        List<GenPolynomial<MOD>> mlift;
        try {
            mlift = HenselMultUtil.liftHensel(pd, pq, muqfactors, Vm, k, lf);
        } catch (NoLiftingException nle) {
            //nle.printStackTrace();
            mlift = new ArrayList<>();
            throw new RuntimeException(nle);
        } catch (ArithmeticException aex) {
            //aex.printStackTrace();
            mlift = new ArrayList<>();
            throw aex;
        }
        if (mlift.size() <= 1) { // irreducible mod I, p^k, can this happen?
            factors.add(pd); // P
            if (cfactors != null) {
                cfactors.addAll(factors);
                factors = cfactors;
            }
            return factors;
        }

        // combine trial factors
        GenPolynomialRing<MOD> mfac = mlift.get(0).ring;
        int dl = (mlift.size() + 1) / 2;
        GenPolynomial<BigInteger> u = P;
        long deg = (u.degree() + 1L) / 2L;

        GenPolynomial<BigInteger> ui = pd;
        for (int j = 1; j <= dl; j++) {
            KsubSet<GenPolynomial<MOD>> subs = new KsubSet<>(mlift, j);
            for (List<GenPolynomial<MOD>> flist : subs) {
                GenPolynomial<MOD> mtrial = Power.multiply(mfac, flist);
                if (mtrial.degree() > deg) { // this test is sometimes wrong
                    //continue;
                }
                GenPolynomial<BigInteger> trial = PolyUtil.integerFromModularCoefficients(pfac, mtrial);
                trial = engine.basePrimitivePart(trial);
                //if ( ! isPrimitive ) {
                //}
                if (debug) {
                }
                if (PolyUtil.<BigInteger>baseSparsePseudoRemainder(ui, trial).isZERO()) {
                    factors.add(trial);
                    ui = PolyUtil.basePseudoDivide(ui, trial);
                    mlift = removeOnce(mlift, flist);
                    if (mlift.size() > 1) {
                        dl = (mlift.size() + 1) / 2;
                        j = 0; // since j++
                        break;
                    }
                    factors.add(ui);
                    if (cfactors != null) {
                        cfactors.addAll(factors);
                        factors = cfactors;
                    }
                    return normalizeFactorization(factors);
                }
            }
        }
        if (!ui.isONE() && !ui.equals(pd)) {
            // pp(ui) ?? no ??
            factors.add(ui);
        }
        if (factors.size() == 0) {
            factors.add(pd); // P
        }
        if (cfactors != null) {
            cfactors.addAll(factors);
            factors = cfactors;
        }
        return normalizeFactorization(factors);
    }


    /**
     * Test if b has a prime factor different to the elements of A.
     *
     * @param A list of integer with at least one different prime factor.
     * @param b integer to test with A.
     * @return true, if b hase a prime factor different to elements of A
     */
    boolean testSeparate(List<BigInteger> A, BigInteger b) {
        int i = 0;
        for (BigInteger c : A) {
            BigInteger g = c.gcd(b).abs();
            if (!g.isONE()) {
                i++;
            }
        }
        //if ( i >= 1 ) {
        //}
        return (i <= 1);
    }

}


/**
 * Container for factorization trial lifting parameters.
 */
class TrialParts {


    /**
     * evaluation points
     */
    public final List<BigInteger> evalPoints;


    /**
     * univariate polynomial
     */
    public final GenPolynomial<BigInteger> univPoly;


    /**
     * irreducible factors of univariate polynomial
     */
    public final List<GenPolynomial<BigInteger>> univFactors;


    /**
     * irreducible factors of leading coefficient
     */
    public final List<GenPolynomial<BigInteger>> ldcfFactors;


    /**
     * evaluated factors of leading coefficient factors by evaluation points
     */
    public final List<BigInteger> ldcfEval;


    /**
     * Constructor.
     *
     * @param ev evaluation points.
     * @param up univariate polynomial.
     * @param uf irreducible factors of up.
     * @param le irreducible factors of leading coefficient.
     * @param lf evaluated le by evaluation points.
     */
    public TrialParts(List<BigInteger> ev, GenPolynomial<BigInteger> up, List<GenPolynomial<BigInteger>> uf,
                      List<BigInteger> le, List<GenPolynomial<BigInteger>> lf) {
        evalPoints = ev;
        univPoly = up;
        univFactors = uf;
        //ldcfPoly = lp;
        ldcfFactors = lf;
        ldcfEval = le;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TrialParts[");
        sb.append("evalPoints = ").append(evalPoints);
        sb.append(", univPoly = ").append(univPoly);
        sb.append(", univFactors = ").append(univFactors);
        sb.append(", ldcfEval = ").append(ldcfEval);
        sb.append(", ldcfFactors = ").append(ldcfFactors);
        sb.append("]");
        return sb.toString();
    }

}
