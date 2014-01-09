/*
 * JAS: Java Algebra System.
 *
 * Copyright (c) 2000-2014:
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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.util;


import java.util.*;


/**
 * Cartesian product of infinite components with iterator. Works also for finite
 * iterables.
 *
 * @author Heinz Kredel
 */
public class CartesianProductInfinite<E> implements Iterable<List<E>> {


    /**
     * data structure.
     */
    public final List<Iterable<E>> comps;


    /**
     * CartesianProduct constructor.
     *
     * @param comps components of the Cartesian product.
     */
    public CartesianProductInfinite(List<Iterable<E>> comps) {
        if (comps == null || comps.size() == 0) {
            throw new IllegalArgumentException("null components not allowed");
        }
        this.comps = comps;
    }


    /**
     * Get an iterator over subsets.
     *
     * @return an iterator.
     */
    public Iterator<List<E>> iterator() {
        if (comps.size() == 1) {
            return new CartesianOneProductInfiniteIterator<>(comps.get(0));
        }
        //         if ( comps.size() == 2 ) { // this part is not realy required
        //             return new CartesianTwoProductInfiniteIterator<E>(comps.get(0),comps.get(1));
        //         }
        int n = comps.size();
        int k = n / 2 + n % 2; // ceiling
        Iterable<List<E>> c0 = new CartesianProductInfinite<>(comps.subList(0, k));
        Iterable<List<E>> c1 = new CartesianProductInfinite<>(comps.subList(k, n));
        return new CartesianTwoProductInfiniteIteratorList<>(c0, c1);
    }

}


/**
 * Cartesian product infinite iterator, one factor.
 *
 * @author Heinz Kredel
 */
class CartesianOneProductInfiniteIterator<E> implements Iterator<List<E>> {


    /**
     * data structure.
     */
    final Iterator<E> compit;


    /**
     * CartesianProduct iterator constructor.
     *
     * @param comps components of the cartesian product.
     */
    public CartesianOneProductInfiniteIterator(Iterable<E> comps) {
        if (comps == null) {
            throw new IllegalArgumentException("null comps not allowed");
        }
        compit = comps.iterator();
    }


    /**
     * Test for availability of a next tuple.
     *
     * @return true if the iteration has more tuples, else false.
     */
    public synchronized boolean hasNext() {
        return compit.hasNext();
    }


    /**
     * Get next tuple.
     *
     * @return next tuple.
     */
    public synchronized List<E> next() {
        List<E> res = new ArrayList<>(1);
        res.add(compit.next());
        return res;
    }


    /**
     * Remove a tuple if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove tuples");
    }

}


/**
 * Cartesian product infinite iterator, two factors list version.
 *
 * @author Heinz Kredel
 */
class CartesianTwoProductInfiniteIteratorList<E> implements Iterator<List<E>> {


    /**
     * data structure.
     */
    final Iterator<List<E>> compit0;


    final Iterator<List<E>> compit1;


    final List<List<E>> fincomps0;


    final List<List<E>> fincomps1;


    Iterator<List<E>> fincompit0;


    Iterator<List<E>> fincompit1;


    List<E> current;


    boolean empty;


    long level;


    /**
     * CartesianProduct iterator constructor.
     *
     * @param comps components of the Cartesian product.
     */
    public CartesianTwoProductInfiniteIteratorList(Iterable<List<E>> comps0, Iterable<List<E>> comps1) {
        if (comps0 == null || comps1 == null) {
            throw new IllegalArgumentException("null comps not allowed");
        }
        current = new ArrayList<>();
        empty = false;
        level = 0;

        compit0 = comps0.iterator();
        List<E> e = compit0.next();
        current.addAll(e);
        fincomps0 = new ArrayList<>();
        fincomps0.add(e);
        fincompit0 = fincomps0.iterator();

        compit1 = comps1.iterator();
        e = compit1.next();
        current.addAll(e);
        fincomps1 = new ArrayList<>();
        fincomps1.add(e);
        fincompit1 = fincomps1.iterator();
        //@SuppressWarnings("unused")

    }


    /**
     * Test for availability of a next tuple.
     *
     * @return true if the iteration has more tuples, else false.
     */
    public synchronized boolean hasNext() {
        return !empty;
    }


    /**
     * Get next tuple.
     *
     * @return next tuple.
     */
    public synchronized List<E> next() {
        if (empty) {
            throw new NoSuchElementException("invalid call of next()");
        }
        List<E> res = current; // new ArrayList<E>(current);
        if (fincompit0.hasNext() && fincompit1.hasNext()) {
            List<E> e0 = fincompit0.next();
            List<E> e1 = fincompit1.next();
            current = new ArrayList<>();
            current.addAll(e0);
            current.addAll(e1);
            return res;
        }
        level++;
        if (level % 2 == 1) {
            Collections.reverse(fincomps0);
        } else {
            Collections.reverse(fincomps1);
        }
        if (compit0.hasNext() && compit1.hasNext()) {
            fincomps0.add(compit0.next());
            fincomps1.add(compit1.next());
        } else {
            empty = true;
            return res;
        }
        if (level % 2 == 0) {
            Collections.reverse(fincomps0);
        } else {
            Collections.reverse(fincomps1);
        }
        fincompit0 = fincomps0.iterator();
        fincompit1 = fincomps1.iterator();
        List<E> e0 = fincompit0.next();
        List<E> e1 = fincompit1.next();
        current = new ArrayList<>();
        current.addAll(e0);
        current.addAll(e1);
        return res;
    }


    /**
     * Remove a tuple if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove tuples");
    }

}
