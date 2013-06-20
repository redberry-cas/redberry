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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.util;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * Cartesian product with iterator.
 *
 * @author Heinz Kredel
 */
public class CartesianProduct<E> implements Iterable<List<E>> {


    /**
     * data structure.
     */
    public final List<Iterable<E>> comps;


    /**
     * CartesianProduct constructor.
     *
     * @param comps components of the Cartesian product.
     */
    public CartesianProduct(List<Iterable<E>> comps) {
        if (comps == null) {
            throw new IllegalArgumentException("null components not allowed");
        }
        this.comps = comps;
    }


    //     /**
    //      * CartesianProduct constructor.
    //      * @param comps components of the Cartesian product.
    //      */
    //     public CartesianProduct(List<List<E>> comps) {
    //         this( listToIterable(comps)  );
    //     }


    /**
     * Get an iterator over subsets.
     *
     * @return an iterator.
     */
    public Iterator<List<E>> iterator() {
        return new CartesianProductIterator<>(comps);
    }


}


/**
 * Cartesian product iterator.
 *
 * @author Heinz Kredel
 */
class CartesianProductIterator<E> implements Iterator<List<E>> {


    /**
     * data structure.
     */
    final List<Iterable<E>> comps;


    final List<Iterator<E>> compit;


    List<E> current;


    boolean empty;


    /**
     * CartesianProduct iterator constructor.
     *
     * @param comps components of the Cartesian product.
     */
    public CartesianProductIterator(List<Iterable<E>> comps) {
        if (comps == null) {
            throw new IllegalArgumentException("null comps not allowed");
        }
        this.comps = comps;
        current = new ArrayList<>(comps.size());
        compit = new ArrayList<>(comps.size());
        empty = false;
        for (Iterable<E> ci : comps) {
            Iterator<E> it = ci.iterator();
            if (!it.hasNext()) {
                empty = true;
                current.clear();
                break;
            }
            current.add(it.next());
            compit.add(it);
        }
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
        List<E> res = new ArrayList<>(current);
        // search iterator which hasNext
        int i = compit.size() - 1;
        for (; i >= 0; i--) {
            Iterator<E> iter = compit.get(i);
            if (iter.hasNext()) {
                break;
            }
        }
        if (i < 0) {
            empty = true;
            return res;
        }
        // update iterators
        for (int j = i + 1; j < compit.size(); j++) {
            Iterator<E> iter = comps.get(j).iterator();
            compit.set(j, iter);
        }
        // update current
        for (int j = i; j < compit.size(); j++) {
            Iterator<E> iter = compit.get(j);
            E el = iter.next();
            current.set(j, el);
        }
        return res;
    }


    /**
     * Remove a tuple if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove tuples");
    }

}
