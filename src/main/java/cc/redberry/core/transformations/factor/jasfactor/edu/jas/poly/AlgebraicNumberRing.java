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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.poly;


import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingElem;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.RingFactory;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.CartesianProduct;
import cc.redberry.core.transformations.factor.jasfactor.edu.jas.util.CartesianProductInfinite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * Algebraic number factory class based on GenPolynomial with RingElem
 * interface. Objects of this class are immutable.
 *
 * @author Heinz Kredel
 */

public class AlgebraicNumberRing<C extends RingElem<C>> implements RingFactory<AlgebraicNumber<C>>,
        Iterable<AlgebraicNumber<C>> {


    /**
     * Ring part of the factory data structure.
     */
    public final GenPolynomialRing<C> ring;


    /**
     * Module part of the factory data structure.
     */
    public final GenPolynomial<C> modul;


    /**
     * Indicator if this ring is a field
     */
    protected int isField = -1; // initially unknown


    //  private final boolean debug = false;


    /**
     * The constructor creates a AlgebraicNumber factory object from a
     * GenPolynomial objects module.
     *
     * @param m module GenPolynomial<C>.
     */
    public AlgebraicNumberRing(GenPolynomial<C> m) {
        ring = m.ring;
        modul = m; // assert m != 0
        if (ring.nvar > 1) {
            throw new IllegalArgumentException("only univariate polynomials allowed");
        }
    }


    /**
     * The constructor creates a AlgebraicNumber factory object from a
     * GenPolynomial objects module.
     *
     * @param m       module GenPolynomial<C>.
     * @param isField indicator if m is prime.
     */
    public AlgebraicNumberRing(GenPolynomial<C> m, boolean isField) {
        ring = m.ring;
        modul = m; // assert m != 0
        this.isField = (isField ? 1 : 0);
        if (ring.nvar > 1) {
            throw new IllegalArgumentException("only univariate polynomials allowed");
        }
    }


    /**
     * Get the module part.
     *
     * @return modul.
     */
    public GenPolynomial<C> getModul() {
        return modul;
    }


    /**
     * Copy AlgebraicNumber element c.
     *
     * @param c algebraic number to copy.
     * @return a copy of c.
     */
    public AlgebraicNumber<C> copy(AlgebraicNumber<C> c) {
        return new AlgebraicNumber<>(this, c.val);
    }


    /**
     * Get the zero element.
     *
     * @return 0 as AlgebraicNumber.
     */
    public AlgebraicNumber<C> getZERO() {
        return new AlgebraicNumber<>(this, ring.getZERO());
    }


    /**
     * Get the one element.
     *
     * @return 1 as AlgebraicNumber.
     */
    public AlgebraicNumber<C> getONE() {
        return new AlgebraicNumber<>(this, ring.getONE());
    }


    /**
     * Get the generating element.
     *
     * @return alpha as AlgebraicNumber.
     */
    public AlgebraicNumber<C> getGenerator() {
        return new AlgebraicNumber<>(this, ring.univariate(0));
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<AlgebraicNumber<C>> generators() {
        List<GenPolynomial<C>> gc = ring.generators();
        List<AlgebraicNumber<C>> gens = new ArrayList<>(gc.size());
        for (GenPolynomial<C> g : gc) {
            gens.add(new AlgebraicNumber<>(this, g));
        }
        return gens;
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return ring.coFac.isFinite();
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
     * @return true if modul is prime, else false.
     */
    public boolean isField() {
        if (isField > 0) {
            return true;
        }
        if (isField == 0) {
            return false;
        }
        if (!ring.coFac.isField()) {
            isField = 0;
            return false;
        }
        return false;
    }


    /**
     * Set field property of this ring.
     *
     * @param field true, if this ring is determined to be a field, false, if it
     *              is determined that it is not a field.
     */
    public void setField(boolean field) {
        if (isField > 0 && field) {
            return;
        }
        if (isField == 0 && !field) {
            return;
        }
        if (field) {
            isField = 1;
        } else {
            isField = 0;
        }
    }


    /**
     * Get the internal field indicator.
     *
     * @return internal field indicator.
     */
    public int getField() {
        return isField;
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
     * Get a AlgebraicNumber element from a BigInteger value.
     *
     * @param a BigInteger.
     * @return a AlgebraicNumber.
     */
    public AlgebraicNumber<C> fillFromInteger(java.math.BigInteger a) {
        if (characteristic().signum() == 0) {
            return new AlgebraicNumber<>(this, ring.fromInteger(a));
        }
        java.math.BigInteger p = characteristic();
        java.math.BigInteger b = a;
        GenPolynomial<C> v = ring.getZERO();
        GenPolynomial<C> x = ring.univariate(0, 1L);
        GenPolynomial<C> t = ring.getONE();
        do {
            java.math.BigInteger[] qr = b.divideAndRemainder(p);
            java.math.BigInteger q = qr[0];
            java.math.BigInteger r = qr[1];
            GenPolynomial<C> rp = ring.fromInteger(r);
            v = v.sum(t.multiply(rp));
            t = t.multiply(x);
            b = q;
        } while (!b.equals(java.math.BigInteger.ZERO));
        AlgebraicNumber<C> an = new AlgebraicNumber<>(this, v);
        //RuntimeException e = new RuntimeException("hihihi");
        //e.printStackTrace();
        return an;
    }


    /**
     * Get a AlgebraicNumber element from a long value.
     *
     * @param a long.
     * @return a AlgebraicNumber.
     */
    public AlgebraicNumber<C> fillFromInteger(long a) {
        return fillFromInteger(new java.math.BigInteger("" + a));
    }


    /**
     * Get a AlgebraicNumber element from a BigInteger value.
     *
     * @param a BigInteger.
     * @return a AlgebraicNumber.
     */
    public AlgebraicNumber<C> fromInteger(java.math.BigInteger a) {
        return new AlgebraicNumber<>(this, ring.fromInteger(a));
    }


    /**
     * Get a AlgebraicNumber element from a long value.
     *
     * @param a long.
     * @return a AlgebraicNumber.
     */
    public AlgebraicNumber<C> fromInteger(long a) {
        return new AlgebraicNumber<>(this, ring.fromInteger(a));
        //         if ( characteristic().signum() == 0 ) {
        //         }
        //         return fromInteger( new java.math.BigInteger(""+a) );
    }


    /**
     * Get the String representation as RingFactory.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AlgebraicNumberRing[ " + modul.toString() + " | isField=" + isField + " :: "
                + ring.toString() + " ]";
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
        if (!(b instanceof AlgebraicNumberRing)) {
            return false;
        }
        AlgebraicNumberRing<C> a = null;
        try {
            a = (AlgebraicNumberRing<C>) b;
        } catch (ClassCastException e) {
        }
        if (a == null) {
            return false;
        }
        return modul.equals(a.modul);
    }


    /**
     * Hash code for this AlgebraicNumber.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return 37 * modul.hashCode() + ring.hashCode();
    }


    /**
     * AlgebraicNumber random.
     *
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random integer mod modul.
     */
    public AlgebraicNumber<C> random(int n) {
        GenPolynomial<C> x = ring.random(n).monic();
        return new AlgebraicNumber<>(this, x);
    }


    /**
     * AlgebraicNumber random.
     *
     * @param n   such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random integer mod modul.
     */
    public AlgebraicNumber<C> random(int n, Random rnd) {
        GenPolynomial<C> x = ring.random(n, rnd).monic();
        return new AlgebraicNumber<>(this, x);
    }


    /**
     * Depth of extension field tower.
     *
     * @return number of nested algebraic extension fields
     */
    @SuppressWarnings("unchecked")
    public int depth() {
        AlgebraicNumberRing<C> arr = this;
        int depth = 1;
        RingFactory<C> cf = arr.ring.coFac;
        if (cf instanceof AlgebraicNumberRing) {
            arr = (AlgebraicNumberRing<C>) cf;
            depth += arr.depth();
        }
        return depth;
    }

    /**
     * Total degree of nested extension fields.
     *
     * @return degree of tower of algebraic extension fields
     */
    @SuppressWarnings("unchecked")
    public long totalExtensionDegree() {
        long degree = modul.degree(0);
        AlgebraicNumberRing<C> arr = this;
        RingFactory<C> cf = arr.ring.coFac;
        if (cf instanceof AlgebraicNumberRing) {
            arr = (AlgebraicNumberRing<C>) cf;
            if (degree == 0L) {
                degree = arr.totalExtensionDegree();
            } else {
                degree *= arr.totalExtensionDegree();
            }
        }
        return degree;
    }


    /**
     * Get a AlgebraicNumber iterator. <b>Note: </b> Only for finite field
     * coefficients or fields which are iterable.
     *
     * @return a iterator over all algebraic numbers in this ring.
     */
    public Iterator<AlgebraicNumber<C>> iterator() {
        return new AlgebraicNumberIterator<>(this);
    }

}


/**
 * Algebraic number iterator.
 *
 * @author Heinz Kredel
 */
class AlgebraicNumberIterator<C extends RingElem<C>> implements Iterator<AlgebraicNumber<C>> {


    /**
     * data structure.
     */
    final Iterator<List<C>> iter;


    final List<GenPolynomial<C>> powers;


    final AlgebraicNumberRing<C> aring;


    //  private final boolean debug = false;


    /**
     * CartesianProduct iterator constructor.
     *
     * @param aring AlgebraicNumberRing components of the Cartesian product.
     */
    public AlgebraicNumberIterator(AlgebraicNumberRing<C> aring) {
        RingFactory<C> cf = aring.ring.coFac;
        this.aring = aring;
        long d = aring.modul.degree(0);
        powers = new ArrayList<>((int) d);
        for (long j = d - 1; j >= 0L; j--) {
            powers.add(aring.ring.univariate(0, j));
        }
        if (!(cf instanceof Iterable)) {
            throw new IllegalArgumentException("only for iterable coefficients implemented");
        }
        List<Iterable<C>> comps = new ArrayList<>((int) d);
        Iterable<C> cfi = (Iterable<C>) cf;
        for (long j = 0L; j < d; j++) {
            comps.add(cfi);
        }
        if (cf.isFinite()) {
            CartesianProduct<C> tuples = new CartesianProduct<>(comps);
            iter = tuples.iterator();
        } else {
            CartesianProductInfinite<C> tuples = new CartesianProductInfinite<>(comps);
            iter = tuples.iterator();
        }
    }


    /**
     * Test for availability of a next tuple.
     *
     * @return true if the iteration has more tuples, else false.
     */
    public boolean hasNext() {
        return iter.hasNext();
    }


    /**
     * Get next tuple.
     *
     * @return next tuple.
     */
    public AlgebraicNumber<C> next() {
        List<C> coeffs = iter.next();
        GenPolynomial<C> pol = aring.ring.getZERO();
        int i = 0;
        for (GenPolynomial<C> f : powers) {
            C c = coeffs.get(i++);
            if (c.isZERO()) {
                continue;
            }
            pol = pol.sum(f.multiply(c));
        }
        return new AlgebraicNumber<>(aring, pol);
    }


    /**
     * Remove a tuple if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove tuples");
    }

}
