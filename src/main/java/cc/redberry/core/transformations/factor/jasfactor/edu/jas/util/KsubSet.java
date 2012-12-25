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

package cc.redberry.core.transformations.factor.jasfactor.edu.jas.util;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * K-Subset with iterator.
 *
 * @author Heinz Kredel
 */
public class KsubSet<E> implements Iterable<List<E>> {


    /**
     * data structure.
     */
    public final List<E> set; // Iterable<E> also ok


    public final int k;


    /**
     * KsubSet constructor.
     *
     * @param set generating set.
     * @param k   size of subsets.
     */
    public KsubSet(List<E> set, int k) {
        if (set == null) {
            throw new IllegalArgumentException("null set not allowed");
        }
        if (k < 0 || k > set.size()) {
            throw new IllegalArgumentException("k out of range");
        }
        this.set = set;
        this.k = k;
    }


    /**
     * Get an iterator over subsets.
     *
     * @return an iterator.
     */
    public Iterator<List<E>> iterator() {
        if (k == 0) {
            return new ZeroSubSetIterator<>(set);
        }
        if (k == 1) {
            return new OneSubSetIterator<>(set);
        }
        return new KsubSetIterator<>(set, k);
    }

}


/**
 * Power set iterator.
 *
 * @author Heinz Kredel
 */
class KsubSetIterator<E> implements Iterator<List<E>> {


    /**
     * data structure.
     */
    public final List<E> set;


    public final int k;


    final List<E> rest;


    private E current;


    private Iterator<List<E>> recIter;


    private final Iterator<E> iter;


    /**
     * KsubSetIterator constructor.
     *
     * @param set generating set.
     * @param k   subset size.
     */
    public KsubSetIterator(List<E> set, int k) {
        if (set == null || set.size() == 0) {
            throw new IllegalArgumentException("null or empty set not allowed");
        }
        if (k < 2 || k > set.size()) {
            throw new IllegalArgumentException("k out of range");
        }
        this.set = set;
        this.k = k;
        iter = this.set.iterator();
        current = iter.next();
        rest = new LinkedList<>(this.set);
        rest.remove(0);
        if (k == 2) {
            recIter = new OneSubSetIterator<>(rest);
        } else {
            recIter = new KsubSetIterator<>(rest, k - 1);
        }
    }


    /**
     * Test for availability of a next subset.
     *
     * @return true if the iteration has more subsets, else false.
     */
    public boolean hasNext() {
        return recIter.hasNext() || (iter.hasNext() && rest.size() >= k);
    }


    /**
     * Get next subset.
     *
     * @return next subset.
     */
    public List<E> next() {
        if (recIter.hasNext()) {
            List<E> next = new LinkedList<>(recIter.next());
            next.add(0, current);
            return next;
        }
        if (iter.hasNext()) {
            current = iter.next();
            rest.remove(0);
            if (rest.size() < k - 1) {
                throw new NoSuchElementException("invalid call of next()");
            }
            if (k == 2) {
                recIter = new OneSubSetIterator<>(rest);
            } else {
                recIter = new KsubSetIterator<>(rest, k - 1);
            }
            return this.next(); // retry
        }
        throw new NoSuchElementException("invalid call of next()");
    }


    /**
     * Remove the last subset returned from underlying set if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove subsets");
    }

}


/**
 * One-subset iterator.
 *
 * @author Heinz Kredel
 */
class OneSubSetIterator<E> implements Iterator<List<E>> {


    /**
     * data structure.
     */
    public final List<E> set;


    private Iterator<E> iter;


    /**
     * OneSubSetIterator constructor.
     *
     * @param set generating set.
     */
    public OneSubSetIterator(List<E> set) {
        this.set = set;
        if (set == null || set.size() == 0) {
            iter = null;
            return;
        }
        iter = this.set.iterator();
    }


    /**
     * Test for availability of a next subset.
     *
     * @return true if the iteration has more subsets, else false.
     */
    public boolean hasNext() {
        if (iter == null) {
            return false;
        }
        return iter.hasNext();
    }


    /**
     * Get next subset.
     *
     * @return next subset.
     */
    public List<E> next() {
        List<E> next = new LinkedList<>();
        next.add(iter.next());
        return next;
    }


    /**
     * Remove the last subset returned from underlying set if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove subsets");
    }

}


/**
 * Zero-subset iterator.
 *
 * @author Heinz Kredel
 */
class ZeroSubSetIterator<E> implements Iterator<List<E>> {


    /**
     * data structure.
     */
    private boolean hasNext;


    /**
     * ZeroSubSetIterator constructor.
     *
     * @param set generating set (ignored).
     */
    public ZeroSubSetIterator(List<E> set) {
        hasNext = true;
    }


    /**
     * Test for availability of a next subset.
     *
     * @return true if the iteration has more subsets, else false.
     */
    public boolean hasNext() {
        return hasNext;
    }


    /**
     * Get next subset.
     *
     * @return next subset.
     */
    public List<E> next() {
        List<E> next = new LinkedList<>();
        hasNext = false;
        return next;
    }


    /**
     * Remove the last subset returned from underlying set if allowed.
     */
    public void remove() {
        throw new UnsupportedOperationException("cannnot remove subsets");
    }

}
