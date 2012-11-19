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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.ExpVector;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.KsubSet;

import java.util.*;


/**
 * Abstract factorization algorithms class. This class contains implementations
 * of all methods of the <code>Factorization</code> interface, except the method
 * for factorization of a squarefree polynomial. The methods to obtain
 * squarefree polynomials delegate the computation to the
 * <code>GreatestCommonDivisor</code> classes and are included for convenience.
 *
 * @param <C> coefficient type
 * @author Heinz Kredel
 * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.FactorFactory
 */

public abstract class FactorAbstract<C extends GcdRingElem<C>> implements Factorization<C> {

    /**
     * Gcd engine for base coefficients.
     */
    protected final GreatestCommonDivisorAbstract<C> engine;


    /**
     * Squarefree decompositon engine for base coefficients.
     */
    protected final SquarefreeAbstract<C> sengine;


    /**
     * No argument constructor.
     */
    protected FactorAbstract() {
        throw new IllegalArgumentException("don't use this constructor");
    }


    /**
     * Constructor.
     *
     * @param cfac coefficient ring factory.
     */
    public FactorAbstract(RingFactory<C> cfac) {
        engine = GCDFactory.getProxy(cfac);
        //engine = GCDFactory.<C> getImplementation(cfac);
        sengine = SquarefreeFactory.getImplementation(cfac);
    }


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
     * GenPolynomial test if is irreducible.
     *
     * @param P GenPolynomial.
     * @return true if P is irreducible, else false.
     */
    public boolean isIrreducible(GenPolynomial<C> P) {
        if (!isSquarefree(P)) {
            return false;
        }
        List<GenPolynomial<C>> F = factorsSquarefree(P);
        if (F.size() == 1) {
            return true;
        } else if (F.size() > 2) {
            return false;
        } else { //F.size() == 2
            boolean cnst = false;
            for (GenPolynomial<C> p : F) {
                if (p.isConstant()) {
                    cnst = true;
                }
            }
            return cnst;
        }
    }

    /**
     * GenPolynomial test if is squarefree.
     *
     * @param P GenPolynomial.
     * @return true if P is squarefree, else false.
     */
    public boolean isSquarefree(GenPolynomial<C> P) {
        return sengine.isSquarefree(P);
    }


    /**
     * GenPolynomial factorization of a squarefree polynomial, using Kronecker
     * substitution.
     *
     * @param P squarefree and primitive! (respectively monic) GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,r} p_i.
     */
    public List<GenPolynomial<C>> factorsSquarefree(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar == 1) {
            return baseFactorsSquarefree(P);
        }
        List<GenPolynomial<C>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.degreeVector().totalDeg() <= 1L) {
            factors.add(P);
            return factors;
        }
        long d = P.degree() + 1L;
        GenPolynomial<C> kr = PolyUfdUtil.substituteKronecker(P, d);
        GenPolynomialRing<C> ufac = kr.ring;
        ufac.setVars(ufac.newVars("zz")); // side effects 

        // factor Kronecker polynomial
        List<GenPolynomial<C>> ulist = new ArrayList<>();
        // kr might not be squarefree so complete factor univariate
        SortedMap<GenPolynomial<C>, Long> slist = baseFactors(kr);
        for (Map.Entry<GenPolynomial<C>, Long> me : slist.entrySet()) {
            GenPolynomial<C> g = me.getKey();
            long e = me.getValue(); // slist.get(g);
            for (int i = 0; i < e; i++) { // is this really required? yes!
                ulist.add(g);
            }
        }
        if (ulist.size() == 1 && ulist.get(0).degree() == P.degree()) {
            factors.add(P);
            return factors;
        }
        //wrong: List<GenPolynomial<C>> klist = PolyUfdUtil.<C> backSubstituteKronecker(pfac, ulist, d);
        // combine trial factors
        int dl = ulist.size() - 1; //(ulist.size() + 1) / 2;
        int ti = 0;
        GenPolynomial<C> u = P;
        long deg = (u.degree() + 1L) / 2L; // max deg
        ExpVector evl = u.leadingExpVector();
        ExpVector evt = u.trailingExpVector();
        for (int j = 1; j <= dl; j++) {
            KsubSet<GenPolynomial<C>> ps = new KsubSet<>(ulist, j);
            for (List<GenPolynomial<C>> flist : ps) {
                GenPolynomial<C> utrial = ufac.getONE();
                for (GenPolynomial<C> aFlist : flist) {
                    utrial = utrial.multiply(aFlist);
                }
                GenPolynomial<C> trial = PolyUfdUtil.backSubstituteKronecker(pfac, utrial, d);
                ti++;
                if (!evl.multipleOf(trial.leadingExpVector())) {
                    continue;
                }
                if (!evt.multipleOf(trial.trailingExpVector())) {
                    continue;
                }
                if (trial.degree() > deg || trial.isConstant()) {
                    continue;
                }
                trial = trial.monic();
                if (ti % 15000 == 0) {
                }
                GenPolynomial<C> rem = PolyUtil.baseSparsePseudoRemainder(u, trial);
                if (rem.isZERO()) {
                    factors.add(trial);
                    u = PolyUtil.basePseudoDivide(u, trial); //u = u.divide( trial );
                    evl = u.leadingExpVector();
                    evt = u.trailingExpVector();
                    if (u.isConstant()) {
                        j = dl + 1;
                        break;
                    }
                    //if (ulist.removeAll(flist)) { // wrong
                    ulist = removeOnce(ulist, flist);
                    dl = (ulist.size() + 1) / 2;
                    j = 0; // since j++
                    break;
                }
            }
        }
        if (!u.isONE() && !u.equals(P)) {
            factors.add(u);
        }
        if (factors.size() == 0) {
            factors.add(P);
        }
        return normalizeFactorization(factors);
    }


    /**
     * Remove one occurrence of elements.
     *
     * @param a list of objects.
     * @param b list of objects.
     * @return remove every element of b from a, but only one occurrence.
     *         <b>Note:</b> not available in java.util.
     */
    static <T> List<T> removeOnce(List<T> a, List<T> b) {
        List<T> res = new ArrayList<>();
        res.addAll(a);
        for (T e : b) {
            res.remove(e);
        }
        return res;
    }


    /**
     * Univariate GenPolynomial factorization ignoring multiplicities.
     *
     * @param P GenPolynomial in one variable.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,k} p_i**{e_i} for some
     *         e_i.
     */
    public List<GenPolynomial<C>> baseFactorsRadical(GenPolynomial<C> P) {
        return new ArrayList<>(baseFactors(P).keySet());
    }


    /**
     * Univariate GenPolynomial factorization.
     *
     * @param P GenPolynomial in one variable.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i**e_i.
     */
    public SortedMap<GenPolynomial<C>, Long> baseFactors(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        SortedMap<GenPolynomial<C>, Long> factors = new TreeMap<>(pfac.getComparator());
        if (P.isZERO()) {
            return factors;
        }
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        if (P.isConstant()) {
            factors.put(P, 1L);
            return factors;
        }
        C c;
        if (pfac.coFac.isField()) { //pfac.characteristic().signum() > 0
            c = P.leadingBaseCoefficient();
        } else {
            c = engine.baseContent(P);
            // move sign to the content
            if (P.signum() < 0 && c.signum() > 0) {
                c = c.negate();
                //P = P.negate();
            }
        }
        if (!c.isONE()) {
            GenPolynomial<C> pc = pfac.getONE().multiply(c);
            factors.put(pc, 1L);
            P = P.divide(c); // make primitive or monic
        }
        SortedMap<GenPolynomial<C>, Long> facs = sengine.baseSquarefreeFactors(P);
        if (facs == null || facs.size() == 0) {
            facs = new TreeMap<>();
            facs.put(P, 1L);
        }
        for (Map.Entry<GenPolynomial<C>, Long> me : facs.entrySet()) {
            GenPolynomial<C> g = me.getKey();
            Long k = me.getValue(); //facs.get(g);
            if (pfac.coFac.isField() && !g.leadingBaseCoefficient().isONE()) {
                g = g.monic(); // how can this happen?
            }
            if (g.degree(0) <= 1) {
                if (!g.isONE()) {
                    factors.put(g, k);
                }
            } else {
                List<GenPolynomial<C>> sfacs = baseFactorsSquarefree(g);
                for (GenPolynomial<C> h : sfacs) {
                    Long j = factors.get(h); // evtl. constants
                    if (j != null) {
                        k += j;
                    }
                    if (!h.isONE()) {
                        factors.put(h, k);
                    }
                }
            }
        }
        return factors;
    }


    /**
     * Univariate GenPolynomial factorization of a squarefree polynomial.
     *
     * @param P squarefree and primitive! GenPolynomial in one variable.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,k} p_i.
     */
    public abstract List<GenPolynomial<C>> baseFactorsSquarefree(GenPolynomial<C> P);


    /**
     * GenPolynomial factorization.
     *
     * @param P GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i**e_i.
     */
    public SortedMap<GenPolynomial<C>, Long> factors(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar == 1) {
            return baseFactors(P);
        }
        SortedMap<GenPolynomial<C>, Long> factors = new TreeMap<>(pfac.getComparator());
        if (P.isZERO()) {
            return factors;
        }
        if (P.isConstant()) {
            factors.put(P, 1L);
            return factors;
        }
        C c;
        if (pfac.coFac.isField()) { // pfac.characteristic().signum() > 0
            c = P.leadingBaseCoefficient();
        } else {
            c = engine.baseContent(P);
            // move sign to the content
            if (P.signum() < 0 && c.signum() > 0) {
                c = c.negate();
                //P = P.negate();
            }
        }
        if (!c.isONE()) {
            GenPolynomial<C> pc = pfac.getONE().multiply(c);
            factors.put(pc, 1L);
            P = P.divide(c); // make base primitive or base monic
        }
        SortedMap<GenPolynomial<C>, Long> facs = sengine.squarefreeFactors(P);
        if (facs == null || facs.size() == 0) {
            facs = new TreeMap<>();
            facs.put(P, 1L);
            throw new RuntimeException("this should not happen, facs is empty: " + facs);
        }
        for (Map.Entry<GenPolynomial<C>, Long> me : facs.entrySet()) {
            GenPolynomial<C> g = me.getKey();
            if (g.isONE()) { // skip 1
                continue;
            }
            Long d = me.getValue(); // facs.get(g);
            List<GenPolynomial<C>> sfacs = factorsSquarefree(g);
            for (GenPolynomial<C> h : sfacs) {
                long dd = d;
                Long j = factors.get(h); // evtl. constants
                if (j != null) {
                    dd += j;
                }
                factors.put(h, dd);
            }
        }
        return factors;
    }


    /**
     * Recursive GenPolynomial factorization of a squarefree polynomial.
     *
     * @param P squarefree recursive GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1, ..., k} p_i.
     */
    public List<GenPolynomial<GenPolynomial<C>>> recursiveFactorsSquarefree(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<GenPolynomial<C>>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        GenPolynomialRing<C> qi = (GenPolynomialRing<C>) pfac.coFac;
        GenPolynomialRing<C> ifac = qi.extend(pfac.getVars());
        GenPolynomial<C> Pi = PolyUtil.distribute(ifac, P);

        C ldcf = Pi.leadingBaseCoefficient();
        if (!ldcf.isONE() && ldcf.isUnit()) {
            Pi = Pi.monic();
        }

        // factor in C[x_1,...,x_n,y_1,...,y_m]
        List<GenPolynomial<C>> ifacts = factorsSquarefree(Pi);
        if (ifacts.size() <= 1) {
            factors.add(P);
            return factors;
        }
        if (!ldcf.isONE() && ldcf.isUnit()) {
            GenPolynomial<C> r = ifacts.get(0);
            ifacts.remove(r);
            r = r.multiply(ldcf);
            ifacts.add(0, r);
        }
        List<GenPolynomial<GenPolynomial<C>>> rfacts = PolyUtil.recursive(pfac, ifacts);
        factors.addAll(rfacts);
        return factors;
    }


    /**
     * Normalize factorization. p'_i &gt; 0 for i &gt; 1 and p'_1 != 1 if k &gt;
     * 1.
     *
     * @param F = [p_1,...,p_k].
     * @return F' = [p'_1,...,p'_k].
     */
    public List<GenPolynomial<C>> normalizeFactorization(List<GenPolynomial<C>> F) {
        if (F == null || F.size() <= 1) {
            return F;
        }
        List<GenPolynomial<C>> Fp = new ArrayList<>(F.size());
        GenPolynomial<C> f0 = F.get(0);
        for (int i = 1; i < F.size(); i++) {
            GenPolynomial<C> fi = F.get(i);
            if (fi.signum() < 0) {
                fi = fi.negate();
                f0 = f0.negate();
            }
            Fp.add(fi);
        }
        if (!f0.isONE()) {
            Fp.add(0, f0);
        }
        return Fp;
    }
}
