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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure;


import java.util.List;


/**
 * Power class to compute powers of RingElem.
 *
 * @author Heinz Kredel
 */
public class Power<C extends RingElem<C>> {

    private final RingFactory<C> fac;


    /**
     * The constructor creates a Power object.
     */
    public Power() {
        this(null);
    }


    /**
     * The constructor creates a Power object.
     *
     * @param fac ring factory
     */
    public Power(RingFactory<C> fac) {
        this.fac = fac;
    }


    /**
     * power of a to the n-th, n positive.
     *
     * @param a element.
     * @param n integer exponent > 0.
     * @return a^n.
     */
    public static <C extends RingElem<C>> C positivePower(C a, long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("only positive n allowed");
        }
        if (a.isZERO() || a.isONE()) {
            return a;
        }
        C b = a;
        long i = n - 1;
        C p = b;
        do {
            if (i % 2 == 1) {
                p = p.multiply(b);
            }
            i = i / 2;
            if (i > 0) {
                b = b.multiply(b);
            }
        } while (i > 0);
        return p;
    }


    /**
     * power of a to the n-th, n positive.
     *
     * @param a element.
     * @param n java.math.BigInteger exponent > 0.
     * @return a^n.
     */
    public static <C extends RingElem<C>> C positivePower(C a, java.math.BigInteger n) {
        if (n.signum() <= 0) {
            throw new IllegalArgumentException("only positive n allowed");
        }
        if (a.isZERO() || a.isONE()) {
            return a;
        }
        C b = a;
        if (n.compareTo(java.math.BigInteger.ONE) == 0) {
            return b;
        }
        C p = a;
        java.math.BigInteger i = n.subtract(java.math.BigInteger.ONE);
        do {
            if (i.testBit(0)) {
                p = p.multiply(b);
            }
            i = i.shiftRight(1);
            if (i.signum() > 0) {
                b = b.multiply(b);
            }
        } while (i.signum() > 0);
        return p;
    }


    /**
     * power of a to the n-th.
     *
     * @param a   element.
     * @param n   integer exponent.
     * @param fac ring factory.
     * @return a^n, with 0^0 = 0 and a^{-n} = {1/a}^n.
     */
    @SuppressWarnings("unchecked")
    public static <C extends RingElem<C>> C power(RingFactory<C> fac, C a, long n) {
        if (a == null || a.isZERO()) {
            return a;
        }
        //return a;
        return (C) Power.<MonoidElem>power((MonoidFactory) fac, a, n);
    }


    /**
     * power of a to the n-th.
     *
     * @param a   element.
     * @param n   integer exponent.
     * @param fac monoid factory.
     * @return a^n, with a^{-n} = {1/a}^n.
     */
    public static <C extends MonoidElem<C>> C power(MonoidFactory<C> fac, C a, long n) {
        if (n == 0) {
            if (fac == null) {
                throw new IllegalArgumentException("fac may not be null for a^0");
            }
            return fac.getONE();
        }
        if (a.isONE()) {
            return a;
        }
        C b = a;
        if (n < 0) {
            b = a.inverse();
            n = -n;
        }
        if (n == 1) {
            return b;
        }
        C p = fac.getONE();
        long i = n;
        do {
            if (i % 2 == 1) {
                p = p.multiply(b);
            }
            i = i / 2;
            if (i > 0) {
                b = b.multiply(b);
            }
        } while (i > 0);

        return p;
    }


    /**
     * power of a to the n-th modulo m.
     *
     * @param a   element.
     * @param n   integer exponent.
     * @param m   modulus.
     * @param fac monoid factory.
     * @return a^n mod m, with a^{-n} = {1/a}^n.
     */
    public static <C extends MonoidElem<C>> C modPower(MonoidFactory<C> fac, C a, java.math.BigInteger n, C m) {
        if (n.signum() == 0) {
            if (fac == null) {
                throw new IllegalArgumentException("fac may not be null for a^0");
            }
            return fac.getONE();
        }
        if (a.isONE()) {
            return a;
        }
        C b = a.remainder(m);
        if (n.signum() < 0) {
            b = a.inverse().remainder(m);
            n = n.negate();
        }
        if (n.compareTo(java.math.BigInteger.ONE) == 0) {
            return b;
        }
        C p = fac.getONE();
        java.math.BigInteger i = n;
        do {
            if (i.testBit(0)) {
                p = p.multiply(b).remainder(m);
            }
            i = i.shiftRight(1);
            if (i.signum() > 0) {
                b = b.multiply(b).remainder(m);
            }
        } while (i.signum() > 0);

        return p;
    }


    /**
     * power of a to the n-th.
     *
     * @param a element.
     * @param n integer exponent.
     * @return a^n, with 0^0 = 0.
     */
    public C power(C a, long n) {
        return power(fac, a, n);
    }


    /**
     * power of a to the n-th mod m.
     *
     * @param a element.
     * @param n integer exponent.
     * @param m modulus.
     * @return a^n mod m, with 0^0 = 0.
     */
    public C modPower(C a, java.math.BigInteger n, C m) {
        return modPower(fac, a, n, m);
    }


    /**
     * Logarithm.
     *
     * @param p logarithm base.
     * @param a element.
     * @return k &ge; 1 minimal with p^k &ge; b.
     */
    public static <C extends RingElem<C>> long logarithm(C p, C a) {
        //if ( p.compareTo(a) < 0 ) {
        //    return 0L;
        //}
        long k = 1L;
        C m = p;
        while (m.compareTo(a) < 0) {
            m = m.multiply(p);
            k++;
        }
        return k;
    }


    /**
     * Multiply elements in list.
     *
     * @param A   list of elements (a_0,...,a_k).
     * @param fac ring factory.
     * @return prod(i=0, ...k) a_i.
     */
    public static <C extends RingElem<C>> C multiply(RingFactory<C> fac, List<C> A) {
        return multiply((MonoidFactory<C>) fac, A);
    }


    /**
     * Multiply elements in list.
     *
     * @param A   list of elements (a_0,...,a_k).
     * @param fac monoid factory.
     * @return prod(i=0, ...k) a_i.
     */
    public static <C extends MonoidElem<C>> C multiply(MonoidFactory<C> fac, List<C> A) {
        if (fac == null) {
            throw new IllegalArgumentException("fac may not be null for empty list");
        }
        C res = fac.getONE();
        if (A == null || A.isEmpty()) {
            return res;
        }
        for (C a : A) {
            res = res.multiply(a);
        }
        return res;
    }


    /**
     * Sum elements in list.
     *
     * @param A   list of elements (a_0,...,a_k).
     * @param fac ring factory.
     * @return sum(i=0, ...k) a_i.
     */
    public static <C extends RingElem<C>> C sum(RingFactory<C> fac, List<C> A) {
        return sum((AbelianGroupFactory<C>) fac, A);
    }


    /**
     * Sum elements in list.
     *
     * @param A   list of elements (a_0,...,a_k).
     * @param fac monoid factory.
     * @return sum(i=0, ...k) a_i.
     */
    public static <C extends AbelianGroupElem<C>> C sum(AbelianGroupFactory<C> fac, List<C> A) {
        if (fac == null) {
            throw new IllegalArgumentException("fac may not be null for empty list");
        }
        C res = fac.getZERO();
        if (A == null || A.isEmpty()) {
            return res;
        }
        for (C a : A) {
            res = res.sum(a);
        }
        return res;
    }

}
