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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Generic Complex ring factory implementing the RingFactory interface. Objects
 * of this class are immutable.
 *
 * @param <C> base type.
 * @author Heinz Kredel
 */
public class ComplexRing<C extends RingElem<C>> implements RingFactory<Complex<C>> {


    private final static Random random = new Random();


    @SuppressWarnings("unused")


    /**
     * Complex class elements factory data structure.
     */
    public final RingFactory<C> ring;


    /**
     * The constructor creates a ComplexRing object.
     *
     * @param ring factory for Complex real and imaginary parts.
     */
    public ComplexRing(RingFactory<C> ring) {
        this.ring = ring;
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<Complex<C>> generators() {
        List<C> gens = ring.generators();
        List<Complex<C>> g = new ArrayList<>(gens.size() + 1);
        for (C x : gens) {
            Complex<C> cx = new Complex<>(this, x);
            g.add(cx);
        }
        g.add(getIMAG());
        return g;
    }


    /**
     * Corresponding algebraic number ring.
     *
     * @return algebraic number ring.
     *         not jet possible.
     */
    public AlgebraicNumberRing<C> algebraicRing() {
        GenPolynomialRing<C> pfac
                = new GenPolynomialRing<>(ring, 1, new TermOrder(TermOrder.INVLEX), new String[]{"I"});
        GenPolynomial<C> I = pfac.univariate(0, 2L).sum(pfac.getONE());
        AlgebraicNumberRing<C> afac = new AlgebraicNumberRing<>(I, ring.isField()); // must indicate field
        return afac;
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return ring.isFinite();
    }


    /**
     * Copy Complex element c.
     *
     * @param c Complex&lt;C&gt;.
     * @return a copy of c.
     */
    public Complex<C> copy(Complex<C> c) {
        return new Complex<>(this, c.re, c.im);
    }


    /**
     * Get the zero element.
     *
     * @return 0 as Complex&lt;C&gt;.
     */
    public Complex<C> getZERO() {
        return new Complex<>(this);
    }


    /**
     * Get the one element.
     *
     * @return 1 as Complex&lt;C&gt;.
     */
    public Complex<C> getONE() {
        return new Complex<>(this, ring.getONE());
    }


    /**
     * Get the i element.
     *
     * @return i as Complex&lt;C&gt;.
     */
    public Complex<C> getIMAG() {
        return new Complex<>(this, ring.getZERO(), ring.getONE());
    }


    /**
     * Query if this ring is commutative.
     *
     * @return true.
     */
    public boolean isCommutative() {
        return ring.isCommutative();
    }


    /**
     * Query if this ring is associative.
     *
     * @return true.
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
        return ring.isField();
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
     * Get a Complex element from a BigInteger.
     *
     * @param a BigInteger.
     * @return a Complex&lt;C&gt;.
     */
    public Complex<C> fromInteger(BigInteger a) {
        return new Complex<>(this, ring.fromInteger(a));
    }


    /**
     * Get a Complex element from a long.
     *
     * @param a long.
     * @return a Complex&lt;C&gt;.
     */
    public Complex<C> fromInteger(long a) {
        return new Complex<>(this, ring.fromInteger(a));
    }


    /**
     * Get the String representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Complex[");
        sb.append(ring.toString());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object b) {
        if (!(b instanceof ComplexRing)) {
            return false;
        }
        ComplexRing<C> a = null;
        try {
            a = (ComplexRing<C>) b;
        } catch (ClassCastException e) {
        }
        if (a == null) {
            return false;
        }
        return ring.equals(a.ring);
    }


    /**
     * Hash code for this ComplexRing&lt;C&gt;.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ring.hashCode();
    }


    /**
     * Complex number random. Random base numbers A and B are generated using
     * random(n). Then R is the complex number with real part A and imaginary
     * part B.
     *
     * @param n such that 0 &le; A, B &le; (2<sup>n</sup>-1).
     * @return R.
     */
    public Complex<C> random(int n) {
        return random(n, random);
        //         C r = ring.random( n ).abs();
        //         C i = ring.random( n ).abs();
        //         return new Complex<C>(this, r, i );
    }


    /**
     * Complex number random. Random base numbers A and B are generated using
     * random(n). Then R is the complex number with real part A and imaginary
     * part B.
     *
     * @param n   such that 0 &le; A, B &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return R.
     */
    public Complex<C> random(int n, Random rnd) {
        C r = ring.random(n, rnd);
        C i = ring.random(n, rnd);
        return new Complex<>(this, r, i);
    }

}
