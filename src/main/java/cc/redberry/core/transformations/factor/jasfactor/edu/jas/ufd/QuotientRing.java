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


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomial;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.GenPolynomialRing;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly.PolyUtil;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.GcdRingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Quotient ring factory based on GenPolynomial with RingElem interface. Objects
 * of this class are immutable.
 *
 * @author Heinz Kredel
 */
public class QuotientRing<C extends GcdRingElem<C>> implements RingFactory<Quotient<C>> {


    //private boolean debug = false;


    /**
     * Polynomial ring of the factory.
     */
    public final GenPolynomialRing<C> ring;


    /**
     * GCD engine of the factory.
     */
    public final GreatestCommonDivisor<C> engine;


    /**
     * Use GCD of package cc.redberry.core.transformations.factor.jasfactor.edu.jas.ufd.
     */
    public final boolean ufdGCD;


    /**
     * The constructor creates a QuotientRing object from a GenPolynomialRing.
     *
     * @param r      polynomial ring.
     * @param ufdGCD flag, if syzygy or gcd based algorithm used for engine.
     */
    public QuotientRing(GenPolynomialRing<C> r, boolean ufdGCD) {
        ring = r;
        this.ufdGCD = ufdGCD;
        //         if (!ufdGCD) {
        //             engine = null;
        //             return;
        //         }
        engine = GCDFactory.getProxy(ring.coFac);
    }


    /**
     * Divide.
     *
     * @param n first polynomial.
     * @param d second polynomial.
     * @return divide(n, d)
     */
    protected GenPolynomial<C> divide(GenPolynomial<C> n, GenPolynomial<C> d) {
        return PolyUtil.basePseudoDivide(n, d);
    }


    /**
     * Greatest common divisor.
     *
     * @param n first polynomial.
     * @param d second polynomial.
     * @return gcd(n, d)
     */
    protected GenPolynomial<C> gcd(GenPolynomial<C> n, GenPolynomial<C> d) {
        if (ufdGCD) {
            return engine.gcd(n, d);
        }
        return engine.gcd(n, d);
        //return syzGcd(n, d);
    }


    /*
     * Least common multiple. Just for fun, is not efficient.
     * @param n first polynomial.
     * @param d second polynomial.
     * @return lcm(n,d)
     */
    //     protected GenPolynomial<C> syzLcm(GenPolynomial<C> n, GenPolynomial<C> d) {
    //         List<GenPolynomial<C>> list = new ArrayList<GenPolynomial<C>>(1);
    //         list.add(n);
    //         Ideal<C> N = new Ideal<C>(n.ring, list, true);
    //         list = new ArrayList<GenPolynomial<C>>(1);
    //         list.add(d);
    //         Ideal<C> D = new Ideal<C>(n.ring, list, true);
    //         Ideal<C> L = N.intersect(D);
    //         if (L.getList().size() != 1) {
    //             throw new RuntimeException("lcm not uniqe");
    //         }
    //         GenPolynomial<C> lcm = L.getList().get(0);
    //         return lcm;
    //     }


    /*
     * Greatest common divisor. Just for fun, is not efficient.
     * @param n first polynomial.
     * @param d second polynomial.
     * @return gcd(n,d)
     */
    //     protected GenPolynomial<C> syzGcd(GenPolynomial<C> n, GenPolynomial<C> d) {
    //         if (n.isZERO()) {
    //             return d;
    //         }
    //         if (d.isZERO()) {
    //             return n;
    //         }
    //         if (n.isONE()) {
    //             return n;
    //         }
    //         if (d.isONE()) {
    //             return d;
    //         }
    //         GenPolynomial<C> p = n.multiply(d);
    //         GenPolynomial<C> lcm = syzLcm(n, d);
    //         GenPolynomial<C> gcd = divide(p, lcm);
    //         return gcd;
    //     }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return false;
    }


    /**
     * Copy Quotient element c.
     *
     * @param c
     * @return a copy of c.
     */
    public Quotient<C> copy(Quotient<C> c) {
        return new Quotient<>(c.ring, c.num, c.den, true);
    }


    /**
     * Get the zero element.
     *
     * @return 0 as Quotient.
     */
    public Quotient<C> getZERO() {
        return new Quotient<>(this, ring.getZERO());
    }


    /**
     * Get the one element.
     *
     * @return 1 as Quotient.
     */
    public Quotient<C> getONE() {
        return new Quotient<>(this, ring.getONE());
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<Quotient<C>> generators() {
        List<GenPolynomial<C>> pgens = ring.generators();
        List<Quotient<C>> gens = new ArrayList<>(pgens.size());
        for (GenPolynomial<C> p : pgens) {
            Quotient<C> q = new Quotient<>(this, p);
            gens.add(q);
        }
        return gens;
    }


    /**
     * Query if this ring is commutative.
     *
     * @return true if this ring is commutative, else false.
     */
    public boolean isCommutative() {
        return ring.isCommutative();
    }


    /**
     * Query if this ring is associative.
     *
     * @return true if this ring is associative, else false.
     */
    public boolean isAssociative() {
        return ring.isAssociative();
    }


    /**
     * Query if this ring is a field.
     *
     * @return true.
     */
    public boolean isField() {
        return true;
    }


    /**
     * Characteristic of this ring.
     *
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return ring.characteristic();
    }


    /**
     * Get a Quotient element from a BigInteger value.
     *
     * @param a BigInteger.
     * @return a Quotient.
     */
    public Quotient<C> fromInteger(java.math.BigInteger a) {
        return new Quotient<>(this, ring.fromInteger(a));
    }


    /**
     * Get a Quotient element from a long value.
     *
     * @param a long.
     * @return a Quotient.
     */
    public Quotient<C> fromInteger(long a) {
        return new Quotient<>(this, ring.fromInteger(a));
    }


    /**
     * Get the String representation as RingFactory.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = null;
        if (ring.coFac.characteristic().signum() == 0) {
            s = "RatFunc";
        } else {
            s = "ModFunc";
        }
        return s + "( " + ring.toString() + " )";
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    // not jet working
    public boolean equals(Object b) {
        if (!(b instanceof QuotientRing)) {
            return false;
        }
        QuotientRing<C> a = null;
        try {
            a = (QuotientRing<C>) b;
        } catch (ClassCastException e) {
        }
        if (a == null) {
            return false;
        }
        return ring.equals(a.ring);
    }


    /**
     * Hash code for this quotient ring.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int h;
        h = ring.hashCode();
        return h;
    }


    /**
     * Quotient random.
     *
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random residue element.
     */
    public Quotient<C> random(int n) {
        GenPolynomial<C> r = ring.random(n).monic();
        GenPolynomial<C> s = ring.random(n).monic();
        while (s.isZERO()) {
            s = ring.random(n).monic();
        }
        return new Quotient<>(this, r, s, false);
    }


    /**
     * Generate a random residum polynomial.
     *
     * @param k bitsize of random coefficients.
     * @param l number of terms.
     * @param d maximal degree in each variable.
     * @param q density of nozero exponents.
     * @return a random residue polynomial.
     */
    public Quotient<C> random(int k, int l, int d, float q) {
        GenPolynomial<C> r = ring.random(k, l, d, q).monic();
        GenPolynomial<C> s = ring.random(k, l, d, q).monic();
        while (s.isZERO()) {
            s = ring.random(k, l, d, q).monic();
        }
        return new Quotient<>(this, r, s, false);
    }


    /**
     * Quotient random.
     *
     * @param n   such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random residue element.
     */
    public Quotient<C> random(int n, Random rnd) {
        GenPolynomial<C> r = ring.random(n, rnd).monic();
        GenPolynomial<C> s = ring.random(n, rnd).monic();
        while (s.isZERO()) {
            s = ring.random(n, rnd).monic();
        }
        return new Quotient<>(this, r, s, false);
    }

}
