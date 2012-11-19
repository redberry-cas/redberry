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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * ModLongRing factory with RingFactory interface. Effectively immutable.
 *
 * @author Heinz Kredel
 */

public final class ModLongRing implements ModularRingFactory<ModLong>, Iterable<ModLong> {


    /**
     * Module part of the factory data structure.
     */
    public final long modul;


    /**
     * Random number generator.
     */
    private final static Random random = new Random();


    /**
     * Indicator if this ring is a field.
     */
    private int isField = -1; // initially unknown


    /*
     * Certainty if module is probable prime.
     */
    //private final int certainty = 10;


    /**
     * maximal representable integer.
     */
    public final static java.math.BigInteger MAX_LONG = new java.math.BigInteger(
            String.valueOf(Integer.MAX_VALUE)); // not larger!


    /**
     * The constructor creates a ModLongRing object from a long integer as
     * module part.
     *
     * @param m long integer.
     */
    public ModLongRing(long m) {
        modul = m;
    }


    /**
     * The constructor creates a ModLongRing object from a long integer as
     * module part.
     *
     * @param m       long integer.
     * @param isField indicator if m is prime.
     */
    public ModLongRing(long m, boolean isField) {
        modul = m;
        this.isField = (isField ? 1 : 0);
    }

    /**
     * The constructor creates a ModLongRing object from a BigInteger converted
     * to long as module part.
     *
     * @param m java.math.BigInteger.
     */
    public ModLongRing(java.math.BigInteger m) {
        this(m.longValue());
        if (MAX_LONG.compareTo(m) <= 0) { // m >= max
            throw new IllegalArgumentException("modul to large for long " + m);
        }
    }


    /**
     * The constructor creates a ModLongRing object from a BigInteger converted
     * to long as module part.
     *
     * @param m       java.math.BigInteger.
     * @param isField indicator if m is prime.
     */
    public ModLongRing(java.math.BigInteger m, boolean isField) {
        this(m.longValue(), isField);
        if (MAX_LONG.compareTo(m) <= 0) { // m >= max
            throw new IllegalArgumentException("modul to large for long " + m);
        }
    }


    /**
     * Get the module part as BigInteger.
     *
     * @return modul.
     */
    public BigInteger getIntegerModul() {
        return new BigInteger(modul);
    }


    /**
     * Create ModLong element c.
     *
     * @param c
     * @return a ModLong of c.
     */
    public ModLong create(java.math.BigInteger c) {
        return new ModLong(this, c);
    }


    /**
     * Create ModLong element c.
     *
     * @param c
     * @return a ModLong of c.
     */
    public ModLong create(long c) {
        return new ModLong(this, c);
    }


    /**
     * Copy ModLong element c.
     *
     * @param c
     * @return a copy of c.
     */
    public ModLong copy(ModLong c) {
        return new ModLong(this, c.val);
    }


    /**
     * Get the zero element.
     *
     * @return 0 as ModLong.
     */
    public ModLong getZERO() {
        return new ModLong(this, 0L);
    }


    /**
     * Get the one element.
     *
     * @return 1 as ModLong.
     */
    public ModLong getONE() {
        return new ModLong(this, 1L);
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<ModLong> generators() {
        List<ModLong> g = new ArrayList<>(1);
        g.add(getONE());
        return g;
    }


    /**
     * Is this structure finite or infinite.
     *
     * @return true if this structure is finite, else false.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#isFinite()
     */
    public boolean isFinite() {
        return true;
    }


    /**
     * Query if this ring is commutative.
     *
     * @return true.
     */
    public boolean isCommutative() {
        return true;
    }


    /**
     * Query if this ring is associative.
     *
     * @return true.
     */
    public boolean isAssociative() {
        return true;
    }


    /**
     * Query if this ring is a field.
     *
     * @return true if module is prime, else false.
     */
    public boolean isField() {
        if (isField > 0) {
            return true;
        }
        if (isField == 0) {
            return false;
        }
        java.math.BigInteger m = new java.math.BigInteger("" + modul);
        if (m.isProbablePrime(m.bitLength())) {
            isField = 1;
            return true;
        }
        isField = 0;
        return false;
    }


    /**
     * Characteristic of this ring.
     *
     * @return characteristic of this ring.
     */
    public java.math.BigInteger characteristic() {
        return new java.math.BigInteger("" + modul);
    }


    /**
     * Get a ModLong element from a BigInteger value.
     *
     * @param a BigInteger.
     * @return a ModLong.
     */
    public ModLong fromInteger(java.math.BigInteger a) {
        return new ModLong(this, a);
    }


    /**
     * Get a ModLong element from a long value.
     *
     * @param a long.
     * @return a ModLong.
     */
    public ModLong fromInteger(long a) {
        return new ModLong(this, a);
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return " mod(" + modul + ")"; //",max="  + MAX_LONG + ")";
    }


    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof ModLongRing)) {
            return false;
        }
        ModLongRing m = (ModLongRing) b;
        return (modul == m.modul);
    }


    /**
     * Hash code for this ModLongRing.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) modul;
    }


    /**
     * ModLong random.
     *
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random integer mod modul.
     */
    public ModLong random(int n) {
        return random(n, random);
    }


    /**
     * ModLong random.
     *
     * @param n   such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random integer mod modul.
     */
    public ModLong random(int n, Random rnd) {
        java.math.BigInteger v = new java.math.BigInteger(n, rnd);
        return new ModLong(this, v); // rnd.nextLong() not ok
    }


    /**
     * ModLong chinese remainder algorithm. This is a factory method. Assert
     * c.modul >= a.modul and c.modul * a.modul = this.modul.
     *
     * @param c  ModLong.
     * @param ci inverse of c.modul in ring of a.
     * @param a  other ModLong.
     * @return S, with S mod c.modul == c and S mod a.modul == a.
     */
    public ModLong chineseRemainder(ModLong c, ModLong ci, ModLong a) {
        if (c.ring.modul < a.ring.modul) {
        }
        ModLong b = a.ring.fromInteger(c.val); // c mod a.modul
        ModLong d = a.subtract(b); // a-c mod a.modul
        if (d.isZERO()) {
            return new ModLong(this, c.val);
        }
        b = d.multiply(ci); // b = (a-c)*ci mod a.modul
        // (c.modul*b)+c mod this.modul = c mod c.modul = 
        // (c.modul*ci*(a-c)+c) mod a.modul = a mod a.modul
        long s = c.ring.modul * b.val;
        s = s + c.val;
        return new ModLong(this, s);
    }


    /**
     * Get a ModLong iterator.
     *
     * @return a iterator over all modular integers in this ring.
     */
    public Iterator<ModLong> iterator() {
        return new ModLongIterator(this);
    }

}


/**
 * Modular integer iterator.
 *
 * @author Heinz Kredel
 */
class ModLongIterator implements Iterator<ModLong> {


    /**
     * data structure.
     */
    long curr;


    final ModLongRing ring;


    /**
     * ModLong iterator constructor.
     *
     * @param fac modular integer factory;
     */
    public ModLongIterator(ModLongRing fac) {
        curr = 0L;
        ring = fac;
    }


    /**
     * Test for availability of a next element.
     *
     * @return true if the iteration has more elements, else false.
     */
    public synchronized boolean hasNext() {
        return curr < ring.modul;
    }


    /**
     * Get next integer.
     *
     * @return next integer.
     */
    public synchronized ModLong next() {
        ModLong i = new ModLong(ring, curr);
        curr++;
        return i;
    }


    /**
     * Remove an element if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove elements");
    }
}
