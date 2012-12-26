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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.BigInteger;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.Modular;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith.ModularRingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.Power;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.*;


/**
 * Modular coefficients factorization algorithms. This class implements
 * factorization methods for polynomials over (prime) modular integers.
 *
 * @author Heinz Kredel
 */

public class FactorModular<MOD extends GcdRingElem<MOD> & Modular> extends FactorAbsolute<MOD> {


    /**
     * Constructor.
     *
     * @param cfac coefficient ring factory.
     */
    public FactorModular(RingFactory<MOD> cfac) {
        super(cfac);
    }


    /**
     * GenPolynomial base distinct degree factorization.
     *
     * @param P squarefree and monic GenPolynomial.
     * @return [e_1 -&gt; p_1, ..., e_k -&gt; p_k] with P = prod_{i=1,...,k} p_i
     *         and p_i has only irreducible factors of degree e_i.
     */
    public SortedMap<Long, GenPolynomial<MOD>> baseDistinctDegreeFactors(GenPolynomial<MOD> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        SortedMap<Long, GenPolynomial<MOD>> facs = new TreeMap<>();
        if (P.isZERO()) {
            return facs;
        }
        GenPolynomialRing<MOD> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        ModularRingFactory<MOD> mr = (ModularRingFactory<MOD>) pfac.coFac;
        java.math.BigInteger m = mr.getIntegerModul().getVal();
        //if (m.longValue() == 2L) {
        //}
        GenPolynomial<MOD> x = pfac.univariate(0);
        GenPolynomial<MOD> h = x;
        GenPolynomial<MOD> f = P;
        GenPolynomial<MOD> g;
        Power<GenPolynomial<MOD>> pow = new Power<>(pfac);
        long d = 0;
        while (d + 1 <= f.degree(0) / 2) {
            d++;
            h = pow.modPower(h, m, f);
            g = engine.gcd(h.subtract(x), f);
            if (!g.isONE()) {
                facs.put(d, g);
                f = f.divide(g);
            }
        }
        if (!f.isONE()) {
            d = f.degree(0);
            facs.put(d, f);
        }
        return facs;
    }


    /**
     * GenPolynomial base equal degree factorization.
     *
     * @param P   squarefree and monic GenPolynomial.
     * @param deg such that P has only irreducible factors of degree deg.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,r} p_i.
     */
    public List<GenPolynomial<MOD>> baseEqualDegreeFactors(GenPolynomial<MOD> P, long deg) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P != null");
        }
        List<GenPolynomial<MOD>> facs = new ArrayList<>();
        if (P.isZERO()) {
            return facs;
        }
        GenPolynomialRing<MOD> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        if (P.degree(0) == deg) {
            facs.add(P);
            return facs;
        }
        ModularRingFactory<MOD> mr = (ModularRingFactory<MOD>) pfac.coFac;
        java.math.BigInteger m = mr.getIntegerModul().getVal();
        boolean p2 = false;
        if (m.equals(java.math.BigInteger.valueOf(2L))) {
            p2 = true;
            //throw new RuntimeException(this.getClass().getName() + " case p = 2 not implemented");
        }
        GenPolynomial<MOD> one = pfac.getONE();
        GenPolynomial<MOD> t = pfac.univariate(0, 1L);
        GenPolynomial<MOD> r;
        GenPolynomial<MOD> h;
        GenPolynomial<MOD> f = P;
        //GreatestCommonDivisor<MOD> engine = GCDFactory.<MOD> getImplementation(pfac.coFac);
        Power<GenPolynomial<MOD>> pow = new Power<>(pfac);
        GenPolynomial<MOD> g;
        int degi = (int) deg; //f.degree(0);
        BigInteger di = Power.positivePower(new BigInteger(m), deg);
        java.math.BigInteger d = di.getVal(); //.longValue()-1;
        d = d.shiftRight(1); // divide by 2
        do {
            if (p2) {
                h = t;
                for (int i = 1; i < degi; i++) {
                    h = t.sum(h.multiply(h));
                    h = h.remainder(f);
                }
                t = t.multiply(pfac.univariate(0, 2L));
            } else {
                r = pfac.random(17, degi, 2 * degi, 1.0f);
                if (r.degree(0) >= f.degree(0)) {
                    r = r.remainder(f);
                }
                r = r.monic();
                h = pow.modPower(r, d, f).subtract(one);
                degi++;
            }
            g = engine.gcd(h, f);
        } while (g.degree(0) == 0 || g.degree(0) == f.degree(0));
        f = f.divide(g);
        facs.addAll(baseEqualDegreeFactors(f, deg));
        facs.addAll(baseEqualDegreeFactors(g, deg));
        return facs;
    }


    /**
     * GenPolynomial base factorization of a squarefree polynomial.
     *
     * @param P squarefree and monic! GenPolynomial.
     * @return [p_1, ..., p_k] with P = prod_{i=1,...,r} p_i.
     */
    @Override
    public List<GenPolynomial<MOD>> baseFactorsSquarefree(GenPolynomial<MOD> P) {
        if (P == null) {
            throw new IllegalArgumentException(this.getClass().getName() + " P == null");
        }
        List<GenPolynomial<MOD>> factors = new ArrayList<>();
        if (P.isZERO()) {
            return factors;
        }
        if (P.isONE()) {
            factors.add(P);
            return factors;
        }
        GenPolynomialRing<MOD> pfac = P.ring;
        if (pfac.nvar > 1) {
            throw new IllegalArgumentException(this.getClass().getName() + " only for univariate polynomials");
        }
        if (!P.leadingBaseCoefficient().isONE()) {
            throw new IllegalArgumentException("ldcf(P) != 1: " + P);
        }
        SortedMap<Long, GenPolynomial<MOD>> dfacs = baseDistinctDegreeFactors(P);
        for (Map.Entry<Long, GenPolynomial<MOD>> me : dfacs.entrySet()) {
            Long e = me.getKey();
            GenPolynomial<MOD> f = me.getValue(); // dfacs.get(e);
            List<GenPolynomial<MOD>> efacs = baseEqualDegreeFactors(f, e);
            factors.addAll(efacs);
        }
        factors = PolyUtil.monic(factors);
        SortedSet<GenPolynomial<MOD>> ss = new TreeSet<>(factors);
        factors.clear();
        factors.addAll(ss);
        return factors;
    }

}
