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
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Abstract squarefree decomposition class.
 *
 * @author Heinz Kredel
 */

public abstract class SquarefreeAbstract<C extends GcdRingElem<C>> implements Squarefree<C> {


    /**
     * GCD engine for respective base coefficients.
     */
    protected final GreatestCommonDivisorAbstract<C> engine;


    /**
     * Constructor.
     */
    public SquarefreeAbstract(GreatestCommonDivisorAbstract<C> engine) {
        this.engine = engine;
    }


    /**
     * GenPolynomial polynomial greatest squarefree divisor.
     *
     * @param P GenPolynomial.
     * @return squarefree(pp(P)).
     */
    public abstract GenPolynomial<C> baseSquarefreePart(GenPolynomial<C> P);


    /**
     * GenPolynomial polynomial squarefree factorization.
     *
     * @param A GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    public abstract SortedMap<GenPolynomial<C>, Long> baseSquarefreeFactors(GenPolynomial<C> A);


    /**
     * GenPolynomial recursive polynomial greatest squarefree divisor.
     *
     * @param P recursive univariate GenPolynomial.
     * @return squarefree(pp(P)).
     */
    public abstract GenPolynomial<GenPolynomial<C>> recursiveUnivariateSquarefreePart(
            GenPolynomial<GenPolynomial<C>> P);


    /**
     * GenPolynomial recursive univariate polynomial squarefree factorization.
     *
     * @param P recursive univariate GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    public abstract SortedMap<GenPolynomial<GenPolynomial<C>>, Long> recursiveUnivariateSquarefreeFactors(
            GenPolynomial<GenPolynomial<C>> P);


    /**
     * GenPolynomial greatest squarefree divisor.
     *
     * @param P GenPolynomial.
     * @return squarefree(P) a primitive respectively monic polynomial.
     */
    public abstract GenPolynomial<C> squarefreePart(GenPolynomial<C> P);


    /**
     * GenPolynomial test if is squarefree.
     *
     * @param P GenPolynomial.
     * @return true if P is squarefree, else false.
     */
    public boolean isSquarefree(GenPolynomial<C> P) {
        GenPolynomial<C> S = squarefreePart(P);
        GenPolynomial<C> Ps = P;
        if (P.ring.coFac.isField()) {
            Ps = Ps.monic();
        } else {
            Ps = engine.basePrimitivePart(Ps);
        }
        boolean f = Ps.equals(S);
        //if (!f) {
        //}
        return f;
    }


    /**
     * GenPolynomial squarefree factorization.
     *
     * @param P GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    public abstract SortedMap<GenPolynomial<C>, Long> squarefreeFactors(GenPolynomial<C> P);


    /**
     * Normalize factorization. p'_i &gt; 0 for i &gt; 1 and p'_1 != 1 if k &gt;
     * 1.
     *
     * @param F = [p_1-&gt;e_1;, ..., p_k-&gt;e_k].
     * @return F' = [p'_1-&gt;e_1, ..., p'_k-&gt;e_k].
     */
    public SortedMap<GenPolynomial<C>, Long> normalizeFactorization(SortedMap<GenPolynomial<C>, Long> F) {
        if (F == null || F.size() <= 1) {
            return F;
        }
        List<GenPolynomial<C>> Fp = new ArrayList<>(F.keySet());
        GenPolynomial<C> f0 = Fp.get(0);
        if (f0.ring.characteristic().signum() != 0) { // only ordered coefficients
            return F;
        }
        long e0 = F.get(f0);
        SortedMap<GenPolynomial<C>, Long> Sp = new TreeMap<>();
        for (int i = 1; i < Fp.size(); i++) {
            GenPolynomial<C> fi = Fp.get(i);
            long ei = F.get(fi);
            if (fi.signum() < 0) {
                fi = fi.negate();
                if (ei % 2 != 0) { // && e0 % 2 != 0
                    f0 = f0.negate();
                }
            }
            Sp.put(fi, ei);
        }
        if (!f0.isONE()) {
            Sp.put(f0, e0);
        }
        return Sp;
    }


    /**
     * GenPolynomial is (squarefree) factorization.
     *
     * @param P GenPolynomial.
     * @param F = [p_1,...,p_k].
     * @return true if P = prod_{i=1,...,r} p_i, else false.
     */
    public boolean isFactorization(GenPolynomial<C> P, List<GenPolynomial<C>> F) {
        if (P == null || F == null) {
            throw new IllegalArgumentException("P and F may not be null");
        }
        GenPolynomial<C> t = P.ring.getONE();
        for (GenPolynomial<C> f : F) {
            t = t.multiply(f);
        }
        boolean f = P.equals(t) || P.equals(t.negate());
        if (!f) {
        }
        return f;
    }

    /**
     * Coefficients squarefree factorization.
     *
     * @param P coefficient.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    public abstract SortedMap<C, Long> squarefreeFactors(C P);
    /* not possible:
    {
        if (P == null) {
            return null;
        }
        SortedMap<C, Long> factors = new TreeMap<C, Long>();
        SquarefreeAbstract<C> reng = SquarefreeFactory.getImplementation((RingFactory<C>) P.factory());
            SortedMap<C, Long> rfactors = reng.squarefreeFactors(P);
            for (C c : rfactors.keySet()) {
                if (!c.isONE()) {
                    C cr = (C) (Object) c;
                    Long rk = rfactors.get(c);
                    factors.put(cr, rk);
                }
            }

        return factors;
    }
    */

}
