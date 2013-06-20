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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.arith;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


/**
 * ModIntegerRing factory with RingFactory interface. Effectively immutable.
 *
 * @author Heinz Kredel
 */

public final class ModIntegerRing implements ModularRingFactory<ModInteger>, Iterable<ModInteger> {


    /**
     * Module part of the factory data structure.
     */
    public final java.math.BigInteger modul;


    private final static Random random = new Random();


    /**
     * Indicator if this ring is a field.
     */
    private int isField = -1; // initially unknown


    /**
     * The constructor creates a ModIntegerRing object from a BigInteger object
     * as module part.
     *
     * @param m math.BigInteger.
     */
    public ModIntegerRing(java.math.BigInteger m) {
        modul = m;
    }


    /**
     * The constructor creates a ModIntegerRing object from a BigInteger object
     * as module part.
     *
     * @param m       math.BigInteger.
     * @param isField indicator if m is prime.
     */
    public ModIntegerRing(java.math.BigInteger m, boolean isField) {
        modul = m;
        this.isField = (isField ? 1 : 0);
    }

    /**
     * Get the module part.
     *
     * @return modul.
     */
    public java.math.BigInteger getModul() {
        return modul;
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
     * Create ModInteger element c.
     *
     * @param c
     * @return a ModInteger of c.
     */
    public ModInteger create(java.math.BigInteger c) {
        return new ModInteger(this, c);
    }


    /**
     * Create ModInteger element c.
     *
     * @param c
     * @return a ModInteger of c.
     */
    public ModInteger create(long c) {
        return new ModInteger(this, c);
    }

    /**
     * Copy ModInteger element c.
     *
     * @param c
     * @return a copy of c.
     */
    public ModInteger copy(ModInteger c) {
        return new ModInteger(this, c.val);
    }


    /**
     * Get the zero element.
     *
     * @return 0 as ModInteger.
     */
    public ModInteger getZERO() {
        return new ModInteger(this, java.math.BigInteger.ZERO);
    }


    /**
     * Get the one element.
     *
     * @return 1 as ModInteger.
     */
    public ModInteger getONE() {
        return new ModInteger(this, java.math.BigInteger.ONE);
    }


    /**
     * Get a list of the generating elements.
     *
     * @return list of generators for the algebraic structure.
     * @see cc.redberry.core.transformations.factor.jasfactor.edu.jas.structure.ElemFactory#generators()
     */
    public List<ModInteger> generators() {
        List<ModInteger> g = new ArrayList<>(1);
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
        // if ( modul.isProbablePrime(certainty) ) {
        if (modul.isProbablePrime(modul.bitLength())) {
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
        return modul;
    }


    /**
     * Get a ModInteger element from a BigInteger value.
     *
     * @param a BigInteger.
     * @return a ModInteger.
     */
    public ModInteger fromInteger(java.math.BigInteger a) {
        return new ModInteger(this, a);
    }


    /**
     * Get a ModInteger element from a long value.
     *
     * @param a long.
     * @return a ModInteger.
     */
    public ModInteger fromInteger(long a) {
        return new ModInteger(this, a);
    }


    /**
     * Get the String representation.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return " bigMod(" + modul.toString() + ")";
    }

    /**
     * Comparison with any other object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object b) {
        if (!(b instanceof ModIntegerRing)) {
            return false;
        }
        ModIntegerRing m = (ModIntegerRing) b;
        return (0 == modul.compareTo(m.modul));
    }


    /**
     * Hash code for this ModIntegerRing.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return modul.hashCode();
    }


    /**
     * ModInteger random.
     *
     * @param n such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @return a random integer mod modul.
     */
    public ModInteger random(int n) {
        return random(n, random);
    }


    /**
     * ModInteger random.
     *
     * @param n   such that 0 &le; v &le; (2<sup>n</sup>-1).
     * @param rnd is a source for random bits.
     * @return a random integer mod modul.
     */
    public ModInteger random(int n, Random rnd) {
        java.math.BigInteger v = new java.math.BigInteger(n, rnd);
        return new ModInteger(this, v);
    }


    /**
     * ModInteger chinese remainder algorithm. This is a factory method. Assert
     * c.modul >= a.modul and c.modul * a.modul = this.modul.
     *
     * @param c  ModInteger.
     * @param ci inverse of c.modul in ring of a.
     * @param a  other ModInteger.
     * @return S, with S mod c.modul == c and S mod a.modul == a.
     */
    public ModInteger chineseRemainder(ModInteger c, ModInteger ci, ModInteger a) {
        //if (false) { // debug
        //    if (c.ring.modul.compareTo(a.ring.modul) < 1) {
        //    }
        //}
        ModInteger b = a.ring.fromInteger(c.val); // c mod a.modul
        ModInteger d = a.subtract(b); // a-c mod a.modul
        if (d.isZERO()) {
            return fromInteger(c.val);
        }
        b = d.multiply(ci); // b = (a-c)*ci mod a.modul
        // (c.modul*b)+c mod this.modul = c mod c.modul = 
        // (c.modul*ci*(a-c)+c) mod a.modul = a mod a.modul
        java.math.BigInteger s = c.ring.modul.multiply(b.val);
        s = s.add(c.val);
        return fromInteger(s);
    }


    /**
     * Get a ModInteger iterator.
     *
     * @return a iterator over all modular integers in this ring.
     */
    public Iterator<ModInteger> iterator() {
        return new ModIntegerIterator(this);
    }

}


/**
 * Modular integer iterator.
 *
 * @author Heinz Kredel
 */
class ModIntegerIterator implements Iterator<ModInteger> {


    /**
     * data structure.
     */
    java.math.BigInteger curr;


    final ModIntegerRing ring;


    /**
     * ModInteger iterator constructor.
     *
     * @param fac modular integer factory;
     */
    public ModIntegerIterator(ModIntegerRing fac) {
        curr = java.math.BigInteger.ZERO;
        ring = fac;
    }


    /**
     * Test for availability of a next element.
     *
     * @return true if the iteration has more elements, else false.
     */
    public synchronized boolean hasNext() {
        return curr.compareTo(ring.modul) < 0;
    }


    /**
     * Get next integer.
     *
     * @return next integer.
     */
    public synchronized ModInteger next() {
        ModInteger i = new ModInteger(ring, curr);
        curr = curr.add(java.math.BigInteger.ONE);
        return i;
    }


    /**
     * Remove an element if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove elements");
    }
}
