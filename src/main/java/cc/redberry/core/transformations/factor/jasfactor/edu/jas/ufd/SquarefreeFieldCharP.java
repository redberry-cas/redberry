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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.*;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Squarefree decomposition for coefficient fields of characteristic p.
 *
 * @author Heinz Kredel
 */

public abstract class SquarefreeFieldCharP<C extends GcdRingElem<C>> extends SquarefreeAbstract<C> {


    /*
     * Squarefree engine for characteristic p base coefficients.
     */
    //protected final SquarefreeAbstract<C> rengine;


    /**
     * Factory for finite field of characteristic p coefficients.
     */
    protected final RingFactory<C> coFac;


    /**
     * Factory for a algebraic extension of a finite field of characteristic p
     * coefficients. If <code>coFac</code> is an algebraic extension, then
     * <code>aCoFac</code> is equal to <code>coFac</code>, else
     * <code>aCoFac</code> is <code>null</code>.
     */
    protected final AlgebraicNumberRing<C> aCoFac;


    /**
     * Factory for a transcendental extension of a finite field of
     * characteristic p coefficients. If <code>coFac</code> is an transcendental
     * extension, then <code>qCoFac</code> is equal to <code>coFac</code>, else
     * <code>qCoFac</code> is <code>null</code>.
     */
    protected final QuotientRing<C> qCoFac;


    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    public SquarefreeFieldCharP(RingFactory<C> fac) {
        super(GCDFactory.<C>getProxy(fac));
        if (!fac.isField()) {
            //throw new IllegalArgumentException("fac must be a field");
        }
        if (fac.characteristic().signum() == 0) {
            throw new IllegalArgumentException("characterisic(fac) must be non-zero");
        }
        coFac = fac;
        Object oFac = coFac;
        if (oFac instanceof AlgebraicNumberRing) {
            aCoFac = (AlgebraicNumberRing<C>) oFac; // <C> is not correct
            //rengine = (SquarefreeAbstract) SquarefreeFactory.getImplementation(aCoFac.ring);
            qCoFac = null;
        } else {
            aCoFac = null;
            if (oFac instanceof QuotientRing) {
                qCoFac = (QuotientRing<C>) oFac; // <C> is not correct
                //rengine = (SquarefreeAbstract) SquarefreeFactory.getImplementation(qCoFac.ring);
            } else {
                qCoFac = null;
                //rengine = null; //(SquarefreeAbstract) SquarefreeFactory.getImplementation(oFac);
            }
        }
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getName() + " with " + engine + " over " + coFac;
    }


    /**
     * GenPolynomial polynomial greatest squarefree divisor.
     *
     * @param P GenPolynomial.
     * @return squarefree(pp(P)).
     */
    @Override
    public GenPolynomial<C> baseSquarefreePart(GenPolynomial<C> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        // just for the moment:
        GenPolynomial<C> s = pfac.getONE();
        SortedMap<GenPolynomial<C>, Long> factors = baseSquarefreeFactors(P);
        for (GenPolynomial<C> sp : factors.keySet()) {
            s = s.multiply(sp);
        }
        return s.monic();
    }


    /**
     * GenPolynomial polynomial squarefree factorization.
     *
     * @param A GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    @Override
    public SortedMap<GenPolynomial<C>, Long> baseSquarefreeFactors(GenPolynomial<C> A) {
        SortedMap<GenPolynomial<C>, Long> sfactors = new TreeMap<>();
        if (A == null || A.isZERO()) {
            return sfactors;
        }
        GenPolynomialRing<C> pfac = A.ring;
        if (A.isConstant()) {
            C coeff = A.leadingBaseCoefficient();
            SortedMap<C, Long> rfactors = squarefreeFactors(coeff);
            if (rfactors != null && rfactors.size() > 0) {
                for (Map.Entry<C, Long> me : rfactors.entrySet()) {
                    C c = me.getKey();
                    if (!c.isONE()) {
                        GenPolynomial<C> cr = pfac.getONE().multiply(c);
                        Long rk = me.getValue(); // rfactors.get(c);
                        sfactors.put(cr, rk);
                    }
                }
            } else {
                sfactors.put(A, 1L);
            }
            return sfactors;
        }
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        C ldbcf = A.leadingBaseCoefficient();
        if (!ldbcf.isONE()) {
            A = A.divide(ldbcf);
            SortedMap<C, Long> rfactors = squarefreeFactors(ldbcf);
            if (rfactors != null && rfactors.size() > 0) {
                for (Map.Entry<C, Long> me : rfactors.entrySet()) {
                    C c = me.getKey();
                    if (!c.isONE()) {
                        GenPolynomial<C> cr = pfac.getONE().multiply(c);
                        Long rk = me.getValue(); //rfactors.get(c);
                        sfactors.put(cr, rk);
                    }
                }
            } else {
                GenPolynomial<C> f1 = pfac.getONE().multiply(ldbcf);
                sfactors.put(f1, 1L);
            }
            ldbcf = pfac.coFac.getONE();
        }
        GenPolynomial<C> T0 = A;
        long e = 1L;
        GenPolynomial<C> Tp;
        GenPolynomial<C> T = null;
        GenPolynomial<C> V = null;
        long k = 0L;
        long mp = 0L;
        boolean init = true;
        while (true) {
            if (init) {
                if (T0.isConstant() || T0.isZERO()) {
                    break;
                }
                Tp = PolyUtil.baseDeriviative(T0);
                T = engine.baseGcd(T0, Tp);
                T = T.monic();
                V = PolyUtil.basePseudoDivide(T0, T);
                k = 0L;
                mp = 0L;
                init = false;
            }
            if (V.isConstant()) {
                mp = pfac.characteristic().longValue(); // assert != 0
                //T0 = PolyUtil.<C> baseModRoot(T,mp);
                T0 = baseRootCharacteristic(T);
                if (T0 == null) {
                    //break;
                    T0 = pfac.getZERO();
                }
                e = e * mp;
                init = true;
                continue;
            }
            k++;
            if (mp != 0L && k % mp == 0L) {
                T = PolyUtil.basePseudoDivide(T, V);
                k++;
            }
            GenPolynomial<C> W = engine.baseGcd(T, V);
            W = W.monic();
            GenPolynomial<C> z = PolyUtil.basePseudoDivide(V, W);
            V = W;
            T = PolyUtil.basePseudoDivide(T, V);
            if (z.degree(0) > 0) {
                if (ldbcf.isONE() && !z.leadingBaseCoefficient().isONE()) {
                    z = z.monic();
                }
                sfactors.put(z, (e * k));
            }
        }
        //      look, a stupid error:
        //         if ( !ldbcf.isONE() ) {
        //             GenPolynomial<C> f1 = sfactors.firstKey();
        //             long e1 = sfactors.remove(f1);
        //             f1 = f1.multiply(c);
        //             sfactors.put(f1,e1);
        //         }
        return sfactors;
    }


    /**
     * GenPolynomial recursive univariate polynomial greatest squarefree
     * divisor.
     *
     * @param P recursive univariate GenPolynomial.
     * @return squarefree(pp(P)).
     */
    @Override
    public GenPolynomial<GenPolynomial<C>> recursiveUnivariateSquarefreePart(GenPolynomial<GenPolynomial<C>> P) {
        if (P == null || P.isZERO()) {
            return P;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName()
                    + " only for multivariate polynomials");
        }
        // just for the moment:
        GenPolynomial<GenPolynomial<C>> s = pfac.getONE();

        SortedMap<GenPolynomial<GenPolynomial<C>>, Long> factors = recursiveUnivariateSquarefreeFactors(P);
        for (GenPolynomial<GenPolynomial<C>> sp : factors.keySet()) {
            s = s.multiply(sp);
        }
        return PolyUtil.monic(s);
    }


    /**
     * GenPolynomial recursive univariate polynomial squarefree factorization.
     *
     * @param P recursive univariate GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    @Override
    public SortedMap<GenPolynomial<GenPolynomial<C>>, Long> recursiveUnivariateSquarefreeFactors(
            GenPolynomial<GenPolynomial<C>> P) {
        SortedMap<GenPolynomial<GenPolynomial<C>>, Long> sfactors = new TreeMap<>();
        if (P == null || P.isZERO()) {
            return sfactors;
        }
        GenPolynomialRing<GenPolynomial<C>> pfac = P.ring;
        if (pfac.nvar > 1) {
            // recursiveContent not possible by return type
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        // if base coefficient ring is a field, make monic
        GenPolynomialRing<C> cfac = (GenPolynomialRing<C>) pfac.coFac;
        C ldbcf = P.leadingBaseCoefficient().leadingBaseCoefficient();
        if (!ldbcf.isONE()) {
            GenPolynomial<C> lc = cfac.getONE().multiply(ldbcf);
            GenPolynomial<GenPolynomial<C>> pl = pfac.getONE().multiply(lc);
            sfactors.put(pl, 1L);
            C li = ldbcf.inverse();
            P = P.multiply(cfac.getONE().multiply(li));
        }
        // factors of content
        GenPolynomial<C> Pc = engine.recursiveContent(P);
        Pc = Pc.monic();
        if (!Pc.isONE()) {
            P = PolyUtil.coefficientPseudoDivide(P, Pc);
        }
        SortedMap<GenPolynomial<C>, Long> rsf = squarefreeFactors(Pc);
        // add factors of content
        for (Map.Entry<GenPolynomial<C>, Long> me : rsf.entrySet()) {
            GenPolynomial<C> c = me.getKey();
            if (!c.isONE()) {
                GenPolynomial<GenPolynomial<C>> cr = pfac.getONE().multiply(c);
                Long rk = me.getValue(); //rsf.get(c);
                sfactors.put(cr, rk);
            }
        }

        // factors of recursive polynomial
        GenPolynomial<GenPolynomial<C>> T0 = P;
        long e = 1L;
        GenPolynomial<GenPolynomial<C>> Tp;
        GenPolynomial<GenPolynomial<C>> T = null;
        GenPolynomial<GenPolynomial<C>> V = null;
        long k = 0L;
        long mp = 0L;
        boolean init = true;
        while (true) {
            if (init) {
                if (T0.isConstant() || T0.isZERO()) {
                    break;
                }
                Tp = PolyUtil.recursiveDeriviative(T0);
                T = engine.recursiveUnivariateGcd(T0, Tp);
                T = PolyUtil.monic(T);
                V = PolyUtil.recursivePseudoDivide(T0, T);
                k = 0L;
                mp = 0L;
                init = false;
            }
            if (V.isConstant()) {
                mp = pfac.characteristic().longValue(); // assert != 0
                //T0 = PolyUtil.<C> recursiveModRoot(T,mp);
                T0 = recursiveUnivariateRootCharacteristic(T);
                if (T0 == null) {
                    //break;
                    T0 = pfac.getZERO();
                }
                e = e * mp;
                init = true;
                //continue;
            }
            k++;
            if (mp != 0L && k % mp == 0L) {
                T = PolyUtil.recursivePseudoDivide(T, V);
                k++;
            }
            GenPolynomial<GenPolynomial<C>> W = engine.recursiveUnivariateGcd(T, V);
            W = PolyUtil.monic(W);
            GenPolynomial<GenPolynomial<C>> z = PolyUtil.recursivePseudoDivide(V, W);
            V = W;
            T = PolyUtil.recursivePseudoDivide(T, V);
            //was: if ( z.degree(0) > 0 ) {
            if (!z.isONE() && !z.isZERO()) {
                z = PolyUtil.monic(z);
                sfactors.put(z, (e * k));
            }
        }
        if (sfactors.size() == 0) {
            sfactors.put(pfac.getONE(), 1L);
        }
        return sfactors;
    }


    /**
     * GenPolynomial greatest squarefree divisor.
     *
     * @param P GenPolynomial.
     * @return squarefree(pp(P)).
     */
    @Override
    public GenPolynomial<C> squarefreePart(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        if (P.isZERO()) {
            return P;
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return baseSquarefreePart(P);
        }
        // just for the moment:
        GenPolynomial<C> s = pfac.getONE();
        SortedMap<GenPolynomial<C>, Long> factors = squarefreeFactors(P);
        for (GenPolynomial<C> sp : factors.keySet()) {
            if (sp.isConstant()) {
                continue;
            }
            s = s.multiply(sp);
        }
        return s.monic();
    }


    /**
     * GenPolynomial squarefree factorization.
     *
     * @param P GenPolynomial.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    @Override
    public SortedMap<GenPolynomial<C>, Long> squarefreeFactors(GenPolynomial<C> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        GenPolynomialRing<C> pfac = P.ring;
        if (pfac.nvar <= 1) {
            return baseSquarefreeFactors(P);
        }
        SortedMap<GenPolynomial<C>, Long> sfactors = new TreeMap<>();
        if (P.isZERO()) {
            return sfactors;
        }
        GenPolynomialRing<C> cfac = pfac.contract(1);
        GenPolynomialRing<GenPolynomial<C>> rfac = new GenPolynomialRing<>(cfac, 1);

        GenPolynomial<GenPolynomial<C>> Pr = PolyUtil.recursive(rfac, P);
        SortedMap<GenPolynomial<GenPolynomial<C>>, Long> PP = recursiveUnivariateSquarefreeFactors(Pr);

        for (Map.Entry<GenPolynomial<GenPolynomial<C>>, Long> m : PP.entrySet()) {
            Long i = m.getValue();
            GenPolynomial<GenPolynomial<C>> Dr = m.getKey();
            GenPolynomial<C> D = PolyUtil.distribute(pfac, Dr);
            sfactors.put(D, i);
        }
        return sfactors;
    }


    /**
     * Coefficient squarefree factorization.
     *
     * @param coeff coefficient.
     * @return [p_1 -&gt; e_1, ..., p_k -&gt; e_k] with P = prod_{i=1,...,k}
     *         p_i^{e_i} and p_i squarefree.
     */
    @Override
    public SortedMap<C, Long> squarefreeFactors(C coeff) {
        if (coeff == null) {
            return null;
        }
        SortedMap<C, Long> factors = new TreeMap<>();
        RingFactory<C> cfac = (RingFactory<C>) coeff.factory();
        if (aCoFac != null) {
            AlgebraicNumber<C> an = (AlgebraicNumber<C>) coeff;
            if (cfac.isFinite()) {
                SquarefreeFiniteFieldCharP<C> reng = (SquarefreeFiniteFieldCharP) SquarefreeFactory
                        .getImplementation(cfac);
                SortedMap<C, Long> rfactors = reng.rootCharacteristic(coeff); // ??
                factors.putAll(rfactors);
                //return factors;
            } else {
                SquarefreeInfiniteAlgebraicFieldCharP<C> reng = (SquarefreeInfiniteAlgebraicFieldCharP) SquarefreeFactory
                        .getImplementation(cfac);
                SortedMap<AlgebraicNumber<C>, Long> rfactors = reng.squarefreeFactors(an);
                for (Map.Entry<AlgebraicNumber<C>, Long> me : rfactors.entrySet()) {
                    AlgebraicNumber<C> c = me.getKey();
                    if (!c.isONE()) {
                        C cr = (C) c;
                        Long rk = me.getValue(); // rfactors.get(c);
                        factors.put(cr, rk);
                    }
                }
            }
        } else if (qCoFac != null) {
            Quotient<C> q = (Quotient<C>) coeff;
            SquarefreeInfiniteFieldCharP<C> reng = (SquarefreeInfiniteFieldCharP) SquarefreeFactory
                    .getImplementation(cfac);
            SortedMap<Quotient<C>, Long> rfactors = reng.squarefreeFactors(q);
            for (Map.Entry<Quotient<C>, Long> me : rfactors.entrySet()) {
                Quotient<C> c = me.getKey();
                if (!c.isONE()) {
                    C cr = (C) c;
                    Long rk = me.getValue(); //rfactors.get(c);
                    factors.put(cr, rk);
                }
            }
        } else if (cfac.isFinite()) {
            SquarefreeFiniteFieldCharP<C> reng = (SquarefreeFiniteFieldCharP) SquarefreeFactory
                    .getImplementation(cfac);
            SortedMap<C, Long> rfactors = reng.rootCharacteristic(coeff); // ??
            factors.putAll(rfactors);
            //return factors;
        } else {
        }
        return factors;
    }


    /* --------- char-th roots --------------------- */


    /**
     * GenPolynomial char-th root univariate polynomial.
     *
     * @param P GenPolynomial.
     * @return char-th_rootOf(P), or null if no char-th root.
     */
    public abstract GenPolynomial<C> baseRootCharacteristic(GenPolynomial<C> P);


    /**
     * GenPolynomial char-th root univariate polynomial with polynomial
     * coefficients.
     *
     * @param P recursive univariate GenPolynomial.
     * @return char-th_rootOf(P), or null if P is no char-th root.
     */
    public abstract GenPolynomial<GenPolynomial<C>> recursiveUnivariateRootCharacteristic(
            GenPolynomial<GenPolynomial<C>> P);

}
